#!/bin/python

import sys

f = sys.argv[1]
lines = open(f).readlines()

arr = []
for l in lines:
    arr.append(l.strip().split("\t"))

arr = map(list, zip(*arr))

exp = f[0:len(f)-4]
w = f[0:len(f)-4] + "-converted.tsv"
w = open(w, 'w')

header = ""
for i in range(len(arr[0])-1):
    header += exp + "_" + str(i) + "\t"
w.write(header.strip()+"\n")

for row in arr:
    r = ""
    for val in row:
        r += val + "\t"
    w.write(r.strip() + "\n")

w.flush()
w.close()
