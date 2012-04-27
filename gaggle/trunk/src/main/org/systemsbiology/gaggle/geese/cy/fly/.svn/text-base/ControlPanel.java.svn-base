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

package org.systemsbiology.gaggle.geese.cy.fly;
//---------------------------------------------------------------------------------------

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;


import java.rmi.*;

import java.util.*;
import java.text.*;
import java.lang.Math;

import org.systemsbiology.gaggle.util.MiscUtil;
import org.systemsbiology.gaggle.geese.cy.util.*;
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;
import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.datatypes.*;

import cytoscape.*;
import cytoscape.undo.UndoableGraphHider;
//import cytoscape.data.*;
import cytoscape.data.servers.*;

import y.base.*;
import y.view.*;

//---------------------------------------------------------------------------------------
public class ControlPanel extends JPanel implements Goose, java.io.Serializable {

    protected JToolBar toolbar;
    protected GridBagLayout gridbagLayout;
    protected JTextField nodeSelectionTextField;
    protected JTextField movieConditionReadout;

    protected CytoscapeWindow cw;
    protected BioDataServer dataServer;
    protected UndoableGraphHider graphHider;
    String myGaggleName = "Cy";
    Boss boss;
    RmiGaggleConnector connector = new RmiGaggleConnector(this);

    JComboBox gooseChooser;
    String targetGoose = "Boss";

    JTextField confidenceReadout;
    double confidenceThreshold;
    DecimalFormat decimalFormatter;

    String species = "unknown";

    static final int HIDE_ABOVE = 1;
    static final int HIDE_BELOW = 2;

    //---------------------------------------------------------------------------------------
    public ControlPanel(CytoscapeWindow cw) {
        ToolTipManager.sharedInstance().setInitialDelay(0);
        this.cw = cw;
        species = cw.getDefaultSpecies();
        dataServer = cw.getBioDataServer();
        graphHider = cw.getGraphHider();
        decimalFormatter = new DecimalFormat("0.000");
        myGaggleName = discoverGooseName(cw.getConfiguration().getArgs());

        add(createGui());
        try {
            connectToGaggle();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        MiscUtil.updateGooseChooserOLD(boss, gooseChooser, myGaggleName, null);

    } // ctor

    //---------------------------------------------------------------------------------------
    public void connectToGaggle() {
        try {
            connector.connectToGaggle();
        }
        catch (Exception ex0) {
            JOptionPane.showMessageDialog(this, "cy-fly Control Panel could not connect to gaggle");
            System.err.println(ex0.getMessage());
        }
        boss = connector.getBoss();
    } // connectToGaggle

    //-------------------------------------------------------------------------------------
    JPanel createGui() {
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BorderLayout());
        JPanel westPanel = new JPanel();
        outerPanel.add(westPanel, BorderLayout.WEST);

        movieConditionReadout = new JTextField(16);
        movieConditionReadout.setBackground(Color.GRAY.brighter());
        movieConditionReadout.setToolTipText("'Movie' condition titles are displayed here.");
        movieConditionReadout.setEditable(false);

        JPanel movieReadoutPanel = new JPanel();
        westPanel.setLayout(new GridLayout(3, 1));
        movieReadoutPanel.add(movieConditionReadout);
        westPanel.add(createGaggleToolBar());
        westPanel.add(createCyToolBar());
        westPanel.add(movieReadoutPanel);

        if (!cw.getEdgeAttributes().hasAttribute("confidence"))
            return outerPanel;

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.add("confidence", createHideByConfidencePanel());

        if (tabbedPane.getTabCount() > 0)
            outerPanel.add(tabbedPane, BorderLayout.EAST);

        return outerPanel;

    } // createGui

