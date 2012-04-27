// MathVector.java
//------------------------------------------------------------------------------
// $Revision: 1.4 $
// $Date: 2003/02/21 01:04:05 $
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
 * Implementation of a vector view of a modifiable sequence of floating-point values.
 */
public class MathVector extends ReadOnlyMathVector implements VectorDataProvider {
    
    VectorDataProvider p;
    
    MathVector(VectorDataProvider p) {
        super(p);
        this.p = p;
    }
    
    /**
     * Sets the element at the specified index to the specified value.
     *
     * @param index  the index of the element to set
     * @param value  the new value for the element
     * @throws IndexOutOfBoundsException  unless index is valid, i.e. 0 <= index < size()
     */
    public void set(int index, double value) throws IndexOutOfBoundsException {
        if (index > -1 && index < p.size()) {
            p.setQuick(index,value);
        } else {
            String s = "Element requested: " + index + ",  vector size: " + p.size();
            throw new IndexOutOfBoundsException(s);
        }
    }
    /**
     * Sets the element at the specified index to the specified value without
     * range-checking. Some unspecified run-time exception will likely result
     * if the index is invalid.
     *
     * @param index  the index of the element to set
     * @param value  the new value for the element
     */
    public void setQuick(int index, double value) {p.setQuick(index, value);}
    
    
    /**
     * Normalizes this vector, i.e. this[i] = this[i]/this.mag()
     *
     * @return MathVector  this modified vector
     */
    public MathVector normalize() {
        double norm = mag();
        if (norm == 0) {return this;}
        for (int i=0; i<p.size(); i++) {
            p.setQuick(i, p.getQuick(i)/norm);
        }
        return this;
    }

    /**
     * Modifies this vector by adding the supplied vector.
     *
     * @param v  the other vector
     * @return MathVector  this modified vector
     * @throws IndexOutOfBoundsException if v is not the same size as this vector
     */
    public MathVector add(ReadOnlyMathVector v) throws IndexOutOfBoundsException {
        int size = ReadOnlyMathVector.verifySizeMatch(this, v);
        for (int i=0; i<size; i++) {
            p.setQuick(i, p.getQuick(i) + v.p.getQuick(i));
        }
        return this;
    }
    
    /**
     * Returns a new MathVector equal to the sum of the arguments, i.e. v1+v2.
     *
     * @param v1  the first vector
     * @param v2 the second vector
     * @return MathVector  a new MathVector holding the result
     * @throws IndexOutOfBoundsException if v1 and v2 have different sizes
     */
    public static MathVector sum(ReadOnlyMathVector v1, ReadOnlyMathVector v2)
    throws IndexOutOfBoundsException {
        int size = ReadOnlyMathVector.verifySizeMatch(v1, v2);
        double[] vals = new double[size];
        for (int i=0; i<size; i++) {
            vals[i] = v1.getQuick(i) + v2.getQuick(i);
        }
        return MathVectorFactory.makeVector(vals);
    }
    
    /**
     * Modifies this vector by subtracting the supplied vector.
     *
     * @param v  the other vector
     * @return MathVector  this modified vector
     * @throws IndexOutOfBoundsException if v is not the same size as this vector
     */    
    public MathVector subtract(ReadOnlyMathVector v) throws IndexOutOfBoundsException {
        int size = ReadOnlyMathVector.verifySizeMatch(this, v);
        for (int i=0; i<size; i++) {
            p.setQuick(i, p.getQuick(i) - v.p.getQuick(i));
        }
        return this;
    }
    
    /**
     * Returns a new MathVector equal to the difference of the arguments, i.e. v1-v2.
     *
     * @param v1  the first vector
     * @param v2 the second vector
     * @return MathVector  a new MathVector holding the result
     * @throws IndexOutOfBoundsException if v1 and v2 have different sizes
     */
    public static MathVector difference(ReadOnlyMathVector v1, ReadOnlyMathVector v2)
    throws IndexOutOfBoundsException {
        int size = ReadOnlyMathVector.verifySizeMatch(v1, v2);
        double[] vals = new double[size];
        for (int i=0; i<size; i++) {
            vals[i] = v1.getQuick(i) - v2.getQuick(i);
        }
        return MathVectorFactory.makeVector(vals);
    }
    
