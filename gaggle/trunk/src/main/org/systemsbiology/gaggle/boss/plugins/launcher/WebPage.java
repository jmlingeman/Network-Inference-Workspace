// WebPage.java
//-------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.boss.plugins.launcher;
//-------------------------------------------------------------------------------------
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.regex.*;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.net.*;
import java.io.*;
import javax.jnlp.*;

import org.systemsbiology.gaggle.util.*;
//-------------------------------------------------------------------------------------
public class WebPage extends JPanel implements HyperlinkListener, java.io.Serializable {

  JPanel centerPanel;  // has a scrollable view of the html
  JTextField urlReadout;
  URL url;
  String briefWebSiteName;  // eg, KEGG, STRING, EcoCyc, ...
  String htmlText;

  String fullUrlString;
  String host;
  String protocol;
  String path;
  String file;
  String query;

//-------------------------------------------------------------------------------------
public WebPage (URL url)
{
  this.url = url;
  this.fullUrlString = url.toString ();
  this.htmlText = null;
  decomposeUrl ();
  initGui ();
  centerPanel.add (createScrolledWebPagePanel (getWebPageText (url)), BorderLayout.CENTER);
  setBorder (BorderFactory.createEmptyBorder (10, 10, 10, 10));
  
} // ctor (URL)
//-------------------------------------------------------------------------------------
public String getBriefTitle ()
{
  return briefWebSiteName;
}
//-------------------------------------------------------------------------------------
protected void decomposeUrl ()
{
  if (url == null) try {
    url = new URL (fullUrlString);
    }
  catch (Exception ex0) {
    ex0.printStackTrace ();
    return;
    }
   
  host = url.getHost ();
  protocol = url.getProtocol ();
  path = url.getPath ();
  file = url.getFile ();
  query = url.getQuery ();
  try {
    if (query != null && query.length () > 0)
      query = URLEncoder.encode (query, "UTF-8");
    }
  catch (Exception proceed) {}

} // decomposeUrl
//-------------------------------------------------------------------------------------
protected String getWebPageText (URL url) 
{
  try {
    TextHttpReader reader = new TextHttpReader (url.toString ());
    reader.read ();
    //return cleanUpHtml (reader.getText ());
    return reader.getText ();
    }
  catch (Exception ex0) {
    ex0.printStackTrace ();
    return ("error when reading from url '" + url.toString () + "'");

    }
 
} // getWebPageText
//-------------------------------------------------------------------------------------
protected void initGui ()
{
  setLayout (new BorderLayout ());
  JPanel topPanel = new JPanel ();
  topPanel.setLayout (new BorderLayout ());
  add (topPanel, BorderLayout.NORTH);
  
  urlReadout = new JTextField ();
  urlReadout.setEditable (true);
  topPanel.add (urlReadout, BorderLayout.CENTER);
  topPanel.setBorder (BorderFactory.createEmptyBorder (2, 2, 2, 5));

  JPanel spacer = new JPanel ();

  centerPanel = new JPanel ();
  centerPanel.setLayout (new BorderLayout ());
  add (centerPanel, BorderLayout.CENTER);
 
} // initGui
//-------------------------------------------------------------------------------------
protected JScrollPane createScrolledWebPagePanel (String webPageText)
{
  urlReadout.setText (fullUrlString);

  JTextPane htmlPanel = new JTextPane ();
  htmlPanel.setEditable (false);   
  htmlPanel.addHyperlinkListener (this);


  htmlPanel.setContentType ("text/html");
  htmlPanel.setText (webPageText);
  htmlPanel.setCaretPosition (0);
  return new JScrollPane (htmlPanel);

} // createScrolledWebPagePanel (String webPageText)
//-------------------------------------------------------------------------------------
public void hyperlinkUpdate (HyperlinkEvent evt) 
{
  if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
    try {
      System.out.println ("url: " + evt.getDescription ());
      MiscUtil.displayWebPage (new URL (evt.getDescription ()));
      }
    catch (Exception e) {
      e.printStackTrace ();
      } 
    } // if
}
//-------------------------------------------------------------------------------------
/**
 * try to remove all confusing elements (meta & javascript in particular) and then
 * absolutize all links
 */
protected String cleanUpHtml (String input)
{
  //System.out.println ("--------------- entering cleanUpHtml");
  //System.out.println (input);
  //System.out.println ("\n\n\n\n\n\n");

  String regex = "href=[^h]";
  Pattern p = Pattern.compile (regex, Pattern.DOTALL);
  Matcher matcher = p.matcher (input);
  String replacement = "href=" + protocol + "://" + host + "/";
  input = matcher.replaceAll (replacement);

  return input;
   
} // cleanUpHtml
//-------------------------------------------------------------------------------------
} // class WebPage
