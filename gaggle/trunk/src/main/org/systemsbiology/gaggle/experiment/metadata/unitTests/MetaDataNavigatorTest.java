// MetaDataNavigatorTest.java
//------------------------------------------------------------------------------
// $Revision: 732 $
// $Date: 2005/04/03 04:37:01 $
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

package org.systemsbiology.gaggle.experiment.metadata.unitTests;
//--------------------------------------------------------------------------------------
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.io.File;
import java.util.*;
import org.systemsbiology.gaggle.experiment.metadata.*;
//------------------------------------------------------------------------------
public class MetaDataNavigatorTest extends TestCase {

//------------------------------------------------------------------------------
public void testSimple () throws Exception
{

  /****
     construct a navigator with a number of xml files in a specified
     directory.  do some simple sanity tests on the tree structure
     (a hash of hashes of hashes of ...) created and available from
     the navigator
  ****/

  System.out.println (".testSimple");
  String uri = "file://../sampleData/reposTiny";
  MetaDataNavigator nav = new MetaDataNavigator (uri);
  String [] experimentNames = nav.getExperimentNames ();
  int experimentCount = experimentNames.length;

  assertTrue (experimentCount == 8);
  String [] expectedNames = new String [] {"boa1.xml", "boa4.xml", "bop.xml", "copper.xml",
                                           "iron.xml", "manganese.xml", "zinc.xml", 
                                           "zincAndSink.xml"};
  Arrays.sort (experimentNames);
  assertTrue (Arrays.equals (experimentNames, expectedNames));

  String [] expectedTitles = new String [] {"boa1 knockout, light and oxygen",
                                            "boa4 knockout, light and oxygen",
                                            "bop knockout, light and oxygen",
                                            "copper concentrations",
                                            "iron concentrations",
                                            "manganese concentrations",
                                            "zinc and sink concentrations",
                                            "zinc concentrations"
                                            };


  String [] experimentTitles = nav.getExperimentTitles ();
  assertTrue (experimentTitles.length == 8);
  Arrays.sort (experimentTitles);
  assertTrue (Arrays.equals (experimentTitles, expectedTitles));

    // an experiment (actually, it's metadata) can be retrieved from the navigator
    // by name or by title.  make sure that we get the same experiment 
    // using each approach.

  for (int i=0; i < experimentNames.length; i++) {
    MetaData experiment = nav.getExperimentByName (experimentNames [i]);
    MetaData experimentCopy = nav.getExperimentByTitle (experiment.getTitle ());
    String [] predicateNames = experiment.getPredicateNamesSorted ();
    String [] predicateNames2 = experimentCopy.getPredicateNamesSorted ();
    assertTrue (Arrays.equals (predicateNames, new String [] {"perturbation", "species", "strain"}));
    assertTrue (Arrays.equals (predicateNames, predicateNames2));
    assertTrue (experiment.toString().equals (experimentCopy.toString ()));
    HashMap predicates = experiment.getPredicates ();
    String [] varNames = experiment.getVariableNames ();
    } // for i


    // the navigator also makes the full set of experiments available, in
    // a hashes, either keyed by experiment name, or by experiment title
    // get those hashes, and make sure that their keys are the expected
    // collection of strings

  HashMap byNameHash = nav.getExperimentsHashedByName ();
  assertTrue (byNameHash.size () == experimentCount);
  String [] keys = (String []) byNameHash.keySet().toArray (new String [0]);
  Arrays.sort (keys);
  assertTrue (Arrays.equals (keys, expectedNames));

  HashMap byTitleHash = nav.getExperimentsHashedByTitle ();
  assertTrue (byTitleHash.size () == experimentCount);
  keys = (String []) byTitleHash.keySet().toArray (new String [0]);
  Arrays.sort (keys);
  assertTrue (Arrays.equals (keys, expectedTitles));


} // simpleTest
//------------------------------------------------------------------------------
public void testTree () throws Exception
{
   // the navigator will supply a tree which describes the entire hierarchy
   // of experiements.  for the test set of experiments, the tree has two
   // roots: environmental & genetic
   //
   //  environmental			    genetic
   //    metals			   	      knockout
   //      copper			        boa1
   //        concentration		          illumination
   //          1000			            Dark
   //          700			            Light
   //          850			          oxygen
   //      iron			   	            High
   //        concentration		            Low
   //          2			        boa4
   //          4			          illumination
   //          6			            Dark
   //          7			            Light
   //      manganese			          oxygen
   //        concentration		            High
   //          1000			            Low
   //          1500			        bop
   //          800			          illumination
   //      zinc			   	            Dark
   //        concentration		            Light
   //          10			          oxygen
   //          20			            High
   //          5                                    Low                 

  System.out.println ("testTree");
  String uri = "file://../sampleData/reposTiny";
  MetaDataNavigator nav = new MetaDataNavigator (uri);

  // nav.dumpTree (nav.getTree (), "");

  HashMap tree = nav.getTree ();
  assertTrue (tree.containsKey ("genetic"));
  assertTrue (tree.containsKey ("environmental"));
  HashMap geneticBranch = (HashMap) tree.get ("genetic");
  HashMap envBranch = (HashMap) tree.get ("environmental");

  assertTrue (geneticBranch.containsKey ("knockout"));
  HashMap knockoutBranch = (HashMap) geneticBranch.get ("knockout");
  assertTrue (knockoutBranch.containsKey ("boa1"));
  assertTrue (knockoutBranch.containsKey ("boa4"));
  assertTrue (knockoutBranch.containsKey ("bop"));
  
  assertTrue (envBranch.containsKey ("metals"));
  HashMap metalsBranch = (HashMap) envBranch.get ("metals");

  assertTrue (metalsBranch.containsKey ("copper"));
  assertTrue (metalsBranch.containsKey ("zinc"));
  assertTrue (metalsBranch.containsKey ("copper"));
  assertTrue (metalsBranch.containsKey ("manganese"));

  HashMap manganeseBranch = (HashMap) metalsBranch.get ("manganese");
  assertTrue (manganeseBranch.containsKey ("concentration"));
  HashMap mnConcBranch = (HashMap) manganeseBranch.get ("concentration");
  HashMap mnConcValues = (HashMap) manganeseBranch.get ("concentration");
  String [] concentrations = (String []) mnConcValues.keySet().toArray (new String [0]);
  assertTrue (concentrations.length == 3);
  String [] expected = new String [] {"1000", "1500", "800"};
  Arrays.sort (concentrations);
  assertTrue (Arrays.equals (concentrations, expected));

   // what are the values stored in the last hashmap of this tree?
   // they should be empty hashmaps.

  for (int i=0; i < expected.length; i++) {
    HashMap shouldBeEmpty = (HashMap) mnConcValues.get (expected [i]);
    assertTrue (shouldBeEmpty.size () == 0);
    }

} // testTree
//------------------------------------------------------------------------------
public void testLookupByPerturbationsString1 () throws Exception

/****
 *  the MetaDataNavigator reads a repository of xml files, typically from a directory
 *  on a file system, on the web, or out of a database.  it creates a hierarchical tree of
 *  the experiments described in these files, which is good input for
 *  a JTree (and thus, for presentation in a Java GUI program).
 *  this test simulates that situation.
 * 
 *  when the user makes selections in the JTree, the JTree selection listener
 *  gets a list of strings -- one list for each current selection. for example
 *     [["genetic", "knockout", "boa1", "illumination", "Dark"]]
 *  this test method makes sure that the MetaDataNavigator client can get the Experiment
 *  object corresponding to this list
 * 
 */
{
  System.out.println ("testLookupByPerturbationsString1");

       //----------------------------------------------------
       // set up the navigator
       //----------------------------------------------------

  String uri = "file://../sampleData/reposTiny";
  MetaDataNavigator nav = new MetaDataNavigator (uri);
  String [] experimentNames = nav.getExperimentNames ();
  String [] experimentTitles = nav.getExperimentTitles ();
  int experimentCount = experimentNames.length;

     //----------------------------------------------------
     // set up the simulated JTree selection output, specifying
     // one condition (illumination = Dark) in the experiment
     // categorized genetic -> knockout -> boa1
     //----------------------------------------------------

   String [] selection = new String [] {
           "genetic", "knockout", "boa1", "illumination", "Dark"};

   ArrayList resultList = nav.findExperimentKeyForPerturbation (selection);
   assertTrue (resultList.size () == 1);
   String [] result = (String []) resultList.get (0);
   String experimentKey = result [0];
   String conditionString = result [1];
   assertTrue (experimentKey.equals ("genetic:knockout:boa1"));
   assertTrue (conditionString.equals ("illumination:Dark"));


} // testLookupByPerturbationsString1
//----------------------------------------------------------------------
public void testLookupByPerturbationsString2 () throws Exception

/****
 *  the MetaDataNavigator reads a repository of xml files, typically from a directory
 *  on a file system, on the web, or out of a database.  it creates a hierarchical tree of
 *  the experiments described in these files, which is good input for
 *  a JTree (and thus, for presentation in a Java GUI program).
 *  this test simulates that situation.
 * 
 *  when the user makes selections in the JTree, the JTree selection listener
 *  gets a list of strings -- one list for each current selection. for example
 *     [["genetic", "knockout", "boa1", "illumination", "Dark"]]
 *  this test method makes sure that the MetaDataNavigator client can get the Experiment
 *  object corresponding to this list
 * 
 */
{
  System.out.println ("testLookupByPerturbationsString2");

       //----------------------------------------------------
       // set up the navigator
       //----------------------------------------------------

  String uri = "file://../sampleData/reposTiny";
  MetaDataNavigator nav = new MetaDataNavigator (uri);
  String [] experimentNames = nav.getExperimentNames ();
  String [] experimentTitles = nav.getExperimentTitles ();
  int experimentCount = experimentNames.length;

     //----------------------------------------------------
     // set up the simulated JTree selection output, specifying
     // no conditions in the experiment
     // categorized genetic -> knockout -> boa1
     //----------------------------------------------------

   String [] selection = new String [] { "genetic", "knockout", "boa1"};
   ArrayList resultList = nav.findExperimentKeyForPerturbation (selection);
   assertTrue (resultList.size () == 1);
   String [] result = (String []) resultList.get (0);
   assertTrue (result [0].equals ("genetic:knockout:boa1"));
   assertTrue (result [1] == null);


} // testLookupByPerturbationsString1
//----------------------------------------------------------------------
public void testLookupByPerturbationsString3 () throws Exception
{
  System.out.println ("testLookupByPerturbationsString3");

       //----------------------------------------------------
       // set up the navigator
       //----------------------------------------------------

  String uri = "file://../sampleData/reposTiny";
  MetaDataNavigator nav = new MetaDataNavigator (uri);
  String [] experimentNames = nav.getExperimentNames ();
  String [] experimentTitles = nav.getExperimentTitles ();
  int experimentCount = experimentNames.length;

  String [] selection = new String [] {"genetic", "knockout"};
  ArrayList resultList = nav.findExperimentKeyForPerturbation (selection);
   assertTrue (resultList.size () == 3);

  String [] expected = new String [] {"genetic:knockout:boa1",
                                      "genetic:knockout:boa4",
                                      "genetic:knockout:bop"};
   
  for (int i=0; i < resultList.size (); i++) {
    String[] result = (String []) resultList.get (i);
    String perturbation = result [0];
    assertTrue (Arrays.binarySearch (expected, perturbation) >= 0);
    assertTrue (result [1] == null);
    }

} // testLookupByPerturbationsString3
//----------------------------------------------------------------------
/**
 * we want "aa:bb:cc" 
 * to match 
 *        "aa:bb:cc"
 *        "aa:bb:cc:dd"
 *        "aa:bb:cc:ee"
 * but not
 *        "aa:bb:ccxxx"
 * in sampleData/reposTiny we have a legitimate zinc experient xml file
 * and a ginned up file, to test the above condition.
 *
 *  <predicate category='perturbation' value='environmental:metals:zinc'/>
.*  <predicate category='perturbation' value='environmental:metals:zincAndSink'/>
 *
 * in this test, make sure that "environmental:metals:zinc" only gets the first experiment
 */
public void testLookupByPerturbationsString4 () throws Exception
{
  System.out.println ("testLookupByPerturbationsString4");


  String uri = "file://../sampleData/reposTiny";
  MetaDataNavigator nav = new MetaDataNavigator (uri);
  String [] experimentNames = nav.getExperimentNames ();
  String [] experimentTitles = nav.getExperimentTitles ();
  int experimentCount = experimentNames.length;

  String [] selection = new String [] {"environmental", "metals", "zinc"};
  ArrayList resultList = nav.findExperimentKeyForPerturbation (selection);
  assertTrue (resultList.size () == 1);
  String [] result = (String []) resultList.get (0);
  assertTrue (result [0].equals ("environmental:metals:zinc"));
  assertTrue (result [1] == null); // implies: get all conditions from this experiment

} // testLookupByPerturbationsString4
//----------------------------------------------------------------------
/**
 */
public void testLookupByPerturbationsStringEmpty () throws Exception
{
  System.out.println ("testLookupByPerturbationsStringEmpty");


  String uri = "file://../sampleData/reposTiny";
  MetaDataNavigator nav = new MetaDataNavigator (uri);
  String [] selection = new String [] {};
  ArrayList resultList = nav.findExperimentKeyForPerturbation (selection);
  assertTrue (resultList.size () == 0);

} // testLookupByPerturbationsStringEmpty
//----------------------------------------------------------------------
public void testGetWholeExperiments () throws Exception
{
  /***
     ask for all experiments known to the navigator categorized
     as "genetic - knockout".  there should be 3

   ***/

  System.out.println ("testGetWholeExperiment");
  String uri = "file://../sampleData/reposTiny";
  MetaDataNavigator nav = new MetaDataNavigator (uri);

  String [] simulatedTreeSelection = new String [] {"genetic", "knockout"};

      // for each simulated selection, above, ask the navigator to find the
      // experiement key and the condition/value pairs implied by the simulated 
      // selection.  since multiple experiments may match (which implies
      // that no sub-selected conditions in any of them) an array of
      // [experimentKey, condition] pairs is returned
      // the experiment key can then be used to retrieve the 
      // actual experiment; and the condition value, if not None, can
      // select some conditions withing the actual experiment


  ArrayList resultsList = nav.findExperimentKeyForPerturbation (simulatedTreeSelection);
  assertTrue (resultsList.size () == 3);
  String [] expectedExperiments = new String [] {"genetic:knockout:boa1",
                                                 "genetic:knockout:boa4",
                                                 "genetic:knockout:bop"};
  for (int i=0; i < resultsList.size (); i++) {
    String [] result = (String []) resultsList.get (i);
    assertTrue (result.length == 2);
    String experimentName = result [0];
    String conditions = result [1];
    assertTrue (conditions == null); // which indicates: all conditions
    } // for i

} // testGetWholeExperiments
//----------------------------------------------------------------------
public void testGetWholeCondition () throws Exception
{
 
  System.out.println ("testGetWholeCondition");

  String uri = "file://../sampleData/reposTiny";
  MetaDataNavigator nav = new MetaDataNavigator (uri);

  String [] simulatedTreeSelection = new String [] {"genetic", "knockout", "boa4", "oxygen"};

  ArrayList resultList = nav.findExperimentKeyForPerturbation (simulatedTreeSelection);
  assertTrue (resultList.size () == 1);
  String [] result0 = (String []) resultList.get (0);
  String experimentKey = result0 [0];
  String condition = result0 [1];
  assertTrue (experimentKey.equals ("genetic:knockout:boa4"));
  assertTrue (condition.equals ("oxygen"));
 
}
//----------------------------------------------------------------------
public void testGetExperimentsByPerturbationList () throws Exception
/**
 *  get all metadata (all experiments) which exactly correspond
 *  to a specified perturbationString, which looks like this:
 *    genetic:knockout:boa1
 *  this perturbation string is part of the metadata associated with
 *  every experiment.  in xml (currently our only metadata format) the
 *  markup looks like this:
 *
 *  <predicate category='perturbation' value='genetic:knockout:boa1'/>
 *
 */
{
  System.out.println ("testGetExperimentsByPerturbationList");

  String uri = "file://../sampleData/reposTiny";
  MetaDataNavigator nav = new MetaDataNavigator (uri);

  MetaData [] result = nav.getExperimentByPerturbationList ("bogus:fake:trumpedUp");
  assertTrue (result.length == 0);

  String [] allPerturbations = nav.getPerturbationStrings ();
  for (int i=0; i < allPerturbations.length; i++) {
    result = nav.getExperimentByPerturbationList (allPerturbations [i]);
    assertTrue (result.length >= 1);
    }

} // testGetExperimentsByPerturbationList");
//----------------------------------------------------------------------
public void testDoubleKnockoutBug () throws Exception
/**
 * the halo group has 3 knockout experiments:
 *    phr1              genetic:knockout:phr1
 *    phr2              genetic:knockout:phr2
 *    prh1_and_2        genetic:knockout:phr1 and phr2
 * make sure we can selectively, and singly, load each experiment's metadata.
 * 
 */
{
  System.out.println ("testDoubleKnockoutBug");

  String uri = "file://../sampleData/doubleKnockoutBug";
  MetaDataNavigator nav = new MetaDataNavigator (uri);
  ArrayList resultList;
  String [] result0;
  String experimentKey, condition;

   /******************************
   // ---- first get the single phr1 knockout
  String [] phr1Tree = new String [] {"genetic", "knockout", "phr1"};
  resultList = nav.findExperimentKeyForPerturbation (phr1Tree);
  assertTrue (resultList.size () == 1);
  result0 = (String []) resultList.get (0);
  experimentKey = result0 [0];
  condition = result0 [1];
  assertTrue (experimentKey.equals ("genetic:knockout:phr1"));
  assertTrue (condition == null);

   // ---- now get the single phr2 knockout
  String [] phr2Tree = new String [] {"genetic", "knockout", "phr2"};
  resultList = nav.findExperimentKeyForPerturbation (phr2Tree);
  assertTrue (resultList.size () == 1);
  result0 = (String []) resultList.get (0);
  experimentKey = result0 [0];
  condition = result0 [1];
  assertTrue (experimentKey.equals ("genetic:knockout:phr2"));
  assertTrue (condition == null);
   ******************************/

   // ---- now get the double knockout
  String [] phr12Tree = new String [] {"genetic", "knockout", "phr1 and phr2"};
  resultList = nav.findExperimentKeyForPerturbation (phr12Tree);
  // System.out.println ("experiment count: " + resultList.size ());
  //for (int i=0; i < resultList.size (); i++) {
  //  System.out.println (((String []) resultList.get (i))[0]);
  //  }
  assertTrue (resultList.size () == 1);
  result0 = (String []) resultList.get (0);
  experimentKey = result0 [0];
  condition = result0 [1];
  assertTrue (experimentKey.equals ("genetic:knockout:phr1 and phr2"));
  assertTrue (condition == null);


} // testDoubleKnockoutBug
//----------------------------------------------------------------------
public void testSameVariableFromMultipleFilesTest () throws Exception
{

 /***********
     when building a tree widget from xml input files:  if two (or more) files
       specify condition values for the same variable, only the last-read file"s
       variables are heeded.
     for an example:
        point the TreeDataBrowser at a directory containing only
   
                 batVbat_cmyc1Vcmyc1.xml
                 tfb-ABC-cmyc.xml
   
         sampleData/bat/batVbat_cmyc1Vcmyc1.xml:    <variable name="bait" value="bat" />
         sampleData/bat/batVbat_cmyc1Vcmyc1.xml:    <variable name="bait" value="cmyc1" />
         sampleData/bat/tfb-ABC-cmyc.xml:    <variable name="bait" value="tfbA" />
         sampleData/bat/tfb-ABC-cmyc.xml:    <variable name="bait" value="tfbB" />
         sampleData/bat/tfb-ABC-cmyc.xml:    <variable name="bait" value="tfbC" />

     make sure that the navigator"s tree has condition leaves for all 5 bait:

  Protein-DNA
    general TF
      cmyc
        bait
          bat
          cmyc1
          tfbA
          tfbB
          tfbC


  ***********/

  System.out.println ("sameVariableFromMultipleFilesTest");
  String uri = "file://../sampleData/baitTest";
  MetaDataNavigator nav = new MetaDataNavigator (uri);

  String [] xmlFileNames = nav.getExperimentNames ();
  String [] expected = new String [] {"batVbat_cmyc1Vcmyc1.xml", "tfb-ABC-cmyc.xml"};
  Arrays.sort (xmlFileNames);
  Arrays.sort (expected);
  assertTrue (Arrays.equals (xmlFileNames, expected));


  HashMap tree = nav.getTree ();
  String key;
  // nav.dumpTree (tree, "");
  
  key = "Protein-DNA";
  assertTrue (tree.containsKey (key));
  tree = (HashMap) tree.get (key);

  key = "general TF";  
  assertTrue (tree.containsKey (key));
  tree = (HashMap) tree.get (key);

  key = "cmyc";  
  assertTrue (tree.containsKey (key));
  tree = (HashMap) tree.get (key);

  key = "bait";  
  assertTrue (tree.containsKey (key));
  tree = (HashMap) tree.get (key);

  String [] bait = (String []) tree.keySet().toArray (new String [0]);
  Arrays.sort (bait);
  String [] expectedBait = new String [] {"bat", "cmyc1", "tfbA", "tfbB", "tfbC"};
  Arrays.sort (expectedBait);
  assertTrue (Arrays.equals (bait, expectedBait));

  for (int i=0; i < bait.length; i++) {
    HashMap value = (HashMap) tree.get (bait [i]);
    assertTrue (value.size () == 0);
    }

} // testSameVariableFromMultipleFilesTest
//----------------------------------------------------------------------
public void testMultipleExperimentsWithTheSamePerturbation () throws Exception
/**
 * this test is realted to the previous one "sameVariableFromMultipleFilesTest"
 * that one tested to be sure that no conditions where lost, for a
 * given variable, as they were read from mulitple files.
 * 
 * this one ensures that the navigator can store multiple experiments
 * with the same perturbation.  this comes up with the same ChIP-chip
 * experiments.  a good examples is seen in this snippet of xml:
 * 
 *   batVbat_cmyc1Vcmyc1.xml:  
 *      <predicate category="perturbation" value="Protein-DNA:general TF:cmyc"/>
 *   tfb-ABC-cmyc.xml:
 *      <predicate category="perturbation" value="Protein-DNA:general TF:cmyc"/>
 * 
 * the crucial public method seems to be 
 *    MetaDataNavigator.getExperimentByPerturbationList (perturbationString)
 * it should return a list
 * and all clients must be prepared to handle that
 */
{
  System.out.println ("multipleExperimentsWithTheSamePerturbation");

  String uri = "file://../sampleData/baitTest";
  MetaDataNavigator nav = new MetaDataNavigator (uri);

  String [] xmlFileNames = nav.getExperimentNames ();
  String [] expected = new String [] {"batVbat_cmyc1Vcmyc1.xml", "tfb-ABC-cmyc.xml"};
  Arrays.sort (xmlFileNames);
  Arrays.sort (expected);
  assertTrue (Arrays.equals (xmlFileNames, expected));

  HashMap hash = nav.getPerturbationExperimentHash ();
  String [] keys = (String []) hash.keySet().toArray (new String [0]);
  assertTrue (keys.length == 1);
  assertTrue (keys [0].equals ("Protein-DNA:general TF:cmyc"));

  ArrayList experimentList = (ArrayList) hash.get (keys [0]);
  assertTrue (experimentList.size () == 2);
  MetaData exp1 = (MetaData) experimentList.get (0);
  MetaData exp2 = (MetaData) experimentList.get (1);

  assertTrue (exp1.getTitle().equals ("bat vs bat, cmyc1 vs cmyc1 ChIP-chip"));
  assertTrue (exp2.getTitle().equals ("tfbABC ChIP-chip, cmyc tag"));

} // multipleExperimentsWithTheSamePerturbation
//---------------------------------------------------------------------------
public void testGetRepositoryUriBase () throws Exception
/**
 * this test ensures that we can distinguish the repository filename
 * (should there be one) from it's location (which may include a uri protocol,
 * like 'http://' or 'file://')
 */
{
  System.out.println ("testGetRepositoryUriBase");

  String uri = "file://../sampleData/copperWithRelativeUris.xml";
  MetaDataNavigator nav = new MetaDataNavigator (uri);
  String base = nav.getUriBase ();
  assertTrue (base.equals ("file://../sampleData"));

  uri = "file://../sampleData/reposTiny";
  nav = new MetaDataNavigator (uri);
  base = nav.getUriBase ();
  assertTrue (base.equals ("file://../sampleData/reposTiny"));

} // testGetRepositoryUriBase
//---------------------------------------------------------------------------
public void testCorrectionOfRelativeUris () throws Exception
/**
 * this test ensures that, when datasets are specified in an experiment xml
 * file, and their uri's are relative (no protocol, no absolute file path)
 * that the MetaDataNavigator properly absolutizes those uris, using the
 * necessarily absolute uri of the MetaData (experiment xml) file
 */
{
  System.out.println ("testCorrectionOfRelativeUris");

  String uri = "file://../sampleData/copperWithRelativeUris.xml";
  MetaDataNavigator nav = new MetaDataNavigator (uri);

  String [] xmlFileNames = nav.getExperimentNames ();
  assertTrue (xmlFileNames.length == 1);

  String [] experimentNames = nav.getExperimentNames ();
  assertTrue (experimentNames.length == 1);
  MetaData experiment = nav.getExperimentByName (experimentNames [0]);
  DataSetDescription [] dsds = experiment.getDataSetDescriptions ();
  assertTrue (dsds.length == 2);
  assertTrue (dsds[0].getUri().equals ("file://../sampleData/copper.ratio"));
  assertTrue (dsds[1].getUri().equals ("file://../sampleData/copper.lambda"));


} // testtCorrectionOfRelativeUris
//------------------------------------------------------------------------------
public static void main (String [] args) 
{
  junit.textui.TestRunner.run (new TestSuite (MetaDataNavigatorTest.class));
}
//------------------------------------------------------------------------------
} // MetaDataTest

