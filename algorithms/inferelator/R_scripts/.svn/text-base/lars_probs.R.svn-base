lars_probs <- function (x, y, type = c("lasso", "lar", "forward.stagewise", 
    "stepwise"), trace = FALSE, normalize = TRUE, intercept = TRUE, 
    Gram, eps = .Machine$double.eps, max.steps, use.Gram = TRUE, prior.prob) 
{
    call <- match.call()
    type <- match.arg(type)
    TYPE <- switch(type, lasso = "LASSO", lar = "LAR", forward.stagewise = "Forward Stagewise", 
        stepwise = "Forward Stepwise")
    if (trace) 
        cat(paste(TYPE, "sequence\n"))
    nm <- dim(x)
    n <- nm[1]
    m <- nm[2]
    im <- inactive <- seq(m)
    one <- rep(1, n)
    vn <- dimnames(x)[[2]]
    if (intercept) {
        meanx <- drop(one %*% x)/n
        x <- scale(x, meanx, FALSE)
        mu <- mean(y)
        y <- drop(y - mu)
    } else {
        meanx <- rep(0, m)
        mu <- 0
        y <- drop(y)
    }
    if (normalize) {
        normx <- sqrt(drop(one %*% (x^2)))
        nosignal <- normx/sqrt(n) < eps
        if (any(nosignal)) {
            ignores <- im[nosignal]
            inactive <- im[-ignores]
            normx[nosignal] <- eps * sqrt(n)
            if (trace) 
                cat("LARS Step 0 :\t", sum(nosignal), "Variables with Variance < eps; dropped for good\n")
        }
        else ignores <- NULL
        names(normx) <- NULL
        x <- scale(x, FALSE, normx)
    } else {
        normx <- rep(1, m)
        ignores <- NULL
    }
    if (use.Gram & missing(Gram)) {
        if (m > 500 && n < m) 
            cat("There are more than 500 variables and n<m;\nYou may wish to restart and set use.Gram=FALSE\n")
        if (trace) 
            cat("Computing X'X .....\n")
        Gram <- t(x) %*% x
    }
    Cvec <- drop(t(y) %*% x)
    ssy <- sum(y^2)
    residuals <- y
    if (missing(max.steps)) 
        max.steps <- 8 * min(m, n - intercept)
    beta <- matrix(0, max.steps + 1, m)
    lambda = double(max.steps)
    Gamrat <- NULL
    arc.length <- NULL
    R2 <- 1
    RSS <- ssy
    first.in <- integer(m)
    active <- NULL
    actions <- as.list(seq(max.steps))
    drops <- FALSE
    Sign <- NULL
    R <- NULL
    k <- 0
    curr.results <- NULL
   
    while ((k < max.steps) & (length(active) < min(m - length(ignores), 
        n - intercept))) {
	#cat("ITERATION #: ",k,"\n") ##greeny 
	#cat("R-dim:\t")
	#cat(dim(R),"\n")
        
	action <- NULL
        C <- Cvec[inactive]  ##vector of the correlations that has cors only for not-yet-incorp predictors

	#cat("inactive:",inactive,"\n") ##greeny

	#incorporation of prior probabilities 
	if(any(prior.prob != 1)){
	    #cat("\nprior: ", prior.prob,"\n") ##greeny
	    if(any(drops)){
	    	#cat("in drops with dropid:",dropid,"\n")
	    	seq1 <- which(inactive<dropid)
	    	end <- length(prior.prob)	    	
	    	if(! (length(seq1)+1)>end )
	    		seq2 <- (length(seq1)+1):end
    		else
    			seq2 <- c()
	    	#cat("concatantae from:", seq1,"\n")
	    	#cat("concatantae to:", seq2,"\n")
	    	prior.prob <- c( prior.prob[seq1], 1,
		   					 prior.prob[seq2] )
	    	#cat("priors after add:",prior.prob,"\n")
	    } else {
	    	#cat("in regular\n")
	    }
	      	    	
		# cat( "\n inactive: ", inactive, "\n") ##greeny

		#cat("x exists and is", x )##greeny
		corvec <- cor.prob(cor(x[,inactive],y),nrow(x)-2)   ## the vector of correlations based on the not-yet-incorporated predictors
		# cat("corvec ",corvec,"\n") ##greeny
	    #cat( "dim prior: ", dim(as.matrix(prior.prob)), " dim corvec ", dim(corvec), "\n") ##greeny
		newcorvec <- log(t(corvec))+ log(prior.prob) ## need to take transpose due to	
		##internal structure that R gives to the vector
		
		#extracting the min value and corresponding location
		#min_val_and_loc <- min_w_ind(newcorvec, active) #change to which.min ag
		#pmin <- min_val_and_loc[1]
		loc <- which.min(newcorvec) #min_val_and_loc[2]
		pmin <- newcorvec[loc]	

		#cat("newcorvec ", newcorvec, "\n", "min ",pmin, " loc ", loc, "\n") ##greeny

	    #new <- loc
		boolvec <- matrix(FALSE,1,length(C))
		#boolvec[1:length(C)] <- FALSE
		boolvec[loc] <- TRUE
		
		#emulating what original lars does to determine the "new" vector
		new <- boolvec
		#cat("boolvec ",boolvec,"\n \n")
			
		Cmax <- abs(C[loc])
	    } else {
		Cmax <- max(abs(C))
            }
	
	#cat("Cmax ",Cmax,"\n")
        
	if (Cmax < eps * 100) {
			      if (trace)
                cat("Max |corr| = 0; exiting...\n")
            break
        }
        k <- k + 1
        lambda[k] = Cmax
        if (!any(drops)) {
            #decision of what to add occurs in the following lines
	    	    
	    if(any(prior.prob != 1)){
	        ## AM # set priors removing one chosen for active
			prior.prob <- prior.prob[!new]
        } else {
        	new <- abs(C) >= Cmax - eps
       	}
	    #cat( "\n bool val of new: ", new) ##greeny
	    C <- C[!new]   #look st thid greeny
	    new <- inactive[new]
	    #cat("  ind val of new ", new,"\n")  ##greeny	    

	  ## lars.results[rowind,k] <- new    ## greeny
           
	    for (inew in new) {
                if (use.Gram) {
                  R <- updateR(Gram[inew, inew], R, drop(Gram[inew, 
                    active]), Gram = TRUE, eps = eps)
                }
                else {
                  R <- updateR(x[, inew], R, x[, active], Gram = FALSE, 
                    eps = eps)
                }
                if (attr(R, "rank") == length(active)) {
                  nR <- seq(length(active))
                  R <- R[nR, nR, drop = FALSE]
                  attr(R, "rank") <- length(active)
                  ignores <- c(ignores, inew)
                  action <- c(action, -inew)
                  if (trace) 
                    cat("LARS Step", k, ":\t Variable", inew, 
                      "\tcollinear; dropped for good\n")
                }
                else {
                  if (first.in[inew] == 0) 
                    first.in[inew] <- k
                  active <- c(active, inew)
                  Sign <- c(Sign, sign(Cvec[inew]))
                  action <- c(action, inew)
                  if (trace) 
                    cat("LARS Step", k, ":\t Variable", inew, 
                      "\tadded\n")
                }
            }
        }
        else action <- -dropid
        Gi1 <- backsolve(R, backsolvet(R, Sign))
        dropouts <- NULL
        if (type == "forward.stagewise") {
            directions <- Gi1 * Sign
            if (!all(directions > 0)) {
                if (use.Gram) {
                  nnls.object <- nnls.lars(active, Sign, R, directions, 
                    Gram[active, active], trace = trace, use.Gram = TRUE, 
                    eps = eps)
                }
                else {
                  nnls.object <- nnls.lars(active, Sign, R, directions, 
                    x[, active], trace = trace, use.Gram = FALSE, 
                    eps = eps)
                }
                positive <- nnls.object$positive
                dropouts <- active[-positive]
                action <- c(action, -dropouts)
                active <- nnls.object$active
                Sign <- Sign[positive]
                Gi1 <- nnls.object$beta[positive] * Sign
                R <- nnls.object$R
                C <- Cvec[-c(active, ignores)]
            }
        }
        A <- 1/sqrt(sum(Gi1 * Sign))
        w <- A * Gi1
        if (!use.Gram) 
            u <- drop(x[, active, drop = FALSE] %*% w)
        if ((length(active) >= min(n - intercept, m - length(ignores))) | 
            type == "stepwise") {
            gamhat <- Cmax/A
        }
        else {
            if (use.Gram) {
                a <- drop(w %*% Gram[active, -c(active, ignores), 
                  drop = FALSE])
            }
            else {
                a <- drop(u %*% x[, -c(active, ignores), drop = FALSE])
            }
            gam <- c((Cmax - C)/(A - a), (Cmax + C)/(A + a))
            gamhat <- min(gam[gam > eps], Cmax/A)
        }
        if (type == "lasso") {
            dropid <- NULL
            b1 <- beta[k, active]
            ## AM 
            #cat("b1:",b1)
            z1 <- -b1/w
            zmin <- min(z1[z1 > eps], gamhat)
            #cat("z1:",z1,"\n")
            #cat("z1[z1 > eps]:",z1[z1 > eps],"\n")
            if (zmin < gamhat) {
                gamhat <- zmin
                drops <- z1 == zmin
            }
            else drops <- FALSE
        }
        beta[k + 1, ] <- beta[k, ]
        beta[k + 1, active] <- beta[k + 1, active] + gamhat * 
            w
        if (use.Gram) {
            Cvec <- Cvec - gamhat * Gram[, active, drop = FALSE] %*% 
                w
        }
        else {
            residuals <- residuals - gamhat * u
            Cvec <- drop(t(residuals) %*% x)
        }
        Gamrat <- c(Gamrat, gamhat/(Cmax/A))

	#cat("A: ", A," \na: ",a, "\nw: ", w)	##greeny
	#cat("\n Cmax: ",Cmax, " C",C)    ##greeny
	#cat("\n active: ", active, "\n inactive: ", inactive, "\ngam: ", gam, "\n gamhat: ", gamhat, "\n final cvec for iteration: ", Cvec,
     #     "\n gamrat", Gamrat, "\n ------------------------------- \n \n") ##greeny

        arc.length <- c(arc.length, gamhat)
        if (type == "lasso" && any(drops)) {
        	## AM
        	#cat("drops:",drops,"\n")
            dropid <- seq(drops)[drops]
            #cat("dropid1:",dropid,"\n")
            for (id in rev(dropid)) {
                if (trace) 
                  cat("Lasso Step", k + 1, ":\t Variable", active[id], 
                    "\tdropped\n")
                R <- downdateR(R, id)
            }
            dropid <- active[drops]
            #cat("dropid1:",dropid,"\n")
            beta[k + 1, dropid] <- 0
            active <- active[!drops]
            Sign <- Sign[!drops]
        }
        if (!is.null(vn)) 
            names(action) <- vn[abs(action)]
        actions[[k]] <- action
        inactive <- im[-c(active, ignores)]
        if (type == "stepwise") 
            Sign = Sign * 0
    } # end od large loop greeny
    beta <- beta[seq(k + 1), , drop = FALSE]
    lambda = lambda[seq(k)]
    dimnames(beta) <- list(paste(0:k), vn)
    if (trace) 
        cat("Computing residuals, RSS etc .....\n")
    residuals <- y - x %*% t(beta)
    beta <- scale(beta, FALSE, normx)
    RSS <- apply(residuals^2, 2, sum)
    R2 <- 1 - RSS/RSS[1]
    actions = actions[seq(k)]
    netdf = sapply(actions, function(x) sum(sign(x)))
    df = cumsum(netdf)
    if (intercept) 
        df = c(Intercept = 1, df + 1)
    else df = c(Null = 0, df)
    rss.big = rev(RSS)[1]
    df.big = n - rev(df)[1]
    if (rss.big < eps | df.big < eps) 
        sigma2 = NaN
    else sigma2 = rss.big/df.big
    Cp <- RSS/sigma2 - n + 2 * df
    attr(Cp, "sigma2") = sigma2
    attr(Cp, "n") = n

   # return(lars.results)

    object <- list(call = call, type = TYPE, df = df, lambda = lambda, 
        R2 = R2, RSS = RSS, Cp = Cp, actions = actions[seq(k)], 
        entry = first.in, Gamrat = Gamrat, arc.length = arc.length, 
        Gram = if (use.Gram) Gram else NULL, beta = beta, mu = mu, 
        normx = normx, meanx = meanx)
    class(object) <- "lars"
    object
}
