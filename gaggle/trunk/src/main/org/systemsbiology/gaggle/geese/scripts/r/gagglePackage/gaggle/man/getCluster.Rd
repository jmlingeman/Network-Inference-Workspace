\name{getCluster}
\alias{getCluster}
\title{Create an R variable from Most Recently Broadcast Cluster}
\description{After a cluster is broadcast to the R goose, one additional step is required of the R
             user before it is available as a character list variable in R.  (This step may be eliminated in
             the future.)}
\usage{cluster <- getCluster ()}
\arguments{
  }

\value{Returns a list with three fields: name, rowNames, columnNames.}

\seealso{
  \code{\link{getNameList}}.
  \code{\link{getMatrix}}.
  \code{\link{getNetwork}}.
  }
\keyword{manip}

