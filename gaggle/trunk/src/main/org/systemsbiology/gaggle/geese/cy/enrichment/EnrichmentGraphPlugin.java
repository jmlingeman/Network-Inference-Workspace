// EnrichmentGraphPlugin.java
//------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.cy.enrichment;
//------------------------------------------------------------------------------

import cytoscape.AbstractPlugin;
import cytoscape.CytoscapeWindow;
import cytoscape.GraphObjAttributes;
import cytoscape.undo.UndoableGraphHider;
import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.datatypes.*;
import org.systemsbiology.gaggle.geese.common.GaggleConnectionListener;
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;
import org.systemsbiology.gaggle.geese.common.GooseShutdownHook;
import org.systemsbiology.gaggle.geese.cy.util.NetworkUtil;
import org.systemsbiology.gaggle.util.MiscUtil;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.View;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

//------------------------------------------------------------------------------
public class EnrichmentGraphPlugin extends AbstractPlugin implements Goose, Serializable,
        GaggleConnectionListener  {

    CytoscapeWindow cw;
    String ORIGINAL_GAGGLE_NAME;
    String myGaggleName = "cytoscape";
    String[] activeGooseNames;

    JButton connectButton = new JButton("C");
    JButton disconnectButton = new JButton("D");

    JComboBox gooseChooser;
    String targetGoose = "Boss";
    protected UndoableGraphHider graphHider;
    protected JTextField nodeSelectionTextField;

    protected JTextField pValsReadout;

    protected JTextField mapNameReadout;
    double pValsThreshold = 0.1;
    DecimalFormat decimalFormatter = new DecimalFormat("0.000");

    static final int HIDE_ABOVE = 1;
    static final int HIDE_BELOW = 2;

    RmiGaggleConnector connector = new RmiGaggleConnector(this);
    Boss boss;
    String species;

    //------------------------------------------------------------------------------
    public EnrichmentGraphPlugin(CytoscapeWindow cytoscapeWindow) {
        new GooseShutdownHook(connector);
        connector.addListener(this);
        ToolTipManager.sharedInstance().setInitialDelay(0);
        cw = cytoscapeWindow;
        graphHider = cw.getGraphHider();
        View view = cytoscapeWindow.getGraph().getCurrentView();
        if (view instanceof Graph2DView) {
            Graph2DView g2dv = (Graph2DView) view;
            g2dv.setPaintDetailThreshold(0.0);      // You only need to do this once
        }
        ORIGINAL_GAGGLE_NAME = discoverGooseName(cw.getConfiguration().getArgs());
        myGaggleName = ORIGINAL_GAGGLE_NAME;
        JPanel utilityPanel = cw.getUtilityPanel();
        utilityPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createLoweredBevelBorder()));

        species = cw.getDefaultSpecies();

        try {
            connectToGaggle();
        }catch (Exception ex) {
            ex.printStackTrace();
        }

        utilityPanel.add(createGui());
        MiscUtil.updateGooseChooser(gooseChooser, myGaggleName, activeGooseNames);
        cw.getMainFrame().pack();
        org.systemsbiology.gaggle.util.MiscUtil.placeInCenter(cw.getMainFrame());
    } // ctor

    //------------------------------------------------------------------------------
    protected JPanel createGui() {
        JPanel mainPanel = new JPanel();
        JPanel westPanel = new JPanel();
        JPanel centerPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        mainPanel.add(westPanel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        westPanel.setLayout(new GridLayout(3, 1));
        westPanel.add(createGaggleToolBar());
        westPanel.add(createCytoscapeControls());
        westPanel.add(createSearchBox());
//        westPanel.add(createMapNameReadout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("p value", createHideByPValuePanel());

        if (tabbedPane.getTabCount() > 0)
            mainPanel.add(tabbedPane, BorderLayout.EAST);

        return mainPanel;

    } // createGui

	private JPanel createSearchBox() {
		JPanel panel = new JPanel();

        nodeSelectionTextField = new JTextField(8);
        String msg = "<html>Enter either gene names or node names. <br>" +
                "Any node that contains your query will be selected.<br></html>";
        nodeSelectionTextField.setToolTipText(msg);

        nodeSelectionTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyText(e.getKeyCode()).equals("Enter"))
	                selectNodes();
            }
        });
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectNodes();
            }
        });
		panel.add(nodeSelectionTextField);
		panel.add(searchButton);
		//add search button
		return panel;
	}

	//------------------------------------------------------------------------------
    protected JPanel createMapNameReadout() {
        JPanel panel = new JPanel();
        mapNameReadout = new JTextField(16);
        mapNameReadout.setBackground(Color.GRAY.brighter());
        mapNameReadout.setToolTipText("Condition titles are displayed here.");
        mapNameReadout.setEditable(false);
        panel.add(mapNameReadout);

        return panel;

    } // createMapNameReadout

    //------------------------------------------------------------------------------
    protected JToolBar createGaggleToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);


        gooseChooser = new JComboBox(new String[]{"Boss"});
        gooseChooser.setPrototypeDisplayValue("a very very long goose name");
        gooseChooser.setToolTipText("Specify goose for broadcast");

        toolbar.add(gooseChooser);

        gooseChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
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

        connectButton.setToolTipText("Connect To Gaggle");
        disconnectButton.setToolTipText("Disconnect From Gaggle");

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

        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    connector.connectToGaggle();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(cw, "Error connecting to Gaggle!");
                }
            }
        });


        disconnectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    connector.disconnectFromGaggle(false);
                    myGaggleName = ORIGINAL_GAGGLE_NAME;
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(cw, "Error disconnecting from Gaggle!");
                }
            }
        });


        toolbar.add(showGooseButton);
        toolbar.add(hideGooseButton);
        toolbar.add(broadcastListButton);
        toolbar.add(broadcastNetworkButton);
        toolbar.add(connectButton);
        toolbar.add(disconnectButton);

        return toolbar;

    } // createGaggleToolBar

    //------------------------------------------------------------------------------
    protected JPanel createCytoscapeControls() {
        JPanel cyControlsPanel = new JPanel();
        JToolBar cyToolbar = new JToolBar();
        cyToolbar.setFloatable(false);
        JButton showAllButton = new JButton("U");
        showAllButton.setToolTipText("Unhide all nodes & edges");
        JButton hideOrphansButton = new JButton("O");
        hideOrphansButton.setToolTipText("Hide Orphan Nodes");

        JButton clearSelectionsButton = new JButton("Cl");
        clearSelectionsButton.setToolTipText("Clear Network Selections");

        showAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                graphHider.unhideAll();
                cw.redrawGraph();
            }
        });

        hideOrphansButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                NetworkUtil.hideOrphans(cw, graphHider);
            }
        });

        clearSelectionsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cw.selectNodes(new Node[]{}, true);
            }
        });

        JButton broadcastGenesButton = new JButton("G");
        broadcastGenesButton.setToolTipText(
                "<html>Broadcast all genes annotated to the <br>selected terms.</html>");
        broadcastGenesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                broadcastGenes();
            }
        });

        cyToolbar.add(showAllButton);
        cyToolbar.add(hideOrphansButton);
        cyToolbar.add(clearSelectionsButton);
        cyToolbar.add(broadcastGenesButton);



        cyControlsPanel.add(cyToolbar);

        return cyControlsPanel;

    } // createCytoscapeControls

    //------------------------------------------------------------------------------
    JPanel createHideByPValuePanel() {
        final String attributeName = "pvalue";

        double minValue = 0.0;
        double maxValue = 1.0;
        JPanel panel = new JPanel();
        JButton hideButton;
        JSlider slider;

        int sliderMin = (int) (1000 * minValue);
        int sliderMax = (int) (1000 * maxValue);

        slider = new JSlider(sliderMin, sliderMax);
        pValsThreshold = (maxValue + minValue) / 2.0;
        StringBuffer sb = decimalFormatter.format(pValsThreshold,
                new StringBuffer(), new FieldPosition(4));
        pValsReadout = new JTextField(sb.toString(), 4);
        pValsReadout.setEditable(false);
        hideButton = new JButton("Hide Above");
        hideButton.setToolTipText("Hide edges with pValue < slider value");

        hideButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("hide nodes with pvalue above " + pValsThreshold);
                hideNodesByThreshold(attributeName, pValsThreshold, HIDE_ABOVE);
            }
        });

        panel.add(pValsReadout);
        panel.add(slider);
        panel.add(hideButton);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                pValsThreshold = (double) (slider.getValue() / 1000.0);
                StringBuffer sb = decimalFormatter.format(pValsThreshold, new StringBuffer(),
                        new FieldPosition(4));
                pValsReadout.setText(sb.toString());
            }
        });

        return panel;

    } // createHideByPValuePanel

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
            boolean hideThisOne = false;
            if (value == null)
                hideThisOne = true;
            else
                hideThisOne =
                        (aboveOrBelow == HIDE_ABOVE && value.doubleValue() > threshold) ||
                                (aboveOrBelow == HIDE_BELOW && value.doubleValue() < threshold);
            if (hideThisOne)
                graphHider.hide(node);
        } // for e

        cw.redrawGraph();


    } // hideNodesByThreshold

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

        try {
            String[] selectedNodeNames = getSelection();
            Namelist nameList = new Namelist();
            nameList.setNames(selectedNodeNames);
            nameList.setSpecies(species);
            boss.broadcastNamelist(myGaggleName, targetGoose, nameList);
        }
        catch (RemoteException rex) {
            System.err.println("error rmi selecting at boss, from GaggleLister " + myGaggleName);
        }

    } // doBroadcastList

    //------------------------------------------------------------------------------
    public void doBroadcastNetwork() {
        Network network = NetworkUtil.createNetworkFromSelection(cw);
         
        try {
            boss.broadcastNetwork(myGaggleName, targetGoose, network);
        }
        catch (RemoteException rex) {
            String msg = "gaggle error, in enrichmentGraphPlugin.doBroadcastNetwork " + myGaggleName + ": " +
                    rex.getMessage();
            System.err.println(msg);
            rex.printStackTrace();
        }

    } // doBroadcastNetwork

    //------------------------------------------------------------------------------
