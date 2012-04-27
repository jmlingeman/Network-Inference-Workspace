/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.
 */
package org.systemsbiology.gaggle.geese.annotation;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.*;


/**
 * Allow the user to enter a species and select an annotation file.
 * 
 * The dialog can be preloaded with a set of known species with or without
 * preloaded annotation files. The dialog's exit status can be queried by
 * the caller to determine exit status (OK or Cancel), selected species, 
 * and a uri for an annotation file. It is the client's responsibility to
 * load the annotation file.
 * 
 * @author cbare
 */
public class LoadAnnotationDialog extends JDialog {
  private static String TITLE = "Set Species and Load Annotations";

  private boolean ok = false;
  private JComboBox speciesComboBox;
  private JTextField uriTextField;
  private Action okAction;
  private JCheckBox appendCheckBox;
  
  // keep a list of known species to allow picking from a list
  private TreeSet<String> knownSpeciesSet = new TreeSet<String>(
      Arrays.asList(new String[] {
      "Halobacterium",
      "Helicobacter pylori",
      "Pyrococcus",
      "Sulfolobus",
      "Saccharomyces cerevisiae",
      "Bacillus subtilis",
      "Bacillus anthracis",
      "Escherichia coli",
      "Desulfovibrio",
      "Salmonella",
      "Mouse",
      "Drosophila melanogaster",
      "Caenorhabditis elegans",
      "Schizosaccharomyces pombe",
      "Arabidopsis thaliana",
      "Geobacter",
      "Caulobacter"
  }));
  
  /** map from species name to the uri of an annotation file, if any preloaded
   * annotation files are present.
   */
  public HashMap<String,String> knownAnnotationFiles = new HashMap<String,String>();


  /**
   * create a dialog with the default list of species
   * @param owner
   * @throws HeadlessException
   */
  public LoadAnnotationDialog(Frame owner) throws HeadlessException {
    super(owner, TITLE, true);
    initGui();
  }

  /**
   * create a dialog with the given list of species
   * @param owner
   * @param species
   * @throws HeadlessException
   */
  public LoadAnnotationDialog(Frame owner, String[] species) throws HeadlessException {
    super(owner, TITLE, true);
    knownSpeciesSet.clear();
    knownSpeciesSet.addAll(Arrays.asList(species));
    initGui();
  }

