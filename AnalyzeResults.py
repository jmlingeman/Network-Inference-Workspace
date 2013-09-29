from DataStore import *
from Network import *
from itertools import cycle, izip
import numpy as np
import matplotlib
matplotlib.use('Agg') # Must be before importing matplotlib.pyplot or pylab!
import pylab as pl
import operator
import os
from sklearn.utils import shuffle
from sklearn.metrics import roc_curve, auc
from sklearn.metrics import precision_recall_curve
import matplotlib.font_manager as fm
import pickle
import math
import random

def VotingNetwork(finished_jobs,gene_list, top_n=None, prop_weights=False):
  """Takes a list of networks, the list of genes, and the top n genes to use from
  each network, and returns a matrix of votes for each edge. These votes can then
  be used to build a consensus network. """
  if top_n == None:
    top_n = len(gene_list)
  networks = []
  for job in finished_jobs:
    networks.append(job.alg.network)

  voting_matrix = {}
  for gene in gene_list:
    voting_matrix[gene] = {}
    for gene2 in gene_list:
      voting_matrix[gene][gene2] = 0

  for network in networks:
    topnet = network.copy()
    #topnet.normalize_values()
    #print topnet.network
    #topnet.set_top_edges(top_n, False, prop_weights)
    ranked_list = network.get_ranked_list()
    #print topnet.network

    for i,edge in enumerate(ranked_list):
      score = -math.log10(i+1) + math.log10(math.pow(len(gene_list),2))
      if score > 0:
        voting_matrix[edge[0]][edge[1]] += score
    #for gene in gene_list:
      #for gene2 in gene_list:
        #if topnet.network[gene][gene2] != 0:
          #if prop_weights:
            #voting_matrix[gene][gene2] += topnet.network[gene][gene2]
            ##voting_matrix[gene][gene2] +=
          #else:
            #voting_matrix[gene][gene2] += 1

  votenet = networks[0].copy()
  votenet.network = voting_matrix

  return votenet

def SaveVotingNetwork(finished_jobs, votejob, comparejob, goldnet, settings, plot=True, topn=None, final_topn=None, prop_weights=False):
  votejob.alg.network = VotingNetwork(finished_jobs, votejob.alg.gene_list, topn, prop_weights)
  #votejob.alg.network.normalize_values()
  votejob.alg.network.compare_graph_network(goldnet,settings["global"]["output_dir"] + "/" + votejob.alg.name + "-network25", 25)

  for job in finished_jobs:
    if len(job.alg.network.gene_list) <= 20:
        #job.alg.network.compare_graph_network(goldnet,settings["global"]["output_dir"] + "/" + job.alg.name + "-network20", 20)
        job.alg.network.compare_graph_network(goldnet,settings["global"]["output_dir"] + "/" + job.alg.name + "-network25", 25)
        #job.alg.network.compare_graph_network(goldnet,settings["global"]["output_dir"] + "/" + job.alg.name + "-network50", 50)
        #job.alg.network.compare_graph_network(goldnet,settings["global"]["output_dir"] + "/" + job.alg.name + "-network10", 10)
    #elif len(job.alg.network.gene_list) <= 100:
        #job.alg.network.compare_graph_network(goldnet, settings["global"]["output_dir"] + "/" + job.alg.name + "-network", 1)

  if final_topn != None:
    votejob.alg.network.set_top_edges(final_topn)

  if plot:
    #print votejob.alg.network.network
    #print goldnet.network
    tprs, fprs, rocs = GenerateMultiROC([votejob, comparejob], goldnet, False, settings["global"]["output_dir"] + "/VotingROC-" + str(topn) + ".pdf")
    ps, rs, precs = GenerateMultiPR([votejob, comparejob], goldnet, False, settings["global"]["output_dir"] + "/VotingPR-" + str(topn) + ".pdf")
  else:
    tprs, fprs, rocs = GenerateMultiROC([votejob], goldnet, False, "", False)
    ps, rs, precs = GenerateMultiPR([votejob], goldnet, False, "", False)

  acc = votejob.alg.network.calculateAccuracy(goldnet)
  return acc, rocs[0][1], precs[0][1]


