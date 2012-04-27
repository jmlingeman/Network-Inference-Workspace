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

package org.systemsbiology.gaggle.geese.cy.tileViewer;
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
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;
import org.systemsbiology.gaggle.geese.common.GooseShutdownHook;
import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.datatypes.*;

import cytoscape.*;
import cytoscape.undo.UndoableGraphHider;
import cytoscape.data.servers.*;


import y.base.*;
import y.view.*;

//---------------------------------------------------------------------------------------
public class ControlPanel extends JPanel implements Goose, java.io.Serializable {

    protected String[] activeGooseNames;
    protected JButton connectButton;
    protected JButton disconnectButton;
    protected JToolBar tileNavigationToolbar;
    protected GridBagLayout gridbagLayout;
    protected JTextField nodeSelectionTextField;
    protected JTextField nodeInClusterSelectionTextField;

    protected CytoscapeWindow cw;
    protected BioDataServer dataServer;
    protected UndoableGraphHider graphHider;

    String myGaggleName = "TileViewer";
    String targetGoose = "Boss";

    Boss gaggleBoss;
    RmiGaggleConnector connector = new RmiGaggleConnector(this);
    JPopupMenu gagglePopupMenu;
    protected GeneFinder geneFinder;

    JComboBox gooseChooser;

    protected Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    protected Cursor busyCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

    JTextField geneFindingDistanceReadout, zoomFactorReadout,
            ratioThresholdReadout, lambdaThresholdReadout;
    JTextField residualsReadout, motifPValsReadout;
    double zoomFactor, ratioThreshold, lambdaThreshold;
    int geneFindingDistance = 200;
    DecimalFormat decimalFormatter;
    double motifPValsThreshold, residualsThreshold;

    String species = "unknown";
    String[] lastSelection = new String[0];
    int selectedNodeIndex = 0;

    static final int HIDE_ABOVE = 1;
    static final int HIDE_BELOW = 2;

    //---------------------------------------------------------------------------------------
    public ControlPanel(CytoscapeWindow cw) {
        new GooseShutdownHook(connector);
        ToolTipManager.sharedInstance().setInitialDelay(0);
        this.cw = cw;
        geneFinder = new GeneFinder(cw);
        species = cw.getDefaultSpecies();
        dataServer = cw.getBioDataServer();
        graphHider = cw.getGraphHider();
        decimalFormatter = new DecimalFormat("0.000");

        try {
            connectToGaggle();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        add(createGui());
        MiscUtil.updateGooseChooser(gooseChooser, myGaggleName, activeGooseNames);

    } // ctor

    //---------------------------------------------------------------------------------------
    public void connectToGaggle() {
        try {
            connector.connectToGaggle();
        }
        catch (Exception ex0) {
            JOptionPane.showMessageDialog(this, "TileViewer Control Panel could not connect to gaggle");
            System.err.println(ex0.getMessage());
        }
        gaggleBoss = connector.getBoss();
    } // connectToGaggle

    //-------------------------------------------------------------------------------------
    JPanel createGui() {
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BorderLayout());
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(3, 1));
        outerPanel.add(leftPanel, BorderLayout.WEST);

        leftPanel.add(createGaggleToolBar());
        JPanel centeringPanel = new JPanel();
        centeringPanel.add(createTileNavigationToolBar());
        leftPanel.add(centeringPanel);

        JPanel selectByNamePanel = new JPanel();
        leftPanel.add(selectByNamePanel);

        JButton selectNodesButton = new JButton("Select: ");
        selectNodesButton.setToolTipText("Select nodes by name.");

        selectNodesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectNodes();
            }
        });

        selectByNamePanel.add(selectNodesButton);
        nodeSelectionTextField = new JTextField(8);
        nodeSelectionTextField.setToolTipText("Select nodes by name.");
        nodeSelectionTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyText(e.getKeyCode()).equals("Enter")) selectNodes();
            }
        });

        selectByNamePanel.add(nodeSelectionTextField);

        JTabbedPane tabbedPane = new JTabbedPane();
        outerPanel.add(tabbedPane, BorderLayout.CENTER);

        tabbedPane.add("'Next' Zoom Factor", createZoomFactorSliderPanel());
        tabbedPane.add("Gene Finding Distance", createGeneFindingDistanceSliderPanel());
        tabbedPane.add("Tile Lambda Threshold", createLambdaThresholdSliderPanel());

        return outerPanel;

    } // createGui

    //-----------------------------------------------------------------------------------
    protected JToolBar createTileNavigationToolBar() {
        tileNavigationToolbar = new JToolBar();
        tileNavigationToolbar.setFloatable(false);

        JButton selectDownstreamGenesButton = new JButton("Genes");
        selectDownstreamGenesButton.setToolTipText(
                "Select genes downstream from all currently selected tiles.");
        selectDownstreamGenesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                geneFinder.selectCandidateGenes(geneFindingDistance);
            }
        });

        JButton previousSelectedNodeButton = new JButton("Prev");
        previousSelectedNodeButton.setToolTipText("Zoom in on previous selected node.");

        previousSelectedNodeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                zoomInOnPreviousSelectedNode();
            }
        });

        JButton nextSelectedNodeButton = new JButton("Next");
        nextSelectedNodeButton.setToolTipText("Zoom in on next selected node.");

        nextSelectedNodeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                zoomInOnNextSelectedNode();
            }
        });

        JButton clearSelectionsButton = new JButton("Clear");
        clearSelectionsButton.setToolTipText("Clear selections.");

        clearSelectionsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cw.selectNodes(new Node[0], true);
            }
        });

        //JButton broadcastNodeNamesButton = new JButton ("b");
        //broadcastNodeNamesButton.setToolTipText ("Broadcast node names.");

        //broadcastNodeNamesButton.addActionListener (new ActionListener () {
        //  public void actionPerformed (ActionEvent e) {doBroadcastList ();}});

        tileNavigationToolbar.add(selectDownstreamGenesButton);
        tileNavigationToolbar.add(previousSelectedNodeButton);
        tileNavigationToolbar.add(nextSelectedNodeButton);
        tileNavigationToolbar.add(clearSelectionsButton);
        //tileNavigationToolbar.add (broadcastNodeNamesButton);

        return tileNavigationToolbar;

    } // createTileNavigationToolBar

    //-----------------------------------------------------------------------------------
    class GaggleButtonListener extends MouseAdapter implements ActionListener {
        Boss gaggleBoss;

        GaggleButtonListener(Boss gaggleBoss) {
            super();
            this.gaggleBoss = gaggleBoss;
        }

        //------------------------------------------------------------------------
        public void mouseClicked(MouseEvent e) {
            JPopupMenu gagglePopupMenu = new JPopupMenu();
            JMenu showMenu = new JMenu("Show");
            JMenu broadcastMenu = new JMenu("Broadcast To");
            gagglePopupMenu.add(showMenu);
            gagglePopupMenu.add(showMenu);
            gagglePopupMenu.add(broadcastMenu);
            JMenuItem showMI, broadcastMI;

            showMI = new JMenuItem("Boss");
            showMI.addActionListener(this);
            showMI.setActionCommand("show");
            showMenu.add(showMI);

            broadcastMI = new JMenuItem("All");
            broadcastMI.addActionListener(this);
            broadcastMI.setActionCommand("broadcast");
            broadcastMenu.add(broadcastMI);

            try {
                String[] gooseNames = gaggleBoss.getGooseNames();
                for (int i = 0; i < gooseNames.length; i++) {
                    showMI = new JMenuItem(gooseNames[i]);
                    showMI.addActionListener(this);
                    showMI.setActionCommand("show");
                    showMenu.add(showMI);
                    broadcastMI = new JMenuItem(gooseNames[i]);
                    broadcastMI.addActionListener(this);
                    broadcastMI.setActionCommand("broadcast");
                    broadcastMenu.add(broadcastMI);
                }
                gagglePopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
            catch (RemoteException rex) {
                System.err.println(" -- error asking boss for goose names: " + rex.getMessage());
                rex.printStackTrace();
            }

        } // mouseClicked

        //------------------------------------------------------------------------
        public void actionPerformed(ActionEvent e) {
            String action = e.getActionCommand();
            String gooseName = ((JMenuItem) e.getSource()).getText();
            System.out.println("do '" + action + "' on " + gooseName);
            try {
                if (action.equalsIgnoreCase("show")) {
                    System.out.println("about to call gaggleBoss.show (" + gooseName + ")");
                    //gaggleBoss.cmd ("show");
                    gaggleBoss.show(gooseName);
                }
            }
            catch (RemoteException rex) {
                System.err.println(" -- gaggle boss error: " + rex.getMessage());
                rex.printStackTrace();
            }

            //JMenuItem source = (JMenuItem) e.getSource ();
            //System.out.println ("action: " + source.getLabel () + "   " + e.getActionCommand ());

        } // actionPerformed
        //------------------------------------------------------------------------

    } // GaggleButtonListner

    //-----------------------------------------------------------------------------------
    public void selectTilesAboveCurrentRatioThreshold() {
        HashSet set = new HashSet();
        String[] nodeNames = getCanonicalNamesOfNodesInGraph();
        boolean foundAtLeastOneNodeWithRatioAttributes = false;
        for (int i = 0; i < nodeNames.length; i++) {
            String canonicalName = nodeNames[i];
            Double ratio = cw.getNodeAttributes().getDoubleValue("log10 ratios", canonicalName);
            if (ratio != null) {
                foundAtLeastOneNodeWithRatioAttributes = true;
                if (ratio.doubleValue() >= ratioThreshold)
                    set.add(canonicalName);
            } // if !null
        } // for i

        if (!foundAtLeastOneNodeWithRatioAttributes) {
            JOptionPane.showMessageDialog(cw.getMainFrame(),
                    "No tiles have been assigned log10 ratio values.");
            return;
        }

        if (set.size() == 0) {
            JOptionPane.showMessageDialog(cw.getMainFrame(),
                    "No tiles have log10 ratio above " + ratioThreshold + ".");
            return;
        }

        String[] names = (String[]) set.toArray(new String[0]);
        boolean clearSelectionFirst = false;
        cw.selectNodesByName(names, clearSelectionFirst);


    } // selectTilesAboveCurrentRatioThreshold

    //-----------------------------------------------------------------------------------
    public void selectTilesAboveCurrentLambdaThreshold() {
        HashSet set = new HashSet();
        String[] nodeNames = getCanonicalNamesOfNodesInGraph();
        boolean foundAtLeastOneNodeWithLambdaAttributes = false;

        for (int i = 0; i < nodeNames.length; i++) {
            String canonicalName = nodeNames[i];
            Double lambda = cw.getNodeAttributes().getDoubleValue("lambdas", canonicalName);
            if (lambda != null) {
                foundAtLeastOneNodeWithLambdaAttributes = true;
                if (lambda.doubleValue() >= lambdaThreshold)
                    set.add(canonicalName);
            } // for i
        } // for i


        if (!foundAtLeastOneNodeWithLambdaAttributes) {
            JOptionPane.showMessageDialog(cw.getMainFrame(),
                    "No tiles have been assigned lambda values.");
            return;
        }

        if (set.size() == 0) {
            JOptionPane.showMessageDialog(cw.getMainFrame(),
                    "No tiles have lambda above " + lambdaThreshold + ".");
            return;
        }

        if (set.size() == 0) {
            JOptionPane.showMessageDialog(cw.getMainFrame(), "No tiles have lambda values.");
            return;
        }

        String[] names = (String[]) set.toArray(new String[0]);
        boolean clearSelectionFirst = false;
        cw.selectNodesByName(names, clearSelectionFirst);

    }

    //-----------------------------------------------------------------------------------
    public void zoomInOnNextSelectedNode() {
        String[] currentSelection = cw.getSelectedNodeNames();
        if (lastSelection.length == 0 || Arrays.equals(currentSelection, lastSelection)) {
            // zoom to the next node
            selectedNodeIndex++;
            if (selectedNodeIndex >= currentSelection.length)
                selectedNodeIndex = 0;
            boolean clearSelectionFirst = true;
            cw.selectNodesByName(new String[]{currentSelection[selectedNodeIndex]}, clearSelectionFirst);
            cw.zoomToSelectedNodes(zoomFactor);
            cw.selectNodesByName(currentSelection, false);
            lastSelection = currentSelection;
        } else {
            lastSelection = currentSelection;
        }

    } // zoomInOnNextSelectedNode

    //-----------------------------------------------------------------------------------
    public void zoomInOnPreviousSelectedNode() {
        String[] currentSelection = cw.getSelectedNodeNames();
        if (lastSelection.length == 0 || Arrays.equals(currentSelection, lastSelection)) {
            // zoom to the next node
            selectedNodeIndex--;
            if (selectedNodeIndex < 0)
                selectedNodeIndex = currentSelection.length - 1;
            boolean clearSelectionFirst = true;
            cw.selectNodesByName(new String[]{currentSelection[selectedNodeIndex]}, clearSelectionFirst);
            cw.zoomToSelectedNodes(zoomFactor);
            cw.selectNodesByName(currentSelection, false);
            lastSelection = currentSelection;
        } else {
            lastSelection = currentSelection;
        }

    } // zoomInOnPreviousSelectedNode

    //-----------------------------------------------------------------------------------
/**
 * **********************
 * public String [] getSelectedNodeNames ()
 * {
 * Graph2D graph = cw.getGraph ();
 * ArrayList list = new ArrayList ();
 * for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next()) {
 * Node node = nc.node ();
 * String canonicalName = cw.getCanonicalNodeName (node);
 * String organism = cw.getNodeAttributes().getStringValue ("species", canonicalName);
 * list.add (canonicalName);
 * } // for nc
 * <p/>
 * return (String []) list.toArray (new String [0]);
 * <p/>
 * } // getSelectedNodeNames
 * ***********************
 */
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
        if (rawTextFieldContents.trim().length() == 0)
            return;

        System.out.println("---------- select by name, input:\n" + rawTextFieldContents);
        String[] candidates = rawTextFieldContents.split(" ");
        HashMap clusteredGenesMap = new HashMap();

        String[] nodeNames = getCanonicalNamesOfNodesInGraph();
        NodeNameMatcher nameMatcher =
                new NodeNameMatcher(nodeNames, clusteredGenesMap, cw.getBioDataServer(),
                        cw.getDefaultSpecies());

        String[] canonicalNames = nameMatcher.canonicalizeNodeNames(candidates);

        System.out.println("------------ match count: " + canonicalNames.length);
        for (int i = 0; i < canonicalNames.length; i++)
            System.out.println("    " + canonicalNames[i]);

        cw.selectNodesByName(canonicalNames, false);

    } // selectNodes

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
            if (countRegulatoryEdges(logicGateNodes[i]) == 0)
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
/**
 * *************************************************
 * protected void broadcastClusterMatrix ()
 * {
 * // System.out.println ("broadcastClusterMatrix");
 * <p/>
 * NodeCursor nc = cw.getGraph().selectedNodes ();
 * <p/>
 * ArrayList clusterNodes = new ArrayList ();
 * while (nc.ok ()) {
 * String name = cw.getNodeAttributes().getCanonicalName (nc.node ());
 * String type = cw.getNodeAttributes().getStringValue ("type", name);
 * if (type != null && type.equals ("cluster"))
 * clusterNodes.add (name);
 * nc.next ();
 * }
 * <p/>
 * if (clusterNodes.size () != 1) {
 * JOptionPane.showMessageDialog (null, "Please choose exactly one cluster to broadcast.",
 * "TileViewer Control Panel Error",
 * JOptionPane.ERROR_MESSAGE);
 * return;
 * }
 * <p/>
 * String clusterName = (String) clusterNodes.get (0);
 * String [] clusteredGenes =
 * cw.getNodeAttributes().getStringArrayValues ("clusterGenes", clusterName);
 * for (int i=0; i < clusteredGenes.length; i++)
 * clusteredGenes [i] = clusteredGenes [i].toUpperCase ();
 * <p/>
 * String [] conditionNames =
 * cw.getNodeAttributes().getStringArrayValues ("clusterConditions", clusterName);
 * <p/>
 * String [] fixedConditionNames = new String [conditionNames.length];
 * for (int i=0; i < conditionNames.length; i++) {
 * String name = conditionNames [i];
 * // if (conditionNameFixer.containsKey (name))
 * //   name = (String) conditionNameFixer.get (name);
 * // else
 * //   System.err.println ("found no map for old condition name '" + name + "'");
 * fixedConditionNames [i] = name;
 * } // for i
 * <p/>
 * //System.out.println (clusteredGenes.length + " genes in "  +
 * //                   fixedConditionNames.length + " conditions.");
 * <p/>
 * //for (int i=0; i < clusteredGenes.length; i++)
 * //  System.out.println (clusteredGenes [i]);
 * <p/>
 * //for (int i=0; i < fixedConditionNames.length; i++)
 * //  System.out.println (fixedConditionNames [i]);
 * <p/>
 * try {
 * System.out.println ("ICP broadcast cluster: " + clusteredGenes.length + " genes " +
 * fixedConditionNames.length + " clusters");
 * gaggleBoss.broadcast (myGaggleName, species, clusteredGenes, fixedConditionNames);
 * }
 * catch (RemoteException rex0) {
 * System.err.println  ("ControlPanel.broadcastClusterMatrix: " +
 * rex0.getMessage ());
 * }
 * <p/>
 * } // broadcastClusterMatrix
 * ****************************************
 */
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
                gaggleBoss.broadcastNamelist(myGaggleName, targetGoose, nameList);
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
    JPanel createZoomFactorSliderPanel() {
        double minValue = 0.05;
        double maxValue = 3.0;
        JPanel panel = new JPanel();
        JButton hideButton;
        JSlider slider;

        int sliderMin = (int) (1000 * minValue);
        int sliderMax = (int) (1000 * maxValue);
        int delta = (sliderMax - sliderMin) / 10;
        sliderMin -= delta;
        sliderMax += delta;
        zoomFactor = 0.5;
        int initialValue = (int) (1000 * zoomFactor);
        slider = new JSlider(sliderMin, sliderMax, initialValue);
        StringBuffer sb = decimalFormatter.format(zoomFactor,
                new StringBuffer(), new FieldPosition(2));
        zoomFactorReadout = new JTextField(sb.toString(), 6);

        panel.add(zoomFactorReadout);
        panel.add(slider);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                zoomFactor = (double) (slider.getValue() / 1000.0);
                StringBuffer sb = decimalFormatter.format(zoomFactor, new StringBuffer(),
                        new FieldPosition(4));
                zoomFactorReadout.setText(sb.toString());
            }
        });

        return panel;

    } // createZoomFactorSliderPanel

    //---------------------------------------------------------------------------------------
    JPanel createRatiosThresholdSliderPanel() {
        double minValue = 0.0;
        double maxValue = 5.0;
        JPanel panel = new JPanel();
        JButton hideButton;
        JSlider slider;

        int sliderMin = (int) (1000 * minValue);
        int sliderMax = (int) (1000 * maxValue);
        int delta = (sliderMax - sliderMin) / 10;
        ratioThreshold = 0.1;
        int initialValue = (int) (1000 * ratioThreshold);
        slider = new JSlider(sliderMin, sliderMax, initialValue);
        StringBuffer sb = decimalFormatter.format(ratioThreshold,
                new StringBuffer(), new FieldPosition(2));
        ratioThresholdReadout = new JTextField(sb.toString(), 6);

        JButton selectButton = new JButton("Select Above");
        selectButton.setToolTipText("Select tiles with log 10 ratio > slider value");

        selectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cw.setInteractivity(false);
                selectTilesAboveCurrentRatioThreshold();
                cw.setInteractivity(true);
            }
        });

        panel.add(ratioThresholdReadout);
        panel.add(slider);
        panel.add(selectButton);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                ratioThreshold = (double) (slider.getValue() / 1000.0);
                StringBuffer sb = decimalFormatter.format(ratioThreshold, new StringBuffer(),
                        new FieldPosition(4));
                ratioThresholdReadout.setText(sb.toString());
            }
        });

        return panel;

    } // createRatiosThresholdSliderPanel

    //---------------------------------------------------------------------------------------
    JPanel createLambdaThresholdSliderPanel() {
        double minValue = 0.0;
        double maxValue = 200.0;
        JPanel panel = new JPanel();
        JButton hideButton;
        JSlider slider;

        int sliderMin = (int) (1000 * minValue);
        int sliderMax = (int) (1000 * maxValue);
        int delta = (sliderMax - sliderMin) / 10;
        lambdaThreshold = 15.0;
        int initialValue = (int) (1000 * lambdaThreshold);
        slider = new JSlider(sliderMin, sliderMax, initialValue);
        StringBuffer sb = decimalFormatter.format(lambdaThreshold,
                new StringBuffer(), new FieldPosition(2));
        lambdaThresholdReadout = new JTextField(sb.toString(), 6);

        JButton selectButton = new JButton("Select Above");
        selectButton.setToolTipText("Select tiles with lambda > slider value");

        selectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectTilesAboveCurrentLambdaThreshold();
            }
        });

        panel.add(lambdaThresholdReadout);
        panel.add(slider);
        panel.add(selectButton);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                lambdaThreshold = (double) (slider.getValue() / 1000.0);
                StringBuffer sb = decimalFormatter.format(lambdaThreshold, new StringBuffer(),
                        new FieldPosition(2));
                lambdaThresholdReadout.setText(sb.toString());
            }
        });

        return panel;

    } // createLambdaThresholdSliderPanel

    //---------------------------------------------------------------------------------------
    JPanel createGeneFindingDistanceSliderPanel() {
        JPanel panel = new JPanel();
        JButton hideButton;
        JSlider slider;

        int sliderMin = 1;
        int sliderMax = 1000;
        slider = new JSlider(sliderMin, sliderMax, geneFindingDistance);

        geneFindingDistanceReadout = new JTextField(new Integer(geneFindingDistance).toString(), 6);

        panel.add(geneFindingDistanceReadout);
        panel.add(slider);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                geneFindingDistance = slider.getValue();
                geneFindingDistanceReadout.setText(new Integer(geneFindingDistance).toString());
            }
        });

        return panel;

    } // createGeneFindingDistanceSliderPanel

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
        motifPValsReadout = new JTextField(sb.toString(), 6);
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
        residualsReadout = new JTextField(sb.toString(), 6);
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
                    "TileViewer: select regulators",
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
                    "TileViewer: select regulators",
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
        // System.out.println (" ---------- " + myGaggleName + ".handleNameList, " + names.length + " names");
        boolean clearSelectionFirst = false;
        cw.selectNodesByName(nameList.getNames(), clearSelectionFirst);

    } // handleNameList

    //-------------------------------------------------------------------------------------
    public void handleMatrix(String source, DataMatrix matrix) throws RemoteException {
        String[] geneNames = matrix.getRowTitles();
        handleNameList(myGaggleName, new Namelist(species, geneNames));
    }
