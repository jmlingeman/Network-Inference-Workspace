// MathVectorFactory.java
//------------------------------------------------------------------------------
// $Revision: 1.1 $
// $Date: 2002/07/18 00:08:28 $
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
 * This factory class contains static methods to create vector views of data.
 * The makeVector set return read-write vectors as MathVector objects, while
 * the makeReadOnlyVector set return read-only vectors as ReadOnlyMathVector objects, 
 */
public class MathVectorFactory {
    
    /**
     * Create a MathVector referencing data from the supplied VectorDataProvider.
     */
    public static MathVector makeVector(VectorDataProvider p) {
        return new MathVector(p);
    }
    
    /**
     * Create a MathVector wrapper around the provided float array.
     */
    public static MathVector makeVector(float[] f) {
        return makeVector(new FloatArrayDataProvider(f));
    }
    
    /**
     * Create a MathVector wrapper around the provided double array.
     */
    public static MathVector makeVector(double[] d) {
        return makeVector(new DoubleArrayDataProvider(d));
    }

    
    /**
     * Create a ReadOnlyMathVector referencing data from the supplied
     * ReadOnlyVectorDataProvider.
     */    
    public static ReadOnlyMathVector makeReadOnlyVector(ReadOnlyVectorDataProvider p) {
        return new ReadOnlyMathVector(p);
    }
           
    /**
     * Create a ReadOnlyMathVector wrapper around the provided float array.
     */ 
    public static ReadOnlyMathVector makeReadOnlyVector(float[] f) {
        return makeReadOnlyVector(new FloatArrayDataProvider(f));
    }
    
    /**
     * Create a ReadOnlyMathVector wrapper around the provided double array.
     */    
    public static ReadOnlyMathVector makeReadOnlyVector(double[] d) {
        return makeReadOnlyVector(new DoubleArrayDataProvider(d));
    }
}
