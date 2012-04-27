# makefile for org.systemsbiology.gaggle.geese.sample
#-----------------------------------------------------------------------------
JC=javac
JI=java -Xmx1G

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

#-----------------------------------------------------------------------------
# make jar for gaggled java web start
#-----------------------------------------------------------------------------
JAR=gaggle.jar
DIRme=org/systemsbiology/gaggle/geese/sample
DIRu=org/systemsbiology/gaggle/util
DIRg=org/systemsbiology/gaggle/geese
DIRb=org/systemsbiology/gaggle/boss
DIRdm=org/systemsbiology/gaggle/experiment/datamatrix
DIRmd=org/systemsbiology/gaggle/experiment/metadata
DIRn=org/systemsbiology/gaggle/network
#-----------------------------------------------------------------------------
OBJS = SampleGoose.class

objs: $(OBJS)

jar: $(JAR)

$(JAR): $(OBJS)
	(cd ../../../../..;  jar cf  $(DIRme)/$(JAR) `find $(DIRg)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRb)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRdm) -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRn)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRu)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRmd) -maxdepth 1 -name "*.class"`)




# --- sign
sign:
	jarsigner -keystore $(HOME)/.jarkey -storepass honker $(JAR) gaggle  

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
	- find . -name "*.class" -exec rm {} ';'
	- find . -name $(JAR) -exec rm {} ';'

help:
	egrep "^#" makefile | sed "s/^#//"

