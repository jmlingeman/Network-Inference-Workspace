# utilities.r:  general purpose (mostly convenience) R functions developed for 
# exploring halobacterium experimental data in the gaggle
#------------------------------------------------------------------------------------------------
scriptVersion <- function ()
{
  return ("halo-utilities.r $Revision: 130 $   $Date: 2005-08-13 17:48:09 -0700 (Sat, 13 Aug 2005) $");
}
#--------------------------------------------------------------------------------------------------
normalizeBK <- function (m)
# burak's (hence, 'BK') proposal for a much faster, loop-less normalizing function.
# the documentation suggest that it handles NA's properly
{
  trans = t (m)
  scaled= scale (trans, center = TRUE, scale = apply (trans,2,sd))
  t (scaled)

}
#--------------------------------------------------------------------------------------------------
normalize <- function (m)
# return a matrix which in which each row is normalized
{
  result <- m
  for (r in 1:dim (m)[1]) {
    #cat ("row ", r, " has length ", length (m [r,]), "\n")
    avg <- mean (m [r,])
    variance <- var (m [r,])
    result [r,] <- result [r,] - avg
    #result [r,] <- result [r,] / variance
    result [r,] <- result [r,] / sqrt (variance)
    }

  result

}
#--------------------------------------------------------------------------------
normalizeNA <- function (m)
# return a matrix which in which each row is normalized; ignore NA's.
# todo: what happens if all values are NA?  need unit test.  promote this to 
# todo: replace 'normalize' once unit tests are in place    
{
  result <- m
  for (r in 1:dim (m)[1]) {
    avg <- mean (m [r,], na.rm=T)
    variance <- var (m [r,], na.rm=T)
    result [r,] <- result [r,] - avg
    result [r,] <- result [r,] / sqrt (variance)
    }

  result
}
#--------------------------------------------------------------------------------
allPos <- function (m)
{
  rownames (m) [apply (m, 1, function (i) all (i > 0))]
}
#--------------------------------------------------------------------------------
filterVector <- function (vec, threshold, minCount, consecutive=T)
 # does <vec> have <minCount> elements above <threshold>, either consecutively
 # or in any order?
 # returns T or F
{
   indicesAbove <- which (vec >= threshold)
   #cat ('\nindices above: ', indicesAbove, '\n')
   if (length (indicesAbove) == 0)
     return (F)

   indexCount = length (indicesAbove)
   #cat ('\nindexCount: ', indexCount, '\n')
   
   if (consecutive != T)
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
         return (T)
         }
       }
     else {
       consecutivePassCount <- 0
       }
     } # for i

   return (F)

} # filterVector
#--------------------------------------------------------------------------------
filterMatrix <- function (matrix, threshold, minCount, consecutive=T)
{
  rownames (matrix) [apply (matrix, 1, function (i) filterVector (i, threshold, minCount, consecutive))]

}
#--------------------------------------------------------------------------------
