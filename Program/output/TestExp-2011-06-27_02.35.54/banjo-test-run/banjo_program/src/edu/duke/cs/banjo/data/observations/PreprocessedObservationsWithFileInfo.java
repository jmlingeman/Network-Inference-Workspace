/*
 * Created on May 5, 2006
 * 
 * This file is part of Banjo (Bayesian Network Inference with Java Objects)
 * edu.duke.cs.banjo
 * Banjo is licensed from Duke University.
 * Copyright (c) 2005-2008 by Alexander J. Hartemink.
 * All rights reserved.
 * 
 * License Info:
 * 
 * For non-commercial use, may be licensed under a Non-Commercial Use License.
 * For commercial use, please contact Alexander J. Hartemink or the Office of
 *   Science and Technology at Duke University. More information available at
 *   http://www.cs.duke.edu/~amink/software/banjo
 * 
 */
package edu.duke.cs.banjo.data.observations;

import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.utility.BANJO;

/**
 * Contains a single, basic, set of observation data (usually supplied by a 
 * single file).
 * 
 * <p><strong>Details:</strong> <br>
 * The supplied data is assumed to be originally supplied in string format. 
 * A copy of the original data is stored in string format.
 * After data transformation and (optional) discretization, the processed data
 * is stored in integer format. The customers of this class are served the data
 * in integer format.
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Dec 14, 2005
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class PreprocessedObservationsWithFileInfo extends PreprocessedObservations {
        
    // Cached variables
    protected String directoryName;
    protected String fileName;
    
    public PreprocessedObservationsWithFileInfo( final Settings _processData ) throws Exception {
        
        super( _processData );
    }
        
    public void loadObservations( final String _directoryName,
            final String _fileName ) throws Exception {
        
        // Store the info where the data came from
        directoryName = _directoryName;
        fileName = _fileName;

        //------------------
        // We now determine in what format the observations are in the specified file
        // and where the variable names are located.
        //------------------  

        // Check if user indicated that they the observations are given in transposed form
        String strVariableAreInRowsChoice = processData.getValidatedProcessParameter( 
                BANJO.SETTING_VARIABLESAREINROWS );
        
        if ( strVariableAreInRowsChoice.equalsIgnoreCase( BANJO.UI_VARIABLESAREINROWS_YES ) ) {
            
            variablesInColumns = false;
        }
        
        //
        String strVariableNamesChoice = processData.getDynamicProcessParameter( 
                BANJO.DATA_VARIABLENAMESCHOICE );
        
        if ( strVariableNamesChoice.equalsIgnoreCase( BANJO.UI_VARIABLENAMESCHOICE_INFILE ) ) {
            
            variableNamesInFile = true;
        }
                
        // Now load as rows or columns, as the user has specified
        if ( variablesInColumns ) {
        
            super.loadObservationsAsRows( _directoryName, _fileName );
        }
        else {
        
            super.loadObservationsAsColumns( _directoryName, _fileName );
        }
    }


    /**
     * @return Returns the directoryName.
     */
    public String getDirectoryName() {
        return directoryName;
    }

    /**
     * @return Returns the fileName.
     */
    public String getFileName() {
        return fileName;
    }
}
