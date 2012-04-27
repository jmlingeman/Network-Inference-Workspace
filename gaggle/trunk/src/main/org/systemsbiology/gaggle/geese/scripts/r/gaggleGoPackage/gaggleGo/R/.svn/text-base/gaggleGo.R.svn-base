# gaggleGo.R: functions which combine the Bioconductor GOStats and the Gaggle packages
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
