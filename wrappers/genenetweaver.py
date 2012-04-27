# TODO: Finish getting GNW started.


import os
import gzip
import numpy
#import scipy.io
from Algorithm import *
from ReadConfig import *
from ReadData import *
from WriteConfig import *
from DataStore import *
from datetime import datetime
from shutil import copytree

class genenetweaver(Algorithm):
    def __init__(self):

        #os.chdir("/Users/Jesse/Workspace/School/MastersThesis/Program")
        self.alg_name = "genenetweaver"

    def run_vt(self, config_file, tau, lambda_w, eta_z):
        settings = ReadConfig({}, "./config/default_values/genenetweaver.cfg")
        settings = ReadConfig(settings, config_file)
        settings["global"]["working_dir"] = os.getcwd() + '/'


        input_files = settings["global"]["time_series_files"]
        tfs = settings["genenetweaver"]["tfs_file"]
        delta_t = [50]*20
        settings["global"]["time_series_delta_t"] = delta_t
        t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")

        name = "genenetweaver_vt_run_" + t

        timeseries_storage = ReadData(input_files, True)
        trans_factors = TFList(timeseries_storage[0].gene_list[0:6])

        self.setup(timeseries_storage, trans_factors, settings, name)

        # Set up our hyperparameters with the parameters from VT
        settings["genenetweaver"]["tau"] = tau
        settings["genenetweaver"]["lambda_w"] = lambda_w
        settings["genenetweaver"]["eta_z"] = eta_z

        self.run_local(settings)


    def setup(self, input_files, tfs, settings, name=None, time_mask=None, allowed_connections=[]):

      """ This function sets up genenetweaver based on the input parameters above.
       For setting something up with just a config file, use setup_config """
      if name == None:
          self.name = "genenetweaver"
      else:
          self.name = name
      self.alg_name = "GeneNetWeaver"
      self.exp_data = None
      self.input_files = input_files
      self.tfs = tfs

      t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")

      # Read in the default values for genenetweaver, we'll override these later
      settings = ReadConfig(settings, "./config/default_values/genenetweaver.cfg")
      settings = ReadConfig(settings, settings["genenetweaver"]["config"])
      #self.read_data(settings)

      # Create the genenetweaver directory in the output folder
      self.output_dir = settings["global"]["working_dir"] + \
              settings["global"]["output_dir"] + "/" + self.name + "/"
      if not os.path.isdir(self.output_dir):
          os.mkdir(self.output_dir)
      if not os.path.isdir(self.output_dir + "/output/"):
          os.mkdir(self.output_dir + "/output/")

      # Create the genenetweaver data directory
      self.data_dir = self.output_dir + "/data/"
      if not os.path.isdir(self.data_dir):
          os.mkdir(self.data_dir)

      # Create the genenetweaver log directory
      self.log_dir = self.output_dir + "/log/"
      if not os.path.isdir(self.log_dir):
          os.mkdir(self.log_dir)

      # Copy the entire working directory into our new directory.
      # We want to keep track of everything that was run.
      self.working_dir = self.output_dir + "/genenetweaver_program/"
      #if not os.path.isdir(self.working_dir):
        #os.mkdir(self.working_dir)
      print self.name + " is copying files that it needs before it begins..."
      copytree(settings["genenetweaver"]["path"], self.working_dir, True)


      settings["genenetweaver"]["output_dir"] = self.output_dir
      settings["genenetweaver"]["log_dir"] = self.log_dir
      settings["genenetweaver"]["data_dir"] = self.data_dir
      self.write_data(input_files, tfs, settings, time_mask)
      self.write_config(settings)

      tau = settings["genenetweaver"]["tau"]
      eta_z = settings["genenetweaver"]["eta_z"]
      lambda_w = settings["genenetweaver"]["lambda_w"]
      n_models = settings["genenetweaver"]["n_models"]

      extract = "java -jar gnw3-standalone --extract --input-net ecoli_transcriptional_network_regulonDB_6_7.tsv --random-seed --greedy-selection --subnet-size=50 --num-subnets=10 --output-net-format=4 --keep-self-interactions --output-path ."

      simulate = "java -jar gnw-standalone.jar --simulate -c settings.txt --input-net InSilicoSize10-Yeast1.xml --output-path /home/thomas/devel/gnw-3_0/datasets"

      self.cmd = "java -jar gnw3-standalone " + str(n_models) + " " + \
              self.output_dir + "/output/" + " " + \
              str(eta_z) + " " + str(tau) + " " + str(lambda_w)
      self.cwd = self.working_dir

    def write_config(self, settings):
        # Code to write the config file on the fly before calling genenetweaver
      # This will allow us to use the stock implementation of genenetweaver
      WriteConfig("genenetweaver", settings)

      # Now we have to write a symlink to this file into the genenetweaver
       # directory.  This is because genenetweaver must be run from its directory
       # because it relies on relative paths.

      if os.path.isfile(settings["genenetweaver"]["working_dir_config"]) or \
              os.path.islink(settings["genenetweaver"]["working_dir_config"]):
                  os.remove(settings["genenetweaver"]["working_dir_config"])
      os.symlink( settings["genenetweaver"]["settings_file"], \
              settings["genenetweaver"]["working_dir_config"])

    def write_data(self, input_files, tfs, settings, time_mask=None):
          # This will write out the data files that genenetweaver is going to read in
       # and set up the input folder.
       settings["genenetweaver"]["ratio_files"] = ""
       settings["genenetweaver"]["delta_t"] = settings["global"]["time_series_delta_t"]

       for i, file in enumerate(input_files):
           # Check and see if the time_mask exists.  Sanity check to make sure
           # it is the right size.
           #if time_mask != None and len(time_mask) != len(file.experiments[0].name):
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
               gene_row = gene.upper() + "\t"
               for j,e in enumerate(file.experiments):
                   # Check the time mask to make sure we are printing this one
                   if time_mask != None and time_mask[j] == 0:
                       continue

                   try:
                       gene_row += str(e.ratios[gene]) + "\t"
                   except:
                       gene_row += "\t"
               data_file.write(gene_row.strip('\t') + "\n")
           self.gene_list = file.gene_list
           settings["genenetweaver"]["ratio_files"] += self.data_dir\
                   + "/" + file.input_files[0] + " "
           print settings["genenetweaver"]["ratio_files"]
           data_file.flush()
           data_file.close()
       r = settings["genenetweaver"]["ratio_files"]
       r = r[0:len(r)-1]
       settings["genenetweaver"]["ratio_files"] = r

       # Now we have to write out the list of transcription factors
       settings["genenetweaver"]["tfs_file"] = self.data_dir + "/"\
               + self.tfs.file_name
       tf_file = open(settings["genenetweaver"]["tfs_file"], 'w')
       for tf in self.tfs.tfs:
           tf_file.write(tf + '\n')
       tf_file.flush()
       tf_file.close()


