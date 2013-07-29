from Algorithm import *

class DFG4GRN(Algorithm):
    def __init__(self, settings):

        #os.chdir("/Users/Jesse/Workspace/School/MastersThesis/Program")
        self.alg_name = "dfg4grn"
        # Read in the default values for dfg4grn, we'll override these later
        settings = ReadConfig(settings, "./config/default_values/dfg4grn.cfg")
        settings = ReadConfig(settings, settings["dfg4grn"]["config"])


    def setup(self, input_files, tfs, settings, name=None, num_models=None, time_mask=None, prior=None, prior_type=None, inferelator=False, dex=None, dex_target=None):

      """ This function sets up DFG4GRN based on the input parameters above.
       For setting something up with just a config file, use setup_config """
      if name == None:
          self.name = "dfg4grn-" + datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
      else:
          self.name = name

      self.exp_data = None
      self.input_files = input_files
      self.tfs = tfs
      self.prior = prior

      if type(input_files) == type(""):
          input_files = ReadData(input_files, "timeseries")

      # Read in settings
      if num_models != None:
          settings["dfg4grn"]["num_models"] = num_models

      self.create_directories(settings)

      self.write_data(input_files, tfs, settings, time_mask)
      self.prior = prior
      if prior != None:
          self.write_prior(settings, prior)
      else:
          settings["dfg4grn"]["prior_file"] = ""
      if dex != None:
          settings["dfg4grn"]["use_dex"] = 1
          settings["dfg4grn"]["dex_target"] = dex_target
          self.write_dex(settings, dex)
      else:
          settings["dfg4grn"]["use_dex"] = 0

      self.prior_type = prior_type
      if prior_type != None:
        settings["dfg4grn"]["prior_type"] = prior_type

      self.inferelator = inferelator
      if inferelator:
        settings["dfg4grn"]["inferelator"] = "true"
      else:
        settings["dfg4grn"]["inferelator"] = "false"
      # This must happen after all data is written, otherwise we dont know where it is!
      self.write_config(settings)

      self.tau = settings["dfg4grn"]["tau"]
      self.eta_z = settings["dfg4grn"]["eta_z"]
      self.lambda_w = settings["dfg4grn"]["lambda_w"]
      self.n_models = settings["dfg4grn"]["num_models"]


      self.cmd = "bash start_local.sh " + str(self.n_models) + " " + \
               "../output/"
      self.cwd = self.working_dir

    def predict_from_weights(self, weights, settings):
        output_file = self.output_dir + "/output/" + \
              "/dfg4grn_output.mat"
        settings["dfg4grn"]["prior_type"] = "weights"
        settings["dfg4grn"]["prior_file"] = "predict_weights.csv"
        self.prior_type = "weights"
        self.prior = weights
        self.write_prior(settings, weights)
        self.cmd = "bash start_prediction.sh " + "../output/" + " " + output_file + " " + settings[self.alg_name]["prior_file"]
        self.run_local(settings)

    def write_data(self, input_files, tfs, settings, time_mask=None):
          # This will write out the data files that dfg4grn is going to read in
       # and set up the input folder.
       settings["dfg4grn"]["ratio_files"] = ""
       settings["dfg4grn"]["delta_t"] = settings["global"]["time_series_delta_t"]

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
           settings["dfg4grn"]["ratio_files"] += self.data_dir\
                   + "/" + file.input_files[0] + " "
           #print settings["dfg4grn"]["ratio_files"]
           data_file.flush()
           data_file.close()
       r = settings["dfg4grn"]["ratio_files"]
       r = r[0:len(r)-1]
       settings["dfg4grn"]["ratio_files"] = r

       # Now we have to write out the list of transcription factors
       settings["dfg4grn"]["tfs_file"] = self.data_dir + "/"\
               + self.tfs.file_name
       tf_file = open(settings["dfg4grn"]["tfs_file"], 'w')
       for tf in self.tfs.tfs:
           tf_file.write(tf + '\n')
       tf_file.flush()
       tf_file.close()



    def read_output(self, settings):
        # Code to write for collecting the output files from the algorithm, writes to the
      # output list in the object
      # What we want to do here is get the prediction rate on the last time
      # point and the network so we can compare it against a gold std.
      output_file = open(self.output_dir + "/output/" + \
              "/dfg4grn_output.txt", 'r')

      dfg_output = scipy.io.loadmat(self.output_dir + "/output/" + \
              "/dfg4grn_output.mat")

      #self.network = (numpy.dot(dfg_output["wAverage"],dfg_output["pNetBinary"])).tolist()
      #self.network = dfg_output["pNetBinary"].tolist()
      self.raw_network = dfg_output["wAverage"].tolist()
      self.raw_network_cutoff = dfg_output["pNetBinary"].tolist()
      #self.raw_zscores = dfg_output["zScores"].tolist()
      #self.raw_zscores_mask = dfg_output["zScores_mask"].tolist()
      #self.beta_weights = dfg_output["beta_weights"].tolist()
      self.pred_mat = dfg_output["pNet"].tolist()
      self.bias = dfg_output["biasAverage"].tolist()
      self.weights = dfg_output["wAverage"].tolist()

      #print self.raw_zscores_mask

      #for i in range(len(self.raw_zscores)):
        #self.raw_zscores[i] = map(abs, self.raw_zscores[i])

      #for i,row in enumerate(self.raw_network):
        #for j,datum in enumerate(row):
          #if self.raw_network[i][j] < 0.001:
            #self.raw_network_cutoff[i][j] = 1
          #else:
            #self.raw_network_cutoff[i][j] = 0

      self.network = net = Network()
      #print "BUILDING NET"
      #print self.gene_list
      #print self.raw_network
      net.read_netmatrix(self.raw_network, self.gene_list)
      #self.network_cutoff = net = Network()

      #print "BUILDING CUTOFF"
      #net.read_netmatrix(self.raw_network_cutoff, self.gene_list)
      #self.zscores = net = Network()
      #print "ZSCORES"
      #net.read_netmatrix(self.raw_zscores, self.gene_list)
      #self.zscores_mask = net = Network()
      #print "ZSCORES MASK"
      #net.read_netmatrix(self.raw_zscores_mask, self.gene_list)

      # Use the zscores to build the network
      #self.network = self.
      #print "COMPLETE"

      ###
      #self.network = self.network_cutoff

      #print self.raw_network
      #print self.network
      #cutoff = settings["dfg4grn"]["edge_cutoff"]


      #for i,row in enumerate(self.raw_network):
          #for j,datum in enumerate(row):
                #self.network[i][j] = float(datum)

      avg_sign = 0
      avg_sign_all = 0
      for line in output_file:
          if "error_trend_sign" in line and "testAv" in line and "all" not in line:
              line = line.split(",")
              error = float(line[len(line)-1])
              if error > avg_sign:
                  avg_sign = error
          if "error_trend_sign_all" in line and "testAv" in line:
              line = line.split(",")
              error = float(line[len(line)-1])
              if error > avg_sign_all:
                  avg_sign_all = error
      #print best_sign
      self.avg_sign = avg_sign
      self.avg_sign_all = avg_sign_all

      output_file = open(self.output_dir + "/output/" + \
              "/dfg4grn_output.txt", 'r')
      best_sign = 0
      best_sign_all = 0
      for line in output_file:
          if "error_trend_sign" in line and "test" in line and "all" not in line:
              line = line.split(",")
              error = float(line[len(line)-1])
              if error > best_sign:
                  best_sign = error
          if "error_trend_sign_all" in line and "test" in line:
              line = line.split(",")
              error = float(line[len(line)-1])
              if error > best_sign_all:
                  best_sign_all = error
      self.best_sign = best_sign
      self.best_sign_all = best_sign_all


      # Now get the network
      #return self.network
      return self.network

    def write_output(self,settings):
      pass




    def read_data(self, settings):
      self.exp_data = MicroarrayData()
      self.exp_data.read_input(settings["dfg4grn"]["ratio_file"])

    ####################################################################################
    # This is the code so we can run it from VisTrails or as a command line module
    ####################################################################################
    def run(self, tsfile, tfsfile, name, goldnet_file, prior_file, delta_t):
        import numpy

        os.chdir(os.environ["gene_path"])

        timeseries_storage = ReadData(tsfile[0], "timeseries")
        for ts in timeseries_storage:
            ts.normalize()

        tfs = None
        if tfsfile == None:
            # Set to all genes
            tfs = TFList(timeseries_storage[0].gene_list)
        else:
            f = open(tfsfile, 'r')
            t = []
            for gene in file:
                t.append(gene)
            tfs = TFList(t)


        settings = {}
        settings = ReadConfig(settings)
        # TODO: CHANGE ME
        settings["global"]["working_dir"] = os.getcwd() + '/'
        settings["global"]["time_series_delta_t"] = int(delta_t)

        # Setup job manager
        print "Starting new job manager"
        jobman = JobManager(settings)

        # Make DFG4GRN jobs
        dfg4grn_job = DFG4GRN()
        dfg4grn_job.setup(timeseries_storage, tfs, settings, name)

        print "Queuing job..."
        jobman.queueJob(dfg4grn_job)

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
            #print jobnet.analyzeMotifs(goldnet).ToString()
            print jobnet.calculateAccuracy(goldnet)

        return jobnet.original_network

if __name__ == "__main__":
    import sys
    from optparse import OptionParser

    usage = "Usage: python dfg4grn.py --name NAME -g goldnetworkfile --tfs transscription_factors_file timeseries_datafile replicate1 replicate2 ..."
    parser = OptionParser(usage)
    parser.add_option("-n", "--name", dest="name",
                  help="Give a name for this run (optional)", metavar="STRING")
    parser.add_option("-t", "--tfs", dest="tfsfile",
            help="List of transcription factors (optional)", metavar="STRING")
    parser.add_option("-d", "--deltaT", dest="delta_t",
            help="Amount of time between time points.  Default 20.", metavar="INT")
    parser.add_option("-g", "--goldnet", dest="goldnet_file",
                  help="The filename of the gold standard to compare to, if available. (optional)",
                  metavar="FILE")
    parser.add_option("-p", "--prior", dest="prior_file",
                  help="The network output file of another algorithm.  (optional)",
                  metavar="FILE")


    (options, args) = parser.parse_args()

    if options.delta_t == None:
        options.delta_t = 20

    dfg4grn = DFG4GRN()
    dfg4grn.run(args, options.tfsfile, options.name, options.goldnet_file, options.prior_file, options.delta_t)
