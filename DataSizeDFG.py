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

exp_data_directory = 'datasets/GeneNetWeaver Data Limit Exp_2'
timeseries_filename = 'insilico_size100_1_dream4_timeseries.tsv'
wildtype_filename = 'insilico_size100_1_wildtype.tsv'
knockout_filename = 'insilico_size100_1_knockouts.tsv'
knockdown_filename = 'insilico_size100_1_knockdowns.tsv'
goldstandard_filename = 'insilico_size100_1_goldstandard.tsv'

# Initialize settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'
settings["global"]["experiment_name"] = "Data_Limit_Experiment-DFG"

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
# Loop over the directories we want, reading in the timeseries files
for name in os.listdir(exp_data_directory):
    data[name] = ReadData(exp_data_directory + '/' + name + '/' + timeseries_filename, "timeseries")
    for tp in data[name]:
        tp.normalize()
    knockouts[name] = ReadData(exp_data_directory + '/' + name + '/' + knockout_filename, "knockout")
    knockouts[name].normalize()
    knockdowns[name] = ReadData(exp_data_directory + '/' + name + '/' + knockdown_filename, "knockdown")
    knockdowns[name].normalize()
    wildtypes[name] = ReadData(exp_data_directory + '/' + name + '/' + wildtype_filename, "wildtype")
    wildtypes[name].normalize()
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

for name in data.keys():

    ts_storage = data[name]
    settings["global"]["time_series_delta_t"] = (1000.0 / (len(ts_storage[0].experiments)-1))
    print settings["global"]["time_series_delta_t"]
    percfps = [0, 100, 250, 500]
    permfps = [1, 5, 5, 5]
    #for i in range(len(percfps)):
    for i in range(1):
        for meth in ["MEN", "BBSR"]:
        #for meth in ["MEN"]:
            percfp = percfps[i]
            permfp = permfps[i]
            perctp = 50
            permtp = 1
            inf = Inferelator2(settings)
            settings["inferelator2"]["num_cores"] = 1
            #settings["inferelator2"]["num_bootstraps"] = 50
            settings["inferelator2"]["num_bootstraps"] = 1
            settings["inferelator2"]["permtp"] = permtp
            settings["inferelator2"]["permfp"] = permfp
            settings["inferelator2"]["perctp"] = perctp
            settings["inferelator2"]["percfp"] = percfp
            settings["inferelator2"]["method"] = meth
            inf.setup(knockouts[name], wildtypes[name], settings, ts_storage, knockdowns[name], "Inferelator2-{4}-permtp-{0}_permfp-{1}_perctp-{2}_percfp-{3}_meth-{5}".format(permtp, permfp, perctp, percfp, name, meth), goldnet)
            jobman.queueJob(inf)

            #inf = Inferelator2(settings)
            #settings["inferelator2"]["num_cores"] = 1
            #settings["inferelator2"]["num_bootstraps"] = 50
            #settings["inferelator2"]["permtp"] = permtp
            #settings["inferelator2"]["permfp"] = permfp
            #settings["inferelator2"]["perctp"] = perctp
            #settings["inferelator2"]["percfp"] = percfp
            #settings["inferelator2"]["method"] = meth
            #inf.setup(None, wildtypes[name], settings, ts_storage, knockdowns[name], "Inferelator2-noko-{4}-permtp-{0}_permfp-{1}_perctp-{2}_percfp-{3}_meth-{5}".format(permtp, permfp, perctp, percfp, name, meth), goldnet)
            #jobman.queueJob(inf)

            #inf = Inferelator2(settings)
            #settings["inferelator2"]["num_cores"] = 1
            #settings["inferelator2"]["num_bootstraps"] = 50
            #settings["inferelator2"]["permtp"] = permtp
            #settings["inferelator2"]["permfp"] = permfp
            #settings["inferelator2"]["perctp"] = perctp
            #settings["inferelator2"]["percfp"] = percfp
            #settings["inferelator2"]["method"] = meth
            #inf.setup(knockouts[name], wildtypes[name], settings, ts_storage, None, "Inferelator2-nokd-{4}-permtp-{0}_permfp-{1}_perctp-{2}_percfp-{3}_meth-{5}".format(permtp, permfp, perctp, percfp, name, meth), goldnet)
            #jobman.queueJob(inf)

            #inf = Inferelator2(settings)
            #settings["inferelator2"]["num_cores"] = 1
            #settings["inferelator2"]["num_bootstraps"] = 50
            #settings["inferelator2"]["permtp"] = permtp
            #settings["inferelator2"]["permfp"] = permfp
            #settings["inferelator2"]["perctp"] = perctp
            #settings["inferelator2"]["percfp"] = percfp
            #settings["inferelator2"]["method"] = meth
            #inf.setup(None, wildtypes[name], settings, ts_storage, None, "Inferelator2-noko-nokd-{4}-permtp-{0}_permfp-{1}_perctp-{2}_percfp-{3}_meth-{5}".format(permtp, permfp, perctp, percfp, name, meth), goldnet)
            #jobman.queueJob(inf)


    #dfg = DFG4GRN(settings)
    #settings["dfg4grn"]["eta_z"] = 0.1
    #settings["dfg4grn"]["lambda_w"] = 0.001
    #settings["dfg4grn"]["tau"] = 3
    #print settings["global"]["time_series_delta_t"]
    #dfg.setup(ts_storage, TFList(tfs[name]), settings, "DFG-{1}_LambdaW-{0}".format(0.1, name), 1)
    #jobman.queueJob(dfg)

    #infjob = InferelatorPipeline()
    #infjob.setup(knockouts[name], wildtypes[name], settings, ts_storage, None, "InferelatorPipeline-{0}".format(name))
    #jobman.queueJob(infjob)

    break
    #infjob = InferelatorPipeline()
    #infjob.setup(knockouts[name], wildtypes[name], settings, ts_storage, knockdowns[name], "InferelatorPipeline-{0}-{1}".format(name, "with_kd"))
    #jobman.queueJob(infjob)

    #infjob = InferelatorPipeline()
    #infjob.setup(None, wildtypes[name], settings, ts_storage, None, "InferelatorPipeline-{0}-{1}".format(name, "no_ko"))
    #jobman.queueJob(infjob)
    #infjob = InferelatorPipeline()
    #infjob.setup(knockouts[name], None, settings, ts_storage, None, "InferelatorPipeline-{0}-{1}".format(name, "no_wt"))
    #jobman.queueJob(infjob)

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

