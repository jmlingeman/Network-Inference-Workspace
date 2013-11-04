/*
 * Created on Aug 10, 2007
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
package edu.duke.cs.banjo.application;

import java.util.*;

import edu.duke.cs.banjo.data.settings.SettingItem;
import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.learner.*;
import edu.duke.cs.banjo.utility.*;

/**
 * Used for searching in a multi-threaded scenario.
 * (Code is based on the old Banjo class, although this class is now used within
 * the wrapper class that handles the actual multi-threading)
 *
 * <p><strong>Change History:</strong> <br>
 * Created on Aug 10, 2007
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class BanjoThreadHandler {
    
    protected volatile Settings settings;
    protected volatile String[] storedArgs;
    protected volatile int threadIndex;
    protected volatile SearcherI searcher;
    protected volatile PreProcessor preProcessor;
    protected volatile PostProcessor postProcessor;
    protected int maxThreads = 1;
    
    /**
     * Simple access point to the Banjo application.
     *
     * @param _args The (optional) arguments for running a search.
     * 
     */    
    public BanjoThreadHandler( String[] _args, 
            final int _threadIndex, 
            Settings _settings ) throws Exception {

        String searcherChoice;
        BanjoErrorHandler errorHandler = new BanjoErrorHandler();
        
        storedArgs = _args;
        threadIndex = _threadIndex;
        
        synchronized( this ) {
            
            try {
                
                // We need each thread to have its own copy of the settings, so use the constructor
                // that makes a (deep) copy of our existing settings
                settings = new Settings( _settings, threadIndex );

                // Set up the error handler to point to our local copy of the settings
                errorHandler = new BanjoErrorHandler( _settings );
                
                // Now validate the settings for our class
                this.validateRequiredData();
                
                // "Adjust" the reportFile values for this (thread) instance:
                String loadedReportFileName = settings.getValidatedProcessParameter( 
                      BANJO.SETTING_REPORTFILE );
                
                loadedReportFileName = 
                    settings.parseForTokensAfterValidation( loadedReportFileName );
                        
                maxThreads = Integer.parseInt( 
                        settings.getDynamicProcessParameter( BANJO.DATA_MAXTHREADS ));
                
                if ( maxThreads > 1 ) {
                    
                    String adjustedReportFileName;
                    String prefixAsLoaded = settings.getValidatedProcessParameter( 
                            BANJO.SETTING_FILENAMEPREFIXFORTHREADS );
                    
                    adjustedReportFileName =
                            StringUtil.getReportFileNameMT( 
                                    loadedReportFileName, prefixAsLoaded, _threadIndex );
                    settings.setDynamicProcessParameter(
                            BANJO.DATA_REPORTFILE, adjustedReportFileName );
    
                    // Associate the proper file I/O with this instance of the settings
                    settings.prepareFileOutput(); 
                }
                                
                // The user's choice for the searcher is now validated
                searcherChoice = settings.getValidatedProcessParameter(
                        BANJO.SETTING_SEARCHERCHOICE );
                
                // ---------------------------------------
                // Set up the searcher object
                // ---------------------------------------
                
                if ( searcherChoice.equalsIgnoreCase( 
                        BANJO.UI_SEARCHER_SIMANNEAL )) {    
                    
                    searcher = new SearcherSimAnneal( settings );
                }
                else if ( searcherChoice.equalsIgnoreCase( 
                        BANJO.UI_SEARCHER_GREEDY ) ) {
            
                    searcher = new SearcherGreedy( settings );
                }
                else if ( searcherChoice.equalsIgnoreCase( 
                        BANJO.UI_SEARCHER_SKIP ) ) {
            
                    searcher = new SearcherSkip( settings );
                }
                else 
                {       
                    
                    // UI_DEFAULT behaviour: We can't guess what searcher the user wants
                    // to use, so skip the search part
                    searcher = new SearcherSkip( settings );
                }

                // ---------------------------------------
                // Run the search
                // ---------------------------------------
                if ( searcher == null ) {
                    
                    // If the searcher choice was valid, but the associated object
                    // can't be created, we need to exit
                    throw new BanjoException( 
                            BANJO.ERROR_BANJO_DEV,
                            "(Banjo.runSearch) " +
                            "Development issue: " +
                            "Searcher object turned out to be invalid (null object) after setup." );
                }
                

                // Set up a pre-processor object, so we can display its options, if necessary
                // (Note: this is commented out because we don't use the pre-processor at this time)
                //preProcessor = new PreProcessor( settings );
                
                // Set up a post-processor object, so we can display its options, if necessary
                postProcessor = new PostProcessor( settings );
                
                // We are now ready to start the search, but since we don't throw
                // exceptions directly anymore, we need to run a quick check:
                if ( settings.wereThereProblems() ) {
                    
                    throw new BanjoException( BANJO.ERROR_CHECKPOINTTRIGGER,
                            "(Checkpoint) Although Banjo was able to perform all validation checks, " +
                            "the following issues prevented further program execution:" + 
                            BANJO.FEEDBACK_NEWLINE +
                            settings.compileErrorMessages().toString() );
                }
            }

            // ---------------------------------------
            // Handle any exception that might have occurred
            // ---------------------------------------
            catch ( final BanjoException e ) {
                
                errorHandler.handleApplicationException( e, settings );
            }
            catch ( OutOfMemoryError e ) {
                                        
                errorHandler.handleOutOfMemoryError( e, settings );
            }
            catch ( Exception e ) {
                
                errorHandler.handleGeneralException( e, settings );
            } 
        }
    }
    
    /**
     * Sets up, then initiates the execution of the Banjo application.
     * 
     */
    public synchronized void runSearch() {
        
        BanjoErrorHandler errorHandler = new BanjoErrorHandler( settings );
                        
        try {
                        
            // ----------------------
            // Do any pre-processing
            // ----------------------
            
            // Execute the preprocessing functions via this wrapper function
            // (Note: this is commented out because we don't use the pre-processor at this time)
            //preProcessor.execute();
                     

            // ----------------------
            // Do the search
            // ----------------------
            
            // Note: based on the settings that govern the search process,
            // this method may take a long time to execute. The search will
            // provide periodic feedback about its progress, though, as well
            // as save results to file intermittently.
            // (The user guide lists the settings and their valid values that
            // control the type and time interval of the feedback)
            if ( searcher != null ) {
            
                searcher.executeSearch();
            }
            
            
            // -------------------------
            // Any problems encountered?
            // -------------------------
            
            // Do a final check (there may have been warnings, or errors that have 
            // been corrected automatically by Banjo)   
            if ( settings.wereThereProblems() ) {
                
                handleFeedbackForProblems();
            }
        }
        
        // ---------------------------------------
        // Handle any exception that might have occurred
        // ---------------------------------------
        catch ( final BanjoException e ) {
            
            errorHandler.handleApplicationException( e, settings );
        }
        catch ( OutOfMemoryError e ) {
                                    
            errorHandler.handleOutOfMemoryError( e, settings );
        }
        catch ( Exception e ) {
            
            errorHandler.handleGeneralException( e, settings );
        }
    }

    /**
     * Validates the settings values required for getting the main Banjo class started.
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
        SettingItem settingItem;
        int validationType;
        
        // Validate the searcher
        settingNameCanonical = BANJO.SETTING_SEARCHERCHOICE;
        settingNameDescriptive = BANJO.SETTING_SEARCHERCHOICE_DESCR;
        settingNameForDisplay = BANJO.SETTING_SEARCHERCHOICE_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
        validationType = BANJO.VALIDATIONTYPE_MANDATORY;
        settingItem = settings.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                validChoices(), 
                BANJO.DATA_SETTINGNODEFAULTVALUE );
                
        // 
        if ( !settingItem.isValidSetting() ) {
            
            throw new BanjoException( BANJO.ERROR_CHECKPOINTTRIGGER,
                    "(Checkpoint) Cannot continue without a valid searcher.\n" + 
                    settings.compileErrorMessages().toString() );
        }

        return isDataValid;
    }
    
    
    /**
     * Provides the valid choices for this class, here: the available searchers.
     */
    public Object validChoices() {
        
        Set validValues = new HashSet();
        
        // Implemented searchers
        validValues.add( BANJO.UI_SEARCHER_GREEDY );
        validValues.add( BANJO.UI_SEARCHER_SIMANNEAL );
        validValues.add( BANJO.UI_SEARCHER_SKIP );
        
        return validValues;
    }

    /**
     * Generates feedback in case the main search loop encountered a problem.
     */
    protected void handleFeedbackForProblems() throws Exception {

        StringBuffer finalCheckPointFeedback = new StringBuffer(
            BANJO.BUFFERLENGTH_STAT );
        int lineLength = BANJO.FEEDBACK_LINELENGTH;
        
        finalCheckPointFeedback.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
                substring( 0, lineLength ));
        finalCheckPointFeedback.append( BANJO.FEEDBACK_NEWLINE );
        finalCheckPointFeedback.append( BANJO.FEEDBACKSTRING_FINALCHECK );
        finalCheckPointFeedback.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
                substring( 0, lineLength ));

        finalCheckPointFeedback.append( BANJO.FEEDBACK_NEWLINE );
        finalCheckPointFeedback.append( settings.compileErrorMessages().toString() );
        
        finalCheckPointFeedback.append( BANJO.FEEDBACK_NEWLINE );
        finalCheckPointFeedback.append( settings.compileWarningMessages().toString() );

        Collection outputFileFlags = new HashSet();
        outputFileFlags.add( new Integer(BANJO.FILE_RESULTS) );
        settings.writeToFile( outputFileFlags , finalCheckPointFeedback );
    }

    /**
     * @return Returns the settings.
     */
    public Settings getSettings() {
        return settings;
    }
}
