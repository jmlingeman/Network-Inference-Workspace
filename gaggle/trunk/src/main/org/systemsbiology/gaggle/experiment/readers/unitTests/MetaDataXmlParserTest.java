// MetaDataXmlParserTest.java
//--------------------------------------------------------------------------------------
// $Revision: 732 $
// $Date: 2004/11/08 23:04:34 $
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
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.io.File;
import java.util.*;
import org.systemsbiology.gaggle.experiment.readers.*;
import org.systemsbiology.gaggle.experiment.metadata.*;
//--------------------------------------------------------------------------------------
public class MetaDataXmlParserTest extends TestCase {

//--------------------------------------------------------------------------------------
public void testCtor () throws Exception
{
  System.out.println ("testCtor");
  String filename = "file://../../sampleData/zinc.xml";
  MetaDataXmlParser parser = new MetaDataXmlParser (filename);

}
//--------------------------------------------------------------------------------------
public void testLocalXmlFile () throws Exception
{
  System.out.println ("testLocalXmlFile");
  String uri = "file://../../sampleData/sample.xml";
  MetaDataXmlParser parser = new MetaDataXmlParser (uri);
  MetaData e = parser.getMetaData ();
  assertTrue (MetaDataXmlParserTest.sampleMetaDataHasExpectedContents (e));
    
} // testLocalXmlFile
//--------------------------------------------------------------------------------------
/**
 * the web server at 8060 is not reliably up
 */
public void notestHttpDirectXmlFile () throws Exception
{
  System.out.println ("testHttpDirectXmlFile");
  String uri = "http://db.systemsbiology.net:8060/halo/data/unitTests/sample.xml";
  MetaDataXmlParser parser = new MetaDataXmlParser (uri);
  MetaData e = parser.getMetaData ();
  assertTrue (MetaDataXmlParserTest.sampleMetaDataHasExpectedContents (e));
    
} // testHttpDirectXmlFile
//--------------------------------------------------------------------------------------
public void testHttpIndirectXmlFile () throws Exception 

{
  System.out.println ("testHttpIndirectXmlFile");

   // the MetaDataXmlParser constructs a TextHttpIndirectFileReader object
   // this has two ctors:  (filename) and (filename, user, password)
   // for the first, user and password will try to be read in a file
   // 'gaggle.props' in the current or home directory
   // 
   // the reader ultimately makes call like these:
   // wget "http://db.systemsbiology.net:8080/halo/DataFetcher.py?\
   //       mode=dir&name=xml&debug&user=tester0&pw=pw0" -O - -q

  String baseUrl = "httpIndirect://db.systemsbiology.net:8080/halo/DataFetcher.py";
  String filename = "sample.xml";
  String uri = baseUrl + "/" + filename;
  MetaDataXmlParser parser = new MetaDataXmlParser (uri);
  MetaData e = parser.getMetaData ();
  assertTrue (MetaDataXmlParserTest.sampleMetaDataHasExpectedContents (e));
    
} // testHttpDirectXmlFile
//--------------------------------------------------------------------------------------
static public boolean sampleMetaDataHasExpectedContents (MetaData e)
{
  assertTrue (e.getTitle ().equals ("sample data"));
  assertTrue (e.getDate ().equals ("2004-02-01"));
  HashMap preds = e.getPredicates ();
  assertTrue (preds.get ("species").equals ("Halobacterium NRC-1"));
  assertTrue (preds.get ("strain").equals ("wild type"));
  assertTrue (preds.get ("perturbation").equals ("environmental:irradiation:gamma"));

  DataSetDescription [] dsds = e.getDataSetDescriptions ();
  assertTrue (dsds.length == 2);

  String uri = dsds [0].getUri ();
  assertTrue (uri.indexOf ("sample.ratio") > 0);

  assertTrue (dsds [0].getStatus ().equals ("primary"));
  assertTrue (dsds [0].getType ().equals ("log10 ratios"));
 
  String expected = "file://../sampleData/sample.lambda";
  uri = dsds [1].getUri ();
  assertTrue (uri.indexOf ("sample.lambda") > 0);

  assertTrue (dsds [1].getStatus ().equals ("derived"));
  assertTrue (dsds [1].getType ().equals ("lambdas"));
  
  HashMap varDefs = e.getVariableSummary ();
  String [] keys = (String []) varDefs.keySet().toArray (new String [0]);
  Arrays.sort (keys);
  assertTrue (Arrays.equals (keys, new String [] {"gamma", "time"}));

  HashMap gammaVars = (HashMap) varDefs.get ("gamma");
  HashMap timeVars = (HashMap) varDefs.get ("time");
 
  keys = (String []) gammaVars.keySet().toArray (new String [0]);
  Arrays.sort (keys);
  assertTrue (Arrays.equals (keys, new String [] {"0000", "2500"}));
  assertTrue (gammaVars.get ("0000").equals ("Gy"));

  keys = (String []) timeVars.keySet().toArray (new String [0]);
  Arrays.sort (keys);
  //for (int i=0; i < keys.length; i++)
  //  System.out.println (keys [i]);

  assertTrue (Arrays.equals (keys, new String [] {"000", "010", "020", "030", "040",
                                                  "060", "120", "240", "480", "960"}));

  assertTrue (((String)timeVars.get ("000")).equals ("minutes"));

  Condition [] conditions = e.getConditions ();
  //assertTrue (conditions.length == 10);
      
  ArrayList list = new ArrayList ();
  for (int c=0; c < conditions.length; c++) {
    //System.out.println (conditions [c].getAlias ());
    list.add (conditions [c].getAlias ());
    }
  String [] aliases = (String []) list.toArray (new String [0]);
  Arrays.sort (aliases);
  String [] aliasesExpected = new String [] {"gamma__0000gy-0000m",
                                             "gamma__0000gy-0010m",
                                             "gamma__0000gy-0030m",
                                             "gamma__0000gy-0060m",
                                             "gamma__0000gy-0240m",
                                             "gamma__0000gy-0960m",
                                             "gamma__2500gy-0000m",
                                             "gamma__2500gy-0010m",
                                             "gamma__2500gy-0020m",
                                             "gamma__2500gy-0030m",
                                             "gamma__2500gy-0040m",
                                             "gamma__2500gy-0060m",
                                             "gamma__2500gy-0120m",
                                             "gamma__2500gy-0240m",
                                             "gamma__2500gy-0480m",
                                             "gamma__2500gy-0960m"};

  Arrays.sort (aliasesExpected);
  assertTrue (Arrays.equals (aliases, aliasesExpected));
  return true;

} // hasExpectedContents
//--------------------------------------------------------------------------------------
public static void main (String [] args) 
{
  junit.textui.TestRunner.run (new TestSuite (MetaDataXmlParserTest.class));
}
//--------------------------------------------------------------------------------------
} // MetaDataXmlParserTest
