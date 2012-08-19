#!/bin/bash

for i in `qstat -au jml654  | grep $1 | cut -f1,2 -d "."`
  do echo killing job $i 
     qdel $i
done
