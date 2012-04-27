// MatrixSlicer.java
//---------------------------------------------------------------------------------------
// $Revision: 2360 $
// $Date: 2007-09-04 18:02:00 -0400 (Tue, 04 Sep 2007) $
// $Author: dtenenba $
//---------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.datamatrix;
//---------------------------------------------------------------------------------------
import java.util.*;

import org.systemsbiology.gaggle.experiment.metadata.*;
import org.systemsbiology.gaggle.experiment.readers.*;

//---------------------------------------------------------------------------------------
/** 
 *  create sliced (subset) views of all of the views in the experiment
 *  described by the MetaData object provided to the ctor.  this is the
 *  mechanism:
 *  <ol>
 *     <li> MetaData supports selection by column
 *     <li> MetaData provides the uri of the datamatrices it describes
 *     <li> for each data matrix (via the MetaData DataSetDescription's)
 *          read the data, creating a DataMatrix
 *     <li> from the DataMatrix, create a LensedDataMatrix
 *     <li> traverse the MetaData's selected columns 
 *     <li> enable only those columns in the lenses
 *     <li> create a DataMatrix via lens.getVisibleMatrix ()
 *     <li> return an array of the sliced views
 *  </ol>
 *
 */
//---------------------------------------------------------------------------------------
public class MatrixSlicer {

   MetaData experiment;

//---------------------------------------------------------------------------------------
/** 
 * slice off some selected columns of the views described by the ctor
 * MetaData arg.
 */
public MatrixSlicer (MetaData experiment)
{

  this.experiment = experiment;

}
//---------------------------------------------------------------------------------------
/**
 *  read all views mentioned in the MetaData, create lenses, select appropriate
 *  columns, get the resulting 'visible matrix' from each; return the list
 *
 */
public HashMap slice () throws Exception
{
   //ArrayList list = new ArrayList ();
   HashMap result = new HashMap ();

   DataSetDescription [] dsd = experiment.getDataSetDescriptions ();
   for (int i=0; i < dsd.length; i++) {
     String uri = dsd [i].getUri ();
     DataMatrixFileReader reader = new DataMatrixFileReader (uri);
     reader.read ();
     org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix = reader.get ();
     LensedDataMatrix lens = new LensedDataMatrix (matrix);
     matrix.setSpecies (experiment.getSpecies ());
     lens.setSpecies (experiment.getSpecies ());
     lens.enableAllColumns ();
     lens.enableAllRows ();
     String [] allLensColumnTitles = lens.getColumnTitles ();
     String [] selectedConditions = experiment.getSelectedConditionsAsAliases ();
       // in the lens, select every column selected in the experiment 
     lens.disableAllColumns ();
     for (int c=0; c < selectedConditions.length; c++) {
       for (int d=0; d < allLensColumnTitles.length; d++) {
         String lensColumn = allLensColumnTitles [d];
         String selectedColumn = selectedConditions [c];
         if (lensColumn.equals (selectedColumn)) {
           lens.setColumnState (d, true);
           } // if
         } // for d
       } // for c
     String matrixType = dsd [i].getType ();
     result.put (matrixType, lens.getVisibleMatrix ());
     } // for i 

   return result;

} // slice
//---------------------------------------------------------------------------------------
} // class MatrixSlicer
