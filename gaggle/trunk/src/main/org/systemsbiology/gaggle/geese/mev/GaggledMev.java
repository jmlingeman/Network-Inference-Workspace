// GaggledMev.java
//------------------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.mev;
//------------------------------------------------------------------------------------------------

import org.tigr.microarray.mev.*;
import org.tigr.microarray.mev.cluster.gui.IData;
//------------------------------------------------------------------------------------------------
import java.rmi.*;

import java.awt.event.*;
import javax.swing.*;

import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.datatypes.*;
import org.systemsbiology.gaggle.util.*;
import org.systemsbiology.gaggle.core.datatypes.DataMatrix;
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;
import org.systemsbiology.gaggle.geese.common.GooseShutdownHook;

//-------------------------------------------------------------------------------------
public class GaggledMev extends MultipleArrayViewer implements Goose {

    String myName = "TMev";
    Boss gaggleBoss;
    RmiGaggleConnector connector = new RmiGaggleConnector(this);
    String targetGoose = "Boss";
    String[] currentlySelectedNames = new String[]{};
    String currentSpecies = "unknown";

    //------------------------------------------------------------------------------------------------
    public GaggledMev() {
        super();
        
        init();
    }

    //------------------------------------------------------------------------------------------------
    public GaggledMev(MultipleArrayData data) {
        super(data);
        init();
    }

    //------------------------------------------------------------------------------------------------
    public GaggledMev(MultipleArrayData data, MultipleArrayMenubar origMenubar) {
        super(data, origMenubar);
        init();
    }

    //------------------------------------------------------------------------------------------------
/**
 * I hijack this standard MultipleArrayViewer method, called (perhaps among other places)
 * from the 'broadcast'  button listener I added to org/tigr/microarray/mev/SearchResultDialog.java.
 */
    public void storeOperationCluster(String source, String clusterID, int[] indices, boolean geneCluster) {

        if (source.equals("broadcast")) {
            String rawSelectionString = clusterID;
            currentlySelectedNames = rawSelectionString.split("::");
            doBroadcastList();
        } else
            super.storeOperationCluster(source, clusterID, indices, geneCluster);

    }

    //------------------------------------------------------------------------------------------------
    protected void init() {

        JMenu gaggleMenu = new JMenu("Gaggle");
        new GooseShutdownHook(connector);
        // todo set up listeners to enable/disable these two buttons as needed:
        gaggleMenu.setMnemonic('G');
        JMenuItem connectButton = new JMenuItem("Connect To Gaggle");
        connectButton.setMnemonic('C');
        JMenuItem disconnectButton = new JMenuItem("Disconnect From Gaggle");
        disconnectButton.setMnemonic('D');


        JMenuItem broadcastButton = new JMenuItem("Broadcast");
        broadcastButton.setMnemonic('B');
        JMenuItem showBossButton = new JMenuItem("Show Boss");
        showBossButton.setMnemonic('S');
        JMenuItem helpButton = new JMenuItem("Help...");
        helpButton.setMnemonic('H');

        gaggleMenu.add(connectButton);
        gaggleMenu.add(disconnectButton);

        gaggleMenu.add(broadcastButton);
        gaggleMenu.add(showBossButton);
        gaggleMenu.add(helpButton);


        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (connector.isConnected()) {
                    JOptionPane.showMessageDialog(getFrame(), "Already connected to boss!");
                    return;
                }
                try {
                    connector.connectToGaggle();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(getFrame(),
                            "Error connecting to Boss! Is the boss running?");
                    e.printStackTrace();
                }
            }
        });

        disconnectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (!connector.isConnected()) {
                    JOptionPane.showMessageDialog(getFrame(), "Already disconnected from boss.");
                    return;
                }
                connector.disconnectFromGaggle(false);
            }
        });

        broadcastButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doBroadcastList();
            }
        });

        showBossButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    gaggleBoss.show("boss");
                }
                catch (RemoteException rex) {
                    rex.printStackTrace();
                }
            }
        });

        helpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showHelp();
            }
        });

        menubar.add(gaggleMenu);

        try {
            connectToGaggle();
        }
        catch (Exception ex0) {
            System.err.println(myName + " failed to export remote object: " + ex0.getMessage());
        }

    }


    //------------------------------------------------------------------------------------------------
    public void connectToGaggle() {
        try {
            connector.connectToGaggle();
        }
        catch (Exception ex0) {
            System.err.println("failed to connect to gaggle: " + ex0.getMessage());
            ex0.printStackTrace();
        }
        gaggleBoss = connector.getBoss();
    }

    //----------------------------------------------------------------------------------------
    public void handleNameList(String source, Namelist nameList) {
    }

    public void handleNetwork(String source, Network network) {
    }

    public void handleCluster(String source, Cluster cluster) {
    }

    //-------------------------------------------------------------------------------------
