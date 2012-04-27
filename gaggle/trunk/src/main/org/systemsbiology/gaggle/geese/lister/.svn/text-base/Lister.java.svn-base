// Lister.java
// a simple jlist with two buttons: select all, clear 
//-----------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.lister;
//---------------------------------------------------------------------------------
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.*;
//-----------------------------------------------------------------------------------
public class Lister extends JFrame {
  String [] listData = {"VNG0040C",
                        "VNG0044H",
                        "VNG0050C",
                        "VNG0156C",
                        "VNG0194H",
                        "VNG0247C",
                        "VNG0254G",
                        "VNG0258H",
                        "VNG0293H",
                        "VNG0296H",
                        "VNG0320H"};
  JList listbox;
  ListSelectionModel lsm;
  JToolBar toolbar;
  ArrayList currentSelection;
//-----------------------------------------------------------------------------------
public Lister ()
{
  super ("Lister");
  addWindowListener (new WindowAdapter () {
    public void windowClosing (WindowEvent e) {
      //System.out.println ("Lister detects its own window closing");
      System.exit (0);}}
    );
  getContentPane().add (createGui ());
  setSize (300, 300);
  setVisible(true);

} // ctor
//-----------------------------------------------------------------------------------
JPanel createGui ()
{
  JPanel outerPanel = new JPanel ();
  outerPanel.setBorder (BorderFactory.createEmptyBorder (10, 10, 10, 10));
  outerPanel.setLayout (new BorderLayout ());
  toolbar = createToolBar ();
  outerPanel.add (toolbar, BorderLayout.NORTH);

  java.util.Arrays.sort (listData, String.CASE_INSENSITIVE_ORDER);
  DefaultListModel model = new DefaultListModel ();
  for (int i=0; i < listData.length; i++)
    model.add (i, listData [i]);

  //listbox = new JList (listData);
  listbox = new JList (model);
  
  listbox.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

  JScrollPane scrollPanel = new JScrollPane (listbox);
  outerPanel.add (scrollPanel, BorderLayout.CENTER);

  JPanel actionButtonPanel = new JPanel ();

  JButton clearButton = new JButton ("Clear");
  JButton removeAllButton = new JButton ("Remove All");
  JButton selectAllButton= new JButton ("Select All");
  JButton describeButton = new JButton ("Describe");

  actionButtonPanel.add (clearButton);
  actionButtonPanel.add (removeAllButton);
  actionButtonPanel.add (selectAllButton);
  actionButtonPanel.add (describeButton);

  clearButton.addActionListener (new ClearSelectionAction());
  selectAllButton.addActionListener (new SelectAllAction());
  describeButton.addActionListener (new DescribeAction());

  removeAllButton.addActionListener (new ActionListener () {
    public void actionPerformed (ActionEvent e) {
      removeAll ();
      }});

  outerPanel.add (actionButtonPanel, BorderLayout.SOUTH);
  return outerPanel;

} // createGui
//-----------------------------------------------------------------------------------
protected JToolBar createToolBar ()
{
  JToolBar bar = new JToolBar ();
  bar.add (new ExitAction ());
  return bar;

} // createToolBar
//------------------------------------------------------------------------------
class ExitAction extends AbstractAction {
  ExitAction () {super ("Exit");}
  public void actionPerformed (ActionEvent e) {
    //dispose ();
   System.exit (0);
   }
}
//------------------------------------------------------------------------------
public JToolBar getToolBar ()
{
  return toolbar;
} 
//------------------------------------------------------------------------------
class ClearSelectionAction extends AbstractAction {
  ClearSelectionAction () {super ("");}
  public void actionPerformed (ActionEvent e) {
    clearSelection ();
    }
}
//------------------------------------------------------------------------------
class SelectAllAction extends AbstractAction {
  SelectAllAction () {super ("");}
  public void actionPerformed (ActionEvent e) {
    int count = listbox.getModel().getSize();
    int [] indices = new int [count];
    for (int i=0; i < count; i++)
      indices [i] = i;
    listbox.setSelectedIndices (indices);
    }
} // SelectAllAction
//------------------------------------------------------------------------------
class DescribeAction extends AbstractAction {
  DescribeAction () {super ("");}
  public void actionPerformed (ActionEvent e) {
    int [] selection = listbox.getSelectedIndices ();
    System.out.print ("selection count: " + selection.length);
    for (int i=0; i < selection.length; i++)
      System.out.print ("  " + selection [i]);
    System.out.println ();
    }
}
//------------------------------------------------------------------------------
private void placeInCenter ()
{
  GraphicsConfiguration gc = getGraphicsConfiguration ();
  int screenHeight = (int) gc.getBounds().getHeight ();
  int screenWidth = (int) gc.getBounds().getWidth ();
  int windowWidth = getWidth ();
  int windowHeight = getHeight ();
  setLocation ((screenWidth-windowWidth)/2, (screenHeight-windowHeight)/2);

} // placeInCenter
//------------------------------------------------------------------------------
class ListboxSelectionListener implements ListSelectionListener {

