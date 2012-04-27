// ScatterPlotter.java
//-----------------------------------------------------------------------------------------------
// $Revision: 18 $  $Date: 2005/04/11 18:12:38 $
//-----------------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui.plotters.scatter;
//-----------------------------------------------------------------------------------------------
import org.jfree.chart.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.entity.*;
import org.jfree.data.xy.*;

import java.io.File;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.*;

import org.systemsbiology.gaggle.experiment.gui.*;
import org.systemsbiology.gaggle.experiment.gui.plotters.matrix.NamedXYSeries;

//-----------------------------------------------------------------------------------------------
public class ScatterPlotter extends JPanel implements DataMatrixView,
                                                      ChartMouseListener,  
                                                      XYToolTipGenerator, 
                                                      SelectableChartPanelController {

  String [] pointNames;
  double [] xVector;
  double [] yVector;
  String title, xAxisLabel, yAxisLabel;
  JPanel mainPanel;
  ArrayList xyPairsList;  
  XYSeriesCollection chartDataSet;
  SelectableChartPanel chartPanel;  
  JComboBox dragActionComboBox;
  JTextField selectionReadout;
  static final int DRAG_MEANS_SELECT = 0;
  static final int DRAG_MEANS_ZOOM = 1;
  int dragMeaning = DRAG_MEANS_SELECT;
  protected ArrayList currentSelection;


//-----------------------------------------------------------------------------------------------
public ScatterPlotter (String [] pointNames, double [] xVector, double [] yVector)
{
  this (pointNames, xVector, yVector, "x", "y", "");
}
//-----------------------------------------------------------------------------------------------
public ScatterPlotter (String [] pointNames, double [] xVector, double [] yVector, 
                       String xAxisLabel, String yAxisLabel, String title)
{
  this.pointNames = pointNames;
  this.xAxisLabel = xAxisLabel;
  this.yAxisLabel = yAxisLabel;
  this.title = title;
  xyPairsList = assembleData (xVector, yVector, pointNames);
  setLayout (new BorderLayout ());
  clearSelection ();
  add (createGui (), BorderLayout.CENTER);

} // ctor
//-----------------------------------------------------------------------------------------------
public String getClassName ()
{
  return "ScatterPlotter";
}
//-----------------------------------------------------------------------------------------------
public String getName ()
{
  return title;
}
//-----------------------------------------------------------------------------------------------
public void select (String species, String [] names)
{
}
//-----------------------------------------------------------------------------------------------
public String [] getSelection ()
{
  return (String []) currentSelection.toArray (new String [0]);
}
//-----------------------------------------------------------------------------------------------
public org.systemsbiology.gaggle.core.datatypes.DataMatrix getSelectedMatrix () {return null;}
//----------------------------------------------------------------------------------------------------
public void clearSelection ()
{
  currentSelection = new ArrayList ();
}
//-----------------------------------------------------------------------------------------------
public String getSpecies ()
{
  return "unknown";
}
//-----------------------------------------------------------------------------------------------
protected JPanel createGui ()
{
  JPanel panel = new JPanel ();
  panel.setLayout (new BorderLayout ());
  XYSeriesCollection chartDataset = createChartDataSet (xyPairsList, "data");
  PlotOrientation plotOrientation = PlotOrientation.VERTICAL;
  boolean legend = false;
  boolean tooltips = false;
  boolean urls = false;

  JFreeChart chart = ChartFactory.createXYLineChart (title, xAxisLabel, yAxisLabel, chartDataset,
                                                     PlotOrientation.VERTICAL, legend, tooltips, urls);
                                            
  XYPlot plotter = chart.getXYPlot ();
  plotter.setRenderer (new StandardXYItemRenderer (StandardXYItemRenderer.SHAPES));
  plotter.getRenderer().setToolTipGenerator (this);
    
  chartPanel = new SelectableChartPanel (chart, this);

  chartPanel.setEnableCustomDrag (true);
  chartPanel.setMouseZoomable (false);
  chartPanel.setRangeZoomable (false);
  chartPanel.setDomainZoomable (false);

  chartPanel.addChartMouseListener (this);

  JPanel buttonPanel = new JPanel ();
    
  JPanel bottomPanel = new JPanel ();
    
  JRadioButton dragMeansSelectButton = new JRadioButton ("Select");
  dragMeansSelectButton.setSelected (true);
  JRadioButton dragMeansZoomButton = new JRadioButton ("Zoom");

  ButtonGroup dragGroup = new ButtonGroup ();
  dragGroup.add (dragMeansSelectButton);
  dragGroup.add (dragMeansZoomButton);

  dragMeansZoomButton.addActionListener (new ActionListener () {
    public void actionPerformed (ActionEvent e) {
       setDragOperationMeaning (e, chartPanel, DRAG_MEANS_ZOOM);}});

  dragMeansSelectButton.addActionListener (new ActionListener () {
    public void actionPerformed (ActionEvent e) {
       setDragOperationMeaning (e, chartPanel, DRAG_MEANS_SELECT);}});

  bottomPanel.add (dragMeansSelectButton);
  bottomPanel.add (dragMeansZoomButton);
    
  panel.add (bottomPanel, BorderLayout.SOUTH);
  panel.add (chartPanel, BorderLayout.CENTER);

  return panel;

} // createGui
//-----------------------------------------------------------------------------------------------
protected XYSeriesCollection createChartDataSet (ArrayList xyPairList, String dataSetName)
{
  //UnsortedXYSeries xySeries = new UnsortedXYSeries (dataSetName);
  boolean autoSort = false;
  NamedXYSeries xySeries = new NamedXYSeries (dataSetName, autoSort);

  for (int i=0; i < xyPairsList.size (); i++) {
    ArrayList pointData = (ArrayList) (xyPairsList.get (i));
    String pointName = (String) pointData.get (0); 
    double x = ((Double) pointData.get (1)).doubleValue ();
    double y = ((Double) pointData.get (2)).doubleValue ();
    if (Double.isNaN (x) || Double.isNaN (y)) 
      continue;
    xySeries.add (x, y);
    // pointNames.add (pointName);
    }

  return new XYSeriesCollection (xySeries);

}  // createChartDataSet
//-----------------------------------------------------------------------------------------------
protected ArrayList assembleData (double [] x, double [] y, String [] names)
{
  //assert (x.length == y.length);
  //assert (x.length == names.length);

  ArrayList result = new ArrayList ();
  for (int i=0; i < x.length; i++) {
    ArrayList nameXY = new ArrayList ();
    nameXY.add (names [i]);
    nameXY.add (new Double (x [i]));
    nameXY.add (new Double (y [i]));
    result.add (nameXY);
    } // for i

  return result;

} // assembleData
//-----------------------------------------------------------------------------------------------
public void chartMouseClicked (ChartMouseEvent event)
{

}
//-----------------------------------------------------------------------------------------------
public void chartMouseMoved (ChartMouseEvent event)
{

}
//-----------------------------------------------------------------------------------------------
public String generateToolTip (XYDataset dataset, int series, int item)
{
  return pointNames [item];
}
//-----------------------------------------------------------------------------------------------
protected void setDragOperationMeaning (ActionEvent e, SelectableChartPanel chartPanel, int newMeaning)
{
  dragMeaning = newMeaning;

  boolean zooming = true;
  if (dragMeaning == DRAG_MEANS_SELECT)
    zooming = false;

  chartPanel.setEnableCustomDrag (!zooming);
  chartPanel.setMouseZoomable (zooming, true);
  chartPanel.setDomainZoomable (zooming);
  chartPanel.setRangeZoomable (zooming);
}
//-----------------------------------------------------------------------------------------------
/**
 * does rect contain item?  rect is the dimensions of the rectangle drawn by a user
 * with the mouse, and item is any of the 1 or more plot symbols (points, diamonds, etc)
 * drawn on the surface of the plot.  the multiple geometries are confusing, however,
 * and the following heuristic -- however obscure -- works:
 *   1) convert the rect coordinates via a call to ChartPanel.translateScreenToJava2D
 *   2) leave the entity coordinates as they are (except for promoting them to doubles)
 */
protected boolean contains (java.awt.Rectangle rect, ChartEntity item)
{
  int rectMinX = (int) rect.getMinX ();
  int rectMaxX = (int) rect.getMaxX ();
  int rectMinY = (int) rect.getMinY ();
  int rectMaxY = (int) rect.getMaxY ();

  Point2D lowerLeft = chartPanel.translateScreenToJava2D (new Point (rectMinX, rectMinY));
  Point2D upperRight = chartPanel.translateScreenToJava2D (new Point (rectMaxX, rectMaxY));

  Rectangle itemRect = item.getArea().getBounds();
  int itemCenterX = (int) itemRect.getCenterX ();
  int itemCenterY = (int) itemRect.getCenterY ();

  boolean result = false;

  if (itemCenterX >= lowerLeft.getX () &&
      itemCenterX <= upperRight.getX () &&
      itemCenterY >= lowerLeft.getY () &&
      itemCenterY <= upperRight.getY ())
    result = true;

  return result;

} // contains
//-----------------------------------------------------------------------------------------------
public void setDraggedRectangle (java.awt.Rectangle rect)
{
  EntityCollection entities = chartPanel.getChartRenderingInfo().getEntityCollection();
  Iterator it = entities.iterator ();

  currentSelection = new ArrayList ();  
  int i=0;
  while (it.hasNext ()) {
    ChartEntity item = (ChartEntity) it.next ();
    if (contains (rect, item)) {
      String entityName = item.getToolTipText();
      currentSelection.add (entityName);
      } // if
   } // while iterator
  
} // setDraggedRectangle
//-----------------------------------------------------------------------------------------------
public void save (File directory)
{
  System.out.println ("ScatterPlotter.save () not yet implemented");
  
} // save
//------------------------------------------------------------------------
public static void main (String [] args) 
{
  String [] pointNames = new String [] {"A", "B", "C", "D", "E", "F", "G"};
  double [] x = new double [] {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
  double [] y = new double [] {0.0, 1.0, 4.0, 9.0, 16.0, 25.0, 36.0};
  ScatterPlotter scatterPlotter = new ScatterPlotter (pointNames, x, y, "xTitle", "yTitle", "title");
  JFrame frame = new JFrame ();
  frame.getContentPane().add (scatterPlotter);
  frame.pack ();
  frame.setVisible (true);

} // main
//-----------------------------------------------------------------------------------------------
} // ScatterPlotter
