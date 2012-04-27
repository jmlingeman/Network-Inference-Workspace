package org.systemsbiology.gaggle.experiment.gui;

import org.systemsbiology.gaggle.experiment.metadata.MetaDataList;
import org.systemsbiology.gaggle.experiment.metadata.Condition;

import javax.swing.*;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.builder.PanelBuilder;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;

/*
* Copyright (C) 2007 by Institute for Systems Biology,
* Seattle, Washington, USA.  All rights reserved.
*
* This source code is distributed under the GNU Lesser
* General Public License, the text of which is available at:
*   http://www.gnu.org/copyleft/lesser.html
*/
public class SearchResultsFrame extends JFrame {
    String[] tags;
    Condition[] results;
    String[] resultNames;
    TreeDataViewer tdv;
    JTable table;

    public SearchResultsFrame(String[] tags, Condition[] results, TreeDataViewer tdv) {
        this.tags = tags;
        this.results = results;
        this.tdv = tdv;

        List<String> listOfConditionNames = new  ArrayList<String>();
        for (Condition cond : results) {
            listOfConditionNames.add(cond.getAlias());
        }
        resultNames = listOfConditionNames.toArray(new String[0]);

        String title = "Found " + results.length + " conditions matching ";
        for (int i = 0; i < tags.length; i++) {
            title += "'" + tags[i] + "'";
            if (i < (tags.length -1)) {
                title += ", ";
            }
        }

        setTitle(title);
        FormLayout layout = new FormLayout("1in, 3dlu, 1.5in, 3dlu, 1.5in, 3dlu, 1.5in",
                "0.5in, 3dlu, 0.5in, 3dlu, 0.5in, 3dlu, 0.5in");
        CellConstraints cc = new CellConstraints();
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        JButton showButton = new JButton("Show");
        showButton.setToolTipText("Highlight these conditions in the Data Matrix Viewer");
        builder.add(showButton, cc.xy(1,1));
        JButton clearButton = new JButton("Clear");
        clearButton.setToolTipText("Clear selections");
        builder.add(clearButton, cc.xy(1,3));


        table = new JTable(new SearchResultsTableModel(results)){

            public String getToolTipText(MouseEvent e) {
                String tip;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                tip = ((SearchResultsTableModel)getModel()).getResults()[rowIndex].toString();
                tip = "<html>" + tip.replaceAll("\n", "<br>\n") + "</html>";
                return tip;
            }

        };
        JScrollPane tableScrollPane = new JScrollPane(table);
        builder.add(tableScrollPane,cc.xywh(3,1,5,5));

        clearButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                table.clearSelection();
            }
        });

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                SearchResultsFrame.this.dispose();
            }
        });
        builder.add(closeButton, cc.xy(5,7));

        showButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // first clear tree selections
                SearchResultsFrame.this.tdv.treeWidget.clearSelection();
                // collapse the tree...
                SearchResultsFrame.this.tdv.expandOrCollapseTree(false);

                // now figure out which rows we want to get treepaths for
                String[] whatToSend;
                if (table.getSelectedRowCount() == 0) { // if nothing is selected,
                    whatToSend = resultNames; // send everything
                } else {
                    whatToSend = new String[table.getSelectedRowCount()];
                    for (int i = 0; i < table.getSelectedRowCount(); i++) {
                        whatToSend[i] = (String)table.getValueAt(table.getSelectedRows()[i],0);
                    }
                }


                // now need to work backward from condition name and find the
                // corresponding tree node, make a list of those nodes
                // and select them
                try {
                    MetaDataList mdl = new MetaDataList(SearchResultsFrame.this.tdv.repository);
                    SearchResultsFrame.this.tdv.treeWidget.
                            setSelectionPaths(mdl.getTreePathsForConditionNames(whatToSend,
                                    SearchResultsFrame.this.tdv.treeWidget));
                    SearchResultsFrame.this.tdv.jframe.setAlwaysOnTop(true);
                    SearchResultsFrame.this.tdv.jframe.setVisible(true);
                    SearchResultsFrame.this.tdv.jframe.setAlwaysOnTop(false);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(SearchResultsFrame.this, "An error occurred during your search.");
                }
            }
        });

        setContentPane(builder.getPanel());
    }


    // when run from here, it can't send data back to the DMV
    public static void main(String[] args) {
        try {
            //String[] tags = new String[] {"cobalt","wild type"};
            String[] tags = new String[] {"VNG1179", "cU"};
            //String repos = "file:///Users/dtenenbaum/emi-sandbox/halobacterium/repos";
            String repos = "file:///net/arrays/emi/halobacterium/repos";
            MetaDataList mdl = new MetaDataList(repos);
            Condition[] results = mdl.filterMetaDataByTags(tags);
            System.out.println("# of results: " + results.length);
            SearchResultsFrame frame = new SearchResultsFrame(tags, results, new TreeDataViewer());
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
