# makefile for org/systemsbiology/gaggle/experiment/gui/plotters/matrix
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

OBJS =  NamedXYSeries.class \
        ConditionNameShortener.class \
        MatrixPlotter.class \
        SelectableMatrixPlotter.class


objs: $(OBJS)

clean:
	- find . -name "*.class" -exec rm  {} ';'

#
# testpy:  from python, run the MatrixPlotter with sample data
#
testpy:
	jython mpDemo.py

#
# test0:  run the MatrixPlotter with sample data, which includes some missing values
#
test0: objs
	java org.systemsbiology.gaggle.experiment.gui.plotters.matrix.MatrixPlotter \
            ../../../sampleData/sample10-someMissing.ratio

#
# test1:  run the SelectableMatrixPlotter with sample data, which includes some missing values
#
test1: objs
	java org.systemsbiology.gaggle.experiment.gui.plotters.matrix.SelectableMatrixPlotter \
            ../../../sampleData/sample10-someMissing.ratio
#            ../../../sampleData/simpleMatrix.txt

help:
	egrep "^#" makefile | sed "s/^#//"





