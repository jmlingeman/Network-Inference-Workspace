/*
 * Created on May 10, 2004
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

package edu.duke.cs.banjo.learner.components;

import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.learner.*;
import edu.duke.cs.banjo.utility.*;

import java.text.DateFormat;
import java.util.*;

/**
 * Combines the common code shared by the different statistics implementations.
 *
 * <p><strong>Details:</strong> <br>
 * 
 * <p><strong>Change History:</strong> <br>
 * Created May 10, 2004
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */

public abstract class Recorder implements RecorderI {

	// Internal data (statistics) collection buffer
	protected volatile StringBuffer statisticsBuffer;
	
	// Individual tracking strings
	protected volatile StringBuffer resultBuffer;
	protected volatile StringBuffer summaryBuffer;
	protected volatile StringBuffer traceBuffer;
	
	// Indicators (using collections) for printing to file(s)
	protected Collection outputFileFlags = new HashSet(
	        BANJO.MAXOUTPUTFILES );
	protected Collection outputResultsOnly = new HashSet(
	        BANJO.MAXOUTPUTFILES );
	protected Collection outputTraceOnly = new HashSet(
	        BANJO.MAXOUTPUTFILES );
	protected Collection outputSummaryOnly = new HashSet(
	        BANJO.MAXOUTPUTFILES );
	protected Collection outputToAllFiles = new HashSet(
	        BANJO.MAXOUTPUTFILES );
	
	protected final Settings processData;
	protected DateFormat timeFormat;
    
	private static final String strHeader = new String(" -- ");	

	public Recorder( Settings _processData ) throws Exception {

		// Keep a link to the settings around
		this.processData = _processData;

		timeFormat = DateFormat.getDateTimeInstance( DateFormat.SHORT, 
		        DateFormat.MEDIUM );
		
		statisticsBuffer = new StringBuffer( BANJO.BUFFERLENGTH_STAT );
		resultBuffer = new StringBuffer( BANJO.BUFFERLENGTH_STAT );
		summaryBuffer = new StringBuffer( BANJO.BUFFERLENGTH_STAT );
		traceBuffer = new StringBuffer( BANJO.BUFFERLENGTH_STAT );

		// Set up the collections used for printing the results	
		outputResultsOnly.add( new Integer(BANJO.FILE_RESULTS) );
		outputSummaryOnly.add( new Integer(BANJO.FILE_SUMMARY) );
		outputTraceOnly.add( new Integer(BANJO.FILE_TRACE) );
		//
		outputToAllFiles.add( new Integer(BANJO.FILE_RESULTS) );
		outputToAllFiles.add( new Integer(BANJO.FILE_SUMMARY) );
		outputToAllFiles.add( new Integer(BANJO.FILE_TRACE) );		
	}
	
	// may want to keep the timing at which we write to disk internal to this class
	protected synchronized void commitData( final Collection _outputFileFlags, 
	        final StringBuffer _textToCommit ) throws Exception {
		
	    try {
	        	        
            processData.writeToFile( _outputFileFlags , _textToCommit );
	    }
		catch (BanjoException e) {
		    
		    // Only for now:
		    System.out.println( "Recorder.commitData  -- BanjoException: " 
					+ e.toString());

			throw new BanjoException( e );
		}
		catch (Exception e) {
		    
		    // Only for now:
		    System.out.println( "Recorder.commitData  -- Exception: " 
					+ e.toString());

			throw new Exception( e );
		}
	}	
	
