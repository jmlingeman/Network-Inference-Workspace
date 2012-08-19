#!/bin/python

# This file will read in data into an internal data structure, allow analyses to be performed
# and then write the data back out into a format that the target algorithm understands.

import re

class Experiment:
   name = ""
   file = ""
   ratios = {}
   type = ""
   tfs = []

   def __init__(self, name, file, type, tfs=[]):
      self.name = name
      self.file = file
      self.ratios = {}
      self.tfs = tfs
      self.type = type


class TFList:
    tfs = []
    file_name = ""

    def __init__(self, tflist):

        self.tfs = []

        if type(tflist) == type(""):
           tflist_file = tflist # Because we are dealing with the string to a file

           self.file_name = tflist_file
           f = open(tflist_file, 'r')
           for line in f:
               line = line.upper().strip()
               line = line.replace(",","") # Just getting rid of any loose , from the csv
               if line == "":
                   continue
               self.tfs.append(line)

        elif type(tflist) == type([]):
            self.tfs = tflist # Just an array, possibly calculated from somewhere and not a file.
            self.file_name = "transcription_factor_list.csv"

class MicroarrayData:
   input_files = []
   experiments = []
   gene_list = []
   time_mask = []
   type = []
   timeseries_num = None

   def __init__(self, input_file, type, gene_list=None, timeseries_num=None):
      if input_file != None:
        self.input_files = []
        self.input_files.append(input_file.split("/")[len(input_file.split("/"))-1])
      else:
        self.input_files = []
      self.experiments = []
      self.type = type
      self.timeseries_num = timeseries_num
      self.gene_list = gene_list

   def average(self):
     import math
     avgexp = Experiment("Average", "gene_average.csv", "average")
     for gene in self.gene_list:
       gene_vals = []
       for e in self.experiments:
         gene_vals.append(e.ratios[gene])
       avgexp.ratios[gene] = math.sum(gene_vals) / float(len(gene_vals))
     self.experiments = [avgexp]

   def median(self):
     import numpy
     medexp = Experiment("Median", "gene_median.csv", "median")
     for gene in self.gene_list:
       gene_vals = []
       for e in self.experiments:
         gene_vals.append(e.ratios[gene])
       medexp.ratios[gene] = numpy.median(gene_vals)
     self.experiments = [medexp]

   def filter(self, filtered_gene_list):
    filtered_experiments = []
    for e in self.experiments:
        f = Experiment(e.name, e.file, e.type)
        for gene in e.ratios.keys():
            if gene in filtered_gene_list:
                f.ratios[gene] = e.ratios[gene]
        f.gene_list = filtered_gene_list
        filtered_experiments.append(f)
    self.experiments = filtered_experiments
    self.gene_list = filtered_gene_list


    for gene in self.gene_list:
        if gene not in filtered_gene_list:
            for e in self.experiments:
                del e.ratios[gene]
            self.gene_list.remove(gene)

   def combine(self, x):
       if type(x) == type([]):
           for e in x:
               if set(self.gene_list) == set(e.gene_list):
                   self.experiments += e.experiments
               else:
                   print "ERROR: Unable to combine datasets with different gene lists"
                   exit()

       else:
           if set(self.gene_list) != set(x.gene_list):
              print "ERROR: Unable to combine datasets with different gene lists"
              exit()
           self.experiments += x.experiments

   def getTimePoint(self, tp_num):
       return self.experiments[tp_num]


   def normalize(self):
       import numpy
       for e in self.experiments:
           values = map(float,e.ratios.values())
           normvals = (numpy.mean(values) - values) / numpy.std(values)
           for i,key in enumerate(e.ratios.keys()):
              e.ratios[key] = normvals[i]


   def read_input(self, input_file, type):
      line_split = re.compile('[\t,]')

      def parse_file(input_file, type):
         # This function will select which parser to use
         self.input_files.append(input_file)
         if type == "wildtype" or type == "multifactorial" or type == "knockdown" or type == "overexpression":
             read_rowexp(input_file, type)
         else:
            read_colexp(input_file, type)


      def read_colexp(input_file, type):
        """This function is to read simple files that are just the
        header with the experiment name and then rows with:
        gene_name\texpression value"""

        print "Loading " + input_file + " as columns of experiments."
        file = open(input_file, 'r')
        file = file.readlines()

        header = line_split.split(file[0])

        names = []
        for i, exp_name in enumerate(header):
           if exp_name.strip() == "":
               continue

           exp = Experiment(exp_name.strip(), input_file, type)
           self.experiments.append(exp)

        for line in file[1:]:
           if len(line.strip()) <= 1 or line.strip()[0] == "#":
              continue
           line = line_split.split(line)
           gene_name = line[0].replace('"','')
           exp_values = line[1:len(self.experiments)+1]
           for i in xrange(len(exp_values)):
              exp_values[i] = exp_values[i].strip()
              try:
                 self.experiments[i].ratios[gene_name.upper()] = float(exp_values[i])
              except:
                 print "Warning: Expression value in " + self.experiments[i].file + " on line " + \
                       str(i) + " will not read in as a float: " + exp_values[i] + "\n"
                 self.experiments[i].ratios[gene_name.upper()] = exp_values[i]

      def read_rowexp(input_file, type):
          """This function reads in files that are columns of genes by rows
          of experiments.  These are usually wildtype and multifactorial data"""

          print "Loading " + input_file + " as rows of experiments."
          file = open(input_file, 'r')
          file = file.readlines()

          header = line_split.split(file.pop().replace('"',''))

          gene_list = header

          for i, exprow in enumerate(file):
              # Instantiate the experiment datatypes
              exp = Experiment(string(i), input_file, type)
              self.experiments.append(exp)

              for j, val in enumerate(exprow.split()):
                  gene_name = gene_list[j]
                  try:
                      self.experiments[i].ratios[gene_name.upper()] = float(exp_values[i])
                  except:
                      print "Warning: Expression value in " + self.experiments[i].file + " on line " + str(i) + " will not read in as a float: " + exp_values[i] + "\n"
                      self.experiments[i].ratios[gene_name.upper()] = exp_values[i]


      parse_file(input_file, type)
      self.gene_list = []
      for e in self.experiments:
          self.gene_list = self.gene_list + e.ratios.keys()
      self.gene_list = list(set(self.gene_list)) # Setify list

