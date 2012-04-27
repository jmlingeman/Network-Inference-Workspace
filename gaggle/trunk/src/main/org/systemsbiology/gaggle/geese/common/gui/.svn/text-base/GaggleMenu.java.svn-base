package org.systemsbiology.gaggle.geese.common.gui;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.systemsbiology.gaggle.geese.common.GaggleConnectionListener;
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;


/**
 * A JMenu that allows connecting to and disconnecting from the
 * Gaggle Boss.
 * @author cbare
 */
public class GaggleMenu extends JMenu {
    RmiGaggleConnector connector;

    /**
     * client code should use GuiWidgetFactory.getGaggleMenu()
     * @param connector
     */
    GaggleMenu(RmiGaggleConnector connector) {
        super("Gaggle");
        setMnemonic('G');
        this.connector = connector;
    }

    /**
     * Adds a new menu item with properties taken from the given action. Also,
     * if the action implements GaggleConnectioListener, adds it as a listener to
     * the connector so it will automatically enable and disable on connection and
     * disconnection events.
     * @param action
     */
    public JMenuItem add(Action action) {
        if (action instanceof GaggleConnectionListener) {
            connector.addListener((GaggleConnectionListener)action);
        }
        return add(new JMenuItem(action));
    }

    /**
     * Adds a new menu item. Also, if the menu item's action implements
     * GaggleConnectioListener, adds it as a listener to
     * the connector so it will automatically enable and disable on connection and
     * disconnection events.
     * @param menuItem A menu item
     */
    public JMenuItem add(JMenuItem menuItem) {
        Action action = menuItem.getAction();
        if (action != null && action instanceof GaggleConnectionListener) {
            connector.addListener((GaggleConnectionListener)action);
        }
        return super.add(menuItem);
    }

}