	public synchronized void recordInitialData( SearcherI searcher ) throws Exception {

        StringBuffer optionalThreadLabel =  new StringBuffer();
        
        synchronized( this ) {
            
            StringBuffer tmpStatisticsBuffer = new StringBuffer( BANJO.BUFFERLENGTH_STAT_INTERNAL );
            
    	    try {

                optionalThreadLabel = processData.getOptionalThreadInfo();
                if ( optionalThreadLabel.length() > 0 ) {

                    tmpStatisticsBuffer.append( optionalThreadLabel );
                    tmpStatisticsBuffer.append( " " );
                    tmpStatisticsBuffer.append( BANJO.DATA_SEARCHDATA );
                    tmpStatisticsBuffer.append( BANJO.FEEDBACK_NEWLINE );
                }
                
    	        int lineLength = BANJO.FEEDBACK_LINELENGTH;
                String dashedLine = BANJO.FEEDBACK_DASHEDLINE.substring( 0, lineLength-1 );
        		
    	        // Add the common header for all searchers (including the 'None'
    	        // searcher option)
                tmpStatisticsBuffer.append( dashedLine );
        		
                tmpStatisticsBuffer.append( StringUtil.getBanjoSignature() );
    
                tmpStatisticsBuffer.append( BANJO.FEEDBACK_NEWLINE );
                tmpStatisticsBuffer.append( dashedLine );
        		
                tmpStatisticsBuffer.append( StringUtil.getJobSignature( processData ) );
    
                tmpStatisticsBuffer.append( BANJO.FEEDBACK_NEWLINE );
                tmpStatisticsBuffer.append( dashedLine );
        		
        		
    	        // Add the data for the specific searchers
    	        if ( !( searcher instanceof SearcherSkip ) ) {
    	            
    	            tmpStatisticsBuffer.append( collectFeedback() );
    	        
    		        // Add the report on the observation data, if it's been created
    		        String discretizationReport = processData.getDynamicProcessParameter( 
    		                BANJO.DATA_DISCRETIZATIONREPORT );
    		        if ( discretizationReport != null && !discretizationReport.equals("") ) {
    
                        tmpStatisticsBuffer.append( BANJO.FEEDBACK_NEWLINE );
    		            tmpStatisticsBuffer.append( discretizationReport );
    		        }
    		        
    				tmpStatisticsBuffer.append( BANJO.FEEDBACK_NEWLINE ); 
    
    //                if ( !(BANJO.DEBUG && BANJO.DATA_SEARCHER_OMITTIMEDISPLAY ) ) {
                    if ( !BANJO.DATA_SEARCHER_OMITTIMEDISPLAY ) {
                        
                        tmpStatisticsBuffer.append( BANJO.FEEDBACK_NEWLINE );
                        if (BANJO.DEBUG) tmpStatisticsBuffer.append( strHeader );
                        tmpStatisticsBuffer.append( "Starting search at " );
                        tmpStatisticsBuffer.append( timeFormat.format(new Date()) );       
                        tmpStatisticsBuffer.append( BANJO.FEEDBACK_NEWLINE );
                    }
    	
    				if (BANJO.DEBUG) tmpStatisticsBuffer.append( strHeader );
                    tmpStatisticsBuffer.append( "Prep. time used: ");
                    tmpStatisticsBuffer.append( StringUtil.formatElapsedTime(
                            Long.parseLong( processData.getDynamicProcessParameter(
                            BANJO.DATA_TOTALTIMEFORPREP )), 1, 
                            BANJO.FEEDBACK_OPTION_TIMEFORMAT_MIXED ));       
                    tmpStatisticsBuffer.append( BANJO.FEEDBACK_NEWLINE );
    
                    double tmpScreenReportInterval = Double.parseDouble( 
                            processData.getValidatedProcessParameter( 
                                    BANJO.SETTING_SCREENREPORTINGINTERVAL ) );
    
                    double tmpWriteToFileInterval = Double.parseDouble( 
                            processData.getValidatedProcessParameter( 
                                    BANJO.SETTING_FILEREPORTINGINTERVAL ) );
                    
                    String strScreenReportInterval;
                    // Note: at first glance this code might be desirable, but it will require
                    // some careful redesign of the feedback mechanism
    //                if ( tmpWriteToFileInterval > 0 && 
    //                        tmpWriteToFileInterval < tmpScreenReportInterval ) {
    //                    
    //                    strScreenReportInterval = StringUtil.formatElapsedTime( 
    //                            tmpWriteToFileInterval, 
    //                            BANJO.FEEDBACK_NUMBEROFDECIMALSINTIMEDISPLAY,
    //                            BANJO.FEEDBACK_OPTION_TIMEFORMAT_MIXED ).toString();
    //                }
    //                else {
    //                    
    //                    strScreenReportInterval = StringUtil.formatElapsedTime( 
    //                            tmpScreenReportInterval, 
    //                            BANJO.FEEDBACK_NUMBEROFDECIMALSINTIMEDISPLAY,
    //                            BANJO.FEEDBACK_OPTION_TIMEFORMAT_MIXED ).toString();
    //                }
                    
                    strScreenReportInterval = StringUtil.formatElapsedTime( 
                            tmpScreenReportInterval, 
                            BANJO.FEEDBACK_NUMBEROFDECIMALSINTIMEDISPLAY,
                            BANJO.FEEDBACK_OPTION_TIMEFORMAT_MIXED ).toString();
    
                    tmpStatisticsBuffer.append( "Beginning to search: expect a status report every " +
                            strScreenReportInterval );
    	        }
    	        
    			tmpStatisticsBuffer.append( BANJO.FEEDBACK_NEWLINE );
                statisticsBuffer.append( tmpStatisticsBuffer );
    
                commitData( outputResultsOnly , statisticsBuffer );
                //          commitData( outputToAllFiles , statisticsBuffer );
    
                // Now clear the text buffer that was written to file
                statisticsBuffer =  new StringBuffer( BANJO.BUFFERLENGTH_STAT_INTERNAL );
    	    }
    		catch ( BanjoException e ) {
    		    
    			throw new BanjoException(e);
    		}
    		catch ( Exception e ) {
                
    			throw new BanjoException( e, BANJO.ERROR_BANJO_DEV,
    			        "(Recorder.recordInitialData) " + 
    			        "Error in composing the string for the initial data reporting." );
    		}
        }
	}
	
