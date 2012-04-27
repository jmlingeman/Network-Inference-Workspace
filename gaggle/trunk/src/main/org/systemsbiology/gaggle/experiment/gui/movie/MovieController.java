// MovieController.java
//-------------------------------------------------------------------------------------
// RCS:  $Revision: 1725 $   $Date: 2005/03/16 00:04:03 $
//-------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui.movie;
//---------------------------------------------------------------------------------
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.*;

import org.systemsbiology.gaggle.experiment.gui.actions.*;
import org.systemsbiology.gaggle.experiment.datamatrix.LensedDataMatrix;


//-------------------------------------------------------------------------------------
abstract public class MovieController {

  String name = "movie";
    
  JPanel mainPanel;
  JPanel conditionsPanel;
  JPanel playPanel;

  JComboBox conditionChooser, frameRateChooser;

  ImageIcon playIcon, stopIcon, questionIcon;
  JButton movieButton, questionButton;

  JCheckBox loopCheckBox;
  JCheckBox reversePlayCheckBox;
  boolean loopMovie = false;
  boolean reversePlay = false;

  String [] matrixNames;

  protected LensedDataMatrix [] lenses;
  protected String [] columnNames = new String [0];
  protected int conditionChooserIndex = 0;

  MovieControllerClient parent;
  Date matricesTimeStamp = null;

  javax.swing.Timer frameTimer;
  FrameAnimator frameAnimator;
  int initialFrameDelay = 1; // second
  protected volatile boolean conditionChooserInitialized = false;  // read by derived classes. see note below

//-------------------------------------------------------------------------------------------
public MovieController (MovieControllerClient parent)
{
  this.parent = parent;
  frameAnimator = new FrameAnimator ();
  frameTimer = new javax.swing.Timer (initialFrameDelay * 1000, frameAnimator);
  parent.getToolbarEtcPanel().add (createComboBoxUI ());

} // ctor
//-------------------------------------------------------------------------------------------
public void setEnabled (boolean newValue)
{
  conditionChooser.setEnabled (newValue);
  frameRateChooser.setEnabled (newValue);
  movieButton.setEnabled (newValue);
  questionButton.setEnabled (newValue);
  loopCheckBox.setEnabled (newValue);
  reversePlayCheckBox.setEnabled (newValue);
 
}
//-------------------------------------------------------------------------------------------
class FrameAnimator implements ActionListener {

  public void actionPerformed (ActionEvent e) {
    changeCondition ();
    }

} // inner class FrameAnimator
//-------------------------------------------------------------------------------------------
protected JPanel createComboBoxUI () 
{
  mainPanel = new JPanel ();
  mainPanel.setLayout (new BorderLayout ());
  JPanel innerPanel = new JPanel ();

  conditionChooser = new JComboBox ();
  // conditionChooser.setPrototypeDisplayValue ("a very very long condition name");
  conditionChooser.setToolTipText ("Experimental condition");
  conditionChooser.addActionListener (new ConditionChooserListener ());
  mainPanel.add (innerPanel, BorderLayout.CENTER);
  innerPanel.add (conditionChooser);
  mainPanel.setBorder (BorderFactory.createEmptyBorder (1,30,1,30));

  //String [] delays = new String [] {"0", "0.5",  "1", "2", "3", "5", "10"};
  String [] delays = new String [] {"1", "2", "3", "5", "10"};

  frameRateChooser = new JComboBox (delays);;
  frameRateChooser.setSelectedIndex (2);
  frameRateChooser.setToolTipText ("Frame Rate");
  frameRateChooser.addActionListener (new FrameRateChooserListener ());
  mainPanel.add (innerPanel, BorderLayout.CENTER);
  innerPanel.add (frameRateChooser);

  playIcon = IconFactory.getPlayIcon ();
  stopIcon = IconFactory.getStopIcon ();
  questionIcon = IconFactory.getQuestionIcon ();

  movieButton = new JButton (playIcon);
  movieButton.setToolTipText ("Run & Stop Movie");
  movieButton.setActionCommand ("run");
  movieButton.addActionListener (new MovieButtonListener ());
  innerPanel.add (movieButton);

  questionButton = new JButton (questionIcon);
  questionButton.addActionListener (new QuestionButtonListener ());

  JPanel checkboxPanel = new JPanel ();
  checkboxPanel.setLayout (new GridLayout (2,1));
  loopCheckBox = new JCheckBox ("loop");
  reversePlayCheckBox = new JCheckBox ("reverse");
  loopCheckBox.addItemListener (new LoopCheckBoxListener ());
  reversePlayCheckBox.addItemListener (new ReversePlayCheckBoxListener ());
  checkboxPanel.add (loopCheckBox);
  checkboxPanel.add (reversePlayCheckBox);
  innerPanel.add (checkboxPanel);
  innerPanel.add (questionButton);

  return mainPanel;

} // createComboBoxUI
//-------------------------------------------------------------------------------------------
class LoopCheckBoxListener implements ItemListener {

