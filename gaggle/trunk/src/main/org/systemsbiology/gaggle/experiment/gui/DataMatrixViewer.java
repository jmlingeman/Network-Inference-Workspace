// DataMatrixViewer.java
//---------------------------------------------------------------------------------------
// $Revision: 2360 $
// $Date: 2005/04/13 02:02:58 $
// $Author: dtenenba $
//---------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui;
//---------------------------------------------------------------------------------------
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.event.*;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.io.*;
import java.awt.datatransfer.*;

import org.systemsbiology.gaggle.experiment.datamatrix.*;
import org.systemsbiology.gaggle.experiment.metadata.*;

import org.systemsbiology.gaggle.util.*;
import org.systemsbiology.gaggle.experiment.gui.plotters.matrix.*;
import org.systemsbiology.gaggle.experiment.gui.plotters.scatter.*;
import org.systemsbiology.gaggle.core.datatypes.*;

//---------------------------------------------------------------------------------------
public class DataMatrixViewer extends JPanel implements MatrixViewCoordinator, ClipboardOwner {

  protected JTabbedPane tabbedPane;
  protected File currentDirectory;
  protected NameHelper nameHelper;
  protected ArrayList metaData;
  protected MetaDataNavigator navigator;

  protected org.systemsbiology.gaggle.core.datatypes.DataMatrix[] matrices;
  protected ArrayList matrixList = new ArrayList ();

  //protected JPanel buttonPanel;  
  protected JPanel controlPanel;
  protected JPanel movieController;
  
  private JButton butNameType;
  protected JToolBar toolbar;
  private Date lastModificationTime = new Date ();
  ArrayList<DataMatrixView> allViews;
  protected HashMap plotTabNames = new HashMap ();
  protected HashMap widgets = new HashMap ();

  int currentTabIndex = -1;  
//---------------------------------------------------------------------------------------
public DataMatrixViewer (org.systemsbiology.gaggle.core.datatypes.DataMatrix[] matrices, MetaDataNavigator navigator) throws Exception
{
  super ();
  //this.metaData = new ArrayList ();
  //if (experimentMetaData != null)
  //  addMetaData (experimentMetaData);
  this.navigator = navigator;
  currentDirectory = new File (System.getProperty ("user.dir"));

  //this.metaData = metaData; // TODO - do something with this to get a NameHelper
  String species = "unknown";
  if (matrices.length > 0 && matrices[0].getSpecies() != null)
    species = matrices [0].getSpecies ();
  this.nameHelper = NameHelperFactory.getNameHelper (species);

  setBorder (BorderFactory.createEmptyBorder (0, 10, 10, 10));
  allViews = new ArrayList ();
  add (createGui (matrices), BorderLayout.CENTER);
    
} // ctor
//---------------------------------------------------------------------------------------
public DataMatrixViewer (MetaDataNavigator navigator) throws Exception
{
  this (new org.systemsbiology.gaggle.core.datatypes.DataMatrix[0], navigator);
}
//---------------------------------------------------------------------------------------

//---------------------------------------------------------------------------------------
public void addMetaData (ArrayList experimentMetaData)
{
  for (int i=0; i < experimentMetaData.size (); i++) {
//    metaData.add ((MetaData) experimentMetaData.get (i));
    }
}
//------------------------------------------------------------------------------
JTabbedPane createGui (org.systemsbiology.gaggle.core.datatypes.DataMatrix[] matrices)
{
  toolbar = new JToolBar ();
  setLayout (new BorderLayout ());
  add (toolbar, BorderLayout.NORTH);

  tabbedPane = new JTabbedPane ();
  tabbedPane.setUI (new MyTabbedPaneUI ());
     
  for (int i=0; i < matrices.length; i++) {
    addMatrixSpreadsheetView (matrices [i], navigator);
    } // for i

  if (matrices.length > 0)
    tabbedPane.setSelectedIndex (0);  // make the first-loaded dataset visible
  return tabbedPane;

} // createGui

public MetaDataNavigator getMetaDataNavigator() {
    return navigator;
}
//------------------------------------------------------------------------------
public MatrixSpreadsheet getTopSpreadsheet ()
{
  int topTabIndex = tabbedPane.getSelectedIndex ();
  String tabType = getTabType (topTabIndex);
  if (!getTabType(topTabIndex).equals ("MatrixSpreadsheet"))
    return null;

  MatrixSpreadsheet topSpreadsheet = (MatrixSpreadsheet) allViews.get (topTabIndex);
 
  return (topSpreadsheet);

} // getTopSpreadsheet
//------------------------------------------------------------------------------
/**
 * when a tab changes, and if the new topmost tab contains a MatrixSpreadsheet,
 * check to see if that matrix has a matching matrix -- same row & column names --
 * anywhere else in the tab set.  if so, then pass that information on to that
 * MatrixSpreadsheet, so that it can enable its 'volcano plot' button; so that it
 * can store the index of that matching matrix, to use whenever that volcano plot
 * button is pressed.
 *
 */
class TabChangeListener implements ChangeListener {

