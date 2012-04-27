#!/bin/bash

#PBS -l nodes={{cluster_nodes}}:ppn={{cluster_procs}},walltime=5:00:00
#PBS -N {{cluster_expname}}
#PBS -e localhost:$PBS_O_WORKDIR/${PBS_JOBNAME}.e${PBS_JOBID}
#PBS -o localhost:$PBS_O_WORKDIR/${PBS_JOBNAME}.o${PBS_JOBID}

QSUB_PRIORITY={{cluster_priority}}

QSUB_CMD="-v {{cluster_prog_script}}"
echo "Sending the following command to QSUB: $QSUB_CMD"

#$QSUB_CMD
qsub $QSUB_CMD

