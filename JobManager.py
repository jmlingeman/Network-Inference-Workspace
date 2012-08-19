import time, os
from threading import Thread

class Job:
    alg = None
    id = None
    status = "Not started."
    settings = None

    def __init__(self, id, alg, settings):
        self.id = id
        self.alg = alg
        self.settings = settings

    def printStatus(self):
        print "Job ID: " + str(self.id) + "\t" + str(self.alg.name) + " is " + self.status

    def setStatusRunning(self):
        self.status = "Running."

    def setStatusError(self, retcode):
        self.status = "Error with retcode = " + str(retcode)

    def setStatusFinished(self):
        self.status = "Finished."

    def run_cluster(self):
        return self.alg.run_cluster(self.settings)

    def run_local(self):
        return self.alg.run_local(self.settings)

class JobThread(Thread):
    job = None # The job that this thread will run
    use_cluster = False # Are we using a cluster or not?
    jobman = None # This is the manager of this job

    def __init__(self, job, jobman, settings, use_cluster):
        Thread.__init__(self)
        self.job = job
        self.use_cluster = use_cluster
        self.jobman = jobman

    def run(self):
        if self.use_cluster:
            self.job.run_cluster()
        else:
            self.job.setStatusRunning()
            retcode = 0
            print "--------RUNNING JOB"
            if self.use_cluster:
                retcode = self.job.run_cluster()
            else:
                retcode = self.job.run_local()
            print "--------FINISHED JOB"

            self.jobman.running.remove(self.job)
            print retcode
            if retcode == 0:
                self.jobman.finished.append(self.job)
                self.job.setStatusFinished()
            else:
                self.jobman.error.append(self.job)
                self.job.setStatusError(retcode)



class JobManager:



    def __init__(self,settings):
        # spawn own thread, start up queue, start connection to server
        self.queue = []
        self.finished = []
        self.running = []
        self.error = []
        self.job_ids = []
        self.threads = []
        self.use_cluster = False
        self.connection = None
        self.curr_id = 0
        self.settings = None

        running_threads = 0
        max_threads = 1
        self.settings = settings
        if settings["global"]["use_cluster"] == True:
            import pbs
            self.use_cluster = True
        else:
            self.use_cluster = False
            self.max_threads = settings["global"]["n_processors"]

        if self.use_cluster:
            # Establish connection to PBS server
            serv_addr = settings["global"]["cluster_address"]

            # Let the cluster's jobman handle scheduling
            self.max_threads = sys.maxint
            self.connection = pbs.pbs_connect(serv_addr)
            if self.connection < 0:
                errno, text = pbs.error()
                print "Error, unable to establish connection to PBS server."
                print errno, text
                sys.exit(1)


    def queueJob(self,alg):
        # Add this job to the queue, returns job id
        j = Job(self.curr_id, alg, self.settings)
        self.queue.append(j)
        j.status = "In queue."
        self.curr_id += 1

        return j.id


    def runQueue(self):
        # Begin running the queue, launching on cluster or running on
        # local machine

        while len(self.queue) > 0:
            os.system("clear")
            self.reportStatus()
            while self.max_threads > len(self.running) and len(self.queue) > 0:

                # Run on cluster
                # launch script.  one thread per script launched.  that thread
                # then will wait for the process to finish, do whatever needs
                # tidying up, update the job and come back.  The entire queue can
                # be sent at once, as the cluster manager software will take care
                # of actual job scheduling.  Our threads just watch that particular
                # algorithm's folders for when it finishes.  This can be done by
                # passing this jobman object along to each thread.  Checking the
                # status of jobs should result in benign race conditions only.

                # Run locally, spawning a thread up until N threads.  These should
                # behave the same way as the cluster threads, but actually do the
                # work instead of idling.  Instead of blasting the entire cluster
                # at once, these will have to be delegated out by this program.
                job = self.queue.pop(0)
                self.running.append(job)
                #job.run_local()
                print "Creating new thread"
                jthread = JobThread(job, self, self.settings, self.use_cluster)
                print "Launching new thread"
                jthread.start()
                print "New thread launched."
                self.threads.append(jthread)
            time.sleep(5)


    def waitToClear(self, status=None):

        # Hold the incoming thread until the queue is clear
        while len(self.queue) + len(self.running) > 0:
            os.system("clear")
            if status != None:
              print "Status: {0}".format(status)
            print "Waiting for " + str(len(self.queue) + len(self.running)) + " jobs to finish."
            self.reportStatus()
            time.sleep(5)
        self.reportStatus()
        print "Queue finished."

    def clear(self):
      self.running = []
      self.queue = []
      self.finished = []
      self.error = []

    def reportStatus(self):
        # TODO: Have this print out statuses
        # Print status of all jobs in the queue/running/finished
        print "RUNNING JOBS:"
        for j in self.running:
            j.printStatus()

        print "QUEUED JOBS:"
        for j in self.queue:
            j.printStatus()

        print "FINISHED JOBS:"
        for j in self.finished:
            j.printStatus()

        print "ERROR JOBS:"
        for j in self.error:
            j.printStatus()


    def writeClusterScripts(self, alg, settings):
        # TODO: Make this check alg first, then global

        # Using the alg settings, write the cluster scripts
        # into the algorithm's main directory.
        algorithm_name = alg.alg_name

        settings[alg.alg_name]["cluster_prog_script"] = alg.cwd + "/cluster_run.sh"
        run_file = open(settings[alg.alg_name]["cluster_prog_script"], 'w')
        run_file.write(alg.cmd)
        run_file.flush()
        run_file.close()


        template_file = open("config/templates/cluster_queue.sh",'r')

        template = template_file.readlines()

        for i in xrange(len(template)):
            line = template[i]
            if "{{" in line and "}}" in line:
                # Loop through the line and replace all of the params with the
                # params from the settings dict.
                if line.count("{{") == line.count("}}"):
                    for p in xrange(line.count("{{")):
                        line = template[i]
                        param_str = line[line.index("{{"):line.index("}}")+2]
                        template[i] = template[i].replace(param_str, \
                            str(settings[algorithm_name][param_str[2:len(param_str)-2]]))
                else:
                    print "ERROR in template file.  Forgot {{ or }}?"

        queue_file = open(alg.cwd + "/cluster_queue.sh",'w')
        for l in template:
            queue_file.write(l)
        queue_file.flush()
        queue_file.close()





    def deleteJob(self,id):
        ind = self.queue.index(id, lambda a: a.id)

        # TODO: Use pbs to delete job

        self.queue.delete(ind)

        print "Job " + str(id) + " deleted."

    def jobStatus(self,id):
        ind = self.queue.index(id, lambda a: a.id)
        return self.queue[ind].status

