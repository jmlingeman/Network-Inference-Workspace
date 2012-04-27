// NameHelperTest.java
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

import org.systemsbiology.gaggle.util.NameHelper;
import org.systemsbiology.gaggle.util.NameHelperFactory;
//------------------------------------------------------------------------------
public class NameHelperTest extends TestCase {

//------------------------------------------------------------------------------
public NameHelperTest (String name) 
{
  super (name);
}

//------------------------------------------------------------------------------
public void testHalo () throws Exception
{
  System.out.println ("testHalo");
  String [] nameVariants = {"halo", "Halobacterium sp.", "Halobacterium"};

  for (int i=0; i < nameVariants.length; i++) {  
    NameHelper nh = NameHelperFactory.getNameHelper (nameVariants [i]);
    assertTrue (nh.getName ("common", "VNG1404G").equals ("trh1"));
    assertTrue (nh.getName ("canonical", "trh1").equals ("VNG1404G"));
    } // for i
 
}  // testHalo
//-------------------------------------------------------------------------
public void testHuman () throws Exception
{
  System.out.println ("testHuman");
  String [] nameVariants = {"human", "Homo sapiens", "homo"};

  for (int i=0; i < nameVariants.length; i++) {  
    NameHelper nh = NameHelperFactory.getNameHelper (nameVariants [i]);
    assertTrue (nh.getName ("common", "NP_057646").equalsIgnoreCase ("TLR7"));
    assertTrue (nh.getName ("canonical", "TLR7").equals("NP_057646"));
    } // for i
 
}  // testHuman
//-------------------------------------------------------------------------
/**
 * if name helpers are properly cached, then the second request for one
 * should be substantially faster than the first.
 */
public void testCachedHuman () throws Exception
{
  System.out.println ("testCachedHuman");

  long startTime = System.nanoTime ();
  NameHelper nh = NameHelperFactory.getNameHelper ("Homo sapiens");
  long endTime = System.nanoTime ();
  assertTrue (nh.getName ("common", "NP_057646").equalsIgnoreCase ("TLR7"));
  long elapsedTime = endTime - startTime;
  System.out.println ("get human nh, first time: " + elapsedTime);
  System.out.println ("start: "+ startTime);
  System.out.println ("end: "+ endTime);
  System.out.println ("elapsed: "+ elapsedTime);

  startTime = System.nanoTime ();
  nh = NameHelperFactory.getNameHelper ("Homo sapiens");
  endTime = System.nanoTime ();
  assertTrue (nh.getName ("common", "NP_057646").equalsIgnoreCase ("TLR7"));
  elapsedTime = endTime - startTime;
  System.out.println ("get human nh, second time: " + elapsedTime);
  System.out.println ("start: "+ startTime);
  System.out.println ("end: "+ endTime);
  System.out.println ("elapsed: "+ elapsedTime);
 
}  // testCachedHuman
//-------------------------------------------------------------------------
public void testHpy () throws Exception
{
  System.out.println ("testHpy");
  String [] nameVariants = {"Helicobacter pylori", "H. pylori", "hpy"};

  for (int i=0; i < nameVariants.length; i++) {  
    NameHelper nh = NameHelperFactory.getNameHelper (nameVariants [i]);
    assertTrue (nh.getName ("common", "HP0121").equalsIgnoreCase ("ppsA"));
    assertTrue (nh.getName ("canonical", "ppsA").equals("HP0121"));
    } // for i
 
}  // testHpy
//-------------------------------------------------------------------------
public void testFly () throws Exception
{
  System.out.println ("testFly");
  String [] nameVariants = {"Drosophila melanogaster", "drosophila", "fly"};

  for (int i=0; i < nameVariants.length; i++) {  
    NameHelper nh = NameHelperFactory.getNameHelper (nameVariants [i]);
    assertTrue (nh.getName ("common", "CG8896").equalsIgnoreCase ("18w"));
    assertTrue (nh.getName ("canonical", "18w").equals("CG8896"));
    } // for i
 
}  // testHpy
//-------------------------------------------------------------------------
public void testUnrecognizedSpecies () throws Exception
{
  System.out.println ("testUnrecognizedSpecies");
  NameHelper nh = NameHelperFactory.getNameHelper ("sillyGoose");
  assertTrue (nh.getName ("common", "SG99").equalsIgnoreCase ("SG99"));
  assertTrue (nh.getName ("canonical", "SG76").equals("SG76"));
 
}  // testUnrecognizedSpecies
//-------------------------------------------------------------------------
public static void main (String [] args) 
{
  junit.textui.TestRunner.run (new TestSuite (NameHelperTest.class));
  System.exit (0);

}// main
//------------------------------------------------------------------------------
} // NameHelperTest
