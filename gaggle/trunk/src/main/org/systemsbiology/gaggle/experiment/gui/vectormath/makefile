# makefile for csplugins.vectormath
#-------------------------------------------------------------------------
# $Revision: 1.2 $
# $Date: 2002/09/30 19:04:49 $
# $Author: amarkiel $
#-------------------------------------------------------------------------

JC=javac

.SUFFIXES: .java .class
.java.class: 
	$(JC) $<

OBJS =	ReadOnlyVectorDataProvider.class \
	ReadOnlyVectorDataAdapter.class \
	VectorDataProvider.class \
	VectorDataAdapter.class \
	DoubleArrayDataProvider.class \
	FloatArrayDataProvider.class \
	ReadOnlyMathVector.class \
	MathVector.class \
	MathVectorFactory.class


default: objs

clean:
	- rm *.class 

## 
##------ objs
## build all out-of-date java source
## 

objs: $(OBJS)
