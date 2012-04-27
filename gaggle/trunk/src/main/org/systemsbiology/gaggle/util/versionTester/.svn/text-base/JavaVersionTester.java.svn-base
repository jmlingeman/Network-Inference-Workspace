// JavaVersionTester.java
// a very simple gui application that reports the current version of java (and of the OS)
//-------------------------------------------------------------------------------------
// $Revision:  $   
// $Date: 2005/04/03 19:15:04 $
//-------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
package org.systemsbiology.gaggle.util.versionTester;
//---------------------------------------------------------------------------------
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import org.systemsbiology.gaggle.util.MiscUtil;
//-------------------------------------------------------------------------------------
public class JavaVersionTester extends JFrame {

  protected JTextArea textArea;

//-------------------------------------------------------------------------------------
public JavaVersionTester ()
{
  add (createGui ());
  setSize (500, 500);
  MiscUtil.placeInCenter (this);
  setVisible(true);

}
//-------------------------------------------------------------------------------------
JPanel createGui ()
{
  JPanel mainPanel = new JPanel ();
  mainPanel.setLayout (new BorderLayout ());
  mainPanel.setBorder (createBorder ());
  setLayout (new BorderLayout ());
  textArea = new JTextArea ();

  String [] propertyNames = getInterestingPropertyNames ();
  for (int i=0; i < propertyNames.length; i++) {
    String name = propertyNames [i];
    if (name.trim().length() == 0)
      textArea.append ("\n");
    else
     textArea.append (name + ": " + System.getProperty (name) + "\n");
    } // for i

  mainPanel.add (textArea, BorderLayout.CENTER);

  JButton quitButton  = new JButton ("Quit");
  JPanel buttonPanel = new JPanel ();
  buttonPanel.add (quitButton);
  mainPanel.add (buttonPanel, BorderLayout.SOUTH);
  quitButton.addActionListener (new ActionListener () {
    public void actionPerformed (ActionEvent e) {
      System.exit (0);
      }});

  return mainPanel;

} // createGui
//-----------------------------------------------------------------------------------
protected String [] getInterestingPropertyNames ()
{
  return new String [] {"java.version", 
                        "os.version",
                        "",
                        "java.class.version", 
                        "java.home", 
                        "java.vendor", 
                        "",
                        "os.arch", 
                        "os.name", 
                        };

}
//-----------------------------------------------------------------------------------
private Border createBorder () 
{
  int right  = 10;
  int left   = 10;
  int top    = 10;
  int bottom = 10;
  return new EmptyBorder (top, left, bottom, right);
}
//-------------------------------------------------------------------------------
public static void main (String [] args) throws Exception
{
  JavaVersionTester tester = new JavaVersionTester ();

} // main
//-------------------------------------------------------------------------------------
} // DiagGoose
