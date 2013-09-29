# TODO: Add comment
#
# Author: Christoph
###############################################################################

BBSR <- function(X, Y, Pi, clr.mat, nS, no.pr.val, weights.mat, cores) {

  if (!all(apply(Pi, 1, identical, Pi[1,]))) {
    stop('BBSR not implemented for biclusters. Use CallBestSubSetRegression instead')
  }

  perm <- Pi[1, ]

  # Scale and permute design and response matrix
  X <- t(scale(t(X[, perm])))
  Y <- t(scale(t(Y[, perm])))

  G <- nrow(Y)  # number of genes
  K <- nrow(X)  # max number of possible predictors (number of TFs)

  pp <- matrix(FALSE, G, K)  # predictors that will be used in the regression

  # keep all predictors that we have priors for
  pp[weights.mat != no.pr.val] <- TRUE

  # for each gene, add the top nS predictors of the list to possible predictors
  for (ind in 1:G) {
    clr.order <- order(clr.mat[ind, ], decreasing=TRUE)
    pp[ind, clr.order[1:min(K, nS)]] <- TRUE
  }
  diag(pp) <- FALSE

  out.list <- mclapply(1:G, BBSRforOneGene, X, Y, pp, weights.mat, nS, mc.cores=cores)

  return(out.list)
}

BBSRforOneGene <- function(ind, X, Y, pp, weights.mat, nS) {
  if (ind %% 100 == 0) {
    cat('Progress: BBSR for gene', ind, '\n')
  }

  pp.i <- pp[ind, ]
  # create BestSubsetRegression input
  y <- as.vector(Y[ind, ], mode="numeric")
  x <- t(as.matrix(X[pp.i, ]))
  g <- weights.mat[ind, pp.i]

  # experimental stuff
  spp <- ReduceNumberOfPredictors(y, x, g, nS)
  pp.i[pp.i == TRUE] <- spp
  x <- t(as.matrix(X[pp.i, ]))
  g <- weights.mat[ind, pp.i]

  betas <- BestSubsetRegression(y, x, g)
  betas.resc <- PredErrRed(y, x, betas)
  return(list(ind=ind, pp=pp.i, betas=betas, betas.resc=betas.resc))
}

CallBestSubSetRegression <- function(ind, Xs, Y, Pi, clr.mat, nS,
                                     no.pr.val = NULL,
                                     weights.mat = NULL) {
  # Calls best subset regression
  # TODO: Add more here

  cat("*")
  browser()
  # Scale and permute design and response matrix
  Xs.scaled <- t(scale(t(Xs[, Pi[ind, ]])))
  Y.scaled <- t(scale(t(Y[, Pi[ind, ]])))

  K <- nrow(Xs.scaled)  # max number of possible predictors

  pp <- rep(FALSE, K)  # predictors that will be used in the regression

  # if a clr matrix is given
  if (is.null(dim(clr.mat)) == FALSE) {

    # bump predictors with priors to top of list; not needed anymore
    #clr.mat[ind, weights.mat[ind, ] != no.pr.val] <- clr.mat[ind, weights.mat[ind, ] != no.pr.val] + max(clr.mat) + .Machine$double.eps

    # send self predictor to end of list if gene we are inferring on is a TF
    # also remove as prior if present
    if (ind <= K) {
      clr.mat[ind, ind] <- -Inf
      weights.mat[ind, ind] <- no.pr.val
    }

    clr.order <- order(clr.mat[ind, ], decreasing=TRUE)
    have.priors <- which(weights.mat[ind, ] != no.pr.val)

    # potential predictors are the union of predictors with priors and the top
    # nS ones based on clr matrix
    pp[unique(c(have.priors, clr.order[1:min(K, nS)]))] <- TRUE

  }
  else {

    pp <- rep(TRUE, K)
    if (ind <= K) {
      pp[ind] <- FALSE
    }

  }

  #cat("\n", length(pp), "\n")
  # create BestSubsetRegression input
  y <- as.vector(Y.scaled[ind, ], mode="numeric")
  x <- t(as.matrix(Xs.scaled[pp, ]))
  g <- weights.mat[ind, pp]

  # experimental stuff
  spp <- ReduceNumberOfPredictors(y, x, g, min(K, nS))
  pp[pp == TRUE] <- spp
  x <- t(as.matrix(Xs.scaled[pp, ]))
  g <- weights.mat[ind, pp]

  # run the best subset regression
  #Rprof("bssr_new.out")
  betas.x <- BestSubsetRegression(y, x, g)
  #betas.x <- BayesianModelAveraging(y, x, g)
  #Rprof(NULL)
  #betas.full <- rep(0, K)
  #betas.full[pp] <- betas.x
  #names(betas.full) <- rownames(Xs)

  # rescale betas based on amount of error reduction by the predictors
  betas.resc <- PredErrRed(y, x, betas.x)
  #betas.resc.full <- rep(0, K)
  #betas.resc.full[pp] <- betas.resc
  #names(betas.resc.full) <- rownames(Xs)

  # cut the crap and end this here
  return(list(ind=ind, pp=pp, betas=betas.x, betas.resc=betas.resc))

}


