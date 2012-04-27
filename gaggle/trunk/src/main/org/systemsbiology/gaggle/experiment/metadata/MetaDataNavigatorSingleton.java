/*
* Copyright (C) 2007 by Institute for Systems Biology,
* Seattle, Washington, USA.  All rights reserved.
*
* This source code is distributed under the GNU Lesser
* General Public License, the text of which is available at:
*   http://www.gnu.org/copyleft/lesser.html
*/
package org.systemsbiology.gaggle.experiment.metadata;

public class MetaDataNavigatorSingleton {
    private static MetaDataNavigatorSingleton ourInstance = new MetaDataNavigatorSingleton();
    private MetaDataNavigator navigator;

    public static MetaDataNavigatorSingleton getInstance() {
        return ourInstance;
    }

    private MetaDataNavigatorSingleton() {
    }


    public MetaDataNavigator getNavigator() {
        return navigator;
    }

    public void setNavigator(MetaDataNavigator navigator) {
        this.navigator = navigator;
    }
}
