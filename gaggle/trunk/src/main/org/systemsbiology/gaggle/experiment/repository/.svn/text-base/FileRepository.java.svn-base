// FileRepository.java
//----------------------------------------------------------------------------------
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
import java.io.File;
import java.util.*;
//----------------------------------------------------------------------------------
import org.systemsbiology.gaggle.experiment.metadata.*;
import org.systemsbiology.gaggle.experiment.readers.*;

//----------------------------------------------------------------------------------
public class FileRepository extends ExperimentRepository { 

  File directory;
  File singleXmlFile;
  boolean repositoryIsDirectory;

//----------------------------------------------------------------------------------
public FileRepository (String uri)
{
  this (uri, false);
}
//----------------------------------------------------------------------------------
public FileRepository (String uri, boolean debug)
{
  super (uri, debug);
  File f = new File (path);
  if (!f.canRead ())
    throw new IllegalArgumentException ("cannot read repository file or directory: '" + path + "'");

  if (f.isDirectory ()) {
    directory = new File (path);
    repositoryIsDirectory = true;
    }
  else {  // must be a file
    if (!f.getPath().endsWith (".xml"))
      throw new IllegalArgumentException ("single file 'repository' needs .xml extension");
    singleXmlFile = new File (path);
    repositoryIsDirectory = false;
    }

  
} // ctor
//----------------------------------------------------------------------------------
public String [] getExperimentNames ()
{
  if (!repositoryIsDirectory)
    return new String [] {singleXmlFile.getName ()};

  String [] allNames = directory.list ();
  ArrayList result = new ArrayList ();

  for (int i=0; i < allNames.length; i++)
    if (allNames [i].endsWith (".xml"))
      result.add (allNames [i]);

  return (String []) result.toArray (new String [0]);

}// getExperimentNames
//----------------------------------------------------------------------------------
public MetaData getMetaData (String experimentName) throws Exception
{
  String fullUri = protocol + "://" + path;
  if (repositoryIsDirectory)
    fullUri += "/" + experimentName;
  MetaDataXmlParser parser = new MetaDataXmlParser (fullUri);
  return parser.getMetaData ();

} // getMetaData
//----------------------------------------------------------------------------------
public org.systemsbiology.gaggle.core.datatypes.DataMatrix getDataSet (DataSetDescription dsd) throws Exception
{
  String uri = dsd.getUri ();
  DataMatrixFileReader reader = new DataMatrixFileReader (uri);
  reader.read ();
  return reader.get ();
  
}
//----------------------------------------------------------------------------------
} // FileRepository
