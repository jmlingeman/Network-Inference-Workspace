from operator import itemgetter, attrgetter
import sys

class Report:
    def __init__(self):
        self.num_correct = 0
        self.num_incorrect = 0
        self.per_correct = 0
        self.gold_fanins = []
        self.fanins = []
        self.fanins_guessed = 0
        self.fanins_correct = 0
        self.fanins_incorrect = 0
        self.gold_fanouts = []
        self.fanouts = []
        self.fanouts_guessed = 0
        self.fanouts_correct = 0
        self.fanouts_incorrect = 0
        self.gold_cascades = []
        self.cascades = []
        self.cascades_guessed = 0
        self.cascades_correct = 0
        self.cascades_incorrect = 0
        self.gold_feedforward_loops = []
        self.feedforward_loops = []
        self.feedforward_loops_guessed = 0
        self.feedforward_loops_correct = 0
        self.feedforward_loops_incorrect = 0

    def ToString(self):
        s = ""
        s += "Edges correct: {0}\nEdges incorrect: {1}\n".format(self.num_correct, self.num_incorrect)
        s += "Predicted Fan-In nodes:\n{0}\n".format(self.fanins)
        s += "Actual Fan-In nodes:\n{0}\n".format(self.gold_fanins)
        s += "Number Fan-In nodes correct: {0}\n\n".format(self.fanins_correct)
        s += "Predicted Fan-Out nodes:\n{0}\n".format(self.fanouts)
        s += "Actual Fan-Out nodes:\n{0}\n".format(self.gold_fanouts)
        s += "Number Fan-Out nodes correct: {0}\n\n".format(self.fanouts_correct)
        s += "Predicted Cascades nodes:\n{0}\n".format(self.cascades)
        s += "Actual Cascade nodes:\n{0}\n".format(self.gold_cascades)
        s += "Number Cascade nodes correct: {0}\n\n".format(self.cascades_correct)
        s += "Predicted Feedforward Loop nodes:\n{0}\n".format(self.feedforward_loops)
        s += "Actual Feedforward Loop nodes:\n{0}\n".format(self.gold_feedforward_loops)
        s += "Number Feedforward Loop nodes correct: {0}\n\n".format(self.feedforward_loops_correct)

        return s


