import os
import gzip
import numpy
import scipy.io
from Algorithm import *
from ReadConfig import *
from ReadData import *
from WriteConfig import *
from DataStore import *
from Network import *
from datetime import datetime
from shutil import copytree

class NIRest(Algorithm):
    def __init__(self):

        #os.chdir("/Users/Jesse/Workspace/School/MastersThesis/Program")
        self.alg_name = "nirest"

    def run_vt(self, config_file, tau, lambda_w, eta_z):
        settings = ReadConfig({}, "./config/default_values/nirest.cfg")
        settings = ReadConfig(settings, config_file)
        settings["global"]["working_dir"] = os.getcwd() + '/'


        input_files = settings["global"]["time_series_files"]
        tfs = settings["nirest"]["tfs_file"]
        delta_t = [50]*20
        settings["global"]["time_series_delta_t"] = delta_t
        t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")

        name = "nirest_vt_run_" + t

        timeseries_storage = ReadData(input_files, True)
        trans_factors = TFList(timeseries_storage[0].gene_list[0:6])

        self.setup(timeseries_storage, trans_factors, settings, name)

        # Set up our hyperparameters with the parameters from VT
        settings["nirest"]["tau"] = tau
        settings["nirest"]["lambda_w"] = lambda_w
        settings["nirest"]["eta_z"] = eta_z

        self.run_local(settings)


    def setup(self, input_files, settings, name=None, time_mask=None, allowed_connections=[]):

      """ This function sets up NIRest based on the input parameters above.
       For setting something up with just a config file, use setup_config """
      if name == None:
          self.name = "nirest"
      else:
          self.name = name
      self.alg_name = "nirest"
      self.exp_data = None
      self.input_files = input_files

      t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")

      # Read in the default values for nirest, we'll override these later
      settings = ReadConfig(settings, "./config/default_values/nirest.cfg")
      settings = ReadConfig(settings, settings["nirest"]["config"])
      #self.read_data(settings)

      # Create the nirest directory in the output folder
      self.output_dir = settings["global"]["working_dir"] + \
              settings["global"]["output_dir"] + "/" + self.name + "/"
      if not os.path.isdir(self.output_dir):
          os.mkdir(self.output_dir)
      if not os.path.isdir(self.output_dir + "/output/"):
          os.mkdir(self.output_dir + "/output/")

      # Create the nirest data directory
      self.data_dir = self.output_dir + "/data/"
      if not os.path.isdir(self.data_dir):
          os.mkdir(self.data_dir)

      # Create the nirest log directory
      self.log_dir = self.output_dir + "/log/"
      if not os.path.isdir(self.log_dir):
          os.mkdir(self.log_dir)

      # Copy the entire working directory into our new directory.
      # We want to keep track of everything that was run.
      self.working_dir = self.output_dir + "/nirest_program/"
      #if not os.path.isdir(self.working_dir):
        #os.mkdir(self.working_dir)
      print self.name + " is copying files that it needs before it begins..."
      copytree(settings["nirest"]["path"], self.working_dir, True)


      settings["nirest"]["output_dir"] = self.output_dir
      settings["nirest"]["log_dir"] = self.log_dir
      settings["nirest"]["data_dir"] = self.data_dir
      settings["nirest"]["working_dir_config"] = self.output_dir + '/nirest_program/' + settings["nirest"]["working_dir_config"]
      self.write_data(input_files, settings, time_mask)
      self.write_config(settings)

      self.cmd = "matlab -nodesktop -nodisplay -nosplash -r \"addpath(genpath('./'));try;run_nirest, catch err, err ,end, exit\""
      self.cwd = self.working_dir

    def write_config(self, settings):
        # Code to write the config file on the fly before calling nirest
      # This will allow us to use the stock implementation of nirest
      WriteConfig("nirest", settings)

      # Now we have to write a symlink to this file into the nirest
       # directory.  This is because nirest must be run from its directory
       # because it relies on relative paths.

      if os.path.isfile(settings["nirest"]["working_dir_config"]) or \
              os.path.islink(settings["nirest"]["working_dir_config"]):
                  os.remove(settings["nirest"]["working_dir_config"])
      os.symlink( settings["nirest"]["settings_file"], \
              settings["nirest"]["working_dir_config"])

    def write_data(self, file, settings, time_mask=None):
          # This will write out the data files that nirest is going to read in
       # and set up the input folder.
       settings["nirest"]["ratio_files"] = ""

       #   print "ERROR: The time mask length does not match the number of \
               #           experiments for ratio files: "
       #   print file.experiments[0].name
       #   exit(1)

       data_file = open(self.data_dir + "/" + file.input_files[0], 'w')
       # Write out the header
       exp_names = ""
       for j, e in enumerate(file.experiments):
           # Check the time mask to make sure we are printing this one
          if time_mask != None and time_mask[j] == 0:
              continue

          if type(e.name) == type([]):
              for n in e.name:
                  exp_names += n + "\t"
          else:
              exp_names += e.name + "\t"
       data_file.write(exp_names.strip("\t"))
       data_file.write("\n")

       # Now write out each gene's name and its values across experiments
       for gene in file.gene_list:
           gene_row = ""
           for j,e in enumerate(file.experiments):
               # Check the time mask to make sure we are printing this one
               try:
                   gene_row += str(e.ratios[gene]) + "\t"
               except:
                   gene_row += "\t"
           data_file.write(gene_row.strip('\t') + "\n")
       self.gene_list = file.gene_list
       settings["nirest"]["ratio_files"] += self.data_dir\
               + "/" + file.input_files[0] + " "
       print settings["nirest"]["ratio_files"]
       data_file.flush()
       data_file.close()
       r = settings["nirest"]["ratio_files"]
       r = r[0:len(r)-1]
       settings["nirest"]["ratio_files"] = r




    def read_output(self,settings):
        # Code to write for collecting the output files from the algorithm, writes to the
      # output list in the object
      # What we want to do here is get the prediction rate on the last time
      # point and the network so we can compare it against a gold std.
      output_file = self.output_dir + "/output/" + \
              "/nirest_output.txt"

      net = Network()
      net.read_netmatrix_file(output_file, self.gene_list)
      self.network = net

    def write_output(self,settings):
      pass




    def read_data(self, settings):
      self.exp_data = MicroarrayData()
      self.exp_data.read_input(settings["nirest"]["ratio_file"])


    ####################################################################################
    # This is the code so we can run it from VisTrails or as a command line module
    ####################################################################################
    def run(self, ko_file, wt_file, ts_file=None, kd_file=None, name=None):
        import numpy

        os.chdir(os.environ["gene_path"])

        print "Reading in knockout data"
        knockout_storage = ReadData(ko_file, "knockout")
        knockout_storage.normalize()
        wildtype_storage = ReadData(wt_file, "wildtype")
        wildtype_storage.normalize()
        knockdown_storage = ReadData(kd_file, "knockdown")
        knockdown_storage.normalize()

        wildtype_storage.combine(knockdown_storage)

        timeseries_storage = None
        if ts_file != None:
            timeseries_storage = ReadData(ts_file, "timeseries")
            for ts in timeseries_storage:
                ts.normalize()

        settings = {}
        settings = ReadConfig(settings)
        # TODO: CHANGE ME
        settings["global"]["working_dir"] = os.getcwd() + '/'

        # Setup job manager
        print "Starting new job manager"
        jobman = JobManager(settings)

        # Make MCZ jobs
        mczjob = MCZ()
        mczjob.setup(knockout_storage, wildtype_storage, settings, timeseries_storage, name)

        print "Queuing job..."
        jobman.queueJob(mczjob)

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

        return jobnet.original_network

if __name__ == "__main__":
    import sys
    if len(sys.argv) < 3:
        print "Error: Not enough arguments.\nSyntax: python mcz.py knockout_file wildtype_file timeseries_file(optional) name(optional)"
        exit()

    ko_file = sys.argv[1]
    wt_file = sys.argv[2]

    if len(sys.argv) > 3:
        ts_file = sys.argv[3]
    else:
        ts_file = None

    if len(sys.argv) > 4:
        kd_file = sys.argv[4]
    else:
        kd_file = None

    name = ""
    if len(sys.argv) > 5:
        name = sys.argv[5]
    else:
        name = None
    mcz = MCZ()
    mcz.run(ko_file, wt_file, ts_file, kd_file, name)

