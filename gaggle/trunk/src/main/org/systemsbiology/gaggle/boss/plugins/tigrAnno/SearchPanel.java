// SearchPanel.java
//---------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.boss.plugins.tigrAnno;
//---------------------------------------------------------------------------------------
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.awt.event.*;


import java.rmi.*;

import java.util.*;

import org.systemsbiology.gaggle.boss.GuiBoss;
import org.systemsbiology.gaggle.util.*;
import org.systemsbiology.gaggle.boss.plugins.GaggleBossPlugin;
import org.systemsbiology.gaggle.core.datatypes.Namelist;

//---------------------------------------------------------------------------------------
public class SearchPanel extends GaggleBossPlugin {

  String name = "TIGR Search Panel";
  protected JScrollPane scrollPane;
  protected JTextField searchBox;
  SearchResultsTableModel tableModel;
  JTable resultsTable;

  protected JTextField resultSetSizeReadout;
  protected JButton broadcastSelectionsButton;
  protected JButton selectAllRowsButton;
  protected JButton clearAllSelectionsButton, clearTableButton;
  protected JTextField conditionCounterTextField;
  protected String species = "unknown";

  protected GuiBoss gaggleBoss;
  protected String annotationUri;
  protected HashMap searchMap = new HashMap ();
  protected HashMap summaryMap = new HashMap ();
  
//---------------------------------------------------------------------------------------
public SearchPanel (GuiBoss boss)
{
  super ("Annotation Search");
  gaggleBoss = boss;
  annotationUri = boss.getConfig().getProperties().getProperty ("annotation");
  species = boss.getConfig().getProperties().getProperty ("species");
  System.out.println ("got annotationUri from props: " + annotationUri);
  createGui ();
  if (annotationUri != null)
    readAnnotationData (annotationUri);

} // ctor
//---------------------------------------------------------------------------------------
protected void readAnnotationData (String uri)
{
  String [] lines = new String [0];

  try {
    TextHttpReader reader = new TextHttpReader (uri);
    System.out.println ("about to read: " + uri);
    reader.read ();
    System.out.println ("reading done");
    String rawText = reader.getText ();
    lines = rawText.split ("\n");
    System.out.println (" * annotation lines read: " + lines.length);
    }
  catch (Exception ex0) {
    String msg = "<html>Error reading annotation from <br>" + annotationUri
                 + ":<br>" + ex0.getMessage () + "</html>";
    JOptionPane.showMessageDialog (this, msg);
    }

  for (int i=1; i < lines.length; i++) {
    //if (i % 100 == 0)
    //  System.out.println ("  line " + i);
    String [] tokens = lines [i].split ("\t");
    //System.out.println ("line " + i + " (" + tokens.length + "): " + lines [i]);
    if (tokens.length > 2) {
      String orf = tokens [0];
      String geneSymbol = tokens [1];
      String function = tokens [2];
      String uniquifier = (new Integer (i)).toString ();
      searchMap.put (orf, orf);
      searchMap.put (geneSymbol, orf);
      searchMap.put (function + uniquifier, orf);
      summaryMap.put (orf, new String [] {geneSymbol, function});
      } // if
    } // for i    

} // readAnnotationData
//---------------------------------------------------------------------------------------
protected void createGui ()
{
  setLayout (new BorderLayout ());
  JPanel innerPanel = new JPanel ();
  innerPanel.setLayout (new BorderLayout ());
  JPanel searchPanel = new JPanel ();
  
  JButton searchButton = new JButton ("Search");
  searchButton.setToolTipText ("Find every gene matching (in name or annotation)");
  searchButton.addActionListener (new ActionListener () {
    public void actionPerformed (ActionEvent e) {performSearch ();}});
  searchBox = new JTextField (20);
  searchBox.setToolTipText ("one or more search terms; use semi-colon delimitors; substrings okay");
  searchBox.addActionListener (new DoSearchAction ());

  searchBox.addKeyListener (new KeyAdapter () {
     public void keyPressed (KeyEvent e) {
       if (e.getKeyText (e.getKeyCode()).equals ("Enter")) performSearch ();}});

  JButton clearSearchBoxButton = new JButton ("Clear");
  clearSearchBoxButton.setToolTipText ("clear the search box of all text");
  clearSearchBoxButton.addActionListener (new ActionListener () {
    public void actionPerformed (ActionEvent e) {searchBox.setText ("");}});
  searchPanel.add (searchButton);
  searchPanel.add (searchBox);
  searchPanel.add (clearSearchBoxButton);
  
  selectAllRowsButton = new JButton ("Select All");
  clearAllSelectionsButton = new JButton ("Deselect All");
  clearTableButton = new JButton ("Clear All");
  broadcastSelectionsButton = new JButton ("Broadcast");

  selectAllRowsButton.setEnabled (false);
  selectAllRowsButton.setToolTipText ("Select all rows in current search result set");
  clearAllSelectionsButton.setEnabled (false);
  clearAllSelectionsButton.setToolTipText ("Deselect all rows");
   
  clearTableButton.setEnabled (false);
  clearTableButton.setToolTipText ("Remove all rows");
  broadcastSelectionsButton.setEnabled (false);

  selectAllRowsButton.addActionListener (new SelectAllRowsAction ());
  clearAllSelectionsButton.addActionListener (new ClearAllSelectionsAction ());
  clearTableButton.addActionListener (new ActionListener () {
    public void actionPerformed (ActionEvent e) {
      tableModel.clearData ();
      tableModel.fireTableStructureChanged ();
      setTableColumnWidths ();
      resultSetSizeReadout.setText ("0");
      }});

  broadcastSelectionsButton.addActionListener (new BroadcastSelectionAction ());
  broadcastSelectionsButton.setToolTipText ("Broadcast all ORF names to all geese");

  innerPanel.add (searchPanel, BorderLayout.NORTH);
  innerPanel.add (createSearchResultsTable (), BorderLayout.CENTER);
  JPanel broadcastButtonPanel = new JPanel ();

  resultSetSizeReadout = new JTextField ("0", 4);
  resultSetSizeReadout.setToolTipText ("Number of genes found in last search");
  resultSetSizeReadout.setEditable (false);

  broadcastButtonPanel.add (resultSetSizeReadout);
  broadcastButtonPanel.add (selectAllRowsButton);
  broadcastButtonPanel.add (clearAllSelectionsButton);
  broadcastButtonPanel.add (clearTableButton);
  broadcastButtonPanel.add (broadcastSelectionsButton);

  innerPanel.add (broadcastButtonPanel, BorderLayout.SOUTH);
  add (innerPanel);
  ToolTipManager.sharedInstance().setInitialDelay (0);

} // createSearchPanel
//-------------------------------------------------------------------------------
protected void setTableColumnWidths ()
{
  int narrow = 80;
  int broad = 400;
  resultsTable.getColumnModel().getColumn (0).setPreferredWidth (narrow);
  resultsTable.getColumnModel().getColumn (0).setMaxWidth (narrow);

  resultsTable.getColumnModel().getColumn (1).setPreferredWidth (narrow);
  resultsTable.getColumnModel().getColumn (1).setMaxWidth (narrow);

  resultsTable.getColumnModel().getColumn (2).setPreferredWidth (broad);
  // resultsTable.getColumnModel().getColumn (2).setMaxWidth (broad);

}
//-------------------------------------------------------------------------------
protected JPanel createSearchResultsTable ()
{
  tableModel = new SearchResultsTableModel ();
  resultsTable = new JTable (tableModel);

  setTableColumnWidths ();

  resultsTable.setShowHorizontalLines (true);
  resultsTable.setShowVerticalLines (true);
  int narrow = 80;
  int broad = 400;

  // resultsTable.setPreferredScrollableViewportSize (new Dimension (broad + (2 * narrow), 200));

  scrollPane = new JScrollPane (resultsTable);

  scrollPane.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
  scrollPane.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

  JPanel outerPanel = new JPanel ();
  outerPanel.setLayout (new BorderLayout ());

  JPanel tablePanel = new JPanel ();
  tablePanel.setLayout (new BorderLayout ());
  tablePanel.setBorder (BorderFactory.createEmptyBorder (10, 10, 10, 10));
  tablePanel.add (scrollPane, BorderLayout.CENTER);

  return tablePanel;

} // createSearchResultsTable
//-------------------------------------------------------------------------------
class BroadcastSelectionAction extends AbstractAction {

