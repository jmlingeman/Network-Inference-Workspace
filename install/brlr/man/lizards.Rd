\name{lizards}
\docType{data}
\alias{lizards}
\title{Habitat Preferences of Lizards}
\usage{data(lizards)}
\description{
The \code{lizards} data frame has 23 rows and 6 columns.
Variables \code{grahami} and \code{opalinus} are counts
of two lizard species at two different perch heights, two
different perch diameters, in sun and in shade, at three
times of day.
}
\format{
  This data frame contains the following columns:
  \describe{
    \item{\code{grahami}}{count of grahami lizards}
    \item{\code{opalinus}}{count of opalinus lizards}
    \item{\code{height}}{a factor with levels \code{"<5ft"}, \code{">=5ft"}}
    \item{\code{diameter}}{a factor with levels \code{"<=2in"}, \code{">2in"}}
    \item{\code{light}}{a factor with levels \code{"sunny"}, \code{"shady"}}
    \item{\code{time}}{a factor with levels \code{"early"}, \code{"midday"},
	\code{"late"}}
  }
}
\source{
  McCullagh, P. and Nelder, J. A. (1989) \emph{Generalized Linear
    Models} (2nd Edition).  London: Chapman and Hall.

  Originally from

  Schoener, T. W. (1970)  Nonsynchronous spatial overlap of lizards in
  patchy habitats.  \emph{Ecology} \bold{51}, 408--418.
}
\examples{
data(lizards)
glm(cbind(grahami, opalinus) ~ height + diameter + light + time,
    family = binomial, data=lizards)
}
\keyword{datasets}
