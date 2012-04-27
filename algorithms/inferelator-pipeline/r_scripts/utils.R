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

#helper function to extract the median network from the ensemble
getMedianNetworkFromBootstraps <- function(allRes, pipeline = "MCZ.MixCLR.Inf"){
	# 1) get all MCZ.MixCLR.Inf confidence scores matrices
	x <- lapply(allRes, function(i) i[[pipeline]])
	# 2) collapse x list into a 3 dim array
	all.conf.scores <- array(0,c(dim(x[[1]]),length(x)))
	for (i in 1:length(x)){
		all.conf.scores[,,i] <- x[[i]]
	}
	# 3) get median (and stndrd dev) matrix (median confidence score for each putative regulatory interaction)
	median.conf.scores <- matrix(0,nrow=dim(all.conf.scores)[1],ncol=dim(all.conf.scores)[2])
	for (i in 1: dim(all.conf.scores)[1]){
		for(j in 1:dim(all.conf.scores)[2]) {
			median.conf.scores[i,j] <- median(all.conf.scores[i,j,])
		}
	}	
	return(median.conf.scores)
}

#helper function to extract median bias from the ensembles
getMedianBias <- function(allRes){
	x <- lapply(allRes, function(i) i[["bias"]])
	allBias <- matrix(0,nrow(x[[1]]),length(x))
	for(i in 1:length(x)){
		allBias[,i] <- x[[i]][,3]
	}
	rownames(allBias) <- paste("G",x[[1]][,1],sep="")
	medBias <- apply(allBias,1,median)
	return(medBias)
}

#helper function to extract the mean network from the ensemble
getMeanNetworkFromBootstraps <- function(allRes, pipeline = "MCZ.MixCLR.Inf"){
	# 1) get all MCZ.MixCLR.Inf confidence scores matrices
	x <- lapply(allRes, function(i) i[[pipeline]])
	# 2) collapse x list into a 3 dim array
	all.conf.scores <- array(0,c(dim(x[[1]]),length(x)))
	for (i in 1:length(x)){
		all.conf.scores[,,i] <- x[[i]]
	}
	# 3) get median (and stndrd dev) matrix (median confidence score for each putative regulatory interaction)
	mean.conf.scores <- matrix(0,nrow=dim(all.conf.scores)[1],ncol=dim(all.conf.scores)[2])
	for (i in 1: dim(all.conf.scores)[1]){
		for(j in 1:dim(all.conf.scores)[2]) {
			mean.conf.scores[i,j] <- mean(all.conf.scores[i,j,])
		}
	}	
	return(mean.conf.scores)
}

