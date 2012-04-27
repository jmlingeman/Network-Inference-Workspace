package org.systemsbiology.gaggle.geese.common.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;
import org.systemsbiology.gaggle.geese.common.actions.*;


/**
 * Factory for Gaggle related GUI widgets:
 * Gaggle menu, Goose Chooser, and associated actions for connecting,
 * disconnecting, and updating.
 * 
 * The goal is to allow code for the widgets to be shared.
 * 
 * Usage pattern is this:
 * 
 *   * The goose creates a connector and registers itself as
 *     a GaggleConnectionListener in order to be notified of
 *     connect and disconnect events, and to get a reference
 *     to the Boss.
 *     
 *   * The goose creates a GuiWidgetFactory and adds the
 *     GaggleMenu and GooseChooser controls to its UI.
 *     
 * <pre>
 *   gaggleConnector = new RmiGaggleConnector(this);
 *   gaggleConnector.addListener(this);
 *   factory = new GuiWidgetFactory(gaggleConnector, this);
 *   
 *   gooseChooser = factory.getGooseChooser();
 *   toolbar.add (gooseChooser);
 *
 *   gooseChooser.addActionListener (new ActionListener () {
 *     public void actionPerformed (ActionEvent e) {
 *       // ...
 *     }});
 *
 *   menubar.add(factory.getGaggleMenu());
 * </pre>
 * 
 * @author cbare
 */
public class GuiWidgetFactory {
    
    private GooseChooserComboBox gooseChooser;
    private JMenu gaggleMenu;
    private ConnectToBossAction connectToBossAction;
    private DisconnectFromBossAction disconnectFromBossAction;
    private UpdateGooseChooserAction updateGooseChooserAction;
    private RmiGaggleConnector connector;
    private JMenuItem connectMenuItem;
    private JMenuItem disconnectMenuItem;
    private JMenuItem updateMenuItem;
    private ShowGooseAction showGooseAction;
    private HideGooseAction hideGooseAction;
    

    public GuiWidgetFactory(RmiGaggleConnector connector, Goose goose) {
        this.connector = connector;
        
        gooseChooser = new GooseChooserComboBox(goose);
        connector.addListener(gooseChooser);

        connectToBossAction = new ConnectToBossAction(connector);
        disconnectFromBossAction = new DisconnectFromBossAction(connector);
        
        updateGooseChooserAction = new UpdateGooseChooserAction();
        updateGooseChooserAction.setGooseChooser(gooseChooser);
        
        showGooseAction = new ShowGooseAction();
        connector.addListener(showGooseAction);
        
        hideGooseAction = new HideGooseAction();
        connector.addListener(hideGooseAction);

        // set an action listener that will notify the hide and show buttons
        // when the target goose is selected.
        gooseChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showGooseAction.setTargetGoose(gooseChooser.getTargetGoose());
                hideGooseAction.setTargetGoose(gooseChooser.getTargetGoose());
            }
        });

        createGaggleMenu();
    }

    private void createGaggleMenu() {
        gaggleMenu = new GaggleMenu(connector);
        
        connectMenuItem = new JMenuItem(connectToBossAction);
        gaggleMenu.add(connectMenuItem);

        disconnectMenuItem = new JMenuItem(disconnectFromBossAction);
        gaggleMenu.add(disconnectMenuItem);

        // all geese who use this had better implement update() or they will have no way to update.
        //updateMenuItem = new JMenuItem(updateGooseChooserAction);
        //gaggleMenu.add(updateMenuItem);
    }

    
    public JComboBox getGooseChooser() {
        return gooseChooser;
    }

    public JMenu getGaggleMenu() {
        return gaggleMenu;
    }

    public Action getUpdateAction() {
        return updateGooseChooserAction;
    }

    public Action getConnectAction() {
        return connectToBossAction;
    }

    public Action getDisconnectAction() {
        return disconnectFromBossAction;
    }
    
    public Action getShowGooseAction() {
        return showGooseAction;
    }
    
    public Action getHideGooseAction() {
        return hideGooseAction;
    }
}
