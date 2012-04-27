#JC = javac -source 1.4
JC = javac

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<


OBJS = PasswordDialog.class \
       SBEAMSClient.class \
       SbeamsResultSetTableModel.class \
       SBeamsSearchPanel.class

# --- objs
# compile all java code
#
objs: $(OBJS)

# --- clean
# remove all .class files
#
clean: 
	- find . -name "*.class" -exec rm {} ';'
	- find . -name "*.jar" -exec rm {} ';'


help:
	egrep "^#" makefile | sed "s/^#//"

