// SequenceFetcherFactory.java
//-----------------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.sequence;
//-----------------------------------------------------------------------------------------------
public class SequenceFetcherFactory {

//-----------------------------------------------------------------------------------------------
public static SequenceFetcher createFetcher (String uri) throws Exception
{
  if (uri == null || uri.length () == 0)
    throw new IllegalArgumentException ("DataMatrixReaderFactory.create called with empty uri");

  String protocol = "file://";
  boolean explicitProtocol = (uri.indexOf ("://") > 0);
  if (explicitProtocol) 
    protocol = uri.substring (0, uri.indexOf ("://") + 3);

   if (protocol.equals ("file://") || protocol.equals ("jar://") || protocol.equals ("http://"))
     return new FileBasedSequenceFetcher (uri);
   else {
     String msg = "no SequenceFetcher defined for protocol '" + protocol + "'";
     throw new IllegalArgumentException (msg);
     }

} // createFetcher
//-----------------------------------------------------------------------------------------------
} // SequenceFetcherFactory
