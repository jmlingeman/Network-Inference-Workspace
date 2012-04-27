cv.lars_probs <- function (x, y, K = 10, fraction = seq(from = 0, to = 1, length = 100), 
    trace = FALSE, plot.it = TRUE, se = TRUE, ...) 
{
    all.folds <- cv.folds(length(y), K)
    residmat <- matrix(0, length(fraction), K)
    for (i in seq(K)) {
        omit <- all.folds[[i]]
        fit <- lars_probs(x[-omit, , drop = FALSE], y[-omit], trace = trace, 
            ...)
        fit <- predict(fit, x[omit, , drop = FALSE], mode = "fraction", 
            s = fraction)$fit
        if (length(omit) == 1) 
            fit <- matrix(fit, nrow = 1)
        residmat[, i] <- apply((y[omit] - fit)^2, 2, mean)
        if (trace) 
            cat("\n CV Fold", i, "\n\n")
    }
    cv <- apply(residmat, 1, mean)
    cv.error <- sqrt(apply(residmat, 1, var)/K)
    object <- list(fraction = fraction, cv = cv, cv.error = cv.error)
    if (plot.it) 
        plotCVLars(object, se = se)
    invisible(object)
}
