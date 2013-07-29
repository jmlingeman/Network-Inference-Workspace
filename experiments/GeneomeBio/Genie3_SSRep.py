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

if sys.argv[2] == 'high':
    fold = "High Noise"
elif sys.argv[2] == 'low':
    fold = "Low Noise"
else:
    fold = "Med Noise"

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

if sys.argv[1] == "100":
    #exp_data_directory = 'datasets/GeneNetWeaver Data Limit Exp_6'
    exp_data_directory = 'datasets/100Genes-SSRep/' + fold + '/'
    exp_name = "100Gene-12rep-5t"
    timeseries_filename = 'insilico_size100_1_dream4_timeseries.tsv'
    wildtype_filename = 'insilico_size100_1_wildtype.tsv'
    knockout_filename = 'insilico_size100_1_knockouts.tsv'
    knockdown_filename = 'insilico_size100_1_knockdowns.tsv'
    multifactorial_filename = 'insilico_size100_1_multifactorial.tsv'
    goldstandard_filename = 'insilico_size100_1_goldstandard.tsv'
elif sys.argv[1] == "20":
    #exp_data_directory = 'datasets/GeneNetWeaver Data Limit Exp_7'
    exp_data_directory = 'datasets/20Genes-SSRep/' + fold + '/'
    exp_name = "20Gene-12rep-5t"
    timeseries_filename = 'insilico_size20-1_dream4_timeseries.tsv'
    wildtype_filename = 'insilico_size20-1_wildtype.tsv'
    knockout_filename = 'insilico_size20-1_knockouts.tsv'
    knockdown_filename = 'insilico_size20-1_knockdowns.tsv'
    multifactorial_filename = 'insilico_size20-1_multifactorial.tsv'
    goldstandard_filename = 'insilico_size20-1_goldstandard.tsv'
elif sys.argv[1] == "20small":
    #exp_data_directory = 'datasets/GeneNetWeaver Data Limit Exp_8'
    #exp_data_directory = 'datasets/100GenesNoise'
    exp_data_directory = 'datasets/100Genes-SS/' + fold + '/'
    exp_name = "20Gene-12rep-5t"
    timeseries_filename = 'insilico_size20-1_dream4_timeseries.tsv'
    wildtype_filename = 'insilico_size20-1_wildtype.tsv'
    knockout_filename = 'insilico_size20-1_knockouts.tsv'
    knockdown_filename = 'insilico_size20-1_knockdowns.tsv'
    multifactorial_filename = 'insilico_size20-1_multifactorial.tsv'
    goldstandard_filename = 'insilico_size20-1_goldstandard.tsv'


#exp_name = "11t_20rep_dream4100"

# Initialize settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'
settings["global"]["experiment_name"] = "GENIE3_SSRep-Only_" + fold+ "_" + sys.argv[1]

settings["global"]["n_processors"] = 6

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

goldnet = Network()
goldnet.read_goldstd(goldnets[data.keys()[0]])



genie3nets = {}
ts_storage = data[name]
settings["global"]["time_series_delta_t"] = (1008.0 / (len(ts_storage[0].experiments)-1))
#combined = timeseries_as_steady_state[name][0]
#for ts in timeseries_as_steady_state[name][1:]:
    #combined.combine(ts)

combined = multifactorials["1"]
combined.experiments = combined.experiments[:-60]
combined.combine(multifactorials["2"])
combined.experiments = combined.experiments[:-60]
combined.combine(multifactorials["3"])
combined.experiments = combined.experiments[:-60]
combined.combine(multifactorials["4"])
combined.experiments = combined.experiments[:-60]
#combined.combine(knockouts["1"])

for i in range(1):
    genie3job = GENIE3()
    genie3job.setup(combined, settings, "Genie3_SS_Replicates_{0}".format(fold))
    jobman.queueJob(genie3job)
    genie3nets[name] = genie3job


jobman.runQueue()
jobman.waitToClear()


for job in jobman.finished:
    job.alg.network.normalize()

tprs, fprs, rocs = GenerateMultiROC(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallROC.pdf")
ps, rs, precs = GenerateMultiPR(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallPR.pdf", True, "{0} Genes {1} - Steady State Replicates".format(len(goldnet.network), fold))

for job in jobman.finished:
    tprs, fprs, rocs = GenerateMultiROC([job], goldnet, False, settings["global"]["output_dir"] + "/" + job.alg.name + ".pdf")
    ps, rs, precs = GenerateMultiPR([job], goldnet, False, settings["global"]["output_dir"] + "/" + job.alg.name + ".pdf" )

accs, precs, rocs = SaveResults(jobman.finished, goldnet, settings)

results = []
for job in jobman.finished:
    results.append([job.alg.name, job.alg.network.num_correct_100, job.alg.network.per_correct_100, job.alg.network.num_correct_10p, job.alg.network.per_correct_10p])

print "Results:"
print results
#out = open(settings["global"]["output_dir"] + "/results.txt",'w')
#for r in results:
    #out.write(r[0] + "\t" + str(r[1]) + "\t" + str(r[2]) + "\t"+str(r[3])+"\t"+str(r[4])+"\n")

out = open(settings["global"]["output_dir"] + "/results-sorted.txt",'w')
results.sort(key=lambda x: x[3])
for r in results:
    out.write(r[0] + "\t" + str(r[1]) + "\t" + str(r[2]) + "\t" + str(r[3])+ "\t" + str(r[4])+  "\n")

print "\a"
print "\a"
print "\a"

high_aupr = []
med_aupr = []
low_aupr = []
for x in precs:
    if "High" in x[0]:
        high_aupr.append(x[1])
    elif "Low" in x[0]:
        low_aupr.append(x[1])
    else:
        med_aupr.append(x[1])

out.write("High Avg: " + str(pl.mean(high_aupr)) + "\n")
out.write("Med Avg: " + str(pl.mean(med_aupr)) + "\n")
out.write("Low Avg: " + str(pl.mean(low_aupr)) + "\n")
print "High Avg:", pl.mean(high_aupr)
print "Medium Avg:", pl.mean(med_aupr)
print "Low Avg:", pl.mean(low_aupr)
out.close()
