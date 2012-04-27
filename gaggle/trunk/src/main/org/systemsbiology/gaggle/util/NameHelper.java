// NameHelper.java
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

import java.util.*;
import cytoscape.data.servers.BioDataServer;

//-----------------------------------------------------------------------------------------
public class NameHelper {
    
  private String species = null;
  private String defaultCategory = null;
  private ArrayList categoryList;
  private BioDataServer bioDataServer = null;
    
//-----------------------------------------------------------------------------------------
public NameHelper (String species, BioDataServer bioDataServer) 
{
  this.species = species;
  this.bioDataServer = bioDataServer;
  categoryList = new ArrayList();
  categoryList.add("Canonical");
  categoryList.add("Common");
  defaultCategory = (String)categoryList.get(0);

}
//-----------------------------------------------------------------------------------------
public NameHelper(String species) 
{
  this.species = species;
}
//-----------------------------------------------------------------------------------------
public NameHelper () 
{
}
//-----------------------------------------------------------------------------------------
public String getName (String category, String name) 
{
  //System.out.println ("NameHelper, getName, species: " + species + " category: " + category + "  name: " + name);
  if (null == species) {
    //System.out.println ("    species = null");
    return name;
    }
  if (null == bioDataServer) {
    //System.out.println ("    bioDataServer = null");
    return name;
    } 
  if (null == defaultCategory) {
    //System.out.println ("    defaultCategory = null");
    return name;
    }
  if (null == categoryList) {
    //System.out.println ("    categoryList = null");
    return name;
    }
  if (categoryList.size() < 2) {
    //System.out.println ("    categoryList.size < 2");
    return name;
   }
  if (category.equalsIgnoreCase ("common")) {
    //System.out.print ("   looking for commonName for " + species + ", " + name);
    String [] commonNames = bioDataServer.getAllCommonNames (species,name);
    if (commonNames != null & commonNames.length > 0) {
       //System.out.println (" commonName found in biodataserver: " + commonNames [0]);
       return commonNames [0];
       }
    //else {
    //  System.out.println ("no common name found in biodataserver");
    //  }
    }
  if (category.equalsIgnoreCase ("canonical")) {
    String canonicalName = bioDataServer.getCanonicalName (species,name);
    if (canonicalName != null)
       return canonicalName;
    }
 
   return name;

} // getName
//-----------------------------------------------------------------------------------------
public String getDefaultCategory () 
{
  return defaultCategory;
}
//-----------------------------------------------------------------------------------------
public String [] getCategoryList ()
{
 if (null == categoryList)
    return null;
  String [] result = new String [categoryList.size()];
  categoryList.toArray (result);
  return result;

} // getCategoryList
//-----------------------------------------------------------------------------------------
} // class NameHelper
