
##
##
##


get.col.map.one.cluster <- function( all.cols, colMap, cluster=NULL, bi.cols=cluster$cols ) {

  ##cat("IN HERE!!!\n")
  col.names <- all.cols

  if ( bi.cols[1] == "all" ) {
    bi.cols <- col.names
    bi.cols.i <- 1:length( col.names ) 
  } else {
    bi.cols.i <- which( col.names %in% bi.cols ) 
  }
  bi.cols <- all.cols[ which( all.cols %in% bi.cols ) ]

  isTs <- logical()
  isTs <- sapply( bi.cols, function(i) colMap[[ i ]]$isTs )
  is1stLast <- integer()
  ##is1stLast[ bi.cols ] <- sapply( bi.cols, function(i) colMap[[ i ]]$is1stLast )
  is1stLast <- sapply( bi.cols, function(i) colMap[[ i ]]$is1stLast )
  ##is1stLast <- c("e","f","m","l")[ as.integer( is1stLast ) ]
  ##is1stLast <- gsub( "1", "e", is1stLast )
  ##is1stLast <- gsub( "2", "f", is1stLast )
  ##is1stLast <- gsub( "3", "m", is1stLast )
  ##is1stLast <- gsub( "4", "l", is1stLast )
  is1stLast[ is1stLast == "1" ]  <- "e"
  is1stLast[ is1stLast == "2" ]  <- "f"
  is1stLast[ is1stLast == "3" ]  <- "m"
  is1stLast[ is1stLast == "4" ]  <- "l"
  ##names( is1stLast ) <- bi.cols
  prevCol <- character()
  prevCol[ bi.cols ] <- sapply( bi.cols, function(i) colMap[[ i ]]$prevCol )
  prevCol[ is.na( prevCol ) ] <- names( prevCol[ is.na( prevCol ) ] )
  delta.t <- numeric()
  delta.t[ bi.cols ] <- sapply( bi.cols, function(i) colMap[[ i ]]$del.t )
  delta.t[ is.na( delta.t ) ] <- 9999

  return( list( isTs=isTs, is1stLast=is1stLast, prevCol=prevCol, delta.t=delta.t, numTS=colMap$numTS ) )
}

################################################################################

