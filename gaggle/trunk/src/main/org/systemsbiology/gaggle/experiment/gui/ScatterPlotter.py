# ScatterPlotter.py
#---------------------------------------------------------------------------------------------
from javax.swing import *
from java.awt import *
from java.awt.event import *
from java.lang import Double
from java.lang import Runnable

from org.jfree.chart import *
from org.jfree.chart.axis import *
from org.jfree.chart.labels import *
from org.jfree.chart.plot import *
from org.jfree.chart.renderer import *
from org.jfree.chart.entity import *
from org.jfree.data import *
#from org.jfree.data.xy import *
from org.jfree.ui import *


from csplugins.isb.dtenenbaum.plot2d import UnsortedXYSeries

from csplugins.isb.pshannon.experiment.gui import *
import csplugins.isb.pshannon.experiment.gui.actions as actions

#---------------------------------------------------------------------------------------------
# $Revision: 18 $   $Date: 2005/01/14 21:30:27 $
#---------------------------------------------------------------------------------------------
class ScatterPlotter (JFrame, ChartMouseListener,  XYToolTipGenerator, SelectionAreaController):

  #------------------------------------------------------------------------------------------
  def __init__ (self, pointNames, xVector, yVector, 
                xAxisLabel='x', yAxisLabel='y', title='XY Plotter', ):

    JFrame.__init__ (self, title=title, size = (400, 400))
    print 'constructing Scatter plotter, (%d, %d)' % (len (xVector), len (yVector))
    #self.connectToTupleSpace ()
    self.xAxisLabel = xAxisLabel
    self.yAxisLabel = yAxisLabel
    self.xyData = self.assembleData (xVector, yVector, pointNames)
    self.pointNames = []  # used for creating tooltips, which happens far from data initialization
    self.namesToBroadcast = []
    self.getContentPane().add (self.createGui ())
    self.show ()

  #-------------------------------------------------------------------------------------------
  #def connectToTupleSpace (self):
  #
  #  self.tupleSpace = TupleSpace ('coord', 'localhost')

  #-------------------------------------------------------------------------------------------
  def createGui (self):
 
    panel = JPanel ()
    panel.setLayout (BorderLayout ())


    title = None
    xAxisLabel = self.xAxisLabel
    yAxisLabel = self.yAxisLabel
    chartDataset = self.createChartDataSet (self.xyData, 'test data')
    plotOrientation = PlotOrientation.VERTICAL
    legend = 0
    tooltips = 0
    urls = 1

    chart = ChartFactory.createXYLineChart (title, xAxisLabel, yAxisLabel, chartDataset,
                                            plotOrientation, legend, tooltips, urls)
                                            
    plotter = chart.getXYPlot ()
    plotter.setRenderer (StandardXYItemRenderer (StandardXYItemRenderer.SHAPES))
    plotter.getRenderer().setToolTipGenerator (MyToolTipGenerator (self.pointNames))
    

    self.chartPanel = SelectionArea (chart,self)
    
    self.chartPanel.setMouseZoomable(1)
    self.chartPanel.setVerticalZoom(1)
    self.chartPanel.setHorizontalZoom(1)
    
    self.chartPanel.addChartMouseListener (self)


    closeButton = JButton ('Dismiss', actionPerformed=self.dismiss)
    buttonPanel = JPanel ()
    
    lbl = JLabel("Drag Action:")
    self.cbox = JComboBox(actionListener=self.toggle(self.chartPanel))
    self.cbox.addItem("Zoom")
    self.cbox.addItem("Select")
    
    self.readout = JTextField (20)
    self.readout.setEditable(0)
    
    #broadcastButton = JButton(actions.IconFactory.getBroadcastIcon(), actionPerformed=self.broadcast)
    #broadcastButton.setBackground(Color.WHITE)
    #broadcastButton.setToolTipText('Broadcast')
    
    topPanel = JPanel()
    topPanel.setLayout(BorderLayout())
    botPanel = JPanel()
    botPanel.setLayout(BorderLayout())
    
    topPanel.add(lbl, BorderLayout.WEST)
    topPanel.add(self.cbox, BorderLayout.CENTER)
    #topPanel.add(broadcastButton, BorderLayout.EAST)
    
    botPanel.add(self.readout, BorderLayout.WEST)
    botPanel.add(closeButton, BorderLayout.CENTER)
    
    bothPanel = JPanel()
    bothPanel.setLayout(BorderLayout())
    
    bothPanel.add(topPanel, BorderLayout.CENTER)
    bothPanel.add(botPanel, BorderLayout.SOUTH)
    
    panel.add (bothPanel, BorderLayout.SOUTH)

    panel.add (self.chartPanel, BorderLayout.CENTER)
    return panel

  #-------------------------------------------------------------------------------------------
  def createChartDataSet (self, xyPoints, name='data'):

    #autoSort = 0
    #allowDuplicateXValues = 0
    
    xySeries = UnsortedXYSeries (name) #, autoSort, allowDuplicateXValues) 
    for i in range (len (xyPoints)):
      point = xyPoints [i]
      x = point.getX ()
      y = point.getY ()
      if (Double.isNaN (x) or Double.isNaN (y)):
        continue
      xySeries.add (x, y)
      self.pointNames.append (point.getName ())


    return XYSeriesCollection (xySeries)

  #-------------------------------------------------------------------------------------------
