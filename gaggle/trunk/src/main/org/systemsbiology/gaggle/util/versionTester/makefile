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
JAR=versionTester.jar
DIRme=org/systemsbiology/gaggle/util/versionTester
DIRu=org/systemsbiology/gaggle/util
DIRg=org/systemsbiology/gaggle/geese
DIRb=org/systemsbiology/gaggle/boss
DIRdm=org/systemsbiology/gaggle/experiment/datamatrix
DIRmd=org/systemsbiology/gaggle/experiment/metadata
DIRn=org/systemsbiology/gaggle/network
#-----------------------------------------------------------------------------
OBJS = JavaVersionTester.class

objs: $(OBJS)

jar: $(JAR)

$(JAR): $(OBJS)
	(cd ../../../../..;  jar cmf $(DIRme)/manifest $(DIRme)/$(JAR))
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRme)             -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRu)             -name "*.class"`)

# --- sign
sign:
	jarsigner -keystore $(HOME)/.jarkey -storepass honker $(JAR) gaggle  


# --- jws
# start (locally) with java web start
#
jws: jar sign
	python ../../util/localizeJnlp.py tester.jnlp-raw > tester.jnlp
	javaws tester.jnlp

# --- deploy
# copy jar to the webstart directory
#
deploy: 
	scp $(JAR) $(DEPLOY)

# --- run
# start the glister
#

run: objs
	$(JI) org.systemsbiology.gaggle.util.versionTester.JavaVersionTester &

# --- clean
# remove all class and jar files
#
clean:
	- find . -name "*.class" -exec rm {} ';'
	- find . -name "*.jar" -exec rm {} ';'


help:
	egrep "^#" makefile | sed "s/^#//"

