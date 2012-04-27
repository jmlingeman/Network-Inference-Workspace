# __run__.py: the default script loaded by jython in a java web start
#----------------------------------------------------------------------------------------
# $Revision: 18 $   $Date: 2004/08/31 21:45:05 $
#----------------------------------------------------------------------------------------
import java
from java.lang import System
from java.io import *
from javax.swing import *
from java.awt import *
from java.awt.event import *
from javax.swing.event import *
import java
#----------------------------------------------------------------------------------------
System.out.println ("running bootstrap script for halo/tiles \n")
global execUrl
scriptHomeDefault='http://db.systemsbiology.net/cytoscape/scripts'
#----------------------------------------------------------------------------------------
def execUrl (url, scriptHome=scriptHomeDefault):
  fullUrl = scriptHome + url
  us = webreader.read(fullUrl)
  exec (compile(us, '<string>', 'exec'))
#----------------------------------------------------------------------------------------
# selectRows.py:  set up some functions for selecting rows in the data matrix browser, by
# threshold
#---------------------------------------------------------------------------------------------
global summarize
def summarize ():
  print 'matix count: %d' % len (matrixList)
  for matrix in matrixList:
    rows = matrix.getRowCount ()
    cols = matrix.getColumnCount ()
    name = matrix.getShortName ()
    print '%20s  %d x %d' % (name, rows, cols)
    for c in range (cols):
      title = matrix.getColumnTitles ()[c]
      vector = matrix.getColumn (title)
      vMin = min (vector)
      vMax = max (vector)
      print '    %2d:  %20s  %10f  %10f' % (c, title, vMin, vMax)
      
#---------------------------------------------------------------------------------------------
global getRowNamesByThreshold
def getRowNamesByThreshold (threshold=60.0, matrix=0, column=0):
  """
   return a list of genes which exceed threshold in the specified column
  """
  result = []
  m = matrixList [matrix]
  vector = m.getColumn (m.getColumnTitles ()[column])
  rowNames = m.getRowTitles ()
  for i in range (len (vector)): 
    value = vector [i]
    if (java.lang.Double (value).isNaN ()):
      #print 'found NaN in row %d: %f' % (i, value)
      continue
    if (value >= threshold):
      name = rowNames [i]
      #print 'row %d (%s) value (%f) above threshold (%f)' % (i, name, value, threshold)
      result.append (name)

  return result

#---------------------------------------------------------------------------------------------
global clear
def clear ():
  print 'clear'

#---------------------------------------------------------------------------------------------
def version ():

  return '$Revision: 18 $   $Date: 2004/08/31 21:45:05 $'

#---------------------------------------------------------------------------------------------
def help ():

  msg = ''
  msg += '--- tips for exploring Halo ChIP-chip data ...\n'
  msg += '\n'
  msg += '   nothing available yet'

  print msg

#-------------------------------------------------------------------------------------
from math import sqrt
#----------------------------------------------------------------------------------------------
#def summarizeAll (mb):
#
#  for matrix in mb.getMatrices ():
#    print '\n------------ %s' % matrix.shortName
#    print '%20s %12s %12s %12s %12s %12s %12s %12s' % ('', 'min', '1st Qu.',  'median', 'mean', '3rd Qu.', 'max', 'sigma')
#    for columnTitle in matrix.columnTitles:
#      vector = matrix.getColumn (columnTitle)
#      print '%20s: %s' % (columnTitle, formattedSummaryStatistics (vector))
  
#----------------------------------------------------------------------------------------------
def formattedSummaryStatistics (vector):

  r.assign ('tmp', vector)
  stats = [x for x in r.eval ('summary (tmp)').getContent ()]
  s = '%12f %12f %12f %12f %12f %12f' % (
       stats [0], stats [1], stats [2], stats [3], stats [4], stats [5])
  sigma = sqrt (r.eval ('var (tmp)').getContent ())
  s += ' %12f' % sigma
  return s

