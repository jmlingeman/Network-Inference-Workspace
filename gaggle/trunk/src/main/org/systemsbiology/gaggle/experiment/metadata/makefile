# makefile for org.systemsbiology.gaggle/experiment/metadata
#-------------------------------------------------------------------------
# $Revision: 18 $
# $Date: 2005/04/03 02:51:01 $
# $Author: pshannon $
#--------------------------------------------------------------------

JC=javac 

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

OBJS =  Variable.class \
        Condition.class \
        DataSetDescription.class \
        MetaData.class \
        MetaDataNavigator.class \

default: objs

objs: $(OBJS)

help:
	egrep "^#" makefile | sed "s/^#//"


test:
	(cd unitTests; make all)



clean:
	find . -name "*.class" -exec rm  {} ';'	

