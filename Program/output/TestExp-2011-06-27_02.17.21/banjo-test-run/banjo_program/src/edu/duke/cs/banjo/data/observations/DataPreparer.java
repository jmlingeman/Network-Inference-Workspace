/*
 * Created on Dec 1, 2005
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

import java.util.HashSet;
import java.util.Set;

import edu.duke.cs.banjo.data.settings.*;
import edu.duke.cs.banjo.utility.BANJO;

/**
 * Contains the code common to DataPreparer implementations.
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Dec 1, 2005
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public abstract class DataPreparer implements DataPreparerI {

	protected Settings processData;

	protected final int varCount;
	protected final int maxMarkovLag;
	
    protected PreprocessedObservations observationsToDiscretize;
	String[][] strOriginalDataPoints;
	
    public DataPreparer( Settings _processData, 
            PreprocessedObservations _observationsToDiscretize ) throws Exception {
        
        observationsToDiscretize = _observationsToDiscretize;
        
		processData = _processData;
		
		varCount = Integer.parseInt( 
		        processData.getValidatedProcessParameter( BANJO.SETTING_VARCOUNT ) );
		maxMarkovLag = Integer.parseInt( 
		        processData.getValidatedProcessParameter( BANJO.SETTING_MAXMARKOVLAG ));
		
		validateRequiredData();
    }

	/**
	 * Validates the settings values required for preparing the data (currently
	 * essentially the discretization options).
	 * 
	 * @return Returns the boolean flag that indicates whether a crucial setting
	 * could not be validated.
	 */
    private boolean validateRequiredData() throws Exception {

	    boolean isDataValid = true;
                
	    // utility variables for validating
	    String settingNameCanonical;
	    String settingNameDescriptive;
	    String settingNameForDisplay;
        String settingDataType;
		int validationType;
		Set validValues = new HashSet();
		final int maxItemsUsed = 4;
		SettingItem[] settingItem = new SettingItem[maxItemsUsed];

	    // Validate the discretization parameters
	    settingNameCanonical = BANJO.SETTING_DISCRETIZATIONPOLICY;
	    settingNameDescriptive = BANJO.SETTING_DISCRETIZATIONPOLICY_DESCR;
	    settingNameForDisplay = BANJO.SETTING_DISCRETIZATIONPOLICY_DESCR;
	    settingDataType = BANJO.VALIDATION_DATATYPE_DISCRETIZATIONPOLICY;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem[0] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            processData, 
	            BANJO.UI_DISCRETIZATIONNONE );

	    // Validate: Exceptions to the discretization policy
	    settingNameCanonical = BANJO.SETTING_DISCRETIZATIONEXCEPTIONS;
	    settingNameDescriptive = BANJO.SETTING_DISCRETIZATIONEXCEPTIONS_DESCR;
	    settingNameForDisplay = BANJO.SETTING_DISCRETIZATIONEXCEPTIONS_DESCR;
	    settingDataType = BANJO.VALIDATION_DATATYPE_DISCRETIZATIONEXCEPTIONS;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem[0] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            processData, 
	            BANJO.BANJO_NOVALUESUPPLIED_STRING );
	    
	    // Data report
	    settingNameCanonical = BANJO.SETTING_DISCRETIZATIONREPORT;
	    settingNameDescriptive = BANJO.SETTING_DATAREPORT_DESCR;
	    settingNameForDisplay = BANJO.SETTING_DATAREPORT_DESCR;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    validValues.clear();
	    validValues.add( BANJO.UI_DATAREPORT_STANDARD );
	    validValues.add( BANJO.UI_DATAREPORT_WITHMAPPEDVALUES );
	    validValues.add( BANJO.UI_DATAREPORT_WITHMAPPEDANDORIGINALVALUES );
	    validValues.add( BANJO.UI_DATAREPORT_NO );
	    settingItem[0] = processData.processSetting( settingNameCanonical, 
	        settingNameDescriptive,
            settingNameForDisplay,
	        settingDataType,
	        validationType,
	        validValues,
	        BANJO.DEFAULT_DATAREPORT );
	    
	    return isDataValid;
    }
    
    public abstract void discretizeData() throws Exception;
    
    public abstract Object prepareReport() throws Exception;
}
