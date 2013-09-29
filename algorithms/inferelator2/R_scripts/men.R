require('elasticnet')

callMEN <- function(ind, Xs, Y, Pi, clr.mat, nS,
                    nCv = 10,lambda = 0,
                    sparseModels=T,
                    verbose = FALSE,
                    plot.it = FALSE,
                    plot.file.name = NULL,
                    weights.mat=weights.mat,
                    no.pr.val = no.pr.val,
                    use_interactions = FALSE,
                    max.steps = 0){

  in.weights = weights.mat[ind, ]

  #since we do cross-validation, we
  #need to get only the unique condtions
  uniqConds <- Pi[ind, ]
  uniqConds <- uniqConds[!duplicated(uniqConds) & uniqConds != 0 & !is.na(uniqConds)]

  #Y     <- t(scale(t(Y[, Pi[ind, uniqConds]])))
  Y     <- Y[, Pi[ind, uniqConds]]
  #Y     <- Y[, Pi[ind, uniqConds]]
  Y_ind <- Y[ind, ]

  #Xs <- t(scale(t(Xs[, Pi[ind, uniqConds]])))#
  Xs <- Xs[, Pi[ind, uniqConds]]
  #Xs <- Xs[, Pi[ind, uniqConds]]

  #setting which predictors we consider

  pp  <- rep(F, nrow(Xs))
  clr.row <- clr.mat[ind, ]

  # send self predictor to end of list if gene we are inferring on is a TF
    # also remove as prior if present
    if (ind <= nrow(Xs)) {
      clr.row[ind] <- -Inf
      in.weights[ind] <- no.pr.val
    }

  clr.order <- order(clr.row, decreasing=TRUE)
    have.priors <- which(in.weights != no.pr.val)

    # potential predictors are the union of predictors with priors and the top
    # nS ones based on clr matrix
    pp[unique(c(have.priors, clr.order[1:nS]))] <- TRUE

  #creating the design matrix
  Xs.priors  	  			<- Xs[pp, ,drop=F]
  rownames(Xs.priors) <- which(pp == TRUE)

  #getting the prior weights for the elements of the design matrix
  in.weights.vec 			  <- in.weights[pp]
  names(in.weights.vec) <- rownames(Xs.priors)

  if(plot.it){
    plot.new()
    title(paste("CV curves for target gene ", ind, sep = ""))
  }

  #calculating the MEN models for all chosen values
  #of lambda
  if (verbose)
    cat("Model for target ", ind, "\n")

  adaEnet.res <- cvAdaptiveEnetAll(Xs.priors, Y_ind, k.folds = nCv, lambda.vec = lambda,
      s.fraction = c( seq(from = 0, to = 0.1, length = 100), seq(from = 0.11, to = 1, length = 90)),
      weights.type="other", in.weights.vec = in.weights.vec, trace = FALSE,
      plot.it = plot.it, se = TRUE, max.steps=max.steps)#, sweep.out=sweep.out, run.van = run.van)#, no.pr.val = no.pr.val)


  #finding the parameters which minimize the cross-validation (cv) error
  which.err 	<- "error.cv.min"  #minimum cv error
  which.coefs <- "coefs.cv.min"  #coefficients at the minimum cv error
  if (sparseModels) {
    #error and coefs one standard deviation to the left
    #of min cv error (results in sparser model)
    which.err   <- "error.sparse"
    which.coefs <- "coefs.sparse"
  }

  models.errors <- unlist(lapply(adaEnet.res, function(i) i[[which.err]]))
  min.model     <- which.min(models.errors)

  models.coefs  <- adaEnet.res[[min.model]][[which.coefs]]
  not.zero      <- models.coefs != 0

  if (verbose) {
    cat("\nCV-min errors are: ", sapply(adaEnet.res, function(i) i$"error.cv.min"), "\n")
    cat("Sparse errors are: ", sapply(adaEnet.res, function(i) i$"error.sparse"), "\n\n")
  }

  #creating matrix to store the output
  beta.mat = matrix(0,ncol=6,nrow=0)
  colnames(beta.mat) = c("trgt","tf","beta","prd_xpln_var","min.l2","bias")

  refit.model   <- NULL
  if (!any(not.zero)) { #if we have a null model, we return the empty beta.mat
    return(list(ind=ind, pp=TRUE, betas=0, betas.resc=0))
    obj <- list()
    obj[["priors"]] <- list(ind = ind, bM = beta.mat, allLambdaRes = adaEnet.res)
    return(obj)
  }

  #reffiting betas
  x 				 <- refitModels(Y_ind, t(Xs.priors[names(models.coefs[not.zero]), ,drop = F]), models.coefs[not.zero], center = F)
  betas.ref  <- x$betas
  bias       <- x$bias
  rm(x)

  #rescaling betas
  betas.resc <- rescaleModels(Y_ind, t(Xs.priors[names(betas.ref), ,drop = F]), betas.ref, center = F, use.ev = F)

  for (i in 1:length(betas.resc))
    beta.mat <- rbind(beta.mat, c(ind, as.numeric(names(betas.ref)[i]), as.numeric(betas.ref[names(betas.resc)[i]]),
                      betas.resc[i], as.numeric(names(models.errors)[min.model]), bias))

  return(list(ind=ind, pp=beta.mat[, 'tf'], betas=beta.mat[, 'beta'], betas.resc=beta.mat[, 'prd_xpln_var']))

  obj <- list()
  obj[["priors"]] <- list(ind = ind, bM = beta.mat, allLambdaRes = adaEnet.res)
  return(obj)
}


