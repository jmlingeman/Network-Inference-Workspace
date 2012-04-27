// MatrixCombinerTest.java
//------------------------------------------------------------------------------
// $Revision: 2360 $
// $Date: 2007-09-04 18:02:00 -0400 (Tue, 04 Sep 2007) $
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

package org.systemsbiology.gaggle.experiment.datamatrix.unitTests;
//--------------------------------------------------------------------------------------
import junit.framework.*;

import java.util.*;

import org.systemsbiology.gaggle.experiment.datamatrix.*;
import org.systemsbiology.gaggle.experiment.readers.*;

//------------------------------------------------------------------------------
public class MatrixCombinerTest extends TestCase {

//------------------------------------------------------------------------------
public MatrixCombinerTest (String name) 
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
public void testCombine1 () throws Exception
/**
 * 'combining' 1 matrix should return an identical matrix
 */
{
  System.out.println ("testCombine1");
  DataMatrixFileReader reader = new DataMatrixFileReader ("file://../../sampleData/simpleMatrix.txt");
  reader.read ();
  org.systemsbiology.gaggle.core.datatypes.DataMatrix m1 = reader.get ();
  MatrixCombiner mc = new MatrixCombiner (new org.systemsbiology.gaggle.core.datatypes.DataMatrix[] {m1});
  org.systemsbiology.gaggle.core.datatypes.DataMatrix m2 = mc.combine ();
  assertTrue (m2.getRowCount () == 4);
  assertTrue (m2.getColumnCount () == 3);

  assertTrue (Arrays.equals (m1.getColumnTitles(), m2.getColumnTitles ()));

  double [][] data1 = m1.get ();
  double [][] data2 = m2.get ();
  assertTrue (data1.length == data2.length);
  assertTrue (data1.length == 4);
  for (int i=0; i < data1.length; i++)
    assertTrue (Arrays.equals (data1 [i], data2 [i]));

} // testCombine1
//-------------------------------------------------------------------------
public void testCombine2 () throws Exception
/**
 * combine 2 matrices
 */
{
  System.out.println ("testCombine2");
  DataMatrixFileReader reader = new DataMatrixFileReader ("file://../../sampleData/simpleMatrix.txt");
  reader.read ();
  org.systemsbiology.gaggle.core.datatypes.DataMatrix m1 = reader.get ();
  reader = new DataMatrixFileReader ("file://../../sampleData/simpleMatrix2.txt");
  reader.read ();
  org.systemsbiology.gaggle.core.datatypes.DataMatrix m2 = reader.get ();
  MatrixCombiner mc = new MatrixCombiner (new org.systemsbiology.gaggle.core.datatypes.DataMatrix[] {m1, m2});
  org.systemsbiology.gaggle.core.datatypes.DataMatrix result = mc.combine ();
  assertTrue (result.getRowCount () == 6);
  assertTrue (result.getColumnCount () == 6);

  String [] expected = new String [] {"cond0", "cond1", "cond2", "cond3", "cond4", "cond5"};
  String [] actual = result.getColumnTitles ();
  assertTrue (Arrays.equals (expected, actual));

  expected = new String [] {"a", "b", "c", "d", "e", "f"};
  actual = result.getRowTitles ();
  assertTrue (Arrays.equals (actual, expected));

  double [] expected2 = new double [] {12.2, 13.8, 4.0, 82.2, 83.8, 84.0};
  double [] actual2 = result.get ("a");
  assertTrue (Arrays.equals (actual, expected));

  double NaN = Double.NaN;
  expected2 = new double [] {-1.2, -8.0, -32.3333, NaN, NaN, NaN};
  actual2 = result.get ("b");
  assertTrue (Arrays.equals (actual2, expected2));

  expected2 = new double [] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
  actual2 = result.get ("c");
  assertTrue (Arrays.equals (actual2, expected2));

  expected2 = new double [] {999.0, 99.0, 9.0, NaN, NaN, NaN};
  actual2 = result.get ("d");
  assertTrue (Arrays.equals (actual2, expected2));

  expected2 = new double [] {NaN, NaN, NaN, 888.0, 88.0, 8.0};
  actual2 = result.get ("e");
  assertTrue (Arrays.equals (actual2, expected2));

  expected2 = new double [] {NaN, NaN, NaN, 8.0, 8.88, 7.888};
  actual2 = result.get ("f");
  assertTrue (Arrays.equals (actual2, expected2));

}  // testCombine2
//------------------------------------------------------------------------------
public void testCombine3 () throws Exception
/**
 * combine 3 matrices
 */
{
  System.out.println ("testCombine3");
  DataMatrixFileReader reader = new DataMatrixFileReader ("file://../../sampleData/simpleMatrix.txt");
  reader.read ();
  org.systemsbiology.gaggle.core.datatypes.DataMatrix m1 = reader.get ();
  reader = new DataMatrixFileReader ("file://../../sampleData/simpleMatrix2.txt");
  reader.read ();
  org.systemsbiology.gaggle.core.datatypes.DataMatrix m2 = reader.get ();
  reader = new DataMatrixFileReader ("file://../../sampleData/simpleMatrix3.txt");
  reader.read ();
  org.systemsbiology.gaggle.core.datatypes.DataMatrix m3 = reader.get ();

  MatrixCombiner mc = new MatrixCombiner (new org.systemsbiology.gaggle.core.datatypes.DataMatrix[] {m1, m2, m3});
  org.systemsbiology.gaggle.core.datatypes.DataMatrix result = mc.combine ();
  assertTrue (result.getRowCount () == 7);
  assertTrue (result.getColumnCount () == 7);

  String [] expected = new String [] {"cond0", "cond1", "cond2", "cond3", "cond4", "cond5", "cond6"};
  String [] actual = result.getColumnTitles ();
  assertTrue (Arrays.equals (actual, expected));

  expected = new String [] {"a", "b", "c", "d", "e", "f", "g"};
  actual = result.getRowTitles ();
  assertTrue (Arrays.equals (actual, expected));

  double [] expected2 = new double [] {12.2, 13.8, 4.0, 82.2, 83.8, 84.0, 1.1};
  double [] actual2 = result.get ("a");
  assertTrue (Arrays.equals (actual2, expected2));

  double NaN = Double.NaN;
  expected2 = new double [] {-1.2, -8.0, -32.3333, NaN, NaN, NaN, 2.2};
  actual2 = result.get ("b");
  assertTrue (Arrays.equals (actual2, expected2));

  expected2 = new double [] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 3.3};
  actual2 = result.get ("c");
  assertTrue (Arrays.equals (actual2, expected2));

