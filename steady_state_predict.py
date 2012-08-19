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


# Use the 12 and 15 minute timepoints to try to predict the next time
# point.

# Using 12 to predict 15

for job in jobman.finished:
    print job.alg.name
    predict_timepoint(ts_storage, job.alg.network, 5, 6)

SaveResults(jobman.finished, [], settings)

