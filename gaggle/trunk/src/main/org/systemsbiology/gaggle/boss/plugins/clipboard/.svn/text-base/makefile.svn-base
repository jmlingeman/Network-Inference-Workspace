#JC = javac -source 1.4
JC = javac

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<


OBJS = Clipboard.class

# --- objs
# compile all java code
#
objs: $(OBJS)


help:
	egrep "^#" makefile | sed "s/^#//"


#-----------------------------------------------------------------------------
# make jars for gaggled java web start
#-----------------------------------------------------------------------------
JAR=clipboardPlugin.jar

DIRme=org/systemsbiology/gaggle/boss/plugins/clipboard
DIRu=org/systemsbiology/gaggle/util
DIRb=org/systemsbiology/gaggle/boss
DIRg=org/systemsbiology/gaggle/geese
DIRn=org/systemsbiology/gaggle/network
DIRe=org/systemsbiology/gaggle/experiment
DIRdm=org/systemsbiology/gaggle/experiment/datamatrix
DIRmd=org/systemsbiology/gaggle/experiment/metadata
#-----------------------------------------------------------------------------
jar: $(JAR)

$(JAR): $(OBJS)
	(cd ../../../../../..;  jar cf  $(DIRme)/$(JAR) `find $(DIRme)             -name "*.class"`)

sign:
	jarsigner -keystore /users/mpshannon/.jarkey -storepass honker $(JAR) gaggle  

deploy: 
	scp $(JAR) $(DEPLOY)


# --- clean
# remove all .class files
#
clean: 
	- find . -name "*.class" -exec rm {} ';'
	- find . -name $(JAR) -exec rm {} ';'

