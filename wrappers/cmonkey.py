import os
import gzip
from Algorithm import *
from ReadConfig import *
from WriteConfig import *
from DataStore import *

class Cmonkey(Algorithm):
   def __init__(self):
     self.alg_name = "cmonkey"

   def setup(self, storage, settings):
      self.name = "cMonkey"
      self.exp_data = storage
      self.cmd = "Rscript R_scripts/main.R"
      self.cwd = "algorithms/cmonkey/"


      # Read in the default values for cmonkey, we'll override these later
      settings = ReadConfig(settings, "./config/default_values/cmonkey.cfg")
      settings = ReadConfig(settings, settings["cmonkey"]["config"])


      self.create_directories(settings)

      self.write_data(settings)
      self.write_config(settings)

   def write_config(self, settings):
      # Code to write the config file on the fly before calling cmonkey
      # This will allow us to use the stock implementation of cMonkey
      WriteConfig("cmonkey", settings)

      # Now we have to write a symlink to this file into the cmonkey
       # directory.  This is because cmonkey must be run from its directory
       # because it relies on relative paths.

      if os.path.isfile(settings["cmonkey"]["working_dir_config"]) or \
              os.path.islink(settings["cmonkey"]["working_dir_config"]):
            os.remove(settings["cmonkey"]["working_dir_config"])
      os.symlink( settings["cmonkey"]["settings_file"], \
           settings["cmonkey"]["working_dir_config"])

   def write_data(self, settings):
       # This will write out the data files that cMonkey is going to read in
       # and set up the input folder.

       # cMonkey takes in a tab separated ratios file, so write that out first.
       data_file = open(settings["cmonkey"]["data_dir"] + \
               settings["cmonkey"]["ratio_file"], 'w')
       lines = []

       # Write out the header
       for e in self.exp_data.experiments:
          data_file.write(e.name + "\t")
       data_file.write("\n")

       # Now write out each gene's name and its values across experiments
       for gene in self.exp_data.gene_list:
           data_file.write('"' + gene.upper() + '"' + "\t")
           for e in self.exp_data.experiments:
               try:
                   data_file.write(str(e.ratios[gene]) + "\t")
               except:
                   data_file.write("\t")
           data_file.write("\n")



       # Now, gather as many other data files as we can.  If their path is
       # given as part of the config, copy taht file into our data directory.
       # If it is not, try to download the file from one of the functions
       # below.
       if settings["cmonkey"]["gene_coord_files"] == None:
           print "No genecoord file specified, attemping to retrieve genecoords..."
           self.retrieve_genecoords(settings)

       if settings["cmonkey"]["upstream_data"] == None:
           print "No upstream data file specified, attempting to retrieve..."
           self.retrieve_upstream(settings)

   def read_output(self):
      # Code to write for collecting the output files from the algorithm, writes to the
      # output list in the object
      pass

   def read_data(self, settings):
      self.exp_data = MicroarrayData()
      self.exp_data.read_input(settings["cmonkey"]["ratio_file"])

   def retrieve_upstream(self, settings):
      import suds
      import socket

      timeout = 300
      socket.setdefaulttimeout(timeout)


      client = suds.client.Client( 'http://rsat.ulb.ac.be/rsat/web_services/RSATWS.wsdl' )

      # Helicobacter pylori 26695
      request           = client.factory.create( 'RetrieveSequenceRequest' )
      request.output    = 'client'
      rsat_orgname = settings["cmonkey"]["organism_species"].replace(" ",\
              "_").lower().capitalize()
      request.organism  = rsat_orgname
      request.query     = self.exp_data.gene_list
      request.noorf     = 1
      request.feattype  = 'CDS'
      request.format    = 'FastA'
      request.type      = 'upstream'
      request.label     = 'full'
      request.imp_pos   = 1

      try:
        print "Waiting for RSAT to respond (this may take a minute...)"
        response          = client.service.retrieve_seq( request )
        print "Got response from RSAT.  Creating gzip file..."
        upstream_sequence = response[1]

        settings["cmonkey"]["upstream_data"] = "upstream_seqs.txt"

        upstream_file = gzip.open(settings["cmonkey"]["data_dir"] + \
                settings["cmonkey"]["upstream_data"], 'wb')
        upstream_file.write(upstream_sequence)
        upstream_file.close()
        print "RSAT download completed."
      except:
          print "RSAT download timed out.  Continuing without upstream data.\n \
            Please download and add this manually."

   def retrieve_genecoords(self, settings):
      import ftputil
      try:
          settings["cmonkey"]["gene_coord_files"] = []

          ftp_site = "ftp.ncbi.nih.gov"

          host = ftputil.FTPHost(ftp_site, "anonymous")
          host.chdir("genomes")
          names = host.listdir(host.curdir)
          org_name = settings["cmonkey"]["organism_species"].lower()

          r = re.compile("[_ ]")
          org_name = r.split(org_name)

          # Now we need to search for our genome then collect each CHR fasta file in the
          # directory, and zip them up.
          for name in names:
             test_name = name.lower()
             matching_words = filter(lambda x: x in test_name.split("_"), org_name)
             if len(matching_words) == len(org_name):
                # Ok, so we have a match here.  Get all of the CHR FastA files from in here.
                host.chdir(name)
                parent_dir = host.curdir
                print "Matched organism name: " + name + ", downloading files from CHR dirs."
                print "If this is not your organism, please cancel this download and download these files manually."
                for d in host.listdir(host.curdir):
                    if "CHR" in d:
                        host.chdir(d)
                        for f in host.listdir(host.curdir):
                            if ".ptt" in f:
                                print "Downloading " + f + " from " + d
                                host.download(f, settings["cmonkey"]["data_dir"] +\
                                        f, 'b')
                                file = open(settings["cmonkey"]["data_dir"] + f,\
                                        'r')
                                gzfile = gzip.open(settings["cmonkey"]["data_dir"]\
                                        + f + '.gz', 'wb')
                                gzfile.writelines(file)
                                file.close()
                                gzfile.close()
                                settings["cmonkey"]["gene_coord_files"].append(f + '.gz')

                                host.chdir("..")
          fs = ""
          for file in settings["cmonkey"]["gene_coord_files"]:
              fs += 'paste( params$data.dir,' + '"/'+file+'", sep="" ),\n'
          fs = fs[0:fs.rindex(",")]
          settings["cmonkey"]["gene_coord_files"] = fs
      except:
          print "WARNING: Attempt to retrieve gene coord files from NCBI failed.  Please add these files yourself, they are required for cMonkey."
