# jython-startup.py: the default script loaded by jython in a java web start
#----------------------------------------------------------------------------------------
# $Revision: 1.1 $   $Date: 2005/01/05 20:56:34 $
#----------------------------------------------------------------------------------------
import java
from java.lang import System
from java.io import *
from javax.swing import *
from java.awt import *
from java.awt.event import *
from javax.swing.event import *
import java

from java.rmi import *
#from java.rmi.server import UnicastRemoteObject
from org.python.rmi import UnicastRemoteObject

import sys
sys.add_package ('org.systemsbiology.gaggle')
import org.systemsbiology.gaggle as gaggle
#----------------------------------------------------------------------------------------
def version ():
  print 'Tuesday, 21 December 2004'
#----------------------------------------------------------------------------------------
class ControlPanel (JFrame, WindowListener, ActionListener, gaggle.Goose, UnicastRemoteObject):

  def __init__ (self):

    JFrame.__init__ (self, title='Inferelator Control Panel', size = (200, 200))
    self.name = 'Inferelator'
    self.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE)
    self.addWindowListener (self)
  
    self.getContentPane().add (self.createGui ())
    self.setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);
    self.placeInCenter ()
    self.pack ()
    #self.show ()
    UnicastRemoteObject.exportObject (self);
    self.connectToGaggle ();


  #------------------------------------------------------------------------------------------
  def connectToGaggle (self):

    serviceName = 'gaggle'
    hostname = 'localhost'
    uri = 'rmi://%s/%s' % (hostname,  serviceName)
    #try:
    self.gaggleBoss =  Naming.lookup (uri)
    print 'after lookup'
    registeredName = gaggleBoss.register (self)
    print 'after register'
    self.setTitle (registeredName)
    print 'after setTitle'
    #except:
    #  print 'failed to connect to gaggle at %s' %  uri

  #------------------------------------------------------------------------------------------
  def select (self, names):

    print 'select %s' % names

  #------------------------------------------------------------------------------------------
  def handleLens (self, lens):

    print 'handleLens: %s' % lens

  #------------------------------------------------------------------------------------------
  def handleBroadcastAttributes (self, attributesData):

    print 'handleBroadcastAttributes...'

  #------------------------------------------------------------------------------------------
  def getName (self):

    print 'getName'
    return self.name

  #------------------------------------------------------------------------------------------
  def setName (self, newName):

     print 'setName: %s' % newName

  #------------------------------------------------------------------------------------------
  def hide (self):

    print 'hide'
    JFrame.hide ();

  #------------------------------------------------------------------------------------------
  def show (self):

    print 'entering ControlPanel.show'
    JFrame.show (self);

  #------------------------------------------------------------------------------------------
  def clearSelections (self):

    print 'clearSelections'

  #------------------------------------------------------------------------------------------
  def setGeometry (self, x, y, width, height):

    print 'setGeometry: %d %d  %d x %d' % (x, y, width, height)

  #------------------------------------------------------------------------------------------
  def getSelectionCount (self):

    print 'getSelectionCount'

  #------------------------------------------------------------------------------------------
  def doBroadcast (self):

    print 'doBroadcast'

  #------------------------------------------------------------------------------------------
  def exit (self):

     print 'goose exit'

  #------------------------------------------------------------------------------------------
  def createGui (self):
 
    outerPanel = JPanel ()
    outerPanel.setLayout (BorderLayout ())
    buttonPanel = JPanel ()
    buttonPanel.layout = BorderLayout ()
    gridButtonPanel = JPanel ()
    buttonPanel.add (gridButtonPanel, BorderLayout.CENTER)
    gridButtonPanel.layout = GridLayout (3, 2)
    showAllButton = JButton ('Show All Nodes & Edges', actionPerformed=self.showAll)
    hideOrphansButton = JButton ('Hide Orphans', actionPerformed=self.hideOrphans)
    #launchDataBrowserButton = JButton ('Data Matrix Browser', actionPerformed=self.launchDataMatrixBrowser)
    selectClusterMatrixButton = JButton ('Select Cluster Matrix',  
                                         actionPerformed=self.selectClusterMatrix)
    clearSelectionsButton = JButton ('Clear Selections', actionPerformed=self.clearSelections)
    broadcastAllButton = JButton ('Broadcast All', actionPerformed=self.broadcastAll)
    dismissButtonPanel = JPanel ()
    dismissButton = JButton ('Dismiss', actionPerformed=self.dismiss)
    
    gridButtonPanel.add (hideOrphansButton)
    #gridButtonPanel.add (launchDataBrowserButton)
    gridButtonPanel.add (showAllButton)
    gridButtonPanel.add (selectClusterMatrixButton)
    gridButtonPanel.add (clearSelectionsButton)
    gridButtonPanel.add (broadcastAllButton)
    dismissButtonPanel.add (dismissButton)
    buttonPanel.add (dismissButtonPanel, BorderLayout.SOUTH)

    outerPanel.add (buttonPanel, BorderLayout.SOUTH)
 
    mainPanel = JPanel ()
    mainPanel.setLayout (BorderLayout ())
    
    outerPanel.add (mainPanel, BorderLayout.CENTER)
    edgeWeights = [0,1] #getEdgeWeights ()
    mainPanel.add (SliderComponent (min (edgeWeights), max (edgeWeights)), BorderLayout.NORTH)
    selectPanel = JPanel ()
    selectPanel.setLayout (GridLayout (2,1))
    mainPanel.add (selectPanel, BorderLayout.CENTER)
    upperSelectPanel = JPanel ()
    upperSelectPanel.add (JButton ('Select Nodes Explicitly', actionPerformed=self.selectNodesCallback))
    self.nodeSelectionTextField = JTextField (20)
    self.nodeInClusterSelectionTextField = JTextField (20)
    upperSelectPanel.add (self.nodeSelectionTextField)

    lowerSelectPanel = JPanel ()
    lowerSelectPanel.add (JButton ('Select Genes in Clusters', 
                          actionPerformed=self.selectGenesInClusters))
    lowerSelectPanel.add (self.nodeInClusterSelectionTextField)

    selectPanel.add (upperSelectPanel)
    selectPanel.add (lowerSelectPanel)

    #mainPanel.layout = GridLayout (columnCount, 1)
    #for c in range (columnCount):
    #   mainPanel.add (SliderComponent (matrix, c))

    return outerPanel

  #------------------------------------------------------------------------------------------
  def placeInCenter (self):

    gc = self.getGraphicsConfiguration ()
    screenHeight = gc.getBounds().getHeight ()
    screenWidth = gc.getBounds().getWidth ()
    windowWidth = self.getWidth ()
    windowHeight = self.getHeight ()
    x = int ((screenWidth-windowWidth)/2)
    y = int ((screenHeight-windowHeight)/2)
    self.setLocation (x, y)

  #------------------------------------------------------------------------------------------
  def clearSelections (self, e):

    cw.selectNodesByName ([], 1)

  #------------------------------------------------------------------------------------------
  def broadcastAll (self, e):

    nc = cw.graph.selectedNodes ()
    genes = []
    clusters = []
    while (nc.ok ()):
      name = cw.nodeAttributes.getCanonicalName (nc.node ())
      type = cw.nodeAttributes.getValue ('type', name)
      if (type == 'cluster'):
        clusters.append (name)
      elif (type == 'gene'):
         genes.append (name);
      nc.next ()

    print 'genes: %s' % genes
    print 'clusters: %s' % clusters
    for cluster in clusters:
      clusterGenes = self.getGenesInCluster (cluster)
      for gene in clusterGenes:
        if (not gene in genes):
          genes.append (gene)

    print 'about to broadcast %d genes' % len (genes)
    if (len (genes) > 0):
      self.broadcast (genes)


  #------------------------------------------------------------------------------------------
  def broadcast (self, genes):
     
     self.gaggleBoss.select (genes)
     #multiTuple = tspace.Tuple ()
     #for gene in genes:
     #  tuple = tspace.Tuple (gene, 'Halobacterium sp.', 'inferelator control panel')
     #  tuple.setExpire (4000)
     #  multiTuple.add (tuple)

     #ts.multiWrite (multiTuple)

     
  #------------------------------------------------------------------------------------------
  def selectGenesInClusters (self, event):

    nodeNames = self.getCanonicalNamesOfNodesInGraph ()
    clusterIDs = []
    for canonicalName in nodeNames:
      nodeType = cw.getNodeAttributes().get ('type', canonicalName)
      if (nodeType and nodeType == 'cluster'):
        clusterIDs.append (canonicalName)

    geneToClusterMap = {}
    for clusterID in clusterIDs:
      genes = self.getGenesInCluster (clusterID)
      for gene in genes:
        if (geneToClusterMap.has_key (gene)):
          list = geneToClusterMap [gene]
        else:
          list = []
        if (not clusterID in list):
          list.append (clusterID)
        geneToClusterMap [gene] = list


    canonicalNames = geneToClusterMap.keys ()
    commonNamesHash = self.getCommonNamesInHash (canonicalNames)
    namesToSelect = self.nodeInClusterSelectionTextField.getText().split ()

    print 'canonicalNames: %s' % canonicalNames
    print 'commonNames:    %s' % commonNamesHash
    print 'namesToSelect:  %s' % namesToSelect

    matches = []
    for name in namesToSelect:
      candidate = name.lower().strip()

      locationOfWildCardCharacter = candidate.find ('*')
      
      if (locationOfWildCardCharacter == (len (candidate) - 1)):
        candidate = candidate [:locationOfWildCardCharacter]
        print 'found wildcard ------'
        for canonicalName in canonicalNames:
          canonicalNameLowered = canonicalName.lower ()
          print 'wildcard match of %s to %s: %d' % \
            (candidate, canonicalNameLowered, canonicalNameLowered.find (candidate))
          if (canonicalNameLowered.find (candidate) == 0):
            if (not canonicalName in matches):
              matches.append (canonicalName)
          for commonName in commonNamesHash.keys ():
            if (commonName.find (candidate) == 0):
              correspondingCanonicalName = commonNamesHash [commonName]
              if (not correspondingCanonicalName in matches):
                matches.append (correspondingCanonicalName)
    
      else:  # looking for exact matches
        for canonicalName in canonicalNames:
          canonicalNameLowered = canonicalName.lower ()
          if (canonicalNameLowered == candidate) and (not canonicalName in matches):
            matches.append (canonicalName)
        for commonName in commonNamesHash.keys ():
          commonNameLowered = commonName.lower ()
          if (commonNameLowered == candidate):
            correspondingCanonicalName = commonNamesHash [commonName]
            if (not correspondingCanonicalName in matches):
              matches.append (correspondingCanonicalName)

    print 'matched nodes: %s' % matches
    clusterNodesToSelect = []
    for match in matches:
      clusters = geneToClusterMap [match]
      for cluster in clusters:
        if (not cluster in clusterNodesToSelect):
          clusterNodesToSelect.append (cluster)
      
    cw.selectNodesByName (clusterNodesToSelect, 0)
    
  #------------------------------------------------------------------------------------------
  def getGenesInCluster (self, clusterID):

    genes = [g for g in cw.getNodeAttributes().getStringArrayValues ('clusterGenes', clusterID)]
    return genes;

  #------------------------------------------------------------------------------------------
  def selectNodesCallback (self, event):

    canonicalNames = self.getCanonicalNamesOfNodesInGraph ()
    commonNamesHash = self.getCommonNamesInHash (canonicalNames)
    namesToSelect = self.nodeSelectionTextField.getText().split ()

    print 'canonicalNames: %s' % canonicalNames
    print 'commonNames:    %s' % commonNamesHash
    print 'namesToSelect:  %s' % namesToSelect

    result = []
    for name in namesToSelect:
      candidate = name.lower().strip()

      locationOfWildCardCharacter = candidate.find ('*')
      
      if (locationOfWildCardCharacter == (len (candidate) - 1)):
        candidate = candidate [:locationOfWildCardCharacter]
        print 'found wildcard ------'
        for canonicalName in canonicalNames:
          canonicalNameLowered = canonicalName.lower ()
          print 'wildcard match of %s to %s: %d' % \
            (candidate, canonicalNameLowered, canonicalNameLowered.find (candidate))
          if (canonicalNameLowered.find (candidate) == 0):
            if (not canonicalName in result):
              result.append (canonicalName)
          for commonName in commonNamesHash.keys ():
            if (commonName.find (candidate) == 0):
              correspondingCanonicalName = commonNamesHash [commonName]
              if (not correspondingCanonicalName in result):
                result.append (correspondingCanonicalName)
    
      else:  # looking for exact matches
        for canonicalName in canonicalNames:
          canonicalNameLowered = canonicalName.lower ()
          if (canonicalNameLowered == candidate) and (not canonicalName in result):
            result.append (canonicalName)
        for commonName in commonNamesHash.keys ():
          commonNameLowered = commonName.lower ()
          if (commonNameLowered == candidate):
            correspondingCanonicalName = commonNamesHash [commonName]
            if (not correspondingCanonicalName in result):
              result.append (correspondingCanonicalName)

    print 'matched nodes: %s' % result
    cw.selectNodesByName (result, 0)

  #------------------------------------------------------------------------------------------
  def getCommonNamesInHash (self, canonicalNames):

    result = {}
    for canonicalName in canonicalNames:
      commonName = cw.getNodeAttributes().getValue ('commonName', canonicalName)
      if (commonName == None):
        continue
      if (not result.has_key (commonName)):
        result [commonName] = canonicalName

    return result

  #------------------------------------------------------------------------------------------
  def getCanonicalNamesOfNodesInGraph (self):

    result = []
    nodeArray = [x for x in cw.graph.getNodeArray ()]
    for node in nodeArray:
      canonicalName = cw.getCanonicalNodeName (node)
      result.append (canonicalName)

    return result

  #------------------------------------------------------------------------------------------
  def windowClosing (self, event):

    self.cleanup ()

  #------------------------------------------------------------------------------------------
  def windowClosed (self, event):
    pass

  def windowOpened (self, event):
    pass

  def windowIconified (self, event):
    pass

  def windowDeiconified (self, event):
    pass

  def windowActivated (self, event):
    pass

  def windowDeactivated (self, event):
    pass

  def windowGainedFocus (self, event):
    pass

  def windowLostFocus (self, event):
    pass
  #------------------------------------------------------------------------------------------
  def cleanup (self):

    self.dismiss (None)

  #------------------------------------------------------------------------------------------
  def dismiss (self, event):

    self.dispose ()

  #------------------------------------------------------------------------------------------
  def showAll (self, event):

    hider.unhideAll ()
    cw.redrawGraph ()

  #-------------------------------------------------------------------
  def hideOrphans (self, event):

    for node in cw.graph.nodeArray:
      if (node.degree () == 0): # and (not node in exclusions)):
        hider.hide (node)

    cw.redrawGraph ()

  #-------------------------------------------------------------------
  def launchDataMatrixBrowser (self, event):

   print 'launchDataMatrixBrowser...'
   # self.treeDataMatrixBrowser = TreeDataBrowser ();

  #-------------------------------------------------------------------
  def fixConditionName (self, condition):
    """
     Rich & Dave work with somewhat different names than those used
     in the xml read by the TreeDataMatrixBrowser.  translate 
     their names to ours here
     examples:
       bop_HO.L_vs_NRC.1   -> bop__HO_L_vs_NRC-1
        
    """
    map = {'C60.rat': 'C60',
           'Cu_2_vs_c': 'Cu_2_vs_C.sig',
           'Cu_3_vs_c': 'Cu_3_vs_C.sig',
  
           'D30.rat': 'D30',
           'D60.rat': 'D60',
  
           'Fe_2_vs_c': 'Fe_2_vs_C.sig',
           'Fe_4_vs_c': 'Fe_4_vs_C.sig',
           'Fe_6_vs_c': 'Fe_6_vs_C.sig',
           'Fe_7_vs_c': 'Fe_7_vs_C.sig',
  
           'L30.rat': 'L30',
           'L60.rat': 'L60',
  
           'Mn_1_vs_c': 'Mn_1_vs_C.sig',
           'Mn_2_vs_c': 'Mn_2_vs_C.sig',
           'Mn_3_vs_c': 'Mn_3_vs_C.sig',
  
           'NRC.1_HO.D_vs_NRC.1': 'NRC-1__HO_D_vs_NRC-1',
           'NRC.1_HO.L_vs_NRC.1': 'NRC-1__HO_L_vs_NRC-1',
           'NRC.1_LO.D_vs_NRC.1': 'NRC-1__LO_D_vs_NRC-1',
           'NRC.1_LO.L_vs_NRC.1': 'NRC-1__LO_L_vs_NRC-1',
  
  
           'VNG0750c_HO_D_vs_NRC.1': 'VNG0750C__HO_D_vs_NRC-1',
           'VNG0750c_HO_L_vs_NRC.1': 'VNG0750C__HO_L_vs_NRC-1',
           'VNG0750c_LO.D_vs_NRC.1': 'VNG0750C__LO_D_vs_NRC-1',
           'VNG0750c_LO.L_vs_NRC.1': 'VNG0750C__LO_L_vs_NRC-1',
  
           'Zn_1_vs_c': 'Zn_1_vs_C.sig',
           'Zn_2_vs_c': 'Zn_2_vs_C.sig',
           'Zn_3_vs_c': 'Zn_3_vs_C.sig',
  
           'ark_HO.D_vs_NRC.1': 'ark__HO_D_vs_NRC-1',
           'ark_HO.L_vs_NRC.1': 'ark__HO_L_vs_NRC-1',
           'ark_LO.D_vs_NRC.1': 'ark__LO_D_vs_NRC-1',
           'ark_LO.L_vs_NRC.1': 'ark__LO_L_vs_NRC-1',
  
           'boa1_HO.D_vs_NRC.1': 'boa1__HO_D_vs_NRC-1',
           'boa1_HO.L_vs_NRC.1': 'boa1__HO_L_vs_NRC-1',
           'boa1_LO.D_vs_NRC.1': 'boa1__LO_D_vs_NRC-1',
           'boa1_LO.L_vs_NRC.1': 'boa1__LO_L_vs_NRC-1',
  
           'boa4_HO.D_vs_NRC.1': 'boa4__HO_D_vs_NRC-1',
           'boa4_HO.L_vs_NRC.1': 'boa4__HO_L_vs_NRC-1',
           'boa4_LO.D_vs_NRC.1': 'boa4__LO_D_vs_NRC-1',
           'boa4_LO.L_vs_NRC.1': 'boa4__LO_L_vs_NRC-1',
  
           'bop_HO.D_vs_NRC.1': 'bop__HO_D_vs_NRC-1',
           'bop_HO.L_vs_NRC.1': 'bop__HO_L_vs_NRC-1',
           'bop_LO.D_vs_NRC.1': 'bop__LO_D_vs_NRC-1',
           'bop_LO.L_vs_NRC.1': 'bop__LO_L_vs_NRC-1',
  
           'gC0': 'gamma-C000',
           'gC10': 'gamma-C010',
           'gC30': 'gamma-C030',
           'gC60': 'gamma-C060',
  
           'htlD_HO.D_vs_NRC.1': 'htlD__HO_D_vs_NRC-1',
           'htlD_HO.L_vs_NRC.1': 'htlD__HO_L_vs_NRC-1',
           'htlD_LO.D_vs_NRC.1': 'htlD__LO_D_vs_NRC-1',
           'htlD_LO.L_vs_NRC.1': 'htlD__LO_L_vs_NRC-1',
           'htr1_HO.D_vs_NRC.1': 'htr1__HO_D_vs_NRC-1',
           'htr1_HO.L_vs_NRC.1': 'htr1__HO_L_vs_NRC-1',
           'htr1_LO.D_vs_NRC.1': 'htr1__LO_D_vs_NRC-1',
           'htr1_LO.L_vs_NRC.1': 'htr1__LO_L_vs_NRC-1',
  
  
           'htr2_HO.D_vs_NRC.1': 'htr2__HO_D_vs_NRC-1',
           'htr2_HO.L_vs_NRC.1': 'htr2__HO_L_vs_NRC-1',
           'htr2_LO.D_vs_NRC.1': 'htr2__LO_D_vs_NRC-1',
           'htr2_LO.L_vs_NRC.1': 'htr2__LO_L_vs_NRC-1',
  
           'htr8_HO.D_vs_NRC.1': 'htr8__HO_D_vs_NRC-1',
           'htr8_HO.L_vs_NRC.1': 'htr8__HO_L_vs_NRC-1',
           'htr8_LO.D_vs_NRC.1': 'htr8__LO_D_vs_NRC-1',
           'htr8_LO.L_vs_NRC.1': 'htr8__LO_L_vs_NRC-1',
  
           'kinA2_HO.D_vs_NRC.1': 'kinA2__HO_D_vs_NRC-1',
           'kinA2_HO.L_vs_NRC.1': 'kinA2__HO_L_vs_NRC-1',
           'kinA2_LO.D_vs_NRC.1': 'kinA2__LO_D_vs_NRC-1',
           'kinA2_LO.L_vs_NRC.1': 'kinA2__LO_L_vs_NRC-1',
  
           'phoR_HO.D_vs_NRC.1': 'phoR__HO_D_vs_NRC-1',
           'phoR_HO.L_vs_NRC.1': 'phoR__HO_L_vs_NRC-1',
           'phoR_LO.D_vs_NRC.1': 'phoR__LO_D_vs_NRC-1',
           'phoR_LO.L_vs_NRC.1': 'phoR__LO_L_vs_NRC-1',
  
           'phr1_HO.D_vs_NRC.1': 'phr1__HO_D_vs_NRC-1',
           'phr1_LO.D_vs_NRC.1': 'phr1__LO_D_vs_NRC-1',
           'phr1_LO.L_vs_NRC.1': 'phr1__LO_L_vs_NRC-1',
  
           'phr1_and_phr2_HO.D_vs_NRC.1': 'phr1_and_2__HO_D_vs_NRC-1',
           'phr1_and_phr2_HO.L_vs_NRC.1': 'phr1_and_2__HO_L_vs_NRC-1',
           'phr1_and_phr2_LO.D_vs_NRC.1': 'phr1_and_2__LO_D_vs_NRC-1',
           'phr1_and_phr2_LO.L_vs_NRC.1': 'phr1_and_2__LO_L_vs_NRC-1',
  
           'phr2_HO.D_vs_NRC.1': 'phr2__HO_D_vs_NRC-1',
           'phr2_HO.L_vs_NRC.1': 'phr2__HO_L_vs_NRC-1',
           'phr2_LO.D_vs_NRC.1': 'phr2__LO_D_vs_NRC-1',
           'phr2_LO.L_vs_NRC.1': 'phr2__LO_L_vs_NRC-1',
  
           'sop2_HO.D_vs_NRC.1': 'sop2__HO_D_vs_NRC-1',
           'sop2_HO.L_vs_NRC.1': 'sop2__HO_L_vs_NRC-1',
           'sop2_LO.D_vs_NRC.1': 'sop2__LO_D_vs_NRC-1',
           'sop2_LO.L_vs_NRC.1': 'sop2__LO_L_vs_NRC-1',
           
           'ura3_HO.D_vs_NRC.1': 'ura3__HO_D_vs_NRC-1',
           'ura3_HO.L_vs_NRC.1': 'ura3__HO_L_vs_NRC-1',
           'ura3_LO.D_vs_NRC.1': 'ura3__LO_D_vs_NRC-1',
           'ura3_LO.L_vs_NRC.1': 'ura3__LO_L_vs_NRC-1',

           'X1':  'Light2_024_vs_Light1_066',
           'X2':  'Light2_027_vs_Light1_066',
           'X3':  'Light2_030_vs_Light1_066',
           'X4':  'Light2_033_vs_Light1_066',
           'X5':  'Light2_036_vs_Light1_066',
           'X6':  'Light2_039_vs_Light1_066',
           'X7':  'Light2_042_vs_Light1_066',
           'X8':  'Light2_045_vs_Light1_066',
           'X9':  'Light2_048_vs_Light1_066',
           'X10': 'Light2_051_vs_Light1_066',
           'X11': 'Light2_054_vs_Light1_066',
           'X12': 'Light2_057_vs_Light1_066',
           'X13': 'Light2_060_vs_Light1_066',
           'X14': 'Light2_063_vs_Light1_066',
           'X15': 'Light2_066_vs_Light1_066',
           'X16': 'Light2_069_vs_Light1_066',
           'X17': 'Light2_072_vs_Light1_066',
           'X18': 'Light2_075_vs_Light1_066',
           'X19': 'Light2_078_vs_Light1_066',
           'X20': 'Light2_081_vs_Light1_066',
           'X21': 'Light2_084_vs_Light1_066',
           'X22': 'Light2_087_vs_Light1_066',
           'X23': 'Light2_090_vs_Light1_066',
           'X24': 'Light2_093_vs_Light1_066',
           'X25': 'Light2_096_vs_Light1_066',
           }

    if (map.has_key (condition)):
      print 'mapping %30s to %30s' % (condition, map [condition])
      return map [condition]
    else:
      print 'no map: %30s' % condition
      return condition


  #-------------------------------------------------------------------
  def selectClusterMatrix (self, event):

    nc = cw.graph.selectedNodes ()
    genes = []
    conditions = []
    numberOfSelectedClusterNodes = 0
    while (nc.ok ()):
      name = cw.nodeAttributes.getCanonicalName (nc.node ())
      type = cw.nodeAttributes.getValue ('type', name)
      if (type == 'cluster'):
        numberOfSelectedClusterNodes += 1
      nc.next ()
      genes.append (name)

    if (numberOfSelectedClusterNodes > 1):
      JOptionPane.showMessageDialog (None, 'More than one cluster selected',
                                     'Inferelator Control Panel Error',
                                     JOptionPane.ERROR_MESSAGE)
      return
 

    for canonicalName in genes:
      clusteredGenes = cw.nodeAttributes.getStringArrayValues ('clusterGenes', canonicalName)
      if (clusteredGenes):
        conditions = [x for x in cw.nodeAttributes.getStringArrayValues ('clusterConditions', 
                                                                         canonicalName)]
        #print 'clustered genes: %s' % clusteredGenes
        genes.remove (canonicalName)
        for gene in clusteredGenes:
          if (not gene in genes):
            genes.append (gene.upper ())

    #try:
    fixedConditions = []
    for condition in conditions:
      fixedConditions.append (self.fixConditionName (condition))
    print 'about to select %d genes in %d conditions' % (len (genes), len (fixedConditions))
    #print 'fixed: %s' % fixedConditions
    #self.treeDataMatrixBrowser.dataCubeBrowser.selectSubTableInCurrentlyVisibleTable (genes, fixedConditions)
    #except:
    #  JOptionPane.showMessageDialog (None, 'Please launch the DataMatrixBrowser first.',
    #                                 'Inferelator Control Panel Error',
    #                                 JOptionPane.ERROR_MESSAGE)
     
   
