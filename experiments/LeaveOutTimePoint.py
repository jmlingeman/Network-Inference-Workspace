import os, sys
from datetime import datetime
from TimeMask import *
from DataStore import *

def get_immediate_subdirectories(dir):
    return [name for name in os.listdir(dir) if os.path.isdir(os.path.join(dir, name))]

N = 2

sys.path += get_immediate_subdirectories("./")

from ReadConfig import *
settings = {}
settings = ReadConfig(settings)
settings["global"]["working_dir"] = os.getcwd() + '/'

# Create date string to appent to output_dir
t = datetime.now().strftime("%Y-%m-%d_%H.%M.%S")
settings["global"]["output_dir"] = settings["global"]["output_dir"] + "/" + t + "/"
os.mkdir(settings["global"]["output_dir"])

ts_filenames = settings["global"]["time_series_files"].split()
#delta_t = map(int,settings["global"]["time_series_delta_t"].split())
delta_t = [60]*50

# Ok, so here we want to read in a while, break take every N time step, and
# then create our first batch of test sets
# Create a time mask for our first test
time_mask = [0] * (len(delta_t) + 1)
mask_delta_t = [0] * (len(delta_t) / N)

# Create our initial smaller time mask
last_i = 0
removed_time = 0
mask_count = 0
for i in xrange(len(time_mask)):
    if i % N == 0:
        time_mask[i] = 1
        last_i = i
        if i != 0:
            print last_i, len(delta_t), removed_time, mask_count, mask_delta_t
            mask_delta_t[mask_count-1] = removed_time + delta_t[last_i - 1]
        mask_count += 1
        removed_time = 0
    else:
        removed_time += delta_t[i - 1]

print time_mask
print mask_delta_t
settings["global"]["time_series_delta_t"] = mask_delta_t

print delta_t
timeseries_storage = []
for f in ts_filenames:
    m = MicroarrayData()
    m.read_input(f, True)
    timeseries_storage.append(m)

# Now what we want to do is use only every 3rd time point.  We'll then start
# removing those one at a time to asertain which one harms out output the most,
# and add in one of the points we removed there, and then repeat the process.

trans_factors = TFList(settings["global"]["tflist"])

print ts_filenames
print delta_t

#ss_filenames = settings["global"]["steady_state_files"].split()
#steadystate_storage = []
#for f in ss_filenames:
#    m = MicroarrayData()
#    m.read_input(f)
#    steadystate_storage.append(m)

#from cmonkey import *
#cm = Cmonkey("550ath.ratios.fix", settings)
#cm.run(settings)

score_record = []

from dfg4grn import *
dfg = DFG4GRN(timeseries_storage, trans_factors, settings, time_mask)
dfg.run(settings)
score = dfg.gather_output(settings)
print score

score_record.append(score)

def getBestScore(time_mask, delta_t):

    scores = []
    for i, bit in enumerate(time_mask):
        if bit == 0:
            new_time_mask = time_mask[:]
            new_time_mask[i] = 1
            print new_time_mask
            ndelta_t = MakeDeltaT(new_time_mask, delta_t)
            settings["global"]["time_series_delta_t"] = ndelta_t
            dfg = DFG4GRN(timeseries_storage, trans_factors, settings, new_time_mask)
            dfg.run(settings)
            score = dfg.gather_output(settings)
            print score
            scores.append((i,score))

    scores = sorted(scores, key = lambda a: -a[1])
    return scores


while time_mask.count(0) > 0:
    print time_mask.count(0)
# Adjust time mask with best score, then rerun
    scores = getBestScore(time_mask, delta_t)
    time_mask[scores[0][0]] = 1
    score_record.append(scores)
    print time_mask, scores

print "RESULTS:"
print scores



