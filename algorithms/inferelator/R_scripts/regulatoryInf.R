## RB, ISB 2004-03 , major rewrite after switch to cv, glm-lasso, ts+eq
## AM, NYU 2008-03 , changing regulatoryInfluencesClust.1and2s to ignore pos neg

#################################################################################################
## pay attention here ratios is a local copy ratios =ratios[reg.infs,]
regulatoryInfluencesClust.1and2s <- function( ratios, reg.infs, cluster, colMap, assNeibs = NA, cluster.col.map=NA,
											  squash = "none" , plot.it = FALSE, tau = 15.00, pred.subset.mode = "all", time.mode = "all",
											  num.single.pred = 7, num.inter.pred = 5, max.inter.corr.cutoff = max.inter.corr.cutoff)
{
	if (! is.list( cluster.col.map ) ) cluster.col.map <- colMap
	best1s = character()
	inOutResp <- makeOutputInput.lars(colMap=NA,
								  ratios[1,], cluster$redExp, tau,
								  bi.cols = cluster$cols, col.map=cluster.col.map,
								  time.mode = time.mode )
	cols.permutation = colnames(inOutResp$inputs)
	output = inOutResp$outputs
#print(str(output))
	x = abs(cor(t(ratios[reg.infs,cols.permutation]),output))
	num.preds = length(reg.infs)

	for(i in 1:num.single.pred) {
		best1s = c(best1s,rownames(x)[which.max(x)])
		x[best1s[i],1]=0
	}
#nrow=(num.preds^2-num.preds)/2
	##AM # M holds all the interaction (pred pred) names
	M=matrix(nrow=0,ncol=2)
	for (i in 1:(num.preds-1)) {
		m =matrix(nrow=length((i+1):num.preds),ncol=2)				
		m[,1] = rep(reg.infs[i],length((i+1):num.preds))
		m[,2] = reg.infs[(i+1):num.preds] 
		M = rbind(M,m)
	}
#print(M[(dim(M)[1]-100):dim(M)[1],])
		cors=numeric()
		for (i in 1:dim(M)[1]) {
			cors = c(cors, 
					 abs(cor(
							 pmin( ratios[M[i,1],cols.permutation], ratios[M[i,2],cols.permutation] )
							 , output))
					 )
		}
#print(cors)
		if ( pred.subset.mode == "all" ) {
			best2s = matrix(nrow=0,ncol=2)
			# choose best 5 unique interactions and not similar to single TFs interactions
			while (dim(best2s)[1] < num.inter.pred) {
				i = which.max(cors)
				cors[i]=0
				#AM # if this tf is already in an icorporated interaction term -> next
				#AM # note: Rich and Dave do a step after function call to remove non uniques 
				##AM # (resulting in different # of interactions for each biclust)
				if (length( which(M[i,] %in% best2s) ) > 0) {
					next
				}
				##AM # this is more appropriate then the next, i think. less overfit.		
				cors.with.single = abs( cor(
											t(ratios[reg.infs,cols.permutation]) ,
											pmin(ratios[M[i,1],cols.permutation],ratios[M[i,2],cols.permutation]) 
											) )
				# this is what Rich and Dave had before	(they implemented it differently but this is just that..)			
				#cors.with.single = abs( cor(
				#							t(ratios[M[i,],cols.permutation]) ,
				#							pmin(ratios[M[i,1],cols.permutation],ratios[M[i,2],cols.permutation]) 
				#							) )
				cor.with.single = cors.with.single[which.max( cors.with.single )]
	#print(cor.with.single)
				if (cor.with.single > max.inter.corr.cutoff) {
					next
				}
				best2s = rbind(best2s , M[i,])
			}
		}
#	print(best2s)
	if ( pred.subset.mode == "all" ) {
		return( list( Best2s = t(best2s), Best1s = best1s ) )
	} else {
		return( list( Best1s = best1s ) )
	}
}

################################################################################

