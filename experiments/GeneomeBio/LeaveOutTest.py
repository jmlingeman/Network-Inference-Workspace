import os, sys
from datetime import datetime
from DataStore import *
from ReadData import *
from JobManager import *
from Network import *
from Generate_Grid import *
from ReadConfig import *
from AnalyzeResults import *
from Helpers import *
import multiprocessing

def get_immediate_subdirectories(dir):
    return [name for name in os.listdir(dir) if os.path.isdir(os.path.join(dir, name))]

sys.path += get_immediate_subdirectories("./")

from inferelator_pipeline import *
from inferelator2 import *
from mcz import *
from genie3 import *

# Initialize settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'
settings["global"]["experiment_name"] = "GenBio-Test"

settings["global"]["n_processors"] = multiprocessing.cpu_count()

# Set up output directory
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])

jobman = JobManager(settings)

if sys.argv[1] == "100":
    exp_data_directory = 'datasets/GeneNetWeaver Data Limit Exp_6'
    exp_name = "100Gene-12rep-5t"
    timeseries_filename = 'insilico_size100_1_dream4_timeseries.tsv'
    wildtype_filename = 'insilico_size100_1_wildtype.tsv'
    knockout_filename = 'insilico_size100_1_knockouts.tsv'
    knockdown_filename = 'insilico_size100_1_knockdowns.tsv'
    multifactorial_filename = 'insilico_size100_1_multifactorial.tsv'
    goldstandard_filename = 'insilico_size100_1_goldstandard.tsv'
else:
    exp_data_directory = 'datasets/GeneNetWeaver Data Limit Exp_7'
    exp_name = "20Gene-12rep-5t"
    timeseries_filename = 'insilico_size20-1_dream4_timeseries.tsv'
    wildtype_filename = 'insilico_size20-1_wildtype.tsv'
    knockout_filename = 'insilico_size20-1_knockouts.tsv'
    knockdown_filename = 'insilico_size20-1_knockdowns.tsv'
    multifactorial_filename = 'insilico_size20-1_multifactorial.tsv'
    goldstandard_filename = 'insilico_size20-1_goldstandard.tsv'

# Read in gold standard network
goldnet = Network()

# Set up output directory
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])

# Read in configs for this algorithm
from dfg4grn import *
settings = ReadConfig(settings, "./config/default_values/dfg4grn.cfg")
settings = ReadConfig(settings, settings["dfg4grn"]["config"])

data = {}
knockouts = {}
wildtypes = {}
knockdowns = {}
multifactorials = {}
goldnets = {}
# Loop over the directories we want, reading in the timeseries files
for name in os.listdir(exp_data_directory):
    if name[0] == ".":
        continue
    data[name] = ReadData(exp_data_directory + '/' + name + '/' + timeseries_filename, "timeseries")
    #for ts in data[name]:
        #ts.normalize()
    knockouts[name] = ReadData(exp_data_directory + '/' + name + '/' + knockout_filename, "knockout")
    #knockouts[name].normalize()
    knockdowns[name] = ReadData(exp_data_directory + '/' + name + '/' + knockdown_filename, "knockdown")
    #knockdowns[name].normalize()
    wildtypes[name] = ReadData(exp_data_directory + '/' + name + '/' + wildtype_filename, "wildtype")
    #wildtypes[name].normalize()
    multifactorials[name] = ReadData(exp_data_directory + '/' + name + '/' + multifactorial_filename, "multifactorial")
    #multifactorials[name].normalize()
    goldnets[name] = exp_data_directory + '/' + name + '/' + goldstandard_filename

ts_storage = data[exp_name]

# Read data into program
# Where the format is "FILENAME" "DATATYPE
# Remove the last time point for testing
leave_out = []
for i, ts in enumerate(ts_storage):
    leave_out.append(ts.experiments[-1])


#goldnet = Network()
#goldnet.read_goldstd(goldnets[exp_name])


#settings["global"]["time_series_delta_t"] = int((1008.0 / (len(ts_storage[0].experiments)-1)))
#print settings["global"]["time_series_delta_t"]


percfp = 0
permfp = 1
perctp = 0
permtp = 1




genie3job = GENIE3()
genie3job.setup(multifactorials[exp_name], settings, "Genie3_Multifactorial")
jobman.queueJob(genie3job)

