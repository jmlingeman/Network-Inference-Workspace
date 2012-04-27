################################################################################
# Filename: threshold_measures.py
# Author: Nikhil Ketkar
# Project: PyROC
# Version: 1.0
# Description: Stores threshold measure expressions and implements functionality
#              for adding new measures.
################################################################################

# Stores the expressions for the measures
expression_db = {}

################################################################################

# True Positive Rate
expression_db["tpr"] = "TP/P"

################################################################################

# Sensitivity
expression_db["sens"] = "TP/P"

################################################################################

# False Positive Rate
expression_db["fpr"] = "FP/N"

################################################################################

# Accuracy
expression_db["acc"] = "(TP + TN)/(P + N)"

################################################################################

# Error
expression_db["err"] = "(FP + FN)/(P + N)"

################################################################################

# Fallout
expression_db["fall"] = "FP/N"

################################################################################

# Recall
expression_db["rec"] = "TP/P"

################################################################################

# False Negative Rate
expression_db["fnr"] = "FN/P"

################################################################################

# Miss
expression_db["miss"] = "FN/P"

################################################################################

# True Negative Rate
expression_db["tnr"] = "TN/(TN+FP)"

################################################################################

# Specificity
expression_db["spec"] = "TN/(TN+FP)"

################################################################################

# Positive Predictive Value
expression_db["ppv"] = "TP/(TP+FP)"

################################################################################

# Precision
expression_db["prec"] = "TP/(TP+FP)"

################################################################################

# Negative Prediction Value
expression_db["npv"] = "TN/(TN+FN)"

################################################################################

# Prediction Conditioned Fallout
expression_db["pcfall"] = "FP/(TP+FP)"

################################################################################

# Prediction Conditioned Miss
expression_db["pcmiss"] = "FN/(TN+FN)"

################################################################################

# Rate of Positive Predictions
expression_db["rpp"] = "(TP+FP)/(TP+FP+TN+FN)"

################################################################################

# Rate of Negative Predictions
expression_db["rnp"] = "(TN+FN)/(TP+FP+TN+FN)"

################################################################################

# Phi Coefficient
expression_db["phi"] = "((TP*TN) - (FP*FN))/ sqrt((TP+FN) * (TN+FP)* (TP+FP) * (TN+FN))"

################################################################################

# Mathews Coefficient
expression_db["mat"] = "((TP*TN) - (FP*FN))/ sqrt((TP+FN) * (TN+FP)* (TP+FP) * (TN+FN))"

################################################################################

# Odds
expression_db["odds"] = "(TP*TN)/(FN*FP)"

################################################################################

# Hit
expression_db["hit"] = "TP/(TP+FP)"

################################################################################

# Lift
expression_db["lift"] = "(TP + FP)/(P+N)"

################################################################################


################################################################################
# Function: add_measure
# Description: Adds a new measure
# Inputs: 
#             measure name
#             measure expression
# Outputs:
#             None
################################################################################
def add_measure(measure_name, formula):
    expression_db[measure_name] = formula

################################################################################    