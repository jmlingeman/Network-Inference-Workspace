// DataMatrixTableModel.java
//---------------------------------------------------------------------------------------
// $Revision: 2360 $
// $Date: 2005/02/19 01:55:55 $
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

package org.systemsbiology.gaggle.experiment.gui;
//---------------------------------------------------------------------------------------
import javax.swing.table.*;

//---------------------------------------------------------------------------------------
public class DataMatrixTableModel extends AbstractTableModel {

  String [] columnNames;
  int[] savedRowIndices;
  int[] newRowIndices;

  Object [][] data;
  int [] columnWidths = {40};  // missing values, which are possible only at
                               // the end of array, mean that default column widths
                               // will be used

  protected final int defaultColumnWidth = 100;
  protected int preferredTableWidth = defaultColumnWidth; // incremented below

//---------------------------------------------------------------------------------------
public DataMatrixTableModel (org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix)
{
  int numberOfColumns = matrix.getColumnCount ();
  int numberOfRows = matrix.getRowCount ();
  columnNames = new String [numberOfColumns];

  for (int i=0; i < columnNames.length; i++)
    columnNames [i] = matrix.getColumnTitles() [i];

  data = new Object [numberOfRows][numberOfColumns];

  String [] rowNames = matrix.getRowTitles ();

  savedRowIndices = new int[numberOfRows];
  newRowIndices = new int[numberOfRows];
  
  for (int r=0; r < numberOfRows; r++) {
  	savedRowIndices[r] = r;
  	newRowIndices[r] = r;
    for (int c=0; c < numberOfColumns; c++) {
      double d = matrix.get (r, c);
      data [r][c] = new Double (d);
      } // for c
    } // for r


} // ctor
//---------------------------------------------------------------------
public void setNewRowIndices (int [] newRowIndices) {this.newRowIndices = newRowIndices;}
//---------------------------------------------------------------------
public String getColumnName (int col) {return columnNames[col];}
//---------------------------------------------------------------------
public int getColumnCount () {return columnNames.length;}
//---------------------------------------------------------------------
public int getCorrectRowIndex (int index) {return savedRowIndices [newRowIndices [index]];}
//---------------------------------------------------------------------
public int [] getCorrectRowIndices (int [] indices) 
{
  int [] correctIndices = new int [indices.length];
  for (int i = 0; i < correctIndices.length; i++) {
        //correctIndices[i] = newRowIndices[indices[i]];
        // TODO - test this
    correctIndices[i] = savedRowIndices[newRowIndices[indices[i]]];
    }
    
  return correctIndices;
}
//---------------------------------------------------------------------
public int getRowCount () { return data.length; }
//---------------------------------------------------------------------
public boolean isCellEditable (int row, int col) {return false;}

//---------------------------------------------------------------------
public Object getValueAt (int row, int col) {
 return data [newRowIndices [row]][col];
 }
//---------------------------------------------------------------------
public int getPreferredColumnWidth (int col) 
// '0' means: there is no preferred width, use the default
//  the columnWidths array can be incomplete. so if, for example,
//  only the first column has a specified width, then the array
//  need only contain one value.
{ 
  if (col >= columnWidths.length)
    return 0;
  else
    return columnWidths [col];
}
//--------------------------------------------------------------------------------------
public Class getColumnClass (int column) 
// though i do not understand the circumstances in which this method
// is called, trial and error has led me to see that -some- class
// must be returned, and that if the 0th row in the specified column
// is null, then returning the String Class seems to work okay.
{
   Object cellValue = getValueAt (0, column);
   if (cellValue == null) { 
     String s = new String ();
     return s.getClass ();
     }
   else
     return getValueAt (0, column).getClass();

} // getColumnClass
//--------------------------------------------------------------------------------------
public void addRow (String rowLabel, double [] values)
{
  if (values.length != getColumnCount ())
    throw new IllegalArgumentException ("can only add row of length " + getColumnCount () +
                                        "; you added " + values.length);

  Object [][] newData = new Object [getRowCount() + 1][getColumnCount ()];
  for (int r=0; r < getRowCount (); r++)
    newData [r] = data [r];

  Double [] newValues = new Double [values.length];
  for (int i=0; i < values.length; i++)
    newValues [i] = new Double (values [i]);

  newData [getRowCount ()] = newValues;
  data = newData;

  int oldSize = savedRowIndices.length;
  int [] tmpIndices = new int [oldSize+1];
  for (int i=0; i < oldSize; i++) 
    tmpIndices [i] = savedRowIndices [i];
  tmpIndices [oldSize] = oldSize;
  savedRowIndices = tmpIndices;

  oldSize = newRowIndices.length;
  tmpIndices = new int [oldSize+1];
  for (int i=0; i < oldSize; i++) 
    tmpIndices [i] = newRowIndices [i];
  tmpIndices [oldSize] = oldSize;
  newRowIndices = tmpIndices;

  fireTableStructureChanged ();  // in superclass
  
} // addRow
//--------------------------------------------------------------------------------------
} // class DataMatrixTableModel


