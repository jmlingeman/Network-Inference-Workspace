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
from Helpers import *

from mcz import *
from dfg4grn import *
from ReadConfig import *
from clr import *
from genie3 import *
from tlclr import *
from inferelator import *

def get_golden_genes(filename):
    gold_genes = []
    f = open(filename, 'r').readlines()
    f.pop(0) # Burn first line
    for line in f:
        gold_genes.append(line.split("\t")[0])

    return gold_genes

golden_genes = get_golden_genes("datasets/RootArrayData/NitrogenGenes.txt")

# Instantsiate settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'
settings["global"]["experiment_name"] = "Dex-" + sys.argv[1]



# Create date string to append to output_dir
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])

# Read in the gold standard network

# Read in the gold standard network
#goldnet.read_goldstd(settings["global"]["large_network_goldnet_file"])
#ko_file, kd_file, ts_file, wt_file, mf_file, goldnet = get_example_data_files(sys.argv[1], settings)


# Read data into program
# Where the format is "FILENAME" "DATATYPE"
dex_storage = ReadData("datasets/RootArrayData/DexRatios.csv", "dex")
dexcombined = ReadData("datasets/RootArrayData/DexRatios.csv", "dex")
dex_storage2 = ReadData("datasets/RootArrayData/HHO3_DEX_ratios.csv", "dex")

cnlo_storage = ReadData("datasets/RootArrayData/Root_CNLO_Krouk.txt", "dex")
cnlo_no3_storage = ReadData("datasets/RootArrayData/Root_CNLO_Krouk.txt", "dex")
no3_1_storage = ReadData("datasets/RootArrayData/Root_NO3_Wang03.txt", "dex")
no3_2_storage = ReadData("datasets/RootArrayData/Root_NO3_Wang04.txt", "dex")
no3_3_storage = ReadData("datasets/RootArrayData/Root_NO3_Wang07.txt", "dex")
#ts_storage = ReadData("datasets/RootArrayData/Root_WT_Krouk11.txt", "dex")

tfs_file = open("datasets/RootArrayData/tfs.csv", 'r')
line = tfs_file.readlines()[0]
tfs = line.strip().split(',')
tfs = [x.upper() for x in tfs]

kno3_1 = ReadData("datasets/RootArrayData/KNO3norm1.csv", "dex")
kno3_2 = ReadData("datasets/RootArrayData/KNO3norm2.csv", "dex")
kno3_3 = ReadData("datasets/RootArrayData/KNO3norm3.csv", "dex")
kno3_4 = ReadData("datasets/RootArrayData/KNO3norm4.csv", "dex")
settings["global"]["time_series_delta_t"] = "3 3 3 3 3 5"

dex_storage.filter(kno3_1.gene_list)
dexcombined.filter(kno3_1.gene_list)
dex_storage2.filter(kno3_1.gene_list)
cnlo_storage.filter(kno3_1.gene_list)
cnlo_no3_storage.filter(kno3_1.gene_list)
no3_1_storage.filter(kno3_1.gene_list)
no3_2_storage.filter(kno3_1.gene_list)
no3_3_storage.filter(kno3_1.gene_list)

dexcombined.combine(dex_storage2)
no3_storage = no3_1_storage
no3_storage.combine(no3_2_storage)
no3_storage.combine(no3_3_storage)

cnlo_no3_storage.combine(no3_storage)

#all_storage.combine(cnlo_no3_storage)

#dex_storage.combine(cnlo_storage)
#dex_storage.combine(no3_storage)

#dex_storage.normalize()
no3_storage.normalize()
cnlo_storage.normalize()
cnlo_no3_storage.normalize()
#all_storage.normalize()

ts_storage = [kno3_1, kno3_2, kno3_3, kno3_4]
#for s in ts_storage:
    #s.normalize()

# Setup job manager
jobman = JobManager(settings)

# Make BANJO jobs
#mczjob = MCZ()
#mczjob.setup(knockout_storage, wildtype_storage, settings, timeseries_storage, knockdown_storage, "MCZ")
#jobman.queueJob(mczjob)

