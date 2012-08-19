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
settings["global"]["experiment_name"] = "Compare_Dex_And_DFG"



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


# Figure out list of dex targets and put them into a network so we can
# compare
net = Network()
net.gene_list = dex_storage.gene_list
target = sys.argv[1]

if target == "At1g25550":
    dex_storage = dex_storage2

for gene1 in dex_storage.gene_list:
    net.network[gene1] = {}
    for gene2 in dex_storage.gene_list:
        net.network[gene1][gene2] = 0
for gene in dex_storage.gene_list:
    print dex_storage.experiments[0].ratios[gene]
    if dex_storage.experiments[0].ratios[gene] >= 2.0:
        print dex_storage.experiments[0].ratios[gene]
        net.network[target][gene] = 1
    elif dex_storage.experiments[0].ratios[gene] <= 0.5:
        net.network[target][gene] = -1
    else:
        net.network[target][gene] = 0



# Run a fresh DFG
#dfg = DFG4GRN()
#settings["dfg4grn"]["eta_z"] = 0.1
#settings["dfg4grn"]["lambda_w"] = 0.1
#settings["dfg4grn"]["tau"] = 3
#settings["dfg4grn"]["delta_t"] = "3 3 3 3 3 5"
#dfg.setup(ts_storage, TFList(tfs), settings, "DFG-NO_WT_LambdaW-{0}_Dex".format(0.1), 1)
#jobman.queueJob(dfg)



#jobman.runQueue()
#jobman.waitToClear()

import pickle
dfg = pickle.load(open("output/Dex-BothDexTest-20models-2012-06-24_00.18.28/DFG-NO_WT_LambdaW-0.1_Dex-False/DFG-NO_WT_LambdaW-0.1_Dex-False.pickle",'r'))

dfg.read_output(dfg.settings)
#tprs, fprs, rocs = GenerateMultiROC(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallROC.pdf")
#ps, rs, precs = GenerateMultiPR(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallPR.pdf")

SaveResults(jobman.finished, [], settings)

# Compare the network targets

#results = []
#for job in jobman.finished:
    #if job.alg.alg_name == "dfg4grn":
        #results.append([job.alg.name, job.alg.best_sign, job.alg.best_sign_all])

#out = open(settings["global"]["output_dir"] + "/results.txt",'w')
#for r in results:
    #out.write(r[0] + "\t" + str(r[1]) + "\t" + str(r[2]) + "\n")

#out = open(settings["global"]["output_dir"] + "/results-sorted.txt",'w')
#results.sort(key=lambda x: x[0])
#for r in results:
    #out.write(r[0] + "\t" + str(r[1]) + "\t" + str(r[2]) + "\n")

out_file = open(target + " to DFG targets.txt", 'w')

correct = 0
targets = 0
for gene1 in net.gene_list:
    if net.network[target + ""][gene1] > 0 and dfg.network.network[target + ""][gene1] > 0:
        correct += 1
        targets += 1
        out_file.write(target + " -> " + gene1 + ": Correct +\n")
    elif net.network[target + ""][gene1] < 0 and dfg.network.network[target + ""][gene1] < 0:
        correct += 1
        targets += 1
        out_file.write( target + " -> " + gene1 + ": Correct -\n")
    elif net.network[target + ""][gene1] > 0:
        targets += 1
        out_file.write(target + " -> " + gene1 + ": Missed +\n")
    elif net.network[target + ""][gene1] < 0:
        targets += 1
        out_file.write(target + " -> " + gene1 + ": Missed -\n")

out_file.write("\nNumber correct: {0}\nTotal targets: {1}\nPercent obtained: {2}".format(correct, targets, float(correct) / targets))
out_file.close()

# Now loop over each gene and find whether or not it is a target from
# dex and a target from DFG, print each list and the intersection
dfg_induct = []
dex_induct = []
dfg_rep = []
dex_rep = []
for gene in net.gene_list:
    if net.network[target][gene] > 0:
        dex_induct.append(gene)
    if net.network[target][gene] < 0:
        dex_rep.append(gene)
    if dfg.network.network[target][gene] > 0:
        dfg_induct.append(gene)
    if dfg.network.network[target][gene] < 0:
        dfg_rep.append(gene)

induct_intersect = []
rep_intersect = []
for gene1 in dfg_induct:
    for gene2 in dex_induct:
        if gene1 == gene2:
            induct_intersect.append(gene1)
for gene1 in dfg_rep:
    for gene2 in dex_rep:
        if gene1 == gene2:
            rep_intersect.append(gene1)

print dfg_rep
print dex_rep

out_file = open(target + " target list.txt", 'w')
out_file.write("Total DEX induction targets: " + str(len(dex_induct)) + "\n")
out_file.write("Total DEX repression targets: " + str(len(dex_rep)) + "\n\n")
out_file.write("DFG Induction Targets\n")
for gene in dfg_induct:
    out_file.write(gene + '\n')
out_file.write("\nDEX Induction Targets\n")
for gene in dex_induct:
    out_file.write(gene + '\n')
out_file.write("\nDFG Repression Targets\n")
for gene in dfg_rep:
    out_file.write(gene + '\n')
out_file.write("\nDEX Repression Targets\n")
for gene in dex_rep:
    out_file.write(gene + '\n')

out_file.write("\nInduction Intersection\n")
for gene in induct_intersect:
    out_file.write(gene + '\n')
out_file.write("\nRepression Intersection\n")
for gene in rep_intersect:
    out_file.write(gene + '\n')

out_file.close()

print dfg.weights[0]
print dfg.network.network[target]
