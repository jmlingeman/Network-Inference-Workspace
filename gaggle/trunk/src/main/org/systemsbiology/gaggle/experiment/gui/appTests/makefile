# makefile for experiment/gui/appTests
#-------------------------------------------------------------------------
# $Revision: 18 $
# $Date: 2005/03/15 21:37:39 $
# $Author: pshannon $
#--------------------------------------------------------------------
help:
	egrep "^#" makefile | sed "s/^#//"


#------ all
#  run all the tests
#
all: 0

#------ test
#  run the current exploratory script, 'tester.py'
#
test: 
	jython -i tester.py

#------ 0
#  run the TreeDataViewer app tests
#
0: 
	jython TreeDataViewerTest.py