/**
 * find all the genes in each selected node, make a list, broadcast them
 */
    protected void broadcastGenes() {
        NodeCursor nc = cw.getGraph().selectedNodes();
        ArrayList geneList = new ArrayList();
        GraphObjAttributes nodeAttributes = cw.getNodeAttributes();
        while (nc.ok()) {
            Node node = nc.node();
            String canonicalName = cw.getCanonicalNodeName(node);
            Object[] arrayValues  = nodeAttributes.getArrayValues("genes", canonicalName);
            for (Object o :arrayValues) {
                if (!geneList.contains(o.toString())) {
                    geneList.add(o.toString());
                }
            }
/*
            String rawGeneString = nodeAttributes.getStringValue("genes", canonicalName);
            if (rawGeneString != null) {
                String[] tokens = rawGeneString.split(",");
                for (int i = 0; i < tokens.length; i++)
                    if (!geneList.contains(tokens[i]))
                        geneList.add(tokens[i].trim());
            } // if !null
*/
            nc.next();
        } // while

        if (geneList.size() == 0)
            return;

        String[] geneNames = (String[]) geneList.toArray(new String[0]);
        Namelist nameList = new Namelist();
        nameList.setSpecies(species);
        nameList.setNames(geneNames);
        try {
            boss.broadcastNamelist(myGaggleName, targetGoose, nameList);
        }
        catch (RemoteException rex) {
            System.err.println("list broadcast error in " + myGaggleName);
            rex.printStackTrace();
        }

    } // broadcastGenes

    //--------------------------------------------------------------------------------------------
    public synchronized void handleNameList(String source, Namelist nameList) throws RemoteException {
        boolean clearSelectionFirst = false;
        System.out.println("GO goose, handleNameList -----");
        for (int i = 0; i < nameList.getNames().length; i++)
            System.out.println("    " + nameList.getNames()[i]);

        selectNodesByName(nameList.getNames());
        //cw.selectNodesByName (names, clearSelectionFirst);

    } // handleNameList

    //-------------------------------------------------------------------------------------
    protected String[] getCanonicalNamesOfNodesInGraph() {
        ArrayList tmp = new ArrayList();
        Node[] nodes = cw.getGraph().getNodeArray();
        for (int i = 0; i < nodes.length; i++) {
            String canonicalName = cw.getCanonicalNodeName(nodes[i]);
//            String canonicalName = cw.getNodeName(nodes[i]);
            if (canonicalName == null || canonicalName.equals("null"))
                continue;
            tmp.add(canonicalName);
        }

        return (String[]) tmp.toArray(new String[0]);
    }


    //-----------------------------------------------------------------------------------
    protected void selectNodesByName(String[] candidates) {
        String[] nodeNames = getCanonicalNamesOfNodesInGraph();
        System.out.println("---- canonical names in graph: " + nodeNames.length);
        ArrayList nodeNamesToSelect = new ArrayList();

        for (int n = 0; n < nodeNames.length; n++) {
            System.out.println(n + "> " + "checking candidates against genes in " + nodeNames[n]);
            String commonName = cw.getNodeAttributes().getStringValue("commonName", nodeNames[n]);//getNodeName(nodeNames[n]);
	        String [] geneNames= cw.getNodeAttributes().getStringArrayValues ("genes", nodeNames[n]);

	       Arrays.sort(geneNames);
            for (int c = 0; c < candidates.length; c++) {
                System.out.print("     " + c + "> candidate: " + candidates[c] + ": ");
                if (Arrays.binarySearch(geneNames, candidates[c]) >= 0 || commonName.matches("(?i).*" + candidates[c] + ".*")) {
                    if (!nodeNamesToSelect.contains(nodeNames[n]))
                        nodeNamesToSelect.add(nodeNames[n]);
                    System.out.println("     match");
                } else {
                    System.out.println("     none");
                }
            } //for c
        } // for n

        System.out.println("count of genes to select: " + nodeNamesToSelect.size());
        String[] matchedNodeNames = (String[]) nodeNamesToSelect.toArray(new String[0]);
        System.out.println("count of genes to select, 2: " + matchedNodeNames.length);
        for (int i = 0; i < matchedNodeNames.length; i++)
            System.out.println(" select " + matchedNodeNames[i]);
	    cw.deselectAllNodes();
        cw.selectNodesByName(matchedNodeNames, false);

    } // selectNodesByName

    //-----------------------------------------------------------------------------------
    public void handleMatrix(String source, DataMatrix matrix) throws RemoteException {
        String[] geneNames = matrix.getRowTitles();
        handleNameList(myGaggleName, new Namelist(species, geneNames));
    }

    //-------------------------------------------------------------------------------------
    protected void selectNodes() {
        String rawTextFieldContents = nodeSelectionTextField.getText().trim();
        String[] candidates = rawTextFieldContents.split(" ");
        selectNodesByName(candidates);
    }