  public void stateChanged (ChangeEvent e) {
    JTabbedPane pane = (JTabbedPane) e.getSource ();
    int topTabIndex = pane.getSelectedIndex ();
    String tabType = getTabType (topTabIndex);
    if (!getTabType(topTabIndex).equals ("MatrixSpreadsheet"))
      return;
    MatrixSpreadsheet topSpreadsheet = (MatrixSpreadsheet) allViews.get (topTabIndex);
    //topSpreadsheet = (MatrixSpreadsheet) allViews.get (topTabIndex);
    org.systemsbiology.gaggle.core.datatypes.DataMatrix topMatrix = topSpreadsheet.getMatrix ();
    String [] topMatrixRowNames = topMatrix.getRowTitles ();
    String [] topMatrixColumnNames = topMatrix.getColumnTitles ();
    topSpreadsheet.setCompanionMatrixID (-1);

    for (int i=0; i < allViews.size (); i++) {
      if (i == topTabIndex) continue;  // don't check the top matrix against itself
      if (getTabType(i).equals ("MatrixSpreadsheet")) {
        MatrixSpreadsheet spreadsheet = (MatrixSpreadsheet) allViews.get (i);
        org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix = ((MatrixSpreadsheet) allViews.get (i)).getMatrix ();
        String [] rowNames = matrix.getRowTitles ();
        String [] columnNames = matrix.getColumnTitles ();
        if (Arrays.equals (topMatrixRowNames, rowNames) &&
            Arrays.equals (topMatrixColumnNames, columnNames))
          topSpreadsheet.setCompanionMatrixID (i); // this matrix congruent to top matrix
        } // if MatrixSpreadsheet
      } // for i
   } // stateChanged

} // inner class TabChangeListener
//------------------------------------------------------------------------------
protected void updateCongruentMatrixInfo (int topTabIndex)
{
  if (topTabIndex < 0)
    return;
  String tabType = getTabType (topTabIndex);
  if (!getTabType(topTabIndex).equals ("MatrixSpreadsheet"))
    return;
  MatrixSpreadsheet topSpreadsheet = (MatrixSpreadsheet) allViews.get (topTabIndex);
  org.systemsbiology.gaggle.core.datatypes.DataMatrix topMatrix = topSpreadsheet.getMatrix ();
  String [] topMatrixRowNames = topMatrix.getRowTitles ();
  String [] topMatrixColumnNames = topMatrix.getColumnTitles ();
  topSpreadsheet.setCompanionMatrixID (-1);

  for (int i=0; i < allViews.size (); i++) {
    if (i == topTabIndex) continue;  // don't check the top matrix against itself
    if (getTabType(i).equals ("MatrixSpreadsheet")) {
      MatrixSpreadsheet spreadsheet = (MatrixSpreadsheet) allViews.get (i);
      org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix = ((MatrixSpreadsheet) allViews.get (i)).getMatrix ();
      String [] rowNames = matrix.getRowTitles ();
      String [] columnNames = matrix.getColumnTitles ();
      if (Arrays.equals (topMatrixRowNames, rowNames) &&
          Arrays.equals (topMatrixColumnNames, columnNames)) {
        topSpreadsheet.setCompanionMatrixID (i);
        } // if this matrix congruent to top matrix
      } // if MatrixSpreadsheet
    } // for i

} // updateCongruentMatrixInfo
//-----------------------------------------------------------------------------------
public JToolBar getToolBar ()
{
  return toolbar;
}
//-----------------------------------------------------------------------------------
public String getTabType (int index) throws IllegalArgumentException
{
  if (!inRange (index))
    throw new IllegalArgumentException ("out of range, max is " + (getTabCount () - 1));

  return ((DataMatrixView) allViews.get (index)).getClassName ();
}
//-----------------------------------------------------------------------------------
public int getTabCount ()
{
  return tabbedPane.getTabCount ();
}
//-----------------------------------------------------------------------------------
public void addButtonToToolbar (JButton button)
{
  button.setBackground(Color.WHITE);
  toolbar.add (button);
}
//-----------------------------------------------------------------------------------
class MyTabbedPaneUI extends BasicTabbedPaneUI {
  public MyTabbedPaneUI () {super(); }

