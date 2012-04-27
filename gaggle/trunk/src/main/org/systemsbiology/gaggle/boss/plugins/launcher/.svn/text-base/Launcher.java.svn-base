// Launcher.java
// start many geese at once, without a web browser
//---------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.boss.plugins.launcher;
//---------------------------------------------------------------------------------------
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.*;


import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;
import java.io.*;
import java.text.*;
import java.net.*;

import org.systemsbiology.gaggle.boss.GuiBoss;
import org.systemsbiology.gaggle.util.*;
import org.systemsbiology.gaggle.boss.plugins.GaggleBossPlugin;
//---------------------------------------------------------------------------------------
public class Launcher extends GaggleBossPlugin {

  String name = "Launcher";
  protected JScrollPane scrollPane;
  protected GuiBoss gaggleBoss;
  protected String species = "unknown";
  protected String uri;
  
//---------------------------------------------------------------------------------------
public Launcher (GuiBoss boss)
{
  super ("Launcher");
  gaggleBoss = boss;
  uri = boss.getConfig().getProperties().getProperty ("uri");
  try {
    System.out.println ("uri: " + uri);
    URL url = new URL (uri);
    add (new WebPage (url));
    }
  catch (Exception ex0) {
    System.err.println ("exception creating web page: " + ex0.getMessage ());
    ex0.printStackTrace ();
    }

} // ctor
//---------------------------------------------------------------------------------------
private Border createBorder () 
{
  int right  = 10;
  int left   = 10;
  int top    = 10;
  int bottom = 10;
   return new EmptyBorder (top, left, bottom, right);
}
//-------------------------------------------------------------------------------
protected void broadcast ()
{
  
}
//-------------------------------------------------------------------------------
public void select (String [] names)
{

} // select
//---------------------------------------------------------------------------------------
protected void javaWebStart (String uri)
// try to invoke javaws as if from the command line, bypassing the browser completely
{
  //String javaxHome =  System.getProperty ("javax.home");
  //System.out
  //Runtime rt = Runtime.getRuntime ();
}
//---------------------------------------------------------------------------------------
} // class Launcher