ReduceNumberOfPredictors <- function(y, x, g, n) {
  K <- ncol(x)
  spp <-
  if (K <= n) {
    return(rep(TRUE, K))
  }

  combos <- cbind(diag(K) == 1, CombCols(diag(K)))
  bics <- ExpBICforAllCombos(y, x, g, combos)
  bics.avg <- apply(t(t(combos) * bics), 1, sum)
  ret <- rep(FALSE, K)
  ret[order(bics.avg)[1:n]] <- TRUE

  return(ret)
}

BayesianModelAveraging <- function(y, x, g) {

  mprior.size <- g
  bms.out <- bms(cbind(y,x), nmodel=0, mprior='pip',
                 mprior.size=mprior.size, user.int=F)
  #bms.out <- bms(cbind(y,x), nmodel=0, user.int=F)

  tmp <- coef(bms.out)
  tmp <- tmp[order(tmp[, 'Idx']), ]

  ret <- tmp[, 'Post Mean']
  ret[tmp[, 'PIP'] < 0.9] <- 0
  #print(sum(tmp[, 'PIP'] > 0.5))
  #return(as.numeric(tmp[, 'PIP'] > 0.9)[order(tmp[, 'Idx'])])
  return(ret)
}

BestSubsetRegressionAllWeights <- function(y, x, g){
  #Do best subset regression without any weight (if this option is chosen)
  #and with the combination of all weights that are chosen
  #return the results in a list

  # Q CH 11|18|2011: Is this ever going to be used?
}

BestSubsetRegression <- function(y, x, g) {
  # Do best subset regression by using all possible combinations of columns of
  # x as predictors of y. Model selection criterion is BIC using results of
  # Bayesian regression with Zellner's g-prior.
  #
  # Args:
  #   y: dependent variable
  #   x: independent variable
  #   g: value for Zellner's g-prior; can be single value or vector
  #
  # Returns:
  #   Beta vector of best model

  K <- ncol(x)
  N <- nrow(x)
  ret <- c()

  combos <- AllCombinations(K)
  bics <- ExpBICforAllCombos(y, x, g, combos)

  not.done <- TRUE
  iter <- 0
  cat('ENTERING\n')
  while (not.done) {
    best <- which.min(bics)
    cat('iter is', iter, '\n')
    cat('best is', best, '\n')
    iter = iter + 1

    # For the return value, re-compute beta ignoring g-prior.
    betas <- rep(0, K)
    if (best > 1) {
      x.tmp <- matrix(x[,combos[, best]], N)

      tryCatch({
        bhat <- solve(crossprod(x.tmp), crossprod(x.tmp, y))
        betas[combos[, best]] <- bhat
        not.done <- FALSE
      }, error = function(e) {
        if (any(grepl('solve.default', e$call)) & grepl('singular', e$message)) {
          # error in solve - system is computationally singular
          cat(bics[best], 'at', best, 'replaced\n')
          bics[best] <<- Inf
        } else {
          stop(e)
        }
      })
    }
    else {
      not.done <- FALSE
    }
  }
  cat('OPT DONE\n')
  return(betas)
}


AllCombinations <- function(k) {
  # Create a boolean matrix with all possible combinations of 1:k.
  # Output has k rows and 2^k columns where each column is one combination.
  # Note that the first column is all FALSE and corresponds to the null model.
  if (k < 1) {
    stop("No combinations for k < 1")
  }

  N <- 2^k
  out <- matrix(FALSE, k, N)
  out[1, 2] <- TRUE

  row <- 2
  col <- 3
  while (col < N) {
    out[row, col] <- TRUE

    for (i in 1:(col-2)) {
      out[, col + i] <- out[, col] | out[, i + 1]
    }

    row <- row + 1
    col <- col * 2 - 1
  }

  return(out)
}


