################################################################################
# Filename: measures.py
# Author: Nikhil Ketkar
# Project: PyROC
# Version: 1.0
# Description: Implements the functionality for computing threshold measures
################################################################################

from base_measures import *
from threshold_measures import *
from math import *

class ThresholdPerformance:
    def __init__(self):
        self.data = {}
    def __str__(self):
        points = self.data.keys()
        points.sort()
        return ''.join(["Threshold = " + str(point) + ", " + self.data[point].__str__() + "\n" for point in points])

################################################################################
# Function: table_dump
# Description: Writes computed measures to a file
# Inputs: 
#             filename (required)
#             measure1 measure2 ... to be written
# Outputs:
#             None
################################################################################    
def measure(expression, labels, raw_predictions, threshold_count = 100, return_list = True):
    curr_dataset_measures = generate_dataset_measures(labels)
    curr_base_measures = generate_base_measures(labels, raw_predictions, threshold_count)
    threshold_values = curr_base_measures.data.keys()
    threshold_values.sort()
    curr_performance = ThresholdPerformance()
    curr_performance_list = []
    for curr_threshold_value in threshold_values:
        curr_threshold_base_measure = curr_base_measures.data[curr_threshold_value]
        TP = float(curr_threshold_base_measure.TP)
        TN = float(curr_threshold_base_measure.TN)
        FP = float(curr_threshold_base_measure.FP)
        FN = float(curr_threshold_base_measure.FN)
        P = float(curr_dataset_measures.P)
        N = float(curr_dataset_measures.N)
        try:
            curr_performance.data[curr_threshold_value] = eval(expression_db[expression])
            curr_performance_list.append(eval(expression_db[expression]))
        except:
            curr_performance.data[curr_threshold_value] = "NaN"
            curr_performance_list.append("NaN")
            
    if return_list:
        return curr_performance_list
    else:
        return curr_performance 

################################################################################


