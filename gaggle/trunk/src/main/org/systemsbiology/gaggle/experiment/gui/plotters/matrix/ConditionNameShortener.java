// ConditionNameShortener.java
//-------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui.plotters.matrix;
//--------------------------------------------------------------------------------------
import java.util.*;
//------------------------------------------------------------------------------------------
public class ConditionNameShortener {

  static int MAX = 8;

//------------------------------------------------------------------------------------------
public ConditionNameShortener ()
{

}
//------------------------------------------------------------------------------------------
static public String shorten (String s)
{
  String result = s;
  if (s.length () <= MAX)
    return result;

  if (s.contains ("_")) {
    String [] tokens = s.split ("_");
    StringBuffer sb = new StringBuffer ();
    for (int i=0; i < tokens.length; i++) {
      String token = tokens [i];
      if (token.length() >= 2)
        sb.append (tokens [i].substring(1, 2));
      } // for i
    result = sb.toString ();
    } // if contains "_"

  return result;
  
} // shorten
//------------------------------------------------------------------------------------------
} // class ConditionNameShortener
