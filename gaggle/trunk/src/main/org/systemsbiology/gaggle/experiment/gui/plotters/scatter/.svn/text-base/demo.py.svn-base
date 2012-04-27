from javax.swing import *
from java.awt.event import *
from java.io import File
from javax.swing.tree import *
from javax.swing.event import *
import os, sys
from org.systemsbiology.gaggle.experiment.gui.plotters.scatter import *

xVector = [-10.0, -9.0, -8.0, -7.0, -6.0, -5.0, -4.0, -3.0, -2.0, -1.0, 0.0,
           1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0]
yVector = [100.0, 81.0, 64.0, 49.0, 36.0, 25.0, 16.0, 9.0, 4.0, 1.0, 0.0,
          1.0, 4.0, 9.0, 16.0, 25.0, 36.0, 49.0, 64.0, 81.0, 100.0]

names = ['-10.0', '-9.0', '-8.0', '-7.0', '-6.0', '-5.0', '-4.0', '-3.0', '-2.0', '-1.0', '0.0',
           '1.0', '2.0', '3.0', '4.0', '5.0', '6.0', '7.0', '8.0', '9.0', '10.0']

p = ScatterPlotter (names, xVector, yVector)

f = JFrame ('mp demo', size=(600,600))
f.getContentPane().add (p)
f.pack ()
f.show ()
