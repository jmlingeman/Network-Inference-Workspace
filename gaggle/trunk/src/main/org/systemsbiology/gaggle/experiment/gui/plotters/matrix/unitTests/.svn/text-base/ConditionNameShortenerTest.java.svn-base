// ConditionNameShortenerTest.java
//------------------------------------------------------------------------------
// $Revision: 1.1 $
// $Date: 2004/11/16 18:33:49 $
// $Author: pshannon $
//--------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui.plotters.matrix.unitTests;
//--------------------------------------------------------------------------------------
import junit.framework.*;
import java.io.*;
import java.util.*;

import org.systemsbiology.gaggle.experiment.gui.plotters.matrix.ConditionNameShortener;
//------------------------------------------------------------------------------
public class ConditionNameShortenerTest extends TestCase {

//------------------------------------------------------------------------------
public ConditionNameShortenerTest (String name) 
{
  super (name);
}

//------------------------------------------------------------------------------
public void setUp () throws Exception
{
}
//------------------------------------------------------------------------------
public void tearDown () throws Exception
{
}
//------------------------------------------------------------------------------
public void testSimple () throws Exception
{
  System.out.println ("testSimple");
  String name = "abasd";
  String result = ConditionNameShortener.shorten(name);
  assertTrue (result.equals (name));


}  // testSimple
//-------------------------------------------------------------------------
public void testHaloComplexNames () throws Exception
{
  System.out.println ("testHaloComplexNames");
  String [] names = {"circadian__dark_cycling_125w/m2_42C_1440min_vs_NRC-1d.sig",
                     "circadian__dark_cycling_125w/m2_42C_1615min_vs_NRC-1d.sig",
                     "circadian__dark_cycling_125w/m2_42C_1800min_vs_NRC-1d.sig"};

  String [] expectedResults = {"iay224sR",
                               "iay226sR",
                               "iay228sR"};

  for (int i=0; i < names.length; i++) {
    String name = names [i];
    String result = ConditionNameShortener.shorten(name);
    assertTrue (result.equals (expectedResults [i]));
    } 

}  // testHaloComplexNames
//-------------------------------------------------------------------------
public static void main (String [] args) 
{
  junit.textui.TestRunner.run (new TestSuite (ConditionNameShortenerTest.class));

} // main
//------------------------------------------------------------------------------
} // ConditionNameShortenerTest
