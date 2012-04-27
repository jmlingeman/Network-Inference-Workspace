package org.systemsbiology.gaggle.geese.common.gui;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.geese.common.GaggleConnectionListener;


public class GooseChooserComboBox extends JComboBox implements GaggleConnectionListener {
    Boss boss;
    Goose goose;

    public GooseChooserComboBox(Goose goose) {
        this.setPrototypeDisplayValue("a very very long goose name");
        this.setToolTipText("Specify goose for broadcast");
        this.goose = goose;
    }

    public void setConnected(boolean connected, Boss boss) {
        this.boss = boss;
        updateGeese();
    }

    /**
     * @deprecated Goose.update() is called when the list of geese is updated
     * Use MiscUtil.updateGooseChooser() instead of this
     */
    public void updateGeese() {
        DefaultComboBoxModel model = (DefaultComboBoxModel) this.getModel ();
        model.removeAllElements ();
        if (boss == null) {
            model.addElement("Not Connected to Boss");
            return;
        }

        model.addElement ("Boss");

        String [] geese = null;
        try {
            geese = boss.getGooseNames ();
        }
        catch (Exception e) {
            model.removeAllElements();
            model.addElement("Couldn't connect to Boss");
            return;
        }

        ArrayList<String> tmp = new ArrayList<String>();
        tmp.addAll(Arrays.asList(geese));
        Collections.sort(tmp);

        for (String gooseName : tmp) {
            if (gooseName.equals(getGooseName()))
                continue;
            else
                model.addElement(gooseName);
        }
    }
    
    private String getGooseName() {
        if (goose == null) {
            return null;
        }
        else {
            try {
                return goose.getName();
            }
            catch (RemoteException e) {
                return null;
            }
        }
    }

    public String getTargetGoose() {
        return (String)getSelectedItem();
    }
}
