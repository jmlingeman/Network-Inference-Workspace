# makefile for org.systemsbiology.gaggle.geese
#-----------------------------------------------------------------------------
JC=javac
JI=java -Xmx1G

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<


# --- clean
# remove all class and jar files
#

default: objs  subdirs


# --- clean
# remove all class and jar files
#

objs: Goose.class \
      DeafGoose.class


# --- subdirs
# build all the subdirectories
#

subdirs: objs
#	(cd clusterImage; make)
	(cd cy; make)
	(cd jaguar; make)
	(cd lister; make)
	(cd mev; make)
#	(cd silly; make)
	(cd dmv; make)
	(cd wbi/multiplex; make)

	


# --- clean
# remove all class and jar files
#
clean:
	- find . -name "*.class" -exec rm {} ';'
	- find . -name "*.jar" -exec rm {} ';'




help:
	egrep "^#" makefile | sed "s/^#//"

