"add1.brlr" <-
    function (object, scope, scale = 0, test = c("none", "Chisq", 
                                        "F"), x = NULL, k = 2, ...) 
{
    Fstat <- function(table, rdf) {
        dev <- table$Deviance
        df <- table$Df
        diff <- pmax(0, (dev[1] - dev)/df)
        Fs <- (diff/df)/(dev/(rdf - df))
        Fs[df < .Machine$double.eps] <- NA
        P <- Fs
        nnas <- !is.na(Fs)
        P[nnas] <- pf(Fs[nnas], df[nnas], rdf - df[nnas], lower.tail = FALSE)
        list(Fs = Fs, P = P)
    }
    if (!is.character(scope)) { 
        scope <- add.scope(object, update.formula(object, scope))
    }
    if (!length(scope)) {
        stop("no terms in scope for adding to object")
    }
    oTerms <- attr(object$terms, "term.labels")
    int <- attr(object$terms, "intercept")
    ns <- length(scope)
    dfs <- dev <- numeric(ns + 1)
    names(dfs) <- names(dev) <- c("<none>", scope)
    dfs[1] <- object$rank
    dev[1] <- object$deviance
    add.rhs <- paste(scope, collapse = "+")
    add.rhs <- eval(parse(text = paste("~ . +", add.rhs)))
    new.form <- update.formula(object, add.rhs)
    Terms <- terms(new.form)
    y <- object$y
    fc <- object$call
    fc$formula <- Terms
    fob <- list(call = fc)
    class(fob) <- oldClass(object)
    m <- model.frame(fob, xlev = object$xlevels)
    wt <- model.weights(m)
    os <- model.offset(m)
    oldn <- length(y)
    y <- model.response(m, "numeric")
    if (NCOL(y) == 2) y <- y[, 1]/(y[, 1] + y[, 2])
    newn <- length(y)
    if (newn < oldn) {
        warning(paste("using the", newn, "/", oldn,
                      "rows from a combined fit"))
    }
    if (is.null(x)) {
        x <- model.matrix(Terms, m, contrasts = object$contrasts)
    }
    n <- nrow(x)
    if (is.null(wt)) wt <- rep.int(1, n)
    y <- cbind(y*wt, (1-y)*wt)
    Terms <- attr(Terms, "term.labels")
    asgn <- attr(x, "assign")
    ousex <- match(asgn, match(oTerms, Terms), 0) > 0
    if (int) ousex[1] <- TRUE
    X <- x[, ousex, drop = FALSE]
    z <- brlr(y ~ X, offset = os, br = object$bias.reduction,
                     control = object$control)
    dfs[1] <- z$rank
    dev[1] <- z$deviance
    for (tt in scope) {
        usex <- match(asgn, match(tt, Terms), 0) > 0
        X <- x[, usex | ousex, drop = FALSE]
        z <- brlr(y ~ X,  offset = os, br = object$bias.reduction,
                     control = object$control)
        dfs[tt] <- z$rank
        dev[tt] <- z$deviance
    }
    if (scale == 0) 
        dispersion <- summary(object, dispersion = NULL)$dispersion
    else dispersion <- scale
    fam <- object$family$family
    if (fam == "gaussian") {
        if (scale > 0) 
            loglik <- dev/scale - n
        else loglik <- n * log(dev/n)
    }
    else loglik <- dev/dispersion
    aic <- loglik + k * dfs
    aic <- aic + (extractAIC(object, k = k)[2] - aic[1])
    dfs <- dfs - dfs[1]
    dfs[1] <- NA
    aod <- data.frame(Df = dfs, Deviance = dev, AIC = aic, row.names = names(dfs), 
                      check.names = FALSE)
    if (all(is.na(aic))) 
        aod <- aod[, -3]
    test <- match.arg(test)
    if (test == "Chisq") {
        dev <- pmax(0, loglik[1] - loglik)
        dev[1] <- NA
        LRT <- if (dispersion == 1) 
            "LRT"
        else "scaled dev."
        aod[, LRT] <- dev
        nas <- !is.na(dev)
        dev[nas] <- pchisq(dev[nas], aod$Df[nas], lower.tail = FALSE)
        aod[, "Pr(Chi)"] <- dev
    }
    else if (test == "F") {
        if (fam == "binomial" || fam == "poisson") 
            warning(paste("F test assumes quasi", fam, " family", 
                          sep = ""))
        rdf <- object$df.residual
        aod[, c("F value", "Pr(F)")] <- Fstat(aod, rdf)
    }
    head <- c("Single term additions", "\nModel:", deparse(as.vector(formula(object))), 
              if (scale > 0) paste("\nscale: ", format(scale), "\n"))
    class(aod) <- c("anova", "data.frame")
    attr(aod, "heading") <- head
    aod
}
