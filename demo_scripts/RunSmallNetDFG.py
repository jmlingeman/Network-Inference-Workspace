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
goldnet.read_goldstd("datasets/Small_Network/Ecoli-1_goldstandard.tsv")
print goldnet.network

# Create date string to append to output_dir
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])

settings["global"]["time_series_files"] = "datasets/Small_Network/Ecoli-1_dream4_timeseries.tsv"
ts_filenames = settings["global"]["time_series_files"].split()
delta_t = [50]*20
settings["global"]["time_series_delta_t"] = delta_t
#delta_t = settings["global"]["time_series_delta_t"].split()
print delta_t
timeseries_storage = []

timeseries_storage = ReadData(ts_filenames[0], "timeseries")
print timeseries_storage

trans_factors = TFList(timeseries_storage[0].gene_list[0:6])

print trans_factors.tfs

print ts_filenames
print delta_t

from dfg4grn import *

settings = ReadConfig(settings, "./config/default_values/dfg4grn.cfg")
settings = ReadConfig(settings, settings["dfg4grn"]["config"])
grid = Generate_Grid("dfg4grn", None, settings, ["eta_z", "lambda_w", "tau"], \
        40).test_list

jobman = JobManager(settings)
dfg = None
for p in grid:
    #settings["dfg4grn"]["eta_z"] = 0.001
    #settings["dfg4grn"]["lambda_w"] = 0.01
    #settings["dfg4grn"]["tau"] = 3
    print p
    dfg = DFG4GRN(settings)
    settings["dfg4grn"]["eta_z"] = p[0]
    settings["dfg4grn"]["lambda_w"] = p[1]
    settings["dfg4grn"]["tau"] = p[2]
    settings["dfg4grn"]["n_models"] = 20
    settings["dfg4grn"]["edge_cutoff"] = 0
    #dfg.setup(timeseries_storage, trans_factors, settings, "dfg4grn-small-net-tes")
    dfg.setup(timeseries_storage, trans_factors, settings, "dfg4grn-small-net-test_ETAZ-" + str(p[0]) + "_LW-"+str(p[1])+"_TAU-"+str(p[2]), 1)
    jobman.queueJob(dfg)

dfg = DFG4GRN(settings)
settings["dfg4grn"]["n_models"] = 20
settings["dfg4grn"]["eta_z"] = 0.001
settings["dfg4grn"]["lambda_w"] = 0.01
settings["dfg4grn"]["tau"] = 3
dfg.setup(timeseries_storage, trans_factors, settings, "dfg4grn-small-net-tes")
jobman.queueJob(dfg)

print jobman.queue
jobman.runQueue()
jobman.waitToClear()

accs = []
out = open("output_runs.txt", 'w')
csv_out = open("MastersThesis.csv", 'w')
header = 'k,n_models,lambda_w,eta_z,tau,tp,tn,fp,fn,sensitivity,specificity,accuracy,fanins_correct,fanins_incorrect,fanouts_correct,fanouts_incorrect,cascades_correct,cascades_incorrect,feedforward_loops_correct,feedforward_loops_incorrect\n'
csv_out.write(header)
for i, job in enumerate(jobman.finished):
    print job.alg.gene_list
    print job.alg.gather_output(settings)
    jobnet = Network()
    for k in xrange(len(job.alg.gene_list) * len(job.alg.gene_list) + 1):
        jobnet.read_netmatrix(job.alg.network, job.alg.gene_list, "timeseries")
        jobnet.cutoff_network(k)
        #print "\n\n\n\n\n\n"+"dfg4grn-small-net-test_ETAZ=" + str(p[0]) + "_LW="+str(p[1])+"_TAU="+str(p[2])
        MastersThesis = jobnet.calculateAccuracy(goldnet)
        report = jobnet.analyzeMotifs(goldnet)
        print report.ToString()
        out.write("dfg4grn-small-net-test_ETAZ-" + str(p[0]) + "_LW-"+str(p[1])+"_TAU-"+ str(p[2]) + "\n")
        out.write(str(jobnet.calculateAccuracy(goldnet)))
        out.write(report.ToString())
        out.write("\n\n\n")

        cstr = "{0},{1},{2},{3},{4},{5},{6},{7},{8},{9},{10},{11},{12},{13},{14},{15},{16},{17},{18},{19},{20},{21}\n".format(
        k,
        job.alg.n_models,
        job.alg.lambda_w,
        job.alg.eta_z,
        job.alg.tau,
        MastersThesis['tp'],
        MastersThesis['tn'],
        MastersThesis['fp'],
        MastersThesis['fn'],
        MastersThesis['sensitivity'],
        MastersThesis['specificity'],
        MastersThesis['accuracy'],
        MastersThesis['precision'],
        MastersThesis['recall'],
        report.fanins_correct,
        report.fanins_incorrect,
        report.fanouts_correct,
        report.fanouts_incorrect,
        report.cascades_correct,
        report.cascades_incorrect,
        report.feedforward_loops_correct,
        report.feedforward_loops_incorrect
        )

        csv_out.write(cstr)

#print accs

