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
from mcz import *
from genie3 import *

remove = int(sys.argv[1])

# Initialize settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'
settings["global"]["experiment_name"] = "GenBio-German-Last-Removed-" + sys.argv[1]

settings["global"]["n_processors"] = 1


# Set up output directory
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])

jobman = JobManager(settings)

# Read data into program
# Where the format is "FILENAME" "DATATYPE"
c4d = ReadData("datasets/German_Data/Caldana-4d.tsv", "dex")
c4l = ReadData("datasets/German_Data/Caldana-4L.tsv", "dex")
c21d = ReadData("datasets/German_Data/Caldana-21d.tsv", "dex")
c21hl = ReadData("datasets/German_Data/Caldana-21HL.tsv", "dex")
c21l = ReadData("datasets/German_Data/Caldana-21L.tsv", "dex")
c21ll = ReadData("datasets/German_Data/Caldana-21LL.tsv", "dex")
c32l = ReadData("datasets/German_Data/Caldana-32L.tsv", "dex")
c32l2 = ReadData("datasets/German_Data/Caldana-32L2.tsv", "dex")

combined = ReadData("datasets/German_Data/Caldana-4d.tsv", "dex")

c21l.experiments = c21l.experiments[1:]

#settings["global"]["time_series_delta_t"] = [5,10,20,40,60,80,100,120,140,160,180,200,220,240,260,280,300,320,340,360,640,1280]
settings["global"]["time_series_delta_t"] = [5,10,20,40,60,80,100,120,140,160,180,200,220,240,260,280,300,320,340,360,640,1280]
#settings["global"]["time_series_delta_t"] = settings["global"]["time_series_delta_t"][:-remove]

ts_storage = [c4d, c4l, c21d, c21hl, c21l, c21ll, c32l, c32l2]
ts_storage = [c21l, c21ll, c21d, c21hl]
#for dataset in ts_storage:
    #dataset.experiments = dataset.experiments[:-remove]


#ts_storage = [c4l, c21l, c21l]

tfs = c4d.gene_list

def mean(list):
    return sum(list) / float(len(list))
def variance(list):
    avg = mean(list)
    for elem in list:
        sq_diff = (elem - avg)**2
    return sq_diff / len(list)

# Find 100 genes with highest variance
variances = {}
print tfs
for gene in tfs:
    for dataset in ts_storage:
        data_points = {}
        for exp in dataset.experiments:
            if gene in data_points:
                data_points[gene].append(exp.ratios[gene])
            else:
                data_points[gene] = [exp.ratios[gene]]
        if gene in variances:
            variances[gene].append(variance(data_points[gene]))
        else:
            variances[gene] = [variance(data_points[gene])]

# Get the top 100 most varying genes
topgenes = {}
for gene in variances:
    topgenes[gene] = mean(variances[gene])

topgenes_list = sorted(topgenes, key=lambda key: topgenes[key], reverse=True)
topgenes_list = topgenes_list[0:20]

c4d.filter(topgenes_list)
c4l.filter(topgenes_list)
c21d.filter(topgenes_list)
c21hl.filter(topgenes_list)
c21l.filter(topgenes_list)
c21ll.filter(topgenes_list)
c32l.filter(topgenes_list)
c32l2.filter(topgenes_list)

#for dataset in ts_storage:
    #dataset.normalize()

combined.filter(topgenes_list)

combined.combine(c4l)
combined.combine(c21d)
combined.combine(c21hl)
combined.combine(c21l)
combined.combine(c21ll)
combined.combine(c32l)
combined.combine(c32l2)


# Remove the last time point for testing
leave_out = []
for i, ts in enumerate(ts_storage):
    leave_out.append(ts.experiments[-1])


#goldnet = Network()
#goldnet.read_goldstd(goldnets[exp_name])


print settings["global"]["time_series_delta_t"]


percfp = 0
permfp = 1
perctp = 0
permtp = 1




genie3job = GENIE3()
genie3job.setup(combined, settings, "Genie3_Multifactorial")
jobman.queueJob(genie3job)

#mczjob = MCZ()
#mczjob.setup(knockouts[exp_name], cnlo_storage, settings, None, None, "MCZ_Knockout")
#jobman.queueJob(mczjob)

jobman.runQueue()
jobman.waitToClear()

#mcznet = mczjob.network.copy()
genie3net = genie3job.network.copy()

#mcznet.set_top_edges_percent(35)
#genie3net.set_top_edges_percent(25)

#for job in jobman.finished:
    #job.alg.network.set_top_edges_percent(10)
    #job.alg.network.normalize()

priors = []
for job in jobman.finished:
    priors.append([job.alg.alg_name, job.alg.network])

num_bootstraps = 1

meth = "BBSR"
inf = Inferelator2(settings)
settings["inferelator2"]["num_cores"] = multiprocessing.cpu_count()
settings["inferelator2"]["num_bootstraps"] = num_bootstraps
#settings["inferelator2"]["num_bootstraps"] = 1
settings["inferelator2"]["permtp"] = permtp
settings["inferelator2"]["permfp"] = permfp
settings["inferelator2"]["nCv"] = 10
settings["inferelator2"]["perctp"] = perctp
settings["inferelator2"]["percfp"] = percfp
settings["inferelator2"]["method"] = meth
inf.setup(None, None, settings, ts_storage, None, "Inferelator2_Time_Series", None, None, leave_out, False)
jobman.queueJob(inf)

percfp = 100
permfp = 1
perctp = 50
permtp = 1

