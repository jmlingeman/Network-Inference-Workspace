JC=javac
JI=java -Xmx1G

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

#-----------------------------------------------------------------------------
# make jars for gaggled java web start
#-----------------------------------------------------------------------------
DIRme=org/systemsbiology/gaggle/geese/cy/generic
DIRu=org/systemsbiology/gaggle/util
DIRcu=org/systemsbiology/gaggle/geese/cy/util
DIRg=org/systemsbiology/gaggle/geese
DIRb=org/systemsbiology/gaggle/boss
DIRdm=org/systemsbiology/gaggle/experiment/datamatrix
DIRmd=org/systemsbiology/gaggle/experiment/metadata
DIRn=org/systemsbiology/gaggle/network

JAR=cyGagglePlugin.jar
#-----------------------------------------------------------------------------
OBJS = CyGagglePlugin.class

objs: $(OBJS)


CyGagglePlugin_Stub.class: CyGagglePlugin.class
	rmic -d ../../../../../.. org.systemsbiology.gaggle.geese.cy.generic.CyGagglePlugin

# --- jar
# build a fresh jar
#

jar: $(JAR)

$(JAR): $(OBJS)
	(cd ../../../../../..;  jar cf  $(DIRme)/$(JAR) `find $(DIRme)             -name "*.class"`)
	(cd ../../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRg)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRb)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRu)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRcu) -maxdepth 1 -name "*.class"`)
	(cd ../../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRdm) -maxdepth 1 -name "*.class"`)
	(cd ../../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRn)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRmd) -maxdepth 1 -name "*.class"`)

# --- sign
# sign cyGagglePlugin.jar with the a java 1.5 key
#
sign:
	jarsigner -keystore $(HOME)/.jarkey -storepass honker $(JAR) gaggle


# --- deploy
# copy boss.jar to the 
#
deploy:
	scp $(JAR) $(DEPLOY)


# --- jws0
# run a blank slate cytoscape
#
jws0: jar sign
	(cd sampleProject; make jws0)



# --- jws1
# run a cytoscape with just 3 nodes
#
jws: jws1
jws1: jar sign
	(cd sampleProject; make jws1)


# --- jws2
# run a cytoscape with about 50 nodes
#
jws2: jar sign
	(cd sampleProject; make jws2)

# --- clean
# remove all class and jar files
#
clean:
	- find . -name "*.class" -exec rm {} ';'
	- rm $(JAR)


help:
	egrep "^#" makefile | sed "s/^#//"

