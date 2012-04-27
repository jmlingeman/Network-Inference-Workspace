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

clr <- function(m) {
	repmat <- function(a,n,m) {kronecker(matrix(1,n,m),a)}
	ur = apply(m, 1, mean)
	uc = apply(m, 2, mean)
	sr = apply(m, 1, sd)
	sc = apply(m, 2, sd)
	
	uc = t(repmat(uc, 1, nrow(m)))
	sc = t(repmat(sc, 1, nrow(m)))
	
	z1 = (m - ur) / sr
	
	z2 = (m - uc) / sc
	
	z1[which(z1 < 0)] = 0
	z2[which(z2 < 0)] = 0
	
	z = sqrt(z1 ^ 2 + z2 ^ 2) 
	
	return(z)
}


mixed_clr <- function(m1,m2) {
	repmat <- function(a,n,m) {kronecker(matrix(1,n,m),a)}
	predNum = dim(m2)[1]
	Z = numeric()
	for (ix in 1:predNum){
		if (ix==1){
# /we predict the first one so it is advanced the rest is lagged
			m = m2[1,]
			m = rbind(m,m1[2:predNum,])
		} else if (ix==predNum){
# /we predict the last one so it is advanced the rest is lagged
			m = m1[1:(predNum-1),]
			m = rbind(m,m2[predNum,])
		} else {
# /we predict the middle one so it is advanced the rest is lagged
			m = m1[1:(ix-1),]
			m = rbind(m,m2[ix,])		
			m = rbind(m,m1[(ix+1):predNum,])				
		}
		clr_mat = clr(m)
		rownames(clr_mat) = rownames(m2)
		colnames(clr_mat) = colnames(m2)	
		Z = rbind(Z,clr_mat[ix,])# add predicted row results
	}
	return(Z)
}

# calc MI matrix in batches
calc_MI_inBatces <- function(Y,X,btch_size) {
	M = matrix(0,nrow=nrow(Y),ncol=nrow(X))
	x= nrow(Y)
	y=1
	cat("mi calculation for: ")
	while(x > btch_size){ 
		cat(y,":",(y+(btch_size-1)),", ", sep="") ; 	flush.console()
		M[y:(y+(btch_size-1)),] = mi(Y[y:(y+(btch_size-1)),], X)
		x = x - btch_size
		y=y+btch_size
	}
	cat(y,":",(y+x-1),"\n", sep="") ; 	flush.console()
	M[y:(y+x-1),] = mi(Y[y:(y+x-1),], X)
	return(M)
}


# like calc MI one by one but parallalized
calc_MI_one_by_one_parallel <- function(Y, X, Pi_s_clr, processorsNumber = 1){
	M = matrix(0,nrow=nrow(Y),ncol=nrow(X))
	ind = as.vector(c(1:nrow(Y)))
	x = mclapply(ind, mi_parllel,Y[ind, ], X, Pi_s, mc.cores=processorsNumber)
	for (i in 1:length(x)) {
		M[i,] = x[[i]]
	}
	return(M)
}

mi_parllel <- function(ix, Y_ind, X, Pi_s, sigma){
	vec = mi(t(as.matrix(Y_ind[ix, ])), X )
	cat(".")
	return(vec)
}
