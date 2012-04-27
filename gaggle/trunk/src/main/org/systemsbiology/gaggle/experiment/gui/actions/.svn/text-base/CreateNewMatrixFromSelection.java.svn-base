// CreateNewMatrixFromSelection
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
//-----------------------------------------------------------------------------------
public class CreateNewMatrixFromSelection extends AbstractAction {

  protected MatrixSpreadsheet parent;
  protected MatrixViewCoordinator mvc;

//-----------------------------------------------------------------------------------
public CreateNewMatrixFromSelection (MatrixSpreadsheet parent, MatrixViewCoordinator mvc) 
{
  super ("",IconFactory.getCreateFromSelectionIcon());
  this.putValue (Action.SHORT_DESCRIPTION, "Create New Matrix From Selection");
  this.parent = parent;
  this.mvc = mvc;
}
//-----------------------------------------------------------------------------------
public void actionPerformed (ActionEvent e) {
  LensedDataMatrix lens = parent.getLens ();
  if (lens.getRowCount () == 0) {
    JOptionPane.showMessageDialog (parent, "No rows are selected!",
                                   "Selection Error",  JOptionPane.ERROR_MESSAGE);
    return;
    }
  String newMatrixName = JOptionPane.showInputDialog (parent, "Please enter a name for this matrix:");
  if (null == newMatrixName)
    return;

  try {
    org.systemsbiology.gaggle.core.datatypes.DataMatrix subMatrix = lens.getVisibleMatrix ();
    subMatrix.setShortName (newMatrixName);
    if (mvc != null) 
      mvc.addMatrixSpreadsheetView (subMatrix, parent.getMetaDataNavigator());
    } 
  catch (Exception e0) {
    e0.printStackTrace();
    }

} // actionPerformed
//-----------------------------------------------------------------------------------
} // class CreateNewMatrixFromSelection
