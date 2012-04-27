// ReadOnlyMathVector.java
//------------------------------------------------------------------------------
// $Revision: 1.3 $
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
 * Implementation of a vector view of a non-modifiable sequence of floating-point values.
 */
public class ReadOnlyMathVector implements ReadOnlyVectorDataProvider {
    
    ReadOnlyVectorDataProvider p;
    
    ReadOnlyMathVector(ReadOnlyVectorDataProvider p) {this.p = p;}
    
    /**
     * Creates a read-write vector referencing a copy of the data behind this vector.
     */
    public MathVector copy() {
        return MathVectorFactory.makeVector(getNewDataArray());
    }
    
    /**
     * Creates a read-only vector referencing a copy of the data behind this vector.
     */
    public ReadOnlyMathVector readOnlyCopy() {
        return MathVectorFactory.makeReadOnlyVector(getNewDataArray());
    }
    
    /**
     * Returns an array containing a copy of the data behind this vector.
     */
    public double[] getNewDataArray() {
        int size = p.size();
        double[] d = new double[size];
        for (int i=0; i<size; i++) {d[i] = p.getQuick(i);}
        return d;
    }
    
    /**
     * Returns the number of elements in this vector.
     */
    public int size() {return p.size();}
    
    /**
     * Helper method to check size matching when combining two vectors.
     *
     * @param v1  the first vector
     * @param v2 the second vector
     * @return int  the size if v1 and v2 have the same size
     * @throws IndexOutOfBoundsException  if v1 and v2 have different sizes
     */
    public static int verifySizeMatch(ReadOnlyMathVector v1, ReadOnlyMathVector v2)
    throws IndexOutOfBoundsException {
        int size = v1.p.size();
        if ( size == v2.p.size() ) {
            return size;
        } else {
            String s = "vector size mismatch: first = " + size + ", second = " + v2.p.size();
            throw new IndexOutOfBoundsException(s);
        }
    }

    /**
     * Returns the double value at the specified index.
     *
     * @param index  the index of the element to return
     * @throws IndexOutOfBoundsException unless index is valid, i.e. 0 <= index < size()
     */
    public double get(int index) throws IndexOutOfBoundsException {
        if (index > -1 && index < p.size()) {
            return p.getQuick(index);
        } else {
            String s = "Element requested: " + index + ",  vector size: " + p.size();
            throw new IndexOutOfBoundsException(s);
        }
    }
    /**
     * Unchecked access to the double value at the specified index. Some unspecified
     * run-time exception will likely result if the index is invalid.
     *
     * @param index  the index of the element to return
     */
    public double getQuick(int index) {return p.getQuick(index);}
    
    /**
     * Returns true if every element of this vector is within 'tolerance' of the
     * corresponding element in v; i.e. Math.abs(this[i] - v[i]) <= tolerance.
     *
     * @param v  the vector to compare to
     * @param tolerance  the desired measure of equality
     * @throws IndexOutOfBoundsException if v is not the same size as this vector
     */
    public boolean equals(ReadOnlyMathVector v,
                          double tolerance) throws IndexOutOfBoundsException {
        if (p == v.p) {return true;}
        int size = verifySizeMatch(this, v);
        for (int i=0; i<size; i++) {
            if ( Math.abs(p.getQuick(i) - v.p.getQuick(i)) > tolerance) {return false;}
        }
        return true;
    }
    
    /**
     * Returns the normal euclidean magnitude of this vector, i.e.
     * sqrt( Sum(this[i]*this[i]) )
     */
    public double mag() {return Math.sqrt(magSquared());}
    
    /**
     * returns the square of the euclidean magnitude of this vector, i.e.
     * Sum(this[i]*this[i])
     */
    public double magSquared() {
        double returnVal = 0;
        for (int i=0; i<p.size(); i++) {
            double d = p.getQuick(i);
            returnVal += d*d;
        }
        return returnVal;
    }

    /**
     * Returns the average of the entries of this vector.
     */
    public double mean() {
        double returnVal = 0.0;
        int size = p.size();
        if (size == 0) {return returnVal;}
        for (int i=0; i<size; i++) {returnVal += p.getQuick(i);}
        return returnVal/size;
    }
    
    /**
     * Returns the dot product of this vector with v.
     *
     * @param v  the other vector
     * @throws IndexOutOfBoundsException if v is not the same size as this vector
     */
    public double dot(ReadOnlyMathVector v) throws IndexOutOfBoundsException {
        int size = verifySizeMatch(this, v);
        double returnVal = 0.0;
        for (int i=0; i<size; i++) {
            returnVal += p.getQuick(i)*v.p.getQuick(i);
        }
        return returnVal;
    }
    
    /**
     * Returns the dot product of the normalized versions of this vector and v
     * (neither vector is modified). i.e., returns
     * this.dot(v)/( this.mag()*v.mag() )
     *
     * If the magnitude of either vector is zero, this method return zero.
     *
     * @param v the other vector
     * @throws IndexOutOfBoundsException if v is not the same size as this vector
     */
    public double dotNorm(ReadOnlyMathVector v) throws IndexOutOfBoundsException {
        double mm = mag() * v.mag();
        return (mm == 0.0) ? 0.0 : dot(v)/mm;
    }
    
    /**
     * Returns the linear correlation coefficient (Pearson correlation) between
     * this vector and v.
     *
     * The correlation coefficient between vectors X and Y is defined as
     *     coeff = Xp.dot(Yp) / ( Xp.mag()*Yp.mag() )
     *
     * where Xp = X - X.mean(),  Yp = Y - Y.mean().
     *
     * @param v  the other vector
     * @throws IndexOutOfBoundsException if v is not the same size as this vector
     */
    public double correlation(ReadOnlyMathVector v) throws IndexOutOfBoundsException {
        int size = verifySizeMatch(this, v);
        
        MathVector x = copy().subtract( this.mean() );
        MathVector y = v.copy().subtract( v.mean() );

        return x.dot(y) / ( x.mag()*y.mag() );
    }
    
    /**
     * Returns a String representation of this vector.
     */
    public String toString() {
        int size = p.size();
        if (size < 1) {return new String("()");}
        StringBuffer sb = new StringBuffer();
        sb.append( "( " + p.getQuick(0) );
        for (int i=1; i<size; i++) {
            sb.append( ", " + p.getQuick(i) );
        }
        sb.append( " )" );
        return sb.toString();
    }
}