regulatoryInfluencesClust.modFit.lars <-  function( ratios, reg.infs, cluster, colMap,
           BestFromRnd1, BestFromRnd2 = "none",
           assNeibs = NA, squash = "none" , plot.it = FALSE, tau = 15.00, cv.min = FALSE, cluster.col.map=NA,
           max.tau.iter=1, tolerance.tau=1, tau.range=c(5,60), time.mode = "all", 
		   lars.use.prob = F, alpha=0.5 ) {
	##AM ## if we use priors set these variables, that are contained in cluster list 
	if(lars.use.prob == T & length(cluster$prior.pvals)>0 ){
		prior.prob = cluster$prior.pvals
		lars.prior.names = cluster$prior.names		
	} else {
		lars.use.prob = F 
	}

   ## now do lasso-glmBinomial-cv on just the guys with good scores plus the scaffold guys
   ##tau.range <- c(10,150) ## in hours ...  this seems like a physically relevant range
                            ## give some msg if we peg this value on either end of range 
   ##max.tau.iter <- 5  ## your guess is as good as mine, but we don't want to blow too much time on this!
   ##tolerence.tau <- 1  ## as in permissible difference NOT willingness to recognize and respect the beliefs of others
  if (! is.list( cluster.col.map ) ) cluster.col.map <- colMap

   ##if (plot.it == TRUE) {
     ## on alice# postscript("/tmp/regInf-CV-curves-raw.ps", paper = "letter")
     ##psOut <- paste("output/",cluster$k,".regInf-CV-curves-raw.ps", sep = "")
     ## postscript(psOut, paper = "letter")
     ## par(mfrow = c(2,2))
   ##}
   
   numTFs <- length( reg.infs )
   influence.nw.row <- numeric( length = numTFs )
   names(influence.nw.row) <- reg.infs
   cv.err <- numeric()
   
   candidate.influence.set <- c( BestFromRnd1 )
   num.single <- length(candidate.influence.set)

   if ( BestFromRnd2 != "none" && (! is.na(BestFromRnd2)) ) {
     for (iter  in 1:max.tau.iter ) {

       ## if we slide into null model then exit
       tau.init <- tau   ## tau moves
       if (iter > 1) {
         cv.err.init <- cv.err
         beta.0.init <- beta.0
         coefficients.init <- coefficients
         candidate.influence.set.wInt.init <- candidate.influence.set.wInt
         inputs.init <- inputs
         outputs.init <- outputs
         ltest.init <- ltest
         cv.ltest.init <- cv.ltest
         best.s.init <- best.s
       }
       ##cat("before makeInOut during final model fitting", iter, "\n")    
       inOutResp <- makeOutputInput.lars(colMap=NA,
                                         ratios[candidate.influence.set,], cluster$redExp, tau, 
                                         time.mode = time.mode, bi.cols = cluster$cols, col.map=cluster.col.map )
       ##RB ## inOutResp <- makeOutputInput.lars.old(colMap,
       ##RB ##                                  ratios[candidate.influence.set,], cluster$redExp, tau, 
       ##RB ##                                  bi.cols = cluster$cols )
       
       ##cat("in1\n")
       ##makeInBest2 <- function (BestFromRnd2, colMap, ratios, redExp, bi.cols.in = "all", tau, cluster.col.map=NA) {
       inInter <- makeInBest2(BestFromRnd2, colMap=NA, 
                              ratios, cluster$redExp, bi.cols.in = cluster$cols , tau,
                              cluster.col.map=cluster.col.map, time.mode = time.mode )
       ##RB ## inInter <- makeInBest2.nold(BestFromRnd2, colMap, 
       ##RB ##                     ratios, cluster$redExp, bi.cols.in = cluster$cols , tau,
       ##RB ##                             cluster.col.map=cluster.col.map )
                              
	   ## cat("before rbind\n");print (inOutResp$inputs);print (inInter$intin )
       ##menu(c(1,2))
	   
        inputs <- rbind( inOutResp$inputs, inInter$intin )
		rownames(inputs)[ (dim(inOutResp$inputs)[1]+1):length(rownames(inputs)) ] <-  inInter$names
		   
	    ##AM ## here I add the priors to the inputs matrix
		if(lars.use.prob == TRUE){
		   cat("incorporating priors to begining of inputs matrix:\n")
		   cat(lars.prior.names,"\n")
		   ##AM ## here we put the priors that were chosen 1 is default
		   chosen.pvals = cor.prob(cor(t(inputs),inOutResp$outputs),nrow(t(inputs))-2)
		   prior.pval = quantile(chosen.pvals,alpha)   
		   prior.prob = rep(prior.pval,length(prior.prob)) 
		   all.prior.prob = rep(1,dim(inputs)[1])
		   ##AM # for each prior
		   for (i in 1:length(lars.prior.names)){
			   prior = lars.prior.names[i]
			   ##AM #if it is a valid regulatory influence e.g. not part of biclust we infer
			   if (prior %in% reg.infs){
				   ##AM #if it was not chosen in preprocessing
				   if (! prior %in% rownames(inputs) ){
					   ##AM #make pval = 1 for unsuported TFs make pval=pval for supported TFs
					   all.prior.prob <- c(prior.prob[i], all.prior.prob)
					   ##AM #add priors to begining of inputs
					   inputs <- rbind(ratios[prior, colnames(inputs)], inputs) 
					   ##AM #get name of prior into rownames
					   rownames(inputs)[1] <- prior 
					  #cat("dim inputs: ", dim(inputs),"\n")
					  #cat("priors as pvals are: ", all.prior.prob,"\n")
				   } else {
					   cat(prior, ": is already chosen by preprocessing.\n")
					   #cat("updating pval for: ",prior, ".\n",sep="")
					   all.prior.prob[which(rownames(inputs) %in% prior)] = prior.prob[i]
					   #cat("after updating, priors as pvals are: ", all.prior.prob,"\n")
				   }
			   } else {
				   cat(prior, ": is not in reg.infs! excluding it.\n")
				   if (prior %in% rownames(cluster$redExp)){
					   cat(prior, ": is part of bicluster.\n")  
				   }
				   next
			   }
		   }
		   cat ("inputs str after concatanating:\n ")
		   cat(str(inputs))			
	   }
		   
       outputs <- inOutResp$outputs
       inOutResp$inputs <- inputs
       candidate.influence.set.wInt <- c(candidate.influence.set, inInter$names) ## names should be in form "vng.vng.max"
       
       ## get cv err for several values of shrinkage parameter
       tmp.na <- which( is.na(outputs) )
       if ( length( tmp.na ) > 0) {
         outputs[tmp.na] <- 0
         cat("NA FOUND IN RESPONCE VECTOR\n AT CLUSTER: ", cluster$k, tmp.na, "\n")
         cat("new outputs:\n")
         print(outputs) 
       } 

       ##modified to include prior probs greeny
       if( lars.use.prob == T){
		##AM # Aviv Debug #######
		   ltest <- lars(t(inputs), outputs, type = "lasso", trace = FALSE)
		   plot(ltest)
		   title.tmp <- paste(cluster$k, tau, "no prior")
		   title(title.tmp)
	   ##AM # Aviv Debug #######
         ltest <- lars_probs(t(inputs), outputs, type = "lasso", trace=FALSE, prior.prob = all.prior.prob)
       } else {
         ltest <- lars(t(inputs), outputs, type = "lasso", trace = FALSE)
       }
		   
       if ( plot.it ) {
         plot(ltest)
         title.tmp <- paste(cluster$k, tau)
         title(title.tmp)
       }       ##cat("after lasso in tau \n")

	   k.fold <- min(10, length(outputs) )
       ##modified to include prior probs greeny
       if( lars.use.prob == T ){
	     cv.ltest <- cv.lars(t(inputs), outputs, K = k.fold, type = "lasso")
		#cv.ltest <- cv.lars_probs(t(inputs), outputs, K = k.fold, type = "lasso", prior.prob = all.prior.prob)
		 cv.ltest <- cv.lars_probs(t(inputs), outputs, K = k.fold, type = "lasso", prior.prob = all.prior.prob, 
								   fraction = c( seq(from = 0, to = 0.1, length = 100), seq(from = 0.11, to = 1, length = 90)) )		   
       } else{
         cv.ltest <- cv.lars(t(inputs), outputs, K = k.fold, type = "lasso")
       }
       
       min.i <- which(cv.ltest$cv == min(cv.ltest$cv) )
       min.err <- cv.ltest$cv.error[ min.i ]

       if ( cv.min ) {
         thresh.cv <- min(cv.ltest$cv)
       } else {
         thresh.cv <- min(cv.ltest$cv) + min.err
       }
       ##thresh.cv <- min(cv.ltest$cv)
       
       best.s <- 0
       
       for (i in 1:min.i ) {
         if  (cv.ltest$cv[i] <= thresh.cv) {
           best.s <- i
           break
         }
       }
       if ( plot.it ) lines(c(cv.ltest$fraction[best.s],cv.ltest$fraction[best.s]), range(cv.ltest$cv), col=2, lty=2, lwd=3)
       
       #cat("cv (1): ", min.i, min.err, best.s, cv.ltest$cv[best.s], "\n")
       
       coefficients <- coef.lars(ltest, s = cv.ltest$fraction[best.s], mode = "fraction")
       ## overfit # coefficients <- coef.lars(ltest, s = cv.ltest$fraction[min.i], mode = "fraction")
       ##cat("coef ...\n")
       ##print(coefficients)
       cv.err <- cv.ltest$cv[best.s]
       ##cat(" 4\n")
       ## if the bound-fration is 0 then we fit no param but intercept... no point fitting 
       ## tau if we can't fit inputs influence ...
       ## CHANGE THIS TO SOME ALTERNATE GUESS IF WE SELECT NULL 
       if (best.s == 1 && iter > 1 ) {
         ## reset tau and old model and exit
         cv.err <- cv.err.init
         beta.0 <- beta.0.init
         tau <- tau.init
         coefficients <- coefficients.init
         candidate.influence.set.wInt <- candidate.influence.set.wInt.init
         inputs <- inputs.init
         outputs <- outputs.init
         ltest <- ltest.init
         cv.ltest <- cv.ltest.init
         best.s <- best.s.init
         cat("Breaking at tauIter: ", iter, " to avoid slide into null model\n")
         break
       }
                                        #
       ##cat("before tau\n")
       ## find better tau given this glm
       ##print(inOutResp)
       ##pause()
       ## inOutResp not needed below
       if (max.tau.iter > 1 && iter < max.tau.iter) {
         tau <- getTau.minRes.lars( ltest, colMap, inOutResp,  cluster$redExp, 
                                   bi.cols = cluster$cols,
                                   tau,
                                   s.fraction = cv.ltest$fraction[best.s])
         cat("tau (1): ", tau, tau.init, tau.range, "\n")
         if ( abs( tau - tau.init ) < tolerance.tau ) {
           cat("tau didn't move enough ...  break from tau optimization\n")
           break
         }
         if ( tau < tau.range[1] ) {
           cat("tau broke floor\n")
           tau <- tau.range[1] 
         } else if (tau > tau.range[2] ) {
           cat("tau broke ceiling\n")
           tau <- tau.range[2]
         }
       }
     }
   } else {
     cat(" no interactions provided ... using single infs\n")
     ##stop(" after breaking makeOutputInput() this is due for fixing\n")
     candidate.influence.set.wInt <- candidate.influence.set  ## redundant for returning this vector
      for (iter  in 1:max.tau.iter ) {
       tau.init <- tau   ## tau moves
       inOutResp <- makeOutputInput.lars(colMap=NA,
                                         ratios[candidate.influence.set,], cluster$redExp, tau, 
                                         time.mode = time.mode, bi.cols = cluster$cols, col.map=cluster.col.map )
      
       inputs <- inOutResp$inputs
		##AM ## here I add the priors to the inputs matrix
		  if(lars.use.prob == T){
			  cat("incorporating priors to begining of inputs matrix:\n")
			  cat(lars.prior.names,"\n")
			##AM ## here we put the priors that were chosen 1 is default
			  all.prior.prob = rep(1,dim(inputs)[1])
			##AM # for each prior
			  for (i in 1:length(lars.prior.names)){
				  prior = lars.prior.names[i]
				##AM #if it is a valid regulatory influence e.g. not part of biclust we infer
				  if (prior %in% reg.infs){
					##AM #if it was not chosen in preprocessing
					  if (! prior %in% rownames(inputs) ){
						##AM #make pval = 1 for unsuported TFs make pval=pval for supported TFs
						  all.prior.prob <- c(prior.prob[i], all.prior.prob)
						##AM #add priors to begining of inputs
						  inputs <- rbind(ratios[prior, colnames(inputs)], inputs) 
						##AM #get name of prior into rownames
						  rownames(inputs)[1] <- prior 
						  cat("dim inputs: ", dim(inputs),"\n")
						  cat("priors as pvals are: ", all.prior.prob,"\n")
					  } else {
						  cat(prior, ": is already chosen by preprocessing.\n")
						  cat("updating pval for: ",prior, ".\n",sep="")
						  all.prior.prob[which(rownames(inputs) %in% prior)] = prior.prob[i]
						  cat("after updating, priors as pvals are: ", all.prior.prob,"\n")
					  }
				  } else {
					  cat(prior, ": is not in reg.infs! excluding it.\n")
					  if (prior %in% rownames(cluster$redExp)){
						  cat(prior, ": is part of bicluster.\n")  
					  }
					  next
				  }
			  }
			  cat ("inputs ste after concatanating:\n ")
			  cat(str(inputs))			
		  }
		  
		  
       outputs <- inOutResp$outputs
       ## inOutResp$inputs <- inputs
       ## candidate.influence.set.wInt <- c(candidate.influence.set, inInter$names) ## names should be in form "vng.vng.max"
       ## get cv err for several values of shrinkage parameter
       tmp.na <- which( is.na(outputs) )
       if ( length( tmp.na ) > 0) {
         outputs[tmp.na] <- 0
         cat("NA FOUND IN RESPONCE VECTOR\n AT CLUSTER: ", cluster$k, tmp.na, "\n")
         cat("new outputs:\n")
         print(outputs) 
       } 

       ##modfied to include prior probs  greeny
       if( lars.use.prob == T){
         ltest <- lars_probs(t(inputs), outputs, type = "lasso", trace=FALSE, prior.prob = all.prior.prob)
       }
       else{
         ltest <- lars(t(inputs), outputs, type = "lasso", trace = FALSE)
       }

       if ( plot.it ) {
         plot(ltest)
         title.tmp <- paste(cluster$k, tau)
         title(title.tmp)
       }
       ##cat("after lasso in tau \n")
       k.fold <- min(7, length(outputs) )

       ##modified to include prior probs greeny
       if( lars.use.prob == T ){
         cv.ltest <- cv.lars_probs(t(inputs), outputs, K = k.fold, type = "lasso", prior.prob = all.prior.prob)
       }
       else{
         cv.ltest <- cv.lars(t(inputs), outputs, K = k.fold, type = "lasso")
       }
       
       min.i <- which(cv.ltest$cv == min(cv.ltest$cv) )
       min.err <- cv.ltest$cv.error[ min.i ]
       ##thresh.cv <- min(cv.ltest$cv) + min.err
       thresh.cv <- min(cv.ltest$cv)
       best.s <- 0
       for (i in 1:min.i ) {
         if  (cv.ltest$cv[i] <= thresh.cv) {
           best.s <- i
           break
         }
       }
       if ( plot.it ) lines(c(cv.ltest$fraction[best.s],cv.ltest$fraction[best.s]), range(cv.ltest$cv), col = 2, lty = 2)
       cat("cv (2): ", min.i, min.err, best.s, cv.ltest$cv[best.s], "\n")
       coefficients <- coef.lars(ltest, s = cv.ltest$fraction[best.s], mode = "fraction")
                                      
       cv.err <- cv.ltest$cv[best.s]
       ## CHANGE THIS TO SOME ALTERNATE GUESS IF WE SELECT NULL 
       if (best.s == 1 && iter < max.tau.iter ) {
         ## add null model revert to last model!
         best.s == min.i  ## ??? arrrr!
       } 
        
       ##cat("before tau\n")
       ## find better tau given this glm
       if (max.tau.iter > 1 && iter < max.tau.iter) {
         tau <- getTau.minRes.lars( ltest, colMap, inOutResp,  cluster$redExp, 
                                   bi.cols = cluster$cols,
                                   tau,
                                   s.fraction = cv.ltest$fraction[best.s])
         cat("tau (2) ", tau, tau.init, tau.range, "\n")
         if ( abs( tau - tau.init ) < tolerance.tau ) {
           cat("tau didn't move enough ...  break from tau optimization\n")
           break
         }
         if ( tau < tau.range[1] ) {
           cat("tau broke floor\n")
           tau <- tau.range[1] 
         } else if (tau > tau.range[2] ) {
           cat("tau broke ceiling\n")
           tau <- tau.range[2]
         }
       }
     }
   }
  ##cat("finishing up\n")
  ## ##################
  
   ##if (plot.it) dev.off()
   ## return info to main with invisible() ?
   if (best.s == 1 ) { 
      ## null model, shiznit!
      beta.0 <- mean( outputs )  ## lasso sweeps out intercpt, so not in coeff vector
      cat("null model selected for : ", cluster$k , "\n")
      object <- list(influence = influence.nw.row, cv.err = cv.err,
                      intercept = beta.0, tau = tau , is.null = TRUE,
                      coeff.lars =  coefficients,
                      cand.influence = candidate.influence.set.wInt,
                      inputs = inputs, outputs = outputs,
                      fraction = 0, lars.obj = ltest, cv.obj = cv.ltest, best.s = 1 )
      invisible (object)
   } else {
      parents <- candidate.influence.set[1:num.single]
      beta.0 <- mean( outputs )  ## 
      influence.nw.row[parents] <- coefficients[1:num.single]
      nonZeroParents <- candidate.influence.set.wInt[ abs(coefficients) > 0]
      nonZeroWeights <- coefficients[ abs(coefficients) > 0]
      names( coefficients ) <- candidate.influence.set.wInt
      # cat( "found nonZero weights for : ", nonZeroParents, "\n")
      # cat( "with weights              : ", nonZeroWeights, "\n")
      object <- list(influence = influence.nw.row, cv.err = cv.err, 
                      intercept = beta.0, tau = tau, is.null = FALSE,
                      coeff.lars =  coefficients,
                      cand.influence = candidate.influence.set.wInt,
                      inputs = inputs, outputs = outputs,
                      fraction = cv.ltest$fraction[best.s],
                      lars.obj = ltest, cv.obj = cv.ltest, best.s = best.s )
      invisible (object)
   }
}   ## end of model fit lars

