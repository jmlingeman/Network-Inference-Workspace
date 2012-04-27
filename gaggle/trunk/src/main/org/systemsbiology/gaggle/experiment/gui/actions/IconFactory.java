// IconFactory.java
//--------------------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui.actions;

import javax.swing.*;

public class IconFactory {

    static String prefix = "images/";
    
    public static ImageIcon getBroadcastIcon() {
        return createImageIcon(prefix + "broadcast.jpg");
    }
    
    public static ImageIcon getCreateFromSelectionIcon() {
        return createImageIcon(prefix + "create_from_selection.jpg");
    }

    public static ImageIcon getDismissIcon() {
        return createImageIcon(prefix + "dismiss.jpg");
    }

    public static ImageIcon getExpandAndContractIcon() {
        return createImageIcon(prefix + "expand_contract.jpg");
    }

    public static ImageIcon getExportIcon() {
        return createImageIcon(prefix + "export.jpg");
    }

    public static ImageIcon getFindCorrelationsIcon() {
        return createImageIcon(prefix + "find_correlations.jpg");
    }

    public static ImageIcon getFitColumnWidthIcon() {
        return createImageIcon(prefix + "fit_column_width.jpg");
    }

    public static ImageIcon getSelectionsFromNetworkIcon() {
        return createImageIcon(prefix + "get_selections_from_network.jpg");
    }

    public static ImageIcon sendSelectionsToNetworkIcon() {
        return createImageIcon(prefix + "select_in_network.jpg");
    }

    public static ImageIcon getLoadSelectedConditionsIcon() {
        return createImageIcon(prefix + "load_selected_conditions.jpg");
    }

    public static ImageIcon getMovieIcon() {
        return createImageIcon(prefix + "movie.jpg");
    }

    
    public static ImageIcon getPauseIcon() {
        return createImageIcon(prefix + "pause.jpg");
    }

    public static ImageIcon getPlayIcon() {
        return createImageIcon(prefix + "play.jpg");
    }

    public static ImageIcon getPlotIcon() {
        return createImageIcon(prefix + "plot.jpg");
    }

    public static ImageIcon getPythonConsoleIcon() {
        return createImageIcon(prefix + "python_console.jpg");
    }
    
    public static ImageIcon getSelectInNetworkIcon() {
        return createImageIcon(prefix + "select_in_network.jpg");
    }

    public static ImageIcon getSortIcon() {
        return createImageIcon(prefix + "sort.jpg");
    }

    public static ImageIcon getStopIcon() {
        return createImageIcon(prefix + "stop.jpg");
    }
    
    public static ImageIcon getToggleIcon() {
        return createImageIcon(prefix + "toggle.jpg");
    }
    
    public static ImageIcon getVolcanoPlotterIcon() {
        return createImageIcon(prefix + "volcano_plotter.jpg");
    }
    
    public static ImageIcon getQuestionIcon() {
        return createImageIcon(prefix + "question.jpg");
    }
    
    
    
    private static ImageIcon createImageIcon(String path) {
        IconFactory a = new IconFactory();
        java.net.URL imgURL = a.getClass().getResource(path);
        if (null != imgURL) {
          return new ImageIcon(imgURL);
        } else {
          System.err.println("----- Couldn't find file: " + path + " --------");
          System.err.flush();
          return null;
        }
      }
}
