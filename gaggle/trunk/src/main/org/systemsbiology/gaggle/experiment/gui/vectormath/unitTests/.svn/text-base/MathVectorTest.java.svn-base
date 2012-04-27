// MathVectorTest.java
//------------------------------------------------------------------------------
// $Revision: 1.4 $
// $Date: 2003/02/21 01:04:05 $
// $Author: amarkiel $
//--------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui.vectormath.unitTests;
//--------------------------------------------------------------------------------------
import junit.framework.*;

import org.systemsbiology.gaggle.experiment.gui.vectormath.*;
//------------------------------------------------------------------------------
public class MathVectorTest extends TestCase {

//------------------------------------------------------------------------------
public MathVectorTest(String name) {super(name);}
//------------------------------------------------------------------------------
public void setUp() throws Exception {}
//------------------------------------------------------------------------------
public void tearDown() throws Exception {}
//------------------------------------------------------------------------------
public void testAll() throws Exception {
    double[] data0 = new double[3];
    data0[0] = 0.0;
    data0[1] = 0.0;
    data0[2] = 0.0;
    
    double[] data1 = new double[3];
    data1[0] = -1.0;
    data1[1] = 1.0;
    data1[2] = 2.0;
    
    double[] data2 = new double[3];
    data2[0] = 3.0;
    data2[1] = 0.0;
    data2[2] = -2.0;
    
    double[] dataBad = new double[2];
    dataBad[0] = 1.0;
    dataBad[1] = 2.0;
    
    MathVector v0 = MathVectorFactory.makeVector(data0);
    MathVector v1 = MathVectorFactory.makeVector(data1);
    MathVector v2 = MathVectorFactory.makeVector(data2);
    MathVector vBad = MathVectorFactory.makeVector(dataBad);
    double frac = 1.0e-10;
    
    assertTrue( v1.size() == 3 );
    assertTrue( v1.getQuick(2) == 2.0 );
    assertTrue( v1.get(0) == -1.0 );
    try {
        v1.get(3);
        assertTrue(false);
    } catch (IndexOutOfBoundsException e) {
    }
    assertTrue( Math.abs(v1.mag() - Math.sqrt(6.0)) < frac );
    assertTrue( Math.abs(v1.magSquared() - 6.0) < frac );
    assertTrue( Math.abs(v1.dot(v2) + 7.0) < frac );
    assertTrue( Math.abs(v2.dot(v1) - v1.dot(v2)) < frac );
    assertTrue( Math.abs(v1.dotNorm(v2) + 7.0/(Math.sqrt(6.0)*Math.sqrt(13.0))) < frac );
    assertTrue( Math.abs(v1.dotNorm(v2) - v2.dotNorm(v1)) < frac );
    assertTrue( v1.equals(v1,0.0) );
    assertTrue( !v1.equals(v2,0.5) );
    assertTrue( v1.equals(v2,5.0) );
    
    assertTrue( Math.abs( MathVector.magSum(v1, v2) - Math.sqrt(5.0) ) < frac );
    assertTrue( Math.abs( MathVector.magSumSquared(v1, v2) - 5.0 ) < frac );
    assertTrue( Math.abs( MathVector.magDiff(v1, v2) - Math.sqrt(33.0) ) < frac );
    assertTrue( Math.abs( MathVector.magDiffSquared(v1, v2) - 33.0 ) < frac );
    
    try {
        vBad.dot(v1);
        assertTrue(false);
    } catch (IndexOutOfBoundsException e) {
    }
    assertTrue( v1.dotNorm(v0) == 0.0 );

    MathVector aCopy = v1.copy();
    assertTrue( aCopy.get(2) == 2.0 );
    assertTrue( aCopy.equals(v1,0.0) );
    aCopy.setQuick(2,3.0);
    assertTrue( v1.get(2) == 2.0 );
    aCopy.setQuick(2,2.0);
    ReadOnlyMathVector pCopy = v1.readOnlyCopy();
    assertTrue( pCopy.get(2) == 2.0 );
    double[] newVals = v1.getNewDataArray();
    assertTrue( newVals.length == 3 );
    assertTrue( newVals[2] == 2.0 );
    newVals[2] = 1.0;
    assertTrue( v1.get(2) == 2.0 );
    
    aCopy.set(0,1.0);
    assertTrue( aCopy.getQuick(0) == 1.0 );
    aCopy.setQuick(0,-1.0);
    assertTrue( aCopy.get(0) == -1.0 );
    MathVector returnCopy = aCopy.add(v2);
    assertTrue( returnCopy.equals(aCopy,0.0) );
    assertTrue( v2.get(0) == 3.0 );
    assertTrue( aCopy.get(0) == 2.0 );
    returnCopy = aCopy.subtract(v2);
    assertTrue( returnCopy.equals(aCopy,0.0) );
    assertTrue( v2.get(0) == 3.0 );
    assertTrue( aCopy.get(0) == -1.0 );
    
    MathVector newVector = MathVector.sum(v1, v2);
    assertTrue( Math.abs(newVector.getQuick(0) - 2.0) < frac );
    newVector = MathVector.difference(v1, v2);
    assertTrue( Math.abs(newVector.getQuick(0) + 4.0) < frac );
    try {
        vBad.add(v1);
        assertTrue(false);
    } catch (IndexOutOfBoundsException e) {
    }
    try {
        vBad.subtract(v1);
        assertTrue(false);
    } catch (IndexOutOfBoundsException e) {
    }
    returnCopy = aCopy.times(2.0);
    assertTrue( returnCopy.equals(aCopy,0.0) );
    assertTrue( Math.abs(aCopy.get(0) + 2.0) < frac );
    assertTrue( Math.abs(aCopy.get(2) - 4.0) < frac );
    returnCopy = aCopy.normalize();
    assertTrue( returnCopy.equals(aCopy,0.0) );
    assertTrue( Math.abs(aCopy.get(0) + 2.0/Math.sqrt(24.0)) < frac );
    
    MathVector powCopy = v1.copy().pow(2);
    assertTrue( (powCopy.get(0) - 1.0) < frac );
    assertTrue( (powCopy.get(2) - 4.0) < frac );
    
    aCopy = v1.copy();
    aCopy.add(1.0);
    assertTrue( (aCopy.get(1) - 2.0) < frac );
    aCopy.subtract(-1.0);
    assertTrue( (aCopy.get(1) - 3.0) < frac );
    
    assertTrue( (v1.mean() - 2.0/3.0) < frac );
    assertTrue( ( v1.correlation(v2) + 69.0/(Math.sqrt(42)*Math.sqrt(114)) ) < frac );
}
//-------------------------------------------------------------------------
public static void main(String[] args) 
{
  junit.textui.TestRunner.run (new TestSuite(MathVectorTest.class));
}
//------------------------------------------------------------------------------
} // MathVectorTest
