"drop1.brlr" <-
    function (object, scope, scale = 0, test = c("none", "Chisq", 
                                        "F"), k = 2, ...) 
{
    x <- model.matrix(object)
    n <- nrow(x)
    asgn <- attr(x, "assign")
    tl <- attr(object$terms, "term.labels")
    if (missing(scope)) 
        scope <- drop.scope(object)
    else {
        if (!is.character(scope)) 
            scope <- attr(terms(update.formula(object, scope)), 
                          "term.labels")
        if (!all(match(scope, tl, FALSE))) 
            stop("scope is not a subset of term labels")
    }
    ndrop <- match(scope, tl)
    ns <- length(scope)
    rdf <- object$df.resid
    chisq <- object$deviance
    dfs <- numeric(ns)
    dev <- numeric(ns)
    y <- object$y
    if (is.null(y)) 
        y <- model.response(model.frame(object), "numeric")
    wt <- object$prior.weights
    if (is.null(wt)) 
        wt <- rep.int(1, n)
    y <- cbind(y*wt, (1-y)*wt)
    for (i in 1:ns) {
        ii <- seq(along = asgn)[asgn == ndrop[i]]
        jj <- setdiff(seq(ncol(x)), ii)
        xi <- x[, jj, drop = FALSE]
        z <- brlr(y ~ 0 + xi, offset = object$offset,
                  br = object$bias.reduction,
                  control = object$control)
        dfs[i] <- z$rank
        dev[i] <- z$deviance
    }
    scope <- c("<none>", scope)
    dfs <- c(object$rank, dfs)
    dev <- c(chisq, dev)
    dispersion <- if (is.null(scale) || scale == 0) 
        summary(object, dispersion = NULL)$dispersion
    else scale
    fam <- object$family$family
    loglik <- if (fam == "gaussian") {
        if (scale > 0) 
            dev/scale - n
        else n * log(dev/n)
    }
    else dev/dispersion
    aic <- loglik + k * dfs
    dfs <- dfs[1] - dfs
    dfs[1] <- NA
    aic <- aic + (extractAIC(object, k = k)[2] - aic[1])
    aod <- data.frame(Df = dfs, Deviance = dev, AIC = aic, row.names = scope, 
                      check.names = FALSE)
    if (all(is.na(aic))) 
        aod <- aod[, -3]
    test <- match.arg(test)
    if (test == "Chisq") {
        dev <- pmax(0, loglik - loglik[1])
        dev[1] <- NA
        nas <- !is.na(dev)
        LRT <- if (dispersion == 1) 
            "LRT"
        else "scaled dev."
        aod[, LRT] <- dev
        dev[nas] <- pchisq(dev[nas], aod$Df[nas], lower.tail = FALSE)
        aod[, "Pr(Chi)"] <- dev
    }
    else if (test == "F") {
        if (fam == "binomial" || fam == "poisson") 
            warning(paste("F test assumes quasi", fam, " family", 
                          sep = ""))
        dev <- aod$Deviance
        rms <- dev[1]/rdf
        dev <- pmax(0, dev - dev[1])
        dfs <- aod$Df
        rdf <- object$df.residual
        Fs <- (dev/dfs)/rms
        Fs[dfs < 1e-04] <- NA
        P <- Fs
        nas <- !is.na(Fs)
        P[nas] <- pf(Fs[nas], dfs[nas], rdf, lower.tail = FALSE)
        aod[, c("F value", "Pr(F)")] <- list(Fs, P)
    }
    head <- c("Single term deletions", "\nModel:", deparse(as.vector(formula(object))), 
              if (!is.null(scale) && scale > 0) paste("\nscale: ", 
                                                      format(scale), "\n"))
    class(aod) <- c("anova", "data.frame")
    attr(aod, "heading") <- head
    aod
}