	protected synchronized StringBuffer collectFeedback() throws Exception {
	    
	    // Collect the common data for a simple report on the start of the search
	    
	    String strSettingChoice;
        StringBuffer searcherFeedback = new StringBuffer();	
        int lineLength = BANJO.FEEDBACK_LINELENGTH;
        String dashedLine = BANJO.FEEDBACK_DASHEDLINE.substring( 0, lineLength-1 ); 
        int filesUsed = 1;

	    // Adding a little extra formatting
		String newLinePlusPrefix = BANJO.FEEDBACK_NEWLINE + BANJO.FEEDBACK_DASH + 
				BANJO.FEEDBACK_SPACE;

		// ------------------------------------
		// Add feedback about user choices
		// ------------------------------------
        
		searcherFeedback.append( StringUtil.formatRightLeftJustified( 
		        newLinePlusPrefix, BANJO.SETTING_CMDARG_SETTINGSFILENAME_DISP, 
		        processData.getDynamicProcessParameter( 
					BANJO.SETTING_CMDARG_SETTINGSFILENAME ), null, lineLength ) );


        searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
        searcherFeedback.append( dashedLine );
        
		// 
		strSettingChoice = processData.getValidatedProcessParameter( 
		        BANJO.SETTING_INPUTDIRECTORY );
		searcherFeedback.append( StringUtil.formatRightLeftJustified( 
		        newLinePlusPrefix, BANJO.SETTING_INPUTDIRECTORY_DISP, 
		        strSettingChoice, null, lineLength ) );
		//
		String observationsFile = processData.getValidatedProcessParameter(
		        BANJO.SETTING_OBSERVATIONSFILE );
	    StringTokenizer tokenizer;
	    int tokenCount;
		tokenizer = new StringTokenizer( observationsFile , 
		        BANJO.DELIMITER_DEFAULT_LIST );
		tokenCount = tokenizer.countTokens();
		if ( observationsFile.indexOf( BANJO.DEFAULT_WILDCARDINDICATOR ) < 0 ) {
		    
			if ( tokenCount > 1 ) {		    
				
				searcherFeedback.append( StringUtil.formatRightLeftJustified( 
				        newLinePlusPrefix, BANJO.SETTING_OBSERVATIONSFILES_DISP, 
				        tokenizer.nextToken(), null, lineLength ) );
				for ( int obsFiles=1; obsFiles < tokenCount; obsFiles++ ) {
				    searcherFeedback.append( StringUtil.formatRightLeftJustified( 
					        newLinePlusPrefix, "", 
					        tokenizer.nextToken(), null, lineLength ) );
				}
			}
			else {
			    
				searcherFeedback.append( StringUtil.formatRightLeftJustified( 
				        newLinePlusPrefix, BANJO.SETTING_OBSERVATIONSFILE_DISP, 
				        observationsFile, null, lineLength ) );
			}
		}
		else {
		    
		    if ( tokenCount > 1 ) {	
		        
				searcherFeedback.append( StringUtil.formatRightLeftJustified( 
				        newLinePlusPrefix, BANJO.SETTING_OBSERVATIONSFILE_WILDCARD_DISP, 
				        tokenizer.nextToken(), null, lineLength ) );
				for ( int obsFiles=1; obsFiles < tokenCount; obsFiles++ ) {
				    searcherFeedback.append( StringUtil.formatRightLeftJustified( 
					        newLinePlusPrefix, "", 
					        tokenizer.nextToken(), 
					        BANJO.DELIMITER_DEFAULT_LIST, lineLength ) );
				}
		    }
		    else {
		        
		        searcherFeedback.append( StringUtil.formatRightLeftJustified( 
				        newLinePlusPrefix, BANJO.SETTING_OBSERVATIONSFILE_WILDCARD_DISP, 
				        observationsFile, null, lineLength ) );
		    }
	        
	        String filesFromWildcardSpec = processData.getDynamicProcessParameter( 
			        BANJO.DATA_WILDCARDOBSERVATIONSFILES );
	        
		    StringTokenizer wildcardTokenizer;
		    int wildcardTokenCount;
		    wildcardTokenizer = new StringTokenizer( filesFromWildcardSpec , 
		            BANJO.DELIMITER_DEFAULT_LIST );
		    wildcardTokenCount = wildcardTokenizer.countTokens(); 
            filesUsed = wildcardTokenCount;
		    searcherFeedback.append( StringUtil.formatRightLeftJustified( 
			        newLinePlusPrefix, BANJO.SETTING_OBSERVATIONSFILES_USED_DISP, 
			        wildcardTokenizer.nextToken(), 
			        BANJO.DELIMITER_DEFAULT_LIST, lineLength ) );
		    
		    while ( wildcardTokenizer.hasMoreTokens() ) {
			    searcherFeedback.append( StringUtil.formatRightLeftJustified( 
				        newLinePlusPrefix, "", wildcardTokenizer.nextToken(), 
				        null, lineLength ) );
			}
		    
		    // List omitted files, if there are any
		    String filesOmittedFromWildcardSpec = 
		        processData.getDynamicProcessParameter( 
			        BANJO.DATA_OMITTEDWILDCARDOBSERVATIONSFILES );
	        
		    if ( filesOmittedFromWildcardSpec != null && 
		            !filesOmittedFromWildcardSpec.equals("") ) {
		        
			    wildcardTokenizer = new StringTokenizer( 
			            filesOmittedFromWildcardSpec, BANJO.DELIMITER_DEFAULT_LIST );
			    wildcardTokenCount = wildcardTokenizer.countTokens();
			        
			    searcherFeedback.append( StringUtil.formatRightLeftJustified( 
				        newLinePlusPrefix, BANJO.SETTING_OBSERVATIONSFILES_OMIT_DISP, 
				        wildcardTokenizer.nextToken(), null, lineLength ) );
			    
			    while ( wildcardTokenizer.hasMoreTokens() ) {
				    searcherFeedback.append( StringUtil.formatRightLeftJustified( 
					        newLinePlusPrefix, "", 
					        wildcardTokenizer.nextToken(), null, lineLength ) );
				}
		    }
		}

		strSettingChoice = processData.getValidatedProcessParameter( 
		        BANJO.SETTING_MINMARKOVLAG );
		int minMarkovLag = Integer.parseInt(strSettingChoice);
		//
		strSettingChoice = processData.getValidatedProcessParameter( 
		        BANJO.SETTING_MAXMARKOVLAG );
		int maxMarkovLag = Integer.parseInt(strSettingChoice);
		
		//
		if ( maxMarkovLag > 0 ) {
            
            strSettingChoice = processData.getDynamicProcessParameter( 
                    BANJO.DATA_OBSERVEDOBSERVATIONROWCOUNT );
            if ( filesUsed > 1 ) {
                
                searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                        newLinePlusPrefix, BANJO.DATA_OBSERVEDOBSERVATIONROWCOUNT_DISP_PL, 
                        strSettingChoice, null, lineLength ) );
            }
            else {
                
                searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                        newLinePlusPrefix, BANJO.DATA_OBSERVEDOBSERVATIONROWCOUNT_DISP, 
                        strSettingChoice, null, lineLength ) );
                
            }

            int specifiedObservationCount = Integer.parseInt( 
                    processData.getDynamicProcessParameter( 
                            BANJO.DATA_SPECIFIEDOBSERVATIONCOUNT ));
            
            int observationRowCount = Integer.parseInt( 
                    processData.getDynamicProcessParameter( 
                            BANJO.DATA_OBSERVEDOBSERVATIONROWCOUNT ));
            
            if ( specifiedObservationCount < observationRowCount ) {
                
                // 
                strSettingChoice = processData.getDynamicProcessParameter( 
                        BANJO.DATA_SPECIFIEDOBSERVATIONCOUNT );
                searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                        newLinePlusPrefix, 
                        BANJO.DATA_SPECIFIEDOBSERVATIONCOUNT_DISP, 
                        strSettingChoice, null, lineLength ) );
            }
            
            strSettingChoice = processData.getDynamicProcessParameter( 
                    BANJO.DATA_OBSERVATIONCOUNT );
            searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                    newLinePlusPrefix, 
                    BANJO.DATA_OBSERVATIONCOUNT_EFFECTIVE_DISP_DBN, 
                    strSettingChoice, null, lineLength ) );
		}
		else {

            int specifiedObservationCount = Integer.parseInt( 
                    processData.getDynamicProcessParameter( 
                            BANJO.DATA_SPECIFIEDOBSERVATIONCOUNT ));
            
            int observedObservationCount = Integer.parseInt( 
                    processData.getDynamicProcessParameter( 
                            BANJO.DATA_OBSERVEDOBSERVATIONCOUNT ));
            
            if ( specifiedObservationCount < observedObservationCount ) {
                
    			strSettingChoice = processData.getDynamicProcessParameter( 
                        BANJO.DATA_OBSERVEDOBSERVATIONCOUNT );
    			searcherFeedback.append( StringUtil.formatRightLeftJustified( 
    			        newLinePlusPrefix, BANJO.DATA_OBSERVEDOBSERVATIONROWCOUNT_DISP, 
    			        strSettingChoice, null, lineLength ) );
                
                strSettingChoice = processData.getDynamicProcessParameter( 
                        BANJO.DATA_OBSERVATIONCOUNT );
                searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                        newLinePlusPrefix, BANJO.DATA_SPECIFIEDOBSERVATIONCOUNT_DISP, 
                        strSettingChoice, null, lineLength ) );
            }
            else {
                
                strSettingChoice = processData.getDynamicProcessParameter( 
                        BANJO.DATA_OBSERVATIONCOUNT );
                searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                        newLinePlusPrefix, BANJO.DATA_OBSERVATIONCOUNT_DISP, 
                        strSettingChoice, null, lineLength ) );
            }
		}

        //
        strSettingChoice = processData.getValidatedProcessParameter( 
                BANJO.SETTING_VARCOUNT );       
        searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_VARCOUNT_DISP, 
                strSettingChoice, null, lineLength ) );

        // May not want to display this, since the user knows it, and there's no way
        // we'll be able to load the data otherwise (unless varCount = obsCount)
        // Are the variables in rows?
        strSettingChoice = processData.getValidatedProcessParameter( 
                BANJO.SETTING_VARIABLESAREINROWS );
        if ( strSettingChoice.equalsIgnoreCase( BANJO.UI_VARIABLESAREINROWS_YES )) {
            
            searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                    newLinePlusPrefix, BANJO.SETTING_VARIABLESAREINROWS_DISP, 
                    strSettingChoice, null, lineLength ) );
        }
        
        // Omit the labels if they are explicitly specified, but let the user know
        // if "inFile" is set
        strSettingChoice = processData.getValidatedProcessParameter( 
                BANJO.SETTING_VARIABLENAMES );
        if ( !strSettingChoice.equalsIgnoreCase( BANJO.UI_VARIABLENAMES_NONE )) {
            
            searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                    newLinePlusPrefix, BANJO.SETTING_VARIABLENAMES_DISP, 
                    strSettingChoice, null, lineLength ) );
        }
        
        // Discretization:
        strSettingChoice = 
            processData.getValidatedProcessParameter( 
                    BANJO.SETTING_DISCRETIZATIONPOLICY );
        
        if ( strSettingChoice != null && strSettingChoice.length() > 0 ) {
            
            searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                    newLinePlusPrefix, BANJO.SETTING_DISCRETIZATIONPOLICY_DISP, 
                    strSettingChoice, null, lineLength ) );
        }
        // 
        strSettingChoice = 
            processData.getValidatedProcessParameter( 
                    BANJO.SETTING_DISCRETIZATIONEXCEPTIONS );
        
        if ( strSettingChoice.equals( BANJO.BANJO_NOVALUESUPPLIED_STRING ) ) {
            
            // We may even decide not to display this when no value was supplied?
            searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                    newLinePlusPrefix, BANJO.SETTING_DISCRETIZATIONEXCEPTIONS_DISP, 
                    BANJO.SETTING_DISCRETIZATIONEXCEPTIONS_NONE_DISP, null, lineLength ) );
        }
        else if ( strSettingChoice != null && strSettingChoice.length() > 0 ) {
            
            searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                    newLinePlusPrefix, BANJO.SETTING_DISCRETIZATIONEXCEPTIONS_DISP, 
                    strSettingChoice, 
                    BANJO.DELIMITER_DEFAULT_LIST, lineLength ) );
        }

        searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
        searcherFeedback.append( dashedLine );

		//
		strSettingChoice = processData.getValidatedProcessParameter( 
		        BANJO.SETTING_INITIALSTRUCTUREFILE );
		searcherFeedback.append( StringUtil.formatRightLeftJustified( 
		        newLinePlusPrefix, BANJO.SETTING_INITIALSTRUCTUREFILE_DISP, 
		        strSettingChoice, null, lineLength ) );
		//
		strSettingChoice = processData.getValidatedProcessParameter( 
		        BANJO.SETTING_MUSTBEPRESENTEDGESFILE );
		searcherFeedback.append( StringUtil.formatRightLeftJustified( 
		        newLinePlusPrefix, BANJO.SETTING_MUSTBEPRESENTEDGESFILE_DISP, 
		        strSettingChoice, null, lineLength ) );
		//
		strSettingChoice = processData.getValidatedProcessParameter( 
		        BANJO.SETTING_MUSTNOTBEPRESENTEDGESFILE );
		searcherFeedback.append( StringUtil.formatRightLeftJustified( 
		        newLinePlusPrefix, BANJO.SETTING_MUSTNOTBEPRESENTEDGESFILE_DISP, 
		        strSettingChoice, null, lineLength ) );
		// 
		searcherFeedback.append( StringUtil.formatRightLeftJustified( 
		        newLinePlusPrefix, BANJO.SETTING_MINMARKOVLAG_DISP, 
		        Integer.toString( minMarkovLag ), null, lineLength ) );
		//
		searcherFeedback.append( StringUtil.formatRightLeftJustified( 
		        newLinePlusPrefix, BANJO.SETTING_MAXMARKOVLAG_DISP, 
		        Integer.toString( maxMarkovLag ), null, lineLength ) );
		
		//
		// Only display the mandatory lag info when it's relevant
		if ( maxMarkovLag > 0 ) {

			strSettingChoice = processData.getValidatedProcessParameter( 
			        BANJO.SETTING_DBNMANDATORYIDENTITYLAGS );
			
			if ( strSettingChoice.equals( BANJO.BANJO_NOVALUESUPPLIED_STRING ) ) {
			
			    strSettingChoice = BANJO.SETTING_DBNMANDATORYIDENTITYLAGS_NONE;
			}
			searcherFeedback.append( StringUtil.formatRightLeftJustified( 
			        newLinePlusPrefix, BANJO.SETTING_DBNMANDATORYIDENTITYLAGS_DISP, 
			        strSettingChoice, null, lineLength ) );
		}
        //
        strSettingChoice = processData.getValidatedProcessParameter( 
                BANJO.SETTING_MAXPARENTCOUNT );
        searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_MAXPARENTCOUNT_DISP, 
                strSettingChoice, null, lineLength ) );
		//
		strSettingChoice = processData.getValidatedProcessParameter( 
		        BANJO.SETTING_EQUIVALENTSAMPLESIZE );
		searcherFeedback.append( StringUtil.formatRightLeftJustified( 
		        newLinePlusPrefix, BANJO.SETTING_EQUIVALENTSAMPLESIZE_DISP, 
		        strSettingChoice, null, lineLength ) );
        //

        searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
        searcherFeedback.append( dashedLine );
        
        searcherFeedback.append( processData.getDynamicProcessParameter( 
                BANJO.DATA_SEARCHERINFO_COREOBJECTS ));

        searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
        searcherFeedback.append( dashedLine );
        
        strSettingChoice = processData.getValidatedProcessParameter( 
                BANJO.SETTING_PRECOMPUTE );
        searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_PRECOMPUTE_DISP, 
                strSettingChoice, null, lineLength ) );
        //
        strSettingChoice = processData.getValidatedProcessParameter( 
                BANJO.SETTING_USECACHE );
        searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_USECACHE_DISP, 
                strSettingChoice, null, lineLength ) );

        //
        String cycleCheckingChoice = processData.getDynamicProcessParameter( 
                BANJO.CONFIG_CYCLECHECKER_METHOD );
        if ( cycleCheckingChoice.equalsIgnoreCase( BANJO.DATA_CYCLECHECKING_DFS )) {
            
            strSettingChoice = BANJO.DATA_CYCLECHECKING_DFS_DISP;
        }
        else if ( cycleCheckingChoice.equalsIgnoreCase( BANJO.DATA_CYCLECHECKING_SHMUELI ) ) {

            strSettingChoice = BANJO.DATA_CYCLECHECKING_SHMUELI_DISP;
        }
        else if ( cycleCheckingChoice.equalsIgnoreCase( BANJO.DATA_CYCLECHECKING_DFSORIG ) ) {

            strSettingChoice = BANJO.DATA_CYCLECHECKING_DFSORIG_DISP;
        }
        else if ( cycleCheckingChoice.equalsIgnoreCase( BANJO.UI_CYCLECHECKER_BFS ) ) {

            strSettingChoice = BANJO.DATA_CYCLECHECKING_BFS_DISP;
        }
        else {

            // default choice
            strSettingChoice = BANJO.DATA_CYCLECHECKING_SHMUELI_DISP;
        }
        
        searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.CONFIG_CYCLECHECKINGMETHOD_DISP, 
                strSettingChoice, null, lineLength ) );

        searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
        searcherFeedback.append( dashedLine );
        
        searcherFeedback.append( processData.getDynamicProcessParameter( 
                BANJO.DATA_SEARCHERINFO_SPECIFICSEARCHER ));

        searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
        searcherFeedback.append( dashedLine );
        
		// 
		strSettingChoice = processData.getValidatedProcessParameter( 
		        BANJO.SETTING_OUTPUTDIRECTORY );
		searcherFeedback.append( StringUtil.formatRightLeftJustified( 
		        newLinePlusPrefix, BANJO.SETTING_OUTPUTDIRECTORY_DISP, 
		        strSettingChoice, null, lineLength ) );
		// 
        strSettingChoice = processData.getDynamicProcessParameter( 
                BANJO.DATA_REPORTFILE );