#  def broadcast(self, event):
#      # write your broadcast method here. The names to broadcast are contained in
#      # self.namesToBroadcast, an array of strings which may contain 0 elements. 
#      
#      ##print "In broadcast, here are the names to broadcast: %s" % self.namesToBroadcast
#      print "In broadcast, there are %s names to broadcast." % len(self.namesToBroadcast)
#      
#      # do your broadcast here
#      multiTuple = Tuple ()
#      for name in self.namesToBroadcast:
#        tuple = Tuple (name, 'Halobacterium sp.', 'ScatterPlotter')
#        tuple.setExpire (4000)
#        multiTuple.add (tuple)
#      print '==== ScatterPlotter about to broadcast %d names' % len (self.namesToBroadcast)
#      if (len (self.namesToBroadcast) > 0):
#        self.tupleSpace.multiWrite (multiTuple)
#      print '==== ScatterPlotter broadcast complete'
#      
#      # when done, clear the selection rectangle:
#      self.chartPanel.clearSelection()
#      # and clear out the names to be broadcast
#      self.namesToBroadcast = []

  #-------------------------------------------------------------------------------------------
  def displayMouseSelectedEntity (self, entity):

    print '------------ entity: %s' % entity
    dataset = entity.getDataset ()
    seriesNumber = entity.getSeries ()
    itemNumber = entity.getItem ()
    xValue = dataset.getXValue (seriesNumber, itemNumber)
    yValue = dataset.getYValue (seriesNumber, itemNumber)
    tooltipText = entity.getToolTipText ()
    print 'mouse clicked or over: %d,%d: %s' % (xValue, yValue, tooltipText)
    
    string = '%s: (%5.2f, %5.2f)' % (tooltipText, xValue, yValue)
    self.readout.setText (string)
 
  #-------------------------------------------------------------------------------------------
  def chartMouseClicked (self, event):

    entity = event.getEntity ()
    if (entity == None):
      return
    self.displayMouseSelectedEntity (entity)

  #-------------------------------------------------------------------------------------------
  def chartMouseMoved (self, event):

    entity = event.getEntity ()
    if (entity):
      self.displayMouseSelectedEntity (entity)

  #---------------------------------------------------------------------------------------------
  def assembleData (self, x, y, names):

    assert (len (x) == len (y))
    print 'names (%d): %s' % (len (names), names)
    print 'x (%d): %s' % (len (x), x)
    assert (len (names) == len (x))
    result = []
    for i in range (len (x)):
      result.append (xyPoint (names [i], x [i], y [i]))
  
    return result

  #-------------------------------------------------------------------------------------------
  #-------------------------------------------------------------------------------------------
  def setNamesToBroadcast(self, namesToBroadcast):
      self.namesToBroadcast = namesToBroadcast
  #-------------------------------------------------------------------------------------------
  
  def setDraggedRectangle(self, rect):
      ents = self.chartPanel.getChartRenderingInfo().getEntityCollection()
      it = ents.iterator()
      
      #self.namesToBroadcast = []
      
      while (it.hasNext()):
          item = it.next()
          r2 = item.getArea().getBounds()
          if (rect.contains(r2)):
              self.namesToBroadcast.append(item.getToolTipText())
      
      dw = DataWindow(self.namesToBroadcast, self)
              
  #-------------------------------------------------------------------------------------------

  class toggle (ActionListener):
      def __init__(self, chartPanel):
          self.chartPanel = chartPanel
      def actionPerformed(self, e):
          if (e.getSource().getSelectedItem() == 'Zoom'):
              self.chartPanel.clearSelection();
              zoom = 1
          else:
              zoom = 0
          
          self.chartPanel.setEnableCustomDrag(not zoom)
          self.chartPanel.setMouseZoomable(zoom,1)
          self.chartPanel.setHorizontalZoom(zoom)
          self.chartPanel.setVerticalZoom(zoom)
  #-------------------------------------------------------------------------------------------
  def dismiss (self, event):

   self.dispose ()

#---------------------------------------------------------------------------------------------
class MyToolTipGenerator (XYToolTipGenerator):

  def __init__ (self, pointNames):
    self.pointNames = pointNames

  def generateToolTip (self, xyDataset, series, item):
    s = '%s' % self.pointNames [item]
    return s

#---------------------------------------------------------------------------------------------
class xyPoint:

  def __init__ (self, name, x, y):
    self.name = name
    self.x = x
    self.y = y

  def getName (self):
    return self.name

  def getX (self):
    return self.x

  def getY (self):
    return self.y

#---------------------------------------------------------------------------------------------
class DataWindow (JFrame):
    
    def __init__(self, nameList, parent):
        JFrame.__init__ (self, title='Selected Names', size = (220, 300))
        self.setLocationRelativeTo(parent) 
        panel = JPanel()
        panel.setLayout(BorderLayout())
        nameList.sort()
        list = JList(nameList)
        scrollPane = JScrollPane(list)
        panel.add(scrollPane, BorderLayout.CENTER)
        okButton = JButton('OK', actionPerformed = self.dismiss)
        panel.add(okButton, BorderLayout.SOUTH)
        self.getContentPane().add(panel)
        #self.pack()
        self.show()
        
    def dismiss(self, event):
        self.dispose()

 
#---------------------------------------------------------------------------------------------
if (__name__ == '__main__'):
  pointNames = ['A', 'B', 'C', 'D', 'E', 'F', 'G']
  x = [0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0]
  y = [0.0, 1.0, 4.0, 9.0, 16.0, 25.0, 36.0]
  sp = ScatterPlotter (pointNames, x, y);
