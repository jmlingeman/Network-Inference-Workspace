package org.systemsbiology.gaggle.geese.common;

import org.systemsbiology.gaggle.core.Boss;


/**
 * Interface through which to notify of connection and disconnection events.
 * @author cbare
 * @see RmiGaggleConnector
 */
public interface GaggleConnectionListener {

    /**
     * notify the listener that the goose has either
     * connected to the boss or disconnected.
     */
    public void setConnected(boolean connected, Boss boss);
}
