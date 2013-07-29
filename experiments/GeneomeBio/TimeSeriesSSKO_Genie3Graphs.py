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
import random
import multiprocessing

def get_immediate_subdirectories(dir):
    return [name for name in os.listdir(dir) if os.path.isdir(os.path.join(dir, name))]

sys.path += get_immediate_subdirectories("./")

from inferelator_pipeline import *
from inferelator2 import *
from genie3 import *

#exp_data_directory = 'datasets/GeneNetWeaver Data Limit Exp_2'
#timeseries_filename = 'insilico_size100_1_dream4_timeseries.tsv'
#wildtype_filename = 'insilico_size100_1_wildtype.tsv'
#knockout_filename = 'insilico_size100_1_knockouts.tsv'
#knockdown_filename = 'insilico_size100_1_knockdowns.tsv'
#goldstandard_filename = 'insilico_size100_1_goldstandard.tsv'

#exp_data_directory = 'datasets/GeneNetWeaver Data Limit Exp_5'
#timeseries_filename = 'Yeast-100-1_dream4_timeseries.tsv'
#wildtype_filename = 'Yeast-100-1_wildtype.tsv'
#knockout_filename = 'Yeast-100-1_knockouts.tsv'
#knockdown_filename = 'Yeast-100-1_knockdowns.tsv'
#goldstandard_filename = 'Yeast-100-1_goldstandard.tsv'


exp_data_directory = 'datasets/GENIE3_Graphs'
exp_name = "100Gene-12rep-5t"
timeseries_filename = 'insilico_size100_1_dream4_timeseries.tsv'
wildtype_filename = 'insilico_size100_1_wildtype.tsv'
knockout_filename = 'insilico_size100_1_knockouts.tsv'
knockdown_filename = 'insilico_size100_1_knockdowns.tsv'
multifactorial_filename = 'insilico_size100_1_multifactorial.tsv'
goldstandard_filename = 'insilico_size100_1_goldstandard.tsv'

#exp_name = "11t_20rep_dream4100"

# Initialize settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'
settings["global"]["experiment_name"] = "GENIE3_Graphs_" + sys.argv[1]

settings["global"]["n_processors"] = multiprocessing.cpu_count()

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
goldnets = {}
multifactorials = {}
timeseries_as_steady_state = {}
# Loop over the directories we want, reading in the timeseries files
for name in os.listdir(exp_data_directory):
    if name[0] == ".":
        continue
    data[name] = ReadData(exp_data_directory + '/' + name + '/' + timeseries_filename, "timeseries")

    timeseries_as_steady_state[name] = ReadData(exp_data_directory + '/' + name + '/' + timeseries_filename, "timeseries")
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

jobman = JobManager(settings)



# Get TFS from the goldstandard
tfs = {}
for name in data.keys():
    t = []
    goldnet = Network()
    goldnet.read_goldstd(goldnets[name])
    for gene1 in goldnet.network:
        for gene2 in goldnet.network[gene1]:
            if goldnet.network[gene1][gene2] > 0:
                t.append(gene1)
    tfs[name] = list(set(t))

for key in goldnets.keys():
    goldnet = Network()
    goldnet.read_goldstd(goldnets[key])
    goldnets[key] = goldnet



genie3nets = {}
for name in data.keys():
    ts_storage = data[name]
    settings["global"]["time_series_delta_t"] = (1008.0 / (len(ts_storage[0].experiments)-1))
    combined = timeseries_as_steady_state[name][0]
    for ts in timeseries_as_steady_state[name][1:]:
        combined.combine(ts)
    combined.combine(knockouts[name])
    combined.combine(multifactorials[name])

    genie3job = GENIE3()
    genie3job.setup(combined, settings, "Genie3_TimeSeries_{0}".format(name))
    jobman.queueJob(genie3job)
    genie3nets[name] = genie3job
    genie3job.goldnet = goldnets[name]


jobman.runQueue()
jobman.waitToClear()


