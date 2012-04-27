# makefile for the vectormath plugin unitTests
#------------------------------------------------------------------------
# $Revision: 1.1 $
# $Date: 2002/07/18 00:08:28 $
# $Author: amarkiel $
#------------------------------------------------------------------------
JC=javac -source 1.4
JI=java
PKG=csplugins.vectormath.unitTests

.SUFFIXES: .java .class


OBJS =	DoubleArrayDataProviderTest.class \
	FloatArrayDataProviderTest.class \
	MathVectorTest.class \
	MathVectorFactoryTest.class

default: $(OBJS) run

run:
	java -ea $(PKG).DoubleArrayDataProviderTest
	java -ea $(PKG).FloatArrayDataProviderTest
	java -ea $(PKG).MathVectorTest
	java -ea $(PKG).MathVectorFactoryTest

.java.class: 
	$(JC) $<

clean:
	- rm *.class
