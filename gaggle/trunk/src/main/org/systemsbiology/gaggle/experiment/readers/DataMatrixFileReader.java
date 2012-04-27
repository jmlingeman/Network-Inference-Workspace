// DataMatrixFileReader.java
//-----------------------------------------------------------------------------------
// $Revision: 2360 $   
// $Date: 2005/04/09 23:16:23 $ 
// $Author: dtenenba $
//-----------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.readers;
//-----------------------------------------------------------------------------------
import java.io.*;
import java.util.*;
import org.systemsbiology.gaggle.util.*;

//-----------------------------------------------------------------------------------
public class DataMatrixFileReader extends DataMatrixReader {
  ArrayList matrices = new ArrayList ();
  boolean debug;
//-----------------------------------------------------------------------------------
public DataMatrixFileReader (String uri, boolean debug)
{
  super (uri);
  this.debug = debug;
}
//-----------------------------------------------------------------------------------
public DataMatrixFileReader (String uri)
{
  this (uri, false);

} // ctor
//-----------------------------------------------------------------------------------
public org.systemsbiology.gaggle.core.datatypes.DataMatrix get () throws Exception
{
  org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix = (org.systemsbiology.gaggle.core.datatypes.DataMatrix) matrices.get(0);
  matrix.sortByRowName();
  return matrix;
}
//-----------------------------------------------------------------------------------
public void read () throws Exception
{
  String rawText = null;

  boolean explicitProtocol = (uri.indexOf ("://") > 0);

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
    TextFileReader reader = new TextFileReader (path);
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

  else if (protocol.equals ("httpIndirect://")) {
    TextHttpIndirectFileReader reader = new TextHttpIndirectFileReader (uri, debug);
    reader.read ();
    rawText = reader.getText ();
    }

  else if (protocol.equals ("obj://")) {
    FileInputStream fstream = new FileInputStream (path);
    ObjectInputStream ostream = new ObjectInputStream (fstream);
    matrices.add ((org.systemsbiology.gaggle.core.datatypes.DataMatrix) ostream.readObject());
    }

  if (rawText != null)
    parseText (rawText);

} // read
//-----------------------------------------------------------------------------------
protected void parseText (String rawText) throws Exception
{
  int row =0;
  int column = 0;

  try {
    row = 0;
    String [] lines = rawText.split ("\n");
    String [] titles = lines [row].split ("\t");
    int dataRows = lines.length - 1;
    int dataColumns = titles.length - 1;

    if (dataRows < 1)
      throw new Exception ("fewer than two lines in matrix file<br>" + uri);

    if (dataColumns < 1) 
      throw new Exception ("there must be at least two (tab-delimited) titles in matrix file<br>" + uri);

    org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix = new org.systemsbiology.gaggle.core.datatypes.DataMatrix(protocol + path);
  
    matrix.setSize (dataRows, dataColumns);
    String rowTitlesTitle = titles [0];
    String [] columnTitles = new String [titles.length - 1];
    for (int i=1; i < titles.length; i++)
      columnTitles [i-1] = titles [i];
  
    matrix.setRowTitlesTitle (rowTitlesTitle);
    matrix.setColumnTitles (columnTitles);
  
    ArrayList rowTitleList = new ArrayList ();
    for (row = 1; row < lines.length; row++) {
      String rowString = lines [row];
      String [] tokens = rowString.split ("\t", -1);
      boolean trailingTab = false;
      if (!hasCorrectTokenCount (tokens, dataColumns)) {
        // System.out.println (" bad rowString\n" + rowString);
        StringBuffer msg = new StringBuffer ();
        msg.append ("incorrect number of columns in line ");
        msg.append (row);
        msg.append (" of<br>");
        msg.append (uri);
        msg.append ("  expected " + (dataColumns + 1) + "<br>");
        msg.append ("     found " + tokens.length);
        throw new Exception (msg.toString ());
        }

      String rowTitle = tokens [0];
      rowTitleList.add (rowTitle);
      //for (column=1; column < tokens.length; column++) {
      for (column=1; column < dataColumns+1; column++) {
        String tmp = tokens [column];
         try {
           double value;
           tmp = tmp.trim ();
           if (tmp.toLowerCase().equals ("na"))
             value = Double.NaN;
           else if (tmp.length () == 0)
             value = Double.NaN;
           else
             value = (new Double (tmp)).doubleValue ();
           matrix.set (row-1, column-1, value);
           }
         catch (NumberFormatException nfe) {
           String msg = "cannot convert '" + tmp + "' to double, at row " + (row-1) + 
                        " column " + (column-1) + " while reading file<br>" + uri;
           throw new IllegalArgumentException (msg);
           }
        } // for column
      } // for row
  
    matrix.setRowTitles ((String []) rowTitleList.toArray (new String [0]));
    matrices.add (matrix);
    } // try
  catch (Exception ex0) {
    StringBuffer sb = new StringBuffer ();
    sb.append ("error reading matrix file<br>" + uri);
    sb.append ("<br> at line: " + row + ", column: " + column);
    sb.append ("<br>");
    sb.append (ex0.getMessage ());
    throw new Exception (sb.toString ());
    }

} // read
//-----------------------------------------------------------------------------------
protected boolean hasCorrectTokenCount (String [] tokens, int dataColumnCount)
// the number of tokens should be <dataColumnCount> + 1 -- the extra one is for the
// row name at tokens [0].
// an additionally 'correct' state includes extra tokens on the end of the line -- typically just
// one extra -- which can be safely ignored, and left unparsed
// this method check for both 'correct' conditions
{
  int expectedTokenCount = dataColumnCount + 1;
  //System.out.println (" correct tokens? expected: " + expectedTokenCount + "  actual: " + 
  //                    dataColumnCount);
  if (tokens.length == expectedTokenCount)
    return true;

  if (tokens.length < expectedTokenCount)
    return false;

    // there are more tokens than expected.  are the extra one all empty strings? --
    // which result from trailing tabs (our standard delimiter) split using ('\t', -1)

  int extraTokenCount = tokens.length - expectedTokenCount;
  for (int i=expectedTokenCount; i < tokens.length; i++) {
    String token = tokens [i].trim ();
    if (token.length () > 0)
      return false;
    } // for i

  return true;

} // hasCorrectTokenCount
//-----------------------------------------------------------------------------------
} // class DataMatrixFileReader