  private void initGui() {
    this.setLayout(new BorderLayout());

    // create set species panel
    JPanel loadFilePanel = new JPanel();
    loadFilePanel.setLayout(new GridBagLayout());
    loadFilePanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    uriTextField = new JTextField("");
    speciesComboBox = new JComboBox(knownSpeciesSet.toArray());
    speciesComboBox.setEditable(true);
    speciesComboBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String species = (String)speciesComboBox.getSelectedItem();
            String uri = knownAnnotationFiles.get(species);
            if (uri != null) {
              uriTextField.setText(uri);
            }
          }
        });
    JButton loadAnnotationButton = new JButton(new BrowseAction());
    
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.anchor = GridBagConstraints.EAST;
    c.insets = new Insets(8,8,8,16);
    loadFilePanel.add(new JLabel("Species:"), c);
    c.gridx++;
    c.gridwidth=2;
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(8,8,8,8);
    loadFilePanel.add(speciesComboBox, c);
    c.gridx = 0;
    c.gridy++;
    c.gridwidth=1;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 0;
    c.insets = new Insets(8,8,8,16);
    loadFilePanel.add(new JLabel("Annotation File URI:"), c);
    c.gridx++;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 1;
    c.insets = new Insets(8,8,8,8);
    loadFilePanel.add(uriTextField, c);
    c.gridx++;
    c.gridwidth = 1;
    c.weightx = 0;
    c.anchor = GridBagConstraints.WEST;
    loadFilePanel.add(loadAnnotationButton, c);
    c.gridx = 0;
    c.gridy++;
    c.gridwidth=3;
    c.anchor = GridBagConstraints.CENTER;
    c.insets = new Insets(8,8,8,8);
    appendCheckBox = new JCheckBox("Append to previously loaded annotations", true);
    appendCheckBox.setToolTipText("Append to previously loaded annotations (or overwrite if unchecked)");
    loadFilePanel.add(appendCheckBox, c);
    c.gridx = 0;
    c.gridy++;
    c.gridwidth=3;
    c.weightx = 0;
    c.anchor = GridBagConstraints.CENTER;
    c.insets = new Insets(8,8,8,8);
    JLabel warning = new JLabel();
    warning.setText(
        "<html>The Annotation goose uses the first column as<br>" +
        "an <b>identifier</b>. There can be only one row for<br>" + 
        "a given identifier, so attempting to merge files<br>" +
        "with the same identifiers will result in earlier data<br>" +
        "being overwritten.</html>");
    loadFilePanel.add(warning, c);

    this.add(loadFilePanel, BorderLayout.CENTER);

    okAction = new OkAction();
    JButton okButton = new JButton(okAction);
    JButton cancelButton = new JButton(new CancelAction());
    this.getRootPane().setDefaultButton(okButton);
    JPanel buttonPanel = new JPanel();
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
    buttonPanel.add(cancelButton);
    buttonPanel.add(okButton);
    this.add(buttonPanel, BorderLayout.SOUTH);

    this.pack();
  }

  /**
   * perform minimal validation, set exit status, make dialog invisible.
   */
  public void exit(boolean ok) {
    if (ok) {
      if (!inputIsValid()) {
        JOptionPane.showMessageDialog(this,
            "Can't continue: You must select a species and an annotation file or press cancel.",
            "Warning", JOptionPane.WARNING_MESSAGE);
        return;
      }
      if (!knownAnnotationFiles.containsKey(getSpecies())) {
        knownAnnotationFiles.put(getSpecies(), getUri());
      }
    }
    this.ok = ok;
    LoadAnnotationDialog.this.setVisible(false);
    // TODO call dispose here?
  }


  /**
   * @return species or null if none is selected
   */
  public String getSpecies() {
    String species = (String)speciesComboBox.getSelectedItem();
    if ("".equals(species))
      return null;
    else
      return species;
  }

  /**
   * select the given species
   */
  public void setSpecies(String species) {
    speciesComboBox.setSelectedItem(species);
  }

  /**
   * Append or overwrite existing annotations
   * @return true for append
   */
  public boolean getAppend() {
    return appendCheckBox.isSelected();
  }

  /**
   * add to the set of known annotation files
   * @param uris a map from species to uri of annotation file
   */
  public void addAnnotationFiles(Map<String,String> uris) {
    knownAnnotationFiles.putAll(uris);
  }

  /**
   * clear the set of known annotation files
   */
  public void clearAnnotationFiles() {
    knownAnnotationFiles.clear();
  }

  /**
   * Add a mapping to the set of known annotation files
   * @param species
   * @param uri
   */
  public void addAnnotationFile(String species, String uri) {
    knownAnnotationFiles.put(species, uri);
  }

  /**
   * @return uri or null if not specified
   */
  public String getUri() {
    String uri = uriTextField.getText();
    if ("".equals(uri))
      return null;
    else
      return uri;
  }

  /**
   * determine the exit status of the dialog
   * @return true if user pressed OK, false otherwise
   */
  public boolean exitedWithOk() {
    return ok;
  }

  /**
   * @return true if something is filled in for both species and uri
   */
  public boolean inputIsValid() {
    return !(getSpecies()==null || getUri()==null);
  }

  /**
   * Action to allow user to choose a local annotation file
   */
  class BrowseAction extends AbstractAction {
    public BrowseAction() {
      super("Choose File");
    }

    public void actionPerformed(ActionEvent e) {
      JFileChooser chooser = new JFileChooser();
      int returnVal = chooser.showOpenDialog(LoadAnnotationDialog.this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        uriTextField.setText(chooser.getSelectedFile().getAbsolutePath());
      }
    }
  }

  
  /**
   * Action for OK button
   */
  class OkAction extends AbstractAction {
    public OkAction() {
      super("OK");
      this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
    }

    public void actionPerformed(ActionEvent e) {
      exit(true);
    }
  }

  
  /**
   * Action for Cancel button
   */
  class CancelAction extends AbstractAction {
    public CancelAction() {
      super("Cancel");
    }

    public void actionPerformed(ActionEvent e) {
      exit(false);
    }
  }

}