  public void itemStateChanged (ItemEvent e) {
    loopMovie = e.getStateChange () == ItemEvent.SELECTED;
    }

} // inner class LoopCheckBoxListener
//-------------------------------------------------------------------------------------------
class ReversePlayCheckBoxListener implements ItemListener {

  public void itemStateChanged (ItemEvent e) {
    reversePlay = e.getStateChange () == ItemEvent.SELECTED;
    }

} // inner class ReversePlayCheckBoxListener
//-------------------------------------------------------------------------------------------
class MovieButtonListener implements ActionListener {

  public void actionPerformed (ActionEvent e) {
    if ("run".equals (e.getActionCommand ()))
      runMovie ();
    else if ("stop".equals (e.getActionCommand ()))
      stopMovie ();
    }

} // inner class MovieButtonListener
//-------------------------------------------------------------------------------------------
class QuestionButtonListener implements ActionListener {

  public void actionPerformed (ActionEvent e) {
    //System.out.println ("lauching info with lens count: " + lenses.length);
    JDialog dialog = new MovieControllerInfoDialog (lenses);
    dialog.pack ();
    dialog.setLocationRelativeTo (mainPanel);
    dialog.setVisible (true);
    }

} // inner class MovieButtonListener
//-------------------------------------------------------------------------------------------
protected void runMovie ()
{
  movieButton.setIcon (stopIcon);
  movieButton.setActionCommand ("stop");
  frameTimer.start ();

}
//-------------------------------------------------------------------------------------------
protected void stopMovie ()
{
  frameTimer.stop ();
  movieButton.setIcon (playIcon);
  movieButton.setActionCommand ("run");

}
//-------------------------------------------------------------------------------------------
class ConditionChooserListener implements ActionListener {

  public void actionPerformed (ActionEvent e) {
      if (conditionChooserInitialized) {
        JComboBox cb = (JComboBox) e.getSource ();
        //currentColumn = cb.getSelectedIndex ();
        conditionChooserIndex = cb.getSelectedIndex ();
        broadcast ();
      }
    }

} // inner class ConditionChooserListener
//-------------------------------------------------------------------------------------------
class FrameRateChooserListener implements ActionListener {