#function to make double knockout predictions
#--INPUT--#
#1) S              - matrix of confidence scores for each regulatory interaction 
#                  (can be the scores derived from any pipeline, though we typicall use MCZ-dervied scores)
#2) models_bias    - a vector containing the bias for each model 
#3) beta.mat       - matrix of dynamical parameters for each regulatory interaction
#4) single_ko      - matrix of single knockouts
#5) wild_type      - what we use for wt values, could be the given wt values, or the ones we calculate (ie. median over all conditions)
#6) pred.mat.lnet  - matrix similar to beta.mat but instead of dynamical parameters contains the contribution of 
#                    each parameter to the explanatory power of the model
#7) inCut          - the percentile value that determines which percent of predicted interactions we replace with wild-type  
#8) dblKo          - the list of double knockout indices for ehich predictions are to be made
#9) initChoice     - determines what we use as our null prediction...choices:
#                   "origWt" - use wt values(whichever way we calculate them) as null predictions
#                   "koMean" - take columns i,j of single_ko (where i and j are the genes being knocked out), and use the mean of them as null prediction  
#                   "combine" - combine columns i,j of single_ko as a weighted sum based on sores of i,j in S
#--OUTPUT--#
#one list containing
#1) dblKoPreds     - a 20 by N matrix (where N is the number of genes) of predictions of the response of the system to double knockuots
makePredictions <- function(S,models_bias,beta.mat,single_ko, wild_type, pred.mat.lnet, inCut=75, dblKo, initChoice="koMean"){
	modelWeights <- cbind(models_bias,beta.mat)
	maxVec <- apply(single_ko,2,max)
	predWeights <- apply(pred.mat.lnet,1,sum)
	wtWeights <- 1 - predWeights
	
	dblKoPreds <- matrix(, nrow(dblKo),ncol(single_ko))
	bestCutOffVal <- quantile(S, inCut/100)
	
	for( dblInd in 1:nrow(dblKo)){
		curKos <- dblKo[dblInd,]
		curWt <- c()
		
		curS <- apply( S[,curKos],1,max )
		if( initChoice == "origWt"){
			curWt <- wild_type
		}else if(initChoice == "koMean"){
			curWt <- apply( single_ko[,dblKo[dblInd,]], 1, median)
		}else if(initChoice == "combine"){
			curWt <- apply( single_ko[,curKos], 1, median)
			if( any(curS > bestCutOffVal) ){
				toChange <- which( curS > bestCutOffVal )
				zOne <- S[toChange,curKos[1]]
				zTwo <- S[toChange,curKos[2]]
				curWt[toChange] <- (zOne*single_ko[ toChange,curKos[1] ] + zTwo*single_ko[ toChange,curKos[2] ])/(zOne + zTwo)
			}	
		}
		
		curInitCond <- curWt 
		curInitCond[ curKos ] <- 0 
		curInitCond <- c(1, curInitCond) #here initCond is just a column vector, we add a one to it to account
                                     #for the bias term
	
		#we make the predictions
		curPrediction <- modelWeights%*%curInitCond
		
		#now we squash between zero and max we see
		curPrediction[ curPrediction > maxVec ] <- maxVec[ curPrediction > maxVec ]
		curPrediction[ curPrediction < 0] <- 0
		
		#now weight our prediction based on explanatory power of each model
		curPrediction <- curPrediction*predWeights + curWt*wtWeights
		
		#now filter based on S
		curIndsToReset <- which( curS < bestCutOffVal )
		curPrediction[ curIndsToReset ] <- wild_type[ curIndsToReset ]
		
		#now set the predicted values for the genes we just knocked out to zero
		curPrediction[ curKos ] <- 0
		dblKoPreds[ dblInd, ] <- curPrediction
	}
	return(dblKoPreds)
}

#function to test how good our double knockout predictions are
#--INPUT--#
#1) predictions  - matrix with the rows being the particular doublkockounts, and columns correspond to values of the other genes
#2) goldStandard - the true values of network states for all genes given particular knockout - rows correspond to rows of predictions matrix
#3) ko_inds      - matrix of indices of which genes are doubly knockedout...columns indiciate which gene is knocked out
#4) single_ko    - matrix of single knockouts
#5) wild_type    - what we use for wt values, could be the given wt values, or the ones we calculate (ie. median over all conditions)
#6) whichNull    - determines what we use as our null prediction...choices:
#                   "origWt" - use wt values(whichever way we calculate them) as null predictions
#                   "meanKo" - take columns i,j of dream_ko (where i and j are the genes being knocked out), and use the mean of them as null prediction  
#--OUTPUT--#
#one list containing
#1) rmsdPred - error(in terms of RMSD) of our prediction to the true answer
#2) rmsdNull - error(in terms of RMSD) of the null prediction(in whichever way we calculated it) to the true answer
calcDblKoRmsd <- function( predictions, goldStandard, ko_inds, single_ko, wild_type, whichNull = "meanKo"){
	rmsdPred <- matrix(0,nrow(predictions),1)
	rmsdNull <- matrix(0, nrow(predictions),1)
	for(i in 1:nrow(predictions)){
		curNull <- c()
		#curWt
		if(whichNull == "origWt"){
			curNull <- wild_type
		}else if(whichNull == "meanKo"){
			curNull <- apply( single_ko[,ko_inds[i,]], 1, mean)#wt_meas
		}
		curNull[ ko_inds[i,] ] <- 0
		rmsdPred[i,1] <- sum((predictions[i,] - goldStandard[i,])^2)/ncol(predictions) 
		rmsdNull[i,1] <- sum((curNull - goldStandard[i,])^2)/ncol(predictions) 
	}
	names(rmsdPred) <- paste(ko_inds[,1],"_",ko_inds[,2],sep="")
	names(rmsdNull) <- paste(ko_inds[,1],"_",ko_inds[,2],sep="")
	return( list(as.vector(rmsdPred), as.vector(rmsdNull)) )
}

