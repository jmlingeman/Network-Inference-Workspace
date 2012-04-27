// TranslatorTest.java
//------------------------------------------------------------------------------
// $Revision: 18 $
// $Date: 2005/01/28 20:59:06 $
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

package org.systemsbiology.gaggle.geese.names.unitTests;
//--------------------------------------------------------------------------------------
import java.util.*;

import org.systemsbiology.gaggle.geese.names.Translator;
import org.systemsbiology.gaggle.util.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;
//--------------------------------------------------------------------------------------
public class TranslatorTest extends TestCase {

//--------------------------------------------------------------------------------------
/**
 * return a hashmap; the keys are the names to be translated; the values are the desired
 * translated values
 */
protected HashMap createNameMap ()
{
  HashMap result = new HashMap ();
  result.put ("ENSP00000306991", "3037");
  result.put ("ENSP00000304440", "3038");
  result.put ("ENSP00000251595", "3039");
  result.put ("ENSP00000251595", "3040");
  result.put ("ENSP00000292902", "3043");
  result.put ("ENSP00000292902", "3045");
  result.put ("ENSP00000292899", "3046");
  result.put ("ENSP00000292899", "3047");
  result.put ("ENSP00000292899", "3048");
  result.put ("ENSP00000199708", "3049");
  result.put ("ENSP00000252951", "3050");
  result.put ("ENSP00000345727", "3052");
  result.put ("ENSP00000215727", "3053");
  result.put ("ENSP00000340351", "3054");
  result.put ("ENSP00000262651", "3055");
  result.put ("ENSP00000320176", "3059");
  result.put ("ENSP00000264908", "306");
  result.put ("ENSP00000293330", "3060");
  result.put ("ENSP00000263696", "3061");
  result.put ("ENSP00000259784", "3062");

  return result;

} // createNameMap
//--------------------------------------------------------------------------------------
protected org.systemsbiology.gaggle.core.datatypes.Network createSimpleNetwork ()
{
  String a = "ENSP00000263696";
  String b = "ENSP00000215727";
  String c = "ENSP00000304440";
  String d = "ENSP00000259784";

  org.systemsbiology.gaggle.core.datatypes.Interaction i0 = new org.systemsbiology.gaggle.core.datatypes.Interaction(a, b, "protein-protein");
  org.systemsbiology.gaggle.core.datatypes.Interaction i1 = new org.systemsbiology.gaggle.core.datatypes.Interaction(b, c, "protein-protein");
  org.systemsbiology.gaggle.core.datatypes.Interaction i2 = new org.systemsbiology.gaggle.core.datatypes.Interaction(d, a, "protein-DNA", true);

  //Interaction i0 = new Interaction ("ENSP00000263696", "ENSP00000215727", "protein-protein");
  //Interaction i1 = new Interaction ("ENSP00000215727", "ENSP00000304440", "protein-protein");
  //Interaction i2 = new Interaction ("ENSP00000259784", "ENSP00000263696", "protein-DNA", true);

  org.systemsbiology.gaggle.core.datatypes.Network network = new org.systemsbiology.gaggle.core.datatypes.Network();
  network.add (i0);
  network.add (i1);
  network.add (i2);

  return network;

} // createSimpleNetwork
//--------------------------------------------------------------------------------------
protected org.systemsbiology.gaggle.core.datatypes.Network createSimpleNetworkWithOrphans ()
{
  String a = "ENSP00000263696";
  String b = "ENSP00000215727";
  String c = "ENSP00000304440";
  String d = "ENSP00000259784";
  String e = "ENSP00000888888";
  String f = "ENSP00000999999";

  org.systemsbiology.gaggle.core.datatypes.Interaction i0 = new org.systemsbiology.gaggle.core.datatypes.Interaction(a, b, "protein-protein");
  org.systemsbiology.gaggle.core.datatypes.Interaction i1 = new org.systemsbiology.gaggle.core.datatypes.Interaction(b, c, "protein-protein");
  org.systemsbiology.gaggle.core.datatypes.Interaction i2 = new org.systemsbiology.gaggle.core.datatypes.Interaction(d, a, "protein-DNA", true);

  //Interaction i0 = new Interaction ("ENSP00000263696", "ENSP00000215727", "protein-protein");
  //Interaction i1 = new Interaction ("ENSP00000215727", "ENSP00000304440", "protein-protein");
  //Interaction i2 = new Interaction ("ENSP00000259784", "ENSP00000263696", "protein-DNA", true);

  org.systemsbiology.gaggle.core.datatypes.Network network = new org.systemsbiology.gaggle.core.datatypes.Network();
  network.add (i0);
  network.add (i1);
  network.add (i2);
  network.add (e);
  network.add (f);

  return network;
}
//--------------------------------------------------------------------------------------
protected org.systemsbiology.gaggle.core.datatypes.Network createNetworkWithNodeAttributes ()
{
  String a = "ENSP00000263696";
  String b = "ENSP00000215727";
  String c = "ENSP00000304440";
  String d = "ENSP00000259784";

  org.systemsbiology.gaggle.core.datatypes.Network network = createSimpleNetwork ();
  network.addNodeAttribute (a, "moleculeType", "protein");
  network.addNodeAttribute (b, "species", "rat");
  network.addNodeAttribute (c, "count", new Integer (32));
  network.addNodeAttribute (d, "probability", new Double (0.88));

  return network;

} // createNetworkWithNodeAttributes
//--------------------------------------------------------------------------------------
protected org.systemsbiology.gaggle.core.datatypes.Network createNetworkWithEdgeAttributes ()
{
  String a = "ENSP00000263696";
  String b = "ENSP00000215727";
  String c = "ENSP00000304440";
  String d = "ENSP00000259784";

  org.systemsbiology.gaggle.core.datatypes.Network network = createSimpleNetwork ();

  network.addEdgeAttribute (a + " (protein-protein) " + b, "confidence", new Double (0.88));
  network.addEdgeAttribute (b + " (activates) " + c, "confidence", new Double (0.18));
  network.addEdgeAttribute (a + " (protein-DNA) " + b, "source",  "DIP");

  return network;

} // createNetworkWithNodeAttributes
//--------------------------------------------------------------------------------------
public void testEdgeNameParsing () throws Exception
{
  String a = "aaaa";
  String b = "bbbb";
  String type = "eponymous snowball";
  String encodedType = "(" + type + ")";
  String edgeName = a + " (" + type + ") " + b;
  String [] parsedParts = Translator.parseEdgeName (edgeName);
  assertTrue (parsedParts.length == 3);
  assertTrue (parsedParts [0].equals (a));
  assertTrue (parsedParts [2].equals (b));
  assertTrue (parsedParts [1].equals (encodedType));

  String bogusEdgeName = "aa bbb ccc";
  try {
    parsedParts = Translator.parseEdgeName (bogusEdgeName);
    assertTrue (false);
    }
  catch (Exception success) {;}

  bogusEdgeName = "aa ((bbb ccc";
  try {
    parsedParts = Translator.parseEdgeName (bogusEdgeName);
    assertTrue (false);
    }
  catch (Exception success) {;}

  bogusEdgeName = "aa (bbb)) ccc";
  try {
    parsedParts = Translator.parseEdgeName (bogusEdgeName);
    assertTrue (false);
    }
  catch (Exception success) {;}


} // testEdgeNameParsing
//--------------------------------------------------------------------------------------
public void testSingleNames () throws Exception 
{
  System.out.println ("testSingleNames");
  NewNameHelper nameHelper = new NewNameHelper ("humanStringToGeneId.tsv");
  Translator translator = new Translator (nameHelper);
  String targetNameSpace = "GeneID";  // a column in the input file

    // testMap provides names in need of mapping (in this case, ENSP numbers)
    // and the names they map to (geneIDs). this is artificial, of course, since
    // the Translator finds translations we do not already know.

  HashMap testMap = createNameMap ();
  String [] originalNames = (String []) testMap.keySet().toArray (new String [0]);

  for (int i=0; i < originalNames.length; i++) {
    String expected = (String) testMap.get (originalNames [i]);
    assertTrue (translator.get (originalNames [i], "GeneID").equals (expected));
    } // for i

  assertTrue (translator.get ("fubar", "GeneID").equals ("fubar"));
  assertTrue (translator.get (originalNames [0], "bogusNameSpace").equals (originalNames [0]));

} // testSingleNames
//--------------------------------------------------------------------------------------
public void testSingleNamesWithNoNameHelper () throws Exception 
{
  System.out.println ("testSingleNamesWithNoNameHelper");
  NewNameHelper nameHelper = new NewNameHelper ("humanStringToGeneId.tsv");
  Translator translator = new Translator (nameHelper);
  String targetNameSpace = "GeneID";  // a column in the input file

    // testMap provides names in need of mapping (in this case, ENSP numbers)
    // and the names they map to (geneIDs). this is artificial, of course, since
    // the Translator finds translations we do not already know.

  HashMap testMap = createNameMap ();
  String [] originalNames = (String []) testMap.keySet().toArray (new String [0]);

  for (int i=0; i < originalNames.length; i++) {
    String expected = (String) testMap.get (originalNames [i]);
    assertTrue (translator.get (originalNames [i], "GeneID").equals (expected));
    } // for i

  assertTrue (translator.get ("fubar", "GeneID").equals ("fubar"));
  assertTrue (translator.get (originalNames [0], "bogusNameSpace").equals (originalNames [0]));

} // testSingleNames
//--------------------------------------------------------------------------------------
public void testNameList () throws Exception 
{
  System.out.println ("testNameList");
  NewNameHelper nameHelper = new NewNameHelper ("humanStringToGeneId.tsv");
  Translator translator = new Translator (nameHelper);
  String targetNameSpace = "GeneID";  // a column in the input file

    // testMap provides names in need of mapping (in this case, ENSP numbers)
    // and the names they map to (geneIDs). this is artificial, of course, since
    // the Translator finds translations we do not already know.

  HashMap testMap = createNameMap ();
  String [] originalNames = (String []) testMap.keySet().toArray (new String [0]);
  String [] translatedNames = translator.get (originalNames, targetNameSpace);

  for (int i=0; i < originalNames.length; i++) {
    String expected = (String) testMap.get (originalNames [i]);
    assertTrue (translatedNames [i].equals (expected));
    } // for i

} // testNameList
//--------------------------------------------------------------------------------------
public void testInteraction () throws Exception 
{
  System.out.println ("testInteraction");
  NewNameHelper nameHelper = new NewNameHelper ("humanStringToGeneId.tsv");
  Translator translator = new Translator (nameHelper);
  String targetNameSpace = "GeneID";  // a column in the input file
  HashMap testMap = createNameMap ();
  String [] originalNames = (String []) testMap.keySet().toArray (new String [0]);

  String source = originalNames [0];
  String target = originalNames [originalNames.length - 1];
  String expectedTranslatedSource = (String) testMap.get (source);
  String expectedTranslatedTarget = (String) testMap.get (target);

  String type = "pd";
  boolean directed = true;
  org.systemsbiology.gaggle.core.datatypes.Interaction orig = new org.systemsbiology.gaggle.core.datatypes.Interaction(source, target, type, directed);
  org.systemsbiology.gaggle.core.datatypes.Interaction translated = translator.get (orig, targetNameSpace);

  assertTrue (translated.getSource().equals (expectedTranslatedSource));
  
} // testInteraction 
//--------------------------------------------------------------------------------------
public void testSimpleNetwork () throws Exception 
{
  System.out.println ("testSimpleNetwork");
  NewNameHelper nameHelper = new NewNameHelper ("humanStringToGeneId.tsv");
  Translator translator = new Translator (nameHelper);
  String targetNameSpace = "GeneID";  // a column in the input file

    // testMap provides names in need of mapping (in this case, ENSP numbers)
    // and the names they map to (geneIDs). this is artificial, of course, since
    // the Translator finds translations we do not already know.  it has the
    // same names as those in 'humanStringToGeneId.tsv' above

  HashMap testMap = createNameMap ();
  org.systemsbiology.gaggle.core.datatypes.Network network = createSimpleNetwork ();
  org.systemsbiology.gaggle.core.datatypes.Network xNetwork = translator.get (network, targetNameSpace);

  assertTrue (network.nodeCount () == xNetwork.nodeCount ());
  assertTrue (network.edgeCount () == xNetwork.edgeCount ());
  assertTrue (network.getOrphanNodeCount () == xNetwork.getOrphanNodeCount ());

  org.systemsbiology.gaggle.core.datatypes.Interaction[] origInteractions = network.getInteractions ();
  org.systemsbiology.gaggle.core.datatypes.Interaction[] translatedInteractions = xNetwork.getInteractions ();
  assertTrue (origInteractions.length == translatedInteractions.length);
  assertTrue (origInteractions.length == 3);

  for (int i=0; i < origInteractions.length; i++) {
   org.systemsbiology.gaggle.core.datatypes.Interaction orig = origInteractions [i];
   org.systemsbiology.gaggle.core.datatypes.Interaction x = translatedInteractions [i];

   String origSource = orig.getSource ();
   String expectedSource = (String) testMap.get (origSource);
   String actualSource = x.getSource ();
   assertTrue (expectedSource.equals (actualSource));

   String origTarget = orig.getTarget ();
   String expectedTarget = (String) testMap.get (origTarget);
   String actualTarget = x.getTarget ();
   assertTrue (expectedTarget.equals (actualTarget));

   assertTrue (orig.getType().equals (x.getType()));
   assertTrue (orig.isDirected() == (x.isDirected()));

    } // for i

} // testSimpleNetwork
//--------------------------------------------------------------------------------------
public void testNetworkWithNodeAttributes () throws Exception 
{
  System.out.println ("testNetworkWithNodeAttributes");
  NewNameHelper nameHelper = new NewNameHelper ("humanStringToGeneId.tsv");
  Translator translator = new Translator (nameHelper);
  String targetNameSpace = "GeneID";  // a column in the input file

    // testMap provides names in need of mapping (in this case, ENSP numbers)
    // and the names they map to (geneIDs). this is artificial, of course, since
    // the Translator finds translations we do not already know.  it has the
    // same names as those in 'humanStringToGeneId.tsv' above

  HashMap testMap = createNameMap ();
  org.systemsbiology.gaggle.core.datatypes.Network oNetwork = createNetworkWithNodeAttributes ();
  org.systemsbiology.gaggle.core.datatypes.Network xNetwork = translator.get (oNetwork, targetNameSpace);
  
  String [] origAttributeNames = oNetwork.getNodeAttributeNames ();
  String [] xAttributeNames = xNetwork.getNodeAttributeNames ();

  Arrays.sort (origAttributeNames);
  String [] expectedAttributeNames = {"moleculeType", "species", "count", "probability"};
  Arrays.sort (expectedAttributeNames);
  assertTrue (Arrays.equals (origAttributeNames, expectedAttributeNames));
  assertTrue (origAttributeNames.length == xAttributeNames.length);
  Arrays.sort (xAttributeNames);
  assertTrue (Arrays.equals (xAttributeNames, expectedAttributeNames));

  for (int i=0; i < xAttributeNames.length; i++) {
    HashMap xMap = xNetwork.getNodeAttributes (xAttributeNames [i]);
    HashMap oMap = oNetwork.getNodeAttributes (xAttributeNames [i]);
    String [] origNodeNames = (String []) oMap.keySet().toArray (new String [0]);
    String [] xNodeNames = (String []) xMap.keySet().toArray (new String [0]);
    Arrays.sort (origNodeNames);
    Arrays.sort (xNodeNames);
    for (int n=0; n < origNodeNames.length; n++) {
      String originalNodeName = origNodeNames [n];
      String expectedNodeName = (String) testMap.get (originalNodeName);
      String actualTranslatedNodeName = xNodeNames [n];
      assertTrue (expectedNodeName.equals (actualTranslatedNodeName));
      Object origValue = oMap.get (originalNodeName);
      assertTrue (origValue != null);
      Object translatedValue = xMap.get (actualTranslatedNodeName);
      assertTrue (origValue.getClass () == translatedValue.getClass ());
      assertTrue (origValue == translatedValue);
      } // for n
    } // for i

} // testNetworkWithNodeAttributes
//--------------------------------------------------------------------------------------
public void testNetworkWithEdgeAttributes () throws Exception 
{
  System.out.println ("testNetworkWithEdgeAttributes");
  NewNameHelper nameHelper = new NewNameHelper ("humanStringToGeneId.tsv");
  Translator translator = new Translator (nameHelper);
  String targetNameSpace = "GeneID";  // a column in the input file

    // testMap provides names in need of mapping (in this case, ENSP numbers)
    // and the names they map to (geneIDs). this is artificial, of course, since
    // the Translator finds translations we do not already know.  it has the
    // same names as those in 'humanStringToGeneId.tsv' above

  HashMap testMap = createNameMap ();
  org.systemsbiology.gaggle.core.datatypes.Network oNetwork = createNetworkWithEdgeAttributes ();
  org.systemsbiology.gaggle.core.datatypes.Network xNetwork = translator.get (oNetwork, targetNameSpace);

  String [] origAttributeNames = oNetwork.getEdgeAttributeNames ();
  String [] xAttributeNames = xNetwork.getEdgeAttributeNames ();

  Arrays.sort (origAttributeNames);
  String [] expectedAttributeNames = {"confidence", "source"};
  Arrays.sort (expectedAttributeNames);
  assertTrue (Arrays.equals (origAttributeNames, expectedAttributeNames));
  assertTrue (origAttributeNames.length == xAttributeNames.length);
  Arrays.sort (xAttributeNames);
  assertTrue (Arrays.equals (xAttributeNames, expectedAttributeNames));

  for (int i=0; i < xAttributeNames.length; i++) {
    HashMap xMap = xNetwork.getEdgeAttributes (xAttributeNames [i]);
    HashMap oMap = oNetwork.getEdgeAttributes (xAttributeNames [i]);
    String [] origEdgeNames = (String []) oMap.keySet().toArray (new String [0]);
    String [] xEdgeNames = (String []) xMap.keySet().toArray (new String [0]);
    Arrays.sort (origEdgeNames);
    Arrays.sort (xEdgeNames);
    for (int n=0; n < origEdgeNames.length; n++) {
      String [] parsedPartsOrig = Translator.parseEdgeName (origEdgeNames [n]);
      String aOrig = parsedPartsOrig [0];
      String bOrig = parsedPartsOrig [2];
      String edgeTypeOrig = parsedPartsOrig [1];
      String [] parsedPartsNew = Translator.parseEdgeName (xEdgeNames [n]);
      String aNew = parsedPartsNew [0];
      String bNew = parsedPartsNew [2];
      String edgeTypeNew = parsedPartsNew [1];
      assertTrue (edgeTypeOrig.equals (edgeTypeNew));
      
      String expectedNewA = (String) testMap.get (aOrig);
      String expectedNewB = (String) testMap.get (bOrig);
      //System.out.println ("expected a: " + expectedNewA);
      //System.out.println ("  actual a: " + aNew);
      //System.out.println ("expected b: " + expectedNewB);
      //System.out.println ("  actual b: " + bNew);
      assertTrue (expectedNewA.equals (aNew));
      assertTrue (expectedNewB.equals (bNew));

         /*********
      String originalEdgeName = origEdgeNames [n];
      String expectedEdgeName = (String) testMap.get (originalEdgeName);
      String actualTranslatedEdgeName = xEdgeNames [n];
      assertTrue (expectedEdgeName.equals (actualTranslatedEdgeName));
      Object origValue = oMap.get (originalEdgeName);
      assertTrue (origValue != null);
      Object translatedValue = xMap.get (actualTranslatedEdgeName);
      assertTrue (origValue.getClass () == translatedValue.getClass ());
      assertTrue (origValue == translatedValue);
        ******************/
      } // for n
    } // for i

} // testNetworkWithEdgeAttributes
//--------------------------------------------------------------------------------------
public void testNetworkWithOrphans () throws Exception 
{
  System.out.println ("testNetworkWithOrphans");
  NewNameHelper nameHelper = new NewNameHelper ("humanStringToGeneId.tsv");
  Translator translator = new Translator (nameHelper);
  String targetNameSpace = "GeneID";  // a column in the input file

    // testMap provides names in need of mapping (in this case, ENSP numbers)
    // and the names they map to (geneIDs). this is artificial, of course, since
    // the Translator finds translations we do not already know.  it has the
    // same names as those in 'humanStringToGeneId.tsv' above

  HashMap testMap = createNameMap ();
  org.systemsbiology.gaggle.core.datatypes.Network network = createSimpleNetworkWithOrphans ();
  org.systemsbiology.gaggle.core.datatypes.Network xNetwork = translator.get (network, targetNameSpace);

  assertTrue (network.nodeCount () == xNetwork.nodeCount ());
  assertTrue (network.edgeCount () == xNetwork.edgeCount ());
  assertTrue (network.getOrphanNodeCount () == xNetwork.getOrphanNodeCount ());

  org.systemsbiology.gaggle.core.datatypes.Interaction[] origInteractions = network.getInteractions ();
  org.systemsbiology.gaggle.core.datatypes.Interaction[] translatedInteractions = xNetwork.getInteractions ();
  assertTrue (origInteractions.length == translatedInteractions.length);
  assertTrue (origInteractions.length == 3);

  for (int i=0; i < origInteractions.length; i++) {
    org.systemsbiology.gaggle.core.datatypes.Interaction orig = origInteractions [i];
    org.systemsbiology.gaggle.core.datatypes.Interaction x = translatedInteractions [i];
 
    String origSource = orig.getSource ();
    String expectedSource = (String) testMap.get (origSource);
    String actualSource = x.getSource ();
    assertTrue (expectedSource.equals (actualSource));
 
    String origTarget = orig.getTarget ();
    String expectedTarget = (String) testMap.get (origTarget);
    String actualTarget = x.getTarget ();
    assertTrue (expectedTarget.equals (actualTarget));
 
    assertTrue (orig.getType().equals (x.getType()));
    assertTrue (orig.isDirected() == (x.isDirected()));

    } // for i

} // testNetworkWithOrphans
//--------------------------------------------------------------------------------------
public void testSingleNamesHaloEC () throws Exception 
{
  System.out.println ("testSingleNamesHaloEC");
  NewNameHelper nameHelper = new NewNameHelper ("halo-ec-to-orf.tsv");
  Translator translator = new Translator (nameHelper);
  String targetNameSpace = "VNG-ORF";  // a column in the input file

  String orf = "VNG2220G";
  String EC =  "1.8.1.4";
  assertTrue (translator.get (EC, "VNG-ORF").equals (orf));
  assertTrue (translator.get (orf, "EC").equals (EC));

} // testSingleNamesHaloEC
//--------------------------------------------------------------------------------------
public void testMatrix () throws Exception 
{
  System.out.println ("testMatrix");
  NewNameHelper nameHelper = new NewNameHelper ("humanStringToGeneId.tsv");
  Translator translator = new Translator (nameHelper);
  String targetNameSpace = "GeneID";  // a column in the input file

  org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix = new org.systemsbiology.gaggle.core.datatypes.DataMatrix();
  int dataRows = 3;
  int dataColumns = 4;
  String [] columnTitles = {"one", "two", "three", "four"};
  String [] rowTitles = {"ENSP00000306991", "ENSP00000292899", "ENSP00000263696"};
  matrix.setSize (3, 4);
  matrix.setRowTitlesTitle ("GENE");
  matrix.setColumnTitles (columnTitles);
  matrix.setRowTitles (rowTitles);

  for (int r=0; r < dataRows; r ++)
    for (int c=0; c < dataColumns; c++)
      matrix.set (r, c, (r * 10.0) + c * 10.0);

  org.systemsbiology.gaggle.core.datatypes.DataMatrix xMatrix = translator.get (matrix, targetNameSpace);

   //----------------------------------------------------
   // were the invariant parts of the matrix preserved?
   //----------------------------------------------------

  assertTrue (xMatrix.getRowCount () == matrix.getRowCount ());
  assertTrue (xMatrix.getColumnCount () == matrix.getColumnCount ());
  String [] xColumnTitles = xMatrix.getColumnTitles ();
  for (int c=0; c < matrix.getColumnCount (); c++) 
    assertTrue (xColumnTitles [c].equals (columnTitles [c]));
  assertTrue (xMatrix.getRowTitlesTitle ().equals (matrix.getRowTitlesTitle ()));

  for (int r=0; r < matrix.getRowCount (); r++) {
    double [] oldRow = matrix.get (r);
    double [] newRow = xMatrix.get (r);
    assertTrue (oldRow.length == newRow.length);
    for (int c=0; c < oldRow.length; c++)
      assertTrue (oldRow [c] == newRow [c]);
    } // for i

   //------------------------------------------------------
   // the real test:  were the row names properly changed?
   //------------------------------------------------------

    // testMap provides names in need of mapping (in this case, ENSP numbers)
    // and the names they map to (geneIDs). this is artificial, of course, since
    // the Translator finds translations we do not already know.  it has the
    // same names as those in 'humanStringToGeneId.tsv' above

  HashMap testMap = createNameMap ();

  String [] oldRowNames = matrix.getRowTitles ();
  String [] newRowNames = xMatrix.getRowTitles ();

  assertTrue (oldRowNames.length == matrix.getRowCount ());
  assertTrue (newRowNames.length == matrix.getRowCount ());
  for (int i=0; i < newRowNames.length; i++) {
    String oldRowName = oldRowNames [i];
    String newRowName = newRowNames [i];
    assertTrue (testMap.containsKey (oldRowName));
    String expectedTranslatedName = (String) testMap.get (oldRowName);
    assertTrue (expectedTranslatedName.equals (newRowName));
    } // for i

} // testMatrix
//--------------------------------------------------------------------------------------
public void NOT_A_testAssociativeArray () throws Exception // todo refactor this test!
{
  System.out.println ("testAssociativeArray");

  NewNameHelper nameHelper = new NewNameHelper ("humanStringToGeneId.tsv");
  Translator translator = new Translator (nameHelper);
  String targetNameSpace = "GeneID";  // a column in the input file

  HashMap map = new HashMap ();

  double [] values = {0.1, 0.2, 0.3, 0.4};
  String [] rowNames = {"ENSP00000306991",
			"ENSP00000251595",
			"ENSP00000292902",
			"ENSP00000292899"};

  String attributeName = "log 10 ratio, simulated";
  ArrayList namesAndValues = new ArrayList ();
  namesAndValues.add (rowNames);
  namesAndValues.add (values);
  map.put (attributeName, namesAndValues);

  /////////HashMap xMap = translator.get (map, targetNameSpace); //todo refactor this test
  HashMap xMap = null;
  assertTrue (xMap.containsKey (attributeName));

  HashMap testMap = createNameMap ();

  ArrayList xArrayList = (ArrayList) xMap.get (attributeName);
  assertTrue (xArrayList.size () == 2);
  String [] xNames = (String []) xArrayList.get (0);
  assertTrue (xNames.length == rowNames.length);

  for (int i=0; i < rowNames.length; i++) {
    String oldName = rowNames [i];
    String newName = xNames [i];
    assertTrue (testMap.containsKey (oldName));
    String expectedTranslatedName = (String) testMap.get (oldName);
    assertTrue (expectedTranslatedName.equals (newName));
    } // for i

} // testAssociativeArray
//--------------------------------------------------------------------------------------
public static void main (String [] args)
{
  junit.textui.TestRunner.run (new TestSuite (TranslatorTest.class));
}
//--------------------------------------------------------------------------------------
} // class TranslatorTest
