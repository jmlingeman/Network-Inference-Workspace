import sys
import pylab as pl
import os

rfile = sys.argv[1]

results_files = []

#for (path, dirs, files) in os.walk(folder):
    #for f in files:
        #if "OverallResults.csv" in path + f:
            #results_files.append(path +"/"+ f)

print results_files

random_20 = [0.078, 0.015]
random_100 = [0.017, 0.0013]



titles = ["Timeseries with Knockout and Steady State Data",
          "Timeseries with Steady State Data",
          "Timeseries with Knockout Data",
          "Timeseries Data Only"]


ko_sims = []
ss_ko_sims = []
ts_sims = []
ss_sims = []


#for rfile in results_files:
if "100" in rfile:
    random_token = random_100
else:
    random_token = random_20
dataset = rfile.split("/")[-2]
directory = "/".join(rfile.split("/")[:-1])
f = open(rfile, 'r')
lines = f.readlines()
for line in lines[1:]:
    name = line.split(',')[0]
    if "KO_SS" in name:
        ss_ko_sims.append(line)
    elif "SS" in name:
        ss_sims.append(line)
    elif "KO" in name:
        ko_sims.append(line)
    elif "Only" in name:
        ts_sims.append(line)

print "Read in data", len(ss_ko_sims), len(ss_sims), len(ko_sims), len(ts_sims)



for i, data in enumerate([ss_ko_sims, ss_sims, ko_sims, ts_sims]):
    dataset = titles[i]
    high_noise = []
    low_noise = []
    med_noise = []

    high_rep = []
    low_rep = []
    med_rep = []
    for line in data:




        line = line.split(',')
        aupr = float(line[-1])
        name = line[0]
        display_name = ""

        if "Genie3" in name:
            display_name = "GENIE3 "
        elif "Inferelator" in name:
            continue
            #display_name = "Inf+GENIE3 "

        #print line




        if "12rep" in name:
            display_name += "12 rep / 5 tp"
        elif "4rep" in name or "3rep" in name:
            display_name += "4 rep / 15 tp"
        else:
            display_name += "6 rep / 10 tp"

        if "high" in name:
            display_name += " High Noise"
        elif "low" in name:
            display_name += " Low Noise"
        else:
            display_name += " Medium Noise"

        token = [display_name, aupr]

        print name
        if "12rep" in name:
            high_rep.append(token)
        elif "4rep" in name or "3rep" in name:
            low_rep.append(token)
        elif "6rep" in name:
            med_rep.append(token)

        if "high" in name:
            high_noise.append(token)
        elif "low" in name:
            low_noise.append(token)
        else:
            med_noise.append(token)

    auprs_hash = {}
    for d in [high_rep, med_rep, low_rep]:
        for token in d:
            if token[0] in auprs_hash:
                auprs_hash[token[0]].append(token[1])
            else:
                auprs_hash[token[0]] = [token[1]]

    for key in auprs_hash.keys():
        auprs_hash[key] = [pl.mean(auprs_hash[key]), pl.std(auprs_hash[key])]

    auprs_hash["Random Edges"] = random_token


    low_noise = sorted(low_noise, key=lambda x: (float(x[0].split()[1]), x[0].split()[0]))
    med_noise = sorted(med_noise, key=lambda x: (float(x[0].split()[1]), x[0].split()[0]))
    high_noise = sorted(high_noise, key=lambda x: (float(x[0].split()[1]), x[0].split()[0]))

    low_rep = sorted(low_rep, key=lambda x: (float(x[0].split()[1]), x[0].split()[0]))
    med_rep = sorted(med_rep, key=lambda x: (float(x[0].split()[1]), x[0].split()[0]))
    high_rep = sorted(high_rep, key=lambda x: (float(x[0].split()[1]), x[0].split()[0]))

    print len(low_rep), len(med_rep), len(high_rep)

    fig = pl.figure(figsize=(9,9))
    idx = 0
    ax = pl.gca()


    pl.title(dataset, fontsize=18)
    tick_pos = []
    names = []
    #idxs = [0, 1, 1.5, 2, 3, 3.5, 4, 5, 5.5, 6]
    idxs = [0, 0.5, 0.75, 1, 1.5, 1.75, 2, 2.5, 2.75, 3]
    names =["Random Edges", "GENIE3 4 rep / 15 tp Low Noise", "GENIE3 4 rep / 15 tp Medium Noise", "GENIE3 4 rep / 15 tp High Noise", "GENIE3 6 rep / 10 tp Low Noise", "GENIE3 6 rep / 10 tp Medium Noise", "GENIE3 6 rep / 10 tp High Noise", "GENIE3 12 rep / 5 tp Low Noise", "GENIE3 12 rep / 5 tp Medium Noise", "GENIE3 12 rep / 5 tp High Noise"]

    #print low_rep, med_rep
    data = [["Random Edges", random_100]] + low_rep + med_rep + high_rep

    #print data


    for name in names:
        print name

        pos = []
        auprs = []
        stds = []

        print idx, name, d
        pos.append(idxs[idx])
        auprs.append(auprs_hash[name][0])
        stds.append(auprs_hash[name][1])
        #names.append(name)
        idx += 1

        tick_pos += [x + 0.125 for x in pos]
        #print pos, auprs, names

        print auprs_hash
        print "AUPRS", auprs
        pl.bar(pos, auprs, 0.23, facecolor="#FFFFFF")
        pl.errorbar(tick_pos[-1], auprs, yerr=stds, fmt="--o", color='k', markerfacecolor='k',ecolor='black')
    pl.ylabel("Area Under Precision-Recall Curve", fontsize=18, labelpad=10)
    pl.yticks(fontsize=12)
    pl.xticks(tick_pos, names, fontsize=11)
    fig.autofmt_xdate()

    ax.spines["right"].set_visible(False)
    ax.spines["top"].set_visible(False)
    ax.xaxis.set_ticks_position('bottom')
    ax.yaxis.set_ticks_position('left')
    pl.ylim([0, 0.41])
    pl.xlim([0,3.25])
    pl.tight_layout()


    pl.savefig(dataset + "-Histogram" + ".pdf")

    pl.show()
