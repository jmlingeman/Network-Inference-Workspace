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
import multiprocessing
from scipy import stats

def get_immediate_subdirectories(dir):
    return [name for name in os.listdir(dir) if os.path.isdir(os.path.join(dir, name))]

sys.path += get_immediate_subdirectories("./")

from inferelator_pipeline import *
from inferelator2 import *
from genie3 import *

nboot = 5

# TODO: Create a new dex method where I simply increase the interactions
# for the various genes using the response matrix from the dataset

if len(sys.argv) > 1 and sys.argv[1] == "diff":
    make_diff = True
    print "MAKING INTO DIFF DATASETS"
else:
    make_diff = False

def get_stats_on_dataset(dataset, prediction):
    # Get mean, median, stddev of each dataset
    last_tp = dataset.experiments[-1]

    # Mean of abs of entire dataset
    sum = 0
    count = 0
    for exp in dataset.experiments:
        for gene in dataset.gene_list:
            sum += abs(exp.ratios[gene])
            count += 1

    mean_dataset = sum / float(count)

    # Mean of prediction
    sum = 0
    count = 0
    for gene in dataset.gene_list:
        sum += abs(prediction[gene])
        count += 1

    mean_prediction = sum / float(count)

    # Mean of last tp
    sum = 0
    count = 0
    for gene in dataset.gene_list:
        sum += abs(last_tp.ratios[gene])
        count += 1

    mean_last_tp = sum / float(count)

    # Stddev of dataset
    dataset_list = []
    for exp in dataset.experiments:
        for gene in dataset.gene_list:
            dataset_list.append(exp.ratios[gene])

    std_dataset = numpy.std(dataset_list)
    std_prediction = numpy.std(prediction.values())
    std_last_tp = numpy.std(last_tp.ratios.values())

    median_dataset = numpy.median(dataset_list)
    median_prediction = numpy.median(prediction.values())
    median_last_tp = numpy.median(last_tp.ratios.values())

    gmean_dataset = stats.gmean(map(abs,dataset_list))
    gmean_prediction = stats.gmean(map(abs,prediction.values()))
    gmean_last_tp = stats.gmean(map(abs,last_tp.ratios.values()))

    print "Mean Dataset:", mean_dataset
    print "Mean Prediction:", mean_prediction
    print "Mean Last Tp:", mean_last_tp
    print "STD Dataset:", std_dataset
    print "STD Prediction:", std_prediction
    print "STD Last Tp:", std_last_tp
    print "Median Dataset:", median_dataset
    print "Median Prediction", median_prediction
    print "Median Last Tp", median_last_tp
    print "Geo Mean Dataset", gmean_dataset
    print "Geo Mean Prediction", gmean_prediction
    print "Geo Mean Last Tp", gmean_last_tp

def make_dataset_into_diffs(dataset):
    for i in range(len(dataset.experiments) - 1):
        exp1 = dataset.experiments[i]
        exp2 = dataset.experiments[i+1]

        for gene in dataset.gene_list:
            exp1.ratios[gene] = exp2.ratios[gene] - exp1.ratios[gene]

    # Now remove last tp
    dataset.experiments = dataset.experiments[:-1]

    return dataset


data_file = "experiments/KranthiData/Normalized.txt"
genes_file = "experiments/KranthiData/genes_of_interest.txt"

ts_names = "experiments/KranthiData/time_series_names.txt"
pert_names = "experiments/KranthiData/perturbation_names.txt"
ss_names = "experiments/KranthiData/steady_state_names.txt"

legend = "experiments/KranthiData/Legend_fixed.csv"

# Initialize settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'
settings["global"]["experiment_name"] = "Kranthi-Testing"

settings["global"]["n_processors"] = 1

# Set up output directory
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])

# Read in configs for this algorithm
from dfg4grn import *
settings = ReadConfig(settings, "./config/default_values/dfg4grn.cfg")
settings = ReadConfig(settings, settings["dfg4grn"]["config"])

data = {}
knockouts = {}
wildtypes = {}
knockdowns = {}
multifactorials = {}
timeseries_as_steady_state = {}
# Loop over the directories we want, reading in the timeseries files
data = ReadData(data_file, "kranthi_data")
ss_names = open(ss_names).read().splitlines()
ts_names = open(ts_names).read().splitlines()
pert_names = open(pert_names).read().splitlines()

# Filter out the genes we don't want
genes_of_interest = open(genes_file).read().splitlines()
genes_of_interest = [x.upper() for x in genes_of_interest]
data.filter(genes_of_interest)

# Read in the legend so we know what the experiment names relate to
col_to_exp = {}
pert_cond = {}
for line in open(legend):
    line = line.strip()
    line = line.split(',')
    colname = line[0]
    expname = line[1]
    col_to_exp[colname] = expname

    if line[2] != "":
        pert_cond[colname] = line[2:4]


