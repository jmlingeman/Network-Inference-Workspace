# gaggle.R
#---------------------------------------------------------------------------------
.onLoad <- function (libname, pkgname)
{
  #require ("methods")
  require ("rJava")
  libname = gsub(" ", "%20", libname, "+")
  cat (paste ('\nonLoad -- libname:', libname, 'pkgname:', pkgname, '\n'))

    # All Bioconductor packages should use an x.y.z version scheme. The following rules apply:
    # The y number should be odd for packages in devel and even for packages in release.
    # This makes it easier for users to know whether the package they have installed
    # is release or devel.  We encourage package maintainers to increment z whenever
    # committing changes to a package in devel. Any change committed to a released
    # package, no matter how small, must bump z.
                                                            
  fullPathToGaggleJar = paste (libname, pkgname, 'jars', 'gaggleRShell.jar', sep=.Platform$file.sep)
  cat ('path to jar:', fullPathToGaggleJar, '\n')
  cat ('      script: ', .scriptVersion (), '\n')

  # Before starting VM, set the system classpath to a blank string
  # this gets around a bug where RMI objects fail to unmarshal
  # if the classpath has spaces in it (which it often does on
  # Windows machines). Yes, this is a weird error.

  Sys.unsetenv("CLASSPATH")


  .jinit (fullPathToGaggleJar)
  cat ('  os version: ', .jcall ("java/lang/System", "S", "getProperty", "os.name"),'\n')

    #-----------------------------------------------------------
    # as of february 2006, the gaggle requires java 1.5.
    # bail out if the version found by .jinit is some other version
    # java versions on macos can be especially confusing.  the
    # url supplied below explains what to do
    #-----------------------------------------------------------
  
  jvmVersion = .jcall ("java/lang/System", "S", "getProperty", "java.version")
  cat (' jvm version: ', jvmVersion, '\n')
  if (is.na (pmatch ("1.5.", jvmVersion)) & is.na (pmatch ("1.6.", jvmVersion))) {
    cat ('\n   You are using the wrong version of Java.\n',
            '  Please see http://gaggle.systemsbiology.org/docs/html/java\n\n')
    return ()
    }
} # first lib 
#---------------------------------------------------------------------------------
gaggleInit <- function (bossHost = 'localhost')
# the user must call this, to create the java R goose, to register it with
# the boss, and before sending or receiving any broadcasts
{

  cat (paste(' initializing gaggle package', .pkgVersion(), '(2007-04)\n'))
  goose <<- .jnew ("org/systemsbiology/gaggle/geese/rShell/RShellGoose", bossHost)
  tester = geese ()
  if (is.null (tester)) {
    cat ('\n\n\tFailed to connect to a Gaggle Boss.  Is one running on your computer?\n')
    cat ('\tUse this Java Web Start link to start a boss:\n')
    cat ('\t\thttp://gaggle.systemsbiology.org/2007-04/boss.jnlp\n')
    cat ('\tYou need to exit R, start the boss, re-enter R, and load the gaggle package once again.\n\n')
    }
  else {
    targetGoose <<- "boss"
    cat ('        name: ', .jcall (goose, "S", "getName"), "\n")
    cat (' RShellGoose: ', .jcall (goose, "S", "getVersion"), "\n")
    }

} # gaggleInit 

.pkgVersion <- function () 
{
	return (as.character(sessionInfo()$otherPkgs$gaggle)[2])
}

#---------------------------------------------------------------------------------
.scriptVersion <- function ()
{
  return ("gaggle.R $Revision: 4499 $   $Date: 2010-08-10 20:15:03 -0400 (Tue, 10 Aug 2010) $");
}
#---------------------------------------------------------------------------------
getNameList <- function ()
{
  nameList <- .jcall (goose, "[S", "getNameList")
  return (nameList)
}
#---------------------------------------------------------------------------------
getCluster <- function ()
{
  result = list ()
  result$name =  .jcall (goose, "S", "getClusterName")
  result$rowNames = .jcall (goose, "[S", "getClusterRowNames")
  result$columnNames = .jcall (goose, "[S", "getClusterColumnNames")
  
  return (result)
}
#---------------------------------------------------------------------------------

