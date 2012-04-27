// ExperimentRepository.java
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
import org.systemsbiology.gaggle.experiment.metadata.*;

//----------------------------------------------------------------------------------
public abstract class ExperimentRepository {
  String uri;
  String protocol;
  String path;
  String [] experimentNames;  
        // experimentNames are strings sufficient to retrieve an experiment, and 
        // are not to be confused with experiment titles.
        // for example:
        //  experimentName:  bop.xml  or   http://db.systemsbiology.net/cytoLink.cgi?experiment=bop
        //  title:  bop knockout, light and oxygen
  boolean debug = false;
//----------------------------------------------------------------------------------
public ExperimentRepository (String uri)
{
  this (uri, false);
}  
//----------------------------------------------------------------------------------
public ExperimentRepository (String uri, boolean debug)
{

  this.uri = uri.trim ();
  this.debug = debug;
  String [] tokens = uri.split ("://");
  if (tokens.length == 2) {
    protocol = tokens [0].trim();
    path = tokens [1].trim();
    }
  else {
    protocol = "file";
    path = uri;
    }
  
} // ctor
//----------------------------------------------------------------------------------
public String getProtocol ()
{
  return protocol;
}
//----------------------------------------------------------------------------------
public String getPath ()
{
  return path;
}
//----------------------------------------------------------------------------------
public String getUri ()
{
  return uri;
}
//----------------------------------------------------------------------------------
public String [] getExperimentNames () throws Exception
{
  return experimentNames;
}
//----------------------------------------------------------------------------------
public abstract MetaData getMetaData (String experimentName) throws Exception;
//----------------------------------------------------------------------------------
public abstract org.systemsbiology.gaggle.core.datatypes.DataMatrix getDataSet (DataSetDescription dsd) throws Exception;
//----------------------------------------------------------------------------------
} // ExperimentRepository
