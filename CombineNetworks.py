import numpy
from Network import *


def VotingNetwork(binary, *networks):
    """ A simple, straightforward voting procedure.  Just sum the networks together,
    then the edges with the most votes are the edges that we take. """
    # Init matrix of zeros
    n[0] = base
    sum_matrix = []
    for i,row in enumerate(base):
        sum_matrix.append([])
        for j,col in enumerate(row):
            sum_matrix[i].append(0)

    sum_matrix = numpy.array(sum_matrix)
    for n in networks:
        sum_matrix += numpy.array(n)

    return sum_matrix

def WeightedNetworks(weights, *networks):
    pass

def TrainWeightedNetworks(goldnet, *networks):
    pass

def RankedListFromMat(matrix, gene_list):
    ranked_list = []
    if type(matrix) == type([]):
        for i,row in enumerate(matrix):
            for j,col in enumerate(row):
                ranked_list.append([gene_list[i], gene_list[j], matrix[i][j])
    elif type(matrix) == type({}):


