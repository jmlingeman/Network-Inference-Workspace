// AnnotationGoose.java
//-------------------------------------------------------------------------------------
// $Revision: 332 $   
// $Date: 2005/04/03 19:15:04 $
//-------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.annotation;


import java.rmi.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.datatypes.*;
import org.systemsbiology.gaggle.geese.common.GaggleConnectionListener;
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;
import org.systemsbiology.gaggle.geese.common.GooseShutdownHook;
import org.systemsbiology.gaggle.geese.common.gui.GuiWidgetFactory;
import org.systemsbiology.gaggle.util.Index;
import org.systemsbiology.gaggle.util.MiscUtil;
import org.systemsbiology.gaggle.util.TextFileReader;
import org.systemsbiology.gaggle.util.TextHttpReader;
import org.systemsbiology.gaggle.util.TextJarReader;


/**
 * Loads a list of names and corresponding annotations. User can search and
 * display matching annotations. Can receive search terms from gaggle and
 * broadcast selected search results.
 */
public class AnnotationGoose extends JFrame implements Goose, GaggleConnectionListener {
    String gooseName = "Annotation";
    Boss boss;

    RmiGaggleConnector gaggleConnector;
    private GuiWidgetFactory factory;

    protected JScrollPane scrollPane;
    JComboBox gooseChooser;
    String targetGoose = "Boss";
    Remote remoteObj;
    int column = 0;

    protected JTextField searchBox;
    SearchResultsTableModel tableModel;
    SelfAdjustingJTable resultsTable;

    protected JButton broadcastSelectionsButton;
    protected JButton broadcastListButton;
    protected JButton selectAllRowsButton;
    protected JButton clearAllSelectionsButton, clearTableButton;
    protected JTextField conditionCounterTextField;

    private JTextField status;
    private JTextField selectionCountReadout;
    private JTextField retrievedCountReadout;
    protected JMenu columnMenu;

    protected String species = "unknown";
    
    // used to look up identifiers by key words.
    protected Index index = new Index();

    // look up fields using identifier as key.
    protected HashMap<String, String[]> summaryMap = new HashMap<String, String[]>();

    private BroadcastSelectionAction broadcastSelectionAction = new BroadcastSelectionAction();

    private Pattern anchorTagPattern = Pattern.compile("\\<a href=\\\"(.*)\\\"\\>.*\\<\\/a\\>", Pattern.CASE_INSENSITIVE);

    private String[] activeGooseNames;

    // Chris made this special hack for Deep. The purpose is to preserve the
    // order of search terms in the special case that we're broadcasting gene
    // names in a special order. (For example from hierarchical clustering in
    // MeV).
    private enum Mode {normal, keys_only};
    private Mode mode = Mode.normal;


    public AnnotationGoose() {
        super("Annotation");


        ToolTipManager.sharedInstance().setInitialDelay(0);

        this.setLayout(new BorderLayout());

        MiscUtil.setApplicationIcon(this);

        gaggleConnector = new RmiGaggleConnector(this);
        gaggleConnector.addListener(this);
        factory = new GuiWidgetFactory(gaggleConnector, this);

        new GooseShutdownHook(gaggleConnector);
        
        setJMenuBar(createMenu());
        add(createGui(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

        // perform a proper exit if window is closed
        addWindowListener(new WindowListener() {
            public void windowClosing(WindowEvent e) {
                doExit();
            }

            public void windowClosed(WindowEvent e) {}
            public void windowOpened(WindowEvent e) {}
            public void windowIconified(WindowEvent e) {}
            public void windowDeiconified(WindowEvent e) {}
            public void windowActivated(WindowEvent e) {}
            public void windowDeactivated(WindowEvent e) {}
        });

        setSize(500, 500);
        MiscUtil.placeInCenter(this);
        setVisible(true);
    } // ctor

    JPanel createGui() {
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        JPanel controlPanel = new JPanel();
        JToolBar toolbar = new JToolBar();
        controlPanel.add(toolbar);
        toolbar.setFloatable(false);

        mainPanel.add(controlPanel, BorderLayout.NORTH);

        //JButton getGeeseNamesButton = new JButton(factory.getUpdateAction());
        //toolbar.add(getGeeseNamesButton);

        gooseChooser = factory.getGooseChooser();
        toolbar.add(gooseChooser);

        gooseChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                int gooseChooserIndex = cb.getSelectedIndex();
                System.out.println("choose goose index: " + gooseChooserIndex);
                targetGoose = (String) cb.getSelectedItem();
                System.out.println("target: " + targetGoose);
            }
        });

