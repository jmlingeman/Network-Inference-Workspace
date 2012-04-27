import os
import gzip
import numpy
#import scipy.io
from Algorithm import *
from ReadConfig import *
from ReadData import *
from WriteConfig import *
from DataStore import *
from Network import *
from datetime import datetime
from shutil import copytree
import copy


# This is a bioconductor function, so it needs an ExpressionSet dataset to work
# This should be created in the launch script, write out like normal TS file

class tdaracne(Algorithm):
    def __init__(self):

        #os.chdir("/Users/Jesse/Workspace/School/MastersThesis/Program")
        self.alg_name = "tdaracne"

    def setup(self, input_files, settings, name=None, time_mask=None, allowed_connections=[]):

      """ This function sets up tdaracne based on the input parameters above.
       For setting something up with just a config file, use setup_config """
      if name == None:
          t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
          self.name = "tdaracne-" + t
      else:
          self.name = name
      self.exp_data = None
      self.input_files = input_files


      # Read in the default values for tdaracne, we'll override these later
      settings = ReadConfig(settings, "./config/default_values/tdaracne.cfg")
      if "config" in settings["tdaracne"].keys():
        settings = ReadConfig(settings, settings["tdaracne"]["config"])
      #self.read_data(settings)

      self.create_directories(settings)

      self.write_data(input_files, settings, time_mask)
      self.settings = copy.deepcopy(settings)
      self.write_config(settings)

      self.cmd = "Rscript tdaracne_launch_script.r "  + \
            settings["tdaracne"]["data_dir"] + '/' + settings["tdaracne"]["ratio_file"] + \
            " " + str(settings["tdaracne"]["num_bins"]) + \
            " " + settings["tdaracne"]["output_dir"] + '/output/' + settings["tdaracne"]["output_file"] + \
            " " + str(settings["tdaracne"]["delta"]) + \
            " " + str(settings["tdaracne"]["likelihood"]) + \
            " " + str(settings["tdaracne"]["norm"]) + \
            " " + str(settings["tdaracne"]["logarithm"]) + \
            " " + str(settings["tdaracne"]["thresh"]) + \
            " " + str(settings["tdaracne"]["ksd"]) + \
            " " + str(settings["tdaracne"]["tolerance"])


      print self.cmd
      self.cwd = self.working_dir


    def write_config(self, settings):
        # Code to write the config file on the fly before calling tdaracne
      # This will allow us to use the stock implementation of tdaracne
      # All parameters passed via command line, no need for config here

      #WriteConfig("tdaracne", settings)
      pass

      # Now we have to write a symlink to this file into the tdaracne
       # directory.  This is because tdaracne must be run from its directory
       # because it relies on relative paths.

      #if os.path.isfile(settings["tdaracne"]["working_dir_config"]) or \
      #        os.path.islink(settings["tdaracne"]["working_dir_config"]):
      #            os.remove(settings["tdaracne"]["working_dir_config"])
      #os.symlink( settings["tdaracne"]["settings_file"], \
      #        settings["tdaracne"]["working_dir_config"])

    def write_data(self, input_files, settings, time_mask=None):
          # This will write out the data files that tdaracne is going to read in
       # and set up the input folder.
       settings["tdaracne"]["ratio_files"] = ""
       settings["tdaracne"]["delta_t"] = settings["global"]["time_series_delta_t"]

       # We can only use the first replicate for tdaracne, so just take that.
       # TODO: Average replicates for algorithms that dont handle them
       file = input_files[0]
       data_file = open(self.data_dir + "/" + file.input_files[0], 'w')

       gene_row = ""
       for gene in file.gene_list:
           gene_row += gene.upper() + '\t'
       gene_row += '\n'
       data_file.write(gene_row)

       for exp in file.experiments:
           row = ""
           for gene in file.gene_list:
               #print exp.ratios, row
               row += exp.ratios[gene] + '\t'
           data_file.write(row + '\n')

       self.gene_list = file.gene_list
       settings["tdaracne"]["ratio_files"] += self.data_dir\
               + "/" + file.input_files[0] + " "
       #print settings["tdaracne"]["ratio_files"]
       data_file.flush()
       data_file.close()
       r = settings["tdaracne"]["ratio_files"]
       r = r[0:len(r)-1]
       settings["tdaracne"]["ratio_file"] = input_files[0].input_files[0]



    def read_output(self,settings):
        # Code to write for collecting the output files from the algorithm, writes to the
      # output list in the object
      # What we want to do here is get the prediction rate on the last time
      # point and the network so we can compare it against a gold std.
      #os.rename(settings["tdaracne"]["working_dir"] + '/' + settings["tdaracne"]["output_file"]+".dot", settings["tdaracne"]["output_dir"] + '/' + settings["tdaracne"]["output_file"])
      settings = self.settings

      output_file = self.output_dir + '/output/' + settings["tdaracne"]["output_file"]

      #print output_file
      #print self.gene_list

      self.network = net = Network()
      self.network.read_dot_file(output_file, self.gene_list)
      return self.network

    def write_output(self,settings):
      pass




    def read_data(self, settings):
      self.exp_data = MicroarrayData()
      self.exp_data.read_input(settings["tdaracne"]["ratio_file"])



