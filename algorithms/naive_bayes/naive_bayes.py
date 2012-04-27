import math, os, sys
import scipy
import scipy.stats.stats as stats
import numpy

class NaiveBayesClassifier:

    phi = [(-3.0, 0.0013),(-2.5,0.0062),(-2.3, 0.011), (-2.0, 0.023),(-1.8, 0.036)]
    phi += [(-1.5, 0.067), (-1.3, 0.097), (-1.0, 0.16), (-0.8,0.21), (-0.5,0.31)]
    phi += [(-0.3,0.38),(-0.2,0.42), (-0.1,0.46),(0.0,0.5),(0.1,0.54),(0.2,0.58)]
    phi += [(1.2,0.88),(1.5, 0.93),(1.7,0.95),(2.0, 0.98), (2.5, 0.99), (3.0,0.999)]

    def __init__(self, microarray):
        # We need some priors for the classes, which can be:
        # Up, Down, None
        # Since we probably no nothing about this particular subnetwork,
        # We will just set these equal
        self.priors = [0.333, 0.333, 0.333]


    def interpolatePhi(self, z):
        # Find the value that x is at or above
        i = 0

        if z > self.phi[len(self.phi)-1][0]:
            return 1.0
        elif z == self.phi[0][0]:
            return self.phi[0][1]
        elif z < self.phi[0][0]:
            return 0.0
        else:
            for k,p in enumerate(self.phi):
                if z >= p[0]:
                    i = k
                    break

        j = i-1
        big_z = self.phi[i][0]
        big_p = self.phi[i][1]
        small_z = self.phi[j][0]
        small_p = self.phi[j][1]

        ratio = (z - small_z) / (big_z - small_z)

        return (ratio * big_p) + ((1 - ratio) * small_p)


    def getCondProb(self, node, parent):
        pass

    def getProbDens(self, given, vals):
        """ Compute the number of z-scores the given value is away from the
        average of vals """

        his = []
        lows = []

        s = scipy.std(vals)
        diff = list( abs( given - numpy.array( vals ) ) )

        zscores = numpy.array(diff) / numpy.array(s)
        print zscores
        for z in zscores:
            his.append(self.interpolatePhi(z + 0.1))
            lows.append(self.interpolatePhi(z))

        print his
        print lows
        diff = list( numpy.array(his) - numpy.array(lows) )
        print diff
        avg = scipy.mean( diff )

        return avg

    #def naiveBayes(self, microarray):
    #    for i, gene in enumerate(microarray.gene_list):



vals = [50, 70, 120, 500, 10000]
n = NaiveBayesClassifier(vals)
print n.getProbDens(100,vals)