        JButton showGooseButton = new JButton(factory.getShowGooseAction());
        JButton hideGooseButton = new JButton(factory.getHideGooseAction());
        broadcastListButton = new JButton(broadcastSelectionAction);

        toolbar.add(showGooseButton);
        toolbar.add(hideGooseButton);
        toolbar.add(broadcastListButton);

        JPanel searchPanel = createSearchPanel();
        // JScrollPane scrollPane = new JScrollPane (searchPanel);
        mainPanel.setBorder(createBorder());
        mainPanel.add(searchPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton loadAnnotationDialogButton = new JButton(
                new ShowLoadAnnotationDialogAction());
        buttonPanel.add(loadAnnotationDialogButton);
        JButton exitButton = new JButton(new QuitAction());
        buttonPanel.add(exitButton);

        JPanel readoutsPanel = new JPanel();
        readoutsPanel.setBorder(BorderFactory.createTitledBorder("Selected"));

        selectionCountReadout = new JTextField("0", 4);
        selectionCountReadout.setToolTipText("Number of genes selected");
        selectionCountReadout.setEditable(false);
        retrievedCountReadout = new JTextField("0", 4);
        retrievedCountReadout.setToolTipText("Number of genes found in last search");
        retrievedCountReadout.setEditable(false);

        readoutsPanel.add(selectionCountReadout);
        readoutsPanel.add(new JLabel(" of "));
        readoutsPanel.add(retrievedCountReadout);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(readoutsPanel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        FlavorListener flavorListener = new FlavorListener() {
            public void flavorsChanged(FlavorEvent e) {
                System.out.println("--->" + e);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                for (DataFlavor df : clipboard.getAvailableDataFlavors()) {
                    System.out.println(df);
                }
            }
        };
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.addFlavorListener(flavorListener);

        MiscUtil.updateGooseChooser(gooseChooser, gooseName, activeGooseNames);
        return mainPanel;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new GridBagLayout());
        statusBar.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        status = new JTextField("");
        status.setEditable(false);
        status.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        statusBar.add(status, c);
        return statusBar;
    }

    private JMenuBar createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenuItem menuItem;
        JMenu menu;

        menu = new JMenu("File");
        menuItem = new JMenuItem(new ShowLoadAnnotationDialogAction());
        menu.add(menuItem);
        menu.addSeparator();
        JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(new ToggleKeyOnlyModeAction());
        cbmi.setSelected(false);
        menu.add(cbmi);
        menu.addSeparator();
        menu.add(new JMenuItem(new QuitAction()));
        menuBar.add(menu);

        TransferActionListener actionListener = new TransferActionListener();
        menu = new JMenu("Edit");
        menuItem = new JMenuItem("Copy");
        menuItem.setActionCommand((String) TransferHandler.getCopyAction().
                getValue(Action.NAME));
        menuItem.addActionListener(actionListener);
        menuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        menuItem.setMnemonic(KeyEvent.VK_C);
        menu.add(menuItem);
        menuItem = new JMenuItem("Paste");
        menuItem.setActionCommand((String) TransferHandler.getPasteAction().
                getValue(Action.NAME));
        menuItem.addActionListener(actionListener);
        menuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        menuItem.setMnemonic(KeyEvent.VK_P);
        menu.add(menuItem);
        menuBar.add(menu);

        columnMenu = new JMenu("Column");
        menuBar.add(columnMenu);