#mczjob = MCZ()
#mczjob.setup(knockouts[exp_name], cnlo_storage, settings, None, None, "MCZ_Knockout")
#jobman.queueJob(mczjob)

jobman.runQueue()
jobman.waitToClear()

#mcznet = mczjob.network.copy()
genie3net = genie3job.network.copy()

#mcznet.set_top_edges_percent(35)
genie3net.set_top_edges_percent(35)

#for job in jobman.finished:
    #job.alg.network.set_top_edges_percent(10)
    #job.alg.network.normalize()

priors = []
for job in jobman.finished:
    priors.append([job.alg.alg_name, job.alg.network])

num_bootstraps = 50

meth = "MEN"
inf = Inferelator2(settings)
settings["inferelator2"]["num_cores"] = 16
settings["inferelator2"]["num_bootstraps"] = num_bootstraps
#settings["inferelator2"]["num_bootstraps"] = 1
settings["inferelator2"]["permtp"] = permtp
settings["inferelator2"]["permfp"] = permfp
settings["inferelator2"]["nCv"] = 10
settings["inferelator2"]["perctp"] = perctp
settings["inferelator2"]["percfp"] = percfp
settings["inferelator2"]["method"] = meth
inf.setup(None, None, settings, ts_storage, None, "Inferelator2_Time_Series", None, None, leave_out)
jobman.queueJob(inf)

percfp = 250
permfp = 5
perctp = 50
permtp = 1

#meth = "MEN"
#inf = Inferelator2(settings)
#settings["inferelator2"]["num_cores"] = 8
#settings["inferelator2"]["num_bootstraps"] = num_bootstraps
##settings["inferelator2"]["num_bootstraps"] = 1
#settings["inferelator2"]["permtp"] = 1
#settings["inferelator2"]["permfp"] = 1
#settings["inferelator2"]["nCv"] = 10
#settings["inferelator2"]["prior_weight"] = 0
#settings["inferelator2"]["perctp"] = 0
#settings["inferelator2"]["percfp"] = 0
#settings["inferelator2"]["method"] = meth
#inf.setup(None, c4d, settings, ts_storage, dex_storage, "Inferelator2_Multifactorial", None, None, leave_out)
#jobman.queueJob(inf)


meth = "MEN"
inf = Inferelator2(settings)
settings["inferelator2"]["num_cores"] = 8
settings["inferelator2"]["num_bootstraps"] = num_bootstraps
#settings["inferelator2"]["num_bootstraps"] = 1
settings["inferelator2"]["permtp"] = permtp
settings["inferelator2"]["permfp"] = permfp
settings["inferelator2"]["nCv"] = 10
settings["inferelator2"]["prior_weight"] = 2.8
settings["inferelator2"]["perctp"] = perctp
settings["inferelator2"]["percfp"] = percfp
settings["inferelator2"]["method"] = meth
inf.setup(None, None, settings, ts_storage, None, "Inferelator2_Genie3_Prior", None, genie3net, leave_out)
jobman.queueJob(inf)



jobman.runQueue()
jobman.waitToClear()

for job in jobman.finished:
    job.alg.network.normalize()


#tprs, fprs, rocs = GenerateMultiROC(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallROC.pdf")
#ps, rs, precs = GenerateMultiPR(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallPR.pdf")

#SaveResults(jobman.finished, None, settings)

#results = []
#for job in jobman.finished:
    #if job.alg.alg_name == "dfg4grn":
        #results.append([job.alg.name, job.alg.best_sign, job.alg.best_sign_all, job.alg.avg_sign, job.alg.avg_sign_all])

#out = open(settings["global"]["output_dir"] + "/results.txt",'w')
#for r in results:
    #out.write(r[0] + "\t" + str(r[1]) + "\t" + str(r[2]) + "\t"+str(r[3])+"\t"+str(r[4])+"\n")

#out = open(settings["global"]["output_dir"] + "/results-sorted.txt",'w')
#results.sort(key=lambda x: x[0])
#for r in results:
    #out.write(r[0] + "\t" + str(r[1]) + "\t" + str(r[2]) + "\t"+str(r[3])+"\t"+str(r[4])+"\n")

print "\a"
print "\a"
print "\a"

