import sys
from csplugins.isb.pshannon.experiment.readers import DataMatrixFileReader
from csplugins.isb.pshannon.experiment.datamatrix import DataMatrix
from PlotControllerListBox import *

if __name__ == '__main__':
    del (sys.argv[0])
    if len(sys.argv) != 2:
        print "Please supply two matrix filenames."
        sys.exit()
    matrices = []
    for arg in sys.argv:
        reader = DataMatrixFileReader(arg)
        try:
            reader.read()
            matrix = reader.get()
            matrices.append(matrix)
        except:
            print "Caught Exception."
            
    controller = PlotControllerListBox(matrices)
    
        
    