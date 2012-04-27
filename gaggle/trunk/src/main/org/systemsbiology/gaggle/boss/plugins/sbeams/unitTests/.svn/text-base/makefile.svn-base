JC=javac
.SUFFIXES: .java .class
.java.class: 
	$(JC) $<
PKG=csplugins.isb.pshannon.gaggle.bossPlugins.sbeams.unitTests

OBJS = SBEAMSClientTest.class

default: objs run

objs: $(OBJS)

run: 
	java $(PKG).SBEAMSClientTest

help:
	egrep "^#" makefile | sed "s/^#//"

