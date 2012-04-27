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

from tdaracne import *



from ReadConfig import *
# Initialize settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'
settings["global"]["experiment_name"] = "TDARACNE-"+sys.argv[1]
if len(sys.argv) > 2:
  settings["global"]["experiment_name"] += "-" + sys.argv[2]

goldnet = Network()
goldnet.read_goldstd("datasets/dream4_10/dream4_10_gold.tsv")

# Create date string to append to output_dir
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
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

timeseries_storage = ReadData(ts_file[0], "timeseries")
# Get config file for tdaracne
settings = ReadConfig(settings, "./config/default_values/tdaracne.cfg")
#settings = ReadConfig(settings, settings["tdaracne"]["config"])

# Setup job manager
jobman = JobManager(settings)

#for i in range(4,20):
for i in [4, 6, 7, 9]:
    # Make tdaracne jobs
    settings["tdaracne"]["num_bins"] = i
    bjob = tdaracne()
    bjob.setup(timeseries_storage, settings, "{0}_Bins".format(i))
    jobman.queueJob(bjob)

#bjob = tdaracne()
#bjob.setup(timeseries_storage, settings, "tdaracne-test-run-2")
#jobman.queueJob(bjob)

print jobman.queue
jobman.runQueue()
jobman.waitToClear()

SaveResults(jobman.finished, goldnet, settings, "Overall", 4)

