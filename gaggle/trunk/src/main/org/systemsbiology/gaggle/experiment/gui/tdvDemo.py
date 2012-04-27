from javax.swing import *
from java.awt import *
from java.awt.event import *
from java.io import File
from javax.swing.tree import *
from javax.swing.event import *
import os, sys
from org.systemsbiology.gaggle.experiment.gui import *
#-----------------------------------------------------------------------------------------
if (len (sys.argv) == 2):
  repos = sys.argv [1]
else:
  repos = "http://db.systemsbiology.net/cytoscape/gaggle/pub/hpy/"

#repos = "httpIndirect://db.systemsbiology.net:8080/halo/PubDataFetcher.py"
#repos = "file:///users/mpshannon/data/macrophage/vtStatic/macrophage.xml"

#tdv = TreeDataViewer ()
print 'starting tdvDemo.py with repos: %s' % repos
tdv = TreeDataViewer (repos)