    //-------------------------------------------------------------------------------------
    protected JPanel createCyToolBar() {
        JPanel cyControlsPanel = new JPanel();

        nodeSelectionTextField = new JTextField(8);
        String msg = "<html>Enter node names here, using common <br>" +
                "or cannonical names, upper or lower case <br>" +
                " &nbsp; '*' &nbsp; at end of name matches all.</html>";
        nodeSelectionTextField.setToolTipText(msg);

        nodeSelectionTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyText(e.getKeyCode()).equals("Enter")) selectNodes();
            }
        });


        JToolBar cyToolbar = new JToolBar();
        cyToolbar.setFloatable(false);
        JButton showAllButton = new JButton("U");
        showAllButton.setToolTipText("Unhide all nodes & edges");
        JButton hideOrphansButton = new JButton("O");
        hideOrphansButton.setToolTipText("Hide Orphan Nodes");

        JButton clearSelectionsButton = new JButton("Cl");
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

        cyToolbar.add(showAllButton);
        cyToolbar.add(hideOrphansButton);
        cyToolbar.add(clearSelectionsButton);

        cyControlsPanel.add(cyToolbar);
        cyControlsPanel.add(nodeSelectionTextField);

        return cyControlsPanel;

    } // createCyToolBar

    //-------------------------------------------------------------------------------------
    protected JPanel createGaggleToolBar() {
        JPanel panel = new JPanel();
        JToolBar gaggleToolbar = new JToolBar();
        gaggleToolbar.setFloatable(false);
        panel.add(gaggleToolbar);

        JButton getGeeseNamesButton = new JButton("Update");
        getGeeseNamesButton.setToolTipText("Update Goose List");
        gaggleToolbar.add(getGeeseNamesButton);
        getGeeseNamesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MiscUtil.updateGooseChooserOLD(boss, gooseChooser, myGaggleName, null);
            }
        });

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
        JButton broadcastNetworkButton = new JButton("N");

        showGooseButton.setToolTipText("Show selected goose");
        hideGooseButton.setToolTipText("Hide selected goose");
        broadcastListButton.setToolTipText("Broadcast list to selected goose");
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

        broadcastNetworkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doBroadcastNetwork();
            }
        });

        gaggleToolbar.add(showGooseButton);
        gaggleToolbar.add(hideGooseButton);
        gaggleToolbar.add(broadcastListButton);
        gaggleToolbar.add(broadcastNetworkButton);

        return panel;

    }  // createGaggleToolBar

    //-------------------------------------------------------------------------------------
    protected JToolBar createToolBar() {
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        JButton showAllButton = new JButton("S");
        showAllButton.setToolTipText("Show all nodes & edges");
        JButton hideOrphansButton = new JButton("H");
        hideOrphansButton.setToolTipText("Hide Orphan Nodes");

        JButton broadcastNodeNamesButton = new JButton("b");
        broadcastNodeNamesButton.setToolTipText("Broadcast node names.");

        JButton clearSelectionsButton = new JButton("Cl");
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
        toolbar.add(broadcastNodeNamesButton);
        toolbar.add(clearSelectionsButton);

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
        //String rawTextFieldContents = nodeSelectionTextField.getText().trim ();
        // String [] candidates = rawTextFieldContents.split (" ");
        HashMap clusteredGenesMap = getGeneClusters();

        String[] nodeNames = getCanonicalNamesOfNodesInGraph();
        NodeNameMatcher nameMatcher =
                new NodeNameMatcher(nodeNames, clusteredGenesMap, cw.getBioDataServer(),
                        cw.getDefaultSpecies());

        String[] matchedNodes = nameMatcher.getMatch(candidates);

        //System.out.println ("------------ match count: " + matchedNodes.length);
        //for (int i=0; i < matchedNodes.length; i++)
        //  System.out.println ("    " + matchedNodes [i]);

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

        Node[] nodes = cw.getGraph().getNodeArray();
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].degree() == 0)
                graphHider.hide(nodes[i]);
        } // for i

        cw.redrawGraph();

    } // hideOrphans

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
                boss.broadcastNamelist(myGaggleName, "boss", nameList);
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
    JPanel createHideByConfidencePanel() {
        double[] confidences = getDoubleEdgeAttributes("confidence");
        if (confidences == null || confidences.length == 0)
            return new JPanel();

        double[] absoluteValueConfidences = absoluteValue(confidences);
        Arrays.sort(absoluteValueConfidences);
        double minValue = 0.0; //absoluteValueConfidences [0];
        double maxValue = 1.0; // absoluteValueConfidences [absoluteValueConfidences.length - 1];
        JPanel panel = new JPanel();
        JButton hideButton;
        JSlider slider;

        int sliderMin = (int) (1000 * minValue);
        int sliderMax = (int) (1000 * maxValue);
        int delta = (sliderMax - sliderMin) / 10;
        //sliderMin -= delta;
        //sliderMax += delta;
        slider = new JSlider(sliderMin, sliderMax);
        confidenceThreshold = (maxValue + minValue) / 2.0;
        StringBuffer sb = decimalFormatter.format(confidenceThreshold,
                new StringBuffer(), new FieldPosition(4));
        confidenceReadout = new JTextField(sb.toString(), 4);
        confidenceReadout.setEditable(false);

        hideButton = new JButton("Hide Below");
        hideButton.setToolTipText("Hide edges with confidence < slider value");
        hideButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hideEdgesByConfidence(confidenceThreshold);
            }
        });

        panel.add(confidenceReadout);
        panel.add(slider);
        panel.add(hideButton);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                confidenceThreshold = (double) (slider.getValue() / 1000.0);
                StringBuffer sb = decimalFormatter.format(confidenceThreshold, new StringBuffer(),
                        new FieldPosition(4));
                confidenceReadout.setText(sb.toString());
            }
        });

        return panel;

    } // createHideByRegulatorWeightPanel

    //---------------------------------------------------------------------------------------
    public void hideEdgesByConfidence(double min) {
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
            Double confidence = ea.getDoubleValue("confidence", edgeName);
            //System.out.println ("checking confidence (" + confidence + ") for " + edgeName);
            if (confidence != null) {
                double value = confidence.doubleValue();
                if (value < min)
                    graphHider.hide(edge);
            } // if !null
        } // for e

        cw.redrawGraph();

    } // hideEdgesByConfidence

    //---------------------------------------------------------------------------------------
    private String getNodeType(Node node) {
        String name = cw.getNodeAttributes().getCanonicalName(node);
        String type = cw.getNodeAttributes().getStringValue("type", name);
        if (type == null)
            type = "unknown";

        return type;

    } // nodeType

    //---------------------------------------------------------------------------------------
    public double[] absoluteValue(double[] array) {
        double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++)
            result[i] = Math.abs(array[i]);

        return result;
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
    public synchronized void handleNameList(String source, Namelist nameList) throws RemoteException {
        System.out.println(" **** handleNameList");
        boolean clearSelectionFirst = false;
        selectNodesByName(nameList.getNames());
        //cw.selectNodesByName (names, clearSelectionFirst);

    } // handleNameList

    //-------------------------------------------------------------------------------------
    public void handleMatrix(String source, DataMatrix matrix) {
    }

    public void handleCluster(String source, Cluster cluster) {
    }

    //-------------------------------------------------------------------------------------
    public void handleNetwork(String source, Network network) throws RemoteException {
        NetworkUtil.extend(cw, network);
        System.out.println("node count before redraw: " + cw.getGraph().getNodeArray().length);
        System.out.println("about to redraw graph");
        cw.redrawGraph(true);

    } // handleNetwork

    //---------------------------------------------------------------------------------------
    protected HashMap getCurrentNodes(Graph2D graph) {
        HashMap result = new HashMap();
        for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
            Node node = nc.node();
            String name = cw.getCanonicalNodeName(node);
            //String name = node.toString ();
            System.out.println("adding node named " + name + " to hash");
            result.put(name, node);
        } // for nc

        return result;

    } // getCurrentNodes

    //---------------------------------------------------------------------------------------
    protected String[] getCurrentNodeNames(Graph2D graph) {
        ArrayList list = new ArrayList();
        for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
            Node node = nc.node();
            String canonicalName = cw.getCanonicalNodeName(node);
            list.add(canonicalName);
        } // for nc

        return (String[]) list.toArray(new String[0]);

    } // getCurrentNodeNames