get.coeffs.for.thresh <- function (x, xvar = c("norm", "df", "arc.length"), breaks = TRUE, 
    plottype = c("coefficients", "Cp"), omit.zeros = TRUE, eps = 1e-10, 
    ...) {
    object <- x
    plottype <- match.arg(plottype)
    xvar <- match.arg(xvar)
    coef1 <- object$beta
    coef1 <- scale(coef1, FALSE, 1/object$normx)
    if (omit.zeros) {
        c1 <- drop(rep(1, nrow(coef1)) %*% abs(coef1))
        nonzeros <- c1 > eps
        cnums <- seq(nonzeros)[nonzeros]
        coef1 <- coef1[, nonzeros]
    }
    else cnums <- seq(ncol(coef1))
    s1 <- switch(xvar, norm = {
        s1 <- apply(abs(coef1), 1, sum)
        s1/max(s1)
    }, df = seq(length(object$arc.length) + 1), arc.length = cumsum(c(0, 
        object$arc.length)))
    return( s1 )
}


################################################################################

regulatoryInfluencesClust.modFit.lm.step   <-  function( ratios, reg.infs, cluster, colMap,
           BestFromRnd1, BestFromRnd2 = "none",
           assNeibs = NA, squash = "none" , plot.it = FALSE, tau = 15.00, cluster.col.map=NA ) {

   stop( "revise after fixing makeOutputInput ? \n")
   ## now do lasso-glmBinomial-cv on just the guys with good scores plus the scaffold guys
   tau.range <- c(10,150) ## in hours ...  this seems like a physically relevant range
                            ## give some msg if we peg this value on either end of range 
   max.tau.iter <- 10  ## your guess is as good as mine, but we don't want to blow too much time on this!
   tolerance.tau <- 0.50  ## as in permissible difference NOT willingness to recognize and respect the beliefs of others

  if ( is.na( cluster.col.map ) ) cluster.col.map <- colMap

   ##if (plot.it == TRUE) {
   ##  ## on alice# postscript("/tmp/regInf-CV-curves-raw.ps", paper = "letter")
   ##  psOut <- paste(clust.curr,"regInf-CV-curves-raw.ps", sep = ".")
   ##  postscript(psOut, paper = "letter")
   ##}
   ## par(mfrow = c(2,2))

   ##tmp.r <- reg.infs[! reg.infs %in% cluster$rows ]
   ##reg.infs <- tmp.r
   ##numTFs <- length(reg.infs)
   ##cat(numTFs," tfs not in this clust\n")
   cv.err <- numeric()
   #candidate.influence.set <- c( assNeibs, BestFromRnd2[1,], BestFromRnd2[2,], BestFromRnd1 )
   candidate.influence.set <- c( BestFromRnd1 )
   num.single <- length(candidate.influence.set)
   ##cat("starting estimation of beta and tau with lm-step (AIC)\n")
   #print(BestFromRnd1);print(BestFromRnd2)
   #cat("single:\n");print(candidate.influence.set)
   ## we need to check for multiple guys ... they are garunteed in this 
   ## function, but if you use cv.gl1ce, don't feed it single row inputs
   if (  BestFromRnd2 != "none" ) {
     for (iter  in 1:max.tau.iter ) {
       tau.init <- tau   ## tau moves
       ##cat("before makeInOut during final model fitting", iter, "\n")
       inOutResp <- makeOutputInput.lars(colMap=NA,
                                         ratios[candidate.influence.set,], cluster$redExp, tau, 
                                         time.mode = time.mode, bi.cols = cluster$cols, col.map=cluster.col.map )
       
       inInter <- makeInBest2(BestFromRnd2, colMap=NA,
                              ratios, cluster$redExp, bi.cols.in = cluster$cols , tau,
                              cluster.col.map=cluster.col.map, time.mode = time.mode )
                                      
       inputs <- rbind( inOutResp$inputs, inInter$intin )
       outputs <- inOutResp$outputs
       inOutResp$inputs <- inputs
       candidate.influence.set.wInt <- c(candidate.influence.set, inInter$names) ## names should be in form "vng.vng.max"
                                    
       ## get cv err for several values of shrinkage parameter
       tmp.na <- which( is.na(outputs) )
       if ( length( tmp.na ) > 0) {
         outputs[tmp.na] <- 0
         cat("NA FOUND IN RESPONCE VECTOR\n AT CLUSTER: ", cluster$k, tmp.na, "\n")
         cat("new outputs\n")
         print(outputs) 
       } 
       ltest <- lm( outputs ~ t(inputs) )
       names(ltest$coefficients) <- c("(Intercept)",candidate.influence.set.wInt)
       ltest.st <- stepAIC( ltest, direction = "both")

       intercept <- ltest.st$coefficients[1]
       parents <- names(ltest.st$coefficients)[-1]
       weights <- as.numeric(ltest.st$coefficients)[-1]
       
       ##cat("before tau\n")
       ## find better tau given this glm
                                        #print(inOutResp)
                                        #pause()
       ## inOutResp not needed below
       tau <- getTau.minRes.lm( ltest.st, colMap, inOutResp,  cluster$redExp, 
                                  bi.cols = cluster$cols,
                                  tau )
       cat("tau; ", tau, tau.init, tau.range, "\n")
       if ( abs( tau - tau.init ) < tolerance.tau ) {
         cat("tau didn't move enough ...  break from tau optimization\n")
         break
       }
       if ( tau < tau.range[1] ) {
         cat("tau broke floor\n")
         tau <- tau.range[1] 
       } else if (tau > tau.range[2] ) {
         cat("tau broke ceiling\n")
         tau <- tau.range[2]
       }
     }
   } else {
     cat(" no interactions provided ... using single infs\n")
     candidate.influence.set.wInt <- candidate.influence.set
     for (iter  in 1:max.tau.iter ) {
       tau.init <- tau   ## tau moves
       inOutResp <- makeOutputInput.lars(colMap=NA,
                                         ratios[candidate.influence.set,], cluster$redExp, tau, 
                                         time.mode = time.mode, bi.cols = cluster$cols, col.map=cluster.col.map )
       inputs <- inOutResp$inputs
       outputs <- inOutResp$outputs
       tmp.na <- which( is.na(outputs) )
       if ( length( tmp.na ) > 0) {
         outputs[tmp.na] <- 0
         cat("NA FOUND IN RESPONCE VECTOR\n AT CLUSTER: ", cluster$k, tmp.na, "\n")
         cat("new outputs:\n")
         print(outputs) 
       } 
      
       ltest <- lm( outputs ~ t(inputs) )
       names(ltest$coefficients) <- c("(Intercept)",candidate.influence.set.wInt)
       ltest.st <- stepAIC( ltest, direction = "both")

       intercept <- ltest.st$coefficients[1]
       parents <- names(ltest.st$coefficients)[-1]
       weights <- as.numeric(ltest.st$coefficients)[-1]
       
       ##cat("before tau\n")
       ## find better tau given this glm
                                        #print(inOutResp)
                                        #pause()
       ## inOutResp not needed below
       tau <- getTau.minRes.lm( ltest.st, colMap, inOutResp,  cluster$redExp, 
                                  bi.cols = cluster$cols,
                                  tau )
       cat("tau; ", tau, tau.init, tau.range, "\n")
       if ( abs( tau - tau.init ) < tolerance.tau ) {
         cat("tau didn't move enough ...  break from tau optimization\n")
         break
       }
       if ( tau < tau.range[1] ) {
         cat("tau broke floor\n")
         tau <- tau.range[1] 
       } else if (tau > tau.range[2] ) {
         cat("tau broke ceiling\n")
         tau <- tau.range[2]
       }
     }
   }
   ##cat("finishing up\n")
   ####################

   ## return info to main with invisible() ?
   object <- list( parents = parents, 
                  tau = tau,
                  coeff.lm =  coefficients,
                  cand.influence = candidate.influence.set.wInt,
                  ##inputs = inputs, outputs = outputs,
                  lm.obj = ltest.st )
   ##if (plot.it) dev.off()
   invisible (object)
   
}   ## end of model fit lm-step


