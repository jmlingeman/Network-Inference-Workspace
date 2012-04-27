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

#creates a permutation matrix where we assume that every cluster has every condition
#we call this Pi_g for global permutation
#INPUT:
#1) inNumCols - the number of columns in the dataset
#2) percentCoverage - how many of the columns in the dataset do you potentiall wanted to be incorporated
#   -theoritically, if all of the columns are supplied, random sampling will converge to 80% coverage of the dataset
#   -so if 80% of the columns are supplied, random sampling will converge to 64% coverage of the initial dataset
create_Pi_g <- function( inNumCols, percentCoverage ){
  numCols <-  inNumCols
 
  
  ##change numCols to be the number of columns that we want to resample on: ie if we only want to
  ##resample on half of the columns, percent coverage would be 50
  
  numCols <- floor(numCols*percentCoverage/100)
  
  permutMat <- matrix(,numCols, numCols)
  
  for( i in 1:numCols ){
    permutThis <- c(1:numCols)
    permutMat[ ,i ] <-  sample( x <- permutThis, size <- numCols, replace <- FALSE) 
  } 
  
  return( permutMat )
  
}
######################################
# AM I have fixed this function to work for all permutations (look for my comments AM)
create_Pi_s <- function( clusterStack, allConds, permutMat, percentCoverage ){
	create_PiSComments <- function(){ 
		#clusConds is the names of the conditions in the cluster
		#allConds is the names of the conditions in the responseMat (ie. all conditions)
		#permutMat is the matrix of permutations
		#goal is to pick a random permutation of the condtions of the responseMatrix such that all of these conditions also exist in the bicluster
		#steps:
		# 1) remove any conditions in the cluster that are not in the response matrix (allConds)
		# 2) pick the first random premutation (first row of permutMat)
		# 3) check to see which of the conditions picked above are not in the cluster, 
		# 4)  record the number of missing conditions, and their location in the permutation
		# 5) proceed to the next row of permutMat
		# 6) find the conditions from this permutation which are in the bicluster
		# 7) repeat steps 4,5 until either there are as many new conditions picked as missing conditions found in step 2, or until the permutMat is exhausted (shouldnt happen!!!) 
	}
	
	Pi_s <- matrix(, length(clusterStack)-1, length(allConds))
	
	#loop  through entire cluster stack
	for( ind in 1:(length(clusterStack)-1)){
		#ind denotes which bicluster we are on
		#  cat("ind is ", ind,"\n")
		csNames <- clusterStack[[ind]]$cols
		#  cat( "colnames ",length(csNames),"\n")
		
		#removing columns which are not in responseMat 
		#AM  csNames <- csNames[which(csNames %in% allConds)]
		csNames <- allConds[which(allConds %in% csNames)]
		#    cat( "colnames, init removed ", length(csNames),"\n" )
		
		#deciding how many columns to have based on percent resampling
		numCols <-floor( length(csNames)*percentCoverage/100 )
		#tempPermMat <- permutMat[ ,which( csNames %in% allConds)][,1:50]
		#tempPermMat <- permutMat[ , which(csNames %in% allConds) ][,1:numCols]
		tempPermMat <- permutMat[ , which(allConds %in% csNames) ][,1:numCols] #AG
		
		#we take the first row of the permutation matrix Pi_s as the indices of our permuted bicluster
		#if any of the conditions are not in the cluster, we systematically go through Pi_s until we find conditions
		#that are in the cluster
		randPermut <- tempPermMat[1,]
		
		##now take permutations only to the length of the cluster
		Pi_s[ ind, which(allConds %in% csNames)] <-  randPermut  #[ which(allConds[randPermut] %in% csNames)]
		
		notInClust <- which( !allConds[randPermut] %in% csNames)
		permRow <- 2
		#as long as some of our resampled columns are not in the blcuster pick new ones
		while( (length(notInClust) > 0) && (nrow(tempPermMat) >= permRow)){  #we stop if we have exhausted our list of permutations (this shouldnt happen unless we are getting the originals! )
			for( i in 1:length(notInClust)){
				randPermut[ notInClust[i] ] <- tempPermMat[ permRow, notInClust[i]]
			}
			permRow <- permRow + 1
			notInClust <- which( !allConds[randPermut] %in% csNames)
			
			Pi_s[ ind, which(allConds %in% csNames)] <-  randPermut
		} 
	}
	
	##now we have the randomPermutation, it is in randPermut
	return(Pi_s)
}
######################################
create_Xpi <- function( Y_pi, allConds, cnamesDesign){
  X_pi <- matrix(,nrow(Y_pi),ncol(Y_pi))
  for( i in 1:nrow(Y_pi)){
    notNA <-  Y_pi[i, which(!is.na(Y_pi[i,]))]
    X_pi[i, ]
    
  }
  return(X_pi)
}

######################################
#quick function to permute the columns, given permutation matrix, works only if we have all singles genes
permuteCols <- function( M, P ){ #works only for non-bicluster case, where evyerbody has same permutation
	for (i in 1:nrow(P))
		M[i,] = M[i,P[i,]]
	return(M)
}
######################################
#create the matrix of permutations that we should use if we have biclusters
#INPUT
#1) cS - clusterStack, contains information of which conditions are in which bicluster
#2) allConds - character vector containing the names of all of the conditions
#3) getOrigPerm - boolean denoting whether we want to get the original order
#4) percentCovereage - means same thing as above - how many of the conditions do we want incorporated
#OUTPUT
#1) matrix that contains the permuted order of columns for each bicluster
createPermMatrix <- function(cS, allConds, getOrigPerm, percentCoverage = 100) {
	if( getOrigPerm ){
		Pi_g = matrix(rep( c(1:length(allConds)), 2), 2, length(allConds), byrow = TRUE )
	}else{ #not using original order
		if( is.null(INPUT$general$redExp) ){ #not using biclusters
			Pi_g = t(apply( matrix(rep( c(1:length(allConds)),5),5,length(allConds), byrow=TRUE),1,sample,length(allConds),T))	
		}
		else{
			#we have biclusters
			Pi_g = create_Pi_g(length(allConds), percentCoverage)
		}
	}
	
	# create bicluster specific permutation matrix (ie. read from Pi_g, algorithm described in method comments)
	Pi_s = create_Pi_s(  INPUT[["general"]][["clusterStack"]], allConds = allConds, permutMat = Pi_g, percentCoverage = percentCoverage) 
	#Pi_s = matrix(rep(Pi_g[1,],nrow(Y_lars)), nrow = nrow(Y_lars),byrow = TRUE)
	return(Pi_s)
}




















