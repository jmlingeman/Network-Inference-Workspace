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
from ReadConfig import *
from AnalyzeResults import *
from Helpers import *
from dfg4grn import *
from banjo import *
from clr import *
from cmonkey import *
from convex_optimization import *
from genie3 import *
from mcz import *
from nir import *
from nirest import *
from tdaracne import *
from tlclr import *
from inferelator import *
from inferelator_pipeline import *



# Initialize settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'
settings["global"]["experiment_name"] = "DFG4GRN-"+sys.argv[1]
if len(sys.argv) > 2:
  settings["global"]["experiment_name"] += "-" + sys.argv[2]

# Set up output directory
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + \
    settings["global"]["experiment_name"] + "-" + t + "/"
os.mkdir(settings["global"]["output_dir"])

