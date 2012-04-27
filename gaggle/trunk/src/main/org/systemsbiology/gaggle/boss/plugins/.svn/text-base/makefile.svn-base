#JC = javac -source 1.4
JC = javac

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<


OBJS = GaggleBossPlugin.class \
       PluginLoader.class

default: objs subs

subs:
	(cd sbeams; make)
	(cd hypothesis; make)
	(cd tigrAnno; make)
	(cd clipboard; make)
#	(cd jython; make)

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


help:
	egrep "^#" makefile | sed "s/^#//"

