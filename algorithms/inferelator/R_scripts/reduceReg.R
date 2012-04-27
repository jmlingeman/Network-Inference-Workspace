## May 10th, 2003
##
## reduceReg.R -- produces a vector with regulatory/transFactor or
##                   not 1/0 for each cluster
##                   i.e. does this cluster have regulatory/transFactor 
##                  capabilities


"reduceReg" <- function(pfam.reg, cog.func, hth.reg, clust.members) {

  clust.names <- names(clust.members)

  ## declare reduced vector
   n.clusters <- length( clust.names)
   clust.reg <- vector(mode = "numeric", length = n.clusters)
   names(clust.reg) <- clust.names

   for ( cl.name  in clust.names ){ # Step through clusters

      cat(cl.name,"\n")

      ## remember these are org indexes with the names of this vector what we want
      members <- clust.members[[cl.name]] # cluster members
      n.members <- length(members)
      seenReg <- 0
      seenNonEnz <- 0
      for (mem.name in members ) {
##        for (mem.name in names(members) ) { VT 5-20-03, for ne clust.members structure 
	 cat(cl.name, " " , mem.name,"\t")
         if (pfam.reg[mem.name] == 1) {
             seenReg <- seenReg + 1
         }
         if (hth.reg[mem.name] == 1) {
             seenReg <- seenReg + 1
         }
         if (cog.func[mem.name] == 1.0 ) {
             seenReg <- seenReg + 1
         }
         if (cog.func[mem.name] != -1.0 ) {
             seenNonEnz <- 1
         }
	 cat("non-enz: ", seenNonEnz, " reg: ",pfam.reg[mem.name],  hth.reg[mem.name], cog.func[mem.name], "\n") 
         
      } ## end loop over clust mem
      
      if (seenReg >= 1) {
         #clust.reg[cl.name] <- seenReg
	 clust.reg[cl.name] <- 1
      } else {
         if (seenNonEnz == 0) {
           clust.reg[cl.name] <- -1.0
         }
      }
      cat("clust.reg: ", cl.name, clust.reg[cl.name], "\n")
 
   } ## end loop over clusters


   clust.reg ## return 
   
}

"getTfNames" <- function (pfam.reg, cog.func, hth.reg, gene.ids ) {
      TFs <- character()
      seenReg <- logical()
      i <- 0
      
      for (gene.id in gene.ids) {

         seenReg <- FALSE

         if (pfam.reg[gene.id] == 1) {
             seenReg <- TRUE       
         }
         #if (hth.reg[gene.id] == 1) {
         #    seenReg <- TRUE       
         #}
         if (cog.func[gene.id] == 1.0 ) {
             seenReg <- TRUE           
         }
         if (seenReg) {
            i <- i + 1
            TFs[i] <- gene.id
         }
      }

      cat(i, " TFs detected based on the func files you read in readFunc\n")

      return( TFs )

}

"getTfNames.new" <- function ( file, gene.ids) {
  tfs <- character()


  tmp.data <- read.delim(file, header = F, sep = "\t")
  #tfs <- tmp.data[ tmp.data$V1 %in% gene.ids ]
  tfs <- as.character( tmp.data[ tmp.data$V1 %in% gene.ids, 1 ])
  
  return( tfs )
}

## 2004-03, ISB, Richard Bonneau
## reduce exp and lambda so that they are now averaged over members of a cluster

"reduceExpressionBiclust" <- function (ratios, clusters) {
  nCol <- dim(ratios)[2]
  redExp <- matrix( , nrow = clusters$k , ncol = nCol)    
  colnames(redExp) <- colnames(ratios)
  for (i in 1:clusters$k) {
    ##cat(i,"\n")
    redExp[i,] <- reduceExpression.single.clust( ratios, clusters[[ i ]] )
    ##if ( length( clusters[[i]]$rows ) >  1 ) {
    ##redExp[i,] = apply( ratios[clusters[[i]]$rows , ] , 2, mean )
    ##} else {
    ##redExp[i,] = ratios[clusters[[i]]$rows , ]
    ##}
  } 
  return( redExp )
 }

"reduceExpression.single.clust" <- function (ratios, cluster) {
  redExp <- NULL
  if ( length( cluster$rows ) >  1 ) {
    redExp <- apply( ratios[cluster$rows , ] , 2, mean )
  } else {
    redExp <- ratios[cluster$rows , ]
  }
  names( redExp ) <- colnames( ratios )
  return( redExp )
 }


 "reduceLambdaBiclust" <- function (lambdas, clusters) {
      nCol <- dim(lambdas)[2]
      redLam <- matrix( , nrow = clusters$k , ncol = nCol)
      colnames(redLam) <- colnames(lambdas)
      for (i in 1:clusters$k) {
          if ( length( clusters[[i]]$rows ) >  1 ) {
              redLam[i,] = apply( lambdas[ clusters[[i]]$rows , ] , 2, mean )
          } else {
              redLam[i,] = lambdas[clusters[[i]]$rows , ]
          }
      }
      return( redLam )
 }
 
 "reduceRegBiclust" <- function (clusters, pfam.reg, cog.func, hth.reg , other.genes = "") {
      redFunc <- numeric()
      for (i in 1:clusters$k) {
          seenNonEnz <- 0
          seenReg <- 0
          for (mem.name in clusters[[i]]$rows) {
             if (pfam.reg[mem.name] == 1) {
                 seenReg <- seenReg + 1
             }
             if (hth.reg[mem.name] == 1) {
                 seenReg <- seenReg + 1
             }
             if (cog.func[mem.name] == 1.0 ) {
                 seenReg <- seenReg + 1
             }
             if (cog.func[mem.name] != -1.0 ) {
                 seenNonEnz <- 1
             }
             if (mem.name %in% other.genes) {
                 seenReg <- seenReg + 1
             }
 
          }
          if (seenReg >= 1) {
             redFunc[i] <- 1
          } else {
             if (seenNonEnz == 0) {
               redFunc[i] <- -1.0
             }
          }

      }
      return( redFunc )
}

reduceNetworkBiclust = function (full.mat, clusters ) {

    red.mat <- matrix(0, nrow = clusters$k, ncol = clusters$k )

    for (ii in 1:(clusters$k - 1) ) {
       cat(".")
       for (jj in (ii+1):clusters$k ) { 
             inter.mat <- full.mat[ clusters[[ii]]$rows, clusters[[jj]]$rows ]
             max.n <- (clusters[[ii]]$nrows * clusters[[jj]]$nrows) 
             red.mat[ii,jj] <- sum(inter.mat) / max.n
             red.mat[jj,ii] <- red.mat[ii,jj]
       }
    }
    cat("\n")
    
    
    ## use invisible so we don't puke out a huge matrix to the screen
    ## if return is not to var
    invisible( red.mat )

}

