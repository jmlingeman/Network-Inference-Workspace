// ControlPanel
//---------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.cy.ip;
//---------------------------------------------------------------------------------------

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;


import java.rmi.*;

import java.util.*;
import java.text.*;

import org.systemsbiology.gaggle.geese.cy.util.*;
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
    protected JToolBar toolbar;

    protected JButton connectButton;
    protected JButton disconnectButton;

    protected GridBagLayout gridbagLayout;
    protected JTextField nodeSelectionTextField;

    //----------------------------------------
    // slider variables
    //----------------------------------------
    protected JTextField probabilityThresholdReadout;
    protected double probabilityThreshold;

    protected JTextField peptideMinCountThresholdReadout;
    protected int peptideMinCountThreshold;
    protected JTextField peptideMaxCountThresholdReadout;
    protected int peptideMaxCountThreshold;

    protected CytoscapeWindow cw;
    protected BioDataServer dataServer;
    protected UndoableGraphHider graphHider;
    String myGaggleName = "IP";

    Boss gaggleBoss;
    RmiGaggleConnector connector = new RmiGaggleConnector(this);

    JComboBox gooseChooser;
    String targetGoose = "Boss";

    JPopupMenu gagglePopupMenu;

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
        JPanel westPanel = new JPanel();
        JPanel centerPanel = new JPanel();
        outerPanel.add(westPanel, BorderLayout.WEST);
        outerPanel.add(centerPanel, BorderLayout.CENTER);
        westPanel.setLayout(new GridLayout(2, 1));

        westPanel.add(createGaggleToolBar());

        JPanel lowerWestPanel = new JPanel();
        lowerWestPanel.add(createLocalToolBar());

        westPanel.add(lowerWestPanel);
        JButton selectNodesButton = new JButton("Select: ");
        selectNodesButton.setToolTipText("Select nodes by name.");
        selectNodesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectNodes();
            }
        });

        nodeSelectionTextField = new JTextField(8);
        nodeSelectionTextField.setToolTipText("Select nodes by name.");
        nodeSelectionTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                //System.out.println("Key pressed: " + KeyEvent.getKeyText(e.getKeyCode()));
                //System.out.println("Code = " + e.getKeyCode());
                //System.out.println("Char = " + e.getKeyChar());
                if (e.getKeyText(e.getKeyCode()).equals("Enter") || (e.getKeyCode() == 10)) selectNodes();
            }
        });

        lowerWestPanel.add(nodeSelectionTextField);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Hide by Probability", createHideByProbabilityPanel());
        tabbedPane.add("Hide by Min Peptides", createHideByMinPeptidesPanel());
        tabbedPane.add("Hide by Max Peptides", createHideByMaxPeptidesPanel());

        centerPanel.add(tabbedPane);

        MiscUtil.updateGooseChooser(gooseChooser, myGaggleName, activeGooseNames);

        return outerPanel;

    } // createGui

    //-----------------------------------------------------------------------------------
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
        JButton broadcastNetworkButton = new JButton("N");

        showGooseButton.setToolTipText("Show selected goose");
        hideGooseButton.setToolTipText("Hide selected goose");
        broadcastListButton.setToolTipText("Broadcast selected nodes to selected goose");
        broadcastNetworkButton.setToolTipText("Broadcast network to selected goose");

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

        broadcastNetworkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doBroadcastNetwork();
            }
        });

        gaggleToolbar.add(showGooseButton);
        gaggleToolbar.add(hideGooseButton);
        gaggleToolbar.add(broadcastListButton);
        gaggleToolbar.add(broadcastNetworkButton);

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

    //-------------------------------------------------------------------------------------
    protected JToolBar createLocalToolBar() {
        toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton showAllButton = new JButton("Show");
        showAllButton.setToolTipText("Show all nodes and edges.");
        showAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showAll();
            }
        });

        JButton clearSelectionsButton = new JButton("Cl");
        clearSelectionsButton.setToolTipText("Clear network selections");
        clearSelectionsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cw.selectNodes(new Node[]{}, true);
            }
        });

        JButton hideProteinAPreyButton = new JButton("HpA");
        hideProteinAPreyButton.setToolTipText("Hide Protein A prey unless they are also bait");
        hideProteinAPreyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hideProteinAPrey();
            }
        });

        JButton hideOrphansButton = new JButton("HO");
        hideOrphansButton.setToolTipText("Hide orphan nodes");
        hideOrphansButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hideOrphans();
            }
        });

        toolbar.add(showAllButton);
        toolbar.add(hideProteinAPreyButton);
        toolbar.add(hideOrphansButton);
        toolbar.add(clearSelectionsButton);

        return toolbar;

    } // createLocalToolBar

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

        String[] candidates = rawTextFieldContents.split(" ");
        HashMap clusteredGenesMap = new HashMap();

        String[] nodeNames = getCanonicalNamesOfNodesInGraph();
        NodeNameMatcher nameMatcher =
                new NodeNameMatcher(nodeNames, clusteredGenesMap, cw.getBioDataServer(),
                        cw.getDefaultSpecies());

        String[] matchedNodes = nameMatcher.getMatch(candidates);

        //System.out.println ("------------ match count: " + matchedNodes.length);
        //for (int i=0; i < matchedNodes.length; i++)
        //  System.out.println ("    " + matchedNodes [i]);

        cw.selectNodesByName(matchedNodes, false);

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
    public void showAll() {
        graphHider.unhideAll();
        cw.redrawGraph();
    }

    //-----------------------------------------------------------------------------------
    protected void hideOrphans() {

        Node[] nodes = cw.getGraph().getNodeArray();
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].degree() == 0)
                graphHider.hide(nodes[i]);
        } // for i

        cw.redrawGraph();

    } // hideOrphans

    //-----------------------------------------------------------------------------------
    String[] getAllSourceNodeNames() {
        HashSet tmp = new HashSet();
        GraphObjAttributes na = cw.getNodeAttributes();
        Edge[] edges = cw.getGraph().getEdgeArray();

        for (int e = 0; e < edges.length; e++) {
            Node sourceNode = edges[e].source();
            String name = na.getCanonicalName(sourceNode);
            tmp.add(name);
        } // for e

        return (String[]) tmp.toArray(new String[0]);

    } // getAllSourceNodes

    //-----------------------------------------------------------------------------------
