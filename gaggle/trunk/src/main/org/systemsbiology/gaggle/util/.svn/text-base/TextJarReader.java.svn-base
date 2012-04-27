// TextJarReader.java
//---------------------------------------------------------------------------
//  $Revision$ 
//  $Date$
//  $Author$
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
//---------------------------------------------------------------------------
public class TextJarReader {
  String filename;
  InputStreamReader reader;
  StringBuffer sb;
//---------------------------------------------------------------------------
public TextJarReader (String URI) throws Exception
{
  sb = new StringBuffer ();
  filename = URI.substring (6);
  ClassLoader cl = this.getClass().getClassLoader();
  InputStream is = cl.getResourceAsStream(filename);
  reader = new InputStreamReader (is);

} // ctor
//-----------------------------------------------------------------------------------
public int read () throws IOException
{
  char [] cBuffer = new char [1024];
  int bytesRead;
  while ((bytesRead = reader.read (cBuffer, 0, 1024)) != -1)
    sb.append (new String (cBuffer, 0, bytesRead));

  return sb.length ();

} // read
//---------------------------------------------------------------------------
public String getText ()
{
  return sb.toString ();

} // read
//---------------------------------------------------------------------------
} // TextJarReader


