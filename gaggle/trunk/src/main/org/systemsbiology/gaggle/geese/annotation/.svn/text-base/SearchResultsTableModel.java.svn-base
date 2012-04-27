// SearchResultsTableModel.java
//-------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.annotation;
//-------------------------------------------------------------------------------
import javax.swing.table.*;
import java.util.ArrayList;
//-------------------------------------------------------------------------------
class SearchResultsTableModel extends AbstractTableModel {

  private String [] columnNames = {};
  private ArrayList<String[]> data;

//-------------------------------------------------------------------------------
public SearchResultsTableModel ()
{
  data = new ArrayList<String[]>();
}
//-------------------------------------------------------------------------------
public void clearData ()
{
   data = new ArrayList<String[]>();
}
//-------------------------------------------------------------------------------
public void addSearchResult (String name, String geneSymbol, String geneFunction)
{
  data.add (new String [] {name, geneSymbol, geneFunction});
}
//-------------------------------------------------------------------------------
public void addSearchResult (String[] row)
{
  data.add (row);
}
//-------------------------------------------------------------------------------
public int getColumnCount ()
{
  return columnNames.length;
}
//-------------------------------------------------------------------------------
public int getRowCount ()
{
 return (data.size ());
}
//-------------------------------------------------------------------------------
public String getColumnName (int col)
{
  return columnNames [col];
}
//-------------------------------------------------------------------------------
public void setColumnNames(String[] columnNames)
{
  // we should copy
  if (columnNames != null)
    this.columnNames = columnNames;
  else
    this.columnNames = new String[] {"NAME", "Gene Symbol", "Function" };
}
//-------------------------------------------------------------------------------
public Object getValueAt (int row, int col)
{
  try {
    int lastIndex = data.size () - 1;
    if (row > lastIndex)
      return null;
  
    String [] rowContents = data.get (row);
    if (rowContents == null)
      return null;
  
    lastIndex = rowContents.length - 1;
    if (col > lastIndex)
      return null;
   
    return rowContents [col];
    }
  catch (Exception ex0) {
    return null;
    }

} // getValueAt
//-------------------------------------------------------------------------------
//public void setValueAt (Object value, int row, int col)
//{
//     self.data [row][col] = value
//}
//-------------------------------------------------------------------------------
public boolean isCellEditable (int row, int col)
{
  return false;
}
//-------------------------------------------------------------------------------
} // SearchResultsTableModle
