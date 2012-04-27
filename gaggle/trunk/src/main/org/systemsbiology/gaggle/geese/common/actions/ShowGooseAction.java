package org.systemsbiology.gaggle.geese.common.actions;

import java.awt.event.ActionEvent;
import java.rmi.RemoteException;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.geese.common.GaggleConnectionListener;


public class ShowGooseAction extends AbstractAction implements GaggleConnectionListener {
    private Boss boss;
    private String targetGoose;

    public ShowGooseAction() {
        super("S");
        this.putValue(Action.SHORT_DESCRIPTION, "Show selected goose.");
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if (boss != null)
                boss.show(targetGoose);
        }
        catch (RemoteException e1) {
            e1.printStackTrace();
        }
    }

    public void setConnected(boolean connected, Boss boss) {
        this.setEnabled(connected);
        this.boss = boss;
    }

    public void setTargetGoose(String targetGoose) {
        this.targetGoose = targetGoose;
    }
}
