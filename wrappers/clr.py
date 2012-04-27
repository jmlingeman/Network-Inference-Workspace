import os
import gzip
import numpy
import scipy.io
from Algorithm import *
from ReadConfig import *
from ReadData import *
from WriteConfig import *
from DataStore import *
from datetime import datetime
from shutil import copytree
import copy

class CLR(Algorithm):
    def __init__(self):

        #os.chdir("/Users/Jesse/Workspace/School/MastersThesis/Program")
        self.alg_name = "clr"


    def setup(self, input_files, settings, name=None, clr_type="plos", num_bins=10, spline_type=3):

      """ This function sets up clr based on the input parameters above.
       For setting something up with just a config file, use setup_config """
      if name == None:
          self.name = "clr-" + datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
      else:
          self.name = name

      self.exp_data = None
      self.input_files = input_files

      t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")

      # Read in the default values for clr, we'll override these later
      settings = ReadConfig(settings, "./config/default_values/clr.cfg")
      settings["clr"]["clr_type"] = clr_type
      settings["clr"]["num_bins"] = num_bins
      settings["clr"]["spline_type"] = 3
      settings = ReadConfig(settings, settings["clr"]["config"])

      self.create_directories(settings)

      self.write_data(input_files, settings)

      self.settings = copy.deepcopy(settings)
      self.write_config(settings)

      self.cmd = "matlab -nodesktop -nodisplay -nosplash -r \"addpath(genpath('./'));try;run_clr, catch ,end, exit\""
      self.cwd = self.working_dir

    def write_config(self, settings):
        # Code to write the config file on the fly before calling clr
      # This will allow us to use the stock implementation of clr
      WriteConfig("clr", settings)

      # Now we have to write a symlink to this file into the clr
       # directory.  This is because clr must be run from its directory
       # because it relies on relative paths.

      if os.path.isfile(settings["clr"]["working_dir_config"]) or \
              os.path.islink(settings["clr"]["working_dir_config"]):
                  os.remove(settings["clr"]["working_dir_config"])
      os.symlink( settings["clr"]["settings_file"], \
              settings["clr"]["working_dir_config"])

    def write_data(self, file, settings):
       # This will write out the data files that clr is going to read in
       # and set up the input folder.
       settings["clr"]["ratio_files"] = ""

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
       settings["clr"]["ratio_files"] += self.data_dir\
               + "/" + file.input_files[0] + " "
       print settings["clr"]["ratio_files"]
       data_file.flush()
       data_file.close()
       r = settings["clr"]["ratio_files"]
       r = r[0:len(r)-1]
       settings["clr"]["ratio_files"] = r


    def read_output(self,settings):
        # Code to write for collecting the output files from the algorithm, writes to the
      # output list in the object
      # What we want to do here is get the prediction rate on the last time
      # point and the network so we can compare it against a gold std.
      output_file = self.output_dir + "/output/" + \
              "/clr_output.txt"

      net = Network()
      net.read_netmatrix_file(output_file, self.gene_list)
      self.network = net

    def write_output(self,settings):
      pass




    def read_data(self, settings):
      self.exp_data = MicroarrayData()
      self.exp_data.read_input(settings["clr"]["ratio_file"])



