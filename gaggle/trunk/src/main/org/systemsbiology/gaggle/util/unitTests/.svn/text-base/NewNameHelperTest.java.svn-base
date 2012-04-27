// NewNameHelperTest.java
//------------------------------------------------------------------------------
// $Revision: 1.1 $
// $Date: 2005/02/02 01:00:25 $
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
package org.systemsbiology.gaggle.util.unitTests;
//--------------------------------------------------------------------------------------
import junit.framework.*;
import java.io.*;
import java.util.*;

import org.systemsbiology.gaggle.util.NewNameHelper;
import org.systemsbiology.gaggle.util.NameHelperFactory;
//------------------------------------------------------------------------------
public class NewNameHelperTest extends TestCase {

//------------------------------------------------------------------------------
public NewNameHelperTest (String name) 
{
  super (name);
}
//------------------------------------------------------------------------------
public void testCtor () throws Exception
{
  System.out.println ("testCtor");
  NewNameHelper nnh = new NewNameHelper ("macrophage-names-35.txt");
  String rawText = nnh.getRawText ();
  assertTrue (rawText.length () == 2982);
 
}  // testCtor
//-------------------------------------------------------------------------
public void testParsingAndRetrievalFromFile () throws Exception
{
  System.out.println ("testParsingAndRetrievalFromFile");
  NewNameHelper nnh = new NewNameHelper ("macrophage-names-35.txt");
  String rawText = nnh.getRawText ();
  String [] titles = nnh.getTitles ();
  assertTrue (titles.length == 5);
  assertTrue (Arrays.equals (titles, 
              new String [] {"NP", "GeneSymbol", "GeneID", "description", "Organism"}));
  String [] canonicalNames = nnh.getCanonicalNames ();
  assertTrue (canonicalNames.length == 35);
  Arrays.sort (canonicalNames);
  assertTrue (Arrays.binarySearch (canonicalNames, "NP_032715") >= 0);
  HashMap info = nnh.getInfo ("NP_032715");
  assertTrue (info.get ("NP").equals ("NP_032715"));
  assertTrue (info.get ("description").equals (
                  "nuclear factor of kappa light chain gene enhancer in B-cells 1, p105"));
  assertTrue (info.get ("GeneSymbol").equals ("Nfkb1"));
  assertTrue (info.get ("commonName").equals ("Nfkb1"));
  assertTrue (info.get ("GeneID").equals ("18033"));
  assertTrue (info.get ("Organism").equals ("Mus musculus"));
  assertTrue (nnh.getCommonName ("NP_032715").equals ("Nfkb1"));
  assertTrue (nnh.getCanonicalName ("Nfkb1").equals ("NP_032715"));
 
}  // testParsingAndRetrievalFromFile
//-------------------------------------------------------------------------
public void testParsingAndRetrievalFromJar () throws Exception
{
  System.out.println ("testParsingAndRetrievalFromJar");
  NewNameHelper nnh = new NewNameHelper ("jar://macrophage-names-35.txt");
  String rawText = nnh.getRawText ();
  String [] titles = nnh.getTitles ();
  assertTrue (titles.length == 5);

} // testParsingAndRetrievalFromJar
//-------------------------------------------------------------------------
public void testParsingAndRetrievalViaHttp () throws Exception
{
  System.out.println ("testParsingAndRetrievalViaHttp");
  String uri = "http://gaggle.systemsbiology.net/tests/macrophage-names-35.txt";
  NewNameHelper nnh = new NewNameHelper (uri);
  String rawText = nnh.getRawText ();
  String [] titles = nnh.getTitles ();
  assertTrue (titles.length == 5);

} // testParsingAndRetrievalViaHttp
//-------------------------------------------------------------------------
public static void main (String [] args) 
{
  junit.textui.TestRunner.run (new TestSuite (NewNameHelperTest.class));
  System.exit (0);

}// main
//------------------------------------------------------------------------------
} // NewNameHelperTest