/**
 * lots of things stick to Protein A.  hide them all unless they, too,
 * happen to have been bait, in their own right
 */
    protected void hideProteinAPrey() {

        String[] sourceNodeNames = getAllSourceNodeNames();
        Arrays.sort(sourceNodeNames);
        GraphObjAttributes na = cw.getNodeAttributes();

        Edge[] edges = cw.getGraph().getEdgeArray();
        for (int e = 0; e < edges.length; e++) {
            Node sourceNode = edges[e].source();
            String sourceName = na.getCanonicalName(sourceNode);
            if (sourceName.equalsIgnoreCase("ProteinA")) {
                Node proteinAPreyNode = edges[e].target();
                String preyName = na.getCanonicalName(proteinAPreyNode);
                if (Arrays.binarySearch(sourceNodeNames, preyName) < 0)
                    graphHider.hide(proteinAPreyNode);
            } // if
        } // for e

        cw.redrawGraph();

    } // hideProteinAPrey

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
        zoomFactor = 0.1;
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
    JPanel createHideByProbabilityPanel() {
        final String attributeName = "protein probability";
        double[] probabilities = getDoubleEdgeAttributes(attributeName);
        if (probabilities == null || probabilities.length == 0)
            return new JPanel();

        Arrays.sort(probabilities);
        double minValue = probabilities[0];
        double maxValue = probabilities[probabilities.length - 1];
        JPanel panel = new JPanel();
        JButton hideButton;
        JSlider slider;

        int sliderMin = (int) (1000 * minValue);
        int sliderMax = (int) (1000 * maxValue);
        int delta = (sliderMax - sliderMin) / 10;
        sliderMin -= delta;
        sliderMax += delta;
        slider = new JSlider(sliderMin, sliderMax);
        probabilityThreshold = (maxValue + minValue) / 2.0;
        StringBuffer sb = decimalFormatter.format(probabilityThreshold,
                new StringBuffer(), new FieldPosition(4));
        probabilityThresholdReadout = new JTextField(sb.toString(), 4);

        hideButton = new JButton("Hide Below");
        hideButton.setToolTipText("Hide edges with probability < slider value");
        hideButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hideEdgesByAttribute(attributeName, probabilityThreshold, "min");
            }
        });

        panel.add(probabilityThresholdReadout);
        panel.add(slider);
        panel.add(hideButton);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                probabilityThreshold = (double) (slider.getValue() / 1000.0);
                StringBuffer sb = decimalFormatter.format(probabilityThreshold, new StringBuffer(),
                        new FieldPosition(4));
                probabilityThresholdReadout.setText(sb.toString());
            }
        });

        return panel;

    } // createHideByProbabilityPanel

    //---------------------------------------------------------------------------------------
    JPanel createHideByMinPeptidesPanel() {
        final String attributeName = "peptide count";
        int[] peptideCounts = getIntegerEdgeAttributes(attributeName);
        if (peptideCounts == null || peptideCounts.length == 0)
            return new JPanel();

        JPanel panel = new JPanel();
        JButton hideButton;
        JSlider slider;

        Arrays.sort(peptideCounts);
        int sliderMin = peptideCounts[0];
        int sliderMax = peptideCounts[peptideCounts.length - 1];
        slider = new JSlider(sliderMin, sliderMax);
        peptideMinCountThreshold = (sliderMax - sliderMin) / 2;
        peptideMinCountThresholdReadout =
                new JTextField(new Integer(peptideMinCountThreshold).toString(), 4);

        hideButton = new JButton("Hide Below");
        hideButton.setToolTipText("Hide edges with peptide count < slider value");
        hideButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hideEdgesByAttribute(attributeName, peptideMinCountThreshold, "min");
            }
        });

        panel.add(peptideMinCountThresholdReadout);
        panel.add(slider);
        panel.add(hideButton);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                peptideMinCountThreshold = slider.getValue();
                peptideMinCountThresholdReadout.setText(new Integer(peptideMinCountThreshold).toString());
            }
        });

        return panel;

    }

    //---------------------------------------------------------------------------------------
    JPanel createHideByMaxPeptidesPanel() {
        final String attributeName = "peptide count";
        int[] peptideCounts = getIntegerEdgeAttributes(attributeName);
        if (peptideCounts == null || peptideCounts.length == 0)
            return new JPanel();

        JPanel panel = new JPanel();
        JButton hideButton;
        JSlider slider;

        Arrays.sort(peptideCounts);
        int sliderMin = peptideCounts[0];
        int sliderMax = peptideCounts[peptideCounts.length - 1];
        slider = new JSlider(sliderMin, sliderMax);
        peptideMaxCountThreshold = (sliderMax - sliderMin) / 2;
        peptideMaxCountThresholdReadout =
                new JTextField(new Integer(peptideMaxCountThreshold).toString(), 4);

        hideButton = new JButton("Hide Above");
        hideButton.setToolTipText("Hide edges with peptide count >= slider value");
        hideButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hideEdgesByAttribute(attributeName, peptideMaxCountThreshold, "max");
            }
        });

        panel.add(peptideMaxCountThresholdReadout);
        panel.add(slider);
        panel.add(hideButton);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                peptideMaxCountThreshold = slider.getValue();
                peptideMaxCountThresholdReadout.setText(new Integer(peptideMaxCountThreshold).toString());
            }
        });

        return panel;

    }

    //---------------------------------------------------------------------------------------
    public void hideEdgesByAttribute(String attributeName, double threshold, String minOrMax) {
        Edge[] edges = cw.getGraph().getEdgeArray();
        GraphObjAttributes ea = cw.getEdgeAttributes();

        ArrayList edgesToHide = new ArrayList();
        ArrayList hiddenEdgeTargetNodes = new ArrayList();

        for (int e = 0; e < edges.length; e++) {
            Edge edge = edges[e];
            Node sourceNode = edge.source();
            Node targetNode = edge.target();
            String edgeName = ea.getCanonicalName(edge);
            Double weight = ea.getDoubleValue(attributeName, edgeName);
            if (weight == null)
                continue;
            boolean hideThisEdge = false;
            hideThisEdge =
                    (minOrMax.equalsIgnoreCase("min") && weight.doubleValue() < threshold) ||
                            (minOrMax.equalsIgnoreCase("max") && weight.doubleValue() > threshold);

            if (hideThisEdge) {
                graphHider.hide(edge);
                //EdgeCursor edgeCursor = sourceNode.inEdges ();
                //while (edgeCursor.ok ()) {
                //  graphHider.hide (edgeCursor.edge ());
                //  edgeCursor.next ();
                //  } // while
            } // if
        } // for e

        cw.redrawGraph();

    } // hideEdgesByAttribute (double)

    //---------------------------------------------------------------------------------------
    public void hideEdgesByAttribute(String attributeName, int threshold, String minOrMax) {
        Edge[] edges = cw.getGraph().getEdgeArray();
        GraphObjAttributes ea = cw.getEdgeAttributes();

        ArrayList edgesToHide = new ArrayList();
        ArrayList hiddenEdgeTargetNodes = new ArrayList();

        for (int e = 0; e < edges.length; e++) {
            Edge edge = edges[e];
            Node sourceNode = edge.source();
            Node targetNode = edge.target();
            String edgeName = ea.getCanonicalName(edge);
            Integer weight = ea.getIntegerValue(attributeName, edgeName);
            if (weight == null)
                continue;
            boolean hideThisEdge = false;
            hideThisEdge =
                    (minOrMax.equalsIgnoreCase("min") && weight.intValue() < threshold) ||
                            (minOrMax.equalsIgnoreCase("max") && weight.intValue() > threshold);

            if (hideThisEdge) {
                graphHider.hide(edge);
                //EdgeCursor edgeCursor = sourceNode.inEdges ();
                //while (edgeCursor.ok ()) {
                //  graphHider.hide (edgeCursor.edge ());
                //  edgeCursor.next ();
                //  } // while
            } // if
        } // for e

        cw.redrawGraph();

    } // hideEdgesByAttribute (int)

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
                        (aboveOrBelow == HIDE_ABOVE && value.doubleValue() > threshold) ||
                                (aboveOrBelow == HIDE_BELOW && value.doubleValue() < threshold);
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
    public int[] getIntegerEdgeAttributes(String attributeName) {
        ArrayList tmp = new ArrayList();
        Edge[] edgeArray = cw.getGraph().getEdgeArray();
        GraphObjAttributes ea = cw.getEdgeAttributes();
        for (int i = 0; i < edgeArray.length; i++) {
            Edge edge = edgeArray[i];
            Integer weight = ea.getIntegerValue(attributeName, ea.getCanonicalName(edge));
            if (weight != null)
                tmp.add(weight);
        }

        int[] result = new int[tmp.size()];
        for (int i = 0; i < tmp.size(); i++) {
            result[i] = ((Integer) tmp.get(i)).intValue();
        }

        return result;

    }  // getIntegerEdgeAttrributes

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
        cw.selectNodesByName(nameList.getNames(), clearSelectionFirst);

    } // select

    //-------------------------------------------------------------------------------------
    public void handleMatrix(String source, DataMatrix matrix) throws RemoteException {
        String[] geneNames = matrix.getRowTitles();
        handleNameList(myGaggleName, new Namelist(species, geneNames));
    }
