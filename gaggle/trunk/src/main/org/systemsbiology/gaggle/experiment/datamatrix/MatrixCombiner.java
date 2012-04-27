// MatrixCombiner.java
//---------------------------------------------------------------------------------------
// $Revision: 2360 $
// $Date: 2007-09-04 18:02:00 -0400 (Tue, 04 Sep 2007) $
// $Author: dtenenba $
//---------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.datamatrix;
//---------------------------------------------------------------------------------------
import org.systemsbiology.gaggle.core.datatypes.DataMatrix;

import java.util.*;

//---------------------------------------------------------------------------------------
/** 
 *
 */
//---------------------------------------------------------------------------------------
public class MatrixCombiner {

  DataMatrix [] matrices;
  String [] newColumnTitles;
  String []  newRowTitles;
  HashMap columnTitleHash;
  HashMap rowTitleHash;

//---------------------------------------------------------------------------------------
public MatrixCombiner (DataMatrix [] matrices)
{
  this.matrices = matrices;

  newColumnTitles = createNonRedundantTitleList ("column");
  newRowTitles = createNonRedundantTitleList ("row");
  columnTitleHash = createTitleHash (newColumnTitles);
  rowTitleHash = createTitleHash (newRowTitles);

} // ctor
//---------------------------------------------------------------------------------------
public DataMatrix combine ()
{
  DataMatrix result = new DataMatrix ();
  result.setSize (newRowTitles.length, newColumnTitles.length);
  result.setRowTitles (newRowTitles);
  result.setRowTitlesTitle (matrices [0].getRowTitlesTitle ());
  result.setColumnTitles (newColumnTitles);
  result.setDefault (Double.NaN);
  result.setSpecies (matrices [0].getSpecies ());
  
  for (int i=0; i < matrices.length; i++) {
    DataMatrix matrix = matrices [i];
    double [][] data = matrix.get ();
    for (int row=0; row < matrix.getRowCount (); row++)
     for (int column=0; column < matrix.getColumnCount (); column++) {
       int [] indices = newIndices (matrix, row, column);
       double value = data [row][column];
       result.set (indices [0], indices [1], value);
       } // for column
     } // for i

  return result;
}  
//---------------------------------------------------------------------------------------
protected int [] newIndices (org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix, int row, int column)
{
  String originalMatrixRowName = matrix.getRowTitles ()[row];
  String originalMatrixColumnName = matrix.getColumnTitles ()[column];
  int targetMatrixRow = ((Integer) rowTitleHash.get (originalMatrixRowName)).intValue ();
  int targetMatrixColumn = ((Integer) columnTitleHash.get (originalMatrixColumnName)).intValue ();
  
  return new int [] {targetMatrixRow, targetMatrixColumn};

} // newIndices
//---------------------------------------------------------------------------------------
protected String [] createNonRedundantTitleList (String titleType)
{
  //assert (titleType.equals ("row") ||  titleType.equals ("column"));
  // ArrayList list = new ArrayList ();
  HashSet list = new HashSet ();

  for (int i=0;i < matrices.length; i++) {
    DataMatrix matrix = matrices [i];
    String [] titles;
    if (titleType.equals ("row")) {
      titles = matrix.getRowTitles ();
      }
    else
      titles = matrix.getColumnTitles ();
    for (int t=0; t < titles.length; t++) {
      String title = titles [t];
      //if (!list.contains (title))
      list.add (title);
      }
    } // for i


  String [] result = (String []) list.toArray (new String [0]);
  Arrays.sort (result);
  return result;

} // createNonRedundantTitleList
//---------------------------------------------------------------------------------------
public HashMap createTitleHash (String [] titleList)
{
  HashMap result = new HashMap ();
  for (int i=0; i < titleList.length; i++)
    result.put (titleList [i], new Integer (i));

  return result;
}
//---------------------------------------------------------------------------------------
protected DataMatrix createEmptyMatrix (String [] columnTitles)
//
//   when combining pairs of views (say: ratios and lambdas from two experiments)
//   we may sometime lack either the ratios or the lambas for one of the experiments.
//   but it will be handy to create a combined matrix (for the lambdas, say) which
//   has the same shape as the combined ratios matrix.  
//   this method creates a matrix with the right number of columns, but with no rows.  
//   
//   at combining time, this zero-row matrix will contribute its full complement
//   of "missing" values to the result, because the "combine" method above writes
//   java.lang.Double.NaN into every missing cell.
{
  DataMatrix m = new DataMatrix ();
  m.setSize (0, columnTitles.length);
  m.setColumnTitles (columnTitles);

  return m;

}
//---------------------------------------------------------------------------------------
} // class MatrixCombiner