infer.one.cluster <- function( cluster, reg.infs, colMap=get( "colMap", .GlobalEnv ),
                              ratios=get( "ratios", .GlobalEnv ), no.inf.cluster.members=T, assNeibs=NA,
                              r.cutoff=0.9, max.inter.corr.cutoff=0.75, smooth.tau=T, time.mode = "all", 
                              pred.subset.mode = "all", lars.use.prob, alpha, tau) {

  ## Smooth.tau = T tries a bazillion taus (5:50) so as to make

  ## we only do the screening of all 1s and 2s with a few taus even if smooth.tau = T
  tau.tries <- tau #c( 20, 10, 5 ) ## c(25,15,5)
  #cluster$cols <- gsub( "\\.", "-", cluster$cols, extended=F )
  
  if ( is.null( cluster$redExp ) ) cluster$redExp <- reduceExpression.single.clust( ratios, cluster )

  cluster.col.map <- get.col.map.one.cluster( colnames( ratios ), colMap, cluster=cluster )
      
  if ( is.null( cluster$k ) ) cluster$k <- 9999

  cat(file="output/log.txt","Starting reg.Influence: cluster", cluster$k, "\n", date() , "\n",append=T)
  cat("time mode is: ", time.mode,"\n")
  cat("predictor subset mode is: ", pred.subset.mode,"\n")

  cat(length( reg.infs ), " TFs with significant change (or env) \n")

  ## Get rid of 0-variance influences (i.e. env factors)
  var.exclude <- 0.01
  tmp <- apply( ratios[ reg.infs, cluster$cols ], 1, var, na.rm=T )
  names( tmp ) <- reg.infs
  ##tmp.2 <- apply( ratios[ reg.infs, ], 1, function(i) sum( i != min(i) ) )
  if ( any( is.na( tmp ) | tmp <= var.exclude ) )
    cat( "Excluding tfs", reg.infs[ which( is.na( tmp ) | tmp <= var.exclude ) ], "with var <=", var.exclude, "\n" )
  
  tmp.r <- reg.infs[! (reg.infs %in% cluster$rows) & ( is.na( tmp ) | tmp > var.exclude ) ]
  cat( length( tmp.r ), " TFs not in this clust\n")
  if ( ! no.inf.cluster.members ) {
    cat( "Inferring on all TFs, including those in the cluster.\n" )
  } else {
    cat( "Inferring on only TFs not in the cluster.\n" )
    reg.infs <- tmp.r
  }

  ## Get rid of highly-correlated influences not in cluster
  redExp.tmp <- cluster$redExp[ cluster$cols ]
  rats <- ratios[ reg.infs, cluster$cols ]
  cors <- apply( rats, 1, cor, redExp.tmp, use="pairwise" )
  correlated <- names( which( cors > r.cutoff ) )
  cat( "These regulators are highly correlated w/ the cluster (cutoff=", r.cutoff, "):\n" )
  print( cors[ correlated ] )
  cat( "Removing them from the list of reg infs\n" )
  reg.infs <- reg.infs[ ! reg.infs %in% correlated ]
  cat( "Inferring on", length( reg.infs ), "influences.\n" )
  rm( redExp.tmp )
  high.cor.regs <- correlated

  best1s <- character()
  best2s <- list()
  for ( tau.try.i in 1:length( tau.tries ) ) {
    cat( "\n\n1and2s: Tau =", tau.tries[ tau.try.i ], "\n" )  
	##AM # this step takes all the time in the inferelator run (making it more efficient? changing it?)
    if ( pred.subset.mode == "env") {
      reg.best <- regulatoryInfluencesClust.1and2s.env( ratios[reg.infs,],  reg.infs,
                                                       cluster = cluster, colMap=colMap,
                                                       cluster.col.map=cluster.col.map,
                                                       assNeibs = NA ,  tau = tau.tries[ tau.try.i ],
                                                       time.mode = time.mode, int.mode = "all")
      cat("done with regulatoryInfluencesClust.1and2s.env\n")
    } else {
      reg.best <- regulatoryInfluencesClust.1and2s( ratios[reg.infs,],  reg.infs,
                                                   cluster = cluster, colMap=colMap,
                                                   cluster.col.map=cluster.col.map,
                                                   assNeibs = NA ,  tau = tau.tries[ tau.try.i ],
                                                   time.mode = time.mode, pred.subset.mode = pred.subset.mode,
                                                   max.inter.corr.cutoff = max.inter.corr.cutoff)

      cat("done with regulatoryInfluencesClust.1and2s\n")
    }
    best1s <- c( best1s, reg.best$Best1s )
    best2s[[ tau.try.i ]] <- reg.best$Best2s 
  }
  
  
  best.1.2 = list()
  best.1.2$best2s = best2s[[1]]
  best.1.2$best1s = best1s
  cat("putative predictors for transcription unit:", cluster$k, "\n")  
  cat("**best single predictors**\n") 
  print( best.1.2$best1s )  
  cat("**best min interaction predictors**\n")
  print( best.1.2$best2s )
  
  ########### done with screening ## time to fit the model with L1 shrinkage
  pdfOut <- paste("output/",cluster$k,"regInf-CV-curves-raw.pdf", sep = "")
  pdf(pdfOut)
  par(mfrow = c(2,2))

  if ( smooth.tau ) {
    tau.tries <- tau
    tau.iter <- 1
    cv.matrix <- matrix(, nrow = length(tau.tries), ncol = 100) ## assuming length = 100 in cv.lars call (default)
    cv.stdErr.matrix <-  matrix(, nrow = length(tau.tries), ncol = 100)
    best.s <- rep(1, length(tau.tries))
    rownames(cv.matrix) <- tau.tries
    colnames(cv.matrix) <- round( seq(0,100, length = 100), 4)
  }else {
    tau.tries <- tau ##, 1:50
    tau.iter <- 1
    cv.matrix <- matrix(, nrow = length(tau.tries), ncol = 100) ## assuming length = 100 in cv.lars call (default)
    cv.stdErr.matrix <-  matrix(, nrow = length(tau.tries), ncol = 100)
    best.s <- rep(1, length(tau.tries))
    rownames(cv.matrix) <- tau.tries
    colnames(cv.matrix) <- round( seq(0,100, length = 100), 4)
  }

  #regInfs.f <- list()
  
  for ( tau.try in tau.tries ) {
    tau.i <- which( tau.tries == tau.try )
    cat( "\n\nmodfit.lars, no cv.min: Tau =", tau.try, "\n" )
    best.1.2$best1s <- best.1.2$best1s[ best.1.2$best1s %in% rownames( ratios ) ]
	#cat ( "new ones : ",  best.1.2$best1s, "\n")

    ##modify below greeny
    ##pass in  lars.probs
##AM # need to fix the names returned for the non-zero weights as prior names return as NA  
		  regInf.test <- regulatoryInfluencesClust.modFit.lars( ratios[reg.infs,],  reg.infs,
																cluster = cluster, colMap=colMap,
																best.1.2$best1s , best.1.2$best2s, tau=tau.try,
																cluster.col.map=cluster.col.map,
																assNeibs=NA, plot.it=TRUE, cv.min=FALSE,
																max.tau.iter = tau.iter,
																time.mode = time.mode,
																lars.use.prob = lars.use.prob, alpha=alpha )
    
    if (smooth.tau && tau.i == 1) {
      num.deg.ols <-  dim(regInf.test$lars.obj$beta)[1]  ## num deg + 1 (by index with 0 included)
      cat("number of deg: ", num.deg.ols, " iter ", tau.i, "\n")
      frac.deg <- matrix(1.0, nrow = length(tau.tries), ncol = num.deg.ols )
      frac.deg[tau.i, ] <- apply( abs( regInf.test$lars.obj$beta ), 1, sum) /
        sum(abs( regInf.test$lars.obj$beta[num.deg.ols,]  ) )
    } else if (smooth.tau) {
      num.deg.ols.tmp <-  dim(regInf.test$lars.obj$beta)[1]
      num.d <- min(num.deg.ols.tmp, num.deg.ols)
      cat("number of deg: ", num.deg.ols.tmp, " iter ", tau.i, "\n")
      #pause()
      frac.deg[tau.i, 1:num.d ] <- apply( abs( regInf.test$lars.obj$beta[1:num.d,] ), 1, sum) /
        sum(abs( regInf.test$lars.obj$beta[num.deg.ols.tmp,]  ) )
    }
    ##select.mode[kk] <- 1
    regInf.test$select.mode <- 1
    #regInfs.f[[ tau.i ]] <- regInf.test
    if (smooth.tau == FALSE && regInf.test$is.null == FALSE ) break ##return( regInf.test ) ##{ break }
    ##if (smooth.tau == TRUE) {
    cv.matrix[tau.i, ]        <- regInf.test$cv.obj$cv
    cv.stdErr.matrix[tau.i, ] <- regInf.test$cv.obj$cv.error
    best.s[tau.i] <- regInf.test$best.s
    ##}
  }
  
  #regInfs.t <- list()

  if ( regInf.test$is.null && smooth.tau == FALSE ) {
    for ( tau.try in tau.tries ) {
      cat( "\n\nmodfit.lars, yes cv.min: Tau =", tau.try, "\n" )
      regInf.test <- regulatoryInfluencesClust.modFit.lars( ratios[reg.infs,], reg.infs,
                                                           cluster=cluster, colMap=colMap,
                                                           best.1.2$best1s, best.1.2$best2s, tau=tau.try,
                                                           cluster.col.map=cluster.col.map,
                                                           assNeibs=NA, plot.it=TRUE, cv.min=TRUE,
                                                           max.tau.iter= tau.iter,
                                                           time.mode = time.mode
                                                          )
      ##select.mode[kk] <- 2
      regInf.test$select.mode <- 2
      #regInfs.t[[ tau.try ]] <- regInf.test
      if (smooth.tau == FALSE && regInf.test$is.null == FALSE ) break
      ##if (! regInf.test$is.null ) break ##return( regInf.test ) ##{ break }
    }
  }

  
  ##if (smooth.tau ) {
  if (FALSE) {
    ## 
    cv.errs.f <- unlist( sapply( regInfs.f, "[[", "cv.err" ) )
    fraction.f <- unlist( sapply( regInfs.f, "[[", "fraction" ) )
    infs.f <- sapply( regInfs.f, function(i) names( i$coeff.lars[ i$coeff.lars != 0 ] ) )
    n.infs.f <- sapply( regInfs.f, function(i) sum( i$coeff.lars != 0 ) )
    
    cv.errs.t <- unlist( sapply( regInfs.t, "[[", "cv.err" ) )
    fraction.t <- unlist( sapply( regInfs.t, "[[", "fraction" ) )
    infs.t <- sapply( regInfs.t, function(i) names( i$coeff.lars[ i$coeff.lars != 0 ] ) )
    n.infs.t <- sapply( regInfs.t, function(i) sum( i$coeff.lars != 0 ) )

    cat(" plotting a few things to get a handle on tau behavior\n")
    par( mfcol=c(2,2) )
    plot( tau.tries, cv.errs.f )
    plot( tau.tries, cv.errs.t )
    plot( tau.tries, n.infs.f[ 5:50 ] )
    plot( tau.tries, n.infs.t[ 5:50 ] )
    pause()
    
    plot( tau.tries, fraction.f )
    plot( tau.tries, fraction.t )
    plot( tau.tries, cv.errs.f - cv.errs.t )
  }
  
  dev.off()
  
  ##lm.step.list[[kk]] <- regulatoryInfluencesClust.modFit.lm.step( ratios[reg.infs,],  redExp[kk,],  reg.infs,
  ##                                                           cluster = cluster, colMap,
  ##                                                          best.1.2$best1s , best.1.2$best2s, 
  ##                                                           assNeibs = NA      , plot.it = TRUE)

  regInf.test$high.cor.regs <- high.cor.regs

  if ( smooth.tau ) {
    ret.obj <- list( regInf.test = regInf.test, cv.matrix = cv.matrix, cv.stdErr.matrix = cv.stdErr.matrix,
                    best.s = best.s, frac.deg = frac.deg )
  } else {
    ret.obj <- list( regInf.test = regInf.test, cv.matrix = cv.matrix, cv.stdErr.matrix = cv.stdErr.matrix, best.s = best.s)
  }
  invisible( ret.obj )
}  

