import os, sys
from datetime import datetime
from DataStore import *
from ReadData import *

def get_immediate_subdirectories(dir):
    return [name for name in os.listdir(dir) if os.path.isdir(os.path.join(dir, name))]

sys.path += get_immediate_subdirectories("./")

from ReadConfig import *
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'

# Create date string to appent to output_dir
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + t + "/"
os.mkdir(settings["global"]["output_dir"])

ts_filenames = settings["global"]["time_series_files"].split()

settings["global"]["time_series_delta_t"] = [1000 / 50] * 50
delta_t = settings["global"]["time_series_delta_t"]
print delta_t
timeseries_storage = []

timeseries_storage = ReadData("./algorithms/GeneNetWeaver/DREAM4-Noise/InSilicoSize50-Ecoli2_dream4_timeseries.tsv", True)


trans_factors = TFList(settings["global"]["tflist"])

trans_factors.tfs = timeseries_storage[0].gene_list

print ts_filenames
print delta_t


#from cmonkey import *
#cm = Cmonkey("550ath.ratios.fix", settings)
#cm.run(settings)

from dfg4grn import *
dfg = DFG4GRN(timeseries_storage, trans_factors, settings)
dfg.run(settings)
