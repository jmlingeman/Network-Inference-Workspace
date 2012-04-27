// SBeamsSearchPanel.java
//---------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.boss.plugins.sbeams;
//---------------------------------------------------------------------------------------
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.*;
import java.util.*;
import java.util.List;

import org.systemsbiology.gaggle.boss.GuiBoss;
import org.systemsbiology.gaggle.util.*;
import org.systemsbiology.gaggle.boss.plugins.GaggleBossPlugin;
import org.systemsbiology.gaggle.core.datatypes.Namelist;


public class SBeamsSearchPanel extends GaggleBossPlugin {

  String name = "SBEAMS Search Panel";
  String targetGoose = "Boss";   // for broadcasts
  private JScrollPane scrollPane;
  private JTextField searchBox;
  private JTextField selectionCountReadout, retrievedCountReadout;
  SbeamsResultSetTableModel sbeamsResultSetTableModel;
  ListSelectionModel selectionModel;
  JTable sbeamsResultSetTable;
  private JButton broadcastSbeamsSelectionsButton;
  private JButton selectAllSbeamsRowsButton;
  private JButton clearAllSbeamsSelectionsButton;
  private GuiBoss gaggleBoss;
  private String species = "Halobacterium sp.";
  
//---------------------------------------------------------------------------------------
public SBeamsSearchPanel (GuiBoss boss)
{
  super ("SBEAMS Halo");
  gaggleBoss = boss;
  createGui ();

} // ctor
//---------------------------------------------------------------------------------------
protected void createGui ()
{
  add (new JButton ("Leo"));
  setLayout (new BorderLayout ());
  JPanel innerPanel = new JPanel ();
  innerPanel.setLayout (new BorderLayout ());
  JPanel searchPanel = new JPanel ();
  
  //JLabel label = new JLabel ("Search ");
  JButton searchButton = new JButton ("Search");
  searchButton.addActionListener (new ActionListener () {
    public void actionPerformed (ActionEvent e) {performSearch ();}});
  searchBox = new JTextField (20);
  searchBox.addActionListener (new DoSearchAction ());

  searchBox.addKeyListener (new KeyAdapter () {
     public void keyPressed (KeyEvent e) {
       if (KeyEvent.getKeyText (e.getKeyCode()).equals ("Enter")) performSearch ();}});

  JButton clearSearchBoxButton = new JButton ("Clear");
  clearSearchBoxButton.addActionListener (new ActionListener () {
    public void actionPerformed (ActionEvent e) {searchBox.setText ("");}});
  searchPanel.add (searchButton);
  searchPanel.add (searchBox);
  searchPanel.add (clearSearchBoxButton);

  selectAllSbeamsRowsButton = new JButton ("Select All");
  clearAllSbeamsSelectionsButton = new JButton ("Clear All");
  broadcastSbeamsSelectionsButton = new JButton ("Broadcast");

  selectAllSbeamsRowsButton.setEnabled (false);
  clearAllSbeamsSelectionsButton.setEnabled (false);
  broadcastSbeamsSelectionsButton.setEnabled (false);

  selectAllSbeamsRowsButton.addActionListener (new SelectAllSbeamsRowsAction ());
  clearAllSbeamsSelectionsButton.addActionListener (new ClearAllSbeamsSelectionsAction ());
  broadcastSbeamsSelectionsButton.addActionListener (new BroadcastSbeamsSelectionAction ());

  innerPanel.add (searchPanel, BorderLayout.NORTH);
  innerPanel.add (createSbeamsResultSetTable (), BorderLayout.CENTER);
  JPanel broadcastButtonPanel = new JPanel ();

  broadcastButtonPanel.add (selectAllSbeamsRowsButton);
  broadcastButtonPanel.add (clearAllSbeamsSelectionsButton);
  broadcastButtonPanel.add (broadcastSbeamsSelectionsButton);

  JPanel bottomPanel = new JPanel (new BorderLayout ());
  bottomPanel.add (broadcastButtonPanel, BorderLayout.CENTER);

  JPanel readoutsPanel = new JPanel ();
  readoutsPanel.setBorder (BorderFactory.createTitledBorder ("Selected"));
  bottomPanel.add (readoutsPanel, BorderLayout.WEST);
  selectionCountReadout = new JTextField (4);
  retrievedCountReadout = new JTextField (4);
  readoutsPanel.add (selectionCountReadout);
  readoutsPanel.add (new JLabel (" of "));
  readoutsPanel.add (retrievedCountReadout);

  innerPanel.add (bottomPanel, BorderLayout.SOUTH);

  add (innerPanel);

} // createSearchPanel
//-------------------------------------------------------------------------------
protected JPanel createSbeamsResultSetTable ()
{
  sbeamsResultSetTableModel = new SbeamsResultSetTableModel ();
  sbeamsResultSetTable = new JTable (sbeamsResultSetTableModel);
  sbeamsResultSetTable.addMouseListener (new WebPageLoader (sbeamsResultSetTable));

   // add a listener to detect selected rows, and to update selection count
  selectionModel = sbeamsResultSetTable.getSelectionModel ();
  selectionModel.addListSelectionListener (new SelectionListener ());


  sbeamsResultSetTable.getColumnModel().getColumn (0).setPreferredWidth (40);
  sbeamsResultSetTable.getColumnModel().getColumn (0).setMaxWidth (40);

  sbeamsResultSetTable.getColumnModel().getColumn (1).setPreferredWidth (70);
  sbeamsResultSetTable.getColumnModel().getColumn (1).setMaxWidth (70);

  sbeamsResultSetTable.getColumnModel().getColumn (2).setPreferredWidth (70);
  sbeamsResultSetTable.getColumnModel().getColumn (2).setMaxWidth (70);

  //sbeamsResultSetTable.getColumnModel().getColumn (3).setPreferredWidth (80);
  //sbeamsResultSetTable.getColumnModel().getColumn (3).setMaxWidth (80);


  // setTableColumnWidths ();
  sbeamsResultSetTable.setShowHorizontalLines (true);
  sbeamsResultSetTable.setShowVerticalLines (true);
  sbeamsResultSetTable.setPreferredScrollableViewportSize (new Dimension (300, 200));
  scrollPane = new JScrollPane (sbeamsResultSetTable);

  //scrollPane.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
  scrollPane.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
  //table.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);

  JPanel outerPanel = new JPanel ();
  outerPanel.setLayout (new BorderLayout ());

  JPanel tablePanel = new JPanel ();
  tablePanel.setLayout (new BorderLayout ());
  tablePanel.setBorder (BorderFactory.createEmptyBorder (10, 10, 10, 10));
  tablePanel.add (scrollPane, BorderLayout.CENTER);

  return tablePanel;

} // createSbeamsResultSetTable
//-------------------------------------------------------------------------------
class WebPageLoader extends MouseInputAdapter {
    JTable table;
  public WebPageLoader (JTable table) { this.table = table; }
  public void mouseClicked  (MouseEvent e) {
    TableColumnModel columnModel = table.getColumnModel ();
    int column = columnModel.getColumnIndexAtX (e.getX ());
    int row  = e.getY () / table.getRowHeight();
    if (row >= table.getRowCount () || row < 0 || 
        column >= table.getColumnCount() || column < 0)
      return;
    String cellContents = (String) table.getValueAt (row, column);
    if (cellContents.startsWith ("http"))
      MiscUtil.displayWebPage (cellContents);
    }// mouseClicked

} // WebPageLoader
//-------------------------------------------------------------------------------
class BroadcastSbeamsSelectionAction extends AbstractAction {

