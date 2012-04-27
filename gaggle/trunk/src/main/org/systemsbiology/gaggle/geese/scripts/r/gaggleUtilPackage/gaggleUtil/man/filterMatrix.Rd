\name{filterMatrix}
\alias{filterMatrix}
\title{Subset a Matrix}
\description{Find names of matrix rows with a minCount of elements surpassing threshold.}
\usage{filterMatrix (matrix, threshold, minCount, consecutive = TRUE)}
\arguments{
  \item{matrix}{the matrix to be filtered}
  \item{threshold}{elements in rows must match or exceed this value}
  \item{minCount}{at least this many elements in the row must pass threshold}
  \item{consecutive}{if TRUE, the \code{mincount} elements must be consecutive}
  }

\value{Returns a list of row names.}


\examples{
  library (gaggleUtil)
  data (gamma)
  rowNames = filterMatrix (gamma.lambdas, 20, 5, TRUE)
  length (rowNames)   # 32
  rowNames [1:4]      # "VNG0101G" "VNG0285C" "VNG0402H" "VNG0550G"
}

\seealso{
  \code{\link{filterVector}}.
  }

\keyword{array}
\keyword{manip}
