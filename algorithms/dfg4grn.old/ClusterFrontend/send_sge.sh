#!/bin/bash

#$ -S /bin/bash
#$ -N state_space_test
# #$ -cwd
#$ -o state_space_test.out
#$ -e state_space_test.err

#PBS -l nodes=1:ppn=1,walltime=5:00:00
#PBS -N arabidopsis_state_space
#PBS -M jml654@nyu.edu
#PBS -m abe
#PBS -e localhost:$PBS_O_WORKDIR/${PBS_JOBNAME}.e${PBS_JOBID}
#PBS -o localhost:$PBS_O_WORKDIR/${PBS_JOBNAME}.o${PBS_JOBID}

cd "/state/partition1/jml654/DFG4GEN_31Mar2010/"

SCRIPT_NAME=$1
OUT_FILE=$2
N_MODELS=$3

PAR1=$4
VAL1=$5
PAR2=$6
VAL2=$7
PAR3=$8
VAL3=$9

PATH_DFG=`pwd`
LOG_FILE="$OUT_FILE.log"

MATLAB_PATH="/share/apps/matlab/R2009b/bin/matlab"
#MATLAB_PATH="/scratch/apps/matlab/bin/matlab"
#MATLAB_PATH="/opt/bin/matlab"
#MATLAB_PATH="/usr/bin/octave"

MATLAB_FLAGS="-nodisplay -nosplash -logfile $LOG_FILE -r"
#MATLAB_FLAGS="--eval"

echo "Starting $N_MODELS models, script $SCRIPT_NAME"
echo "parameters $PAR1 = $VAL1, $PAR2 = $VAL2, $PAR3 = $VAL3"

$MATLAB_PATH $MATLAB_FLAGS "addpath(genpath('$PATH_DFG')); \
GRN_Batch_MultiModel_AR1($N_MODELS, '$SCRIPT_NAME', '', '$OUT_FILE', 'n_steps_display', 0, '$PAR1', [$VAL1], '$PAR2', [$VAL2], '$PAR3', [$VAL3])"
