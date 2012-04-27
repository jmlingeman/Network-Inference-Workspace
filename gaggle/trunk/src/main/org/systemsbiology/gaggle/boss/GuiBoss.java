// GuiBoss.java
//-------------------------------------------------------------------------------
// RCS:  $Revision: 4359 $   $Date: 2009-12-09 17:02:43 -0500 (Wed, 09 Dec 2009) $
//  (29 aug 2005): gaggle no longer requires RMI stubs
//  (01 sep 2005): GuiBoss now has 'listen all' and 'listen none' buttons
//  (03 sep 2005): annotation plugin now gets uri from properties file
//  (09 sep 2005): removed vestigial broadcast buttons from goose table
//  (15 sep 2005): forcing rebuild with boss.jar built including JGR rmi stub
//  (24 sep 2005): forcing rebuild with boss.jar built including latest Network class
//  (25 sep 2005): forcing rebuild with fix to Network class getInteractions
//  (25 sep 2005): forcing rebuild with fix to Network class species handling
//  (30 sep 2005): forcing rebuild after restoring MatrixPlotter from class file
//  (04 oct 2005): forcing rebuild after expanding broadcasts to include targetGoose
//  (06 oct 2005): forcing rebuild -- refresh now called before returning goose names
//  (28 oct 2005): forcing rebuild -- NetworkUtil now handles cytoscape network broadcast & receive
//  (02 nov 2005): forcing rebuild -- after fixing SBeamsSearchPanel plugin
//  (03 nov 2005): forcing rebuild -- trial deploy to gaggle.systemsbiology.net/2005-11
//  (15 mar 2005): forcing rebuild -- public release of source code
//  (30 may 2006): forcing rebuild
//  (30 may 2006): forcing uprev   -- SBeams search now has count readouts
//  (23 aug 2006): forcing upref   -- BossConfig now checks for null props file name
//-------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
// GuiBoss.java
//-------------------------------------------------------------------------------
// RCS:  $Revision: 4359 $   $Date: 2009-12-09 17:02:43 -0500 (Wed, 09 Dec 2009) $
//  (29 aug 2005): gaggle no longer requires RMI stubs
//  (01 sep 2005): GuiBoss now has 'listen all' and 'listen none' buttons
//  (03 sep 2005): annotation plugin now gets uri from properties file
//  (09 sep 2005): removed vestigial broadcast buttons from goose table
//  (15 sep 2005): forcing rebuild with boss.jar built including JGR rmi stub
//  (24 sep 2005): forcing rebuild with boss.jar built including latest Network class
//  (25 sep 2005): forcing rebuild with fix to Network class getInteractions
//  (25 sep 2005): forcing rebuild with fix to Network class species handling
//  (30 sep 2005): forcing rebuild after restoring MatrixPlotter from class file
//  (04 oct 2005): forcing rebuild after expanding broadcasts to include targetGoose
//  (06 oct 2005): forcing rebuild -- refresh now called before returning goose names
//  (28 oct 2005): forcing rebuild -- NetworkUtil now handles cytoscape network broadcast & receive
//  (02 nov 2005): forcing rebuild -- after fixing SBeamsSearchPanel plugin
//  (03 nov 2005): forcing rebuild -- trial deploy to gaggle.systemsbiology.net/2005-11
//  (15 mar 2005): forcing rebuild -- public release of source code
//  (30 may 2006): forcing rebuild
//  (30 may 2006): forcing uprev   -- SBeams search now has count readouts
//  (23 aug 2006): forcing upref   -- BossConfig now checks for null props file name
//-------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.boss;
//------------------------------------------------------------------------------- 

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.table.TableColumnModel;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.*;


import java.rmi.server.*;
import java.rmi.registry.*;
import java.rmi.Naming;
import java.rmi.RemoteException;

import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import java.io.*;
import java.security.Security;

import org.systemsbiology.gaggle.util.MiscUtil;
import org.systemsbiology.gaggle.util.NewNameHelper;
import org.systemsbiology.gaggle.geese.*;
import org.systemsbiology.gaggle.boss.plugins.*;
import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.datatypes.*;

//-------------------------------------------------------------------------------

