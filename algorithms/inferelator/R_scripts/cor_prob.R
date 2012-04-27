cor.prob <- function(cor,dfr) {
    r2 <- cor^2
  Fstat <- r2 * dfr / (1 - r2)
  R <- pf(Fstat, 1, dfr,lower.tail=FALSE)
  R
}
