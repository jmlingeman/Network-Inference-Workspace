# gaggleUtilTest.R:  unit tests for functions in gaggleUtil.R
#---------------------------------------------------------------------------------
.First.lib <- function (libname, pkgname)
{
  cat ('running gaggleTest package 0.99-22\n')
  require (gaggleUtil)
  require (RUnit)
  
} # first lib
#-------------------------------------------------------------------------------------
test.gaggleUtil <- function ()
{
  data (gamma)
  test.normalize ()
  test.normalize.withNAs ()
  test.allPositiveRowNames ()
  test.filterVector ()
  test.fcor ()
  test.sharedSigCondsSearch ()
  
}
#-------------------------------------------------------------------------------------
test.normalize <- function ()
# the ratios are a complete 2400 x 16 matrix of expression ratios
# normalize it, and check that each row has a zero mean variance of one
{
  normalizedRatios = normalize (gamma.ratios)
  checkEquals (dim (gamma.ratios), c (2400, 16))
  checkEquals (dim (normalizedRatios),  c (2400, 16))
  invisible (apply (normalizedRatios, 1, function (row) checkTrue (abs (mean (row)) < 1.0e-10)))
  invisible (apply (normalizedRatios, 1, function (row) checkEquals (var (row), 1)))
}
#-------------------------------------------------------------------------------------
test.normalize.withNAs <- function ()
# generate a 10 x 10 matrix, with one NA placed in a random column in each row
# ensure that each row of the normalized matrix has mean 0 and variance 1
{
  set.seed (123)

  m = matrix (1:100, nrow=10, ncol=10, byrow=TRUE)
  for (i in 1:10) {
    columnNumber = as.integer (runif (1) * 10) + 1
    m [1, columnNumber] = NA
    }

  nm = normalize (m)
  checkEquals (dim (m), c (10, 10))
  checkEquals (dim (nm), c (10, 10))
  invisible (apply (nm, 1, function (row) checkTrue (abs (mean (row, na.rm=TRUE)) < 1.0e-10)))
  invisible (apply (nm, 1, function (row) checkEquals (var (row, na.rm=TRUE), 1)))

} # test.normalize.withNAs
#-------------------------------------------------------------------------------------
test.allPositiveRowNames <- function ()
{
  rowNames = allPositiveRowNames (gamma.ratios)
  checkEquals (length (rowNames), 81)

    ## check a few at random
  for (r in c (1, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 53)) {
    row = gamma.ratios [rowNames [r], ]
    checkEquals (length (row [row > 0]), length (row))
    }

} # test.allPositiveRowNames
#-------------------------------------------------------------------------------------
test.filterVector <- function ()
{
  vec = c (1, 2, 3, 1, 4)
  checkTrue (filterVector (vec, 0, 5, consecutive=T))
  checkTrue (filterVector (vec, 1, 5, consecutive=T))

  checkTrue (!filterVector (vec, 2, 5, consecutive=T))
  checkTrue (!filterVector (vec, 2, 3, consecutive=T))

  checkTrue (filterVector (vec, 2, 3, consecutive=F))


} # test.filterVector
#-------------------------------------------------------------------------------------
test.fcor <- function ()
{
  g1 = 'VNG0055H'
  g2 = 'VNG0057H'
  result = fcor (g1, g2, gamma.ratios, gamma.lambdas, 10.0)
  checkEquals (result$rowNames, c ("VNG0055H", "VNG0057H"))
  checkEquals (result$columnNames, 
               c ("gamma__0000gy-0000m", "gamma__0000gy-0060m", "gamma__2500gy-0040m"))
  checkEqualsNumeric (result$correlation, 0.990223, tolerance=1.0e-5)

}
#-------------------------------------------------------------------------------------
test.sharedSigCondsSearch <- function ()
{
  gene = "VNG1213C"
  lambdaThreshold = 15
  overlapToBeInteresting = 0.9
  result = sharedSigCondsSearch (gene, gamma.lambdas, lambdaThreshold, overlapToBeInteresting)

  checkEquals (names (result),  c ("gene", "significantConditions",
                                   "VNG1132G", "VNG1137G", "VNG1213C", "VNG2469G"))
  checkEquals (result$gene, gene)
  checkEquals (length (result$significantConditions), 15)

  checkEqualsNumeric (result$VNG2469G, 0.9333333, tolerance=1.0e-5)
  checkEqualsNumeric (result$VNG1137G, 0.9333333, tolerance=1.0e-5)
  checkEqualsNumeric (result$VNG1213C, 1, tolerance=1.0e-5)   # the original gene
  checkEqualsNumeric (result$VNG2469G, 0.9333333, tolerance=1.0e-5)

} # test.sharedSigCondsSearch
#-------------------------------------------------------------------------------------
