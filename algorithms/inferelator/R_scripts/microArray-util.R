##
## microArray-util.R
##
## utilities used with the inferelator
## RBonneau, ISB, 2002-2004
################################################################################
"associationMatToGroups" <- function ( aMat )  {

    aMat[aMat == 0] <- 5.0
    aMat[aMat == 1] <- jitter(0.1)
    diag( aMat ) <- 0.0
    a.dist <- distMat.to.distObj( aMat )
   
    a.tree <- hclust(a.dist, method = "single")
    a.groups <- cutree(a.tree, h = 0.3)
    num.a.mem <- numeric()

    #for (i in range(a.groups)[2] ) {
    #    num.a.mem[i] <- length( a.groups[a.groups == i] )
    #}

    rm( a.tree, a.dist)

    return ( a.groups )
}
################################################################################
"noChange" <- function (lambdas, lambdaThresh, mode = "names") {
     ## two modes == names, bin (0/1 vector ... 0 == nochange)
     gene.ids <- rownames( lambdas )
     inOrOut <- vector(mode = "numeric", length = length(gene.ids) )
     names(inOrOut) <- gene.ids
     noChange <- character()
     
     for (gene.id in gene.ids) {
          maxLam <- max(lambdas[gene.id,])
          if (maxLam > lambdaThresh) {
              inOrOut[gene.id] <- 1
          } else {
              inOrOut[gene.id] <- 0
              noChange <- c(noChange, gene.id)
         }
     }
     
     if (mode == "names") {
        return ( noChange )
     } else {
        return ( inOrOut )
     }
}
################################################################################
lambdaFilt <- function (lambdas, lambdaThresh, mode = "names", n.thresh = 2) {
     ## two modes == names, bin (0/1 vector ... 0 == nochange)
     gene.ids <- rownames( lambdas )
     inOrOut <- vector(mode = "numeric", length = length(gene.ids) )
     names(inOrOut) <- gene.ids
     noChange <- character()

     for (gene.id in gene.ids) {
        
        ##for (j in 1:( dim(lambdas)[2] ) ) {
        ##  if (lambdas[gene.id, j] > lambdaThresh) {
        ##      inOrOut[gene.id] <- inOrOut[gene.id] + 1
        ##  }
        ##}
       inOrOut[ gene.id ] <- sum( lambdas[ gene.id, ] > lambdaThresh, na.rm=T )
       
        if (inOrOut[gene.id] < n.thresh ) {
            noChange <- c(noChange, gene.id)
        }
     }

     if (mode == "names") {
        return ( noChange )
     } else {
        return ( inOrOut )
     }

}

lambdaFilt.names <- function (lambdas, lambdaThresh, mode = "names", n.thresh = 2) {     
## two modes == names, bin (0/1 vector ... 0 == nochange)
     gene.ids <- rownames( lambdas )
     inOrOut <- vector(mode = "numeric", length = length(gene.ids) )
     names(inOrOut) <- gene.ids
     noChange <- character()
     for (gene.id in gene.ids) {
        for (j in 1:( dim(lambdas)[2] ) ) {
          if (lambdas[gene.id, j] > lambdaThresh) {
              inOrOut[gene.id] <- inOrOut[gene.id] + 1
          }
        }
        if (inOrOut[gene.id] < n.thresh ) {
            noChange <- c(noChange, gene.id)
        }
     }

     return ( noChange )

}


################################################################################
################################################################################
## the next two functions to reconstruct the actual values
"minExp" <- function( ratios) {
        #min.rat <- numeric()
        #for (cl.name in rownames(ratios) ) {
        #        tailFix.mm <- 0.05 * (max.rat[cl.name] - min.rat[cl.name])
        #        min.rat[cl.name] <- min.rat[cl.name] - tailFix.mm

        #}
        min.rat <- apply(ratios, 2, min)
        names(min.rat) <- rownames(ratios)
        min.rat
}
################################################################################
"maxExp" <- function( ratios) {
        #max.rat <- numeric()
        #for (cl.name in rownames(ratios) ) {
        #        tailFix.mm <- 0.05 * (max.rat[cl.name] - min.rat[cl.name])
        #        max.rat[cl.name] <- max( ratios[cl.name,]) + tailFix.mm
        #}
        max.rat <- apply(ratios, 2, max)
        names(max.rat) <- rownames(ratios)
        max.rat
}
################################################################################
#### columns represent the objects ... like a df going into lm, glm, l1ce, g1lce, etc.
"standardize.col" <- function( xx ) {

     xx.std <- xx
     for (coll in colnames(xx) ) {
          xx.std[,coll] <- ( xx[,coll] - mean(xx[,coll]) ) / sd( xx[,coll] )
     }
     xx.std

}

################################################################################
#### rows represent the objects ...
"standardize.row" <- function( xx ) {

     xx.std <- xx
     for (roww in rownames(xx) ) {
          ##xx.std[roww,] <- ( xx[roww,] - mean(xx[roww,]) ) / var( xx[roww,] )
          xx.std[roww,] <- ( xx[roww,] - mean(xx[roww,]) ) / sd( xx[roww,] )
     }
     xx.std

}
################################################################################
#### rows are genes
"standardize.row.logistic" <- function( xx, delta.xx = 0.05 ) {
      n.row <- nrow( xx )
      n.col <- ncol( xx )
 
      xx.std <- matrix(0 , nrow = n.row, ncol = n.col)
      
      for (i in 1:n.row) {
          out.tmp <- xx[i,]
          slack <- abs(delta.xx * min(out.tmp))
          min.o <- min( out.tmp ) - slack
          max.o <- max( out.tmp ) + slack
          diff.mm <- max.o - min.o
          xx.std[i,]  <- ( out.tmp - min.o ) / diff.mm
      }

      invisible( xx.std )
}


################################################################################
rnd.rows <- function( in.mat ) {
   
    rnd.mat <- in.mat
    for (i in 1:nrow(in.mat) ) {
       rnd.mat[i,] <- sample( in.mat[i,] )
    }
    invisible( rnd.mat )
}
################################################################################
rnd.all <- function( in.mat ) {

  rnd.mat <- sample( in.mat )
  dim(rnd.mat) <- dim(in.mat)
  dimnames(rnd.mat) <- dimnames(in.mat)
  invisible( rnd.mat )
}







