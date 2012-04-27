// GaggledTreeDataViewer.java
//-------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.dmv;
//---------------------------------------------------------------------------------

import java.rmi.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.*;

import org.systemsbiology.gaggle.experiment.gui.*;
import org.systemsbiology.gaggle.experiment.gui.movie.*;
import org.systemsbiology.gaggle.experiment.datamatrix.*;

import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.datatypes.*;
import org.systemsbiology.gaggle.core.datatypes.DataMatrix;
import org.systemsbiology.gaggle.geese.common.GaggleConnectionListener;
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;
import org.systemsbiology.gaggle.geese.common.GooseShutdownHook;
import org.systemsbiology.gaggle.geese.common.gui.GuiWidgetFactory;
import org.systemsbiology.gaggle.util.*;

//-------------------------------------------------------------------------------------
public class GaggledTreeDataViewer implements MovieControllerClient, Goose,
        GaggleConnectionListener, java.io.Serializable {

    String myGaggleName = "DMV";
    TreeDataViewer tdv;
    MovieController movieController;
    Boss boss;
    JComboBox gooseChooser;
    String targetGoose = "Boss";
    String[] activeGooseNames = new String[0];
    JButton getGeeseNamesButton;
    JButton showGooseButton;
    JButton hideGooseButton;
    JButton broadcastListButton;
    JButton broadcastClusterButton;   // not yet used, but could plausibly be.
    JButton broadcastMatrixButton;
    private RmiGaggleConnector gaggleConnector;
    private GuiWidgetFactory factory;

    //-------------------------------------------------------------------------------------
    public GaggledTreeDataViewer(String repositoryName, String preSelectedPaths) {
        ToolTipManager.sharedInstance().setInitialDelay(0);
        tdv = new TreeDataViewer(repositoryName);
        MiscUtil.placeInCenter(tdv.getMainFrame());

        gaggleConnector = new RmiGaggleConnector(this);
        new GooseShutdownHook(gaggleConnector);
        factory = new GuiWidgetFactory(gaggleConnector, this);
        addGaggleControlsToToolBar();
        addGaggleMenu();

        movieController = new GaggledMovieController(this);
        //movieController.setEnabled (true);
        movieController.setEnabled(false);
        tdv.setMovieController(movieController);
        tdv.getMainFrame().pack();

        gaggleConnector.addListener((GaggledMovieController) movieController);
        gaggleConnector.addListener(this);

        // receive notification that the TDV is exiting so we can close the
        // connection with the Gaggle Boss.
        tdv.addExitListener(new ExitListener() {
            public void exitInProgress() {
                gaggleConnector.disconnectFromGaggle(true);
            }
        });
    } // ctor

    //-------------------------------------------------------------------------------------
    protected void addGaggleControlsToToolBar() {
        JToolBar toolbar = tdv.getExtraToolBar();
        toolbar.setFloatable(false);

        //JButton getGeeseNamesButton = new JButton(factory.getUpdateAction());
        //toolbar.add(getGeeseNamesButton);

        gooseChooser = factory.getGooseChooser();
        toolbar.add(gooseChooser);

        gooseChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                int gooseChooserIndex = cb.getSelectedIndex();
                System.out.println("choose goose index: " + gooseChooserIndex);
                targetGoose = (String) cb.getSelectedItem();
            }
        });
        populateGooseChooser();

        showGooseButton = new JButton(factory.getShowGooseAction());
        hideGooseButton = new JButton(factory.getHideGooseAction());
        broadcastListButton = new JButton("B");
        broadcastMatrixButton = new JButton("M");

        showGooseButton.setToolTipText("Show selected goose");
        hideGooseButton.setToolTipText("Hide selected goose");
        broadcastListButton.setToolTipText("Broadcast list to selected goose");
        broadcastMatrixButton.setToolTipText("Broadcast matrix to selected goose");

        broadcastListButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doBroadcastList();
            }
        });

        broadcastMatrixButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doBroadcastMatrix();
            }
        });

        toolbar.add(showGooseButton);
        toolbar.add(hideGooseButton);
        toolbar.add(broadcastListButton);
        toolbar.add(broadcastMatrixButton);

    } // addGaggleControlsToToolBar

    //-------------------------------------------------------------------------------------
    public void addGaggleMenu() {
        tdv.addMenu(factory.getGaggleMenu());
    }

    //-------------------------------------------------------------------------------------
    public TreeDataViewer getTreeDataViewer() {
        return tdv;
    }

    //-------------------------------------------------------------------------------------
    public JPanel getToolbarEtcPanel() {
        return tdv.getToolbarEtcPanel();
    }

    //------------------------------------------------------------------------------
    public LensedDataMatrix[] getLenses() {
        DataMatrixViewer dmv = tdv.getDataMatrixViewer();
        if (dmv != null)
            return dmv.getAllLenses();
        else
            return new LensedDataMatrix[]{};

    }

    //------------------------------------------------------------------------------
    public Date getLastModificationTime() {
        DataMatrixViewer dmv = tdv.getDataMatrixViewer();
        if (dmv != null)
            return dmv.getLastModificationTime();
        else
            return null;

    }

    //------------------------------------------------------------------------------
    public synchronized void handleNameList(String source, Namelist nameList) throws RemoteException {
        // System.out.println ("GaggledTreeDataViewer.select, count = " + names.length);
        DataMatrixViewer dmv = tdv.getDataMatrixViewer();
        if (dmv != null)
            dmv.select(source, nameList.getNames());

    } // handleNameList

    //-------------------------------------------------------------------------------------
    public void handleMatrix(String source, DataMatrix matrix) {
        System.out.println("receiving matrix.");
        DataMatrixViewer dmv = tdv.getDataMatrixViewer();
        if (dmv == null)
            return;
        dmv.addMatrixSpreadsheetView(matrix, dmv.getMetaDataNavigator());
        movieController.loadMatrices();

    } // handleMatrix

    //-------------------------------------------------------------------------------------
    public void handleTuple(String source, GaggleTuple gaggleTuple) {
    }

    //-------------------------------------------------------------------------------------
    public void handleCluster(
            String source, Cluster cluster) {
        DataMatrixViewer dmv = tdv.getDataMatrixViewer();
        if (dmv != null)
            dmv.handleCluster(cluster);
    }

    //-------------------------------------------------------------------------------------
    public void handleNetwork(String source, Network network) {
    }

    //-------------------------------------------------------------------------------------
    public String getName() {
        return myGaggleName;
    }

    //-------------------------------------------------------------------------------------
    public void setName(String newValue) {
        myGaggleName = newValue;
        tdv.getMainFrame().setTitle(myGaggleName);
    }

    //-------------------------------------------------------------------------------------
    public void doHide() {
        tdv.getMainFrame().setVisible(false);
    }

    //-------------------------------------------------------------------------------------
    public void doShow() {
        JFrame frame = tdv.getMainFrame();

        if (frame.getExtendedState() != java.awt.Frame.NORMAL)
            frame.setExtendedState(java.awt.Frame.NORMAL);

        MiscUtil.setJFrameAlwaysOnTop(frame, true);
        frame.setVisible(true);
        MiscUtil.setJFrameAlwaysOnTop(frame, false);

    }

    //-------------------------------------------------------------------------------------
    public void clearSelections() {
        DataMatrixViewer dmv = tdv.getDataMatrixViewer();
        if (dmv != null)
            dmv.clearSelections();

    }

    //-------------------------------------------------------------------------------------
    private String[] getSelection() {
        DataMatrixViewer dmv = tdv.getDataMatrixViewer();
        if (dmv != null)
            return dmv.getSelection();

        return new String[0];

    }

    //-------------------------------------------------------------------------------------
    public int getSelectionCount() {
        return getSelection().length;
    }

    //-------------------------------------------------------------------------------------
    public void setGeometry(int x, int y, int width, int height) {
        tdv.getMainFrame().setSize(width, height);
        tdv.getMainFrame().setLocation(x, y);
    }

    //-------------------------------------------------------------------------------------
    public void doExit() {
        gaggleConnector.disconnectFromGaggle(true);
        System.exit(0);
    }

    //-------------------------------------------------------------------------------------
    public void connectToGaggle() throws Exception {
        gaggleConnector.connectToGaggle();
        boss = gaggleConnector.getBoss();
    }

    //----------------------------------------------------------------------------------------
    public void setConnected(boolean connected, Boss boss) {
        this.boss = boss;
        broadcastListButton.setEnabled(connected);
        broadcastMatrixButton.setEnabled(connected);
    }

    //----------------------------------------------------------------------------------------
    public String getTargetGoose() {
        return targetGoose;
    }

    //----------------------------------------------------------------------------------------
    public void doBroadcastList() {
        DataMatrixViewer dmv = tdv.getDataMatrixViewer();
        if (dmv == null)
            return;

        String[] selection = dmv.getSelection();

        if (selection.length == 0)
            return;

        String species = dmv.getSpecies();
        dmv.select(species, selection);  // ensure that other tabs see this selection
        Namelist nameList = new Namelist();
        nameList.setSpecies(species);
        nameList.setNames(selection);

        try {
            boss.broadcastNamelist(myGaggleName, targetGoose, nameList);
        }
        catch (RemoteException rex) {
            System.err.println("GaggledTreeDataViewer: " +
                    "rmi error calling boss.select (species, selection)");
            rex.printStackTrace();
        }

    } // doBroadcastNames

    //------------------------------------------------------------------------------
    public void doBroadcastMatrix() {
        DataMatrixViewer dmv = tdv.getDataMatrixViewer();
        if (dmv == null)
            return;

        // broadcast a copy of the lensed matrix.
        // This avoids copying a large dataset of which you only want a small
        // selection and gets around problems which seem to crop up when lenses
        // are stacked on top of each other. -cbare
        LensedDataMatrix lense = dmv.getLensedMatrix(dmv.getIndexOfSelectedMatrix());
        System.out.println("GTDV.doBroadcastMatrix, matrix: " +
                lense.getRowCount() + " x " + lense.getColumnCount());
        if (null == lense)
            return;

        DataMatrix matrix;
        if (lense.getRowCount() == 0) {
            // if no rows are selected, broadcast the whole thing.
            // (we may want to copy here too.)
            matrix = lense.getUnderlyingMatrix();
        } else {
            matrix = lense.getVisibleMatrix();
        }

        try {
            boss.broadcastMatrix(myGaggleName, targetGoose, matrix);
        }
        catch (RemoteException rex) {
            System.err.println("GaggledTreeDataViewer: rmi error calling boss.broadcast(matrix)");
            rex.printStackTrace();

        }

    } // doBroadcastNames

    void populateGooseChooser() {
        if (boss == null) {
            gooseChooser.removeAllItems();
            gooseChooser.addItem("Couldn't connect to Boss");
            return;
        }

        gooseChooser.removeAllItems();
        gooseChooser.addItem("Boss");
        for (String name : activeGooseNames) {
            if (!name.equals(myGaggleName)) {
                gooseChooser.addItem(name);
            }
        }
    }

    public void update(String[] gooseNames) {
        activeGooseNames = gooseNames;
        if (gooseChooser != null) {
            populateGooseChooser();
        }
    }


    //------------------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        String repositoryName = null;
        String pathsToSelect = null;
        if (args.length == 1)
            repositoryName = args[0].trim();

        if (args.length == 2)
            pathsToSelect = args[1].trim();

        GaggledTreeDataViewer goose = new GaggledTreeDataViewer(repositoryName, pathsToSelect);
        try {
            goose.connectToGaggle();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    } // main
//------------------------------------------------------------------------------
} // GaggledTreeDataViewer

