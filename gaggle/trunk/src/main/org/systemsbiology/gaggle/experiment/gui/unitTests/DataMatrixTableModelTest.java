// DataMatrixTableModelTest.java
//------------------------------------------------------------------------------
// $Revision: 2360 $
// $Date: 2005/02/17 17:09:05 $
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

package org.systemsbiology.gaggle.experiment.gui.unitTests;
//--------------------------------------------------------------------------------------
import junit.framework.*;

import java.util.*;

import org.systemsbiology.gaggle.experiment.gui.DataMatrixTableModel;
import org.systemsbiology.gaggle.experiment.readers.*;

//------------------------------------------------------------------------------
public class DataMatrixTableModelTest extends TestCase {

//------------------------------------------------------------------------------
public DataMatrixTableModelTest (String name) 
{
  super (name);
}
//------------------------------------------------------------------------------
public void testCtor () throws Exception
{
  System.out.println ("testCtor");
  DataMatrixReader reader = 
      new DataMatrixFileReader ("file://" + "../../sampleData/simpleMatrix.txt");
  reader.read ();
  org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix = reader.get ();
  DataMatrixTableModel model = new DataMatrixTableModel (matrix);
  assertTrue (model.getRowCount () == 4);
  assertTrue (model.getColumnCount () == 3);
 
}  // testCtor
//-------------------------------------------------------------------------
public void testRearrangeRows () throws Exception
{
  System.out.println ("testRearrangeRows");
  DataMatrixReader reader = 
      new DataMatrixFileReader ("file://" + "../../sampleData/simpleMatrix.txt");
  reader.read ();
  org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix = reader.get ();
  DataMatrixTableModel model = new DataMatrixTableModel (matrix);
  assertTrue (model.getRowCount () == 4);
  assertTrue (model.getColumnCount () == 3);
  int [] rowIndices = new int [] {0,1,2,3};
  int [] translatedRowIndices = model.getCorrectRowIndices (rowIndices);
  assertTrue (Arrays.equals (rowIndices, translatedRowIndices));

  int [] newArrangement = new int [] {2,3,0,1};
  model.setNewRowIndices (newArrangement);
  assertTrue (model.getCorrectRowIndex (0) == 2);
  assertTrue (model.getCorrectRowIndex (1) == 3);
  assertTrue (model.getCorrectRowIndex (2) == 0);
  assertTrue (model.getCorrectRowIndex (3) == 1);

  assertTrue (Arrays.equals (model.getCorrectRowIndices (rowIndices), newArrangement));
 
}  // testRearrangeRows
//-------------------------------------------------------------------------
public void testAddRow () throws Exception
{
  System.out.println ("testAddRow");
  DataMatrixReader reader = 
      new DataMatrixFileReader ("file://" + "../../sampleData/simpleMatrix.txt");
  reader.read ();
  org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix = reader.get ();
  DataMatrixTableModel model = new DataMatrixTableModel (matrix);
  assertTrue (model.getRowCount () == 4);
  assertTrue (model.getColumnCount () == 3);

  double [] newRow = new double [] {32.23, 45.54, 54.45};
  model.addRow ("new row", newRow);
  assertTrue (model.getRowCount () == 5);
  assertTrue (model.getColumnCount () == 3);
 
}  // testAddRow
//-------------------------------------------------------------------------
public void testAddRowAndRearrangeRows () throws Exception
{
  System.out.println ("testAddRowAndRearrangeRows");
  DataMatrixReader reader = 
      new DataMatrixFileReader ("file://" + "../../sampleData/simpleMatrix.txt");
  reader.read ();
  org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix = reader.get ();
  DataMatrixTableModel model = new DataMatrixTableModel (matrix);
  assertTrue (model.getRowCount () == 4);
  assertTrue (model.getColumnCount () == 3);

  double [] newRow = new double [] {32.23, 45.54, 54.45};
  model.addRow ("new row", newRow);
  assertTrue (model.getRowCount () == 5);
  assertTrue (model.getColumnCount () == 3);

  int [] originalRowIndices = new int [] {0, 1, 2, 3, 4};
  int [] correctedRowIndices = model.getCorrectRowIndices (originalRowIndices);
  assertTrue (Arrays.equals (originalRowIndices, correctedRowIndices));

  int [] newArrangement = new int [] {4, 3, 2, 1, 0};
  model.setNewRowIndices (newArrangement);
  assertTrue (model.getCorrectRowIndex (0) == 4);
  assertTrue (model.getCorrectRowIndex (1) == 3);
  assertTrue (model.getCorrectRowIndex (2) == 2);
  assertTrue (model.getCorrectRowIndex (3) == 1);
  assertTrue (model.getCorrectRowIndex (4) == 0);
 
}  // testAddRowAndRearrangeRows
//-------------------------------------------------------------------------
public static void main (String [] args) 
{
  junit.textui.TestRunner.run (new TestSuite (DataMatrixTableModelTest.class));
  System.exit (0);

}// main
//------------------------------------------------------------------------------
} // DataMatrixTableModelTest
