// NameGoose.java
// provide translation to alternative names, and optional 'auto-broadcast' 
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

package org.systemsbiology.gaggle.geese.names;
//---------------------------------------------------------------------------------
// todo - port this fully to new api so it can be used in R goose unit tests written in R
// see this jnlp to make a new webstart:
// http://gaggle.systemsbiology.net/2005-11/echo/echo.jnlp
import org.systemsbiology.gaggle.core.datatypes.DataMatrix;
import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.datatypes.*;
import org.systemsbiology.gaggle.util.*;
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;
import org.systemsbiology.gaggle.geese.common.GooseShutdownHook;

import java.io.File;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
//-------------------------------------------------------------------------------------

/**
 * A Goose that provides name translation between namespaces or between
 * species by orthology.
 */
public class NameGoose extends JFrame implements Goose {

    String myGaggleName = "NameTranslator";
    Boss boss;
    RmiGaggleConnector connector = new RmiGaggleConnector(this);
    protected JScrollPane scrollPane;
    protected JTextArea textArea;
    protected JPanel controlPanel;

    File currentDirectory;

    JComboBox gooseChooser;
    String targetGoose = "Boss";

    JComboBox nameSpaceChooser;
    String targetNameSpace = "unknown";

    String species = "unknown";
    boolean autoBroadcast = false;
    NewNameHelper nameHelper;
    Translator translator;

    String dataUri = null;   // for hypothetical, future, manual loading of name table

    String[] currentInputNames = new String[0];
    String[] translatedNames = new String[0];

    org.systemsbiology.gaggle.core.datatypes.Network currentInputNetwork = null;
    org.systemsbiology.gaggle.core.datatypes.Network translatedNetwork = null;

    DataMatrix currentInputMatrix = null;
    DataMatrix translatedMatrix = null;

    String currentClusterName;
    String[] currentClusterRowNames = null;
    String[] currentClusterColumnNames = null;
    String[] translatedClusterRowNames = null;
    String[] translatedClusterColumnNames = null; 

    //AttributeMap translatedAttributeMap = null;

    //-------------------------------------------------------------------------------------
    public NameGoose(String[] args) {
        super("NameTranslator");
        new GooseShutdownHook(connector);
        ToolTipManager.sharedInstance().setInitialDelay(0);
        parseCommandLineArguments(args);

        MiscUtil.setApplicationIcon(this);
        

        try {
            connectToGaggle();
        }
        catch (Exception ex0) {
            System.err.println("NameGoose failed to export remote object: " + ex0.getMessage());
            ex0.printStackTrace();
        }

        if (dataUri != null) try {
            nameHelper = new NewNameHelper(dataUri);
            translator = new Translator(nameHelper);
        }
        catch (Exception ex0) {
            String msg = "error reading names file: " + ex0.getMessage();
            ex0.printStackTrace();
            JOptionPane.showMessageDialog(NameGoose.this, msg);
        }

        if (dataUri == null) try {
            nameHelper = boss.getNameHelper();
        }
        catch (Exception ex1) {
            String msg = "NameGoose failed to obtain nameHelper from boss";
            ex1.printStackTrace();
            JOptionPane.showMessageDialog(NameGoose.this, msg);
        }

        add(createGui());
        MiscUtil.updateGooseChooserOLD(boss, gooseChooser, myGaggleName, null);
        setSize(500, 500);
        MiscUtil.placeInCenter(this);
        setVisible(true);

    }  // ctor

    //-------------------------------------------------------------------------------------
    protected void parseCommandLineArguments(String[] args) {
        int max = args.length;

        for (int i = 0; i < max; i++) {
            //System.out.println ("parse, args " + i + ": " + args [i]);
            String arg = args[i].trim();
            if (args[i].equals("--gooseName") && i + 1 < max) {
                myGaggleName = args[i + 1];
            }
            if (arg.equals("-s") && i + 1 < max) {
                species = args[i + 1].trim();
                //System.out.println ("found -s, assigning species: " + species);
            }
            if (arg.equals("-d") && i + 1 < max)
                dataUri = args[i + 1].trim();
        } // for i

        System.out.println("leaving NameGoose parseCommandLineArguments, species: " + species + "   uri: " + dataUri +
                " goose name: " + myGaggleName);

    } // parseCommandLineArguments

    //-------------------------------------------------------------------------------------
    JPanel createGui() {
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(2, 1));
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        controlPanel.add(toolbar);
        JCheckBox autoBroadcastButton = new JCheckBox("auto");
        autoBroadcastButton.setToolTipText(
                "<html>Send names to target goose<br>as soon as they are tranlsated.</html>");

        autoBroadcastButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                autoBroadcast = !autoBroadcast;
            }
        });

        if (nameHelper == null) {
            String msg = "No name translator available from Gaggle Boss.";
            JOptionPane.showMessageDialog(NameGoose.this, msg);
        } // if !nameHelper
        else {
            controlPanel.add(createNameHelperGui(nameHelper));
        } // if nameHelper

        mainPanel.add(controlPanel, BorderLayout.NORTH);

        JButton loadNameHelperFileButton = new JButton("Load...");
        loadNameHelperFileButton.setToolTipText("Add new name translations from tab-delimited file.");
        JButton getGeeseNamesButton = new JButton("Update");
        getGeeseNamesButton.setToolTipText("Get an up-to-date list of the geese in the gaggle.");

        loadNameHelperFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    chooseAndLoadNewNameHelperFile();
                } //try
                catch (Exception ex0) {
                    ex0.printStackTrace();
                }
            }
        });


        toolbar.add(loadNameHelperFileButton);
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
        gooseChooser.setToolTipText("Select goose.");
        toolbar.add(gooseChooser);


        gooseChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                //System.out.println ("choose goose index: " + gooseChooserIndex);
                targetGoose = (String) cb.getSelectedItem();
            }
        });

        JButton showGooseButton = new JButton("S");
        JButton hideGooseButton = new JButton("H");
        JButton broadcastNamesButton = new JButton("B");
        JButton broadcastMatrixButton = new JButton("M");
        JButton broadcastNetworkButton = new JButton("N");
        JButton broadcastHashButton = new JButton("A");
        JButton broadcastClusterButton = new JButton("C");

        showGooseButton.setToolTipText("Show selected goose");
        hideGooseButton.setToolTipText("Hide selected goose");
        broadcastNamesButton.setToolTipText("Broadcast latest name list, translated");
        broadcastMatrixButton.setToolTipText("Broadcast latest matrix, translated");
        broadcastNetworkButton.setToolTipText("Broadcast latest network, translated");
        broadcastHashButton.setToolTipText("Broadcast latest associative array, translated");
        broadcastClusterButton.setToolTipText("Broadcast latest cluster, translated");

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

        broadcastNamesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    doBroadcastList();
                }
                catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        });

        broadcastMatrixButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    doBroadcastMatrix();
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

        broadcastHashButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    doBroadcastHashMap();
                }
                catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        });

        broadcastClusterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    doBroadcastCluster();
                }
                catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        });

        toolbar.add(showGooseButton);
        toolbar.add(hideGooseButton);
        toolbar.add(broadcastNamesButton);
        toolbar.add(broadcastMatrixButton);
        toolbar.add(broadcastNetworkButton);
        toolbar.add(broadcastHashButton);
        toolbar.add(broadcastClusterButton);
        toolbar.add(autoBroadcastButton);

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

        return mainPanel;

    } // createGui

    //-----------------------------------------------------------------------------------
    protected JPanel createNameHelperGui(NewNameHelper nameHelper) {
        String[] nameSpaces = nameHelper.getTitles();

        nameSpaceChooser = new JComboBox(nameSpaces);
        if (nameSpaces.length >= 2) {
            targetNameSpace = nameSpaces[1];  // rough guess: translate to second column name space
            nameSpaceChooser.setSelectedIndex(1);
        }

        JPanel layoutHelper = new JPanel();
        layoutHelper.add(new JLabel("Translate incoming names to: "));
        layoutHelper.add(nameSpaceChooser);

        nameSpaceChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                targetNameSpace = (String) cb.getSelectedItem();
            }
        });