refitModels <- function(y, x, in.coefs, center = F) {
  if (center == T) {
    #cat("centering predictors and response prior to refitting \n")
    n 		<- dim(x)[1]
    one 	<- rep(1, n)
    meanx <- drop(one %*% x)/n
    x		  <- scale(x, meanx, FALSE)
    normx <- sqrt(drop(one %*% (x^2)))
    x		  <- scale(x, FALSE, normx)

    #scale Y by its mean
    y <- drop(y - mean(y))
  }

  refit.model 			 <- lm(y ~ x)
  refit.coefs        <- refit.model$coefficients[-1]
  names(refit.coefs) <- names(in.coefs)
  bias				       <- refit.model$coefficients[1]

  return(list(betas = refit.coefs, bias = bias))
}

rescaleModels <- function(y, x, model.coefs, center = F, use.ev = F) {
  #not.zero <- model.coefs != 0

  if (center) {
    #cat("centering predictors and response prior to refitting \n")
    n 		<- dim(x)[1]
    one 	<- rep(1, n)
    meanx <- drop(one %*% x)/n
    x		  <- scale(x, meanx, FALSE)
    normx <- sqrt(drop(one %*% (x^2)))
    x		  <- scale(x, FALSE, normx)

    #scale Y by its mean
    y <- drop(y - mean(y))
  }

  refit.func <- refitBySSE
  if (use.ev)
    refit.func <- PredErrRed

  betas.resc    		<- refit.func(y, x, model.coefs)
  names(betas.resc) <- names(model.coefs)#[which(not.zero)]

  return(betas.resc)
}

