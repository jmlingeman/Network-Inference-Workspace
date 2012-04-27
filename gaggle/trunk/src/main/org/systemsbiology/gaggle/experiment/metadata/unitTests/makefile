# makefile for org/systemsbiology/gaggle/experiment/metadata/unitTests
#-------------------------------------------------------------------------
# $Revision: 18 $
# $Date: 2005/04/03 02:51:03 $
# $Author: pshannon $
#--------------------------------------------------------------------

JC=javac 

PKG=org.systemsbiology.gaggle.experiment.metadata.unitTests

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

OBJS =  VariableTest.class \
        ConditionTest.class \
        DataSetDescriptionTest.class \
        MetaDataTest.class \
        MetaDataNavigatorTest.class


help:
	egrep "^#" makefile | sed "s/^#//"



objs: $(OBJS)


default: objs run

clean:
	- rm *.class

#------ all
#  build and run all tests
#
all: $(OBJS) 0 1 2 3 4

#------ 0
#  build and run the VariableTest
#
0: VariableTest.class
	java $(PKG).VariableTest


#------ 1
#  build and run the ConditionTest
#
1: ConditionTest.class
	java $(PKG).ConditionTest

#------ 2
#  build and run the DataSetDescriptionTest
#
2: DataSetDescriptionTest.class
	java $(PKG).DataSetDescriptionTest


#------ 3
#  build and run the MetaDataTest
#
3: MetaDataTest.class
	java $(PKG).MetaDataTest


#------ 4
#  build and run the MetaDataNavigatorTest
#
4: MetaDataNavigatorTest.class
	java $(PKG).MetaDataNavigatorTest

