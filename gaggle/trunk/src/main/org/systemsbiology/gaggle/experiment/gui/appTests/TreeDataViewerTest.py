#-----------------------------------------------------------------------------------------
# change all occurences of TreeDataViewer to the name of your actual class
#---------------------------------------------------------------------------
# RCSid: $Revision: 18 $   $Date: 2005/03/15 21:37:36 $
#--------------------------------------------------------------------------
import sys
import unittest
import time
#---------------------------------------------------------------------------
from java.lang import *
from csplugins.isb.pshannon.experiment.gui import *
from csplugins.isb.pshannon.experiment.datamatrix import *
from javax.swing import *
from javax.swing.tree import *
#---------------------------------------------------------------------------
class TreeDataViewerTestCase (unittest.TestCase):

  #----------------------------------------------------------------------
  def loadMetadataTest (self):
    """
     load metadata, and then data, from the 'repos' directory immediately
     below the current working directory.  select all the garlic and all
     the pepper data, check the selected condition counts, and then
     load all of the corresponding data
     make sure that the expected row and column count, and the row
     and column names are all present, in two matrices.
    """

    print 'loadMetadataTest'
    repos = 'file://repos'
    tdv = TreeDataViewer (repos)
    jtree = tdv.getJTree ()
    expandTree (jtree, jtree.getModel().getRoot())
    tdv.getMainFrame().pack()
    assert (jtree.getVisibleRowCount () == 20)
    assert (tdv.getSelectedConditionCount () == 0)
    garlicRow = 3
    jtree.setSelectionRows ([garlicRow])
    expected = '[Experiments, environmental, irradiation, garlic]'
    assert (jtree.getPathForRow (garlicRow).toString() == expected)

    assert (tdv.getSelectedConditionCount () == 16)
    pepperRow = 18
    jtree.setSelectionRows ([garlicRow, pepperRow])
    assert (tdv.getSelectedConditionCount () == 19)

    tdv.loadSelectedConditions ()
    dmv = tdv.getDataMatrixViewer ()
    assert (dmv.getTabCount () == 2)
    assert (dmv.getTabType (0) == 'MatrixSpreadsheet')
    assert (dmv.getTabType (1) == 'MatrixSpreadsheet')

  #----------------------------------------------------------------------
  def volcanoPlotButtonTest (self):
    """
     starts just like loadMetadataTest above, and proceeds thus:
       1) 
    """

    print 'loadMetadataTest'
    repos = 'file://repos'
    tdv = TreeDataViewer (repos)
    jtree = tdv.getJTree ()
    expandTree (jtree, jtree.getModel().getRoot())
    tdv.getMainFrame().pack()
    assert (jtree.getVisibleRowCount () == 20)
    assert (tdv.getSelectedConditionCount () == 0)
    garlicRow = 3
    jtree.setSelectionRows ([garlicRow])
    expected = '[Experiments, environmental, irradiation, garlic]'
    assert (jtree.getPathForRow (garlicRow).toString() == expected)

    assert (tdv.getSelectedConditionCount () == 16)
    pepperRow = 18
    jtree.setSelectionRows ([garlicRow, pepperRow])
    assert (tdv.getSelectedConditionCount () == 19)

    tdv.loadSelectedConditions ()
    dmv = tdv.getDataMatrixViewer ()
    assert (dmv.getTabCount () == 2)
    assert (dmv.getTabType (0) == 'MatrixSpreadsheet')
    assert (dmv.getTabType (1) == 'MatrixSpreadsheet')
    mss0 = dmv.getDataMatrixView (0)
    mss1 = dmv.getDataMatrixView (1)
    lambdas = mss0.getMatrix ()
    ratios = mss1.getMatrix ()
    assert (lambdas.getShortName () == 'lambdas')
    assert (ratios.getShortName () == 'log10 ratios')
    widgets = mss1.getWidgets ()

      # now do a scatter plot of ratios vs lambdas. check
      # to make sure that one is created in a new tab
    assert (widgets ['volcanoPlotButton'].isEnabled ())
    widgets ['volcanoPlotButton'].doClick ()
    lb = widgets ['vpcsListbox']
    lb.addSelectionInterval (1,1)
    widgets ['vpcsPlotButton'].doClick ()
    widgets ['vpcsDismissButton'].doClick ()
    assert (dmv.getTabCount () == 3)
    assert (dmv.getTabbedPane().getTitleAt (2) == 'garlic__0000gy-0010m')
    assert (dmv.getTabType (2) == 'ScatterPlotter')

      #-----------------------------------------------------------------------
      # finally, create a new matrix from a selection of the ratios matrix
      # since this matrix will have no companion (congruent) matrix,
      # the volcanoPlotButton on that matrixSpreadSheet should be disabled
      #-----------------------------------------------------------------------

    assert (dmv.getTabbedPane().getTitleAt (1) == 'log10 ratios')
    dmv.getTabbedPane().setSelectedIndex (1)
    mss1 = dmv.getDataMatrixView (1)
    ratios = dmv.getDataMatrixView(1).getMatrix ()
    assert (ratios.getRowCount () == 2400)
    assert (ratios.getColumnCount () == 19)
    rowsToSelect = [0,5,10,15,25,30,35,40,45,50]
    allRowTitles = ratios.getRowTitles ()
    selection = []
    for r in rowsToSelect:
      selection.append (allRowTitles [r])
    dmv.select ('Halobacterium sp.', selection)
    mss1.createNewMatrixFromSelection ('aaa')    
    assert (dmv.getTabCount () == 4)
    assert (dmv.getTabbedPane().getTitleAt (3) == 'aaa')
    mss2 = dmv.getDataMatrixView (3)
    widgets = mss2.getWidgets ()
    assert (not widgets ['volcanoPlotButton'].isEnabled ())
    littleMatrix = mss2.getMatrix ()
    assert (littleMatrix.getRowCount () == len (rowsToSelect))
    assert (littleMatrix.getColumnCount () == ratios.getColumnCount ())
    selectedNames = [x for x in selection]
    newNames = [x for x in littleMatrix.getRowTitles ()]
    assert (newNames == selectedNames)


  #----------------------------------------------------------------------

#---------------------------------------------------------------------------
def expandTree (tree, node):

  for kid in node.children ():
    if (not kid.isLeaf ()):
      path = TreePath (kid.getPath ())
      tree.expandPath (path)
      expandTree (tree, kid)

#---------------------------------------------------------------------------
suite = unittest.TestSuite ()
suite.addTest (TreeDataViewerTestCase ("loadMetadataTest"))
suite.addTest (TreeDataViewerTestCase ("volcanoPlotButtonTest"))
runner = unittest.TextTestRunner ()
runner.run (suite)
System.exit (0)
