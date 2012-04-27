// DataMatrixMovieDialog

// TODO - allow the escape key to dismiss this dialog. 
// TODO - add key strokes or accelerators for UI elements
// TODO - which component should have focus at first?

/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui.actions;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import cytoscape.*;
import org.systemsbiology.gaggle.experiment.datamatrix.*;

/**
 * Facilitates the display of condition data as a slideshow or animation
 * (movie). 
 * 
 * @author Iliana Avila-Campillo
 * @author Paul Shannon
 * @author Dan Tenenbaum
 */
public class DataMatrixMovieDialog extends JDialog {
    
  private JPanel mainPanel;
  private JPanel edgedPanel;
  private JPanel conditionsPanel;
  private JPanel speedPanel;
  private JPanel playPanel;
  private JPanel statusPanel;
  private JPanel loopPanel;
  private JPanel buttonPanel;
  private JPanel dismissPanel;

  private JLabel fileLabel;
  private JLabel speedLabel;
  private JLabel [] conditionLabels;

  private JTextField fileField;
  private JButton browseButton;

  private JSlider conditionsSlider;
  private JSlider speedSlider;

  private ConditionsSliderListener conditionsListener;
    
    
  private JButton playButton;
  private JButton stopButton;
  private JButton pauseButton;
  private JButton dismissButton;

  private JCheckBox loop;

  private JProgressBar pBar;

  private LensedDataMatrix [] lenses;
  private String [] attributeNames;
  private int maxConditions = 0;
  private CytoscapeWindow cytoscapeWindow;
  
  private javax.swing.Timer timer;
  
  private static final int PLAYING = 0;
  private static final int PAUSED = 1;
  private static final int STOPPED = 2;

  private String startLabelText = "...";
   