clr_cnlojob = CLR()
clr_cnlojob.setup(cnlo_storage, settings, "clr_cnlo")
jobman.queueJob(clr_cnlojob)

genie3_cnlojob = GENIE3()
genie3_cnlojob.setup(cnlo_storage, settings, "genie3_cnlo")
jobman.queueJob(genie3_cnlojob)

clr_no3job = CLR()
clr_no3job.setup(no3_storage, settings, "clr_no3")
jobman.queueJob(clr_no3job)

genie3_no3job = GENIE3()
genie3_no3job.setup(no3_storage, settings, "genie3_no3")
jobman.queueJob(genie3_no3job)

clr_cnlo_no3job = CLR()
clr_cnlo_no3job.setup(cnlo_no3_storage, settings, "clr_cnlo_no3")
jobman.queueJob(clr_cnlo_no3job)

genie3_cnlo_no3job = GENIE3()
genie3_cnlo_no3job.setup(cnlo_no3_storage, settings, "genie3_cnlo_no3")
jobman.queueJob(genie3_cnlo_no3job)

#import vimpdb; vimpdb.set_trace()
#tlclrjob = TLCLR()
#dex_storage.experiments = [dex_storage.experiments[0]]
#tlclrjob.setup(None, dex_storage, settings, ts_storage, None, "tlclr_dex_test")
#jobman.queueJob(tlclrjob)

print jobman.queue
jobman.runQueue()
jobman.waitToClear()

algorithms = ["dfg"]
wildtype = ["cnlo", "no3", "cnlo+no3"]
lambda_w = [0.1, 0.5]
#use_dex = [True, False]
use_dex = [True, False]

randnet = generate_random_network(kno3_1.gene_list)

# Build dex networks
#dex_clrnet = apply_dex("At1g13300", dex_storage, clrjob.network)
#dex_genie3net = apply_dex("At1g13300", dex_storage, genie3job.network)


# Clip our normal networks
#clrjob.network.set_top_edges_percent(int(sys.argv[2]))
#genie3job.network.set_top_edges_percent(int(sys.argv[2]))

