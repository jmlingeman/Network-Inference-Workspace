// GaggleBossPlugin.java
//-------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.boss.plugins;
//-------------------------------------------------------------------------------------
import javax.swing.JPanel;
//-------------------------------------------------------------------------------------
public abstract class GaggleBossPlugin extends JPanel {

  protected String name;

//---------------------------------------------------------------------------------
public GaggleBossPlugin (String name) 
{
  this.name = name;
}
//---------------------------------------------------------------------------------
public String getName ()
{
  return name;
}
//---------------------------------------------------------------------------------
abstract public void select (String [] names);
//---------------------------------------------------------------------------------
} // GaggleBossPlugin