def SaveResults(finished_jobs, goldnet, settings, graph_name="Overall", topn=None):
  accs = []
  os.mkdir(settings["global"]["output_dir"] + "/" + graph_name + "-networks/")

  for job in finished_jobs:
    if goldnet == []:
      job.alg.network.compare_graph_network([], settings["global"]["output_dir"] + "/" + job.alg.name + "-network", 1)
      job.alg.network.printNetworkToFile(settings["global"]["output_dir"] + "/" + graph_name + "-networks/" + job.alg.name + ".sif")
      continue

    else:
        jobnet = job.alg.network
        accs.append((job.alg.name, jobnet.calculateAccuracy(goldnet)))

        print job.alg.name
        jobnet.printNetworkToFile(settings["global"]["output_dir"] + "/" + graph_name + "-networks/" + job.alg.name + ".sif")

        if len(job.alg.network.gene_list) <= 20:
          job.alg.network.compare_graph_network(goldnet,settings["global"]["output_dir"] + "/" + job.alg.name + "-network20", 20)
          #job.alg.network.compare_graph_network(goldnet,settings["global"]["output_dir"] + "/" + job.alg.name + "-network25", 25)
          ##job.alg.network.compare_graph_network(goldnet,settings["global"]["output_dir"] + "/" + job.alg.name + "-network50", 50)
          ##job.alg.network.compare_graph_network(goldnet,settings["global"]["output_dir"] + "/" + job.alg.name + "-network10", 10)
        #elif len(job.alg.network.gene_list) <= 100:
          #job.alg.network.compare_graph_network(goldnet, settings["global"]["output_dir"] + "/" + job.alg.name + "-network", 1)


    #accs.append(jobnet.analyzeMotifs(goldnet))
    #print jobnet.analyzeMotifs(goldnet).ToString()


  if goldnet == []:
      return
  tprs, fprs, rocs = GenerateMultiROC(finished_jobs, goldnet, False, settings["global"]["output_dir"] + "/" + graph_name + "ROC.pdf", False)
  ps, rs, precs = GenerateMultiPR(finished_jobs, goldnet, False, settings["global"]["output_dir"] + "/" + graph_name + "PR.pdf", False)

  print "Accuracy:"
  for row in accs:
      print row

  print "ROC Data:"
  for row in rocs:
      print row

  print "PR Data:"
  for row in precs:
      print row

  if topn != None:
    sorted_rocs = sorted(rocs, key=lambda x: x[1], reverse=True)
    sorted_jobs = []
    for r in sorted_rocs[0:topn]:
      for j in finished_jobs:
        if r[0] == j.alg.name:
          sorted_jobs.append(j)
    GenerateMultiROC(sorted_jobs, goldnet, False, settings["global"]["output_dir"] + "/OverallROC-Top" + str(topn) + ".pdf", False)
    GenerateMultiPR(sorted_jobs, goldnet, False, settings["global"]["output_dir"] + "/OverallPR-Top" + str(topn) + ".pdf", False)

  pickle.dump((finished_jobs, accs, rocs, precs), open(settings["global"]["output_dir"] + "./" + settings["global"]["experiment_name"] + ".pickle", 'w'))

  outfile = open(settings["global"]["output_dir"] + "./" + settings["global"]["experiment_name"] + "_" + graph_name + "Results.csv",'w')
  header = "ExpName," + ",".join(accs[0][1].keys()) + ",auroc" + ",aupr" + "\n"
  file = header
  for i, row in enumerate(accs):
      file += row[0] + ','
      for key in row[1].keys():
          file += str(row[1][key]) + ','
      for a in rocs:
        if a[0] == row[0]:
          file += str(a[1]) + ','
      for a in precs:
        if a[0] == row[0]:
          file += str(a[1]) + "\n"

  outfile.write(file)
  outfile.close()

  return accs, precs, rocs


def GenerateMultiROC(finished_jobs, goldnet, show=True, save_path="", plot=True):
    networks = []
    tprs = []
    fprs = []
    areas = []
    fig = pl.figure()
    ax = pl.subplot(111)
    # Shink current axis by 20%
    box = ax.get_position()
    ax.set_position([box.x0, box.y0, box.width * 0.8, box.height])


    cm = pl.get_cmap('Dark2')
    lines = ['-', '--', ':', '-.']


    for job in finished_jobs:
        networks.append((job.alg.name, job.alg.network))
    networks = sorted(networks, key=operator.itemgetter(0))
    for i,net in enumerate(networks):
        color = cm(1.*i/len(finished_jobs))  # color will now be an RGBA tuple
        #color = 'black'
        l = lines[i % 4]
        if i != len(networks)-1:
            tpr, fpr, area  = GenerateROC(net[1], goldnet, fig, ax, plot, False, "", net[0], color, l)
        else:
            tpr, fpr, area = GenerateROC(net[1], goldnet, fig, ax, plot, show, save_path, net[0], color, l)
        tprs.append((net[0], tpr))
        fprs.append((net[0], fpr))
        areas.append((net[0], area))

    return tprs, fprs, areas

