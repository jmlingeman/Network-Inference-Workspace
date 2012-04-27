package org.systemsbiology.gaggle.experiment.readers;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/*
* Copyright (C) 2007 by Institute for Systems Biology,
* Seattle, Washington, USA.  All rights reserved.
*
* This source code is distributed under the GNU Lesser
* General Public License, the text of which is available at:
*   http://www.gnu.org/copyleft/lesser.html
*/
public class TextHttpDatabaseReader {
    String uri;

    public TextHttpDatabaseReader(String uri) {
        this.uri = uri;
        
    }

    static public String getPage (String urlString) throws Exception
    {
      String result;
      try {
        result = getPage (new URL (urlString));
        }
      catch (Exception e) {
        result = "";
        }

      return result;

    } // getPage


    // imagine a uri like this:
    // database://localhost:8080/GWAP/emiml?ids=1,27,300
    public String adjustUri(String uri) {
        return uri.replace("database://", "http://");
    }

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
        throw new IOException("\nHTTP response code: " + responseCode);

      BufferedReader theHTML = new BufferedReader
                       (new InputStreamReader(urlConnection.getInputStream ()));
      String thisLine;
      while ((thisLine = theHTML.readLine ()) != null) {
        result.append (thisLine);
        result.append ("\n");
        }

      return result.toString ();

    } // getPage


}
