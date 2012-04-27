# makefile for org.systemsbiology.gaggle.geese.ihop
#-----------------------------------------------------------------------------
JC=javac
JI=java -Xmx1G

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

#-----------------------------------------------------------------------------
# make JAR for a webstart application, in the usual gaggle style
#-----------------------------------------------------------------------------
JAR=ihopGoose.jar
DIRme=org/systemsbiology/gaggle/geese/ihop
DIRu=org/systemsbiology/gaggle/util
DIRg=org/systemsbiology/gaggle/geese
DIRb=org/systemsbiology/gaggle/boss
DIRdm=org/systemsbiology/gaggle/experiment/datamatrix
DIRmd=org/systemsbiology/gaggle/experiment/metadata
DIRn=org/systemsbiology/gaggle/network
#-----------------------------------------------------------------------------
OBJS = IHopGoose.class

objs: $(OBJS)


jar: $(JAR)

$(JAR): $(OBJS)
	(cd ../../../../..;  jar cmf $(DIRme)/manifest $(DIRme)/$(JAR))
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRme)             -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRb)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRg)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRdm) -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRn)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRu)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRmd) -maxdepth 1 -name "*.class"`)




# --- sign
sign:
	jarsigner -keystore $(HOME)/.jarkey -storepass honker $(JAR) gaggle  


# --- deploy
# copy webstart-able jar to the standard deploy directory
#

deploy: jar sign
	scp $(JAR) $(DEPLOY)


# --- jws
# start (locally) with java web start
#
jws: jar sign
	python ../../util/localizeJnlp.py test.jnlp-raw > test.jnlp
	javaws test.jnlp


# --- clean
# remove all class and jar files
#
clean:
	- find . -maxdepth 1 -name "*.class" -exec rm {} ';'
	- find . -name $(JAR) -exec rm {} ';'

help:
	egrep "^#" makefile | sed "s/^#//"