refitBySSE <- function(Y, X, orig.coefs){
  if (!any(orig.coefs != 0))
    return(rep(0, length(orig.coefs)))

  non.zero.preds <- names(which(orig.coefs != 0))
  print("X AND Y")
  print(X)
  print(Y)
  lin.model <- lm(Y ~ X[, non.zero.preds, drop=FALSE])

  coefs <- lin.model$coefficients[-1]
  bias <- lin.model$coefficients[1]
  names(coefs) <- names(which(orig.coefs != 0))

  resids <- lin.model$residuals
  #modVals <- bias + X[,non.zero.preds,drop=F]%*%as.matrix(coefs) #calculating the resultant model
  #----------how much error is explained by the model-------------#
  YHat.SSE <- sum(resids^2)
  y.SSE <- sum((Y-mean(Y))^2)#sum(Y^2)
  rel.model.err <- YHat.SSE/y.SSE #this measures how much better the model is than the
                                  #the null model
  #----------done calculating the amount of error explained by the model-------------------#
  names(coefs) <- non.zero.preds

  #-----------calculate contribution of each beta-----------------------#
  #---this is done by dividing the residual of the full model-----------#
  #---by the residual of the model without a particular beta------------#
  beta.contrib <- vector("numeric",length(coefs))
  names(beta.contrib) <- non.zero.preds

  for(m.comp in names(beta.contrib)){
    in.model <- non.zero.preds[-which(non.zero.preds == m.comp)]
    if(length(beta.contrib) == 1){
      cur.y.hat <-  bias
    }else{
      cur.y.hat <-  bias*rep(1,nrow(X)) + X[,in.model,drop=F]%*%as.matrix(coefs[in.model])
      }
    cur.y.hat.sse <- sum((Y - cur.y.hat)^2)
    beta.contrib[m.comp] = 1 - (YHat.SSE/cur.y.hat.sse)
  }

  object <- list()
  object[["coefs"]] <- coefs
  object[["bias"]] <- bias
  object[["resids"]] <- resids
  object[["relError"]] <- rel.model.err
  object[["SSR"]] <- YHat.SSE
  object[["SST"]] <- y.SSE
  object[["beta.contrib"]] <- beta.contrib

  #return(object)
  return(beta.contrib)
}


#wrapper function which runs cvAdaptiveEnet vanilla version, with sweep.out, and with priors
#assumes priors and sweep.out are both not null
cvAdaptiveEnetAll <- function(x, y, k.folds = 10, in.folds = NULL, lambda.vec = c(0), nCv=10, s.fraction = seq(from = 0, to = 1, length = 100), weights.type = "other",
    in.weights.vec = NULL,trace = FALSE, plot.it = FALSE, se = TRUE, max.steps=0) {#, no.pr.val = 1){

  #create one CV split to use for running elastic net
  if( is.null(in.folds)){
    all.folds <- split(sample(1:length(y)), rep(1:k.folds, length = length(y)))#cv.folds(length(y), k.folds)
  }else{
    all.folds = in.folds
  }
  x.priors <- cvAdaptiveEnet(t(x), y, k.folds = k.folds, lambda.vec = lambda.vec, s.fraction = s.fraction,# no.pr.val = no.pr.val,
                             weights.type = weights.type, in.folds=all.folds,in.weights.vec = in.weights.vec, trace = FALSE, plot.it = plot.it, se = TRUE, max.steps=max.steps)

  return(x.priors)
}


