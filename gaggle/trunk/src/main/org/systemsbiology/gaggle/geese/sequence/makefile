# makefile for org.systemsbiology.gaggle.geese.sequence
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
JAR=sequence.jar
DIRme=org/systemsbiology/gaggle/geese/sequence
DIRu=org/systemsbiology/gaggle/util
DIRg=org/systemsbiology/gaggle/geese
DIRb=org/systemsbiology/gaggle/boss
DIRdm=org/systemsbiology/gaggle/experiment/datamatrix
DIRmd=org/systemsbiology/gaggle/experiment/metadata
DIRn=org/systemsbiology/gaggle/network
#-----------------------------------------------------------------------------
OBJS = SequenceFetcher.class \
       FileBasedSequenceFetcher.class \
       SequenceFetcherFactory.class \
       CodonTable.class \
       SequenceGoose.class 

objs: $(OBJS)

jar: $(JAR)

$(JAR): $(OBJS)
	(cd ../../../../..;  jar cmf $(DIRme)/manifest $(DIRme)/$(JAR))
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRme)             -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRg)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRb)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRdm) -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRn)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRu)  -maxdepth 1 -name "*.class"`)
	(cd ../../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRmd) -maxdepth 1 -name "*.class"`)

	jar cvf halo.jar halobacterium_orfs

# --- sign
sign:
	jarsigner -keystore $(HOME)/.jarkey -storepass honker $(JAR) gaggle  
	jarsigner -keystore $(HOME)/.jarkey -storepass honker halo.jar gaggle  


# --- jws
# start (locally) with java web start
#
jws: jar sign
	python ../../util/localizeJnlp.py sequence.jnlp-raw > sequence.jnlp
	javaws sequence.jnlp

# --- deploy
# copy jar to the webstart directory
#
deploy: 
	scp $(JAR) $(DEPLOY)

# --- run
# start the sequence goose
#

run: objs
	(CLASSPATH=$$CLASSPATH:halo.jar; $(JI) org.systemsbiology.gaggle.geese.sequence.SequenceGoose \
          -s  Halobacterium sp. -d  jar://halobacterium_orfs)



# --- clean
# remove all class and jar files
#
clean:
	- find . -name "*.class" -exec rm {} ';'
	- find . -name $(JAR) -exec rm {} ';'




help:
	egrep "^#" makefile | sed "s/^#//"

