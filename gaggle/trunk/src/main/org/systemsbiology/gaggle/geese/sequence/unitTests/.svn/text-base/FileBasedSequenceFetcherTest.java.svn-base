// FileBasedSequenceFetcherTest.java
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
public class FileBasedSequenceFetcherTest extends TestCase {

//--------------------------------------------------------------------------------------
public void testTinyFile () throws Exception 
{
  System.out.println ("testTinyFile");
  String species = "Halobacterium sp.";
  String uri = "file://haloTiny.fasta";
  FileBasedSequenceFetcher fetcher = new FileBasedSequenceFetcher (uri);

  String geneName = "VNG0001h";
  String sequence = fetcher.getDnaSequence (species, geneName);
  assertTrue (sequence.length () == 1206);

} // testTinyFile
//--------------------------------------------------------------------------------------
public void testSmallFile () throws Exception 
{
  System.out.println ("testSmallFile");
  String species = "Halobacterium sp.";
  String uri = "file://haloSmall.fasta";
  FileBasedSequenceFetcher fetcher = new FileBasedSequenceFetcher (uri);

  assertTrue (fetcher.getDnaSequence (species, "VNG0001h").length () == 1206);
  assertTrue (fetcher.getDnaSequence (species, "VNG0002g").length () == 666);
  assertTrue (fetcher.getDnaSequence (species, "VNG0003c").length () == 1110);
  assertTrue (fetcher.getDnaSequence (species, "VNG0005h").length () == 2322);

} // testSmallFile
//--------------------------------------------------------------------------------------
public void testSmallFileWithBlankLines () throws Exception 
{
  System.out.println ("testSmallFileWithBlankLines");
  String species = "Halobacterium sp.";
  String uri = "file://haloSmall.fasta";
  FileBasedSequenceFetcher fetcher = new FileBasedSequenceFetcher (uri);

  assertTrue (fetcher.getDnaSequence (species, "VNG0001h").length () == 1206);
  assertTrue (fetcher.getDnaSequence (species, "VNG0002g").length () == 666);
  assertTrue (fetcher.getDnaSequence (species, "VNG0003c").length () == 1110);
  assertTrue (fetcher.getDnaSequence (species, "VNG0005h").length () == 2322);

} // testSmallFileWithBlankLines
//--------------------------------------------------------------------------------------
public void testTinyFileOverHttp () throws Exception 
{
  System.out.println ("testTinyFileOverHttp");
  String species = "Halobacterium sp.";
  String uri = "http://gaggle.systemsbiology.net/tests/haloTiny.fasta";
  FileBasedSequenceFetcher fetcher = new FileBasedSequenceFetcher (uri);

  String geneName = "VNG0001h";
  String sequence = fetcher.getDnaSequence (species, geneName);
  assertTrue (sequence.length () == 1206);

} // testTinyFileOverHttp
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
  junit.textui.TestRunner.run (new TestSuite (FileBasedSequenceFetcherTest.class));
}
//--------------------------------------------------------------------------------------
} // class FileBasedSequenceFetcherTest