cvAdaptiveEnet <- function(x, y, k.folds = 10, lambda.vec = c(0),
                           s.fraction = seq(from = 0, to = 1, length = 100), weights.type = "other", in.folds=NULL,
                           in.weights.vec = NULL,trace = FALSE, plot.it = TRUE, se = TRUE, max.steps=0) {

  all.preds.names <- colnames(x)
  if( is.null(in.weights.vec) || (length(in.weights.vec) != ncol(x)) ){
    in.weights.vec <- rep(1,length=ncol(x))
  }
  names(in.weights.vec) <- all.preds.names

  if(is.null(in.folds)){
    all.folds <- split(sample(1:length(y)), rep(1:k.folds, length = length(y)))#cv.folds(length(y), k.folds)
  }else{
    all.folds <- in.folds
  }

  all.lambda.obj <- list()
  for(lambda in lambda.vec){
    cat(".")
    residmat <- matrix(0, length(s.fraction), k.folds)
    for (i in seq(k.folds)) {
      omit <- all.folds[[i]]

      y.train <- y[-omit]
      y.test <- y[omit]

      x.train <- x[-omit,,drop=F]
      x.test <- x[omit,,drop=F]

      weights.vec <- NULL
      if((weights.type == "other") && is.null(weights.vec)){
        if(any(in.weights.vec == 0))
          in.weights.vec[which(in.weights.vec == 0)] <- 1# no.pr.val

          weights.vec <- in.weights.vec
      }

      fit <- enetWeights(x.train, y.train,max.steps , trace = trace, lambda=lambda,in.weights=weights.vec)
      fit <- predict(fit, x.test, mode = "fraction", s = s.fraction)$fit

      if (length(omit) == 1)
        fit <- matrix(fit, nrow = 1)

      residmat[, i] <- apply((y.test - fit)^2, 2, mean)
      if (trace)
        cat("\n CV Fold", i, "\n\n")
    }

    cv <- apply(residmat, 1, mean)
    cv.error <- sqrt(apply(residmat, 1, var)/k.folds)
    cv.res <- list(fraction = s.fraction, cv = cv, cv.error = cv.error)
    cv.res[["mode"]] = "fraction"
    cv.res[["index"]] = s.fraction
    if (plot.it) #{
      plotCVLars(cv.res, se = se)

    enet.model <- enetWeights(x,y,max.steps,trace = trace,lambda=lambda,in.weights=weights.vec)

    #now get the coefficients of the chosen model, and rescale,
    #as described in adaptive-lars paper

    #getting coefs at cv.min
    min.idx <- which.min(cv)
    cv.opt.frac <- s.fraction[min.idx]
    coefs.cv.min <- predict(enet.model, s=cv.opt.frac, type="coef", mode="fraction")$coefficients

    #gettings coefs at one st.dev away from cv.min
    sparse.idx <- pickSparseModel(cv.res, plot.it, lambda)
    cv.sparse.frac <- s.fraction[sparse.idx]
    coefs.sparse <- predict(enet.model, s=cv.sparse.frac, type="coef", mode="fraction")$coefficients

    object <- list()
    object[["cv.obj"]] <- cv.res
    object[["coefs.cv.min"]] <- coefs.cv.min
    object[["error.cv.min.idx"]] <- min.idx
    object[["error.cv.min"]] <- cv[min.idx]/cv[1] #get relative cv error
    object[["coefs.sparse"]] <- coefs.sparse
    object[["error.sparse.idx"]] <- sparse.idx
    object[["error.sparse"]] <- cv[sparse.idx]/cv[1] #get relative cv error
    object[["weights"]] <- weights.vec

    all.lambda.obj[[paste(lambda,sep="")]] <- object
  }
  invisible(all.lambda.obj)
}

