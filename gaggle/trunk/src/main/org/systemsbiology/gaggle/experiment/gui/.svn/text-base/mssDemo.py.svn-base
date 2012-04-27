from javax.swing import *
from java.awt import *
from java.awt.event import *
from java.io import File
from javax.swing.tree import *
from javax.swing.event import *
import os, sys
from org.systemsbiology.gaggle.experiment.readers import *
from org.systemsbiology.gaggle.experiment.datamatrix import *
from org.systemsbiology.gaggle.experiment.gui import *

if (len (sys.argv) == 2):
  f = sys.argv [1]
else:
  f = '../sampleData/sample.ratio'

#f = '/Users/mpshannon/data/hpylori/2005-may/ratios.table.txt'

reader = DataMatrixFileReader (f)
#reader = DataMatrixFileReader ('../sampleData/simpleMatrix.txt')
#reader = DataMatrixFileReader ('../sampleData/huge.ratio')

reader.read ()
ratios = reader.get ()
print 'rows: %d' % ratios.getRowCount ()
browser = MatrixSpreadsheet (ratios)

f = JFrame ('mss demo', size=(600,600))
f.getContentPane().add (browser)
f.pack ()
f.show ()