CombCols <- function(m) {
  K <- ncol(m)
  ret <- matrix(TRUE, nrow(m), K * (K - 1) / 2)
  ret.col <- 1
  for (i in 1:(K - 1)) {
    for (j in (i + 1):K) {
      ret[, ret.col] <- m[, i] | m[, j]
      ret.col <- ret.col + 1
    }
  }
  return(ret)
}


ExpBICforAllCombos <- function(y, x, g, combos) {
  # For a list of combinations of predictors do Bayesian linear regression,
  # more specifically calculate the parametrization of the inverse gamma
  # distribution that underlies sigma squared using Zellner's g-prior method.
  # Parameter g can be a vector. The expected value of the log of sigma squared
  # is used to compute expected values of BIC.
  # Returns list of expected BIC values, one for each model.
  K <- ncol(x)
  N <- nrow(x)

  C <- ncol(combos)
  bics <- rep(0, C)

  # is the first combination the null model?
  first.combo <- 1
  if (sum(combos[, 1]) == 0) {
    bics[1] <- N * log(var(y))
    first.combo <- 2
  }

  # shape parameter for the inverse gamma sigma squared would be drawn from
  shape <- N / 2

  # compute digamma of shape here, so we can re-use it later
  dig.shape <- digamma(shape)

  # pre-compute the crossproducts that we will need to solve for beta
  xtx <- crossprod(x)
  xty <- crossprod(x, y)

  # In Zellner's formulation there is a factor in the calculation of the rate
  # parameter: 1 / (g + 1)
  # Here we replace the factor with the approriate matrix since g is a vector
  # now.
  var.mult <- matrix(sqrt(1 / (g + 1)), K, K)
  var.mult <- var.mult * t(var.mult)

  for (i in first.combo:C){
    comb <- combos[, i]
    x.tmp <- matrix(x[, comb], N)
    k <- sum(comb)

    tryCatch({
      # this is faster than calling lm
      bhat <- solve(xtx[comb, comb], xty[comb])

      ssr <- sum((y - x.tmp %*% bhat)^2)  # sum of squares of residuals

        # rate parameter for the inverse gamma sigma squared would be drawn from
        # our guess on the regression vector beta is all 0 for sparse models
        rate <- (ssr +
                (0 - t(bhat)) %*%
                (xtx[comb, comb] * var.mult[comb, comb]) %*%
                t(0 - t(bhat))) / 2

        # the expected value of the log of sigma squared based on the
        # parametrization of the inverse gamma by rate and shape
        exp.log.sigma2 <- log(rate) - dig.shape

        # expected value of BIC
        bics[i] <- N * exp.log.sigma2 + k * log(N)

    }, error = function(e) {
      if (any(grepl('solve.default', e$call)) & grepl('singular', e$message)) {
        # error in solve - system is computationally singular
        bics[i] <<- Inf
      } else {
        stop(e)
      }
    })

  }

  return(bics)
}



PredErrRed <- function(y, x, beta) {
  # Calculates the error reduction (measured by variance of residuals) of each
  # predictor - compare full model to model without that predictor
  N <- nrow(x)
  K <- ncol(x)
  pred <- beta != 0
  P <- sum(pred)

  # compute sigma^2 for full model
  residuals <- y - x %*% beta
  sigma.sq.full <- var(residuals)

  # this will be the output
  err.red <- rep(0, K)

  # special case if there is only one predictor
  if (P == 1) {
    err.red[pred] <- 1 - sigma.sq.full / var(y)
    return(err.red)
  }

  # one by one leave out each predictor and re-compute the model with the
  # remaining ones
  for (i in (1:K)[pred]) {
    pred.tmp <- pred
    pred.tmp[i] <- FALSE
    x.tmp <- matrix(x[,pred.tmp], N, P-1)
    #bhat <- solve(t(x.tmp) %*% x.tmp) %*% t(x.tmp) %*% y
    bhat <- solve(crossprod(x.tmp), crossprod(x.tmp, y))
    residuals <- y - x.tmp %*% bhat
    sigma.sq <- var(residuals)

    err.red[i] <- 1 - sigma.sq.full / sigma.sq
  }
  return(err.red)
}