enetWeights <- function (x, y, lambda = 0, max.steps, normalize = FALSE, intercept = TRUE,
    trace = FALSE, eps = .Machine$double.eps, in.weights=NULL)
{
  call <- match.call()
  nm <- dim(x)
  n <- nm[1]
  m <- nm[2]
  im <- seq(m)
  one <- rep(1, n)
  vn <- dimnames(x)[[2]]
  meanx <- drop(one %*% x)/n
  if (intercept == FALSE) {
    meanx <- rep(0, m)
  }
  x <- scale(x, meanx, FALSE)
  normx <- sqrt(drop(one %*% (x^2)))
  if (normalize == FALSE) {
    normx <- rep(1, m)
  }
  if (any(normx < eps * sqrt(n)))
    stop("Some of the columns of x have zero variance")
  names(normx) <- NULL
  x <- scale(x, FALSE, normx)
  mu <- mean(y)
  if (intercept == FALSE) {
    mu <- 0
  }
  y <- drop(y - mu)

  #adding scaling of predictors, x
  #by in.weights here!
  if(!is.null(in.weights)){
    x <- scale(x,FALSE,in.weights)
  }

  d1 <- sqrt(lambda)
  d2 <- 1/sqrt(1 + lambda)
  Cvec <- drop(t(y) %*% x) * d2
  ssy <- sum(y^2)
  residuals <- c(y, rep(0, m))
  if (lambda > 0) {
    maxvars <- m
  }
  if (lambda == 0) {
    maxvars <- min(m, n - 1)
  }
  if (missing(max.steps)) {
    max.steps <- 50 * maxvars
  }
  if (max.steps == 0) {
    max.steps <- 50 * maxvars
  }

  L1norm <- 0
  penalty <- max(abs(Cvec))
  beta <- rep(0, m)
  betactive <- list(NULL)
  first.in <- integer(m)
  active <- NULL
  Actset <- list(NULL)
  df <- 0
  if (lambda != 0) {
    Cp <- ssy
  }
  ignores <- NULL
  actions <- as.list(seq(max.steps))
  drops <- FALSE
  Sign <- NULL
  R <- NULL
  k <- 0
  cat('BEGINING MEN ITER\n')
  while ((k < max.steps) & (length(active) < maxvars)) {
    action <- NULL
    k <- k + 1
    cat('iter', k, '\n')
    inactive <- if (k == 1)
          im
        else im[-c(active, ignores)]
    C <- Cvec[inactive]
    Cmax <- max(abs(C))
    if (!any(drops)) {
      new <- abs(C) == Cmax
      C <- C[!new]
      new <- inactive[new]
      for (inew in new) {
        R <- updateRR(x[, inew], R, x[, active], lambda)
        if (attr(R, "rank") == length(active)) {
          nR <- seq(length(active))
          R <- R[nR, nR, drop = FALSE]
          attr(R, "rank") <- length(active)
          ignores <- c(ignores, inew)
          action <- c(action, -inew)
          if (trace)
            cat("LARS-EN Step", k, ":\t Variable", inew,
                "\tcollinear; dropped for good\n")
        }
        else {
          if (first.in[inew] == 0)
            first.in[inew] <- k
          active <- c(active, inew)
          Sign <- c(Sign, sign(Cvec[inew]))
          action <- c(action, inew)
          if (trace)
            cat("LARS-EN Step", k, ":\t Variable", inew,
                "\tadded\n")
        }
      }
    }
    else action <- -dropid
    Gi1 <- backsolve(R, backsolvet(R, Sign))
    A <- 1/sqrt(sum(Gi1 * Sign))
    w <- A * Gi1
    u1 <- drop(x[, active, drop = FALSE] %*% w * d2)
    u2 <- rep(0, m)
    u2[active] <- d1 * d2 * w
    u <- c(u1, u2)
    if (lambda > 0) {
      maxvars <- m - length(ignores)
    }
    if (lambda == 0) {
      maxvars <- min(m - length(ignores), n - 1)
    }
    if (length(active) >= maxvars) {
      gamhat <- Cmax/A
    }
    else {
      a <- (drop(u1 %*% x[, -c(active, ignores)]) + d1 *
            u2[-c(active, ignores)]) * d2
      gam <- c((Cmax - C)/(A - a), (Cmax + C)/(A + a))
      gamhat <- min(gam[gam > eps], Cmax/A)
      Cdrop <- c(C - gamhat * a, -C + gamhat * a) - (Cmax -
            gamhat * A)
    }
    dropid <- NULL
    b1 <- beta[active]
    z1 <- -b1/w
    zmin <- min(z1[z1 > eps], gamhat)
    if (zmin < gamhat) {
      gamhat <- zmin
      drops <- z1 == zmin
    }
    else drops <- FALSE
    beta[active] <- beta[active] + gamhat * w
    betactive[[k]] <- beta[active]
    Actset[[k]] <- active
    residuals <- residuals - (gamhat * u)
    Cvec <- (drop(t(residuals[1:n]) %*% x) + d1 * residuals[-(1:n)]) *
        d2
    L1norm <- c(L1norm, sum(abs(beta[active]))/d2)
    penalty <- c(penalty, penalty[k] - abs(gamhat * A))
    if (any(drops)) {
      dropid <- seq(drops)[drops]
      for (id in rev(dropid)) {
        if (trace)
          cat("LARS-EN Step", k, ":\t Variable", active[id],
              "\tdropped\n")
        R <- downdateR(R, id)
      }
      dropid <- active[drops]
      beta[dropid] <- 0
      active <- active[!drops]
      Sign <- Sign[!drops]
    }
    if (!is.null(vn))
      names(action) <- vn[abs(action)]
    actions[[k]] <- action
  }
  allset <- Actset[[1]]
  for (i in 2:k) {
    allset <- union(allset, Actset[[i]])
  }
  allset <- sort(allset)
  max.p <- length(allset)
  beta.pure <- matrix(0, k + 1, max.p)
  for (i in 2:(k + 1)) {
    for (j in 1:length(Actset[[i - 1]])) {
      l <- c(1:max.p)[allset == Actset[[i - 1]][j]]
      beta.pure[i, l] <- betactive[[i - 1]][j]
    }
  }
  beta.pure <- beta.pure/d2
  dimnames(beta.pure) <- list(paste(0:k), vn[allset])
  k <- dim(beta.pure)[1]
  df <- 1:k
  for (i in 1:k) {
    a <- drop(beta.pure[i, ])
    df[i] <- 1 + length(a[a != 0])
  }
  residuals <- y - x[, allset, drop = FALSE] %*% t(beta.pure)
  beta.pure <- scale(beta.pure, FALSE, normx[allset])
  RSS <- apply(residuals^2, 2, sum)
  R2 <- 1 - RSS/RSS[1]
  Cp <- ((n - m - 1) * RSS)/rev(RSS)[1] - n + 2 * df
  object <- list(call = call, actions = actions[seq(k)], allset = allset,
      beta.pure = beta.pure, vn = vn, mu = mu, normx = normx[allset],
      meanx = meanx[allset], lambda = lambda, L1norm = L1norm,
      penalty = penalty * 2/d2, df = df, Cp = Cp, sigma2 = rev(RSS)[1]/(n -
            m - 1))
  class(object) <- "enet"
  object
}


