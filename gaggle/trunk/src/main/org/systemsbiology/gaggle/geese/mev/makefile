#JC=javac -source 1.4
JC=javac
JI=java -Xmx1G

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

#-----------------------------------------------------------------------------
# make jars for gaggled java web start
#-----------------------------------------------------------------------------
JAR=mev.jar

DIRme=org/systemsbiology/gaggle/geese/mev
DIRu=org/systemsbiology/gaggle/util
DIRg=org/systemsbiology/gaggle/geese
DIRb=org/systemsbiology/gaggle/boss
DIRdm=org/systemsbiology/gaggle/experiment/datamatrix
DIRn=org/systemsbiology/gaggle/network
DIRmd=org/systemsbiology/gaggle/experiment/metadata

#-----------------------------------------------------------------------------
OBJS = GaggledMev.class

objs: $(OBJS)

jar: mev.jar

$(JAR): $(OBJS)
	(cd ../../../../..;  jar cmf $(DIRme)/manifest $(DIRme)/$(JAR))
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRme)             -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRg)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRb)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRu)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRdm) -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRn)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRmd) -maxdepth 1 -name "*.class"`)
	(jar uf  $(JAR) tmev.cfg)

# --- sign
# sign the jar
#
sign:
	jarsigner -keystore /users/mpshannon/.jarkey -storepass honker $(JAR) gaggle  

# --- configJar
# create and sign config.jar, which has the tmev configuration file, tmev.cfg
#
configJar:
	(jar cvf config.jar tmev.cfg)
	jarsigner -keystore /users/mpshannon/.jarkey -storepass honker config.jar gaggle  


# --- deploy
# cp the jar to 
#
deploy: configJar
	scp $(JAR) $(DEPLOY)/tmev/
	scp config.jar $(DEPLOY)/tmev/

# --- run
# start the mev
#

run: objs
	$(JI) org.tigr.microarray.mev.TMEV

# --- jws
# run the GaggledMev as a local 'web start'
#

jws: jar sign configJar
	javaws tmev-dev.jnlp


# --- clean
# remove all class and jar files
#
clean:
	- find . -name "*.class" -exec rm {} ';'
	- find . -name "*.jar" -exec rm {} ';'




help:
	egrep "^#" makefile | sed "s/^#//"

