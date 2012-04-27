# makefile for org/systemsbiology/gaggle/experiment/repository/unitTests
#-------------------------------------------------------------------------
# $Revision: 18 $
# $Date: 2004/10/25 23:20:36 $
# $Author: pshannon $
#--------------------------------------------------------------------

JC=javac

PKG=org.systemsbiology.gaggle.experiment.repository.unitTests

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

OBJS = ExperimentRepositoryTest.class

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
#  build and run ExperimentRepositoryTest
#
0: ExperimentRepositoryTest.class
	java $(PKG).ExperimentRepositoryTest


