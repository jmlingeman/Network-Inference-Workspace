// DiagGoose.java
//-------------------------------------------------------------------------------------
// $Revision: 2360 $   
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

package org.systemsbiology.gaggle.geese.diag;
//---------------------------------------------------------------------------------
// todo - figure out if this still needs to exist; seems to basically be the same as SampleGoose
import java.rmi.*;

import org.systemsbiology.gaggle.core.datatypes.DataMatrix;
import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.datatypes.*;
import org.systemsbiology.gaggle.core.datatypes.Interaction;
import org.systemsbiology.gaggle.util.MiscUtil;
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

//-------------------------------------------------------------------------------------
public class DiagGoose extends JFrame implements Goose {

    String myGaggleName = "Debug";
    Boss boss;
    RmiGaggleConnector connector = new RmiGaggleConnector(this);
    protected JScrollPane scrollPane;
    protected JTextArea textArea;
    JComboBox gooseChooser;
    String targetGoose = "Boss";

    //-------------------------------------------------------------------------------------
    public DiagGoose() {
        super("Debug");

        try {
            connectToGaggle();
        }
        catch (Exception ex0) {
            System.err.println("DiagGoose failed to export remote object: " + ex0.getMessage());
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
        JButton broadcastHashButton = new JButton("H");

        showGooseButton.setToolTipText("Show selected goose");
        hideGooseButton.setToolTipText("Hide selected goose");
        broadcastListButton.setToolTipText("Broadcast sample name list");
        broadcastMatrixButton.setToolTipText("Broadcast sample matrix");
        broadcastNetworkButton.setToolTipText("Broadcast sample network");
        broadcastHashButton.setToolTipText("Broadcast sample name/value pairs (HashMap)");

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

        broadcastListButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    broadcastSampleList();
                }
                catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        });

        broadcastMatrixButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                broadcastSampleMatrix();
            }
        });

        broadcastNetworkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    broadcastSampleNetwork();
                }
                catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        });

        broadcastHashButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    broadcastSampleHashMap();
                }
                catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        });

        toolbar.add(showGooseButton);
        toolbar.add(hideGooseButton);
        toolbar.add(broadcastListButton);
        toolbar.add(broadcastMatrixButton);
        toolbar.add(broadcastNetworkButton);
        toolbar.add(broadcastHashButton);

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
        StringBuffer sb = new StringBuffer();
        sb.append(" >>> handleNameList, length " + nameList.getNames().length + ", source =  " + source + "\n");
        for (int i = 0; i < nameList.getNames().length; i++)
            sb.append("   " + nameList.getNames()[i] + "\n");

        sb.append("\n\n");
        textArea.append(sb.toString());
        textArea.setCaretPosition(textArea.getText().length());
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
    public void handleCluster(String source, Cluster cluster) {
        StringBuffer sb = new StringBuffer();
        sb.append(" >>> handleCluster: " + cluster.getRowNames() + "\n");
        sb.append("  Source: " + source + "\n");
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
    protected void broadcastSampleList() {
        String[] nameList = {"YFL036W", "YFL037W", "YLR212C", "YLR213C",
                "YML085C", "YML086C", "YML123C", "YML124C"};

        String species = "Saccharomyces cerevisiae";
        Namelist gaggleNameList = new Namelist();
        gaggleNameList.setSpecies(species);
        gaggleNameList.setNames(nameList);

        try {
            boss.broadcastNamelist(myGaggleName, targetGoose, gaggleNameList);
        }
        catch (RemoteException rex) {
            System.err.println("DiagGoose: " + "rmi error calling boss.broadcast (nameList)");
            rex.printStackTrace();
        }

    } // broadcastSampleList

    //----------------------------------------------------------------------------------------
    protected void broadcastSampleMatrix() {
        DataMatrix matrix = new org.systemsbiology.gaggle.core.datatypes.DataMatrix();

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
            System.err.println("DiagGoose: " + "rmi error calling boss.broadcast (matrix)");
            rex.printStackTrace();
        }

    } // broadcastSampleMatrix

    //----------------------------------------------------------------------------------------
    protected void broadcastSampleNetwork() {
        Interaction i0 = new Interaction("YFL036W", "YFL037W", "GeneCluster");
        Interaction i1 = new Interaction("YFL037W", "YLR212C", "GeneFusion");
        Interaction i2 = new Interaction("YFL037W", "YML085C", "GeneFusion");
        Interaction i3 = new Interaction("YFL037W", "YML124C", "GeneFusion");
        Interaction i4 = new Interaction("YLR212C", "YLR213C", "GeneCluster");
        Interaction i5 = new Interaction("YLR212C", "YML085C", "GeneFusion");
        Interaction i6 = new Interaction("YLR212C", "YML124C", "GeneFusion");
        Interaction i7 = new Interaction("YML123C", "YML124C", "GeneCluster");
        Interaction i8 = new Interaction("YML085C", "YML086C", "GeneCluster");
        Interaction i9 = new Interaction("YML085C", "YML124C", "GeneFusion");

        org.systemsbiology.gaggle.core.datatypes.Network network = new org.systemsbiology.gaggle.core.datatypes.Network();

        network.add(i0);
        network.add(i1);
        network.add(i2);
        network.add(i3);
        network.add(i4);
        network.add(i5);
        network.add(i6);
        network.add(i7);
        network.add(i8);
        network.add(i9);

        String species = "Saccharomyces cerevisiae";
        String[] nodeNames = {"YFL036W", "YFL037W", "YLR212C", "YLR213C",
                "YML085C", "YML086C", "YML123C", "YML124C"};
        for (int i = 0; i < nodeNames.length; i++) {
            network.addNodeAttribute(nodeNames[i], "moleculeType", "DNA");
            network.addNodeAttribute(nodeNames[i], "species", species);
        }

        network.addEdgeAttribute("YFL036W (GeneCluster) YFL037W", "score", new Double(0.5));
        network.addEdgeAttribute("YFL037W (GeneFusion) YLR212C", "score", new Double(0.4));
        network.addEdgeAttribute("YFL037W (GeneFusion) YML085C", "score", new Double(0.3));
        network.addEdgeAttribute("YFL037W (GeneFusion) YML124C", "score", new Double(0.2));
        network.addEdgeAttribute("YLR212C (GeneCluster) YLR213C", "score", new Double(0.1));
        network.addEdgeAttribute("YLR212C (GeneFusion) YML085C", "score", new Double(0.8));
        network.addEdgeAttribute("YLR212C (GeneFusion) YML124C", "score", new Double(0.75));
        network.addEdgeAttribute("YML123C (GeneCluster) YML124C", "score", new Double(0.55));
        network.addEdgeAttribute("YML085C (GeneCluster) YML086C", "score", new Double(0.45));
        network.addEdgeAttribute("YML085C (GeneFusion) YML124C", "score", new Double(0.35));

        try {
            boss.broadcastNetwork(myGaggleName, targetGoose, network);
        }
        catch (RemoteException rex) {
            System.err.println("DiagGoose: " + "rmi error calling boss.broadcast (network)");
            rex.printStackTrace();
        }

    } // broadcastSimpleNetwork

    //----------------------------------------------------------------------------------------
    protected void broadcastSampleHashMap() {
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
            System.err.println("DiagGoose: " + "rmi error calling boss.broadcast (hashMap)");
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

    }

    //----------------------------------------------------------------------------------------
    public void doHide() {
        setVisible(false);
    }

    //----------------------------------------------------------------------------------------
    public void doShow() {
        setVisible(true);
    }

    //----------------------------------------------------------------------------------------
    public void doExit() {
        connector.disconnectFromGaggle(true);
        System.exit(0);
    }

    //----------------------------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        DiagGoose goose = new DiagGoose();

    } // main

    public void update(String[] gooseNames) {
    }
//-------------------------------------------------------------------------------------
} // DiagGoose
