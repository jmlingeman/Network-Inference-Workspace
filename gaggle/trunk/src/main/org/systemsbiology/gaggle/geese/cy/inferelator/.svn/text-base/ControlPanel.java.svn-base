// ControlPanel.java
//---------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.cy.inferelator;
//---------------------------------------------------------------------------------------

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;


import java.rmi.*;

import java.util.*;
import java.text.*;

import org.systemsbiology.gaggle.util.MiscUtil;
import org.systemsbiology.gaggle.geese.cy.util.NodeNameMatcher;
import org.systemsbiology.gaggle.geese.cy.util.NetworkUtil;
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;
import org.systemsbiology.gaggle.geese.common.GooseShutdownHook;
import org.systemsbiology.gaggle.geese.common.GaggleConnectionListener;
import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.datatypes.*;

import cytoscape.*;
import cytoscape.undo.UndoableGraphHider;
import cytoscape.data.servers.*;

import y.base.*;
import y.view.*;

//---------------------------------------------------------------------------------------
public class ControlPanel extends JPanel implements Goose, java.io.Serializable,
        GaggleConnectionListener {

    protected JToolBar toolbar;
    protected GridBagLayout gridbagLayout;
    protected JTextField nodeSelectionTextField;
    protected JTextField nodeInClusterSelectionTextField;

    String targetGoose = "Boss";

    JComboBox gooseChooser;

    JButton connectButton;
    JButton disconnectButton;

    String[] activeGooseNames = new String[0];
    protected CytoscapeWindow cw;
    protected BioDataServer dataServer;
    protected UndoableGraphHider graphHider;
    String myGaggleName = "Inferelator";
    RmiGaggleConnector connector = new RmiGaggleConnector(this);
    Boss boss;

    JTextField residualsReadout, motifPValsReadout, regulatorWeightsReadout;
    double regulatorWeightsThreshold, motifPValsThreshold, residualsThreshold;
    DecimalFormat decimalFormatter;

    // HashMap conditionNameFixer;
    String species = "unknown";

    static final int HIDE_ABOVE = 1;
    static final int HIDE_BELOW = 2;

    //---------------------------------------------------------------------------------------
    public ControlPanel(CytoscapeWindow cw) {
        new GooseShutdownHook(connector);

        ToolTipManager.sharedInstance().setInitialDelay(0);
        this.cw = cw;
        species = cw.getDefaultSpecies();
        dataServer = cw.getBioDataServer();
        graphHider = cw.getGraphHider();
        myGaggleName = discoverGooseName(cw.getConfiguration().getArgs());
        decimalFormatter = new DecimalFormat("0.000");

        try {
            connectToGaggle();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        add(createGui());
        MiscUtil.updateGooseChooser(gooseChooser, myGaggleName, activeGooseNames);
        connector.addListener(this);

    } // ctor

    //---------------------------------------------------------------------------------------
    protected String discoverGooseName(String[] commandLineArgs) {
        String result = "cytoscape";  // the default
        int max = commandLineArgs.length;

        for (int i = 0; i < max; i++) {
            if (commandLineArgs[i].equals("--gooseName") && i + 1 < max) {
                result = commandLineArgs[i + 1];
                break;
            } // if
        } // for i

        return result;

    } // discoverGooseName

    //------------------------------------------------------------------------------
    public void connectToGaggle() {
        try {
            connector.connectToGaggle();
        }
        catch (Exception ex0) {
            JOptionPane.showMessageDialog(this, "Inferelator Control Panel could not connect to gaggle");
            System.err.println(ex0.getMessage());
            ex0.printStackTrace();
        }
        boss = connector.getBoss();
    } // connectToGaggle

    //-------------------------------------------------------------------------------------
    JPanel oldCreateGui() {
        JPanel outerPanel = new JPanel();
        gridbagLayout = new GridBagLayout();
        outerPanel.setLayout(gridbagLayout);

        //------------------------------------
        // toolbar
        //------------------------------------

        toolbar = createToolBar();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.ipadx = 0;
        gbc.ipady = 0;
        gridbagLayout.setConstraints(toolbar, gbc);
        outerPanel.add(toolbar);

        //------------------------------------
        // select by name button
        //------------------------------------

        JButton selectNodesButton = new JButton("Select: ");
        selectNodesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectNodes();
            }
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 10, 0, 10);
        gbc.ipadx = 0;
        gbc.ipady = 0;
        gridbagLayout.setConstraints(selectNodesButton, gbc);
        outerPanel.add(selectNodesButton);

        //------------------------------------
        // node selection text field
        //------------------------------------

        nodeSelectionTextField = new JTextField(8);
        nodeSelectionTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyText(e.getKeyCode()).equals("Enter")) selectNodes();
            }
        });

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.ipadx = 0;
        gbc.ipady = 0;
        gridbagLayout.setConstraints(nodeSelectionTextField, gbc);
        outerPanel.add(nodeSelectionTextField);

        JTabbedPane tabbedPane = new JTabbedPane();

        if (cw.getEdgeAttributes().hasAttribute("weight"))
            tabbedPane.add("regulator weight", createHideByRegulatorWeightPanel());

        if (cw.getNodeAttributes().hasAttribute("motifPValue"))
            tabbedPane.add("cluster p value", createHideByClusterPValuePanel());

        if (cw.getNodeAttributes().hasAttribute("residual"))
            tabbedPane.add("cluster residual", createHideByClusterResidualPanel());

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.gridheight = 2;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.ipadx = 0;
        gbc.ipady = 0;
        gridbagLayout.setConstraints(tabbedPane, gbc);

        if (tabbedPane.getTabCount() > 0)
            outerPanel.add(tabbedPane);

        return outerPanel;

    } // oldCreateGui

    //-------------------------------------------------------------------------------------
    JPanel createGui() {
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BorderLayout());
        JPanel leftPanel = new JPanel();
        outerPanel.add(leftPanel, BorderLayout.WEST);

        leftPanel.setLayout(new GridLayout(3, 1));

        JToolBar gaggleToolbar = new JToolBar();
        gaggleToolbar.setFloatable(false);


        gooseChooser = new JComboBox(new String[]{"Boss"});
        gooseChooser.setPrototypeDisplayValue("a very very long goose name");
        gooseChooser.setToolTipText("Specify goose for broadcast");

        gaggleToolbar.add(gooseChooser);

        gooseChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                int gooseChooserIndex = cb.getSelectedIndex();
                System.out.println("choose goose index: " + gooseChooserIndex);
                targetGoose = (String) cb.getSelectedItem();
            }
        });


        JButton showGooseButton = new JButton("S");
        JButton hideGooseButton = new JButton("H");
        JButton broadcastListButton = new JButton("B");
        JButton broadcastSelectedClustersButton = new JButton("Cl");
        broadcastSelectedClustersButton.setToolTipText(
                "<html>Broadcast genes and conditions for ALL <br>selected clusters, one at a time.</html>");
        broadcastSelectedClustersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                broadcastSelectedClusters();
            }
        });

        JButton broadcastNetworkButton = new JButton("N");

        connectButton = new JButton("C");
        disconnectButton = new JButton("D");

        connectButton.setToolTipText("Connect to Boss");
        connectButton.setEnabled(!connector.isConnected());
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    connector.connectToGaggle();
                    connectButton.setEnabled(false);
                    disconnectButton.setEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(cw.getMainFrame(),
                            "Error connecting to boss - is the boss running?");
                }
            }
        });

        disconnectButton.setToolTipText("Disconnect from Boss");
        disconnectButton.setEnabled(connector.isConnected());
        disconnectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                connector.disconnectFromGaggle(false);
                connectButton.setEnabled(true);
                disconnectButton.setEnabled(false);
            }
        });


        showGooseButton.setToolTipText("Show selected goose");
        hideGooseButton.setToolTipText("Hide selected goose");
        broadcastListButton.setToolTipText("Broadcast selected nodes to selected goose");
        broadcastNetworkButton.setToolTipText("Broadcast network to selected goose");

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
                doBroadcastList();
            }
        });

        broadcastNetworkButton.addActionListener (new ActionListener () {
          public void actionPerformed (ActionEvent e) {
            doBroadcastNetwork ();
            }});

        gaggleToolbar.add(showGooseButton);
        gaggleToolbar.add(hideGooseButton);
        gaggleToolbar.add(broadcastListButton);
        gaggleToolbar.add(broadcastSelectedClustersButton);
        gaggleToolbar.add (broadcastNetworkButton);

        gaggleToolbar.add(connectButton);
        gaggleToolbar.add(disconnectButton);

        JPanel cyControlsPanel = new JPanel();


        nodeSelectionTextField = new JTextField(8);
        String msg = "<html>Enter node names here, using common <br>" +
                "or cannonical names, upper or lower case <br>" +
                " &nbsp; '*' &nbsp; at end of name matches all.</html>";
        nodeSelectionTextField.setToolTipText(msg);

        nodeSelectionTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyText(e.getKeyCode()).equals("Enter") || (e.getKeyCode() == 10)) selectNodes();
            }
        });


        JToolBar cyToolbar = new JToolBar();
        cyToolbar.setFloatable(false);
        JButton showAllButton = new JButton("U");
        showAllButton.setToolTipText("Unhide all nodes & edges");
        JButton hideOrphansButton = new JButton("O");
        hideOrphansButton.setToolTipText("Hide Orphan Nodes");

        JButton clearSelectionsButton = new JButton("C");
        clearSelectionsButton.setToolTipText("Clear Network Selections");

        showAllButton.addActionListener(new ShowAllAction());
        hideOrphansButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hideOrphans();
            }
        });

        clearSelectionsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearNetworkSelections();
            }
        });


        JButton findAllRegulatorsButton = new JButton("R");
        findAllRegulatorsButton.setToolTipText("Select all regulators of selected clusters.");
        findAllRegulatorsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectAllRegulatorsOfSelectedClusters();
            }
        });

        JButton findAllRegulatedClustersButton = new JButton("r");
        findAllRegulatedClustersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectAllRegulatedClusters();
            }
        });
        findAllRegulatedClustersButton.setToolTipText("Select all regulated clusters.");

        cyToolbar.add(showAllButton);
        cyToolbar.add(hideOrphansButton);
        cyToolbar.add(clearSelectionsButton);
        cyToolbar.add(findAllRegulatorsButton);
        cyToolbar.add(findAllRegulatedClustersButton);

        cyControlsPanel.add(cyToolbar);
        cyControlsPanel.add(nodeSelectionTextField);

        leftPanel.add(gaggleToolbar);
        leftPanel.add(cyControlsPanel);
        //leftPanel.add (movieReadoutPanel);

        JTabbedPane tabbedPane = new JTabbedPane();

        if (cw.getEdgeAttributes().hasAttribute("weight"))
            tabbedPane.add("regulator weight", createHideByRegulatorWeightPanel());

        if (cw.getNodeAttributes().hasAttribute("motifPValue"))
            tabbedPane.add("cluster p value", createHideByClusterPValuePanel());

        if (cw.getNodeAttributes().hasAttribute("residual"))
            tabbedPane.add("cluster residual", createHideByClusterResidualPanel());

        System.out.println("tab count: " + tabbedPane.getTabCount());
        if (tabbedPane.getTabCount() > 0)
            outerPanel.add(tabbedPane, BorderLayout.EAST);

        return outerPanel;

    } // createGui

    //-----------------------------------------------------------------------------------
    protected JToolBar createToolBar() {
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        JButton showAllButton = new JButton("S");
        showAllButton.setToolTipText("Show all nodes & edges");
        JButton hideOrphansButton = new JButton("H");
        hideOrphansButton.setToolTipText("Hide Orphan Nodes");

        JButton findAllRegulatorsButton = new JButton("R");
        findAllRegulatorsButton.setToolTipText("Select all regulators of selected clusters.");
        findAllRegulatorsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectAllRegulatorsOfSelectedClusters();
            }
        });

        JButton findAllRegulatedClustersButton = new JButton("r");
        findAllRegulatedClustersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectAllRegulatedClusters();
            }
        });
        findAllRegulatedClustersButton.setToolTipText("Select all regulated clusters.");


        JButton broadcastSelectedClustersButton = new JButton("B");
        broadcastSelectedClustersButton.setToolTipText(
                "<html>Broadcast genes and conditions for ALL <br>selected clusters, one at a time.</html>");
        broadcastSelectedClustersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                broadcastSelectedClusters();
            }
        });

        JButton broadcastNodeNamesButton = new JButton("b");
        broadcastNodeNamesButton.setToolTipText("Broadcast node names.");

        JButton clearSelectionsButton = new JButton("C");
        clearSelectionsButton.setToolTipText("Clear Network Selections");

        showAllButton.addActionListener(new ShowAllAction());
        hideOrphansButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hideOrphans();
            }
        });


        broadcastNodeNamesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doBroadcastList();
            }
        });

        clearSelectionsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearNetworkSelections();
            }
        });

        toolbar.add(showAllButton);
        toolbar.add(hideOrphansButton);
        toolbar.add(broadcastSelectedClustersButton);
        toolbar.add(broadcastNodeNamesButton);
        toolbar.add(clearSelectionsButton);
        toolbar.add(findAllRegulatorsButton);
        toolbar.add(findAllRegulatedClustersButton);

        JButton gaggleButton = new JButton("Boss");
        gaggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    boss.show("boss");
                }
                catch (RemoteException rex) {
                    rex.printStackTrace();
                }
            }
        });
        toolbar.add(gaggleButton);

        return toolbar;

    } // createToolBar

    //-----------------------------------------------------------------------------------
    protected HashMap getCommonNamesInHash(String[] canonicalNames) {
        HashMap result = new HashMap();
        for (int i = 0; i < canonicalNames.length; i++) {
            String canonicalName = canonicalNames[i];
            String commonName = dataServer.getCommonName(species, canonicalName);
            if (commonName == null)
                continue;
            if (!result.containsKey(commonName))
                result.put(commonName, canonicalName);
        } // for i

        return result;

    } // getCommonNamesInHash

    //-----------------------------------------------------------------------------------
    protected void selectNodes() {
        String rawTextFieldContents = nodeSelectionTextField.getText().trim();
        String[] candidates = rawTextFieldContents.split(" ");
        selectNodesByName(candidates);
    }

    //-----------------------------------------------------------------------------------
    protected void selectNodesByName(String[] candidates) {
        System.out.println("inf.selectNodesByName, count: " + candidates.length);
        for (int i = 0; i < candidates.length; i++)
            System.out.println("  " + i + ") " + candidates[i]);

        HashMap clusteredGenesMap = getGeneClusters();

        String[] nodeNames = getCanonicalNamesOfNodesInGraph();
        NodeNameMatcher nameMatcher =
                new NodeNameMatcher(nodeNames, clusteredGenesMap, cw.getBioDataServer(),
                        cw.getDefaultSpecies());

        String[] matchedNodes = nameMatcher.getMatch(candidates);

        System.out.println("------------ match count: " + matchedNodes.length);
        for (int i = 0; i < matchedNodes.length; i++)
            System.out.println("    " + matchedNodes[i]);

        cw.selectNodesByName(matchedNodes, false);

    } // selectNodesByName

    //-----------------------------------------------------------------------------------
    protected String[] getCanonicalNamesOfNodesInGraph() {
        ArrayList tmp = new ArrayList();
        Node[] nodes = cw.getGraph().getNodeArray();
        for (int i = 0; i < nodes.length; i++) {
            String canonicalName = cw.getCanonicalNodeName(nodes[i]);
            if (canonicalName == null || canonicalName.equals("null"))
                continue;
            tmp.add(canonicalName);
        }

        return (String[]) tmp.toArray(new String[0]);
    }

    //-----------------------------------------------------------------------------------
    protected HashMap getGeneClusters() {
        HashMap result = new HashMap();
        String[] canonicalNames = getCanonicalNamesOfNodesInGraph();

        for (int i = 0; i < canonicalNames.length; i++) {
            String canonicalName = canonicalNames[i];
            String nodeType = cw.getNodeAttributes().getStringValue("type", canonicalName);
            if (nodeType != null && nodeType.equals("cluster")) {
                String[] clusteredGenes =
                        cw.getNodeAttributes().getStringArrayValues("clusterGenes", canonicalName);
                result.put(canonicalName, clusteredGenes);
            } // if cluster
        } // for c

        return result;

    } // getGeneClusters

    //-----------------------------------------------------------------------------------
    String[] getGenesInCluster(String clusterName) {
        return cw.getNodeAttributes().getStringArrayValues("clusterGenes", clusterName);
    }

    //-----------------------------------------------------------------------------------
    class ShowAllAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            graphHider.unhideAll();
            cw.redrawGraph();
        }
    }

    //-----------------------------------------------------------------------------------
