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

from tlclr import *
from ReadConfig import *

# Instantsiate settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'
settings["global"]["experiment_name"] = "TLCLR-DFG4grn-"+sys.argv[1]



# Create date string to append to output_dir
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])

# Read in the gold standard network

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

# Make BANJO jobs
tlclrjob = TLCLR()
tlclrjob.setup(knockout_storage, wildtype_storage, settings, timeseries_storage, knockdown_storage, "TLCLR_All_Data")
jobman.queueJob(tlclrjob)

tlclrjob = TLCLR()
tlclrjob.setup(knockout_storage, wildtype_storage, settings, timeseries_storage, None, "TLCLR_No_KD")
jobman.queueJob(tlclrjob)

#tlclrjob = TLCLR()
#tlclrjob.setup(None, wildtype_storage, settings, timeseries_storage, knockdown_storage, "TLCLR_No_KO")
#jobman.queueJob(tlclrjob)

#tlclrjob = TLCLR()
#tlclrjob.setup(None, wildtype_storage, settings, timeseries_storage, None, "TLCLR_No_KO_or_KD")
#jobman.queueJob(tlclrjob)


print jobman.queue
jobman.runQueue()
jobman.waitToClear()

accs = []
precs = []

#dfg = DFG4GRN()
#dfg.setup(timeseries_storage,  TFList(timeseries_storage[0].gene_list), settings, "DFG4GRN_Baseline", 20)
#jobman.queueJob(dfg)


#accs.append("TLCLR:")
for job in jobman.finished:
    #threshnet = job.alg.network.copy()
    #threshnet.network = threshnet.apply_threshold(0)
    #accs.append((job.alg.name, threshnet.calculateAccuracy(goldnet)))

    #pre, rec, area = GeneratePR(job.alg.network, goldnet, True, False, job.alg.name)
    #precs.append((job.alg.name, area))
    #for i in range(8, 10):
    #for i in [15,20,25,30,35,5,3,1,2,50]:
    #num_edge_list = [x for x in range(21)]
    #num_edge_list += [ 25, 30, 45, 50, 60, 70, 100, 150 ]
    num_edge_list = [ 20,15,60,150,50,30,300 ]
    for i in num_edge_list:
        #print job.alg.read_output(settings)

        topnet = job.alg.network.copy()
        topnet.set_top_edges(i)
        accs.append(("TLCLR-Top_{0}_Edges".format(i), topnet.calculateAccuracy(goldnet)))
        print "Running DFG4GRN with i = {0}".format(i)
        #dfg = DFG4GRN()
        #dfg.setup(timeseries_storage, TFList(timeseries_storage[0].gene_list), settings, "TLCLR-DFG4GRN_{0}-Edges_Connections".format(i), 20, None, topnet, 'connections')
        #jobman.queueJob(dfg)
        #dfg = DFG4GRN()
        #dfg.setup(timeseries_storage, TFList(timeseries_storage[0].gene_list), settings, "TLCLR-DFG4GRN_{0}-Edges_Weights".format(i), 20, None, topnet, 'weights')
        #jobman.queueJob(dfg)
        #dfg = DFG4GRN()
        #dfg.setup(timeseries_storage, TFList(timeseries_storage[0].gene_list), settings, "TLCLR-DFG4GRN_{0}-Edges_BothPriors".format(i), 20, None, topnet, 'both')
        #jobman.queueJob(dfg)

jobman.runQueue()
jobman.waitToClear()
rocs = []
labels = []
recs = []
for i,job in enumerate(jobman.finished):
    if job.alg.alg_name == "dfg4grn":
      #jobnet = job.alg.network_cutoff
      jobnet = job.alg.zscores
    else:
      jobnet = job.alg.network
    labels.append(job.alg.name)
    print "PREDICTED NETWORK:"
    print jobnet.network
    print "GOLDEN NETWORK:"
    print goldnet.network
    #jobnet.normalize_values()
    #if i == len(jobman.finished)-1:
        #tpr, fpr, roc_auc = GenerateROC(jobnet, goldnet, True, True)
        #pre, rec, area = GeneratePR(jobnet, goldnet, True, True, job.alg.name)
    #else:
        #tpr, fpr, roc_auc = GenerateROC(jobnet, goldnet, True, False)
        #pre, rec, area = GeneratePR(jobnet, goldnet, True, False, job.alg.name)
    #precs.append((job.alg.name, area))
    #rocs.append((job.alg.name, roc_auc))
    #rocs.append(GenerateROC(jobnet, goldnet))

    threshnet = jobnet.copy()
    #threshnet.network = threshnet.apply_threshold(0)
    accs.append((job.alg.name, threshnet.calculateAccuracy(goldnet)))
    #print jobnet.analyzeMotifs(goldnet).ToString()

import pickle

tprs, fprs, rocs = GenerateMultiROC(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallROC.pdf")
ps, rs, precs = GenerateMultiPR(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallPR.pdf")

print "Accuracy:"
for row in accs:
    print row

print "ROC Data:"
for row in rocs:
    print row

print "PR Data:"
for row in precs:
    print row

pickle.dump((jobman.finished, accs, rocs, precs), open(settings["global"]["output_dir"] + "./TLCLR-DFG4GRN.pickle", 'w'))

outfile = open(settings["global"]["output_dir"] + "./TLCLR-DFG_Results.csv",'w')
header = "ExpName," + ",".join(accs[0][1].keys()) + ",auroc" + ",aupr" + "\n"
file = header
for i, row in enumerate(accs):
    file += row[0] + ','
    for key in row[1].keys():
        file += str(row[1][key]) + ','
    for a in rocs:
      if a[0] == row[0]:
        file += str(a[1]) + ','
    for a in precs:
      if a[0] == row[0]:
        file += str(a[1])
    file += "\n"

outfile.write(file)
outfile.close()


SaveResults(jobman.finished, goldnet, settings)
#print "AOCS"
#for r in rocs:
    #print r.auc()
