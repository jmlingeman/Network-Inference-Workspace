#!/usr/bin/python

import sys
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
from datetime import datetime
from JobManager import *
import random

class Inferelator2(Algorithm):
    def __init__(self, settings):

        #os.chdir("/Users/Jesse/Workspace/School/MastersThesis/Program")
        self.alg_name = "inferelator2"
        settings = ReadConfig(settings, "./config/default_values/inferelator2.cfg")
        settings = ReadConfig(settings, settings["inferelator2"]["config"])


    def setup(self, ko_storage, wt_storage, settings, ts_storage=None, other_storage=None, name=None, gold_std=None, prior=None, leave_outs=None, split_ts=True, allowed_connections=[]):

      """ This function sets up Inferelator2 based on the input parameters above.
       For setting something up with just a config file, use setup_config """
      if name == None:
          self.name = "inferelator2-" + datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
      else:
          self.name = name

      self.exp_data = None
      self.ko_storage = ko_storage
      self.other_storage = other_storage
      self.ts_storage = ts_storage
      self.wt_storage = wt_storage
      self.gold_standard = gold_std
      self.leave_outs = leave_outs

      if leave_outs != None:
          settings["inferelator2"]["leave_out"] = "leave_out.tsv"


      if prior != None:
          settings["inferelator2"]["prior_file"] = "priors.tsv"

      if gold_std != None:
          settings["inferelator2"]["gold_standard"] = "gold_standard.tsv"

      if ts_storage != None:
        tZero_storage = MicroarrayData("TimeZeros.tsv", "steadystate", ts_storage[0].gene_list)
        for t in ts_storage:
            tZero_storage.experiments.append(t.getTimePoint(0))
        self.tZero_storage = tZero_storage
      else:
        tZero_storage = None

      # Read in the default values for inferelator2, we'll override these later
      #self.read_data(settings)

      self.create_directories(settings)

      storage_data = [wt_storage, ko_storage, other_storage, ts_storage]
      storage_data = filter(None, storage_data)

      self.write_data(storage_data, settings, gold_std, None, prior, leave_outs, split_ts)
      self.write_config(settings)

      self.cmd = "Rscript inferelator.R " + settings[self.alg_name]["working_dir_config"]

      print self.cmd
      #exit()
      self.cwd = self.working_dir

    def write_config(self, settings):
        # Code to write the config file on the fly before calling inferelator2
      # This will allow us to use the stock implementation of inferelator2
      WriteConfig("inferelator2", settings)

      # Now we have to write a symlink to this file into the inferelator2
       # directory.  This is because inferelator2 must be run from its directory
       # because it relies on relative paths.

      if os.path.isfile(settings["inferelator2"]["working_dir_config"]) or \
              os.path.islink(settings["inferelator2"]["working_dir_config"]):
                  os.remove(settings["inferelator2"]["working_dir_config"])
      os.symlink( settings["inferelator2"]["settings_file"], \
              settings["inferelator2"]["working_dir_config"])

    def write_data(self, storage_units, settings, gold_standard=None, tf_names=None, priors=None, leave_outs=[], split_ts=True):
       # This will write out the data files that inferelator2 is going to read in
       # and set up the input folder.

       exp_file = open(self.data_dir + "/" + "expression.tsv", 'w')
       priors_file = open(self.data_dir + "/" + "priors.tsv", 'w')
       tf_file = open(self.data_dir + "/" + "tf_names.tsv", 'w')
       meta_file = open(self.data_dir + "/" + "meta_data.tsv", 'w')
       gold_file = open(self.data_dir + "/" + "gold_standard.tsv", 'w')
       leave_file = open(self.data_dir + "/" + "leave_out.tsv", 'w')
       delta_t_setting = settings["global"]["time_series_delta_t"]

       # Start by creating the metadata file for the different kinda of
       # data we have

       # Add support for breaking time series in half (when perturb is
       # removed)

       names_hash = {}

       meta_header = '"isTs"\t"is1stLast"\t"prevCol"\t"del.t"\t"condName"\n'
       meta_file.write(meta_header)

       for storage in storage_units:
           if storage == None:
               continue
           is_ts = "FALSE"
           pos = '"e"'
           prev = "NA"
           delta_t = "NA"
           ts_num = 1
           # Check for time series
           if type(storage) == type([]):
               is_ts = "TRUE"
               # Loop over the replicates
               # Find the midpoint fo the time series and split it there
               prev_col_name = ""
               if split_ts:
                   for i, rep in enumerate(storage):
                       if len(rep.experiments) % 2 == 0:
                           midpoint = (len(rep.experiments) / 2.0)
                       else:
                           midpoint = round(len(rep.experiments) / 2.0) - 1
                       for j, exp in enumerate(rep.experiments):
                           if type(delta_t_setting) == type([]):
                               dt = delta_t_setting[j]
                           else:
                               dt = j * delta_t_setting
                           if j == midpoint:
                               # Write both f and l
                               col_name1 = "TS_" + str(ts_num) + "delt_" + str(int(dt))
                               pos = '"l"'
                               prev = '"'+prev_col_name+'"'
                               delta_t = delta_t_setting
                               outline = "\t".join([is_ts, pos, prev, str(delta_t), '"'+col_name1+'"']) + "\n"
                               meta_file.write(outline)
                               ts_num += 1

                               col_name2 = "TS_" + str(ts_num) + "delt_" + str(int(dt))
                               pos = '"f"'
                               prev = "NA"
                               delta_t = "NA"
                               outline = "\t".join([is_ts, pos, prev, str(delta_t), '"'+col_name2+'"']) + "\n"
                               prev_col_name = col_name2
                               meta_file.write(outline)
                               names_hash[exp] = [col_name1, col_name2]
                           else:
                               col_name = "TS_" + str(ts_num) + "delt_" + str(int(dt))
                               if j == 0:
                                   pos = '"f"'
                                   prev = "NA"
                                   delta_t = "NA"
                               elif j == len(rep.experiments) - 1:
                                   pos = '"l"'
                                   prev = '"'+prev_col_name+'"'
                                   delta_t = delta_t_setting
                                   ts_num += 1
                               else:
                                   pos = '"m"'
                                   prev = '"'+prev_col_name+'"'
                                   delta_t = delta_t_setting
                               prev_col_name = col_name
                               outline = "\t".join([is_ts, pos, prev, str(delta_t), '"'+col_name+'"']) + "\n"
                               meta_file.write(outline)
                               names_hash[exp] = col_name
               else:
                   for i, rep in enumerate(storage):
                       prev_dt = 0
                       for j, exp in enumerate(rep.experiments):
                           if type(delta_t_setting) == type([]):
                               print rep.input_files
                               print exp.name
                               print j, delta_t_setting[j]
                               dt = delta_t_setting[j] - prev_dt
                               time = delta_t_setting[j]
                           else:
                               dt = j * delta_t_setting
                               time = dt
                           col_name = "TS_" + str(ts_num) + "delt_" + str(int(time))
                           if j == 0:
                               pos = '"f"'
                               prev = "NA"
                               delta_t = "NA"
                           elif j == len(rep.experiments) - 1:
                               pos = '"l"'
                               prev = '"'+prev_col_name+'"'
                               delta_t = dt
                               ts_num += 1
                           else:
                               pos = '"m"'
                               prev = '"'+prev_col_name+'"'
                               delta_t = dt
                           prev_col_name = col_name
                           outline = "\t".join([is_ts, pos, prev, str(delta_t), '"'+col_name+'"']) + "\n"
                           meta_file.write(outline)
                           names_hash[exp] = col_name

           else:
                for i, exp in enumerate(storage.experiments):
                    try:
                        col_name = storage.gene_list[int(exp.name)]
                        ftype = storage.type
                        if ftype == "knockout":
                            col_name += '(-/-)'
                        elif ftype == "knockdown":
                            col_name += '(+/-)'
                        else:
                            col_name = 'wt' + str(i)
                    except:
                        col_name = 'wt' + str(i)
                    outline = "\t".join([is_ts, pos, prev, str(delta_t), '"'+col_name+'"']) + "\n"
                    meta_file.write(outline)
                    names_hash[exp] = col_name

       # Now write out expression file
       # Build header
       header = []
       values_hash = {}
       gene_list = []
       for storage in storage_units:
           if type(storage) == type([]):
               for rep in storage:
                   gene_list = rep.gene_list
                   for exp in rep.experiments:
                       col_name = names_hash[exp]
                       if type(col_name) == type([]):
                           header.append(col_name[0])
                           header.append(col_name[1])
                           values_hash[col_name[0]] = exp.ratios
                           values_hash[col_name[1]] = exp.ratios
                       else:
                           header.append(col_name)
                           values_hash[col_name] = exp.ratios
           else:
               gene_list = storage.gene_list
               for exp in storage.experiments:
                   col_name = names_hash[exp]
                   if type(col_name) == type([]):
                       header.append(col_name[0])
                       header.append(col_name[1])
                       values_hash[col_name[0]] = exp.ratios
                       values_hash[col_name[1]] = exp.ratios
                   else:
                       header.append(col_name)
                       values_hash[col_name] = exp.ratios
       exp_file.write("\t".join(header) + "\n")
       for gene in gene_list:
           if tf_names == None:
               tf_file.write(gene + "\n")
           elif gene in tf_names:
               tf_file.write(gene + "\n")
           exp_file.write(gene + "\t")
           expression_values = []
           for col_name in header:
               values = values_hash[col_name]

               print list(set(gene_list) - set(values.keys()))
               print col_name
               eval = values[gene]
               expression_values.append(str(eval))
           outline = "\t".join(expression_values) + "\n"
           exp_file.write(outline)

       if priors == None:
           priors_file.write("\t".join(gene_list)+"\n")
           for g1 in gene_list:
               priors_file.write(g1)
               for g2 in gene_list:
                   priors_file.write("\t0")
               priors_file.write("\n")
       else:
           priors_file.write("\t".join(gene_list)+"\n")
           for g1 in gene_list:
               priors_file.write(g1)
               for g2 in gene_list:
                   priors_file.write("\t" + str(priors.network[g2][g1]))
               priors_file.write("\n")

       if gold_standard != None:
           gold_file.write("\t".join(gene_list)+"\n")
           for g1 in gene_list:
               gold_file.write(g1)
               for g2 in gene_list:
                   gold_file.write("\t" + str(gold_standard.network[g2][g1]))
               gold_file.write("\n")

       if leave_outs != None:
           for exp in leave_outs:
               col_name = names_hash[exp]
               leave_file.write(col_name + "\n")

       self.gene_list = gene_list

       tf_file.flush()
       tf_file.close()
       meta_file.flush()
       meta_file.close()
       exp_file.flush()
       exp_file.close()
       priors_file.flush()
       priors_file.close()
       gold_file.flush()
       gold_file.close()
       leave_file.flush()
       leave_file.close()


    def read_output(self,settings):
        # Code to write for collecting the output files from the algorithm, writes to the
      # output list in the object

      output_results = {}
      for dirname, dirnames, filenames in os.walk(self.output_dir + "/output"):
          for filename in filenames:
              print filename
              if ".tsv" in filename and "combinedconf" in filename and filename[0] != ".":
                  print "FOUND FILE"
                  net = Network()
                  net.read_netmatrix_file_transpose(dirname+"/"+filename, self.gene_list)
                  output_results[filename] = net


      self.network = output_results.values()[0]

      min_gene_val = sys.maxint
      for gene1 in self.gene_list:
          for gene2 in self.gene_list:
              if self.network.network[gene1][gene2] < min_gene_val:
                  min_gene_val = self.network.network[gene1][gene2]

      for gene1 in self.gene_list:
          for gene2 in self.gene_list:
              self.network.network[gene1][gene2] = self.network.network[gene1][gene2] - min_gene_val + random.random() * 0.00000001

      # Now convert eh weights to between 0-1
      #self.network.normalize()

      print self.network.network

      self.output_results = output_results
      return output_results

      #return self.network






    def write_output(self,settings):
      pass




    def read_data(self, settings):
      self.exp_data = MicroarrayData()
      self.exp_data.read_input(settings["inferelator2"]["ratio_file"])


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

        # Make Inferelator2 jobs
        inferelator2job = Inferelator2()
        inferelator2job.setup(knockout_storage, wildtype_storage, settings, timeseries_storage, other_storage, name)

        print "Queuing job..."
        jobman.queueJob(inferelator2job)

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

    usage = "Usage: python inferelator2.py --name NAME -g goldnetworkfile -t timeseriesfile -k knockoutfile steadystate_datafile1 steadystate_datafile2 ..."
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

    inferelator2 = Inferelator2()
    inferelator2.run(options.kofile, options.tsfile, options.wtfile, args, options.name, options.goldnet_file)



