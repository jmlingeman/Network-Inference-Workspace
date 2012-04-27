// MatrixViewCoordinatory.java
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

import org.systemsbiology.gaggle.experiment.datamatrix.LensedDataMatrix;
import org.systemsbiology.gaggle.experiment.metadata.MetaDataNavigator;

public interface MatrixViewCoordinator  {
  public void doPlot (LensedDataMatrix lens);
  public void doVolcanoPlot (org.systemsbiology.gaggle.core.datatypes.DataMatrix topMostMatrix, String columnName, int companionMatrixID);
  public void addMatrixSpreadsheetView (org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix, MetaDataNavigator navigator);

}