    /**
     * Adds the supplied scalar value to each element of this vector.
     *
     * @param d  the value to add
     * @return MathVector  this modified vector
     */
    public MathVector add(double d) {
        for (int i=0; i<p.size(); i++) {
            p.setQuick(i, p.getQuick(i)+d );
        }
        return this;
    }
    
    /**
     * Subtracts the supplied scalar value from each element of this vector.
     *
     * @param d  the value to subtract
     * @return MathVector  this modified vector
     */
    public MathVector subtract(double d) {
        for (int i=0; i<p.size(); i++) {
            p.setQuick(i, p.getQuick(i)-d );
        }
        return this;
    }
    
    /**
     * Multiplies this vector by the supplied scalar value.
     *
     * @param d  the multiplying factor
     * @return MathVector  this modified vector
     */
    public MathVector times(double d) {
        for (int i=0; i<p.size(); i++) {
            p.setQuick(i, d*p.getQuick(i));
        }
        return this;
    }

    /**
     * Raises each element of this vector to the given power; i.e.
     *   this[i] = Math.pow(this[i],d)
     * All the qualifications of java.lang.Math.pow() apply.
     *
     * @param d  the exponent
     * @return MathVector  this modified vector
     */
    public MathVector pow(double d) {
        for (int i=0; i<p.size(); i++) {
            double val = p.getQuick(i);
            p.setQuick(i, Math.pow(val,d) );
        }
        return this;
    }
    
    /**
     * Returns the euclidean magnitude of the sum of the argument vectors; i.e.
     * sqrt( Sum( (v1[i]+v2[i]) ) )
     *
     * @param v1  the first vector
     * @param v2 the second vector
     * @return double  the result
     * @throws IndexOutOfBoundsException if v1 and v2 have different sizes
     */
    public static double magSum(ReadOnlyMathVector v1, ReadOnlyMathVector v2)
    throws IndexOutOfBoundsException {
        return Math.sqrt( magSumSquared(v1, v2) );
    }
    
    /**
     * Returns the square of the euclidean magnitude of the sum of the arguments; i.e.
     * Sum( (v1[i]+v2[i]) )
     *
     * @param v1  the first vector
     * @param v2 the second vector
     * @return double  the result
     * @throws IndexOutOfBoundsException if v1 and v2 have different sizes
     */
    public static double magSumSquared(ReadOnlyMathVector v1, ReadOnlyMathVector v2) {
        int size = ReadOnlyMathVector.verifySizeMatch(v1, v2);
        double returnVal = 0.0;
        for (int i=0; i<size; i++) {
            double sum = v1.getQuick(i) + v2.getQuick(i);
            returnVal += sum*sum;
        }
        return returnVal;
    }
    
    /**
     * Returns the euclidean magnitude of the difference of the argument vectors; i.e.
     * sqrt( Sum( (v1[i]-v2[i]) ) )
     *
     * @param v1  the first vector
     * @param v2 the second vector
     * @return double  the result
     * @throws IndexOutOfBoundsException if v1 and v2 have different sizes
     */
    public static double magDiff(ReadOnlyMathVector v1, ReadOnlyMathVector v2)
    throws IndexOutOfBoundsException {
        return Math.sqrt( magDiffSquared(v1, v2) );
    }
    
    /**
     * Returns the square of the euclidean magnitude of the difference of the arguments;
     * i.e.  Sum( (v1[i]+v2[i]) )
     *
     * @param v1  the first vector
     * @param v2 the second vector
     * @return double  the result
     * @throws IndexOutOfBoundsException if v1 and v2 have different sizes
     */
    public static double magDiffSquared(ReadOnlyMathVector v1, ReadOnlyMathVector v2) {
        int size = ReadOnlyMathVector.verifySizeMatch(v1, v2);
        double returnVal = 0.0;
        for (int i=0; i<size; i++) {
            double diff = v1.getQuick(i) - v2.getQuick(i);
            returnVal += diff*diff;
        }
        return returnVal;
    }
}

