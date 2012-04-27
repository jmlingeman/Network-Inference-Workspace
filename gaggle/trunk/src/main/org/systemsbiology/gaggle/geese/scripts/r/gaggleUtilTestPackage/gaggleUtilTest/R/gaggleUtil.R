# gaggleUtil.R: general purpose (mostly convenience) R functions developed for microarray 
# data in the gaggle
#------------------------------------------------------------------------------------------------
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
normalizeBK <- function (m)
# burak's (hence, 'BK') proposal for a much faster, loop-less normalizing function.
# the documentation suggest that it handles NA's properly
{
  trans = t (m)
  scaled = scale (trans, center = TRUE, scale = apply (trans,2,sd))
  t (scaled)

}
#--------------------------------------------------------------------------------------------------
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
allPositiveRowNames <- function (m)
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
goEnrichment <- function (genes, lib='hgu95av2', ontology='MF')
{
  require (GO) || stop ("no GO library, giving up in function goEnrichment")
  require (GOstats) ||  stop ("no GOstats library, giving up in function goEnrichment")
  cat ('creating', ontology, 'GO graph...\n')
  goGraph = makeGOGraph (genes, Ontology=ontology)
  cat (paste ('broadcasting', ontology, 'graph to', getTargetGoose (), '\n'))
  broadcast (goGraph)

  cat (paste ('creating', ontology, 'GO Hypergraph\n'))
  #goHyperGraph = GOHyperG (genes, lib=lib, what=ontology)
  goHyperGraph = GOalG (genes, what=ontology)

  cat (paste ('broadcast pvalues to ', getTargetGoose (), '\n'))
  goHyperGraph$pvalues [goHyperGraph$pvalues == 0.0] = 1.0
  broadcast (goHyperGraph$pvalues, 'pvalue')

  cat (paste ('broadcast data set incidences ', getTargetGoose (), '\n'))
  broadcast (goHyperGraph$intCounts, 'dataSetCount')

  cat (paste ('broadcast organism-wide incidences to ', getTargetGoose (), '\n'))
  broadcast (goHyperGraph$goCounts, 'organismCount')

  cat (paste ('broadcast processed organism-wide incidences to ', getTargetGoose (), '\n'))
  rarity = 1.0 / log10 (goHyperGraph$goCounts)
  broadcast (rarity, 'rarity')

  termToGeneMap = getTermToGeneMappings (genes)
  cat (paste ('broadcast gene names to ', getTargetGoose (), '\n'))
  broadcast (termToGeneMap, 'genes')

  return (goHyperGraph)

} # goEnrichment
#--------------------------------------------------------------------------------------------------
getTermToGeneMappings <- function (genes)
{
  goTermMap = mget (genes, env = GOLOCUSID2GO, ifnotfound = NA)
  geneNames = unique (names (goTermMap))

  result = new.env ()
  for (gene in geneNames) {
    goTerms = unique (names (goTermMap [gene][[1]]))
    for (goTerm in goTerms) {
      #print (paste (goTerm, gene))
      if (is.na (match (goTerm, ls (result))))
        geneList = c ()
      else
        geneList = get (goTerm, result)
      geneList = c (geneList, gene)
      assign (goTerm, geneList, envir=result)
      } # for goTerm
    } # for gene

  result

} # getTermToGeneMappings
#--------------------------------------------------------------------------------------------------
GOalG <- function (x, envs = c("GOLOCUSID2GO","GOALLLOCUSID"), what = "MF")
{  require(GO) || stop("no GO library")
      if (any(duplicated(x)))
        stop("input IDs must be unique")
    match.arg(what, c("MF", "BP", "CC"))
    cLLs <- names(as.list(get(envs[1],  mode = "environment")))
    ourLLs <- cLLs[match(x, cLLs)]
    goV <- as.list(get(envs[2], mode = "environment"))
    whWeHave <- sapply(goV, function(y) {
        if (is.na(y) || length(y) == 0)
            return(FALSE)
            any(x %in% y)
    })
    goV <- goV[whWeHave]
    goCat <- unlist(getGOOntology(names(goV)))
    goodGO <- goCat == what
    mm <- match(names(goCat), names(goV))
    mm <- mm[goodGO]
    goV <- goV[mm]
    goVcts = sapply(goV, function(x) {
        if (length(x) == 0 || is.na(x))
            return(NA)
        lls <- unique(x)
        lls
    })
    bad <- sapply(goVcts, function(x) (length(x) == 1 && is.na(x)))
    goVcts = goVcts[!bad]
    goV = goV[!bad]
    cLLs <- unique(unlist(goVcts))
    nLL <- length(cLLs)
    goCounts <- sapply(goVcts, length)
    ourLLs <- unique(ourLLs[!is.na(ourLLs)])
    ours <- ourLLs[!duplicated(ourLLs)]
    whGood <- ours[ours %in% cLLs]
    nInt = length(whGood)
    if (nInt == 0)
        warning("no interesting genes found")
    useCts <- sapply(goVcts, function(x) sum(whGood %in% x))
    pvs <- phyper(useCts-1, nInt, nLL - nInt, goCounts, lower.tail = FALSE)
    ord <- order(pvs)
    return(list(pvalues = pvs[ord], goCounts = goCounts[ord],
        background = "whole_genome", go2LL = goV, intCounts = useCts[ord], numLL = nLL,
        numInt = nInt, intLLs = x))
}
#--------------------------------------------------------------------------------------------------
