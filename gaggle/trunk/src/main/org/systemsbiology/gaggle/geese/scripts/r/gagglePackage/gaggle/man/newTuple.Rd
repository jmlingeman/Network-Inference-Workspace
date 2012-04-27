\name{newTuple}
\alias{newTuple}
\alias{newSingle}
\alias{addSingle}
\alias{tupleToList}
\alias{getTupleAsList}

\title{Manipulate the Gaggle Tuple data type}

\description{Gaggle Tuples are lists of key/value pairs which can be nested. These functions support
creation of tuple objects for broadcast to other Gaggle-connected software or conversion of tuples
to R list objects.

The function \emph{getTupleAsList} receives a Gaggle Tuple broadcast by converting the received value to a list. This is
an alternative to the older \emph{getTuple} which returns an environment object.}

\usage{
newTuple(name, list=NULL)
newSingle(name, value)
addSingle(tuple, name=NULL, value=NULL, single=NULL)
tupleToList(tuple)
getTupleAsList()
}

\arguments{
  \item{name}{name to be assigned to the tuple or individual value}
  \item{list}{a list of items to be converted to Singles and added to the tuple}
  \item{value}{the value of a key/value pair}
  \item{tuple}{a java tuple object}
  \item{single}{a java single object}
}

\seealso{
  \code{\link{getTuple}}.
  \code{\link{broadcast}}.
}

\examples{
  t <- newTuple('sequence', list(name='chromosome', length=2014239))
  s <- newSingle('topology', 'circular')
  addSingle(t, single=s)
  addSingle(t, name='color', 'blue')
  tupleToList(t)
  # expected output:
  #  $name
  #    [1] "chromosome"
  #  $length
  #    [1] 2014239
  #  $topology
  #   [1] "circular"
  #  $color
  #    [1] "blue"
}

\keyword{gaggle}
\keyword{single}
\keyword{tuple}
\keyword{broadcast}

