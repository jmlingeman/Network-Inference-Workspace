from javax.swing import *
#from java.awt import *
from java.awt.event import *
from java.io import File
from javax.swing.tree import *
from javax.swing.event import *
import os, sys
from org.systemsbiology.gaggle.experiment.readers import *
from org.systemsbiology.gaggle.experiment.datamatrix import *
from org.systemsbiology.gaggle.experiment.gui.plotters.matrix import *
#from csplugins.isb.dtenenbaum.nameHelper import *

#reader = DataMatrixFileReader ('../../../sampleData/simpleMatrix.txt')
reader = DataMatrixFileReader ('../../../sampleData/simpleMatrixLongConditionNames.txt')
reader.read ()
matrix = reader.get ()
#lens = LensedDataMatrix (matrix)
#lens.enableAllRows ()
#print 'rows: %d' % lens.getRowCount ()
#nameHelper = NameHelperFactory.getNameHelper();
#browser = MatrixPlotter ("mpDemo", "x", "y", lens, nameHelper)
browser = MatrixPlotter (matrix)

f = JFrame ('mp demo', size=(600,600))
f.getContentPane().add (browser)
f.pack ()
f.show ()
