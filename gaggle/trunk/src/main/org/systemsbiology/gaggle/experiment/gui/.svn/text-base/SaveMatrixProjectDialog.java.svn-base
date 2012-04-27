package org.systemsbiology.gaggle.experiment.gui;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class SaveMatrixProjectDialog extends JDialog {

    JTextField nameField = new JTextField();
    JTextArea descriptionField = new JTextArea(10,50);
    boolean ok = false;

    public SaveMatrixProjectDialog() {
        setTitle("Save The Currently Open Matrices");
        setModal(true);
        FormLayout layout = new FormLayout("4in",
                "0.2in, 0.2in, 0.2in, 2in, 0.4in");
        CellConstraints cc = new CellConstraints();
        PanelBuilder builder = new PanelBuilder(layout);
        builder.add(new JLabel("Choose a name for this project:"), cc.xy(1,1));
        builder.add(nameField, cc.xy(1,2));
        builder.add(new JLabel("Enter a description:"), cc.xy(1,3));
        builder.add(new JScrollPane(descriptionField), cc.xy(1,4));

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (nameField.getText().trim().equals("") ||
                        descriptionField.getText().trim().equals("")) {
                    JOptionPane.showMessageDialog(SaveMatrixProjectDialog.this,
                            "Name and description are required!");
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

        builder.add(ButtonBarFactory.buildCenteredBar(okButton, cancelButton),cc.xy(1,5));

        this.setContentPane(builder.getPanel());
    }

    public boolean isOk() {
        return ok;
    }

    public String getName() {
        return nameField.getText().trim();
    }

    public String getDescription() {
        return descriptionField.getText().trim();
    }

    public void setDescription(String description) {
        descriptionField.setText(description.trim());
    }

    public static void main(String[] args) {
        SaveMatrixProjectDialog dialog = new SaveMatrixProjectDialog();
        dialog.pack();
        dialog.setVisible(true);

        System.out.println("ok? " + dialog.isOk());
        if (dialog.isOk()) {
            System.out.println("name = " + dialog.getName());
            System.out.println("description = \n" + dialog.getDescription());
        }

        System.exit(0);
    }


}