//-------------------------------------------------------------------------------------

    public void handleTuple(String source, GaggleTuple gaggleTuple) {
        // todo tuple fix
        /*
        String[] attributeNames = AttributeMapUtil.getAttributeNames(null);
        GraphObjAttributes goa = cw.getNodeAttributes();
        for (int i = 0; i < attributeNames.length; i++) {
            String attributeName = attributeNames[i];
            Map<String, Object> map = AttributeMapUtil.getKeyValuePairsForAttribute(null, attributeName);
            for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
                String key = it.next();
                double value = (Double) map.get(key);
                if (Double.isNaN(value)) {
                    value = 0.0;
                }
                goa.set(attributeName, key, value);
            }
        } // for i

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
        NetworkUtil.extend(cw, network);
        cw.redrawGraph(true);
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
            gaggleBoss.broadcastNamelist(myGaggleName, targetGoose, nameList);
        }
        catch (RemoteException rex) {
            System.err.println("error rmi selecting at boss, from " + myGaggleName);
        }

    } // doBroadcast

    //-------------------------------------------------------------------------------------
    public void doBroadcastNetwork() {
        org.systemsbiology.gaggle.core.datatypes.Network network = NetworkUtil.createNetworkFromSelection(cw);

        try {
            gaggleBoss.broadcastNetwork(myGaggleName, targetGoose, network);
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
        cw.getMainFrame().dispose();
    }

    public void update(String[] gooseNames) {
        this.activeGooseNames = gooseNames;
        MiscUtil.updateGooseChooser(gooseChooser, myGaggleName, activeGooseNames);
    }


    //---------------------------------------------------------------------------------------
    public String[] getCommands() {
        return new String[]{"show", "hide", "terminate"};
    }
//-------------------------------------------------------------------------------------
} // class ControlPanel

