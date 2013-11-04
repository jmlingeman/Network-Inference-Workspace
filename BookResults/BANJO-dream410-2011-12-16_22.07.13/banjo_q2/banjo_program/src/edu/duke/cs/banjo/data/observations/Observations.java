/*
 * Created on Mar 4, 2004
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

import java.util.*;
import java.util.regex.Pattern;

import edu.duke.cs.banjo.data.settings.SettingItem;
import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.utility.BANJO;
import edu.duke.cs.banjo.utility.BanjoException;
import edu.duke.cs.banjo.utility.BanjoError;
import edu.duke.cs.banjo.utility.StringUtil;

/**
 * Base class for loading the observation data.
 *
 * <p><strong>Details:</strong> <br>
 *  - <strong>Important:</strong>
 * The Evaluator class uses the internal representation of the observations 
 * for performance, thus breaking encapsulation. <br>
 * 
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Mar 4, 2004 <br>
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 * 
 */

public abstract class Observations implements ObservationsI {

    // The observationRowCount shows the number of rows that were loaded from
    // the associated data file(s) of an observations object
    protected int observationRowCount;  
    
    // The observationCount shows the number of actual observations used in computing
    // the score. Of course, for maxMarkovLag=0, both  observationCount and 
    // observationRowCount are the same. We don't combine both (its possible at a
    // cost of much complexity in the code) because it keeps the code much simpler
    protected int observationCount;

    // The specifiedObservationCount is a user specified number, that needs to be less or
    // equal to the observed observation count (i.e., the observationRowCount), i.e., we
    // interpret it as a number of rows (namely the first n=specifiedObservationCount ones),
    // which is then used to compute the "effective" count of data points that are being
    // used to learn the network.
    // Can only be used when the number of observations files is 1 (otherwise there are
    // issues with the order in which the files are processed - impossible to handle
    // when wildcards are used, but even extremely tricky for multiple files, so for user
    // protection we restrict this feature)
    protected int specifiedObservationCount;
    
    // The observedObservationCount is a computed number, namely the maximum "effective"
    // number of data points that can be used for learning the network (this only comes
    // into play when maxMarkovLag>0 AND number of observation files > 1)
    protected int observedObservationCount;
        
    // "Raw" data points as loaded from file, in dimension observedObservationCount by varCount
    protected String[][] combinedObservationDataPointsAsLoaded;

    // For the set of observations (possibly loaded from multiple files) we keep
    // the individual file info around (useful for debugging; may come in handy for
    // future extensions that need to manipulate the data set in new ways)
    protected PreprocessedObservationsWithFileInfo[] preprocessedObservationsWithFileInfo;
    protected PreprocessedObservations combinedPreprocessedObservationsAsLoaded;
    
    // The number of files that contain observations
    protected int numberOfObservationsFiles;
    
    // This stores the max. number of values that each variables 
    // assumes in the observations
    protected int[] maxValueCount;
    // This is the max. value across all the variables
    protected int maxValue;
    
    // Cached values
    protected Settings processData;
    protected final int varCount;
    protected final int minMarkovLag;
    protected final int maxMarkovLag;
    
    // The (optional) names of the variables
    protected String[] variableNames;
    
    public Observations( 
            final Settings _processData ) throws Exception {

        processData = _processData; 
        
        varCount = Integer.parseInt( processData.getValidatedProcessParameter(
                BANJO.SETTING_VARCOUNT ) );
        
        minMarkovLag = Integer.parseInt( processData.getValidatedProcessParameter(
                BANJO.SETTING_MINMARKOVLAG ) );
        
        maxMarkovLag = Integer.parseInt( processData.getValidatedProcessParameter(
                BANJO.SETTING_MAXMARKOVLAG ) );
        
        maxValueCount = new int[varCount];
        
        boolean isDataValid = validateRequiredData();
        
        // We check if there were any problems. If we found any, we cannot continue
        // setting up the searcher.
        if ( !isDataValid ) {
            
            throw new BanjoException( BANJO.ERROR_CHECKPOINTTRIGGER, 
                    processData.compileErrorMessages().toString() );
        }
    }

