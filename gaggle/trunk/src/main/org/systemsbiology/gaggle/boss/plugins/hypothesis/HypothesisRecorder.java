// HypothesisRecorder.java
//---------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.boss.plugins.hypothesis;
//---------------------------------------------------------------------------------------
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

import javax.jnlp.*;

import java.net.*;
import java.util.*;

import org.systemsbiology.gaggle.boss.*;
import org.systemsbiology.gaggle.boss.plugins.*;

//---------------------------------------------------------------------------------------
public class HypothesisRecorder extends GaggleBossPlugin {

  String name = "Hypothesis";
  private JTextArea textArea;
  private JButton saveHypothesisButton, clearTextButton;
  private GuiBoss gaggleBoss;
  private String species = "Halobacterium sp.";
  JTextField titleField;
  JTextField nameField;
  JTextField dateField;
  JTextField summaryField;
  JTextField ratingField;

//---------------------------------------------------------------------------------------
public HypothesisRecorder (GuiBoss boss)
{
  super ("Hypothesis");
  gaggleBoss = boss;
  createGui ();

} // ctor
//---------------------------------------------------------------------------------------
public String getName ()
{
  return name;
}
//---------------------------------------------------------------------------------------
protected void createGui ()
{
  setLayout (new BorderLayout ());
  JPanel mainPanel = new JPanel ();
  mainPanel.setLayout (new BorderLayout ());
  JPanel formPanel = new JPanel ();
   
  formPanel.setLayout (new BorderLayout ());
  JPanel titlesPanel = new JPanel ();
  titlesPanel.setLayout (new GridLayout (0,1));
  titlesPanel.add (new JLabel (" Title ", JLabel.RIGHT));
  titlesPanel.add (new JLabel (" Name ", JLabel.RIGHT));
  // titlesPanel.add (new JLabel (" Date ", JLabel.RIGHT));
  titlesPanel.add (new JLabel (" Summary ", JLabel.RIGHT));
  titlesPanel.add (new JLabel (" Rating ", JLabel.RIGHT));

  JPanel fieldsPanel = new JPanel ();
  fieldsPanel.setLayout (new GridLayout (0,1));

  titleField = new JTextField ();
  fieldsPanel.add (titleField);
  nameField = new JTextField ();
  fieldsPanel.add (nameField);
  String dateString = (new Date ()).toString ();
  dateField = new JTextField (dateString);
  dateField.setEditable (false);
  // fieldsPanel.add (dateField);
  summaryField = new JTextField ();
  fieldsPanel.add (summaryField);
  ratingField = new JTextField ();
  fieldsPanel.add (ratingField);

  formPanel.add (titlesPanel, BorderLayout.WEST);
  formPanel.add (fieldsPanel, BorderLayout.CENTER);
  formPanel.setBorder (createBorder ()); // ompoundBorder);

  mainPanel.add (formPanel, BorderLayout.NORTH);

  textArea = new JTextArea ();
  JScrollPane scrollPane = new JScrollPane (textArea);
  scrollPane.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
  scrollPane.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
  scrollPane.setBorder (createBorder ());

  mainPanel.add (scrollPane, BorderLayout.CENTER);

  clearTextButton = new JButton ("Clear");
  clearTextButton.addActionListener (new ActionListener () {
    public void actionPerformed (ActionEvent e) {
      textArea.setText (null);
      titleField.setText (null);
      nameField.setText (null);
      summaryField.setText (null);
      ratingField.setText (null);
      }});

  saveHypothesisButton = new JButton ("Save");
  saveHypothesisButton.addActionListener (new ActionListener () {
    public void actionPerformed (ActionEvent e) {saveHypothesis ();}});

  JPanel bottomButtonPanel = new JPanel ();
  bottomButtonPanel.add (clearTextButton);
  bottomButtonPanel.add (saveHypothesisButton);
  mainPanel.add (bottomButtonPanel, BorderLayout.SOUTH);

  add (mainPanel);

} // createGui
//-------------------------------------------------------------------------------
private Border createBorder ()
{
  Border raisedBevelBorder = BorderFactory.createRaisedBevelBorder ();
  Border loweredBevelBorder = BorderFactory.createLoweredBevelBorder ();
  Border compoundBorder = BorderFactory.createCompoundBorder (raisedBevelBorder, loweredBevelBorder);
  return compoundBorder;
}
//-------------------------------------------------------------------------------
public void select (String [] names)
{
  // System.out.println ("HypothesisRecorder.select: " + names.length);
  StringBuffer sb = new StringBuffer ();
  sb.append (textArea.getText());
  for (int i=0; i < names.length; i++) {
    if (sb.length () > 0) 
      sb.append (";");
    sb.append (names [i]);
    }

  textArea.setText (sb.toString ());  

} // select
//---------------------------------------------------------------------------------------
private void saveHypothesis ()
{
  String stringUrl = "http://db.systemsbiology.net:8080/halo/saveHypothesis.py";

  String titleText = titleField.getText().trim();
  if (titleText.length () == 0) {
    String msg = "The title field (at least) is required for hypothesis saving.";
    JOptionPane.showMessageDialog (gaggleBoss.getFrame (), msg);
    return;
    }

  String title = null;
  String name = null;
  String date = null;
  String summary = null;
  String rating = null;
  String freeText = null;

    try {
        title = URLEncoder.encode (titleField.getText(), "UTF-8");
        name = URLEncoder.encode (nameField.getText(), "UTF-8");
        date = URLEncoder.encode ((new Date ()).toString(), "UTF-8");
        summary = URLEncoder.encode (summaryField.getText(), "UTF-8");
        rating = URLEncoder.encode (ratingField.getText(), "UTF-8");
        freeText = URLEncoder.encode (textArea.getText(), "UTF-8");
    } catch (UnsupportedEncodingException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

  StringBuffer sb = new StringBuffer ();
  sb.append (stringUrl);
  sb.append ("?");

  sb.append ("title=");
  sb.append (title);
  sb.append ("&");

  sb.append ("name=");
  sb.append (name);
  sb.append ("&");

  sb.append ("date=");
  sb.append (date);
  sb.append ("&");

  sb.append ("summary=");
  sb.append (summary);
  sb.append ("&");

  sb.append ("rating=");
  sb.append (rating);
  sb.append ("&");

  sb.append ("body=");
  sb.append (freeText);


  String fullUrl = sb.toString ();
  try {
    URL url = new URL (fullUrl);
    String junk = getPage (url);
    //displayWebPage (url);
    }
  catch (Exception ex0) {
    ex0.printStackTrace ();
    }

}// saveHypothesis
//---------------------------------------------------------------------------------------
protected void displayWebPage (URL url)
{
  try {
    BasicService bs = (BasicService) ServiceManager.lookup ("javax.jnlp.BasicService");
    bs.showDocument (url);
    System.out.println ("BasicService url: " + url);
    }
  catch (Exception ex) {
    ex.printStackTrace ();
    }

} // displayWebPage
//-------------------------------------------------------------------------------------
private String getPage (String urlString) throws Exception
{
  return getPage (new URL (urlString));
}
//---------------------------------------------------------------------------
private String getPage (URL url) throws Exception
{
  int characterCount = 0;
  StringBuffer result = new StringBuffer ();

  HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection ();
  int responseCode = urlConnection.getResponseCode ();
  String contentType = urlConnection.getContentType ();

  int contentLength = urlConnection.getContentLength ();

  String contentEncoding = urlConnection.getContentEncoding ();

  if (responseCode != HttpURLConnection.HTTP_OK)
    throw new IOException ("\nHTTP response code: " + responseCode);

  BufferedReader theHTML = new BufferedReader 
                   (new InputStreamReader (urlConnection.getInputStream ()));
  String thisLine;
  while ((thisLine = theHTML.readLine ()) != null) {
    result.append (thisLine);
    result.append (" ");
    }

  return result.toString ();

} // getPage
//-------------------------------------------------------------------------
} // class HypothesisRecorder
