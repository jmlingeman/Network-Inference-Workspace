# coding: utf-8
import math, itertools, random
from scipy import linspace, polyval, polyfit, sqrt, stats, randn
import scipy
import itertools
from AnalyzeResults import *

class GradientDescent:

    def run(self, jobs, goldnets):
        # The idea is to run a gradient descent to create a new model
        # for each gene across the jobs, solving for weights a_n for the
        # model target_gene = a1A + a2B + a3C + a4AC + a5AB + a6BC +
        # a7ABC ...

        # Are A B C etc weights or expvals? I'll try weights first

        # Just try this for one job first
        for job in jobs:
            gene_list = job.alg.gene_list

            combination_list = []
            for L in range(len(gene_list)+1):
                combination_list.append( itertools.combinations(gene_list), L )

            for target_gene in gene_list:
                # We want to build a model for target_gene from a
                # combination of all of the other genes in the network
                # (most of which will be 0, but include them... those
                # terms will just be 0) using those genes in-weights to
                # target_gene

                # Initialize the weights for this gene between -1 and 1
                a_weights = {}
                for c in combination_list:
                    a_weights[c] = random.random() * 2 - 1


# TODO
class LinearRegression:

    def run(self, jobs, goldnet=None):
        return 0







class SimulatedAnnealing:

  def run(self, jobs, goldnets, starting_temp=10, seed=None, max_iter=3000, alpha=0.9992, restart=True, transition_prob=0.3):

    self.jobs = jobs
    self.goldnets = goldnets
    self.starting_temp = starting_temp

    # Set seed, if defined
    if seed != None:
      random.seed(seed)

    best_e = 9999999
    e = 9999999
    best_weights = []
    best_net = None
    best_iter = 0
    iter = 0
    self.temp = starting_temp

    # Initialize weights
    weights = []
    for job in jobs[0]:
      # TODO Make this rand? Option?
      weights.append(random.random() * 10)

    while iter < max_iter and best_e > 0 and self.temp > 0 :
      new_weights = self.select_neighbor(weights, self.temp)
      new_e, new_net = self.calculate_energy(jobs, new_weights, goldnets)

      if abs(e - new_e) < 0.01:
        reset_count += 1

      print self.P(e, new_e, self.temp)
      if self.P(e, new_e, self.temp) > random.random():
        weights = new_weights
        e = new_e
        net = new_net

      if new_e < best_e:
        best_weights = weights
        best_e = e
        best_iter = iter
        best_net = new_net
        reset_count = 0

      if restart and reset_count > 100:
        weights = []
        for job in jobs[0]:
          weights.append(random.random() * 10)
        reset_count = 0



      iter += 1
      self.update_temp(alpha)

      print "Iter: {6}\tTemp: {7}\tBestE: {0}\t\tBestW: {1}\t\tCurE: {2}\t\tCurW: {3}\t\tNewE: {4}\t\tNewW: {5}".format(best_e, best_weights, e, weights, new_e, new_weights, iter, self.temp)

    self.best_e = best_e
    self.best_iter = best_iter
    self.best_weights = best_weights
    self.best_net = best_net
    return best_weights, best_e

# Try this first by adjusting weights in the matrix, then sum the matrices, then try to adjust matrix output with weight
  def calculate_energy(self, jobs, weights, goldnets):
    for i in range(len(jobs)):
      jobs[i] = sorted(jobs[i], key=lambda job: job.alg.name)

    batch_nets = []
    for batch in jobs:
      nets = []
      for i,job in enumerate(batch):
        net = job.alg.network.copy()
        for gene1 in net.network:
          for gene2 in net.network[gene1]:
            net.network[gene1][gene2] *= weights[i]
        nets.append(net)
      batch_nets.append(nets)

    sum_roc = 0
    for i,batch in enumerate(batch_nets):
      compiled_net = batch[0].copy()
      for net in batch[1:]:
        for gene1 in net.network:
          for gene2 in net.network[gene1]:
            compiled_net.network[gene1][gene2] += net.network[gene1][gene2]

      tpr, fpr, roc_auc = GenerateROC(compiled_net, goldnets[i])
      sum_roc += roc_auc

    return (len(jobs)-sum_roc) * 1000, compiled_net




  def update_temp(self, alpha):
    self.temp *= alpha

  def select_neighbor(self, weights, temp):
    new_weights = weights[:]
    for i in range(len(new_weights)):
      new_weights[i] += (random.random() * 2 - 1) * ((2.0 / self.starting_temp) * self.temp)
      #if new_weights[i] < 0:
        #new_weights[i] = 0
      #if new_weights[i] > 10:
        #new_weights[i] = 10
    return new_weights


  def P(self, e, new_e, temp):
    #In the formulation of the method by Kirkpatrick et al., the acceptance
    #probability function P(e,e',T) was defined as 1 if e' < e, and exp((e − e') / T)
    #otherwise
    if abs(new_e - e) < 0.0001:
      return 0.1
    elif new_e < e:
      return 1
    else:
      return math.exp((e - new_e) / temp)

  def test(self, jobs, goldnet, settings):
    baseweights = []
    jobs = sorted(jobs, key=lambda job: job.alg.name)
    print "New jobs:"
    for job in jobs:
      print job.alg.name
      if "mcz" in job.alg.name.lower():
        basejob = job
        baseweights.append(1)
      else:
        baseweights.append(0)
    print "Old jobs:"
    for batch in self.jobs:
      for job in batch:
        print job.alg.name

    baseline_roc, baseline_network = self.calculate_energy([jobs], baseweights, [goldnet])
    roc, network = self.calculate_energy([jobs], self.best_weights, [goldnet])
    print network.calculateAccuracy(goldnet)
    print "Test network AUROC: {0}".format(1 - (roc / 1000.0))
    print self.best_weights
    print "Test baseline: {0}".format(1 - (baseline_roc / 1000.0))
    return roc, baseline_roc, network, baseline_network, basejob