#meth = "MEN"
#inf = Inferelator2(settings)
#settings["inferelator2"]["num_cores"] = 8
#settings["inferelator2"]["num_bootstraps"] = num_bootstraps
##settings["inferelator2"]["num_bootstraps"] = 1
#settings["inferelator2"]["permtp"] = 1
#settings["inferelator2"]["permfp"] = 1
#settings["inferelator2"]["nCv"] = 10
#settings["inferelator2"]["prior_weight"] = 0
#settings["inferelator2"]["perctp"] = 0
#settings["inferelator2"]["percfp"] = 0
#settings["inferelator2"]["method"] = meth
#inf.setup(None, c4d, settings, ts_storage, dex_storage, "Inferelator2_Multifactorial", None, None, leave_out)
#jobman.queueJob(inf)


meth = "BBSR"
inf = Inferelator2(settings)
settings["inferelator2"]["num_cores"] = multiprocessing.cpu_count()
settings["inferelator2"]["num_bootstraps"] = num_bootstraps
#settings["inferelator2"]["num_bootstraps"] = 1
settings["inferelator2"]["permtp"] = permtp
settings["inferelator2"]["permfp"] = permfp
settings["inferelator2"]["nCv"] = 10
settings["inferelator2"]["prior_weight"] = 1.1
settings["inferelator2"]["perctp"] = perctp
settings["inferelator2"]["percfp"] = percfp
settings["inferelator2"]["method"] = meth
inf.setup(None, None, settings, ts_storage, None, "Inferelator2_Genie3_Prior", None, genie3net, leave_out, False)
jobman.queueJob(inf)



jobman.runQueue()
jobman.waitToClear()

for job in jobman.finished:
    job.alg.network.normalize()

# Read in the results from each algorithm and then compare the up/down
# while doing the naive results

out = open(settings["global"]["output_dir"] + "/results.txt",'w')

for job in jobman.finished:
    if job.alg.alg_name == "inferelator2":
        infalg = job.alg
        output_prediction_file = open(infalg.output_dir + "/output/" + "leave_out_points.tsv", 'r')
        output_prediction_file = output_prediction_file.readlines()
        predictions = {}
        for line in output_prediction_file[1:]:
            gene, val = line.split("\t")
            predictions[gene.strip('"')] = float(val)

        ts_storage = infalg.ts_storage
        ts_0 = {}
        ts_1 = {}
        tm_1 = {}

        for rep in ts_storage:
            tm1 = rep.experiments[-3]
            t0 = rep.experiments[-2]
            t1 = rep.experiments[-1]
            for gene in rep.gene_list:
                if gene in tm_1:
                    tm_1[gene].append(tm1.ratios[gene])
                else:
                    tm_1[gene] = [tm1.ratios[gene]]
                if gene in ts_0:
                    ts_0[gene].append(t0.ratios[gene])
                else:
                    ts_0[gene] = [t0.ratios[gene]]
                if gene in ts_1:
                    ts_1[gene].append(t1.ratios[gene])
                else:
                    ts_1[gene] = [t1.ratios[gene]]

        for gene in ts_0:
            ts_0[gene] = sum(ts_0[gene]) / len(ts_0[gene])
            ts_1[gene] = sum(ts_1[gene]) / len(ts_1[gene])
            tm_1[gene] = sum(tm_1[gene]) / len(tm_1[gene])

        actual_updown = []
        predict_updown = []
        correct = 0
        incorrect = 0
        naive_correct = 0
        naive_incorrect= 0
        for i, gene in enumerate(ts_storage[0].gene_list):
            val0 = ts_0[gene]
            val1 = ts_1[gene]
            valm1 = tm_1[gene]
            pre1 = predictions[gene]
            #print valm1, val0, val1, pre1
            if val0 < val1 and val0 < pre1:
                correct += 1
            elif val0 > val1 and val0 > pre1:
                correct += 1
            else:
                incorrect += 1

            if tm1 < val0 and val0 < val1:
                naive_correct += 1
            elif tm1 > val0 and val0 > val1:
                naive_correct += 1
            else:
                naive_incorrect += 1

        out.write("PREDICT: {0} {1} {2} {3}\n".format(job.alg.name, correct, incorrect, correct / float(correct + incorrect)))
        out.write("NAIVE: {0} {1} {2} {3}\n".format(job.alg.name, naive_correct, naive_incorrect, correct / float(naive_correct + naive_incorrect)))
        print "PREDICT:", job.alg.name, correct, incorrect, correct / float(correct + incorrect)
        print "NAIVE:", job.alg.name, naive_correct, naive_incorrect, naive_correct / float(naive_correct + naive_incorrect)


#tprs, fprs, rocs = GenerateMultiROC(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallROC.pdf")
#ps, rs, precs = GenerateMultiPR(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallPR.pdf")

#SaveResults(jobman.finished, None, settings)

#results = []
#for job in jobman.finished:
    #if job.alg.alg_name == "dfg4grn":
        #results.append([job.alg.name, job.alg.best_sign, job.alg.best_sign_all, job.alg.avg_sign, job.alg.avg_sign_all])

#out = open(settings["global"]["output_dir"] + "/results.txt",'w')
#for r in results:
    #out.write(r[0] + "\t" + str(r[1]) + "\t" + str(r[2]) + "\t"+str(r[3])+"\t"+str(r[4])+"\n")

#out = open(settings["global"]["output_dir"] + "/results-sorted.txt",'w')
#results.sort(key=lambda x: x[0])
#for r in results:
    #out.write(r[0] + "\t" + str(r[1]) + "\t" + str(r[2]) + "\t"+str(r[3])+"\t"+str(r[4])+"\n")

print "\a"
print "\a"
print "\a"

