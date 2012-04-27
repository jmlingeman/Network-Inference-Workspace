// TileCalculatorTest.java
//------------------------------------------------------------------------------
// $Revision: 1.1 $
// $Date: 2004/12/07 04:11:38 $
// $Author: paulshannon $
//--------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.boss.unitTests;
//--------------------------------------------------------------------------------------
import junit.framework.*;
import java.util.*;

import org.systemsbiology.gaggle.boss.*;
//------------------------------------------------------------------------------
public class TileCalculatorTest extends TestCase {

//------------------------------------------------------------------------------
public TileCalculatorTest (String name) 
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
public void testRowAndColumnCounts () throws Exception
{
  System.out.println  ("testRowAndColumnCounts");

  TileCalculator tc = new TileCalculator (1000, 1000, 4);
  assertEquals(2, tc.getRowCount ());
  assertEquals(2, tc.getColumnCount ());

  tc = new TileCalculator (1000, 1000, 5);
  assertEquals(2, tc.getRowCount ());
  assertEquals(3, tc.getColumnCount ());

  tc = new TileCalculator (1000, 1000, 48);
  assertEquals(7, tc.getRowCount ());
  assertEquals(7, tc.getColumnCount ());

  tc = new TileCalculator (1000, 1000, 49);
  assertEquals(7, tc.getRowCount ());
  assertEquals(7, tc.getColumnCount ());

  tc = new TileCalculator (1000, 1000, 50);
  assertEquals(8, tc.getColumnCount ());
  assertEquals(7, tc.getRowCount ());

} // testRowAndColumnCounts
//------------------------------------------------------------------------------
public void testWindowSize ()
{
  System.out.println  ("testWindowSize");

  TileCalculator tc = new TileCalculator (1000, 1000, 4);
  assertEquals (500, tc.getCellWidth ());
  assertEquals (500, tc.getCellHeight ());

  tc = new TileCalculator (1000, 1000, 9);
  assertEquals (333, tc.getCellWidth ());
  assertEquals (333, tc.getCellHeight ());
  
  tc = new TileCalculator (1000, 1000, 8);
  assertEquals(333, tc.getCellWidth ());
  assertEquals(333, tc.getCellHeight ());

  tc = new TileCalculator (1000, 1000, 7);
  assertEquals(333, tc.getCellWidth ());
  assertEquals(333, tc.getCellHeight ());

  tc = new TileCalculator (1000, 1000, 6);
  assertEquals(333, tc.getCellWidth ());
  assertEquals(500, tc.getCellHeight ());

  tc = new TileCalculator (1000, 1000, 1);
  assertEquals(1000, tc.getCellWidth ());
  assertEquals(1000, tc.getCellHeight ());

  tc = new TileCalculator (1000, 1000, 1000000);
  assertEquals(1, tc.getCellWidth ());
  assertEquals(1, tc.getCellHeight ());

}     
//------------------------------------------------------------------------------
public void testGetRowAndColumn ()
{
  System.out.println  ("testGetRowAndColumn");
  int [] rc;

  TileCalculator tc = new TileCalculator (1000, 1000, 4);
  rc = tc.getRowAndColumn (0);
  assertEquals(0, rc [0]);
  assertEquals(0, rc [1]);

  tc = new TileCalculator (1000, 1000, 4);
  rc = tc.getRowAndColumn (1);
  assertEquals(0, rc [0]);
  assertEquals(1, rc [1]);

  tc = new TileCalculator (1000, 1000, 4);
  rc = tc.getRowAndColumn (2);
  assertEquals(1, rc [0]);
  assertEquals(0, rc [1]);

  tc = new TileCalculator (1000, 1000, 4);
  rc = tc.getRowAndColumn (3);
  assertEquals(1, rc [0]);
  assertEquals(1, rc [1]);

    //------------------ try out an 11-window tile

  tc = new TileCalculator (1000, 1000, 11);
  assertEquals(4, tc.getColumnCount ());
  assertEquals(3, tc.getRowCount ());

  assertEquals(250, tc.getCellWidth ());
  assertEquals(333, tc.getCellHeight ());

  rc = tc.getRowAndColumn (0);
  assertEquals(0, rc [0]);
  assertEquals(0, rc [1]);

  rc = tc.getRowAndColumn (1);
  assertEquals(0, rc [0]);
  assertEquals(1, rc [1]);

  rc = tc.getRowAndColumn (2);
  assertEquals(0, rc [0]);
  assertEquals(2, rc [1]);

  rc = tc.getRowAndColumn (3);
  assertEquals(0, rc [0]);
  assertEquals(3, rc [1]);

  rc = tc.getRowAndColumn (4);
  assertEquals(1, rc [0]);
  assertEquals(0, rc [1]);

  rc = tc.getRowAndColumn (5);
  assertEquals(1, rc [0]);
  assertEquals(1, rc [1]);

  rc = tc.getRowAndColumn (6);
  assertEquals(1, rc [0]);
  assertEquals(2, rc [1]);

  rc = tc.getRowAndColumn (7);
  assertEquals(1, rc [0]);
  assertEquals(3, rc [1]);

  rc = tc.getRowAndColumn (8);
  assertEquals(2, rc [0]);
  assertEquals(0, rc [1]);

  rc = tc.getRowAndColumn (9);
  assertEquals(2, rc [0]);
  assertEquals(1, rc [1]);

  rc = tc.getRowAndColumn (10);
  assertEquals(2, rc [0]);
  assertEquals(2, rc [1]);

}    
//------------------------------------------------------------------------------
public void testGetGeometry ()
{
  System.out.println  ("testGetGeometry");

  TileCalculator tc = new TileCalculator (1000, 1000, 4);
  assertTrue (Arrays.equals (tc.getGeometry (0), new int [] {0,0,500, 500}));
  assertTrue (Arrays.equals (tc.getGeometry (1), new int [] {500,0,500, 500}));
  assertTrue (Arrays.equals (tc.getGeometry (2), new int [] {0,500,500, 500}));
  assertTrue (Arrays.equals (tc.getGeometry (3), new int [] {500,500,500, 500}));
}
//------------------------------------------------------------------------------
public static void main (String [] args) 
{
  junit.textui.TestRunner.run (new TestSuite (TileCalculatorTest.class));

}// main
//------------------------------------------------------------------------------
} // TileCalculatorTest
