#!/bin/bash

#PBS -l nodes=1:ppn=1,walltime=5:00:00
#PBS -N arabidopsis_state_space
#PBS -M jml654@nyu.edu
#PBS -m abe
#PBS -e localhost:$PBS_O_WORKDIR/${PBS_JOBNAME}.e${PBS_JOBID}
#PBS -o localhost:$PBS_O_WORKDIR/${PBS_JOBNAME}.o${PBS_JOBID}

PATH_TO_SCRIPT=$1
SCRIPT_NAME=$2
OUT_FILE=$3
NUM_MODELS=$4

PAR1=$5
VAL1=$6
PAR2=$7
VAL2=$8
PAR3=$9
VAL3=${10}

QSUB_PRIORITY=-1
SEND_SCRIPT="${PATH_TO_SCRIPT}/send_statespace.sh"

QSUB_CMD="$SEND_SCRIPT $SCRIPT_NAME $OUT_FILE $NUM_MODELS $PAR1 $VAL1 $PAR2 $VAL2 $PAR3 $VAL3"
echo "Sending the following command to QSUB: $QSUB_CMD"

#$QSUB_CMD
qsub $QSUB_CMD

