// SelectableMatrixPlotter.java
//-----------------------------------------------------------------------------------------------
// $Revision: 18 $  $Date: 2005/04/11 18:12:31 $
//-----------------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui.plotters.matrix;
//-----------------------------------------------------------------------------------------------


import java.io.File;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.systemsbiology.gaggle.experiment.gui.*;
import org.systemsbiology.gaggle.experiment.readers.*;

//-----------------------------------------------------------------------------------------------
public class SelectableMatrixPlotter extends JPanel implements DataMatrixView {

  MatrixPlotter matrixPlotter;
  JList selectionListBox;
  ArrayList currentSelection = new ArrayList ();
  String [] rowNames;
  protected JButton selectionToggleButton;
  protected boolean selectionToggleState = false;
  protected JTextField selectionCountTextField;
  protected String namesToDisplay = "canonical";

//-----------------------------------------------------------------------------------------------
public SelectableMatrixPlotter (org.systemsbiology.gaggle.core.datatypes.DataMatrix dataMatrix)
{
  setLayout (new BorderLayout ());

  rowNames = dataMatrix.getRowTitles ();
  matrixPlotter = new MatrixPlotter (dataMatrix);
  add (matrixPlotter, BorderLayout.CENTER);

  JPanel selectionPanel = createSelectionPanel ();
  add (selectionPanel, BorderLayout.WEST);

} // ctor
//-----------------------------------------------------------------------------------------------
protected JPanel createSelectionPanel ()
{
  JPanel mainPanel = new JPanel ();
  mainPanel.setLayout (new BorderLayout ());

  JPanel topPanel = new JPanel ();
  JPanel bottomPanel = new JPanel ();
  JPanel centerPanel = new JPanel ();
  centerPanel.setLayout (new BorderLayout ());

  mainPanel.add (topPanel, BorderLayout.NORTH);
  mainPanel.add (bottomPanel, BorderLayout.SOUTH);
  mainPanel.add (centerPanel, BorderLayout.CENTER);

  JButton nameToggleButton = new JButton ("Names");
  topPanel.add (nameToggleButton);
  nameToggleButton.addActionListener (new ActionListener () {
    public void actionPerformed (ActionEvent e) {
      toggleListNamesBetweenCanonicalAndCommon ();
      }});

  DefaultListModel model = new DefaultListModel ();

  for (int i=0; i < rowNames.length; i++)
    model.add (i, rowNames [i]);

  selectionListBox = new JList (model);
  selectionListBox.addListSelectionListener (new ListBoxSelectionListener ());
  selectionListBox.setCellRenderer (new ColoredRenderer ());
  JScrollPane scrollPane = new JScrollPane (selectionListBox);

  centerPanel.add (scrollPane, BorderLayout.CENTER);
  centerPanel.setBorder (createBorder ());

  selectionToggleButton = new JButton ("-");
  selectionToggleButton.addActionListener (new SelectToggleButtonListener ());
    
  bottomPanel.add (selectionToggleButton);
  selectionCountTextField = new JTextField (3);
  bottomPanel.add (selectionCountTextField);

  return mainPanel;

} // createSelectionPanel
//-----------------------------------------------------------------------------------------------
protected void toggleListNamesBetweenCanonicalAndCommon ()
{
  if (namesToDisplay.equals ("common"))
    namesToDisplay = "canonical";
  else
    namesToDisplay = "common";

  DefaultListModel model = (DefaultListModel) selectionListBox.getModel(); 
  for (int i=0; i < rowNames.length; i++) {
    if (namesToDisplay.equals ("common")) {
      String commonName = matrixPlotter.getCommonName (rowNames [i]);
      model.set (i, commonName);
      }
    else {
      model.set (i, rowNames [i]);
      }
    } // for i


} // toggleNamesBetweenCanonicalAndCommon 
//-----------------------------------------------------------------------------------------------
class SelectToggleButtonListener implements ActionListener {