pickSparseModel <- function(cv.res, plot.it, lambda){
  cv 				 <- cv.res$cv
  s.fraction <- cv.res$fraction
  cv.error 	 <- cv.res$cv.error

  cv.min.idx 		 <- which.min(cv)
  best.cvErr.min <- Inf
  thresh.cv 		 <- cv[cv.min.idx] + cv.error[cv.min.idx]#/cv[1] #taking error one std dev from min
  best.s 				 <- 1

  for (i in 1:cv.min.idx) {
    if  (cv[i] <= thresh.cv) {
      best.s <- i
      break
    }
  }

  if(plot.it){
    bestSRng <- range(cv[best.s]+.2*cv[best.s], cv[best.s]-.2*cv[best.s])
    lines(c(s.fraction[best.s],s.fraction[best.s]), range(cv[best.s]+.02*cv[best.s], cv[best.s]-.02*cv[best.s]), col=colors()[258], lty=1, lwd=3)

    errRng <- range(cv[cv.min.idx] + cv.error[cv.min.idx],  cv[cv.min.idx] - cv.error[cv.min.idx])
    lines(c(s.fraction[cv.min.idx],s.fraction[cv.min.idx]), errRng, col=2, lty=2, lwd=3) # red line denoting where the minimum is
    lines(range(s.fraction), c(thresh.cv,thresh.cv), col=4, lty=2, lwd=3)   #blue line going to where one std. dev. above the min intersects the curve
    #title(paste("enet curve for biclust: ",ind," w/ l2 weight: ",l2))


    mtext(paste("l2 weight of: ", lambda, sep = ""), 3, line = -1)
  }
  #min.idx <- best.s
  sparse.idx = best.s

  return(sparse.idx)
}

