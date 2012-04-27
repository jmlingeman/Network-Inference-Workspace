package org.systemsbiology.gaggle.geese.cy.util;

import cytoscape.GraphObjAttributes;
import cytoscape.CytoscapeWindow;
import org.systemsbiology.gaggle.core.datatypes.Tuple;
import org.systemsbiology.gaggle.core.datatypes.GaggleTuple;

import javax.swing.text.JTextComponent;

/*
* Copyright (C) 2007 by Institute for Systems Biology,
* Seattle, Washington, USA.  All rights reserved.
*
* This source code is distributed under the GNU Lesser
* General Public License, the text of which is available at:
*   http://www.gnu.org/copyleft/lesser.html
*/
public class MovieUtil {

    /**
     * This is for cytoscape 1.x
     * @param cw
     * @param mapNameReadout
     * @param gaggleTuple
     */
    public static void playMovieFrame(CytoscapeWindow cw, JTextComponent mapNameReadout, GaggleTuple gaggleTuple) {
        GraphObjAttributes goa = cw.getNodeAttributes();

        // todo - validate movie

        String condition = (String)gaggleTuple.getMetadata().getSingleAt(0).getValue();
        mapNameReadout.setText(condition); // do this before or after changing goa?
        for (int i = 0; i < gaggleTuple.getData().getSingleList().size(); i++) {
            Tuple tuple = (Tuple)gaggleTuple.getData().getSingleAt(i).getValue();
            String node = (String)tuple.getSingleAt(0).getValue();
            String attribute = (String)tuple.getSingleAt(1).getValue();
            Object valueObject = tuple.getSingleAt(2).getValue();

            if (valueObject instanceof Double) {
                Double value = (Double)valueObject;
                if (Double.isInfinite(value)) {
                    value = 0.0;
                }
                goa.set(attribute, node, value);
            } else if (valueObject instanceof Integer) {
                Integer value = (Integer)valueObject;
                goa.set(attribute, node, value);
            } else if (valueObject instanceof String) {
                String value = (String)valueObject;
                goa.set(attribute, node, value);
            } else {
                throw new RuntimeException("Got a movie frame of the wrong type!");
            }
        }

        cw.redrawGraph();


    }

}