  public void actionPerformed (ActionEvent e) {
    JComboBox cb = (JComboBox) e.getSource ();
    String selection = (String) cb.getSelectedItem ();
    try {
      double seconds = (new Double (selection)).doubleValue ();
      int msecs = (int) (seconds * 1000);
      frameTimer.setDelay (msecs);
      }
    catch (Exception ex0) {
      System.err.println (ex0.getMessage ());
      }
    } // actionPerformed

} // inner class FrameRateChooserListener
//-------------------------------------------------------------------------------------------
public void loadMatrices ()
{
  if (parent.getLastModificationTime() == null)
     return;

  if (matricesTimeStamp == null || parent.getLastModificationTime().after (matricesTimeStamp)) {
    lenses = extractCongruentMatrices (parent.getLenses ());
    matrixNames = new String [lenses.length];
    for (int i=0; i < lenses.length; i++)
      matrixNames [i] = lenses [i].getShortName ();

    if (lenses.length > 0)
      this.columnNames = lenses [0].getColumnTitles ();
     matricesTimeStamp = parent.getLastModificationTime ();

    loadConditionChooserComboBox (lenses);
    setEnabled (true);
    } // if parent has modified the lensed matrices requiring fresh import of data

} // loadMatrices
//-------------------------------------------------------------------------------------------
/**
 * select only those LensedDataMatrices whose structure is identical with the zeroth lens;
 * congruent matrices have same number of rows and column, same row and column titles
 */
protected LensedDataMatrix [] extractCongruentMatrices (LensedDataMatrix [] lenses)
{
  if (lenses.length == 1)
    return lenses;

  ArrayList tmp = new ArrayList ();
  LensedDataMatrix refLens = lenses [0];
  tmp.add (refLens);
  int refRowCount = refLens.getRowCount ();
  int refColumnCount = refLens.getColumnCount ();
  String [] refColumnTitles = refLens.getColumnTitles ();
  String [] refRowTitles = refLens.getRowTitles ();
  for (int i=1; i < lenses.length; i++) {
    int newRowCount = lenses [i].getRowCount ();
    int newColumnCount = lenses [i].getColumnCount ();
    if (newRowCount != refRowCount || newColumnCount != refColumnCount)
      continue;
    String [] newColumnTitles = lenses [i].getColumnTitles ();
    if (!Arrays.equals (newColumnTitles, refColumnTitles))
      continue;
    String [] newRowTitles = lenses [i].getRowTitles ();
    if (!Arrays.equals (newRowTitles, refRowTitles))
      continue;
    tmp.add (lenses [i]);
    }

  return (LensedDataMatrix []) tmp.toArray (new LensedDataMatrix [0]);
  
} // extractCongruentMatrices
//-------------------------------------------------------------------------------------------
protected void loadConditionChooserComboBox (LensedDataMatrix [] lenses)
{
  ArrayList conditionValues = new ArrayList ();
  String [] firstMatrixColumnTitles = lenses [0].getColumnTitles ();
  for (int i=0; i < firstMatrixColumnTitles.length; i++)
    conditionValues.add (firstMatrixColumnTitles [i]);

  // tells derived classes they can now respond to 
  // choices made in this combobox.  wierdly, 
  // adding elements to the model fires a selection
  // event, which we do not want to respond to!
  conditionChooserInitialized = false;

  DefaultComboBoxModel model = (DefaultComboBoxModel) conditionChooser.getModel ();
  model.removeAllElements ();
  model.addElement ("None");
  for (int i=0; i < conditionValues.size (); i++) {
    model.addElement (conditionValues.get (i));
    }

  // HACK: This is of questionable thread safety, but seems to work (fingers crossed).
  // The idea is to insert the runnable into the event queue *behind* the events we
  // wish to avoid. The flag will only be set *after* those events have been processed.
  // -cbare
  SwingUtilities.invokeLater(new Runnable() {
    public void run() { conditionChooserInitialized = true; }
  });

} // loadConditionChooserComboBox
//-------------------------------------------------------------------------------------------
abstract protected void broadcast ();
//-------------------------------------------------------------------------------------------
protected void changeCondition ()
{
  // System.out.println ("changeCondition, starting value: " + conditionChooserIndex);
  if (reversePlay)
    handleFrameDecrementRequest ();
  else
    handleFrameIncrementRequest ();

  // System.out.println ("changeCondition, ending value: " + conditionChooserIndex);

  conditionChooser.setSelectedIndex (conditionChooserIndex);
  //broadcast ();

} // changeCondition
//-------------------------------------------------------------------------------------------
protected void handleFrameIncrementRequest ()
{
  //System.out.println ("hfir, cci before: " + conditionChooserIndex);
  conditionChooserIndex++;

  int minIndex = 0;
  int maxIndex = conditionChooser.getModel().getSize () - 1;

  if (conditionChooserIndex > maxIndex) {
   if (loopMovie)
     conditionChooserIndex = 1;
   else {
    conditionChooserIndex = maxIndex;
    stopMovie ();
    }
  } // reach end

  // System.out.println ("hfir, cci after: " + conditionChooserIndex);

} // handleFrameIncrementRequest
//-------------------------------------------------------------------------------------------
protected void handleFrameDecrementRequest ()
{
  conditionChooserIndex--;

  int minIndex = 0;
  int maxIndex = conditionChooser.getModel().getSize () - 1;

  if (conditionChooserIndex <= minIndex) {
    if (loopMovie) 
      conditionChooserIndex = maxIndex;
    else {
      stopMovie ();
      conditionChooserIndex = 1;
      } 
    } // reached bottom

} // handleFrameDecrementRequest
//-------------------------------------------------------------------------------------------
} //public class MovieController

