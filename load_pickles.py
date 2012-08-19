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

import pickle
# Get all files
fileList = []
rootdir = sys.argv[1]
for root, subFolders, files in os.walk(rootdir):
    for file in files:
        fileList.append(os.path.join(root,file))

pickles = []
for file in fileList:
    if ".pickle" in file:
        p = pickle.load(open(file, 'r'))
        pickles.append(p)

cols = {"avg" : 0,
        "avg_all" : 1,
        "best" : 2,
        "best_all" : 3}

print "Got pickles"
results = []
for p in pickles:
    if p.alg_name == "dfg4grn":
        p.read_output(p.settings)
        r = [p.name, str(p.avg_sign), str(p.avg_sign_all), str(p.best_sign), str(p.best_sign_all)]
        results.append(r)


out = open(sys.argv[2],'w')
results.sort(key=lambda x: x[0])
out.write("Algorithm,Parameters,Dataset,Average Network Consistent Genes,Average Network All Genes,Best Network Consistent Genes,Best Network All Genes\n")
for r in results:
    if "CLR" in r[0]:
        out.write("clr+dfg,")
    elif "GENIE3" in r[0]:
        out.write("genie3+dfg,")
    else:
        out.write("dfg alone,")

    if "DEFAULT" in r[0]:
        out.write("default,")
    else:
        out.write("modified,")

    if "cnlo" in r[0] and "no3" in r[0]:
        out.write("cnlo+no3+timeseries")
    elif "cnlo" in r[0]:
        out.write("cnlo+timeseries")
    elif "no3" in r[0]:
        out.write("no3+timeseries")

    if "True" in r[0]:
        out.write("+dex,")
    else:
        out.write(",")

    out.write(str(r[1]) + "," + str(r[2]) + ","+str(r[3])+","+str(r[4])+"\n")
