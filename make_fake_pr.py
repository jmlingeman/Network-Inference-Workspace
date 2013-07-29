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
import numpy
import random

def get_immediate_subdirectories(dir):
    return [name for name in os.listdir(dir) if os.path.isdir(os.path.join(dir, name))]

sys.path += get_immediate_subdirectories("./")

gene_list = map(str, list(numpy.arange(0,100)))

prop_correct = 1.0

curvenet = []
goldnet = []
v = 1.0
counter = 0
for i in range(len(gene_list)):
    curvenet.append([])
    goldnet.append([])
    for j in range(len(gene_list)):
        counter += 1
        curvenet[i].append(v)
        if random.random() > prop_correct:
        #if counter > 7000 and random.random() > prop_correct / 2.0:
            goldnet[i].append(0)
        #elif random.random() > prop_correct * (1.0 / (counter / 30.0) ):
            #goldnet[i].append(0)
        else:
            goldnet[i].append(1)
        v -= 0.0001
goldnet[i][j] = 0

cnet = Network()
print goldnet
cnet.read_netmatrix(curvenet, gene_list)

gnet = Network()
gnet.read_netmatrix(goldnet, gene_list)



ps, rs, precs = GenerateMultiPRList([cnet], gnet, False, "./" + sys.argv[1] + "_pr.png")
print ps, rs, precs
