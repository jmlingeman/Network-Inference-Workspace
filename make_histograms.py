import sys
import pylab as pl
import os

folder = sys.argv[1]

results_files = []

for (path, dirs, files) in os.walk(folder):
    for f in files:
        if "OverallResults.csv" in path + f:
            results_files.append(path +"/"+ f)

print results_files

for rfile in results_files:
    dataset = rfile.split("/")[-2]
    directory = "/".join(rfile.split("/")[:-1])

    print dataset
    print directory

    f = open(rfile, 'r')

    high_noise = []
    low_noise = []
    med_noise = []

    high_rep = []
    low_rep = []
    med_rep = []

    lines = f.readlines()
    for line in lines[1:]:
        line = line.split(',')
        aupr = float(line[-1])
        name = line[0]
        display_name = ""

        if "Genie3" in name:
            display_name = "GENIE3 "
        elif "Inferelator" in name:
            display_name = "Inf+GENIE3 "

        if "12" in name:
            display_name += "12 rep / 5 tp"
            high_rep.append(token)
        elif "4" in name:
            display_name += "4 rep / 15 tp"
            med_rep.append(token)
        else:
            display_name += "6 rep / 10 tp"
            low_rep.append(token)

        token = [display_name, aupr]

        if "high" in name:
            high_noise.append(token)
        elif "low" in name:
            low_noise.append(token)
        else:
            med_noise.append(token)


    low_noise = sorted(low_noise, key=lambda x: (float(x[0].split()[1]), x[0].split()[0]))
    med_noise = sorted(med_noise, key=lambda x: (float(x[0].split()[1]), x[0].split()[0]))
    high_noise = sorted(high_noise, key=lambda x: (float(x[0].split()[1]), x[0].split()[0]))

    for ds in [('Low Noise', low_noise), ('Medium Noise', med_noise), ('High Noise', high_noise)]:
        pos = []
        auprs = []
        names = []

        for i in range(len(ds[1])):
            pos.append(i)
            auprs.append(ds[1][i][1])
            names.append(ds[1][i][0])

        tick_pos = [x + 0.5 for x in pos]
        print pos, auprs, names

        fig = pl.figure(figsize=(9,6))
        ax = pl.gca()
        pl.title(dataset + "\n" + ds[0], fontsize=18)
        pl.bar(pos, auprs, facecolor="#000000")
        pl.ylabel("Area Under Precision-Recall Curve", fontsize=18, labelpad=10)
        pl.yticks(fontsize=12)
        pl.xticks(tick_pos, names, fontsize=11)
        fig.autofmt_xdate()

        ax.spines["right"].set_visible(False)
        ax.spines["top"].set_visible(False)
        ax.xaxis.set_ticks_position('bottom')
        ax.yaxis.set_ticks_position('left')
        pl.ylim([0, 0.41])
        pl.savefig(directory + "/" + dataset +"-"+ ds[0] + ".png")
        #pl.show()
