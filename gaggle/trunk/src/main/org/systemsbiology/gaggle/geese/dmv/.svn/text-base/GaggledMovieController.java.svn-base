// GaggledMovieController.java
// a gaggle wrapper around the swing components which broadcast
// movie data to the gaggle boss; these components are expected to be displayed in a
// TreeDataMatrixBrowser frame
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

import org.systemsbiology.gaggle.experiment.gui.*;
import org.systemsbiology.gaggle.experiment.gui.movie.*;
import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.datatypes.*;
import org.systemsbiology.gaggle.core.datatypes.DataMatrix;
import org.systemsbiology.gaggle.geese.*;
import org.systemsbiology.gaggle.geese.common.GaggleConnectionListener;

//-------------------------------------------------------------------------------------
/**
 * todo: the base class uses the LensedDataMatrix class, but should probably use
 * todo: only DataMatrix directly. (pshannon, 20 jan 2005)
 */
public class GaggledMovieController extends MovieController implements DeafGoose, GaggleConnectionListener {

    String myName = "movie";
    Boss gaggleBoss;
    String species = "unknown";
    GaggledTreeDataViewer gtdv;

    //-------------------------------------------------------------------------------------------
    public GaggledMovieController(GaggledTreeDataViewer gtdv) {
        super(gtdv);
        this.gtdv = gtdv;

    } // ctor

    //-------------------------------------------------------------------------------------------
    protected void broadcast() {
        // see MovieController.java for an explanation
        if (!conditionChooserInitialized || gaggleBoss == null)
            return;

        int columnNumber = conditionChooserIndex - 1;
        DataMatrixViewer dmv = gtdv.getTreeDataViewer().getDataMatrixViewer();
        MatrixSpreadsheet topSpreadsheet = dmv.getTopSpreadsheet();
        int companionMatrixID = topSpreadsheet.getCompanionMatrixID();
        boolean topMatrixHasCongruentCompanion = companionMatrixID >= 0;

        //System.out.println ("companion matrix, id: " + companionMatrixID);

        DataMatrix[] sourceMatrices;
        if (topMatrixHasCongruentCompanion) {
            sourceMatrices = new DataMatrix[2];
            sourceMatrices[0] = dmv.getMatrix(dmv.getIndexOfSelectedMatrix());
            sourceMatrices[1] = dmv.getMatrix(companionMatrixID);
        } else {
            //System.out.println ("\n\n--- no congruent matrix");
            sourceMatrices = new DataMatrix[1];
            sourceMatrices[0] = dmv.getMatrix(dmv.getIndexOfSelectedMatrix());
        }

        // ensure that the row names of the selection (if any) in the topmost & visible
        // matrix is used for all matrices we broadcast.  this solves the problem
        // of the top matrix having a selection (say 8 rows), but all rows of the congruent
        // matrix are broadcast

        String[] rowNames = sourceMatrices[0].getRowTitles();
        int rowCount = rowNames.length;

        /*
        AttributeMap attrMap = new AttributeMap();

        if (columnNumber >= 0 && columnNumber < columnNames.length) {
            for (DataMatrix matrix : sourceMatrices) {
                //System.out.println ("\n\tbroadcasting matrix " + i + "\n");
                //double [] values = matrix.getColumn (columnNumber);
                double[] values = new double[rowCount];
                for (int r = 0; r < rowCount; r++) {
                    double[] rowValues = matrix.get(rowNames[r]);
                    values[r] = rowValues[columnNumber];
                } // for r
                String attributeName = matrix.getShortName();
                AttributeMapUtil.addAttribute(attrMap, attributeName, rowNames,
                        AttributeMapUtil.convertDoubleArrayToObjectArray(values));
            } // for i
        } // if  columnNumber in range
        */

        if (columnNumber >= 0 && columnNumber < columnNames.length) {
            GaggleTuple gaggleTuple = new GaggleTuple();
            gaggleTuple.setSpecies(sourceMatrices[0].getSpecies());
            gaggleTuple.setName("a tuple list from dmv"); // todo make this name more useful
            // todo - think of convenience methods that would allow doing this in one line:
            Single condition = new Single("condition", sourceMatrices[0].getColumnTitles()[columnNumber]);
            gaggleTuple.getMetadata().addSingle(condition);
            //

            for (int i = 0; i < sourceMatrices.length; i++) {
                for (int j = 0; j < sourceMatrices[i].getRowCount(); j++) {
                    Tuple tuple = new Tuple();
                    tuple.addSingle(new Single(null, sourceMatrices[i].getRowTitles()[j]));
                    tuple.addSingle(new Single(null, sourceMatrices[i].getShortName()));
                    tuple.addSingle(new Single(null, sourceMatrices[i].get(j, columnNumber)));
                    gaggleTuple.getData().addSingle(new Single(tuple));
                }
            }



            try {
                gaggleBoss.broadcastTuple(myName, gtdv.getTargetGoose(), gaggleTuple);  // todo tuple fix
            }
            catch (RemoteException rex0) {
                System.err.println(rex0.getMessage());
                rex0.printStackTrace();
            }

        }

    } // broadcast

    //-------------------------------------------------------------------------------------------
/**
 * deprecated in favor of implementing GaggleConnectionListener
 * todo - address this
 */
    public void connectToGaggle() {

        String serviceName = "gaggle";
        String hostname = "localhost";

        String uri = "rmi://" + hostname + "/" + serviceName;
        try {
            gaggleBoss = (Boss) Naming.lookup(uri);
            /*String registeredName = */
            gaggleBoss.register(this);
        }
        catch (Exception ex0) {
            System.err.println("GaggledMovieController failed to connect to gaggle at " + uri);
            System.err.println("error: " + ex0.getMessage());
            ex0.printStackTrace();
        }

    } // connectToGaggle

    //----------------------------------------------------------------------------------------
    public void exit() {
    }

    //----------------------------------------------------------------------------------------
/**
 * Implements GaggleConnectionListener
 *
 * @see org.systemsbiology.gaggle.geese.common.RmiGaggleConnector
 */
    public void setConnected(boolean connected, Boss boss) {
        this.gaggleBoss = boss;
    }
} //public class GaggledMovieController
