// FlyPlugin.java
//------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.cy.fly;
//------------------------------------------------------------------------------
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

import cytoscape.*;
import y.base.*;
import y.view.*;

import java.util.*;

//------------------------------------------------------------------------------
public class FlyPlugin extends AbstractPlugin {

  transient CytoscapeWindow cw;

//------------------------------------------------------------------------------
public FlyPlugin (CytoscapeWindow cytoscapeWindow) 
{
  cw = cytoscapeWindow;
  View view = cw.getGraph().getCurrentView();

  if (view instanceof Graph2DView) {
    Graph2DView g2dv = (Graph2DView) view;
    System.out.println ("setting paint threshold...");
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
} // FlyPlugin
