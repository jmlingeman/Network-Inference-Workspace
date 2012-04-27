# makefile for org/systemsbiology/gaggle/experiment/gui/plotters/scatter
#-------------------------------------------------------------------------
# $Revision: 18 $
# $Date: 2005/04/13 16:53:01 $
# $Author: pshannon $
#-------------------------------------------------------------------------
#JC=javac
JC=javac -Xlint:deprecation
#JC=javac -Xlint:unchecked
JI=java

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

default: objs 

OBJS =  ScatterPlotter.class


objs: $(OBJS)

clean:
	- find . -name "*.class" -exec rm  {} ';'

# ----------------------------------------------------------------------------
# test:  run the ScatterPlotter with simple data, 1-6 and their squares
#

test: $(OBJS)
	java org.systemsbiology.gaggle.experiment.gui.plotters.scatter.ScatterPlotter

help:
	egrep "^#" makefile | sed "s/^#//"





