#!/bin/bash

PATH_OCTAVE="/Applications/Octave.app/Contents/Resources/bin/octave"
PATH_DFG="/Users/piotr/Documents/Projets/GRN/DFG4GRN"

SCRIPT_NAME=$1
OUT_FILE=$2
NUM_MODELS=$3

PAR1=$4
VAL1=$5
PAR2=$6
VAL2=$7
PAR3=$8
VAL3=$9

$PATH_OCTAVE --eval "addpath $PATH_DFG; GRN_Batch_MultiModel_AR1($NUM_MODELS, '$SCRIPT_NAME', '', '$OUT_FILE', 'n_steps_display', 0, '$PAR1', [$VAL1], '$PAR2', [$VAL2], '$PAR3', [$VAL3])"

