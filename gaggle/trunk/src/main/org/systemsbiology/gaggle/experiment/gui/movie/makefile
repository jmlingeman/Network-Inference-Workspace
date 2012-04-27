# makefile for csplugins/isb/pshannon/experiment/gui/movie
#-------------------------------------------------------------------------
# $Revision: 18 $
# $Date: 2005/04/03 02:50:57 $
# $Author: pshannon $
#-------------------------------------------------------------------------
JC=javac 
JI=java

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

default: objs

OBJS =  MovieControllerClient.class \
        MovieControllerInfoDialog.class \
        MovieController.class \
      


objs: $(OBJS)

clean:
	- find . -name "*.class" -exec rm  {} ';'

