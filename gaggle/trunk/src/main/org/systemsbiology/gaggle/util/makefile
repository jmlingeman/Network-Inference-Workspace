# makefile for org.systemsbiology.gaggle.util
#-----------------------------------------------------------------------------
#JC=javac -source 1.4
JC=javac
JI=java -Xmx1G

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

#-----------------------------------------------------------------------------
# --- objs
# build all class files
#
objs: MiscUtil.class \
      TextFileReader.class \
      TextHttpReader.class \
      TextJarReader.class \
      NameHelper.class \
      NameHelperFactory.class \
      NewNameHelper.class



# --- clean
# remove all class and jar files
#
clean:
	- find . -name "*.class" -exec rm {} ';'
	- find . -name "*.jar" -exec rm {} ';'

help:
	egrep "^#" makefile | sed "s/^#//"

