require('Matrix')
require('parallel')

# mean squared error
mse <- function(x, y) {
  N <- length(x)
  if (!identical(N, length(y))) {
    stop('x and y have to have the same length')
  }
  return(sum((x - y)^2) / N)
}

# get the design and response matrix used in bootstrap i
bootstrap.des.res <- function(IN, i) {
  bs.pi <- IN$bs.pi[[i]]
  # set up bootstrap response matrix
  res.mat <- IN$final_response_matrix
  for (i in 1:nrow(res.mat)) {
    res.mat[i, ] <- res.mat[i, bs.pi[i, ]]
  }
  des.mat <- IN$final_design_matrix
  for (i in 1:nrow(des.mat)) {
    des.mat[i, ] <- des.mat[i, bs.pi[i, ]]
  }
  return(list(des.mat=des.mat, res.mat=res.mat))
}

# given a design and response matrix, refit the betas
# useful if we don't know whether the old betas came from scaled design and
# response matrices
refit.betas.mc <- function(X, Y, betas.old) {
  X <- rbind(1, X)
  beta.rows <- mclapply(1:nrow(Y), refit.one, X, Y, betas.old, mc.cores=8)
  beta <- matrix(unlist(beta.rows), nrow(Y), byrow=TRUE)
  return(beta=beta)
}

refit.one <- function(i, X, Y, betas.old) {
  K <- nrow(X)
  beta <- rep(0, K)
  selected <- c(TRUE, betas.old[i, ] != 0)
  x <- t(matrix(X[selected, ], sum(selected)))
  coefs <- as.numeric(solve(crossprod(x), crossprod(x, Y[i, ])))
  beta[selected] <- coefs
  return(beta)
}


# the following function will go through the conditions that were in the
# leave-out file and tries to predict data
# input: path to a directory with inferelator results
predict.data <- function(results.dir) {
  load(file.path(results.dir, 'params_and_input.RData'))
  beta.files <- list.files(results.dir, "betas_.+\\.RData$")

  conditions <- colnames(IN$exp.mat.lo)
  for (cond in conditions) {
    print(cond)

    # find the previous condition in the data set
    # we jump through some hoops to find the closest previous conditions
    # in case the immediately rpeceding one is also in the leave out set
    prev.cond <- NULL
    tries <- 0
    tmp.cond <- cond
    del.t <- 0
    while (is.null(prev.cond)) {
      tries <- tries + 1
      prev.cond <- as.character(IN$meta.data.lo$prevCol[IN$meta.data.lo$condName == tmp.cond])
      del.t <- del.t + IN$meta.data.lo$del.t[IN$meta.data.lo$condName == tmp.cond]
      if (!(prev.cond %in% IN$meta.data$condName)) {
        tmp.cond <- prev.cond
        prev.cond <- NULL
      }
      if (tries > nrow(IN$exp.mat.lo)) {
        stop(paste('Could not find previous condition for', cond, sep=' '))
      }
    }

    print(prev.cond)
    print(del.t)
    for (beta.file in beta.files) {
      cat(cond, beta.files, '\n')

      load(file.path(results.dir, beta.file))
      N <- length(betas)
      predicted <- matrix(0, nrow(IN$exp.mat), N)  # this hold the predicted data for each bootstrap
      for (i in 1:N) {
        beta <- betas[[i]]
        mats <- bootstrap.des.res(IN, i)
        beta.refit <- refit.betas.mc(mats$des.mat[IN$tf.names, ], mats$res.mat, beta)

        y <- beta.refit %*% c(1, IN$exp.mat[IN$tf.names, prev.cond])
        x <- IN$exp.mat[, prev.cond]
        x.pred <- as.numeric((y - x) / PARS$tau * del.t + x)
        predicted[, i] <- x.pred

      }
    }
    x.pred <- apply(predicted, 1, mean)  # average over all bootstrap predictions
    x.pred.mse <- mse(x.pred, IN$exp.mat.lo[, cond])  # mean squared error of the prediction
    data.mean.mse <- mse(apply(IN$exp.mat, 1, mean), IN$exp.mat.lo[, cond])  # MSE of the data average
    prev.cond.mse <- mse(IN$exp.mat[, prev.cond], IN$exp.mat.lo[, cond])  # MSE of simply using the previous condition
    fvu <- x.pred.mse / data.mean.mse  # fraction of variance unexplained by the prediction
    f <- file(paste(results.dir, "/leave_out_results.txt", sep=""))
    x.pred <- as.matrix(x.pred)
    rownames(x.pred) <- rownames(IN$exp.mat)
    cat('MSE of prediction is', x.pred.mse, '\n')
    cat('Fraction of Variance Unexplained is', fvu, '\n')
    cat('MSE of previous condition is', prev.cond.mse, '\n')
    cat('Fraction of Variance Unexplained is', prev.cond.mse / data.mean.mse, '\n')
    writeLines(c( paste("MSE:\t", x.pred.mse, sep=""),
                  paste("FractionVarianceUnexplained:\t", fvu, sep=""),
                  paste("MSEPrevCond:\t", prev.cond.mse, sep=""),
                  paste("FractionVarianceUnexplainedPrevCond:\t", prev.cond.mse / data.mean.mse, sep="") ), f)
    write.table(as.matrix(x.pred), file=file.path(paste(results.dir, "/leave_out_points", ".tsv", sep="")), sep="\t", row.names=TRUE, col.names=TRUE)
    close(f)
  }
}



