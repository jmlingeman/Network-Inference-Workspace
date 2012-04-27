# makefile for csplugins.trial.pshannon.dataCube
#-------------------------------------------------------------------------
# $Revision: 17 $
# $Date: 2005-07-29 20:19:31 -0400 (Fri, 29 Jul 2005) $
# $Author: pshannon $
#-------------------------------------------------------------------------
D=/home/pshannon/work/csplugins/trial/pshannon/dataCube
CP=$D/cytoscape.jar:$D/visad.jar:/home/pshannon/work
JC=javac 
JI=java

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

default: objs 

OBJS =  DataMatrix.class \
        LensedDataMatrix.class \
        MatrixSlicer.class \
        MatrixCombiner.class \


objs: $(OBJS)

clean:
	- find . -name "*.class" -exec rm  {} ';'