//        strSettingChoice = processData.getValidatedProcessParameter( 
//                BANJO.SETTING_REPORTFILE );
		searcherFeedback.append( StringUtil.formatRightLeftJustified( 
		        newLinePlusPrefix, BANJO.SETTING_REPORTFILE_DISP, 
		        strSettingChoice, null, lineLength ) );

        searcherFeedback.append( processData.getDynamicProcessParameter( 
                BANJO.DATA_SEARCHERINFO_FEEDBACK_1 ) );
        
        // Convention: for the next three settings, we omit their value in
        // the report display, if no value was originally provided
        strSettingChoice = processData.getValidatedProcessParameter( 
                BANJO.SETTING_MAXSEARCHTIME );
        if ( !strSettingChoice.equals( 
                Integer.toString( BANJO.BANJO_NOVALUESUPPLIED_NUMBER ))) {

            int numberOfDecimals = BANJO.FEEDBACK_NUMBEROFDECIMALSINTIMEDISPLAY;
            searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, 
                BANJO.SETTING_MAXSEARCHTIME_DESCR + BANJO.FEEDBACK_COLON, 
                StringUtil.formatElapsedTime( 
                        Integer.parseInt( strSettingChoice ), numberOfDecimals,
                        BANJO.FEEDBACK_OPTION_TIMEFORMAT_MIXED ).toString(), 
                        null, lineLength ) );
        }
        //
        strSettingChoice = processData.getValidatedProcessParameter( 
                BANJO.SETTING_MAXPROPOSEDNETWORKS );
        if ( !strSettingChoice.equals( 
                Integer.toString( BANJO.BANJO_NOVALUESUPPLIED_NUMBER ))) {
        
            searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_MAXPROPOSEDNETWORKS_DISP, 
                strSettingChoice, null, lineLength ) );
        }
        //
        strSettingChoice = processData.getValidatedProcessParameter( 
                BANJO.SETTING_MAXRESTARTS );
        if ( !strSettingChoice.equals( 
                Integer.toString( BANJO.BANJO_NOVALUESUPPLIED_NUMBER ))) {
            
            searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_MAXRESTARTS_DISP, 
                strSettingChoice, null, lineLength ) );
        }
        //
        strSettingChoice = processData.getValidatedProcessParameter( 
                BANJO.SETTING_MINNETWORKSBEFORECHECKING );
        searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_MINNETWORKSBEFORECHECKING_DISP, 
                strSettingChoice, null, lineLength ) ); 
        

        searcherFeedback.append( processData.getDynamicProcessParameter( 
                BANJO.DATA_SEARCHERINFO_FEEDBACK_2 ) );

        searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
        searcherFeedback.append( dashedLine );
        
		// Post-processing options:
		strSettingChoice = 
	        processData.getValidatedProcessParameter( 
	                BANJO.SETTING_COMPUTEINFLUENCESCORES );
		if ( strSettingChoice != null && strSettingChoice.length() > 0 &&
		        !( strSettingChoice.equalsIgnoreCase( BANJO.BANJO_NOVALUESUPPLIED_STRING ) ) ) {

			searcherFeedback.append( StringUtil.formatRightLeftJustified( 
			        newLinePlusPrefix, BANJO.SETTING_COMPUTEINFLUENCESCORES_DISP, 
			        strSettingChoice, null, lineLength ) );
		}
		//
		strSettingChoice = 
	        processData.getValidatedProcessParameter( 
	                BANJO.SETTING_COMPUTECONSENSUSGRAPH );
		if ( strSettingChoice != null && strSettingChoice.length() > 0 &&
		        !( strSettingChoice.equalsIgnoreCase( BANJO.BANJO_NOVALUESUPPLIED_STRING ) ) ) {
		    
			searcherFeedback.append( StringUtil.formatRightLeftJustified( 
			        newLinePlusPrefix, BANJO.SETTING_COMPUTECONSENSUSGRAPH_DISP, 
			        strSettingChoice, null, lineLength ) );
		}
		//
		strSettingChoice = 
	        processData.getValidatedProcessParameter( 
	                BANJO.SETTING_DISPLAYCONSENSUSGRAPHASHTML );
		if ( strSettingChoice != null && strSettingChoice.length() > 0 &&
		        !( strSettingChoice.equalsIgnoreCase( BANJO.BANJO_NOVALUESUPPLIED_STRING ) ) ) {
		    
			searcherFeedback.append( StringUtil.formatRightLeftJustified( 
			        newLinePlusPrefix, BANJO.SETTING_DISPLAYCONSENSUSGRAPHASHTML_DISP, 
			        strSettingChoice, null, lineLength ) );
		}
        //
        strSettingChoice = 
            processData.getValidatedProcessParameter( 
                    BANJO.SETTING_CREATEDOTOUTPUT );
        if ( strSettingChoice != null && strSettingChoice.length() > 0 &&
                !( strSettingChoice.equalsIgnoreCase( BANJO.BANJO_NOVALUESUPPLIED_STRING ) ) ) {

            searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                    newLinePlusPrefix, BANJO.SETTING_CREATEDOTOUTPUT_DISP, 
                    strSettingChoice, null, lineLength ) );
        }
        //
        strSettingChoice = 
            processData.getValidatedProcessParameter( 
                    BANJO.SETTING_FULLPATHTODOTEXECUTABLE );
        if ( strSettingChoice != null && strSettingChoice.length() > 0 &&
                processData.isSettingValueValid( BANJO.SETTING_FULLPATHTODOTEXECUTABLE ) ) {
            
            searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                    newLinePlusPrefix, BANJO.SETTING_FULLPATHTODOTEXECUTABLE_DISP, 
                    strSettingChoice, null, lineLength ) );
        }
        else {
            
            searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                    newLinePlusPrefix, BANJO.SETTING_FULLPATHTODOTEXECUTABLE_DISP, 
                    BANJO.UI_FULLPATHTODOTEXECUTABLE_NOTSUPPLIED, null, lineLength ) );
        }
        
        // Add the (optional) XML-related input/output
        searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
        searcherFeedback.append( dashedLine );
        strSettingChoice = 
            processData.getValidatedProcessParameter( 
                    BANJO.SETTING_XMLINPUTFILES );
        if ( strSettingChoice != null && strSettingChoice.length() > 0 &&
                processData.isSettingValueValid( BANJO.SETTING_XMLINPUTFILES ) ) {

            searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                    newLinePlusPrefix, BANJO.SETTING_XMLINPUTFILES_DISP, 
                    strSettingChoice, null, lineLength ) );
                
            strSettingChoice = BANJO.SETTING_XMLINPUTDIRECTORY;
            searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                        newLinePlusPrefix, BANJO.SETTING_XMLINPUTDIRECTORY_DISP, 
                        strSettingChoice, null, lineLength ) );
        }
        else {

            strSettingChoice = 
                processData.getValidatedProcessParameter( 
                        BANJO.SETTING_XMLOUTPUTDIRECTORY );
            searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                    newLinePlusPrefix, BANJO.SETTING_XMLOUTPUTDIRECTORY_DISP, 
                    strSettingChoice, null, lineLength ) );

            strSettingChoice = 
                processData.getValidatedProcessParameter( 
                        BANJO.SETTING_XMLREPORTFILE );
            searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                    newLinePlusPrefix, BANJO.SETTING_XMLREPORTFILE_DISP, 
                    strSettingChoice, null, lineLength ) );

            strSettingChoice = 
                processData.getValidatedProcessParameter( 
                        BANJO.SETTING_XMLSETTINGSTOEXPORT );
            searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                    newLinePlusPrefix, BANJO.SETTING_XMLSETTINGSTOEXPORT_DISP, 
                    strSettingChoice, null, lineLength ) );
        }
            
        searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.DATA_XMLPARSER_DISP, 
                BANJO.DATA_XMLPARSERCHOICE, null, lineLength ) );
        
        searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.DATA_BANJOXMLFORMATVERSION_DISP, 
                BANJO.DATA_BANJOXMLFORMATVERSION, null, lineLength ) );

        // Add the seed value for the random sequence
        searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
        searcherFeedback.append( dashedLine );
        strSettingChoice = 
            processData.getValidatedProcessParameter( 
                    BANJO.DATA_BANJOSEED );
        // Here we grab the seed that is currently in use by BanjoRandomNumber
        searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_BANJOSEED_DISP, 
                Long.toString( processData.getBanjoSeed() ), null, lineLength ) );

        // Add the number of threads
        searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
        searcherFeedback.append( dashedLine );
        strSettingChoice = 
            processData.getValidatedProcessParameter( 
                    BANJO.SETTING_THREADS );
        int numberOfThreads = new Integer( strSettingChoice ).intValue();
        searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_THREADS_DISP, 
                strSettingChoice, null, lineLength ) );
        // get the threadIndex
        strSettingChoice = processData.getDynamicProcessParameter( BANJO.DATA_THREADINDEX );
        if ( numberOfThreads > 1 ) {
        
            // Note that we add 1 to each thread index (for the user), to have the index
            // go from 1 to numberOfThreads
            searcherFeedback.append( StringUtil.formatRightLeftJustified( 
                    newLinePlusPrefix, BANJO.DATA_THREADINDEX_DISP, 
                    Integer.toString( new Integer( strSettingChoice ).intValue() + 1 ), 
                    null, lineLength ) );
        }
               
        // Check if the user instructs us to dispplay the structures (initial, must-be-present,
        // and must-be-absent)
        if ( processData.getValidatedProcessParameter( 
                BANJO.SETTING_DISPLAYSTRUCTURES ).equals( BANJO.UI_DISPLAYSTRUCTURES_YES ) ) {
        
            // Only display if a structure was provided
            
    		if ( processData.getDynamicProcessParameter( 
    		        BANJO.DATA_INITIALSTRUCTURE ) != null ) {

                searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
                searcherFeedback.append( dashedLine );
                
    			searcherFeedback.append( StringUtil.formatRightLeftJustified( 
    			        newLinePlusPrefix, BANJO.DATA_INITIALSTRUCTURE_DISP, 
    			        "", null, lineLength ) );


                searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
                searcherFeedback.append( dashedLine );
    			searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
    			
    			searcherFeedback.append( processData.getDynamicProcessParameter( 
    			        BANJO.DATA_INITIALSTRUCTURE ));
    		}
    
    		if ( processData.getDynamicProcessParameter( 
    		        BANJO.DATA_MUSTBEPRESENTPARENTS ) != null ) {

                searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
                searcherFeedback.append( dashedLine );
    		    
        		searcherFeedback.append( StringUtil.formatRightLeftJustified( 
        		        newLinePlusPrefix, BANJO.DATA_MUSTBEPRESENTPARENTS_DISP, 
        		        "", null, lineLength ) );

                searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
                searcherFeedback.append( dashedLine );
        		searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
        		
        		searcherFeedback.append( processData.getDynamicProcessParameter( 
        		        BANJO.DATA_MUSTBEPRESENTPARENTS ));
    		}
    		
    		if ( processData.getDynamicProcessParameter( 
    		        BANJO.DATA_MUSTBEABSENTPARENTS ) != null ) {
    
                searcherFeedback.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
                        substring( 0, lineLength ));
    			
    			searcherFeedback.append( StringUtil.formatRightLeftJustified( 
    			        newLinePlusPrefix, BANJO.DATA_MUSTBEABSENTPARENTS_DISP, 
    			        "", null, lineLength ) );

                searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
                searcherFeedback.append( dashedLine );
    			searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
    			
    			searcherFeedback.append( processData.getDynamicProcessParameter( 
    			        BANJO.DATA_MUSTBEABSENTPARENTS ));
    		}
        }
		
		// Record any warnings
		if ( processData.getCollectedWarnings().size() > 0 ) {

            searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
            searcherFeedback.append( dashedLine );
			
			searcherFeedback.append( StringUtil.formatRightLeftJustified( 
			        newLinePlusPrefix, "Warnings:", 
			        "", null, lineLength ) );

            searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
            searcherFeedback.append( dashedLine );
			searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
			
			searcherFeedback.append( processData.compileWarningMessages() );
		}

        searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
        searcherFeedback.append( dashedLine );
        

        String displayMemoryInfo = processData.getValidatedProcessParameter(
                BANJO.SETTING_DISPLAYMEMORYINFO );

        if ( displayMemoryInfo.equals( BANJO.UI_DISPLAYMEMORYINFO_YES ) ) {

            searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
            searcherFeedback.append( BANJO.FEEDBACK_NEWLINE );
            searcherFeedback.append( 
                    StringUtil.compileMemoryInfo( 
                            BANJO.DATA_MEMORYINFOBEFORESTART ) );
        }
		
	    return searcherFeedback;
	}
	
	
	/* (non-Javadoc)
	 * @see edu.duke.cs.banjo.data.RecorderI
	 * 		#recordFinalData(edu.duke.cs.banjo.learner.SearcherI)
	 */
	public synchronized void recordFinalData(SearcherI searcher) throws Exception {
        // nothing to do
	};
	
	/* (non-Javadoc)
	 * @see edu.duke.cs.banjo.data.RecorderI
	 * 		#recordRecurringData(edu.duke.cs.banjo.learner.SearcherI)
	 */
	public abstract void recordRecurringData(SearcherI searcher) throws Exception;
	/* (non-Javadoc)
	 * @see edu.duke.cs.banjo.data.RecorderI
	 * 		#recordSpecifiedData(java.lang.String)
	 */
	public abstract void recordSpecifiedData(StringBuffer dataToRecord) throws Exception;
}
