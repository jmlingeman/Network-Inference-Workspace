// SequenceGoose.java
// obtain and possibly 'auto-broadcast' to a sequence-consuming goose
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

package org.systemsbiology.gaggle.geese.sequence;
//---------------------------------------------------------------------------------

import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.datatypes.*;
import org.systemsbiology.gaggle.util.MiscUtil;
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;
import org.systemsbiology.gaggle.geese.common.GooseShutdownHook;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

//-------------------------------------------------------------------------------------
public class SequenceGoose extends JFrame implements Goose {

    String myGaggleName = "Sequence";
    String[] activeGeeseNames;
    JButton connectButton;
    JButton disconnectButton;
    Boss boss;
    RmiGaggleConnector connector = new RmiGaggleConnector(this);
    protected JScrollPane scrollPane;
    protected JTextArea textArea;
    JComboBox gooseChooser;
    String targetGoose = "Boss";
    String species = "unknown";
    String dataUri = "unknown";
    SequenceFetcher fetcher;
    String sequenceType = "aa";   // "DNA" is the alternative
    boolean autoBroadcast = false;
    CodonTable codonTable;
    ArrayList sequencesForBroadcast;

    //-------------------------------------------------------------------------------------
    public SequenceGoose(String[] args) {
        super("Sequence");
        parseCommandLineArguments(args);
        new GooseShutdownHook(connector);

        MiscUtil.setApplicationIcon(this);
        

        try {
            connectToGaggle();
        }
        catch (Exception ex0) {
            System.err.println("SequenceGoose failed to export remote object: " + ex0.getMessage());
        }

        try {
            System.out.println("about to create fetcher for: " + dataUri);
            fetcher = SequenceFetcherFactory.createFetcher(dataUri);
            System.out.println(" ....done");
        }
        catch (Exception ex0) {
            String msg = "SequenceGoose failed create sequenceFetcher from '" + dataUri + "'";
            //  ex0.getMessage ());
            System.err.println(msg);
        }

        codonTable = new CodonTable();
        add(createGui());
        setSize(500, 500);
        MiscUtil.placeInCenter(this);
        setVisible(true);

    }

    //-------------------------------------------------------------------------------------
    protected void parseCommandLineArguments(String[] args) {
        int max = args.length;

        for (int i = 0; i < max; i++) {
            System.out.println("parse, args " + i + ": " + args[i]);
            String arg = args[i].trim();
            if (arg.equals("-s") && i + 1 < max) {
                species = args[i + 1].trim();
                System.out.println("found -s, assigning species: " + species);
            }
            if (arg.equals("-d") && i + 1 < max)
                dataUri = args[i + 1].trim();
        } // for i

        System.out.println("leaving seq goose parse, species: " + species + "   uri: " + dataUri);

    } // parseCommandLineArguments

