// JarFileBasedSequenceFetcherTest.java
//------------------------------------------------------------------------------
// $Revision: 18 $
// $Date: 2005/01/28 20:59:06 $
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

package org.systemsbiology.gaggle.geese.sequence.unitTests;
//--------------------------------------------------------------------------------------
import org.systemsbiology.gaggle.geese.sequence.FileBasedSequenceFetcher;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.util.*;
//--------------------------------------------------------------------------------------
public class JarFileBasedSequenceFetcherTest extends TestCase {

//--------------------------------------------------------------------------------------
public void testTinyFileFromJar () throws Exception 
{
  System.out.println ("testTinyFileFromJar");
  String species = "Halobacterium sp.";
  String uri = "jar://haloTiny.fasta";
  FileBasedSequenceFetcher fetcher = new FileBasedSequenceFetcher (uri);

  String geneName = "VNG0001h";
  String sequence = fetcher.getDnaSequence (species, geneName);
  assertTrue (sequence.length () == 1206);

} // testTinyFileFromJar
//--------------------------------------------------------------------------------------
public static void main (String [] args)
{
  junit.textui.TestRunner.run (new TestSuite (JarFileBasedSequenceFetcherTest.class));
}
//--------------------------------------------------------------------------------------
} // class JarFileBasedSequenceFetcherTest
