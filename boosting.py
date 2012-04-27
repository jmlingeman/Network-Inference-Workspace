import os, sys
from DataStore import *
from Network import *
from JobManager import *

class Boosting:
    training_results = None
    goldnet = None

    # Read in the gold std network.  Load in the training data if there already
    # is one.  Otherwise, assume this is a dataset that needs to be trained.
    def __init__(self, goldnet, loadtrain=None):


