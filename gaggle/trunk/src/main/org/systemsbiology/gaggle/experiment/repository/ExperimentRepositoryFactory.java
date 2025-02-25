// ExperimentRepositoryFactory.java
//-----------------------------------------------------------------------------------------------------
// $Revision: 732 $   
// $Date: 2004/11/03 21:56:02 $ 
// $Author: pshannon $
//-----------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.repository;
//-------------------------------------------------------------------------------------------
public class ExperimentRepositoryFactory {
  static String uri;
  static String protocol;
  static String path;
//-----------------------------------------------------------------------------------------------------
public static ExperimentRepository create (String uri, String user, String password) throws Exception
{
  return create (uri, user, password, false);
}
//-----------------------------------------------------------------------------------------------------
public static ExperimentRepository create (String uri, String user, String password, boolean debug) 
                                         throws Exception
{
  if (uri == null || uri.length () == 0)
    throw new IllegalArgumentException ("ExperimentRepositoryFactory.create called with empty uri");

   parseUri (uri);

   if (protocol.equals ("file://"))
     return new FileRepository (uri, debug);
   else if (protocol.equals ("http://"))
     return new HttpDirectRepository (uri, debug);
   else if (protocol.equals ("httpIndirect://"))
     return new HttpIndirectRepository (uri, user, password, debug);
   else
     throw new IllegalArgumentException ("no ExperimentRepository for protocol '" + protocol + "'");
   
} // create
//-----------------------------------------------------------------------------------------------------
public static ExperimentRepository create (String uri) throws Exception
{
  return ExperimentRepositoryFactory.create (uri, null, null);

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
} // class ExperimentRepositoryFactory
