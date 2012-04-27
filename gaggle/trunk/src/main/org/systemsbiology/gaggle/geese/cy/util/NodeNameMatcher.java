// NodeNameMatcher.java
//-----------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.cy.util;
//-----------------------------------------------------------------------------------
import java.util.*;
import cytoscape.data.servers.*;
//-----------------------------------------------------------------------------------
public class NodeNameMatcher {

  String [] nodeNames;
  HashMap nestedNodesHash;
  ArrayList matches;
  BioDataServer annotationServer;
  String species;

//-----------------------------------------------------------------------------------
public NodeNameMatcher (String [] nodeNames, HashMap nestedNodesHash)
{
  this (nodeNames, nestedNodesHash, null, "unknown");
}
//-----------------------------------------------------------------------------------
public NodeNameMatcher (String [] nodeNames, HashMap nestedNodesHash,
                        BioDataServer annotationServer, String species)

{
  this.nodeNames = nodeNames;
  if (nestedNodesHash == null)
    this.nestedNodesHash = new HashMap ();
  else
     this.nestedNodesHash = nestedNodesHash;
  
  this.annotationServer = annotationServer;
  this.species = species;

  matches = new ArrayList ();

} // ctor
//-----------------------------------------------------------------------------------
public String [] getMatch (String [] candidates)
{
  String [] canonicalizedCandidates = canonicalizeNodeNames (candidates);
  findDirectMatches (canonicalizedCandidates);   // directly matches a nodeName, with or w/o wildcards
  findIndirectMatches (canonicalizedCandidates); // matches a nested node, 'contained' by a named node

  return (String []) matches.toArray (new String [0]);
}
//-----------------------------------------------------------------------------------
private void findDirectMatches (String [] candidates)
{
  String [] directMatches;

  for (int i=0; i < candidates.length; i++) {
    String candidate = candidates [i].trim ();
    int locationOfWildCardCharacter = candidate.indexOf ('*');
    boolean wildcardSearch = (locationOfWildCardCharacter == candidate.length () - 1);
    if (wildcardSearch)
      candidate = candidate.substring (0,locationOfWildCardCharacter);
    // System.out.println ("NodeNameMatcher.findDirectMatches, candidate: " + candidate);
    directMatches = doSearch (candidate, nodeNames, wildcardSearch);
    for (int m=0; m < directMatches.length; m++) {
      if (!matches.contains (directMatches [m]))
        matches.add (directMatches [m]);
      } // for m
    } // for i
  
} // findDirectMatches
//-----------------------------------------------------------------------------------
private void findIndirectMatches (String [] candidates)
{
  String [] directMatches;

  for (int i=0; i < candidates.length; i++) {
    String candidate = candidates [i].trim ();
    int locationOfWildCardCharacter = candidate.indexOf ('*');
    boolean wildcardSearch = (locationOfWildCardCharacter == candidate.length () - 1);
    if (wildcardSearch)
      candidate = candidate.substring (0,locationOfWildCardCharacter);

    String [] compoundNodeNames = (String []) nestedNodesHash.keySet().toArray (new String [0]);
    for (int c=0; c < compoundNodeNames.length; c++) {
      String parentNodeName = compoundNodeNames [c];
      // System.out.println ("searching parent node name: " + parentNodeName);
      String [] clusteredNodeNames = (String []) nestedNodesHash.get (parentNodeName);
      directMatches = doSearch (candidate, clusteredNodeNames, wildcardSearch);
      if (directMatches.length > 0)
        if (!matches.contains (parentNodeName))
           matches.add (parentNodeName);
      } // for c
    } // for i
  
} // findIndirectMatches
//-----------------------------------------------------------------------------------
protected String [] doSearch (String candidate, String [] targets, boolean wildcardSearch)
{
  //System.out.println ("NNM.doSearch, " + candidate + " *: " + wildcardSearch + "  target count: " +
  //                    targets.length);

  candidate = candidate.toLowerCase ();
  ArrayList result = new ArrayList ();

  for (int c=0; c < targets.length; c++) {
    String canonicalName = targets [c];
    String canonicalNameLowered = canonicalName.toLowerCase ();
    boolean foundMatch = false;
    if (wildcardSearch) {
      foundMatch = (canonicalNameLowered.indexOf (candidate) == 0);
      if (!foundMatch && annotationServer != null) {
        String commonNameOfTarget = annotationServer.getCommonName (species, canonicalName);
        //System.out.println ("commonNameOfTarget: " + commonNameOfTarget);
        if (commonNameOfTarget.indexOf (candidate) == 0)
          foundMatch = true;
        } //
      } // if wildcardSearch
    else
      foundMatch = (canonicalNameLowered.equals (candidate));
    if (foundMatch && !result.contains (canonicalName))
      result.add (canonicalName);
    } // for c

  return (String []) result.toArray (new String [0]);
   
} // doSearch
//-----------------------------------------------------------------------------------
/** traverse the full synonyms table, creating a complete list of the canonical
  * names implied by the candidates.
  */
public String [] canonicalizeNodeNames (String [] candidates)
{
  ArrayList list = new ArrayList ();
  if (annotationServer == null)
    return candidates;

  for (int i=0; i < candidates.length; i++) {
    String candidate = candidates [i].trim ();
    if (candidate.endsWith ("*"))
      list.add (candidate);
    else {
      String canonicalName = annotationServer.getCanonicalName (species, candidate);
      // System.out.println ("NNM.canonicalize (" + candidate + "): " + canonicalName);
      if (canonicalName != null)
        list.add (canonicalName);
      else
        list.add (candidate);
      } // else: no wildcard
    } // for i

  String [] result = (String []) list.toArray (new String [0]);

  return result;
 
} // canonicalizeNodeNames
//-----------------------------------------------------------------------------------
} // classNodeNameMatcher
