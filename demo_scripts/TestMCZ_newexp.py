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

from mcz import *
from dfg4grn import *
from ReadConfig import *

# Instantsiate settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'
settings["global"]["experiment_name"] = "MCZ-testexp"



# Create date string to append to output_dir
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])

# Read in the gold standard network

# Read in the gold standard network
goldnet = Network()
#goldnet.read_goldstd(settings["global"]["large_network_goldnet_file"])
#ko_file, kd_file, ts_file, wt_file, mf_file, goldnet = get_example_data_files(sys.argv[1], settings)

ko_file = "algorithms/genenetweaver/InSilicoSize10-Ecoli1_knockouts.tsv"
kd_file = "algorithms/genenetweaver/InSilicoSize10-Ecoli1_knockdowns.tsv"
wt_file = "algorithms/genenetweaver/InSilicoSize10-Ecoli1_wildtype.tsv"
ts_file = "algorithms/genenetweaver/InSilicoSize10-Ecoli1_dream4_timeseries.tsv"
goldnet.read_goldstd("algorithms/genenetweaver/InSilicoSize10-Ecoli1_goldstandard.tsv")


# Read data into program
# Where the format is "FILENAME" "DATATYPE"
knockout_storage = ReadData(ko_file, "knockout")
knockdown_storage = ReadData(kd_file, "knockdown")
timeseries_storage = ReadData(ts_file, "timeseries")
wildtype_storage = ReadData(wt_file, "wildtype")



# Setup job manager
jobman = JobManager(settings)

# Make MCZ job
mczjob = MCZ()
mczjob.setup(knockout_storage, wildtype_storage, settings, timeseries_storage, knockdown_storage, "MCZ")
jobman.queueJob(mczjob)

print jobman.queue
jobman.runQueue()
jobman.waitToClear()


tprs, fprs, rocs = GenerateMultiROC(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallROC.pdf")
ps, rs, precs = GenerateMultiPR(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallPR.pdf")

SaveResults(jobman.finished, goldnet, settings)
