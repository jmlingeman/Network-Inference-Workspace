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
from JobManager import *

class Banjo(Algorithm):
    def __init__(self):

        #os.chdir("/Users/Jesse/Workspace/School/MastersThesis/Program")
        self.alg_name = "banjo"

    def run_vt(self, config_file):
        settings = ReadConfig({}, "./config/default_values/banjo.cfg")
        settings = ReadConfig(settings, config_file)
        settings["global"]["working_dir"] = os.getcwd() + '/'


        input_files = settings["global"]["time_series_files"]
        tfs = settings["banjo"]["tfs_file"]
        delta_t = [50]*20
        settings["global"]["time_series_delta_t"] = delta_t
        t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")

        name = "banjo_vt_run_" + t

        timeseries_storage = ReadData(input_files, True)
        trans_factors = TFList(timeseries_storage[0].gene_list[0:6])

        self.setup(timeseries_storage, trans_factors, settings, name)

        # Set up our hyperparameters with the parameters from VT
        settings["banjo"]["tau"] = tau
        settings["banjo"]["lambda_w"] = lambda_w
        settings["banjo"]["eta_z"] = eta_z

        self.run_local(settings)


    def setup(self, input_files, settings, name=None, prior=None, prior_percent=0.15, time_mask=None, allowed_connections=[]):

      """ This function sets up banjo based on the input parameters above.
       For setting something up with just a config file, use setup_config """
      if name == None:
          self.name = "banjo-" + datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
      else:
          self.name = name
      self.alg_name = "banjo"
      self.exp_data = None
      self.input_files = input_files


      # Read in the default values for banjo, we'll override these later
      settings = ReadConfig(settings, "./config/default_values/banjo.cfg")
      settings = ReadConfig(settings, settings["banjo"]["config"])
      #self.read_data(settings)

      self.create_directories(settings)

      self.write_data(input_files, settings, time_mask)
      if prior != None:
        self.write_prior(prior, prior_percent, settings)
      settings["banjo"]["num_genes"] = len(self.gene_list)
      self.write_config(settings)


      self.cmd = "java -jar banjo.jar settingsFile=" \
        + settings["banjo"]["settings_file"]
      self.cwd = self.working_dir


    def write_config(self, settings):
        # Code to write the config file on the fly before calling banjo
      # This will allow us to use the stock implementation of banjo
      WriteConfig("banjo", settings)

      # Now we have to write a symlink to this file into the banjo
       # directory.  This is because banjo must be run from its directory
       # because it relies on relative paths.

      if os.path.isfile(settings["banjo"]["working_dir_config"]) or \
              os.path.islink(settings["banjo"]["working_dir_config"]):
                  os.remove(settings["banjo"]["working_dir_config"])
      #print settings["banjo"]["settings_file"]
      #print settings["banjo"]["working_dir_config"]
      os.symlink( settings["banjo"]["settings_file"], \
              settings["banjo"]["working_dir_config"])

    def write_prior(self, network, percent, settings):
      must_have_edges = {}
      added_edges = 0
      edge_list = network.get_ranked_list()
      gene_nums = {}
      for i,gene in enumerate(network.gene_list):
        gene_nums[gene] = str(i)
      if percent > 1:
        percent = percent / 100.0
      for edge in edge_list:
        if added_edges / float(len(edge_list)) < percent:
          if edge[1] not in must_have_edges.keys():
            must_have_edges[edge[1]] = []
          must_have_edges[edge[1]].append(edge[0])
          added_edges += 1
        else:
          break
      edges_file = open(self.data_dir + "/" + "must_have_edges.str", 'w')
      for child in must_have_edges.keys():
        edges_file.write(gene_nums[child] + " ")
        for parent in must_have_edges[child]:
          edges_file.write(gene_nums[parent] + " ")
        edges_file.write("\n")
      settings["banjo"]["known_edges_file"] = self.data_dir + "/" + "must_have_edges.str"


    def write_data(self, input_files, settings, time_mask=None):
          # This will write out the data files that banjo is going to read in
       # and set up the input folder.
       settings["banjo"]["ratio_files"] = ""
       settings["banjo"]["delta_t"] = settings["global"]["time_series_delta_t"]

       # We can only use the first replicate for BANJO, so just take that.
       file = input_files[0]
       data_file = open(self.data_dir + "/" + file.input_files[0], 'w')
       header = "# The data format is as follows: one row per observation, \
               columns for each gene.\n\n"
       data_file.write(header)

       gene_row = "#"
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
       settings["banjo"]["ratio_files"] += self.data_dir\
               + "/" + file.input_files[0] + " "
       #print settings["banjo"]["ratio_files"]
       data_file.flush()
       data_file.close()
       r = settings["banjo"]["ratio_files"]
       r = r[0:len(r)-1]
       settings["banjo"]["ratio_file"] = input_files[0].input_files[0]



    def read_output(self,settings):
      #output_file = open(self.output_dir + "/output/banjo/top.graph.txt", 'r')

      output_file = open(self.output_dir + "./output/banjo/top.graph.txt", 'r')

      import re
      network = []
      for i in range(len(self.gene_list)):
        row = []
        for j in range(len(self.gene_list)):
          row.append(0)
        network.append(row)

      lines = output_file.readlines()


      for i, line in enumerate(lines):
        line = line.strip()
        if line != '' and line[0] == '"':
            line = re.sub(r'\(.*?\)', '', line).replace('"','').replace(';','').strip().replace('->','')
            #print line

            ls = line.split()
            edge = ls[1]
            gene = ls[0]
            network[int(gene)][int(edge)] = 1
            network[int(edge)][int(gene)] = 1
      net = Network()
      net.read_netmatrix(network, self.gene_list)
      self.network = net

    def write_output(self,settings):
      pass




    def read_data(self, settings):
      self.exp_data = MicroarrayData()
      self.exp_data.read_input(settings["banjo"]["ratio_file"])

    ####################################################################################
    # This is the code so we can run it from VisTrails or as a command line module
    ####################################################################################
    def run(self, ts_file, name=None, delta_t=30):

        os.chdir(os.environ["gene_path"])

        print "Reading in knockout data"
        timeseries_storage = ReadData(ts_file, "timeseries")

        settings = {}
        settings = ReadConfig(settings)
        # TODO: CHANGE ME
        settings["global"]["working_dir"] = os.getcwd() + '/'

        # Setup job manager
        print "Starting new job manager"
        jobman = JobManager(settings)

        # Make Banjo jobs
        banjojob = Banjo()
        if delta_t != None:
            settings["global"]["time_series_delta_t"] = int(delta_t)
        else:
            settings["global"]["time_series_delta_t"] = 30
        if name != None:
            banjojob.setup(timeseries_storage, settings, name)
        else:
            banjojob.setup(timeseries_storage, settings)


        print "Queuing job..."
        jobman.queueJob(banjojob)

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
        #print job.alg.network.network
        #print jobnet.original_network

        return jobnet.original_network

if __name__ == "__main__":
    import sys
    if len(sys.argv) < 2:
        print "Error: Not enough arguments.\nSyntax: python banjo.py timeseries_file name(optional) delta_t(optional)"
        exit()

    ts_file = sys.argv[1]

    name = ""
    if len(sys.argv) > 2:
        name = sys.argv[2]
    else:
        name = None

    if len(sys.argv) > 3:
        delta_t = sys.argv[3]
    else:
        delta_t = None

    banjo = Banjo()
    banjo.run(ts_file, name, delta_t)