  BroadcastSbeamsSelectionAction () { super (""); }

  public void actionPerformed (ActionEvent e) {
    int [] selectedRows = sbeamsResultSetTable.getSelectedRows ();
    List<String> selectedOrfs = new ArrayList<String>();
    for (int i=0; i < selectedRows.length; i++) {
      String orfName = (String) sbeamsResultSetTableModel.getValueAt (selectedRows [i], 0);
      orfName = orfName.toUpperCase ();
      selectedOrfs.add (orfName);
      } // for i
    Namelist nameList = new Namelist();
    nameList.setSpecies(species);
    nameList.setNames(selectedOrfs.toArray(new String[0]));
    try {
      gaggleBoss.broadcastNamelist(name, targetGoose, nameList);
      }
    catch (RemoteException rex) {
      rex.printStackTrace ();
      }
    } // actionPerformed
  
} // inner class BroadcastSbeamsSelectionAction
//-------------------------------------------------------------------------------
class SelectAllSbeamsRowsAction extends AbstractAction {

  SelectAllSbeamsRowsAction () { super (""); }

  public void actionPerformed (ActionEvent e) {
    sbeamsResultSetTable.selectAll ();
    } // actionPerformed
  
} // inner class SelectAllSbeamsRowsAction
//-------------------------------------------------------------------------------
class ClearAllSbeamsSelectionsAction extends AbstractAction {

  ClearAllSbeamsSelectionsAction () { super (""); }

  public void actionPerformed (ActionEvent e) {
    sbeamsResultSetTable.clearSelection ();
    } // actionPerformed
  
} // inner class ClearAllSbeamsSelectionsAction
//-------------------------------------------------------------------------------
class DoSearchAction extends AbstractAction {

  DoSearchAction () { super (""); }

