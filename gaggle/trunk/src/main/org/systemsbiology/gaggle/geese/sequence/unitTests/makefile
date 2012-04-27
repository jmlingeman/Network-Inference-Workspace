# makefile for csplugins.isb.pshannon.inferelator.unitTests
#-------------------------------------------------------------------------
# $Revision: 18 $
# $Date: 2005/01/28 20:59:06 $
# $Author: pshannon $
#--------------------------------------------------------------------
JC=javac
JR=java 

PKG=org.systemsbiology.gaggle.geese.sequence.unitTests

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

OBJS =  ConfigTest.class \
        FileBasedSequenceFetcherTest.class \
        JarFileBasedSequenceFetcherTest.class \

help:
	egrep "^#" makefile | sed "s/^#//"

default: objs run

objs: $(OBJS)


clean:
	- rm *.class

run: $(OBJS) 0 

# --- 0
#  run ConfigTest
#

0: ConfigTest.class
	$(JR) $(PKG).ConfigTest



JAR=fetcherTest.jar
DIRme=org/systemsbiology/gaggle/geese/sequence/unitTests
DIRp=org/systemsbiology/gaggle/geese/sequence
DIRu=org/systemsbiology/gaggle/util

# --- 1
#  run FileBasedSequenceFetchterTest 
#

1:  FileBasedSequenceFetcherTest.class
	(cd ../../../../../..;  jar cf  $(DIRme)/$(JAR) `find $(DIRp)               -name "*.class"`)
	(cd ../../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRu)   -maxdepth 1 -name "*.class"`)
	(cd ../../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRme)  -maxdepth 1 -name "*.class"`)
	(jar uf  $(JAR) haloTiny.fasta)
	java -cp $(JAR):../../../lib/signed-15/httpunit/junit.jar \
           org.systemsbiology.gaggle.geese.sequence.unitTests.FileBasedSequenceFetcherTest



# --- 2
#  run CodonTableTest
#

2:  CodonTableTest.class
	java $(PKG).CodonTableTest