/*
  JButton retranslateButton = new JButton ("Retranslate");
  layoutHelper.add (retranslateButton);
  retranslateButton.setToolTipText (
    "<html>Translate most recently received names <br>to currently selected name space.</html>");
  
  retranslateButton.addActionListener (new ActionListener () {
    public void actionPerformed (ActionEvent e) {
      //translateAndPossiblyBroadcastCurrentNames ();
      displayMessage ("\n --- retranslate\n\n");
      }});
*/
        return layoutHelper;

    } // createGui

    //-----------------------------------------------------------------------------------
    protected void chooseAndLoadNewNameHelperFile() {

        JFileChooser chooser = new JFileChooser(currentDirectory);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnVal = chooser.showOpenDialog(NameGoose.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File chosenFile = chooser.getSelectedFile();
            if (chosenFile != null) {
                currentDirectory = chooser.getCurrentDirectory();
                try {
                    nameHelper = new NewNameHelper(chosenFile.getPath());
                    controlPanel.add(createNameHelperGui(nameHelper));
                }
                catch (Exception ex0) {
                    ex0.printStackTrace();
                    JOptionPane.showMessageDialog(NameGoose.this, "Read NameHelper file error", ex0.getMessage(),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } // if !null
        } // if approve

    } // chooseAndLoadNewNameHelperFile

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
    } // connectToGaggle

    //----------------------------------------------------------------------------------------
    public void handleNameList(String source, Namelist nameList) {
        this.species = source;
        displayMessage(" >>> handleNameList: " + nameList.getNames().length + "\n");
        currentInputNames = nameList.getNames();
        translatedNames = translator.get(currentInputNames, targetNameSpace);

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < currentInputNames.length; i++)
            sb.append("   " + currentInputNames[i] + " -> " + translatedNames[i] + "\n");

        sb.append("\n\n");
        textArea.append(sb.toString());
        textArea.setCaretPosition(textArea.getText().length());

        if (autoBroadcast)
            doBroadcastList();

    } // handleNameList

    //----------------------------------------------------------------------------------------
    public void handleMatrix(String source, DataMatrix matrix) {
        displayMessage(" >>> handleMatrix: " + matrix.getRowCount() + " x " +
                matrix.getColumnCount() + "\n");

        translatedMatrix = translator.get(matrix, targetNameSpace);
        if (autoBroadcast)
            doBroadcastMatrix();

    }

    //----------------------------------------------------------------------------------------
    public void handleTuple(String source, GaggleTuple gaggleTuple) {
         // todo tuple fix
        /*
        String[] keys = AttributeMapUtil.getAttributeNames(attributeMap);
        StringBuffer sb = new StringBuffer();

        for (int k = 0; k < keys.length; k++) {
            String attribute = keys[k];
            sb.append("  ");
            sb.append(attribute);

            sb.append(": " + AttributeMapUtil.getNamesForAttribute(attributeMap, keys[k]).length);
        } // for k

        displayMessage(" >>> handleMap: " + attributeMap.getName() + "," + sb.toString() + "\n");


        translatedAttributeMap = translator.get(attributeMap, targetNameSpace);

        if (autoBroadcast)
            doBroadcastHashMap();
      */
    } 

    //----------------------------------------------------------------------------------------
    public void handleCluster(
            String source, Cluster cluster) {
        displayMessage(" >>> handleCluster:  '" + cluster.getName() + "'   " + cluster.getRowNames().length + " row names, " +
                cluster.getColumnNames().length + " column names\n");
        // todo some refactoring here
        currentClusterName = cluster.getName();
        currentClusterRowNames = cluster.getRowNames();
        currentClusterColumnNames = cluster.getColumnNames();

        //displayMessage (" calling translate cluster row names...\n");
        translatedClusterRowNames = translator.get(cluster.getRowNames(), targetNameSpace);
        //displayMessage (" translated cluster row names, count: " + translatedClusterRowNames.length + "\n");
        translatedClusterColumnNames = translator.get(cluster.getColumnNames(), targetNameSpace);

        if (autoBroadcast)
            doBroadcastCluster();


    } // handleCluster

    //----------------------------------------------------------------------------------------
    public void handleNetwork(String source, Network network) {
        Interaction[] interactions = network.getInteractions();
        displayMessage(" >>> handleNetwork: " + interactions.length + " interactions\n");

        this.species = species;
        currentInputNetwork = network;
        //System.out.println ("NameGoose, network node count: " + network.getNodes().length);
        translatedNetwork = translator.get(currentInputNetwork, targetNameSpace);
        //System.out.println ("        translated node count: " + translatedNetwork.getNodes().length);

        if (autoBroadcast)
            doBroadcastNetwork();

    } // handleNetwork

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
        if (translatedNames == null || translatedNames.length == 0)
            return;

        Namelist nameList = new Namelist();
        nameList.setSpecies(species);
        nameList.setNames(translatedNames);

        try {
            boss.broadcastNamelist(myGaggleName, targetGoose, nameList);
        }
        catch (Exception ex0) {
            ex0.printStackTrace();
        }

    } // doBroadcastList

    //----------------------------------------------------------------------------------------
    public void doBroadcastNetwork() {
        if (translatedNetwork == null)
            return;

        try {
            boss.broadcastNetwork(myGaggleName, targetGoose, translatedNetwork);
        }
        catch (Exception ex0) {
            ex0.printStackTrace();
        }

    } // doBroadcastNetwork

    //----------------------------------------------------------------------------------------
    public void doBroadcastMatrix() {
        if (translatedMatrix == null)
            return;

        try {
            boss.broadcastMatrix(myGaggleName, targetGoose, translatedMatrix);
        }
        catch (Exception ex0) {
            ex0.printStackTrace();
        }

    } // doBroadcastNetwork

    //----------------------------------------------------------------------------------------
    public void doBroadcastCluster() {
        if (translatedClusterRowNames == null || translatedClusterRowNames.length == 0)
            return;

        if (translatedClusterColumnNames == null || translatedClusterColumnNames.length == 0)
            return;

        if (currentClusterName == null)
            return;

        try {
            boss.broadcastCluster(myGaggleName, targetGoose, new Cluster(currentClusterName, species, translatedClusterRowNames,
                    translatedClusterColumnNames));
        }
        catch (Exception ex0) {
            ex0.printStackTrace();
        }

    } // doBroadcastCluster

    //----------------------------------------------------------------------------------------
    public void doBroadcastHashMap() {
        /*
        if (translatedAttributeMap == null || AttributeMapUtil.isEmpty(translatedAttributeMap))
            return;

        try {
            boss.broadcastTuple(myGaggleName, targetGoose, null);  // todo tuple fix
        }
        catch (Exception ex0) {
            ex0.printStackTrace();
        }
        */
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

    //----------------------------------------------------------------------------------------
    protected void displayMessage(String msg) {
        textArea.append(msg);
        textArea.setCaretPosition(textArea.getText().length());
    }

    //----------------------------------------------------------------------------------------
    public void update(String[] gooseNames) {
    }


    public static void main(String[] args) throws Exception {
        new NameGoose(args);

    } // main
//-------------------------------------------------------------------------------------
} // NameGoose