  protected void paintTab (Graphics g, int tabPlacement, Rectangle[] rects,
                           int tabIndex, Rectangle iconRect, Rectangle textRect) {

    super.paintTab (g, tabPlacement, rects, tabIndex, iconRect, textRect);
    Rectangle rect = rects [tabIndex];
    g.setColor(Color.black);
    g.drawRect(rect.x + 5, rect.y + 5, 10, 10);
    g.drawLine(rect.x + 5, rect.y + 5, rect.x + 15, rect.y + 15);
    g.drawLine(rect.x + 15, rect.y + 5, rect.x + 5, rect.y + 15);
    }

  protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
    return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + 20;
    }

  protected MouseListener createMouseListener() {
    return new MyMouseHandler();
    }

  class MyMouseHandler extends MouseHandler {
    public MyMouseHandler() { super();}

    public void mouseClicked (MouseEvent e) {
      int x = e.getX();
      int y = e.getY();
      currentTabIndex = -1;
      int tabCount = tabPane.getTabCount();
      for (int i = 0; i < tabCount; i++) {
        if (rects[i].contains(x, y)) {
          currentTabIndex = i;
          break;
          } // if contains
        } // for i
      if (currentTabIndex >= 0) {
        Rectangle tabRect = rects[currentTabIndex];
        x = x - tabRect.x;
        y = y - tabRect.y;
        if ((x >= 5) && (x <= 15) && (y >= 5) && (y <= 15)) {
            closeTab(currentTabIndex);
        } // if
         } // if currentTabIndex >= 0
       System.gc ();
       updateCongruentMatrixInfo (tabPane.getSelectedIndex ());
       } // mouseClicked

  } // inner-inner class MyMouseHandler



} // inner class MyTabbedPaneUi

    public void closeTab(int tabIndex) {
        try {
          String viewType = ((DataMatrixView) allViews.get (tabIndex)).getClassName ();
          tabbedPane.remove (tabIndex);
          allViews.remove (tabIndex);
          if (viewType.equals ("MatrixSpreadsheet"))
              matrixList.remove (tabIndex);
          lastModificationTime = new Date();
          //System.out.println ("tab deletion, allViews (" + allViews.size () +
          //             ")  tab panels (" + tabbedPane.getTabCount () +
          //             ") matrices (" + matrixList.size () + ")");
          } catch (Exception ex) {ex.printStackTrace();}
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
//------------------------------------------------------------------------
public JTabbedPane getTabbedPane() 
{
  return tabbedPane;
}
//-------------------------------------------------------------------------------
public void lostOwnership (Clipboard clipboard, Transferable contents) {}
//-------------------------------------------------------------------------------
public void select (String species, String [] names)
{
  DataMatrixView [] views = (DataMatrixView []) allViews.toArray (new DataMatrixView [0]);
  for (int i=0; i < views.length; i++) {
    views [i].select (species, names);
    }

}
//-------------------------------------------------------------------------------
public void clearSelections ()
{
  DataMatrixView [] views = (DataMatrixView []) allViews.toArray (new DataMatrixView [0]);
  for (int i=0; i < views.length; i++)
    views [i].clearSelection ();

}
//-------------------------------------------------------------------------------
/**
 *  get the species from the topmost -- the visible -- tab
 */
public String getSpecies ()
{
  int topTab = tabbedPane.getSelectedIndex ();
  if (topTab < 0)
     return "unknown";

  if (allViews == null || allViews.size () == 0)
    return "unknown";

  DataMatrixView topView = (DataMatrixView) allViews.get (topTab);
  return topView.getSpecies ();

} // getSpecies
//-------------------------------------------------------------------------------
/**
 *  get the selection from the topmost -- the visible -- tab
 */
public String [] getSelection ()
{
  int topTab = tabbedPane.getSelectedIndex ();
  if (topTab < 0)
    return new String [0];

  if (allViews == null || allViews.size () == 0)
    return new String [0];

  if (topTab >= allViews.size ())
    return new String [0];

  DataMatrixView topView = (DataMatrixView) allViews.get (topTab);
  String [] selectedNames = ((DataMatrixView) allViews.get (topTab)).getSelection ();
  return selectedNames;

} // getSelection
//-------------------------------------------------------------------------------
public int getIndexOfSelectedMatrix ()
{
  return tabbedPane.getSelectedIndex ();
}
//-------------------------------------------------------------------------------
public org.systemsbiology.gaggle.core.datatypes.DataMatrix getMatrix (int index)
{
  if (index < 0 || index >= allViews.size ()) {
    System.err.println ("DataMatrixViewer.getMatrix () index out of range");
    System.err.println ("rec'd " + index + ", max is " + (allViews.size () - 1));
    return null;
    }

  org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix = ((MatrixSpreadsheet) allViews.get (index)).getMatrix ();
  return matrix;

} // getMatrix
//-------------------------------------------------------------------------------
public LensedDataMatrix getLensedMatrix (int index)
{
  if (index < 0 || index >= allViews.size ()) {
    System.err.println ("DataMatrixViewer.getLens () index out of range");
    System.err.println ("rec'd " + index + ", max is " + (allViews.size () - 1));
    return null;
    }

  LensedDataMatrix lensedMatrix = ((MatrixSpreadsheet) allViews.get (index)).getLens ();
  System.out.println ("DataMatrix.getLensedMatrix (" + index + ") rows: " + 
                      lensedMatrix.getRowCount () + "  cols: " + lensedMatrix.getColumnCount ());
  return lensedMatrix;

} // getLens
//-------------------------------------------------------------------------------
/**
 *  get the selection from the topmost -- the visible -- tab
 */
public org.systemsbiology.gaggle.core.datatypes.DataMatrix getSelectedMatrix ()
{
  return getMatrix (getIndexOfSelectedMatrix ());

} // getSelectedMatrix
//-------------------------------------------------------------------------------
// todo refactor this to take a Cluster object    
public void handleCluster (Cluster cluster)
{
  DataMatrixView [] views = (DataMatrixView []) allViews.toArray (new DataMatrixView [0]);
  for (int i=0; i < views.length; i++) {
     if (views [i].getClassName().equals ("MatrixSpreadsheet")) {
       MatrixSpreadsheet mss = (MatrixSpreadsheet) views [i];
       mss.selectSubTable (cluster.getRowNames(), cluster.getColumnNames());
       } // if spreadsheet
     } // for i
  
} // handleCluster
//-------------------------------------------------------------------------------
public Date getLastModificationTime ()
{
  return lastModificationTime;
} 
//-------------------------------------------------------------------------------
public void addMatrixSpreadsheetView (org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix, MetaDataNavigator navigator)
{
  String shortName = matrix.getShortName ();
  try {
    MatrixSpreadsheet mss = new MatrixSpreadsheet (matrix, this, navigator);
    allViews.add (mss);
    tabbedPane.add (shortName, mss);
    int newTopTabIndex = tabbedPane.getTabCount() - 1;
    tabbedPane.setSelectedIndex (newTopTabIndex);
    lastModificationTime = new Date ();
    matrixList.add (matrix);
    updateCongruentMatrixInfo (newTopTabIndex);
    }
  catch (Exception ex0) {
    System.err.println ("error creating spreadsheet panel for " + 
                         shortName + ": " + ex0.getMessage());
    ex0.printStackTrace ();
    } // catch

} // addMatrixSpreadsheetView

    public void addView(DataMatrixView view) {
        System.out.println("handling view: " + view.getClass().getName());
        System.out.println("handling view: " + view.getClassName());
        System.out.println("");

        if (view instanceof SelectableMatrixPlotter) {
            SelectableMatrixPlotter plotter = (SelectableMatrixPlotter)view;
            allViews.add (plotter);
            tabbedPane.add (plotter.getName(), plotter);
            tabbedPane.setSelectedIndex (tabbedPane.getTabCount () - 1);
        } else if (view instanceof MatrixSpreadsheet) {
            MatrixSpreadsheet mss = (MatrixSpreadsheet)view;
            addMatrixSpreadsheetView(mss.getMatrix(), navigator);
        } else if (view instanceof ScatterPlotter) {
            ScatterPlotter plotter = (ScatterPlotter)view;
            allViews.add(plotter);
            tabbedPane.add(plotter.getName(), plotter);
            tabbedPane.setSelectedIndex (tabbedPane.getTabCount () - 1);

        }
    }
//-------------------------------------------------------------------------------
protected boolean inRange (int viewIndex)
{
  return (viewIndex >= 0 && viewIndex < getTabCount ());
}
//-------------------------------------------------------------------------------
public LensedDataMatrix [] getAllLenses ()
{
  ArrayList tmp = new ArrayList ();
  for (int i=0; i < allViews.size (); i++) {
    DataMatrixView view = (DataMatrixView) allViews.get (i);
    if (view.getClassName ().equals ("MatrixSpreadsheet")) {
      LensedDataMatrix lens = ((MatrixSpreadsheet)view).getLens ();
      tmp.add (lens);
      }  // if mss
    }  // for i
   return (LensedDataMatrix []) tmp.toArray (new LensedDataMatrix [0]);

}
    public DataMatrixView[] getAllViews() {
        return allViews.toArray(new DataMatrixView[0]);
    }

    public DataMatrixView[] getAllMatrixSpreadsheets() {
        List<DataMatrixView> spreadsheets = new ArrayList<DataMatrixView>();
        for (DataMatrixView view : allViews) {
            if (view.getClassName().equals("MatrixSpreadsheet")) {
                spreadsheets.add(view);
            }
        }

        return spreadsheets.toArray(new DataMatrixView[0]);
    }

    public org.systemsbiology.gaggle.core.datatypes.DataMatrix[] getAllMatrices ()
    {
      ArrayList tmp = new ArrayList ();
      for (int i=0; i < allViews.size (); i++) {
        DataMatrixView view = (DataMatrixView) allViews.get (i);
        if (view.getClassName ().equals ("MatrixSpreadsheet")) {
          org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix = ((MatrixSpreadsheet)view).getMatrix();
          tmp.add (matrix);
          }
        }  
       return (org.systemsbiology.gaggle.core.datatypes.DataMatrix[]) tmp.toArray (new org.systemsbiology.gaggle.core.datatypes.DataMatrix[0]);

    }


    //-------------------------------------------------------------------------------
public DataMatrixView getDataMatrixView (int i)
{
  if (!inRange (i))
    throw new IllegalArgumentException ("out of range, max is " + (getTabCount () - 1));

  return (DataMatrixView) allViews.get (i);

}
//-------------------------------------------------------------------------------
public void doBroadcast (String [] selectedNames)
{

}
//-------------------------------------------------------------------------------
public void doMatrixBroadcast (String [] selectedNames)
{
  //System.out.println ("broadcast selection: " + selectedNames.length);
}
//-------------------------------------------------------------------------------
/**
 * required of the MatrixViewCoordinator interface, so that a MatrixView (a
 * spreadsheet view, for instance) can ask for a plot of some selection it has
 */
public void doPlot (LensedDataMatrix lens)
{
  String name = lens.getShortName ();
  StringBuffer sb = new StringBuffer ();
  sb.append (lens.getShortName ());
  sb.append (" (plot");

  int count = 0;
  if (plotTabNames.containsKey (name))
    count = ((Integer) plotTabNames.get (name)).intValue ();
  count += 1;
  plotTabNames.put (name, new Integer (count));
  if (count > 1) {
    sb.append (" #");
    sb.append ((new Integer (count)).toString ());
    }
  sb.append (")");
  String tabName = sb.toString ();  
  SelectableMatrixPlotter plotter = new SelectableMatrixPlotter (lens);
  allViews.add (plotter);
  tabbedPane.add (tabName, plotter);
  tabbedPane.setSelectedIndex (tabbedPane.getTabCount () - 1);

} // doPlot
//-------------------------------------------------------------------------------
/**
 * required of the MatrixViewCoordinator interface, so that a MatrixView (a
 * spreadsheet view, for instance) can ask for a plot of some selection it has
 */
public void doVolcanoPlot (org.systemsbiology.gaggle.core.datatypes.DataMatrix topMostMatrix, String columnName, int companionMatrixID)
{
    //---------------------------------------------------------------
    // extract the named column from the matrix:  this is the x vector
    //---------------------------------------------------------------

  String [] columnNames = topMostMatrix.getColumnTitles ();
  int desiredColumn = -1;
  for (int i=0; i < columnNames.length; i++) {
    if (columnNames [i].equals (columnName)) {
      desiredColumn = i;
      break;
      }
    } // for i

  // assert (desiredColumn >= 0);
  double [] x = topMostMatrix.getColumn (desiredColumn);
  String [] pointNames = topMostMatrix.getRowTitles ();

  MatrixSpreadsheet companionSpreadsheet = (MatrixSpreadsheet) allViews.get (companionMatrixID);
  org.systemsbiology.gaggle.core.datatypes.DataMatrix companionMatrix = companionSpreadsheet.getMatrix ();

  double [] y = companionMatrix.getColumn (columnName);
  StringBuffer sb = new StringBuffer ();
  sb.append (columnName);

  boolean [] goodRows = new boolean [x.length];
  int goodRowCount = 0;
  for (int i=0; i < x.length; i++) {
    if (Double.isNaN (x [i]) || Double.isNaN (y [i]))
      goodRows [i] = false;
    else {
      goodRows [i] = true;
      goodRowCount++;
      }
    } // for i
      
  System.out.println ("dmv.doVolcanoPlot, from original count " + x.length + " to " + goodRowCount);
  String [] cleanNames = new String [goodRowCount];
  double [] cleanX  = new double [goodRowCount];
  double [] cleanY  = new double [goodRowCount];

  int index = 0;
  for (int i=0; i < goodRows.length; i++) {
    if (goodRows [i]) {
      cleanNames [index] = pointNames [i];
      cleanX [index] = x [i];
      cleanY [index] = y [i];
      index++;
      } // if good
   } // for i

  int count = 0;
  if (plotTabNames.containsKey (columnName))
    count = ((Integer) plotTabNames.get (columnName)).intValue ();
  count += 1;
  plotTabNames.put (columnName, new Integer (count));
  if (count > 1) {
    sb.append (" #");
    sb.append ((new Integer (count)).toString ());
    }
  String tabName = sb.toString ();

  String dataTypeOfSuppliedMatrix = topMostMatrix.getDataTypeBriefName ();
  String dataTypeOfCompanionMatrix = companionMatrix.getDataTypeBriefName ();

  ScatterPlotter scatterPlotter = new ScatterPlotter (cleanNames, cleanX, cleanY, 
                                                      dataTypeOfSuppliedMatrix, 
                                                      dataTypeOfCompanionMatrix,
                                                      columnName);
  allViews.add (scatterPlotter);
  tabbedPane.add (tabName, scatterPlotter);
  tabbedPane.setSelectedIndex (tabbedPane.getTabCount () - 1);

} // doVolcanoPlot
//-------------------------------------------------------------------------------
public void saveAll (File directory)
{
  for (int i=0; i < allViews.size (); i++) {
   DataMatrixView view = (DataMatrixView) allViews.get (i);
   view.save (directory);
    } // for i

} // saveAll
//-------------------------------------------------------------------------------
} // class DataMatrixViewer