plotDblKoPreds <- function( allResults, medEnsPred, dblKoInd, saveToDir ){
	pdf(file=paste(saveToDir,"/doubleKnockoutPrediction.pdf",sep=""))
	
	predEnsemble <- matrix(0,length(allResults), length(medEnsPred[[1]]))
	dblKoNames <- paste(dblKoInd[,1],",",dblKoInd[,2],sep="")
	for(i in 1:length(allResults)){
		predEnsemble[i,] <- allResults[[i]]$predRes[[1]]
	}
	maxVal <- max(medEnsPred[[2]],predEnsemble)
	boxplot(predEnsemble,ylim=c(-.002,round(maxVal,4)),axes=F,col=rgb(0,0,0,.8),xlab="knocked out indices",ylab="mean squared error",outline=FALSE)
	axis(2,at=seq(0,round(maxVal,4),by=round(maxVal/10,3)))
	axis(1,at=seq(1,20,by=1),labels=F,line=-2.5)
	text(seq(1,20,by=1),rep(-.002,20),dblKoNames,cex=.7,srt=45)	
	points(medEnsPred[[1]],col=colors()[374],pch=15)
	points(medEnsPred[[2]],col=colors()[62],pch=16)
	title("Predicting Double-Knockouts (MSE for each knockout")
	legend("topright","topright",c("MCZ + tlCLR-Inf ensemble predictions", "predictions via the ensemble median","baseline predictions" ),c("black",colors()[374],colors()[62]))
	dev.off()
}

# description:
calcFoldChange <- function( perturbedData, wildType, epsZero ){
	epsZero <- epsZero #.0050
	foldChange <- matrix( 0, nrow(perturbedData), nrow(perturbedData))
	rownames( foldChange ) <- rownames( perturbedData )
	colnames( foldChange ) <- colnames( perturbedData )
	for( i in 1:nrow(perturbedData)){
	  foldChange[,i] <- t((perturbedData[,i] - wildType)/(wildType + epsZero))	
		#rownames(foldChange) <- rownames( perturbedData )
	}
	return( foldChange )
}
# description:
#otherData is the other dataset which is to be used as an estimate of sd
calcZscores <- function( inData, otherData, wtData, sigmaZero, numRem, calcSDOverAll = FALSE ){ #sigma-zero is a correction term
 	#eventually want a psuedocount
	sigmaZero <- sigmaZero#.00375 #can also try .005 or .0025 #is most useful for dream3, not so much for dream4
	
	zScores <- matrix(,nrow(inData), ncol(inData))
	rownames( zScores ) <- rownames( inData )
	colnames( zScores ) <- colnames( inData )
	
	allSds <- c()
	
	numRem <- numRem#c() #number of outliers to remove in each direction when calculating sd
#	if( nrow(inData) == 10){
#		numRem <- 0 #prev value was 1
#	}else{
#		numRem <- 0 #prev val was 5
#	}
	
	for( i in 1:nrow(inData)){
		highest <- sort(inData[i,], decreasing=T, index.return=T)$ix
		highest <- highest[ -which( highest == i ) ]
		lowest <- sort(inData[i,], decreasing=F, index.return=T)$ix
		lowest <- lowest[ -which( lowest == i ) ]
		self <- i
		toRemove <- unique( c(lowest[1:numRem], highest[1:numRem], self))
		
		if(calcSDOverAll){
			otherHighest <- sort(otherData[i,], decreasing=T, index.return=T)$ix
			otherHighest <- otherHighest[ -which( otherHighest == i ) ]
			otherLowest <- sort(otherData[i,], decreasing=F, index.return=T)$ix
			otherLowest <- otherLowest[ -which( otherLowest == i ) ]
			otherSelf <- i
			otherToRemove <- unique( c(otherLowest[1:numRem], otherHighest[1:numRem], otherSelf))
		}
		
		if(calcSDOverAll){
			allSds[i] <- sd( c(inData[i,-toRemove], otherData[i,-otherToRemove]) )
		}else{
			allSds[i] <- sd( inData[i,-toRemove] )
		}
	}
	
	for(i in 1:ncol(inData)){
		#cat("using sigma zero! \n")
		zScores[,i] <- (inData[,i] - wtData)/(allSds + sigmaZero)
	}
	rtrn.obj = list()
	rtrn.obj[[1]] = zScores
	rtrn.obj[[2]] = allSds
	return( rtrn.obj )
}

