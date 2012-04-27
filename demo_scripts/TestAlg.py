import os, sys
from datetime import datetime
from DataStore import *
from ReadData import *
from JobManager import *
from Network import *
from Generate_Grid import *

def get_immediate_subdirectories(dir):
    return [name for name in os.listdir(dir) if os.path.isdir(os.path.join(dir, name))]

sys.path += get_immediate_subdirectories("./")

from ReadConfig import *
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'

goldnet = Network()
goldnet.read_goldstd("datasets/dream4_10/dream4_10_gold.tsv")

# Create date string to append to output_dir
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])

ts_filenames = settings["global"]["time_series_files"].split()
delta_t = [50]*20
settings["global"]["time_series_delta_t"] = delta_t
#delta_t = settings["global"]["time_series_delta_t"].split()
print delta_t
timeseries_storage = []

timeseries_storage = ReadData(ts_filenames[0], True)
print timeseries_storage

trans_factors = TFList(timeseries_storage[0].gene_list[0:6])

print trans_factors.tfs

print ts_filenames
print delta_t

from dfg4grn import *

settings = ReadConfig(settings, "./config/default_values/dfg4grn.cfg")
settings = ReadConfig(settings, settings["dfg4grn"]["config"])
grid = Generate_Grid("dfg4grn", None, settings, ["eta_z", "lambda_w", "tau"], \
        25).test_list

jobman = JobManager(settings)
for p in grid:
   settings["dfg4grn"]["eta_z"] = p[0]
   settings["dfg4grn"]["lambda_w"] = p[1]
   settings["dfg4grn"]["tau"] = p[2]
   dfg = DFG4GRN(timeseries_storage, trans_factors, settings, "dfg4grn-" + str(p[0]) +"." + str(p[1]) +"."+ str(p[2]) )
   jobman.queueJob(dfg)

print jobman.queue
jobman.runQueue()
jobman.waitToClear()

accs = []

for job in jobman.finished:
    print job.alg.gene_list
    print job.alg.gather_output(settings)
    jobnet = Network()
    jobnet.read_netmatrix(job.alg.network, job.alg.gene_list, True)
    accs.append(jobnet.calcAcc(goldnet))

print accs

