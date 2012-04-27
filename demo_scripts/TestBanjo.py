import os, sys
from datetime import datetime
from DataStore import *
from ReadData import *
from JobManager import *
from Network import *
from Generate_Grid import *
from ReadConfig import *
from AnalyzeResults import *

def get_immediate_subdirectories(dir):
    return [name for name in os.listdir(dir) if os.path.isdir(os.path.join(dir, name))]

sys.path += get_immediate_subdirectories("./")

from banjo import *
# Initialize settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'
settings["global"]["experiment_name"] = "BANJO-"+sys.argv[1]
if len(sys.argv) > 2:
  settings["global"]["experiment_name"] += "-" + sys.argv[2]

# Read in gold standard network
goldnet = Network()

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

if sys.argv[1] == "dream410_1000TP":
    goldnet.read_goldstd(settings["global"]["dream410_1000tp_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["dream410_1000tp_network_knockout_file"].split()
    kd_file = settings["global"]["dream410_1000tp_network_knockdown_file"].split()
    ts_file = settings["global"]["dream410_1000tp_network_timeseries_file"].split()
    wt_file = settings["global"]["dream410_1000tp_network_wildtype_file"].split()

if sys.argv[1] == "dream4100_1000TP":
    goldnet.read_goldstd(settings["global"]["dream4100_1000tp_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["dream4100_1000tp_network_knockout_file"].split()
    kd_file = settings["global"]["dream4100_1000tp_network_knockdown_file"].split()
    ts_file = settings["global"]["dream4100_1000tp_network_timeseries_file"].split()
    wt_file = settings["global"]["dream4100_1000tp_network_wildtype_file"].split()

timeseries_storage = ReadData(ts_file[0], "timeseries")


# Set up output directory
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])


# Get config file for Banjo
settings = ReadConfig(settings, "./config/default_values/banjo.cfg")
#settings = ReadConfig(settings, settings["banjo"]["config"])

# Setup job manager
jobman = JobManager(settings)

# Make BANJO jobs
settings["banjo"]["discretization_policy"] = "q4"
settings["banjo"]["max_time"] = "1"
bjob = Banjo()
bjob.setup(timeseries_storage, settings, "banjo_" + settings["banjo"]["discretization_policy"] )
jobman.queueJob(bjob)

settings["banjo"]["discretization_policy"] = "q3"
bjob = Banjo()
bjob.setup(timeseries_storage, settings, "banjo_" + settings["banjo"]["discretization_policy"] )
jobman.queueJob(bjob)

settings["banjo"]["discretization_policy"] = "q2"
bjob = Banjo()
bjob.setup(timeseries_storage, settings, "banjo_" + settings["banjo"]["discretization_policy"] )
jobman.queueJob(bjob)

settings["banjo"]["discretization_policy"] = "q5"
bjob = Banjo()
bjob.setup(timeseries_storage, settings, "banjo_" + settings["banjo"]["discretization_policy"] )
jobman.queueJob(bjob)

settings["banjo"]["discretization_policy"] = "q6"
bjob = Banjo()
bjob.setup(timeseries_storage, settings, "banjo_" + settings["banjo"]["discretization_policy"] )
jobman.queueJob(bjob)

settings["banjo"]["discretization_policy"] = "q7"
bjob = Banjo()
bjob.setup(timeseries_storage, settings, "banjo_" + settings["banjo"]["discretization_policy"] )
jobman.queueJob(bjob)

settings["banjo"]["discretization_policy"] = "q8"
bjob = Banjo()
bjob.setup(timeseries_storage, settings, "banjo_" + settings["banjo"]["discretization_policy"] )
jobman.queueJob(bjob)

settings["banjo"]["discretization_policy"] = "q9"
bjob = Banjo()
bjob.setup(timeseries_storage, settings, "banjo_" + settings["banjo"]["discretization_policy"] )
jobman.queueJob(bjob)

print jobman.queue
jobman.runQueue()
jobman.waitToClear()

SaveResults(jobman.finished, goldnet, settings, "Overall", 4)