##########################################################################################

makeOutputInput.lars <- function (colMap, ratios.i, ratios.o, tau, bi.cols = "all", squash.mode = "none",
                                  col.map=NA, time.mode = "all" ) {
  ## make response and input vectors similar to wahde + hertz
  ## this sub/func deals with the problem of
  ## how to fit the regulatory function when some of the columns in our
  ## data are from time series and some are from equilibrium experiemnts.
  ## time series are dealt with like discreete samples from a decay/accretion process
  ##
  
  ##  with the constant tau controling decay, while equilibrium data
  ## is dealt with as we've always dealt with it (set d/dt(ratios) = 0 and fit)
  
  ## * note, some of the mess in this function is due to the fact that
  ##   when you pass R funcs a single row from a matrix the type is converted to 
  ##   vector and when you then say "as.matrix(rowThatShouldBeMatrix)" you 
  ##   get a column ... SO  all these ifs are 'cause of that!
  
  ##cat("starting to fill inOut\n")
  ##inOut <- list()
  ## colMap$numTS is number of distinct time series in this set of experiments
  ## BUT if we do this over just the conditions in the bicluster 
  ## SO we must declare the matricies bigger (nCol cols) and then subtract the number of "f"s.
  ##cat("rat\n", ratios.i,"\n")

  if ( time.mode == "eq.override") {  
    col.names <- names(ratios.o)
    names.shift <- character()
    
    if (bi.cols[1] == "all" ) {
      bi.cols <- col.names
      bi.cols.i <- 1:length(col.names) 
    } else {
      ##cat ("using biclust defined columns\n")
      ##print ( bi.cols ) 
      bi.cols.i <- which( col.names %in% bi.cols ) 
    }    
    
    ## we check , mabey we've been fed only one row ... R data types --- love/hate !?
    ##if (length(ratios.i) == length(ratios.o) ) {
    if ( ! is.matrix( ratios.i ) ) {
      ratios.i <- t( as.matrix( ratios.i ) ) ## Convert to 1-row matrix
    } 
    nCol <- length(bi.cols)  ## number of conditions before taking out 1st/Last occurences (-1 per ts)
    nRow <- nrow(ratios.i) ## number of predictors
    
    if (! is.list( col.map ) ) {
      col.map <- get.col.map.one.cluster( col.names, colMap, cluster=NULL, bi.cols=bi.cols )
    }
    
    ## numFirsts <- sum( col.map$is1stLast == "f" & col.map$isTs == TRUE, na.rm=T )
    
    ## if (numFirsts > col.map$numTS) {
    ##   stop("numFirsts is greater than the number of ts is whole set\n" )
    ## }   
    out.tmp <- ratios.o[ bi.cols.i ]
    in.tmp <- ratios.i[ ,bi.cols.i ]
    if ( ! is.matrix( in.tmp ) ) in.tmp <- t( in.tmp )
    ##colnames( in.tmp ) <- names( prevs[ good.i ] )
    ##names.shift <- names( prevs[ good.i ] ) ##col.names[ bi.cols.i ][ good.inds ]
    
  } else if (time.mode == "all" ) {
    ##stop("eq override mode not tested\n")
    col.names <- names(ratios.o)
    names.shift <- character()
    
    if (bi.cols[1] == "all" ) {
      bi.cols <- col.names
      bi.cols.i <- 1:length(col.names) 
    } else {
      ##cat ("using biclust defined columns\n")
      ##print ( bi.cols ) 
      bi.cols.i <- which( col.names %in% bi.cols ) 
    }    
    
    ## we check , mabey we've been fed only one row ... R data types --- love/hate !?
    ##if (length(ratios.i) == length(ratios.o) ) {
    if ( ! is.matrix( ratios.i ) ) {
      ratios.i <- t( as.matrix( ratios.i ) ) ## Convert to 1-row matrix
    }
    nCol <- length(bi.cols)  ## number of conditions before taking out 1st/Last occurences (-1 per ts)
    nRow <- nrow(ratios.i) ## number of predictors

    
    if (! is.list( col.map ) ) {
      col.map <- get.col.map.one.cluster( col.names, colMap, cluster=NULL, bi.cols=bi.cols )
    }
    
    numFirsts <- sum( col.map$is1stLast == "f" & col.map$isTs == TRUE, na.rm=T )
    
    if (numFirsts > col.map$numTS) {
      stop("numFirsts is greater than the number of ts is whole set\n" )
    }
    
    good.i <- ( ( col.map$isTs == TRUE ) & ( col.map$is1stLast == "m" | col.map$is1stLast == "l" ) ) |
    ( col.map$isTs == FALSE & col.map$is1stLast == "e" )
    prevs <- col.map$prevCol
    del.ts <- col.map$delta.t  
    out.tmp <- ( (tau / del.ts) * (ratios.o[ bi.cols.i ] - ratios.o[ prevs ]) ) + ratios.o[ prevs ]
    out.tmp[ out.tmp > 3.0 ] <- 3.0
    out.tmp[ out.tmp < -3.0 ] <- -3.0
    ##out.tmp[ good.es ] <- ratios.o[ bi.cols.i ][ good.es ]
    out.tmp <- out.tmp[ good.i ]
    in.tmp <- ratios.i[ ,prevs[ good.i ] ]
    if ( ! is.matrix( in.tmp ) ) in.tmp <- t( in.tmp )
    ##colnames( in.tmp ) <- names( prevs[ good.i ] )
    ##names.shift <- names( prevs[ good.i ] ) ##col.names[ bi.cols.i ][ good.inds ]
    
  } else if (time.mode == "eq.restrict" ) {
    good.cols = TRUE  ## this is to make sure
    stop("eq.restrict mode not tested\n")
    col.names <- names(ratios.o)
    names.shift <- character()
    
    if (bi.cols[1] == "all" ) {
      bi.cols <- col.names
      bi.cols.i <- 1:length(col.names) 
    } else {
      ##cat ("using biclust defined columns\n")
      ##print ( bi.cols ) 
      bi.cols.i <- which( col.names %in% bi.cols ) 
    }    
    
    ## we check , mabey we've been fed only one row ... R data types --- love/hate !?
    ##if (length(ratios.i) == length(ratios.o) ) {
    if ( ! is.matrix( ratios.i ) ) {
      ratios.i <- t( as.matrix( ratios.i ) ) ## Convert to 1-row matrix
    } 
    ##nCol <- length(bi.cols)  ## number of conditions before taking out 1st/Last occurences (-1 per ts)
    ##nRow <- nrow(ratios.i) ## number of predictors
    
    if (! is.list( col.map ) ) {
      col.map <- get.col.map.one.cluster( col.names, colMap, cluster=NULL, bi.cols=bi.cols )
    }
    
    ##numFirsts <- sum( col.map$is1stLast == "f" & col.map$isTs == TRUE, na.rm=T )
    ##if (numFirsts > col.map$numTS) {
    ##  stop("numFirsts is greater than the number of ts is whole set\n" )
    ##}
    
    good.i <- ( col.map$isTs == FALSE & col.map$is1stLast == "e" )
    if ( length(good.i) < 5 ) good.cols <- FALSE 
    out.tmp <- ratios.o[ bi.cols.i[ good.i ] ]
    in.tmp <- ratios.i[ ,bi.cols.i[ good.i ] ]
    if ( ! is.matrix( in.tmp ) ) in.tmp <- t( in.tmp )
    ##colnames( in.tmp ) <- names( prevs[ good.i ] )
    ##names.shift <- names( prevs[ good.i ] ) ##col.names[ bi.cols.i ][ good.inds ]
    
  } else if ( time.mode == "ts.restrict" ){
    stop("ts.restrict not supported\n")
  }
  
  if ( time.mode != "eq.restrict" && time.mode != "ts.restrict" ) {
    return( list( names=names.shift, inputs=in.tmp, outputs=out.tmp ) )
  } else {
    return( list(  names=names.shift, inputs=in.tmp, outputs=out.tmp, good.cols = good.cols ) )
  }
}
############################################################################################################

