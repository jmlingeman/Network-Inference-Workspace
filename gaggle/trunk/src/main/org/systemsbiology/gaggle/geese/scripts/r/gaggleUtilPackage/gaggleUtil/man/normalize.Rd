\name{normalize}
\alias{normalize}
\title{row-wise normalization of a matrix}

\description{each row is normalized, producing rows with a mean of zero,
  and variance of 1.0. Missing values (NA's) are skipped over in the calucations.}

\usage{nm <- normalize (matrix)}

\arguments{
  \item{matrix}{a matrix to be normalized.}
}


\examples{
  library (gaggleUtil)
  data (gamma)
  mean (gamma.ratios [1,])                        # 0.03025
  var  (gamma.ratios [1,])                        # 0.002743667
  normalizedRatios = normalize (gamma.ratios)
  mean (normalizedRatios [1,])                    # 1.561251e-17
  var  (normalizedRatios [1,])                    #  1
}


\value{Returns a matrix of the same dimension, in which each row is normalized.}
\keyword{array}