  public void valueChanged (ListSelectionEvent e) {
    if (e.getValueIsAdjusting ()) return;
    ListSelectionModel lsm = (ListSelectionModel) e.getSource();
    currentSelection = new ArrayList ();
    if (!lsm.isSelectionEmpty()) {
      int minIndex = lsm.getMinSelectionIndex ();
      int maxIndex = lsm.getMaxSelectionIndex ();
      for (int i = minIndex; i <= maxIndex; i++) {
        if (lsm.isSelectedIndex (i)) {
          currentSelection.add (listData [i]);
         }
        } // for i
      } // if !empty
    } // valueChanged

} // inner class AttributeListSelectionListener
//-----------------------------------------------------------------------------
public void select (String [] names)
{
  //System.out.println ("(" + getTitle () + " Lister.select, count = " + names.length);
  ListModel model = listbox.getModel ();
  int [] alreadySelectedIndices = listbox.getSelectedIndices ();
  ArrayList indicesToSelect = new ArrayList ();
  for (int i=0; i < alreadySelectedIndices.length; i++)
    indicesToSelect.add (new Integer (alreadySelectedIndices [i]));

  for (int i=0; i < model.getSize (); i++) {
    String entry = (String) model.getElementAt (i);
    if (Arrays.binarySearch (names, entry) >= 0) {
      //System.out.println ("select " + i + ": " + entry);
      Integer candidate = new Integer (i);
      if (!indicesToSelect.contains (candidate))
        indicesToSelect.add (candidate);
      }// if
    } // for i

  int count = indicesToSelect.size ();
  int [] selection = new int [count];
  for (int i=0; i < count; i++) {
    int index = ((Integer) indicesToSelect.get (i)).intValue ();
    selection [i] = index;
    }

  //System.out.println ("Lister.select, selecting " + count);
  for (int i=0; i < count; i++)
    //System.out.println ("   " + selection [i]);

  listbox.setSelectedIndices (selection);
  
} // select
//-----------------------------------------------------------------------------
public String [] getNames ()
{
  DefaultListModel model = (DefaultListModel) listbox.getModel ();
  Object [] tmp = model.toArray ();
  ArrayList nameList = new ArrayList ();
  for (int i=0; i < tmp.length; i++) {
    System.out.println (tmp [i] + "   " + tmp [i].getClass());
    nameList.add (tmp [i]);
    }

  return (String []) nameList.toArray (new String [0]);

} // getNames
//-----------------------------------------------------------------------------
public void addNames (String [] names)
{
  DefaultListModel model = (DefaultListModel) listbox.getModel ();
  for (int i=0; i < names.length; i++) {
    if (model.indexOf (names [i]) == -1)
      model.add (0, names [i]);
    } // for i

}
//-----------------------------------------------------------------------------
public void clearSelection ()
{
  listbox.clearSelection ();
}
//-----------------------------------------------------------------------------
public void removeAll ()
{
  DefaultListModel model = (DefaultListModel) listbox.getModel ();
  model.removeAllElements ();
}
//-----------------------------------------------------------------------------
public String [] getCurrentSelection ()
{
  Object [] selections = listbox.getSelectedValues ();
  String [] result = new String [selections.length];
  for (int i=0; i < selections.length; i++)
    result [i] = (String) selections [i];

  return result;

}
//-----------------------------------------------------------------------------
public static void main (String[] args) throws Exception
{
  new Lister ();
}
//------------------------------------------------------------------------------------
} // Lister
