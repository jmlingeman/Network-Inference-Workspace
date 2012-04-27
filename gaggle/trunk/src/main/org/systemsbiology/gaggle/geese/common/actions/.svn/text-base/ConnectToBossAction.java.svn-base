/**
 * 
 */
package org.systemsbiology.gaggle.geese.common.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.geese.common.GaggleConnectionListener;
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;

/**
 * Connect a Goose to the Gaggle
 * @author cbare
 */
public class ConnectToBossAction extends AbstractAction implements GaggleConnectionListener {
    private RmiGaggleConnector connector;

    public ConnectToBossAction(RmiGaggleConnector connector) {
        super("Connect To Boss");
        this.putValue(Action.SHORT_DESCRIPTION, "Connect to the Gaggle Boss");
        this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                KeyEvent.VK_C, InputEvent.META_DOWN_MASK));
        this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
        this.connector = connector;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {
            connector.connectToGaggle();
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setConnected(boolean connected, Boss boss) {
        setEnabled(!connected);
    }
}