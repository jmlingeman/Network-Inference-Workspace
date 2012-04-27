\name{filterVector}
\alias{filterVector}
\title{Does a Vector have some Minimum Values Above Threshold?}
\description{Determine whether a vector has some number, or some
             consecutive number, of elements above a threshold.}

\usage{filterVector (vec, threshold, minCount, consecutive = TRUE)}
\arguments{
  \item{vec}{the vector to be filtered}
  \item{threshold}{elements in row must match or exceed this value}
  \item{minCount}{at least this many elements in the row must pass threshold}
  \item{consecutive}{if TRUE, the \code{mincount} elements must be consecutive}
  }

\value{Returns TRUE or FALSE.}

\examples{
  library (gaggleUtil)
  data (gamma)
  vec = gamma.lambdas ["VNG0177G",]
  filterVector (vec, 20.0, 5, consecutive=FALSE) #  TRUE
  filterVector (vec, 20.0, 5, consecutive=TRUE)  #  FALSE
  filterVector (vec, 15.0, 5, consecutive=TRUE)  #  TRUE

}

\seealso{
  \code{\link{filterMatrix}}.
  }
\keyword{array}
\keyword{manip}
