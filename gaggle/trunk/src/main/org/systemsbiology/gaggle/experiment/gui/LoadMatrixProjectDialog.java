package org.systemsbiology.gaggle.experiment.gui;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;

import javax.swing.*;

import org.systemsbiology.gaggle.experiment.datamatrix.MatrixProject;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class LoadMatrixProjectDialog extends JDialog  {
    private boolean ok = false;
    JCheckBox checkBox;
    JList list;

    public LoadMatrixProjectDialog(MatrixProject[] projects) {
        setTitle("Load Saved Matrix Project");
        setModal(true);
        FormLayout layout = new FormLayout("3in",
                "0.2in, 1dlu, 3in, 1dlu, 0.2in, 1dlu, 0.5in");
        CellConstraints cc = new CellConstraints();
        PanelBuilder builder = new PanelBuilder(layout);
        builder.add(new JLabel("Choose a project to load:"), cc.xy(1,1));
        list = new JList(new LoadMatrixProjectListModel(projects)){

            public String getToolTipText(MouseEvent event) {
                String tip;
                java.awt.Point p = event.getPoint();
                int rowIndex = locationToIndex(p);
                tip = ((LoadMatrixProjectListModel)getModel()).getProjects()[rowIndex].toString();
                tip = "<html>" + tip.replaceAll("\n", "<br>\n") + "</html>";
                return tip;
            }
        };
        builder.add(new JScrollPane(list), cc.xy(1,3));
        checkBox = new JCheckBox("Close all open tabs first");
        checkBox.setToolTipText("<html>If checked, closes all open tabs in the DMV<br>(if any are open)<br>before loading your project.</html>");
        builder.add(checkBox,cc.xy(1,5));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (list.getSelectedIndex() == -1) {
                    JOptionPane.showMessageDialog(LoadMatrixProjectDialog.this,
                            "You must select a project!");
                    return;
                }
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
        builder.add(buttonBar, cc.xy(1,7));
        //FormDebugUtils.dumpAll(builder.getPanel());


        this.setContentPane(builder.getPanel());

    }

    public boolean closeAllTabsFirst() {
        return checkBox.isSelected();
    }

    public boolean isOk() {
        return ok;
    }

    public MatrixProject getSelectedProject() {
        int index = list.getSelectedIndex();
        return ((LoadMatrixProjectListModel)list.getModel()).getProjects()[index];
    }


    public static void main(String[] args) {
        /*
        MatrixProject[] projects = new MatrixProject[2];
        MatrixProject p1 = new MatrixProject();
        p1.setDate(new Date());
        p1.setDescription("descr1\non\n3lines");
        p1.setName("p1");
        p1.setViews(new DataMatrix[0]);
        projects[0] = p1;

        MatrixProject p2 = new MatrixProject();
        p2.setDate(new Date());
        p2.setDescription("descr2\non\n4lines\nbla");
        p2.setName("p2");
        p2.setViews(new DataMatrix[0]);
        projects[1] = p2;


        LoadMatrixProjectDialog dialog = new LoadMatrixProjectDialog(projects);
        dialog.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        dialog.pack();
        dialog.setVisible(true);

        System.out.println("ok? " + dialog.isOk());
        if (dialog.isOk()) {
            System.out.println("close all tabs first? " + dialog.closeAllTabsFirst());
            System.out.println("Selected project:\n" + dialog.getSelectedProject().toString());
        }
        System.exit(0);
        */
    }
}
