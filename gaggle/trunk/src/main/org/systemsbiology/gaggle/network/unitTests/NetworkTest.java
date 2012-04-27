// NetworkTest.java
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

import java.util.*;

//------------------------------------------------------------------------------
public class NetworkTest extends TestCase {

//------------------------------------------------------------------------------
public NetworkTest (String name) 
{
  super (name);
}
//------------------------------------------------------------------------------
public void testSimple () throws Exception
{
  System.out.println ("testSimple");
  org.systemsbiology.gaggle.core.datatypes.Network network = new org.systemsbiology.gaggle.core.datatypes.Network();
  org.systemsbiology.gaggle.core.datatypes.Interaction i0 = new org.systemsbiology.gaggle.core.datatypes.Interaction("a", "b", "protein-protein");
  org.systemsbiology.gaggle.core.datatypes.Interaction i1 = new org.systemsbiology.gaggle.core.datatypes.Interaction("b", "c", "protein-protein");
  org.systemsbiology.gaggle.core.datatypes.Interaction i2 = new org.systemsbiology.gaggle.core.datatypes.Interaction("d", "a", "protein-DNA", true);

  network.add (i0);
  network.add (i1);
  network.add (i2);

  assertTrue (network.edgeCount () == 3);
  assertTrue (network.nodeCount () == 4);

}  // testSimple
//-------------------------------------------------------------------------
public void testNodeAttributes () throws Exception
{
  System.out.println ("testNodeAttributes");
  org.systemsbiology.gaggle.core.datatypes.Network network = new org.systemsbiology.gaggle.core.datatypes.Network();
  org.systemsbiology.gaggle.core.datatypes.Interaction i0 = new org.systemsbiology.gaggle.core.datatypes.Interaction("a", "b", "protein-protein");
  org.systemsbiology.gaggle.core.datatypes.Interaction i1 = new org.systemsbiology.gaggle.core.datatypes.Interaction("b", "c", "protein-protein");
  org.systemsbiology.gaggle.core.datatypes.Interaction i2 = new org.systemsbiology.gaggle.core.datatypes.Interaction("d", "a", "protein-DNA", true);

  network.add (i0);
  network.add (i1);
  network.add (i2);

  assertTrue (network.edgeCount () == 3);
  assertTrue (network.nodeCount () == 4);

  network.addNodeAttribute ("a", "moleculeType", "protein");
  network.addNodeAttribute ("d", "moleculeType", "DNA");
  network.addNodeAttribute ("a", "organism", "Homo sapiens");

  String [] expected = new String [] {"moleculeType", "organism"};
  String [] actual = network.getNodeAttributeNames ();

  Arrays.sort (expected);
  Arrays.sort (actual);
  assertTrue (Arrays.equals (expected, actual));

  HashMap moleculeTypes = network.getNodeAttributes ("moleculeType");
  assertTrue (moleculeTypes.size () == 2);
  String [] expectedKeys = new String [] {"a", "d"};
  String [] actualNodeNames = (String []) moleculeTypes.keySet().toArray (new String [0]);

  Arrays.sort (expectedKeys);
  Arrays.sort (actualNodeNames);
  assertTrue (Arrays.equals (expectedKeys, actualNodeNames));

  Class valueClass = moleculeTypes.get ("a").getClass ();
  assertTrue (valueClass == (new String ("").getClass ()));

}  // testNodeAttributes
//-------------------------------------------------------------------------
public void testEdgeAttributes () throws Exception
{
  System.out.println ("testEdgeAttributes");
  org.systemsbiology.gaggle.core.datatypes.Network network = new org.systemsbiology.gaggle.core.datatypes.Network();
  org.systemsbiology.gaggle.core.datatypes.Interaction i0 = new org.systemsbiology.gaggle.core.datatypes.Interaction("a", "b", "protein-protein");
  org.systemsbiology.gaggle.core.datatypes.Interaction i1 = new org.systemsbiology.gaggle.core.datatypes.Interaction("b", "c", "protein-protein");
  org.systemsbiology.gaggle.core.datatypes.Interaction i2 = new org.systemsbiology.gaggle.core.datatypes.Interaction("d", "a", "protein-DNA", true);

  network.add (i0);
  network.add (i1);
  network.add (i2);

  assertTrue (network.edgeCount () == 3);
  assertTrue (network.nodeCount () == 4);

  network.addEdgeAttribute ("a (protein-protein) b", "confidence", new Double (0.88));
  network.addEdgeAttribute ("b (protein-protein) c", "confidence", new Double (0.18));
  network.addEdgeAttribute ("a (protein-protein) b", "source",  "DIP");

  String [] expected = new String [] {"confidence", "source"};
  String [] actual = network.getEdgeAttributeNames ();

  Arrays.sort (expected);
  Arrays.sort (actual);
  assertTrue (Arrays.equals (expected, actual));

  HashMap confidences = network.getEdgeAttributes ("confidence");
  assertTrue (confidences.size () == 2);
  String [] expectedKeys = new String [] {"a (protein-protein) b", "b (protein-protein) c"};
  String [] actualEdgeNames = (String []) confidences.keySet().toArray (new String [0]);

  Arrays.sort (expectedKeys);
  Arrays.sort (actualEdgeNames);
  assertTrue (Arrays.equals (expectedKeys, actualEdgeNames));

  Class valueClass = confidences.get ("a (protein-protein) b").getClass ();
  assertTrue (valueClass == (new Double (0.0).getClass ()));

}  // testEdgeAttributes
//-------------------------------------------------------------------------
public static void main (String [] args) 
{
  junit.textui.TestRunner.run (new TestSuite (NetworkTest.class));

}// main
//------------------------------------------------------------------------------
} // NetworkTest
