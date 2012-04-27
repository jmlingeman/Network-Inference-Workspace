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

class MCZ(Algorithm):
    def __init__(self):

        #os.chdir("/Users/Jesse/Workspace/School/MastersThesis/Program")
        self.alg_name = "mcz"


    def setup(self, ko_storage, wt_storage, settings, ts_storage=None, other_storage=None, name=None, time_mask=None, allowed_connections=[]):

      """ This function sets up MCZ based on the input parameters above.
       For setting something up with just a config file, use setup_config """
      if name == None:
          self.name = "mcz-" + datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
      else:
          self.name = name

      self.exp_data = None
      self.ko_storage = ko_storage
      self.other_storage = other_storage
      self.ts_storage = ts_storage
      self.wt_storage = wt_storage

      if ts_storage != None:
        tZero_storage = MicroarrayData("TimeZeros.tsv", "steadystate", ts_storage[0].gene_list)
        for t in ts_storage:
            tZero_storage.experiments.append(t.getTimePoint(0))
        self.tZero_storage = tZero_storage
      else:
        tZero_storage = None

      # Read in the default values for mcz, we'll override these later
      settings = ReadConfig(settings, "./config/default_values/mcz.cfg")
      settings = ReadConfig(settings, settings["mcz"]["config"])
      #self.read_data(settings)

      self.create_directories(settings)

      self.write_data(ko_storage, settings, "knockout")
      self.write_data(wt_storage, settings, "wildtype")
      if other_storage != None:
          self.write_data(other_storage, settings, "other")
      else:
          settings["mcz"]["other"] = "NULL"
      if tZero_storage != None:
          self.write_data(tZero_storage, settings, "tzero")
      else:
          settings["mcz"]["tzero"] = "NULL"

      self.settings = settings
      #self.write_config(settings)

      self.cmd = "Rscript mcz.R {0} {1} {2} {3}".format(settings["mcz"]["wildtype"], settings["mcz"]["knockout"], settings["mcz"]["other"], settings["mcz"]["tzero"])
      #self.cmd = "python mcz.py {0} {1} {2}".format(settings["mcz"]["wildtype"], settings["mcz"]["knockout"], settings["mcz"]["output_dir"] + "/output/mcz_output.txt")
      print self.cmd
      #exit()
      self.cwd = self.working_dir

    def write_config(self, settings):
        # Code to write the config file on the fly before calling mcz
      # This will allow us to use the stock implementation of mcz
      WriteConfig("mcz", settings)

      # Now we have to write a symlink to this file into the mcz
       # directory.  This is because mcz must be run from its directory
       # because it relies on relative paths.

      if os.path.isfile(settings["mcz"]["working_dir_config"]) or \
              os.path.islink(settings["mcz"]["working_dir_config"]):
                  os.remove(settings["mcz"]["working_dir_config"])
      os.symlink( settings["mcz"]["settings_file"], \
              settings["mcz"]["working_dir_config"])

    def write_data(self, storage, settings, filetype):
       # This will write out the data files that mcz is going to read in
       # and set up the input folder.
       settings["mcz"][filetype] = ""
       data_file = open(self.data_dir + "/" + storage.input_files[0], 'w')
       # Write out the header

       if filetype == "wildtype":
           exp_names = ""
           for gene in storage.gene_list:
                exp_names += gene + "\t"
           data_file.write(exp_names.strip("\t"))
           data_file.write("\n")
           for e in storage.experiments:
               gene_row = ""
               for gene in storage.gene_list:
                   gene_row += str(e.ratios[gene]) + "\t"
               data_file.write(gene_row.strip('\t') + "\n")

       else:

           if filetype != "wildtype" and filetype != "tzero":
               exp_names = ""
               for gene in storage.gene_list:
                    exp_names += gene + "\t"
               data_file.write(exp_names.strip("\t"))
               data_file.write("\n")

           if filetype == "tzero":
               exp_names = ""
               for e in storage.experiments:
                   exp_names += e.name + "\t"
               data_file.write(exp_names.strip("\t"))
               data_file.write("\n")
           # Now write out each gene's name and its values across experiments
           for gene in storage.gene_list:
               gene_row = ""
               gene_row = gene + "\t"

               for j,e in enumerate(storage.experiments):
                   try:
                       gene_row += str(e.ratios[gene]) + "\t"
                   except:
                       gene_row += "\t"
               data_file.write(gene_row.strip('\t') + "\n")
       self.gene_list = storage.gene_list
       settings["mcz"][filetype] += self.data_dir\
               + "/" + storage.input_files[0] + " "
       data_file.flush()
       data_file.close()

       r = settings["mcz"][filetype]
       r = r[0:len(r)-1]
       settings["mcz"][filetype] = r




    def read_output(self,settings):
        # Code to write for collecting the output files from the algorithm, writes to the
      # output list in the object
      # What we want to do here is get the prediction rate on the last time
      # point and the network so we can compare it against a gold std.
      # This file is a bunch of zscores, so we have to load the cutoff we want
      output_file = open(self.output_dir + "/output/mcz_output.txt", 'r')

      output_file = output_file.readlines()
      zscores = []
      for g1, line in enumerate(output_file[1:]):
          for g2, val in enumerate(line.split('\t')[1:]):
              zscores.append((self.gene_list[g2], self.gene_list[g1], abs(float(line.split()[1:][g2]))))
      topn = settings["mcz"]["top_n_edges"]
      #for line in output_file:
          #gene1, gene2, zscore = line.split()
          #zscore = float(zscore)
          #zscores.append((gene1, gene2, zscore))
      zscores = sorted(zscores, key=lambda zscore: abs(zscore[2]), reverse=True)

      self.zscores = zscores[:]
      network = []

      #for i,zscore in enumerate(zscores):
          #if i < topn:
            #gene1, gene2, zscore = zscore
            #if zscore > 0:
                #zscores[i] = (gene1, gene2, 1)
            #if zscore < 0:
                #zscores[i] = (gene1, gene2, -1)

          #else:
            #gene1, gene2, zscore = zscore
            #zscores[i] = (gene1, gene2, 0)
      net = Network()
      net.read_networklist(zscores)
      net.gene_list = self.gene_list
      self.network = net

      return self.network






    def write_output(self,settings):
      pass




    def read_data(self, settings):
      self.exp_data = MicroarrayData()
      self.exp_data.read_input(settings["mcz"]["ratio_file"])


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

        # Make MCZ jobs
        mczjob = MCZ()
        mczjob.setup(knockout_storage, wildtype_storage, settings, timeseries_storage, other_storage, name)

        print "Queuing job..."
        jobman.queueJob(mczjob)

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

    usage = "Usage: python mcz.py --name NAME -g goldnetworkfile -t timeseriesfile -k knockoutfile steadystate_datafile1 steadystate_datafile2 ..."
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

    mcz = MCZ()
    mcz.run(options.kofile, options.tsfile, options.wtfile, args, options.name, options.goldnet_file)



