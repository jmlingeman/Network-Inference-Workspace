\name{fcor}
\alias{fcor}
\title{Correlation of Expression Ratios of Two Genes in Significant Conditions Only}
\description{Given a matrix of gene expression ratios, and expression lambda values (in which larger
values indicate statistical signficance; 15.0 being a typical threshold), calculate the correlation of 
the expression ratios of two genes, g1 and g2, in only those conditions (columns) in which both both genes have
a lambda greater than lambdaThreshold.}
\usage{
  fcor (g1, g2, ratios, lambdas, lambdaThreshold = 15)
}

\arguments{
  \item{g1}{name of the first gene}
  \item{g2}{name of the second gene}
  \item{ratios}{a matrix of expression ratios}
  \item{lambdas}{a matrix of lambda values, with the same dimensions, and the same row and column names,
                 as the ratios}
  \item{lambdaThreshold}{significance threshold; 15 is typical, and the default}
  }

\value{Returns a list with three slots: rownNames, columnNames, correlation.}
\examples{
  library (gaggleUtil)
  data (gamma)
  result = fcor ("VNG0745G", "VNG0748G", gamma.ratios, gamma.lambdas, 15.0)

    # now examine the results, first printing the names of the slots
    # then which significant experimental conditions were shared by the two genes
    # and finally, the correlation of expression ratio in those conditions

  names (result)      # "rowNames"    "columnNames" "correlation"
  result$columnNames  # "gamma__2500gy-0040m" "gamma__2500gy-0060m" "gamma__2500gy-0240m"
  result$correlation  #  0.9788373
}


\keyword{array}
\keyword{manip}
