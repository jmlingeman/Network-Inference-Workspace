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

# all the non-zero weights after l1-regression (LARS)
# inputs:
# 1- X Design matrix
# 2- Y response matrix
# 3- Xd Design matrix of double interaction terms (equal NULL by default)
# 4- Pi permutation matrix: for each row (most generally bicluster) you have a different permutation of conditions
# 5- M1 or M2 a scoring matrix (e.g positive z_scores) for all of the single or double predictors, respectively
# 6- nS or nD a max number of predictors for single or double predictors, respectively
# 7- nCv the fold of cross validation (number of cross validation steps)
# 9- lambda a value for the importance of l2 regularization as well as lasso l1
# 10- verbose output TRUE or FALSE
# 11- plots into this file (as pdf)

# output:
# bM the sparse ODE model matrix 

# helper functions:
# get the best n predictors as weighted in x (highest positive z_scores)
get_best_preds_idx <- function(x,n) {
	bestPreds <- sort(x, decreasing = TRUE, index.return=TRUE)
	n = min(n, length(which(bestPreds$x > 0)))
	if(n != 0)
		bestPreds <- bestPreds$ix[1:n]
	else
		bestPreds <-NULL
	return(bestPreds)
}

calc_ode_model_weights <- function(Xs, Y, Xd = NULL, Pi, M1, nS, M2 = NULL, nD = NULL, Inter_lars = NULL,nCv = 10,lambda = 0, verbose = FALSE, plot.it = FALSE, plot.file.name = NULL){	
	if (!is.null(Xd)) {
		use_interactions = TRUE
	} else {
		use_interactions = FALSE
	}
	
	if ( plot.it == TRUE & !is.null(plot.file.name) )
		pdf(plot.file.name)

	bM.list <- list()
	for (i in 1:length(lambda)) {
		bM.list[[i]] = matrix(0,ncol=3,nrow=0)
		colnames(bM.list[[i]]) = c("trgt","tf","beta")
	}
	cv.err.all = matrix(0,ncol=nrow(Y),nrow=length(lambda))
	l2.all = numeric(length=nrow(Y))
	biasVec <- matrix(0,ncol=nrow(Y),nrow=length(lambda))
	allEnetResAndCoeff <- list()
	cat("\nrunning elastic net with l2 norm constraint weight = ", paste(lambda," ",sep="") ,"\n",sep="")
	for( ind in 1:nrow(Y)){
		uniqConds <- unique( which(!is.na(Pi[ind, ])) ) #finds unique conditions!
		if (length(which(is.na(uniqConds)))>1)
			uniqConds <- uniqConds[-which(is.na(uniqConds))]
		# get best single predictors
		bestSingles <- get_best_preds_idx(M1[ind, ],nS)
		
		Xs_ind <- Xs[ bestSingles, which(!is.na(Pi[ind, uniqConds])),drop=F]
		rownames(Xs_ind) <-  bestSingles
		
		#if any gene has zero variance, remove it!  #might not need this! AG Apr 17
		allVar <- apply(Xs_ind, 1, var)
		if( any(allVar == 0) ){
			cat("A TF or EF has zero variance!! \n")
			Xs_ind <- Xs_ind[-which(allVar==0),]
		} 
	
		# get best double predictors		
		if(use_interactions) {
			bestDoubles <- get_best_preds_idx(M2[ind, ],nD)
			if(length(bestDoubles)>1) {
				Xd_ind <- Xd[ bestDoubles, which(!is.na(Pi[ind, uniqConds]))]
				for (i in 1:nrow(Xd_ind))
					rownames(Xd_ind)[i] <- paste(Inter_lars[bestDoubles[i],1],",",Inter_lars[bestDoubles[i],2],sep="")
				X = rbind(Xs_ind,Xd_ind)
			} else if (length(bestDoubles)==1) {
				Xd_ind <- Xd[ bestDoubles, which(!is.na(Pi[ind, uniqConds]))]
				Xd_ind = as.matrix(t(Xd_ind))
				rownames(Xd_ind)[1] <- paste(Inter_lars[bestDoubles[1],1],",",Inter_lars[bestDoubles[1],2],sep="")
				X = rbind(Xs_ind,Xd_ind)
			} else{
				X = Xs_ind
			}
		} else {
			X = Xs_ind
		}
		
		# permute response based on current bootstrap Pi
		Y_ind <- Y[ind, which(!is.na(Pi[ind, uniqConds]))]
		
		# placeholders for storing enet and cv.enet results
		enetRes <- list()
		cv.enetRes <- list()

    # find lambda2 (l2 norm regularization weight out of possible lambda vector)
		best.cvErr.min <- Inf
		k.fold <- min(nCv, length(Y_ind) )
	
		allLambdaRes <- list()
		#cat("GOT HERE ",dim(X), " \n")
		
		for (l in 1:length(lambda)) {
			l2=lambda[l]
			
			cv.enetRes <- cv.enet(t(X),Y_ind,lambda=l2,s=c( seq(from = 0, to = 0.1, length = 100), seq(from = 0.11, to = 1, length = 90)),
									  mode="fraction",trace=FALSE,plot.it=plot.it)	
				
			min.idx <- which.min(cv.enetRes$cv)
			min <- (cv.enetRes$cv[min.idx] +cv.enetRes$cv.error[min.idx])/cv.enetRes$cv[1] #<== why is this addition being done? does it have to do with percentage of error explained?
			cv.err.all[l,ind] = min
			if (l2 != lambda[length(lambda)]){
				cat("(g=",ind,",l2=",l2,",mincvErr=",min,") ",sep="")# format(min, digits = 3)
			}else{ 
				cat("(g=",ind,",l2=",l2,",mincvErr=",min,").\n",sep="")
			}
			if (min < best.cvErr.min) {
				best.l2 <- l2
				best.cvErr.min <- min
				best.cvErr.idx <- min.idx
			}

			thresh.cv <- cv.enetRes$cv[min.idx] + cv.enetRes$cv.error[min.idx]
			
			best.s <- 1
			for (i in 1:best.cvErr.idx ) {
				if  (cv.enetRes$cv[i] <= thresh.cv) {
					best.s <- i
					break
				}
			}
			if(plot.it){
				bestSRng <- range(cv.enetRes$cv[best.s]+.2*cv.enetRes$cv[best.s], cv.enetRes$cv[best.s]-.2*cv.enetRes$cv[best.s])
				lines(c(cv.enetRes$s[best.s],cv.enetRes$s[best.s]), range(cv.enetRes$cv[best.s]+.02*cv.enetRes$cv[best.s], cv.enetRes$cv[best.s]-.02*cv.enetRes$cv[best.s]), col=colors()[258], lty=1, lwd=3)		
				
				errRng <- range(cv.enetRes$cv[min.idx] + cv.enetRes$cv.error[min.idx],  cv.enetRes$cv[min.idx] - cv.enetRes$cv.error[min.idx])
				lines(c(cv.enetRes$s[min.idx],cv.enetRes$s[min.idx]), errRng, col=2, lty=2, lwd=3) # red line denoting where the minimum is
				lines(range(cv.enetRes$s), c(thresh.cv,thresh.cv), col=4, lty=2, lwd=3)   #blue line going to where one std. dev. above the min intersects the curve
				title(paste("enet curve for biclust: ",ind," w/ l2 weight: ",l2))
			
			}
			
			enetRes <- enet(t(X),Y_ind,lambda=l2)			
			
			orig_coefficients <- predict(enetRes, s=cv.enetRes$s[best.s], type="coef", mode="fraction")$coefficients
			
			#refit using least squares
			if( any(orig_coefficients != 0)){
		  	nonZeroPreds <- names(which(orig_coefficients != 0))
				linModel <- lm( Y_ind ~ t(X[nonZeroPreds,,drop=FALSE]))
				coefficients <- linModel$coefficients
				biasVec[l,ind] <- coefficients[1]
				coefficients <- coefficients[-1] #removing the intercept...we dont need it
 				resids <- linModel$residuals
				names(coefficients) <- nonZeroPreds
			}else{
				coefficients <- orig_coefficients
				resid <- NULL
			}
			allLambdaRes[[paste(lambda[l])]] <- list()
			allLambdaRes[[l]][["enetModel"]] <- cv.enetRes#list(cv.enetRes, min.idx,min,best.s,enetRes,orig_coefficients, coefficients)	
			allLambdaRes[[l]][["enetMinIdx"]] <- best.s
			allLambdaRes[[l]][["enetCoeff"]] <- orig_coefficients
			allLambdaRes[[l]][["refitCoeff"]] <- coefficients
			allLambdaRes[[l]][["refitResidual"]] <- resid
			
			#			print(enetRes)
			coef.inds = numeric(length = length(coefficients))
			for (i in 1:length(coefficients)) {
				if(coefficients[i]!=0) {
					if (i <= length(bestSingles)) {  
						bM.list[[l]]=rbind(bM.list[[l]],c(ind, as.numeric(names(coefficients)[i]),coefficients[i]))
					} else {
						x = strsplit(names(coefficients)[i],",")[[1]]
						x1 = as.numeric(x[1])
						x2 = as.numeric(x[2])
						bM.list[[l]]=rbind(bM.list[[l]],c(ind,which(Inter_lars[,1] %in% x1 & Inter_lars[,2] %in% x2) + nrow(Xs),coefficients[i]))
					}
				}
			}
		}

		allEnetResAndCoeff[[ind]] <- allLambdaRes
#		cat("adding bestL2, it is, ",best.l2,"\n")
		allEnetResAndCoeff[[ind]][["bestL2"]] <- as.character(best.l2)
		
		if( verbose ){
			cat("shrinkage value is: \t", cv.enetRes$s[best.s], "\n coefficients are:\n")
			print(coefficients)
		}
		
	# keep relative cv.err and chosen l2 to report for each model
	#		cv.err.all[ind] = best.cvErr.min
		l2.all[ind] = best.l2 
		
	}
	
	if ( plot.it == TRUE & !is.null(plot.file.name) )
		dev.off()
	
	#save(Xs,Y, Pi, M1, nS, allEnetResAndCoeff, file = paste(PARAMS$general$saveToDir,"/bootNum_",bootNum,".RData",sep="")) #used for debugging only
	allEnetResAndCoeff[["permMat"]] <- Pi
	allEnetResAndCoeff[["bestPreds"]] <- X
	
	#return(list(bM.list,cv.err.all,l2.all,allEnetResAndCoeff))
	return(list(bM.list,cv.err.all,biasVec,allEnetResAndCoeff))
} 


