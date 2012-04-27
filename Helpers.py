from Network import *

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

