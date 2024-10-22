// DataMatrixReader.java
//-----------------------------------------------------------------------------------
// $Revision: 2360 $   
// $Date: 2004/10/25 23:23:01 $ 
// $Author: dtenenba $
//-----------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.readers;
//-----------------------------------------------------------------------------------

//-----------------------------------------------------------------------------------
abstract public class DataMatrixReader {
  protected String uri;
  protected String protocol = "unassigned";
  protected String path;
  abstract public org.systemsbiology.gaggle.core.datatypes.DataMatrix get () throws Exception;
  abstract public void read () throws Exception;
//-----------------------------------------------------------------------------------
public DataMatrixReader (String uri)
{
  this.uri = uri;
  String [] tokens = uri.split ("://");
  if (tokens.length != 2) {
    this.protocol = "file://";
    this.path = uri;
    }
  else {
    this.protocol = tokens [0] + "://";
    this.path = tokens [1];
    }
}
//-----------------------------------------------------------------------------------
public DataMatrixReader (String protocol, String path)
{
  this.protocol = protocol;
  this.path = path;
}
//-----------------------------------------------------------------------------------
public String getProtocol () 
{
  return protocol;
}
//-----------------------------------------------------------------------------------
public String getPath () 
{
  return path;
}
//-----------------------------------------------------------------------------------
} // class DataMatrixReader
