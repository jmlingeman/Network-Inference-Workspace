// FileBasedSequenceFetcher.java
//-----------------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.sequence;
//-----------------------------------------------------------------------------------------------
import org.systemsbiology.gaggle.util.*;
import java.util.HashMap;
import java.io.*;
//-----------------------------------------------------------------------------------------------
public class FileBasedSequenceFetcher implements SequenceFetcher {

  HashMap sequenceMap;

//-----------------------------------------------------------------------------------------------
public FileBasedSequenceFetcher (String uri) throws Exception
{
  sequenceMap = getSequences (uri);
}
//------------------------------------------------------------------------------------------
protected String readData (String uri) throws Exception
{
  boolean explicitProtocol = (uri.indexOf ("://") > 0);
  String protocol = "file://";
  if (explicitProtocol) 
    protocol = uri.substring (0, uri.indexOf ("://") + 3);
  String result = "";

  if (!explicitProtocol || protocol.equals ("file://")) {
    String filename;
    if (explicitProtocol)
      filename = uri.substring (7);
    else
      filename = uri;
    File file = new File (filename);
    if (!file.exists ())
      throw new IllegalArgumentException ("cannot find file named '" + filename + "'");
    if (!file.canRead ())
      throw new IllegalArgumentException ("cannot read file named '" + filename + "'");
    TextFileReader reader = new TextFileReader (file.getPath ());
    reader.read ();
    result = reader.getText ();
    }

  else if (protocol.equals ("jar://")) {
    TextJarReader reader = new TextJarReader (uri);
    reader.read ();
    result = reader.getText ();
    }

  else if (protocol.equals ("http://")) {
    TextHttpReader reader = new TextHttpReader (uri);
    reader.read ();
    result = reader.getText ();
    }

  else {
    String msg = "unrecognized protocol for SequenceFetcher: '" + protocol + "'";
    throw new IllegalArgumentException (msg);
    }

  return result;

}  // readData
//-----------------------------------------------------------------------------------------------
public HashMap getSequences (String uri) throws Exception
{
  HashMap result = new HashMap ();

  if (uri != null && uri.length () > 0) {
    String rawText = readData (uri);
    result = parseText (rawText);
    }

  return result;

} // getSequences
//------------------------------------------------------------------------------------------
protected HashMap parseText (String rawText)
{
  StringBuffer sb = new StringBuffer ();
  String id = null;

  String [] lines = rawText.split ("\n");

  HashMap result = new HashMap ();

  for (int i=0; i < lines.length; i++) {
    String line = lines [i].trim ();
    if (line.length () == 0)
      continue;
    if (line.indexOf (">") == 0) {
      if (sb.length () > 0 && id != null) {  // put the preceeding sequence in the hash
        //System.out.println("id = " + id);
        result.put (id, sb.toString ());
        }
      String [] tokens = line.split ("\\W+");
      id = tokens [1];
      sb = new StringBuffer ();
      } // if initial line '> gene name'
    else {
      sb.append (line);
      }
    } // for i

     // save the last sequence

  //System.out.println("id = " + id);
  result.put (id, sb.toString ());
  return result;

} // parseText
//------------------------------------------------------------------------------------------
public String getDnaSequence (String organism, String identifier)
{
  if (sequenceMap.containsKey (identifier))
    return (String) sequenceMap.get (identifier);
  else
    return null;

}
//-----------------------------------------------------------------------------------------------
public String getAminoAcidSequence (String organism, String identifier)
{
  return getDnaSequence (organism, identifier);
}
//-----------------------------------------------------------------------------------------------
} // class FileBasedSequenceFetcher

