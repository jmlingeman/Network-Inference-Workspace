import os, sys
from datetime import datetime
from DataStore import *
from ReadData import *
from JobManager import *
from Network import *
from Generate_Grid import *
from ReadConfig import *
from AnalyzeResults import *
from Helpers import *

def get_immediate_subdirectories(dir):
    return [name for name in os.listdir(dir) if os.path.isdir(os.path.join(dir, name))]

sys.path += get_immediate_subdirectories("./")

# Initialize settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'
settings["global"]["experiment_name"] = "DFG4GRN-"+sys.argv[1]
if len(sys.argv) > 2:
  settings["global"]["experiment_name"] += "-" + sys.argv[2]

# Read in gold standard network
goldnet = Network()

ko_file, kd_file, ts_file, wt_file, mf_file, goldnet = get_example_data_files(sys.argv[1], settings)

timeseries_storage = ReadData(ts_file[0], "timeseries")


# Set up output directory
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])



# Read in configs for this algorithm
from dfg4grn import *
settings = ReadConfig(settings, "./config/default_values/dfg4grn.cfg")
settings = ReadConfig(settings, settings["dfg4grn"]["config"])
grid = Generate_Grid("dfg4grn", None, settings, ["eta_z", "lambda_w", "tau"], \
        5).test_list

jobman = JobManager(settings)

dfg = DFG4GRN()
settings["dfg4grn"]["eta_z"] = 0.1
settings["dfg4grn"]["lambda_w"] = 0.01
settings["dfg4grn"]["tau"] = 3.5
dfg.setup(timeseries_storage, TFList(timeseries_storage[0].gene_list), settings, "EtaZ-{0}_LamdaW-{1}_Tau-{2}".format(0.1, 0.01, 3.5), 20)
jobman.queueJob(dfg)

dfg = DFG4GRN()
settings["dfg4grn"]["eta_z"] = 0.01
settings["dfg4grn"]["lambda_w"] = 0.001
settings["dfg4grn"]["tau"] = 3
dfg.setup(timeseries_storage, TFList(timeseries_storage[0].gene_list), settings, "EtaZ-{0}_LamdaW-{1}_Tau-{2}".format(0.01, 0.001, 3), 20)
jobman.queueJob(dfg)
for i, p in enumerate(grid):
    settings["dfg4grn"]["eta_z"] = p[0]
    settings["dfg4grn"]["lambda_w"] = p[1]
    settings["dfg4grn"]["tau"] = p[2]
    settings["dfg4grn"]["edge_cutoff"] = 0.75
    dfg = DFG4GRN()
    dfg.setup(timeseries_storage, TFList(timeseries_storage[0].gene_list), settings, "EtaZ-{0}_LamdaW-{1}_Tau-{2}".format(p[0], p[1], p[2]), 20)
    jobman.queueJob(dfg)

print jobman.queue
jobman.runQueue()
jobman.waitToClear()

accs = []

for job in jobman.finished:
    jobnet = job.alg.zscores
    print job.alg.raw_zscores_mask
    accs.append((job.alg.name, jobnet.calculateAccuracy(goldnet)))
    #accs.append(jobnet.analyzeMotifs(goldnet))
    #print jobnet.analyzeMotifs(goldnet).ToString()

import pickle

tprs, fprs, rocs = GenerateMultiROC(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallROC.pdf")
ps, rs, precs = GenerateMultiPR(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallPR.pdf")

print "Accuracy:"
for row in accs:
    print row

print "ROC Data:"
for row in rocs:
    print row

print "PR Data:"
for row in precs:
    print row

pickle.dump((jobman.finished, accs, rocs, precs), open(settings["global"]["output_dir"] + "./DFG4GRN.pickle", 'w'))

outfile = open(settings["global"]["output_dir"] + "./DFG_Results.csv",'w')
header = "ExpName," + ",".join(accs[0][1].keys()) + ",auroc" + ",aupr" + "\n"
file = header
for i, row in enumerate(accs):
    file += row[0] + ','
    for key in row[1].keys():
        file += str(row[1][key]) + ','
    for a in rocs:
      if a[0] == row[0]:
        file += str(a[1]) + ','
    for a in precs:
      if a[0] == row[0]:
        file += str(a[1]) + "\n"

outfile.write(file)
outfile.close()

print jobman.finished[0].alg.network.network
print jobman.finished[0].alg.zscores_mask.network
print goldnet.network

combos = []
for gene1 in jobman.finished[0].alg.network.gene_list:
    for gene2 in jobman.finished[0].alg.network.gene_list:
        combos.append((jobman.finished[0].alg.network.network[gene1][gene2], goldnet.network[gene1][gene2]))
print sorted(combos, key=lambda combo: combo[0])

SaveResults(jobman.finished, goldnet, settings, "Overall", 4)
#print accs

