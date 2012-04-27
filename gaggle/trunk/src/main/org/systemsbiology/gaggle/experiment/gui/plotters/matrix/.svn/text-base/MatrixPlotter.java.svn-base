// MatrixPlotter.java
//------------------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui.plotters.matrix;
//------------------------------------------------------------------------------------------------
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.util.*;


import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.SymbolicXYItemLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ExtensionFileFilter;

import org.systemsbiology.gaggle.core.datatypes.DataMatrix;
import org.systemsbiology.gaggle.experiment.gui.Settings;
import org.systemsbiology.gaggle.experiment.readers.DataMatrixFileReader;
import org.systemsbiology.gaggle.experiment.metadata.*;
import org.systemsbiology.gaggle.util.NameHelper;
import org.systemsbiology.gaggle.util.NameHelperFactory;

//------------------------------------------------------------------------------------------------
public class MatrixPlotter extends JPanel {

  DataMatrix dataMatrix;
  String xAxisLabel;
  String yAxisLabel;
  int preferredWidth;
  int preferredHeight;
  HashMap seriesHash;
  XYSeriesCollection dataset;
  XYPlot plot;
  StandardXYItemRenderer renderer;
  JButton showConditionsToggleButton;
  String [] xAxisConditionNames;
  NameHelper nameHelper;
  Settings settings = Settings.getInstance();

//------------------------------------------------------------------------------------------------
public MatrixPlotter (org.systemsbiology.gaggle.core.datatypes.DataMatrix datamatrix)
{
  xAxisLabel = null;
  preferredWidth = 800;
  preferredHeight = 600;
  ToolTipManager.sharedInstance ().setInitialDelay(0);
  dataMatrix = datamatrix;
  nameHelper = NameHelperFactory.getNameHelper(datamatrix.getSpecies());
  setLayout (new BorderLayout ());
  xAxisConditionNames = datamatrix.getColumnTitles ();
  seriesHash = new HashMap ();
  dataset = createDataSet (datamatrix);
  plot = createPlot (dataset);
  ChartPanel chartpanel = createChartPanel (createChart (plot));
  add (chartpanel, "Center");
  JPanel jpanel = new JPanel ();
  addConditionNameDisplayController (jpanel);
  add (jpanel, "South");

} // ctor
//------------------------------------------------------------------------------------------------
protected void addConditionNameDisplayController (JPanel jpanel)
{
  ButtonGroup radioButtonGroup = new ButtonGroup ();
  JRadioButton fullButton = new JRadioButton ("Full");
  JRadioButton briefButton = new JRadioButton ("Brief");
  JRadioButton hideButton = new JRadioButton ("Hide");
  
  radioButtonGroup.add (fullButton);
  radioButtonGroup.add (briefButton);
  radioButtonGroup.add (hideButton);
  jpanel.add (fullButton);
  jpanel.add (briefButton);
  jpanel.add (hideButton);

  fullButton.addActionListener (new ActionListener () {
    public void actionPerformed (ActionEvent e) {
      changeConditionNameDisplay ("full");
      }});

  briefButton.addActionListener (new ActionListener () {
    public void actionPerformed (ActionEvent e) {
      changeConditionNameDisplay ("brief");
      }});

  hideButton.addActionListener (new ActionListener () {
    public void actionPerformed (ActionEvent e) {
      changeConditionNameDisplay ("hide");
      }});
  
  // select button for style from settings
  String style = settings.getConditionNameDisplayStyle();
  if ("full".equalsIgnoreCase(style))
    fullButton.setSelected(true);
  else if ("brief".equalsIgnoreCase(style))
    briefButton.setSelected(true);
  else if ("hide".equalsIgnoreCase(style))
    hideButton.setSelected(true);
  else
    fullButton.setSelected(true);

  // apply style from settings
  changeConditionNameDisplay (style);

} // addConditionNameDisplayController
//------------------------------------------------------------------------------------------------
public String getSpecies ()
{
    return dataMatrix.getSpecies ();
}
//------------------------------------------------------------------------------------------------
public String getCanonicalName (String s)
{
    return nameHelper.getName ("canonical", s);
}
//------------------------------------------------------------------------------------------------
public String getCommonName (String s)
{
  return nameHelper.getName ("common", s);
}
//------------------------------------------------------------------------------------------------
protected void changeConditionNameDisplay (String newStyle)
{
  int i = xAxisConditionNames.length;
  String newNames [] = new String [i];
  String buttonTitle = "";

  if (newStyle==null || newStyle.equalsIgnoreCase ("full"))
    newNames = xAxisConditionNames;

  else if (newStyle.equalsIgnoreCase ("brief")) {
    for (int j = 0; j < xAxisConditionNames.length; j++)
      newNames [j] =  ConditionNameShortener.shorten(xAxisConditionNames [j]);
    } // brief

  else if (newStyle.equalsIgnoreCase ("hide")) {
    for (int j = 0; j < xAxisConditionNames.length; j++)
      newNames [j] = "";
    } // hide

  SymbolicAxis symbolicaxis = new SymbolicAxis (xAxisLabel, newNames);
  SymbolicTickUnit symbolictickunit = new SymbolicTickUnit (1.0D, newNames);
  symbolicaxis.setTickUnit (symbolictickunit);
  symbolicaxis.setVerticalTickLabels (true);
  plot.setDomainAxis (symbolicaxis);
  
  // record user's preference and reapply in the future
  settings.setConditionNameDisplayStyle(newStyle);

} // changeConditionNameDisplay
//------------------------------------------------------------------------------------------------
protected ChartPanel createChartPanel (JFreeChart jfreechart)
{
  ChartPanel chartpanel = new ChartPanel (jfreechart) {
    /**
     * here we override ChartPanel.soSaveAs to remember a working directory
     */
    public void doSaveAs() throws IOException {
      System.out.println("working directory = " + String.valueOf(settings.getWorkingDirectory()));
      JFileChooser fileChooser = new JFileChooser(settings.getWorkingDirectory());
      ExtensionFileFilter filter = new ExtensionFileFilter(
          localizationResources.getString("PNG_Image_Files"), ".png"
      );
      fileChooser.addChoosableFileFilter(filter);

      int option = fileChooser.showSaveDialog(this);
      if (option == JFileChooser.APPROVE_OPTION) {
          String filename = fileChooser.getSelectedFile().getPath();
          settings.setWorkingDirectory(
              fileChooser.getSelectedFile().getParentFile().getPath());
          if (isEnforceFileExtensions()) {
              if (!filename.endsWith(".png")) {
                  filename = filename + ".png";
              }
          }
          ChartUtilities.saveChartAsPNG(
              new File(filename), getChart(), getWidth(), getHeight()
          );
      }
    }
  };
  chartpanel.setSize (preferredWidth, preferredHeight);
  chartpanel.setPreferredSize (new Dimension (preferredWidth, preferredHeight));
  chartpanel.setDomainZoomable (true);
  chartpanel.setRangeZoomable (true);
  chartpanel.setMouseZoomable (true);
  chartpanel.setBorder (new EmptyBorder (20, 5, 5, 5));
  LineBorder lineborder = new LineBorder (Color.BLACK, 2);
  chartpanel.setBorder (lineborder);
  chartpanel.setSize (preferredWidth, preferredHeight);
  chartpanel.setPreferredSize (new Dimension (preferredWidth, preferredHeight));
  return chartpanel;
}
//------------------------------------------------------------------------------------------------
protected JFreeChart createChart (XYPlot xyplot)
{
  boolean flag = false;
  JFreeChart jfreechart = new JFreeChart (null, JFreeChart.DEFAULT_TITLE_FONT, xyplot, flag);
  return jfreechart;
}
//------------------------------------------------------------------------------------------------
protected XYPlot createPlot (XYSeriesCollection xyseriescollection)
{
  xAxisLabel = null;
  yAxisLabel = dataMatrix.getDataTypeBriefName ();
  NumberAxis numberaxis = new NumberAxis (yAxisLabel);
  SymbolicAxis symbolicaxis = new SymbolicAxis (xAxisLabel, xAxisConditionNames);
  SymbolicTickUnit symbolictickunit = new SymbolicTickUnit (1.0D, xAxisConditionNames);
  symbolicaxis.setTickUnit (symbolictickunit);
  symbolicaxis.setVerticalTickLabels (true);
  renderer = new StandardXYItemRenderer (3, new SymbolicXYItemLabelGenerator ());
  renderer.setShape (new java.awt.geom.Rectangle2D.Double (-1.75D, -1.75D, 3.5D, 3.5D));
  for (int i = 0; i < xyseriescollection.getSeriesCount (); i++)  {
    NamedXYSeries namedxyseries =  (NamedXYSeries) xyseriescollection.getSeries (i);
    Color color = namedxyseries.getColor ();
    renderer.setSeriesPaint (i, color);
    }

  XYPlot xyplot = new XYPlot (xyseriescollection, symbolicaxis, numberaxis, renderer);
  xyplot.setDomainGridlinesVisible (true);
  xyplot.setRangeGridlinesVisible (true);
  xyplot.getRenderer ().setToolTipGenerator (new ToolTipGenerator ());
  return xyplot;

} // createPlot
//------------------------------------------------------------------------------------------------
protected Color [] getAvailableColors ()
{
  Color color0 = new Color (255, 0, 0);
  Color color1 =  (new Color (0, 255, 0)).darker ().darker ();
  Color color2 =  (new Color (0, 0, 255)).darker ().darker ();
  Color color3 = Color.CYAN.darker ().darker ().darker ();
  Color color4 = Color.MAGENTA.darker ().darker ().darker ();
  Color color5 = Color.ORANGE.darker ().darker ();
  Color color6 = Color.PINK.darker ().darker ();
  Color color7 = Color.BLACK;
  return  (new Color[] {color0, color1, color2, color3, color4, color5, color6, color7});
}
//------------------------------------------------------------------------------------------------
protected XYSeriesCollection createDataSet (org.systemsbiology.gaggle.core.datatypes.DataMatrix datamatrix)
{
  boolean autoSort = false;
  String rowNames [] = datamatrix.getRowTitles ();
  XYSeriesCollection newDataSet = new XYSeriesCollection ();
  Color colors [] = getAvailableColors ();
  for (int i = 0; i < rowNames.length; i++) {
    String rowName = rowNames [i];
    NamedXYSeries namedXYSeries = new NamedXYSeries (rowName, autoSort);
    namedXYSeries.setCommonName (rowName.toLowerCase ());
    int j = i % colors.length;
    namedXYSeries.setColor (colors [j]);
    double values [] = datamatrix.get (i);
    for (int k = 0; k < values.length; k++) {
      double d =  (new Double (k)).doubleValue ();
      namedXYSeries.add (d, values [k]);
      } // for k

    newDataSet.addSeries (namedXYSeries);
    seriesHash.put (rowName, namedXYSeries);
    } // for i

  return newDataSet;

} // createDataSet
//------------------------------------------------------------------------------------------------
public DataMatrix getDataMatrix ()
{
   return dataMatrix;
}
//------------------------------------------------------------------------------------------------
public Color getColor (String s)
{
  Color color = Color.BLACK;
  if (seriesHash.containsKey (s)) {
    NamedXYSeries namedXYSeries =  (NamedXYSeries)seriesHash.get (s);
    color = namedXYSeries.getColor ();
    }

  return color;
}
//------------------------------------------------------------------------------------------------
/**
 * 'gray-out' all plot lines except those in 'names'
 *
 */
public void select (String names [])
// strategy and notes
//   1) remove all series (xy pairs providing the data in a plotted line) from the dataset 
//      (the collection of all series which determine what is plotted).
//   2) as the series are removed, sort them into two bins:  selected (to be re-added, first,
//      and displayed in their normal color) and deselected (to be re-added last, and
//      displayed in gray). 
//   3) note:  jfreechart seems, by default, to draw the lines in 
//      reverse order, so by adding selected items -first- they get drawn last, on top
//      of the gray lines.
// from posting at the jfree.org (http://www.jfree.org/phpBB2/viewtopic.php?t=13949&)
// i learned of 
//    - XYPlot.getDatasetRenderingOrder() and 
//    - XYPlot.setDatasetRenderingOrder (DatasetRenderingOrder order);  FORWARD|REVERSE
//    - XYPlot.get|setSeriesRendieringOrder
// which, though they didn't solve my problem, are at least related to it. both datasets
// and series are rendered in this order:  first added, last rendered -- ensuring that
// series added first end up on top.
{
  String sortedNames [] =  (String []) names.clone ();  // required for binarySearch, below
  Arrays.sort (sortedNames);

  ArrayList selected = new ArrayList ();
  ArrayList deselected  = new ArrayList ();

  int max = dataset.getSeriesCount () - 1;
  for (int i = max; i >= 0; i--)  {
    NamedXYSeries series = (NamedXYSeries) dataset.getSeries (i);
    String canonicalName = series.getCanonicalName ();
    if (Arrays.binarySearch (sortedNames, canonicalName) < 0)
      deselected.add (series);
    else
      selected.add (series);
    dataset.removeSeries (series);
    }

  for (int i=0; i < selected.size (); i++) {
    NamedXYSeries series = (NamedXYSeries) selected.get (i);
    dataset.addSeries (series);
    Color color = series.getColor ();
    String name = series.getCanonicalName ();
    int index = dataset.getSeriesCount () - 1;
    renderer.setSeriesPaint (index, color);
    } // for i

  for (int i=0; i < deselected.size (); i++) {
    NamedXYSeries series = (NamedXYSeries) deselected.get (i);
    dataset.addSeries (series);
    String name = series.getCanonicalName ();
    int index = dataset.getSeriesCount () - 1;
    renderer.setSeriesPaint (index,  Color.lightGray);
    } // for i

} // select
//------------------------------------------------------------------------------------------------
class ToolTipGenerator implements XYToolTipGenerator  {

