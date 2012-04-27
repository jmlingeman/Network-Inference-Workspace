// Boss.java
// define the interface
//---------------------------------------------------------------------------------
/*
 * Copyright (C) 2007 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.core;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.systemsbiology.gaggle.core.datatypes.*;
import org.systemsbiology.gaggle.geese.DeafGoose;
import org.systemsbiology.gaggle.util.NewNameHelper;

/**
 * The core interface for the Gaggle Boss. Any software that wishes to coordinate
 * communication between one or more Gaggled applications (geese) must implement this.
 * Currently the Gaggle uses Java RMI for communication between Geese and the Boss.
 * Other (non-language-dependent) options are being considered.
 *
 * Implementations of Boss are desgined to be compiled by rmic into stubs which
 * can communicate across JVMs. Hence every method must throw RemoteException.

 */
public interface Boss extends Remote {
    /**
     * Adds a new goose to the Gaggle
     * @param goose The goose to be added
     * @return The uniquified name of the new goose
     * @throws RemoteException if RMI communication fails
     */
    public String register(Goose goose) throws RemoteException;

    /**
     * Called when the named goose is to be removed from the gaggle.
     * @param gooseName The name of the goose to remove
     * @throws RemoteException if RMI communication fails
     */
    public void unregister(String gooseName) throws RemoteException;

    /**
     * @deprecated All code involving DeafGoose will be removed.
     * @param deafGoose The DeafGoose
     * @return the uniquified name of the DeafGoose
     * @throws RemoteException if RMI communication fails
     */
    public String register(DeafGoose deafGoose) throws RemoteException;

    /**
     * Tells the boss to broadcast a Namelist object.
     * @param sourceGoose The name of the goose originating the broadcast
     * @param targetGoose The name of the goose to receive the broadcast. If this is "boss",
     * all listening geese will receive the broadcast.
     * @param nameList The NameList object
     * @throws RemoteException if RMI communication fails
     */
    public void broadcastNamelist(String sourceGoose, String targetGoose, Namelist nameList) throws RemoteException;

    /**
     * Tells the boss to broadcast a DataMatrix object.
     * @param sourceGoose The name of the goose originating the broadcast
     * @param targetGoose The name of the goose to receive the broadcast. If this is "boss",
     * all listening geese will receive the broadcast.
     * @param matrix The DataMatrix object
     * @throws RemoteException if RMI communication fails
     */
    public void broadcastMatrix(String sourceGoose, String targetGoose, DataMatrix matrix) throws RemoteException;

    /**
     * Tells the boss to broadcast a GaggleTuple object.
     * @param sourceGoose The name of the goose originating the broadcast
     * @param targetGoose The name of the goose to receive the broadcast. If this is "boss",
     * all listening geese will receive the broadcast.
     * @param gaggleTuple the GaggleTuple object
     * @throws RemoteException if RMI communication fails
     */
    public void broadcastTuple(String sourceGoose, String targetGoose, GaggleTuple gaggleTuple) throws RemoteException;

    /**
     * Tells the boss to broadcast a Cluster object.
     * @param sourceGoose The name of the goose originating the broadcast
     * @param targetGoose The name of the goose to receive the broadcast. If this is "boss",
     * all listening geese will receive the broadcast.
     * @param cluster the Cluster object
     * @throws RemoteException if RMI communication fails
     */
    public void broadcastCluster(String sourceGoose, String targetGoose, Cluster cluster) throws RemoteException;

    /**
     * Tells the boss to broadcast a Network object.
     * @param sourceGoose The name of the goose originating the broadcast
     * @param targetGoose The name of the goose to receive the broadcast. If this is "boss",
     * all listening geese will receive the broadcast.
     * @param network the Network object
     * @throws RemoteException if RMI communication fails
     */
    public void broadcastNetwork(String sourceGoose, String targetGoose, Network network) throws RemoteException;

    /**
     * Tells the boss to hide the specified goose.
     * @param gooseName The name of the goose to hide
     * @throws RemoteException  if RMI communication fails
     */
    public void hide(String gooseName) throws RemoteException;

    /**
     * Tells the boss to show the specified goose.
     * @param gooseName The name of the goose to show
     * @throws RemoteException if RMI communication fails
     */
    public void show(String gooseName) throws RemoteException;


    /**
     * @deprecated Goose.update() will automatically provide you with the latest list of goose names.
     * @return list of currently connected geese names
     * @throws RemoteException if there is an RMI connection error
     */
    public String[] getGooseNames() throws RemoteException; // todo - this can be removed when all geese implement update()


    public String renameGoose(String oldName, String newName) throws RemoteException;
    /**
     * @deprecated Eventually there will be a new way to handle this
     * @return a NewNameHelper object
     * @throws RemoteException if RMI communication fails
     */
    public NewNameHelper getNameHelper() throws RemoteException; // eventually there will be a new way to handle this

    /**
     * Terminates the specified goose.
     * @param gooseName The name of the goose to terminate
     * @throws RemoteException if RMI communication fails
     */
    public void terminate(String gooseName) throws RemoteException;

}