    //-------------------------------------------------------------------------------------
    JPanel createGui() {
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        JPanel controlPanel = new JPanel();

        ButtonGroup radioButtonGroup = new ButtonGroup();
        JRadioButton dnaButton = new JRadioButton("DNA");
        JRadioButton aminoAcidButton = new JRadioButton("aa");
        dnaButton.setSelected(false);
        aminoAcidButton.setSelected(true);
        radioButtonGroup.add(dnaButton);
        radioButtonGroup.add(aminoAcidButton);
        controlPanel.add(dnaButton);
        controlPanel.add(aminoAcidButton);

        dnaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sequenceType = e.getActionCommand();
            }
        });

        aminoAcidButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sequenceType = e.getActionCommand();
            }
        });

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        controlPanel.add(toolbar);
        JCheckBox autoBroadcastButton = new JCheckBox("auto");
        //controlPanel.add (autoBroadcastButton);
        autoBroadcastButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                autoBroadcast = !autoBroadcast;
            }
        });

        //controlPanel.add (autoBroadcastButton);
        mainPanel.add(controlPanel, BorderLayout.NORTH);


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
        JButton broadcastSequencesButton = new JButton("B");

        showGooseButton.setToolTipText("Show selected goose");
        hideGooseButton.setToolTipText("Hide selected goose");
        broadcastSequencesButton.setToolTipText("Broadcast sequence");

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

        broadcastSequencesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    doBroadcastList();
                }
                catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        });

        toolbar.add(showGooseButton);
        toolbar.add(hideGooseButton);
        toolbar.add(broadcastSequencesButton);
        toolbar.add(autoBroadcastButton);


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
                    JOptionPane.showMessageDialog(null,
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

        toolbar.add(connectButton);
        toolbar.add(disconnectButton);


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

        MiscUtil.updateGooseChooser(gooseChooser, myGaggleName, activeGeeseNames);


        return mainPanel;

    } // createGui


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
        textArea.setText("");
        sequencesForBroadcast = new ArrayList();
        StringBuffer sb = new StringBuffer();
        int max = 1;
        if (nameList.getNames().length < max)
            max = nameList.getNames().length;
        for (int i = 0; i < max; i++) {
            System.out.println(" --- handleNameList, orf " + nameList.getNames()[i] + ", mode: " + sequenceType + "\n");
            String sequence = fetcher.getDnaSequence(source, nameList.getNames()[i]);
            if (sequence == null) {
                textArea.append("no sequence available for " + nameList.getNames()[i] + "\n");
                textArea.setCaretPosition(textArea.getText().length());
                return;
            }
            System.out.println(">>> fetched dna sequence: " + sequence);
            if (sequenceType.equals("aa"))
                sequence = dnaToAminoAcids(sequence);
            System.out.println(" aa sequence: " + sequence.length());
            sequencesForBroadcast.add(sequence);
            sb.append(formatSequence(sequence));
        }

        sb.append("\n\n");
        textArea.append(sb.toString());
        textArea.setCaretPosition(textArea.getText().length());

        if (autoBroadcast)
            doBroadcastList();

    } // handleNameList

    //----------------------------------------------------------------------------------------
    protected String dnaToAminoAcids(String dna) {
        //System.out.println ("=== dnaToAminoAcids, length: " + dna.length ());
        StringBuffer sb = new StringBuffer();
        int codonCount = dna.length() / 3;
        //System.out.println ("codonCount: " + codonCount);

        for (int i = 0; i < codonCount; i++) {
            int start = i * 3;
            int end = start + 3;
            String codon = dna.substring(start, end);
            String aminoAcid = codonTable.getAminoAcidSequence(codon);
            // System.out.println (codon + " -> " + aminoAcid);
            if (aminoAcid == null)
                break;
            sb.append(aminoAcid);
        }
        return sb.toString();

    } // dnaToAminoAcids

    //----------------------------------------------------------------------------------------
    protected String formatSequence(String sequence) {
        int LINE_SIZE = 60;
        StringBuffer sb = new StringBuffer();
        int max = sequence.length();
        int marker = 0;

        while ((marker + LINE_SIZE) < max) {
            sb.append(sequence.substring(marker, marker + LINE_SIZE));
            sb.append("\n");
            marker += LINE_SIZE;
        }

        sb.append(sequence.substring(marker));

        return sb.toString();

    } // formatSequence

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
        StringBuffer sb = new StringBuffer();
        sb.append(" >>> handleMap: " + gaggleTuple.getName() + "\n");
        sb.append("    Got a Tuple with    ");
        sb.append(gaggleTuple.getData().getSingleList().size());
        sb.append(" tuples.\n");
        // todo - more description?
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
    } // handleMap

    //----------------------------------------------------------------------------------------
    public void handleCluster(
            String source, Cluster cluster) {
        StringBuffer sb = new StringBuffer();
        sb.append(" >>> handleCluster: " + cluster.getName() + "\n");
        sb.append("  rows: " + cluster.getRowNames().length + "\n");
        sb.append("  list2: " + cluster.getColumnNames().length + "\n");

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
        String[] sequences = (String[]) sequencesForBroadcast.toArray(new String[0]);
        System.out.println("number of sequences: " + sequences.length);
        if (targetGoose.equals("BLASTP")) {
            String url = "http://db.systemsbiology.net:8080/halo/blastpsi.py?seq=" + sequences[0];
            System.out.println("url: " + url);
            MiscUtil.displayWebPage(url);
            return;
        }

        Namelist nameList = new Namelist();
        nameList.setSpecies(species);
        nameList.setNames(sequences);
        try {
            boss.broadcastNamelist(myGaggleName, targetGoose, nameList);
        }
        catch (Exception ex0) {
            ex0.printStackTrace();
        }

    } // doBroadcastList

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
        this.activeGeeseNames = gooseNames;
        MiscUtil.updateGooseChooser(gooseChooser, myGaggleName, gooseNames);
    }
    

    //----------------------------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        SequenceGoose goose = new SequenceGoose(args);

    } // main
//-------------------------------------------------------------------------------------
} // SequenceGoose
