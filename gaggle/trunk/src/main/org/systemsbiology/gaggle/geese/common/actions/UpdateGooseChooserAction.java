package org.systemsbiology.gaggle.geese.common.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.geese.common.GaggleConnectionListener;
import org.systemsbiology.gaggle.geese.common.gui.GooseChooserComboBox;

/**
 * calls updateGeese() on the contained GooseChooser
 * @author cbare
 */
public class UpdateGooseChooserAction extends AbstractAction implements GaggleConnectionListener {
    GooseChooserComboBox gooseChooser;

    public UpdateGooseChooserAction() {
        super("Update");
        this.putValue(Action.SHORT_DESCRIPTION, "Update the list of connected Geese");
        this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                KeyEvent.VK_U, InputEvent.META_DOWN_MASK));
        this.putValue(AbstractAction.MNEMONIC_KEY, new Integer('U'));
    }

    public void setGooseChooser(GooseChooserComboBox gooseChooser) {
        this.gooseChooser = gooseChooser;
    }

    public void setConnected(boolean connected, Boss boss) {
        setEnabled(connected);
    }

    public void actionPerformed(ActionEvent event) {
        gooseChooser.updateGeese();
    }
}
