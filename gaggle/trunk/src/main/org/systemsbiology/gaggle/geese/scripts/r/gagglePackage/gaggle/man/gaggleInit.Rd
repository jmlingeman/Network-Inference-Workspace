\name{gaggleInit}
\alias{gaggleInit}
\title{Initialize the R Goose, registering with the Gaggle}
\description{Call this function to register the R
  Goose with a Gaggle Boss. If you do not specify an argument, the function
  will automatically start the Gaggle Boss for you if it is not already running.
  If you do specify an argument (a remote machine on which a boss is running),
  the boss must be running on that machine or this function will fail.
  By default, this function looks for, and registers with, a boss on your current
  computer ('localhost').   Alternatively, you may supply the
  name of another computer where a boss is running.
  This is useful if you wish to run R on a remote compute 
  server (perhaps with extra memory and speed), and connect it 
  to a gaggle running on your desktop.
}
\usage{gaggleInit (bossHost = 'localhost')}

\arguments{
  \item{bossHost}{the computer ('host') on which your GaggleBoss is running. \emph{The host machine
  must be accessible through any firewall (port 1099 must be open).}}
  }


\value{Returns NULL.}

\seealso{
  \code{\link{broadcast}}.
  \code{\link{getNameList}}.
  \code{\link{getMatrix}}.
  \code{\link{getNetwork}}.
  \code{\link{getCluster}}.
  \code{\link{getTuple}}.
  }

\keyword{manip}

