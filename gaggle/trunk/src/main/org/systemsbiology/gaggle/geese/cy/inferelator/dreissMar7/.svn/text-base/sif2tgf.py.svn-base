file = 'network.sif'
lines =  open (file).read().split ('\n')

nodes = {}
nodeCount = 0
edges = []

for line in lines: # [:10]:
  tokens = line.split ();
  if (len (tokens) == 3):
    a = tokens [0]
    b = tokens [2]
    edgeName = tokens [1]

    if (not nodes.has_key (a)):
      nodeCount += 1
      nodes [a] = nodeCount
      print '%d %s' % (nodeCount, a)
    aIndex = nodes [a]

    if (not nodes.has_key (b)):
      nodeCount += 1
      nodes [b] = nodeCount
      print '%d %s' % (nodeCount, b)
    bIndex = nodes [b]
    edges.append ('%d %d %s' % (aIndex, bIndex, edgeName))

print '#'
for edge in edges:
  print edge