# lot's of code duplication from above....
# this function does a within-training-set evaluation of data prediction
# for every condition that is part of a time-series but not the first point,
# we try to predict its expression
# input: path to a directory with inferelator results
predict.data.on.train <- function(results.dir) {
  load(file.path(results.dir, 'params_and_input.RData'))
  beta.files <- list.files(results.dir, "betas_.+\\.RData$")

  conditions <- IN$meta.data$condName[!is.na(IN$meta.data$prevCol)]
  results <- matrix(0, length(conditions), 3, dimnames=list(conditions, c('del.t', 'fvu', 'fvu.prevCond')))
  for (cond in conditions) {
    # find the previous condition in the data set
    prev.cond <- as.character(IN$meta.data$prevCol[IN$meta.data$condName == cond])
    del.t <- as.numeric(IN$meta.data$del.t[IN$meta.data$condName == cond])

    for (beta.file in beta.files) {

      load(file.path(results.dir, beta.file))
      N <- length(betas)
      predicted <- matrix(0, nrow(IN$exp.mat), N)
      for (i in 1:N) {
        beta <- betas[[i]]
        # set up bootstrap design and response matrix
        mats <- bootstrap.des.res(IN, i)
        beta.refit <- refit.betas.mc(mats$des.mat[IN$tf.names, ], mats$res.mat, beta)

        y <- beta.refit %*% c(1, IN$exp.mat[IN$tf.names, prev.cond])
        x <- IN$exp.mat[, prev.cond]
        x.pred <- as.numeric((y - x) / PARS$tau * del.t + x)
        predicted[, i] <- x.pred

      }

    }
    x.pred <- apply(predicted, 1, mean)
    x.pred.mse <- mse(x.pred, IN$exp.mat[, cond])
    data.mean.mse <- mse(apply(IN$exp.mat, 1, mean), IN$exp.mat[, cond])
    prev.cond.mse <- mse(IN$exp.mat[, prev.cond], IN$exp.mat[, cond])
    fvu <- x.pred.mse / data.mean.mse

    cat(cond, '\t', del.t, '\t', fvu, '\t', prev.cond.mse / data.mean.mse, '\n')
    results[cond, ] <- c(del.t, fvu, prev.cond.mse / data.mean.mse)
  }
  return(as.data.frame(results))
}


# example calls:
# predict.data('/home/ch1421/Projects/Inferelator/output/bsubtilis_bbsr_noprior_some_conditions_left_out')
# df <- predict.data.on.train('/home/ch1421/Projects/Inferelator/output/bsubtilis_bbsr_nopriors')
