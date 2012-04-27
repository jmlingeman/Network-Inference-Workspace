// Variable.java
// an experimental variable consists of a name, a value, and (often) units
// for example (drawing from an xml experiment file), in which two variables describe an
// experimental condition 'C10'
//
//   <condition alias='C10'>
//     <variable name='gamma irradiation' value='false'/>
//     <variable name='time'              value='10'      units='minutes'/>
//   </condition>
//
// this class captures the 3 fields: name, value, units 
//-----------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.metadata;
//--------------------------------------------------------------------------------------
public class Variable implements java.io.Serializable {
  String name, value, units;
//-----------------------------------------------------------------------------------------
public Variable (String name, String value)
{
  this (name, value, null);
}
//-----------------------------------------------------------------------------------------
public Variable (String name, String value, String units)
{
  this.name = name;
  this.value = value;
  this.units = units;
}
//-----------------------------------------------------------------------------------------
public String getName ()
{
  return name;
}
//-----------------------------------------------------------------------------------------
public String getValue ()
{
  return value;
}
//-----------------------------------------------------------------------------------------
public String getUnits ()
{
  return units;
}
//-----------------------------------------------------------------------------------------
public boolean equals (Variable other)
{
  //assert name != null;
  //assert getValue () != null;
  //assert other.getName ();
  //assert other.getValue ();

  boolean unitsMatch = false;  // be pessimistic
  if (getUnits () == null) {
    if (other.getUnits () == null)
    unitsMatch = true;
    }
  else if (other.getUnits () != null && other.getUnits ().equals (getUnits ()))
    unitsMatch = true;
      
  return (getName().equals (other.getName ()) &&
          getValue().equals (other.getValue ()) &&
          unitsMatch);
}
//-----------------------------------------------------------------------------------------
public String toString ()
{
  StringBuffer sb = new StringBuffer ();
  sb.append (name);
  sb.append (": ");
  sb.append (value);
  sb.append (" (");
  sb.append (units);
  sb.append (")");
  return sb.toString ();

}
//-----------------------------------------------------------------------------------------
} // class Variable
