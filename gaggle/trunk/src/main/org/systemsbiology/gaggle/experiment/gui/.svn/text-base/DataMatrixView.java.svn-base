// DataMatrixView.java
//------------------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui;

import java.io.File;

public interface DataMatrixView {
  public String getClassName ();
  public String getName ();
  public void select (String species, String [] names);
  public String [] getSelection ();
  public org.systemsbiology.gaggle.core.datatypes.DataMatrix getSelectedMatrix ();
  public void clearSelection ();
  public String getSpecies ();
  public void save (File directory);
}