//-----------------------------------------------------------------------------------
//protected void selectNodesByName (String [] candidates)
//{
//  String [] nodeNames = getCanonicalNamesOfNodesInGraph ();
//  NodeNameMatcher nameMatcher =  
//     new NodeNameMatcher (nodeNames,  null, cw.getBioDataServer (),
//                          cw.getDefaultSpecies ());
//
//  String [] matchedNodes = nameMatcher.getMatch (candidates);
//
//  //System.out.println ("------------ match count: " + matchedNodes.length);
//  //for (int i=0; i < matchedNodes.length; i++) 
//  //  System.out.println ("    " + matchedNodes [i]);
//
//  cw.selectNodesByName (matchedNodes, false);
//
//} // selectNodesByName
////-------------------------------------------------------------------------------------
//protected String [] getCanonicalNamesOfNodesInGraph ()
//{
//  ArrayList tmp = new ArrayList ();
//  Node [] nodes = cw.getGraph().getNodeArray ();
//  for (int i=0; i < nodes.length; i++) {
//    String canonicalName = cw.getCanonicalNodeName (nodes [i]);
//    if (canonicalName == null || canonicalName.equals ("null"))
//      continue;
//    tmp.add (canonicalName);
//    }
//
//  return (String []) tmp.toArray (new String [0]);

    //}