getTuple <- function ()
{
  hash = new.env ()
  hash$title = .jcall (goose, "S", "getTupleTitle")
  #hash$attributeName = .jcall (goose, "S". "getTupleAttributeName") #leave this be for now; it might mess things up
  keys =   .jcall (goose, "[S", "getTupleKeys")
  rawValues = .jcall (goose, "[S", "getTupleValues")
  #print (keys)
  #print (rawValues)

  options (warn = -1)  # failed conversion to numeric prints an message to stdout; disable it

  for (i in seq (length (keys))) {
    value = as.numeric (rawValues [i])
    if (is.na (value)) value = rawValues [i]
    assign (keys [i], value, envir=hash)
    }

  options (warn = 1)  # turn warnings back on

  return (hash)

} # getHashMap


getMatrix <- function ()
{
  rowCount <- .jcall (goose, "I", "getMatrixRowCount")
  columnCount <- .jcall (goose, "I", "getMatrixColumnCount")
  matrixRowNames <- .jcall (goose, "[S", "getMatrixRowNames")
  matrixColumnNames <- .jcall (goose, "[S", "getMatrixColumnNames")
  #cat ("rows: ", rowCount, "\n");
  #cat ("cols: ", columnCount, "\n");
  #cat ("row names ", matrixRowNames, "\n");
  #cat ("col names ", matrixColumnNames, "\n");

  data <- .jcall (goose, "[D", "getAllMatrixData");
  
  m <- matrix (data, nrow=rowCount, ncol=columnCount, byrow=T,
                dimnames = list (matrixRowNames, matrixColumnNames))
  
  return (m)

}
#---------------------------------------------------------------------------------
getNetwork <- function (directed=T)
{
  edgeStrings          = .jcall (goose, "[S", "getNetworkAsStringArray")
  nodeAttributeStrings = .jcall (goose, "[S", "getNetworkNodeAttributesAsStringArray")
  edgeAttributeStrings = .jcall (goose, "[S", "getNetworkEdgeAttributesAsStringArray")
  result = .gaggleNetworkToGraphNEL (edgeStrings, nodeAttributeStrings, edgeAttributeStrings, directed) 
  #print (paste ('getNetwork: ', result))
  return (result)
}
#---------------------------------------------------------------------------------
#getHashMap <- function ()
#{
#  hash = new.env ()
#  hash$title = .jcall (goose, "S", "getHashMapTitle")
#  keys =   .jcall (goose, "[S", "getHashMapKeys")
#  rawValues = .jcall (goose, "[S", "getHashMapValues")
#  #print (keys)
#  #print (rawValues)
#
#  options (warn = -1)  # failed conversion to numeric prints an message to stdout; disable it
#
#  for (i in seq (length (keys))) {
#    value = as.numeric (rawValues [i])
#    if (is.na (value)) value = rawValues [i]
#    assign (keys [i], value, envir=hash)
#    }
#
#  options (warn = 1)  # turn warnings back on
#
#  return (hash)
#
#} # getHashMap
#---------------------------------------------------------------------------------
.graphNELtoGaggleNetwork  <- function (g)
# the name of this function is a slight exaggeration:  we don't really create
# a Gaggle Network object here, but we do translate the R graph into 3 String
# arrays, which are easy to send to Java, and from which RShellGoose.java can
# easily construct a real Gaggle Network.
# 
# these are the string types
#
#   edges:           "VNG0723G::VNG1233G::PhylogeneticProfile"
#   edge attributes: "VNG0723G::VNG1233G::PhylogeneticProfile::confidence::0.533"
#   node attributes: "VNG1233G::commonName::pepq2"
{
     # set some default values.  these will be assigned in this function.
     # node attributes may not exist, but the other two will, always.
  
   edgeStringList = NA
   nodeAttributeStringList = NA
   edgeAttributeStringList = NA
  
   # does it have an explicit edge type?  gaggle network graphs always do, but
   # those originating in R may not.  supply one if needed

  hasExplicitEdgeType = .listMember ("edgeType", names (edgeDataDefaults (g)))
  if (!hasExplicitEdgeType)
    edgeDataDefaults (g, "edgeType") = "edge"

   #------------------------------------------------------------------------
   # create the edge strings:  nodeA::nodeB:<edgeType>
   # and the degenerate case:  nodeC
   #------------------------------------------------------------------------

  edgeStringList = c ()
  for (node in nodes (g)) {
    #print (paste ('looking for nodes linked to', node))
    edgePartners = edges (g)[[node]]
    #print (paste ('     partners:', edgePartners))
    if (length (edgePartners) == 0) {
      edgeString = node
      if (!.listMember (edgeString, edgeStringList))
        edgeStringList = c (edgeStringList, edgeString)
      } # a degenerate edge, just a node
    else {
      for (partner in edgePartners) {
        nodeA = node
        nodeB = partner
          # keep edge strings nodes alphabetically sorted, so we can check for duplicates
        if (!isDirected (g) && nodeA > nodeB) { 
          tmp = nodeB
          nodeB = nodeA
          nodeA = tmp
          }
        edgeType = edgeData (g, nodeA, nodeB, "edgeType")
        edgeString = paste (nodeA, nodeB, edgeType, sep='::')
        #print (paste ('new edge:', edgeString))
        if (!.listMember (edgeString, edgeStringList))
           edgeStringList = c (edgeStringList, edgeString)
        } # for partner
      } # else: a real edge
    }# for node

   #------------------------------------------------------------------------------
   # now create the node attribute strings:  node::attributeName::attributeValue
   #------------------------------------------------------------------------------

  nodeAttributeNames = names (nodeDataDefaults (g))
  if (length (nodeAttributeNames) > 0) {
    nodeAttributeStringList = c ()
    for (attributeName in nodeAttributeNames) {
      for (node in nodes (g)) {
        value = nodeData (g, node, attributeName )[[1]]
        noaString = paste (node, attributeName, value, sep='::')
        nodeAttributeStringList = c (nodeAttributeStringList, noaString)
        } # for node
      } # for attributeName
   }# if > 0

   #------------------------------------------------------------------------------
   # lastly,  create the edge attribute strings:  
   #    nodeA::nodeB::edgeType::attributeName::attributeValue
   #------------------------------------------------------------------------------

  edgeAttributeStringList = c ()
  edgeAttributeNames = names (edgeDataDefaults (g))

    # partner nodes (sharing an edge) have already been identifed, and stroed in edgeStringList
    # exploit that rather than searching again

  for (attributeName in edgeAttributeNames) {
    for (edgeString in edgeStringList) {
      tokens = unlist (strsplit (edgeString, "::"))
      if (length (tokens) != 3) # is this a degenerate 'edge' -- just a node?
        next
      nodeA = tokens [1]
      nodeB = tokens [2]
      edgeType = tokens [3]
      attributeValue = edgeData (g, nodeA, nodeB, attributeName)[[1]]
      edaString = paste (nodeA, nodeB, edgeType, attributeName, attributeValue, sep="::")
      edgeAttributeStringList = c (edgeAttributeStringList, edaString)
      } # for edgeString
    } # for attributeName

   results = list ()
   results$edges = edgeStringList
   results$noa = nodeAttributeStringList
   results$eda = edgeAttributeStringList

   return (results)

} # .graphNELtoGaggleNetwork 
#---------------------------------------------------------------------------------
.gaggleNetworkToGraphNEL <- function (edges, noas, edas, directed)
# it's easy to send arrays of strings between java and R, so that's what we rely on.
# there are three types of strings, each of which has some 'magic' coding, as you can 
# see below:
#   edges consist of strings like  "VNG0723G::VNG1233G::PhylogeneticProfile"
#   edge attributes: "VNG0723G::VNG1233G::PhylogeneticProfile::confidence::0.533"
#   node attributes: "VNG1233G::commonName::pepq2"
{
   # ---- create the graph, add nodes, add edges between them, 
   #      add edgeType attributes (which are implicit in the edge strings)

  #print (cat ('lkjsadf;lkjasdf;lkjasdf;lkjasd;lfgkj'))
  edgeMode = "directed"
  if (directed) {
    edgeMode = "directed"
  } else {
    edgeMode = "undirected"
  }
  g = new ("graphNEL", edgemode=edgeMode)
  edgeDataDefaults (g, "edgeType") = ""

  if (!is.null (edges) && !is.na (edges)) for (edge in edges) {
    tokens = unlist (strsplit (edge, '::'))
    if (length (tokens) == 1) {
      orphanNode = tokens [1]
      if (!.listMember (orphanNode, nodes (g)))
         g = addNode (tokens [1], g)
      } # 1 token only
    else if (length (tokens) == 3) {
      nodeA = tokens [1]
      nodeB = tokens [2]
      edgeType = tokens [3]
      if (!.listMember (nodeA, nodes (g)))
        g = addNode (nodeA, g)
      if (!.listMember (nodeB, nodes (g)))
        g = addNode (nodeB, g)
      g = addEdge (nodeA, nodeB, g)
      edgeData (g, from=nodeA, to=nodeB, attr="edgeType") = edgeType
      } # 3 tokens
    } # for

   # ---- now traverse the explicit edge attributes, and add them
  if ((!length (edas) == 0) && !is.null (edas) && !is.na (edas)) for (eda in edas) {
    tokens = unlist (strsplit (eda, '::'))
    nodeA = tokens [1]
    nodeB = tokens [2]
    edgeType = tokens [3]  # we ignore this for now, since graph allows only 1 edge
    attributeName = tokens [4]
    #print (paste ('edge attribute name:', attributeName))
    rawAttributeValue = tokens [5]
    options (warn = -1)
    attributeValue = as.numeric (rawAttributeValue)
    options (warn = 1)
    if (is.na (attributeValue)) attributeValue = rawAttributeValue
     # is this an unknown edge attribute type?  if so, initialize it
    if (!.listMember (attributeName, names (edgeDataDefaults (g)))) {
      if (attributeName == 'weight')
        edgeDataDefaults (g, attributeName) = 1.0
      else
        edgeDataDefaults (g, attributeName) = ""
      } # if new edge attribute
    edgeData (g, from=nodeA, to=nodeB, attr=attributeName) = attributeValue
  } # if edge attributes

  if ((!length (noas) == 0) && !is.null (noas) && !is.na (noas)) for (noa in noas) {
    tokens = unlist (strsplit (noa, '::'))
    node = tokens [1]
    attributeName = tokens [2]
    rawAttributeValue = tokens [3]
     # is this an unknown node attribute type? if so, initialize it
    if (!.listMember (attributeName, names (nodeDataDefaults (g))))
      nodeDataDefaults (g, attributeName) = ""
    options (warn = -1)
    attributeValue = as.numeric (rawAttributeValue)
    options (warn = 1)
    if (is.na (attributeValue)) attributeValue = rawAttributeValue
    nodeData (g, node, attributeName) = attributeValue
    } # for

  return (g)

}# .gaggleNetworkToGraphNEL
#-------------------------------------------------------------------------------------------
# test whether the given object is an instance of a Java Tuple object
isJavaTuple <- function(obj) {
	return (class(obj)=="jobjRef" && isS4(obj) && slot(obj, "jclass")=="org/systemsbiology/gaggle/core/datatypes/Tuple" && !is.jnull(obj))
}
#-------------------------------------------------------------------------------------------

