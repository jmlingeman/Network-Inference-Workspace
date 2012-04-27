################################################################################
# Filename: base_measures.py
# Author: Nikhil Ketkar
# Project: PyROC
# Version: 1.0
# Description: Implements the functionality for computing P, N, TP, TN, FP, FN
################################################################################

################################################################################
# Class: DatasetMeasures
# Description: Stores dataset measures
################################################################################
class DatasetMeasures:
    def __init__(self):
        self.P = 0
        self.N = 0
    def __str__(self):
        return "P = " + str(self.P) + ", N = " + str(self.N)

################################################################################
# Function: generate_dataset_measures
# Description: Computes P and N
# Inputs: 
#             class labels (list)
# Outputs:
#             DatasetMeasures object
################################################################################
def generate_dataset_measures(labels):
    curr_dataset_measures = DatasetMeasures()
    for label in labels:
        if label == 1:
            curr_dataset_measures.P += 1
        elif label == 0:
            curr_dataset_measures.N += 1
    return curr_dataset_measures

################################################################################
# Class: BaseMeasures
# Description: Stores base measures
################################################################################
class BaseMeasures:
    def __init__(self):
        self.TP = 0
        self.TN = 0
        self.FP = 0
        self.FN = 0
    def __str__(self):
        return "TP = " + str(self.TP) + ", " +\
               "TN = " + str(self.TN) + ", "+\
               "FP = " + str(self.FP) + ", "+\
               "FN = " + str(self.FN) 

################################################################################
# Class: ThresholdBaseMeasures
# Description: Stores thresholded measures
################################################################################
class ThresholdBaseMeasures:
    def __init__(self):
        self.data = {}
    def __str__(self):
        points = self.data.keys()
        points.sort()
        return ''.join(["Threshold = " + str(point) + ", " + self.data[point].__str__() + "\n" for point in points])

################################################################################
# Function: predict_at_threshold
# Description: Computes prediction at a threshold
# Inputs: 
#             labels
#             raw prediction
#             threshold
# Outputs:
#             prediction
################################################################################
def predict_at_threshold(labels, raw_predictions, threshold):
    predictions = []
    for raw_prediction in raw_predictions:
        if raw_prediction < threshold:
            predictions.append(0)
        else:
            predictions.append(1)
    
    curr_measures = BaseMeasures()
    
    for example in xrange(0, len(labels)):
        if predictions[example] == labels[example] and labels[example] == 1:
            curr_measures.TP += 1
        elif predictions[example] == labels[example] and labels[example] == 0:
            curr_measures.TN += 1
        elif predictions[example] != labels[example] and labels[example] == 1:
            curr_measures.FN += 1
        elif predictions[example] != labels[example] and labels[example] == 0:
            curr_measures.FP += 1
    
    return curr_measures

################################################################################
# Function: predict_accross_thresholds
# Description: Generates predictions accross throlds
# Inputs: 
#             labels
#             raw_predictions
# Outputs:
#             predictions
################################################################################
def predict_accross_thresholds(labels, raw_predictions, thresholds):
    curr_measures = ThresholdBaseMeasures()
    for threshold in thresholds:
        curr_measures.data[threshold] = predict_at_threshold(labels, raw_predictions, threshold)
    return curr_measures

################################################################################
# Function: generate_threshold
# Description: Generates thresholds
# Inputs: 
#             threshold count default = 100
# Outputs:
#             thresholds
################################################################################
def generate_thresholds(threshold_count = 100):
    thresholds = []
    increment = 1.0 / threshold_count
    for value in xrange(0, threshold_count + 1):
        thresholds.append(value * increment)
    return thresholds

################################################################################
# Function: generate_base_measures
# Description: Computes base measure at thresholds after generation
# Inputs: 
#             labels
#             raw predictions
#             threshold_count 
# Outputs:
#             computed measure
################################################################################
def generate_base_measures(labels, raw_predictions, threshold_count):
    thresholds = generate_thresholds(threshold_count)
    return predict_accross_thresholds(labels, raw_predictions, thresholds)

################################################################################