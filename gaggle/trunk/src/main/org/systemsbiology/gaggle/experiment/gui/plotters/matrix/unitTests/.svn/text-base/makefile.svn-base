# makefile for org/systemsbiology/gaggle/unitTests
#-------------------------------------------------------------------------
# $Revision: 1.2 $
# $Date: 2004/12/14 20:15:48 $
# $Author: pshannon $
#--------------------------------------------------------------------

JC=javac

PKG=org.systemsbiology.gaggle.experiment.gui.plotters.matrix.unitTests

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

OBJS =  ConditionNameShortenerTest.class \



help:
	egrep "^#" makefile | sed "s/^#//"

objs: $(OBJS)


default: objs run

clean:
	- rm *.class

#------ all
#  run all the tests
#
all: $(OBJS) 0

#------ 0
#  build and run the ConditionNameShortenerTest
#
0: ConditionNameShortenerTest.class
	java $(PKG).ConditionNameShortenerTest