    /**
     * Validates the settings values required for loading the observations.
     * 
     * @return Returns the boolean flag that indicates whether a crucial setting
     * could not be validated.
     */
    private boolean validateRequiredData() throws Exception {

        boolean isDataValid = true;
        
        // Set up some utility variables used for validation:
        String settingNameCanonical;
        String settingNameDescriptive;
        String settingNameForDisplay;
        String settingDataType;
        int validationType;
        SettingItem settingItem;
        String strCondition;
        double dblValue;
        Set validValues = new HashSet();
        
        //  Validate:
        settingNameCanonical = BANJO.SETTING_OBSERVATIONSFILE;
        settingNameDescriptive = BANJO.SETTING_OBSERVATIONSFILE_DESCR;
        settingNameForDisplay = BANJO.SETTING_OBSERVATIONSFILE_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
        validationType = BANJO.VALIDATIONTYPE_MANDATORY;
        settingItem = processData.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                BANJO.BANJO_FREEFORMINPUT, 
                "" );
        
        // Validate
        settingNameCanonical = BANJO.SETTING_VARIABLESAREINROWS;
        settingNameDescriptive = BANJO.SETTING_VARIABLESAREINROWS_DESCR;
        settingNameForDisplay = BANJO.SETTING_VARIABLESAREINROWS_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
        validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        validValues.clear();
        validValues.add( BANJO.UI_VARIABLESAREINROWS_YES );
        validValues.add( BANJO.UI_VARIABLESAREINROWS_NO );
        settingItem = processData.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                validValues, 
                BANJO.DEFAULT_VARIABLESAREINROWS );

        // Validate:
        settingNameCanonical = BANJO.SETTING_VARIABLENAMES;
        settingNameDescriptive = BANJO.SETTING_VARIABLENAMES_DESCR;
        settingNameForDisplay = BANJO.SETTING_VARIABLENAMES_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
        validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        settingItem = processData.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                BANJO.BANJO_FREEFORMINPUT, 
                "" );

        String strVariableNamesToStore = "";
        String strVariableNamesChoice = BANJO.DEFAULT_VARIABLENAMES;
        
        // If the user didn't specify the value, use the default
        if ( settingItem.getItemValueValidated() == null  
             || settingItem.getItemValueValidated().equals("") ) {
            
            // Use the default choice for the variable names (none, unless changed by dev.)
            settingItem.setItemValueValidated( BANJO.DEFAULT_VARIABLENAMES );
        }
        // otherwise parse the supplied string or list of strings
        else {
            
            // This is a little involved, so here are the rules:
            // 1. If the value for variableNames starts with "commas:" then we interpret it
            //    as a comma-separated list. The idea behind this is to allow the user to use
            //    variable names that contain spaces.
            // 2. Otherwise we check if variable names is simply a single string, "inFile", to
            //    indicate that we need to load the variable names from file.
            // 3. Else we assume that multiple (white-space separated) strings are the
            //    variable names (note: the length of the list needs to match the varCount,
            //    but we are lenient and fill in the names of variables that are left unassigned)
            
            Pattern patternCheck1 = Pattern.compile( 
                    BANJO.PATTERN_VARIABLENAMESCHECKFORCOLON );
            String strVariableNames = processData.getValidatedProcessParameter( 
                    BANJO.SETTING_VARIABLENAMES );
            
            String[] strVariableNamesCheck1 = patternCheck1.split( strVariableNames );
            
            if ( strVariableNamesCheck1[0].equalsIgnoreCase( 
                    BANJO.PATTERN_VARIABLENAMES_SPECIALSPECIFIER ) ) {
                
                // We found our comma-delimited list
                // so process the names
                strVariableNamesChoice = BANJO.UI_VARIABLENAMESCHOICE_COMMAS;
                strVariableNames = strVariableNamesCheck1[1];                
                
                Pattern p = Pattern.compile( 
                        BANJO.PATTERN_VARIABLENAMESCHECKFORCOMMAS );
                
                // We now split the comma-separated list into its components
                variableNames = p.split( strVariableNames );
                
                // Check that the number of names is the same as the number of variables
                if ( variableNames.length != varCount ) {
    
                    processData.addToWarnings( new BanjoError( 
                            "The number of supplied variable names (" + variableNames.length +
                            ") needs to match the number of variables (" + varCount +
                            "); Banjo defaults to the variable index numbers instead.",
                            BANJO.ERRORTYPE_INVALIDRANGE,
                            settingNameCanonical,
                            null ) );
                    
                    // Default to the variable index numbers
                    variableNames = new String[ varCount ];
                    for ( int i=0; i<varCount; i++ ) {
                        
                        variableNames[i] = Integer.toString( i );
                    }
                }
                else {
                    
                    // This is just a sanity loop to avoid empty strings for some of the variable
                    // names
                    for ( int i=0; i<varCount; i++ ) {
                        
                        if ( variableNames[i].trim().equals("") ) {
                            
                            variableNames[i] = Integer.toString( i );
                        }
                    }
                }
            } // end: variable names specified as comma-separated list
            
            else {
                
                // We have a "standard" whitespace-delimited list (or a single token to 
                // indicate that the variable names are listed in the file itself)
                
                Pattern p = Pattern.compile( 
                        BANJO.PATTERN_VARIABLENAMESPARSING_WHITESPACE );
                
                variableNames = p.split( strVariableNames );
                
                // If there's only a single string, we check if another separator was used
                if ( variableNames.length == 1 ) {
                    
                    p = Pattern.compile( 
                            BANJO.PATTERN_VARIABLENAMESPARSING_DELS );
                    
                    variableNames = p.split( strVariableNames );
                }
                
                // If there's still only a single string, we check against the known values
                if ( variableNames.length == 1 ) {
                    
                    // Do a special validation on the allowable strings
                    
                    settingNameCanonical = BANJO.SETTING_VARIABLENAMES;
                    settingNameDescriptive = BANJO.SETTING_VARIABLENAMES_DESCR;
                    settingNameForDisplay = BANJO.SETTING_VARIABLENAMES_DISP;
                    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
                    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
                    validValues.clear();
                    validValues.add( BANJO.UI_VARIABLENAMES_INFILE );
                    validValues.add( BANJO.UI_VARIABLENAMES_NONE );
                    settingItem = processData.processSetting( settingNameCanonical, 
                            settingNameDescriptive,
                            settingNameForDisplay,
                            settingDataType,
                            validationType,
                            validValues, 
                            BANJO.DEFAULT_VARIABLENAMES );
    
                    strVariableNamesChoice = processData.getValidatedProcessParameter( 
                            BANJO.SETTING_VARIABLENAMES );
                }
                // otherwise check if we have exactly the right number of names
                else {
                
                    // Check that the number of names is the same as the number of variables
                    if ( variableNames.length != varCount ) {
        
                        processData.addToWarnings( new BanjoError( 
                                "The number of supplied variable names (" + variableNames.length +
                                ") needs to match the number of variables (" + varCount +
                                "); Banjo defaults to the variable index numbers instead.",
                                BANJO.ERRORTYPE_INVALIDRANGE,
                                settingNameCanonical,
                                null ) );
                        
                        // Default to the variable index numbers
                        variableNames = new String[ varCount ];
                        for ( int i=0; i<varCount; i++ ) {
                            
                            variableNames[i] = Integer.toString( i );
                        }
                    }
                    else {
                        
                        // This is just a sanity loop to avoid empty strings for some of the variable
                        // names
                        for ( int i=0; i<varCount; i++ ) {
                            
                            if ( variableNames[i].trim().equals("") ) {
                                
                                variableNames[i] = Integer.toString( i );
                            }
                        }
                    }
                } // end: variableNames contains more than one item (i.e., is a list)
            }
        }

        if ( variableNames != null && variableNames.length == varCount ) {
            
            // Collect the names
            strVariableNamesToStore = variableNames[0];
            for ( int i=1; i<varCount; i++ ) {
                
                strVariableNamesToStore += BANJO.DELIMITER_SPECIAL + variableNames[i];
            }
        }
        else { 
            
            // Set the names to their index values
            strVariableNamesToStore = "0";
            for ( int i=1; i<varCount; i++ ) {
                
                strVariableNamesToStore += BANJO.DELIMITER_SPECIAL + i;
            }
        }
        
        processData.setDynamicProcessParameter( 
                BANJO.DATA_VARIABLENAMESCHOICE, strVariableNamesChoice );
        processData.setDynamicProcessParameter( 
                BANJO.DATA_VARIABLENAMES, strVariableNamesToStore );
        
        if ( strVariableNamesChoice.equalsIgnoreCase( BANJO.UI_VARIABLENAMESCHOICE_INFILE )) {
            
            // We make the observation count mandatory, to be able to check for accidental
            // mistakes that could otherwise invalidate results
            
            // Validate:
            settingNameCanonical = BANJO.SETTING_OBSERVATIONCOUNT;
            settingNameDescriptive = BANJO.SETTING_OBSERVATIONCOUNT_DESCR;
            settingNameForDisplay = BANJO.SETTING_OBSERVATIONCOUNT_DISP;
            settingDataType = BANJO.VALIDATION_DATATYPE_INTEGER;
            validationType = BANJO.VALIDATIONTYPE_MANDATORY;
            settingItem = processData.processSetting( settingNameCanonical, 
                    settingNameDescriptive,
                    settingNameForDisplay,
                    settingDataType,
                    validationType,
                    null, 
                    Integer.toString( BANJO.BANJO_OBSERVATIONCOUNT_NONUMBERSUPPLIED ) );
            
            if ( settingItem.isValidSetting() && !settingItem.getItemValueValidated().equals( 
                    Integer.toString( BANJO.BANJO_OBSERVATIONCOUNT_NONUMBERSUPPLIED ) ) ) {
                
                try {
            
                    strCondition = new String( "greater than 0" );
                    dblValue = Double.parseDouble( 
                            processData.getValidatedProcessParameter(
                                    settingNameCanonical ));
                    if ( dblValue <= 0 ) {
                        
                        processData.addToErrors( new BanjoError( 
                                StringUtil.composeErrorMessage( 
                                        settingItem, 
                                        strCondition ),
                                BANJO.ERRORTYPE_INVALIDRANGE,
                                settingNameCanonical,
                                StringUtil.getClassName( this ) ) );
                        
                        isDataValid = false;
                    }
                }
                catch ( Exception e ) {
            
                    throw new BanjoException(
                            BANJO.ERROR_BANJO_DEV, settingItem, this );
                }
            }
            else {
              
              processData.addToErrors( new BanjoError( 
                      "The number of observations ('" + settingNameCanonical +
                      "') needs to be specified in the settings file, when " +
                      "the variable names are provided in the observations file.",
                      BANJO.ERRORTYPE_MISSINGVALUE,
                      settingNameCanonical,
                      null ) );
                
                isDataValid = false;
            }

        }
        else {
            
            // In this case the observation count is optional as in the past,
            // but when it is supplied, it needs to be >0
        
            //  Validate:
            settingNameCanonical = BANJO.SETTING_OBSERVATIONCOUNT;
            settingNameDescriptive = BANJO.SETTING_OBSERVATIONCOUNT_DESCR;
            settingNameForDisplay = BANJO.SETTING_OBSERVATIONCOUNT_DISP;
            settingDataType = BANJO.VALIDATION_DATATYPE_INTEGER;
            validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
            settingItem = processData.processSetting( settingNameCanonical, 
                    settingNameDescriptive,
                    settingNameForDisplay,
                    settingDataType,
                    validationType,
                    null, 
                    Integer.toString( BANJO.BANJO_OBSERVATIONCOUNT_NONUMBERSUPPLIED ) );
            
            if ( settingItem.isValidSetting() && !settingItem.getItemValueValidated().equals( 
                    Integer.toString( BANJO.BANJO_OBSERVATIONCOUNT_NONUMBERSUPPLIED ) ) ) {
                
                try {
            
                    strCondition = new String( "greater than 0" );
                    dblValue = Double.parseDouble( 
                            processData.getValidatedProcessParameter(
                                    settingNameCanonical ));
                    if ( dblValue <= 0 ) {
                        
                        processData.addToErrors( new BanjoError( 
                                StringUtil.composeErrorMessage( 
                                        settingItem, 
                                        strCondition ),
                                BANJO.ERRORTYPE_INVALIDRANGE,
                                settingNameCanonical,
                                StringUtil.getClassName( this ) ) );
                    }
                }
                catch ( Exception e ) {
            
                    throw new BanjoException(
                            BANJO.ERROR_BANJO_DEV, settingItem, this );
                }
            }
        }
        
        return isDataValid;
    }
}
