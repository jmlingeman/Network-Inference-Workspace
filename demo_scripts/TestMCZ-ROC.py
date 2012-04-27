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



# Create date string to append to output_dir
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])

# Read in the gold standard network
goldnet = Network()
goldnet.read_goldstd(settings["global"]["medium_network_goldnet_file"])

#Get a list of the knockout files
ko_file = settings["global"]["medium_network_knockout_file"].split()
kd_file = settings["global"]["medium_network_knockdown_file"].split()
ts_file = settings["global"]["medium_network_timeseries_file"].split()

wt_file = settings["global"]["medium_network_wildtype_file"].split()

# Read in the gold standard network
#goldnet = Network()
#goldnet.read_goldstd(settings["global"]["large_network_goldnet_file"])

#ko_file = settings["global"]["large_network_knockout_file"].split()
#kd_file = settings["global"]["large_network_knockdown_file"].split()
#ts_file = settings["global"]["large_network_timeseries_file"].split()

#wt_file = settings["global"]["large_network_wildtype_file"].split()

# Read data into program
# Where the format is "FILENAME" "DATATYPE"
knockout_storage = ReadData(ko_file[0], "knockout")
knockdown_storage = ReadData(kd_file[0], "knockdown")
timeseries_storage = ReadData(ts_file[0], "timeseries")
wildtype_storage = ReadData(wt_file[0], "wildtype")
wildtype_storage.combine(knockout_storage)
wildtype_storage.combine(knockdown_storage)
wildtype_storage.combine(timeseries_storage)

wildtype_storage.normalize()
knockout_storage.normalize()


# Setup job manager
jobman = JobManager(settings)

# Make BANJO jobs
mczjob = MCZ()
mczjob.setup(knockout_storage, wildtype_storage, settings, None, "mcz-test-run-1")
jobman.queueJob(mczjob)

print jobman.queue
jobman.runQueue()
jobman.waitToClear()

accs = []

accs.append("MCZ:")
#for job in jobman.finished:
    #for i in range(1,25):
        #settings["mcz"]["top_n_edges"] = i
        #print job.alg.read_output(settings)

        #print "Running ConvOpt with i = {0}".format(i)
        #cojob = ConvexOptimization()
        #cojob.setup(knockout_storage, settings, "MCZ-DFG_Test_Top_{0}_Edges".format(i), job.alg.network)
        #jobman.queueJob(cojob)

jobman.runQueue()
jobman.waitToClear()
rocs = []
accs.append("Convex Opt + MCZ prior:")
for job in jobman.finished:
    jobnet = job.alg.network
    print "PREDICTED NETWORK:"
    print jobnet.network
    print "GOLDEN NETWORK:"
    print goldnet.network
    #jobnet.normalize_values()
    jobnet.normalize_values()
    print jobnet.network
    rocs.append(GenerateROC(jobnet, goldnet))
    threshnet = Network(jobnet)
    threshnet.network = threshnet.apply_threshold(0)
    accs.append(threshnet.calculateAccuracy(goldnet))
    #print jobnet.analyzeMotifs(goldnet).ToString()

PlotMultipleROC(rocs, 'ConvexOpt + MCZ')


for row in accs:
    print row

print "AOCS"
for r in rocs:
    print r.auc()
