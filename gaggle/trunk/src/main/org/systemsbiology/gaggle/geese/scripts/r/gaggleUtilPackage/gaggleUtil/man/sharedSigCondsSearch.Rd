\name{sharedSigCondsSearch}
\alias{sharedSigCondsSearch}
\title{Discover genes that share conditions of significant expression with a specified gene.}
\description{For the specified gene, and from the supplied matrix of lambdas, find all conditions in
             which the gene has a significance greater than \code{lambdaThreshold}.  Then look for
             other genes which have high lambdas in (some of) those same conditions.  If the overlap
             in conditions is greater than or equal to \code{overlapToBeInteresting}, print the
             information to stdout.}

\usage{sharedSigCondsSearch (gene, lambdas, lambdaThreshold=15, overlapToBeInteresting=.5)}

\arguments{
  \item{gene}{a list of candidate genes}
  \item{lambdas}{a matrix of lambda values, with the same dimensions, and the same row and column names,
                 as the ratios}
  \item{lambdaThreshold}{significance threshold; 15.0 is a typical}
  \item{overlapToBeInteresting}{threshold overlap of significant conditions}
  }

\value{NULL (for now; maybe this needs fixing); results are printed to stdout}

\keyword{array}
\keyword{manip}
