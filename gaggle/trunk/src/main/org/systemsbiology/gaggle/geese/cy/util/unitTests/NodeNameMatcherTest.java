// NodeNameMatchergTest
//------------------------------------------------------------------------------
// $Revision: 735 $
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

package org.systemsbiology.gaggle.geese.cy.util.unitTests;
//--------------------------------------------------------------------------------------
import org.systemsbiology.gaggle.geese.cy.util.NodeNameMatcher;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.util.*;

import cytoscape.data.servers.*;
//--------------------------------------------------------------------------------------
public class NodeNameMatcherTest extends TestCase {

//--------------------------------------------------------------------------------------
static String [] getNodeNames ()
{
     // 9 genes and 2 clusters
  return new String [] {"VNG2112C", "VNG6287H", "VNG6438G", "VNG0996G", "VNG1464G",
                        "VNG5237H", "VNG0101G", "VNG2113H", "VNG1467G", 
                        "1", "2"};

}
//--------------------------------------------------------------------------------------
static String [] getNodeNamesWithCommonNameStartingWithB ()
// halobacterium has a handful of genes whose common name begins with 'b'
// return them here, so that we can ensure that a wildcard test ('b*') 
// find them all
//
// VNG0160G boa1
// VNG0664G birL
// VNG0996G boa4
// VNG1197G bcp
// VNG1463G blp
// VNG1464G bat
// VNG1465G brp
// VNG1467G bop
// VNG1488G boa2
// VNG2136G blh
// VNG2213G brr2
// VNG2638G bchP
// VNG5068G boa3
// VNG6211G bdb
{
  String [] result = new String [] {"VNG0160G", "VNG0664G", "VNG0996G", "VNG1197G", 
                                    "VNG1463G", "VNG1464G", "VNG1465G", "VNG1467G", 
                                    "VNG1488G", "VNG2136G", "VNG2213G", "VNG2638G", 
                                     "VNG5068G", "VNG6211G"};

  return result;

}
//--------------------------------------------------------------------------------------
static HashMap getNestedNodesHash  ()
{
  HashMap result = new HashMap ();

  String [] cluster1 = new String [] 
             {"VNG1468h","VNG1464g","VNG1461h","VNG1463g","VNG1465g", "VNG0200H"};

  String [] cluster2 = new String [] 
             {"VNG0402h","VNG1800h","VNG1047h","VNG0617h","VNG0947G","VNG0209h",
              "VNG0207h","VNG1659g","VNG1798h","VNG1465g","VNG2008h","VNG2006c"};

  result.put ("1", cluster1);
  result.put ("2", cluster2);

  return result;

} // getNestedNodesHash
//--------------------------------------------------------------------------------------
public void testExactExplicitMatch () throws Exception 
{
  System.out.println ("testExactExplicitMatch");
  String [] candidates = new String [] {"VNG1467G"};

  NodeNameMatcher matcher = new NodeNameMatcher (getNodeNames (), getNestedNodesHash ());
  String [] match = matcher.getMatch (candidates);
  assertTrue (match.length == 1);
  assertTrue (match [0].equals ("VNG1467G"));

  match = matcher.getMatch (new String [] {"vng1467g"});
  assertTrue (match.length == 1);
  assertTrue (match [0].equals ("VNG1467G"));

} // testExactExplicitMatch
//--------------------------------------------------------------------------------------
public void testWildcardExplicitMatch () throws Exception 
{
  System.out.println ("testWildcardExplicitMatch");
  String [] candidates = new String [] {"VNG211*"};

  NodeNameMatcher matcher = new NodeNameMatcher (getNodeNames (), getNestedNodesHash ());
  String [] match = matcher.getMatch (candidates);
  assertTrue (match.length == 2);
  Arrays.sort (match);
  assertTrue (match [0].equals ("VNG2112C"));
  assertTrue (match [1].equals ("VNG2113H"));

} // testWildcardExplicitMatch
//--------------------------------------------------------------------------------------
public void testExactImplicitMatch () throws Exception 
{
  System.out.println ("testExactImplicitMatch");
  String [] candidates = new String [] {"VNG1800h"};

  NodeNameMatcher matcher = new NodeNameMatcher (getNodeNames (), getNestedNodesHash ());
  String [] match = matcher.getMatch (candidates);
  assertTrue (match.length == 1);
  assertTrue (match [0].equals ("2"));

} // testExactImplicitMatch
//--------------------------------------------------------------------------------------
public void testWildcardImplicitMatch () throws Exception 
{
  System.out.println ("testWildcardImplicitMatch");
  String [] candidates = new String [] {"VNG02*"};

  NodeNameMatcher matcher = new NodeNameMatcher (getNodeNames (), getNestedNodesHash ());
  String [] match = matcher.getMatch (candidates);
  assertTrue (match.length == 2);
  Arrays.sort (match);
  assertTrue (match [0].equals ("1"));
  assertTrue (match [1].equals ("2"));

} // testWilcardImplicitMatch
//--------------------------------------------------------------------------------------
public void testSingleExactExplicitCommonNameMatch () throws Exception 
{
  System.out.println ("testSingleExactExplicitCommonNameMatch");
  String [] candidates = new String [] {"bop"}; 

  String species = "Halobacterium sp.";
  String uri = "http://db.systemsbiology.net/cytoscape/annotation/halo/manifest";
  BioDataServer annotationServer = new BioDataServer (uri);

  NodeNameMatcher matcher = 
      new NodeNameMatcher (getNodeNames (), getNestedNodesHash (), annotationServer, species);

  String [] match = matcher.getMatch (candidates);
  assertTrue (match.length == 1);
  assertTrue (match [0].equals ("VNG1467G"));

} // testSingleExactExplicitCommonNameMatch
//--------------------------------------------------------------------------------------
public void testTripleExactExplicitCommonNameMatch () throws Exception 
{
  System.out.println ("testTripleExactExplicitCommonNameMatch");
  String [] candidates = new String [] {"bop",  "bat",  "fapj"};
    // bop matches VNG1467G
    // bat matches VNG1464G  and is found in cluster 1
    // fapJ is in cluster 2 

  String species = "Halobacterium sp.";
  String uri = "http://db.systemsbiology.net/cytoscape/annotation/halo/manifest";
  BioDataServer annotationServer = new BioDataServer (uri);

  NodeNameMatcher matcher = 
      new NodeNameMatcher (getNodeNames (), getNestedNodesHash (), annotationServer, species);

  String [] match = matcher.getMatch (candidates);
  // System.out.println ("ex ex, match length: " + match.length);
  assertTrue (match.length == 4);
  Arrays.sort (match);
  String [] expected = new String [] {"1", "2", "VNG1464G", "VNG1467G"};
  assertTrue (Arrays.equals (match, expected));

  //for (int i=0; i < match.length; i++)
  //  System.out.println (match [i]);

  //assertTrue (match [0].equals ("1"));
  //assertTrue (match [1].equals ("2"));

} // testTripleExactExplicitCommonNameMatch
//--------------------------------------------------------------------------------------
public void testWildcardCommonNameMatch () throws Exception 
{
  System.out.println ("testWildcardCommonNameMatch");
  String [] candidates = new String [] {"b*"};
  String uri = "http://db.systemsbiology.net/cytoscape/annotation/halo/manifest";
  BioDataServer annotationServer = new BioDataServer (uri);
  String species = "Halobacterium sp.";
  HashMap clusters = null;

  NodeNameMatcher matcher = new NodeNameMatcher (getNodeNamesWithCommonNameStartingWithB (),
                                                 clusters, annotationServer, species);


  String [] match = matcher.getMatch (candidates);
  assertTrue (match.length == 14);

} // testWilcardImplicitMatch
//--------------------------------------------------------------------------------------
public static void main (String [] args)
{
  junit.textui.TestRunner.run (new TestSuite (NodeNameMatcherTest.class));
}
//--------------------------------------------------------------------------------------
} // class NodeNameMatcherTest
