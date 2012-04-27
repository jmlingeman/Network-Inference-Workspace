\name{mi}
\alias{mi}

\title{ MI }
\description{
  Calculate MI using B splines on given data through C calls.
}
\usage{
mi(data, n, k, g)

}

\arguments{
  \item{data}{ data matrix, first m rows are response, and following rows are predictors. }
  \item{n}{ number of bins, defaults to 10 }
  \item{k}{ spline order, defaults to 3 }
  \item{g}{ number of non-predictors }
}
\details{

}
\value{
  Returns data matrix containing Mutual Information between response (rows), and predictors ().
}

\author{ Devorah Kengmana }

  
\examples{

m = matrix(runif(50), nrow = 5)

mi(m, 3, 3, 2)

}
