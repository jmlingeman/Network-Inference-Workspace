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

from inferelator import *
from ReadConfig import *

# Instantsiate settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'
settings["global"]["experiment_name"] = "TLCLR-DFG4grn-"+sys.argv[1]



# Create date string to append to output_dir
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["experiment_name"] = "InferelatorTest"
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])

# Read in the gold standard network

# Read in the gold standard network
goldnet = Network()
#goldnet.read_goldstd(settings["global"]["large_network_goldnet_file"])

if sys.argv[1] == "small":
    goldnet.read_goldstd(settings["global"]["small_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["small_network_knockout_file"].split()
    kd_file = settings["global"]["small_network_knockdown_file"].split()
    ts_file = settings["global"]["small_network_timeseries_file"].split()
    wt_file = settings["global"]["small_network_wildtype_file"].split()

if sys.argv[1] == "medium":
    goldnet.read_goldstd(settings["global"]["medium_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["medium_network_knockout_file"].split()
    kd_file = settings["global"]["medium_network_knockdown_file"].split()
    ts_file = settings["global"]["medium_network_timeseries_file"].split()
    wt_file = settings["global"]["medium_network_wildtype_file"].split()

if sys.argv[1] == "medium2":
    goldnet.read_goldstd(settings["global"]["medium2_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["medium2_network_knockout_file"].split()
    kd_file = settings["global"]["medium2_network_knockdown_file"].split()
    ts_file = settings["global"]["medium2_network_timeseries_file"].split()
    wt_file = settings["global"]["medium2_network_wildtype_file"].split()

if sys.argv[1] == "dream410":
    goldnet.read_goldstd(settings["global"]["dream410_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["dream410_network_knockout_file"].split()
    kd_file = settings["global"]["dream410_network_knockdown_file"].split()
    ts_file = settings["global"]["dream410_network_timeseries_file"].split()
    wt_file = settings["global"]["dream410_network_wildtype_file"].split()

if sys.argv[1] == "dream4100":
    goldnet.read_goldstd(settings["global"]["dream4100_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["dream4100_network_knockout_file"].split()
    kd_file = settings["global"]["dream4100_network_knockdown_file"].split()
    ts_file = settings["global"]["dream4100_network_timeseries_file"].split()
    wt_file = settings["global"]["dream4100_network_wildtype_file"].split()

# Read data into program
# Where the format is "FILENAME" "DATATYPE"
knockout_storage = ReadData(ko_file[0], "knockout")
knockdown_storage = ReadData(kd_file[0], "knockdown")
timeseries_storage = ReadData(ts_file[0], "timeseries")
wildtype_storage = ReadData(wt_file[0], "wildtype")



# Setup job manager
jobman = JobManager(settings)

# Make BANJO jobs
infjob = Inferelator()
infjob.setup(None, wildtype_storage, settings, timeseries_storage, None, "Inferelator")
jobman.queueJob(infjob)

print jobman.queue
jobman.runQueue()
jobman.waitToClear()

accs = []
precs = []

#dfg = DFG4GRN()
#dfg.setup(timeseries_storage,  TFList(timeseries_storage[0].gene_list), settings, "DFG4GRN_Baseline", 20)
#jobman.queueJob(dfg)




SaveResults(jobman.finished, goldnet, settings)

#print "AOCS"
#for r in rocs:
    #print r.auc()
