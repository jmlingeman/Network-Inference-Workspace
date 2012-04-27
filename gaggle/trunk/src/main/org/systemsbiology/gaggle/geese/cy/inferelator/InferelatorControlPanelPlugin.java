// InferelatorControlPanelPlugin.java
//------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.cy.inferelator;
//------------------------------------------------------------------------------
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

import cytoscape.*;
import y.base.*;
import y.view.*;

import java.util.*;

//------------------------------------------------------------------------------
public class InferelatorControlPanelPlugin extends AbstractPlugin {

  transient CytoscapeWindow cw;

//------------------------------------------------------------------------------
public InferelatorControlPanelPlugin (CytoscapeWindow cytoscapeWindow) 
{
  cw = cytoscapeWindow;
  JPanel utilityPanel = cw.getUtilityPanel ();
  utilityPanel.setBorder (BorderFactory.createCompoundBorder (
                             BorderFactory.createRaisedBevelBorder (),
                             BorderFactory.createLoweredBevelBorder ()));

  utilityPanel.add (new ControlPanel (cw));
  cw.getMainFrame().pack ();
  org.systemsbiology.gaggle.util.MiscUtil.placeInCenter (cw.getMainFrame ());

}
//----------------------------------------------------------------------------------------
} // InferelatorControlPanel
