// DataSetDescription.java
//  information about a dataset involved in an experiment
//  there may be multiple datasets in an experiment, for example, a matrix of log10 ratios
//  (control vs. perturbed condition), and a companion error measurement file, often
//  called lambda or p-value.
//  in order for datasets to belong to the same experiment, their matrix of data must
//  have 
//     - the same shape
//     - the same column names
//     - the same row names
// the xml looks like this:
//   <dataset status='primary' type='log10 ratios'>
//     <uri> /users/pshannon/data/halo/microarrayXml/unitTests/zinc.ratio </uri>
//   </dataset>
// 
//   <dataset status='derived' type='lambdas'>
//     <uri> /users/pshannon/data/halo/microarrayXml/unitTests/zinc.lambda </uri>
//   </dataset>
// 
// 
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


/**
 * Describes a subset of a DataMatrix. For example, log-ratios or
 * corresponding lambda values.
 * 
 * uri    = file location
 * status = primary/secondary
 * type   = log10ratios/lambdas
 */
public class DataSetDescription implements java.io.Serializable {

  String uri, status, type;

  public DataSetDescription (String uri, String status, String type)
  {
    this.uri = uri;
    this.status = status;
    this.type = type;
  }

  public String getUri ()
  {
    return uri;
  }

  public void setUri (String newValue)
  {
    uri = newValue;
  }

  public String getStatus ()
  {
    return status;
  }

  public String getType ()
  {
    return type;
  }

  public String toString ()
  {
    StringBuffer sb = new StringBuffer ();
    sb.append ("dataSetDescription: ");
    sb.append (uri);
    sb.append ("   (");
    sb.append (status);
    sb.append (", ");
    sb.append (type);
    sb.append (")");
  
    return sb.toString ();
  }

}