//---------------------------------------------------------------------------------------

    public void handleTuple(String source, GaggleTuple gaggleTuple) {
         // todo tuple fix
        /*
        movieConditionReadout.setText(attributeMap.getName());
        String[] attributeNames = AttributeMapUtil.getAttributeNames(attributeMap);
        GraphObjAttributes goa = cw.getNodeAttributes();
        for (int i = 0; i < attributeNames.length; i++) {
            String attributeName = attributeNames[i];
            Map<String, Object> map = AttributeMapUtil.getKeyValuePairsForAttribute(attributeMap, attributeName);
            for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
                String key = it.next();
                double value = (Double) map.get(key);
                if (Double.isNaN(value)) {
                    value = 0.0;
                }
                goa.set(attributeName, key, value);
            }
        }
        cw.redrawGraph();
        */
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
            boss.broadcastNamelist(myGaggleName, "boss", nameList);
        }
        catch (RemoteException rex) {
            System.err.println("error rmi selecting at boss, from GaggleLister " + myGaggleName);
        }

    } // doBroadcastList

    //-------------------------------------------------------------------------------------
    public void doBroadcastNetwork() {
        Network network = NetworkUtil.createNetworkFromSelection(cw);

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

    //------------------------------------------------------------------------------
    public void doExit() {
        connector.disconnectFromGaggle(true);
        // last window in a jvm, when disposed, cause jvm to exit
        cw.getMainFrame().dispose();

    }

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

    public void update(String[] gooseNames) {
    }


    //------------------------------------------------------------------------------
    public String[] getCommands() {
        return new String[]{"show", "hide", "terminate"};
    }
//-------------------------------------------------------------------------------------
} // class ControlPanel

