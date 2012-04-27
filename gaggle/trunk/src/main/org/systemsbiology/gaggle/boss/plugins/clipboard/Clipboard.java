// Clipboard.java
//---------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.boss.plugins.clipboard;
//---------------------------------------------------------------------------------------
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;


import java.rmi.*;

import org.systemsbiology.gaggle.boss.GuiBoss;
import org.systemsbiology.gaggle.boss.plugins.GaggleBossPlugin;
import org.systemsbiology.gaggle.core.datatypes.Namelist;

//---------------------------------------------------------------------------------------
public class Clipboard extends GaggleBossPlugin {

  String name = "Clipboard";
  protected JScrollPane scrollPane;
  protected JTextArea textArea;
  protected GuiBoss gaggleBoss;
  protected String species = "unknown";
  
//---------------------------------------------------------------------------------------
public Clipboard (GuiBoss boss)
{
  super ("Clipboard");
  gaggleBoss = boss;
  createGui ();

} // ctor
//---------------------------------------------------------------------------------------
protected void createGui ()
{
  setLayout (new BorderLayout ());
  JPanel innerPanel = new JPanel ();
  innerPanel.setLayout (new BorderLayout ());
  JPanel searchPanel = new JPanel ();
  
  textArea = new JTextArea ();
  JScrollPane scrollPane = new JScrollPane (textArea);
  scrollPane.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
  scrollPane.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
  innerPanel.setBorder (createBorder ());
  innerPanel.add (scrollPane, BorderLayout.CENTER);

  JPanel broadcastButtonPanel = new JPanel ();

  JButton clearButton = new JButton ("Clear");
  clearButton.addActionListener (new ActionListener () {
    public void actionPerformed (ActionEvent e) {
      textArea.setText ("");
      }});

  JButton broadcastButton = new JButton ("Broadcast");
  broadcastButton.addActionListener (new ActionListener () {
    public void actionPerformed (ActionEvent e) {
      broadcast ();
      }});



  broadcastButtonPanel.add (clearButton);
  broadcastButtonPanel.add (broadcastButton);

  innerPanel.add (broadcastButtonPanel, BorderLayout.SOUTH);
  add (innerPanel);
  ToolTipManager.sharedInstance().setInitialDelay (0);

} // createSearchPanel
//-------------------------------------------------------------------------------
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
  String s = textArea.getText ();
  s =  s.replaceAll ("\\n", " ");
  String [] tokens = s.split ("\\s+");
  Namelist nameList = new Namelist();
  nameList.setSpecies(species);
  nameList.setNames(tokens);
  try {
    gaggleBoss.broadcastNamelist(name, "all", nameList);
    }
  catch (RemoteException rex) {
    rex.printStackTrace ();
    }

  //System.out.println ("number of tokens: " + tokens.length);
  //for (int i=0; i < tokens.length; i++)
  //  System.out.println ("    --> " + tokens [i]);
  
}
//-------------------------------------------------------------------------------
public void select (String [] names)
{
  // System.out.println ("SearchPanel.select: " + names.length);
  StringBuffer sb = new StringBuffer ();
  //sb.append (searchBox.getText());
  for (int i=0; i < names.length; i++) {
    if (sb.length () > 0) 
      sb.append ("\n");
    sb.append (names [i]);
    }

 textArea.setText (sb.toString ());  

} // select
//---------------------------------------------------------------------------------------
} // class Clipboard
