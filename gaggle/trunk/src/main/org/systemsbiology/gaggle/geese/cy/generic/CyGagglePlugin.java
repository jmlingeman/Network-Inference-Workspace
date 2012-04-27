// CyGagglePlugin.java
//------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.cy.generic;
//------------------------------------------------------------------------------

import cytoscape.*;
import cytoscape.undo.UndoableGraphHider;

import java.rmi.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;

import y.base.*;
import y.view.*;


import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.util.*;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.datatypes.*;
import org.systemsbiology.gaggle.geese.cy.util.*;
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;
import org.systemsbiology.gaggle.geese.common.GooseShutdownHook;
import org.systemsbiology.gaggle.geese.common.GaggleConnectionListener;

//------------------------------------------------------------------------------
public class CyGagglePlugin extends AbstractPlugin implements Goose, java.io.Serializable,
        GaggleConnectionListener {

    CytoscapeWindow cw;
    Graph2DView g2dView;
    Graph2D g2d;

    String myGaggleName = "cytoscape";
    String[] activeGooseNames = new String[0];
    protected JButton incomingNameListModeButton; // either "Create Nodes" or "Select Nodes"
    JComboBox gooseChooser;
    String targetGoose = "Boss";
    protected UndoableGraphHider graphHider;
    protected JTextField nodeSelectionTextField;

    protected JTextField mapNameReadout;

    Boss boss;
    RmiGaggleConnector connector = new RmiGaggleConnector(this);
    String species;

    JButton connectButton;
    JButton disconnectButton;

//------------------------------------------------------------------------------
public CyGagglePlugin(CytoscapeWindow cytoscapeWindow) 
{
  new GooseShutdownHook (connector); // register a shutdown hook to disconnect from boss
  ToolTipManager.sharedInstance().setInitialDelay(0);
  cw = cytoscapeWindow;
  graphHider = cw.getGraphHider();
  View view = cytoscapeWindow.getGraph().getCurrentView();
  if (view instanceof Graph2DView) {
    g2dView = (Graph2DView) view;
    g2dView.setPaintDetailThreshold(0.0);      // You only need to do this once
    g2d = g2dView.getGraph2D ();
    }

  myGaggleName = discoverGooseName(cw.getConfiguration().getArgs());
  JPanel utilityPanel = cw.getUtilityPanel();
  utilityPanel.setBorder(BorderFactory.createCompoundBorder (BorderFactory.createRaisedBevelBorder(),
                                                             BorderFactory.createLoweredBevelBorder()));
  species = cw.getDefaultSpecies();
  try {
   connectToGaggle();
   }
 catch (Exception ex) {
   ex.printStackTrace();
   }

  utilityPanel.add(createGui());
  updateGooseChooser();
  cw.getMainFrame().pack();
  org.systemsbiology.gaggle.util.MiscUtil.placeInCenter(cw.getMainFrame());
  connector.addListener(this);

} // ctor
//------------------------------------------------------------------------------
    protected JPanel createGui() {
        JPanel mainPanel = new JPanel();
        JPanel westPanel = new JPanel();
        JPanel centerPanel = new JPanel();
        JPanel lowerControlsPanel = new JPanel ();
        mainPanel.setLayout(new BorderLayout());

        mainPanel.add(westPanel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        westPanel.setLayout(new GridLayout(2, 1));
        westPanel.add(createGaggleToolBar());
        lowerControlsPanel.add(createCytoscapeControls());
        lowerControlsPanel.add(createMapNameReadout());
        westPanel.add (lowerControlsPanel);

        return mainPanel;

    } // createGui

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

        JButton getGeeseNamesButton = new JButton("Update");
        getGeeseNamesButton.setToolTipText("Update Goose List");
        //toolbar.add(getGeeseNamesButton);
        getGeeseNamesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MiscUtil.updateGooseChooserOLD(boss, gooseChooser, myGaggleName, null);
            }
        });

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

        connectButton = new JButton("Connect");
        connectButton.setToolTipText("Connect to Boss");
        connectButton.setEnabled(!connector.isConnected());
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    connector.connectToGaggle();
                    connectButton.setEnabled(false);
                    disconnectButton.setEnabled(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(cw.getMainFrame(),
                            "Could not connect to boss -- is the boss running?");
                }
            }
        });

        disconnectButton = new JButton("Disconnect");
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
        incomingNameListModeButton = new JButton ("Select Nodes");
        String msg = "<html>Incoming name list broadcasts " +
               " either select<br> or create nodes.</html>";
        incomingNameListModeButton.setToolTipText (msg);
        incomingNameListModeButton.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent e) {
				  if (incomingNameListModeButton.getLabel().equalsIgnoreCase ("Select Nodes"))
					incomingNameListModeButton.setLabel ("Create Nodes");
				  else
					incomingNameListModeButton.setLabel ("Select Nodes");
			 }});

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

		cyToolbar.add(incomingNameListModeButton);
        cyToolbar.add(showAllButton);
        cyToolbar.add(hideOrphansButton);
        cyToolbar.add(clearSelectionsButton);

        nodeSelectionTextField = new JTextField(8);
        String tooltip = "<html>Enter node names here, using common <br>" +
                "or cannonical names, upper or lower case <br>" +
                " &nbsp; '*' &nbsp; at end of name matches all.</html>";
        nodeSelectionTextField.setToolTipText(tooltip);

        nodeSelectionTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (KeyEvent.getKeyText(e.getKeyCode()).equals("Enter")) selectNodes();
            }
        });

        cyControlsPanel.add(cyToolbar);
        cyControlsPanel.add(nodeSelectionTextField);

        return cyControlsPanel;

    } // createCytoscapeControls

    //------------------------------------------------------------------------------
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
            list.add(canonicalName);
        } // for nc

        return (String[]) list.toArray(new String[0]);

    } // getSelection

    //-------------------------------------------------------------------------------------
    public void doBroadcastList() {
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

    //------------------------------------------------------------------------------
    public void doBroadcastNetwork() {
        org.systemsbiology.gaggle.core.datatypes.Network network = NetworkUtil.createNetworkFromSelection(cw);
        System.out.println ("CyGagglePlugin.doBroadcastNetwork, noa count: " + network.getNodeAttributeNames().length );

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
public synchronized void handleNameList (String source, Namelist nameList) throws RemoteException 
{
  GraphObjAttributes nodeAttributes = cw.getNodeAttributes ();
  String [] names = nameList.getNames ();
  if (names.length > 1 && names [0].equalsIgnoreCase ("vizrule")) {  // extract the command, and try to execute it
     // expected:  broadcast (c ('vizrule', 'node label', 'label'))
    if (names.length == 3 && names [1].equalsIgnoreCase ("node label")) {
      String labelDeterminingAttribute = names [2];
      Node [] nodes = cw.getGraph().getNodeArray();
      for (int i=0; i < nodes.length; i++) {
        String canonicalName = cw.getCanonicalNodeName (nodes [i]);
        HashMap attributes = nodeAttributes.getAttributes (canonicalName);
        if (attributes.containsKey (labelDeterminingAttribute)) {
          Object value = attributes.get (labelDeterminingAttribute);
          String stringValue = value.toString ();
          NodeRealizer nr = g2d.getRealizer (nodes [i]);
          NodeLabel nl = nr.getLabel ();
          nl.setText (stringValue);
          //ShapeNodeRealizer snr = (ShapeNodeRealizer) g2d.getRealizer (nodes [i]);
          //snr.setLabelText ("new label");
          } // if has labelDeterminingAttribute
        } // for i
      } // if node label
    } // vizrule

  if (incomingNameListModeButton.getLabel().equalsIgnoreCase ("Select Nodes")) {
    boolean clearSelectionFirst = false;
    cw.selectNodesByName (nameList.getNames(), clearSelectionFirst);
    }
  else if (incomingNameListModeButton.getLabel().equalsIgnoreCase ("Create Nodes")) {
    Network network = new Network ();
    for (int i=0; i < nameList.getNames().length; i++)
      network.add (nameList.getNames() [i]);
    handleNetwork (source, network);
   } // else: create nodes

} // handleNameList
//-------------------------------------------------------------------------------------
    public void handleMatrix(String source, DataMatrix matrix) throws RemoteException {
        String[] geneNames = matrix.getRowTitles();
        Namelist nameList = new Namelist();
        nameList.setSpecies(species);
        nameList.setNames(geneNames);
        handleNameList(myGaggleName, nameList);
    }

    //-------------------------------------------------------------------------------------
    protected void selectNodes() {
        String rawTextFieldContents = nodeSelectionTextField.getText().trim();
        String[] candidates = rawTextFieldContents.split(" ");
        selectNodesByName(candidates);
    }

    //-----------------------------------------------------------------------------------
    protected void selectNodesByName(String[] candidates) {
        String[] nodeNames = getCanonicalNamesOfNodesInGraph();
        NodeNameMatcher nameMatcher =
                new NodeNameMatcher(nodeNames, null, cw.getBioDataServer(),
                        cw.getDefaultSpecies());

        String[] matchedNodes = nameMatcher.getMatch(candidates);

        //System.out.println ("------------ match count: " + matchedNodes.length);
        //for (int i=0; i < matchedNodes.length; i++)
        //  System.out.println ("    " + matchedNodes [i]);

        cw.selectNodesByName(matchedNodes, false);

    } // selectNodesByName

    //-------------------------------------------------------------------------------------
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

    public void handleTuple(String source, GaggleTuple gaggleTuple) {
        MovieUtil.playMovieFrame(cw, mapNameReadout, gaggleTuple);
    }

    //-------------------------------------------------------------------------------------
    public void handleCluster(
            String source, Cluster cluster) throws RemoteException {
        Namelist nameList = new Namelist();
        nameList.setSpecies(species);
        nameList.setNames(cluster.getRowNames());
        //todo nameList.setName(clusterName);???
        handleNameList(myGaggleName, nameList);
    }

    //---------------------------------------------------------------------------------------
    public void handleNetwork(String source, Network network) throws RemoteException {
        NetworkUtil.extend(cw, network);
        System.out.println("CyGagglePlugin.handleNetwork, node count before redraw: " + cw.getGraph().getNodeArray().length);
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

    //-------------------------------------------------------------------------------------

    private void updateGooseChooser() { // todo refactor this to MiscUtil when all geese implement update()
        if (gooseChooser == null ) {
            return;
        }
        DefaultComboBoxModel model = (DefaultComboBoxModel) gooseChooser.getModel ();
        model.removeAllElements ();
        model.addElement("Boss");

        for (String gooseName : activeGooseNames) {
            if (!gooseName.equals(myGaggleName)) {
                model.addElement(gooseName);
            }
        }
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
            //JOptionPane.showMessageDialog(cw.getMainFrame(), msg);
        }
        boss = connector.getBoss();
    } // connectToGaggle

    public void update(String[] gooseNames) {
        this.activeGooseNames = gooseNames;
        updateGooseChooser();
    }

    public void setConnected(boolean connected, Boss boss) {
        this.boss = boss;
        if (connectButton != null)
            connectButton.setEnabled(!connected);
        if (disconnectButton != null)
            disconnectButton.setEnabled(connected);
    }

//-------------------------------------------------------------------------------------
} // CyGagglePlugin
