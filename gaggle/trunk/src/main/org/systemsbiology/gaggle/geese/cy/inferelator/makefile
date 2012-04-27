# makefile for csplugins/isb/pshannon/inferelator
#-------------------------------------------------------------------------
# $Revision: 1.4 $
# $Date: 2005/04/03 03:02:17 $
# $Author: pshannon $
#-------------------------------------------------------------------------
JC=javac
JI=java

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

default: objs 

OBJS=   InferelatorControlPanelPlugin.class \
	ControlPanel.class

help:
	egrep "^#" makefile | sed "s/^#//"
      
      
#--------------------------------------------------------------------------------
DIRroot=../../../../../..
DIRme=org/systemsbiology/gaggle/geese/cy/inferelator
DIRu=org/systemsbiology/gaggle/util
DIRg=org/systemsbiology/gaggle/geese
DIRcu=org/systemsbiology/gaggle/geese/cy/util
DIRb=org/systemsbiology/gaggle/boss
DIRdm=org/systemsbiology/gaggle/experiment/datamatrix
DIRmd=org/systemsbiology/gaggle/experiment/metadata
DIRn=org/systemsbiology/gaggle/network
JAR=inferelator.jar
#-----------------------------------------------------------------------------

jar: $(JAR)

$(JAR): $(OBJS)
	(cd $(DIRroot);  jar cf  $(DIRme)/$(JAR) `find $(DIRme)             -name "*.class"`)
	(cd $(DIRroot);  jar uf  $(DIRme)/$(JAR) `find $(DIRg)  -maxdepth 1 -name "*.class"`)
	(cd $(DIRroot);  jar uf  $(DIRme)/$(JAR) `find $(DIRb)  -maxdepth 1 -name "*.class"`)
	(cd $(DIRroot);  jar uf  $(DIRme)/$(JAR) `find $(DIRcu) -maxdepth 1 -name "*.class"`)
	(cd $(DIRroot);  jar uf  $(DIRme)/$(JAR) `find $(DIRu)  -maxdepth 1 -name "*.class"`)
	(cd $(DIRroot);  jar uf  $(DIRme)/$(JAR) `find $(DIRdm) -maxdepth 1 -name "*.class"`)
	(cd $(DIRroot);  jar uf  $(DIRme)/$(JAR) `find $(DIRn)  -maxdepth 1 -name "*.class"`)
	(cd $(DIRroot);  jar uf  $(DIRme)/$(JAR) `find $(DIRmd) -maxdepth 1 -name "*.class"`)



# --- sign
# sign inferelator.jar with the java 1.5 key
#
sign:
	jarsigner -keystore $(HOME)/.jarkey -storepass honker $(JAR) gaggle

# --- deploy
# copy boss.jar to the 
#
deploy:
	scp $(JAR) $(DEPLOY)


objs: $(OBJS)

clean:
	- find . -name "*.class" -exec rm  {} ';'
	- find . -name $(JAR) -exec rm  {} ';'

jws:  jar sign
	(cd tinyProject; make)

