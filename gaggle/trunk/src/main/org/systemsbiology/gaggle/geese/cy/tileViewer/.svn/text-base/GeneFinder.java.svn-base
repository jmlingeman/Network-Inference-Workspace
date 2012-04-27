// GeneFinder.java
//  find and select the genes transcriptionally controlled by specific tiles
//--------------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.cy.tileViewer;
//---------------------------------------------------------------------------------------
import java.io.*;
import java.util.*;
import y.base.*;
import cytoscape.*;
//---------------------------------------------------------------------------------------
public class GeneFinder {

  protected CytoscapeWindow cw;
  protected GraphObjAttributes nodeAttributes;
  protected HashMap geneAndTileInfo;

//--------------------------------------------------------------------------------------------
public GeneFinder (CytoscapeWindow cw)
{

  this.cw = cw;
  this.nodeAttributes = cw.getNodeAttributes ();
  geneAndTileInfo = extractGeneAndTileInfo ();

}
//--------------------------------------------------------------------------------------------
/**
 * look at all the nodes in the cytoscape window from which the
 *  the matrix browser was launched.  examine each node's attributes
 *  and store those relevant for our purpose
 */
private HashMap extractGeneAndTileInfo ()
{
  HashMap result = new HashMap ();
  int nodeCount = 0;
  int geneCount = 0;
  Node [] allNodes = cw.getGraph().getNodeArray ();
  for (int i=0; i < allNodes.length; i++) {
    nodeCount += 1;
    String canonicalName = cw.getCanonicalNodeName (allNodes [i]);
    if (nodeAttributes.hasAttribute ("Orientation", canonicalName)) {
      geneCount += 1;
      int start = nodeAttributes.getIntegerValue ("Start", canonicalName).intValue ();
      int stop  = nodeAttributes.getIntegerValue ("Stop", canonicalName).intValue ();
      String orientation =  nodeAttributes.getStringValue ("Orientation", canonicalName);
      String replicon =  nodeAttributes.getStringValue ("replicon", canonicalName);
      HashMap summary = new HashMap ();
      summary.put ("start", new Integer (start));
      summary.put ("stop", new Integer (stop));
      summary.put ("orientation", orientation);
      summary.put ("replicon", replicon);
      result.put (canonicalName, summary);
      } // if "Orientation"
   } // for i

  return result;

} // extractGeneAndTileInfo
//--------------------------------------------------------------------------------------------
public void selectCandidateGenes (int distance)
{
  String [] selectedTiles = getSelectedTiles ();
  HashSet genes = new HashSet ();
  for (int i=0; i < selectedTiles.length; i++) {
    String [] candidates = findTranscriptionCandidates (selectedTiles [i], distance);
    for (int c=0; c < candidates.length; c++)
      genes.add (candidates [c]);
    } // for i

  cw.selectNodesByName ((String [])genes.toArray (new String [0]), false);

} // selecteCandidateGenes
//--------------------------------------------------------------------------------------------
String [] findTranscriptionCandidates (String tileName, int distance)
{
  HashSet set = new HashSet ();  
  int tileStart = nodeAttributes.getIntegerValue ("Start", tileName).intValue ();
  int tileStop  = nodeAttributes.getIntegerValue ("Stop", tileName).intValue ();
  String tileReplicon = nodeAttributes.getStringValue ("replicon", tileName);
  String [] geneNames = (String []) geneAndTileInfo.keySet().toArray (new String [0]);
  for (int i=0; i < geneNames.length; i++) {
    String geneName = geneNames [i];
    HashMap geneInfo = (HashMap) geneAndTileInfo.get (geneName);
    int geneStart = ((Integer) geneInfo.get ("start")).intValue ();
    int geneStop = ((Integer) geneInfo.get ("stop")).intValue ();
    String geneOrientation = (String) geneInfo.get ("orientation");
    String geneReplicon = (String) geneInfo.get ("replicon");
    if (isDownstream (tileStart, tileStop, tileReplicon, 
                      geneStart, geneStop, geneOrientation, geneReplicon, distance))
      set.add (geneName);
    } // for i

  return (String []) set.toArray (new String [0]);

}  // findTranscriptionCandidates
//-------------------------------------------------------------------------------------------
/**
 *  a gene is judged downstream of a tile if its start position is within the tile
 *  or within <distance> base pairs of any portion of the tile
 */
boolean isDownstream (int tileStart, int tileStop, String tileReplicon,
                      int geneStart, int geneStop, String geneOrientation,
                      String geneReplicon, int distance)
{  
  if (!tileReplicon.equals (geneReplicon))
    return false;

  if ((geneStart >= tileStart) && (geneStart <= tileStop))
    return true;

  if (geneOrientation.equals ("For")) {
    int gap = geneStart - tileStart;
    if (gap > 0 && gap < distance)
      return true;
    }

  if (geneOrientation.equals ("Rev")) {
    int gap = tileStop - geneStart;
    if (gap > 0 && gap < distance)
      return true;
    }

   return false;

} // isDownstream
//-------------------------------------------------------------------------------------------
String [] getSelectedTiles ()
{
  HashSet set = new HashSet ();

  String [] selectedNames = cw.getSelectedNodeNames ();  
  for (int i=0; i < selectedNames.length; i++) {
    String canonicalName = selectedNames [i];
    if (!nodeAttributes.hasAttribute ("Orientation", canonicalName))
      set.add (canonicalName);
    } // for i

  return (String []) set.toArray (new String [0]);

} // getSelectedTiles
//-------------------------------------------------------------------------------------------
} // class GeneFinder

