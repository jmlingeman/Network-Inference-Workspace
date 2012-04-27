# makefile for org/systemsbiology/gaggle/geese/pdfViewer
#-----------------------------------------------------------------------------
JC=javac
JI=java -Xmx1G

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

#-----------------------------------------------------------------------------
# make jars for gaggled java web start
#-----------------------------------------------------------------------------
JAR=pdfViewer.jar

DIRme=org/systemsbiology/gaggle/geese/pdfViewer
DIRu=org/systemsbiology/gaggle/util
DIRg=org/systemsbiology/gaggle/geese
DIRb=org/systemsbiology/gaggle/boss
DIRrd=org/systemsbiology/gaggle/experiment/readers
DIRre=org/systemsbiology/gaggle/experiment/repository
DIRn=org/systemsbiology/gaggle/network
DIRdm=org/systemsbiology/gaggle/experiment/datamatrix
DIRgu=org/systemsbiology/gaggle/experiment/gui
DIRmd=org/systemsbiology/gaggle/experiment/metadata
DIRd0=csplugins/isb/dtenenbaum/plot2d
DIRd1=csplugins/isb/dtenenbaum/nameHelper
DIRd2=csplugins/isb/dtenenbaum/speciesGuesser
DIRvm=csplugins/vectormath
#-----------------------------------------------------------------------------
default: objs

OBJS = ClusterViewer.class

objs: $(OBJS)

ClusterViewer_Skel.class: ClusterViewer.class 
	rmic -d ../../../../.. org.systemsbiology.gaggle.geese.pdfViewer.ClusterViewer

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
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRn)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRre) -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRdm) -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRgu)             -name "*.jpg"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRmd) -maxdepth 1 -name "*.class"`)


# --- sign
# sign $(JAR) with the java 1.5 key
#
sign:
	jarsigner -keystore $(HOME)/.jarkey -storepass honker $(JAR) gaggle


# --- jws
# start (locally) with java web start
#
jws: jar sign
	python ../../util/localizeJnlp.py pdf.jnlp-raw > pdf.jnlp
	javaws pdf.jnlp



# --- deploy
# copy boss.jar to the 
#
deploy:
	scp $(JAR) $(DEPLOY)


# --- run
# start the pdfViewer  in the current environemnt
#
run: objs
	$(JI) org.systemsbiology.gaggle.geese.pdfViewer.ClusterViewer http://gaggle.systemsbiology.net/hpy/cy/bicluster/pdf

# --- clean
# remove all class and jar files
#
clean:
	- find . -name "*.class" -exec rm {} ';'
	- find . -name $(JAR) -exec rm {} ';'

help:
	egrep "^#" makefile | sed "s/^#//"


