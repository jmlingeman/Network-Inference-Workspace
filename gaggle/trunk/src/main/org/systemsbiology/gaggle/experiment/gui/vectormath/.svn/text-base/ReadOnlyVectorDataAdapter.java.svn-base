// ReadOnlyVectorDataAdapter.java
//------------------------------------------------------------------------------
// $Revision: 1.1 $
// $Date: 2002/09/30 19:04:49 $
// $Author: amarkiel $
//------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui.vectormath;
//------------------------------------------------------------------------------
/**
 * This class provides a default implementation of the ReadOnlyVectorDataProvider
 * interface. The purpose of this class is for constructing anonymous inner classes
 * to implement a ReadOnlyVectorDataProvider; the anonymous class should override
 * all the methods. For example:
 *
 * final List theData = myData;
 * ReadOnlyVectorDataProvider p = new ReadOnlyVectorDataAdapter() {
 *   public int size() {return theData.size();}
 *   public double getQuick(int index) {return ((Double)theData.get(index)).doubleValue();}
 * };
 * ReadOnlyMathVector vector = MathVectorFactory.makeReadOnlyVector(p);
 */
public class ReadOnlyVectorDataAdapter implements ReadOnlyVectorDataProvider {
    
    public ReadOnlyVectorDataAdapter() {}
    
    public int size() {return 0;}
    public double getQuick(int index) {return 0.0;}
}