#--------------------------------------------------------------------------------------------
class SliderComponent (JPanel, ChangeListener):
  """
  """
  def __init__ (self, minValue, maxValue):

    sliderMin = int (1000 * minValue)
    sliderMax = int (1000 * maxValue)
    slider = JSlider (sliderMin, sliderMax)
    self.currentValue = (maxValue - minValue) / 2.0
    self.readout = JTextField ('%5.3f' % self.currentValue, 8)
    button = JButton ('Hide Edges Below: ', actionPerformed=self.hideEdgesCallback)
    #self.add (label)
    self.add (button)
    self.add (self.readout)
    self.add (slider)
    slider.addChangeListener (self)

    
  #------------------------------------------------------------------------------------------
  def stateChanged (self, e):

    slider = e.getSource ()
    #if (slider.valueIsAdjusting):
    #  return

    self.currentValue = float (slider.value) / 1000.0
    self.readout.setText ('%5.3f' % self.currentValue)

  #------------------------------------------------------------------------------------------
  def hideEdgesCallback (self, e):

    #print 'current threshold: %f' % self.currentValue
    hideEdgesByWeight (self.currentValue)


#--------------------------------------------------------------------------------------------
def getEdgeWeights ():

  """
   return a 2 element array, the min and max of all edges
  """

  result = []
  edgeObjects = cw.graph.edgeArray
  ea = cw.getEdgeAttributes ()
  for edge in edgeObjects:
    weight = ea.getValue ('weight', ea.getCanonicalName (edge))
    if (weight):
      result.append (weight)

  return result

#--------------------------------------------------------------------------------------------
def hideEdgesByWeight (min):

  ea = cw.getEdgeAttributes ()
  edges = cw.getGraph().getEdgeArray()

  edgesToHide = []
  hiddenEdgeTargetNodes = []

  for edge in edges: # [:10]:
    sourceNode = edge.source ()
    targetNode = edge.target ()
    edgeName = ea.getCanonicalName (edge)
    weight = ea.getValue ('weight', edgeName)
    if (weight == None):
      continue
    if (weight < min):
      hider.hide (edge)
      edgeCursor = sourceNode.inEdges ()
      while (edgeCursor.ok ()):
        hider.hide (edgeCursor.edge ())
        edgeCursor.next ()


  
  cw.redrawGraph ()
  
#-------------------------------------------------------------------
#hider = cw.getGraphHider ()
cp = ControlPanel ()
# ts = tspace.TupleSpace ('coord', 'localhost')
