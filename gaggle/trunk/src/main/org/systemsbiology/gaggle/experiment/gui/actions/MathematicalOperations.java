// MathematicalOperations.java
//-----------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui.actions;
//-----------------------------------------------------------------------------------
import org.systemsbiology.gaggle.experiment.datamatrix.*;
import org.systemsbiology.gaggle.experiment.gui.*;

import javax.swing.*;
import java.awt.event.*;

import java.io.*;
//-----------------------------------------------------------------------------------
public class MathematicalOperations extends AbstractAction {

  protected MatrixSpreadsheet parentSpreadsheet;
  protected String opName;
  protected int averageCount = 0;
  protected int andCount = 0;
  protected int orCount = 0;

//-----------------------------------------------------------------------------------
public MathematicalOperations (MatrixSpreadsheet parent, String opName)
{ 
  this.parentSpreadsheet = parent;
  this.opName = opName;
}
//-----------------------------------------------------------------------------------
public void actionPerformed (ActionEvent e) 
{

  if (parentSpreadsheet.getLens().getColumnCount () < 
      parentSpreadsheet.getLens().getUnderlyingMatrix().getColumnCount ()) {
    String msg = "Cannot perform this operation on a matrix with disabled columns.";
    JOptionPane.showMessageDialog (parentSpreadsheet, msg);
    return;
    }

  if (opName.equalsIgnoreCase ("and"))
    doLogicalAnd ();
  else if (opName.equalsIgnoreCase ("or"))
    doLogicalOr ();
  else if (opName.equalsIgnoreCase ("avg"))
    doAverage ();

} // actionPerformed
//---------------------------------------------------------------------------------
protected void doLogicalAnd ()
{
  LensedDataMatrix lens = parentSpreadsheet.getLens ();
  int selectedRowCount = lens.getRowCount ();

  if (selectedRowCount != 2) {
    String msg = "Two rows must be selected.";
    JOptionPane.showMessageDialog (parentSpreadsheet, msg);
    return;
    } // if no selections

  double [] row1 = lens.get (0);
  double [] row2 = lens.get (1);
  int max = row1.length;

  andCount++;
  String newRowTitle = "and-" + andCount;

  double [] newRowValues = new double [max];

  for (int i=0; i < row1.length; i++) {
    double x1 = row1 [i];
    double x2 = row2 [i];
    newRowValues [i] = x1;
    if (x2 < x1)
      newRowValues [i] = x2;
    }
  
  parentSpreadsheet.addRow (newRowTitle, newRowValues);

} // doLogicalAnd
//---------------------------------------------------------------------------------
protected void doLogicalOr ()
{
  LensedDataMatrix lens = parentSpreadsheet.getLens ();
  int selectedRowCount = lens.getRowCount ();

  if (selectedRowCount != 2) {
     String msg = "Two rows must be selected.";
     JOptionPane.showMessageDialog (parentSpreadsheet, msg);
     return;
     } // if no selections

  double [] row1 = lens.get (0);
  double [] row2 = lens.get (1);
  int max = row1.length;

  orCount++;
  String newRowTitle = "or-" + orCount;

  double [] newRowValues = new double [max];

  for (int i=0; i < row1.length; i++) {
    double x1 = row1 [i];
    double x2 = row2 [i];
    newRowValues [i] = x1;
    if (x2 > x1)
      newRowValues [i] = x2;
    }
  
  parentSpreadsheet.addRow (newRowTitle, newRowValues);

} // doLogicalOr
//---------------------------------------------------------------------------------
protected void doAverage ()
{
  LensedDataMatrix lens = parentSpreadsheet.getLens ();
  int selectedRowCount = lens.getRowCount ();

  if (selectedRowCount < 2) {
     String msg = "Two or more rows must be selected.";
     JOptionPane.showMessageDialog (parentSpreadsheet, msg);
     return;
     } // if no selections

  int max = lens.getColumnCount ();
  averageCount++;
  String newRowTitle = "avg-" + averageCount;

  double [] average = new double [max];
  for (int i=0; i < max; i++)
    average [i] = 0.0;

  int rowCount = lens.getRowCount ();
  for (int r=0; r < rowCount; r++) {
    for (int c=0; c < max; c++)
      average [c] += lens.get (r,c);
    } // for r

  for (int c=0; c < max; c++)
    average [c] = average [c] / rowCount;

  
  parentSpreadsheet.addRow (newRowTitle, average);

} // doAverage
//---------------------------------------------------------------------------------
} // class MathematicalOperations