public final class GuiBoss extends UnicastRemoteObject
        implements Boss, WindowStateListener, Serializable {

    protected JFrame frame;
    protected JTable gooseTable;
    protected JScrollPane scrollPane;
    protected JTextField searchBox;
    protected GaggleBossTableModel gooseTableModel;
    protected String serviceName = "gaggle";
    //protected String serviceUrl = "//127.0.0.1:1099/gaggle"; //todo - determine if this will break users who use geese on non-local machines
    protected NewNameHelper nameHelper;

    protected HashMap<String, Goose> gooseMap;
    protected HashMap gooseListeningMap;
    //transient NameUniquifier nameUniquifier;

    ArrayList deafGeese = new ArrayList();
    ArrayList plugins = new ArrayList();

    JPanel outerPanel;
    JTabbedPane tabbedPanel;
    JButton frameSizeToggleButton;
    boolean bodyVisible = true;
    BossConfig config;

    //---------------------------------------------------------------------------------
    public GuiBoss() throws RemoteException {
        this(new String[0]);
    }

    //---------------------------------------------------------------------------------
    public GuiBoss(String[] args) throws RemoteException {

        Security.setProperty("networkaddress.cache.ttl","0");
        Security.setProperty("networkaddress.cache.negative.ttl","0");
        System.out.println("ttl settings changed in boss");

        config = new BossConfig(args);
        String nameHelperUri = config.getNameHelperUri();
        if (nameHelperUri != null && nameHelperUri.length() > 0)
            try {
                nameHelper = new NewNameHelper(nameHelperUri);
            }
            catch (Exception ex0) {
                String msg = "Error reading name helper file from " + nameHelperUri + "\n" + ex0.getMessage();
                JOptionPane.showMessageDialog(frame, msg);
            }
        System.out.println("start invisibly? " + config.startInvisibly());
        System.out.println("start minimized? " + config.startMinimized());

        gooseMap = new HashMap<String, Goose>();
        gooseListeningMap = new HashMap();
        //nameUniquifier = new NameUniquifier();

        frame = new JFrame(createFrameTitle());
        frame.addWindowStateListener(this);

        MiscUtil.setApplicationIcon(frame);


        try {
            LocateRegistry.createRegistry(1099);
            Naming.rebind(serviceName, this);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Gaggle Port already in use.  Exiting....");
            System.exit(0);
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowStateListener(this);
        frame.getContentPane().add(createGui());

        frame.pack();
        if (!config.startInvisibly()) {
            frame.setVisible(true);
            frame.toFront();
        }
        if (config.startMinimized()) {
            frame.setVisible(true);
            frame.setState(Frame.ICONIFIED);
        }

    } // ctor

    //---------------------------------------------------------------------------------
    public BossConfig getConfig() {
        return config;
    }

    //---------------------------------------------------------------------------------
    public NewNameHelper getNameHelper() {
        return nameHelper;
    }

    //---------------------------------------------------------------------------------
    public JFrame getFrame() {
        return frame;
    }

    //---------------------------------------------------------------------------------
    public void windowStateChanged(WindowEvent e) {
        //System.out.println("\n-------- GuiBoss window event: " + e.paramString());
        //System.out.println("old state: " + e.getOldState() + "  new state: " + e.getNewState());
    }

    //---------------------------------------------------------------------------------
    public String actuallyRenameGoose(String oldName, String proposedName) throws RemoteException {
        String uniqueName = NameUniquifier.makeUnique(proposedName,
                gooseMap.keySet().toArray(new String[0]));
        //String uniqueName = nameUniquifier.makeUnique(proposedName);

        if (gooseMap.containsKey(oldName)) {
            Goose goose = (Goose) gooseMap.get(oldName);
            gooseMap.remove(oldName);
            gooseMap.put(uniqueName, goose);
            goose.setName(uniqueName);
            unregisterIdleGeeseAndUpdate();
            return uniqueName;
        }

        return null;

    }



    public String renameGoose(String oldName, String proposedName) {
        String uniqueName = null;
        try {
           uniqueName = actuallyRenameGoose(oldName, proposedName);
        } catch (RemoteException ex) {
            String msg = "Failed to contact goose to rename: " + oldName + " -> " + proposedName;
            JOptionPane.showMessageDialog(frame, msg);
            return null;
        }

        String[] appNames = GuiBoss.this.gooseTableModel.getAppNames();
        for (int i = 0; i < appNames.length; i++) {
            if (appNames[i].equals(oldName)) {
                GuiBoss.this.gooseTableModel.setAppNameAtRow(uniqueName, i);
                GuiBoss.this.gooseTableModel.fireTableDataChanged();
            }
        }

        return uniqueName;

    } // renameGoose

    /**
     * Check to see if we can communicate with all currently registered geese
     * and unregister any that do not respond, then update all geese with the
     * newly derived list of active geese. This is currently triggered by
     * the refresh button, and also by any goose registering or unregistering
     * or being renamed.
     */
    protected void unregisterIdleGeeseAndUpdate() {
        List<String> idleGeeseNames = new ArrayList<String>();
        for (String gooseName : gooseMap.keySet()) {
            try {
                gooseMap.get(gooseName).getName();
            } catch (RemoteException e) {
                System.out.println("Removing idle goose '" + gooseName + "'");
                idleGeeseNames.add(gooseName);
            }
        }
        for (String idleGooseName : idleGeeseNames) {
            unregister(idleGooseName, false);
        }
        updateGeese();
    }

    //---------------------------------------------------------------------------------
    protected String createFrameTitle() {
        String id = "$Revision: 4359 $";
        String signature = "Revision: ";
        int start = id.indexOf(signature);
        start += signature.length();
        int end = id.indexOf(" $", start);
        String versionNumber = id.substring(start, end);
        return "Gaggle Boss v." + versionNumber;
    }


    //-----------------------------------------------------------------------------------------------------
    public String register(Goose goose) throws RemoteException {
        String uniqueName = NameUniquifier.makeUnique(goose.getName(),
                gooseMap.keySet().toArray(new String[0]));
        //String uniqueName = nameUniquifier.makeUnique(goose.getName());
        goose.setName(uniqueName);
        addNewGoose(uniqueName, goose);
        refresh();
        unregisterIdleGeeseAndUpdate();
        return uniqueName;
    }


    private void updateGeese() {
        String[] keys = (String[]) gooseMap.keySet().toArray(new String[0]);
        Arrays.sort(keys); // why does this need to be sorted?
        for (String gooseName : keys) {
            Goose goose = (Goose) gooseMap.get(gooseName);
            try {
                goose.update(keys);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    //---------------------------------------------------------------------------------
    public String register(DeafGoose deafGoose) throws RemoteException {
        deafGeese.add(deafGoose);
        return "";
    }


    //---------------------------------------------------------------------------------
    /**
     * Unregisters a goose
     *
     * @param gooseName the name of the goose to unregister
     */
    public void unregister(String gooseName) {
        unregister(gooseName, true);
    }

    public void unregister(String gooseName, boolean doUpdate) {
        System.out.println("boss: received unregister request for " + gooseName);
        try {
            if (gooseMap.containsKey(gooseName)) {
                gooseMap.remove(gooseName);
            }
            if (gooseListeningMap.containsKey(gooseName))
                gooseListeningMap.remove(gooseName);
            gooseTableModel.removeGoose(gooseName);
            setTableColumnWidths();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (doUpdate)
            unregisterIdleGeeseAndUpdate();
    }

    //---------------------------------------------------------------------------------
    public Goose[] getGeese() {
        return (Goose[]) gooseMap.values().toArray(new Goose[0]);
    }

    //---------------------------------------------------------------------------------
/**
 * return the names of the geese currently selected in the goose table
 */
    private String[] getSelectedGooseNames() {
        ArrayList list = new ArrayList();
        int[] selectedRows = gooseTable.getSelectedRows();
        String[] gooseNamesInTable = gooseTableModel.getAppNames();

        for (int i = 0; i < selectedRows.length; i++)
            list.add(gooseNamesInTable[selectedRows[i]]);

        return (String[]) list.toArray(new String[0]);

    } // getSelectedGooseNames

    //---------------------------------------------------------------------------------
    private String[] getUnselectedGooseNames() {
        ArrayList list = new ArrayList();
        int[] selectedRows = gooseTable.getSelectedRows();
        String[] selectedGooseNames = getSelectedGooseNames();
        String[] allNames = getGooseNames();
        Arrays.sort(selectedGooseNames);
        Arrays.sort(allNames);
        for (int i = 0; i < allNames.length; i++)
            if (Arrays.binarySearch(selectedGooseNames, allNames[i]) < 0)
                list.add(allNames[i]);


        return (String[]) list.toArray(new String[0]);

    } // getUnelectedGooseNames

    //---------------------------------------------------------------------------------
    public Goose getGoose(String name) {
        return (Goose) gooseMap.get(name);
    }

    //---------------------------------------------------------------------------------
    public String[] getGooseNames() {
        return (String[]) gooseMap.keySet().toArray(new String[0]);
    }

    //---------------------------------------------------------------------------------
    public HashMap getGooseMap() {
        return gooseMap;
    }

    //---------------------------------------------------------------------------------
    protected void broadcastToPlugins(String[] names) {
        for (int i = 0; i < plugins.size(); i++) {
            GaggleBossPlugin plugin = (GaggleBossPlugin) plugins.get(i);
            plugin.select(names);
        }

    }

    //---------------------------------------------------------------------------------
    public void broadcastNamelist(String sourceGoose, String targetGoose, Namelist nameList) throws RemoteException {
        long startTime = System.currentTimeMillis();
        broadcastToPlugins(nameList.getNames());

        String[] gooseNames;
        if (targetGoose == null || targetGoose.equalsIgnoreCase("boss") || targetGoose.equalsIgnoreCase("all"))
            gooseNames = getListeningGeese();
        else
            gooseNames = new String[]{targetGoose};

        for (int i = 0; i < gooseNames.length; i++) {
            String gooseName = gooseNames[i];
            if (gooseName.equals(sourceGoose))
                continue;
            Goose goose = getGoose(gooseName);
            if (goose == null)
                continue;

            try {
                goose.handleNameList(sourceGoose, nameList);
            }
            catch (Exception ex0) {
                System.err.println("error in select request to " + goose.getName() + ": " +
                        ex0.getMessage());
                ex0.printStackTrace();
            }
        } // for i

        refresh(true);
        long duration = System.currentTimeMillis() - startTime;

    } // broadcast (String, String, String [])

    //---------------------------------------------------------------------------------
    public void broadcastMatrix(String sourceGoose, String targetGoose, DataMatrix matrix) throws RemoteException {
        long startTime = System.currentTimeMillis();
        broadcastToPlugins(matrix.getRowTitles());

        String[] gooseNames;
        if (targetGoose == null || targetGoose.equalsIgnoreCase("boss") || targetGoose.equalsIgnoreCase("all"))
            gooseNames = getListeningGeese();
        else
            gooseNames = new String[]{targetGoose};


        for (int i = 0; i < gooseNames.length; i++) {
            String gooseName = gooseNames[i];
            if (gooseName.equals(sourceGoose))
                continue;
            Goose goose = getGoose(gooseName);
            if (goose == null)
                continue;
            try {
                goose.handleMatrix(sourceGoose, matrix);
            }
            catch (Exception ex0) {
                System.err.println("error in handleMatrix request to " + gooseName + ": " +
                        ex0.getMessage());
                ex0.printStackTrace();
            }

        } // for i

        refresh(true);
        long duration = System.currentTimeMillis() - startTime;
        //System.out.println ("GuiBoss.broadcast (matrix), " + matrix.getRowCount () + " x " +
        //                    matrix.getColumnCount () + ": " +
        //                    duration + " msecs");

    } // broadcast (matrix)

    //---------------------------------------------------------------------------------
    public void broadcastTuple(String sourceGoose, String targetGoose, GaggleTuple gaggleTuple) {
        long startTime = System.currentTimeMillis();
        refresh();
        String[] gooseNames;
        if (targetGoose == null || targetGoose.equalsIgnoreCase("boss") || targetGoose.equalsIgnoreCase("all"))
            gooseNames = getListeningGeese();
        else
            gooseNames = new String[]{targetGoose};

        for (int i = 0; i < gooseNames.length; i++) {
            String gooseName = gooseNames[i];
            if (gooseName.equals(sourceGoose))
                continue;
            Goose goose = getGoose(gooseName);
            if (goose == null)
                continue;
            try {
                System.out.println("broadcastTuple to " + gooseName);
                goose.handleTuple(sourceGoose, gaggleTuple);
            }
            catch (Exception ex0) {
                System.err.println("error in broadcastTuple to " + gooseName + ": " +
                        ex0.getMessage());
                ex0.printStackTrace();
            }
        } // for i

        // refresh ();
        //long duration = System.currentTimeMillis () - startTime;
        //System.out.println ("GuiBoss.broadcastAttributes, hash size: " + attributesData.size () +
        //                     ": " + duration + " msecs");

    }

    //---------------------------------------------------------------------------------
    public void broadcastCluster(String sourceGoose, String targetGoose,
                                 Cluster cluster) {
        long startTime = System.currentTimeMillis();
        broadcastToPlugins(cluster.getRowNames());

        String[] gooseNames;
        if (targetGoose == null || targetGoose.equalsIgnoreCase("boss") || targetGoose.equalsIgnoreCase("all"))
            gooseNames = getListeningGeese();
        else
            gooseNames = new String[]{targetGoose};

        for (int i = 0; i < gooseNames.length; i++) {
            String gooseName = gooseNames[i];
            if (gooseName.equals(sourceGoose))
                continue;
            if (!isListening(gooseName))
                continue;
            Goose goose = getGoose(gooseName);
            if (goose == null)
                continue;
            try {
                goose.handleCluster(sourceGoose, cluster);
            }
            catch (Exception ex0) {
                System.err.println("error in broadcastCluster () to " + gooseName + ": " +
                        ex0.getMessage());
                ex0.printStackTrace();
            }
        } // for i

        long duration = System.currentTimeMillis() - startTime;
        refresh();
        System.out.println("GuiBoss.broadcastCluster  " + cluster.getName() + ", " +
                "rows: " + cluster.getRowNames().length +
                "columns: " + cluster.getColumnNames().length +
                ": " + duration + " msecs");

    } // broadcast (species, clusterName, rowNames, columnNames)

    //---------------------------------------------------------------------------------
    public void broadcastNetwork(String sourceGoose, String targetGoose, Network network) {
        String[] gooseNames;
        if (targetGoose == null || targetGoose.equalsIgnoreCase("boss") || targetGoose.equalsIgnoreCase("all"))
            gooseNames = getListeningGeese();
        else
            gooseNames = new String[]{targetGoose};


        for (int i = 0; i < gooseNames.length; i++) {
            String gooseName = gooseNames[i];
            if (gooseName.equals(sourceGoose))
                continue;
            Goose goose = getGoose(gooseName);
            if (goose == null)
                continue;
            try {
                goose.handleNetwork(sourceGoose, network);
            }
            catch (Exception ex0) {
                System.err.println("error in broadcastNetwork () to " + gooseName + ": " +
                        ex0.getMessage());
                ex0.printStackTrace();
            }
        } // for i

        refresh();

    } // broadcast (network)

    //---------------------------------------------------------------------------------
    public void cleanUpOnExit(String appName) throws RemoteException {
        //System.out.println ("Boss told to clean up exiting app, name: " + appName);
    }

    //---------------------------------------------------------------------------------
    public void hide(String targetGoose) throws RemoteException {
        String[] gooseNames;
        if (targetGoose == null)
            gooseNames = getListeningGeese();
        else if (targetGoose.equalsIgnoreCase("boss")) {
            frame.setVisible(false);
            return;
        } else
            gooseNames = new String[]{targetGoose};

        for (int i = 0; i < gooseNames.length; i++) {
            Goose goose = getGoose(gooseNames[i]);
            if (goose == null)
                continue;
            try {
                goose.doHide();
            }
            catch (Exception ex0) {
                System.err.println("error in hide request to " + targetGoose + ": " + ex0.getMessage());
                ex0.printStackTrace();
            }
        } // for i

    } // hide

    //---------------------------------------------------------------------------------
    public void show(String gooseName) throws RemoteException {
        if (gooseName.equalsIgnoreCase("boss")) {
            if (getFrame().getExtendedState() != java.awt.Frame.NORMAL)
                getFrame().setExtendedState(java.awt.Frame.NORMAL);
            getFrame().setAlwaysOnTop(true);
            MiscUtil.setJFrameAlwaysOnTop(getFrame(), true);
            getFrame().setVisible(true);
            getFrame().setAlwaysOnTop(false);
            MiscUtil.setJFrameAlwaysOnTop(getFrame(), false);
            return;
        }

        final Goose goose = getGoose(gooseName);
        if (goose == null)
            return;

        try {
            // System.out.println ("about to call " + gooseName + " show");
            goose.doShow();
            //System.out.println ("show " + gooseName);
        }
        catch (Exception ex0) {
            System.err.println("error in show request to " + gooseName + ": " + ex0.getMessage());
            ex0.printStackTrace();
        }

    } // show

    //---------------------------------------------------------------------------------
    public void askForShow(String gooseName) throws RemoteException {
        //getFrame().show ();
        getFrame().toBack();
        getFrame().toFront();
        getFrame().setVisible(true);

    } // show


    //---------------------------------------------------------------------------------
    public void terminate(String gooseName) throws RemoteException {
        Goose goose = getGoose(gooseName);
        if (goose == null)
            return;
        try {
            goose.doExit();
        }
        catch (java.rmi.UnmarshalException ignore0) {
        }
        catch (Exception ex1) {
            System.err.println("error in terminate request to " + gooseName + ": " + ex1.getMessage());
        }

    } // setGeometry

    //---------------------------------------------------------------------------------
    public void toggleVisibility() {
        bodyVisible = !bodyVisible;
        String label = "Shrink";
        if (!bodyVisible)
            label = "Boss";
        frameSizeToggleButton.setText(label);
        tabbedPanel.setVisible(bodyVisible);
        frame.pack();
        // outerPanel.pack ();
    }

    //---------------------------------------------------------------------------------
    protected JPanel createGui() {
        outerPanel = new JPanel();
        outerPanel.setLayout(new BorderLayout());
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        frameSizeToggleButton = new JButton("Shrink");
        frameSizeToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                toggleVisibility();
            }
        });
        toolbar.add(frameSizeToggleButton);
        outerPanel.add(toolbar, BorderLayout.NORTH);

        JButton aboutButton = new JButton("About");
        aboutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StringBuffer sb = new StringBuffer();
                sb.append("<html><center>");
                sb.append("<h3>");
                sb.append("The Gaggle is developed by the Baliga Laboratory<br>");
                sb.append("at the Institute for Systems Biology.<br>");
                sb.append("</h3>");
                sb.append("<font color='blue'>");
                sb.append("http://www.systemsbiology.org<br>");
                sb.append("</font>");
                sb.append("<br>");
                sb.append("Software engineer:  Paul Shannon<br>");
                sb.append("Principal Investigator: Nitin S. Baliga<br>");
                sb.append("<br>");
                sb.append("Supported by research grants from NSF, DoE and DoD<br>");
                sb.append("<br>");
                sb.append("For more information visit the Baliga Laboratory: <br>");
                sb.append("<font color='blue' size='-1'>");
                sb.append("http://www.systembiology.org/Scientists_and_Research/Faculty_Groups/Baliga_Group");
                sb.append("</font>");
                sb.append("</center></html>");
                String msg = sb.toString();
                String title = "About the Gaggle";
                int messageType = JOptionPane.INFORMATION_MESSAGE;
                Icon icon = null;
                JOptionPane.showMessageDialog(frame, msg, title, messageType, icon);
            }
        });
        toolbar.add(aboutButton);
        outerPanel.add(toolbar, BorderLayout.NORTH);

        tabbedPanel = new JTabbedPane();
        outerPanel.add(tabbedPanel, BorderLayout.CENTER);
        // outerPanel.add (new JTextField (30), BorderLayout.SOUTH);
        tabbedPanel.add(createGaggleControlPanel(), "Gaggle");

        String[] pluginClassNames = config.getPluginNames();
        for (int i = 0; i < pluginClassNames.length; i++) {
            System.out.println("about to load boss plugin: " + pluginClassNames[i]);
            GaggleBossPlugin plugin = loadPlugin(pluginClassNames[i], this);
            if (plugin == null) {
                String msg = "Could not locate plugin '" + pluginClassNames[i] + "'";
                JOptionPane.showMessageDialog(frame, msg);
                continue;
            }
            plugins.add(plugin);
            tabbedPanel.add(plugin, plugin.getName());
        }

        return outerPanel;

    } // createGui

    //-------------------------------------------------------------------------------
    protected JPanel createGaggleControlPanel() {
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        int topBorder = 20;
        int bottomBorder = 10;
        int sideBorder = 20;

        tablePanel.setBorder(BorderFactory.createEmptyBorder(topBorder, sideBorder,
                bottomBorder, sideBorder));
        JPanel tableOuterPanel = new JPanel();
        tableOuterPanel.setLayout(new BorderLayout());
        tableOuterPanel.add(tablePanel, BorderLayout.CENTER);
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BorderLayout());
        controlsPanel.add(createButtonsInPanel(), BorderLayout.CENTER);
        // controlsPanel.add (createSearchBoxInPanel (), BorderLayout.NORTH);
        tableOuterPanel.add(controlsPanel, BorderLayout.SOUTH);


        gooseTableModel = new GaggleBossTableModel(this);
        gooseTable = new JTable(gooseTableModel);
        gooseTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        gooseTable.setShowGrid(true);
        gooseTable.setGridColor(Color.gray);
        gooseTable.setDefaultRenderer(JButton.class, new ButtonCellRenderer(
                gooseTable.getDefaultRenderer(JButton.class)));
        setTableColumnWidths();

        gooseTable.setPreferredScrollableViewportSize(new Dimension(400, 200));
        gooseTable.addMouseListener(new GaggleMouseListener(gooseTable));
        scrollPane = new JScrollPane(gooseTable);

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tableOuterPanel;

    } // createGaggleControlPanel

    //-------------------------------------------------------------------------------
    protected JPanel createButtonsInPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        JPanel actionButtonPanel = new JPanel();
        JPanel upperActionButtonPanel = new JPanel();
        JPanel lowerActionButtonPanel = new JPanel();
        JPanel quitButtonPanel = new JPanel();

        actionButtonPanel.setLayout(new GridLayout(2, 1));
        actionButtonPanel.add(upperActionButtonPanel);
        actionButtonPanel.add(lowerActionButtonPanel);

        JButton showButton = new JButton("Show");
        JButton showOthersButton = new JButton("Show Others");
        JButton hideButton = new JButton("Hide");
        JButton hideOthersButton = new JButton("Hide Others");
        JButton selectAllButton = new JButton("Select All");
        JButton refreshButton = new JButton("Refresh");
        //JButton clearSelectionsButton = new JButton("Clear Selections");
        JButton listenAllButton = new JButton("Listen All");
        JButton listenNoneButton = new JButton("Listen None");
        //JButton tileButton       = new JButton ("Tile");
        //JButton staggerButton    = new JButton ("Stagger");
        JButton terminateButton = new JButton("Terminate");
        JButton quitButton = new JButton("Quit");

        // staggerButton.setEnabled (false);

        showButton.addActionListener(new ShowAction());
        showOthersButton.addActionListener(new ShowOthersAction());
        hideButton.addActionListener(new HideAction());
        hideOthersButton.addActionListener(new HideOthersAction());
        selectAllButton.addActionListener(new SelectAllAction());
        refreshButton.addActionListener(new RefreshAction());
        listenAllButton.addActionListener(new ListenAllAction());
        listenNoneButton.addActionListener(new ListenNoneAction());
        //tileButton.addActionListener (new TileAction ());
        //staggerButton.addActionListener (new StaggerAction ());
        terminateButton.addActionListener(new TerminateAction());
        quitButton.addActionListener(new QuitAction());

        //clearSelectionsButton.addActionListener(new ClearSelectionsAction());

        upperActionButtonPanel.add(showButton);
        upperActionButtonPanel.add(showOthersButton);
        upperActionButtonPanel.add(hideButton);
        upperActionButtonPanel.add(hideOthersButton);
        //upperActionButtonPanel.add(clearSelectionsButton);

        lowerActionButtonPanel.add(selectAllButton);
        lowerActionButtonPanel.add(refreshButton);
        lowerActionButtonPanel.add(listenAllButton);
        lowerActionButtonPanel.add(listenNoneButton);
        //lowerActionButtonPanel.add (tileButton);
        //lowerActionButtonPanel.add (staggerButton);
        lowerActionButtonPanel.add(terminateButton);

        quitButtonPanel.add(quitButton);

        buttonPanel.add(actionButtonPanel, BorderLayout.CENTER);
        buttonPanel.add(quitButtonPanel, BorderLayout.SOUTH);

        return buttonPanel;

    } // createButtonsInPanel

    //-------------------------------------------------------------------------------
    protected void setTableColumnWidths() {
        return;
/**********************************
 System.out.println ("setTableColumnWidths start");
 // gooseTable.getColumnModel().getColumn (0).setPreferredWidth (100);
 gooseTable.getColumnModel().getColumn (1).setPreferredWidth (60);
 gooseTable.getColumnModel().getColumn (1).setMaxWidth (60);

 gooseTable.getColumnModel().getColumn (2).setPreferredWidth (60);
 gooseTable.getColumnModel().getColumn (2).setMaxWidth (60);

 gooseTable.getColumnModel().getColumn (3).setPreferredWidth (70);
 gooseTable.getColumnModel().getColumn (3).setMaxWidth (70);
 System.out.println ("   setTableColumnWidths end");
 **********************************/

    }

    //-------------------------------------------------------------------------------
    class SelectAllAction extends AbstractAction {

        SelectAllAction() {
            super("");
        }

        public void actionPerformed(ActionEvent e) {
            gooseTable.selectAll();
        }

    } // inner class SelectAllAction

    //-------------------------------------------------------------------------------
    class ShowAction extends AbstractAction {

        ShowAction() {
            super("");
        }

        public void actionPerformed(ActionEvent e) {
            String[] names = getSelectedGooseNames();
            for (int i = 0; i < names.length; i++) {
                try {
                    show(names[i]);
                }
                catch (RemoteException rex) {
                    rex.printStackTrace();
                }
            } // for i
        } // actionPerformed

    } // inner class ShowAction

    //-------------------------------------------------------------------------------
    class ShowOthersAction extends AbstractAction {

        ShowOthersAction() {
            super("");
        }

        public void actionPerformed(ActionEvent e) {
            String[] names = getUnselectedGooseNames();
            for (int i = 0; i < names.length; i++) {
                try {
                    show(names[i]);
                }
                catch (RemoteException rex) {
                    rex.printStackTrace();
                }
            } // for i
        }

    } // inner class ShowOthersAction

    //-------------------------------------------------------------------------------
    class HideAction extends AbstractAction {

        HideAction() {
            super("");
        }

        public void actionPerformed(ActionEvent e) {
            String[] names = getSelectedGooseNames();
            for (int i = 0; i < names.length; i++) {
                try {
                    hide(names[i]);
                }
                catch (RemoteException rex) {
                    rex.printStackTrace();
                }
            } // for i

        } // actionPerformed

    } // inner class HideAction

    //-------------------------------------------------------------------------------
    class HideOthersAction extends AbstractAction {

        HideOthersAction() {
            super("");
        }

        public void actionPerformed(ActionEvent e) {
            String[] names = getUnselectedGooseNames();
            for (int i = 0; i < names.length; i++) {
                try {
                    hide(names[i]);
                }
                catch (RemoteException rex) {
                    rex.printStackTrace();
                }
            } // for i
        } // actionPerformed

    } // inner class HideOthersAction

    //-------------------------------------------------------------------------------
    protected String[] getListeningGeese() {
        String[] allGeese = getGooseNames();
        ArrayList tmp = new ArrayList();
        for (int i = 0; i < allGeese.length; i++)
            if (listening(allGeese[i]))
                tmp.add(allGeese[i]);

        return (String[]) tmp.toArray(new String[0]);

    }

    //-------------------------------------------------------------------------------
    protected boolean listening(String gooseName) {
        return gooseTableModel.isListening(gooseName);
    }

    //-------------------------------------------------------------------------------
    protected void setSelectionCount(String gooseName, int count) {
        gooseTableModel.setSelectionCount(gooseName, count);
    }

    //-------------------------------------------------------------------------------
    //-------------------------------------------------------------------------------
    public void refresh() {
        refresh(false);
    }

    //-------------------------------------------------------------------------------
    public void refresh(boolean resetTableColumnWidths) {
        // todo - remove the following? is it used?
        if (resetTableColumnWidths)
            setTableColumnWidths();

    } // refresh

    //-------------------------------------------------------------------------------
    class RefreshAction extends AbstractAction {

        RefreshAction() {
            super("");
            putValue(AbstractAction.SHORT_DESCRIPTION,
                    "Remove disconnected geese from list.");
        }

        public void actionPerformed(ActionEvent e) {
            unregisterIdleGeeseAndUpdate();
            refresh(true);
        } // actionPerformed

    } // inner class RefreshAction

    //-------------------------------------------------------------------------------
    class ListenAllAction extends AbstractAction {

        ListenAllAction() {
            super("");
        }

        public void actionPerformed(ActionEvent e) {
            String[] gooseNames = getGooseNames();
            for (int i = 0; i < gooseNames.length; i++) {
                String name = gooseNames[i];
                gooseTableModel.setListeningState(name, true);
            } // for i
        } // actionPerformed

    } // inner class ListenAllAction

    //-------------------------------------------------------------------------------
    class ListenNoneAction extends AbstractAction {

        ListenNoneAction() {
            super("");
        }

        public void actionPerformed(ActionEvent e) {
            String[] gooseNames = getGooseNames();
            for (int i = 0; i < gooseNames.length; i++) {
                String name = gooseNames[i];
                gooseTableModel.setListeningState(name, false);
            } // for i
        } // actionPerformed

    } // inner class ListenNoneAction

    //-------------------------------------------------------------------------------
    class TileAction extends AbstractAction {

        TileAction() {
            super("");
        }

        public void actionPerformed(ActionEvent e) {
        } // actionPerformed

    } // inner class TileAction

    //-------------------------------------------------------------------------------
    class StaggerAction extends AbstractAction {

        StaggerAction() {
            super("");
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("StaggerAction");
        }

    } // inner class StaggerAction

    //-------------------------------------------------------------------------------

    class TerminateAction extends AbstractAction {

        TerminateAction() {
            super("");
        }

        public void actionPerformed(ActionEvent e) {
            refresh();
            String[] names = getSelectedGooseNames();
            for (int i = 0; i < names.length; i++) {
                try {
                    terminate(names[i]);
                }
                catch (RemoteException rex) {
                    rex.printStackTrace();
                }
            } // for i
            refresh(true);
        }

    } // inner class TerminateAction

    //-------------------------------------------------------------------------------
    class QuitAction extends AbstractAction {

        QuitAction() {
            super("");
        }

        public void actionPerformed(ActionEvent e) {
            int dialogResult = JOptionPane.showConfirmDialog(frame, "Really Quit?",
                    "Exit the Gaggle Boss?", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.NO_OPTION)
                return;

            try {
                Naming.unbind(serviceName);
            }
            catch (Exception ex0) {
                ex0.printStackTrace();
            }
            System.exit(0);
        } // actionPerformed

    } // inner class QuitAction

    //-------------------------------------------------------------------------------

    //-------------------------------------------------------------------------------

    class GaggleMouseListener implements MouseListener {
        private JTable table;

        public GaggleMouseListener(JTable table) {
            this.table = table;
        }

        private void forwardEventToButton(MouseEvent e) {
            TableColumnModel columnModel = table.getColumnModel();
            int column = columnModel.getColumnIndexAtX(e.getX());
            int row = e.getY() / table.getRowHeight();
            Object value;
            JButton button;
            MouseEvent buttonEvent;
            if (row >= table.getRowCount() || row < 0 ||
                    column >= table.getColumnCount() || column < 0)
                return;
            value = table.getValueAt(row, column);
            if (!(value instanceof JButton)) return;
            button = (JButton) value;
            buttonEvent = (MouseEvent) SwingUtilities.convertMouseEvent(table, e, button);
            button.doClick();
            //button.dispatchEvent (buttonEvent);
            table.repaint();
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
            forwardEventToButton(e);
        }

    } // inner class GaggleMouseListener

    //-------------------------------------------------------------------------------
    public void windowClosed(WindowEvent event) {
    }

    public void windowClosing(WindowEvent event) {
    }

    public void windowOpened(WindowEvent event) {
    }

    public void windowIconified(WindowEvent event) {
    }

    public void windowDeiconified(WindowEvent event) {
    }

    public void windowActivated(WindowEvent event) {
    }

    public void windowDeactivated(WindowEvent event) {
    }

    public void windowGainedFocus(WindowEvent event) {
    }

    public void windowLostFocus(WindowEvent event) {
    }

    //-------------------------------------------------------------------------------
    class ButtonCellRenderer implements TableCellRenderer {

        private TableCellRenderer defaultRenderer;

        //----------------------------------------------------
        public ButtonCellRenderer(TableCellRenderer renderer) {
            defaultRenderer = renderer;
        }

        //----------------------------------------------------
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row, int column) {
            if (value instanceof Component)
                return (Component) value;
            else
                return defaultRenderer.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);
        }
