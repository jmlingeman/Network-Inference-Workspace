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
from Helpers import *

from genie3 import *
from ReadConfig import *

# Instantsiate settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'

# Read in gold standard network
goldnet = Network()
#goldnet.read_goldstd(settings["global"]["large_network_goldnet_file"])
settings["global"]["experiment_name"] = "GENIE3" + sys.argv[1]
ko_file, kd_file, ts_file, wt_file, mf_file, goldnet = get_example_data_files(sys.argv[1], settings)# Create date string to append to output_dir
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])

# Get a list of the multifactorial files

# Read data into program
# Where the format is "FILENAME" "DATATYPE"
mf_storage = ReadData(mf_file[0], "multifactorial")
ko_storage = ReadData(ko_file[0], "knockout")
kd_storage = ReadData(kd_file[0], "knockdown")
wt_storage = ReadData(wt_file[0], "wildtype")

# Setup job manager
jobman = JobManager(settings)

# Make GENIE3 jobs
genie3job = GENIE3()
genie3job.setup(mf_storage, settings, "MF")
jobman.queueJob(genie3job)

mf_storage.combine(ko_storage)
genie3job = GENIE3()
genie3job.setup(mf_storage, settings, "MF_KO")
jobman.queueJob(genie3job)

mf_storage.combine(wt_storage)
genie3job = GENIE3()
genie3job.setup(mf_storage, settings, "MF_KO_WT")
jobman.queueJob(genie3job)

mf_storage.combine(kd_storage)
genie3job = GENIE3()
genie3job.setup(mf_storage, settings, "MF_KO_WT_KD")
jobman.queueJob(genie3job)

print jobman.queue
jobman.runQueue()
jobman.waitToClear()


SaveResults(jobman.finished, goldnet, settings)
