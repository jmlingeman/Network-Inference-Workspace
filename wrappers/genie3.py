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

class GENIE3(Algorithm):
    def __init__(self):

        #os.chdir("/Users/Jesse/Workspace/School/MastersThesis/Program")
        self.alg_name = "genie3"

        # Set supported datatypes
        self.datatypes = ["overexpression", "knockout", "multifactorial", "knockdown"]


    def setup(self, data_storage, settings, name=None, time_mask=None, allowed_connections=[]):

      """ This function sets up GENIE3 based on the input parameters above.
       For setting something up with just a config file, use setup_config """
      if name == None or name == "":
          self.name = "genie3-" + datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
      else:
          self.name = name

      # Store the data in this object for later
      self.data_storage = data_storage

      # Read in the default values for genie3, we'll override these later
      settings = ReadConfig(settings, "./config/default_values/genie3.cfg")
      settings = ReadConfig(settings, settings["genie3"]["config"])

      # Jump back out to the inherited methods to make the directories
      self.create_directories(settings)

      #self.read_data(settings)

      # Write the data to disk in the format the algorithm expects it
      self.write_data(data_storage, settings, data_storage.type)

      # Write the configuration file to disk.
      self.write_config(settings)

      # Now build the command
      # This algorithm uses matlab, so we need to get the matlab path.
      matlab_path = settings["global"]["matlab_path"]
      command = matlab_path + " -nodesktop -nodisplay -nosplash -r \"addpath(genpath('./'));try;run_genie3, catch err, err, end, exit\""
      self.cmd = command
      print self.cmd


      self.cwd = self.working_dir

    def write_data(self, input_file, settings, filetype):
       # This will write out the data files that genie3 is going to read in
       # and set up the input folder.
       settings["genie3"][filetype] = ""

       settings["genie3"]["ratio_files"] = self.data_dir + "/" + input_file.input_files[0]
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
       settings["genie3"][filetype] += self.data_dir\
               + "/" + input_file.input_files[0] + " "
       print settings["genie3"][filetype]
       data_file.flush()
       data_file.close()

       r = settings["genie3"][filetype]
       r = r[0:len(r)-1]
       settings["genie3"][filetype] = r


    def read_output(self,settings):
      # Code to write for collecting the output files from the algorithm, writes to the
      # output list in the object
      # What we want to do here is get the prediction rate on the last time
      # point and the network so we can compare it against a gold std.
      # This file is a bunch of zscores, so we have to load the cutoff we want
      output_file = open(self.output_dir + "/output/ranked_edges.txt", 'r')
      topn = None
      if "top_n_edges" in settings["genie3"].keys():
        topn = settings["genie3"]["top_n_edges"]
      else:
        topn = len(self.gene_list)
      zscores = []
      for line in output_file:
          gene1, gene2, zscore = line.split()
          zscore = float(zscore)
          zscores.append((gene1, gene2, zscore))
      zscores = sorted(zscores, key=lambda zscore: abs(zscore[2]), reverse=True)

      self.zscores = zscores[:]
      network = []

      #for i,zscore in enumerate(zscores):
          #if i < topn:
            #gene1, gene2, zscore = zscore
            #zscores[i] = (gene1, gene2, 1)
          #else:
            #gene1, gene2, zscore = zscore
            #zscores[i] = (gene1, gene2, 0)
      net = Network()
      net.read_networklist(zscores)
      net.gene_list = self.gene_list
      self.network = net

      return self.zscores


    def read_data(self, settings):
      self.exp_data = MicroarrayData()
      self.exp_data.read_input(settings["genie3"]["ratio_file"])


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

        # Make GENIE3 jobs
        genie3 = GENIE3()
        genie3.setup(datastore, settings, name)

        print "Queuing job..."
        jobman.queueJob(genie3)

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

    usage = "Usage: python genie3.py --name=NAME (optional) datafile1 datafile2 ..."
    parser = OptionParser(usage)
    parser.add_option("-n", "--name", dest="name",
                  help="Give a name for this run", metavar="STRING")
    parser.add_option("-g", "--goldnet", dest="goldnet",
                  help="The filename of the gold standard to compare to, if available.",
                  metavar="FILE")

    (options, args) = parser.parse_args()

    if len(args) < 1:
        parser.error("At least one steady-state data file must be specified.")


    genie3 = GENIE3()
    print options.name
    genie3.run(options.name, args, options.goldnet)