/**
 * 'orphan' is defined broadly here to include, not only all unconnected nodes, but also
 * all nodes not connected, directly or indirectly, to a bicluster.  a simple trick
 * makes it easy to figure out where the second rule applies:
 * <ol>
 * <li> find all the logicGate nodes
 * <li> for each, count the number of regulatory edges (type = 'activates' or 'represses')
 * <li> when that count is zero, remove ALL edges on the logicGate node
 * </ol>
 * <p/>
 * after these edges are removed, the final step is simply to hide all nodes with no edges
 */
    protected void hideOrphans() {
        Node[] logicGateNodes = findLogicGateNodes();
        for (int i = 0; i < logicGateNodes.length; i++) {
            // if (countRegulatoryEdges (logicGateNodes [i]) == 0)
            if (logicGateNodes[i].degree() <= 2)
                graphHider.hide(logicGateNodes[i].edges());
        } // for i

        Node[] nodes = cw.getGraph().getNodeArray();
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].degree() == 0)
                graphHider.hide(nodes[i]);
        } // for i

        //Edge [] edges = cw.getGraph().getEdgeArray ();
        //GraphObjAttributes ea = cw.getEdgeAttributes ();
        //ArrayList edgesToHide = new ArrayList ();
        // for (int e=0; e < edges.length; e++)
        //   Edge edge = edges [e];

        cw.redrawGraph();

    } // hideOrphans

    //-----------------------------------------------------------------------------------