################################################################################

### old versions of the code to test R-i-fied versions 
### RB testing new code by DJR 


#########################################################################################

makeDesignMat.pair <- function (X.mat, mode = "comp" ) {

    ## changed to use only mins() not maxes by DJR
    ##   this makes for much less overfit models.
  
    ## posibly put names on these guys
       
    ## we check , mabey we've been fed only one row ... R data types --- love/hate !?
    if ( is.null(dim(X.mat))  ) {
       stop(" single row OR incorrect dimmentsions read in TO PAIR INT FUNC... STOPPING\n") 
    } else if (dim(X.mat)[1] >= 2) {
       nCol <- dim(X.mat)[2] ## number of conditions before taking out 1st/Last occurences (-1 per ts)
       nRow <- dim(X.mat)[1] ## number of predictors
    } else {
      stop(" problem with dimensions in makeDesignMat.pair\n")
    }

    #cat("making names vec\n")
    if (is.null(rownames(X.mat)) ) {
      r.names <- as.character(1:nRow)
    } else {
      r.names <- rownames(X.mat)
    }
    
    if (mode == "comp") {
       #include diagonal i.e. the predictors and thier interactions
      cat( "comp mode not supported!!!\n" )
#        nNewDim <- nRow ^ 2
#        if ( ! use.maxes ) nNewDim <- nNewDim / 2
#        Z.mat <- matrix(0.0, ncol = nCol, nrow = nNewDim)
#        rownames(Z.mat) <- as.character(1:nNewDim)
#        rownames(Z.mat)[1:nRow] <- r.names
#        Z.mat[1:nRow,] <- X.mat
    
#        shift.row <- 1 + nRow
#        for (i in 1:(nRow-1)) {
#          for (j in (i+1):nRow ) {
#            ##max
#            ##cat("before rbind:", i , j, "\n")
#            ##print(X.mat)
#            if ( use.maxes ) {  
#              Z.mat[shift.row,] <- apply(rbind(X.mat[i,], X.mat[j,]), 2, max)
#              rownames(Z.mat)[shift.row] <- paste(r.names[i], r.names[j], "max", sep = ".")
#              shift.row <- shift.row + 1
#            }
#            ## min
#            Z.mat[shift.row,] <- apply(rbind(X.mat[i,], X.mat[j,]), 2, min)
#            rownames(Z.mat)[shift.row] <- paste(r.names[i], r.names[j], "min", sep = ".")
#            shift.row <- shift.row + 1
#          }
#        }
       
    } else if (mode == "int") {
      # include just the interactions
      nNewDim <- nRow^2 - nRow
      if ( ! use.maxes ) nNewDim <- nNewDim / 2
      Z.mat <- matrix(0.0, ncol = nCol, nrow = nNewDim)
      rownames(Z.mat) <- as.character(1:nNewDim)
      
       shift.row <- 1
       for (i in 1:(nRow-1)) {
         for (j in (i+1):nRow ) {
           ##max
           ##cat("before rbind:", i , j, "\n")
           ##print(X.mat)
           if ( use.maxes ) {
             Z.mat[shift.row,] <- apply(rbind(X.mat[i,], X.mat[j,]), 2, max)
             rownames(Z.mat)[shift.row] <- paste(r.names[i], r.names[j], "max", sep = ".")
             shift.row <- shift.row + 1
           }
           ## min
           Z.mat[shift.row,] <- apply(rbind(X.mat[i,], X.mat[j,]), 2, min)
           rownames(Z.mat)[shift.row] <- paste(r.names[i], r.names[j], "min", sep = ".")
           shift.row <- shift.row + 1
         }
       }
    }
    
    return (Z.mat )
}