#### foof #####
plot.cv.matrixes <- function( cvmats, best.ses , human = F ) {  ### foof
  for (jj in 1:length(cvmats) ) {
    plot.cv.matrix( cvmats[[jj]], best.ses[[jj]], p.type = "p")
    title(paste( "Cluster: ", jj, sep = "") )
    if (human) pause()
    plot.cv.matrix( cvmats[[jj]], best.ses[[jj]], p.type = "c")
    title(paste( "Cluster: ", jj, sep = "") )
    if (human) pause()
  }
}

plot.cv.matrixes.deg <- function( cvmats, best.ses , frac.deg, human = F ) {  ### foof
  for (jj in 1:length(cvmats) ) {
    ##plot.cv.matrix.deg( cvmats[[jj]], best.ses[[jj]], p.type = "p")
    ##title(paste( "Cluster: ", jj, sep = "") )
    ##if (human) pause()
    plot.cv.matrix.deg( cvmats[[jj]], best.ses[[jj]], frac.deg[[jj]], p.type = "deg")
    title(paste( "Cluster: ", jj, sep = "") )
    if (human) pause()
  }
}  

plot.cv.matrix <- function ( cv.mat, best.s, p.type = "p" ) { ### foof
  ##require(sm)
  tau <- as.numeric( rownames( cv.mat) )
  fraction <- as.numeric( colnames( cv.mat) )
  if ( p.type == "p" ) {
    p.colors <- surf.colors( cv.mat , col = yb.colors(50))
    persp( tau, fraction, cv.mat, axes = T,ticktype = "detailed", xlab = "tau", ylab = "fraction" , d = 20, phi = -20, theta = 40, col = p.colors )
  }
  ##if ( type == "h" ) {
  ##}
  if ( p.type == "c" ) {
    image( tau, fraction, cv.mat , col=gray((30:100)/100) )
    contour( tau, fraction, cv.mat, nlevels = 40 ,col = rainbow(40) , add = T)
    lines( tau, fraction[ best.s ], lwd = 3, col = 2)
                                        #text( tau[10], fraction[10], "cv.min")
    min.cv.i <- numeric()
    
    for ( i in 1:length(cv.mat[,1]) ) {
      min.cv.i[i] <- which( cv.mat[i,] == min( cv.mat[i,] ) )
    }
    lines( tau, fraction[min.cv.i], lwd = 3)
    tmp <- which( cv.mat == min( cv.mat ) , arr.ind = T)
    ## points( rep(tau[tmp[1]],3) , rep(fraction[tmp[2]],3) , cex = seq(5,20,5) ) ## see dave, i care about vertical compression
    points( tau[tmp[1]], fraction[tmp[2]] , cex = 20)
    points( tau[tmp[1]], fraction[tmp[2]] , cex = 15)
    points( tau[tmp[1]], fraction[tmp[2]] , cex = 10)
    points( tau[tmp[1]], fraction[tmp[2]] , cex = 5)
  }
  
}