/**
 * return an array of all Nodes with 'type' attribute of 'logicGate'
 */
    private Node[] findLogicGateNodes() {
        GraphObjAttributes nodeAttributes = cw.getNodeAttributes();
        Node[] nodes = cw.getGraph().getNodeArray();
        ArrayList result = new ArrayList();
        for (int i = 0; i < nodes.length; i++) {
            String canonicalName = cw.getCanonicalNodeName(nodes[i]);
            String nodeType = nodeAttributes.getStringValue("type", canonicalName);
            if (nodeType != null && nodeType.equals("logicGate"))
                result.add(nodes[i]);
        }
        return (Node[]) result.toArray(new Node[0]);

    } // findLogicGateNodes

    //--------------------------------------------------------------------------------------------
    protected int countRegulatoryEdges(Node logicGateNode) {
        GraphObjAttributes ea = cw.getEdgeAttributes();
        EdgeCursor ec = logicGateNode.edges();
        int count = 0;
        while (ec.ok()) {
            String edgeName = ea.getCanonicalName(ec.edge());
            String edgeType = ea.getStringValue("interaction", edgeName);
            if (edgeType.equals("activates") || edgeType.equals("represses"))
                count += 1;
            ec.next();
        } // while

        return count;

    }  // countRegulatoryEdges

    //--------------------------------------------------------------------------------------------
    protected void broadcastSelectedClusters() {
        NodeCursor nc = cw.getGraph().selectedNodes();

        ArrayList clusterNodes = new ArrayList();
        while (nc.ok()) {
            String name = cw.getNodeAttributes().getCanonicalName(nc.node());
            String type = cw.getNodeAttributes().getStringValue("type", name);
            if (type != null && type.equals("cluster"))
                clusterNodes.add(name);
            nc.next();
        }

        int clusterCount = clusterNodes.size();
        if (clusterCount == 0) {
            JOptionPane.showMessageDialog(null, "Please choose at least one cluster to broadcast.",
                    "Inferelator Control Panel Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (clusterCount > 1) {
            String msg = "Do you really want to broadcast " + clusterCount + " clusters?";
            int result = JOptionPane.showConfirmDialog(cw.getMainFrame(), msg,
                    "Inferelator Control Panel Question",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION)
                return;
        }

        String[] nodeNames = (String[]) clusterNodes.toArray(new String[0]);
        for (int i = 0; i < nodeNames.length; i++)
            broadcastSingleClusterMatrix(nodeNames[i]);

    } // broadcastSelectedClusters

    //--------------------------------------------------------------------------------------------
    protected void broadcastSingleClusterMatrix(String clusterName) {
        String[] clusteredGenes =
                cw.getNodeAttributes().getStringArrayValues("clusterGenes", clusterName);
        for (int i = 0; i < clusteredGenes.length; i++)
            clusteredGenes[i] = clusteredGenes[i].toUpperCase();

        String[] conditionNames =
                cw.getNodeAttributes().getStringArrayValues("clusterConditions", clusterName);

        String[] fixedConditionNames = new String[conditionNames.length];
        for (int i = 0; i < conditionNames.length; i++) {
            String name = conditionNames[i];
            fixedConditionNames[i] = name;
        } // for i

        try {
            System.out.println("ICP broadcast cluster: " + clusteredGenes.length + " genes " +
                    fixedConditionNames.length + " clusters");
            boss.broadcastCluster(myGaggleName, targetGoose, new Cluster(clusterName, species, clusteredGenes, fixedConditionNames));
        }
        catch (RemoteException rex0) {
            System.err.println("ControlPanel.broadcastSingleClusterMatrix: " +
                    rex0.getMessage());
        }

    } // broadcastSingleClusterMatrix

    //-----------------------------------------------------------------------------------
    protected void clearNetworkSelections() {
        cw.selectNodes(new Node[]{}, true);
    }

    //-----------------------------------------------------------------------------------
    class BroadcastSelectedNodesOnly implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            NodeCursor nc = cw.getGraph().selectedNodes();
            ArrayList list = new ArrayList();
            while (nc.ok()) {
                String name = cw.getNodeAttributes().getCanonicalName(nc.node());
                list.add(name);
                nc.next();
            }
            String[] names = (String[]) list.toArray(new String[0]);
            Namelist nameList = new Namelist();
            nameList.setSpecies(species);
            nameList.setNames(names);
            try {
                boss.broadcastNamelist(myGaggleName, targetGoose, nameList);
            }
            catch (RemoteException rex0) {
                System.err.println("ControlPanel.broadcastSelectedNodesOnly: " +
                        rex0.getMessage());
            }
        } // actionPerformed

    } // inner class BroadcastAllAction

    //-----------------------------------------------------------------------------------
    class DismissAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            doExit();
        }
    }

    //-----------------------------------------------------------------------------------
    JPanel createHideByRegulatorWeightPanel() {
        double[] edgeWeights = getDoubleEdgeAttributes("weight");
        if (edgeWeights == null || edgeWeights.length == 0)
            return new JPanel();

        Arrays.sort(edgeWeights);
        double minValue = edgeWeights[0];
        double maxValue = edgeWeights[edgeWeights.length - 1];
        JPanel panel = new JPanel();
        JButton hideButton;
        JSlider slider;

        int sliderMin = (int) (1000 * minValue);
        int sliderMax = (int) (1000 * maxValue);
        int delta = (sliderMax - sliderMin) / 10;
        sliderMin -= delta;
        sliderMax += delta;
        slider = new JSlider(sliderMin, sliderMax);
        regulatorWeightsThreshold = (maxValue + minValue) / 2.0;
        StringBuffer sb = decimalFormatter.format(regulatorWeightsThreshold,
                new StringBuffer(), new FieldPosition(4));
        regulatorWeightsReadout = new JTextField(sb.toString(), 4);

        hideButton = new JButton("Hide Below");
        hideButton.setToolTipText("Hide edges with weight < slider value");
        hideButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hideEdgesByWeight(regulatorWeightsThreshold);
            }
        });

        panel.add(regulatorWeightsReadout);
        panel.add(slider);
        panel.add(hideButton);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                regulatorWeightsThreshold = (double) (slider.getValue() / 1000.0);
                StringBuffer sb = decimalFormatter.format(regulatorWeightsThreshold, new StringBuffer(),
                        new FieldPosition(4));
                regulatorWeightsReadout.setText(sb.toString());
            }
        });

        return panel;

    } // createHideByRegulatorWeightPanel

    //---------------------------------------------------------------------------------------
    JPanel createHideByClusterPValuePanel() {
        double[] pvals = getDoubleNodeAttributes("motifPValue");
        if (pvals == null || pvals.length == 0)
            return new JPanel();

        Arrays.sort(pvals);
        double minValue = pvals[0];
        double maxValue = pvals[pvals.length - 1];
        JPanel panel = new JPanel();
        JButton hideButton;
        JSlider slider;

        int sliderMin = (int) (1000 * minValue);
        int sliderMax = (int) (1000 * maxValue);
        int delta = (sliderMax - sliderMin) / 10;
        sliderMin -= delta;
        sliderMax += delta;

        slider = new JSlider(sliderMin, sliderMax);
        motifPValsThreshold = (maxValue + minValue) / 2.0;
        StringBuffer sb = decimalFormatter.format(motifPValsThreshold,
                new StringBuffer(), new FieldPosition(4));
        motifPValsReadout = new JTextField(sb.toString(), 4);
        hideButton = new JButton("Hide Above");
        hideButton.setToolTipText("Hide edges with weight < slider value");

        hideButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hideNodesByThreshold("motifPValue", motifPValsThreshold, HIDE_ABOVE);
            }
        });

        panel.add(motifPValsReadout);
        panel.add(slider);
        panel.add(hideButton);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                motifPValsThreshold = (double) (slider.getValue() / 1000.0);
                StringBuffer sb = decimalFormatter.format(motifPValsThreshold, new StringBuffer(),
                        new FieldPosition(4));
                motifPValsReadout.setText(sb.toString());
            }
        });

        return panel;

    } // createHideByClusterPValuePanel

    //---------------------------------------------------------------------------------------
    JPanel createHideByClusterResidualPanel() {
        double[] residuals = getDoubleNodeAttributes("residual");
        if (residuals == null || residuals.length == 0)
            return new JPanel();

        Arrays.sort(residuals);
        double minValue = residuals[0];
        double maxValue = residuals[residuals.length - 1];
        JPanel panel = new JPanel();
        JButton hideButton;
        JSlider slider;

        int sliderMin = (int) (1000 * minValue);
        int sliderMax = (int) (1000 * maxValue);
        int delta = (sliderMax - sliderMin) / 10;
        sliderMin -= delta;
        sliderMax += delta;

        slider = new JSlider(sliderMin, sliderMax);
        residualsThreshold = (maxValue + minValue) / 2.0;
        StringBuffer sb = decimalFormatter.format(residualsThreshold,
                new StringBuffer(), new FieldPosition(4));
        residualsReadout = new JTextField(sb.toString(), 4);
        hideButton = new JButton("Hide Above");
        hideButton.setToolTipText("Hide edges with weight < slider value");

        hideButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hideNodesByThreshold("residual", residualsThreshold, HIDE_ABOVE);
            }
        });

        panel.add(residualsReadout);
        panel.add(slider);
        panel.add(hideButton);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                residualsThreshold = (double) (slider.getValue() / 1000.0);
                StringBuffer sb = decimalFormatter.format(residualsThreshold, new StringBuffer(),
                        new FieldPosition(4));
                residualsReadout.setText(sb.toString());
            }
        });

        return panel;

    } // createHideByClusterResidualPanel

    //---------------------------------------------------------------------------------------
    public void hideEdgesByWeight(double min) {
        // System.out.println ("by weight: " + min);
        Edge[] edges = cw.getGraph().getEdgeArray();
        GraphObjAttributes ea = cw.getEdgeAttributes();

        ArrayList edgesToHide = new ArrayList();
        ArrayList hiddenEdgeTargetNodes = new ArrayList();

        for (int e = 0; e < edges.length; e++) {
            Edge edge = edges[e];
            Node sourceNode = edge.source();
            Node targetNode = edge.target();
            String edgeName = ea.getCanonicalName(edge);
            Double weight = ea.getDoubleValue("weight", edgeName);
            if (weight != null && weight.doubleValue() < min) {
                graphHider.hide(edge);
                EdgeCursor edgeCursor = sourceNode.inEdges();
                while (edgeCursor.ok()) {
                    graphHider.hide(edgeCursor.edge());
                    edgeCursor.next();
                } // while
            } // if
        } // for e

        cw.redrawGraph();

    } // hideEdgesByWeight

    //---------------------------------------------------------------------------------------
    public void hideNodesByThreshold(String nodeAttributeName, double threshold,
                                     int aboveOrBelow) {
        Node[] nodes = cw.getGraph().getNodeArray();
        GraphObjAttributes na = cw.getNodeAttributes();

        ArrayList nodesToHide = new ArrayList();
        ArrayList hiddenNodeTargetNodes = new ArrayList();

        for (int e = 0; e < nodes.length; e++) {
            Node node = nodes[e];
            String nodeName = na.getCanonicalName(node);
            Double value = na.getDoubleValue(nodeAttributeName, nodeName);
            if (value != null) {
                boolean hideThisOne =
                        (aboveOrBelow == HIDE_ABOVE && value.doubleValue() >= threshold) ||
                                (aboveOrBelow == HIDE_BELOW && value.doubleValue() <= threshold);
                if (hideThisOne)
                    graphHider.hide(node);
            } // if
        } // for e

        cw.redrawGraph();


    } // hideNodesByThreshold

    //---------------------------------------------------------------------------------------
    private String getNodeType(Node node) {
        String name = cw.getNodeAttributes().getCanonicalName(node);
        String type = cw.getNodeAttributes().getStringValue("type", name);
        if (type == null)
            type = "unknown";

        return type;

    } // nodeType

    //---------------------------------------------------------------------------------------
    public void selectAllRegulatorsOfSelectedClusters() {
        boolean error = true;
        Node clusterNode = null;
        NodeCursor nc = cw.getGraph().selectedNodes();
        ArrayList clusterNodeList = new ArrayList();

        while (nc.ok()) {
            Node candidateNode = nc.node();
            if (getNodeType(candidateNode).equals("cluster")) {
                clusterNodeList.add(candidateNode);
                error = false;
            } // if cluster
            nc.next();
        } // while

        if (error) {
            JOptionPane.showMessageDialog(null, "Please choose at least one cluster.",
                    "Inferelator: select regulators",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Node[] clusterNodes = (Node[]) clusterNodeList.toArray(new Node[0]);
        for (int i = 0; i < clusterNodes.length; i++)
            selectRegulatorsOfOneCluster(clusterNodes[i]);


    } // selectAllRegulatorsOfSelectedClusters

    //---------------------------------------------------------------------------------------
    protected void selectRegulatorsOfOneCluster(Node clusterNode) {
        ArrayList nodeNamesToSelect = new ArrayList();
        nodeNamesToSelect.add(cw.getCanonicalNodeName(clusterNode));

        ArrayList firstNeighbors = new ArrayList();
        EdgeCursor ec = clusterNode.edges();

        //-----------------------------------------------------------------------
        // for first neighbors, accept any node which is not a cluster node
        //-----------------------------------------------------------------------

        for (ec.toFirst(); ec.ok(); ec.next()) {
            Edge edge = ec.edge();
            Node source = edge.source();
            String name = cw.getCanonicalNodeName(source);
            if (!nodeNamesToSelect.contains(name) && !getNodeType(source).equals("cluster")) {
                firstNeighbors.add(source);
                nodeNamesToSelect.add(name);
            }
            Node target = edge.target();
            name = cw.getCanonicalNodeName(target);
            if (!nodeNamesToSelect.contains(name) && !getNodeType(target).equals("cluster")) {
                firstNeighbors.add(target);
                nodeNamesToSelect.add(name);
            }
        } // for edges, collecting first neighbors

        //-----------------------------------------------------------------------
        // for second neighbors, accept only gene nodes
        //-----------------------------------------------------------------------

        Node[] firstNeighborNodes = (Node[]) firstNeighbors.toArray(new Node[0]);
        for (int i = 0; i < firstNeighborNodes.length; i++) {
            Node firstNeighbor = firstNeighborNodes[i];
            ec = firstNeighbor.edges();
            for (ec.toFirst(); ec.ok(); ec.next()) {
                Edge edge = ec.edge();
                Node source = edge.source();
                String name = cw.getCanonicalNodeName(source);
                if (!nodeNamesToSelect.contains(name) && getNodeType(source).equals("gene"))
                    nodeNamesToSelect.add(name);
                Node target = edge.target();
                name = cw.getCanonicalNodeName(target);
                if (!nodeNamesToSelect.contains(name) && getNodeType(target).equals("gene"))
                    nodeNamesToSelect.add(name);
            } // for ec
        } // for i

        cw.selectNodesByName((String[]) nodeNamesToSelect.toArray(new String[0]), false);

    } // selectRegulatorsOfOneCluster

    //---------------------------------------------------------------------------------------
    public void selectAllRegulatedClusters() {
        boolean error = true;
        Node clusterNode = null;
        NodeCursor nc = cw.getGraph().selectedNodes();
        ArrayList regulatorNodeList = new ArrayList();

        while (nc.ok()) {
            Node candidateNode = nc.node();
            if (getNodeType(candidateNode).equals("gene")) {
                regulatorNodeList.add(candidateNode);
                error = false;
            } // if cluster
            nc.next();
        } // while

        if (error) {
            JOptionPane.showMessageDialog(null, "Please choose at least one regulator.",
                    "Inferelator: select regulators",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Node[] regulatorNodes = (Node[]) regulatorNodeList.toArray(new Node[0]);
        for (int i = 0; i < regulatorNodes.length; i++)
            selectClustersRegulatedByThisGene(regulatorNodes[i]);


    } // selectAllRegulatedClusters

    //---------------------------------------------------------------------------------------
/**
 * select all clusters, all co-regulating genes, and all logicGate nodes within
 * one or two steps from the specified regulator.  the success of the current
 * algorithm depends upon there being either zero or one logic gates between
 * regulators and the clusters they regulate.
 */
    protected void selectClustersRegulatedByThisGene(Node regulatorNode) {
        // System.out.println ("selectRegulatedClustersOfOneRegulator " + regulatorNode);
        ArrayList nodeNamesToSelect = new ArrayList();
        nodeNamesToSelect.add(cw.getCanonicalNodeName(regulatorNode));

        ArrayList firstNeighbors = new ArrayList();
        EdgeCursor ec = regulatorNode.edges();
        HashSet coRegulators = new HashSet();

        //-----------------------------------------------------------------------
        // for first neighbors, accept all nodes
        //-----------------------------------------------------------------------

        for (ec.toFirst(); ec.ok(); ec.next()) {
            Edge edge = ec.edge();
            Node source = edge.source();
            String name = cw.getCanonicalNodeName(source);
            if (!nodeNamesToSelect.contains(name)) {
                firstNeighbors.add(source);
                nodeNamesToSelect.add(name);
            }
            Node target = edge.target();
            name = cw.getCanonicalNodeName(target);
            if (!nodeNamesToSelect.contains(name)) {
                firstNeighbors.add(target);
                nodeNamesToSelect.add(name);
                //---------------------------------------------------------------------
                // special step if node is a logicGate:  include all regulating nodes
                // connected to this gate. this ensures that co-regulating genes
                // are included. but -do not- add these to the first neighbors list: that
                // might bring in other clusters not related to the seed <regulatorNode>
                //---------------------------------------------------------------------
                if (getNodeType(target).equals("logicGate")) {
                    // System.out.println ("need to select first neighbors of logic gate '" + name + "'");
                    EdgeCursor logicNodeEc = target.edges();
                    //System.out.println ("   edge count: " + logicNodeEc.size ());
                    for (logicNodeEc.toFirst(); logicNodeEc.ok(); logicNodeEc.next()) {
                        Edge logicEdge = logicNodeEc.edge();
                        Node candidate = logicEdge.source();
                        String candidateName = cw.getCanonicalNodeName(candidate);
                        if (!nodeNamesToSelect.contains(candidateName) && getNodeType(candidate).equals("gene"))
                            nodeNamesToSelect.add(candidateName);
                    } // for each edge attached to a logicGate node
                } // if logicGate
            } // if not yet selected
        } // for edges, collecting first neighbors

        //-----------------------------------------------------------------------
        // for second neighbors, accept only cluster nodes
        //-----------------------------------------------------------------------

        Node[] firstNeighborNodes = (Node[]) firstNeighbors.toArray(new Node[0]);
        for (int i = 0; i < firstNeighborNodes.length; i++) {
            Node firstNeighbor = firstNeighborNodes[i];
            ec = firstNeighbor.edges();
            for (ec.toFirst(); ec.ok(); ec.next()) {
                Edge edge = ec.edge();
                Node source = edge.source();
                String name = cw.getCanonicalNodeName(source);
                if (!nodeNamesToSelect.contains(name) && getNodeType(source).equals("cluster"))
                    nodeNamesToSelect.add(name);
                Node target = edge.target();
                name = cw.getCanonicalNodeName(target);
                if (!nodeNamesToSelect.contains(name) && getNodeType(target).equals("cluster"))
                    nodeNamesToSelect.add(name);
            } // for ec
        } // for i

        cw.selectNodesByName((String[]) nodeNamesToSelect.toArray(new String[0]), false);

    }

    //---------------------------------------------------------------------------------------
    public double[] getDoubleEdgeAttributes(String attributeName) {
        ArrayList tmp = new ArrayList();
        Edge[] edgeArray = cw.getGraph().getEdgeArray();
        GraphObjAttributes ea = cw.getEdgeAttributes();
        for (int i = 0; i < edgeArray.length; i++) {
            Edge edge = edgeArray[i];
            Double weight = ea.getDoubleValue(attributeName, ea.getCanonicalName(edge));
            if (weight != null)
                tmp.add(weight);
        }

        double[] result = new double[tmp.size()];
        for (int i = 0; i < tmp.size(); i++) {
            result[i] = ((Double) tmp.get(i)).doubleValue();
        }

        return result;

    }  // getDoubleEdgeAttrributes

    //---------------------------------------------------------------------------------------
    public double[] getDoubleNodeAttributes(String attributeName) {
        ArrayList tmp = new ArrayList();
        Node[] nodeArray = cw.getGraph().getNodeArray();
        GraphObjAttributes na = cw.getNodeAttributes();
        for (int i = 0; i < nodeArray.length; i++) {
            Node node = nodeArray[i];
            Double weight = na.getDoubleValue(attributeName, na.getCanonicalName(node));
            if (weight != null)
                tmp.add(weight);
        }

        double[] result = new double[tmp.size()];
        for (int i = 0; i < tmp.size(); i++) {
            result[i] = ((Double) tmp.get(i)).doubleValue();
        }

        return result;

    }  // getDoubleNodeAttrributes

    //---------------------------------------------------------------------------------------
    public double[] getEdgeWeights() {
        ArrayList tmp = new ArrayList();
        Edge[] edgeArray = cw.getGraph().getEdgeArray();
        GraphObjAttributes ea = cw.getEdgeAttributes();
        for (int i = 0; i < edgeArray.length; i++) {
            Edge edge = edgeArray[i];
            Double weight = ea.getDoubleValue("weight", ea.getCanonicalName(edge));
            if (weight != null)
                tmp.add(weight);
        }

        double[] result = new double[tmp.size()];
        for (int i = 0; i < tmp.size(); i++) {
            result[i] = ((Double) tmp.get(i)).doubleValue();
        }

        return result;

    }  // getEdgeWeights

    //---------------------------------------------------------------------------------------
    public synchronized void handleNameList(String source, Namelist nameList) throws RemoteException {
        boolean clearSelectionFirst = false;
        selectNodesByName(nameList.getNames());
        //cw.selectNodesByName (names, clearSelectionFirst);

    } // handleNameList

    //-------------------------------------------------------------------------------------
    public void handleMatrix(String source, DataMatrix matrix) {
    }

    public void handleTuple(String source, GaggleTuple gaggleTuple) {
    }

    public void handleCluster(String source, Cluster cluster) {
    }

    public void handleNetwork(String source, Network network) {
    }

    //---------------------------------------------------------------------------------------
    public String getName() {
        return myGaggleName;
    }

    //-------------------------------------------------------------------------------------
    public void setName(String newValue) {
        myGaggleName = newValue;
        cw.getMainFrame().setTitle(myGaggleName);
    }

    //-------------------------------------------------------------------------------------
    public int getSelectionCount() {
        return cw.getGraph().selectedNodes().size();
    }

    //-------------------------------------------------------------------------------------
    public void doHide() {
        cw.getMainFrame().setVisible(false);
    }

    //-------------------------------------------------------------------------------------
    public void doShow() {
        JFrame frame = cw.getMainFrame();

        if (frame.getExtendedState() != java.awt.Frame.NORMAL)
            frame.setExtendedState(java.awt.Frame.NORMAL);
        System.out.println("2) GuiBoss.show, boss, state: " + frame.getExtendedState());

        MiscUtil.setJFrameAlwaysOnTop(frame, true);
        frame.setVisible(true);
        MiscUtil.setJFrameAlwaysOnTop(frame, false);

    }

    //-------------------------------------------------------------------------------------
    public void clearSelections() {
        cw.selectNodes(new Node[]{}, true);
    }

    //-------------------------------------------------------------------------------------
    public void setGeometry(int x, int y, int width, int height) {
        cw.getMainFrame().setSize(width, height);
        cw.getMainFrame().setLocation(x, y);
    }

    //-------------------------------------------------------------------------------------
    private String[] getSelection() {
        Graph2D graph = cw.getGraph();
        ArrayList list = new ArrayList();
        for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next()) {
            Node node = nc.node();
            String canonicalName = cw.getCanonicalNodeName(node);
            String organism = cw.getNodeAttributes().getStringValue("species", canonicalName);
            list.add(canonicalName);
        } // for nc

        return (String[]) list.toArray(new String[0]);

    } // getSelection

    //-------------------------------------------------------------------------------------
    public void doBroadcastList() {
        Graph2D graph = cw.getGraph();
        ArrayList list = new ArrayList();
        String[] selectedNodeNames = getSelection();
        Namelist nameList = new Namelist();
        nameList.setSpecies(species);
        nameList.setNames(selectedNodeNames);

        try {
            boss.broadcastNamelist(myGaggleName, targetGoose, nameList);
        }
        catch (RemoteException rex) {
            System.err.println("error rmi selecting at boss, from GaggleLister " + myGaggleName);
        }

    } // doBroadcastList


    public void doBroadcastNetwork() {
        org.systemsbiology.gaggle.core.datatypes.Network network = NetworkUtil.createNetworkFromSelection(cw);

        try {
            boss.broadcastNetwork(myGaggleName, targetGoose, network);
        }
        catch (RemoteException rex) {
            String msg = "gaggle error, in CyGagglePlugin.doBroadcastNetwork " + myGaggleName + ": " +
                    rex.getMessage();
            System.err.println(msg);
            rex.printStackTrace();
        }

    } // doBroadcastNetwork


    //-------------------------------------------------------------------------------------
    public void doExit() {
        connector.disconnectFromGaggle(true);
        // last window in a jvm, when disposed, cause jvm to exit
        cw.getMainFrame().dispose();

    }

    //---------------------------------------------------------------------------------------
    public String[] getCommands() {
        return new String[]{"show", "hide", "terminate"};
    }


    public void update(String[] gooseNames) {
        this.activeGooseNames = gooseNames;
        MiscUtil.updateGooseChooser(gooseChooser, myGaggleName, activeGooseNames);
    }

    public void setConnected(boolean connected, Boss boss) {
        this.boss = boss;
        if (connectButton != null)
            connectButton.setEnabled(!connected);
        if (disconnectButton != null)
            disconnectButton.setEnabled(connected);
    }

//-------------------------------------------------------------------------------------
} // class ControlPanel

