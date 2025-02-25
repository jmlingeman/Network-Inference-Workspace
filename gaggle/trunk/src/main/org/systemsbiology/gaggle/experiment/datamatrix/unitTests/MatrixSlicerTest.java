// MatrixSlicerTest.java
//------------------------------------------------------------------------------
// $Revision: 2360 $
// $Date: 2007-09-04 18:02:00 -0400 (Tue, 04 Sep 2007) $
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

package org.systemsbiology.gaggle.experiment.datamatrix.unitTests;
//--------------------------------------------------------------------------------------
import junit.framework.*;

import java.util.*;

import org.systemsbiology.gaggle.experiment.datamatrix.*;
import org.systemsbiology.gaggle.experiment.metadata.*;
import org.systemsbiology.gaggle.experiment.readers.*;

//------------------------------------------------------------------------------
public class MatrixSlicerTest extends TestCase {

//------------------------------------------------------------------------------
public MatrixSlicerTest (String name) 
{
  super (name);
}

//------------------------------------------------------------------------------
public void setUp () throws Exception
{
}
//------------------------------------------------------------------------------
public void tearDown () throws Exception
{
}
//------------------------------------------------------------------------------
/**
 *  create a one-column slice of two matrices in one experiment
 */
public void testOneColumnSlice () throws Exception
{
  System.out.println ("testOneColumnSlice");

  String uri = "file://sampleData/sample.xml";
  MetaDataXmlParser parser = new MetaDataXmlParser (uri);
  MetaData experiment = parser.getMetaData ();

  MatrixSlicer slicer = new MatrixSlicer (experiment);
  experiment.addSelectionCriterion ("time", "0");
  experiment.addSelectionCriterion ("gamma irradiation", "false");

  //DataMatrix [] slices = slicer.slice ();
  HashMap slices = slicer.slice ();
  assertTrue (slices.size () == 2);

  String [] matrixTypes = (String []) slices.keySet().toArray (new String [0]);
  String [] expectedTypes = new String [] {"lambdas", "log10 ratios"};
  assertTrue (Arrays.equals (matrixTypes, expectedTypes));

  org.systemsbiology.gaggle.core.datatypes.DataMatrix ratios = (org.systemsbiology.gaggle.core.datatypes.DataMatrix) slices.get ("log10 ratios");
  org.systemsbiology.gaggle.core.datatypes.DataMatrix lambdas = (org.systemsbiology.gaggle.core.datatypes.DataMatrix) slices.get ("lambdas");

  assertTrue (lambdas.getColumnCount () == 1);
  assertTrue (ratios.getColumnCount () == 1);

  assertTrue (Arrays.equals (lambdas.getColumnTitles (), new String [] {"C000"}));
  assertTrue (Arrays.equals (ratios.getColumnTitles (), new String [] {"C000"}));
  assertTrue (Arrays.equals (ratios.get (0), new double [] {-0.088}));
  assertTrue (Arrays.equals (lambdas.get (0), new double [] {4.359}));

} // testOneColumnSlice
//-------------------------------------------------------------------------
public static void main (String [] args) 
{
  junit.textui.TestRunner.run (new TestSuite (MatrixSlicerTest.class));

}// main
//------------------------------------------------------------------------------
} // MatrixSlicerTest


