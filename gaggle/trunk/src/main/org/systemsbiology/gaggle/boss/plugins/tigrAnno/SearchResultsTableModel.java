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

package org.systemsbiology.gaggle.boss.plugins.tigrAnno;
//-------------------------------------------------------------------------------
import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.util.ArrayList;
//-------------------------------------------------------------------------------
class SearchResultsTableModel extends AbstractTableModel {

  private String [] columnNames = {"NAME", "Gene Symbol", "Function" };
  private ArrayList data;

//-------------------------------------------------------------------------------
public SearchResultsTableModel ()
{
  data = new ArrayList ();
}
//-------------------------------------------------------------------------------
public void clearData ()
{
   data = new ArrayList ();
}
//-------------------------------------------------------------------------------
public void addSearchResult (String name, String geneSymbol, String geneFunction)
{
  data.add (new String [] {name, geneSymbol, geneFunction});
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
public Object getValueAt (int row, int col)
{
  try {
    int lastIndex = data.size () - 1;
    if (row > lastIndex)
      return null;
  
    String [] rowContents = (String []) data.get (row);
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
