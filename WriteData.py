def WriteDream4(data_store, settings, filename):
    # Write the data in the same style as the DREAM4 files
    # Use the type information of data_store to figure out how to write it
    if type(data_store) == type([]): # Timeseries
        return WriteDream4_Timeseries(data_store, settings, filename)
    else:
        return WriteDream4_SteadyState(data_store, settings, filename)

def WriteDream4_Timeseries(data_store, settings, filename):
    file = open(filename, 'w')
    ts0 = data_store[0] # At minimum this should have one
    header = "\"Time\"\t" + "\t".join(ts0.gene_list) + "\r\n\r\n"
    file.write(header)
    for i, ts in enumerate(data_store):
        out = ""
        for experiment in ts.experiments:
            out += str(int(float(experiment.name))) + "\t"
            for gene in ts0.gene_list:
               out += str(experiment.ratios[gene]) + "\t"
            out = out.strip() + "\r\n"
        if i != len(data_store)-1:
            out += "\r\n"
        file.write(out)

def WriteDream4_SteadyState(data_store, settings, filename):
    file = open(filename, 'w')
    header = "\t".join(data_store.gene_list) + "\n"
    file.write(header)
    for e in data_store.experiments:
        out = ""
        for gene in data_store.gene_list:
            out += str(e.ratios[gene]) + "\t"
        out = out.strip() + "\n"
        file.write(out)