def GenerateMultiPR(finished_jobs, goldnet, show=True, save_path="", plot=True, title=None):
    networks = []
    ps = []
    rs = []
    areas = []
    fig = pl.figure()
    ax = pl.subplot(111)
    # Shink current axis by 20%
    box = ax.get_position()
    ax.set_position([box.x0, box.y0, box.width * 0.8, box.height])
    cm = pl.get_cmap('Dark2')
    lines = ['-', '--', ':', '-.']

    for job in finished_jobs:
        networks.append((job.alg.name, job.alg.network))
    networks = sorted(networks, key=operator.itemgetter(0))
    for i,net in enumerate(networks):
        color = cm(1.*i/len(finished_jobs))  # color will now be an RGBA tuple
        color = 'black'
        l = lines[i % 4]
        if i != len(networks)-1:
            p, r, area = GeneratePR(net[1], goldnet, fig, ax, plot, False, "", net[0], color, l, title)
        else:
            p, r, area = GeneratePR(net[1], goldnet, fig, ax, plot, show, save_path, net[0], color, l, title)
        ps.append((net[0], p))
        rs.append((net[0], r))
        areas.append((net[0], area))

    return ps, rs, areas

def GenerateMultiPRList(networks, goldnet, show=True, save_path="", plot=True):
    ps = []
    rs = []
    areas = []
    fig = pl.figure()
    ax = pl.subplot(111)
    # Shink current axis by 20%
    box = ax.get_position()
    #ax.set_position([box.x0, box.y0, box.width * 0.8, box.height])
    cm = pl.get_cmap('Dark2')
    lines = ['-', '--', ':', '-.']

    for i,net in enumerate(networks):
        color = cm(1.*i/len(networks))  # color will now be an RGBA tuple
        color = 'black'
        l = lines[i % 4]
        if i != len(networks)-1:
            p, r, area = GeneratePR(net, goldnet, fig, ax, plot, False, "", "", color, l)
        else:
            p, r, area = GeneratePR(net, goldnet, fig, ax, plot, show, save_path, "", color, l)
        ps.append((i, p))
        rs.append((i, r))
        areas.append((i, area))

    return ps, rs, areas


def GenerateROC(inferred_network, goldnet, fig=None, ax=None, plot=False, show=False, save_path="", name="", line_color=None, line_style=None):
    import yard
    labels = []
    predictions = []
    for gene1 in inferred_network.gene_list:
        for gene2 in inferred_network.gene_list:
            #print gene1, gene2
            labels.append(goldnet.network[gene1][gene2])
            predictions.append(inferred_network.network[gene1][gene2])

    # Compute ROC curve and area the curve
    #print "labels", labels
    #print "predictions", predictions
    fpr, tpr, thresholds = roc_curve(np.array(labels), np.array(predictions))
    roc_auc = auc(fpr, tpr)

    if plot:
        # Plot ROC curve
        #pl.clf()
        #pl.plot(fpr, tpr, label=name)
        ##pl.legend(loc="lower right")
        #fontP = FontProperties()
        #fontP.set_size('small')
        #legend([plot1], "title", prop = fontP)

        #pl.clf()
        #ax = pl.subplot(111)

        #print "Creating ROC Plot"
        line, = ax.plot(fpr, tpr, label=name.replace("_", " "), color=line_color, linestyle=line_style, linewidth=2)
        ax.title.set_y(1.0)
        ax.plot([0, 1], [0, 1], 'k--')
        pl.xlim([0.0, 1.0])
        pl.ylim([0.0, 1.0])
        pl.xlabel('False Positive Rate')
        pl.ylabel('True Positive Rate')
        pl.title("ROC Curve - {0} Gene Network".format(len(inferred_network.gene_list)))

        # Put a legend to the right of the current axis
        prop = fm.FontProperties(size=6)
        ax.legend(loc='top right', prop=prop)

        if show:
          pl.show()
        if save_path != "":
          #pl.tight_layout()
          pl.savefig(save_path)
    return tpr, fpr, roc_auc