#########################################################################################

makeInBest2 <- function (BestFromRnd2, colMap, ratios, redExp, bi.cols.in = "all", tau, cluster.col.map=NA, time.mode = "all") {
 
  ## this fnx takes a list of screened pairs of genes (genes that had good signal with
  ## max or min, and adds thier min and max to a matrix compatible in dim with the
  ## results of makeOutputInput.lars()
  ##cat("BestFromRnd2 inside\n")
  ##print( BestFromRnd2 )
  ## cat("1. ")

  if (! is.list( cluster.col.map ) ) cluster.col.map <- colMap
  
  candidate.influence.set <- c( BestFromRnd2[1,1], BestFromRnd2[2,1] ) 
  ##cat("2. ")
  inOutResp <- makeOutputInput.lars(colMap=NA,
                                     ratios[candidate.influence.set,],
                                     redExp, tau, bi.cols = bi.cols.in, col.map=cluster.col.map )
  ##cat("3. ")
  rownames(inOutResp$inputs) <- candidate.influence.set
  ##cat("4. ")
  in.interactions <- makeDesignMat.pair(inOutResp$inputs, mode = "int")
  int.names <-  rownames(in.interactions)

  ncol.int <- ncol(in.interactions) 
  nrow.int <- ncol(BestFromRnd2) * 2
  if ( ! use.maxes ) nrow.int <- ncol(BestFromRnd2)
  int.intin <- matrix(, nrow = nrow.int, ncol = ncol.int)
  int.intin[1,] <- in.interactions[1,]
  if ( use.maxes ) int.intin[2,] <- in.interactions[2,]
  ##cat("5. ")
  ##cat(" ncol(Best2 ) : ", ncol( BestFromRnd2 ), "\n")
  if ( ncol(BestFromRnd2) > 1 ) {
    for (i in 2:ncol(BestFromRnd2) ) {
      ##cat("\n 6 ", i, "\n")
      ##if ( use.maxes )
      candidate.influence.set <- c( BestFromRnd2[1,i], BestFromRnd2[2,i] )
      ##else candidate.influence.set <- BestFromRnd2[1,i]
      inOutResp <- makeOutputInput.lars(colMap=NA,
                                      ratios[candidate.influence.set,],
                                      redExp, tau, bi.cols = bi.cols.in, col.map=cluster.col.map, time.mode = time.mode)
      rownames(inOutResp$inputs) <- candidate.influence.set
      in.interactions <- makeDesignMat.pair(inOutResp$inputs, mode = "int")
      ##cat("...")
      int.names <- c(int.names, rownames(in.interactions) )
      if ( use.maxes ) {
        int.intin[(2*i - 1),] <- in.interactions[1,]
        int.intin[(2*i),] <- in.interactions[2,]
      } else {
        int.intin[i,] <- in.interactions[1,]
      }
     }
   }

   ##cat("\n")
   object <- list( names = int.names, intin = int.intin )
   return( object )
}


