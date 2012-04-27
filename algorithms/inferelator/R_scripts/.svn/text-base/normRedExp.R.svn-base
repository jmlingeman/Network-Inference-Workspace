# june 2003 ## not nececsarily a permenant part of the code
#           ## but needed if we keep using glm with ratios ... 
#          
# normRedExp.R --- normalizes ratio values to 0-1 for glm
#    ... actually it's norm 0-1 :: 0.5-0.95 ... a little slop so we don't
#    ... screw up the glm fit (0 and 1 at asymtotic extreems, bad)
#

# we would have to normalize absolute values as well ... but this script
# expects log(ratios) which can have negative values
"normExp" <- function( ratios) {

   normRedExp <- ratios
   clust.names <- rownames(ratios)

   n.conds <- ncol(ratios)
   n.clusters <- length(clust.names)

   min.rat <- numeric()
   max.rat <- numeric()
 
   for (cl.name in clust.names) {
      min.rat[cl.name] <- min( ratios[cl.name,]) 
      max.rat[cl.name] <- max( ratios[cl.name,])
      tailFix.mm <- 0.05 * (max.rat[cl.name] - min.rat[cl.name])
      min.rat[cl.name] <-  min.rat[cl.name]  - tailFix.mm
      max.rat[cl.name] <-  max.rat[cl.name]  + tailFix.mm
      normRedExp[cl.name,] <- ( ratios[cl.name,] - min.rat[cl.name]) / (max.rat[cl.name] - min.rat[cl.name])
   }
   # return normalized matrix
   normRedExp  

}

## the next two functions to reconstruct the actual values
"minExp" <- function( ratios) {
	min.rat <- numeric()
        for (cl.name in rownames(ratios) ) {
                tailFix.mm <- 0.05 * (max.rat[cl.name] - min.rat[cl.name])
		min.rat[cl.name] <- min.rat[cl.name] - tailFix.mm 

        }
        min.rat
}

"maxExp" <- function( ratios) {
        max.rat <- numeric()
        for (cl.name in rownames(ratios) ) {
                tailFix.mm <- 0.05 * (max.rat[cl.name] - min.rat[cl.name])
                max.rat[cl.name] <- max( ratios[cl.name,]) + tailFix.mm
        }
        max.rat
}

#### columns represent the objects ... like a df going into lm, glm, l1ce, g1lce, etc.
#"standardize.col" <- function( xx ) {

#     xx.std <- xx
#     for (coll in colnames(xx) ) { 
#          xx.std[,coll] <- ( xx[,coll] - mean(xx[,coll]) ) / sd( xx[,coll] )
#     }
#     xx.std     

#}


##### rows represent the objects ... 
#"standardize.row" <- function( xx ) {

#     xx.std <- xx
#     for (roww in rownames(xx) ) { 
#          xx.std[roww,] <- ( xx[roww,] - mean(xx[roww,]) ) / sd( xx[roww,] )
#     }
#     xx.std     

#}


## turns square network/association matrix into grous
## with cheep hclust trick
## ripped off from operon-centric clusterer
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

### return list of guys with little change
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









