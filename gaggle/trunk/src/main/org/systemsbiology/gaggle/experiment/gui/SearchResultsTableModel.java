package org.systemsbiology.gaggle.experiment.gui;

import org.systemsbiology.gaggle.experiment.metadata.Condition;

import javax.swing.table.AbstractTableModel;
import java.util.Arrays;
import java.util.Comparator;

/*
* Copyright (C) 2007 by Institute for Systems Biology,
* Seattle, Washington, USA.  All rights reserved.
*
* This source code is distributed under the GNU Lesser
* General Public License, the text of which is available at:
*   http://www.gnu.org/copyleft/lesser.html
*/
public class SearchResultsTableModel extends AbstractTableModel {

    private Condition[] results;



    public SearchResultsTableModel(Condition[] results) {
        this.results = results;
        Arrays.sort(results, new Comparator<Condition>(){
            public int compare(Condition o, Condition o1) {
                return o.getAlias().compareToIgnoreCase(o1.getAlias());
            }
        });
        
    }

    public int getRowCount() {
        return results.length;
    }


    public String getColumnName(int i) {
        return "Condition Name";
    }

    public int getColumnCount() {
        return 1;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return results[rowIndex].getAlias();
    }

    public Condition[] getResults() {
        return results;
    }

    


}
