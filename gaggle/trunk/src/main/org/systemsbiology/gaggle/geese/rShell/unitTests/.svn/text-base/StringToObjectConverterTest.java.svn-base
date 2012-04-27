// StringToOjbectConverterTest.java
//------------------------------------------------------------------------------
// $Revision: 738 $
// $Date: 2006-03-16 13:21:44 -0800 (Thu, 16 Mar 2006) $
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
package org.systemsbiology.gaggle.geese.rShell.unitTests;
//--------------------------------------------------------------------------------------
import junit.framework.*;
import java.io.*;
import java.util.*;

import org.systemsbiology.gaggle.geese.rShell.StringToObjectConverter;
//------------------------------------------------------------------------------
public class StringToObjectConverterTest extends TestCase {

//------------------------------------------------------------------------------
public StringToObjectConverterTest (String name) 
{
  super (name);
}
//------------------------------------------------------------------------------
public void testString () throws Exception
{
  System.out.println ("testString");
  Object obj = StringToObjectConverter.convert ("this is a string");
  assertTrue (obj.getClass().getName().equals ("java.lang.String"));

}  // testString
//-------------------------------------------------------------------------
public void testInteger () throws Exception
{
  System.out.println ("testInteger");
  Object obj = StringToObjectConverter.convert ("99");
  assertTrue (obj.getClass().getName().equals ("java.lang.Integer"));

}  // testInteger
//-------------------------------------------------------------------------
public void testDouble () throws Exception
{
  System.out.println ("testDouble");
  Object obj = StringToObjectConverter.convert ("99.99");
  assertTrue (obj.getClass().getName().equals ("java.lang.Double"));

}  // testDouble
//-------------------------------------------------------------------------
public void testURL () throws Exception
{
  System.out.println ("testURL");
  Object obj = StringToObjectConverter.convert ("http://gaggle.systemsbiology.org");
  assertTrue (obj.getClass().getName().equals ("java.net.URL"));

}  // testURL
//-------------------------------------------------------------------------
public static void main (String [] args) 
{
  junit.textui.TestRunner.run (new TestSuite (StringToObjectConverterTest.class));

}// main
//------------------------------------------------------------------------------
} // StringToObjectConverterTest
