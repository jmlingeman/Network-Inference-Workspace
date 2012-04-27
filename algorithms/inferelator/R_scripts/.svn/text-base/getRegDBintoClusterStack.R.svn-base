# here i put regDB interactions into clusterStack[[i]]
# i.e. each cluster has inside it the known regDB tfs that regualte the biclust.
# regulate biclust means at least one gene in biclust is regulated by tf

getRegDBintoClusterStack <- function (cluster, regDBlist, tfNames){
	regDB <- character()
	genes.in.clust = cluster$rows
	for (i in 1:length(genes.in.clust)) {
		gene = genes.in.clust[i]
		if(! is.null(regDBlist[[gene]]) ){
			regDB= c(regDB,regDBlist[[gene]])	
		}
	}
	regDB=unique(regDB)
	return(regDB)
}