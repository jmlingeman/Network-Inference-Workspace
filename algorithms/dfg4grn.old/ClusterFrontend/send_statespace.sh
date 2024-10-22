#!/bin/bash

#$ -S /bin/bash
#$ -N state_space_test
# #$ -cwd
#$ -o state_space_test.out
#$ -e state_space_test.err

#PBS -l nodes=1:ppn=1,walltime=6:00:00
#PBS -N arabidopsis_state_space
#PBS -M jml654@nyu.edu
#PBS -m abe
#PBS -e localhost:$PBS_O_WORKDIR/${PBS_JOBNAME}.e${PBS_JOBID}
#PBS -o localhost:$PBS_O_WORKDIR/${PBS_JOBNAME}.o${PBS_JOBID}

cd DFG4GRN_31Mar2010/
echo `pwd` >> DIR.txt
#SCRIPT_NAME=$1
#OUT_FILE=$2
#N_MODELS=$3

#PAR1=$4
#VAL1=$5
#PAR2=$6
#VAL2=$7
#PAR3=$8
#VAL3=$9

PATH_DFG="/home/jml654/DFG4GRN_31Mar2010"
LOG_FILE="$OUT_FILE.log"

MATLAB_PATH="/share/apps/matlab/R2009b/bin/matlab"
#MATLAB_PATH="/scratch/apps/matlab/bin/matlab"
#MATLAB_PATH="/opt/bin/matlab"
#MATLAB_PATH="/usr/bin/octave"

MATLAB_FLAGS="-nodisplay -nosplash -logfile $LOG_FILE -r"
#MATLAB_FLAGS="--eval"

echo "Starting $N_MODELS models, script $SCRIPT_NAME" >> test_out.log
echo "parameters $PAR1 = $VAL1, $PAR2 = $VAL2, $PAR3 = $VAL3" >> test_out.log
echo $MATLAB_PATH $MATLAB_FLAGS "addpath(genpath('$PATH_DFG')); \
GRN_Batch_MultiModel_AR1($N_MODELS, '$SCRIPT_NAME', '', '$OUT_FILE', 'n_steps_display', 0, '$PAR1', [$VAL1], '$PAR2', [$VAL2], '$PAR3', [$VAL3])" >> command.log

$MATLAB_PATH $MATLAB_FLAGS "addpath(genpath('$PATH_DFG')); \
GRN_Batch_MultiModel_AR1($N_MODELS, '$SCRIPT_NAME', '', '$OUT_FILE', 'n_steps_display', 0, '$PAR1', [$VAL1], '$PAR2', [$VAL2], '$PAR3', [$VAL3])" 2> ~/error_out.log
