# makefile for org/systemsbiology/gaggle/experiment/datamatrix/unitTests
#-------------------------------------------------------------------------
# $Revision: 19 $
# $Date: 2005-07-29 17:25:11 -0700 (Fri, 29 Jul 2005) $
# $Author: pshannon $
#--------------------------------------------------------------------

JC=javac

PKG=org.systemsbiology.gaggle.geese.rShell.unitTests

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

OBJS = StringToOjbectConverterTest.class

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
#  build and run the StringToObjectConverterTest
#
0: StringToObjectConverterTest.class
	java $(PKG).StringToObjectConverterTest