//-------------------------------------------------------------------------------------

    public void handleTuple(String source, GaggleTuple gaggleTuple) {
         // todo tuple fix
        /*
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

    //-------------------------------------------------------------------------------------
    public void handleCluster(
            String source, Cluster cluster) throws RemoteException {
        handleNameList(myGaggleName, new Namelist(species, cluster.getRowNames()));
    }

    //---------------------------------------------------------------------------------------
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

        MiscUtil.setJFrameAlwaysOnTop(frame, true);
        frame.setVisible(true);
        MiscUtil.setJFrameAlwaysOnTop(frame, false);

    } // show

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
        Namelist nameList = new Namelist();
        nameList.setSpecies(species);
        String[] selectedNodeNames = getSelection();
        nameList.setNames(selectedNodeNames);

        try {
            gaggleBoss.broadcastNamelist(myGaggleName, targetGoose, nameList);
        }
        catch (RemoteException rex) {
            System.err.println("error rmi selecting at boss, from " + myGaggleName);
        }

    } // doBroadcastList

    //-------------------------------------------------------------------------------------
    public void doExit() {
        connector.disconnectFromGaggle(true);
        // last window in a jvm, when disposed, cause jvm to exit
        cw.getMainFrame().dispose();

    } // exit

    //---------------------------------------------------------------------------------------
    public String[] getCommands() {
        return new String[]{"show", "hide", "terminate"};
    }

    //-------------------------------------------------------------------------------------
    protected JToolBar createGaggleToolBar() {
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

        showGooseButton.setToolTipText("Show selected goose");
        hideGooseButton.setToolTipText("Hide selected goose");
        broadcastListButton.setToolTipText("Broadcast selected nodes to selected goose");

        showGooseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    gaggleBoss.show(targetGoose);
                }
                catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        });

        hideGooseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    gaggleBoss.hide(targetGoose);
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




        gaggleToolbar.add(showGooseButton);
        gaggleToolbar.add(hideGooseButton);
        gaggleToolbar.add(broadcastListButton);



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

        gaggleToolbar.add(connectButton);
        gaggleToolbar.add(disconnectButton);


        return gaggleToolbar;

    } // createGaggleToolBar

    public void update(String[] gooseNames) {
        this.activeGooseNames = gooseNames;
        MiscUtil.updateGooseChooser(gooseChooser, myGaggleName, activeGooseNames);
    }

//-------------------------------------------------------------------------------------
} // class ControlPanel

