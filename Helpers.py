from Network import *
import numpy

def predict_timepoint(ts_data, network, idx_to_use, idx_to_predict):
    timepoint = ts_data[0].experiments[idx_to_use]
    predict = ts_data[0].experiments[idx_to_predict]

    diffs = []
    for gene in ts_data[0].gene_list:
        diffs.append(predict.ratios[gene] - timepoint.ratios[gene])

    # Each prediction is a linear combination of
    correct = 0
    incorrect = 0

    for i, gene1 in enumerate(network.gene_list):
        predicted_value = 0
        target_value = predict.ratios[gene1]
        for gene2 in network.gene_list:
            predicted_value += timepoint.ratios[gene1] * network.network[gene1][gene2]
        if predicted_value > 0 and diffs[i] > 0:
            correct += 1
        elif predicted_value < 0 and diffs[i] < 0:
            correct += 1
        elif predicted_value == 0 and diffs[i] == 0:
            correct += 1
        else:
            incorrect += 1

    print "PERCENT CORRECT"
    print float(correct) / len(network.gene_list)
    print correct, incorrect, len(network.gene_list)


def generate_random_network(gene_list):
    mat = numpy.random.randn(len(gene_list), len(gene_list))
    mat = mat / numpy.sqrt(len(gene_list))
    net = Network()
    net.read_netmatrix(mat.tolist(), gene_list, True)
    return net

def apply_dex(dex_gene, dex_storage, target_network, op="add"):
    # Normalize the dex storage, take the bottom and top 20%
    #dex_storage.normalize()
    mod_network = target_network.copy()

    #if op == "add":
        #for gene in target_network.gene_list:
            #print dex_storage.experiments[0].ratios[gene]
            #print mod_network.network[gene][dex_gene]
            #print mod_network.network[dex_gene][gene]
            #mod_network.network[gene][dex_gene] += float(dex_storage.experiments[0].ratios[gene])
            #mod_network.network[dex_gene][gene] += float(dex_storage.experiments[0].ratios[gene])
    #if op == "mult":
    print dex_storage.experiments[0].ratios
    for gene in target_network.gene_list:
        if dex_storage.experiments[0].ratios[gene] >= 2.0:
            #mod_network.network[gene][dex_gene] *= float(dex_storage.experiments[0].ratios[gene])
            mod_network.network[gene][dex_gene] = 1
        elif dex_storage.experiments[0].ratios[gene] <= 0.5:
            #mod_network.network[dex_gene][gene] *= -(1.0 / float(dex_storage.experiments[0].ratios[gene]))
            mod_network.network[dex_gene][gene] = -1
        else:
            mod_network.network[dex_gene][gene] = 0.0001

    return mod_network

def read_dko_index(filename):
  file = open(file, 'r')
  ind = []
  for row in file:
    ind.append(map(int, Set(row.strip().split)))
  return ind

