package org.systemsbiology.gaggle.geese.echo;

import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.datatypes.*;
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;
import org.systemsbiology.gaggle.geese.common.GooseShutdownHook;
import org.systemsbiology.gaggle.util.MiscUtil;

import javax.swing.*;
import java.rmi.RemoteException;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/*
* Copyright (C) 2007 by Institute for Systems Biology,
* Seattle, Washington, USA.  All rights reserved.
*
* This source code is distributed under the GNU Lesser
* General Public License, the text of which is available at:
*   http://www.gnu.org/copyleft/lesser.html
*/
public class EchoGoose extends JFrame implements Goose, WindowListener {

    RmiGaggleConnector connector = new RmiGaggleConnector(this);
    String myGaggleName = "Echo";
    Boss boss;

    public EchoGoose() {
        super("Echo Goose");
        new GooseShutdownHook(connector);
        addWindowListener(this);
        MiscUtil.setApplicationIcon(this);
        
        try {
            connectToGaggle();
        }
        catch (Exception ex0) {
            System.err.println("EchoGoose failed to connect to gaggle: " + ex0.getMessage());
        }
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Echo Goose is running");
        panel.add(label);
        add(panel);
        setSize(200, 200);
        setVisible(true);
    }

    public void connectToGaggle() {
        try {
            connector.connectToGaggle();
        }
        catch (Exception ex0) {
            System.err.println("failed to connect to gaggle: " + ex0.getMessage());
            ex0.printStackTrace();
        }
        boss = connector.getBoss();
    }


    public void handleNameList(String source, Namelist nameList) throws RemoteException {
        System.out.println("Received a Namelist from " + source + ", sending it back....");
        boss.broadcastNamelist(myGaggleName, source, nameList);
    }

    public void handleMatrix(String source, DataMatrix matrix) throws RemoteException {
        System.out.println("Received a DataMatrix from " + source + ", sending it back....");
        boss.broadcastMatrix(myGaggleName, source, matrix);
    }

    public void handleTuple(String source, GaggleTuple gaggleTuple) throws RemoteException {
        System.out.println("Received a GaggleTuple from " + source + ", sending it back....");
        boss.broadcastTuple(myGaggleName, source, gaggleTuple);
    }

    public void handleCluster(String source, Cluster cluster) throws RemoteException {
        System.out.println("Received a Cluster from " + source + ", sending it back....");
        boss.broadcastCluster(myGaggleName, source, cluster);
    }

    public void handleNetwork(String source, Network network) throws RemoteException {
        System.out.println("Received a Network from " + source + ", sending it back....");
        boss.broadcastNetwork(myGaggleName, source, network);
    }

    public void update(String[] gooseNames) throws RemoteException {
    }

    public String getName() {
        return myGaggleName;
    }

    public void setName(String newName) {
        myGaggleName = newName;
        setTitle(myGaggleName);
    }

    public void doBroadcastList() throws RemoteException { // todo - remove
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void doHide() throws RemoteException {
    }

    public void doShow() throws RemoteException {
    }

    public void doExit() /*throws RemoteException*/ {
        System.out.println("Received doExit() command, exiting....");
        System.exit(0);
        System.out.println("what is this?");
    }


    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        doExit();
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public static void main(String[] args) {
        System.out.println("Starting echo goose...");
        new EchoGoose();
    }
}
