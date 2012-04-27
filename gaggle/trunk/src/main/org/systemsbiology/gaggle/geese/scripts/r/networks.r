#--------------------------------------------------------------------------------------------------
broadcastGraph <- function (graph)
{
  cat ("broadcasting network with", length (nodes (graph)), "nodes\n")
  edgeList = edgeL (graph)
  edgeListSize = length (edgeList)
  for (i in 1:edgeListSize) {
    edgeInfo = edgeL (graph)[[i]]
    if (!is.null (edgeInfo)) {
      sourceNode = names (edgeL (graph) [i])
      targetNodes = nodes (graph)[edgeL (graph)[[1]]$edges]
      targetWeights = edgeL (graph)[[1]]$weights
      targetTypes = edgeL (graph)[[1]]$type
      cat ("broadcast", length (targetNodes), "edges from", sourceNode, "\n")
      cat ("     targets:", targetNodes, "\n")
      cat ("     weights:", targetWeights, "\n")
      cat ("       types:", targetTypes, "\n")
      if (length (targetNodes) == 1)  # 
        targetNodes <- as.vector (c (targetNodes, targetNodes))
      if (length (targetWeights) == 1)  # 
        targetWeights <- as.vector (c (targetWeights, targetWeights))
      if (length (targetTypes) == 1)  # 
        targetTypes <- as.vector (c (targetTypes, targetTypes))

     .jcall (goose, "V", "createAndBroadcastNetwork", sourceNode, targetNodes, 
                          targetWeights, targetTypes)
     cat ("---- after r broadcastGraph\n")
      } # if good edges
    } # for

} # broadcastGraph
#--------------------------------------------------------------------------------------------------
corToGraph <- function (source, correlations) 
{
  cat ('creating graph of correlations to ', source, '\n')

  targets = setdiff (names (correlations), source)
  targetCount = length (targets)
  if (targetCount == 0)
    return (NULL)

  nodes = union (source, targets)
  nodeCount = length (nodes)

  cat ('all nodes: ', nodes, '\n')
  cat ('target count is ', targetCount, '\n')

  edgeList = vector ("list", nodeCount)
  names (edgeList) <- nodes
 
  edges = 2:(targetCount + 1)     # indices of targets in nodes list
  weights = as.numeric (correlations [targets])
  edgeTypes = c ()
  for (i in 1:targetCount) edgeTypes = c (edgeTypes, 'correlation')
  edgeList [[1]] = list (edges=edges, weights=weights, type=edgeTypes)
  result = new ('graphNEL', nodes=nodes, edgeL=edgeList)
  invisible (result)

}
#--------------------------------------------------------------------------------------------------
addAllCorrelations <- function (correlationMatrix, threshold)
{
  max = length (rownames (correlationMatrix))
  for (i in 1:max) {
    geneName = rownames (correlationMatrix) [i]
    cat ('addAllCorrelations: ', geneName, '\n')
    correlationOfOneGene = correlationMatrix [geneName,]
    filteredCorrelations = correlationOfOneGene [abs (correlationOfOneGene) > threshold]
    g = corToGraph (geneName, filteredCorrelations)
    if (is.null (g)) next
    broadcast (g)
    #  cor1281 = mgoicor ['1281',]
    #broadcast (corToGraph ('1281', cor1281 [abs (cor1281) > 0.9]))
    }
}
#--------------------------------------------------------------------------------------------------
# taf1.genes = getNameList ()  # after broadcast of 22 genes from mhc network
# m = getMatrix ()
# dim (m) --> [1] 3535   12
# overlapping.genes = intersect (taf1.genes, rownames (m))
# m2 = m [overlapping.genes,]
# dim (m2) --> 13 12
# cm = cor (t (m2))
# dim (cm) --> 13 13   # rownames == colnames == overlapping.genes
# taf1 = '6890'
# taf1.correlations = cm [taf1,]
# taf1.correlations.filtered = taf1.correlations [taf1.correlations > 0.9]
#  -->      6890      3134       567 
#       1.0000000 0.9405320 0.9565688 
# corToGraph ('6890', filteredCorrelations)
#   creating graph of correlations to  6890 
#   all nodes:  6890 3134 567 
#   target count is  2
# g = corToGraph ('6890', filteredCorrelations)
# nodes (g) -->  "6890" "3134" "567" 
# edges (g) --> $"6890"  [1] "3134" "567"   (with some NULL's)
# edgeL (g) --> $"6890"
#               $"6890"$edges -> [1] 2 3
#               $"6890"$weights -> [1] 0.9405320 0.9565688
#               $"6890"$type -> [1] "correlation" "correlation"
# broadcast (g)
