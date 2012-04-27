// SpreadsheetLineFinder.java
// given a name for a row, find its line, allowing for a re-sorted and 
// name-substituted spreadsheet
//----------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui;
//----------------------------------------------------------------------------
import java.util.*;
import org.systemsbiology.gaggle.util.*;
//-----------------------------------------------------------------------------
public class SpreadsheetLineFinder {

  String currentRowNames;
  NameHelper nameHelper;
  HashMap lookupTable;

//-----------------------------------------------------------------------------
public SpreadsheetLineFinder (String [] currentRowNames, NameHelper nameHelper)
{
  lookupTable = new HashMap ();  
  for (int i=0; i < currentRowNames.length; i++) {
    String canonicalName = nameHelper.getName ("canonical", currentRowNames [i]);
    lookupTable.put (canonicalName, new Integer (i));
    }

  this.nameHelper = nameHelper;
  
} // ctor
//-----------------------------------------------------------------------------
public int getRowNumber (String candidate)
{
  String adjustedName = candidate; // .toUpperCase ();
  if (!lookupTable.containsKey (adjustedName)) { // try to canonicalize the name
    adjustedName = nameHelper.getName ("canonical", adjustedName);
    }

  Integer lookupResult = (Integer) lookupTable.get (adjustedName);

  if (lookupResult == null)
    return -1;
  else
    return lookupResult.intValue ();

} // getRowNumber
//-----------------------------------------------------------------------------
} // class SpreadsheetLineFinder
