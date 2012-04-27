// DataMatrixReaderFactory.java
//-----------------------------------------------------------------------------------------------------
// $Revision: 869 $   
// $Date: 2004/10/25 23:23:02 $ 
// $Author: cbare $
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
//-------------------------------------------------------------------------------------------
import org.systemsbiology.gaggle.experiment.datamatrix.*;
//-------------------------------------------------------------------------------------------
// TODO unused
public class DataMatrixReaderFactory {
  static String uri;
  static String protocol;
  static String path;
//-----------------------------------------------------------------------------------------------------
public static DataMatrixReader createReader (String uri) throws IllegalArgumentException
{
  if (uri == null || uri.length () == 0)
    throw new IllegalArgumentException ("DataMatrixReaderFactory.create called with empty uri");

   parseUri (uri);
   if (protocol.equals ("file://") || protocol.equals ("jar://"))
     return new DataMatrixFileReader (uri);
   else
     throw new IllegalArgumentException ("no DataMatrixReader for protocol '" + protocol + "'");
   
} // create
//-----------------------------------------------------------------------------------------------------
static private void parseUri (String uri)
{
   String [] tokens = uri.split ("://");
   if (tokens.length == 2) {
     protocol = tokens [0] + "://";
     path = tokens [1];
     }
   else {
     protocol = "file://";
     path = uri;
     }
    
} // parseUri
//--------------------------------------------------------------------
} // class DataMatrixReaderFactory