calc_ode_model_weights_parallel <- function(Xs, Y, Xd = NULL, Pi, M1, nS, M2 = NULL, nD = NULL, Inter_lars = NULL,nCv = 10,lambda = 0, verbose = FALSE, plot.it = FALSE, plot.file.name = NULL, processorsNumber = 1){
	if (!is.null(Xd)) {
		use_interactions = TRUE
	} else {
		use_interactions = FALSE
	}
	
	if ( plot.it == TRUE & !is.null(plot.file.name) )
		pdf(plot.file.name)
	
	bM.list <- list()
	for (i in 1:length(lambda)) {
		bM.list[[i]] = matrix(0,ncol=3,nrow=0)
		colnames(bM.list[[i]]) = c("trgt","tf","beta")
	}
	cv.err.all = matrix(0,ncol=nrow(Y),nrow=length(lambda))
	fracExp.err.all = matrix(0,ncol=nrow(Y),nrow=length(lambda))
	#l2.all = numeric(length=nrow(Y))
	biasVec <- matrix(0,ncol=nrow(Y),nrow=length(lambda))
	allEnetResAndCoeff <- vector("list",nrow(Y))

	ind <- as.vector(c(1:nrow(Y)))
	#date()
	x <- mclapply(ind,parEnet,Xs=Xs, Y=Y, Xd = Xd, Pi=Pi, M1=M1, nS=nS, M2=M2, nCv=nCv, nD=nD, Inter_lars=Inter_lars,lambda = lambda, 
					mc.cores=processorsNumber, verbose=verbose, plot.it=plot.it, plot.file.name=plot.file.name)
	if(plot.it)
			dev.off()
	#date()
	#putting the results of parallelization into the same data structures as they were in before
	lengthOut <- vector("numeric", length=length(ind))
	for(i in 1:length(ind))
		lengthOut[i] <- length(x[[ i ]])

	if( any(lengthOut < 6) ){
		notDone <- which(lengthOut < 6)
		cat("NOT DONE, REDOING ",length(notDone),": ",notDone)
		for(i in 1:length(notDone)){
			x[[ notDone[i] ]]	<- parEnet(notDone[i],Xs=Xs, Y=Y, Xd = Xd, Pi=Pi, M1=M1, nS=nS, M2=M2, nCv=nCv, nD=nD, Inter_lars=Inter_lars,lambda = lambda, verbose=verbose, plot.it=plot.it, plot.file.name=plot.file.name, use_interactions)
		}
	}
	
	for(i in 1:length(x)){
		for(l in 1:length(lambda)){
			bM.list[[l]] <- rbind(bM.list[[l]], x[[i]]$bM.list[[l]])
		}
		cv.err.all[ ,x[[i]]$ind ] <- x[[i]]$cv.err.all
		fracExp.err.all[ ,x[[i]]$ind ] <- x[[i]]$fracExp.err.all
		biasVec[ ,x[[i]]$ind ] <- x[[i]]$biasVec
		
		allEnetResAndCoeff[[ x[[i]]$ind ]] <- x[[i]]$allLambdaRes  
	}
	
	allEnetResAndCoeff[["permMat"]] <- Pi

#	date()
#xOrig = calc_ode_model_weights(Xs = Xs_lars,Y = Y_lars, Pi = Pi_s_lars, M1 = Zs, nS = 15, lambda=lambda, plot.it = FALSE,plot.file.name = paste(PARAMS$general$saveToDir,"/boot_",b,"_models.pdf",sep=""),verbose = FALSE)
#	date()
#	
#	
	return(list(bM.list,cv.err.all,biasVec,allEnetResAndCoeff,fracExp.err.all))
} 