def GeneratePR(inferred_network, goldnet, fig=None, ax=None, plot=False, show=False, save_path="", name="", line_color=None, line_style=None, title=None):
    labels = []
    predictions = []
    for gene1 in inferred_network.gene_list:
        for gene2 in inferred_network.gene_list:
            labels.append(goldnet.network[gene1][gene2])
            predictions.append(inferred_network.network[gene1][gene2])


    num_correct = 0
    labels_np = np.array(labels)
    pred_np = np.array(predictions)
    decreasing_probas_indices = np.argsort(pred_np, kind="mergesort")[::-1]
    probas_pred = pred_np[decreasing_probas_indices]
    y_true = labels_np[decreasing_probas_indices]
    #print y_true[0:30]
    #print probas_pred[0:30]
    for i in range(100):
        if y_true[i] == 1:
            num_correct += 1

    inferred_network.num_correct_100 = num_correct
    inferred_network.per_correct_100 = num_correct / float(sum(y_true))

    num_correct = 0
    for i in range( int(round(len(y_true) * 0.10)) ):
        if y_true[i] == 1:
            num_correct += 1

    inferred_network.num_correct_10p = num_correct
    inferred_network.per_correct_10p = num_correct / float(sum(y_true))
    #if shuffle:
    #random.shuffle(labels)
    #print labels
    # Compute ROC curve and area the curve
    # Compute Precision-Recall and plot curve
    precision, recall, thresholds = precision_recall_curve(np.array(labels), np.array(predictions))
    #print precision[::-1]
    #print recall[::-1]
    #print thresholds[::-1]
    area = auc(recall, precision)
    #print "Area Under Curve: %0.2f" % area

    if plot:
        # Plot ROC curve
        #pl.clf()
        #pl.plot(fpr, tpr, label=name)
        ##pl.legend(loc="lower right")
        #fontP = FontProperties()
        #fontP.set_size('small')
        #legend([plot1], "title", prop = fontP)

        #pl.clf()

        line, = ax.plot(recall, precision, label=name.replace("_", " "), color=line_color, linestyle=line_style, linewidth=2)
        ax.title.set_y(1.0)
        pl.ylim([0.0, 1.05])
        pl.xlim([0.0, 1.0])
        pl.xlabel('Recall')
        pl.ylabel('Precision')
        if title == None:
            pl.title("Precision-Recall Curve - {0} Gene Network".format(len(inferred_network.gene_list)))
        else:
            pl.title(title)

        # Put a legend to the right of the current axis
        prop = fm.FontProperties(size=6)
        ax.legend(loc=1, prop=prop)

        pl.tight_layout()

        if show:
          pl.show()
        if save_path != "":
          pl.savefig(save_path)
    return precision, recall, area


def PlotMultipleROC(rocs, title='', labels=None, include_baseline=True):
    import pyroc
    pyroc.plot_multiple_roc(rocs, title, labels, include_baseline)

def get_figure_for_curves(self, curve_class, predictions, expected):
    """Plots curves given by `curve_class` for all the data in `self.data`.
`curve_class` is a subclass of `BinaryClassifierPerformanceCurve`.
`self.data` must be a dict of lists, and the ``__class__`` key of
`self.data` must map to the expected classes of elements. Returns an
instance of `matplotlib.figure.Figure`."""
    fig, axes = None, None

    data = self.data
    expected = data["__class__"]

    keys = sorted(data.keys())
    keys.remove("__class__")

    styles = ["r-", "b-", "g-", "c-", "m-", "y-", "k-", \
              "r--", "b--", "g--", "c--", "m--", "y--", "k--"]

    # Plot the curves
    line_handles, labels, aucs = [], [], []
    for key, style in izip(keys, cycle(styles)):
        self.log.info("Calculating %s for %s..." %
                (curve_class.get_friendly_name(), key))
        observed = data[key]

        bc_data = BinaryClassifierData(zip(observed, expected), title=key)
        curve = curve_class(bc_data)

        if self.options.resampling:
            curve.resample(x/2000. for x in xrange(2001))

        if self.options.show_auc:
            aucs.append(curve.auc())
            labels.append("%s, AUC=%.4f" % (key, aucs[-1]))
        else:
            labels.append(key)

        if not fig:
            dpi = self.options.dpi
            fig = curve.get_empty_figure(dpi=dpi,
                    figsize=parse_size(self.options.size, dpi=dpi))
            axes = fig.get_axes()[0]

        line_handle = curve.plot_on_axes(axes, style=style, legend=False)
        line_handles.append(line_handle)

    if aucs:
        # Sort the labels of the legend in decreasing order of AUC
        indices = sorted(range(len(aucs)), key=aucs.__getitem__,
                         reverse=True)
        line_handles = [line_handles[i] for i in indices]
        labels = [labels[i] for i in indices]
        aucs = [aucs[i] for i in indices]

    if axes:
        legend_pos = "best"

        # Set logarithmic axes if needed
        if "x" in self.options.log_scale:
            axes.set_xscale("log")
            legend_pos = "upper left"
        if "y" in self.options.log_scale:
            axes.set_yscale("log")

        # Plot the legend
        axes.legend(line_handles, labels, loc = legend_pos)

    return fig

