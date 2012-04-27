// HttpDirectRepository.java
//----------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.repository;
//----------------------------------------------------------------------------------
import java.util.*;
import java.io.*;
import java.net.*;
//----------------------------------------------------------------------------------
import org.systemsbiology.gaggle.experiment.metadata.*;
import org.systemsbiology.gaggle.experiment.readers.*;

//----------------------------------------------------------------------------------
public class HttpDirectRepository extends ExperimentRepository { 

  File directory;
  File singleXmlFile;
  boolean repositoryIsDirectory;

//----------------------------------------------------------------------------------
public HttpDirectRepository (String uri)
{
  this (uri, false);

} // ctor
//----------------------------------------------------------------------------------
public HttpDirectRepository (String uri, boolean debug)
{
  super (uri, debug);
  if(uri.trim().contains(".xml"))
    repositoryIsDirectory = false;
  else // the usual, expected case
    repositoryIsDirectory = true;

} // ctor
//----------------------------------------------------------------------------------
public String [] getExperimentNames () throws Exception
{
  if (!repositoryIsDirectory) {
    URL url = new URL (uri);
    String urlPath = url.getPath ();
    String filename = urlPath;
    if (filename.indexOf ("/") >= 0) {
      String [] tokens = filename.split ("/");
      filename = tokens [tokens.length - 1];
      } // if has slash
    return new String [] {filename};
    }
  else {
    return getHttpDirectoryExperimentNames ();
    }

} // getExperimentNames
//----------------------------------------------------------------------------------
public String [] getHttpDirectoryExperimentNames () throws Exception
{

 /****
     tomcat returns a directory listing with repeated entries like this 
     for each file in that directory:
      <a href="/halo/data/zinc.lambda.xml"><tt>zinc.lambda.xml</tt></a></td>
     this method traverses the page at <self.uri>, gathering entries of
     the above form, and separating them out into key/value pairs like

       copper.xml -> http://db.systemsbiology.net:8060/halo/data/test/copper.xml
       boa4.xml -> http://db.systemsbiology.net:8060/halo/data/test/boa4.xml

  ****/
  ArrayList list = new ArrayList ();
  String rawText = getWebPage (uri);
  //System.out.println ("HttpDirectRepository.getExperimentNames, rawText:\n" + rawText);
  int base = 0;
  int start, end;
  boolean done = false;
  while (!done) {
    String signature = "<a href=\"";
    start = rawText.indexOf (signature, base);
    if (start < 0)
      done = true;
    else {
      start += 9;
      end = rawText.indexOf ("\">", start);
      if (end < 0) 
          done = true;
      else {
        String filename = rawText.substring (start, end);
        if (filename.endsWith (".xml")) {
          int lastSlashPosition = filename.lastIndexOf ('/');
          if (lastSlashPosition > 0)
            filename = filename.substring (lastSlashPosition + 1);
          list.add (filename);
          }
        base = end;
        }
      } // else: '<a href' was found
    } // while ! done

  return (String []) list.toArray (new String [0]);

} // getHttpDirectoryExperimentNames
//----------------------------------------------------------------------------------
public MetaData getMetaData (String experimentName) throws Exception
// this class was constructed with a directory uri (eg, 'http://host/directory') 
// or with a file uri (eg, 'http://host/directory/someFile.xml')
// in either case, the full path of the supplied uri is used, below, to create
// an argument for the MetaDataXmlParser:  add the experiment name if the original
// uri was a directory; otherwise, just return the original uri
{
  String fullUri = protocol + "://" + path + "/" + experimentName;
  if (!repositoryIsDirectory)
    fullUri =  protocol + "://" + path;

  MetaDataXmlParser parser = new MetaDataXmlParser (fullUri);
  return parser.getMetaData ();

} // getMetaData
//----------------------------------------------------------------------------------
public org.systemsbiology.gaggle.core.datatypes.DataMatrix getDataSet (DataSetDescription dsd) throws Exception
{
  String uri = dsd.getUri ();
  DataMatrixFileReader reader = new DataMatrixFileReader (uri);
  reader.read ();
  return reader.get ();
  
} // getDataSet
//---------------------------------------------------------------------------
private String getWebPage (String uri) throws Exception
{
  URL url = new URL (uri);
  int characterCount = 0;
  StringBuffer result = new StringBuffer ();
  HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection ();
  int responseCode = urlConnection.getResponseCode ();
  if (responseCode != HttpURLConnection.HTTP_OK)
    throw new IOException ("\nHTTP response code: " + responseCode);

  BufferedReader theHTML = new BufferedReader 
                   (new InputStreamReader (urlConnection.getInputStream ()));
  String thisLine;
  while ((thisLine = theHTML.readLine ()) != null) {
    result.append (thisLine);
    result.append ("\n");
    }

  return result.toString ();

} // getPage
//-------------------------------------------------------------------------
} // HttpDirectRepository
