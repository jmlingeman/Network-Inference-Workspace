// CodonTableTest.java
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
import org.systemsbiology.gaggle.geese.sequence.CodonTable;
import junit.framework.TestCase;
import junit.framework.TestSuite;
//--------------------------------------------------------------------------------------
public class CodonTableTest extends TestCase {

//--------------------------------------------------------------------------------------
public void testSimple () throws Exception 
{
  System.out.println ("testSimple");
  CodonTable table = new CodonTable ();

  assertTrue (table.getAminoAcidSequence ("TAA") == null);
  assertTrue (table.getAminoAcidSequence ("TAG") == null);
  assertTrue (table.getAminoAcidSequence ("TGA") == null);

  assertTrue (table.getAminoAcidSequence ("") == null);
  assertTrue (table.getAminoAcidSequence ("bogus") == null);
  assertTrue (table.getAminoAcidSequence ("TTTTTA") == null);
  assertTrue (table.getAminoAcidSequence ("TTT TTA") == null);

  assertTrue (table.getAminoAcidSequence ("TTT").equals ("F"));
  assertTrue (table.getAminoAcidSequence ("TTC").equals ("F"));
  assertTrue (table.getAminoAcidSequence ("TTA").equals ("L"));
  assertTrue (table.getAminoAcidSequence ("TTG").equals ("L"));
  assertTrue (table.getAminoAcidSequence ("TCT").equals ("S"));
  assertTrue (table.getAminoAcidSequence ("TCC").equals ("S"));
  assertTrue (table.getAminoAcidSequence ("TCA").equals ("S"));
  assertTrue (table.getAminoAcidSequence ("TCG").equals ("S"));
  assertTrue (table.getAminoAcidSequence ("TAT").equals ("Y"));
  assertTrue (table.getAminoAcidSequence ("TAC").equals ("Y"));
  assertTrue (table.getAminoAcidSequence ("TGT").equals ("C"));
  assertTrue (table.getAminoAcidSequence ("TGC").equals ("C"));
  assertTrue (table.getAminoAcidSequence ("TGG").equals ("W"));
  assertTrue (table.getAminoAcidSequence ("CTT").equals ("L"));
  assertTrue (table.getAminoAcidSequence ("CTC").equals ("L"));
  assertTrue (table.getAminoAcidSequence ("CTA").equals ("L"));
  assertTrue (table.getAminoAcidSequence ("CTG").equals ("L"));
  assertTrue (table.getAminoAcidSequence ("CCT").equals ("P"));
  assertTrue (table.getAminoAcidSequence ("CCC").equals ("P"));
  assertTrue (table.getAminoAcidSequence ("CCA").equals ("P"));
  assertTrue (table.getAminoAcidSequence ("CCG").equals ("P"));
  assertTrue (table.getAminoAcidSequence ("CAT").equals ("H"));
  assertTrue (table.getAminoAcidSequence ("CAC").equals ("H"));
  assertTrue (table.getAminoAcidSequence ("CAA").equals ("Q"));
  assertTrue (table.getAminoAcidSequence ("CAG").equals ("Q"));
  assertTrue (table.getAminoAcidSequence ("CGT").equals ("R"));
  assertTrue (table.getAminoAcidSequence ("CGC").equals ("R"));
  assertTrue (table.getAminoAcidSequence ("CGA").equals ("R"));
  assertTrue (table.getAminoAcidSequence ("CGG").equals ("R"));
  assertTrue (table.getAminoAcidSequence ("ATT").equals ("I"));
  assertTrue (table.getAminoAcidSequence ("ATC").equals ("I"));
  assertTrue (table.getAminoAcidSequence ("ATA").equals ("I"));
  assertTrue (table.getAminoAcidSequence ("ATG").equals ("M"));
  assertTrue (table.getAminoAcidSequence ("ACT").equals ("T"));
  assertTrue (table.getAminoAcidSequence ("ACC").equals ("T"));
  assertTrue (table.getAminoAcidSequence ("ACA").equals ("T"));
  assertTrue (table.getAminoAcidSequence ("ACG").equals ("T"));
  assertTrue (table.getAminoAcidSequence ("AAT").equals ("N"));
  assertTrue (table.getAminoAcidSequence ("AAC").equals ("N"));
  assertTrue (table.getAminoAcidSequence ("AAA").equals ("K"));
  assertTrue (table.getAminoAcidSequence ("AAG").equals ("K"));
  assertTrue (table.getAminoAcidSequence ("AGT").equals ("S"));
  assertTrue (table.getAminoAcidSequence ("AGC").equals ("S"));
  assertTrue (table.getAminoAcidSequence ("AGA").equals ("R"));
  assertTrue (table.getAminoAcidSequence ("AGG").equals ("R"));
  assertTrue (table.getAminoAcidSequence ("GTT").equals ("V"));
  assertTrue (table.getAminoAcidSequence ("GTC").equals ("V"));
  assertTrue (table.getAminoAcidSequence ("GTA").equals ("V"));
  assertTrue (table.getAminoAcidSequence ("GTG").equals ("V"));
  assertTrue (table.getAminoAcidSequence ("GCT").equals ("A"));
  assertTrue (table.getAminoAcidSequence ("GCC").equals ("A"));
  assertTrue (table.getAminoAcidSequence ("GCA").equals ("A"));
  assertTrue (table.getAminoAcidSequence ("GCG").equals ("A"));
  assertTrue (table.getAminoAcidSequence ("GAT").equals ("D"));
  assertTrue (table.getAminoAcidSequence ("GAC").equals ("D"));
  assertTrue (table.getAminoAcidSequence ("GAA").equals ("E"));
  assertTrue (table.getAminoAcidSequence ("GAG").equals ("E"));
  assertTrue (table.getAminoAcidSequence ("GGT").equals ("G"));
  assertTrue (table.getAminoAcidSequence ("GGC").equals ("G"));
  assertTrue (table.getAminoAcidSequence ("GGA").equals ("G"));
  assertTrue (table.getAminoAcidSequence ("GGG").equals ("G"));


} // test
//--------------------------------------------------------------------------------------
public static void main (String [] args)
{
  junit.textui.TestRunner.run (new TestSuite (CodonTableTest.class));
}
//--------------------------------------------------------------------------------------
} // class CodonTableTest
