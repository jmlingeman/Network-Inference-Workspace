# makefile for csplugins.isb.pshannon.inferelator.unitTests
#-------------------------------------------------------------------------
# $Revision: 18 $
# $Date: 2005/01/28 20:59:06 $
# $Author: pshannon $
#--------------------------------------------------------------------
JC=javac
JR=java 

PKG=org.systemsbiology.gaggle.geese.names.unitTests

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

OBJS =  TranslatorTest.class

help:
	egrep "^#" makefile | sed "s/^#//"

default: objs run

objs: $(OBJS)


clean:
	- rm *.class

run: $(OBJS) 0 

# --- 0
#  run TranslatorTest
#

0: TranslatorTest.class
	$(JR) $(PKG).TranslatorTest


