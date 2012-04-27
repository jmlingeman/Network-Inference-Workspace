#--------------------------------------------------------------------------------------------
def findLogicGateNodes (cw):

  nodeAttributes = cw.getNodeAttributes ()
  nodes = g.getNodeArray ()
  result = []
  for node in nodes:
    canonicalName = cw.getCanonicalNodeName (node)
    nodeType = nodeAttributes.getStringValue ('type', canonicalName)
    #print '%12s: %s' % (canonicalName, nodeType)
    if (nodeType == 'logicGate'):
      result.append (node)

  return result

#--------------------------------------------------------------------------------------------
def countRegulatoryEdges (cw, logicGateNode):

  ea = cw.getEdgeAttributes ()
  ec = logicGateNode.edges ()
  count = 0
  while (ec.ok ()):
    edgeName = ea.getCanonicalName (ec.edge ())
    edgeType = ea.getStringValue ('interaction', edgeName)
    if (edgeType in ['activates', 'represses']):
     count += 1
    ec.next ()

  return count
  
#--------------------------------------------------------------------------------------------
def hideAllEdges (cw, node):

  cw.getGraphHider().hide (node.edges ())

#--------------------------------------------------------------------------------------------
logicGateNodes = findLogicGateNodes (cw)
for node in logicGateNodes:
  nodeName = cw.getCanonicalNodeName (node)
  regulatorEdgeCount = countRegulatoryEdges (cw, node)
  print '%12s: %d' % (nodeName, regulatorEdgeCount)
  if (regulatorEdgeCount == 0):
    hideAllEdges (cw, node)

cw.redrawGraph ()

