// ConditionTest.java
//------------------------------------------------------------------------------
// $Revision: 732 $
// $Date: 2004/10/25 23:22:55 $
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
import java.util.Properties;
import org.systemsbiology.gaggle.experiment.metadata.*;
//------------------------------------------------------------------------------
public class ConditionTest extends TestCase {

//----------------------------------------------------------------------
public void testSimple () throws Exception 
{
  System.out.println ("testSimple");
    //  <condition alias="C0">
    //    <variable name="gamma irradiation" value="false"/>
    //    <variable name="time" value="0" units="minutes"/>
    //  </condition>

    String alias = "C0";
    Condition c = new Condition (alias);
    assertTrue (c.getAlias () == alias);
    Variable var1 = new Variable ("gamma irradiation", "false");
    Variable var2 = new Variable ("time", "0", "minutes");
    c.addVariable (var1);
    c.addVariable (var2);

    String [] variableNames = c.getVariableNames ();
    assertTrue (variableNames.length == 2);
    assertTrue (variableNames [0].equals ("gamma irradiation"));
    assertTrue (variableNames [1].equals ("time"));

    Variable gamma = c.getVariable ("gamma irradiation");
    assertTrue (gamma.getValue().equals ("false"));
    assertTrue (gamma.getUnits () == null);

    Variable time = c.getVariable ("time");
    assertTrue (time.getValue ().equals ("0"));
    assertTrue (time.getUnits ().equals ("minutes"));
}   
//---------------------------------------------------------------------------
public static void main (String [] args) 
{
  junit.textui.TestRunner.run (new TestSuite (ConditionTest.class));
}
//------------------------------------------------------------------------------
} // ConditionTest

