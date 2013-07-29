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

from inferelator_pipeline import *
from ReadConfig import *

# Instantsiate settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'
settings["global"]["experiment_name"] = "InferelatorPipeline-"+sys.argv[1]



# Create date string to append to output_dir
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])

# Read in the gold standard network

ko_file, kd_file, ts_file, wt_file, mf_file, goldnet = get_example_data_files(sys.argv[1], settings)

# Read data into program
# Where the format is "FILENAME" "DATATYPE"
knockout_storage = ReadData(ko_file[0], "knockout")
knockdown_storage = ReadData(kd_file[0], "knockdown")
timeseries_storage = ReadData(ts_file[0], "timeseries")
wildtype_storage = ReadData(wt_file[0], "wildtype")



# Setup job manager
jobman = JobManager(settings)

# Make BANJO jobs
infjob = InferelatorPipeline()
infjob.setup(None, wildtype_storage, settings, timeseries_storage, None, "InferelatorPipeline")
jobman.queueJob(infjob)

print jobman.queue
jobman.runQueue()
jobman.waitToClear()

accs = []
precs = []

#dfg = DFG4GRN()
#dfg.setup(timeseries_storage,  TFList(timeseries_storage[0].gene_list), settings, "DFG4GRN_Baseline", 20)
#jobman.queueJob(dfg)



#import pickle

#tprs, fprs, rocs = GenerateMultiROC(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallROC.pdf")
#ps, rs, precs = GenerateMultiPR(jobman.finished, goldnet, False, settings["global"]["output_dir"] + "/OverallPR.pdf")

##print "Accuracy:"
##for row in accs:
    ##print row

#print "ROC Data:"
#for row in rocs:
    #print row

#print "PR Data:"
#for row in precs:
    #print row

#pickle.dump((jobman.finished, accs, rocs, precs), open(settings["global"]["output_dir"] + "./Inferelator.pickle", 'w'))

#outfile = open(settings["global"]["output_dir"] + "./Inferelator_Results.csv",'w')
#outfile.write("ROC = " + str(rocs))
#outfile.write("\n")
#outfile.write("PR = " + str(precs))

#outfile.close()


#print "AOCS"
#for r in rocs:
    #print r.auc()
SaveResults(jobman.finished, goldnet, settings)
