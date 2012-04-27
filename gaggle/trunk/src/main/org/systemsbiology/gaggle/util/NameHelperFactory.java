// NameHelperFactory.java
//-----------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

//-----------------------------------------------------------------------------------------
// $Revision: 768 $
// $Date: 2006-04-25 09:58:55 -0700 (Tue, 25 Apr 2006) $
// $Author: pshannon $
//-----------------------------------------------------------------------------------------
package org.systemsbiology.gaggle.util;
//-----------------------------------------------------------------------------------------
import java.util.Arrays;
import cytoscape.data.servers.BioDataServer;
import java.util.HashMap;
//-----------------------------------------------------------------------------------------
public class NameHelperFactory {

  static protected HashMap helpers = new HashMap ();

//-----------------------------------------------------------------------------------------
public static NameHelper getNameHelper (String species) 
{
  BioDataServer bioDataServer = null;
  String canonicalSpeciesName = canonicalizeSpeciesName (species);
  boolean recognizedSpecies = true;  // assume the best
  if (helpers.containsKey (canonicalSpeciesName)) {
    System.out.println ("found cached NameHelper for " + canonicalSpeciesName);
    return (NameHelper) helpers.get (canonicalSpeciesName);
    }
  else
    System.out.println ("   no cached NameHelper for " + canonicalSpeciesName);

  String serverUri = "";
  
  if (canonicalSpeciesName.equals("Halobacterium sp."))
    serverUri = "http://db.systemsbiology.net/cytoscape/annotation/halo/manifest";
  else if (canonicalSpeciesName.equals ("Helicobacter pylori"))
    serverUri = "http://db.systemsbiology.net/cytoscape/annotation/hpy/manifest";
  else if (canonicalSpeciesName.equals ("Homo sapiens"))
    serverUri = "http://db.systemsbiology.net/cytoscape/annotation/human/manifest-synonyms-only";
  else if (canonicalSpeciesName.equals ("Mus musculus"))
    serverUri = "http://db.systemsbiology.net/cytoscape/annotation/mouse/manifest-synonyms-only";
  else if (canonicalSpeciesName.equals ("Drosophila melanogaster"))
    serverUri = "http://db.systemsbiology.net/cytoscape/annotation/fly/manifest-cg";
  else if (canonicalSpeciesName.equals ("Saccharomyces cerevisiae"))
    serverUri = "http://db.systemsbiology.net/cytoscape/annotation/yeast/manifest";
  else if (canonicalSpeciesName.equals ("Pyrococcus furiosus")) {
    serverUri = "http://db.systemsbiology.net/cytoscape/annotation/pyrococcusFuriosus/manifest";
    System.out.println ("NHF detected pyrococcus");
    } 
  else if (canonicalSpeciesName.equals ("Sulfolobus solfataricus"))
    serverUri = "http://db.systemsbiology.net/cytoscape/annotation/sulfolobusSolfataricus/manifest";
  else
    recognizedSpecies = false;

  System.out.println ("\n---------- recognized species? " + recognizedSpecies + "\n");

  if (recognizedSpecies) {
    try {
      bioDataServer = new BioDataServer (serverUri);
      } 
    catch (Exception e) {
      System.err.println ("Exception creating biodataserver for species '" + species + "'");
      e.printStackTrace();
      }
    }
        
  NameHelper newHelper = new NameHelper (canonicalSpeciesName, bioDataServer);
  helpers.put (canonicalSpeciesName, newHelper);

  return newHelper;

} // getNameHelper
//-----------------------------------------------------------------------------------------
protected static String canonicalizeSpeciesName (String name)
{
  String lowerCaseName = name.toLowerCase ();
  String result = name;

  String [] haloNames = {"halo", "halobacterium", "halobacterium sp.", 
                         "halobacterium nrc-1", "halobacterium sp. nrc-1",
                         "halobacterium salinarum"};
  Arrays.sort (haloNames);
  if (Arrays.binarySearch (haloNames, lowerCaseName) >= 0)
    result = "Halobacterium sp.";

  String [] humanNames = {"human", "homo sapiens", "homo"};
  Arrays.sort (humanNames);
  if (Arrays.binarySearch (humanNames, lowerCaseName) >= 0)
    result = "Homo sapiens";

  String [] hpyNames = {"helicobacter pylori", "h. pylori", "hpy"};
  Arrays.sort (hpyNames);
  if (Arrays.binarySearch (hpyNames, lowerCaseName) >= 0)
    result = "Helicobacter pylori";

  String [] flyNames = {"drosophila melanogaster", "drosophila", "fly"};
  Arrays.sort (flyNames);
  if (Arrays.binarySearch (flyNames, lowerCaseName) >= 0)
    result = "Drosophila melanogaster";

  return result;
}
//-----------------------------------------------------------------------------------------
} // class NameHelperFactory
