// ProlinksControlPanelPlugin.java
//------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.cy.prolinks;
//------------------------------------------------------------------------------
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

import cytoscape.*;
import y.base.*;
import y.view.*;

import java.util.*;

//------------------------------------------------------------------------------
public class ProlinksControlPanelPlugin extends AbstractPlugin {

  transient CytoscapeWindow cw;

//------------------------------------------------------------------------------
public ProlinksControlPanelPlugin (CytoscapeWindow cytoscapeWindow) 
{
  cw = cytoscapeWindow;
  View view = cytoscapeWindow.getGraph().getCurrentView();
  if (view instanceof Graph2DView) {
    Graph2DView g2dv = (Graph2DView) view;
    g2dv.setPaintDetailThreshold(0.0);      // You only need to do this once
    }

  JPanel utilityPanel = cw.getUtilityPanel ();
  utilityPanel.setBorder (BorderFactory.createCompoundBorder (
                             BorderFactory.createRaisedBevelBorder (),
                             BorderFactory.createLoweredBevelBorder ()));

  utilityPanel.add (new ControlPanel (cw));
  cw.getMainFrame().pack ();
  org.systemsbiology.gaggle.util.MiscUtil.placeInCenter (cw.getMainFrame ());

}
//----------------------------------------------------------------------------------------
} // ProlinksControlPanel
