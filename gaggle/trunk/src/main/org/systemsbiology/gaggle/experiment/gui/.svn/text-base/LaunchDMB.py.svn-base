
import csplugins.isb.pshannon.experiment.gui as gui
from csplugins.isb.pshannon.experiment.readers import DataMatrixFileReader
from csplugins.isb.pshannon.experiment.datamatrix import DataMatrix
from javax.swing import *
import sys

if (len(sys.argv)==1):
    print "Supply 1 or more matrix name arguments."
    sys.exit(0)

del(sys.argv[0])
matrices = []
for arg in sys.argv:
    reader = DataMatrixFileReader(arg)
    try:
        reader.read()
        matrix = reader.get()
        matrices.append(matrix)
    except:
        print "Caught Exception."


f = JFrame("Data Matrix Browser")
dmb = None
try:
    dmb = gui.DataMatrixBrowser(None, matrices, None)
except:
    print "yadayadayada"
f.getContentPane().add(dmb)
f.pack()
f.show()
