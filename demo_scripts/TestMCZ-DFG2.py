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
from clr import *
from convex_optimization import *
from genie3 import *
from dfg4grn import *
from ReadConfig import *

# Instantsiate settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'
settings["global"]["experiment_name"] = "MCZ-DFG-Pipeline-"+sys.argv[1]
if len(sys.argv) > 2:
  settings["global"]["experiment_name"] += "-" + sys.argv[2]




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
    mf_file = settings["global"]["dream410_network_multifactorial_file"].split()

if sys.argv[1] == "dream4100":
    goldnet.read_goldstd(settings["global"]["dream4100_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["dream4100_network_knockout_file"].split()
    kd_file = settings["global"]["dream4100_network_knockdown_file"].split()
    ts_file = settings["global"]["dream4100_network_timeseries_file"].split()
    wt_file = settings["global"]["dream4100_network_wildtype_file"].split()
    mf_file = settings["global"]["dream4100_network_multifactorial_file"].split()

# Read data into program
# Where the format is "FILENAME" "DATATYPE"
knockout_storage = ReadData(ko_file[0], "knockout")
knockdown_storage = ReadData(kd_file[0], "knockdown")
timeseries_storage = ReadData(ts_file[0], "timeseries")
wildtype_storage = ReadData(wt_file[0], "wildtype")
mf_storage = ReadData(mf_file[0], "multifactorial")

# Setup job manager
jobman = JobManager(settings)

# Make BANJO jobs
mczjob = MCZ()
mczjob.setup(knockout_storage, wildtype_storage, settings, timeseries_storage, knockdown_storage, "MCZ_Alone")
jobman.queueJob(mczjob)

clrjob = CLR()
clrjob.setup(knockout_storage, settings, "clr_" + t + "_Bins-" + str(6), "plos", 6)
jobman.queueJob(clrjob)

#cojob = ConvexOptimization()
#cojob.setup(knockout_storage, settings, "ConvOpt_T-Plos",None, None, 0.04)
#jobman.queueJob(cojob)

mf_storage.combine(knockout_storage)
mf_storage.combine(wildtype_storage)
mf_storage.combine(knockdown_storage)
genie3job = GENIE3()
genie3job.setup(mf_storage, settings, "MF_KO_WT_KD")
jobman.queueJob(genie3job)

print jobman.queue
jobman.runQueue()
jobman.waitToClear()

accs = []
precs = []
settings["dfg4grn"]["eta_z"] = 0.001
settings["dfg4grn"]["lambda_w"] = 0.01
settings["dfg4grn"]["tau"] = 3

dfg = DFG4GRN()
dfg.setup(timeseries_storage,  TFList(timeseries_storage[0].gene_list), settings, "DFG4GRN_Baseline", 20)
jobman.queueJob(dfg)


mcznet = mczjob.network
clrnet = clrjob.network
genienet = genie3job.network
#num_edge_list = [ 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 ]
num_edge_list = [ 20, 30, 40 ]
for i in num_edge_list:
    num_edges = math.floor(math.pow(len(timeseries_storage[0].gene_list),2) * (i / 100.0))
    #print job.alg.read_output(settings)

    mcz_topnet = mcznet.copy()
    mcz_topnet.set_top_edges(num_edges)
    accs.append(("MCZ-Top_{0}%_Edges".format(i), mcz_topnet.calculateAccuracy(goldnet)))
    print "Running MCZ-DFG4GRN with i = {0}%".format(i)
    #dfg = DFG4GRN()
    #dfg.setup(timeseries_storage, TFList(timeseries_storage[0].gene_list), settings, "MCZ-DFG4GRN_{0}-Edges_Connections".format(i), 20, None, topnet, 'connections')
    #jobman.queueJob(dfg)
    #dfg = DFG4GRN()
    #dfg.setup(timeseries_storage, TFList(timeseries_storage[0].gene_list), settings, "MCZ-DFG4GRN_{0}-Edges_Weights".format(i), 20, None, topnet, 'weights')
    #jobman.queueJob(dfg)
    dfg = DFG4GRN()
    dfg.setup(timeseries_storage, TFList(timeseries_storage[0].gene_list), settings, "MCZ-DFG4GRN_{0}-Edges_BothPriors".format(i), 20, None, mcz_topnet, 'both')
    jobman.queueJob(dfg)

    clr_topnet = clrnet.copy()
    clr_topnet.set_top_edges(num_edges)
    accs.append(("CLR-Top_{0}%_Edges".format(i), clr_topnet.calculateAccuracy(goldnet)))
    print "Running CLR-DFG4GRN with i = {0}%".format(i)
    #dfg = DFG4GRN()
    #dfg.setup(timeseries_storage, TFList(timeseries_storage[0].gene_list), settings, "MCZ-DFG4GRN_{0}-Edges_Connections".format(i), 20, None, topnet, 'connections')
    #jobman.queueJob(dfg)
    #dfg = DFG4GRN()
    #dfg.setup(timeseries_storage, TFList(timeseries_storage[0].gene_list), settings, "MCZ-DFG4GRN_{0}-Edges_Weights".format(i), 20, None, topnet, 'weights')
    #jobman.queueJob(dfg)
    dfg = DFG4GRN()
    dfg.setup(timeseries_storage, TFList(timeseries_storage[0].gene_list), settings, "CLR-DFG4GRN_{0}-Edges_BothPriors".format(i), 20, None, clr_topnet, 'both')
    jobman.queueJob(dfg)

    genie_topnet = genienet.copy()
    genie_topnet.set_top_edges(num_edges)
    accs.append(("GENIE3-Top_{0}%_Edges".format(i), genie_topnet.calculateAccuracy(goldnet)))
    print "Running GENIE3-DFG4GRN with i = {0}".format(i)
    #dfg = DFG4GRN()
    #dfg.setup(timeseries_storage, TFList(timeseries_storage[0].gene_list), settings, "MCZ-DFG4GRN_{0}-Edges_Connections".format(i), 20, None, topnet, 'connections')
    #jobman.queueJob(dfg)
    #dfg = DFG4GRN()
    #dfg.setup(timeseries_storage, TFList(timeseries_storage[0].gene_list), settings, "MCZ-DFG4GRN_{0}-Edges_Weights".format(i), 20, None, topnet, 'weights')
    #jobman.queueJob(dfg)
    dfg = DFG4GRN()
    dfg.setup(timeseries_storage, TFList(timeseries_storage[0].gene_list), settings, "GENIE3-DFG4GRN_{0}-Edges_BothPriors".format(i), 20, None, genie_topnet, 'both')
    jobman.queueJob(dfg)

jobman.runQueue()
jobman.waitToClear()

SaveResults(jobman.finished, goldnet, settings, "Overall", 4)
