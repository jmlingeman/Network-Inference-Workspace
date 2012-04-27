# gaggleUtil.R: general purpose (primarily convenience) R functions developed for microarray 
# data in the gaggle
##------------------------------------------------------------------------------------------------
normalize <- function (m)
# return a matrix which in which each row is normalized
{
  result <- m
  for (r in 1:dim (m)[1]) {
    avg <- mean (m [r,], na.rm=TRUE)
    variance <- var (m [r,], na.rm=TRUE)
    result [r,] <- result [r,] - avg
    result [r,] <- result [r,] / sqrt (variance)
    }

  invisible (result)

}
##--------------------------------------------------------------------------------
allPositiveRowNames <- function (matrix)
{
  rownames (matrix) [apply (matrix, 1, function (i) all (i > 0))]
}
##--------------------------------------------------------------------------------
filterVector <- function (vec, threshold, minCount, consecutive=TRUE)
# does <vec> have <minCount> elements above <threshold>, either consecutively
# or in any order?
# returns TRUE or FALSE
{
   indicesAbove <- which (vec >= threshold)
   #cat ('\nindices above: ', indicesAbove, '\n')
   if (length (indicesAbove) == 0)
     return (FALSE)

   indexCount = length (indicesAbove)
   #cat ('\nindexCount: ', indexCount, '\n')
   
   if (consecutive != TRUE)
     return (length (indicesAbove) >= minCount)

   if (indexCount == 1)
     return (minCount == 1)

   consecutivePassCount <- 0
   minCountAdjusted <- minCount - 1
   max = length (indicesAbove) - 1
   for (i in 1:max) {
     diff <- indicesAbove [i+1] - indicesAbove [i]
     if (diff == 1) {
       consecutivePassCount <- consecutivePassCount + 1
       if (consecutivePassCount >= minCountAdjusted) {
         return (TRUE)
         }
       }
     else {
       consecutivePassCount <- 0
       }
     } # for i

   return (FALSE)

} # filterVector
##--------------------------------------------------------------------------------
filterMatrix <- function (matrix, threshold, minCount, consecutive=TRUE)
{
  rownames (matrix) [apply (matrix, 1, function (i) filterVector (i, threshold, minCount, consecutive))]
}
#--------------------------------------------------------------------------------
fcor = function (g1, g2, ratios, lambdas, lambdaThreshold = 15)
# calculate the corrleation of the expression ratios of two genes, g1 and g2,
# in only those conditions (columns) in which both both genes have
# a lambda greater than lambdaThreshold
{
  if ((length (intersect (g1, rownames (lambdas))) == 0) ||
      (length (intersect (g2, rownames (lambdas))) == 0))
    return (NULL)
  
  g1.lambdas = lambdas [g1,]
  g2.lambdas = lambdas [g2,]
  g1SignificantConditions = names (g1.lambdas [g1.lambdas >= lambdaThreshold])
  g2SignificantConditions = names (g2.lambdas [g2.lambdas >= lambdaThreshold])
  sharedConditions =  intersect (g1SignificantConditions, g2SignificantConditions)
  if (length (sharedConditions) < 3) return (NA)

  percentSharedConditions = length (sharedConditions) * 100 /
                     (min (length (g1SignificantConditions), length (g2SignificantConditions)))
  correlation = cor (ratios [g1,sharedConditions], ratios [g2,sharedConditions])

  if ((abs (correlation) > .90) && percentSharedConditions > 80) {
    fixedCor = (as.integer (correlation * 100))
    fixedShared = (as.integer (percentSharedConditions))
    }

  result = list ()
  result$rowNames = c (g1, g2)
  result$columnNames = sharedConditions
  result$correlation = correlation

  return (result)

} # fcor
#----------------------------------------------------------------------------------------------------------
sharedSigCondsSearch = function (gene, lambdas, lambdaThreshold=15, overlapToBeInteresting=.5)
# find all the genes which share an 'interesting' number of significant conditions 
# with the provided gene. 
{
  gene.lambdas = lambdas [gene,]
  geneSigConds = names (gene.lambdas [gene.lambdas >= lambdaThreshold])
  allGenes = rownames (lambdas)
  
  result = list ()
  result$gene = gene
  result$significantConditions = geneSigConds

  for (otherGene in allGenes) {
    other.lambdas = lambdas [otherGene,]
    otherSigConds = names (other.lambdas [other.lambdas >= lambdaThreshold])
    overlapConds = intersect (geneSigConds, otherSigConds)
    overlapPercentage = length(overlapConds)/length(geneSigConds)
    if (overlapPercentage > overlapToBeInteresting) {
       result [otherGene] = overlapPercentage
       }
    } # for gene

  return (result)

} # sharedSigCondsSearch
#----------------------------------------------------------------------------------------------------------
testGaggleUtilPackage <- function()
{
  require ("RUnit", quietly=TRUE) || stop ("RUnit package not found")
  testScript = paste (system.file (package="gaggleUtil"), "unitTests/gaggleUtilTest.R", sep="/")
  file.exists (testScript) || stop (paste (testScript, "not found."))
  source (testScript)
  test.gaggleUtil ()

} # testGaggleUtilPackage
#-------------------------------------------------------------------------------------------------------
#shortestPath <- function (g, startNode, endNode)
#{
#
#  require ("RGBL", quietly=TRUE) || stop ("RGBL package not found")
#  sp = sp.between (g, startNode, endNode)
#  endpointsPathSelector = paste (startNode, endNode, sep=":")
#  result = sp [[endpointsPathSelector]][["path"]]
#  return (result)
#
#} # shortestPath
#-------------------------------------------------------------------------------------------------------
