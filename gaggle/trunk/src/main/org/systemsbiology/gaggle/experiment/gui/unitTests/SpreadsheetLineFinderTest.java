// SpreadsheetLineFinderTest.java
//------------------------------------------------------------------------------
// $Revision: 843 $
// $Date: 2005/02/02 13:29:17 $
// $Author: cbare $
//--------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
package org.systemsbiology.gaggle.experiment.gui.unitTests;
//--------------------------------------------------------------------------------------
import junit.framework.*;
import java.io.*;
import java.util.*;

import org.systemsbiology.gaggle.experiment.gui.SpreadsheetLineFinder;
import org.systemsbiology.gaggle.util.NameHelper;
import org.systemsbiology.gaggle.util.NameHelperFactory;

//------------------------------------------------------------------------------
public class SpreadsheetLineFinderTest extends TestCase {

//------------------------------------------------------------------------------
public SpreadsheetLineFinderTest (String name) 
{
  super (name);
}

//------------------------------------------------------------------------------
public void testCanonicalNamesToStart () throws Exception
{
  System.out.println ("testCanonicalNamesToStart");

  NameHelper nameHelper = NameHelperFactory.getNameHelper("halo"); // no longer defaults to halo
  String [] names = {"VNG1401C", "VNG1402H",  "VNG1404G",  "VNG1405C",  "VNG1406G",  
                     "VNG1406GM", "VNG1407C",  "VNG1408G",  "VNG1409C"};

  String VNG1406gGeneSymbol = "rhl";
  String VNG1408gGeneSymbol = "ush";

  SpreadsheetLineFinder finder = new SpreadsheetLineFinder (names, nameHelper);
  assertTrue (finder.getRowNumber ("VNG1401C") == 0);

  assertTrue (finder.getRowNumber ("VNGbogus") == -1);

  assertTrue (finder.getRowNumber ("VNG1404G") == 2);
  assertTrue (finder.getRowNumber ("trh1") == 2);

 
}  // testCanonicalNamesToStart
//-------------------------------------------------------------------------
public void testSomeCommonNamesToStart () throws Exception
{
  System.out.println ("testSomeCommonNamesToStart");

  NameHelper nameHelper = NameHelperFactory.getNameHelper ("halo"); // defaults to halo
  String [] names = {"VNG1401C", "VNG1402H",  "trh1",  "VNG1405C",  "VNG1406G",  
                     "VNG1406GM", "VNG1407C",  "ush",  "VNG1409C"};

  String VNG1406gGeneSymbol = "rhl";
  String VNG1408gGeneSymbol = "ush";

  SpreadsheetLineFinder finder = new SpreadsheetLineFinder (names, nameHelper);
  assertTrue (finder.getRowNumber ("VNG1401C") == 0);

  assertTrue (finder.getRowNumber ("VNGbogus") == -1);

  assertTrue (finder.getRowNumber ("VNG1404G") == 2);
  assertTrue (finder.getRowNumber ("trh1") == 2);

  assertTrue (finder.getRowNumber ("VNG1408G") == 7);
  assertTrue (finder.getRowNumber ("ush") == 7);

 
}  // testSomeCommonNamesToStart
//-------------------------------------------------------------------------
public static void main (String [] args) 
{
  junit.textui.TestRunner.run (new TestSuite (SpreadsheetLineFinderTest.class));
  System.exit (0);

}// main
//------------------------------------------------------------------------------
} // SpreadsheetLineFinderTest
