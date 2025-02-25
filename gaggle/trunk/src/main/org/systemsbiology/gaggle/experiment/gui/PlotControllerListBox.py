# PlotControllerListBox.py
#---------------------------------------------------------------------------------------------
from javax.swing import *
from java.awt import *
from ScatterPlotter import *
#-----------------------------------------------------------------------
# $Revision: 18 $   $Date: 2005/01/14 21:31:14 $
#-----------------------------------------------------------------------
class PlotControllerListBox (JFrame):

  #------------------------------------------------------------------------------------------
  def __init__ (self, matrices):

    JFrame.__init__ (self, title='Chooser', size = (200, 200))
    self.matrices = matrices
    assert (len (self.matrices) == 2)
    self.getContentPane().add (self.createGui ())
    self.currentSelection = []

    self.show ()

  #-------------------------------------------------------------------------------------------
  def createGui (self):
 
    listboxPanel = JPanel ()
    listboxPanel.setLayout (BorderLayout ())
    listItems = self.matrices [0].getColumnTitles ()
    self.listbox = JList (listItems, valueChanged=self.listSelectionChangedCallback)
    #self.listbox.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
    listboxscrollPane = JScrollPane (self.listbox)
    listboxPanel.add (listboxscrollPane, BorderLayout.CENTER)
    border = BorderFactory.createEmptyBorder (10, 10, 10, 10)
    listboxPanel.setBorder (border)
    outerPanel = JPanel ()
    outerPanel.setLayout (BorderLayout ())

    buttonPanel = JPanel ()

    plotButton = JButton ('Plot', actionPerformed=self.doPlot)
    dismissButton = JButton ('Dismiss', actionPerformed=self.dismiss)

    buttonPanel.add (plotButton)
    buttonPanel.add (dismissButton)

    outerPanel.add (buttonPanel, BorderLayout.SOUTH)
    outerPanel.add (listboxPanel, BorderLayout.CENTER)

    return outerPanel

  #-------------------------------------------------------------------------------------------
  def doPlot (self,event):

    m0 = self.matrices [1]
    m1 = self.matrices [0]
    columnName = self.currentSelection [0]
    dataX = m0.getColumn (columnName)
    dataY = m1.getColumn (columnName)
    pointNames = m0.getRowTitles ()
    xAxisLabel = m0.getShortName ()
    yAxisLabel = m1.getShortName ()
    plotter = ScatterPlotter (pointNames, dataX, dataY, xAxisLabel, yAxisLabel, columnName)

  #-------------------------------------------------------------------------------------------
  def dismiss (self,event):

    self.dispose ()

  #-----------------------------------------------------------------------
  def listSelectionChangedCallback (self, event):
 
    if (event.getValueIsAdjusting ()):
      return

    self.currentSelection = [s for s in self.listbox.getSelectedValues ()]
    print 'current selection: %s' % self.currentSelection


#-----------------------------------------------------------------------
if __name__ == '__main__':

  nameList = ['aaaa', 'bbbbb', 'ccccc']
  controller = PlotControllerListBox (nameList)
