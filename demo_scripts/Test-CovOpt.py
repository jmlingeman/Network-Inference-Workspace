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
from convex_optimization import *
from ReadConfig import *

# Instantsiate settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'
settings["global"]["experiment_name"] = "Convex-Opt-"+sys.argv[1]



# Create date string to append to output_dir
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-MCZ-ConvOpt-PositivePrior-Medium-" + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])

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

cojob = ConvexOptimization()
t = 0.005
cojob.setup(knockout_storage, settings, "ConvOpt_T-"+ str(t),None, None, t)
jobman.queueJob(cojob)
for t in range(0,10):
    cojob = ConvexOptimization()
    t = t / 100.0
    cojob.setup(knockout_storage, settings, "ConvOpt_T-"+ str(t),None, None, t)
    jobman.queueJob(cojob)

#accs.append("MCZ:")
#for job in jobman.finished:
    ##threshnet = job.alg.network.copy()
    ##threshnet.network = threshnet.apply_threshold(0)
    ##accs.append((job.alg.name, threshnet.calculateAccuracy(goldnet)))

    ##pre, rec, area = GeneratePR(job.alg.network, goldnet, True, False, job.alg.name)
    ##precs.append((job.alg.name, area))
    ##for i in range(8, 10):
    ##for i in [15,20,25,30,35,5,3,1,2,50]:
    #num_edge_list = [x for x in range(21)]
    ##num_edge_list += [ 25, 30, 45, 50, 55, 60, 65, 70 ]
    #num_edge_list = [70, 80, 50, 10]
    #for i in num_edge_list:
        ##print job.alg.read_output(settings)

        #topnet = job.alg.network.copy()
        #topnet.set_top_edges(i)
        #accs.append(("MCZ-Top_{0}_Edges".format(i), topnet.calculateAccuracy(goldnet)))
        #print "Running ConvOpt with i = {0}".format(i)
        #cojob = ConvexOptimization()
        #cojob.setup(knockout_storage, settings, "MCZ-ConvOpt_Test_Top_{0}_Edges".format(i), topnet)
        #jobman.queueJob(cojob)

jobman.runQueue()
jobman.waitToClear()

SaveResults(jobman.finished, goldnet, settings, "Overall", 4)