# get the Java class name of an rJava object
getJavaClassName <- function(jobj, quiet=FALSE) {
	if (class(jobj)=='jobjRef') {
		if (is.jnull(jobj)) {
			return("null")
		}
		return(.jcall(.jcall(jobj, 'Ljava/lang/Class;', 'getClass'), 'S', 'getName'))
	}
	else {
		if (!quiet) {
			cat("Error in getJavaClassName: Not a java object?\n")
		}
		return("Not a java object")
	}
}

#-------------------------------------------------------------------------------------------

broadcast <- function (x, name='from R')
{
  if (is.matrix (x) || is.data.frame(x)) {
     #cat ('broadcasting matrix, name: ', name, '\n')
       # java stores matrices in row-major order, R uses column-major, so be sure to
       # transpose the actual data before sending it to java
    if (is.null(rownames(x)) || is.null(colnames(x))) {
      cat('Error: row and column names required to broadcast matrix')
    } else {
      .jcall (goose, "V", "createAndBroadcastMatrix", rownames (x), colnames (x),
                          as.numeric (as.vector (t(x))), name)
    }
  }

  else if (is.vector (x)) {
    if (length (intersect (c("rowNames", "columnNames"), names (x))) == 2) {
      rowNames = x$rowNames
      columnNames = x$columnNames
     .jcall (goose, "V", "broadcastCluster", name, rowNames, columnNames)
      }
    else if (!is.null (names (x))) {
      .broadcastAssociativeArray (x, name)
    } else {
      if (length (x) == 1)  # 
		x <- as.vector(c(x))
      .jcall (goose, "V", "broadcastList", x, name)
      } # unnamed list
    } # vector
 
  # Broadcast a (Java) Tuple object.
  # Added by JCB to support genome browser. 
  else if (isJavaTuple(x)) {
    if (name!='from R') {
      .jcall(x, "V", "setName", name)
    }
    .jcall(goose, "V", "broadcastTuple", x) 
  }

  else if (class (x) == "graphNEL") {
    .broadcastGraph (x, name)
    }

  else if (class (x) == "environment") {
    .broadcastEnvironment (x, name)
    #.broadcastAssociativeArray(x, name)
    }

  else {
    cat ("no support yet for broadcasting variables of type ", typeof (x), "\n")
    }
  invisible (NULL)
  
} # generic broadcast
#---------------------------------------------------------------------------------
geese <- function ()
{
  return (.jcall (goose, "[S", "getGeeseNames"));
  
}
#------------------------------------------------------------------------------------------------
getTargetGoose <- function ()
{
  return (.jcall (goose, "S", "getTargetGoose"))

} # getTargetGoose
#--------------------------------------------------------------------------------------------------
setTargetGoose <- function (gooseName)
{
  targetGoose <<- gooseName
  .jcall (goose, "V", "setTargetGoose", gooseName)
  invisible (NULL)

} # setTargetGoose
#--------------------------------------------------------------------------------------------------
setSpecies <- function (newValue)
{
  .jcall (goose, "V", "setSpecies", newValue)
  invisible (NULL)

} # setSpecies
#--------------------------------------------------------------------------------------------------
getSpecies <- function ()
{
  return (.jcall (goose, "S", "getSpecies"))

} # getSpecies
#--------------------------------------------------------------------------------------------------
showGoose <- function (target=NULL)
{
  if (is.null (target))
    target = getTargetGoose ()

  .jcall (goose, "V", "show", target);
  invisible (NULL)
  
}
#------------------------------------------------------------------------------------------------
hideGoose <- function (target=NULL)
{
  if (is.null (target))
    target = getTargetGoose ()

  .jcall (goose, "V", "hide", target);
  invisible (NULL)
  
}
#------------------------------------------------------------------------------------------------
.getEdgeType = function (edgeList, targetNodeIndex)
{
  if (.listMember ("type", names (edgeList)))
    return (edgeList [["type"]][targetNodeIndex])
  else
    return ("edge")

} # .getEdgeType
#-----------------------------------------------------------------------------------------
.broadcastAssociativeArray <- function (list, name)
{ 
  listBaseType = typeof (as.vector (list) [1])
  cat ('list base type:', listBaseType, '\n')
  if (listBaseType == 'list') {
    cat ('error! cannot broadcast a nested list\n')
    return (NULL)
    }

  listKeys = names (list)
  if (is.null (listKeys)) {
    cat ('error! no names found as index to list\n')
    return (NULL)
    }

  if (listBaseType == 'double') {
    listValues = as.double (list)
   .jcall (goose, "V", "createAndBroadcastDoubleAttributes", name, listKeys, listValues)
    }

  else if (listBaseType == 'integer') {
    listValues = as.integer (list)
    #cat ('    type of int list elements:', typeof (listValues [1]), '\n')
   .jcall (goose, "V", "createAndBroadcastIntegerAttributes", name, listKeys, listValues)
    #cat ('   after int call \n')
    }

  else {
    cat ('error! cannot broadcast hash of listBaseType ', listBaseType, '\n')
  }

} # broadcastAssociativeArray
#--------------------------------------------------------------------------------
.broadcastGraph <- function (graph, name)
{
  graphAsStrings = .graphNELtoGaggleNetwork (graph)
  interactionStrings   = graphAsStrings$edges
  nodeAttributeStrings = graphAsStrings$noa
  #print (paste ('gnoa:', graphAsStrings$noa))
  #print (paste ('noas:', nodeAttributeStrings))
  edgeAttributeStrings = graphAsStrings$eda

  emptyStringArray = c ('', '')  # two elements to keep JNI from making this a scalar

  options (warn = -1)  # is.na prints a warning about length > 1.  turn it off

  if (is.na (interactionStrings)) interactionStrings = emptyStringArray
  if (is.na (nodeAttributeStrings)) {
    #print (paste ('noa before:', nodeAttributeStrings))
    nodeAttributeStrings = emptyStringArray
    #print (paste ('noa after:', nodeAttributeStrings))
    }
  if (length (edgeAttributeStrings) == 0 || is.na (edgeAttributeStrings))
    edgeAttributeStrings = emptyStringArray

  options (warn = 1)

  #print (paste ("is:", interactionStrings))
  #print (paste ("na:", nodeAttributeStrings))
  #print (paste ("ea:", edgeAttributeStrings))
  
  if (length (interactionStrings) == 1)
    interactionStrings = c (interactionStrings, "")

  if (length (nodeAttributeStrings) == 1)
    nodeAttributeStrings = c (nodeAttributeStrings, "")

  if (length (edgeAttributeStrings) == 1)
    edgeAttributeStrings = c (edgeAttributeStrings, "")

	l = names(attributes(graph))
	#e = new.env()
	for (i in 1:length(l)) {
		item <- l[i]
		result = grep('^gaggle\\.', item)
		if (length(result) > 0) {
			#cat(item)
			#cat(attr(n,item))  
			#assign(item, attr(graph, item), envir=e)
			.jcall(goose, "V", "addNetworkMetadata", item, attr(graph, item))
		}
	}


  .jcall (goose, "V", "createAndBroadcastNetwork", interactionStrings,
                       nodeAttributeStrings, edgeAttributeStrings, name)

} # broadcastGraph
#--------------------------------------------------------------------------------------------------
.broadcastEnvironment <- function (map, attributeName)
# the map has a magic field:  'title'
# this slot, if present, supplies the title of this hash map, which is not to
# be confused with the name of the attribute being assigned.
# for example:  
#    title = 'simulation #4'  
#    attributeName='log2 ratio'
#    names = c ("A", "B", "C")
#    values = c (0.5, 1.8, 0.85)
#
# in code:
#   m = new.env ()
#   m$title = 'simulation #4'
#   assign ("A", 0.5,  envir=m)
#   assign ("B", 1.8,  envir=m)
#   assign ("C", 0.85, envir=m)
#   broadcast (m, "log2 ratio")
#
# -- or, if Biobase:mulitassign is available:
#
#   m = new.env ()
#   m$title = 'simuluation #4'
#   assign ("A", 0.5,  envir=m)

