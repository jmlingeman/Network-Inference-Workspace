import os, sys
from datetime import datetime
from DataStore import *
from ReadData import *
from JobManager import *
from Network import *
from Generate_Grid import *

def get_immediate_subdirectories(dir):
    return [name for name in os.listdir(dir) if os.path.isdir(os.path.join(dir, name))]

sys.path += get_immediate_subdirectories("./")

from ReadConfig import *
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'

goldnet = Network()
goldnet.read_goldstd(settings["global"]["small_network_goldnet_file"])

# Create date string to append to output_dir
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])

knockdown_filenames = settings["global"]["small_network_knockdown_file"].split()

knockdown_storage = ReadData(knockdown_filenames[0], "knockdown")

from nirest import *

settings = ReadConfig(settings, "./config/default_values/nirest.cfg")
settings = ReadConfig(settings, settings["nirest"]["config"])

jobman = JobManager(settings)

nirrun = NIRest()
nirrun.setup(knockdown_storage, settings, "nirest-test")
jobman.queueJob(nirrun)

print jobman.queue
jobman.runQueue()
jobman.waitToClear()

accs = []

for job in jobman.finished:
    print job.alg.gene_list
    print job.alg.read_output(settings)
    print job.alg.network.calculateAccuracy(goldnet)
    report = job.alg.network.analyzeMotifs(goldnet)
    print report.ToString()


print accs