        menuBar.add(factory.getGaggleMenu());

        return menuBar;
    }

    /**
     * set the text in the status bar
     */
    protected void setStatus(String msg) {
        status.setText(msg);
    }

    /**
     * update the select column menu to reflect currectly loaded columns
     */
    private void updateColumnMenu() {
        column = 0;
        columnMenu.removeAll();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(new SelectColumnAction(tableModel.getColumnName(i), i));
            cbmi.setSelected(i == column);
            columnMenu.add(cbmi);
        }
    }

    private Border createBorder() {
        int right = 10;
        int left = 10;
        int top = 10;
        int bottom = 10;
        return new EmptyBorder(top, left, bottom, right);
    }

    protected JPanel createSearchPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BorderLayout());
        JPanel searchPanel = new JPanel();

        DoSearchAction searchAction = new DoSearchAction();
        JButton searchButton = new JButton(searchAction);
        searchBox = new JTextField(20);
        searchBox.setToolTipText("one or more search terms; use semi-colon delimitors; substrings okay");
        searchBox.addActionListener(searchAction);
        searchBox.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (KeyEvent.getKeyText(e.getKeyCode()).equals("Enter"))
                    search();
            }
        });

        JButton clearSearchBoxButton = new JButton("Clear");
        clearSearchBoxButton.setToolTipText("clear the search box of all text");
        clearSearchBoxButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchBox.setText("");
            }
        });
        searchPanel.add(searchButton);
        searchPanel.add(searchBox);
        searchPanel.add(clearSearchBoxButton);

        selectAllRowsButton = new JButton(new SelectAllRowsAction());
        clearAllSelectionsButton = new JButton(new ClearAllSelectionsAction());
        clearTableButton = new JButton(new ClearTableAction());
        broadcastSelectionsButton = new JButton(broadcastSelectionAction);
        broadcastSelectionsButton.setText("Broadcast");

        selectAllRowsButton.setEnabled(false);
        clearAllSelectionsButton.setEnabled(false);
        clearTableButton.setEnabled(false);

        innerPanel.add(searchPanel, BorderLayout.NORTH);
        innerPanel.add(createSearchResultsTable(), BorderLayout.CENTER);
        JPanel broadcastButtonPanel = new JPanel();

        broadcastButtonPanel.add(selectAllRowsButton);
        broadcastButtonPanel.add(clearAllSelectionsButton);
        broadcastButtonPanel.add(clearTableButton);
        broadcastButtonPanel.add(broadcastSelectionsButton);

        innerPanel.add(broadcastButtonPanel, BorderLayout.SOUTH);
        p.add(innerPanel);
        return p;

    } // createSearchPanel

    protected void setTableColumnWidths() {
        int narrow = 100;
        int broad = 400;

        if (resultsTable.getColumnCount() == 3) {
            resultsTable.getColumnModel().getColumn(0).setPreferredWidth(narrow);
            resultsTable.getColumnModel().getColumn(0).setMaxWidth(narrow);

            resultsTable.getColumnModel().getColumn(1).setPreferredWidth(narrow);
            resultsTable.getColumnModel().getColumn(1).setMaxWidth(narrow);

            resultsTable.getColumnModel().getColumn(2).setPreferredWidth(broad);
        }
    }

    protected JPanel createSearchResultsTable() {
        tableModel = new SearchResultsTableModel();
        resultsTable = new SelfAdjustingJTable(tableModel);

        resultsTable.setShowHorizontalLines(true);
        resultsTable.setShowVerticalLines(true);

        ListSelectionModel selectionModel = resultsTable.getSelectionModel();
        selectionModel.addListSelectionListener(new UpdateSelectionListener());

        resultsTable.addMouseListener(new LinkMouseListener());
        resultsTable.addMouseMotionListener(new LinkMouseMotionListener());

        InputMap imap = resultsTable.getInputMap();
        imap.put(KeyStroke.getKeyStroke("ctrl X"),
                TransferHandler.getCutAction().getValue(Action.NAME));
        imap.put(KeyStroke.getKeyStroke("ctrl C"),
                TransferHandler.getCopyAction().getValue(Action.NAME));
        imap.put(KeyStroke.getKeyStroke("ctrl V"),
                TransferHandler.getPasteAction().getValue(Action.NAME));

        ActionMap map = resultsTable.getActionMap();
        map.put(TransferHandler.getCutAction().getValue(Action.NAME),
                TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME),
                TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME),
                TransferHandler.getPasteAction());

        resultsTable.setDefaultRenderer(Object.class, new LinkRenderer());

        scrollPane = new JScrollPane(resultsTable);

        scrollPane
                .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane
                .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BorderLayout());

        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    } // createSearchResultsTable


    public void connectToGaggle() {
        try {
            gaggleConnector.connectToGaggle();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        boss = gaggleConnector.getBoss();
    }

    public void disconnectFromGaggle() {
        gaggleConnector.disconnectFromGaggle(true);
    }


    /**
     * @see GaggleConnectionListener
     */
    public void setConnected(boolean connected, Boss boss) {
        this.boss = boss;
        broadcastSelectionAction.setEnabled(connected);
    }

    public void handleNameList(String source, Namelist nameList) {
        StringBuffer sb = new StringBuffer();
        sb.append(searchBox.getText());
        if (sb.length() > 0)
            sb.append(";");
        int max = nameList.getNames().length;
        for (int i = 0; i < max; i++)
            sb.append(nameList.getNames()[i] + ";");
        searchBox.setText(sb.toString());
        searchBox.setCaretPosition(searchBox.getText().length());

        selectMatchingRows(nameList.getNames());
    }

    /**
     * If no rows are selected, selects rows matching entries in the given list of
     * names. If there is an existing selection, selects the intersection of the
     * list of names with the existing selection.
     */
    private void selectMatchingRows(String[] names) {
        ListSelectionModel selectionModel = resultsTable.getSelectionModel();

        if (selectionModel.isSelectionEmpty()) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (String name : names) {
                    if (name.equalsIgnoreCase((String) tableModel.getValueAt(i, 0))) {
                        selectionModel.addSelectionInterval(i, i);
                    }
                }
            }
        } else {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (selectionModel.isSelectedIndex(i)) {
                    boolean select = false;
                    for (String name : names) {
                        if (name.equalsIgnoreCase((String) tableModel.getValueAt(i, 0))) {
                            select = true;
                            break;
                        }
                    }
                    if (!select) {
                        selectionModel.removeSelectionInterval(i, i);
                    }
                }
            }
        }
    }


    public void handleMatrix(String source, DataMatrix matrix) {
        StringBuffer sb = new StringBuffer();
        sb.append(" >>> handleMatrix: " + matrix.getRowCount() + " x "
                + matrix.getColumnCount() + "\n");

        sb.append("\n\n");
        // textArea.append (sb.toString ());
        // textArea.setCaretPosition (textArea.getText().length()); 

    }

    @SuppressWarnings("unchecked")
    public void handleTuple(String source, GaggleTuple gaggleTuple) {
         // todo tuple fix
        /*
        StringBuffer sb = new StringBuffer();
        sb.append(" >>> handleMap: " + attributeMap.getName() + "\n");
        String[] keys = AttributeMapUtil.getAttributeNames(attributeMap);
        Namelist nameList = new Namelist();
        nameList.setSpecies(species);
        nameList.setNames(keys);
        handleNameList(gooseName, nameList);
        */
    }

    public void handleCluster(
            String source, Cluster cluster) {
        Namelist nameList = new Namelist();
        nameList.setSpecies(species);
        nameList.setNames(cluster.getRowNames());
        handleNameList(gooseName, nameList);
    }

    public void handleNetwork(String source, Network network) {
        String[] nodes = network.getNodes();

        handleNameList(gooseName, new Namelist(species, nodes));
        // StringBuffer sb = new StringBuffer ();
        // sb.append (" >>> handleNetwork: " + "\n");
        // sb.append (" nodes: " + network.nodeCount () + "\n");
        // sb.append (" edges: " + network.edgeCount () + "\n");
        //
        // sb.append ("\n\n");
        // textArea.append (sb.toString ());
        // textArea.setCaretPosition (textArea.getText().length());
    }

    public void clearSelections() {
        resultsTable.clearSelection();
    }

    private String[] getSelection() {
        int[] selectedRows = resultsTable.getSelectedRows();
        String[] selectedNames = new String[selectedRows.length];

        for (int i = 0; i < selectedRows.length; i++) {
            selectedNames[i] = (String) tableModel.getValueAt(selectedRows[i], column);
        }

        return selectedNames;
    }

    public String[] getSearchResults() {
        String[] names = new String[tableModel.getRowCount()];
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            names[i] = (String) tableModel.getValueAt(i, column);
        }
        return names;
    }

    public int getSelectionCount() {
        if (resultsTable == null)
            return 0;
        return resultsTable.getSelectedRowCount();
    }

    public String getName() {
        return gooseName;
    }

    public void setName(String newName) {
        gooseName = newName;
        setTitle(gooseName);
        super.setName(gooseName);
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public void setGeometry(int x, int y, int width, int height) {
        setBounds(x, y, width, height);
    }


    /**
     * select the column that will be broadcast. Defaults to 0.
     */
    public void setColumn(int column) {
        this.column = column;
    }

    public void doBroadcastList() {
        String[] names = getSelection();

        // if nothing's selected default to all search results
        if (names.length == 0) {
            names = getSearchResults();
        }

        // if we still got nothing, bail.
        if (names.length == 0) {
            setStatus("Nothing selected to broadcast");
            return;
        }

        Namelist nameList = new Namelist();
        nameList.setSpecies(species);
        nameList.setNames(names);
        try {
            boss.broadcastNamelist(gooseName, targetGoose, nameList);
            setStatus("Broadcast " + names.length + " names");
        }
        catch (RemoteException rex) {
            rex.printStackTrace();
            setStatus("Error while trying to broadcast");
        }
    }

    public void doHide() {
        setVisible(false);
    }

    public void doShow() {
        setAlwaysOnTop(true);
        setVisible(true);
        setAlwaysOnTop(false);
    }

    public void doExit() {
        gaggleConnector.disconnectFromGaggle(true);
        System.exit(0);
    }

    public void clearSearchResults() {
        tableModel.clearData();
        tableModel.fireTableDataChanged();
        setSearchResultCount(0);
    }

    public void toggleKeyOnlyMode() {
        mode = (mode == Mode.normal) ? Mode.keys_only : Mode.normal;
    }

    /**
     * Reads a tab-delimited text file. Parses the first three columns as name,
     * gene symbol, and annotation. Skips first row.
     */
    protected void readAnnotationData(String uri, boolean append) {
        String[] lines = new String[0];
        String protocol = "file://"; // the default
        boolean explicitProtocol = (uri.indexOf("://") > 0);
        if (explicitProtocol)
            protocol = uri.substring(0, uri.indexOf("://") + 3);
        String rawText = "";

        try {
            if (!explicitProtocol || protocol.equals("file://")) {
                String filename;
                if (explicitProtocol)
                    filename = uri.substring(7);
                else
                    filename = uri;
                File file = new File(filename);
                if (!file.exists())
                    throw new IllegalArgumentException("cannot find file named '"
                            + filename + "'");
                if (!file.canRead())
                    throw new IllegalArgumentException("cannot read file named '"
                            + filename + "'");
                TextFileReader reader = new TextFileReader(file.getPath());
                reader.read();
                rawText = reader.getText();
            } else if (protocol.equals("jar://")) {
                TextJarReader reader = new TextJarReader(uri);
                reader.read();
                rawText = reader.getText();
            } else if (protocol.equals("http://")) {
                TextHttpReader reader = new TextHttpReader(uri);
                reader.read();
                rawText = reader.getText();
            } else {
                String msg = "unrecognized protocol for SequenceFetcher: '" + protocol
                        + "'";
                throw new IllegalArgumentException(msg);
            }
            lines = rawText.split("\n");
            System.out.println(" * annotation lines read: " + lines.length);
        } // try
        catch (Exception ex0) {
            String msg = "<html>Error reading annotation from <br>" + uri + ":<br>"
                    + ex0.getMessage() + "</html>";
            JOptionPane.showMessageDialog(this, msg);
        }

        if (!append) {
            index.clear();
            summaryMap.clear();
        }

        if (lines.length == 0)
            return;

        int i = 0;
        while (lines[i] == null || lines[i].startsWith("#")) {
            i++;
        }

        // first non-comment line determines column names
        if (lines[i] != null) {
            String[] columnNames = lines[i].split("\t");

            // if we're appending we use the widest set of column names
            if (!append || tableModel.getColumnCount() < columnNames.length) {
                tableModel.setColumnNames(columnNames);
                tableModel.fireTableStructureChanged();
                setTableColumnWidths();
            }
        }

        // parse remaining lines
        int count = 0;
        for (i++; i < lines.length; i++) {
            if (lines != null && !lines[i].startsWith("#")) {
                String[] fields = lines[i].split("\t");
                if (fields.length > 1) {
                    String orf = fields[0];
                    index.put(orf, orf);
                    for (int j = 1; j < fields.length; j++) {
                        index.put(fields[j], orf);
                    }
                    summaryMap.put(orf, fields);
                    count++;
                }
            }
        }

        setStatus("Loaded " + count + " annotations. " + summaryMap.size() + " total.");

        // set the selections in the column menu
        updateColumnMenu();

        // display all loaded annotations
        displayHits(summaryMap.keySet());
    }

    protected String rationalizeSearchText(String searchText) {
        searchText = searchText.replaceAll("\\s+", ";");
        searchText = searchText.replaceAll(":", ";");
        return searchText;
    }

    protected void search() {
        String searchText = searchBox.getText().trim();
        if (searchText.length() < 1)
            return;

        searchText = rationalizeSearchText(searchText);

        if (mode == Mode.keys_only) {
            // In keys_only mode, the search terms are keys, so there's no need to do
            // a full text search. Plus, we want to preserve the order of the keys.
            displayHits(Arrays.asList(searchText.split(";")));
        }
        else {
            HashSet<String> hitsList = new HashSet<String>();

            // 'arrows' are supplied by the user. we see if any strike a 'target' -- a
            // key in the index
            String soughtForText = searchText.toLowerCase();
            String[] arrows = soughtForText.split(";");

            for (String key : index.keySet()) {
                for (int a = 0; a < arrows.length; a++) {
                    String arrow = arrows[a].trim();
                    if (arrow.length() > 0 && key.indexOf(arrow) >= 0) {
                        hitsList.addAll(index.get(key));
                    }
                }
            }

            displayHits(hitsList);
        }
    }

    /**
     * display all rows whose id field is in the collection of hits
     */
    private void displayHits(Collection<String> hits) {
        String[] ids = (String[]) hits.toArray(new String[hits.size()]);

        setStatus("Found " + ids.length + " matches out of " + summaryMap.size()
                + " annotations.");
        setSearchResultCount(ids.length);

        tableModel.clearData();

        if (mode == Mode.normal)
            Arrays.sort(ids);

        for (int i = 0; i < ids.length; i++) {
            String orf = ids[i];
            String[] row = (String[]) summaryMap.get(orf);
            tableModel.addSearchResult(row);
        }

        tableModel.fireTableDataChanged();

        if (ids.length == 0)
            return;

        selectAllRowsButton.setEnabled(true);
        clearTableButton.setEnabled(true);
        clearAllSelectionsButton.setEnabled(true);
    }

    /**
     * show in the gui how many search results were found
     */
    private void setSearchResultCount(int length) {
        retrievedCountReadout.setText(String.valueOf(length));
    }

    public void select(String[] names) {
        // System.out.println ("SearchPanel.select: " + names.length);
        StringBuffer sb = new StringBuffer();
        sb.append(searchBox.getText());
        for (int i = 0; i < names.length; i++) {
            if (sb.length() > 0)
                sb.append(";");
            sb.append(names[i]);
        }
        searchBox.setText(sb.toString());
    } // select

    /* actions  ------------------------------------------------------------- */

    class BroadcastSelectionAction extends AbstractAction {
        BroadcastSelectionAction() {
            this("B");
        }

        BroadcastSelectionAction(String name) {
            super(name);
            this.putValue(Action.SHORT_DESCRIPTION,
                    "Broadcast selected names to selected goose");
        }

        public void actionPerformed(ActionEvent e) {
            doBroadcastList();
        } // actionPerformed

    } // inner class BroadcastSelectionAction

    class SelectAllRowsAction extends AbstractAction {
        SelectAllRowsAction() {
            super("Select All");
            this.putValue(Action.SHORT_DESCRIPTION,
                    "Select all rows in current search results");
        }

        public void actionPerformed(ActionEvent e) {
            resultsTable.selectAll();
        } // actionPerformed

    } // inner class SelectAllRowsAction

    class ClearAllSelectionsAction extends AbstractAction {
        ClearAllSelectionsAction() {
            super("Deselect All");
            this.putValue(Action.SHORT_DESCRIPTION,
                    "Clear selection in search results");
        }

        public void actionPerformed(ActionEvent e) {
            resultsTable.clearSelection();
        } // actionPerformed
    } // inner class ClearAllSelectionsAction

    class ClearTableAction extends AbstractAction {
        ClearTableAction() {
            super("Clear All");
            this.putValue(Action.SHORT_DESCRIPTION, "Clear search results");
        }

        public void actionPerformed(ActionEvent e) {
            clearSearchResults();
        }
    }

    class DoSearchAction extends AbstractAction {
        DoSearchAction() {
            super("Search");
            this.putValue(Action.SHORT_DESCRIPTION,
                    "Find every gene matching (in name or annotation)");
        }

        public void actionPerformed(ActionEvent e) {
            search();
        }
    }

    class QuitAction extends AbstractAction {
        QuitAction() {
            super("Quit");
        }

        public void actionPerformed(ActionEvent e) {
            doExit();
        }
    }

    class ToggleKeyOnlyModeAction extends AbstractAction {
        ToggleKeyOnlyModeAction() {
            super("Key-only mode");
        }

        public void actionPerformed(ActionEvent e) {
            toggleKeyOnlyMode();
        }
    }

    class ShowLoadAnnotationDialogAction extends AbstractAction {
        ShowLoadAnnotationDialogAction() {
            super("Load Annotation File");
            this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_L, InputEvent.META_DOWN_MASK));
            this.putValue(AbstractAction.MNEMONIC_KEY, new Integer('L'));
            // Will appear as tooltip text
            this.putValue(SHORT_DESCRIPTION, "Select an annotation file and load it");
        }

        public void actionPerformed(ActionEvent e) {
            LoadAnnotationDialog dialog = new LoadAnnotationDialog(
                    AnnotationGoose.this);

            // position dialog box
            Point p = AnnotationGoose.this.getLocation();
            p.x += 80;
            p.y += 100;
            dialog.setLocation(p);

            dialog.setSpecies(species);
            dialog.setVisible(true);

            if (dialog.exitedWithOk()) {
                setSpecies(dialog.getSpecies());
                readAnnotationData(dialog.getUri(), dialog.getAppend());
            }
        }
    }


    public class UpdateSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            selectionCountReadout.setText(String.valueOf(resultsTable
                    .getSelectedRowCount()));
        }
    }

    class SelectColumnAction extends AbstractAction {
        private int index;

        public SelectColumnAction(String name, int index) {
            super(name);
            this.index = index;
            this.putValue(Action.SHORT_DESCRIPTION, "Selects the column that will be broadcast");
            this.putValue(AbstractAction.MNEMONIC_KEY, new Integer('S'));
        }

        public void actionPerformed(ActionEvent event) {
            column = index;
            for (int i = 0; i < columnMenu.getItemCount(); i++) {
                columnMenu.getItem(i).setSelected(i == column);
            }
        }
    }

    /**
     * Keep track of which component has focus so we can cut from and
     * paste to the right component when using the menu.
     */
    public class TransferActionListener implements ActionListener, PropertyChangeListener {
        private JComponent focusOwner = null;

        public TransferActionListener() {
            KeyboardFocusManager manager = KeyboardFocusManager.
                    getCurrentKeyboardFocusManager();
            manager.addPropertyChangeListener("permanentFocusOwner", this);
        }

        public void propertyChange(PropertyChangeEvent e) {
            Object o = e.getNewValue();
            if (o instanceof JComponent) {
                focusOwner = (JComponent) o;
            } else {
                focusOwner = null;
            }
        }

        public void actionPerformed(ActionEvent e) {
            // there are only two components for which copy
            // and paste make sense, so if the currently focused
            // component isn't the search box, default to the
            // results table.
            JComponent component = focusOwner;
            if (component != searchBox) {
                component = resultsTable;
            }
            String action = (String) e.getActionCommand();
            Action a = component.getActionMap().get(action);
            if (a != null) {
                a.actionPerformed(new ActionEvent(component,
                        ActionEvent.ACTION_PERFORMED,
                        null));
            }
        }
    }

    /**
     * render table cells containing urls as clickable links
     */
    class LinkRenderer extends DefaultTableCellRenderer {

        public LinkRenderer() {
        }

        @Override
        protected void setValue(Object value) {
            String s = String.valueOf(value);
            if (isUrl(s)) {
                setText("<html><a href=\"" + s + "\">" + s + "</a></html>");
            } else if (isAnchorTag(s)) {
                setText("<html>" + s + "</html>");
            } else {
                super.setValue(value);
            }
        }
    }

    private boolean isUrl(String s) {
        return s.startsWith("http:") || s.startsWith("ftp:") || s.startsWith("file:") || s.startsWith("sbeams:");
    }

    private boolean isAnchorTag(String s) {
        Matcher m = anchorTagPattern.matcher(s);
        return m.matches();
    }

    private String getLink(Point point) {
        int row = resultsTable.rowAtPoint(point);
        int column = resultsTable.columnAtPoint(point);
        String value = String.valueOf(resultsTable.getValueAt(row, column));
        if (isUrl(value)) {
            return value;
        } else {
            Matcher m = anchorTagPattern.matcher(value);
            if (m.matches()) {
                return m.group(1);
            } else
                return null;
        }
    }

    /**
     * When mousing over urls, show hand cursor and respond to clicks.
     */
    class LinkMouseMotionListener extends MouseMotionAdapter {
        @Override
        public void mouseMoved(MouseEvent e) {
            String url = getLink(e.getPoint());
            if (url != null) {
                resultsTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                resultsTable.setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    class LinkMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            String url = getLink(e.getPoint());
            if (url != null) {
                System.out.println("Browse to " + url);
                MiscUtil.displayWebPage(url);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            resultsTable.setCursor(Cursor.getDefaultCursor());
        }
    }

    public void update(String[] activeGooseNames) {
        this.activeGooseNames = activeGooseNames;
        MiscUtil.updateGooseChooser(gooseChooser, gooseName, activeGooseNames);
    }


    public static void main(String[] args) throws Exception {
        AnnotationGoose goose = new AnnotationGoose();

        // set options
        Options options = new Options(args);
        goose.setSpecies(options.species);
        goose.setName(options.gooseName);

        // read data file
        if (options.gotDataUri())
            goose.readAnnotationData(options.dataUri, false);

        goose.connectToGaggle();
    }

} // AnnotationGoose
