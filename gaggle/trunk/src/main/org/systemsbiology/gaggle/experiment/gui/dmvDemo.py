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

reader = DataMatrixFileReader ('../sampleData/sample.ratio')
reader.read ()
matrix1 = reader.get ()
print 'rows: %d' % matrix1.getRowCount ()

reader = DataMatrixFileReader ('../sampleData/simpleMatrix.txt')
reader.read ()
matrix2 = reader.get ()

viewer = DataMatrixViewer ([matrix1, matrix2])

f = JFrame ('mss demo', size=(600,600))
f.getContentPane().add (viewer)
#f.pack ()
f.show ()
