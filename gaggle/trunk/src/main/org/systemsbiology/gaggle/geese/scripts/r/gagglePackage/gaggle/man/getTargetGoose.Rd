\name{getTargetGoose}
\alias{getTargetGoose}
\title{Which Goose will Receive Broadcasts?}
\description{A gaggle broadcast usually (though not necessarily) goes to a specific goose; this function
             will tell you which one.  Note that if the current target goose is \emph{'all'} or \emph{'boss'}
             then the actual distribution of your broadcast will be controlled by setting you have made
             in the graphical user interface to the Gaggle Boss.}
\usage{nm <- getTargetGoose ()}

\value{Returns a goose name, or \emph{'all'} or \emph{'boss'}}

\seealso{
  \code{\link{setTargetGoose}}.
  \code{\link{broadcast}}.
  }
\keyword{interface}

