package org.systemsbiology.gaggle.experiment.gui;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.builder.PanelBuilder;

import javax.swing.*;

import org.systemsbiology.gaggle.experiment.metadata.MetaDataList;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/*
* Copyright (C) 2007 by Institute for Systems Biology,
* Seattle, Washington, USA.  All rights reserved.
*
* This source code is distributed under the GNU Lesser
* General Public License, the text of which is available at:
*   http://www.gnu.org/copyleft/lesser.html
*/
public class SearchDialog extends JDialog {
    private boolean ok = false;
    private JComboBox tag1;
    private JComboBox tag2;
    private JComboBox tag3;
    String[] allTags = null;

    public SearchDialog(String uri) {
        setModal(true);
        
        MetaDataList mdl;
        try {
            mdl = new MetaDataList(uri);
            allTags = mdl.getAllTags();
        } catch (Exception e) {
            e.printStackTrace();  //log this
        }

        setTitle("Search Metadata");
        FormLayout layout = new FormLayout("1in, 3dlu, 2in", "0.5in, 3dlu, 0.5in,  3dlu, 0.5in, 3dlu, 0.5in");
        CellConstraints cc = new CellConstraints();
        PanelBuilder builder = new PanelBuilder(layout);
        String toolTip = "<html>Type in part of the value you are searching for.<br>"
                +"Wildcards such as <i>vng*</i> are allowed.</html>";
        JLabel lbl = new JLabel();

        builder.setDefaultDialogBorder();
        lbl.setText("Tag 1:");
        lbl.setToolTipText(toolTip);
        builder.add(lbl, cc.xy(1,1));
        allTags = prependBlank(allTags);


        tag1 = new JComboBox(allTags);
        tag1.setToolTipText(toolTip);
        tag1.setEditable(true);
        AutoCompletion.enable(tag1);

        tag2 = new JComboBox(allTags);
        tag2.setToolTipText(toolTip);
        tag2.setEditable(true);
        AutoCompletion.enable(tag2);

        tag3 = new JComboBox(allTags);
        tag3.setToolTipText(toolTip);
        tag3.setEditable(true);
        AutoCompletion.enable(tag3);



        builder.add(tag1, cc.xy(3,1));

        lbl = new JLabel();
        lbl.setText("Tag 2:");
        lbl.setToolTipText(toolTip);
        builder.add(lbl, cc.xy(1,3));
        builder.add(tag2, cc.xy(3,3));

        lbl = new JLabel();
        lbl.setText("Tag 3:");
        lbl.setToolTipText(toolTip);
        builder.add(lbl, cc.xy(1,5));
        builder.add(tag3, cc.xy(3,5));

        JButton okButton = new JButton("OK");
        okButton.setMnemonic('O');
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (getTags() == null) {
                    JOptionPane.showMessageDialog(SearchDialog.this, "You must choose something to search for!");
                    return;
                }

                String[] lcTags = new String[allTags.length];
                for (int i = 0; i < lcTags.length; i++) {
                    lcTags[i] = allTags[i].toLowerCase();
                }
                for (String tag : getTags()) {
                    if (!tag.endsWith("*")) {
                        int index = Arrays.binarySearch(lcTags, tag);
                        if (index < 0) {
                            JOptionPane.showMessageDialog(SearchDialog.this, "'" + tag + "' is not a valid tag!");
                            return;
                        }
                    }
                }
                

                ok = true;
                setVisible(false);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setMnemonic('C');
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                ok = false;
                setVisible(false);
            }
        });

        builder.add(okButton, cc.xy(1,7));
        builder.add(cancelButton, cc.xy(3,7));



        this.setContentPane(builder.getPanel());

    }

    public boolean okPressed() {
        return ok;
    }

    public String[] getTags() {
        List<String> tags = new ArrayList<String>();
        String item1 = (String)tag1.getSelectedItem();
        if (!item1.trim().equals("")) {
            tags.add(item1.trim());
        }

        String item2 = (String)tag2.getSelectedItem();
        if (!item2.trim().equals("")) {
            tags.add(item2.trim());
        }

        String item3 = (String)tag3.getSelectedItem();
        if (!item3.trim().equals("")) {
            tags.add(item3.trim());
        }

        if (tags.size() == 0) {
            return null;
        }
        return tags.toArray(new String[0]);
    }

    private String[] prependBlank(String[] items) {
        List<String> list = new ArrayList<String>();
        list.add("");
        for (String item : items) {
            list.add(item);
        }
        return list.toArray(new String[0]);
    }

    public static void main(String[] args) {
        //SearchDialog sd = new SearchDialog("file:///Users/dtenenbaum/emi-sandbox/halobacterium/repos");
        SearchDialog sd = new SearchDialog("file:///net/arrays/emi/halobacterium/repos");
        sd.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        sd.pack();
        sd.setVisible(true);
    }

}
