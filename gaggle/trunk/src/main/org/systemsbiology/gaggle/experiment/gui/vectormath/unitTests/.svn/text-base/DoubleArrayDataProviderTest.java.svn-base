// DoubleArrayDataProviderTest.java
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
public class DoubleArrayDataProviderTest extends TestCase {

//------------------------------------------------------------------------------
public DoubleArrayDataProviderTest(String name) {super(name);}
//------------------------------------------------------------------------------
public void setUp() throws Exception {}
//------------------------------------------------------------------------------
public void tearDown() throws Exception {}
//------------------------------------------------------------------------------
public void testBasics() throws Exception {
    double[] data = new double[3];
    data[0] = -1.0;
    data[1] = 1.0;
    data[2] = 2.0;
    DoubleArrayDataProvider dp = new DoubleArrayDataProvider(data);
    assertTrue( dp.size() == 3 );
    assertTrue( dp.getQuick(0) == -1.0 );
    assertTrue( dp.getQuick(1) == 1.0 );
    assertTrue( dp.getQuick(2) == 2.0 );
    dp.setQuick(0,5.0);
    assertTrue( dp.getQuick(0) == 5.0 );
    assertTrue( data[0] == 5.0 );
    
    VectorDataProvider vp = dp;
    vp.setQuick(0,7.0);
    assertTrue( vp.getQuick(0) == 7.0 );
    assertTrue( dp.getQuick(0) == 7.0 );
    ReadOnlyVectorDataProvider pp = dp;
    assertTrue( pp.getQuick(0) == 7.0 );
}
//-------------------------------------------------------------------------
public static void main(String[] args) 
{
  junit.textui.TestRunner.run (new TestSuite(DoubleArrayDataProviderTest.class));
}
//------------------------------------------------------------------------------
} // DoubleArrayDataProviderTest
