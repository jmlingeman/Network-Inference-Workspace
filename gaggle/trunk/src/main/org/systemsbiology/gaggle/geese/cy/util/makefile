# makefile for org.systemsbiology.gaggle.geese.cy.util
#-----------------------------------------------------------------------------
JC=javac 
JI=java -Xmx1G

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

#-----------------------------------------------------------------------------
OBJS = NodeNameMatcher.class \
       NetworkUtil.class


# --- objs
# build all class files
#
objs: $(OBJS)


# --- clean
# remove all class and jar files
#
clean:
	- find . -name "*.class" -exec rm {} ';'
	- find . -name "*.jar" -exec rm {} ';'




help:
	egrep "^#" makefile | sed "s/^#//"

