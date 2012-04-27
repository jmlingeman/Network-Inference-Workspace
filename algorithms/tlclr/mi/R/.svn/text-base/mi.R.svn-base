mi <- function(x,y=NULL, n = 10, k = 3, trace = FALSE) {
	
	if(!is.matrix(x))
		stop ("'x' must be a matrix")
	if(!is.null(y) & !is.matrix(y))
		stop ("'y' must be a matrix")		
	if (trace)
		cat("Beginning Mutual Information calculation")
	
	if (is.null(y)) {
		g <- dim(x)[1]
		data <- rbind(x,x) # this is for all against all 
	} else {
		g <- dim(x)[1]
		if (dim(y)[2] == dim(x)[2])
			data <- rbind(x,y)
		else if(dim(y)[1] == dim(x)[2])
			data <- rbind(x,t(y))
		else stop("'y' must have the same number of rows or columns as the number of rows in 'x'")
	}
	
	x <- c(t(data))
	# Rmi(float *m, float *z, int *numVar, int *numSamp, int *n, int *k, int *g) {			
	#	-float *m		--Data to run mutual information on. A double vector in R.
	#	-float *z     --Variable for return. A double vector in R.
	#	-int *numVar  --Number of variables (genes) in the data. An int in R.
	#	-int *numSamp --Number of conditions in the data. An int in R.
	#	-int *g	      --Number of non-predictors in the data. An int in R.
	#	-int *n	      --Number of bins. An int in R.
	#	-int *k	      --Spline order. An int in R.
	y <- .C('Rmi',
			  as.single(x), 
			  y = as.single(double( g*(nrow(data)-g) )), 
			  as.integer(nrow(data)), 
			  as.integer(ncol(data)), 
			  as.integer(n), 
			  as.integer(k), 
  			  as.integer(g))$y

		
			
	mi = matrix(y, nrow = g, byrow = T)
	if (trace)
		cat("returning MI matrix")		
	return(mi)
}