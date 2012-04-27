# makefile for org.systemsbiology.gaggle.geese.lister
#-----------------------------------------------------------------------------
#JC=javac -source 1.4
JC=javac
JI=java -Xmx1G

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

#-----------------------------------------------------------------------------
# make jar for gaggled java web start
#-----------------------------------------------------------------------------
JAR=diag.jar
DIRme=org/systemsbiology/gaggle/geese/diag
DIRu=org/systemsbiology/gaggle/util
DIRg=org/systemsbiology/gaggle/geese
DIRb=org/systemsbiology/gaggle/boss
DIRdm=org/systemsbiology/gaggle/experiment/datamatrix
DIRmd=org/systemsbiology/gaggle/experiment/metadata
DIRn=org/systemsbiology/gaggle/network
#-----------------------------------------------------------------------------
OBJS = DiagGoose.class

objs: $(OBJS)

jar: $(JAR)

$(JAR): $(OBJS)
	(cd ../../../../..;  jar cmf $(DIRme)/manifest $(DIRme)/$(JAR))
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRme)              -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRg)  -maxdepth 1 -name "*.class"`)
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
	python ../../util/localizeJnlp.py diag.jnlp-raw > diag.jnlp
	javaws diag.jnlp

# --- deploy
# copy jar to the webstart directory
#
deploy: 
	scp $(JAR) $(DEPLOY)

# --- run
# start the glister
#

run: objs
	$(JI) org.systemsbiology.gaggle.geese.diag.DiagGoose &


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
	- find . -name $(JAR) -exec rm {} ';'




help:
	egrep "^#" makefile | sed "s/^#//"

