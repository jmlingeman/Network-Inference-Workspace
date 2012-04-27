# makefile for org/systemsbiology/gaggle/geese/dmv
#-----------------------------------------------------------------------------
JC=javac
JI=java -Xmx1G

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

#-----------------------------------------------------------------------------
# make jars for gaggled java web start
#-----------------------------------------------------------------------------
JAR=dmv.jar

DIRme=org/systemsbiology/gaggle/geese/dmv
DIRu=org/systemsbiology/gaggle/util
DIRg=org/systemsbiology/gaggle/geese
DIRb=org/systemsbiology/gaggle/boss
DIRrd=org/systemsbiology/gaggle/experiment/readers
DIRre=org/systemsbiology/gaggle/experiment/repository
DIRn=org/systemsbiology/gaggle/network
DIRdm=org/systemsbiology/gaggle/experiment/datamatrix
DIRgu=org/systemsbiology/gaggle/experiment/gui
DIRmd=org/systemsbiology/gaggle/experiment/metadata
DIRpl=org/systemsbiology/gaggle/experiment/gui/plotters
#-----------------------------------------------------------------------------
default: objs

OBJS = GaggledTreeDataViewer.class \
       GaggledMovieController.class \

objs: $(OBJS)

GaggledTreeDataViewer_Stub.class: GaggledTreeDataViewer.class 
	rmic -d ../../../../.. org.systemsbiology.gaggle.geese.dmv.GaggledTreeDataViewer

GaggledMovieController_Stub.class: GaggledMovieController.class
	rmic -d ../../../../.. org.systemsbiology.gaggle.geese.dmv.GaggledMovieController

# --- jar
# create an executable, signed jar
#

jar: $(JAR)

$(JAR): $(OBJS)
	(cd ../../../../..;  jar cmf $(DIRme)/manifest $(DIRme)/$(JAR))
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRme)             -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRg)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRb)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRu)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRrd) -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRn)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRre) -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRdm) -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRgu) -maxdepth 2 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRgu)             -name "*.jpg"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRmd) -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRpl) -maxdepth 2 -name "*.class"`)



# --- sign
sign:
	jarsigner -keystore $(HOME)/.jarkey -storepass honker $(JAR) gaggle  

# --- jws
# start (locally) with java web start
#
jws: jar sign
	python ../../util/localizeJnlp.py dmv.jnlp-raw > dmv.jnlp
	javaws dmv.jnlp

# --- deploy
# copy jar to the webstart directory
#
deploy:
	scp dmv.jar $(DEPLOY)

testDeploy: 
	scp $(JAR) $(TEST_DEPLOY)


# --- deploy15
# copy dmv.jar to the java 1.5 jar directory
#
deploy15:
	scp dmv.jar $(DEPLOY15)
	



# --- run
# start the dmv in the current environemtn
#
run: objs
	$(JI) org.systemsbiology.gaggle.geese.dmv.GaggledTreeDataViewer httpIndirect://db.systemsbiology.net:8080/halo/DataFetcher.py &

# --- clean
# remove all class and jar files
#
clean:
	- find . -name "*.class" -exec rm {} ';'
	- find . -name $(JAR) -exec rm {} ';'

help:
	egrep "^#" makefile | sed "s/^#//"


