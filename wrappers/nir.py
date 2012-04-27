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

class NIR(Algorithm):
    def __init__(self):

        #os.chdir("/Users/Jesse/Workspace/School/MastersThesis/Program")
        self.alg_name = "nir"

    def setup(self, data_storage, settings, name=None, topd=None, restk=None, allowed_connections=[],):

      """ This function sets up nir based on the input parameters above.
       For setting something up with just a config file, use setup_config """
      if name == None:
          self.name = "nir-" + datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
      else:
          self.name = name

      self.exp_data = None
      self.data_storage = data_storage

      # Read in the default values for nir, we'll override these later
      settings = ReadConfig(settings, "./config/default_values/nir.cfg")
      settings = ReadConfig(settings, settings["nir"]["config"])
      if topd != None:
          settings["nir"]["topd"] = topd
      if restk != None:
          settings["nir"]["restk"] = restk
      #self.read_data(settings)

      self.create_directories(settings)

      self.write_data(data_storage, settings, "knockout")

      self.settings = settings
      self.write_config(settings)

      self.cmd = "matlab -nodesktop -nodisplay -nosplash -r \"addpath(genpath('./'));try;run_nir, catch err, err ,end, exit\""
      self.cwd = self.working_dir

    def write_config(self, settings):
        # Code to write the config file on the fly before calling nir
      # This will allow us to use the stock implementation of nir
      WriteConfig("nir", settings)

      # Now we have to write a symlink to this file into the nir
       # directory.  This is because nir must be run from its directory
       # because it relies on relative paths.

      if os.path.isfile(settings["nir"]["working_dir_config"]) or \
              os.path.islink(settings["nir"]["working_dir_config"]):
                  os.remove(settings["nir"]["working_dir_config"])
      os.symlink( settings["nir"]["settings_file"], \
              settings["nir"]["working_dir_config"])

    def write_data(self, file, settings, time_mask=None):
       # This will write out the data files that nir is going to read in
       # and set up the input folder.
       settings["nir"]["ratio_files"] = ""

       #   print "ERROR: The time mask length does not match the number of \
               #           experiments for ratio files: "
       #   print file.experiments[0].name
       #   exit(1)

       data_file = open(self.data_dir + "/" + file.input_files[0], 'w')
       # Write out the header
       exp_names = ""
       for j, e in enumerate(file.experiments):

          if type(e.name) == type([]):
              for n in e.name:
                  exp_names += n + "\t"
          else:
              exp_names += e.name + "\t"
       data_file.write(exp_names.strip("\t"))
       data_file.write("\n")

       # Now write out each gene's name and its values across experiments
       for gene in file.gene_list:
           gene_row = gene + "\t"
           for j,e in enumerate(file.experiments):
               # Check the time mask to make sure we are printing this one
               try:
                   gene_row += str(e.ratios[gene]) + "\t"
               except:
                   gene_row += "\t"
           data_file.write(gene_row.strip('\t') + "\n")
       self.gene_list = file.gene_list
       settings["nir"]["ratio_files"] += self.data_dir\
               + "/" + file.input_files[0] + " "
       print settings["nir"]["ratio_files"]
       data_file.flush()
       data_file.close()
       r = settings["nir"]["ratio_files"]
       r = r[0:len(r)-1]
       settings["nir"]["ratio_files"] = r




    def read_output(self,settings):
        # Code to write for collecting the output files from the algorithm, writes to the
      # output list in the object
      # What we want to do here is get the prediction rate on the last time
      # point and the network so we can compare it against a gold std.
      output_file = self.output_dir + "/output/" + \
              "/nir_output.txt"

      net = Network()
      net.read_netmatrix_file(output_file, self.gene_list)
      self.network = net

    def write_output(self,settings):
      pass




    def read_data(self, settings):
      self.exp_data = MicroarrayData()
      self.exp_data.read_input(settings["nir"]["ratio_file"])


    ####################################################################################
    # This is the code so we can run it from VisTrails or as a command line module
    ####################################################################################
    def run(self, datafiles=None, name=None, goldnet_file=None, topd=None, restk=None):
        import numpy

        os.chdir(os.environ["gene_path"])

        print "Reading in data"
        data_storage = ReadData(datafiles[0], "steadystate")
        for file in datafiles[1:]:
            data_storage.combine(ReadData(file, "steadystate"))

        settings = {}
        settings = ReadConfig(settings)
        # TODO: CHANGE ME
        settings["global"]["working_dir"] = os.getcwd() + '/'

        # Setup job manager
        print "Starting new job manager"
        jobman = JobManager(settings)

        # Make nir jobs
        nirjob = NIR()
        nirjob.setup(data_storage, settings, name, topd, restk)

        print "Queuing job..."
        jobman.queueJob(nirjob)

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

        if goldnet_file != None:
            goldnet = Network()
            goldnet.read_goldstd(goldnet_file)
            #print "GOLD NETWORK:"
            #print goldnet.network
            #print jobnet.analyzeMotifs(goldnet).ToString()
            print jobnet.calculateAccuracy(goldnet)
            import AnalyzeResults
            tprs, fprs, rocs = AnalyzeResults.GenerateMultiROC(jobman.finished, goldnet, True, job.alg.output_dir + "/ROC.pdf" )
            ps, rs, precs = AnalyzeResults.GenerateMultiPR(jobman.finished, goldnet, True, job.alg.output_dir + "/PR.pdf")
            print "Area Under ROC"
            print rocs

            print "Area Under PR"
            print precs

        return job.alg.network.network

if __name__ == "__main__":

    import sys
    from optparse import OptionParser

    usage = "Usage: python nir.py --name NAME -g goldnetworkfile steadystate_datafile1 steadystate_datafile2 ..."
    parser = OptionParser(usage)
    parser.add_option("-n", "--name", dest="name",
                  help="Give a name for this run (optional)", metavar="STRING")
    parser.add_option("-g", "--goldnet", dest="goldnet_file",
                  help="The filename of the gold standard to compare to, if available. (optional)", metavar="FILE")
    parser.add_option("-t", "--topd", help="Number of top networks to use in regression (default=5)", metavar="INT", dest="topd")
    parser.add_option("-r", "--restk", help="Maximum network connectiveness (default=10)", metavar="INT", dest="restk")


    (options, args) = parser.parse_args()

    nir = NIR()
    nir.run(args, options.name, options.goldnet_file, options.topd, options.restk)

