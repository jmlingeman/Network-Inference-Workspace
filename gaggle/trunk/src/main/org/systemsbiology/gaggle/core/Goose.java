// Goose.java
//-------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.core;

import java.rmi.*;

import org.systemsbiology.gaggle.core.datatypes.*;

/**
 * The core client interface for the Gaggle - any software which needs to communicate
 * with the gaggle must implement this interface.
 *
 * Implementations of Goose are desgined to be compiled by rmic into stubs which
 * can communicate across JVMs. Hence every method must throw RemoteException.
 * 
 * @see org.systemsbiology.gaggle.geese.sample.SampleGoose for a "canonical" implementation
 */
public interface Goose extends Remote {
    /**
     * Called when the goose receives a NameList object
     * @param source Optional string indicating name of source goose
     * @param nameList The NameList object
     * @throws RemoteException if RMI communication fails
     */
    public void handleNameList(String source, Namelist nameList) throws RemoteException;

    /**
     * Called when the goose receives a DataMatrix object
     * @param source Optional string indicating name of source goose
     * @param matrix The DataMatrix object
     * @throws RemoteException if RMI communication fails
     */
    public void handleMatrix(String source, DataMatrix matrix) throws RemoteException;


    /**
     * Called when the goose receives a GaggleTuple object
     * @param source Optional string indicating name of source goose
     * @param gaggleTuple The GaggleTuple object
     * @throws RemoteException if RMI communication fails
     */
    public void handleTuple(String source, GaggleTuple gaggleTuple) throws RemoteException;


    /**
     * Called when the goose receives a Cluster object
     * @param source Optional string indicating name of source goose
     * @param cluster The Cluster object
     * @throws RemoteException if RMI communication fails
     */
    public void handleCluster(String source, Cluster cluster) throws RemoteException;

    /**
     * Called when the goose receives a Network object
     * @param source Optional string indicating name of source goose
     * @param network The Network object
     * @throws RemoteException if RMI communication fails
     */
    public void handleNetwork(String source, Network network) throws RemoteException;


    /**
     * Provides the goose with an updated list of the names of active geese. This list
     * includes the name of the goose receiving the list.
     * @see org.systemsbiology.gaggle.util.MiscUtil#updateGooseChooser(javax.swing.JComboBox gooseChooser, String callingGoose, String[] allGeese);
     * @param gooseNames the names of the currently active geese
     * @throws RemoteException if RMI communication fails
     */
    public void update(String[] gooseNames) throws RemoteException;

    /**
     * Returns the name of the goose.
     * @return the name of the goose
     * @throws RemoteException if RMI communication fails
     */
    public String getName() throws RemoteException;

    /**
     * Sets the name of (renames) the goose.
     * @param newName The new name of the goose
     * @throws RemoteException if RMI communication fails
     */
    public void setName(String newName) throws RemoteException;

    /**
     * @deprecated This is no longer used.
     * @throws RemoteException if RMI communication fails
     */
    public void doBroadcastList() throws RemoteException; // todo - remove

    /**
     * Tells the goose to hide itself
     * @throws RemoteException if RMI communication fails
     */
    public void doHide() throws RemoteException;

    /**
     * Tells the goose to show itself
     * @throws RemoteException if RMI communication fails
     */
    public void doShow() throws RemoteException;

    /**
     * Tells the goose to exit (terminate).
     * @throws RemoteException if RMI communication fails
     */
    public void doExit() throws RemoteException;

}