  public void actionPerformed (ActionEvent e) {

    String newLabel;
    int newCount;

    if (selectionToggleState) {
      newLabel = "-";
      matrixPlotter.select (rowNames);
      newCount = rowNames.length;
      }
    else {
      newLabel = "+";
      clearSelection ();
      newCount = 0;
      }
    selectionToggleButton.setText (newLabel);
    selectionToggleState = !selectionToggleState;
    setSelectionCountTextField (newCount);
    selectionListBox.clearSelection ();
    } // actionPeformed

} // SelectToggleButtonListener
//----------------------------------------------------------------------------------------------------
protected void setSelectionCountTextField (int newCount)
{
  selectionCountTextField.setText (new Integer (newCount).toString ());
}
//----------------------------------------------------------------------------------------------------
class ListBoxSelectionListener implements ListSelectionListener {

  public void valueChanged (ListSelectionEvent e) {
    if (e.getValueIsAdjusting ()) return;
    JList lb = (JList) e.getSource ();
    ListSelectionModel lsm = lb.getSelectionModel ();
    currentSelection = new ArrayList ();
    if (!lsm.isSelectionEmpty()) {
      int minIndex = lsm.getMinSelectionIndex ();
      int maxIndex = lsm.getMaxSelectionIndex ();
      for (int i = minIndex; i <= maxIndex; i++) {
        if (lsm.isSelectedIndex (i)) {
          currentSelection.add (rowNames [i]);
          }
        } // for i
      } // if !empty
    String [] selectedNames = (String []) currentSelection.toArray (new String [0]);
    matrixPlotter.select (selectedNames);
    setSelectionCountTextField (selectedNames.length);
    } // valueChanged

} // inner class ListboxSelectionListener
//-----------------------------------------------------------------------------------------------
private Border createBorder () 
{
  int right  = 10;
  int left   = 10;
  int top    = 10;
  int bottom = 10;
  EmptyBorder eb = new EmptyBorder (top, left, bottom, right);

  return eb;
}
//-----------------------------------------------------------------------------------------------
class ColoredRenderer extends DefaultListCellRenderer {
 
  public Component getListCellRendererComponent (JList list,
                                                 Object value,
                                                 int index,
                                                 boolean isSelected,
                                                 boolean hasFocus) {
    JLabel label = 
         (JLabel) super.getListCellRendererComponent (list, value, index, isSelected, hasFocus);
    String canonicalName = matrixPlotter.getCanonicalName (label.getText ());
    Color lineColor = matrixPlotter.getColor (canonicalName);
    //System.out.println ("setting foreground for " + canonicalName + " to " + lineColor);
    label.setForeground (lineColor);
    return (label);
    } // getListCellRendererComponent

} // inner class ColoredRenderer
//-----------------------------------------------------------------------------------------------
public void save (File directory) {}
//-----------------------------------------------------------------------------------------------
public String getSpecies ()
{ 
  return matrixPlotter.getSpecies ();
}
//-----------------------------------------------------------------------------------------------
public String getClassName () {return "MatrixPlotter";}
public String getName () {return "unknown from SelectableMatrixPlotter";}
//-----------------------------------------------------------------------------------------------
public void select (String species, String [] names)
{
  matrixPlotter.select (names);
}
//-----------------------------------------------------------------------------------------------
public void clearSelection ()
{
  matrixPlotter.select (new String [] {});
  setSelectionCountTextField (0);
}
//-----------------------------------------------------------------------------------------------
public String [] getSelection ()
{
  if (currentSelection == null)
   return new String [0];

  return (String []) currentSelection.toArray (new String [0]);
}
//-----------------------------------------------------------------------------------------------
public org.systemsbiology.gaggle.core.datatypes.DataMatrix getSelectedMatrix ()
{
  return null;
}
//-----------------------------------------------------------------------------------------------
static public void main (String [] args) throws Exception
{
  String filename = "../../../sampleData/simpleMatrix.txt";
  if (args.length == 1) 
    filename = args [0];

  JFrame f = new JFrame ("MatrixPlotter");
  DataMatrixFileReader reader = new DataMatrixFileReader (filename);
  reader.read ();
  org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix = reader.get ();
  matrix.setSpecies ("Halobacterium sp.");
  SelectableMatrixPlotter plotter = new SelectableMatrixPlotter (reader.get ());
  f.add (plotter);
  f.pack ();
  f.setVisible (true);

} // test main
//------------------------------------------------------------------------
} // class SelectableMatrixPlotter
