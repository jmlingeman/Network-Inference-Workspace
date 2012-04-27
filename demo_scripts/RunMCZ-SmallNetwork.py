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

from mcz import *
from ReadConfig import *

# Instantsiate settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'

# Read in the gold standard network
goldnet = Network()
goldnet.read_goldstd(settings["global"]["small_network_goldnet_file"])

# Create date string to append to output_dir
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])

# Get a list of the knockout files
ko_file = settings["global"]["small_network_knockout_file"].split()

wt_file = settings["global"]["small_network_wildtype_file"].split()

# Read data into program
# Where the format is "FILENAME" "DATATYPE"
knockout_storage = ReadData(ko_file[0], "knockout")
wildtype_storage = ReadData(wt_file[0], "wildtype")

print knockout_storage.experiments

# Setup job manager
jobman = JobManager(settings)

# Make BANJO jobs
mczjob = MCZ()
mczjob.setup(knockout_storage, wildtype_storage, settings, "mcz-test-run-1")
jobman.queueJob(mczjob)

print jobman.queue
jobman.runQueue()
jobman.waitToClear()

accs = []

for job in jobman.finished:
    for i in range(1,15):
        settings["mcz"]["top_n_edges"] = i
        print job.alg.gene_list
        print job.alg.read_output(settings)
        jobnet = job.alg.network
        print "PREDICTED NETWORK:"
        print job.alg.network.network
        print "GOLDEN NETWORK:"
        print goldnet.network
        #jobnet = Network()
        #jobnet.read_netmatrix(job.alg.network, job.alg.gene_list, True)
        accs.append(jobnet.calculateAccuracy(goldnet))
        print jobnet.analyzeMotifs(goldnet).ToString()

print accs

