// DataMatrixFileReaderTest.java
//------------------------------------------------------------------------------
// $Revision: 2360 $
// $Date: 2005/04/09 22:52:52 $
// $Author: dtenenba $
//--------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.readers.unitTests;
//--------------------------------------------------------------------------------------
import junit.framework.*;

import org.systemsbiology.gaggle.core.datatypes.DataMatrix;
import org.systemsbiology.gaggle.experiment.readers.DataMatrixFileReader;
//------------------------------------------------------------------------------
public class DataMatrixFileReaderTest extends TestCase {

//------------------------------------------------------------------------------
public DataMatrixFileReaderTest (String name) 
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
  String protocol = "file://";
  String filename = "bogus";

  DataMatrixFileReader reader = new DataMatrixFileReader (protocol + filename);
  try {
    reader.read ();
    assertTrue (false);
    }
  catch (IllegalArgumentException iae) {;}

  filename = "simpleMatrix.txt";

 reader = new DataMatrixFileReader (protocol + filename);
  try {
    reader.read ();
    assertTrue (true);
    }
  catch (IllegalArgumentException iae) {
    assertTrue (false);
    }

  org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix = reader.get ();
  assertTrue (matrix.getRowCount () == 4);
  assertTrue (matrix.getColumnCount () == 3);

  assertTrue (matrix.get (0,0) == 12.2);
  assertTrue (matrix.get (0,1) == 13.8);
  assertTrue (matrix.get (0,2) == 4);

  assertTrue (matrix.get (1,0) == -1.2);
  assertTrue (matrix.get (1,1) == -8);
  assertTrue (matrix.get (1,2) == -32.3333);

  assertTrue (matrix.get (2,0) == 0.0);
  assertTrue (matrix.get (2,1) == 0.0);
  assertTrue (matrix.get (2,2) == 0);

  assertTrue (matrix.get (3,0) == 999.0);
  assertTrue (matrix.get (3,1) == 99.0);
  assertTrue (matrix.get (3,2) == 9.0);

  String [] columnTitles = matrix.getColumnTitles ();
  String [] rowTitles = matrix.getRowTitles ();

  assertTrue (matrix.getRowTitlesTitle().equals ("gene"));
  assertTrue (columnTitles [0].equals ("cond0"));
  assertTrue (columnTitles [1].equals ("cond1"));
  assertTrue (columnTitles [2].equals ("cond2"));;

