// TextHttpIndirectFileReaderTest.java
//------------------------------------------------------------------------------
// $Revision: 732 $
// $Date: 2004/11/09 20:44:45 $
// $Author: pshannon $
//--------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.readers.unitTests;
//--------------------------------------------------------------------------------------
import junit.framework.*;
import java.io.*;
import java.util.*;
import org.systemsbiology.gaggle.experiment.readers.TextHttpIndirectFileReader;
//-----------------------------------------------------------------------------------------
public class TextHttpIndirectFileReaderTest extends TestCase {


//------------------------------------------------------------------------------
public TextHttpIndirectFileReaderTest (String name) 
{
  super (name);
}
//------------------------------------------------------------------------------
public void setUp () throws Exception
{
  }
//------------------------------------------------------------------------------
public void tearDown () throws Exception
{
}
//------------------------------------------------------------------------------
public void testGetXmlListing () throws Exception
{ 
  System.out.println ("testGetXmlListing");
  String uri = "httpIndirect://db.systemsbiology.net:8080/halo/DataFetcher.py";

   // user & password obtained from ./gaggle.props
  TextHttpIndirectFileReader reader = new TextHttpIndirectFileReader (uri);
  
  String adjustedUri = reader.getAdjustedUri ();
  String expected = "http://db.systemsbiology.net:8080/halo/DataFetcher.py?" +
                    "mode=dir&name=xml&user=tester0&pw=pw0";
  assertTrue (adjustedUri.equals (expected));

  int count = reader.read ();
  String text = reader.getText ();
  String [] experimentNames = text.split ("\n");
  assertTrue (experimentNames.length == 4);
  Arrays.sort (experimentNames);
  assertTrue (Arrays.equals (experimentNames, 
                             new String [] {"afsQ2.xml", "sample.xml", "test0.xml", "test1.xml"}));

} // testGetXmlListing
//-------------------------------------------------------------------------
public void testGetXmlListingForDifferentUsers () throws Exception
{ 
  System.out.println ("testGetXmlListingForDifferentUsers");
  String uri = "httpIndirect://db.systemsbiology.net:8080/halo/DataFetcher.py";

   // user & password same as from gaggle.props, but specified explicitly
  TextHttpIndirectFileReader reader = new TextHttpIndirectFileReader (uri, "tester0", "pw0");
  
  String adjustedUri = reader.getAdjustedUri ();
  String expected = "http://db.systemsbiology.net:8080/halo/DataFetcher.py?" +
                    "mode=dir&name=xml&user=tester0&pw=pw0";

  assertTrue (adjustedUri.equals (expected));

  int count = reader.read ();
  String text = reader.getText ();
  String [] lines = text.split ("\n");
  String [] experimentNames = text.split ("\n");
  assertTrue (experimentNames.length == 4);
  Arrays.sort (experimentNames);
  assertTrue (Arrays.equals (experimentNames, 
                             new String [] {"afsQ2.xml", "sample.xml", "test0.xml", "test1.xml"}));

    // now try with a different, explicit user and password
  reader = new TextHttpIndirectFileReader (uri, "tester1", "pw1");
  adjustedUri = reader.getAdjustedUri ();
  expected = "http://db.systemsbiology.net:8080/halo/DataFetcher.py?" +
             "mode=dir&name=xml&user=tester1&pw=pw1";

  assertTrue (adjustedUri.equals (expected));

  count = reader.read ();
  text = reader.getText ();
  lines = text.split ("\n");
  assertTrue (lines.length == 1);
  assertTrue (lines [0].equals ("test1.xml"));

    // now try with a bogus user and password

  reader = new TextHttpIndirectFileReader (uri, "bogus88", "pw1");
  adjustedUri = reader.getAdjustedUri ();
  expected = "http://db.systemsbiology.net:8080/halo/DataFetcher.py?" +
             "mode=dir&name=xml&user=bogus88&pw=pw1";

  assertTrue (adjustedUri.equals (expected));

  count = reader.read ();
  text = reader.getText ();
  lines = text.split ("\n");
  assertTrue (lines.length == 1);
  assertTrue (lines [0].trim().length() == 0);


} // testGetXmlListingForDifferentUsers
//-------------------------------------------------------------------------
public void testGetXmlFile () throws Exception
{ 
  System.out.println ("testGetXmlFile");

  String uri = "httpIndirect://db.systemsbiology.net:8080/halo/DataFetcher.py/test0.xml";

   // user & password obtained from ./gaggle.props
  TextHttpIndirectFileReader reader = new TextHttpIndirectFileReader (uri);

  
  String expected = "http://db.systemsbiology.net:8080/halo/DataFetcher.py?" +
                    "mode=getFile&name=test0.xml&user=tester0&pw=pw0";
  String adjustedUri = reader.getAdjustedUri ();

  assertTrue (adjustedUri.equals (expected));

  int count = reader.read ();
  String text = reader.getText ();
  String [] lines = text.split ("\n");
  assertTrue (lines.length == 39);

} // testGetXmlFile
//-------------------------------------------------------------------------
public void testFailedGetXmlFile () throws Exception
{ 
  System.out.println ("testFailedGetXmlFile");
  String uri = "httpIndirect://db.systemsbiology.net:8080/halo/DataFetcher.py/bogus99.xml";

   // user & password obtained from ./datamatrix.props
  TextHttpIndirectFileReader reader = new TextHttpIndirectFileReader (uri);

  int count = reader.read ();
  String text = reader.getText ();
  System.out.println (text);
  String [] lines = text.split ("\n");
  assertTrue (lines.length == 1);
  assertTrue (lines[0].trim().length() == 0);

} // testGetXmlFile
//-------------------------------------------------------------------------
public void testGetDataFile () throws Exception
{ 
  System.out.println ("testGetDataFile");
  String uri = "httpIndirect://db.systemsbiology.net:8080/halo/DataFetcher.py/test0.ratio";

   // user & password obtained from ./datamatrix.props
  TextHttpIndirectFileReader reader = new TextHttpIndirectFileReader (uri);

  String adjustedUri = reader.getAdjustedUri ();
  String expected = "http://db.systemsbiology.net:8080/halo/DataFetcher.py?" +
                    "mode=getFile&name=test0.ratio&user=tester0&pw=pw0";

  assertTrue (adjustedUri.equals (expected));

  int count = reader.read ();
  String text = reader.getText ();
  String [] lines = text.split ("\n");
  assertTrue (lines.length == 2401);

} // testGetXmlFile
//-------------------------------------------------------------------------
public static void main (String[] args) 
{
  junit.textui.TestRunner.run (new TestSuite (TextHttpIndirectFileReaderTest.class));
}
//------------------------------------------------------------------------------
} // TextHttpIndirectFileReaderTest


