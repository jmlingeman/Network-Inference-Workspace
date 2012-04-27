# copyToHazel.py:  create a fresh copy of the gaggleGo R package, creating a new
# yyyy-mm-dd directory if needed.
#----------------------------------------------------------------------------------------------
import os
from os.path import *
from time import *
#----------------------------------------------------------------------------------------------
suffix = strftime ('%Y-%m-%d')
targetDirectory = '/users/pshannon/work/r/gaggleGo-package-%s' % suffix

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