/**
 * this, the GaggledMev implementation of Goose.handleMatrix () is inspired by the
 * org/tigr/microarray/mev/file/StanfordFileLoader class.
 * it reads a file of (typically) log10 ratio values, and returns a vector version
 * of an array it constructs out of SlideData objects, one for each column found
 * in the incoming data.
 * <p/>
 * ---- ISlideData [] slideDataArray = new ISlideData [experimentCount]
 * slideDataArray [0] = new SlideData (rRows == spotCount == # of genes, rColumn=1);
 * for (int i=1; i < slideDataArray.length; i++) {
 * slideDataArray[i] = new FloatSlideData (slideDataArray[0].getSlideMetaData(), spotCount);
 * <p/>
 * the above suggests that the 0th slideDataArray element is metadata
 * and that 1-n+1 elements are the actual data
 * <p/>
 * int experimentCount = ss.countTokens () + 1 - preExperimentColumns;  // numerical columns + 1
 * slideDataArray = new ISlideData [experimentCount];
 * <p/>
 * upon reading first row of file -- the title line -- these things occur,
 * creating & initializing a structure to hold a column's worth (a condition) of data
 * <p/>
 * slideDataArray = new ISlideData [experimentCount];
 * slideDataArray [0] = new SlideData (rRows == spotCount == # of genes, rColumn=1);
 * slideDataArray [0].setSlideFileName (f.getPath());
 * for (int i=1; i < slideDataArray.length; i++) {
 * slideDataArray[i] = new FloatSlideData (slideDataArray[0].getSlideMetaData(), spotCount);
 * slideDataArray[i].setSlideFileName (f.getPath());
 * }
 * <p/>
 * then, looping through all rows in the input matrix (or file) these things occur:
 * a  SlideDataElement 'sde' is created, and added to SlideDataArray [0]
 * i am not sure what this accomplishes
 * <p/>
 * then looping through the columns,
 * slideDataArray [columnNumber].setIntensities (rowNumber, cy3=0, cy5=ration)
 * <p/>
 * SlideDataElement sde:  constructed with these arguments:
 * String UID
 * int [] rows
 * int [] columns
 * float [] intensities
 * String [] values)
 * <p/>
 * Vector slideDataList: a vector form of the slideDataArray
 *
 * @param source
 * @param matrix
 */
    public void handleMatrix(String source, DataMatrix matrix) {
        //System.out.println ("GaggledMev, matrix dim: " + matrix.getRowCount () + " x " +
        //                    matrix.getColumnCount ());
        currentSpecies = matrix.getSpecies();

        float cy3, cy5;
        String[] moreFields = new String[1];
        final int rColumns = 1;
        int rRows = matrix.getRowCount();

        int counter, row, column;
        counter = 0;
        row = column = 1;

        // ----------------------------------
        // make header assignments
        // ----------------------------------

        int experimentCount = matrix.getColumnCount();  // no kidding!
        //System.out.println ("  experimentCount: " + experimentCount);

        // each element slideDataArray seems to be storage for one column of data

        ISlideData[] slideDataArray = new ISlideData[experimentCount];
        slideDataArray[0] = new SlideData(matrix.getRowCount(), 1);
        slideDataArray[0].setSlideFileName(matrix.getShortName());
        for (int i = 1; i < experimentCount; i++) {
            slideDataArray[i] = new FloatSlideData(slideDataArray[0].getSlideMetaData(), matrix.getRowCount());
            //System.out.println (i + " new Float SlideData (" + matrix.getRowCount () + ")");
            slideDataArray[i].setSlideFileName(matrix.getShortName());
        } // for i

        //get Field Names
        String[] fieldNames = new String[1];
        fieldNames[0] = matrix.getRowTitlesTitle();
        //System.out.println ("fieldNames [0]: " + fieldNames [0]);
        TMEV.setFieldNames(fieldNames);
        for (int i = 0; i < experimentCount; i++)
            slideDataArray[i].setSlideDataName(matrix.getColumnTitles()[i]);

        // ----------------------------------
        // assign the data
        // ----------------------------------

        double matrixData[][] = matrix.get();
        String[] rowTitles = matrix.getRowTitles();

        for (int r = 0; r < matrix.getRowCount(); r++) {
            int[] rows = new int[]{0, 1, 0};
            int[] columns = new int[]{0, 1, 0};
            rows[0] = rows[2] = row;
            columns[0] = columns[2] = column;
            if (column == rColumns) {
                column = 1;
                row++;
            } else {
                column++;
            }

            moreFields[0] = rowTitles[r];
            SlideDataElement sde = new SlideDataElement(String.valueOf(row + 1), rows, columns,
                    new float[2], moreFields);
            slideDataArray[0].addSlideDataElement(sde);

            for (int i = 0; i < slideDataArray.length; i++) {
                //System.out.println ("   i: " + i + "  row: " + row + "  col: " + column);
                cy3 = 1f;  //set cy3 to a default value of 1.
                cy5 = (new Double(matrixData[r][i])).floatValue();
                slideDataArray[i].setIntensities(r, cy3, cy5);
                //System.out.println ("slideDataArray [" + i + "].setIntensities (" +
                //                   r + ", " + cy3 + ", " + cy5);
            } // for i
        } // for r

        super.fireDataLoaded(slideDataArray, IData.DATA_TYPE_RATIO_ONLY);

    } // handleMatrix

    //-------------------------------------------------------------------------------------
    public void handleTuple(String source, GaggleTuple gaggleTuple) {
    }

    //-------------------------------------------------------------------------------------
    public void handleCluster(String species, String[] geneNames, String[] conditionNames) {
    }

    //-------------------------------------------------------------------------------------
    public String getName() {
        return myName;
    }

    //-------------------------------------------------------------------------------------
    public void setName(String newName) {
        this.myName = newName;
        mainframe.setTitle(newName);
    }

    //-------------------------------------------------------------------------------------
    public void doHide() {
        mainframe.setVisible(false);
    }

    //-------------------------------------------------------------------------------------
    public void doShow() {
        //mainframe.show ();
        //mainframe.toFront ();
        MiscUtil.setJFrameAlwaysOnTop(mainframe, true);
        mainframe.setVisible(true);
        MiscUtil.setJFrameAlwaysOnTop(mainframe, false);
    }

    //-------------------------------------------------------------------------------------
    public void clearSelections() {
        currentlySelectedNames = new String[]{};
    }

    //-------------------------------------------------------------------------------------
    public void setGeometry(int x, int y, int width, int height) {
    }

    //-------------------------------------------------------------------------------------
    public int getSelectionCount() {
        return currentlySelectedNames.length;
    }

    //-------------------------------------------------------------------------------------
    public void doBroadcastList() {
        IData data = super.getData();
        if (data == null)
            return;

        int rowCount = data.getFeaturesSize();
        if (rowCount > 100) {
            String title = "Broadcast names warning";
            String msg = "Do you really wish to broadcast " + rowCount + " names?";
            int dialogResult = JOptionPane.showConfirmDialog(this, msg, title,
                    JOptionPane.YES_NO_OPTION);
            if (dialogResult != JOptionPane.YES_OPTION)
                return;
        } // if warning dialog needed

        String[] selection = new String[rowCount];
        for (int i = 0; i < rowCount; i++)
            selection[i] = data.getGeneName(i);

        Namelist nameList = new Namelist();
        nameList.setSpecies(currentSpecies);
        nameList.setNames(selection);
        try {
            gaggleBoss.broadcastNamelist(myName, targetGoose, nameList);
        }
        catch (RemoteException rex) {
            System.err.println("GaggledTreeDataViewer: " +
                    "rmi error calling gaggleBoss.select (species, selection)");
            rex.printStackTrace();
        }

    } // doBroadcastList

    //------------------------------------------------------------------------------------------------
    public void doExit() {
        connector.disconnectFromGaggle(true);
        mainframe.dispose();
    } // doExit

    //-------------------------------------------------------------------------------------
    protected void showHelp() {
        StringBuffer sb = new StringBuffer();


        sb.append("<html>");
        sb.append("TIGR Mev consists of one or more Multiple Array Viewer (<b><i>mav</i></b>) windows, and ");
        sb.append("<br>");
        sb.append("one very long and skinny main menu, typically at the top of your screen.  We have added a ");
        sb.append("<br>");
        sb.append("<b><i>Gaggle</i></b> menu to the main menu bar of every <b><i>mav</i></b> window. ");
        sb.append("<br>");
        sb.append("<p> A typical use of Mev in the Gaggle goes like this:");
        sb.append("<br>");

        sb.append("<ol>");
        sb.append("  <li> Broadcast your microarray data to a blank <b><i> mav</b></i> window. You will see");
        sb.append("       a 'heat map' appear. ");
        sb.append("<br>");
        sb.append("  <li> Cluster or otherwise analyze your matrix of microarray data, using one or <br>");
        sb.append("       more of the many modules provided by Mev.");
        sb.append("  <li> Once you identify a subset of genes, place them (that is, place the submatrix heat<br>");
        sb.append("       map containing just their data) in a fresh, new <b><i>mav</i></b> window.");
        sb.append("  <li> From the <b>Gaggle</b> menu of that <b>new</b> window, select <b>Broadcast</b>.");
        sb.append("  <li> Any listening geese (DMV, KEGG WBI, R, Cytoscape) will hear this selection,<br>");
        sb.append("       and respond appropriately.");
        sb.append("</ol>");
        sb.append("");
        sb.append("<center><h4>Some Notes and Precautions</h4></center>");
        sb.append("<ol>");
        sb.append("  <li> To see the Gaggle Boss, click on the <b><i>Show Boss</i></b> menu button on any ");
        sb.append("       <b><i>mav</i></b>'s menubar.");
        sb.append("  <li> If you try to receive a new matrix into a <b><i>mav</i></b> window which is already");
        sb.append("       displaying another <br>matrix, the new matrix will either be added on to the old, or will<br>");
        sb.append("       not show up at all.  If you want to combine matrices, the <b>DMV</b> and <br> ");
        sb.append("       the <b>R</b> goose provide better ways to do this.");
        sb.append("       Therefore:");
        sb.append("");
        sb.append("<blockquote> Always have a fresh <b><i>mav</i></b> window open before you broadcast ");
        sb.append("   receive a matrix.  <br>Also make sure this window is listening for broadcasts -- which you can");
        sb.append("find out from checking its<br> status in the <b><i>Boss</i></b>.");
        sb.append("</blockquote>");
        sb.append("");
        sb.append("  <li> You may wish to normalize your matrix before sending it to Mev, using the R goose.");
        sb.append("  <li> Be careful about broadcasting name lists back to the gaggle.  The <br>");
        sb.append("       <b><i>Broadcast button</i></b> always sends the names of all of the genes<br>");
        sb.append("       in the current <b><i>mav</i></b> window, which could be thousands!");
        sb.append("</ol>");

        sb.append("</html>");
        String title = "Help for the gaggled Mev";
        int messageType = JOptionPane.INFORMATION_MESSAGE;
        Icon icon = null;
        JOptionPane.showMessageDialog(this, sb.toString(), title, messageType, icon);

    } // showHelp

    public void update(String[] gooseNames) {
    }

//-------------------------------------------------------------------------------------

    public static void main(String[] args) {
        GaggledMev gmev = new GaggledMev();
        gmev.getFrame().setSize(1150, 700);
        gmev.getFrame().setVisible(true);

    }
//-------------------------------------------------------------------------------------
} // GaggledMev