  assertTrue (rowTitles [0].equals ("a"));
  assertTrue (rowTitles [1].equals ("b"));
  assertTrue (rowTitles [2].equals ("c"));
  assertTrue (rowTitles [3].equals ("d"));

} // testCtor
//-------------------------------------------------------------------------
public void test50lineTraditionalMatrixFile () throws Exception
{ 
  System.out.println ("test50lineTraditionalMatrixFile");
  String protocol = "file://";
  String filename = "matrix.expression";

  DataMatrixFileReader reader = new DataMatrixFileReader (protocol + filename);
  try {
    reader.read ();
    }
  catch (IllegalArgumentException iae) {
    System.out.println (iae.getMessage ());
    assertTrue (false);
    }

  DataMatrix matrix = reader.get ();
  assertTrue (matrix.getRowCount () == 49);
  assertTrue (matrix.getColumnCount () == 5);

  String [] expectedColumnTitles = {"Spellman_alphaT028_vs_async",
                                    "Spellman_alphaT119_vs_async", 
                                    "Spellman_alphaT091_vs_async",
                                    "Spellman_alphaT063_vs_async",
                                    "Spellman_alphaT000_vs_async"};

  assertTrue (matrix.getRowTitlesTitle().equals ("GENE"));
  String [] actualColumnTitles = matrix.getColumnTitles ();
  for (int i=0; i < actualColumnTitles.length; i++)
    assertTrue (expectedColumnTitles [i].equals (actualColumnTitles [i]));

  String [] actualRowTitles = matrix.getRowTitles ();

    // 0th, 10th, 20th, 30th, 40th row titles:
  String [] expectedRowTitles = {"YAL014C", "YAR018C", "YBL059W",
                                 "YBL101C", "YBR058C"};
  assertTrue (expectedRowTitles [0].equals (actualRowTitles [0]));
  assertTrue (expectedRowTitles [1].equals (actualRowTitles [10]));
  assertTrue (expectedRowTitles [2].equals (actualRowTitles [20]));
  assertTrue (expectedRowTitles [3].equals (actualRowTitles [30]));
  assertTrue (expectedRowTitles [4].equals (actualRowTitles [40]));


} // test50lineTraditionalMatrixFile 
//-------------------------------------------------------------------------
public void test50lineIcatMatrixFile () throws Exception
{ 
  System.out.println ("test50lineIcatMatrixFile");
  String protocol = "file://";
  String filename = "matrix.icat";

  DataMatrixFileReader reader = new DataMatrixFileReader (protocol + filename);
  try {
    reader.read ();
    }
  catch (IllegalArgumentException iae) {
    assertTrue (false);
    }

  DataMatrix matrix = reader.get ();
  assertTrue (matrix.getRowCount () == 49);
  // System.out.println ("columns: " + matrix.getColumnCount ());
  assertTrue (matrix.getColumnCount () == 5);


} // test50lineTraditionalMatrixFile 
//-------------------------------------------------------------------------
public void testHugeMatrixFile () throws Exception
{ 
  System.out.println ("testHugeMatrixFile");
  String protocol = "file://";
  String filename = "../../sampleData/huge.ratio";

  DataMatrixFileReader reader = new DataMatrixFileReader (protocol + filename);
  try {
    reader.read ();
    }
  catch (IllegalArgumentException iae) {
    assertTrue (false);
    }

  DataMatrix matrix = reader.get ();
  assertTrue (matrix.getRowCount () == 45101);
  assertTrue (matrix.getColumnCount () == 25);


} // testHugeMatrixFile 
//-------------------------------------------------------------------------
public void testHttpRead () throws Exception
{ 
  System.out.println ("testHttpRead");
  String protocol = "http://";
  //String filename = "db.systemsbiology.net/cytoscape/projects/static/halo/data/uvRepair.ratio";
  String filename = "db:8060/halo/data/uvRepair.ratio";

  DataMatrixFileReader reader = new DataMatrixFileReader (protocol + filename);
  try {
    reader.read ();
    }
  catch (IllegalArgumentException iae) {
    assertTrue (false);
    }

  org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix = reader.get ();
  assertTrue (matrix.getColumnCount () == 5);
  assertTrue (matrix.getRowCount () == 2400);


} // testHttpRead
//-------------------------------------------------------------------------
public void testHttpIndirectRead () throws Exception
{ 
  System.out.println ("testHttpIndirectRead");
  String protocol = "httpIndirect://";
  String filename = "db.systemsbiology.net:8080/halo/DataFetcher.py/test0.ratio";

  DataMatrixFileReader reader = new DataMatrixFileReader (protocol + filename);
  try {
    reader.read ();
    }
  catch (IllegalArgumentException iae) {
    assertTrue (false);
    }

  DataMatrix matrix = reader.get ();
  assertTrue (matrix.getColumnCount () == 4);
  assertTrue (matrix.getRowCount () == 2400);


} // testHttpIndirectRead
//-------------------------------------------------------------------------
public void testSerializedObjectRead () throws Exception
{ 
  System.out.println ("testSerializedObjectRead");
  String protocol = "obj://";
  String filename = "matrix.obj";

  DataMatrixFileReader reader = new DataMatrixFileReader (protocol + filename);
  try {
    reader.read ();
    }
  catch (IllegalArgumentException iae) {
    assertTrue (false);
    }

  org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix = reader.get ();
  assertTrue (matrix.getColumnCount () == 3);
  assertTrue (matrix.getRowCount () == 4);


} // testSerializedObjectRead
//-------------------------------------------------------------------------
public void testBadFileTitleLineException () throws Exception
{ 
  System.out.println ("testBadFileTitleLineExceptions");
  String protocol = "file://";
  String filename = "../../sampleData/badTitleLine.ratio";  // no tabs

  DataMatrixFileReader reader = new DataMatrixFileReader (protocol + filename);
  try {
    reader.read ();
    assertTrue (false);
    }
  catch (Exception ex0) {
    assertTrue (ex0.getMessage().indexOf ("error reading matrix file") >= 0);
    assertTrue (ex0.getMessage().indexOf ("badTitleLine.ratio") >= 0);
    assertTrue (ex0.getMessage().indexOf ("two (tab-delimited) titles") >= 0);
    }

} // testBadFileTitleLineExceptions
//-------------------------------------------------------------------------
public void testBadFileRowTokenCountException () throws Exception
{ 
  System.out.println ("testBadFileRowTokenCountExceptions");
  String protocol = "file://";
  String filename = "../../sampleData/badRowTokenCount.ratio";  // no tabs

  DataMatrixFileReader reader = new DataMatrixFileReader (protocol + filename);
  try {
    reader.read ();
    assertTrue (false);
    }
  catch (Exception ex0) {
    assertTrue (ex0.getMessage().indexOf ("error reading matrix file") >= 0);
    assertTrue (ex0.getMessage().indexOf ("at line: 2, column: 2") > 0);
    assertTrue (ex0.getMessage().indexOf ("incorrect number of columns") > 0);
    }

} // testBadFileTitleLineExceptions
//-------------------------------------------------------------------------
public void testNAsAndMissingValues () throws Exception
{ 
  System.out.println ("testNAsAndMissingValues");
  String protocol = "file://";
  String filename = "../../sampleData/smallMatrixWithMissingValues.ratio";  // no tabs

  DataMatrixFileReader reader = new DataMatrixFileReader (protocol + filename);
  reader.read ();
  DataMatrix matrix = reader.get ();
  assertTrue (matrix.getColumnCount () == 3);
  assertTrue (matrix.getRowCount () == 4);

  assertTrue (matrix.get (0,0) == 0.2);
  assertTrue (matrix.get (0,1) == 0.8);
  assertTrue (matrix.get (0,2) == -0.9);

  assertTrue (matrix.get (1,0) == 99.0);
  assertTrue (Double.isNaN (matrix.get (1,1)));
  assertTrue (matrix.get (1,2) == 87.3);

  assertTrue (Double.isNaN (matrix.get (2,0)));
  assertTrue (Double.isNaN (matrix.get (2,1)));
  assertTrue (Double.isNaN (matrix.get (2,2)));

  assertTrue (Double.isNaN (matrix.get (3,0)));
  assertTrue (Double.isNaN (matrix.get (3,1)));
  assertTrue (matrix.get (3,2) == 32.3222);
  

} // testNAsAndMissingValues
//-------------------------------------------------------------------------
/** october 2005: i discovered that (new to java 1.5?) a line with multiple
 *  empty columns, consisting of contiguous tab characters, was parsed with those
 *  empty columns ignored.  this method demonstrates that bug.
 *  the fix (tested here) is to add the extra 'limit' argument to String.split:
 *
 *          String [] tokens = rowString.split ("\t", -1);
 *
 *  documented thus:
 *
 *   the limit parameter controls the number of times the pattern is applied
 *   and therefore affects the length of the resulting array. If the limit
 *   n is greater than zero then the pattern will be applied at most n - 1
 *   times, the array's length will be no greater than n, and the array's
 *   last entry will contain all input beyond the last matched
 *   delimiter. If n is non-positive then the pattern will be applied as
 *   many times as possible and the array can have any length. If n is zero
 *   then the pattern will be applied as many times as possible, the array
 *   can have any length, and trailing empty strings will be discarded.
 *
 */
public void testMissingValuesAndColumnCount () throws Exception
{ 
  System.out.println ("testMissingValuesAndColumnCount");
  String protocol = "file://";
  String filename = "../../sampleData/manyEmptyColumns.ratio";

  DataMatrixFileReader reader = new DataMatrixFileReader (protocol + filename);
  reader.read ();
  DataMatrix matrix = reader.get ();
  //System.out.println ("columns: " + matrix.getColumnCount ());
  //System.out.println ("rows: " + matrix.getRowCount ());
  assertTrue (matrix.getColumnCount () == 643);
  assertTrue (matrix.getRowCount () == 3);

} // testValuesAndColumnCount
//-------------------------------------------------------------------------
public static void main (String [] args) 
{
  junit.textui.TestRunner.run (new TestSuite (DataMatrixFileReaderTest.class));

}// main
//------------------------------------------------------------------------------
} // DataMatrixFileReaderTest
