# copyToHazel.py:  create a fresh copy of the R package, creating a new yyyy-mm-dd directory
# if needed.
#----------------------------------------------------------------------------------------------
import sys
import os
from os.path import *
from time import *
#----------------------------------------------------------------------------------------------
deleteVignettesDirectory = 1

print sys.argv

if (len (sys.argv) >= 2 and sys.argv [1] == '-vig'):
  deleteVignettesDirectory = 0


suffix = strftime ('%Y-%m-%d')
targetDirectory = '/users/pshannon/work/r/gaggle-package-%s' % suffix

if (exists (targetDirectory)):
  cmd = 'rm -rf %s/' % targetDirectory
  os.system (cmd)

os.mkdir (targetDirectory)
print 'copying files to %s' % targetDirectory

cmd = 'cp -pr * %s' % targetDirectory
os.system (cmd)

cmd = 'find %s -name .svn | xargs rm -rf' % targetDirectory
print 'deleting subversion files copied there'
os.system (cmd)

if (deleteVignettesDirectory):
  cmd = 'rm -rf %s/gaggle/inst/doc' % targetDirectory
  msg = 'deleting vignettes directory'
else:
  cmd = 'rm -rf %s/gaggle/inst/doc/makefile' % targetDirectory
  msg = 'deleting just makefile in vignettes directory'

print msg
os.system (cmd)


