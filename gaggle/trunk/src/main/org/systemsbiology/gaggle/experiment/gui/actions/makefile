# makefile for csplugins.browsers
#-------------------------------------------------------------------------
# $Revision: 18 $
# $Date: 2005/04/03 02:50:56 $
# $Author: pshannon $
#-------------------------------------------------------------------------
D=/home/pshannon/work/csplugins/trial/pshannon/dataCube
CP=$D/cytoscape.jar:$D/visad.jar:/home/pshannon/work
JC=javac -classpath $(CP)
JC=javac 

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

OBJS =  \
       IconFactory.class \
       CorrelationFinder.class \
       CorrelationFinderDialog.class \
       MatrixExporter.class \
       CreateNewMatrixFromSelection.class \
       MathematicalOperations.class \
       ThresholdSelectorDialog.class
#       PyConsoleLauncher.class \
#       FindCorrelations.class \
#       GetSelectionsFromNetwork.class \
#       SelectionPlotter.class \
#       SendSelectionsToNetwork.class \
#       RunMovie.class \
#       ExportMatrix.class \
#       DataMatrixMovieDialog.class \


default: objs

#
#------ objs
# compile all out of date source
# 

objs: $(OBJS)

clean:
	- rm *.class 



help:
	egrep "^#" makefile | sed "s/^#//"

