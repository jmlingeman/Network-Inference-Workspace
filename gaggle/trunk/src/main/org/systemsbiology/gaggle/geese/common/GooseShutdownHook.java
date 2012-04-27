package org.systemsbiology.gaggle.geese.common;

/*
* Copyright (C) 2007 by Institute for Systems Biology,
* Seattle, Washington, USA.  All rights reserved.
*
* This source code is distributed under the GNU Lesser
* General Public License, the text of which is available at:
*   http://www.gnu.org/copyleft/lesser.html
*/

/**
 * Provides a shutdown hook to ensure that the boss is notified when
 * a goose shuts down.  All you have to do is put the following
 * in your initialization code after you have instantiated an RmiGaggleConnector:
 * <code>
 *      new GooseShutdownHook(rmiGaggleConnector);
 * </code>
 * todo - make a boss shutdown hook?
 */
public class GooseShutdownHook extends Thread {
    RmiGaggleConnector connector;

    public GooseShutdownHook(RmiGaggleConnector connector) {
        this.connector = connector;
        Runtime.getRuntime().addShutdownHook(this);
    }

    public void run() {
        System.out.println("about to shut down, are we connected? " + connector.isConnected());
        if (connector.isConnected()) {
            connector.disconnectFromGaggle(true);
        }
    }
}
