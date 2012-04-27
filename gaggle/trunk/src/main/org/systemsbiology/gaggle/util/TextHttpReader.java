// TextHttpReader.java
//---------------------------------------------------------------------------
//  $Revision: 858 $ 
//  $Date: 2006-07-18 18:17:42 -0400 (Tue, 18 Jul 2006) $
//  $Author: cbare $
//---------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.util;
//------------------------------------------------------------------------------
import java.io.*;
import java.util.*;
import java.net.*;
//---------------------------------------------------------------------------
public class TextHttpReader {
  InputStreamReader reader;
  StringBuffer sb;
  String uri;
//---------------------------------------------------------------------------
public TextHttpReader (String URI) throws Exception
{
  // TODO change to url encode
  uri = URI.replaceAll (" ", "%20");
  sb = new StringBuffer ();

} // ctor
//-----------------------------------------------------------------------------------
public int read () throws Exception
{
  sb.append (getPage (uri));
  return sb.length ();

} // read
//---------------------------------------------------------------------------
public String getText ()
{
  return sb.toString ();

} // read
//---------------------------------------------------------------------------
static public String getPage (String urlString) throws Exception
{
  return getPage (new URL (urlString));
}
//-----------------------------------------------------------------------------------------------
static public String getPage (URL url) throws Exception
{
  int characterCount = 0;
  StringBuffer result = new StringBuffer ();

  HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection ();
  int responseCode = urlConnection.getResponseCode ();
  String contentType = urlConnection.getContentType ();

  int contentLength = urlConnection.getContentLength ();

  String contentEncoding = urlConnection.getContentEncoding ();

  if (responseCode != HttpURLConnection.HTTP_OK)
    throw new IOException ("\nHTTP response code: " + responseCode + " url: " + url.toString () + "\n");

  BufferedReader theHTML = new BufferedReader 
                   (new InputStreamReader (urlConnection.getInputStream ()));
  String thisLine;
  while ((thisLine = theHTML.readLine ()) != null) {
    result.append (thisLine);
    result.append ("\n");
    }

  return result.toString ();

} // getPage
//-----------------------------------------------------------------------------------------------
} // TextReader


