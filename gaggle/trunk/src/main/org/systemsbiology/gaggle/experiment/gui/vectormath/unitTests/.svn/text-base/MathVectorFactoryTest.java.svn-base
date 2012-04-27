// MathVectorFactoryTest.java
//------------------------------------------------------------------------------
// $Revision: 1.1 $
// $Date: 2002/07/18 00:08:28 $
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
public class MathVectorFactoryTest extends TestCase {

//------------------------------------------------------------------------------
public MathVectorFactoryTest(String name) {super(name);}
//------------------------------------------------------------------------------
public void setUp() throws Exception {}
//------------------------------------------------------------------------------
public void tearDown() throws Exception {}
//------------------------------------------------------------------------------
public void testAll() throws Exception {
    double[] data1 = new double[3];
    data1[0] = -1.0;
    data1[1] = 1.0;
    data1[2] = 2.0;
    
    float[] fData = new float[2];
    fData[0] = 4.5f;
    fData[1] = -3.2f;

    
    MathVector v1 = MathVectorFactory.makeVector(data1);
    assertTrue( v1.get(2) == 2.0 );
    MathVector v2 = MathVectorFactory.makeVector(new DoubleArrayDataProvider(data1));
    assertTrue( v2.equals(v1,0.0) );
    ReadOnlyMathVector pv1 = MathVectorFactory.makeReadOnlyVector(data1);
    assertTrue( pv1.equals(v1,0.0) );
    ReadOnlyMathVector pv2 = MathVectorFactory.makeReadOnlyVector(new DoubleArrayDataProvider(data1));
    assertTrue( pv2.equals(v1,0.0) );
    
    MathVector v3 = MathVectorFactory.makeVector(fData);
    assertTrue( v3.get(1) == -3.2f );
    MathVector v4 = MathVectorFactory.makeVector(new FloatArrayDataProvider(fData));
    assertTrue( v4.equals(v3,0.0) );
    ReadOnlyMathVector pv3 = MathVectorFactory.makeReadOnlyVector(fData);
    assertTrue( pv3.equals(v3,0.0) );
    ReadOnlyMathVector pv4 = MathVectorFactory.makeReadOnlyVector(new FloatArrayDataProvider(fData));
    assertTrue( pv4.equals(v3,0.0) );
}
//-------------------------------------------------------------------------
public static void main(String[] args) 
{
  junit.textui.TestRunner.run (new TestSuite(MathVectorFactoryTest.class));
}
//------------------------------------------------------------------------------
} // MathVectorFactoryTest
