package org.systemsbiology.gaggle.geese.common;

import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;


public class GaggleUtil {

    // copied this from MiscUtil 'cause MiscUtil requires the presence of jnlp.jar which
    // is not automatically the case for java apps started outside of JWS. The original
    // method remains in MiscUtil for now but should probably be removed.

    /**
     * Updates the UI to show the current list of geese. Removes the name of the
     * calling goose from the list, and sorts the remainder. Tries to preserve the
     * original selection in the list, if it still exists in the newly received
     * current goose list.
     * @param gooseChooser The UI element containing the list of geese
     * @param callingGoose The name of the calling goose
     * @param allGeese The list of all currently active geese
     */
    public static void updateGooseChooser(JComboBox gooseChooser, String callingGoose, String[] allGeese) {
        if (gooseChooser == null || allGeese == null) {
            return;
        }

        Object savedItem = gooseChooser.getSelectedItem();

        Arrays.sort(allGeese);
        DefaultComboBoxModel model = (DefaultComboBoxModel) gooseChooser.getModel ();
        model.removeAllElements ();
        model.addElement("Boss");

        for (String gooseName : allGeese) {
            if (!gooseName.equals(callingGoose)) {
                model.addElement(gooseName);
            }
        }

        // this will attempt to set selected item to what it was before.
        // if that item is no longer in the list, it will silently fail
        // leaving the selection unchanged. Passing null to setSelectedItem()
        // clears selections.
        if (savedItem != null)
            gooseChooser.setSelectedItem(savedItem);
    }

}
