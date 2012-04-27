# makefile for org/systemsbiology/gaggle/experiment/datamatrix/unitTests
#-------------------------------------------------------------------------
# $Revision: 19 $
# $Date: 2005-07-29 20:25:11 -0400 (Fri, 29 Jul 2005) $
# $Author: pshannon $
#--------------------------------------------------------------------

JC=javac

PKG=org.systemsbiology.gaggle.network.unitTests

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

OBJS = InteractionTest.class \
       NetworkTest.class

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
#  build and run the InteractionTest
#
0: InteractionTest.class
	java $(PKG).InteractionTest

#------ 1
#  build and run the NetworkTest
#
1: NetworkTest.class
	java $(PKG).NetworkTest

