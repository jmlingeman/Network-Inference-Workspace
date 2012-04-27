################################################################################
# Filename: pyroc.py
# Author: Nikhil Ketkar
# Project: PyROC 
# Version: 1.0
# Description: Initialization file, loads the necessary modules and implements
#              plotting functionality.
################################################################################

from table import *
from measures import *
from base_measures import *
from combine import *
from dumper import *
from pylab import *

################################################################################
# Function: plot_measure
# Description: Implements plotting functionality
# Inputs: 
#             x (measure) required
#             y (measure) required
# Outputs:
#             None
################################################################################
def plot_measure(x,y, *args):
    """Plots measure x vs. measure y and passes the other arguments to plot."""

    # Temporary storage for the cleaned measures
    cleaned_x = [] 
    cleaned_y = []

    # Clean the NaN values in the input measures

    # Cycle through each member of the lists
    for position in xrange(0, len(x)):
        # Check if the value of the measure at position in NaN
        if x[position] != "NaN" and y[position] != "NaN":
            # Put non-NaN values in the cleaned measure lists
            cleaned_x.append(x[position])
            cleaned_y.append(y[position])
            
    #Call plot annd pass the remaing arguments
    plot(cleaned_x, cleaned_y, *args)

################################################################################