//-----------------------------------------------------------------------------------
    public void handleTuple(String source, GaggleTuple gaggleTuple) {
     // todo tuple fix
    /*
        System.out.println(" ------ EnrichmentGraphPlugin.handleMap: " + attributeMap.getName() + "  size: " +
                AttributeMapUtil.getSize(attributeMap));
        mapNameReadout.setText(attributeMap.getName());
        String[] attributeNames = AttributeMapUtil.getAttributeNames(attributeMap);
        GraphObjAttributes goa = cw.getNodeAttributes();
        for (int i = 0; i < attributeNames.length; i++) {
            String attributeName = attributeNames[i];
            Map<String, Object> map = AttributeMapUtil.getKeyValuePairsForAttribute(attributeMap, attributeName);
            AttributeMapUtil.DataType dataType = AttributeMapUtil.getDataType(attributeMap);
            for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
                String key = it.next();
                if (dataType.equals(AttributeMapUtil.DataType.DOUBLE)) {
                    double value = (Double) map.get(key);
                    if (Double.isNaN(value)) {
                        value = 0.0;
                    }
                    goa.set(attributeName, key, value);
                } else if (dataType.equals(AttributeMapUtil.DataType.INT)) {
                    int value = (Integer) map.get(key);
                    goa.set(attributeName, key, value);
                } else if (dataType.equals(AttributeMapUtil.DataType.STRING)) {
                    String value = (String) map.get(key);
                    goa.set(attributeName, key, value);
                }
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
            //System.out.println ("adding node named " + name + " to hash");
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
    protected HashSet getCurrentEdgeNames(Graph2D graph) {
        //ArrayList list = new ArrayList ();
        HashSet list = new HashSet();
        for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
            Edge edge = ec.edge();
            String canonicalName = cw.getEdgeAttributes().getCanonicalName(edge);
            list.add(canonicalName);
        } // for ec

        return list;

    } // getCurrentEdgeNames

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
    public void doExit() {
        connector.disconnectFromGaggle(true);
        cw.getMainFrame().dispose();  // the last frame disposed of in a jvm
        // causes the jvm to exit

    } // exit

    //-------------------------------------------------------------------------------------
    public void connectToGaggle() {
        try {
            connector.connectToGaggle();
        }
        catch (Exception ex0) {
            String msg = "Could not connect to gaggle: " + ex0.getMessage();
            JOptionPane.showMessageDialog(cw.getMainFrame(), msg);
        }
        boss = connector.getBoss();
    } // connectToGaggle

    public void update(String[] gooseNames) {
        activeGooseNames  = gooseNames;
        MiscUtil.updateGooseChooser(gooseChooser, myGaggleName, activeGooseNames);
    }

    public void setConnected(boolean connected, Boss boss) {
        if (connected) {
            if (boss != null) {
                this.boss = boss;
            }
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
        } else {
            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
        }
    }

    //-------------------------------------------------------------------------------------
} // enrichmentGraphPlugin
