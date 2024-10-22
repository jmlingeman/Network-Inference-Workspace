// VariableTest.java
//------------------------------------------------------------------------------
// $Revision: 732 $
// $Date: 2004/10/25 23:22:58 $
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
public class VariableTest extends TestCase {

//----------------------------------------------------------------------
public void testSimple () throws Exception
{
    // sample xml, in which the C0 (control, time zero) condition's 
    // experimental variables are laid out
    //
    //  <condition alias='C0'>
    //    <variable name='gamma irradiation' value='false'/>
    //    <variable name='time' value='0' units='minutes'/>
    //  </condition>

  System.out.println ("testSimple");
  String name = "gamma irradiation";
  String value = "false";
  Variable v0 = new Variable (name, value);
  assertTrue (v0.getName ().equals (name));
  assertTrue (v0.getValue ().equals (value));
  assertTrue (v0.getUnits () == (null));

  name = "time";
  value = "0";
  String units = "minutes";
  Variable v1 = new Variable (name, value, units);
  assertTrue (v1.getName ().equals (name));
  assertTrue (v1.getValue ().equals (value));
  assertTrue (v1.getUnits ().equals (units));

} 
//----------------------------------------------------------------------
public void testEquality ()
{

  System.out.println ("testEquality");
  String name = "gamma irradiation";
  String value = "false";
  Variable v0 = new Variable (name, value);

  name = "time";
  value = "0";
  String units = "minutes";
  Variable v1 = new Variable (name, value, units);

  assertTrue (v0.equals (v0));
  assertTrue (v1.equals (v1));
  assertFalse(v0.equals (v1));
  assertFalse (v1.equals (v0));

}
//---------------------------------------------------------------------------
public static void main (String [] args) 
{
  junit.textui.TestRunner.run (new TestSuite (VariableTest.class));
}
//------------------------------------------------------------------------------
} // VariableTest
