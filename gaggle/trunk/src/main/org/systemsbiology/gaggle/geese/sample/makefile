# makefile for org.systemsbiology.gaggle.geese.sample
#-----------------------------------------------------------------------------
JC=javac
JI=java -Xmx1G

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

#-----------------------------------------------------------------------------
# make DEMO_JAR for the SampleGoose web page demo
# make JAR for a webstart application, in the usual gaggle style
#-----------------------------------------------------------------------------
DEMO_JAR=gaggle.jar
JAR=sampleGoose.jar
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

demo: $(DEMO_JAR)

$(DEMO_JAR): $(OBJS)


$(DEMO_JAR): $(OBJS)
	(jar cf  $(DEMO_JAR) `find .  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(DEMO_JAR) `find $(DIRb)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(DEMO_JAR) `find $(DIRg) -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(DEMO_JAR) `find $(DIRdm) -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(DEMO_JAR) `find $(DIRn)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(DEMO_JAR) `find $(DIRu)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(DEMO_JAR) `find $(DIRmd) -maxdepth 1 -name "*.class"`)
	(jar uf  $(DEMO_JAR) javax/jnlp/BasicService.class)

jar: $(DEMO_JAR)
	cp $(DEMO_JAR) $(JAR)
	(jar uf  $(JAR) *.class)
	(jar muf manifest $(JAR))



# --- deploy
# copy source & full jar to the demo directory
#

DEMO=db:/net/dblocal/wwwspecial/gaggle/projects/sampleGoose/2005-11/

deploy: $(DEMO_JAR)
	scp SampleGoose.java $(DEMO)
	scp gaggle.jar       $(DEMO)


# --- sign
sign:
	jarsigner -keystore $(HOME)/.jarkey -storepass honker $(JAR) gaggle  

# --- test
# start the SampleGoose
#

test: objs jar
	$(JI) -cp .:gaggle.jar  SampleGoose


# --- jwsDeploy
# copy webstart-able jar to the standard deploy directory
#

jwsDeploy: jar sign
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