  expected2 = new double [] {999.0, 99.0, 9.0, NaN, NaN, NaN, 4.4};
  actual2 = result.get ("d");
  assertTrue (Arrays.equals (actual2, expected2));

  expected2 = new double [] {NaN, NaN, NaN, 888.0, 88.0, 8.0, 5.5};
  actual2 = result.get ("e");
  assertTrue (Arrays.equals (actual2, expected2));

  expected2 = new double [] {NaN, NaN, NaN, 8.0, 8.88, 7.888, 6.6};
  actual2 = result.get ("f");
  assertTrue (Arrays.equals (actual2, expected2));

  expected2 = new double [] {NaN, NaN, NaN, NaN, NaN, NaN, 7.7};
  actual2 = result.get ("g");
  assertTrue (Arrays.equals (actual2, expected2));

} // testCombine3
//------------------------------------------------------------------------------
public void testCombine2CheckForAlphabetizedColumnNames () throws Exception
/**
 *  just like 'testCombine2' above, but matrices are presented in 
 *  reverse order:  is the result the same (i.e., columns are all in alphabetical order?)
 */
{
  System.out.println ("testCombine2CheckForAlphabetizedColumnNames");

  DataMatrixFileReader reader = new DataMatrixFileReader ("file://../../sampleData/simpleMatrix.txt");
  reader.read ();
  org.systemsbiology.gaggle.core.datatypes.DataMatrix m1 = reader.get ();
  reader = new DataMatrixFileReader ("file://../../sampleData/simpleMatrix2.txt");
  reader.read ();
  org.systemsbiology.gaggle.core.datatypes.DataMatrix m2 = reader.get ();

  MatrixCombiner mc = new MatrixCombiner (new org.systemsbiology.gaggle.core.datatypes.DataMatrix[] {m2, m1});
  org.systemsbiology.gaggle.core.datatypes.DataMatrix result = mc.combine ();
  assertTrue (result.getRowCount () == 6);
  assertTrue (result.getColumnCount () == 6);

  String [] expected = new String [] {"cond0", "cond1", "cond2", "cond3", "cond4", "cond5"};
  String [] actual = result.getColumnTitles ();
  assertTrue (Arrays.equals (expected, actual));

  expected = new String [] {"a", "b", "c", "d", "e", "f"};
  actual = result.getRowTitles ();
  assertTrue (Arrays.equals (actual, expected));

  double [] expected2 = new double [] {12.2, 13.8, 4.0, 82.2, 83.8, 84.0};
  double [] actual2 = result.get ("a");
  assertTrue (Arrays.equals (actual, expected));

  double NaN = Double.NaN;
  expected2 = new double [] {-1.2, -8.0, -32.3333, NaN, NaN, NaN};
  actual2 = result.get ("b");
  assertTrue (Arrays.equals (actual2, expected2));

  expected2 = new double [] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
  actual2 = result.get ("c");
  assertTrue (Arrays.equals (actual2, expected2));

  expected2 = new double [] {999.0, 99.0, 9.0, NaN, NaN, NaN};
  actual2 = result.get ("d");
  assertTrue (Arrays.equals (actual2, expected2));

  expected2 = new double [] {NaN, NaN, NaN, 888.0, 88.0, 8.0};
  actual2 = result.get ("e");
  assertTrue (Arrays.equals (actual2, expected2));

  expected2 = new double [] {NaN, NaN, NaN, 8.0, 8.88, 7.888};
  actual2 = result.get ("f");
  assertTrue (Arrays.equals (actual2, expected2));

} // testCombine2CheckForAlphabetizedColumnNames
//------------------------------------------------------------------------------
public void testCombine2WithOneEmpty () throws Exception
/**
 * combine 2 matrices, the second is empty
 * we sometimes need to "combine" a matrix with a missing matrix.
 * for example:  we want to run a movie over a set of specially 
 * selected experimental conditions, from -different- experiments,
 * using log10 ratios to control node color, and lambdas to control node size.
 * this requires that the movie player be able to iterate over two
 * matrices with the same shape.  if one of the experiments has no lambdas
 * then the paired ratio/lambda matrices still need to have the same
 * shape.
 * 
 * here we test the method which creates an empty (that is, zero-row) matrix 
 * from supplied column names.  when combined, these columns are in the resulting
 * matrix; every cell in the column is a NaN, indicating a missing value
 */
{
  System.out.println ("testCombine2WithOneEmpty");
  DataMatrixFileReader reader = new DataMatrixFileReader ("file://../../sampleData/simpleMatrix.txt");
  reader.read ();
  org.systemsbiology.gaggle.core.datatypes.DataMatrix m1 = reader.get ();
  String [] columnTitles = m1.getColumnTitles ();
  String [] expected = new String [] {"cond0", "cond1", "cond2"};
  assertTrue (Arrays.equals (columnTitles, expected));

  reader = new DataMatrixFileReader ("file://../../sampleData/simpleMatrix2.txt");
  reader.read ();
  org.systemsbiology.gaggle.core.datatypes.DataMatrix m2 = reader.get ();
  columnTitles = m2.getColumnTitles ();
  expected = new String [] {"cond3", "cond4", "cond5"};
  assertTrue (Arrays.equals (columnTitles, expected));

  org.systemsbiology.gaggle.core.datatypes.DataMatrix m2Empty = new org.systemsbiology.gaggle.core.datatypes.DataMatrix();
  m2Empty.setSize (0, 4);
  m2Empty.setRowTitlesTitle ("gene");
  m2Empty.setColumnTitles (m2.getColumnTitles ());
  MatrixCombiner mc = new MatrixCombiner (new org.systemsbiology.gaggle.core.datatypes.DataMatrix[] {m1, m2Empty});
  org.systemsbiology.gaggle.core.datatypes.DataMatrix combined = mc.combine ();

  assertTrue (combined.getRowCount () == 4);
  assertTrue (combined.getColumnCount () == 6);

  assertTrue (combined.getRowTitlesTitle ().equals("gene"));
  columnTitles = combined.getColumnTitles ();
  expected = new String [] {"cond0", "cond1", "cond2", "cond3", "cond4", "cond5"};
  assertTrue (Arrays.equals (columnTitles, expected));

  String [] rowTitles = combined.getRowTitles ();
  expected = new String [] {"a", "b", "c", "d"};
  assertTrue (Arrays.equals (rowTitles, expected));

  double NaN = Double.NaN;

  double [] expected2 = new double [] {12.2, 13.8, 4.0, NaN, NaN, NaN};
  double [] actual2 = combined.get (0);
  assertTrue (Arrays.equals (expected2, actual2));
      
  actual2 = combined.get (1);
  expected2 = new double [] {-1.2, -8.0, -32.3333, NaN, NaN, NaN};
  assertTrue (Arrays.equals (expected2, actual2));

  actual2 = combined.get (2);
  expected2 = new double [] {0.0, 0.0, 0.0, NaN, NaN, NaN};
  assertTrue (Arrays.equals (expected2, actual2));

  actual2 = combined.get (3);
  expected2 = new double [] {999.0, 99.0, 9.0, NaN, NaN, NaN};
  assertTrue (Arrays.equals (expected2, actual2));

} // testCombine2WithOneEmpty
//------------------------------------------------------------------------------
public static void main (String [] args) 
{
  junit.textui.TestRunner.run (new TestSuite (MatrixCombinerTest.class));

}// main
//------------------------------------------------------------------------------
} // MatrixCombinerTest
