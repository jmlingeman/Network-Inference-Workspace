#!/usr/bin/python

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
from datetime import datetime
from JobManager import *

class Inferelator(Algorithm):
    def __init__(self):

        #os.chdir("/Users/Jesse/Workspace/School/MastersThesis/Program")
        self.alg_name = "inferelator"


    def setup(self, ko_storage, wt_storage, settings, ts_storage=None, other_storage=None, name=None):

      """ This function sets up inferelator based on the input parameters above.
       For setting something up with just a config file, use setup_config """
      if name == None:
          self.name = "inferelator-" + datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
      else:
          self.name = name

      settings = ReadConfig(settings, "./config/default_values/inferelator.cfg")
      settings = ReadConfig(settings, settings["inferelator"]["config"])
      self.create_directories(settings)

      self.exp_data = None
      self.ko_storage = ko_storage
      self.other_storage = other_storage
      self.ts_storage = ts_storage
      self.wt_storage = wt_storage

      if ts_storage != None:
        self.write_data(ts_storage, settings, "timeseries")
      else:
        settings["inferelator"]["timeseries"] = "None"
      if ko_storage != None:
          self.write_data(ko_storage, settings, "knockout")
          num_ko_exp = len(ko_storage.experiments)
      else:
          settings["inferelator"]["knockout"] = "None"
          num_ko_exp = 0

      if wt_storage != None:
        self.write_data(wt_storage, settings, "wildtype")
        num_wt_exp = len(wt_storage.experiments)
      else:
          settings["inferelator"]["wildtype"] = "None"
          num_wt_exp = 0
      self.gene_list = ts_storage[0].gene_list

      # Read in the default values for inferelator, we'll override these later
      #self.read_data(settings)


      num_steadystate_exp = num_wt_exp + num_ko_exp
      if other_storage != None:
          num_steadystate_exp += len(other_storage.experiments)
          self.write_data(other_storage, settings, "other")
      else:
          settings["inferelator"]["other"] = "None"
      settings[self.alg_name]["num_steadystate_exp"] = num_steadystate_exp
      self.settings = settings
      self.write_config(settings)

      self.cmd = "Rscript r_scripts/main.R"
      #self.cmd = "python inferelator.py {0} {1} {2}".format(settings["inferelator"]["wildtype"], settings["inferelator"]["knockout"], settings["inferelator"]["output_dir"] + "/output/inferelator_output.txt")
      print self.cmd
      #exit()
      self.cwd = self.working_dir

    def write_config(self, settings):
        # Code to write the config file on the fly before calling inferelator
      # This will allow us to use the stock implementation of inferelator
      WriteConfig("inferelator", settings)

      # Now we have to write a symlink to this file into the inferelator
       # directory.  This is because inferelator must be run from its directory
       # because it relies on relative paths.

      if os.path.isfile(settings["inferelator"]["working_dir_config"]) or \
              os.path.islink(settings["inferelator"]["working_dir_config"]):
                  os.remove(settings["inferelator"]["working_dir_config"])
      os.symlink( settings["inferelator"]["settings_file"], \
              settings["inferelator"]["working_dir_config"])

    def write_data(self, storage, settings, filetype):
       # This will write out the data files that inferelator is going to read in
       # and set up the input folder.

       import WriteData
       if filetype == "timeseries":
        settings["inferelator"][filetype] = filename = self.data_dir \
            + "/" + storage[0].input_files[0]
       else:
           settings["inferelator"][filetype] = filename = self.data_dir\
               + "/" + storage.input_files[0]
       WriteData.WriteDream4(storage, settings, filename)



    def read_output(self,settings):
        # Code to write for collecting the output files from the algorithm, writes to the
      # output list in the object
      # What we want to do here is get the prediction rate on the last time
      # point and the network so we can compare it against a gold std.
      # This file is a bunch of zscores, so we have to load the cutoff we want
      output_file = self.output_dir + "/output/inferelator_output.csv"

      net = Network()
      net.read_netmatrix_file(output_file, self.gene_list)
      self.network = net

      return self.network






    def write_output(self,settings):
      pass




    def read_data(self, settings):
      self.exp_data = MicroarrayData()
      self.exp_data.read_input(settings["inferelator"]["ratio_file"])


    ####################################################################################
    # This is the code so we can run it from VisTrails or as a command line module
    ####################################################################################
    def run(self, kofile, tsfile, wtfile, datafiles, name, goldnet_file, normalize=False):
        os.chdir(os.environ["gene_path"])
        knockout_storage = ReadData(kofile, "knockout")
        print "Reading in knockout data"
        wildtype_storage = ReadData(wtfile, "steadystate")

        if datafiles == []:
          other_storage = None
        else:
          other_storage = ReadData(datafiles[0], "steadystate")
          for file in datafiles[1:]:
              other_storage.combine(ReadData(file, "steadystate"))

        timeseries_storage = None
        if tsfile != None:
            timeseries_storage = ReadData(tsfile, "timeseries")
            #for ts in timeseries_storage:
                #ts.normalize()

        #if normalize:
            #knockout_storage.normalize()
            #wildtype_storage.normalize()
            #other_storage.normalize()


        settings = {}
        settings = ReadConfig(settings)
        # TODO: CHANGE ME
        settings["global"]["working_dir"] = os.getcwd() + '/'

        # Setup job manager
        print "Starting new job manager"
        jobman = JobManager(settings)

        # Make inferelator jobs
        inferelatorjob = inferelator()
        inferelatorjob.setup(knockout_storage, wildtype_storage, settings, timeseries_storage, other_storage, name)

        print "Queuing job..."
        jobman.queueJob(inferelatorjob)

        print jobman.queue
        print "Running queue..."
        jobman.runQueue()
        jobman.waitToClear()

        print "Queue finished"
        job = jobman.finished[0]
        #print job.alg.gene_list
        #print job.alg.read_output(settings)
        jobnet = job.alg.network
        #print "PREDICTED NETWORK:"
        #print job.alg.network.network
        print jobnet.original_network

        if goldnet_file != None:
            goldnet = Network()
            goldnet.read_goldstd(goldnet_file)
            #print "GOLD NETWORK:"
            #print goldnet.network
            #print jobnet.analyzeMotifs(goldnet).ToString()
            print jobnet.calculateAccuracy(goldnet)
            import AnalyzeResults
            tprs, fprs, rocs = AnalyzeResults.GenerateMultiROC(jobman.finished, goldnet )
            ps, rs, precs = AnalyzeResults.GenerateMultiPR(jobman.finished, goldnet)
            print "Area Under ROC"
            print rocs

            print "Area Under PR"
            print precs

        return jobnet.original_network

if __name__ == "__main__":
    import sys
    from optparse import OptionParser

    usage = "Usage: python inferelator.py --name NAME -g goldnetworkfile -t timeseriesfile -k knockoutfile steadystate_datafile1 steadystate_datafile2 ..."
    parser = OptionParser(usage)
    parser.add_option("-n", "--name", dest="name",
                  help="Give a name for this run (optional)", metavar="STRING")
    parser.add_option("-k", "--knockout", dest="kofile",
                  help="The knockout datafile", metavar="STRING")
    parser.add_option("-w", "--wildtype", dest="wtfile",
                  help="The wildtype datafile", metavar="STRING")
    parser.add_option("-t", "--timeseries", dest="tsfile",
            help="The timeseries file to analyze as steady state data.  (Note: because of the timeseries file format, this program can only handle one time series file.  Simply combining timeseries files into one beforehand should work just fine.) (optional)", metavar="STRING")
    parser.add_option("-g", "--goldnet", dest="goldnet_file",
                  help="The filename of the gold standard to compare to, if available. (optional)",
                  metavar="FILE")

    (options, args) = parser.parse_args()

    inferelator = inferelator()
    inferelator.run(options.kofile, options.tsfile, options.wtfile, args, options.name, options.goldnet_file)



