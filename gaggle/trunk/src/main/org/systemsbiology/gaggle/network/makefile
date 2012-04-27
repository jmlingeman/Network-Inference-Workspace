# makefile for org.systemsbiology.gaggle.network
#-------------------------------------------------------------------------
# $Revision: 19 $
# $Date: 2005-07-29 20:25:11 -0400 (Fri, 29 Jul 2005) $
# $Author: pshannon $
#-------------------------------------------------------------------------
JC=javac 
JI=java

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

default: objs 

OBJS =  Interaction.class \
        Network.class


objs: $(OBJS)

clean:
	- find . -name "*.class" -exec rm  {} ';'


