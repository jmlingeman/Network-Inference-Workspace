// CorrelationFinderDialog.java
//------------------------------------------------------------------------------
// $Revision: 2360 $
// $Date: 2005/01/13 23:11:55 $
// $Author: dtenenba $
//------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui.actions;
//------------------------------------------------------------------------------
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.systemsbiology.gaggle.experiment.datamatrix.*;
import org.systemsbiology.gaggle.experiment.gui.*;

//------------------------------------------------------------------------------
/**
 * This class provides visualization of the correlation of gene expression vectors
 * with a sample expression vector. The average of the normalized expression vectors
 * of the current selection is the target vector against which all other genes are
 * compared. Correlation values range from +1 (correlated) to 0 (uncorrelated) to
 * -1 (anti-correlated).
 *
 * @author Andrew Markiel
 * @date 07/15/02
 */
//-------------------------------------------------------------------------------------------------
public class CorrelationFinderDialog extends JDialog implements ActionListener {

  protected MatrixSpreadsheet parentSpreadsheet;

  private org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix;
  private LensedDataMatrix lens;

  private CorrelationFinder finder;
  private double currentCorrelationThreshold = 1.0;
  private ArrayList namesOfCorrelatedNodes = new ArrayList ();
  //private String [] namesOfCorrelatedNodes = new String [0];
  private String [] namesOfReferenceNodes = new String [0];   // the nodes to correlate -to-

  JTextField correlatedCountTextField;
  JTextField thresholdTextField;
  String correlationMode = "Positive";

//-------------------------------------------------------------------------------------------------
public CorrelationFinderDialog (MatrixSpreadsheet  parent)
{ 
  super ();
  setTitle ("Correlation Finder");
  this.parentSpreadsheet = parent;
  this.lens = parentSpreadsheet.getLens ();
  this.matrix = lens.getUnderlyingMatrix ();
  createGui ();
}
//-------------------------------------------------------------------------------------------------
public void createGui ()
{
  int selectedRowCount = matrix.getRowCount ();
  
  if (selectedRowCount < 1) {
    String msg = "Please select one or more rows and try again.";
    JOptionPane.showMessageDialog (parentSpreadsheet, msg);
    return;
    }

  namesOfReferenceNodes = lens.getRowTitles ();
  // System.out.println ("number of selected rows in lens: " + namesOfReferenceNodes.length);
   //   String [] selectedNames = 
  finder = new CorrelationFinder (matrix);
  finder.buildCorrelationTable (namesOfReferenceNodes);
  Map table = finder.getCorrelationTable ();

     //----------- construct the gui proper
  
  JSlider slider = new JSlider (JSlider.HORIZONTAL, 0, 100, 100);
  slider.setBorder (BorderFactory.createEmptyBorder(0,0,10,0));
  slider.setMajorTickSpacing (10);
  slider.setMinorTickSpacing (1);
  slider.setPaintTicks (true);
  slider.setPaintLabels (true);
  slider.addChangeListener (new SliderListener ());

  JButton browserSelectButton = new JButton ("Select in Browser");
  browserSelectButton.addActionListener (new SelectNodesAboveThreshold ());
  
  Container contentPane = getContentPane ();
  contentPane.setLayout (new BorderLayout ());
  String lineSep = System.getProperty ("line.separator");

  JPanel topPanel = new JPanel ();

  topPanel.setBorder (BorderFactory.createCompoundBorder (
                        BorderFactory.createEmptyBorder (10,10,2,10),
                        BorderFactory.createCompoundBorder (
                          BorderFactory.createEtchedBorder (),
                          BorderFactory.createEmptyBorder (10,10,10,10))));

  JPanel middlePanel = new JPanel ();
  // middlePanel.setLayout (new GridLayout (2,1));
  JPanel readoutPanel = new JPanel ();
  middlePanel.add (readoutPanel);
  topPanel.add (middlePanel, BorderLayout.CENTER);
  readoutPanel.setLayout (new GridLayout (2, 2));
  
  correlatedCountTextField = new JTextField ("0", 6);

  thresholdTextField = new JTextField ("100", 6);

  readoutPanel.add (new JLabel ("Nodes meeting threshold "));// , BorderLayout.WEST);
  readoutPanel.add (correlatedCountTextField); //, BorderLayout.CENTER);
  readoutPanel.add (new JLabel ("Threshold "));
  readoutPanel.add (thresholdTextField);

  JPanel radioButtonPanel = new JPanel ();
  radioButtonPanel.setLayout (new GridLayout (1,3));

  JRadioButton positiveButton = new JRadioButton ("Positive", true);
  JRadioButton negativeButton = new JRadioButton ("Negative");
  JRadioButton bothButton = new JRadioButton ("Both");

  positiveButton.addActionListener (this);
  negativeButton.addActionListener (this);
  bothButton.addActionListener (this);

  radioButtonPanel.add (positiveButton);
  radioButtonPanel.add (negativeButton);
  radioButtonPanel.add (bothButton);


  ButtonGroup radioGroup = new ButtonGroup ();
  radioGroup.add (positiveButton);
  radioGroup.add (negativeButton);
  radioGroup.add (bothButton);

  //middlePanel.add (radioButtonPanel);


  JPanel sliderPanel = new JPanel ();
  sliderPanel.setLayout (new BorderLayout ());
  sliderPanel.setBorder (BorderFactory.createEmptyBorder (20,20,20,20));
  sliderPanel.add (radioButtonPanel, BorderLayout.NORTH);
  sliderPanel.add (slider, BorderLayout.CENTER);

  JPanel textAndSliderPanel = new JPanel ();
  textAndSliderPanel.setLayout (new BorderLayout ());
  contentPane.add (textAndSliderPanel,  BorderLayout.CENTER);
  textAndSliderPanel.add (topPanel, BorderLayout.CENTER);
  textAndSliderPanel.add (sliderPanel, BorderLayout.SOUTH);

  JPanel buttonPanel = new JPanel ();
  JButton dismissButton = new JButton ("Dismiss");
  dismissButton.addActionListener (new DismissAction ());
  contentPane.add (buttonPanel, BorderLayout.SOUTH);
  buttonPanel.add (browserSelectButton);
  buttonPanel.add (dismissButton);

  pack ();
  placeInCenter ();
  setVisible (true);
  refreshSelection ();

} // createGui
//-------------------------------------------------------------------------------------------------
protected void refreshSelection ()
// called by the SliderListener, and the radio button action listener, so that
// any change (to either slider position, or mode -- positive correlations, negative, or both)
// causes a fresh calculation and display of nodes correlated to the initial selected node
// or nodes
{
  int count = findCorrelatedNodes ();
  // System.out.println ("refreshSelection, count: " + count);
  String countAsString = (new Integer (count)).toString ();
  correlatedCountTextField.setText (countAsString);
}
//-------------------------------------------------------------------------------------------------
protected class SliderListener implements ChangeListener {
        
