# makefile for experiment/datamatrix/unitTests
#-------------------------------------------------------------------------
# $Revision: 18 $
# $Date: 2005/02/17 17:09:05 $
# $Author: pshannon $
#--------------------------------------------------------------------

JC=javac

PKG=org.systemsbiology.gaggle.experiment.gui.unitTests

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

OBJS =  SpreadsheetLineFinderTest.class

help:
	egrep "^#" makefile | sed "s/^#//"



objs: $(OBJS)


default: objs run

clean:
	- rm *.class

#------ all
#  run all the tests
#
all: $(OBJS) 0 1

#------ 0
#  build and run the SpreadsheetLineFinderTest
#
0: SpreadsheetLineFinderTest.class
	java $(PKG).SpreadsheetLineFinderTest

#------ 1
#  build and run the DataMatrixTableModelTest
#
1: DataMatrixTableModelTest.class
	java $(PKG).DataMatrixTableModelTest

