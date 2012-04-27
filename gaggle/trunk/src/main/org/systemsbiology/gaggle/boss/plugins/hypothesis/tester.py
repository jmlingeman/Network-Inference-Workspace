from javax.swing import *
from csplugins.isb.pshannon.gaggle.bossPlugins.hypothesis import HypothesisRecorder
import os, sys

hp = HypothesisRecorder (None)

f = JFrame ('mss demo', size=(600,600))
f.getContentPane().add (hp)
f.pack ()
f.show ()
