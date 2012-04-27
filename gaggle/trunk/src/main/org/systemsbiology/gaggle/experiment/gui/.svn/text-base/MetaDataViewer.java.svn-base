package org.systemsbiology.gaggle.experiment.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import org.systemsbiology.gaggle.experiment.metadata.MetaData;

/**
 * Display a dropdown list of experiments. When an experiment is chosen, display
 * details about that experiment.
 * @author cbare
 */
public class MetaDataViewer extends JDialog {
    
    private JComboBox experimentChooser;
    private Map<String, MetaData> experiments = new HashMap<String, MetaData>();
    private JTextArea textArea;

    public MetaDataViewer(JFrame owner) {
        super(owner, "Experiment Details", true);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        add(createGui());
        pack();
    }

    private JPanel createGui() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        
        JPanel controlPanel = new JPanel();

        controlPanel.add(new JLabel("Select Experiment:"));

        experimentChooser = new JComboBox();
        experimentChooser.addActionListener(new SelectionListener());
        experimentChooser.setPrototypeDisplayValue("a very extremely absurdly long condition name");
        controlPanel.add(experimentChooser);

        controlPanel.add(new JButton(new DismissAction()));
        
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        
        textArea = new JTextArea(20,40);
        textArea.setEditable(false);
        
        mainPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    public void setExperimentList(List<MetaData> metaDataList) {
        
        Collections.sort(metaDataList, new TitleComparator());
        
        DefaultComboBoxModel cbm = (DefaultComboBoxModel)experimentChooser.getModel();
        cbm.removeAllElements();
        experiments.clear();
        for (MetaData metaData : metaDataList) {
            experiments.put(metaData.getTitle(), metaData);
            cbm.addElement(metaData.getTitle());
        }
        
    }
    
    private String metaDataToString(MetaData metaData) {
        if (metaData == null)
            return "null";
        
        String ln = System.getProperty("line.separator");
        
        StringBuilder sb = new StringBuilder("Title: ");
        sb.append(metaData.getTitle()).append(ln);
        
        sb.append("Date: ").append(metaData.getDate()).append(ln + ln);
        
        String uri = metaData.getUri();
        sb.append("Filename: ").append(getFilename(uri)).append(ln);
        
        HashMap predicates = metaData.getPredicates();
        for (Object key : predicates.keySet()) {
            sb.append(key).append(": ").append(predicates.get(key)).append(ln);
        }
        
        sb.append(ln);
        
        sb.append("Conditions:").append(ln);
        for (String conditionAlias : metaData.getConditionAliases()) {
            sb.append("    ").append(conditionAlias).append(ln);
        }
        
        return sb.toString();
    }
    
    private String getFilename(String uri) {
        try {
            return uri.substring(uri.lastIndexOf("/")+1);
        }
        catch (Exception e) {
            return "???";
        }
    }

    private class SelectionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.out.println(e);
            String selectedExperimentName = String.valueOf(experimentChooser.getSelectedItem());
            MetaData metaData = experiments.get(selectedExperimentName);
            textArea.setText(metaDataToString(metaData));
        }
    }

    private class DismissAction extends AbstractAction {
        
        public DismissAction() {
            super("Dismiss");
            this.putValue(Action.SHORT_DESCRIPTION, "Close the Experiment Details window.");
            this.putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_D);
        }

        public void actionPerformed(ActionEvent e) {
            MetaDataViewer.this.setVisible(false);
            MetaDataViewer.this.dispose();            
        }
    }

    /**
     * sort MetaData objects based on their title
     */
    private class TitleComparator implements Comparator<MetaData> {

        public int compare(MetaData md1, MetaData md2) {
            if (md1 == null && md2 == null)
                return 0;
            if (md1 == null)
                return -1;
            if (md2 == null)
                return 1;
            
            String title1 = md1.getTitle();
            String title2 = md2.getTitle();
            
            if (title1 == null && title2 == null)
                return 0;
            if (title1 == null)
                return -1;
            if (title2 == null)
                return 1;

            return title1.compareTo(title2);
        }
        
    }
}
