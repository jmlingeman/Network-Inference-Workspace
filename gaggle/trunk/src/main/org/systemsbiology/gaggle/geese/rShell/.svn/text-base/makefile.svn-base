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
JAR=gaggleRShell.jar
DIRme=org/systemsbiology/gaggle/geese/rShell
DIRu=org/systemsbiology/gaggle/util
DIRg=org/systemsbiology/gaggle/geese
DIRb=org/systemsbiology/gaggle/boss
DIRn=org/systemsbiology/gaggle/network
DIRdm=org/systemsbiology/gaggle/experiment/datamatrix
DIRmd=org/systemsbiology/gaggle/experiment/metadata
DIRco=org/systemsbiology/gaggle/core
#-----------------------------------------------------------------------------
OBJS = StringToObjectConverter.class \
       RShellGoose.class

objs: $(OBJS)

RShellGoose_Stub.class: RShellGoose.class
	rmic -d ../../../../.. org.systemsbiology.gaggle.geese.rShell.RShellGoose

jar: $(JAR)

$(JAR): $(OBJS)
	(cd ../../../../..;  jar cf  $(DIRme)/$(JAR) `find $(DIRme)             -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRn)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRg)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRu)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRb)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRdm) -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRmd) -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRco)/$(JAR) `find $(DIRco) -maxdepth 1 -name "*.class"`)


# --- deploy
# copy the latest jar to the gagglePackage construction site
#

deploy: jar
	cp $(JAR) ../scripts/r/gagglePackage/gaggle/inst/jars/



# --- clean
# remove all class and jar files
#
clean:
	- find . -name "*.class" -exec rm {} ';'
	- find . -name "*.jar" -exec rm {} ';'




help:
	egrep "^#" makefile | sed "s/^#//"