#----------------------------------------------------------------------------------------------
from java.io import *
from javax.swing import *
from java.awt import *
from java.awt.event import *
import java
#--------------------------------------------------------------------------------------------
global MarcsGui
class MarcsGui (JFrame, WindowListener):

  #------------------------------------------------------------------------------------------
  def __init__ (self):

    JFrame.__init__ (self, title='marc', size = (200, 200))

    self.geneFinder = GeneFinder (cw)
    self.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE)
    self.addWindowListener (self)

    self.getContentPane().add (self.createGui ())
    self.setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);
    self.placeInCenter ()
    self.pack ()
    self.show ()

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
  def runDialog (self, event):

    pass
    #dialog = MyDialog ()
    #dialog = Login ()

  #------------------------------------------------------------------------------------------
  def selectCandidateGenes (self, distance):

    self.geneFinder.selectCandidateGenes (distance)

  #------------------------------------------------------------------------------------------
  def clearSelections (self, e):

    print 'clearSelections'

  #------------------------------------------------------------------------------------------
  def createGui (self):
 
    outerPanel = JPanel ()
    outerPanel.setLayout (BorderLayout ())
    buttonPanel = JPanel ()
    clearSelectionsButton = JButton ('Clear Selections', actionPerformed=self.clearSelections)
    selectCandidateGenesButton = JButton ('Select Genes', actionPerformed=self.selectCandidateGenes)
    dismissButton = JButton ('Dismiss', actionPerformed=self.dismiss)
    buttonPanel.add (clearSelectionsButton)
    geneSelectionDistanceComponent = GeneDistanceSelectionComponent ()
    buttonPanel.add (geneSelectionDistanceComponent)
    buttonPanel.add (dismissButton)
    outerPanel.add (buttonPanel, BorderLayout.SOUTH)
 
    mainPanel = JPanel ()
    outerPanel.add (mainPanel, BorderLayout.CENTER)

    #mainPanel.add (JButton ('1'))
    #mainPanel.add (JButton ('2'))
    #matrix = mb.getMatrices()[0]
    #columnCount = len (matrix.columnTitles)
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

#--------------------------------------------------------------------------------------------
class GeneDistanceSelectionComponent (JPanel, ChangeListener):
  """
   a simple combination of slider, readout, and 'Select' button, used
   to select rows in the DataMatrixBrowser from which this script is run.

     1) the ctor reads the vector at matrix [columnNumber]
     2) creates a properly scaled slider
     3) uses the column title as the label
     4) adds a select button, which calls the global function

          mb.selectRowsByName (getRowNamesByThreshold (self.currentValue, matrix=0, 
                                                       column=self.columnNumber))
  """
  def __init__ (self):

    JPanel.__init__ (self)
    self.setLayout (GridLayout (2,1))
    minValue = 1
    maxValue = 1000
    self.currentValue = 200
    slider = JSlider (minValue, maxValue, self.currentValue)
    initialValue = '%s' % self.currentValue
    self.readout = JTextField (initialValue, 8)
    button = JButton ('Select Genes', actionPerformed=self.selectGenesButtonCallback)
    sliderPanel = JPanel ()
    sliderPanel.add (slider)
    sliderPanel.add (self.readout)
    self.add (button)
    self.add (sliderPanel)
    slider.addChangeListener (self)

    
  #------------------------------------------------------------------------------------------
  def stateChanged (self, e):

    slider = e.getSource ()
    self.currentValue = slider.value
    self.readout.setText ('%s' % self.currentValue)

  #------------------------------------------------------------------------------------------
  def selectGenesButtonCallback (self, e):

    marcsGui.selectCandidateGenes (self.currentValue)

#--------------------------------------------------------------------------------------------
class SliderComponent (JPanel, ChangeListener):
  """
   a simple combination of label, slider, readout, and 'Select' button, used
   to select rows in the DataMatrixBrowser from which this script is run.

     1) the ctor reads the vector at matrix [columnNumber]
     2) creates a properly scaled slider
     3) uses the column title as the label
     4) adds a select button, which calls the global function

          mb.selectRowsByName (getRowNamesByThreshold (self.currentValue, matrix=0, 
                                                       column=self.columnNumber))
  """
  def __init__ (self, matrix, columnNumber):

    self.columnNumber = columnNumber
    self.title = matrix.columnTitles [columnNumber]
    vector = matrix.getColumn (self.title)
       
    minValue = int (min (vector))
    maxValue = int (max (vector))
    label = JLabel (self.title)
    slider = JSlider (minValue, maxValue)
    self.currentValue = int ((maxValue - minValue) / 2.0)
    initialValue = '%s' % self.currentValue
    self.readout = JTextField (initialValue, 8)
    button = JButton ('Select', actionPerformed=self.selectButtonCallback)
    self.add (label)
    self.add (slider)
    self.add (self.readout)
    self.add (button)
    slider.addChangeListener (self)

    
  #------------------------------------------------------------------------------------------
  def stateChanged (self, e):

    slider = e.getSource ()
    #if (slider.valueIsAdjusting):
    #  return

    self.currentValue = slider.value
    self.readout.setText ('%s' % self.currentValue)

  #------------------------------------------------------------------------------------------
  def selectButtonCallback (self, e):

    #print 'column %d, %s, select all rows above %s' % (
    #        self.columnNumber, self.title, self.currentValue)
    selectedRowNames = getRowNamesByThreshold (self.currentValue, matrix=0, 
                                               column=self.columnNumber)
    print 'selected row count: %d' % len (selectedRowNames)
    print selectedRowNames
    #mb.selectRowsByName (selectedRowNames)

