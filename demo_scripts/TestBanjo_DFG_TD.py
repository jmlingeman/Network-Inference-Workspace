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

from tdaracne import *
from dfg4grn import *
from banjo import *

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

# Read data into program
timeseries_storage = ReadData(ts_filenames[0], True)

# Get config file for tdaracne
settings = ReadConfig(settings, "./config/default_values/tdaracne.cfg")
settings = ReadConfig(settings, "./config/default_values/banjo.cfg")
settings = ReadConfig(settings, "./config/default_values/dfg4grn.cfg")
#settings = ReadConfig(settings, settings["tdaracne"]["config"])

# Setup job manager
jobman = JobManager(settings)

# Make tdaracne jobs
bjob = tdaracne()
bjob.setup(timeseries_storage, settings, "tdaracne-test-run-1")
jobman.queueJob(bjob)

trans_factors = TFList(timeseries_storage[0].gene_list)
settings["dfg4grn"]["eta_z"] = 0.01
settings["dfg4grn"]["lambda_w"] = 0.001
settings["dfg4grn"]["tau"] = 2
dfg = DFG4GRN()
dfg.setup(timeseries_storage, trans_factors, settings, "dfg4grn-test-run-1")
jobman.queueJob(dfg)

bjob = banjo()
bjob.setup(timeseries_storage, settings, "banjo-test-run-1")
jobman.queueJob(bjob)


print jobman.queue
jobman.runQueue()
jobman.waitToClear()

accs = []

for job in jobman.finished:
    print job.alg.gene_list
    print job.alg.read_output(settings)
    jobnet = job.alg.network
    #jobnet = Network()
    #jobnet.read_netmatrix(job.alg.network, job.alg.gene_list, True)
    accs.append(jobnet.calcAcc(goldnet))

print accs