#########################################################################################


######################################################################################################
## new version to account for biclust

getTau.minRes.lars <- function (lars.intl, colMap, inOut, ratios.o, bi.cols = "all", tau.intl , s.fraction = 0.5,
                                t.dt.range = c( 0.1, 3.0 ) ) {    
    ## changed on 7/7/2004 to account for the different delta.t values for different time series.    
    ## changed again on 8/24 to account for biclusters
    ## RB 7/7/2004    

    ##cat("bi.cols:\n")
    ##print( bi.cols )
    ##cat("names ratios.o :\n")
    ##print( names( ratios.o ) )
    
    ##cat("checking input params and setting up\n")
    if (length(t.dt.range) != 2) {
    	stop( "incorrect tau/ delta.t range specified with t.dt.range\n")
    }
    
    tau.new <- numeric()    
    
    ## X wil be tau.new/delta.t    
  
    X <- numeric()
    i.ts <- 0
    i.bx <- 0
    BX <- numeric()   #C### predicted outputs
    St1 <- numeric()   #A### true response t
    St0 <- numeric()   #B### true response t - 1
    delta.t <- numeric() # delta.t for different time series


    ## check this to get correct lars predict ...
    ## BX.raw is Y^, BX is Y^ for just time series
    ##cat("getting BX.raw \n")
    BX.raw <- predict.lars( lars.intl , t(inOut$inputs), s = s.fraction, mode = "fraction", type = "fit" )
    #cat("BX.raw\n")

    #print(BX.raw)

    ##cat("bi.cols prior setup\n")
    if (bi.cols == "all") {
      bi.cols <-  names(ratios.o)
    }
    
    col.names <- names(ratios.o)    

    ## only optimize Tau over guys that are part of time series #
    ## further: don't use first point in time series (no prior point!). #
    ##cat("doing meat of tau opt\n")
    for( col.name in  bi.cols ) {
       #cat("col.name: " , col.name, "\n")
       #cat("col.names:\n")
       #print( col.names )
       i <- which( col.names %in% col.name )
       #cat("b0 ", i, "\n")
       if (colMap[[i]]$is1stLast != "f") {
         #cat("c0 ")
         i.bx <- i.bx + 1
         #cat("d0 ")
         #cat(" ",i, col.name, colMap[[i]]$isTs, "\n")
         if (colMap[[i]]$isTs == TRUE ) {
             
             #check tau/delta.t to see if it is in a good range and only then fit tau #
             if ( (tau.intl / colMap[[i]]$del.t) > t.dt.range[1] || 
                  (tau.intl / colMap[[i]]$del.t) < t.dt.range[2]    ) { 
                i.ts <- i.ts + 1
             
                #cat("a ")
                BX[i.ts] <- BX.raw$fit[i.bx]
                #cat("b ")
                St1[i.ts] <- ratios.o[i] 
                #cat("c ")
                St0[i.ts] <- ratios.o[colMap[[i]]$prevCol] 
                #cat("d \n")
                delta.t[i.ts] <- colMap[[i]]$del.t
             } else {## tau/delta.t check #
                cat("range : ", t.dt.range[1], t.dt.range[2], "\n")
                cat("tau / delta t exceeded:", i, col.name, tau.intl, tau.intl / colMap[[i]]$del.t, " \n")
             }
         }
       }
    }
    #cat("i.ts: ", i.ts, " i.bx: ", i.bx, "\n")
    #menu(c(1,2))
    if (i.ts > 7) {
      ## cat("getting new tau w:", i.ts, " points in ts data\n")
      #cat("st0\n")
      #print(St0)
      #cat("st1:\n")
      #print(St1)
      #cat("BX\n")
      #print(BX)
    
      #cat("1\n")
      b.prime <- 2 * delta.t * ( (St0 * St1) - (St0 ^ 2) + (St0 * BX) - (St1 * BX) ) ## 2C2
      #cat("2\n")
      a.prime <- (St1 ^ 2) + (St0 ^ 2) - (2 * St1 * St0)                     ## C1
      #cat("3\n")
      #print (b.prime)
      #print (a.prime)

      X <- - sum( b.prime ) / ( 2.0 * sum( a.prime ) )
      #cat("4\n")
      tau.new <- X
      if (is.na(tau.new) ) {
         ## this should never occur
         tau.new <- tau.intl
         cat("tau == NaN, RESETTING tau to initl val: ", tau.intl, "\n")
         #menu(c(1,2))
      }
    } else {
      tau.new <- tau.intl
      cat("TOO FEW TS POINTS TO GET TAU\n")
    }
    return (tau.new)
}

################################################################################

