// NamedXYSeries.java
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
//-------------------------------------------------------------------------------------
import org.jfree.data.xy.XYSeries;
import java.awt.Color;
//-------------------------------------------------------------------------------------
public class NamedXYSeries extends XYSeries {

  protected String canonicalName;
  protected String commonName;
  protected Color color;

//-------------------------------------------------------------------------------------
public NamedXYSeries (String canonicalName)
{
  super (canonicalName);
  this.canonicalName = canonicalName;
  this.commonName = canonicalName;
  this.color = Color.BLACK;
}
//-------------------------------------------------------------------------------------
public NamedXYSeries (String canonicalName, boolean autoSort)
{
  super (canonicalName, autoSort);
  this.canonicalName = canonicalName;
  this.commonName = canonicalName;
  this.color = Color.BLACK;
}
//-------------------------------------------------------------------------------------
public NamedXYSeries (String canonicalName, String commonName, boolean autoSort)
{
  super (canonicalName, autoSort);
  this.canonicalName = canonicalName;
  this.commonName = canonicalName;
  this.color = Color.BLACK;
}
//-------------------------------------------------------------------------------------
public NamedXYSeries (String canonicalName, boolean autoSort, boolean allowDuplicateXValues) 
{
  super (canonicalName, autoSort, allowDuplicateXValues);
  this.canonicalName = canonicalName;
  this.commonName = canonicalName;
  this.color = Color.BLACK;
}
//-------------------------------------------------------------------------------------
public void setCommonName (String newValue)
{
  commonName = newValue;
}
//-------------------------------------------------------------------------------------
public String getCanonicalName ()
{
  return canonicalName;
}
//-------------------------------------------------------------------------------------
public String getCommonName ()
{
  return commonName;
}
//-------------------------------------------------------------------------------------
public void setColor (Color newValue)
{
  color = newValue;
}
//-------------------------------------------------------------------------------------
public Color getColor ()
{
  return color;
}
//-------------------------------------------------------------------------------------
public String toString ()
{
  return canonicalName + ", " + commonName + ": " + color;
}
//-------------------------------------------------------------------------------------
}  // NamedXYSeries
