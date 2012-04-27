// NameUniquifier.java
// given a list of known names, and a proposed name, return a 
// unique name as similar as possible to the proposed name
//-------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.boss;
//--------------------------------------------------------------------------------------
import java.util.*;
//------------------------------------------------------------------------------------------
/**
 * @deprecated Use NameUniquifier
 */
public class OldNameUniquifier {

  HashMap nameTable;
//------------------------------------------------------------------------------------------
public OldNameUniquifier()
{
  //System.out.println ("\n------------ creating new NameUniquifier -----------\n"); 
  nameTable = new HashMap ();
  //System.out.println (toString ());
}
//------------------------------------------------------------------------------------------
synchronized public String makeUnique (String candidate)
{

 //System.out.println ("NU.mu, candidate: " + candidate);
 //System.out.println (toString ());

 if (!nameTable.containsKey (candidate)) {
   //System.out.println ("NU.mu, new stem");
   ArrayList list = new ArrayList ();
   nameTable.put (candidate, list);
   //System.out.println ("NU.mu, new stem, returning ->" + candidate);
   return candidate;
   }
 else {
    //System.out.println ("NU.mu, old stem");
    ArrayList numbers = (ArrayList) nameTable.get (candidate);
    int i = 1;
    while (numbers.indexOf (new Integer (i)) >= 0)
      i++;
    numbers.add (new Integer (i));
    StringBuffer sb = new StringBuffer ();
    sb.append (candidate);
    sb.append ('-');
    //System.out.println ("NU.mu, old stem, i: " + i);
    if (i < 10)
      sb.append ('0');
    sb.append (i);
    return sb.toString ();
    }
  
} // makeUnique
//------------------------------------------------------------------------------------------
public String toString ()
{
  StringBuffer sb = new StringBuffer ();
  String [] stemNames = (String []) nameTable.keySet().toArray (new String [0]);
  sb.append ("known stem count: " + stemNames.length + "\n");

  for (int i=0; i < stemNames.length; i++) {
    sb.append ("stem " + i + ": " + stemNames [i]);
    ArrayList list = (ArrayList) nameTable.get (stemNames [i]);
    for (int j=0; j < list.size (); j++)
      sb.append (list.get (j) + " ");
    sb.append ("\n");
    } // for i

  return sb.toString ();

} // toString
//------------------------------------------------------------------------------------------
} // class NameUniquifier
