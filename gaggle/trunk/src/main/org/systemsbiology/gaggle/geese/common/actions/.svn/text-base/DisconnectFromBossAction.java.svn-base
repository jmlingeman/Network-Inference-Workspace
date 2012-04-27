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
 * Disconnect a goose from the Gaggle
 * @author cbare
 */
public class DisconnectFromBossAction extends AbstractAction implements GaggleConnectionListener {
    private RmiGaggleConnector connector;

    public DisconnectFromBossAction(RmiGaggleConnector connector) {
        super("Disconnect from Boss");
        this.putValue(Action.SHORT_DESCRIPTION, "Disconnect from the Gaggle Boss");
        this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                KeyEvent.VK_D, InputEvent.META_DOWN_MASK));
        this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
        this.connector = connector;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        connector.disconnectFromGaggle(true);
        // todo - how does this action remove a connection listener?
    }

    public void setConnected(boolean connected, Boss boss) {
        setEnabled(connected);
    }
}