splitDreamDataByType <- function(inData){
	ratios <- c()
	if(is.null(inData)){
		source("r_scripts/init_util.R")
		dataSet <- let_usr_choose_dataset()
		allData <- get_usr_chosen_dataset(dataSet)
		ratios <- allData[[1]]	
		gs <- allData[[5]]
		dreamPred <- allData[[6]]
	}else{
		ratios <- inData
	}

	#return( list(dream4_ts,dream4_ko,dream4_kd,dream4_wt) )
	wtCols <- grep( "wt", colnames(ratios) )
	dream3_wt <-  ratios[,wtCols[1]]
	
	tsCols <- grep( "TS", colnames(ratios)) 
	dream3_ts <- ratios[ ,tsCols]
	
	koCols <- grep( "-/-", colnames(ratios) )
	dream3_ko <- ratios[,koCols]
	
	ssCols <- grep("SS",colnames(ratios))
	
	#nonKd <- c(wtCols,koCols,tsCols)
	if(any(ssCols)){
		kdCols <- c(1:ncol(ratios))[ -c(wtCols,koCols,tsCols,ssCols) ]
	}else{
		kdCols <- c(1:ncol(ratios))[ -c(wtCols,koCols,tsCols) ]
	}
	dream3_kd <- ratios[ ,kdCols]
	
	return ( list( dream3_ts, dream3_ko, dream3_kd, dream3_wt))	
}


# nicely print all parameters of a inf run
print_params <- function(PARAMS,file="") {
	cat("",file=file)
	for (i in 1:length(PARAMS)) {
		if(length(PARAMS[[i]]) > 0){
			cat(names(PARAMS[i]), ":\n",file=file,append = TRUE)
			for (j in 1:length(PARAMS[[i]]))
				cat("\t",names(PARAMS[[i]][j])," -> ", PARAMS[[i]][[j]],"\n",file=file,append = TRUE)
		} else {
			cat(names(PARAMS[i]),": empty list","\n",file=file,append = TRUE)
		}
	}
}

# Add a confidance measure for each non-zero wighted predictor
# Measure is how much does each predictor explain of the total variance (as part of the whole model)

add_weight_beta <- function(betaList,model_errors,n,pS,pD,col=4,col_name = "prd_xpln_var"){
	emptyMat=matrix(0,n,ncol=(pS+pD))
	wBetaList = betaList
	for (j in 1:length(betaList)) {
		if(length(betaList[[j]]) > 0) {
			w = emptyMat
			x = unsparse(betaList[[j]],emptyMat)
			sum_beta = apply(abs(x),1,sum)
			for (k in 1:n) {
				if(sum_beta[k] > 0){
					w[k,] = x[k,]/sum_beta[k]*(1-model_errors[k,j])
				}
			}
			wBetaList[[j]] = cbind(wBetaList[[j]],0)
			x= make_sparse2(w)
#		y = sort(x[,1],index.return=TRUE)
			for (i in 1:nrow(x)){
				r=which(wBetaList[[j]][,1]==x[i,1] & wBetaList[[j]][,2]==x[i,2])
				wBetaList[[j]][r,col] = abs(x[i,3])
			}
			colnames(wBetaList[[j]])[col] = col_name
		}
	}
	return(wBetaList)
}

# Add a bias term for each non-zero wighted predictor	
# bT - bias term
# bL - beta list

