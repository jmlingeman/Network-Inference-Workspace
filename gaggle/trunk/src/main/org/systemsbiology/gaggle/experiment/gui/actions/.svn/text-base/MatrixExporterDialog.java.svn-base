package org.systemsbiology.gaggle.experiment.gui.actions;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

import org.systemsbiology.gaggle.util.MiscUtil;

/*
* Copyright (C) 2007 by Institute for Systems Biology,
* Seattle, Washington, USA.  All rights reserved.
*
* This source code is distributed under the GNU Lesser
* General Public License, the text of which is available at:
*   http://www.gnu.org/copyleft/lesser.html
*/
public class MatrixExporterDialog extends JDialog {

    private boolean ok;
    private JCheckBox exportAllRowsCheckbox;
    private JCheckBox transposeMatrixCheckbox;

    public MatrixExporterDialog(boolean rowsAreSelected) {
        placeInCenter();
        setTitle("Export Matrix Data");
        setModal(true);
        FormLayout layout = new FormLayout("5in",
                "0.5in, 3dlu, 0.5in, 3dlu, 0.5in");
        CellConstraints cc = new CellConstraints();
        PanelBuilder builder = new PanelBuilder(layout);
        exportAllRowsCheckbox = new JCheckBox("No rows are selected; export all rows?",true);
        transposeMatrixCheckbox = new JCheckBox("Transpose rows and columns before export?");

        builder.add(exportAllRowsCheckbox, cc.xy(1,1));
        if (rowsAreSelected)
            exportAllRowsCheckbox.setEnabled(false);
        builder.add(transposeMatrixCheckbox, cc.xy(1,3));

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok = true;
                setVisible(false);
            }
        });


        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok = false;
                setVisible(false);
            }
        });


        JPanel buttonBar = ButtonBarFactory.buildCenteredBar(cancelButton, okButton);
        builder.add(buttonBar, cc.xy(1,5));

        this.setContentPane(builder.getPanel());

    }

    public boolean transpose() {
        return transposeMatrixCheckbox.isSelected();
    }

    public boolean exportAllRows() {
        return exportAllRowsCheckbox.isSelected();
    }

    public boolean isOk() {
        return ok;
    }


    private void placeInCenter() {
        GraphicsConfiguration gc = getGraphicsConfiguration();
        int screenHeight = (int) gc.getBounds().getHeight();
        int screenWidth = (int) gc.getBounds().getWidth();
        int windowWidth = getWidth();
        int windowHeight = getHeight();
        setLocation((screenWidth - windowWidth) / 2, (screenHeight - windowHeight) / 2);

    } // placeInCenter


    public static void main(String[] args) {
        MatrixExporterDialog dialog = new MatrixExporterDialog(true);
        dialog.pack();
        dialog.setVisible(true);
    }
}
