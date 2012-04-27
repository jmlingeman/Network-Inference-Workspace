# makefile for csplugins.isb.experiment.readers
#-------------------------------------------------------------------------
# $Revision: 18 $
# $Date: 2004/10/25 23:23:05 $
# $Author: pshannon $
#-------------------------------------------------------------------------
D=/home/pshannon/work/csplugins/trial/pshannon/dataCube
CP=$D/cytoscape.jar:$D/visad.jar:/home/pshannon/work
JC=javac -classpath $(CP)
JC=javac
JI=java

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

default: subdirectory objs 
#default: objs 

OBJS =  DataMatrixReader.class \
        DataMatrixFileReader.class \
        DataMatrixReaderFactory.class \
        MetaDataXmlParser.class \
        TextHttpIndirectFileReader.class


subdirectory:
#	(cd gui; make)
#	(cd gui/actions; make)
#	(cd ops; make)
#	(cd ops/gui; make)

objs: $(OBJS)

clean:
	- find . -name "*.class" -exec rm  {} ';'



#
#------ run
# run cytoscape with DataMatrix plugin,
# 

run: $(OBJS)
	$(JI) cytoscape.CyMain -p project



PLUGINS=/users/pshannon/cy2
IMG=/users/pshannon/cy2/csplugins/isb/pshannon/dataMatrix/gui
EXTRA=/users/pshannon/jars/dataMatrixExtra
JAR=/users/pshannon/cy2/csplugins/isb/pshannon/dataMatrix/DataMatrixPlugin.jar
#------ jar
# make a plugin jar
#

jar:
	(cd $(PLUGINS); jar cvf $(JAR) `find csplugins/isb/pshannon/dataMatrix -name "*.class" -print`)
	(cd $(PLUGINS); jar uvf $(JAR) `find csplugins/isb/pshannon/py -name "*.class" -print`)
	(cd $(PLUGINS);     jar uvf $(JAR) `find . -name "*.jpg" -print`)
	(cd $(PLUGINS); jar uvf $(JAR) `find csplugins/isb/dante/plot2d -name "*.class" -print`)
	(cd $(PLUGINS); jar uvf $(JAR) `find csplugins/common/vectormath -name "*.class" -print`)
	(cd $(EXTRA);   jar uvf $(JAR) `find . -name "*.class" -print`)
	(cd $(EXTRA);   jar uvf $(JAR) `find . -name "*.properties" -print`)

#(cd $(PLUGINS); jar uvf $(JAR) `find csplugins/isb/pshannon/py -name "*.class" -print`)

#------ cpjar
# cp my plugin class files to /users/pshannon/jars/dataMatrixExtra
#

cpjar:
	(cd $(EXTRA); jar cvf $(JAR) `find csplugins/isb/pshannon/dataMatrix -name "*.class" -print`)
	(cd $(PLUGINS); jar cvf $(JAR) `find csplugins/isb/dante/plot2d -name "*.class" -print`)
	(cd $(EXTRA);   jar uvf $(JAR) `find org/jfree -name "*.class" -print`)




#------ sign
# sign the jar
#

sign:
	echo 'signing jar...'
	jarsigner -keystore /users/pshannon/.keystore  -storepass cytokey cytoscape.jar cytoscape

