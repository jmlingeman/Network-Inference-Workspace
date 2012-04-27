// MatrixExporter.java
//-----------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui.actions;
//-----------------------------------------------------------------------------------

import org.systemsbiology.gaggle.experiment.datamatrix.*;
import org.systemsbiology.gaggle.experiment.gui.*;
import org.systemsbiology.gaggle.core.datatypes.DataMatrix;

import javax.swing.*;
import java.awt.event.*;

import java.io.*;

//-----------------------------------------------------------------------------------
public class MatrixExporter extends AbstractAction {

    protected MatrixSpreadsheet parentSpreadsheet;
    File currentDirectory;

    //-----------------------------------------------------------------------------------
    public MatrixExporter(MatrixSpreadsheet parent) {
        // super ("", IconFactory.getExportIcon ());
        //this.putValue (Action.SHORT_DESCRIPTION,"Export");
        this.parentSpreadsheet = parent;
        currentDirectory = new File(System.getProperty("user.dir"));
    }

    //-----------------------------------------------------------------------------------
    public void actionPerformed(ActionEvent e) {
        LensedDataMatrix lens = parentSpreadsheet.getLens();
        DataMatrix matrix;
        int selectedRowCount = lens.getRowCount();
        boolean enabledAllRows = false;


        MatrixExporterDialog dialog = new MatrixExporterDialog(selectedRowCount > 0);
        dialog.pack();
        dialog.setVisible(true);
        if (!dialog.isOk())
                return;

        if ((selectedRowCount == 0) && (!dialog.exportAllRows()))
                return;

        if (selectedRowCount == 0) {
            lens.enableAllRows();
            enabledAllRows = true;
        }

        matrix = LensedDataMatrix.toDataMatrix(lens);

        if (dialog.transpose()) {
            LensedDataMatrix.transposeMatrix(matrix);
        }

        dialog.dispose();



        String matrixAsString = matrix.toString();
        JFileChooser chooser = new JFileChooser(currentDirectory);

        if (chooser.showSaveDialog(parentSpreadsheet) == JFileChooser.APPROVE_OPTION) {
            currentDirectory = chooser.getCurrentDirectory();
            File matrixFile = chooser.getSelectedFile();
            try {
                FileWriter matrixFileWriter = new FileWriter(matrixFile);
                matrixFileWriter.write(matrixAsString);
                matrixFileWriter.close();
                if (enabledAllRows)
                    lens.disableAllRows();
            } // try
            catch (IOException exc) {
                JOptionPane.showMessageDialog(null, exc.toString(),
                        "Error Writing to \"" + matrixFile.getName() + "\"",
                        JOptionPane.ERROR_MESSAGE);
            } // catch
        } // if chooser ->  save

    } // actionPerformed
//---------------------------------------------------------------------------------
} // class MatrixExporter