  public SliderListener () {;}

  public void stateChanged (ChangeEvent ce) {
    JSlider source = (JSlider) ce.getSource();
    if (!source.getValueIsAdjusting ()) {
      int currentValue = source.getValue ();
      thresholdTextField.setText ((new Integer (currentValue)).toString ());
      currentCorrelationThreshold  = currentValue / 100.0;
      refreshSelection ();
      } // if
    } // stateChanged

} // inner class SliderListener
//-------------------------------------------------------------------------------------------------
private int findCorrelatedNodes ()
{
  Map correlations = finder.getCorrelationTable ();
  String [] keys = (String []) correlations.keySet().toArray (new String [0]);
  // System.out.println ("find correlated nodes, key count: " + keys.length);
  namesOfCorrelatedNodes = new ArrayList ();

  // System.out.println ("CFD threshold: " + currentCorrelationThreshold);
  for (int k=0; k < keys.length; k++) {
    String nodeName = keys [k];
    double correlation = ((Double) correlations.get (nodeName)).doubleValue ();
    //System.out.println ("correlation: " + correlation);
    boolean correlated = false;
    if (correlation >= currentCorrelationThreshold && 
       (correlationMode.equals ("Positive") || correlationMode.equals ("Both")))
      correlated = true;

    if ((-1.0 * correlation) >= currentCorrelationThreshold && 
       (correlationMode.equals ("Negative") || correlationMode.equals ("Both")))
      correlated = true;

    if (correlated) {
      namesOfCorrelatedNodes.add (nodeName);
      // System.out.println ("CFD.findCorrelatedNodes, " + nodeName + ": " + correlation);
      }
    } // for k

  //namesOfCorrelatedNodes = (String []) nodeList.toArray (new String [0]);
  //namesOfCorrelatedNodes = (String []) nodeList.toArray (new String [0]);
  return (namesOfCorrelatedNodes.size ());

} // findCorrelatedNodes
//-------------------------------------------------------------------------------------------------
class SelectNodesAboveThreshold extends AbstractAction  {

  SelectNodesAboveThreshold () {super ("");};
  
  public void actionPerformed (ActionEvent e) {
      
    for (int i=0; i < namesOfReferenceNodes.length; i++) 
      if (!namesOfCorrelatedNodes.contains (namesOfReferenceNodes [i]))
        namesOfCorrelatedNodes.add (namesOfReferenceNodes [i]);
    
    String [] names = (String []) namesOfCorrelatedNodes.toArray (new String [0]);
    //System.out.println ("select in browser, count: " + names.length);

    if (parentSpreadsheet != null) {
      parentSpreadsheet.clearSelection ();
      parentSpreadsheet.select (names);
      }
   } // actionPerformed

} // inner class SelectNodesAboveThreshold
//-------------------------------------------------------------------------------------------------
public void actionPerformed (ActionEvent e) 
{
  correlationMode = e.getActionCommand ();
  refreshSelection ();

} // actionPerformed  (radio button callback)
//-------------------------------------------------------------------------------------------------
public class DismissAction extends AbstractAction {

  DismissAction () {;}

  public void actionPerformed (ActionEvent e) {
    CorrelationFinderDialog.this.dispose ();
    }

} // DismissAction
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
}  // class CorrelationFinderDialog