plot.cv.matrix.deg <- function ( cv.mat, best.s, frac.deg, p.type = "deg" ) { ### foof
  ##require(sm)
  tau <- as.numeric( rownames( cv.mat) )
  fraction <- as.numeric( colnames( cv.mat) )
  image( tau, fraction, cv.mat , col=gray((30:100)/100) )

  col.deg <- rainbow( dim(frac.deg)[2] )
  for (j in 2:( dim(frac.deg)[2] ) ) {
    lines( tau, frac.deg[,j]*100, col = col.deg[j], lty = 2 )
    tmp.i <- seq(1, length(tau), 4)
    text( tau[tmp.i], frac.deg[tmp.i,j]*100, as.character( j -1 ), col = col.deg[j], cex = 0.5 )
  }
  
  lines( tau, fraction[ best.s ], lwd = 3, col = 2)
  min.cv.i <- numeric()
  
  for ( i in 1:length(cv.mat[,1]) ) {
    min.cv.i[i] <- which( cv.mat[i,] == min( cv.mat[i,] ) )
  }
  lines( tau, fraction[min.cv.i], lwd = 3)
  tmp <- which( cv.mat == min( cv.mat ) , arr.ind = T)
  points( tau[tmp[1]], fraction[tmp[2]] , cex = 20)
  points( tau[tmp[1]], fraction[tmp[2]] , cex = 15)
  points( tau[tmp[1]], fraction[tmp[2]] , cex = 10)
  points( tau[tmp[1]], fraction[tmp[2]] , cex = 5)
  
}