for job in jobman.finished:
    job.alg.network.normalize()

tprs, fprs, rocs = GenerateMultiROC(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallROC.pdf")
ps, rs, precs = GenerateMultiPR(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallPR.pdf")

for job in jobman.finished:
    tprs, fprs, rocs = GenerateMultiROC([job], job.alg.goldnet, False, settings["global"]["output_dir"] + "/" + job.alg.name + ".pdf")
    ps, rs, precs = GenerateMultiPR([job], job.alg.goldnet, False, settings["global"]["output_dir"] + "/" + job.alg.name + ".pdf")

SaveResults(jobman.finished, goldnet, settings)

high_noise = []
low_noise = []
med_noise = []

high_rep = []
low_rep = []
med_rep = []

for job in jobman.finished:
    if "12" in job.alg.name or "6" in job.alg.name:
        high_rep.append(job)
    elif "4" in job.alg.name or "2" in job.alg.name:
        low_rep.append(job)
    else:
        med_rep.append(job)

for job in jobman.finished:
    if "high" in job.alg.name:
        high_noise.append(job)
    elif "low" in job.alg.name:
        low_noise.append(job)
    else:
        med_noise.append(job)

tprs, fprs, rocs = GenerateMultiROC(low_noise, goldnet, False, settings["global"]["output_dir"] + "/LowNoiseROC.pdf")
ps, rs, precs = GenerateMultiPR(low_noise, goldnet, False, settings["global"]["output_dir"] + "/LowNoisePR.pdf")

tprs, fprs, rocs = GenerateMultiROC(med_noise, goldnet, False, settings["global"]["output_dir"] + "/MedNoiseROC.pdf")
ps, rs, precs = GenerateMultiPR(med_noise, goldnet, False, settings["global"]["output_dir"] + "/MedNoisePR.pdf")

tprs, fprs, rocs = GenerateMultiROC(high_noise, goldnet, False, settings["global"]["output_dir"] + "/HighNoiseROC.pdf")
ps, rs, precs = GenerateMultiPR(high_noise, goldnet, False, settings["global"]["output_dir"] + "/HighNoisePR.pdf")

tprs, fprs, rocs = GenerateMultiROC(low_rep, goldnet, False, settings["global"]["output_dir"] + "/LowRepROC.pdf")
ps, rs, precs = GenerateMultiPR(low_rep, goldnet, False, settings["global"]["output_dir"] + "/LowRepPR.pdf")

tprs, fprs, rocs = GenerateMultiROC(med_rep, goldnet, False, settings["global"]["output_dir"] + "/MedRepROC.pdf")
ps, rs, precs = GenerateMultiPR(med_rep, goldnet, False, settings["global"]["output_dir"] + "/MedRepPR.pdf")

tprs, fprs, rocs = GenerateMultiROC(high_rep, goldnet, False, settings["global"]["output_dir"] + "/HighRepROC.pdf")
ps, rs, precs = GenerateMultiPR(high_rep, goldnet, False, settings["global"]["output_dir"] + "/HighrepPR.pdf")

print len(high_noise), len(low_noise), len(med_noise)

results = []
for job in jobman.finished:
    if job.alg.alg_name == "dfg4grn":
        results.append([job.alg.name, job.alg.best_sign, job.alg.best_sign_all, job.alg.avg_sign, job.alg.avg_sign_all])

out = open(settings["global"]["output_dir"] + "/results.txt",'w')
for r in results:
    out.write(r[0] + "\t" + str(r[1]) + "\t" + str(r[2]) + "\t"+str(r[3])+"\t"+str(r[4])+"\n")

out = open(settings["global"]["output_dir"] + "/results-sorted.txt",'w')
results.sort(key=lambda x: x[0])
for r in results:
    out.write(r[0] + "\t" + str(r[1]) + "\t" + str(r[2]) + "\t"+str(r[3])+"\t"+str(r[4])+"\n")

print "\a"
print "\a"
print "\a"

