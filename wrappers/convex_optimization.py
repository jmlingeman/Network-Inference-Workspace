import os
import gzip
import numpy
import scipy.io
from Algorithm import *
from ReadConfig import *
from ReadData import *
from DataStore import *
from Network import *
from datetime import datetime
from shutil import copytree
from JobManager import *

class ConvexOptimization(Algorithm):
    def __init__(self):

        #os.chdir("/Users/Jesse/Workspace/School/MastersThesis/Program")
        self.alg_name = "convex_optimization"

        # Set supported datatypes
        self.datatypes = ["overexpression", "knockout", "multifactorial", "knockdown"]


    def setup(self, data_storage, settings, name=None, prior=None, init_mat=None, t=None):

      """ This function sets up Convext Optimization based on the input parameters above.
       For setting something up with just a config file, use setup_config """
      if name == None or name == "":
          self.name = "convex_optimization-" + datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
      else:
          self.name = name

      # Store the data in this object for later
      self.data_storage = data_storage

      # Read in the default values for convex_optimization, we'll override these later
      settings = ReadConfig(settings, "./config/default_values/convex_optimization.cfg")
      #settings = ReadConfig(settings, settings["convex_optimization"]["config"])

      if t != None:
          settings[self.alg_name]["param_t"] = t

      # Jump back out to the inherited methods to make the directories
      self.create_directories(settings)

      #self.read_data(settings)

      # Write the data to disk in the format the algorithm expects it
      self.write_data(data_storage, settings, data_storage.type)
      self.prior = prior
      if prior != None:
          self.write_prior(settings, prior)
      else:
          settings["convex_optimization"]["prior_file"] = ""
      if init_mat != None:
          pass
      else:
          settings["convex_optimization"]["init_mat_file"] = ""

      # Write the configuration file to disk.
      self.write_config(settings)

      # Now build the command
      # This algorithm uses matlab, so we need to get the matlab path.
      matlab_path = settings["global"]["matlab_path"]
      command = matlab_path + " -nodesktop -nodisplay -nosplash -r \"addpath(genpath('./'));try;run_convex_optimization, catch err, err, end, exit\""
      self.cmd = command
      print self.cmd


      self.cwd = self.working_dir

    def write_data(self, input_file, settings, filetype):
       # This will write out the data files that convex_optimization is going to read in
       # and set up the input folder.
       settings["convex_optimization"][filetype] = ""

       settings["convex_optimization"]["ratio_files"] = self.data_dir + "/" + input_file.input_files[0]
       data_file = open(self.data_dir + "/" + input_file.input_files[0], 'w')
       # Write out the header
       exp_names = ""
       for j, e in enumerate(input_file.experiments):
            exp_names += e.name + "\t"
            print e.name
       data_file.write(exp_names.strip("\t"))
       data_file.write("\n")

       # Now write out each gene's name and its values across experiments
       for gene in input_file.gene_list:
           gene_row = ""
           if filetype != "wildtype":
               gene_row = gene + "\t"

           for j,e in enumerate(input_file.experiments):
               try:
                   gene_row += str(e.ratios[gene]) + "\t"
               except:
                   gene_row += "\t"
           data_file.write(gene_row.strip('\t') + "\n")
       self.gene_list = input_file.gene_list
       settings["convex_optimization"][filetype] += self.data_dir\
               + "/" + input_file.input_files[0] + " "
       print settings["convex_optimization"][filetype]
       data_file.flush()
       data_file.close()

       r = settings["convex_optimization"][filetype]
       r = r[0:len(r)-1]
       settings["convex_optimization"][filetype] = r


    def read_output(self,settings):
      import scipy
      # Code to write for collecting the output files from the algorithm, writes to the
      # output list in the object
      # What we want to do here is get the prediction rate on the last time
      # point and the network so we can compare it against a gold std.
      # This file is a bunch of zscores, so we have to load the cutoff we want
      co_output = scipy.io.loadmat(self.output_dir + "/output/" + \
              "/convex_optimization_output.mat")

      self.raw_network = co_output["A"].tolist()


      net = Network()
      net.read_netmatrix(self.raw_network, self.gene_list)
      self.network = net

      return self.network


    def read_data(self, settings):
      self.exp_data = MicroarrayData()
      self.exp_data.read_input(settings["convex_optimization"]["ratio_file"])


    ####################################################################################
    # This is the code so we can run it from VisTrails or as a command line module
    ####################################################################################
    def run(self, name, datafiles, goldnet_file):
        import numpy

        os.chdir(os.environ["gene_path"])

        datastore = ReadData(datafiles[0], "steadystate")
        for file in datafiles[1:]:
            datastore.combine(ReadData(file, "steadystate"))
        datastore.normalize()

        settings = {}
        settings = ReadConfig(settings)
        # TODO: CHANGE ME
        settings["global"]["working_dir"] = os.getcwd() + '/'

        # Setup job manager
        print "Starting new job manager"
        jobman = JobManager(settings)

        # Make convex_optimization jobs
        co = ConvexOptimization()
        co.setup(datastore, settings, name)

        print "Queuing job..."
        jobman.queueJob(co)

        print jobman.queue
        print "Running queue..."
        jobman.runQueue()
        jobman.waitToClear()

        print "Queue finished"
        job = jobman.finished[0]
        print job.alg.gene_list
        print job.alg.read_output(settings)
        jobnet = job.alg.network
        print "PREDICTED NETWORK:"
        print job.alg.network.network
        print jobnet.original_network

        if goldnet_file != None:
            goldnet = Network()
            goldnet.read_goldstd(goldnet_file)
            print "GOLD NETWORK:"
            print goldnet.network
            print jobnet.analyzeMotifs(goldnet).ToString()
            print jobnet.calculateAccuracy(goldnet)

        return jobnet.original_network

if __name__ == "__main__":
    import sys
    from optparse import OptionParser

    usage = "Usage: python convex_optimization.py --name=NAME (optional) --prior prior_file (optional) datafile1 datafile2 ..."
    parser = OptionParser(usage)
    parser.add_option("-n", "--name", dest="name",
                  help="Give a name for this run", metavar="STRING")
    parser.add_option("-p", "--prior", dest="prior_file",
                  help="Path to a file containing the prior matrix", metavar="FILE")
    parser.add_option("-g", "--goldnet", dest="goldnet",
                  help="The filename of the gold standard to compare to, if available.",
                  metavar="FILE")

    (options, args) = parser.parse_args()

    if len(args) < 1:
        parser.error("At least one steady-state data file must be specified.")


    co = ConvexOptimization()
    print options.name
    co.run(options.name, args, options.goldnet)