add_bias_term <- function(bL,bT,col=7,col_name = "bias"){
	for (j in 1:length(bL)) {
		bL[[j]] = cbind(bL[[j]],rep(0,nrow(bL[[j]])))
		colnames(bL[[j]])[length(colnames(bL[[j]]))] = col_name
		for(i in 1:nrow(bL[[j]])){
			bL[[j]][i,col_name] = bT[bL[[j]][i,"trgt"],j]
		}		
	}
	return(bL)
}

# Combine the predictions from several L2 params into one list based on error

combine_l2_net_res <- function(bL,mE,col = "prd_xpln_var"){
	lnet.mat = matrix(0,ncol=3,nrow=0)
	colnames(lnet.mat) = c(colnames(bL[[1]])[1:2],col)
	for (i in 1:nrow(mE)) {
		l = which.min(mE[i,])
		rg_intrs = which(bL[[l]][,"trgt"]==i)
		if(length(rg_intrs)>0){
			lnet.mat = rbind(lnet.mat,bL[[l]][rg_intrs,colnames(lnet.mat)])
		}
	}
	if( col == "bias" ){
		unqModels <- unique(lnet.mat[,1])
		#new.mat = matrix(0,ncol=3,nrow=max(unqModels))
                new.mat = matrix(0,ncol=3,nrow=dim(mE)[1])
		#new.mat[,1] <- c(1:max(unqModels))
                new.mat[,1] <- c(1:dim(mE)[1])
		for( i in 1:length(unqModels)){
			new.mat[unqModels[i],1] <- lnet.mat[ which(lnet.mat[,1] == unqModels[i])[1], 1]
			new.mat[unqModels[i],2] <- lnet.mat[ which(lnet.mat[,1] == unqModels[i])[1], 2]
			new.mat[unqModels[i],3] <- lnet.mat[ which(lnet.mat[,1] == unqModels[i])[1], 3]
		}
		lnet.mat <- new.mat
	}
	return(lnet.mat)
}

# Combine two matrices with entries either >0 or 0
# map higest values in tmplt.mat (M1) with sml.mat (M2) that you want to combine to tmplt_mat

combine_mtrcs <- function(M1,M2){
	ix_m1 = sort(M1,decreasing = TRUE,index.return = TRUE)$ix
	ix_m2 = sort(M2,decreasing = TRUE,index.return = TRUE)$ix
	M = M1
	i=1
	while(M2[ix_m2[i]] > 0){
		M[ix_m2[i]] = sqrt(M[ix_m2[i]]^2+M[ix_m1[i]]^2)
		i = i+1
	}
	return(M)
}

add_zscore <- function(bL,M1,M2=NULL,col=5,col_name = "clr_zs"){
	zBL = bL
	if(length(bL) > 0) {
		for(i in 1:length(bL)) {
			zBL[[i]] = cbind(zBL[[i]],0)
		}
#	for (i in 1:length(bL)) {
#		if(length(bL) > 0) {
		for(i in 1:length(bL)) {
			r = bL[[i]][,1]
			c = bL[[i]][,2]
			inters = which(c > nrow(M1))
			singles= which(c <= nrow(M1))
			if (length(singles) > 0)
			zBL[[i]][singles,col] = M1[cbind(r[singles],c[singles])]
			if (length(inters) > 0)
			zBL[[i]][inters,col] = M2[cbind(r[inters],c[inters]-nrow(M1))]
			colnames(zBL[[i]])[col] = col_name
		}
	}
#	}
	return(zBL)
}

######################################
make_sparse2 <- function(M)
{	
	non_zero_idx = which(M != 0,arr.ind = TRUE)
	w = matrix(0,nrow(non_zero_idx),3)
	w[,1] = non_zero_idx[,1]
	w[,2] = non_zero_idx[,2]
	w[,3] = M[non_zero_idx]
	return(w)
}
######################################
#INPUT
#1) sM - a matrix of dim N*3, where N is the number of interactions
#2) M -  the matrix into which the un-sparsed network go
#OUTPUT
#1) M - binary matrix of unsparsed network. Rows are targets and columns are regulators
unsparse <- function(sM,M) {
	for (i in 1:nrow(sM)){
		M[sM[i,1],sM[i,2]] = sM[i,3]
	}
	return(M)
}
