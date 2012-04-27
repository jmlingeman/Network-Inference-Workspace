// RegexFilterGoose.java
//-------------------------------------------------------------------------------------
// $Revision: 503 $   
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
//-------------------------------------------------------------------------------------
package org.systemsbiology.gaggle.geese.regexFilter;
//-------------------------------------------------------------------------------------

import java.rmi.*;

import org.systemsbiology.gaggle.core.datatypes.DataMatrix;
import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.datatypes.*;
import org.systemsbiology.gaggle.util.MiscUtil;
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

//-------------------------------------------------------------------------------------
public class RegexFilterGoose extends JFrame implements Goose {

    String myGaggleName = "RegexFilter";
    String regexPattern = "n/a";
    Boss boss;
    RmiGaggleConnector connector = new RmiGaggleConnector(this);
    protected JScrollPane scrollPane;
    protected JTextArea textArea;
    JComboBox gooseChooser;
    boolean autoBroadcast = false;
    String targetGoose = "Boss";
    String currentSpecies = "Homo sapiens";

    String[] filteredNames = new String[0];

    //-------------------------------------------------------------------------------------
    public RegexFilterGoose(String[] args) {
        super("Debug");
        parseCommandLineArguments(args);

        try {
            connectToGaggle();
        }
        catch (Exception ex0) {
            System.err.println("RegexFilterGoose failed to export remote object: " + ex0.getMessage());
        }

        add(createGui());
        setSize(500, 500);
        MiscUtil.placeInCenter(this);
        setVisible(true);

    }

