#---------------------------------------------------------------------------
from csplugins.isb.pshannon.experiment.gui import *
from csplugins.isb.pshannon.experiment.datamatrix import *
from javax.swing import *
from javax.swing.tree import *
#---------------------------------------------------------------------------
def expandTree (tree, node):

  for kid in node.children ():
    if (not kid.isLeaf ()):
      path = TreePath (kid.getPath ())
      tree.expandPath (path)
      expandTree (tree, kid)

#---------------------------------------------------------------------------
repos = 'file://repos'
tdv = TreeDataViewer (repos)
jtree = tdv.getJTree ()
expandTree (jtree, jtree.getModel().getRoot())
print 'row count %d' % jtree.getRowCount ()
tdv.getMainFrame().pack ()

garlicRow = 3
jtree.setSelectionRows ([garlicRow])
expected = '[Experiments, environmental, irradiation, garlic]'
pepperRow = 18
jtree.setSelectionRows ([garlicRow, pepperRow])
tdv.loadSelectedConditions ()
dmv = tdv.getDataMatrixViewer ()
mss0 = dmv.getDataMatrixView (0)
mss1 = dmv.getDataMatrixView (1)
lambdas = mss0.getMatrix ()
ratios = mss1.getMatrix ()
print 'ratios size: %d x %d' % (ratios.getRowCount (), ratios.getColumnCount ())
widgets = mss1.getWidgets ()
assert (widgets ['volcanoPlotButton'].isEnabled ())
widgets ['volcanoPlotButton'].doClick ()
dialog = widgets ['volcanoPlotColumnSelector']
keys = [x for x in widgets.keySet ()]
lb = widgets ['vpcsListbox']
lb.addSelectionInterval (1,1)
widgets ['vpcsPlotButton'].doClick ()
widgets ['vpcsDismissButton'].doClick ()
dmv.getTabbedPane().setSelectedIndex (1)
ratios = dmv.getDataMatrixView(1).getMatrix ()
assert (ratios.getRowCount () == 2400)
assert (ratios.getColumnCount () == 19)
rowsToSelect = [0,5,10,15,25,30,35,40,45,50]
allRowTitles = ratios.getRowTitles ()
selection = []
for r in rowsToSelect:
  selection.append (allRowTitles [r])
dmv.select ('Halobacterium sp.', selection)
widgets = dmv.getDataMatrixView (1).getWidgets ()



