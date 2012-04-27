# spDemo.py:  run the ScatterPlotter for testing
#--------------------------------------------------------------------
# $Revision: 18 $   $Date: 2005/01/16 01:17:05 $
#--------------------------------------------------------------------
from javax.swing import *
from csplugins.isb.pshannon.experiment.gui import *
names = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i']
x = [1,1.1,1.2,1.3,1.4,1.5,1.6, 2, 3]
y = [1, 8, 8, 8, 8, 8, 6, 4, 9]
sp = ScatterPlotter (names, x, y, "ordinal numbers", "squares", "tester")
f = JFrame ()
f.getContentPane().add (sp)
f.pack ()
f.show ()
