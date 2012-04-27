// InteractionTest.java
//------------------------------------------------------------------------------
// $Revision: 1415 $
// $Date: 2007-04-16 21:02:42 -0400 (Mon, 16 Apr 2007) $
// $Author: dtenenba $
//--------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
package org.systemsbiology.gaggle.network.unitTests;
//--------------------------------------------------------------------------------------
import junit.framework.*;

//------------------------------------------------------------------------------
public class InteractionTest extends TestCase {

//------------------------------------------------------------------------------
public InteractionTest (String name) 
{
  super (name);
}
//------------------------------------------------------------------------------
public void testSimple () throws Exception
{
  System.out.println ("testSimple");
  org.systemsbiology.gaggle.core.datatypes.Interaction i0 = new org.systemsbiology.gaggle.core.datatypes.Interaction("a", "b" , "associated with", false);
  assertTrue (i0.toString().equals ("a (associated with) b"));

}  // testSimple
//-------------------------------------------------------------------------
public static void main (String [] args) 
{
  junit.textui.TestRunner.run (new TestSuite (InteractionTest.class));

}// main
//------------------------------------------------------------------------------
} // InteractionTest