getTau.minRes.lm <- function (lm.intl, colMap, inOut, ratios.o, bi.cols = "all", tau.intl,
                                t.dt.range = c( 0.1, 3.0 ) ) {    
    ## changed on 7/7/2004 to account for the different delta.t values for different time series.    
    ## changed again on 8/24 to account for biclusters
    ## RB 7/7/2004    
    
    ##cat("checking input params and setting up\n")
    if (length(t.dt.range) != 2) {
    	stop( "incorrect tau/ delta.t range specified with t.dt.range\n")
    }
    tau.new <- numeric()    
    ## X wil be tau.new/delta.t    
    X <- numeric()
    i.ts <- 0
    i.bx <- 0
    BX <- numeric()   #C### predicted outputs
    St1 <- numeric()   #A### true response t
    St0 <- numeric()   #B### true response t - 1
    delta.t <- numeric() # delta.t for different time series

    ## check this to get correct lars predict ...
    ## BX.raw is Y^, BX is Y^ for just time series
    ##cat("getting BX.raw \n")
    #BX.raw <- predict( lm.intl , t(inOut$inputs) )
    BX.raw <- predict( lm.intl )
    ##cat("bi.cols prior setup\n")
    if (bi.cols == "all") {
      bi.cols <-  names(ratios.o)
    } 
    col.names <- names(ratios.o)      
    ## only optimize Tau over guys that are part of time series #
    ## further: don't use first point in time series (no prior point!). #
    ##cat("doing meat of tau opt\n")
    for( col.name in  bi.cols ) {     
       i <- which( col.names %in% col.name )
       #cat("b0 ", i, "\n")
       if (colMap[[i]]$is1stLast != "f") {
         #cat("c0 ")
         i.bx <- i.bx + 1
         if (colMap[[i]]$isTs == TRUE ) {
             #check tau/delta.t to see if it is in a good range and only then fit tau #
             if ( (tau.intl / colMap[[i]]$del.t) > t.dt.range[1] || 
                  (tau.intl / colMap[[i]]$del.t) < t.dt.range[2]    ) { 
                i.ts <- i.ts + 1
                BX[i.ts] <- BX.raw$fit[i.bx]
                St1[i.ts] <- ratios.o[i] 
                St0[i.ts] <- ratios.o[colMap[[i]]$prevCol] 
                delta.t[i.ts] <- colMap[[i]]$del.t
             } else {           ## tau/delta.t check #
                cat("range : ", t.dt.range[1], t.dt.range[2], "\n")
                cat("tau / delta t exceeded:", i, col.name, tau.intl, tau.intl / colMap[[i]]$del.t, " \n")
             }
         }
       }
    }
    #cat("i.ts: ", i.ts, " i.bx: ", i.bx, "\n");menu(c(1,2))
    if (i.ts > 7) {
      b.prime <- 2 * delta.t * ( (St0 * St1) - (St0 ^ 2) + (St0 * BX) - (St1 * BX) ) ## 2C2
      a.prime <- (St1 ^ 2) + (St0 ^ 2) - (2 * St1 * St0)                     ## C1
      X <- - sum( b.prime ) / ( 2.0 * sum( a.prime ) )
      tau.new <- X
      if (is.na(tau.new) ) {
         ## this should never occur
         tau.new <- tau.intl
         cat("tau == NaN, RESETTING tau to initl val: ", tau.intl, "\n")
         #menu(c(1,2))
      }
    } else {
      tau.new <- tau.intl
      cat("TOO FEW TS POINTS TO GET TAU\n")
    }
    return (tau.new)
}

#################################################################################################


#################################################################################################
## pre-bi-clust
getTau.minRes.lars.defunct <- function (lars.intl, colMap, inOut, tau.intl , s.fraction = 0.5) {
    ## changed on 7/7/2004 to account for the different delta.t values for different time series.
    ## RB 7/7/2004
    tau.new <- numeric()
    ## X wil be tau.new/delta.t
    nCol <- length(inOut$outputs)
    X <- numeric()
    i.ts <- 0
    BX <- numeric()   #C### predicted outputs 
    St1 <- numeric()   #A### true response t
    St0 <- numeric()   #B### true response t - 1
    delta.t <- numeric() # delta.t for different time series
   
    ## check this to get correct lars predict ...
    ## BX.raw is Y^, BX is Y^ for just time series 
    BX.raw <- predict.lars( lars.intl , t(inOut$inputs), s = s.fraction, mode = "fraction", type = "fit" )
    #cat("BX.raw\n")
    #print(BX.raw)
    
    ## only optimize Tau over guys that are part of time series
    ## further: don't use first point in time series (no prior point!).
    for(i in 1:nCol ) {
         if (colMap[[i]]$isTs == TRUE && colMap[[i]]$is1stLast != "f" ) {
             i.ts <- i.ts + 1
             BX[i.ts] <- BX.raw$fit[i]
             St1[i.ts] <- inOut$outputs[i]
             St0[i.ts] <- inOut$outputs[colMap[[i]]$prevCol] 
             delta.t[i.ts] <- colMap[[i]]$del.t
         }
    } 
    ##
    #cat("getting new tau w:", i.ts, " points in ts data\n")
    #print(St0)
    #print(St1)
    #print(BX)
    
    #cat("1\n")
    b.prime <- 2 * delta.t * ( (St0 * St1) - (St0 ^ 2) + (St0 * BX) - (St1 * BX) ) ## 2C2 
    #cat("2\n")
    a.prime <- (St1 ^ 2) + (St0 ^ 2) - (2 * St1 * St0)                     ## C1  
    #cat("3\n")
    #print (b.prime)
    #print (a.prime)

    X <- - sum( b.prime ) / ( 2.0 * sum( a.prime ) )
    #cat("4\n")
    tau.new <- X 
    if (is.na(tau.new) ) {
       tau.new <- tau.intl
       cat("tau == NaN, RESETTING tau to initl val: ", tau.intl, "\n") 
       ##menu(c(1,2))
    }

    return (tau.new)
}

getAssNeibs <- function (interactionMat, row.num, cut = 0.005, maxNum = 5 ) {
   ## Ass stands for association network, neibs for 1st neighborhs in ass
   assNeibs <- numeric()
   assRow <- interactionMat[row.num, ]
   assNeibs <- which ( assRow > cut ) 
   if (length(assNeibs) > maxNum ) {
      assNeibs <- assNeibs[1:maxNum]
   }
   return ( assNeibs ) 
}



###  just a placeholder for now
### for  a version that looks one gene at a time (no clustering as a previous step)
#regulatoryInfluencesSingle =
#function( ratios, redFuncReg, tfs , gene.id , plot.it = TRUE ) {
#   ## ratios are raw ratio matrix before any cluster reduction
#
#   
#}

histDegree.defunct <- function( inf.nw , hdim = 2 ) {

    if (hdim == 2) { ## slice cols give deg  dist
       len <- ncol(inf.nw) 
       deg.hist <- numeric( length = len)
       names(deg.hist) <- colnames(inf.nw)
       for (i in 1:len) {
          deg.hist[i] <- length( which(influence.nw[,i] != 0) )
       }
    } else if (hdim == 1) {
       len <- nrow(inf.nw)     
       deg.hist <- numeric( length = len)
       names(deg.hist) <- rownames(inf.nw)
       for (i in 1:len) {
          deg.hist[i] <- length( which(influence.nw[i,] != 0) )
       }
    } else {
       stop ("incorrect dim spec\n")
    }
    return (deg.hist )

}
#################################################################################################


