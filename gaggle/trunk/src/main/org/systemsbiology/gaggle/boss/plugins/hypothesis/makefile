# makefile for org/systemsbiology/gaggle/boss/plugins/hypothesis
#-----------------------------------------------------------------
#JC = javac -source 1.4
JC = javac

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<


OBJS = HypothesisRecorder.class


# --- objs
# compile all java code
#
objs: $(OBJS)

# --- clean
# remove all .class files
#
clean: 
	- find . -name "*.class" -exec rm {} ';'
	- find . -name "*.jar" -exec rm {} ';'

# --- demo
# run tester.py
#
demo: 
	jython tester.py


help:
	egrep "^#" makefile | sed "s/^#//"