parEnet <- function(ind,Xs, Y, Xd = NULL, Pi, M1, nS, M2 = NULL, nD = NULL, Inter_lars = NULL,nCv = 10,lambda = 0, verbose = FALSE, plot.it = FALSE, plot.file.name = NULL, use_interactions = FALSE){
	bM.list <- list()
	for (i in 1:length(lambda)) {
		bM.list[[i]] = matrix(0,ncol=3,nrow=0)
		colnames(bM.list[[i]]) = c("trgt","tf","beta")
	}
	cv.err.all = vector("numeric",length(lambda))
	names(cv.err.all) <- names(lambda)
	fracExp.err.all = vector("numeric",length(lambda))
	names(fracExp.err.all) <- names(lambda)
	biasVec <- vector("numeric",length(lambda))
	names(biasVec) <- names(lambda)
	
	uniqConds <- unique( which(!is.na(Pi[ind, ])) ) #finds unique conditions!
	if(verbose){
		cat("u=",length(uniqConds),sep="")
	}
	
	if (length(which(is.na(uniqConds)))>1)
		uniqConds <- uniqConds[-which(is.na(uniqConds))]
	# get best single predictors
	bestSingles <- get_best_preds_idx(M1[ind, ],nS)
	
	Xs_ind <- Xs[ bestSingles, which(!is.na(Pi[ind, uniqConds])),drop=F]
	rownames(Xs_ind) <-  bestSingles
	
	#if any gene has zero variance, remove it!  #might not need this! AG Apr 17
	allVar <- apply(Xs_ind, 1, var)
	if( any(allVar == 0) ){
		cat("A TF or EF has zero variance!! \n")
		Xs_ind <- Xs_ind[-which(allVar==0),]
	} 
	
	# get best double predictors		
	if(use_interactions) {
		bestDoubles <- get_best_preds_idx(M2[ind, ],nD)
		if(length(bestDoubles)>1) {
			Xd_ind <- Xd[ bestDoubles, which(!is.na(Pi[ind, uniqConds]))]
			for (i in 1:nrow(Xd_ind))
				rownames(Xd_ind)[i] <- paste(Inter_lars[bestDoubles[i],1],",",Inter_lars[bestDoubles[i],2],sep="")
			X = rbind(Xs_ind,Xd_ind)
		} else if (length(bestDoubles)==1) {
			Xd_ind <- Xd[ bestDoubles, which(!is.na(Pi[ind, uniqConds]))]
			Xd_ind = as.matrix(t(Xd_ind))
			rownames(Xd_ind)[1] <- paste(Inter_lars[bestDoubles[1],1],",",Inter_lars[bestDoubles[1],2],sep="")
			X = rbind(Xs_ind,Xd_ind)
		} else{
			X = Xs_ind
		}
	} else {
		X = Xs_ind
	}
	
	# permute response based on current bootstrap Pi
	Y_ind <- Y[ind, which(!is.na(Pi[ind, uniqConds]))]
	
	# placeholders for storing enet and cv.enet results
	enetRes <- list()
	cv.enetRes <- list()
	
	# find lambda2 (l2 norm regularization weight out of possible lambda vector)
	best.cvErr.min <- Inf
	k.fold <- min(nCv, length(Y_ind) )
	
	allLambdaRes <- list()
	#cat("GOT HERE ",dim(X), " \n")
	
	for (l in 1:length(lambda)) {
		#print something out just to let the user know things are happening
		cat(".")
		l2=lambda[l]
		
		cv.enetRes <- cv.enet(t(X),Y_ind,lambda=l2,s=c( seq(from = 0, to = 0.1, length = 100), seq(from = 0.11, to = 1, length = 90)),
				mode="fraction",trace=FALSE,plot.it=plot.it)	
		
		min.idx <- which.min(cv.enetRes$cv)
		min <- (cv.enetRes$cv[min.idx] + cv.enetRes$cv.error[min.idx])/cv.enetRes$cv[1] #<== why is this addition being done? does it have to do with percentage of error explained?
		cv.err.all[l] = (cv.enetRes$cv[min.idx] + cv.enetRes$cv.error[min.idx])/cv.enetRes$cv[1]#cv.enetRes$cv[min.idx] + cv.enetRes$cv.error[min.idx]
		
		if (l2 != lambda[length(lambda)]){
			if(verbose){	
				cat("(g=",ind,",l2=",l2,",mincvErr=",min,") ",sep="")# format(min, digits = 3)
			}
		}else{ 
			if(verbose){
				cat("(g=",ind,",l2=",l2,",mincvErr=",min,").\n",sep="")
			}
		}
		if (min < best.cvErr.min) {
			best.l2 <- l2
			best.cvErr.min <- min
			best.cvErr.idx <- min.idx
		}
		
		thresh.cv <- cv.enetRes$cv[min.idx] + cv.enetRes$cv.error[min.idx]
		
		best.s <- 1
		for (i in 1:best.cvErr.idx ) {
			if  (cv.enetRes$cv[i] <= thresh.cv) {
				best.s <- i
				break
			}
		}
		if(plot.it){
			bestSRng <- range(cv.enetRes$cv[best.s]+.2*cv.enetRes$cv[best.s], cv.enetRes$cv[best.s]-.2*cv.enetRes$cv[best.s])
			lines(c(cv.enetRes$s[best.s],cv.enetRes$s[best.s]), range(cv.enetRes$cv[best.s]+.02*cv.enetRes$cv[best.s], cv.enetRes$cv[best.s]-.02*cv.enetRes$cv[best.s]), col=colors()[258], lty=1, lwd=3)		
			
			errRng <- range(cv.enetRes$cv[min.idx] + cv.enetRes$cv.error[min.idx],  cv.enetRes$cv[min.idx] - cv.enetRes$cv.error[min.idx])
			lines(c(cv.enetRes$s[min.idx],cv.enetRes$s[min.idx]), errRng, col=2, lty=2, lwd=3) # red line denoting where the minimum is
			lines(range(cv.enetRes$s), c(thresh.cv,thresh.cv), col=4, lty=2, lwd=3)   #blue line going to where one std. dev. above the min intersects the curve
			title(paste("enet curve for biclust: ",ind," w/ l2 weight: ",l2))
			
		}
		
		enetRes <- enet(t(X),Y_ind,lambda=l2)			
		
		orig_coefficients <- predict(enetRes, s=cv.enetRes$s[best.s], type="coef", mode="fraction")$coefficients
#FEB 22 2010: RETURN THE REFIT RESIDUAL (USE THIS FOR RANKING)
		#error is sum of squared error devided by the number of samples
		#refit using least squares
		if( any(orig_coefficients != 0)){
			nonZeroPreds <- names(which(orig_coefficients != 0))
			linModel <- lm( Y_ind ~ t(X[nonZeroPreds,,drop=FALSE]))
			coefficients <- linModel$coefficients
			cBias <- coefficients[1]
			biasVec[l] <- cBias
			coefficients <- coefficients[-1] #removing the intercept...we dont need it
			names(coefficients) <- names(which(orig_coefficients != 0))
			resids <- linModel$residuals
			#modVals <- cBias + t(X[nonZeroPreds,])%*%coefficients #calculating the resultant model
			#----------how much error is explained by the model-------------#
			YHatSSE <- sum(resids^2)
			ySSE <- sum(Y_ind^2)
			relModelErr <- YHatSSE/ySSE #this measures how much better the model is than the 
			#null model (a line at zero). 1-relModelErr is the portion of explained variance of the model
			fracExp.err.all[l] <- relModelErr
			#----------done calculating the amount of error explained by the model-------------------#
			names(coefficients) <- nonZeroPreds
		}else{
			coefficients <- orig_coefficients
			resids <- NULL
		}
		allLambdaRes[[paste(lambda[l])]] <- list()
		allLambdaRes[[l]][["enetModel"]] <- cv.enetRes#list(cv.enetRes, min.idx,min,best.s,enetRes,orig_coefficients, coefficients)	
		allLambdaRes[[l]][["enetMinIdx"]] <- best.s
		allLambdaRes[[l]][["enetCoeff"]] <- orig_coefficients
		allLambdaRes[[l]][["refitCoeff"]] <- coefficients
		allLambdaRes[[l]][["refitResidual"]] <- resids
		
		#			print(enetRes)
		
		coef.inds = numeric(length = length(coefficients))
		for (i in 1:length(coefficients)) {
			if(coefficients[i]!=0) {
				if (i <= length(bestSingles)) {  
					bM.list[[l]]=rbind(bM.list[[l]],c(ind, as.numeric(names(coefficients)[i]),coefficients[i]))
				} else {
					x = strsplit(names(coefficients)[i],",")[[1]]
					x1 = as.numeric(x[1])
					x2 = as.numeric(x[2])
					bM.list[[l]]=rbind(bM.list[[l]],c(ind,which(Inter_lars[,1] %in% x1 & Inter_lars[,2] %in% x2) + nrow(Xs),coefficients[i]))
				}
			}
		}
	}
	
	#allEnetResAndCoeff[[ind]] <- allLambdaRes

	#allEnetResAndCoeff[[ind]][["bestL2"]] <- as.character(best.l2)
	
	if( verbose ){
		cat("adding bestL2, it is, ",best.l2,"\n")
		cat("shrinkage value is: \t", cv.enetRes$s[best.s], "\n coefficients are:\n")
		print(coefficients)
	}
	
	# keep relative cv.err and chosen l2 to report for each model
	#		cv.err.all[ind] = best.cvErr.min
	#l2.all[ind] = best.l2 
	if(plot.it){
		dev.off()
	}
	
	retList <- list(ind, bM.list,cv.err.all,fracExp.err.all,biasVec,allLambdaRes)
	names(retList) <- c( "ind","bM.list","cv.err.all","fracExp.err.all","biasVec","allLambdaRes")
	#cat(length(retList)," ")
	return(retList)
	
}
