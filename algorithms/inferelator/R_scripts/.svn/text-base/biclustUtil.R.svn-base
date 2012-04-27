
sort.cluster.stack <- function( clusterStack, len.ret = "all") {
  if (len.ret == "all") {
    len.ret <- clusterStack$k
  } 
  sortedClust <- list()

  sort.i <- sort( histResid( clusterStack ), index.return = T)$ix
  if (length( sort.i) < len.ret) {
    stop( "len specified is bigger than num of biclusts\n")
  }
  for (i in 1:len.ret ) {
    sortedClust[[i]] <- clusterStack[[ sort.i[i] ]]
  }

  sortedClust$k <- len.ret
   
  invisible( sortedClust )

}

############################### 
"histResid" <- function ( clust , n1 = 0, n2 = 0) {
    hRes <- numeric()
    if (n1 == 0 && n2 == 0) {
      for (i in 1:clust$k ) {
         hRes[i] <- clust[[i]]$resid
      }
    } else {
      for (i in n1:n2) {
         hRes[i- n1 + 1] <- clust[[i]]$resid
      }
    }
    return( hRes )
}
###############################
histNrows <- function( clust ) {
    NR <- numeric()
    for (i in 1:clust$k ) {
       NR[i] <- clust[[i]]$nrows
    }
    return( NR )
}

###############################
histNcols <- function( clust ) {
    NC <- numeric()
    for (i in 1:clust$k ) {
       NC[i] <- clust[[i]]$ncols
    }
    return( NC )
}

############################################################
"meanRes" <- function (clusters) {
       sumRes <- 0
       for( i in 1:clusters$k ) {
           sumRes <- sumRes + clusters[[i]]$resid
       }
       mRes <- sumRes / clusters$k
       return( mRes )
}
############################################################
"geneInWhichClust" <- function(clusters, gene) {
      isIn <- numeric()
      if ( is.na(gene) ) { return( isIn ) }
      for( i in 1:clusters$k ) {
           inKth <- length( grep(gene,clusters[[i]]$rows) )
           if (inKth > 0) {
              isIn <- c(isIn, i)
           }
       }
       return( isIn )

}