{
  keys = ls (map)
  allNames = c ()
  allValues = c ()
  for (name in keys) {
    allNames = c (allNames, name)
    values = get (name, map)
    valueCount = length (get (name, map))
    capturedValue = .captureQuotedNumbersAsStrings(values[1])
    valuesAsString = as.character(capturedValue)    
	valuesAsString = paste (capturedValue)
    allValues = c (allValues, valuesAsString)
    }
  
  if (! "title" %in% ls (map))
    title = "from R"
  else
    title = map$title

  .jcall (goose, "V", "createAndBroadcastGaggleTuple", title, attributeName, allNames, allValues)

  invisible ()

} # broadcastEnvironment


.captureQuotedNumbersAsStrings <- function(i) {
	previousWarnLevel = getOption('warn')
	if (is.numeric(i)) { 
		return (i)
	}
	
	options(warn = -1)
	if (is.na(as.numeric(i))) {
		options(warn = previousWarnLevel)
		return (i)
	}
	options(warn = previousWarnLevel)
	
	return (paste("|",i,"|", sep=""))
}


#--------------------------------------------------------------------------------------------------
.listMember <- function (item, list)
{
  return (length (intersect (item, list)) > 0)
}
#-------------------------------------------------------------------------------------------------------
testGagglePackage <- function()
{
  require ("RUnit", quietly=TRUE) || stop ("RUnit package not found")
  testScript = paste (system.file (package="gaggle"), "unitTests/gaggleTest.R", sep="/")
  file.exists (testScript) || stop (paste (testScript, "not found."))
  source (testScript)
  test.gaggle ()

} # testGagglePackage
#-------------------------------------------------------------------------------------------------------
connectToGaggle <- function()
{
    .jcall (goose, "V", "connectToGaggle")
    invisible()
}
#--------------------------------------------------------------------------------------------------
disconnectFromGaggle <- function()
{
    .jcall (goose, "V", "doExit")
    invisible()
}
#--------------------------------------------------------------------------------------------------
# Receive a gaggle tuple and converts it to an R list
getTupleAsList <- function() {
	tuple <- .jcall(goose, "Lorg/systemsbiology/gaggle/core/datatypes/Tuple;", "getTuple")
	return(tupleToList(tuple))
}
#--------------------------------------------------------------------------------------------------
# Converts a Gaggle Tuple into a nested list structure. Receiving gaggle data
# types other than tuple nested in the list is not supported.
tupleToList <- function(tuple) {
	result <- list()
	if (!is.null(tuple) && !is.jnull(tuple)) {
		singles <- .jcall(tuple, "Ljava/util/List;", "getSingleList")
		len <- .jcall(singles, "I", "size")
		for (i in seq(from=0, length.out = len)) {
			single <- .jcall(tuple, "Lorg/systemsbiology/gaggle/core/datatypes/Single;", "getSingleAt", as.integer(i))
			key <- .jcall(single, "Ljava/lang/String;", "getName")
			value <- .jcall(single, "Ljava/io/Serializable;", "getValue")

			if (is.null(value) || is.jnull(value)) {
				r.value <- NULL
			}
			else {
				# I'd like to use instanceof, which requires rJava 0.8.
				# Note that the current method will fail to detect subclasses of Tuple
				j.class <- .jcall(.jcall(value, "Ljava/lang/Class;", "getClass"), "Ljava/lang/String;", "getName")
				# cat("j.class = ", j.class, "\n")
				if (j.class == "org.systemsbiology.gaggle.core.datatypes.Tuple") {
					# recurse into tuple values
					r.value <- tupleToList(value)
				} else {
					r.value <- .jsimplify(value)
				}
			}

			# append the value or key/value pair to the result
			if (is.jnull(key)) {
				result[[length(result)+1]] <- r.value
			} else {
				result[[key]] <- r.value
			}
		}
	}
	return(result)
}
#--------------------------------------------------------------------------------------------------
# Create a Gaggle Single object from an R value of type integer, double, logical, or character.
# Numeric values are naturally doubles in R, so use as.integer(x) to create an integer.
newSingle <- function(name, value) {
	if (typeof(value)=="integer") {
		jvalue = .jnew('java/lang/Integer', value)
	}
	else if (typeof(value)=="double") {
		jvalue = .jnew('java/lang/Double', value)
	}
	else if (typeof(value)=="character") {
		jvalue = .jnew('java/lang/String', value)
	}
	else if (typeof(value)=="logical") {
		jvalue = .jnew('java/lang/Boolean', value)
	}
	else if (class(value)=='list') {
		jvalue = newTuple(name, value)
	}
	else if (class(value)=='jobjRef') {
		if (isJavaTuple(value)) {
			jvalue <- value
		}
	}
	else {
		cat("Can't convert R type ", typeof(value), " to Java.\n")
	}
	return(.jnew('org/systemsbiology/gaggle/core/datatypes/Single', name, .jcast(jvalue, 'java/io/Serializable')))
}
#--------------------------------------------------------------------------------------------------
# add a single to a tuple, either as a name,value pair
# or add a premade single object.
addSingle <- function(tuple, name=NULL, value=NULL, single=NULL) {
	if (is.null(single)) {
		single = newSingle(name, value)
	}
	.jcall(tuple, 'V', 'addSingle', single)
	return(tuple)
}
#--------------------------------------------------------------------------------------------------
# Create a new tuple. Use with addSingle.
# list can hold values that map to a java primitive type or recursively list of such values
newTuple <- function(name, list=NULL) {
	tuple <- .jnew('org/systemsbiology/gaggle/core/datatypes/Tuple', name)
	if (!is.null(list) && is.environment(list)) {
		for (key in ls(list)) {
			addSingle(tuple, key, list[[key]])
		}
	}
	else if (!is.null(list)) {
		# can't figure out how to get the name of each element inside the reduce function, we have
		# to wimp out and use a loop
		# Reduce(function(t, element) { addSingle(t, ??name of element??, element)}, cmd.import.track, tuple)
		for(i in 1:length(list)) {
			if (!is.null(list[[i]]))
				addSingle(tuple, names(list)[i], list[[i]])
		}
	}
	return(tuple)
}
#--------------------------------------------------------------------------------------------------
