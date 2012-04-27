// DataSetDescriptionTest.java
//------------------------------------------------------------------------------
// $Revision: 732 $
// $Date: 2005/03/05 01:50:49 $
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
import org.systemsbiology.gaggle.experiment.metadata.*;
//------------------------------------------------------------------------------
public class DataSetDescriptionTest extends TestCase {

//----------------------------------------------------------------------
public void testSimple () throws Exception 
{
  System.out.println ("testSimple");
    //  <dataset status='primary' type='log10 ratios'>
    //    <uri> /users/pshannon/data/halo/microarrayXml/unitTests/gamma.ratio </uri>
    //  </dataset>

  String uri = "/users/pshannon/data/halo/microarrayXml/unitTests/gamma.ratio";
  String status = "primary";
  String type = "log10 ratios";
  DataSetDescription ds = new DataSetDescription (uri, status, type);
  assertTrue (ds.getUri().equals (uri));
  assertTrue (ds.getStatus().equals (status));
  assertTrue (ds.getType().equals (type));

  String expected0 = "dataSetDescription: /users/pshannon/data/halo/microarrayXml/unitTests/gamma.ratio";
  String expected1 = "   (primary, log10 ratios)";
  String expected = expected0 + expected1;

  assertTrue (ds.toString ().equals (expected));

  String newUri = "hocusPocus.ratio";
  ds.setUri (newUri);
  assertTrue (ds.getUri ().equals (newUri));

} // testSimple
//---------------------------------------------------------------------------
public static void main (String [] args) 
{
  junit.textui.TestRunner.run (new TestSuite (DataSetDescriptionTest.class));
}
//------------------------------------------------------------------------------
} // DataSetDescriptionTest
