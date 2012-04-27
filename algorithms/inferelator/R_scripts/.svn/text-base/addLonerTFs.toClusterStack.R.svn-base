### RBonneau 2004-03-20

addLonerTFs.toClusterStack = function (clusters, tf.names, colNames ) {

    newClusts <- list()
    n.newC <- 0

    seenInC <- logical()
    seenInC[tf.names] <- FALSE
    
    for (ii in 1:clusters$k ) {
        newClusts[[ii]] <- clusters[[ii]]
        seenInC[ clusters[[ii]]$rows ] <- TRUE
    }
    addTFs <- names( seenInC[seenInC == FALSE] )
    cat( "Adding", length(addTFs), "loner clusters to cluster stack...\n" )
    for (tf.name in addTFs) {
        cat("adding tf to end of cluster stack: ", tf.name, "\n")
        ii <- ii + 1
        n.newC <- n.newC + 1
        newC <- list()
         newC$cols <- colNames
         newC$ncols <- length( colNames)
         newC$rows <- tf.name 
         newC$nrows <- 1
         newC$resid <- NA
        newClusts[[ii]] <- newC
        rm (newC)
    }
    newClusts$k <- ii
    cat("added ", n.newC, " tf to end of cluster stack\n")
 
    return(newClusts)
}

########################################################################################

addLonerTFs.toClusterStack.lamFilt = function (clusters, lambdas, colNames,
                                   tf.names, l.thresh = 2.5 ) {

    ## only do this if lam and ratios are in same order, and have same names
    #if (! colNames ) {
    #  colNames <- colnames( lambdas ) ### to avoid any col reordering mixups
    #}
    
    newClusts <- list()
    n.newC <- 0

    seenInC <- logical()
    seenInC[tf.names] <- FALSE
    
    for (ii in 1:clusters$k ) {
        newClusts[[ii]] <- clusters[[ii]]
        seenInC[ clusters[[ii]]$rows ] <- TRUE
    }
    addTFs <- names( seenInC[seenInC == FALSE] )
    for (tf.name in addTFs) {
        cat("adding tf to end of cluster stack: ", tf.name, "\n")
        ii <- ii + 1
        n.newC <- n.newC + 1
        newC <- list()
         newC$cols <- colNames[ which( lambdas[ tf.name, ] > l.thresh ) ]
         newC$ncols <- length( colNames[ which( lambdas[ tf.name, ] > l.thresh ) ] )
         #newC$cols <- colNames
         #newC$ncols <- length( colNames)
         newC$rows <- tf.name 
         newC$nrows <- 1
         newC$resid <- NA
        newClusts[[ii]] <- newC
        rm (newC)
    }
    newClusts$k <- ii
    cat("added ", n.newC, " tf to end of cluster stack\n")
 
    return(newClusts)
}

