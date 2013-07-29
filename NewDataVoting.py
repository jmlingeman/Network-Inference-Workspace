import os, sys
def get_immediate_subdirectories(dir):
    return [name for name in os.listdir(dir) if os.path.isdir(os.path.join(dir, name))]

sys.path += get_immediate_subdirectories("./")
from datetime import datetime
from DataStore import *
from ReadData import *
from JobManager import *
from Network import *
from Generate_Grid import *
from AnalyzeResults import *

from mcz import *
from nir import *
from dfg4grn import *
from tlclr import *
from clr import *
from genie3 import *
from tdaracne import *
from convex_optimization import *
from ReadConfig import *
from Helpers import *

# Instantsiate settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'

ko_file, kd_file, ts_file, wt_file, mf_file, goldnet = get_example_data_files(sys.argv[1], settings)

# Create date string to append to output_dir
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["experiment_name"] = "VotingPredictionTest-{0}-{1}".format(sys.argv[1], sys.argv[2])
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])

# Get a list of the multifactorial files

# Read data into program
# Where the format is "FILENAME" "DATATYPE"
mf_storage = ReadData(mf_file[0], "multifactorial")
knockout_storage = ReadData(ko_file[0], "knockout")
knockdown_storage = ReadData(kd_file[0], "knockdown")
wildtype_storage = ReadData(wt_file[0], "wildtype")
timeseries_storage = ReadData(ts_file[0], "timeseries")
gene_list = knockout_storage.gene_list

# Setup job manager
jobman = JobManager(settings)

# MCZ
mczjob = MCZ()
mczjob.setup(knockout_storage, wildtype_storage, settings, timeseries_storage, knockdown_storage, "MCZ")
jobman.queueJob(mczjob)

# CLR
clrjob = CLR()
clrjob.setup(knockout_storage, settings, "CLR", "plos", 6)
jobman.queueJob(clrjob)

# GENIE3
mf_storage.combine(knockout_storage)
mf_storage.combine(wildtype_storage)
mf_storage.combine(knockdown_storage)
genie3job = GENIE3()
genie3job.setup(mf_storage, settings, "GENIE3")
jobman.queueJob(genie3job)

## TLCLR
tlclrjob = TLCLR()
tlclrjob.setup(knockout_storage, wildtype_storage, settings, timeseries_storage, knockdown_storage, "TLCLR")
jobman.queueJob(tlclrjob)

#if sys.argv[1] != "dream4100":
    #cojob = ConvexOptimization()
    #cojob.setup(knockout_storage, settings, "ConvOpt_T-"+ str(0.01),None, None, 0.01)
    #jobman.queueJob(cojob)

### DFG4GRN
dfg = DFG4GRN()
settings["dfg4grn"]["eta_z"] = 0.01
settings["dfg4grn"]["lambda_w"] = 0.001
settings["dfg4grn"]["tau"] = 3
dfg.setup(timeseries_storage, TFList(timeseries_storage[0].gene_list), settings, "DFG", 20)
jobman.queueJob(dfg)

### Inferelator

### NIR
nirjob = NIR()
nirjob.setup(knockout_storage, settings, "NIR", 5, 5)
jobman.queueJob(nirjob)

#### TDARACNE
#settings = ReadConfig(settings, "./config/default_values/tdaracne.cfg")
#bjob = tdaracne()
#settings["tdaracne"]["num_bins"] = 4
#bjob.setup(timeseries_storage, settings, "TDARACNE")
#jobman.queueJob(bjob)


print jobman.queue
jobman.runQueue()
jobman.waitToClear(sys.argv[1])

# Gather networks

# Send to voting algorithm
for job in jobman.finished:
  if "mcz" in job.alg.name.lower():
    mczjob = job

SaveResults(jobman.finished, goldnet, settings)

votejob = MCZ()
votejob.setup(knockout_storage, wildtype_storage, settings, timeseries_storage, knockdown_storage, "Voting")
jobman.queueJob(votejob)
votejob = jobman.queue[0]

print "All jobs finished. Creating voting network..."
results = []
if sys.argv[1] == "dream4100":
    step = 10
else:
    step = 1
#for i in range(5,len(gene_list) * len(gene_list)-1, step):
  #votejob.alg.name = "Voting Top-" + str(i)
  #print "On iteration {0} of {1}".format(i, len(gene_list)**2)
  #results.append((i, SaveVotingNetwork(jobman.finished, votejob, mczjob, goldnet, settings, False, i, None, True)))

votejob.alg.name = "Voting Top"
#results.append((i, SaveVotingNetwork(jobman.finished, votejob, mczjob, goldnet, settings, False, 0, None, True)))


#print "Sorting..."
#sorted_results = sorted(results, key=lambda x: x[1][1], reverse=True)
#for result in sorted_results[0:8]:
  #print result
#print "Saving graphs..."
#topn = sorted_results[0][0]
##votejob.alg.network = VotingNetwork(jobman.finished, votejob.alg.gene_list, sorted_results[0][0])
#votejob.alg.name = "Voting Top-" + str(topn)
##SaveResults([votejob], goldnet, settings, "Voting")
##print votejob.alg.network.network
#print "Saving voting graphs..."
results.append(SaveVotingNetwork(jobman.finished, votejob, mczjob, goldnet, settings, False, 0, None, True))
SaveVotingNetwork(jobman.finished, votejob, mczjob, goldnet, settings, True, 0, None, True)
f = open(settings["global"]["output_dir"] + "/TopVoting.csv", 'w')
#for result in sorted_results:
for result in results:
    f.write(str(result) + "\n")
print results