  BroadcastSelectionAction () { super (""); }

  public void actionPerformed (ActionEvent e) {
    int [] selectedRows = resultsTable.getSelectedRows ();

    List<String> selectedOrfs = new ArrayList<String>();
    for (int i=0; i < selectedRows.length; i++) {
      String orfName = (String) tableModel.getValueAt (selectedRows [i], 0);
      orfName = orfName.toUpperCase ();
      selectedOrfs.add (orfName);
      } // for i
    Namelist nameList = new Namelist();
    nameList.setSpecies(species);
    nameList.setNames(selectedOrfs.toArray (new String [0]));
    try {
      gaggleBoss.broadcastNamelist(name, "all", nameList);
      }
    catch (RemoteException rex) {
      rex.printStackTrace ();
      }
    } // actionPerformed
  
} // inner class BroadcastSelectionAction
//-------------------------------------------------------------------------------
class SelectAllRowsAction extends AbstractAction {

  SelectAllRowsAction () { super (""); }

  public void actionPerformed (ActionEvent e) {
    resultsTable.selectAll ();
    } // actionPerformed
  
} // inner class SelectAllRowsAction
//-------------------------------------------------------------------------------
class ClearAllSelectionsAction extends AbstractAction {

  ClearAllSelectionsAction () { super (""); }