for d in use_dex:
    for alg in algorithms:

        if d:
            # Pure DFG, no
            dfg = DFG4GRN()
            settings["dfg4grn"]["eta_z"] = 0.1
            settings["dfg4grn"]["lambda_w"] = 0.1
            settings["dfg4grn"]["tau"] = 3
            settings["dfg4grn"]["delta_t"] = "3 3 3 3 3 5"
            dfg.setup(ts_storage, TFList(tfs), settings, "DFG-NO_WT_LambdaW-{0}_Dex-{1}".format(0.1, d), 20, None, None, None, False, dexcombined, "At1g13300,At1g25550")
            jobman.queueJob(dfg)
        #else:
            #dfg = DFG4GRN()
            #settings["dfg4grn"]["eta_z"] = 0.1
            #settings["dfg4grn"]["lambda_w"] = 0.1
            #settings["dfg4grn"]["tau"] = 3
            #settings["dfg4grn"]["delta_t"] = "3 3 3 3 3 5"
            #dfg.setup(ts_storage, TFList(tfs), settings, "DFG-NO_WT_LambdaW-{0}_Dex-{1}".format(0.1, d), 20)
            #jobman.queueJob(dfg)

        for wt in wildtype:
            if wt == "cnlo":
                genie3_network = genie3_cnlojob.network.copy()
                clr_network = clr_cnlojob.network.copy()
            elif wt == "no3":
                genie3_network = genie3_no3job.network.copy()
                clr_network = clr_no3job.network.copy()
            else:
                genie3_network = genie3_cnlo_no3job.network.copy()
                clr_network = clr_cnlo_no3job.network.copy()

            clr_network.normalize()
            genie3_network.normalize()

            if d:
                genie3_network = apply_dex("At1g13300", dex_storage, genie3_network, sys.argv[2])
                genie3_network = apply_dex("At1g25550", dex_storage2, genie3_network, sys.argv[2])
                clr_network = apply_dex("At1g13300", dex_storage, clr_network, sys.argv[2])
                clr_network = apply_dex("At1g25550", dex_storage2, clr_network, sys.argv[2])

            #clr_network.normalize()
            #genie3_network.normalize()



            if alg == "dfg":
                dfg = DFG4GRN()
                settings["dfg4grn"]["eta_z"] = 0.01
                settings["dfg4grn"]["lambda_w"] = 0.5
                settings["dfg4grn"]["tau"] = 3
                settings["dfg4grn"]["delta_t"] = "3 3 3 3 3 5"
                dfg.setup(ts_storage, TFList(tfs), settings, "DFG-GENIE3_Type-{0}_LambdaW-{1}_Dex-{2}".format(wt, 0.5, d), 20, None, genie3_network, "weights")
                jobman.queueJob(dfg)

                dfg = DFG4GRN()
                settings["dfg4grn"]["eta_z"] = 0.01
                settings["dfg4grn"]["lambda_w"] = 0.5
                settings["dfg4grn"]["tau"] = 3
                settings["dfg4grn"]["delta_t"] = "3 3 3 3 3 5"
                dfg.setup(ts_storage, TFList(tfs), settings, "DFG-CLR_Type-{0}_LambdaW-{1}_Dex-{2}".format(wt, 0.5, d), 20, None, clr_network, "weights")
                #jobman.queueJob(dfg)

                # Inferelator mode
                #dfg = DFG4GRN()
                #settings["dfg4grn"]["eta_z"] = 0.1
                #settings["dfg4grn"]["lambda_w"] = 0.005
                #settings["dfg4grn"]["tau"] = 3
                #settings["dfg4grn"]["delta_t"] = "3 3 3 3 3 5"
                #dfg.setup(ts_storage, TFList(tfs), settings, "Inferelator-CLR_Type-{0}_LambdaW-{1}_Dex-{2}".format(wt, 0.005, d), 20, None, clr_network, "weights", True)
                #jobman.queueJob(dfg)

                # Inferelator mode
                #dfg = DFG4GRN()
                #settings["dfg4grn"]["eta_z"] = 0.1
                #settings["dfg4grn"]["lambda_w"] = 0.005
                #settings["dfg4grn"]["tau"] = 3
                #settings["dfg4grn"]["delta_t"] = "3 3 3 3 3 5"
                #dfg.setup(ts_storage, TFList(tfs), settings, "Inferelator-GENIE3_Type-{0}_LambdaW-{1}_Dex-{2}".format(wt, 0.005, d), 20, None, genie3_network, "weights", True)
                #jobman.queueJob(dfg)

                dfg = DFG4GRN()
                settings["dfg4grn"]["eta_z"] = 0.1
                settings["dfg4grn"]["lambda_w"] = 0.1
                settings["dfg4grn"]["tau"] = 3
                settings["dfg4grn"]["delta_t"] = "3 3 3 3 3 5"
                dfg.setup(ts_storage, TFList(tfs), settings, "DFG-CLR_DEFAULT_Type-{0}_LambdaW-{1}_Dex-{2}".format(wt, 0.1, d), 20, None, clr_network, "weights")
                #jobman.queueJob(dfg)

                dfg = DFG4GRN()
                settings["dfg4grn"]["eta_z"] = 0.1
                settings["dfg4grn"]["lambda_w"] = 0.1
                settings["dfg4grn"]["tau"] = 3
                settings["dfg4grn"]["delta_t"] = "3 3 3 3 3 5"
                dfg.setup(ts_storage, TFList(tfs), settings, "DFG-GENIE3_DEFAULT_Type-{0}_LambdaW-{1}_Dex-{2}".format(wt, 0.1, d), 20, None, genie3_network, "weights")
                jobman.queueJob(dfg)




jobman.runQueue()
jobman.waitToClear()

#tprs, fprs, rocs = GenerateMultiROC(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallROC.pdf")
#ps, rs, precs = GenerateMultiPR(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallPR.pdf")

SaveResults(jobman.finished, [], settings)

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

