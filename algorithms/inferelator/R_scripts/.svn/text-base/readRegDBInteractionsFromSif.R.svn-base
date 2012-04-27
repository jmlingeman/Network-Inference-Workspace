# this file reads regulonDB from a text sif file
# pred activator/repressor/dual gene
# pred and gene are in B####  names
load("data/tfNamesEcoli-445.RData")

##AM # hash of tf to genes
#x=as.matrix(read.table(file="data/tf.genes.regulated.sif"))
#rownames(x)=x[,"V1"]
#regDB_tf_to_gene = list()
#regDB_tf_to_gene_no_autoreg = list()
#for (i in 1:length(tfNames))	{
#	pred = tfNames[i]
#	regDB_tf_to_gene[[pred]] =  character()
#	targets.ix = which(rownames(x) %in% pred)
#	targets =  x[targets.ix,"V3"]
#	names(targets) = NULL
#	regDB_tf_to_gene[[pred]] = targets
#	regDB_tf_to_gene_no_autoreg[[pred]] = targets[which(!targets %in% pred)]
#}
#rm(x,i,pred,targets.ix,targets)

##AM # hash of gene to tfs
x=as.matrix(read.table(file="data/tf.genes.regulated.sif"))
#name rows by target gene
rownames(x)=x[,"V3"]
# init
regDB_tf_to_gene = list()
regDB_tf_to_gene_no_autoreg = list()
genes = unique(rownames(x))
genes = genes[which(genes %in% rownames (ratios))]
for (i in 1:length(genes))	{
	gene = genes[i]
	regDB_tf_to_gene[[gene]] =  character()
	gene.ix = which( rownames(x) %in% gene)
	regulators =  x[gene.ix,"V1"]
	regDBfunction = x[gene.ix,"V2"]
	names(regulators) = regDBfunction
	regDB_tf_to_gene[[gene]] = regulators
	regDB_tf_to_gene_no_autoreg[[gene]] = regulators[which(!regulators %in% gene)]
}

rm(x,genes,i,gene,gene.ix,regulators)