  public void actionPerformed (ActionEvent e) {
    resultsTable.clearSelection ();
    } // actionPerformed
  
} // inner class ClearAllSelectionsAction
//-------------------------------------------------------------------------------
class DoSearchAction extends AbstractAction {

  DoSearchAction () { super (""); }

  public void actionPerformed (ActionEvent e) {
    performSearch ();
    } // actionPerformed

} // inner class DoSearchAction
//-------------------------------------------------------------------------------
protected String rationalizeSearchText (String searchText)
{
  searchText = searchText.replaceAll ("\\s+", ";");
  searchText = searchText.replaceAll (":", ";");
  return searchText;
}
//-------------------------------------------------------------------------------
protected void performSearch ()
{
  String searchText = searchBox.getText ().trim();
  if (searchText.length () < 1)
    return;

  searchText = rationalizeSearchText (searchText);    
  String [] searchMapKeys = (String []) searchMap.keySet().toArray (new String [0]);

  HashSet hitsList = new HashSet ();

  String soughtForText = searchText.toLowerCase ();
  String [] arrows = soughtForText.split (";");

   // 'arrows' are supplied by the user.  we see if any strike a 'target' -- a key in
   // the searchMap

  for (int i=0; i < searchMapKeys.length; i++) {
    String targetText = searchMapKeys [i].toLowerCase ();
    for (int a=0; a < arrows.length; a++) {
      String arrow = arrows [a].trim ();
      if (arrow.length () > 0 && targetText.indexOf (arrow) >= 0) {
        String matchKey = searchMapKeys [i];
        String hit = (String) searchMap.get (matchKey);
        hitsList.add (hit);
        } // if matched
      } // for a
    } // for i

  String [] hits = (String []) hitsList.toArray (new String [0]);
  resultSetSizeReadout.setText ((new Integer (hits.length)).toString ());

  if (hits.length == 0)
    return;

  broadcastSelectionsButton.setEnabled (true);
  selectAllRowsButton.setEnabled (true);
  clearTableButton.setEnabled (true);
  clearAllSelectionsButton.setEnabled (true);
  tableModel.clearData ();

  for (int i=0; i < hits.length; i++) {
    String orf = hits [i];
    String [] searchValue = (String []) summaryMap.get (orf);
    String geneSymbol = searchValue [0];
    String function = searchValue [1];
    tableModel.addSearchResult (orf, geneSymbol, function);
    } // for i

  tableModel.fireTableStructureChanged ();
  setTableColumnWidths ();

} // performSearch
//-------------------------------------------------------------------------------
public void select (String [] names)
{
  // System.out.println ("SearchPanel.select: " + names.length);
  StringBuffer sb = new StringBuffer ();
  sb.append (searchBox.getText());
  for (int i=0; i < names.length; i++) {
    if (sb.length () > 0) 
      sb.append (";");
    sb.append (names [i]);
    }

  searchBox.setText (sb.toString ());  

} // select
//---------------------------------------------------------------------------------------
} // class SearchPanel
