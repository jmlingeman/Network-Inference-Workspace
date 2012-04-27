# makefile for csplugins/isb/pshannon/tileViewer
#-------------------------------------------------------------------------
# $Revision: 437 $
# $Date: 2005/04/03 02:57:55 $
# $Author: pshannon $
#-------------------------------------------------------------------------
JC=javac
JI=java

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

#-----------------------------------------------------------------------------
DIRroot=../../../../../..
DIRme=org/systemsbiology/gaggle/geese/cy/tileViewer
DIRu=org/systemsbiology/gaggle/util
DIRcu=org/systemsbiology/gaggle/geese/cy/util
DIRg=org/systemsbiology/gaggle/geese
DIRb=org/systemsbiology/gaggle/boss
DIRdm=org/systemsbiology/gaggle/experiment/datamatrix
DIRmd=org/systemsbiology/gaggle/experiment/metadata
DIRn=org/systemsbiology/gaggle/network

JAR=tileViewer.jar
#-----------------------------------------------------------------------------
default: objs 

OBJS= GeneFinder.class \
      TileViewerPlugin.class \
      ControlPanel.class \

help:
	egrep "^#" makefile | sed "s/^#//"
      
#--------------------------------------------------------------------------------

jar: $(JAR)

$(JAR): $(OBJS)
	(cd $(DIRroot);  jar cf  $(DIRme)/$(JAR) `find $(DIRme)             -name "*.class"`)
	(cd $(DIRroot);  jar uf  $(DIRme)/$(JAR) `find $(DIRg)  -maxdepth 1 -name "*.class"`)
	(cd $(DIRroot);  jar uf  $(DIRme)/$(JAR) `find $(DIRb)  -maxdepth 1 -name "*.class"`)
	(cd $(DIRroot);  jar uf  $(DIRme)/$(JAR) `find $(DIRu)  -maxdepth 1 -name "*.class"`)
	(cd $(DIRroot);  jar uf  $(DIRme)/$(JAR) `find $(DIRcu) -maxdepth 1 -name "*.class"`)
	(cd $(DIRroot);  jar uf  $(DIRme)/$(JAR) `find $(DIRdm) -maxdepth 1 -name "*.class"`)
	(cd $(DIRroot);  jar uf  $(DIRme)/$(JAR) `find $(DIRn)  -maxdepth 1 -name "*.class"`)
	(cd $(DIRroot);  jar uf  $(DIRme)/$(JAR) `find $(DIRmd) -maxdepth 1 -name "*.class"`)

# --- sign
# sign  with the old key
#
sign:
	jarsigner -keystore $(HOME)/.jarkey -storepass honker $(JAR) gaggle

# --- deploy
# copy tileViewer.jar to the deploy directory
#
deploy:
	scp $(JAR) $(DEPLOY)

# --- jws
# run a simple test
#
jws: jar sign
	(cd tinyProject; make)



objs: $(OBJS)

clean:
	- find . -name "*.class" -exec rm  {} ';'
	- find . -name $(JAR) -exec rm  {} ';'

demo:  objs
	(cd tinyProject; make)

