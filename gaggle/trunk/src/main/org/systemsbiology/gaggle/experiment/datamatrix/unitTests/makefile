# makefile for org/systemsbiology/gaggle/experiment/datamatrix/unitTests
#-------------------------------------------------------------------------
# $Revision: 17 $
# $Date: 2005-07-29 20:19:31 -0400 (Fri, 29 Jul 2005) $
# $Author: pshannon $
#--------------------------------------------------------------------

JC=javac

PKG=org.systemsbiology.gaggle.experiment.datamatrix.unitTests

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

OBJS =  DataMatrixTest.class \
        LensedDataMatrixTest.class \
        MatrixSlicerTest.class \
        MatrixCombinerTest.class 
#        SubMatrixClusterTest.class

help:
	egrep "^#" makefile | sed "s/^#//"



objs: $(OBJS)


default: objs run

clean:
	- rm *.class

#------ all
#  run all the tests
#
all: $(OBJS) 0 1 2

#------ 0
#  build and run the DataMatrixTest
#
0: DataMatrixTest.class
	java $(PKG).DataMatrixTest


#------ 1
#  build and run the LensedDataMatrixTest
#
1: LensedDataMatrixTest.class
	java -Xmx1G $(PKG).LensedDataMatrixTest


#------ 2
#  build and run the MatrixSlicerTest
#
2: MatrixSlicerTest.class
	java $(PKG).MatrixSlicerTest


#------ 3
#  build and run the MatrixCombinerTest
#
3: MatrixCombinerTest.class
	java $(PKG).MatrixCombinerTest