  private int movieState = STOPPED;
  private int movieFrame = -1;
  private boolean loopMovie = false;
	

/**
 * Creates a dialog box with UI elements allowing creation
 * of slideshow or movie.
 * 
 * @param cytoscapeWindow The window containing the graph to manipulate
 * @param matrices Matrix data
 * @param attributeNames Names of attributes contained in this matrix
 */
public DataMatrixMovieDialog (CytoscapeWindow cytoscapeWindow, LensedDataMatrix [] lenses,
                            String [] attributeNames)
{
  super ();
  this.cytoscapeWindow = cytoscapeWindow;
  this.lenses = lenses;
  this.attributeNames = attributeNames;
  setTitle ("Data Matrix Movie");
  findMaxColumns ();
  //System.out.println("max columns = " + maxConditions);
  int startingDelay = 2500;
  timer = new javax.swing.Timer(startingDelay,new TimerListener());
  timer.setRepeats(true);
  timer.setInitialDelay(startingDelay);
  
  createUI ();

} // ctor


/**
 * Returns the maximum number of columns in the lenses.
 *
 */
private void findMaxColumns ()
{
  maxConditions = 0;
  for (int i=0; i < lenses.length; i++) {
    int currentCount = lenses [i].getColumnCount ();
    if (currentCount > maxConditions)
      maxConditions = currentCount;
    } // for i

} // findMaxColumns


/**
 * Sets up initial UI elements.
 *
 */
protected void createUI () 
{
  if (mainPanel != null)
    mainPanel.removeAll ();

  mainPanel = new JPanel ();
  mainPanel.setLayout (new BoxLayout (mainPanel, BoxLayout.Y_AXIS));
  
  mainPanel.setBorder (BorderFactory.createEmptyBorder(5,5,5,5));

  edgedPanel = new JPanel ();
  edgedPanel.setLayout (new BoxLayout (edgedPanel, BoxLayout.Y_AXIS));
  edgedPanel.setBorder (BorderFactory.createEtchedBorder());

  conditionsPanel = new JPanel ();
  conditionsPanel.setLayout ( new BoxLayout (conditionsPanel,BoxLayout.Y_AXIS));
  conditionsPanel.setBorder (BorderFactory.createTitledBorder ("Conditions"));

  String [] conditions = lenses [0].getColumnTitles ();
  conditionLabels = new JLabel [lenses.length];


  for (int m=0; m < lenses.length; m++) {
  	  conditionLabels[m] = new JLabel(startLabelText);
  	  conditionsPanel.add(conditionLabels[m]);
  }
  
    
  int numConditions = conditions.length;
  conditionsSlider = new JSlider (JSlider.HORIZONTAL,0, numConditions,0);
  conditionsSlider.setMajorTickSpacing (1);
  conditionsSlider.setSnapToTicks (true);
  conditionsSlider.setPaintTicks (true);
  conditionsSlider.setPaintLabels (true);
  conditionsSlider.setEnabled (true);
  conditionsListener = new ConditionsSliderListener ();
  conditionsSlider.addChangeListener (conditionsListener);
  conditionsPanel.add (conditionsSlider);
  conditionsPanel.add (Box.createRigidArea  (new Dimension (8,0)));

  edgedPanel.add (conditionsPanel);

  speedPanel = new JPanel ();
  speedPanel.setLayout ( new BoxLayout (speedPanel,BoxLayout.Y_AXIS));
  speedPanel.setBorder 
    ( BorderFactory.createTitledBorder ("Speed - ms. between frames"));
  speedSlider = new JSlider (JSlider.HORIZONTAL,0,5000,2500);
  speedSlider.setMajorTickSpacing (1000);
  speedSlider.setMinorTickSpacing (100);
  speedSlider.setSnapToTicks (true);
  speedSlider.setPaintTicks (true);
  speedSlider.setPaintLabels(true);
  speedSlider.addChangeListener (new SpeedSliderListener ());
  speedPanel.add (speedSlider);
  speedPanel.add (Box.createRigidArea  (new Dimension (8,0)));
  edgedPanel.add (speedPanel);

  playPanel = new JPanel();
  playPanel.setLayout (new BoxLayout (playPanel, BoxLayout.Y_AXIS));
  playPanel.setBorder (BorderFactory.createTitledBorder ("Play"));
  
        
  buttonPanel = new JPanel ();
  buttonPanel.setLayout (new FlowLayout (FlowLayout.CENTER));
  ImageIcon playIcon = IconFactory.getPlayIcon();
  playButton = new JButton (playIcon);
  playButton.addActionListener (new PlayActionListener ());
  playButton.setToolTipText("Play");
  playButton.setBackground(Color.WHITE);
  buttonPanel.add (playButton);
  buttonPanel.add (Box.createRigidArea  (new Dimension (16,0)));
  ImageIcon pauseIcon = IconFactory.getPauseIcon();
  pauseButton = new JButton (pauseIcon);
  pauseButton.addActionListener (new PauseActionListener ());
  pauseButton.setToolTipText("Pause");
  pauseButton.setBackground(Color.WHITE);
  buttonPanel.add (pauseButton);
  buttonPanel.add (Box.createRigidArea  (new Dimension (16,0)));
  ImageIcon stopIcon = IconFactory.getStopIcon();
  stopButton = new JButton (stopIcon);
  stopButton.setVerticalTextPosition (AbstractButton.CENTER);
  stopButton.setHorizontalTextPosition (AbstractButton.LEADING); 
  stopButton.addActionListener (new StopActionListener ());
  stopButton.setToolTipText("Stop");
  stopButton.setBackground(Color.WHITE);
  buttonPanel.add (stopButton);
  playPanel.add (buttonPanel);
  statusPanel = new JPanel();
  statusPanel.setLayout(new FlowLayout());
  pBar = new JProgressBar();
  pBar.setStringPainted(true);
  pBar.setString("Movie Stopped.");
  loopPanel = new JPanel();
  loopPanel.setLayout(new BoxLayout(loopPanel, BoxLayout.X_AXIS));
  loopPanel.setBorder(BorderFactory.createEtchedBorder());

  JLabel lblLoop = new JLabel("Loop Movie");
  loop = new JCheckBox();
  loop.addActionListener(new LoopListener());
  
  statusPanel.add(pBar);
  loopPanel.add(loop);
  loopPanel.add(lblLoop);
  statusPanel.add(loopPanel);

  playPanel.add(statusPanel);
  edgedPanel.add(playPanel);

  dismissPanel = new JPanel ();
  dismissPanel.setLayout (new FlowLayout (FlowLayout.CENTER));
  dismissButton = new JButton ("Dismiss");
  dismissButton.addActionListener (new DismissActionListener ());
  dismissPanel.add (dismissButton);


  mainPanel.add (edgedPanel);
  mainPanel.add (dismissPanel);
      
  setContentPane (mainPanel);

} // createUI

/**
 * The action listener for the Dismiss button.
 */
class DismissActionListener extends AbstractAction {

