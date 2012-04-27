################################################################################
# Filename: combine.py
# Author: Nikhil Ketkar
# Project: PyROC
# Version: 1.0
# Description: Implements the functionality for combining measures
################################################################################

from math import *

################################################################################
# Function: combine
# Description: Combines computed measures
# Inputs: 
#             parameter describing how to combine the mesures
#             measure1 measure2 ... to be combined
# Outputs:
#             combined measure
################################################################################
def combine(parameter = "mean", *args):
    sum = []
    mean = []
    var = []
    stdev = []
    stderr = []
    for element in xrange(0, len(args[0])):
        sum.append(0.0)
        mean.append(0.0)
        var.append(0.0)
        stdev.append(0.0)
        stderr.append(0.0)

    for list in args:
        for position in xrange(0, len(list)):
            sum[position] += list[position]
    
    for position in xrange(0, len(sum)):
        mean[position] = sum[position]/len(args)
    
    for list_position in xrange(0, len(args)):
        for position in xrange(0, len(args[list_position])):
            var[position] += (mean[position] - args[list_position][position]) * (mean[position] - args[list_position][position])
        var[position] = var[position] / (len(args) - 1)
        stdev[position] = sqrt(var[position])
        stderr[position] = stdev[position] / sqrt(len(args))
    
    if parameter == "sum":
        return sum
    elif parameter == "mean":
        return mean
    elif parameter == "var":
        return var
    elif parameter == "stddev":
        return stdev
    elif parameter == "stderr":
        return stderr
    else:
        return None

################################################################################