  public void actionPerformed (ActionEvent e) {
    performSearch ();
    } // actionPerformed

} // inner class DoSearchAction
//-------------------------------------------------------------------------------
private String rationalizeSearchText (String searchText)
{
  searchText = searchText.replaceAll ("\\s+", ";");
  searchText = searchText.replaceAll (":", ";");
  return searchText;
}
//-------------------------------------------------------------------------------
protected void performSearch ()
{
  String searchText = searchBox.getText ().trim();
  searchText = rationalizeSearchText (searchText);    
  sbeamsResultSetTableModel.clearData ();
  broadcastSbeamsSelectionsButton.setEnabled (false);
  selectAllSbeamsRowsButton.setEnabled (false);
  clearAllSbeamsSelectionsButton.setEnabled (false);
  if (searchText.length() > 0) {
    String [] results = doSbeamsSearch (searchText);
    if (results.length > 0) {
      broadcastSbeamsSelectionsButton.setEnabled (true);
      selectAllSbeamsRowsButton.setEnabled (true);
      clearAllSbeamsSelectionsButton.setEnabled (true);
      retrievedCountReadout.setText ((new Integer (results.length)).toString ());
    }

    ArrayList<Annotation> annotations = new ArrayList<Annotation>();
    for (int i=0; i < results.length; i++) {
      String [] tokens = results [i].split ("\t");
      if (tokens.length < 4)
        continue;
      String orf = tokens [0];
      String geneSymbol = tokens [1];
      String ecNumbers  = tokens [2];
      String function   = tokens [3];
      String domainHitsUrl = "n/a";
      if (tokens.length >= 15)
        domainHitsUrl = "https://db.systemsbiology.net" + tokens [14];
      annotations.add(new Annotation(orf, geneSymbol, ecNumbers, function, domainHitsUrl));
    }
    
    // ideally, we'd make SBEAMS do the sorting, but as a quick
    // hack, we'll do it here
    Collections.sort(annotations, new Comparator<Annotation>() {
      public int compare(Annotation a1, Annotation a2) {
        return a1.orf.compareTo(a2.orf);
      }
    });

    sbeamsResultSetTableModel.addSearchResults(annotations);
    sbeamsResultSetTableModel.fireTableStructureChanged ();
  } // if searchText

} // performSearch

//-------------------------------------------------------------------------------
protected String [] doSbeamsSearch (String searchText)
{
  //System.out.println ("SBeamsSearchPanel.doSbeamsSearch, searchText: " + searchText);
  //searchText = searchText.replaceAll (";", "%3B");
    try {
        searchText = java.net.URLEncoder.encode(searchText, "UTF-8");
    } catch (UnsupportedEncodingException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    String urlHead = "https://db.systemsbiology.net/sbeams/cgi/ProteinStructure/GetAnnotations?";
  String urlAction = "search_scope=All&search_key=";
  String urlTail = "&action=GO&biosequence_set_id=2&action=QUERY&output_mode=";
  String outputMode = "tsvfull";  // "tsv"
  String fullUrl = urlHead + urlAction + searchText + urlTail + outputMode;
  String tabDelimitedResultText = "";
  System.out.println (fullUrl);
  try {
    SBEAMSClient client = new SBEAMSClient (true);
    tabDelimitedResultText = client.fetchSbeamsPage (fullUrl);
    //System.out.println ("\n\ntabDelimitedText\n" + tabDelimitedResultText);
    }
  catch (IOException e) {
    System.err.println("Page Not Found");
    }
  catch (Exception t) {
    t.printStackTrace();
    }

  String [] lines = tabDelimitedResultText.split ("\n");
  if (lines.length <= 1)
    return new String [] {"no match\t\t\t"};

  String [] result = new String [lines.length - 1];
  for (int i=1; i < lines.length; i++) {
    result [i-1] = lines [i];
    }
  return result;

} // doSbeamsSearch
//-------------------------------------------------------------------------------
public void select (String [] names)
{
  StringBuffer sb = new StringBuffer ();
  sb.append (searchBox.getText());
  for (int i=0; i < names.length; i++) {
    if (sb.length () > 0) 
      sb.append (";");
    sb.append (names [i]);
    selectMatchingRow (names [i]);
    }

  searchBox.setText (sb.toString ());  

} // select
//---------------------------------------------------------------------------------------
/**
 * upon receiving a namelist broadcast, select matching rows in the the sbeams
 * result table.
 */
protected void selectMatchingRow (String name)
{
  for (int i=0; i < sbeamsResultSetTableModel.getRowCount (); i++) {
    String rowName = (String) sbeamsResultSetTableModel.getValueAt (i, 0);
    if (rowName.equalsIgnoreCase (name))
      selectionModel.addSelectionInterval (i, i);
    } // for i

} // selectMatchingRow
//---------------------------------------------------------------------------------------
class SelectionListener implements ListSelectionListener {

  public void valueChanged (ListSelectionEvent e) {
    if (e.getValueIsAdjusting())
      return;
    System.out.println ("selection listener");
    int count = sbeamsResultSetTable.getSelectedRowCount ();
    selectionCountReadout.setText ((new Integer (count)).toString ());
    } // valueChanged

} // TableSelectionListener
//------------------------------------------------------------------------------
} // class SBeamsSearchPanel