surf.colors <- function(x, col = terrain.colors(50)) { ### foof

  # First we drop the 'borders' and average the facet corners
  # we need (nx - 1)(ny - 1) facet colours!
  x.avg <- (x[-1, -1] + x[-1, -(ncol(x) - 1)] +
             x[-(nrow(x) -1), -1] + x[-(nrow(x) -1), -(ncol(x) - 1)]) / 4

  # Now we construct the actual colours matrix
  colors = col[cut(x.avg, breaks = length(col), include.lowest = T)]

  return(colors)
}


rg.colors = function (n)
{
    if ((n <- as.integer(n[1])) > 0) {
        even.n <- n%%2 == 0
        k <- n%/%2
        l1 <- k + 1 - even.n
        l2 <- n - k + even.n
        c(if (l1 > 0) hsv(h = 12/12, s = seq(0.5, ifelse(even.n,
            0.5/k, 0), length = l1), v = 1), if (l2 > 1) hsv(h = 4/12,
            s = seq(0, 0.5, length = l2)[-1], v = 1))
    }
    else character(0)
}

yb.colors = function (n)   ### foof
{
    if ((n <- as.integer(n[1])) > 0) {
        even.n <- n%%2 == 0
        k <- n%/%2
        l1 <- k + 1 - even.n
        l2 <- n - k + even.n
        c(if (l1 > 0) hsv(h = 2/12, s = seq(0.5, ifelse(even.n,
            0.5/k, 0), length = l1), v = 1), if (l2 > 1) hsv(h = 8/12,
            s = seq(0, 0.5, length = l2)[-1], v = 1))
    }
    else character(0)
}


