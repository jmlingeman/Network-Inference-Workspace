JC=javac
JI=java -Xmx1G

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

#-----------------------------------------------------------------------------
# make jars for gaggled java web start
#-----------------------------------------------------------------------------
DIRme=org/systemsbiology/gaggle/geese/cy/mastermap
DIRu=org/systemsbiology/gaggle/util
DIRcu=org/systemsbiology/gaggle/geese/cy/util
DIRg=org/systemsbiology/gaggle/geese
DIRb=org/systemsbiology/gaggle/boss
DIRdm=org/systemsbiology/gaggle/experiment/datamatrix
DIRmd=org/systemsbiology/gaggle/experiment/metadata
DIRn=org/systemsbiology/gaggle/network

JAR=cyMasterMap.jar
#-----------------------------------------------------------------------------
OBJS = MasterMapPlugin.class

objs: $(OBJS)

# --- jar
# build a fresh jar
#

jar: $(JAR) sign

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


# --- jws
# run a mouse example
#
jws: jar 
	(cd sampleProject; make jws)

# --- jwsClean
# start with no network
#
jwsClean: jar 
	(cd sampleProject; make jwsClean)

# --- clean
# remove all class and jar files
#
clean:
	- find . -name "*.class" -exec rm {} ';'
	- rm $(JAR)


help:
	egrep "^#" makefile | sed "s/^#//"