def TestDoubleKO(beta_weights, pred_mat, model_bias, wildtype_storage, dko_index, dko_storage):
    import numpy

    if type(beta_weights) == type([]):
      beta_weights = numpy.matrix(beta_weights)

    if type(pred_mat) == type([]):
      pred_mat = numpy.matrix(pred_mat)

    if type(model_bias) == type([]):
      model_bias = numpy.matrix(model_bias)

    model_weights = numpy.concatenate((model_bias, beta_weights), axis=1)
    pred_weights = pred_weights.sum(axis=1)
    wtWeights = 1 - pred_weights

    predictions = []

    # If wt storage is ko experiments, average them. Otherwise just use wts
    if wildtype_storage.type == "knockout":
      wildtype_storage = wildtype_storage.copy().median()

     # Convert to numpy
    init_cond_save = [1]
    for gene in wildtype_storage.gene_list:
      init_cond_save.append(wildtype_storage.experiments[0].ratios[gene])
    init_cond_save = numpy.matrix(init_cond_save)

    predictions = []
    for i, index in enumerate(dko_index):
      init_storage = wildtype_storage.copy()
      init_cond = init_cond_save.copy()

      # Set idx of kos to 0 in wildtype_storage
      ko_gene1 = wildtype_storage.gene_list[index[0]]
      ko_gene2 = wildtype_storage.gene_list[index[1]]

      init_cond[init_storage.gene_list.index(ko_gene1)] = 0
      init_cond[init_storage.gene_list.index(ko_gene2)] = 0

      prediction = numpy.inner(model_weights, init_cond)

      prediction.clip(min=0, max=numpy.max(init_cond))
      prediction = numpy.multiply(prediction, pred_weights) + numpy.multiply(init_cond_save,wtWeights)

      prediction[init_storage.gene_list.index(ko_gene1)] = 0
      prediction[init_storage.gene_list.index(ko_gene2)] = 0

      predictions.append(prediction)

    return predictions



#makePredictions <- function(S,models_bias,beta.mat,single_ko, wild_type, pred.mat.lnet, inCut=75, dblKo, initChoice="koMean"){
	#modelWeights <- cbind(models_bias,beta.mat)
	#maxVec <- apply(single_ko,2,max)
	#predWeights <- apply(pred.mat.lnet,1,sum)
	#wtWeights <- 1 - predWeights

	#dblKoPreds <- matrix(, nrow(dblKo),ncol(single_ko))
	#bestCutOffVal <- quantile(S, inCut/100)

	#for( dblInd in 1:nrow(dblKo)){
		#curKos <- dblKo[dblInd,]
		#curWt <- c()

		#curS <- apply( S[,curKos],1,max )
		#if( initChoice == "origWt"){
			#curWt <- wild_type
		#}else if(initChoice == "koMean"){
			#curWt <- apply( single_ko[,dblKo[dblInd,]], 1, median)
		#}else if(initChoice == "combine"){
			#curWt <- apply( single_ko[,curKos], 1, median)
			#if( any(curS > bestCutOffVal) ){
				#toChange <- which( curS > bestCutOffVal )
				#zOne <- S[toChange,curKos[1]]
				#zTwo <- S[toChange,curKos[2]]
				#curWt[toChange] <- (zOne*single_ko[ toChange,curKos[1] ] + zTwo*single_ko[ toChange,curKos[2] ])/(zOne + zTwo)
			#}
		#}

		#curInitCond <- curWt
		#curInitCond[ curKos ] <- 0
		#curInitCond <- c(1, curInitCond) #here initCond is just a column vector, we add a one to it to account
                                     ##for the bias term

		##we make the predictions
		#curPrediction <- modelWeights%*%curInitCond

		##now we squash between zero and max we see
		#curPrediction[ curPrediction > maxVec ] <- maxVec[ curPrediction > maxVec ]
		#curPrediction[ curPrediction < 0] <- 0

		##now weight our prediction based on explanatory power of each model
		#curPrediction <- curPrediction*predWeights + curWt*wtWeights

		##now filter based on S
		#curIndsToReset <- which( curS < bestCutOffVal )
		#curPrediction[ curIndsToReset ] <- wild_type[ curIndsToReset ]

		##now set the predicted values for the genes we just knocked out to zero
		#curPrediction[ curKos ] <- 0
		#dblKoPreds[ dblInd, ] <- curPrediction
	#}
	#return(dblKoPreds)
