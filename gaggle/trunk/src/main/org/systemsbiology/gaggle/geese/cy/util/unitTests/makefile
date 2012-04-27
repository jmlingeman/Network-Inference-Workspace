# makefile for csplugins.isb.pshannon.inferelator.unitTests
#-------------------------------------------------------------------------
# $Revision: 18 $
# $Date: 2005/01/28 20:59:06 $
# $Author: pshannon $
#--------------------------------------------------------------------
JC=javac -source 1.4
JR=java 

PKG=org.systemsbiology.gaggle.geese.cy.util.unitTests

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

OBJS =  NodeNameMatcherTest.class 

default: objs run

objs: $(OBJS)


clean:
	- rm *.class

run: $(OBJS) 0 

# --- 0
#  run NodeNameMatcherTest
#

0: NodeNameMatcherTest.class
	$(JR) $(PKG).NodeNameMatcherTest

help:
	egrep "^#" makefile | sed "s/^#//"




