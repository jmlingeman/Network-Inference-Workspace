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

#exp_data_directory = 'datasets/GeneNetWeaver Data Limit Exp_2'
#timeseries_filename = 'insilico_size100_1_dream4_timeseries.tsv'
#wildtype_filename = 'insilico_size100_1_wildtype.tsv'
#knockout_filename = 'insilico_size100_1_knockouts.tsv'
#knockdown_filename = 'insilico_size100_1_knockdowns.tsv'
#multifactorial_filename = 'insilico_size100_1_multifactorial.tsv'
#goldstandard_filename = 'insilico_size100_1_goldstandard.tsv'
#exp_name = "11t_20rep_dream4100"

exp_data_directory = 'datasets/DREAM4_InSilico_Size100'
timeseries_filename = 'insilico_size100_1_timeseries.tsv'
wildtype_filename = 'insilico_size100_1_wildtype.tsv'
knockout_filename = 'insilico_size100_1_knockouts.tsv'
knockdown_filename = 'insilico_size100_1_knockdowns.tsv'
multifactorial_filename = 'insilico_size100_1_multifactorial.tsv'
goldstandard_filename = 'DREAM4_GoldStandard_InSilico_Size100_1.tsv'
exp_name = "insilico_size100_1"

#exp_data_directory = 'datasets/GeneNetWeaver Data Limit Exp_3'
#timeseries_filename = 'Yeast-100-1_dream4_timeseries.tsv'
#wildtype_filename = 'Yeast-100-1_wildtype.tsv'
#knockout_filename = 'Yeast-100-1_knockouts.tsv'
#knockdown_filename = 'Yeast-100-1_knockdowns.tsv'
#multifactorial_filename = 'Yeast-100-1_multifactorial.tsv'
#goldstandard_filename = 'Yeast-100-1_goldstandard.tsv'
#exp_name = "100Gene-6rep-5t"

#exp_data_directory = 'datasets/GeneNetWeaver Data Limit Exp_5'
#timeseries_filename = 'Yeast-100-1_dream4_timeseries.tsv'
#wildtype_filename = 'Yeast-100-1_wildtype.tsv'
#knockout_filename = 'Yeast-100-1_knockouts.tsv'
#knockdown_filename = 'Yeast-100-1_knockdowns.tsv'
#multifactorial_filename = 'Yeast-100-1_multifactorial.tsv'
#goldstandard_filename = 'Yeast-100-1_goldstandard.tsv'
#exp_name = "100Gene-12rep-5t"
#exp_name = "test"


# Initialize settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'
settings["global"]["experiment_name"] = "BestOfBreed_" + exp_name

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
    break



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
goldnet.read_goldstd(goldnets[exp_name])


ts_storage = data[exp_name]
settings["global"]["time_series_delta_t"] = int((1008.0 / (len(ts_storage[0].experiments)-1)))
print settings["global"]["time_series_delta_t"]



#dfg = DFG4GRN(settings)
#settings["dfg4grn"]["eta_z"] = 0.1
#settings["dfg4grn"]["lambda_w"] = 0.001
#settings["dfg4grn"]["tau"] = 3
#print settings["global"]["time_series_delta_t"]
#dfg.setup(ts_storage, TFList(tfs[exp_name]), settings, "DFG-{1}_LambdaW-{0}".format(0.1, exp_name), 1)
#jobman.queueJob(dfg)
percfp = 0
permfp = 1
perctp = 0
permtp = 1




genie3job = GENIE3()
genie3job.setup(multifactorials[exp_name], settings, "Genie3_Multifactorial")
jobman.queueJob(genie3job)

mczjob = MCZ()
mczjob.setup(knockouts[exp_name], wildtypes[exp_name], settings, None, None, "MCZ_Knockout")
jobman.queueJob(mczjob)


#mcznet = mczjob.network.copy()
#genie3net = genie3job.network.copy()

#mcznet.set_top_edges_percent(10)
#genie3net.set_top_edges_percent(10)

#for job in jobman.finished:
    #job.alg.network.set_top_edges_percent(10)
    #job.alg.network.normalize()

priors = []
for job in jobman.finished:
    priors.append([job.alg.alg_name, job.alg.network])


meth = "MEN"
inf = Inferelator2(settings)
settings["inferelator2"]["num_cores"] = 16
settings["inferelator2"]["num_bootstraps"] = 50
settings["inferelator2"]["num_bootstraps"] = 3
settings["inferelator2"]["permtp"] = permtp
settings["inferelator2"]["permfp"] = permfp
settings["inferelator2"]["nCv"] = 10
settings["inferelator2"]["perctp"] = perctp
settings["inferelator2"]["percfp"] = percfp
settings["inferelator2"]["method"] = meth
inf.setup(knockouts[exp_name], wildtypes[exp_name], settings, ts_storage, knockdowns[exp_name], "Inferelator2_Time_Series".format(meth, exp_name), goldnet)
jobman.queueJob(inf)



jobman.runQueue()
jobman.waitToClear()

for job in jobman.finished:
    job.alg.network.normalize()


tprs, fprs, rocs = GenerateMultiROC(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallROC.pdf")
ps, rs, precs = GenerateMultiPR(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallPR.pdf")

SaveResults(jobman.finished, goldnet, settings)

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

