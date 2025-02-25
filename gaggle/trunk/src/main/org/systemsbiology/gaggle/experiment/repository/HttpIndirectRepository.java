// HttpDirectRepository
//----------------------------------------------------------------------------------
// $Revision: 2360 $
// $Date: 2004/11/03 21:56:05 $
// $Author: dtenenba $
//--------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.repository;
//----------------------------------------------------------------------------------
import java.io.*;
//----------------------------------------------------------------------------------
import org.systemsbiology.gaggle.experiment.metadata.*;
import org.systemsbiology.gaggle.experiment.readers.*;

//----------------------------------------------------------------------------------
public class HttpIndirectRepository extends ExperimentRepository { 

  TextHttpIndirectFileReader indirectReader;

//----------------------------------------------------------------------------------
public HttpIndirectRepository (String uri, String user, String password) throws Exception
{
  this (uri, user, password, false);
}
//----------------------------------------------------------------------------------
public HttpIndirectRepository (String uri, String user, String password, boolean debug)
                          throws Exception
{
  super (uri, debug);

    // user & password obtained from ./datamatrix.props
    // 
    // a typical uri is
    //    httpIndirect://db.systemsbiology.net:8080/halo/DataFetcher.py
    // which the reader will translate into
    //    http://db.systemsbiology.net:8080/halo/DataFetcher.py?mode=dir&name=xml&user=tester0&pw=pw0
    // similarly, 
    //    halo/DataFetcher.py/sample.ratio
    // is translated ionto
    //    halo/DataFetcher/mode=getFile&name=sample.ratio&user=tester&pw=pw0
    
  indirectReader = new TextHttpIndirectFileReader (uri, user, password, debug);

} // ctor
//----------------------------------------------------------------------------------
public HttpIndirectRepository (String uri) throws Exception
{
  this (uri, null, null);

} // ctor
//----------------------------------------------------------------------------------
public String [] getExperimentNames () throws Exception
{
  int count = indirectReader.read (uri);
  String text = indirectReader.getText ();
  String [] lines = text.split ("\n");
  if (lines.length == 1 && lines [0].trim().length () == 0)
    return new String [0];
  return lines;

}// getExperimentNames
//----------------------------------------------------------------------------------
public MetaData getMetaData (String experimentName) throws Exception
{
  String fullUri = protocol + "://" + path + "/" + experimentName;
  int bytesRead;
  try {
    bytesRead = indirectReader.read (fullUri);
    }
  catch (Exception e0) {
    throw new IOException ("HttpIndirectRepository.getMetaData failed to read " + fullUri);
    }


  if (debug)
    System.err.println ("HttpIndirectRepository, bytes read: " + bytesRead);

  File tmpFile = indirectReader.writeToTemporaryFile ();

  MetaDataXmlParser parser = new MetaDataXmlParser (tmpFile.getPath ());
  MetaData result = parser.getMetaData ();
  result.setUri(fullUri);
  if (!debug)
    tmpFile.delete ();
  return result;

} // getMetaData
//----------------------------------------------------------------------------------
public org.systemsbiology.gaggle.core.datatypes.DataMatrix getDataSet (DataSetDescription dsd) throws Exception
{
  String uri = dsd.getUri ();
  int bytesRead;
  try {
    bytesRead = indirectReader.read (uri);
    }
  catch (Exception e0) {
    throw new IOException ("HttpIndirectRepository.getDataSet failed to read " + uri);
    }

  if (debug)
    System.err.println ("HttpIndirectRepository.getDataSet, bytes read: " + bytesRead);

  File tmpFile = indirectReader.writeToTemporaryFile ();
  DataMatrixFileReader reader = new DataMatrixFileReader (tmpFile.getPath ());
  reader.read ();

  org.systemsbiology.gaggle.core.datatypes.DataMatrix result = reader.get ();
  if (!debug)
     tmpFile.delete ();
  return result;
 
  
} // getDataSet
//---------------------------------------------------------------------------
} // HttpIndirectRepository
