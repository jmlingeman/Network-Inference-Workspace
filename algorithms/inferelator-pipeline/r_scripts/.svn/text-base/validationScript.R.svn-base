##  .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.
## /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ / / \ \ / / \ \
##`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   ' '

## May 2010 Dream3/4 pipeline (MCZ,tlCLR,Inferelator)
## Bonneau lab - "Aviv Madar" <am2654@nyu.edu>, 
##  		     "Alex Greenfield" <ag1868@nyu.edu> 
## NYU - Center for Genomics and Systems Biology

##  .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.
## /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ /|/ \|\ / / \ \ / / \ \
##`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   ' '

# validation script taken from DREAM3
# The function evaluate evaluates the accuracy of a prediction compared to
# a gold standard
# 
# Usage: DreamEvaluationScript(testMat, goldStdrdMat)
#
# evaluate calcuates a precision and ROC values for a given prediction 
# (testMat) compared to a gold standard (goldStdrdMat).
# The input (testMat) holds the information on which goldstandard 
# should be used for comparison. fname is a file name for logging 
# purposes it is used to collect results from different executions of the script 
# in one file. 
# 
# All further information about the actual calculations can be found elsewhere.
#
#
# The algorithm was developed by Gustavo Stolovitzky and implemented by
# Bernd Jagla, Columbia University (baj2107_A_T_columbia.edu). All questions/suggestions should be
# directed to both, Gustavo and Bernd.
#
# Aviv Madar, New York University (am2654@nyu.edu), modified it and converted it from MATLAB to R.

evaluate <- function(testMat, goldStdrdMat, fname = "output/evaluationResults.txt", paste.it = FALSE) {
	#must modify this script when using not DREAM data, for example: in e.coli i,i interactions are allowed
	#because the regulators are a different set of genes than the tfs!
	maxN = 10000000;
	tstRnkdLst = sort(testMat,decreasing=TRUE,index.return=TRUE)[[2]]
# convert vectorized index two double array index
	tstRnkdLst1 = tstRnkdLst %% nrow(goldStdrdMat)
	tstRnkdLst1[which(tstRnkdLst1 == 0)] = nrow(goldStdrdMat)
	tstRnkdLst2 = tstRnkdLst/nrow(goldStdrdMat)
	tstRnkdLst2[which(! tstRnkdLst2%%1==0)] = as.integer(tstRnkdLst2[which(! tstRnkdLst2%%1==0)]+1)
# remove diagonal enteries we are not predicting auto reg
	rm = which(tstRnkdLst1 == tstRnkdLst2)
	tstRnkdLst1 = tstRnkdLst1[-rm]
	tstRnkdLst2 = tstRnkdLst2[-rm]
	
	
	# Analysis
	# initialization
	
	k=0
	Ak=0
	TPk=0
	FPk=0
	P = sum(goldStdrdMat)
	N = length(goldStdrdMat)-nrow(goldStdrdMat) - P # Diagonal is not considered
	T = P+N
	L = length(tstRnkdLst1)                                               
	rec = numeric(length=L)
	prec = numeric(length=L)
	tpr = numeric(length=L)
	fpr = numeric(length=L)
	
	if(length(tstRnkdLst1) > 0) {
		while(k < L) {
			k = k + 1
			if ((k %% 1000) == 0) {	
				#cat(k, "out of", L,"\n") 
			} ##huh? what does this line mean??
			# if k pred is correct
			if(goldStdrdMat[tstRnkdLst1[k],tstRnkdLst2[k]] == 1) {
				TPk = TPk + 1
				if(k==1)	
					{ delta=1/P }  
				else		
					{ delta=(1-FPk*log(k/(k-1)))/P }
				Ak = Ak + delta
			} else if (goldStdrdMat[tstRnkdLst1[k],tstRnkdLst2[k]] == 0) {
				FPk = FPk + 1
			} else {
				cat("could not find ", tstRnkdLst1[k],tstRnkdLst2[k]);
			}
			rec[k] = TPk/P
			prec[k] = TPk/k
			tpr[k] = rec[k]
			fpr[k] = FPk/N
		}
	}

	TPL=TPk
	if (L < T) 	{ 
		rh = (P-TPL)/(T-L) 
	} else { 
		rh = 0 
	}
	
	if (L>0) {
		recL = rec[L]
	} else {
		recL = 0
	}
	while (TPk < P) {
		k = k + 1
		TPk = TPk + 1
		rec[k] = TPk/P
		if ( ((rec[k]-recL)*P + L * rh) != 0 ) {
			prec[k] = rh * P * rec[k]/((rec[k]-recL)*P + L * rh)
		} else {
			prec[k] = 0
		}
		tpr[k] = rec[k]
		FPk = TPk * (1-prec[k])/prec[k]
		fpr[k] = FPk/N
	}
	AL = Ak;
	if (!is.nan(rh) & rh != 0  & L != 0) {
		AUC = AL + rh * (1-recL) + rh * (recL - L * rh / P) * log((L * rh + P * (1-recL) )/(L *rh))
	} else if (L==0) {
		AUC = P/T
	} else {
		AUC = Ak
	}
	
	# Integrate area under ROC
	lc = fpr[1] * tpr[1] /2
	for (n in 1:(L+P-TPL-1)) {
		lc = lc + (fpr[n+1]+fpr[n]) * (tpr[n+1]-tpr[n]) / 2
	}
	
	AUROC = 1 - lc

	# specific precision values
	TrueP = c(1, 2, 5, 20, 100, 500) 
	prec0 = numeric(length=length(TrueP))
	names(prec0) = as.character(TrueP)
	rec0 = numeric(length=length(TrueP))
	for (i in 1:length(TrueP)) {
		if(TrueP[i]<=P) {
			rec0[i]=TrueP[i]/P
			j=which(rec == rec0[i])
			j=min(j) #In case there is more than 1 precision values for rec(i)
			prec0[i]=prec[j]
		}
	}

	# handle output
	object = list()
	object[["prec"]] = prec
	object[["rec"]] = rec
	object[["tpr"]] = tpr
	object[["fpr"]] = fpr
	object[["AUROC"]] = AUROC
	object[["AUPR"]] = AUC
	object[["specificPrecVals"]] = prec0
	return(object)

}

