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
from inferelator2 import *
from inferelator_pipeline import *



# Initialize settings file
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'


