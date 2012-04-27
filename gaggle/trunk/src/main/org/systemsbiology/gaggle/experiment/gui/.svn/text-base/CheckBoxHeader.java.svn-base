// CheckBoxHeader.java
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
import java.awt.event.ItemListener;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

//---------------------------------------------------------------------------------------
/**
 * provides the gui component (a checkbox with a label) for drawing
 * table headers
 */
public class CheckBoxHeader extends JCheckBox implements TableCellRenderer {

  protected int column;

//------------------------------------------------------------------------------------------
public CheckBoxHeader (ItemListener itemListener) 
{
  addItemListener (itemListener);
  setSelected (true);
}
//------------------------------------------------------------------------------------------
public Component getTableCellRendererComponent (JTable table, Object value,
                                                boolean isSelected, boolean hasFocus, 
                                                int row, int column) 
{
    return this;
}
//------------------------------------------------------------------------------------------
public void fireStateChanged ()
{
  super.fireStateChanged ();
}
//------------------------------------------------------------------------------------------
protected void setColumn (int column) 
{
  this.column = column;
}
//------------------------------------------------------------------------------------------
public int getColumn () 
{
  return column;
}
//------------------------------------------------------------------------------------------
public String toString ()
{
  return "CheckBoxHeader for column " + column;
}
//------------------------------------------------------------------------------------------
} // class CheckBoxHeader

