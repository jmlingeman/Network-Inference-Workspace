from javax.swing import *
from java.awt import *
from java.awt.event import *
from java.io import File
from javax.swing.tree import *
from javax.swing.event import *
import os, sys
from csplugins.isb.pshannon.experiment.readers import *
from csplugins.isb.pshannon.experiment.datamatrix import *
from csplugins.isb.pshannon.experiment.gui import *

reader = DataMatrixFileReader ('../sampleData/sample.ratio')
reader.read ()
ratios = reader.get ()
print 'rows: %d' % ratios.getRowCount ()
browser = DataMatrixBrowser (None, [ratios])

f = JFrame ('dmb demo', size=(600,600))
f.getContentPane().add (browser)
f.pack ()
f.show ()