  DismissActionListener () {super ("");}
  public void actionPerformed (ActionEvent e) {
    DataMatrixMovieDialog.this.dispose();
    }
        
} //inner class DismissActionListener


/**
 * The action listener for the movie timer.
 */
class TimerListener implements ActionListener {
	
	public void actionPerformed (ActionEvent e) {
		// This method has an awkward flow; 
		// would it be clearer to rewrite as a while loop?
		if (++movieFrame > maxConditions) {
			if (loopMovie) {
				movieFrame = 0;
				conditionsSlider.setValue(0);
				for (int m=0; m < lenses.length; m++) {
					conditionLabels[m].setText (startLabelText);
				}
			} else {
				timer.stop();
				conditionsSlider.setEnabled(true);
				movieState = STOPPED;
				movieFrame = -1;
				conditionsSlider.setValue(0);
				for (int m=0; m < lenses.length; m++) {
					conditionLabels[m].setText (startLabelText);
				}
				clearAttributes();
				pBar.setString("Movie Stopped.");
				pBar.setIndeterminate(false);
			}
		return;
		}
		conditionsSlider.setValue(movieFrame);
		if (movieFrame == 0) {
			clearAttributes();
			for (int m=0; m < lenses.length; m++) {
				conditionLabels[m].setText (startLabelText);
			}
		} else {
			for (int m=0; m < lenses.length; m++) {
				  conditionLabels[m].setText (lenses [m].getColumnTitles ()[movieFrame-1]);
			}
			updateNodeAttributesAndRedraw (movieFrame-1); //? +1?
		}
	}
	
} // inner class TimerListener

/**
 * The action listener for the Play button.
 */
class PlayActionListener extends AbstractAction {
  PlayActionListener () {super("");}
    
  public void actionPerformed (ActionEvent e) {
  	if (movieState == PLAYING)
  		return;
  		
  	movieState = PLAYING;
  	pBar.setString("Playing Movie...");
  	conditionsSlider.setEnabled(false);
  	pBar.setIndeterminate(true);
  		
  	timer.start();
   }

} // inner class PlayActionListener


/**
 * The action listener for the Stop button.
 */
class StopActionListener extends AbstractAction {
  StopActionListener () {
		  super("");
  }
    
  public void actionPerformed (ActionEvent e) {
  	if (movieState == STOPPED)
  		return;
  	movieState = STOPPED;
  	conditionsSlider.setEnabled(true);
  	pBar.setString("Movie Stopped.");
  	pBar.setIndeterminate(false);
  	timer.stop();
  	conditionsSlider.setValue(0);
  	clearAttributes(); 
  	
  }
}//inner class StopActionListener

/**
 * The action listener for the Pause button.
 */
class PauseActionListener extends AbstractAction {
  PauseActionListener () {
		  super("");
  }
    