#--------------------------------------------------------------------------------------------
class GeneFinder:
  """
   a class for finding the genes transcriptionally controlled by specific tiles
  """
  def __init__ (self, cw):

    self.cw = cw
    self.na = cw.getNodeAttributes ()
    self.geneAndTileInfo = self.__extractGeneAndTileInfo ()

  #------------------------------------------------------------------------------------------
  def selectCandidateGenes (self, distance):

    selectedTiles = self.__getSelectedTiles ()
    genes = []
    for tile in selectedTiles:
      candidates = self.__findTranscriptionCandidates (tile, distance)
      genes += candidates
  
    #print 'genes to select: %s' % genes
    self.cw.selectNodesByName (genes, 0)

  #------------------------------------------------------------------------------------------
  def __extractGeneAndTileInfo (self):
    """
     look at all the nodes in the cytoscape window from which the
     the matrix browser was launched.  examine each node's attributes
     and store those relevant for our purpose
    """
    result = {}
    nodeCount = 0
    geneCount = 0
    for node in cw.graph.getNodeArray ():
      nodeCount += 1
      canonicalName = cw.getCanonicalNodeName (node)
      if (self.na.hasAttribute ('Orientation', canonicalName)):
        geneCount += 1
        start = self.na.getIntegerValue ('Start', canonicalName)
        stop  = self.na.getIntegerValue ('Stop', canonicalName)
        orientation =  self.na.getValue ('Orientation', canonicalName)
        replicon =  self.na.getValue ('replicon', canonicalName)
        hash = {'start': start, 'stop': stop, 'orientation': orientation, 'replicon': replicon}
        result [canonicalName] = hash
        #print geneCount
  
    return result     

  #------------------------------------------------------------------------------------------
  def __findTranscriptionCandidates (self, tileName, distance):
  
    result = []
    tileStart = self.na.getIntegerValue ('Start', tileName)
    tileStop  = self.na.getIntegerValue ('Stop', tileName)
    tileReplicon = self.na.getValue ('replicon', tileName)
    #print 'selected tile: %s  %d:%d  (%s)' % (tileName, tileStart, tileStop, tileReplicon)
    for geneName in self.geneAndTileInfo.keys ():
      geneStart = self.geneAndTileInfo [geneName]['start']
      geneStop  = self.geneAndTileInfo [geneName]['stop']
      geneOrientation = self.geneAndTileInfo [geneName]['orientation']
      geneReplicon = self.geneAndTileInfo [geneName]['replicon']
      #if (geneName.count ('0429H') or geneName.count ('0430H')):
      #  print '  %s  %d:%d  (%s, %s)' % (geneName, geneStart, geneStop, geneOrientation, geneReplicon)
      if (self.__isDownstream (tileStart, tileStop, tileReplicon, 
                               geneStart, geneStop, geneOrientation, geneReplicon, distance)):
        #print '%s may be transcribed by %s' % (geneName, tileName)
        result.append (geneName)
  
    return result
  
  #---------------------------------------------------------------------------------------------
  def __isDownstream (self, tileStart, tileStop, tileReplicon, 
                      geneStart, geneStop, geneOrientation, geneReplicon, distance):
    """
     a gene is judged downstream of a tile if its start position is within the tile
     or within <distance> base pairs of any portion of the tile
    """
  
    if (not tileReplicon == geneReplicon):
      return 0

    if ((geneStart >= tileStart) and (geneStart <= tileStop)):
      return 1

    if (geneOrientation == 'For'):
      gap = geneStart - tileStart
      if (gap > 0 and gap < distance):
        return 1
  
    if (geneOrientation == 'Rev'):
      gap = tileStop - geneStart
      if (gap > 0 and gap < distance):
        return 1
  
    return 0     
  
  #---------------------------------------------------------------------------------------------
  def __getSelectedTiles (self):
  
    nc = self.cw.graph.selectedNodes ()
    sn = []
    while nc.ok ():
      sn.append (nc.node ())
      nc.next ()
  
    result = []
  
    for node in sn:
      canonicalName = cw.getCanonicalNodeName (node)
      if (not self.na.hasAttribute ('Orientation', canonicalName)):
        result.append (canonicalName)
  
    return result

#--------------------------------------------------------------------------------------------
#def marc ():
#
#  MarcsGui ()
#
#--------------------------------------------------------------------------------------------
System.out.println ('~/tomcat/server1/webapps/halo/tiles/__run__.py')
System.out.println ("intializing R....")
sys.add_package ('org.rosuda.JRclient')
import org.rosuda.JRclient
host = 'trickster'
try:
  r = org.rosuda.JRclient.Rconnection (host)
  System.out.println (r.eval ('R.version.string'))
except:
  print 'failed to find R server at %s' % host

marcsGui = MarcsGui ()