def get_example_data_files(name, settings):
  # Read in gold standard network
  goldnet = Network()

  dko_file = None

  if name == "small":
    goldnet.read_goldstd(settings["global"]["small_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["small_network_knockout_file"].split()
    kd_file = settings["global"]["small_network_knockdown_file"].split()
    ts_file = settings["global"]["small_network_timeseries_file"].split()
    wt_file = settings["global"]["small_network_wildtype_file"].split()
    mf_file = settings["global"]["small_network_multifactorial_file"].split()

  elif name == "medium":
    goldnet.read_goldstd(settings["global"]["medium_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["medium_network_knockout_file"].split()
    kd_file = settings["global"]["medium_network_knockdown_file"].split()
    ts_file = settings["global"]["medium_network_timeseries_file"].split()
    wt_file = settings["global"]["medium_network_wildtype_file"].split()
    mf_file = settings["global"]["medium_network_multifactorial_file"].split()

  elif name == "medium_2":
    goldnet.read_goldstd(settings["global"]["medium2_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["medium2_network_knockout_file"].split()
    kd_file = settings["global"]["medium2_network_knockdown_file"].split()
    ts_file = settings["global"]["medium2_network_timeseries_file"].split()
    wt_file = settings["global"]["medium2_network_wildtype_file"].split()
    mf_file = settings["global"]["medium2_network_multifactorial_file"].split()

  elif name == "dream410":
    goldnet.read_goldstd(settings["global"]["dream410_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["dream410_network_knockout_file"].split()
    kd_file = settings["global"]["dream410_network_knockdown_file"].split()
    ts_file = settings["global"]["dream410_network_timeseries_file"].split()
    wt_file = settings["global"]["dream410_network_wildtype_file"].split()
    mf_file = settings["global"]["dream410_network_multifactorial_file"].split()
    dko_file = settings["global"]["dream410_network_doubleknockout_file"].split()
    dko_idx_file = settings["global"]["dream410_network_doubleknockout_index_file"].split()

  elif name == "dream410_2":
    goldnet.read_goldstd(settings["global"]["dream410_2_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["dream410_2_network_knockout_file"].split()
    kd_file = settings["global"]["dream410_2_network_knockdown_file"].split()
    ts_file = settings["global"]["dream410_2_network_timeseries_file"].split()
    wt_file = settings["global"]["dream410_2_network_wildtype_file"].split()
    mf_file = settings["global"]["dream410_2_network_multifactorial_file"].split()

  elif name == "dream410_3":
    goldnet.read_goldstd(settings["global"]["dream410_3_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["dream410_3_network_knockout_file"].split()
    kd_file = settings["global"]["dream410_3_network_knockdown_file"].split()
    ts_file = settings["global"]["dream410_3_network_timeseries_file"].split()
    wt_file = settings["global"]["dream410_3_network_wildtype_file"].split()
    mf_file = settings["global"]["dream410_3_network_multifactorial_file"].split()

  elif name == "dream410_4":
    goldnet.read_goldstd(settings["global"]["dream410_4_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["dream410_4_network_knockout_file"].split()
    kd_file = settings["global"]["dream410_4_network_knockdown_file"].split()
    ts_file = settings["global"]["dream410_4_network_timeseries_file"].split()
    wt_file = settings["global"]["dream410_4_network_wildtype_file"].split()
    mf_file = settings["global"]["dream410_4_network_multifactorial_file"].split()

  elif name == "dream410_5":
    goldnet.read_goldstd(settings["global"]["dream410_5_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["dream410_5_network_knockout_file"].split()
    kd_file = settings["global"]["dream410_5_network_knockdown_file"].split()
    ts_file = settings["global"]["dream410_5_network_timeseries_file"].split()
    wt_file = settings["global"]["dream410_5_network_wildtype_file"].split()
    mf_file = settings["global"]["dream410_5_network_multifactorial_file"].split()

  elif name == "dream4100":
    goldnet.read_goldstd(settings["global"]["dream4100_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["dream4100_network_knockout_file"].split()
    kd_file = settings["global"]["dream4100_network_knockdown_file"].split()
    ts_file = settings["global"]["dream4100_network_timeseries_file"].split()
    wt_file = settings["global"]["dream4100_network_wildtype_file"].split()
    mf_file = settings["global"]["dream4100_network_multifactorial_file"].split()

  elif name == "dream4100_2":
    goldnet.read_goldstd(settings["global"]["dream4100_2_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["dream4100_2_network_knockout_file"].split()
    kd_file = settings["global"]["dream4100_2_network_knockdown_file"].split()
    ts_file = settings["global"]["dream4100_2_network_timeseries_file"].split()
    wt_file = settings["global"]["dream4100_2_network_wildtype_file"].split()
    mf_file = settings["global"]["dream4100_2_network_multifactorial_file"].split()

  elif name == "dream4100_3":
    goldnet.read_goldstd(settings["global"]["dream4100_3_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["dream4100_3_network_knockout_file"].split()
    kd_file = settings["global"]["dream4100_3_network_knockdown_file"].split()
    ts_file = settings["global"]["dream4100_3_network_timeseries_file"].split()
    wt_file = settings["global"]["dream4100_3_network_wildtype_file"].split()
    mf_file = settings["global"]["dream4100_3_network_multifactorial_file"].split()

  elif name == "dream4100_4":
    goldnet.read_goldstd(settings["global"]["dream4100_4_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["dream4100_4_network_knockout_file"].split()
    kd_file = settings["global"]["dream4100_4_network_knockdown_file"].split()
    ts_file = settings["global"]["dream4100_4_network_timeseries_file"].split()
    wt_file = settings["global"]["dream4100_4_network_wildtype_file"].split()
    mf_file = settings["global"]["dream4100_4_network_multifactorial_file"].split()

  elif name == "dream4100_5":
    goldnet.read_goldstd(settings["global"]["dream4100_5_network_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["dream4100_5_network_knockout_file"].split()
    kd_file = settings["global"]["dream4100_5_network_knockdown_file"].split()
    ts_file = settings["global"]["dream4100_5_network_timeseries_file"].split()
    wt_file = settings["global"]["dream4100_5_network_wildtype_file"].split()
    mf_file = settings["global"]["dream4100_5_network_multifactorial_file"].split()

  elif name == "ecoli400":
    goldnet.read_goldstd(settings["global"]["ecoli400_goldnet_file"])
    #Get a list of the knockout files
    ko_file = settings["global"]["ecoli400_knockout_file"].split()
    kd_file = settings["global"]["ecoli400_knockdown_file"].split()
    ts_file = settings["global"]["ecoli400_timeseries_file"].split()
    wt_file = settings["global"]["ecoli400_wildtype_file"].split()
    mf_file = settings["global"]["ecoli400_multifactorial_file"].split()

  else:
    print "ERROR: Dataset {0} not found, exiting.".format(name)
    exit()
  if dko_file != None:
    return ko_file, kd_file, ts_file, wt_file, mf_file, goldnet, dko_file, dko_idx_file
  else:
    return ko_file, kd_file, ts_file, wt_file, mf_file, goldnet

def cross_timeseries_data(ds1, ds2):
    """ Based on Piotr Mirovski's DFG4GRN code """

    return