  public void actionPerformed (ActionEvent e) {
	if (movieState == STOPPED)
		return;
		
	if (movieState == PAUSED){
		playButton.doClick();
		return;
	}
	
	movieState = PAUSED;
	pBar.setString("Movie Paused.");
	timer.stop();
  }
}//inner class PauseActionListener



/**
 * The action listener for the "Loop Movie" checkbox.
 */
class LoopListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
			if (loop.isSelected()) {
				loopMovie = true;	
			} else {
				loopMovie = false;
			}
	}
} // inner class LoopListener


/**
 * The change listener for the Speed Slider, which governs the speed
 * of the movie. 
 */
class SpeedSliderListener implements ChangeListener {
  SpeedSliderListener () { }
        
  public void stateChanged(ChangeEvent e){
		  if (speedSlider.getValueIsAdjusting())
			return; 
		  int delay = speedSlider.getValue();
		  timer.setDelay(delay);
		  timer.setInitialDelay(delay);
  }//stateChanged

}// inner class SpeedSliderListener


/**
 * The event handler for the Conditions Slider, which allows
 * the user to manually change the condition. This slider is
 * disabled when a movie is playing. 
 */
class ConditionsSliderListener implements ChangeListener {

  ConditionsSliderListener () { }
  
  
  public void stateChanged (ChangeEvent e){

	if ( (movieState == PLAYING) || (movieState == PAUSED) ) 
		return; // Slider should be disabled anyway.


	if (conditionsSlider.getValueIsAdjusting())
	  return;



	int conditionNumber = conditionsSlider.getValue() - 1;
	
	if (conditionNumber == -1) {
		clearAttributes();
		for (int m=0; m < lenses.length; m++) {
			conditionLabels[m].setText (startLabelText);
		}
		return;
	}

	for (int m=0; m < lenses.length; m++) {
		conditionLabels[m].setText (lenses [m].getColumnTitles ()[conditionNumber]);//?+1
	}


	updateNodeAttributesAndRedraw (conditionNumber);
	}//stateChanged

} // inner class ConditionsSliderListener


  private void clearAttributes() {
	GraphObjAttributes nodeAttributes = cytoscapeWindow.getNodeAttributes ();
	for (int i=0; i < lenses.length; i++) {
	  LensedDataMatrix lens = lenses [i];
	  String matrixName = lens.getShortName ();
	  String attribute = attributeNames [i];
	    if ((null == attribute) || ("".equals(attribute))) {
	        attribute = matrixName;
	    }
	  for (int r=0; r < lens.getUnderlyingMatrix().getRowCount (); r++) {
		String nodeName = lens.getUnderlyingMatrix().getRowTitles()[r];
		nodeAttributes.set(attribute, nodeName, 0.0);
	  } // for r
	} // for i

	cytoscapeWindow.redrawGraph (false, true);
  } // clearAttributes
  
  

/**
 * Updates the attributes of the nodes listed in the specified matrix column.
 * @param columnNumber The index of the matrix column 
 */
private void updateNodeAttributesAndRedraw (int columnNumber)
{
  GraphObjAttributes nodeAttributes = cytoscapeWindow.getNodeAttributes ();

  for (int i=0; i < lenses.length; i++) {
    LensedDataMatrix lens = lenses [i];
    String matrixName = lens.getShortName ();
    String attribute = attributeNames [i];
    if ((null == attribute) || ("".equals(attribute))) {
        attribute = matrixName;
    }
    for (int r=0; r < lens.getUnderlyingMatrix().getRowCount (); r++) {
      double value = lenses [i].getUnderlyingMatrix().get(r, columnNumber);
      String nodeName = lens.getUnderlyingMatrix().getRowTitles()[r];
      nodeAttributes.set (attribute, nodeName, value);
      } // for r
    } // for i

  cytoscapeWindow.redrawGraph (false, true);

} // updateNodeAttributesAndRedraw

//-------------------------------------------------------------------------------------
} //public class DataMatrixMovieDialog