    //-------------------------------------------------------------------------------------
    JPanel createGui() {
        ToolTipManager.sharedInstance().setInitialDelay(0);
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        JPanel controlPanel = new JPanel();
        JToolBar toolbar = new JToolBar();
        controlPanel.add(toolbar);
        toolbar.setFloatable(false);

        JCheckBox autoBroadcastButton = new JCheckBox("auto");
        autoBroadcastButton.setSelected(true);

        autoBroadcastButton.setToolTipText(
                "<html>Send matching names to target goose<br>automatically.</html>");

        autoBroadcastButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                autoBroadcast = !autoBroadcast;
            }
        });


        mainPanel.add(controlPanel, BorderLayout.NORTH);

        JButton getGeeseNamesButton = new JButton("Update");
        toolbar.add(getGeeseNamesButton);
        getGeeseNamesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    MiscUtil.updateGooseChooserOLD(boss, gooseChooser, myGaggleName, null);
                } //try
                catch (Exception ex0) {
                    ex0.printStackTrace();
                }
            }
        });

        gooseChooser = new JComboBox(new String[]{"Boss"});
        gooseChooser.setPrototypeDisplayValue("a very very long goose name");
        gooseChooser.setToolTipText("Specify goose for broadcast");
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

        JButton showGooseButton = new JButton("S");
        JButton hideGooseButton = new JButton("H");

        // broadcast small & simple versions of each data type
        JButton broadcastListButton = new JButton("B");
        JButton broadcastMatrixButton = new JButton("M");
        JButton broadcastNetworkButton = new JButton("N");
        JButton broadcastHashButton = new JButton("A");
        JButton broadcastClusterButton = new JButton("C");

        showGooseButton.setToolTipText("Show selected goose");
        hideGooseButton.setToolTipText("Hide selected goose");
        broadcastListButton.setToolTipText("Broadcast regexFilter name list");
        broadcastMatrixButton.setToolTipText("Broadcast regexFilter matrix");
        broadcastNetworkButton.setToolTipText("Broadcast regexFilter network");
        broadcastHashButton.setToolTipText("Broadcast regexFilter name/value pairs (associative array)");
        broadcastClusterButton.setToolTipText("Broadcast cluster: selected row and column names");

        showGooseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    boss.show(targetGoose);
                }
                catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        });

        hideGooseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    boss.hide(targetGoose);
                }
                catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        });

        broadcastNetworkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    doBroadcastNetwork();
                }
                catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        });

        broadcastListButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    doBroadcastList();
                }
                catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        });

        /*******************************
         broadcastMatrixButton.addActionListener (new ActionListener () {
         public void actionPerformed (ActionEvent e) {broadcastRegexFilterMatrix ();}});


         broadcastHashButton.addActionListener (new ActionListener () {
         public void actionPerformed (ActionEvent e) {
         try {
         broadcastRegexFilterHashMap ();
         }
         catch (Exception ex2) {ex2.printStackTrace ();}
         }});

         broadcastClusterButton.addActionListener (new ActionListener () {
         public void actionPerformed (ActionEvent e) {
         try {
         broadcastRegexFilterCluster ();
         }
         catch (Exception ex2) {ex2.printStackTrace ();}
         }});
         *******************************/

        toolbar.add(showGooseButton);
        toolbar.add(hideGooseButton);
        toolbar.add(broadcastListButton);
        //toolbar.add (broadcastMatrixButton);
        toolbar.add(broadcastNetworkButton);
        toolbar.add(autoBroadcastButton);
        //toolbar.add (broadcastHashButton);
        //toolbar.add (broadcastClusterButton);

        JPanel searchPanel = new JPanel();
        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        mainPanel.setBorder(createBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");
            }
        });


        JButton exitButton = new JButton("Quit");
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doExit();
            }
        });

        buttonPanel.add(clearButton);
        buttonPanel.add(exitButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        MiscUtil.updateGooseChooserOLD(boss, gooseChooser, myGaggleName, null);

        return mainPanel;

    } // createGui

    //-----------------------------------------------------------------------------------
    protected void parseCommandLineArguments(String[] args) {
        int max = args.length;
        myGaggleName = "none found";
        regexPattern = "none found";

        for (int i = 0; i < max; i++) {
            String arg = args[i].trim();
            if (args[i].equals("--gooseName") && i + 1 < max) {
                myGaggleName = args[i + 1];
            } else if (args[i].equals("--pattern") && i + 1 < max) {
                regexPattern = args[i + 1];
            }
        } // for i

        System.out.println("leaving regex goose parse, regex: " + regexPattern +
                " goose name: " + myGaggleName);

    } // parseCommandLineArguments

    //-------------------------------------------------------------------------------------
    private Border createBorder() {
        int right = 10;
        int left = 10;
        int top = 10;
        int bottom = 10;
        return new EmptyBorder(top, left, bottom, right);
    }

    //-------------------------------------------------------------------------------
    public void connectToGaggle() {
        try {
            connector.connectToGaggle();
        }
        catch (Exception ex0) {
            System.err.println("failed to connect to gaggle: " + ex0.getMessage());
            ex0.printStackTrace();
        }
        boss = connector.getBoss();
    }

    //----------------------------------------------------------------------------------------
    public void handleNameList(String source, Namelist nameList) {
        currentSpecies = source;
        StringBuffer sb = new StringBuffer();
        sb.append(" >>> handleNameList, length " + nameList.getNames().length + "  (" + source + ")\n");
        ArrayList names = new ArrayList();

        for (int i = 0; i < nameList.getNames().length; i++) {
            String name = nameList.getNames()[i];
            System.out.print("    candidate: '" + name + "'");
            System.out.print("  regex: '" + regexPattern + "'");
            boolean match = name.matches(regexPattern);
            System.out.println("  match? " + match);
            if (match) {
                sb.append(name);
                sb.append("\n");
                names.add(name);
            }
        } // for

        filteredNames = (String[]) names.toArray(new String[0]);

        textArea.append(sb.toString());
        textArea.setCaretPosition(textArea.getText().length());
        if (autoBroadcast)
            doBroadcastList();
    }

    //----------------------------------------------------------------------------------------
    public void handleMatrix(String source, DataMatrix matrix) {
        StringBuffer sb = new StringBuffer();
        sb.append(" >>> handleMatrix: " + matrix.getRowCount() + " x " + matrix.getColumnCount() + "\n");

        sb.append("\n\n");
        textArea.append(sb.toString());
        textArea.setCaretPosition(textArea.getText().length());

    }

    //----------------------------------------------------------------------------------------
    public void handleTuple(String source, GaggleTuple gaggleTuple) {
         // todo tuple fix
        /*
        StringBuffer sb = new StringBuffer();
        sb.append(" >>> handleMap: " + attributeMap.getName() + "\n");
        String[] keys = AttributeMapUtil.getAttributeNames(attributeMap);
        for (int i = 0; i < keys.length; i++) {
            Map<String, Object> map = AttributeMapUtil.getKeyValuePairsForAttribute(attributeMap, keys[i]);

            sb.append(i + ") " + keys[i] + " (count: " + map.keySet().size() + ", " + map.values().size() + ")\n");
            int max = 4;
            if (map.values().size() < 4) max = map.values().size();

            int count = 0;
            for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
                if (count > max) {
                    break;
                }
                String key = it.next();
                Double value = (Double) map.get(key);
                sb.append("    " + key + ": " + value + "\n");
                count++;
            }
            sb.append("    ..." + "\n");

        }

        sb.append("\n\n");
        textArea.append(sb.toString());
        textArea.setCaretPosition(textArea.getText().length());
          */
    } 


    //----------------------------------------------------------------------------------------
    public void handleCluster(
            String source, Cluster cluster) {
        StringBuffer sb = new StringBuffer();
        sb.append(" >>> handleCluster: " + cluster.getName() + "\n");
        sb.append("  Rows: " + cluster.getRowNames().length + "\n");
        sb.append("  Cols: " + cluster.getColumnNames().length + "\n");

        sb.append("\n\n");
        textArea.append(sb.toString());
        textArea.setCaretPosition(textArea.getText().length());

    }

    //----------------------------------------------------------------------------------------
    public void handleNetwork(String source, Network network) {
        StringBuffer sb = new StringBuffer();
        sb.append(" >>> handleNetwork: " + "\n");
        sb.append("  nodes: " + network.nodeCount() + "\n");
        sb.append("  edges: " + network.edgeCount() + "\n");

        sb.append("\n\n");
        textArea.append(sb.toString());
        textArea.setCaretPosition(textArea.getText().length());
    }

    //----------------------------------------------------------------------------------------
    protected void broadcastRegexFilterCluster() {
        String[] rowNames = {"YFL036W", "YLR212C", "YML085C", "YML123C"};
        String[] columnNames = {"T000", "T120", "T240"};

        String species = "Saccharomyces cerevisiae";
        String clusterName = "RegexFilter";
        Cluster cluster = new Cluster(clusterName, species, rowNames, columnNames);
        try {
            boss.broadcastCluster(myGaggleName, targetGoose, cluster);
        }
        catch (RemoteException rex) {
            System.err.println("RegexFilterGoose: " + "rmi error calling boss.broadcast (cluster)");
            rex.printStackTrace();
        }

    } // broadcastRegexFilterCluster

    //----------------------------------------------------------------------------------------
    protected void broadcastRegexFilterMatrix() {
        org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix = new DataMatrix();

        matrix.setFullName("Demo Yeast created on the fly, meaningless data");
        matrix.setShortName("Demo Yeast");

        String[] columnTitles = {"T000", "T060", "T120", "T240"};
        String[] rowTitles = {"YFL036W", "YFL037W", "YLR212C", "YLR213C",
                "YML085C", "YML086C", "YML123C", "YML124C"};
        int dataRows = rowTitles.length;
        int dataColumns = columnTitles.length;
        matrix.setSize(dataRows, dataColumns);

        matrix.setSpecies("Saccharomyces cerevisiae");
        matrix.setRowTitlesTitle("GENE");
        matrix.setColumnTitles(columnTitles);
        matrix.setRowTitles(rowTitles);

        for (int r = 0; r < dataRows; r++)
            for (int c = 0; c < dataColumns; c++)
                matrix.set(r, c, (r * 0.38) + c * 0.09);

        try {
            boss.broadcastMatrix(myGaggleName, targetGoose, matrix);
        }
        catch (RemoteException rex) {
            System.err.println("RegexFilterGoose: " + "rmi error calling boss.broadcast (matrix)");
            rex.printStackTrace();
        }

    } // broadcastRegexFilterMatrix

    //----------------------------------------------------------------------------------------
    protected void doBroadcastNetwork() {
        org.systemsbiology.gaggle.core.datatypes.Network network = new org.systemsbiology.gaggle.core.datatypes.Network();
        for (int i = 0; i < filteredNames.length; i++)
            network.add(filteredNames[i]);

        try {
            boss.broadcastNetwork(myGaggleName, targetGoose, network);
        }
        catch (RemoteException rex) {
            System.err.println("RegexFilterGoose: " + "rmi error calling boss.broadcast (network)");
            rex.printStackTrace();
        }

    } // broadcastRegexFilteredNetwork

    //----------------------------------------------------------------------------------------
    protected void broadcastRegexFilterHashMap() {
        /*
        HashMap map = new HashMap();
        String species = "Saccharomyces cerevisiae";

        double[] values = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8};
        String[] rowNames = {"YFL036W", "YFL037W", "YLR212C", "YLR213C",
                "YML085C", "YML086C", "YML123C", "YML124C"};
        String dataTitle = "demo data";
        String attributeName = "log 10 ratio, simulated";

        AttributeMap attributeMap = AttributeMapUtil.createOneAttributeMap(dataTitle, attributeName, rowNames,
                AttributeMapUtil.convertDoubleArrayToObjectArray(values));
        attributeMap.setSpecies(species);


        try {
            boss.broadcastTuple(myGaggleName, targetGoose, null);  // todo tuple fix
        }
        catch (RemoteException rex) {
            System.err.println("RegexFilterGoose: " + "rmi error calling boss.broadcast (hashMap)");
            rex.printStackTrace();
        }
        */
    }

    //----------------------------------------------------------------------------------------
    public void clearSelections() {
        System.out.println("clearSelections");
    }

    //----------------------------------------------------------------------------------------
    public int getSelectionCount() {
        return 0;
    }

    //----------------------------------------------------------------------------------------
    public String getName() {
        return myGaggleName;
    }

    //----------------------------------------------------------------------------------------
    public void setName(String newName) {
        myGaggleName = newName;
        setTitle(myGaggleName);
    }

    //----------------------------------------------------------------------------------------
    public void setGeometry(int x, int y, int width, int height) {
        System.out.println("setGeometry");
    }

    //----------------------------------------------------------------------------------------
    public void doBroadcastList() {
        if (filteredNames == null || filteredNames.length == 0)
            return;

        Namelist nameList = new Namelist();
        nameList.setSpecies(currentSpecies);
        nameList.setNames(filteredNames);

        try {
            boss.broadcastNamelist(myGaggleName, targetGoose, nameList);
        }
        catch (Exception ex0) {
            ex0.printStackTrace();
        }


    }

    //----------------------------------------------------------------------------------------
    public void doHide() {
        setVisible(false);
    }

    //----------------------------------------------------------------------------------------
    public void doShow() {
        setAlwaysOnTop(true);
        setVisible(true);
        setAlwaysOnTop(false);

    }

    //----------------------------------------------------------------------------------------
    public void doExit() {
        connector.disconnectFromGaggle(true);
        System.exit(0);
    }

    public void update(String[] gooseNames) {
    }

    //----------------------------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        RegexFilterGoose goose = new RegexFilterGoose(args);

    } // main
//-------------------------------------------------------------------------------------
} // RegexFilterGoose
