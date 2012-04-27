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
from WeightedConsensus import *

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
settings["global"]["experiment_name"] = "SimulatedAnnealing"
settings["global"]["working_dir"] = os.getcwd() + '/'

t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])
settings["global"]["output_dir_save"] = settings["global"]["output_dir"]

def get_network_results(name, settings, cache):
  print "STARTING", name

  if name in cache.keys():
    print "CACHE HIT"
    return cache[name]

  ko_file, kd_file, ts_file, wt_file, mf_file, goldnet = get_example_data_files(name, settings)

  # Create date string to append to output_dir
  t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
  settings["global"]["output_dir"] = settings["global"]["output_dir_save"] + "/" + \
      settings["global"]["experiment_name"] + "-" + t + "-" + name + "/"
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
  settings = ReadConfig(settings, "./config/default_values/tdaracne.cfg")
  bjob = tdaracne()
  settings["tdaracne"]["num_bins"] = 4
  bjob.setup(timeseries_storage, settings, "TDARACNE")
  jobman.queueJob(bjob)


  print jobman.queue
  jobman.runQueue()
  jobman.waitToClear(name)
  SaveResults(jobman.finished, goldnet, settings, name)

  cache[name] = jobman.finished[:]

  return cache[name]

# Gather networks
ko_file, kd_file, ts_file, wt_file, mf_file, goldnet = get_example_data_files(sys.argv[1], settings)

# Read data into program
# Where the format is "FILENAME" "DATATYPE"
mf_storage = ReadData(mf_file[0], "multifactorial")
knockout_storage = ReadData(ko_file[0], "knockout")
knockdown_storage = ReadData(kd_file[0], "knockdown")
wildtype_storage = ReadData(wt_file[0], "wildtype")
timeseries_storage = ReadData(ts_file[0], "timeseries")
gene_list = knockout_storage.gene_list
votejob = MCZ()
votejob.setup(knockout_storage, wildtype_storage, settings, timeseries_storage, knockdown_storage, "SimAnnealing")
jobman = JobManager(settings)
jobman.queueJob(votejob)
votejob = jobman.queue[0]
jobman.runQueue()
jobman.waitToClear("VotingJob")

# Send to voting algorithm
dream410 = ["dream410","dream410_2","dream410_3","dream410_4","dream410_5"]
#dream410 = ["dream410","dream410_2"]
dream4100 = ["dream4100","dream4100_2","dream4100_3","dream4100_4","dream4100_5"]
if sys.argv[1] == "dream410":
  networks = dream410
elif sys.argv[1] == "dream4100":
  networks = dream4100
else:
  networks = [sys.argv[1]]
results = []
cache = {}

for test_net in networks:
  training_jobs = []
  goldnets = []
  for name in networks:
    if name != test_net:
      training_jobs.append(get_network_results(name, settings, cache))
      ko_file, kd_file, ts_file, wt_file, mf_file, training_goldnet = get_example_data_files(name, settings)
      goldnets.append(training_goldnet)
      print cache



  sa = SimulatedAnnealing()

  best_weights, best_e = sa.run(training_jobs, goldnets)
  print "FINISHED:"
  print best_weights, best_e

  ko_file, kd_file, ts_file, wt_file, mf_file, test_goldnet = get_example_data_files(test_net, settings)
  roc, baseline, sa_net, baseline_net, basejob = sa.test(get_network_results(test_net, settings, cache), test_goldnet, settings)

  votejob.alg.network = sa_net

  t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
  settings["global"]["output_dir"] = settings["global"]["output_dir_save"] + "/" + \
  settings["global"]["experiment_name"] + "-" + t + "-AnnealingResultFor-" + test_net + "/"
  os.mkdir(settings["global"]["output_dir"])
  SaveResults([votejob,basejob], test_goldnet, settings, test_net)

  results.append((test_net,roc, baseline))

print results

baseline_sum = 0
result_sum = 0
f = open(settings["global"]["output_dir"] + "/sa_test.txt", 'w')
f.write("TestNet,ROC,Baseline_ROC\n")
for r in results:
  f.write(r[0] + "," + str(1 - (r[1] / 1000.0)) + "," + str(1 - (r[2] / 1000.0)) + "\n")
  baseline_sum += r[2]
  result_sum += r[1]

f.write(str(best_weights) + "\n")

baseline_avg = baseline_sum / len(results)
result_avg = result_sum / len(results)

f.write("\n\nBaseline Avg: " + str(baseline_avg))
f.write("\nResult Avg: " + str(result_avg))

