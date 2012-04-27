#!/bin/python

""" This is a function to calculate Median Corrected Z-Scores from the
Greenfield, et al 2010 Inferelator Pipeline paper.

Implemented by Jesse Lingeman at NYU, licensed under the MIT license.

"""

import math
import numpy
import sys
from operator import itemgetter

def import_wildtype_data(filename):
    file = open(filename, 'r')
    wild_type_data = {}
    header = file.readline()
    header = header.split()

    for gene in header:
        wild_type_data[gene] = []

    for i, row in enumerate(file):
        row = row.split()
        for datum in row:
            wild_type_data[header[i]].append(float(datum))

    return wild_type_data


def import_knockout_data(filename):
    """ Data format of the knockout data is knockout[gene][knocked_out_gene] """
    file = open(filename, 'r')
    knockout_data = {}
    header = file.readline()

    # Initialize hash of hashes
    header = header.split()
    for gene in header:
        knockout_data[gene] = {}

    for i, row in enumerate(file):
        row = row.split()
        gene = row[0]
        for j, datum in enumerate(row[1:]):
            if i != j:
                knockout_data[header[i]][header[j]] = float(datum)

    return knockout_data


def combine_data(wild_type, knockout):
    combined_data = {}
    for gene in wild_type.keys():
        combined_data[gene] = wild_type[gene] + knockout[gene].values()
        print combined_data[gene]
    return combined_data

def find_medians(combined_data):
    medians = {}

    # Find the median of the combined data
    for gene in combined_data.keys():
        medians[gene] = numpy.median(combined_data[gene])

    return medians

def calculate_stddev(combined_data):
    stddevs = {}

    for gene in combined_data.keys():
        stddevs[gene] = numpy.std(combined_data[gene])

    return stddevs

def calculate_zscores(wild_type, knockout):
    combined_data = combine_data(wild_type, knockout)
    medians = find_medians(combined_data)
    stddevs = calculate_stddev(combined_data)

    zscores = {}
    for xi in knockout.keys():
        zscores[xi] = {}
        for xij in knockout[xi].keys():
            zscores[xi][xij] = (knockout[xi][xij] - medians[xi]) / stddevs[xi]

    return zscores

def rank_zscores(zscores):
    ranked_scores = []
    for g1 in zscores.keys():
        for g2 in zscores[g1].keys():
            ranked_scores.append((g2, g1, zscores[g1][g2]))
    ranked_scores = sorted(ranked_scores, key=itemgetter(2), reverse=True)

    return ranked_scores


def write_output(filename, ranked_zscores):
    file = open(filename, 'w')
    for row in ranked_zscores:
        file.write(row[0] + '\t' + row[1] + '\t' + str(row[2]) + '\n')


def main(wt_file, ko_file, out_file):
    wild_type = import_wildtype_data(wt_file)
    knockout = import_knockout_data(ko_file)

    zscores = calculate_zscores(wild_type, knockout)
    ranked_zscores = rank_zscores(zscores)

    write_output(out_file, ranked_zscores)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2], sys.argv[3])
