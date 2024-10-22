// MetaDataTest.java
//------------------------------------------------------------------------------
// $Revision: 956 $
// $Date: 2005/04/13 02:03:09 $
// $Author: cbare $
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
public class MetaDataTest extends TestCase {

//----------------------------------------------------------------------
private HashMap createExperimentPredicates ()
{
  HashMap predicates = new HashMap ();
  predicates.put ("species", "Halobacterium NRC-1");
  predicates.put ("strain",  "wild type");
  predicates.put ("manipulation", "environmental");

  return predicates;

} // createExperimentPredicates
//----------------------------------------------------------------------
private MetaData createMetaDataTestObjectWithAbsoluteDataSetUris ()
/**
 *    construct an MetaData with a title and a date
 *    add predicates (things broadly true of the metaData)
 *    specify the dataSetDescriptions (each with a uri, a status, and a description)
 *    describe the experimental conditions (which correspond to corresponding columns 
 *    in all data sets)
**/
{
  String title = "gamma";
  String date = "2004-02-01";
  MetaData e = new MetaData (title, date);

  e.setPredicates (createExperimentPredicates ());
  e.setUri ("/users/pshannon/data/halo/microarrayXml/unitTests/sample.xml");

  ArrayList dsDescriptions = new ArrayList ();
  String uri = "/users/pshannon/data/halo/microarrayXml/unitTests/gamma.ratio";
  String status = "primary";
  String description = "log10 ratios";
  DataSetDescription dsd0 = new DataSetDescription (uri, status, description);
  dsDescriptions.add (dsd0);

  uri = "/users/pshannon/data/halo/microarrayXml/unitTests/gamma.lambda";
  status = "derived";
  description = "lambdas";
  DataSetDescription dsd1 = new DataSetDescription (uri, status, description);
  dsDescriptions.add (dsd1);

  e.setDataSetDescriptions (dsDescriptions);
  Condition c0 = new Condition ("C0");
  c0.addVariable (new Variable ("gamma irradiation", "false"));
  c0.addVariable (new Variable ("time", "0", "minutes"));
  e.addCondition (c0);

  Condition c10 = new Condition ("C10");
  c10.addVariable (new Variable ("gamma irradiation", "false"));
  c10.addVariable (new Variable ("time", "10", "minutes"));
  e.addCondition (c10);

  Condition c20 = new Condition ("C20");
  c20.addVariable (new Variable ("gamma irradiation", "false"));
  c20.addVariable (new Variable ("time", "20", "minutes"));
  e.addCondition (c20);

  Condition g0 = new Condition ("G0");
  g0.addVariable (new Variable ("gamma irradiation", "true"));
  g0.addVariable (new Variable ("time", "0", "minutes"));
  e.addCondition (g0);

  Condition g10 = new Condition ("G10");
  g10.addVariable (new Variable ("gamma irradiation", "true"));
  g10.addVariable (new Variable ("time", "10", "minutes"));
  e.addCondition (g10);

  Condition g20 = new Condition ("G20");
  g20.addVariable (new Variable ("gamma irradiation", "true"));
  g20.addVariable (new Variable ("time", "20", "minutes"));
  e.addCondition (g20);

  return e; 

} // createMetaDataTestObjectWithAbsoluteDataSetUris
//----------------------------------------------------------------------
private MetaData createMetaDataTestObjectWithRelativeDataSetUris ()
{
  MetaData md = createMetaDataTestObjectWithAbsoluteDataSetUris ();
  DataSetDescription [] dsds = md.getDataSetDescriptions ();
  for (int i=0; i < dsds.length; i++) {
    String relativeFilename = (new File (dsds [i].getUri())).getName ();
    dsds [i].setUri (relativeFilename);
    } // for i

  return md;

} // createMetaDataTestObjectWithRelativeDataSetUris 
//----------------------------------------------------------------------
public void testHighLevelContents () throws Exception 
{
  System.out.println ("testHighLevelContents");

  MetaData e = createMetaDataTestObjectWithAbsoluteDataSetUris ();

  assertTrue (e.getTitle().equals ("gamma"));
  assertTrue (e.getDate().equals ("2004-02-01"));
  assertTrue (e.getPredicates ().size() == 3);
  assertTrue (e.getDataSetDescriptions().length == 2);
  assertTrue (e.getConditions ().length == 6);

} // testHighLevelContents
//----------------------------------------------------------------------
public void testPredicates () throws Exception
{
  System.out.println ("testPredicates");
  MetaData e = createMetaDataTestObjectWithAbsoluteDataSetUris ();
  
  HashMap predicates = e.getPredicates ();
  String [] predicateNames = e.getPredicateNamesSorted ();
  String [] expectedPredicateNames = new String [] {"manipulation", "species", "strain"};
  assertTrue (Arrays.equals (predicateNames, expectedPredicateNames));

  assertTrue (e.getPredicate ("species").equals ("Halobacterium NRC-1"));
  assertTrue (e.getPredicate ("strain").equals ("wild type"));
  assertTrue (e.getPredicate ("manipulation").equals ("environmental"));

  assertTrue (e.getSpecies ().equals ("Halobacterium NRC-1"));

} // testPredicates
//----------------------------------------------------------------------
public void testDataSetDescriptions () throws Exception
{
  System.out.println ("testDataSetDescriptions");
  MetaData e = createMetaDataTestObjectWithAbsoluteDataSetUris ();

  DataSetDescription [] dsds = e.getDataSetDescriptions ();
  assertTrue (dsds.length == 2);
  assertTrue (dsds[0].getUri().equals ("/users/pshannon/data/halo/microarrayXml/unitTests/gamma.ratio"));
  assertTrue (dsds[0].getStatus().equals ("primary"));
  assertTrue (dsds[0].getType().equals ("log10 ratios"));
  assertTrue (dsds[1].getUri().equals ("/users/pshannon/data/halo/microarrayXml/unitTests/gamma.lambda"));
  assertTrue (dsds[1].getStatus().equals ("derived"));
  assertTrue (dsds[1].getType().equals ("lambdas"));

} // testDataSetDescriptions 
//----------------------------------------------------------------------
public void testConditions () throws Exception
{
  System.out.println ("testConditions");
  MetaData e = createMetaDataTestObjectWithAbsoluteDataSetUris ();

  Condition [] conds = e.getConditions ();
  assertTrue (conds.length == 6);

  ArrayList aliasList = new ArrayList ();
  for (int i=0; i < conds.length; i++)
    aliasList.add (conds [i].getAlias ());

  String [] actualAliases = (String []) aliasList.toArray (new String [0]);
  Arrays.sort (actualAliases);
  assertTrue (Arrays.equals (new String [] {"C0", "C10", "C20", "G0", "G10", "G20"}, actualAliases));

  String [] allAliases = e.getConditionAliases ();
  Arrays.sort (allAliases);
  assertTrue (Arrays.equals (actualAliases, allAliases));

} // testDataSetDescriptions 
//----------------------------------------------------------------------
public void testVariableSummary () throws Exception
{
  System.out.println ("testVariableSummary");

  MetaData e = createMetaDataTestObjectWithAbsoluteDataSetUris ();

  HashMap summary = e.getVariableSummary ();
  String [] variableNames = (String []) summary.keySet().toArray (new String [0]);
  Arrays.sort (variableNames);
  String [] expected = new String [] {"gamma irradiation", "time"};
  assertTrue (Arrays.equals (variableNames, expected));

}
//----------------------------------------------------------------------
public void testVariableAccess () throws Exception
{
  System.out.println ("testVariableAcess");

  MetaData e = createMetaDataTestObjectWithAbsoluteDataSetUris ();

    //----------------------------------------------------------------
    // this experiment has two variables:  gamma irradiation and time
    //----------------------------------------------------------------

  String [] actual = e.getVariableNames ();
  String [] expected = new String [] {"gamma irradiation", "time"};
  Arrays.sort (actual);
  assertTrue (Arrays.equals (expected, actual));
    
    //----------------------------------------------------------------
    // gamma can be true or false
    //----------------------------------------------------------------

  String [] gammaValues = e.getSortedVariableValues ("gamma irradiation");
  expected = new String [] {"false", "true"};
  assertTrue (Arrays.equals (gammaValues, expected));

    //----------------------------------------------------------------
    // measurements were made at 0, 10, and 20 minutes
    //----------------------------------------------------------------

  String [] timeValues = e.getSortedVariableValues ("time");
  expected = new String [] {"0", "10", "20"};
  assertTrue (Arrays.equals (timeValues, expected));
  
    //----------------------------------------------------------------
    // time is measured in minutes; gamma has no units
    //----------------------------------------------------------------

  assertTrue (e.getUnits ("time", "0").equals ("minutes"));
  assertTrue (e.getUnits ("time", "10").equals ("minutes"));
  assertTrue (e.getUnits ("time", "20").equals ("minutes"));

  assertTrue (e.getUnits ("gamma irradiation", "false") == null);
  assertTrue (e.getUnits ("gamma irradiation", "true") == null);

} // testVariables
//---------------------------------------------------------------------------
public void testColumnSelected () throws Exception
{
  System.out.println ("testColumnSelection");
  MetaData e = createMetaDataTestObjectWithAbsoluteDataSetUris ();
  e.addSelectionCriterion ("gamma irradiation", "true");

  Condition c0 = new Condition ("C0");
  c0.addVariable (new Variable ("gamma irradiation", "false"));
  c0.addVariable (new Variable ("time", "0", "minutes"));

  Condition c10 = new Condition ("C10");
  c10.addVariable (new Variable ("gamma irradiation", "false"));
  c10.addVariable (new Variable ("time", "10", "minutes"));

  Condition c20 = new Condition ("C20");
  c20.addVariable (new Variable ("gamma irradiation", "false"));
  c20.addVariable (new Variable ("time", "20", "minutes"));

  Condition g0 = new Condition ("G0");
  g0.addVariable (new Variable ("gamma irradiation", "true"));
  g0.addVariable (new Variable ("time", "0", "minutes"));

  Condition g10 = new Condition ("G10");
  g10.addVariable (new Variable ("gamma irradiation", "true"));
  g10.addVariable (new Variable ("time", "10", "minutes"));

  Condition g20 = new Condition ("G20");
  g20.addVariable (new Variable ("gamma irradiation", "true"));
  g20.addVariable (new Variable ("time", "20", "minutes"));

  assertTrue (e.columnSelected (g0, e.getSelectionCriteria ()));
  assertTrue (e.columnSelected (g10, e.getSelectionCriteria ()));
  assertTrue (e.columnSelected (g20, e.getSelectionCriteria ()));

  assertTrue (!e.columnSelected (c0, e.getSelectionCriteria ()));
  assertTrue (!e.columnSelected (c10, e.getSelectionCriteria ()));
  assertTrue (!e.columnSelected (c20, e.getSelectionCriteria ()));

  e.addSelectionCriterion ("time", "10");
  assertTrue (e.columnSelected (g10, e.getSelectionCriteria ()));
  assertTrue (!e.columnSelected (g0, e.getSelectionCriteria ()));
  assertTrue (!e.columnSelected (g20, e.getSelectionCriteria ()));
  assertTrue (!e.columnSelected (c0, e.getSelectionCriteria ()));
  assertTrue (!e.columnSelected (c10, e.getSelectionCriteria ()));
  assertTrue (!e.columnSelected (c20, e.getSelectionCriteria ()));
 
  e.addSelectionCriterion ("time", "0");
  assertTrue (e.columnSelected (g0, e.getSelectionCriteria ()));
  assertTrue (e.columnSelected (g10, e.getSelectionCriteria ()));
  assertTrue (!e.columnSelected (g20, e.getSelectionCriteria ()));
  assertTrue (!e.columnSelected (c0, e.getSelectionCriteria ()));
  assertTrue (!e.columnSelected (c10, e.getSelectionCriteria ()));
  assertTrue (!e.columnSelected (c20, e.getSelectionCriteria ()));

  e.clearSelectionCriteria ();
  e.addSelectionCriterion ("time", "20");
  assertTrue (!e.columnSelected (g0, e.getSelectionCriteria ()));
  assertTrue (!e.columnSelected (g10, e.getSelectionCriteria ()));
  assertTrue (e.columnSelected (g20, e.getSelectionCriteria ()));
  assertTrue (!e.columnSelected (c0, e.getSelectionCriteria ()));
  assertTrue (!e.columnSelected (c10, e.getSelectionCriteria ()));
  assertTrue (e.columnSelected (c20, e.getSelectionCriteria ()));


} // testColumnSelected
//----------------------------------------------------------------------
public void testSelection () throws Exception
/**
 *  public String [] getNamesOfSelectedVariables ()
 *  public boolean columnSelected (Condition condition, HashMap selectionCriteria)
 *  public Condition [] getSelectedConditions ()
 *  public String [] getSelectedConditionsAsAliases ()
 *  public String [] oldGetSelectionAsAliases ()
 *  public void clearSelectionCriteria ()
 *  public HashMap getSelectionCriteria ()
 *  public void selectConditionByName (String conditionName)
 *  public void selectAllConditions ()
 *  public void addSelectionCriterion (String variableName, String variableValue)
 *
 * there are two variables in this experiment: time (0,10,20 minutes) and
 * gamma irradiation (true or false)
 * thus there are 2 x 3 = 6 possible conditions
 *
 */
{
  System.out.println ("testSelection");
  MetaData e = createMetaDataTestObjectWithAbsoluteDataSetUris ();

  assertTrue (e.getSelectedConditions().length == 0);
  e.addSelectionCriterion ("gamma irradiation", "true");
  assertTrue (Arrays.equals (e.getNamesOfSelectedVariables (), new String [] {"gamma irradiation"}));
  String [] sa = e.getSelectedConditionsAsAliases ();
  assertTrue (Arrays.equals (sa, new String [] {"G0", "G10", "G20"}));

  e.clearSelectionCriteria ();
  e.addSelectionCriterion ("gamma irradiation", "false");
  assertTrue (Arrays.equals (e.getNamesOfSelectedVariables (), new String [] {"gamma irradiation"}));
  sa = e.getSelectedConditionsAsAliases ();
  assertTrue (Arrays.equals (sa, new String [] {"C0", "C10", "C20"}));

  e.clearSelectionCriteria ();
  e.addSelectionCriterion ("time", "10");
  assertTrue (Arrays.equals (e.getNamesOfSelectedVariables (), new String [] {"time"}));
  sa = e.getSelectedConditionsAsAliases ();
  assertTrue (Arrays.equals (sa, new String [] {"C10", "G10"}));

  e.addSelectionCriterion ("time", "20");
  assertTrue (Arrays.equals (e.getNamesOfSelectedVariables (), new String [] {"time"}));
  sa = e.getSelectedConditionsAsAliases ();
  assertTrue (Arrays.equals (sa, new String [] {"C10", "C20", "G10", "G20"}));

  e.addSelectionCriterion ("gamma irradiation", "true");
  String [] sv = e.getNamesOfSelectedVariables ();

  assertTrue (Arrays.equals (e.getNamesOfSelectedVariables (), new String [] {"gamma irradiation", "time"}));
  sa = e.getSelectedConditionsAsAliases ();
  assertTrue (Arrays.equals (sa, new String [] {"G10", "G20"}));

  e.clearSelectionCriteria ();
  assertTrue (e.getSelectedConditions().length == 0);
  assertTrue (Arrays.equals (e.getNamesOfSelectedVariables (), new String [] {}));
  sa = e.getSelectedConditionsAsAliases ();
  assertTrue (Arrays.equals (sa, new String [] {}));

  e.selectAllConditions ();
  sa = e.getSelectedConditionsAsAliases ();
  assertTrue (Arrays.equals (sa, new String [] {"C0", "C10", "C20", "G0", "G10", "G20"}));
  
}
//----------------------------------------------------------------------
private void displaySelectionState (String msg, MetaData e)
{

  HashMap selcrit = e.getSelectionCriteria ();
  String [] keys = (String []) selcrit.keySet().toArray(new String [0]);

  System.out.println ("------------ selection criteria: " + msg);

  for (int i=0; i < keys.length; i++) {
    String variable = keys [i];
    System.out.print (variable);
    String [] values = (String [])((ArrayList) selcrit.get (variable)).toArray (new String [0]);
    for (int j=0; j < values.length; j++)
      System.out.print ("  " + values [j]);
    System.out.println ();
    } // for i

  //String [] aliases = e.getSelectionAsAliases();
  //System.out.println ("---------- selection as aliases: " + aliases.length);
  //for (int i=0; i < aliases.length; i++)
  //  System.out.println ("   " + aliases [i]);


} // displaySelectionCriteria
//----------------------------------------------------------------------
public void testGetConditionByAlias () throws Exception
/**
 * from a datamatrix, you can obtain its column names, which are
 * typically abbreviated and condensed forms of longer, more descriptive
 * names.   for example, the alias (and column name)
 *    cu__1000um_vs_NRC-1
 * has this fuller xml statement:
 * 
 *  <condition alias='cu__1000um_vs_NRC-1'>
 *    <variable name='Cu' value='1000'  units='micromolar'/>
 *    <variable name='oxygen' value='High'/>
 *    <variable name='illumination' value='Light'/>
 *  </condition>
 *
 * further, as a Condition variable:
 *
 *    condition.getVariableNames () -> {"Cu", "oxygen", "illumination"}
 *    condition.getVariable ("Cu").getValue () -> "1000"
 *    condition.getVariable ("Cu").getUnits() -> "micromolar"
 */
{
  System.out.println ("testGetConditionByAlias");
  MetaData md = createMetaDataTestObjectWithAbsoluteDataSetUris ();
  Condition [] conds = md.getConditions ();
  assertTrue (conds.length == 6);

  assertTrue (md.hasCondition ("G10"));
  assertTrue (md.hasCondition ("g10"));
  assertFalse (md.hasCondition ("bogus"));

  Condition g10 = md.getCondition ("G10");
  String [] variableNames = g10.getVariableNames ();
  assertTrue (variableNames.length == 2);
  Arrays.sort (variableNames);
  assertTrue (Arrays.equals (variableNames, new String [] {"gamma irradiation", "time"}));
  Variable gamma = g10.getVariable ("gamma irradiation");
  Variable time = g10.getVariable ("time");

} // testGetConditionByAlias
//----------------------------------------------------------------------
/**
 *  make sure the relatve DataSetDescription uri's make sense, before using
 *  them in other tests -- testAbsolutizingOfRelativeDataSetUris in particular
 */ 
public void testCreateMetaDataTestObjectWithRelativeDataSetUris () throws Exception 
{
  System.out.println ("testCreateMetaDataTestObjectWithRelativeDataSetUris");
  MetaData md =  createMetaDataTestObjectWithRelativeDataSetUris ();
  DataSetDescription [] dsds = md.getDataSetDescriptions ();
  assertTrue (dsds.length == 2);
  //assertTrue (dsds [0].getUri().equals ("gamma.ratio"));
  //assertTrue (dsds [1].getUri().equals ("gamma.lambda"));

} // testCreateMetaDataTestObjectWithRelativeDataSetUris 
//----------------------------------------------------------------------
/**
 *
 */ 
public void testAbsolutizingOfRelativeDataSetUris () throws Exception 
{
  System.out.println ("testAbsolutizingOfRelativeDataSetUris");
  MetaData md = createMetaDataTestObjectWithRelativeDataSetUris ();

  DataSetDescription [] dsds = md.getRawDataSetDescriptions ();
  assertTrue (dsds.length == 2);
  assertTrue (dsds [0].getUri().equals ("gamma.ratio"));
  assertTrue (dsds [1].getUri().equals ("gamma.lambda"));

  dsds = md.getDataSetDescriptions ();
  assertTrue (dsds.length == 2);

  String uriBase = md.getUriBase ();
  assertTrue (uriBase.equals ("/users/pshannon/data/halo/microarrayXml/unitTests"));

  String expected1 = uriBase + "gamma.ratio";
  String expected2 = uriBase + "gamma.lambda";
  System.out.println (dsds [0].getUri ());
  assertTrue (dsds [0].getUri().equals (expected1));
  assertTrue (dsds [1].getUri().equals (expected2));

} // testAbsolutizingOfRelativeDataSetUris
//----------------------------------------------------------------------
public static void main (String [] args) 
{
  junit.textui.TestRunner.run (new TestSuite (MetaDataTest.class));
}
//------------------------------------------------------------------------------
} // MetaDataTest
