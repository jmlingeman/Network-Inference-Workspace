# makefile for ...
#-------------------------------------------------------------------------
# $Revision: 1.1 $
# $Date: 2005/02/02 01:00:18 $
# $Author: pshannon $
#--------------------------------------------------------------------

JC=javac

PKG=org.systemsbiology.gaggle.util.unitTests

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

OBJS =  NameHelperTest.class \
        NewNameHelperTest.class

help:
	egrep "^#" makefile | sed "s/^#//"



objs: $(OBJS)


default: objs run

clean:
	- rm *.class

#------ all
#  run all the tests
#
all: $(OBJS) 0 1

#------ 0
#  build and run the NameHelperTest
#
0: NameHelperTest.class
	java $(PKG).NameHelperTest

#------ 1
#  build and run the NewNameHelperTest, simple filesystem-only test
#
1: NewNameHelperTest.class
	java $(PKG).NewNameHelperTest



JAR=nameHelperTest.jar
DIRme=org/systemsbiology/gaggle/util/unitTests
DIRp=org/systemsbiology/gaggle/util

# --- 2
#  build and run the NewNameHelperTest, simple filesystem-only test
#

2:  NewNameHelperTest.class
	(cd ../../../../..;  jar cf  $(DIRme)/$(JAR) `find $(DIRp)               -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRme)  -maxdepth 1 -name "*.class"`)
	(jar uf  $(JAR) macrophage-names-35.txt)
	java -cp $(JAR):../../lib/signed-15/httpunit/junit.jar \
           org.systemsbiology.gaggle.util.unitTests.NewNameHelperTest