//----------------------------------------------------
    } // inner class ButtonCellRenderer

    //-------------------------------------------------------------------------------
    private void placeInCenter() {
        GraphicsConfiguration gc = frame.getGraphicsConfiguration();
        int screenHeight = (int) gc.getBounds().getHeight();
        int screenWidth = (int) gc.getBounds().getWidth();
        int windowWidth = frame.getWidth();
        int windowHeight = frame.getHeight();
        frame.setLocation((screenWidth - windowWidth) / 2, (screenHeight - windowHeight) / 2);

    } // placeInCenter

    //------------------------------------------------------------------------------
    public void addNewGoose(String name, Goose goose) {
        gooseMap.put(name, goose);
        gooseTableModel.addClient(name);
        setTableColumnWidths();
    }

    //------------------------------------------------------------------------------
    public boolean isListening(String gooseName) {
        return gooseTableModel.isListening(gooseName);
    }

    //------------------------------------------------------------------------------
    public GaggleBossPlugin loadPlugin(String className, GuiBoss gaggleBoss) {
        try {
            Class pluginClass = Class.forName(className);
            Class[] argClasses = new Class[1];
            argClasses[0] = gaggleBoss.getClass();
            Object[] args = new Object[1];
            args[0] = gaggleBoss;
            Constructor[] ctors = pluginClass.getConstructors();
            Constructor ctor = pluginClass.getConstructor(argClasses);
            Object plugin = ctor.newInstance(args);
            return (GaggleBossPlugin) plugin;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            return null;
        }

    } // loadPlugin

    //------------------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        GuiBoss app = new GuiBoss(args);
        // UnicastRemoteObject.exportObject (app);

    } // main
//-------------------------------------------------------------------------------
} // GuiBoss class