# Now split the Kranthi data file up into the different data types
ss_data = MicroarrayData(data_file, "steadystate")
combined = MicroarrayData(data_file, "steadystate")
kcl_ts_data = MicroarrayData(data_file, "timeseries")
kno_ts_data = MicroarrayData(data_file, "timeseries")
pert_data = MicroarrayData(data_file, "overexpression")
pert_baseline = MicroarrayData(data_file, "steadystate")
for exp in data.experiments:
    exp.name = exp.name[:-4]
    if exp.name in ss_names:
        ss_data.experiments.append(exp)
    elif exp.name in ts_names and "KCl" in col_to_exp[exp.name]:
        kcl_ts_data.experiments.append(exp)
    elif exp.name in ts_names and "KNO3" in col_to_exp[exp.name]:
        kno_ts_data.experiments.append(exp)
    elif exp.name in pert_names and pert_cond[exp.name][0] == "Y":
        pert_baseline.experiments.append(exp)
    elif exp.name in pert_names:
        pert_data.experiments.append(exp)
    else:
        print "Error: experiment name not found:", exp.name
    exp.name = col_to_exp[exp.name]
    combined.experiments.append(exp)



ss_data.gene_list = data.gene_list
kcl_ts_data.gene_list = data.gene_list
kno_ts_data.gene_list = data.gene_list
pert_data.gene_list = data.gene_list
pert_baseline.gene_list = data.gene_list
combined.gene_list = data.gene_list

# Set minimum values to 0
min_value = 2.262319935
for dataset in [pert_data, ss_data, pert_baseline]:
    for exp in dataset.experiments:
        for gene in dataset.gene_list:
            exp.ratios[gene] = exp.ratios[gene] - min_value
            if exp.ratios[gene] < 0.0001:
                #dataset.experiments.remove(exp)
                #break
                exp.ratios[gene] = 0


#timeseries_data = [kno_ts_data, kcl_ts_data]

if make_diff:
    kno_ts_data = make_dataset_into_diffs(kno_ts_data)
    kcl_ts_data = make_dataset_into_diffs(kcl_ts_data)

timeseries_data = [kcl_ts_data]
#timeseries_data = [kno_ts_data]

leave_out = []
#for i, ts in enumerate(timeseries_data):
    #leave_out.append(ts.experiments[-1])

for i, ts in enumerate(timeseries_data):
    leave_out.append(ts.experiments[-1])
# Remove the last time point
#kno_ts_data.experiments = kno_ts_data.experiments[:-1]
#kcl_ts_data.experiments = kcl_ts_data.experiments[:-1]

jobman = JobManager(settings)



for i, exp in enumerate(pert_baseline.experiments):
    # For each experiment, replace the value with the diff between base
    # and dex data
    pert = pert_data.experiments[i]
    for gene1 in pert_baseline.gene_list:
        baseval = exp.ratios[gene1]
        pertval = pert.ratios[gene1]
        pert.ratios[gene1] = pertval-baseval

        print gene1, baseval, pertval, pert.ratios[gene1]

genie3job = GENIE3()
genie3job.setup(combined, settings, "Genie3_All_Data")
jobman.queueJob(genie3job)
genie3nets = genie3job


jobman.runQueue()
jobman.waitToClear()

#for gene1 in genie3job.network.gene_list:
    #for gene2 in genie3job.network.gene_list:
        #genie3job.network.network[gene1][gene2] = genie3job.network.network[gene1][gene2] * 100.0

#pert_data.combine(pert_baseline)

# What we want to do here now is use the dex data to augment the
# prior that we have created. Additionally we should create a new prior
# using only dex correllations
# Compare each dataset to its baseline



meth = "MEN"
inf = Inferelator2(settings)
settings["inferelator2"]["num_cores"] = 6
settings["inferelator2"]["num_bootstraps"] = nboot
#settings["inferelator2"]["num_bootstraps"] = 3
settings["inferelator2"]["permtp"] = 1
settings["inferelator2"]["permfp"] = 1
settings["inferelator2"]["delta_t_max"] = 5
settings["inferelator2"]["delta_t_min"] = 0
settings["inferelator2"]["tau"] = 45
settings["inferelator2"]["nCv"] = 10
settings["inferelator2"]["perctp"] = 0
settings["inferelator2"]["percfp"] = 0
settings["inferelator2"]["prior_weight"] = 10.0
settings["inferelator2"]["method"] = meth
settings["global"]["time_series_delta_t"] = 3.0
inf.setup(None, pert_data, settings, timeseries_data, ss_data, "Inferelator2_TS_SS".format(meth, "inf_test"), None, prior=genie3job.network, split_ts=False, leave_outs=leave_out)
#inf.setup(None, ss_data, settings, [kcl_ts_data], None, "Inferelator2_TS_SS".format(meth, "inf_test"), prior=genie3job.network, split_ts=False, gold_std=None, leave_outs=leave_out)
jobman.queueJob(inf)