class Network:
    network = None
    original_network = []
    gene_list = None
    signed = None
    binary = None
    thresh = 0
    cutoff = 0  # For taking the top N edges

    def __init__(self, network=None):
        self.network = {}
        self.original_network = []
        self.gene_list = []
        self.signed = False
        self.binary = False
        self.thresh = 0
        self.cutoff = 0


    def copy(self):
        import copy
        copynet = Network()
        copynet.network = copy.deepcopy(self.network)
        copynet.original_network = self.original_network[:]
        copynet.gene_list = self.gene_list[:]
        return copynet


    def read_dot_file(self, input_file, gene_list):
        file = open(input_file, 'r')
        lines = file.readlines()
        self.gene_list = gene_list

        # Instantiate the network
        for gene in gene_list:
            self.network[gene] = {}
            for gene2 in gene_list:
                self.network[gene][gene2] = 0

        for line in lines[1:len(lines)-2]:
            #print line
            tokens = line.split()
            #print tokens
            gene1 = tokens[0]
            gene2 = tokens[2]
            #print gene1, gene2
            self.network[gene1][gene2] = 1
            #print self.network
            #print gene_list


    def read_netmatrix(self, network_list, gene_list, signed=True, binary=False, thresh=0, cutoff=0):
        """ This function takes an NxN list of network connections either in
        binary or floating point format between nodes.  Gene list is a simple
        list of gene names that are ordered in the same way that the NxN
        network matrix is, and then a bool as to whether or not they are
        signed."""

        self.gene_list = gene_list
        self.signed = signed
        self.binary = binary
        self.thresh = thresh
        self.original_network = network_list
        self.network = {}

        #print network_list

        for i in xrange(len(network_list)):
            for j in xrange(len(network_list)):
                val = 0
                if gene_list[i] not in self.network.keys():
                    self.network[gene_list[i]] = {}

                if self.signed:
                    val = float(network_list[i][j])
                else:
                    val = abs(float(network_list[i][j]))

                if self.binary:
                    if val > self.thresh:
                        val = 1
                    else:
                        val = 0

                self.network[gene_list[i]][gene_list[j]] = val

    def read_netmatrix_file(self, file, gene_list, signed=True, binary=False, thresh=0, cutoff=0):
        """ This function takes an NxN list of network connections either in
        binary or floating point format between nodes.  Gene list is a simple
        list of gene names that are ordered in the same way that the NxN
        network matrix is, and then a bool as to whether or not they are
        signed."""

        output = open(file, 'r')
        header = output.readline()
        #gene_list = header.split()
        net = []
        for line in output:
            line = line.split()[1:]
            #print line
            net.append(line)

        self.read_netmatrix(net, gene_list, signed, binary, thresh, cutoff)

    def write_network(self, filename, binary=False):
        output = open(filename, 'w')
        header = "\t".join(self.gene_list) + "\n"
        file = header
        for g1 in self.gene_list:
            file += g1
            for g2 in self.gene_list:
               file += "\t" + str(self.network[g1][g2])
            file += "\n"

        output.write(file)
        output.close()

    def get_ranked_list(self):
      ranked_list = []
      for gene1 in self.gene_list:
        for gene2 in self.gene_list:
          ranked_list.append((gene1, gene2, self.network[gene1][gene2]))
      return sorted(ranked_list, key=lambda x: x[2], reverse=True)

    def compare_graph_network(self, goldnet, filename, topn=None):
      import networkx as nx
      #import Image as im
      import matplotlib as plt
      import pylab as P
      import pydot
      G = nx.DiGraph()

      net = self
      if topn != None:
        net = self.copy()
        net.set_top_edges_percent(topn)

      # Add all of the nodes
      for gene in net.network.keys():
        G.add_node(gene)

      Gp = nx.to_pydot(G)

      if goldnet == []:
        for gene1 in net.network.keys():
            for gene2 in net.network.keys():
                if net.network[gene1][gene2] != 0:
                    Gp.add_edge(pydot.Edge(gene1, gene2, style="bold"))
      else:
          for gene1 in net.network.keys():
            for gene2 in net.network.keys():
              # Figure out of node is correct, apply color/style based on that
              # If true positive, bold line
              if net.network[gene1][gene2] != 0 and goldnet.network[gene1][gene2] != 0:
                # thick line
                Gp.add_edge(pydot.Edge(gene1, gene2, style="bold"))

              # If false positive, dashed line
              elif net.network[gene1][gene2] != 0 and goldnet.network[gene1][gene2] == 0:
                Gp.add_edge(pydot.Edge(gene1, gene2, style="dashed"))

              # If false negative, dotted line
              elif net.network[gene1][gene2] == 0 and goldnet.network[gene1][gene2] != 0:
                Gp.add_edge(pydot.Edge(gene1, gene2, style="dotted"))

      # now output your graph to a file and display it
      outstem = filename
      dot = pydot.Dot(Gp)
      Gp.set_size('18!')
      Gp.set_rankdir('RL')
      Gp.set_page('20')
      Gp.set_ranksep(0.5)
      Gp.set_nodesep(0.25)
      #Gp.write_pdf(outstem + '_dot.pdf', prog='dot')  # writes Gp to png file #use
      #Gp.write_pdf(outstem + '_neato.pdf', prog='neato')  # writes Gp to png file #use
      #Gp.write_pdf(outstem + '_fdp.pdf', prog='fdp')  # writes Gp to png file #use
      #Gp.write_pdf(outstem + '_twopi.pdf', prog='twopi')  # writes Gp to png file #use
      Gp.write_pdf(outstem + '_circo.pdf', prog='circo')  # writes Gp to png file #use


    def set_top_edges(self, topn, binary=False, prop_weights=False):
        ranking = []
        for g1 in self.gene_list:
            for g2 in self.gene_list:
                ranking.append((g1, g2, self.network[g1][g2]))
        ranking = sorted(ranking, key=lambda ranking: abs(ranking[2]), reverse=True)
        min_vote = 0.1
        vote_step = 1.0 / topn
        for i,row in enumerate(ranking):
            if i < topn:
                gene1, gene2, val = row
                if binary:
                  if val > 0:
                      ranking[i] = (gene1, gene2, 1)
                  if val < 0:
                      ranking[i] = (gene1, gene2, -1)
                elif prop_weights:
                  vote = 1.0 - (i * vote_step)
                  if vote < min_vote:
                    vote = min_vote
                  ranking[i] = (gene1, gene2, vote)
                else:
                  ranking[i] = (gene1, gene2, val)

            else:
                gene1, gene2, val = row
                ranking[i] = (gene1, gene2, 0)
        self.read_networklist(ranking)

    def set_top_edges_percent(self, topn):
        ranking = []
        for g1 in self.gene_list:
            for g2 in self.gene_list:
                ranking.append((g1, g2, self.network[g1][g2]))
        ranking = sorted(ranking, key=lambda ranking: abs(ranking[2]), reverse=True)
        for i,row in enumerate(ranking):
            if i / float(len(self.gene_list) * len(self.gene_list)) * 100 < topn:
                gene1, gene2, val = row
                ranking[i] = (gene1, gene2, val)
            else:
                gene1, gene2, val = row
                ranking[i] = (gene1, gene2, 0)
        self.read_networklist(ranking)

    def set_bottom_edges_percent(self, topn):
        ranking = []
        for g1 in self.gene_list:
            for g2 in self.gene_list:
                ranking.append((g1, g2, self.network[g1][g2]))
        ranking = sorted(ranking, key=lambda ranking: abs(ranking[2]), reverse=False)
        for i,row in enumerate(ranking):
            if i / float(len(self.gene_list) * len(self.gene_list)) * 100 < topn:
                gene1, gene2, val = row
                ranking[i] = (gene1, gene2, val)
            else:
                gene1, gene2, val = row
                ranking[i] = (gene1, gene2, 0)
        self.read_networklist(ranking)

    def set_top_and_bottom_edges_percent(self, topn):
        topn = float(topn)
        ranking = []
        for g1 in self.gene_list:
            for g2 in self.gene_list:
                ranking.append((g1, g2, self.network[g1][g2]))
        top_ranking = sorted(ranking, key=lambda ranking: abs(ranking[2]), reverse=True)
        bottom_ranking = sorted(ranking, key=lambda ranking: abs(ranking[2]), reverse=False)


        for i,row in enumerate(top_ranking):
            if i / float(len(self.gene_list) * len(self.gene_list)) * 100 < (topn/2.0):
                gene1, gene2, val = row
                top_ranking[i] = (gene1, gene2, val)
            else:
                gene1, gene2, val = row
                top_ranking[i] = (gene1, gene2, 0)

        for i,row in enumerate(bottom_ranking):
            if i / float(len(self.gene_list) * len(self.gene_list)) * 100 < (topn/2.0):
                gene1, gene2, val = row
                bottom_ranking[i] = (gene1, gene2, val)
            else:
                gene1, gene2, val = row
                bottom_ranking[i] = (gene1, gene2, 0)

        ranking = sorted(ranking, key=lambda ranking: abs(ranking[2]), reverse=False)
        for i,row in enumerate(ranking):
            if i <= topn:
                ranking[i] = bottom_ranking[i]


        #ranking = sorted(ranking, key=lambda ranking: abs(ranking[2]), reverse=True)
        for i,row in enumerate(ranking):
            if i > topn:
                ranking[i] = top_ranking[i]
        self.read_networklist(ranking)


    def normalize(self):
        minimum = sys.maxint
        newmin = sys.maxint
        maximum = -sys.maxint
        newmax = -sys.maxint

        for g in self.gene_list:
            if min(self.network[g].values()) < minimum:
                minimum = min(self.network[g].values())
            if max(self.network[g].values()) > maximum:
                maximum = max(self.network[g].values())

        for g1 in self.gene_list:
            for g2 in self.gene_list:
                self.network[g1][g2] = (self.network[g1][g2] - float(minimum)) / float((maximum - minimum))


    def apply_threshold(self, thresh):
        thresh_net = self.network.copy()
        for gene1 in self.gene_list:
            for gene2 in self.gene_list:
                if self.network[gene1][gene2] > thresh:
                    thresh_net[gene1][gene2] = 1
                else:
                    thresh_net[gene1][gene2] = 0
        return thresh_net


    def cutoff_network(self, cutoff):
        network_ranks = {}

        # Build cutoff list
        for i in xrange(len(self.original_network)):
            for j in xrange(len(self.original_network)):
                network_ranks[(i,j)] = self.original_network[i][j]

        network_ranked_list = sorted(network_ranks.iteritems(), key=itemgetter(1), reverse=True)
        for i, edge in enumerate(network_ranked_list):
            if i >= cutoff:
                #print i, edge
                self.network[self.gene_list[edge[0][0]]][self.gene_list[edge[0][1]]] = 0
            else:
                self.network[self.gene_list[edge[0][0]]][self.gene_list[edge[0][1]]] = 1

        #print self.network

    def read_networklist(self, netlist, signed=True, binary=False, thresh=0):
        for line in netlist:
            gene1, gene2, val = line

            if gene1 not in self.network.keys():
                self.network[gene1] = {gene1:0}

            if signed:
                val = float(val)
            else:
                val = abs(float(val))

            if self.binary:
                if val > thresh:
                    val = 1
                else:
                    val = 0
            self.network[gene1][gene2] = val

    def read_goldstd(self, filename, signed=False, binary=True, thresh=0):

        f = open(filename,'r')
        for line in f:
            row = line.replace('"','').split()
            #print row

            gene1 = row[0]
            gene2 = row[1]
            val = row[2]

            if gene1 not in self.network.keys():
                self.network[gene1] = {gene1:0}

            try:
                val = float(val)
            except:
                if val == "+":
                    val = 1
                if val == "-":
                    val = -1
            finally:
                self.network[gene1][gene2] = val
        f.close()


    def compare(self,comp_net):
        matches = 0
        misses = 0
        false_edges_predicted = 0
        missed_edges = 0
        correct_edges = 0

        for gene1 in self.network.keys():
            for gene2 in self.network[gene1].keys():
                if gene1 in comp_net.network.keys() and gene2 in comp_net.network[gene1].keys():
                   if (self.network[gene1][gene2] > 0 and comp_net.network[gene1][gene2] > 0) or \
                         (self.network[gene1][gene2] < 0 and comp_net.network[gene1][gene2] < 0) or \
                         (self.network[gene1][gene2] == 0 and comp_net.network[gene1][gene2] == 0):
                       matches += 1
                       if self.network[gene1][gene2] == 1:
                          correct_edges += 1
                   else:
                       misses += 1
                       if self.network[gene1][gene2] == 1:
                          false_edges_predicted += 1
                       else:
                          missed_edges += 1

                   #print gene1, gene2, self.network[gene1][gene2], comp_net.network[gene1][gene2]
                else:
                   print "Error, network 2 is missing a gene from network 1: ", gene1, gene2

        print "Results of network comparison: \nMatches/Misses:", matches, matches + misses
        print "Correct edges/missed edges: ", correct_edges, missed_edges
        print "False edges predicted: ", false_edges_predicted

    def analyzeMotifs(self, goldnet):
        """Analyzes motifs found in the network, such as directionality (if supported),
        Feedforward loops, cascades, fan-ins, and fan-outs.  These motifs are discussed
        in detail in the DREAM5 paper, Marbach, et al, 2011.

        Feedforward loops where A->B, B->C, and A->C.
        Cascades where A->C->B, but not A->B
        Fan-ins where A->B and C->B (i.e., B is regulated by more than one gene)
        Fan-outs where A->B and A->C (i.e., A regulates more than one gene)

        """

        # Keeping count of what we've found
        directionality_correct = 0
        directionality_incorrect = 0
        feedforward_correct = 0
        feedforward_incorrect = 0
        cascade_correct = 0
        cascade_incorrect = 0
        fanin_correct = 0
        fanin_incorrect = 0
        fanout_correct = 0
        fanout_incorrect = 0



        # A rather ugly internal function for detecting FFLs
        # TODO: Add support for directional edges
        def enumerateFeatures(network):
            # For storage of the different motifs
            feedforward_loops = []
            cascades = []
            fanins = []
            fanouts = []
            for gene1 in network.network.keys():
                for gene2 in network.network.keys():
                    if gene1 != gene2 and network.network[gene1][gene2] == 1:
                        for gene3 in network.network.keys():
                            if gene3 == gene1 or gene3 == gene2:
                                continue

                            # Detect FFL
                            if network.network[gene2][gene3] == 1 and \
                                    network.network[gene1][gene3] == 1:
                                        feedforward_loops.append(tuple(sorted([gene1,gene2,gene3])))

                            # Detect cascades
                            if network.network[gene2][gene3] == 1 and \
                                    network.network[gene1][gene3] == 0:
                                        cascades.append(tuple(sorted([gene1,gene2,gene3])))

                            # Detect Fan-Ins
                            if network.network[gene3][gene2] == 1:
                                fanins.append(tuple(sorted([gene1, gene2, gene3])))

                            # Detect Fan-Outs
                            if network.network[gene1][gene3] == 1:
                                fanouts.append(tuple(sorted([gene1, gene2, gene3])))
            return feedforward_loops, cascades, fanins, fanouts

        # Remove any duplicates
        report = Report()
        feedforward_loops, cascades, fanins, fanouts = enumerateFeatures(goldnet)
        report.gold_feedforward_loops = list(set(feedforward_loops))
        report.gold_cascades = list(set(cascades))
        report.gold_fanins = list(set(fanins))
        report.gold_fanouts = list(set(fanouts))

        feedforward_loops, cascades, fanins, fanouts = enumerateFeatures(self)
        report.feedforward_loops = list(set(feedforward_loops))
        report.cascades = list(set(cascades))
        report.fanins = list(set(fanins))
        report.fanouts = list(set(fanouts))

        for t in feedforward_loops:
            if t in report.gold_feedforward_loops:
                report.feedforward_loops_correct += 1
            else:
                report.feedforward_loops_incorrect += 1

        for t in cascades:
            if t in report.gold_cascades:
                report.cascades_correct += 1
            else:
                report.cascades_incorrect += 1

        for t in fanins:
            if t in report.gold_fanins:
                report.fanins_correct += 1
            else:
                report.fanins_incorrect += 1

        for t in fanouts:
            if t in report.gold_fanouts:
                report.fanouts_correct += 1
            else:
                report.fanouts_incorrect += 1


        return report


    def printNetworkToFile(self, filename):
        """ Saves a .sif representation of the network """
        output_file = open(filename, 'w')
        lines = ""
        #print self.network
        for gene in self.gene_list:
            lines += gene + "\tedge"
            for target in self.gene_list:
                #print "Gene: {0}, Target: {1}".format(gene, target)
                if self.network[gene][target] != 0 :
                    lines += "\t" + target
            lines += "\n"
        output_file.write(lines)
        output_file.flush()
        output_file.close()



    def calculateAccuracy(self, goldnet):
        """Calculates the Area Under the Precision-Recall curve, using network1
        as the inferred network, and goldnet as the gold standard (known)
        network.  Links that the inferred network has that do not exist in the
        gold standard are not counted."""

        tp = 0 # True positives
        fp = 0 # False positives
        tn = 0 # True negatives
        fn = 0 # False negatives
        total = 0

        #print "THIS NETWORK:"
        #print self.network
        #print "GOLDNET:"
        #print goldnet.network

        for gene1 in self.network.keys():
            for gene2 in self.network[gene1].keys():
                if gene1 in goldnet.network.keys() and gene2 in goldnet.network[gene1].keys():
                   total += 1
                   if (self.network[gene1][gene2] > 0 and goldnet.network[gene1][gene2] > 0) or (self.network[gene1][gene2] < 0 and goldnet.network[gene1][gene2] < 0) or (self.network[gene1][gene2] == 0 and goldnet.network[gene1][gene2] == 0):
                       if self.network[gene1][gene2] != 0:
                          tp += 1
                       else:
                          tn += 1
                   else:
                       if self.network[gene1][gene2] != 0:
                          fp += 1
                       else:
                          fn += 1

                   #print gene1, gene2, self.network[gene1][gene2], goldnet.network[gene1][gene2]
                else:
                   print "Error, network 2 is missing a gene from network 1: ", gene1, gene2
        if( tp != 0 or fp != 0):
            precision = tp / float((tp + fp))
        else:
            precision = 0
        #print "Precision: ", precision

        if(tp != 0 or fp != 0):
            recall = tp / float((tp + fn))
        else:
            recall = 0
        #print "Recall: ", recall

        if total != 0:
            acc = float(tp + tn) / float(total)
        else:
            acc = 0
        #print "Accuracy: ", acc

        if(tn != 0 or fp != 0):
            specificity = float(tn) / float(tn + fp)
        else:
            specificity = 0
        #print "Specificity: ", specificity

        if(tp != 0 or fn != 0):
            sensitivity = float(tp) / float(tp + fn)
        else:
            sensitivity = 0
        #print "Sensitivity: ", sensitivity

        #print "True Positives: {0}\nTrue Negatives: {1}\nFalse Positives: {2}\nFalse Negatives: {3}\n".format(tp, tn, fp, fn)

        result_hash = { 'tp':tp ,
                        'tn':tn ,
                        'fp':fp ,
                        'fn':fn ,
                        'sensitivity': sensitivity ,
                        'specificity': specificity ,
                        'accuracy': acc,
                        'precision': precision,
                        'recall': recall
                        }
        return result_hash












