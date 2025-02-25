// MovieControllerInfoDialog.java
//-------------------------------------------------------------------------------------------
// $Date: 2004/12/18 21:23:36 $
// $Author: pshannon $
//------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui.movie;
//------------------------------------------------------------------------------
import java.util.*;
import java.io.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.systemsbiology.gaggle.experiment.datamatrix.*;
//------------------------------------------------------------------------------
/**
 * This dialog describes the matrices currently loaded into
 * the MovieController, and makes it possible to edit the node attribute to which
 * (typically) the data is written.  That is:  lambda values are usually written
 * to 'lambda', ratios to 'log10 ratio'.
 *
 * @author Paul Shannon
 * @date 12/16/04
 */
//-------------------------------------------------------------------------------------------------
public class MovieControllerInfoDialog extends JDialog { // implements ActionListener {
 
  LensedDataMatrix [] lenses;
  JButton saveChangesButton;
  JTable [] allTables;
  final static String BRIEF_DATA_TYPE_KEY = "brief type name";

//-------------------------------------------------------------------------------------------------
public MovieControllerInfoDialog (LensedDataMatrix [] lenses)
{ 
  super ();
  this.lenses = lenses;
  setTitle ("Movie Matrices Info");
  allTables = new JTable [lenses.length];
  getContentPane().add (createGui ());
  
} // ctor
//-------------------------------------------------------------------------------------------------
public JPanel createGui ()
{
  JPanel topPanel = new JPanel ();
  topPanel.setLayout (new BorderLayout ());

  JPanel buttonPanel = new JPanel ();
  JButton dismissButton = new JButton ("Dimiss");
  dismissButton.addActionListener (new DismissAction ());
  buttonPanel.add (dismissButton);
  saveChangesButton = new JButton ("Save Changes");
  saveChangesButton.setEnabled (false);
  saveChangesButton.addActionListener (new SaveChangesAction ());
  buttonPanel.add (saveChangesButton);
  topPanel.add (buttonPanel, BorderLayout.SOUTH);

  JTabbedPane tabbedPane = new JTabbedPane ();
  topPanel.add (tabbedPane, BorderLayout.CENTER);

  for (int i=0; i < lenses.length; i++) {
    JPanel tabPanel = new JPanel ();
    tabPanel.setLayout (new BorderLayout ());
    JTable table = new JTable (new InfoTableModel (lenses [i]));
    allTables [i] = table;
    String matrixDataTypeBriefName = lenses [i].getDataTypeBriefName ();
    table.setShowHorizontalLines (true);
    table.setShowVerticalLines (true);
    table.setPreferredScrollableViewportSize (new Dimension (300, 200));
    JScrollPane scrollPane = new JScrollPane (table);
    scrollPane.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    JPanel tablePanel = new JPanel ();
    tablePanel.setLayout (new BorderLayout ());
    tablePanel.setBorder (BorderFactory.createEmptyBorder (10, 10, 10, 10));
    tablePanel.add (scrollPane, BorderLayout.CENTER);
    tabPanel.add (tablePanel);
    tabbedPane.add (matrixDataTypeBriefName, tabPanel);
    } // for i

  return topPanel;

} // createGui
//-------------------------------------------------------------------------------------------------
class InfoTableModel extends AbstractTableModel {

    LensedDataMatrix lens;
    String [] columnNames = new String [] {"name", "value"};
    HashMap map;

  InfoTableModel (LensedDataMatrix lens) {
    this.lens = lens;
    lens.enableAllRows ();
    map = new HashMap ();
    map.put (BRIEF_DATA_TYPE_KEY, lens.getDataTypeBriefName ());
    map.put ("uri", lens.getFullName ());
    map.put ("rows", (new Integer (lens.getRowCount ())).toString ());
    map.put ("columns", (new Integer (lens.getColumnCount ())).toString ());
    }
  public String getColumnName (int column) {return columnNames [column];}
  public int getRowCount () {return map.size ();}
  public int getColumnCount () {return columnNames.length;}

  public void setValueAt (Object value, int row, int column) {
    String [] keys = getDataKeys ();
    if (keys [row].equals (BRIEF_DATA_TYPE_KEY) && column == 1) {
      map.put (BRIEF_DATA_TYPE_KEY, (String) value);
      saveChangesButton.setEnabled (true);
      }
    }

  public Object getValueAt (int row, int column) {
    String [] keys = getDataKeys ();
    String result = "";
    if (column == 0)
      result = keys [row];
    else if (column == 1)
      result = (String) map.get (keys [row]);
    return result;
    }

  public boolean isCellEditable (int row, int column)  {
    boolean result = false;
    String [] keys = getDataKeys ();
    if (keys [row].equals (BRIEF_DATA_TYPE_KEY) && column == 1)
      result = true;
    return result;
    }
  
  public Class getColumnClass (int column) {return "string".getClass ();}

  protected String [] getDataKeys () {
    return (String []) map.keySet().toArray(new String [0]);
    }

} // inner class InfoTableModel
//-------------------------------------------------------------------------------------------------
public class DismissAction extends AbstractAction {

  DismissAction () {;}

  public void actionPerformed (ActionEvent e) {
    MovieControllerInfoDialog.this.dispose ();
    }

} // public
//-----------------------------------------------------------------------------------
public class SaveChangesAction extends AbstractAction {

  SaveChangesAction () {;}

  public void actionPerformed (ActionEvent e) {
    for (int t=0; t < allTables.length; t++) {
      JTable table = allTables [t];
      for (int r=0; r < table.getRowCount (); r++) {
        String key = (String) allTables [t].getValueAt (r,0);
        if (key.equals (BRIEF_DATA_TYPE_KEY)) {
          String name = (String) allTables [t].getValueAt (r,1);
          lenses [t].setDataTypeBriefName (name);
          } // if found key
        } // for r
      } // for t
    } // actionPerformed

} // SaveChangesAction
//-----------------------------------------------------------------------------------
protected void placeInCenter ()
{
  GraphicsConfiguration gc = getGraphicsConfiguration ();
  int screenHeight = (int) gc.getBounds().getHeight ();
  int screenWidth = (int) gc.getBounds().getWidth ();
  int windowWidth = getWidth ();
  int windowHeight = getHeight ();
  setLocation ((screenWidth-windowWidth)/2, (screenHeight-windowHeight)/2);

} // placeInCenter
//------------------------------------------------------------------------------
}  // class MovieControllerInfoDialog

