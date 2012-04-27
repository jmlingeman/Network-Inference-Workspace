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
from Helpers import *
from mcz import *


from ReadConfig import *

settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'

# Read in the gold standard network
goldnet = Network()
#goldnet.read_goldstd(settings["global"]["large_network_goldnet_file"])
ko_file, kd_file, ts_file, wt_file, mf_file, goldnet = get_example_data_files(sys.argv[1], settings)
knockout_storage = ReadData(ko_file[0], "knockout")
knockdown_storage = ReadData(kd_file[0], "knockdown")
timeseries_storage = ReadData(ts_file[0], "timeseries")
wildtype_storage = ReadData(wt_file[0], "wildtype")


# Create date string to append to output_dir
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])

# Get config file for tdaracne
settings = ReadConfig(settings, "./config/default_values/tdaracne.cfg")
settings = ReadConfig(settings, "./config/default_values/banjo.cfg")
settings = ReadConfig(settings, "./config/default_values/dfg4grn.cfg")
#settings = ReadConfig(settings, settings["tdaracne"]["config"])

# Setup job manager
jobman = JobManager(settings)

mczjob = MCZ()
mczjob.setup(knockout_storage, wildtype_storage, settings, timeseries_storage, knockdown_storage, "MCZ_Alone")
jobman.queueJob(mczjob)

print jobman.queue
jobman.runQueue()
jobman.waitToClear()

bjob = Banjo()
bjob.setup(timeseries_storage, settings, "banjo-test-run-1", mczjob.network)
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

