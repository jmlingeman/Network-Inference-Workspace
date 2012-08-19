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

# Read data into program
# Where the format is "FILENAME" "DATATYPE"
dex_storage = ReadData("datasets/RootArrayData/DexRatios.csv", "dex")
dex_storage2 = ReadData("datasets/RootArrayData/HHO3_DEX_ratios.csv", "dex")
#all_storage.combine(cnlo_no3_storage)

#dex_storage.combine(cnlo_storage)
#dex_st
# Figure out list of dex targets and put them into a network so we can
# compare
net = Network()
net.gene_list = dex_storage.gene_list
target = sys.argv[1]

dex_induct = []
dex_rep = []

if target == "At1g25550":
    dex_storage = dex_storage2

for gene in dex_storage.gene_list:
    print dex_storage.experiments[0].ratios[gene]
    if dex_storage.experiments[0].ratios[gene] >= 2.0:
        dex_induct.append(gene)
    elif dex_storage.experiments[0].ratios[gene] <= 0.5:
        dex_rep.append(gene)


out_file = open(target + "_dex_targets.txt", 'w')

out_file.write("Total number of induction targets: " + str(len(dex_induct)) + "\n")
out_file.write("Total number of repression targets: " + str(len(dex_rep)) + "\n")
out_file.write("\nDEX Induction Targets\n")
for gene in dex_induct:
    out_file.write(gene + '\n')
out_file.write("\nDEX Repression Targets\n")
for gene in dex_rep:
    out_file.write(gene + '\n')

out_file.close()