#function to plot area under precision recall (AUPR
#and area under receiver operator (AUROC) curves
plotAUPRCurves <- function(resList, saveToDir){
	clrs <- c("red",colors()[654],colors()[76],colors()[96],colors()[107])
	#plotting AUPR
	pdf(paste(saveToDir,"/","AUPR.pdf",sep=""))
	plot( resList[[1]]$rec, resList[[1]]$prec, xlab="precision", ylim=c(0,1), ylab="recall",lwd=1.5,type="l",col=clrs[1])
	for(i in 2:length(resList)){
		lines( resList[[i]]$rec, resList[[i]]$prec,lwd=1.5,type="l",col=clrs[i])
	}
	title("AUPR for All Tested Methods")
	auprLabels <- vector(mode="character",length=length(resList))
	for(i in 1:length(resList)){
		auprLabels[i] <- paste("AUPR = ",round(resList[[i]]$AUPR,3),sep="")
	}
	legend("topright","topright",paste(names(resList),", ",auprLabels,sep=""),clrs[1:length(resList)])
	dev.off()
	
	pdf(paste(saveToDir,"/","AUROC.pdf",sep=""))
	plot( resList[[1]]$fpr, resList[[1]]$tpr, xlab="false positive rate", ylim=c(0,1), ylab="true positive rate",lwd=1.5,type="l",col=clrs[1])
	for(i in 2:length(resList)){
		lines( resList[[i]]$fpr, resList[[i]]$tpr,lwd=1.5,type="l",col=clrs[i])
	}
	title("AUROC for All Tested Methods")
	aurocLabels <- vector(mode="character",length=length(resList))
	for(i in 1:length(resList)){
		aurocLabels[i] <- paste("AUROC = ",round(resList[[i]]$AUROC,3),sep="")
	}
	legend("bottomright","bottomright",paste(names(resList),", ",aurocLabels,sep=""),clrs[1:length(resList)])
	dev.off()
}