inf = Inferelator2(settings)
settings["inferelator2"]["num_cores"] = 6
settings["inferelator2"]["num_bootstraps"] = nboot
#settings["inferelator2"]["num_bootstraps"] = 3
settings["inferelator2"]["permtp"] = 1
settings["inferelator2"]["permfp"] = 1
settings["inferelator2"]["delta_t_max"] = 5
settings["inferelator2"]["delta_t_min"] = 0
settings["inferelator2"]["nCv"] = 10
settings["inferelator2"]["tau"] = 45
settings["inferelator2"]["perctp"] = 0
settings["inferelator2"]["percfp"] = 0
settings["inferelator2"]["prior_weight"] = 0.0
settings["inferelator2"]["method"] = meth
settings["global"]["time_series_delta_t"] = 3.0
inf.setup(None, pert_data, settings, timeseries_data, ss_data, "Inferelator2_TS_SS_NOPRIOR".format(meth, "inf_test"), None, prior=None, split_ts=False, leave_outs=leave_out)
#inf.setup(None, ss_data, settings, [kcl_ts_data], None, "Inferelator2_TS_SS_NOPRIOR".format(meth, "inf_test"), None, prior=genie3job.network, split_ts=False, leave_outs=leave_out)
jobman.queueJob(inf)

jobman.runQueue()
jobman.waitToClear()

inf = Inferelator2(settings)
settings["inferelator2"]["num_cores"] = 6
settings["inferelator2"]["num_bootstraps"] = nboot
#settings["inferelator2"]["num_bootstraps"] = 3
settings["inferelator2"]["permtp"] = 1
settings["inferelator2"]["permfp"] = 1
settings["inferelator2"]["delta_t_max"] = 5
settings["inferelator2"]["delta_t_min"] = 1
settings["inferelator2"]["nCv"] = 10
settings["inferelator2"]["tau"] = 45
settings["inferelator2"]["perctp"] = 0
settings["inferelator2"]["percfp"] = 0
settings["inferelator2"]["prior_weight"] = 10.0
settings["inferelator2"]["method"] = meth
settings["global"]["time_series_delta_t"] = 3.0
inf.setup(None, ss_data, settings, timeseries_data, None, "Inferelator2_TS_SS_NOPERT".format(meth, "inf_test"), None, prior=genie3job.network, split_ts=False, leave_outs=leave_out)
#inf.setup(None, ss_data, settings, [kcl_ts_data], None, "Inferelator2_TS_SS".format(meth, "inf_test"), prior=genie3job.network, split_ts=False, gold_std=None, leave_outs=leave_out)
jobman.queueJob(inf)

inf = Inferelator2(settings)
settings["inferelator2"]["num_cores"] = 6
settings["inferelator2"]["num_bootstraps"] = nboot
#settings["inferelator2"]["num_bootstraps"] = 3
settings["inferelator2"]["permtp"] = 1
settings["inferelator2"]["permfp"] = 1
settings["inferelator2"]["delta_t_max"] = 5
settings["inferelator2"]["delta_t_min"] = 1
settings["inferelator2"]["nCv"] = 10
settings["inferelator2"]["tau"] = 45
settings["inferelator2"]["perctp"] = 0
settings["inferelator2"]["percfp"] = 0
settings["inferelator2"]["prior_weight"] = 0.0
settings["inferelator2"]["method"] = meth
settings["global"]["time_series_delta_t"] = 3.0
inf.setup(None, ss_data, settings, timeseries_data, None, "Inferelator2_TS_SS_NOPRIOR_NOPERT".format(meth, "inf_test"), None, prior=None, split_ts=False, leave_outs=leave_out)
#inf.setup(None, ss_data, settings, [kcl_ts_data], None, "Inferelator2_TS_SS_NOPRIOR".format(meth, "inf_test"), None, prior=genie3job.network, split_ts=False, leave_outs=leave_out)
jobman.queueJob(inf)


jobman.runQueue()
jobman.waitToClear()

#for job in jobman.finished:
    #job.alg.network.normalize()

#for job in jobman.finished:
    #tprs, fprs, rocs = GenerateMultiROC([job], None, False, settings["global"]["output_dir"] + "/" + job.alg.name + ".pdf")
    #ps, rs, precs = GenerateMultiPR([job], None, False, settings["global"]["output_dir"] + "/" + job.alg.name + ".pdf")

#SaveResults(jobman.finished, None, settings)

#results = []
##for job in jobman.finished:
    ##results.append([job.alg.name, job.alg.network.num_correct_100, job.alg.network.per_correct_100, job.alg.network.num_correct_10p, job.alg.network.per_correct_10p])

