# makefile for org/systemsbiology/gaggle/unitTests
#-------------------------------------------------------------------------
# $Revision: 1.2 $
# $Date: 2004/12/14 20:15:48 $
# $Author: pshannon $
#--------------------------------------------------------------------

JC=javac

PKG=org.systemsbiology.gaggle.boss.unitTests

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

OBJS =  NameUniquifierTest.class \
        TileCalculatorTest.class



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
#  build and run the NameUniquifierTest
#
0: NameUniquifierTest.class
	java $(PKG).NameUniquifierTest

#------ 1
#  build and run the TileCalculatorTest
#
1: TileCalculatorTest.class
	java $(PKG).TileCalculatorTest

