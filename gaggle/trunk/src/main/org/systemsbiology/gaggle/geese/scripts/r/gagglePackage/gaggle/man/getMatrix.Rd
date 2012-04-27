\name{getMatrix}
\alias{getMatrix}
\title{Create an R matrix variable from Most Recently Broadcast Matrix}
\description{After a matrix is broadcast to the R goose, one additional step is required of the R
             user before it is available as a matrix variable.  (This step may be eliminated in
             the future.)}

\usage{m <- getMatrix ()}
\value{Returns a matrix with double precision elements, row names, and column names.}

\seealso{
  \code{\link{getNameList}}.
  \code{\link{getNetwork}}.
  }
\keyword{interface}