  public String generateToolTip (XYDataset xydataset, int i, int j)   {
      XYSeriesCollection xyseriescollection =  (XYSeriesCollection)xydataset;
      //double d = java.lang.Double.valueOf (xyseriescollection.getXValue (i, j)).doubleValue ();
      //double d1 = java.lang.Double.valueOf (xyseriescollection.getYValue (i, j)).doubleValue ();
      double y = java.lang.Double.valueOf (xyseriescollection.getYValue (i, j)).doubleValue ();

      String rowName = ((NamedXYSeries)xyseriescollection.getSeries (i)).getCanonicalName ();
      String tooltip = "<html><b>" + rowName;
       tooltip += ", " + xAxisConditionNames [j] +   ", " + y + "</b>";

      try {
          MetaDataNavigator navigator = MetaDataNavigatorSingleton.getInstance().getNavigator();
          //MetaData metaData  = navigator.getMetaDataForCondition(xAxisConditionNames[j]);
          //Condition condition = metaData.getCondition(xAxisConditionNames[j]);
          Condition condition = navigator.getConditionForSpecificExperiment(xAxisConditionNames[j],
                  rowName);

          String s = "<br>\n";
          for (Variable v : condition.getVariables()) {
              s += v.getName();
              s += ": ";
              s += v.getValue();
              if (v.getUnits() != null) {
                  s += " ";
                  s += v.getUnits();
              }
              s += "<br>\n";
          }
          tooltip =  tooltip + s;
      } catch (NullPointerException e) {
          return tooltip + "</html>";
      }
      return tooltip + "</html>";
    }

} // inner class ToolTipGenerator
//------------------------------------------------------------------------------------------------
public static void main (String [] args) throws Exception
{
  try {
    String s = "../../../sampleData/simpleMatrix.txt";
    if (args.length == 1)
        s = args[0];
    JFrame jframe = new JFrame ("MatrixPlotter");
    DataMatrixFileReader datamatrixfilereader = new DataMatrixFileReader (s);
    datamatrixfilereader.read ();
    MatrixPlotter matrixplotter = new MatrixPlotter (datamatrixfilereader.get ());
    jframe.add (matrixplotter);
    jframe.pack ();
    jframe.setVisible (true);
    }
  catch (Exception ex0) {
    System.err.println (ex0.getMessage ());
    ex0.printStackTrace ();
    }

} // main
//------------------------------------------------------------------------------------------------

} // class MatrixPlotter