#print "Results:"
#print results
##out = open(settings["global"]["output_dir"] + "/results.txt",'w')
##for r in results:
    ##out.write(r[0] + "\t" + str(r[1]) + "\t" + str(r[2]) + "\t"+str(r[3])+"\t"+str(r[4])+"\n")

#out = open(settings["global"]["output_dir"] + "/results-sorted.txt",'w')
#results.sort(key=lambda x: x[3])
#for r in results:
    #out.write(r[0] + "\t" + str(r[1]) + "\t" + str(r[2]) + "\t" + str(r[3])+ "\t" + str(r[4])+  "\n")
#out.close()

# Read in the results from each algorithm and then compare the up/down
# while doing the naive results

# Also calculate the MSE for both naive and prediction

out = open(settings["global"]["output_dir"] + "/results.txt",'w')

nomove_cutoff = 0.05

for job in jobman.finished:

    job.alg.network.compare_graph_network([], settings["global"]["output_dir"] + "/" + job.alg.name + "-network", 1)
    job.alg.network.printNetworkToFile(settings["global"]["output_dir"] + "/" + job.alg.name + ".sif")

    if job.alg.alg_name == "inferelator2":
        infalg = job.alg
        output_prediction_file = open(infalg.output_dir + "/output/" + "leave_out_points.tsv", 'r')
        output_prediction_file = output_prediction_file.readlines()
        output_mse_file = open(infalg.output_dir + "/output/" + "leave_out_results.txt", 'r')
        output_mse_file = output_mse_file.readlines()
        predictions = {}
        for line in output_prediction_file[1:]:
            gene, val = line.split("\t")
            predictions[gene.strip('"')] = float(val)

        ts_storage = infalg.ts_storage

        correct = 0
        incorrect = 0
        naive_correct = 0
        naive_incorrect= 0
        for rep in ts_storage:

            naive_mse = 0
            naive_mse_reverse = 0
            predict_mse = 0

            naive_errors = []
            predict_errors = []
            naive_errors_reverse = []

            ts_0 = {}
            ts_1 = {}
            tm_1 = {}
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
            for i, gene in enumerate(ts_storage[0].gene_list):
                val0 = ts_0[gene]
                val1 = ts_1[gene]
                valm1 = tm_1[gene]
                pre1 = predictions[gene]
                if abs(val0 - val1) > nomove_cutoff and val0 < val1 and val0 < pre1:
                    correct += 1
                elif abs(val0 - val1) > nomove_cutoff and val0 > val1 and val0 > pre1:
                    correct += 1
                #elif abs(val0 - val1) < nomove_cutoff and abs(val0 - pre1) < nomove_cutoff:
                #    correct += 1
                else:
                    incorrect += 1


                if abs(valm1 - val0) > nomove_cutoff and  valm1 < val0 and val0 < val1:
                    naive_correct += 1
                elif abs(valm1 - val0) > nomove_cutoff and valm1 > val0 and val0 > val1:
                    naive_correct += 1
                #elif abs(valm1 - val0) < nomove_cutoff and abs(val0 - val1) < nomove_cutoff:
                #    naive_correct += 1
                else:
                    naive_incorrect += 1

                predict_error = (val1 - pre1)**2

                naive_movement = valm1 - val0
                naive_predict = val0 + naive_movement
                naive_predict_reverse = val0 - naive_movement

                naive_error = (val1 - naive_predict)**2
                naive_error_reverse = (val1 - naive_predict_reverse)**2

                naive_errors.append(naive_error)
                naive_errors_reverse.append(naive_error_reverse)
                predict_errors.append(predict_error)
                #print valm1, val0, val1, pre1, predict_error, naive_error, naive_movement


            for dataset in ts_storage:
                get_stats_on_dataset(dataset, predictions)


            naive_mse = numpy.mean(naive_errors)
            naive_mse_reverse = numpy.mean(naive_errors_reverse)
            predict_mse = numpy.mean(predict_errors)

            print "Naive MSE:", naive_mse
            print "Naive MSE Reverse:", naive_mse_reverse
            print "Prediction MSE:", predict_mse




        out.write("PREDICT: {0} {1} {2} {3}\n".format(job.alg.name, correct, incorrect, correct / float(correct + incorrect)))
        print "PREDICT:", job.alg.name, correct, incorrect, correct / float(correct + incorrect)
        for line in output_mse_file:
            print line.split("\t")

        out.write("NAIVE: {0} {1} {2} {3}\n".format(job.alg.name, naive_correct, naive_incorrect, correct / float(naive_correct + naive_incorrect)))
        print "NAIVE:", job.alg.name, naive_correct, naive_incorrect, naive_correct / float(naive_correct + naive_incorrect)

print "\a"
print "\a"
print "\a"

