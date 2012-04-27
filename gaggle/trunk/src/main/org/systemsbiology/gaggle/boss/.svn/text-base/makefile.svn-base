# makefile for org.systemsbiology.gaggle.boss
#-----------------------------------------------------------------------------
#JC=javac -source 1.4
JC=javac
JI=java -Xmx1G

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

#-----------------------------------------------------------------------------
# make jars for gaggled java web start
#-----------------------------------------------------------------------------
DIRme=org/systemsbiology/gaggle/boss
DIRu=org/systemsbiology/gaggle/util
DIRb=org/systemsbiology/gaggle/boss
DIRg=org/systemsbiology/gaggle/geese
DIRn=org/systemsbiology/gaggle/network
DIRe=org/systemsbiology/gaggle/experiment
DIRdm=org/systemsbiology/gaggle/experiment/datamatrix
DIRmd=org/systemsbiology/gaggle/experiment/metadata
JAR=boss.jar
TEST_DEPLOY=hazel:examples/r/rJava/
#-----------------------------------------------------------------------------
OBJS = BossConfig.class \
       GaggleBossTableModel.class \
       NameUniquifier.class \
       TileCalculator.class \
       Boss.class \
       GuiBoss.class \
       GuiBoss_Stub.class

objs: $(OBJS)

GuiBoss_Stub.class: GuiBoss.class
	rmic -d ../../../.. org.systemsbiology.gaggle.boss.GuiBoss

jar: $(JAR)

$(JAR): $(OBJS)
	(cd ../../../..;  jar cmf $(DIRme)/manifest $(DIRme)/$(JAR))
	(cd ../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRme)             -name "*.class"`)
	(cd ../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRn) -maxdepth 1 -name "*.class"`)
	(cd ../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRdm) -maxdepth 1 -name "*.class"`)
	(cd ../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRmd) -maxdepth 1 -name "*.class"`)
	(cd ../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRg)  -maxdepth 1 -name "*.class"`)
	(cd ../../../..;  jar uf  $(DIRme)/$(JAR) `find $(DIRu)  -maxdepth 1  -name "*.class"`)

sign:
	jarsigner -keystore $(HOME)/.jarkey -storepass honker $(JAR) gaggle  

# --- testjar: make a fresh boss.jar, and copy it to server as testboss.jar
#
testjar: jar sign
	scp $(JAR) $(DEPLOY)/testboss.jar



# --- subdirs
# build code in all subdirectories
#

subdirs: 
	(cd plugins; make)




deploy: 
	scp $(JAR) $(DEPLOY)


testDeploy: 
	scp $(JAR) $(TEST_DEPLOY)

# --- runHalo
# start the boss with halo plugins
#

runHalo: objs
	$(JI) org.systemsbiology.gaggle.boss.GuiBoss -p haloBoss.props &


# --- runNames
# start the boss with a sample names file
#

runNames: objs
	$(JI) org.systemsbiology.gaggle.boss.GuiBoss -n http://gaggle.systemsbiology.net/tests/macrophage-names-35.txt



# --- runHpy
# start the boss with hpy plugins
#

runHpy: objs
	$(JI) org.systemsbiology.gaggle.boss.GuiBoss -p hpyBoss.props


# --- hpy
# add a H. pylori annotation search tab to the boss
#
hpy: $(JAR)
	jar cvf bossConfig.jar hpyBoss.props
	jarsigner -keystore  $(HOME)/.jarkey -storepass honker bossConfig.jar  gaggle  



# --- jdrf
# add jdrf boss plugins
#
jdrf: $(JAR)
	jar cvf bossConfig.jar jdrfBoss.props
	jarsigner -keystore $(HOME)/.jarkey -storepass honker bossConfig.jar  gaggle  




# --- jws
# run the boss from a local jnlp file; create a sample config file based on hpy
#

jws: $(JAR) sign 
	python ../util/localizeJnlp.py boss.jnlp-raw > boss.jnlp
	javaws boss.jnlp


# --- clean
# remove all class and jar files
#
clean:
	- find . -name "*.class" -maxdepth 1 -exec rm {} ';'
	- rm $(JAR)




help:
	egrep "^#" makefile | sed "s/^#//"

