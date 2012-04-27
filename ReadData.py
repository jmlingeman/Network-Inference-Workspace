import re
from DataStore import *

def ReadData(input_file, type):

      line_split = re.compile('[\t,]')
      def read_genenetweaver(input_file, type):
          # In these data files it is column ordered,
          # so the first column is time (for time series)
          # and the rest are the genes.  Separate time series
          # experiments are delimited by a \n row.
          print "Loading " + input_file + " as GeneNetWeaver type."
          file = open(input_file, 'r')
          file = file.readlines()

          header = line_split.split(file.pop(0).replace('"',''))

          datasets = []
          # TODO: Add in non-timeseries GNW files
          if type.lower() == "timeseries":
              file.pop(0) # Burn first line
              genes = header[1:] # removing the time column from the header
              for ds in xrange(file.count('\n')):
                  input_file = "Timeseries-Rep" + str(ds) + ".csv"

                  # Read in each MicroArray
                  datasets.append(MicroarrayData(input_file, type, ds))
                  for i in range(len(genes)):
                      genes[i] = genes[i].strip()
                  datasets[ds].gene_list = map(str.strip,genes)
              #print datasets
              sep_exps = []
              # Separate the different time series
              prev_row = 0
              for exp in xrange(len(datasets)):
                  #print prev_row, file.index('\n')
                  sep_exps.append(file[prev_row:file.index('\n')])
                  file = file[file.index('\n') + 1:len(file)]
              # Create the experiments for each time point in the
              # MicroarrayData class
              for i, exp in enumerate(sep_exps):
                 for j, line in enumerate(exp):
                    ls = line_split.split(line)
                    #print ls
                    datasets[i].experiments.append(Experiment(ls[0].strip(), input_file, type))
                    for k, gene in enumerate(genes):
                        datasets[i].experiments[j].ratios[genes[k]] = ls[k+1].strip()

          # Return steady state dataset
          else:
              genes = map(str.strip, header)
              microarray = MicroarrayData(input_file, type)
              microarray.gene_list = genes
              expmatrix = {}
              for row in file:
                  expressions = row.split()
                  for i, e in enumerate(expressions):
                      if genes[i] not in expmatrix.keys():
                          expmatrix[genes[i]] = []
                      expmatrix[genes[i]].append(e)
              for i in range(len(expmatrix[genes[0]])):
                  exp = Experiment(str(i), input_file, type)
                  for gene in genes:
                      exp.ratios[gene] = expmatrix[gene][i]
                  microarray.experiments.append(exp)

              return microarray


          return datasets


 #     def read_simple(input_file, type):
        #"""This function is to read simple files that are just the
        #header with the experiment name and then rows with:
        #gene_name\texpression value"""

        #dataset = MicroarrayData(input_file, type)

        #print "Loading " + input_file + " as simple file type."
        #file = open(input_file, 'r')
        #file = file.readlines()

        ## TODO: Add support for different experiments in different files
        #header = line_split.split(file[0])

        #datasets = []

        #names = []
        #for i, exp_name in enumerate(header):
           #if exp_name.strip() == "":
               #continue
           #exp = Experiment(exp_name.strip(), input_file, type)
           ##exp.ratios = {}

           #datasets.experiments.append(exp)
           ##print "Found new experiment: " + exp.name

        #for line in file[1:]:
           #if len(line.strip()) <= 1 or line.strip()[0] == "#":
              #continue
           #line = line_split.split(line)
           #gene_name = line[0]
           #exp_values = line[1:len(datasets.experiments)+1]
           #for i in xrange(len(exp_values)):
              #exp_values[i] = exp_values[i].strip()
              #try:
                 #datasets.experiments[i].ratios[gene_name.upper()] = float(exp_values[i])
              #except:
                 #print "Warning: Expression value in " + datasets.experiments[i].file + " on line " + \
                       #str(i) + " will not read in as a float: " + exp_values[i] + "\n"
                 #datasets.experiments[i].ratios[gene_name.upper()] = exp_values[i]

      return read_genenetweaver(input_file, type)

