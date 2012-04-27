# makefile for org.systemsbiology.gaggle.geese.lister
#-----------------------------------------------------------------------------
JC=javac
JI=java -Xmx1G

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

#-----------------------------------------------------------------------------
# make jar for gaggled java web start
#-----------------------------------------------------------------------------
JAR=glister.jar
DIRme=org/systemsbiology/gaggle/geese/lister
DIRu=org/systemsbiology/gaggle/util
DIRg=org/systemsbiology/gaggle/geese
DIRb=org/systemsbiology/gaggle/boss
DIRn=org/systemsbiology/gaggle/network
DIRdm=org/systemsbiology/gaggle/experiment/datamatrix
DIRmd=org/systemsbiology/gaggle/experiment/metadata
DEPLOY=db:/net/dblocal/wwwspecial/gaggle/2005-06/jars
TEST_DEPLOY=hazel:examples/r/rJava/
#-----------------------------------------------------------------------------
OBJS = Lister.class \
       GaggledLister.class

objs: $(OBJS)

GaggledLister.class: Lister.class GaggledLister.java ../Goose.class

jar: $(JAR)

$(JAR): $(OBJS)
	(cd ../../../../..;  jar cmf $(DIRme)/manifest $(DIRme)/$(JAR))
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRme)             -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRn)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRu)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRg)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRb)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRdm) -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRmd) -maxdepth 1 -name "*.class"`)

sign:
	jarsigner -keystore /users/pshannon/.keystore  -storepass cytokey $(JAR) cytoscape


deploy: 
	scp $(JAR) $(DEPLOY)

testDeploy: 
	scp $(JAR) $(TEST_DEPLOY)

# --- run
# start the glister
#

run: objs
	$(JI) org.systemsbiology.gaggle.geese.lister.GaggledLister &


# --- runjar
# run the clusterCompareGoose from a jar
#

runjar: jar
	$(JI) -jar $(JAR) &


# --- clean
# remove all class and jar files
#
clean:
	- find . -name "*.class" -exec rm {} ';'
	- find . -name "*.jar" -exec rm {} ';'




help:
	egrep "^#" makefile | sed "s/^#//"

