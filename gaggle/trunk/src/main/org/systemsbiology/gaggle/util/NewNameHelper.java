// NewNameHelper
//-----------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
//-----------------------------------------------------------------------------------------
// $Revision: 768 $
// $Date: 2006-04-25 09:58:55 -0700 (Tue, 25 Apr 2006) $
// $Author: pshannon $
//-----------------------------------------------------------------------------------------
package org.systemsbiology.gaggle.util;

import java.util.*;
import java.io.File;
import java.io.Serializable;

//-----------------------------------------------------------------------------------------

/**
 * Map names (on a one-to-one basis) across namespaces. Can map between "canonical" and
 * "common" names, or between "canonical" and other namespaces.
 */
public class NewNameHelper implements Serializable {
    
  protected String uri;
  protected String rawText;
  protected String [] titles = {};
  protected HashMap map = new HashMap ();
  protected HashMap canonicalToCommonMap = new HashMap ();
  protected HashMap commonToCanonicalMap = new HashMap ();
    
//-----------------------------------------------------------------------------------------
public NewNameHelper (String uri) throws Exception
{
  this.uri = uri;
  int positionOfProtocolChars = uri.indexOf ("://");
  if (positionOfProtocolChars < 0) {
    uri = "file://" + uri;
    positionOfProtocolChars = uri.indexOf ("://");
    }
  String protocol = uri.substring (0, positionOfProtocolChars + 3);

  if (protocol.equals ("file://")) {
    String filename;
    filename = uri.substring (7);
    File file = new File (filename);
    if (!file.exists ())
      throw new IllegalArgumentException ("cannot find file named '" + filename + "'");
    if (!file.canRead ())
      throw new IllegalArgumentException ("cannot read file named '" + filename + "'");
    TextFileReader reader = new TextFileReader (file.getPath ());
    reader.read ();
    rawText = reader.getText ();
    }

  else if (protocol.equals ("jar://")) {
    TextJarReader reader = new TextJarReader (uri);
    reader.read ();
    rawText = reader.getText ();
    }

  else if (protocol.equals ("http://")) {
    TextHttpReader reader = new TextHttpReader (uri);
    reader.read ();
    rawText = reader.getText ();
    }

  if (rawText != null)
    parseText (rawText);

} // ctor
//-----------------------------------------------------------------------------------------
public String getRawText ()
{
  return rawText;
}
//-----------------------------------------------------------------------------------------

/**
 * Expects first line to contain column headings. Reads at least two
 * tab-delimited columns. Creates hashmaps keyed by "canonical name"
 * assumed to be in the first column and "commonName" assumed to be in the
 * second column.
 */
protected void parseText (String rawText)
{
  String [] lines = rawText.split ("\n");
  titles = lines [0].split ("\t");
  for (int i=1; i < lines.length; i++) {
    String [] tokens = lines [i].split ("\t");
    String canonicalName = tokens [0];
    String commonName = tokens [1];
    HashMap info = new HashMap ();
    if (commonName != null && !"".equals(commonName))
      info.put ("commonName", commonName);
    for (int col=0; col < titles.length; col++)
      if (tokens[col] != null && !"".equals(tokens[col]))
        info.put (titles [col], tokens [col]);
    map.put (canonicalName, info);
    map.put (commonName, info);
    canonicalToCommonMap.put (canonicalName, commonName);
    commonToCanonicalMap.put (commonName, canonicalName);
    } // for i

} // parseText
//-----------------------------------------------------------------------------------------
public String [] getTitles ()
{
  return titles;
}
//-----------------------------------------------------------------------------------------
public String [] getCanonicalNames ()
{
  return (String []) map.keySet().toArray (new String [0]);
}
//-----------------------------------------------------------------------------------------
/**
 * @return a Map from the target namespace (string) to the name (another string)
 * corresponding to the given canonical name in the target namespace.
 */
public HashMap getInfo (String canonicalName)
{
  //System.out.println ("NewNameHelper.getInfo (" + canonicalName + ")");
  return (HashMap) map.get (canonicalName);
}
//-----------------------------------------------------------------------------------------
public String getCommonName (String canonicalName)
{
  return (String) canonicalToCommonMap.get (canonicalName);
}
//-----------------------------------------------------------------------------------------
public String getCanonicalName (String commonName)
{
  return (String) commonToCanonicalMap.get (commonName);
}
//-----------------------------------------------------------------------------------------
} // class NewNameHelper
