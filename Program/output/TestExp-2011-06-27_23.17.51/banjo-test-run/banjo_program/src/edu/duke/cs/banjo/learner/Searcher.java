/*
 * Created on Apr 14, 2004
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
package edu.duke.cs.banjo.learner;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Pattern;

import edu.duke.cs.banjo.bayesnet.*;
//import edu.duke.cs.banjo.bayesnet.BayesNetChangeI;
//import edu.duke.cs.banjo.bayesnet.BayesNetManagerI;
//import edu.duke.cs.banjo.bayesnet.BayesNetStructureI;
import edu.duke.cs.banjo.data.settings.SettingItem;
import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.learner.components.*;
import edu.duke.cs.banjo.utility.BANJO;
import edu.duke.cs.banjo.utility.BanjoException;
import edu.duke.cs.banjo.utility.BanjoError;
import edu.duke.cs.banjo.utility.StringUtil;

/**
 * Combines common code shared by the different searcher implementations. 
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Apr 14, 2004
 * <p>
 * 10/18/2005 (v2.0) hjs
 *		Scope change of several variables.<br>
 *		Code cleanup as part of initial setting up of version 1.5.
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public abstract class Searcher implements SearcherI {

	// --------------------------------------------------------------------
	// Common functionality that every derived Searcher subclass will share
	// --------------------------------------------------------------------
	
	// Core "conceptual" (high-level) objects used in every search
    protected ProposerI proposer;
    protected EvaluatorI evaluator;
    protected DeciderI decider;				
    protected CycleCheckerI cycleChecker;

	protected SearchExecuter searchExecuter;
    protected SearchTerminator searchTerminator;
    protected HighScoreSetUpdater highScoreSetUpdater;
	protected SearchTerminator terminator[] = new SearchTerminator[3];
	
    protected final int varCount;
    protected final int minMarkovLag;
    protected final int maxMarkovLag;

	// Every Searcher will need to keep track of the total number
	// of graphs visited:
	protected long networksVisitedGlobalCounter;
	protected long networksVisitedInnerLoopCounter;
	protected long maxNetworksVisitedInInnerLoop;
	protected final double unreachableScore = BANJO.BANJO_UNREACHABLESCORE_BDE;
	
	// Variables related to the main search loop (didn't make them final
	// in case a subclass wants to take a more dynamic approach to their management)
    protected long maxSearchTime;
    protected long maxSearchLoops;
    protected long maxRestarts;
	protected long restartCount;

	// Tracking the numbers where the restarts take place (not of general interest
	// so generally it isn't displayed)
	protected StringBuffer restartsAtCounts;	
		
	// Objects that won't change once they've been created, and that are
	// used by every subclass:
	// Containers for the search process related data
	protected Settings processData;
	
	// "Global" statistics collector for use in searcher subclasses
    protected volatile RecorderI searcherStatistics;
//    protected final RecorderI searcherStatistics;
	
	// Equivalence checking for tracking of n-best networks
	protected final EquivalenceCheckerI equivalenceChecker;
	// Variable used in the equivalence checking code
	protected boolean isEquivalent;
	
	// String container for collecting info about the process
	protected volatile StringBuffer searcherStats;
	
	// Core objects that provide the underpinning of the search
	// (Note: these objects will generally change over the course of the search)
	protected BayesNetManagerI bayesNetManager;
	protected BayesNetChangeI suggestedBayesNetChange;
	protected double currentBestScoreSinceRestart;
	protected BayesNetStructureI highScoreStructureSinceRestart;
	
	// Objects and variables used to maintain the constantly sorted set 
	// of high scoring networks ("n-best" networks)
	protected volatile TreeSet highScoreStructureSet;
    protected volatile TreeSet nonEquivalentHighScoreStructureSet;
	protected long nBestCount;
	protected final long nBestMax;
	protected double nBestThresholdScore;
	
	// Maintain a list of bayesNetChange objects used for search strategies
	// based on proposers that generate more than a single change at the time
	// (Typically, a search that considers all possible single moves per time
	// step, etc)
	List suggestedChangeList;
	
	// 10/18/2005 (v2.0) hjs
	//		Simplify use to utility variables (scope change; subsequent use
	//		in inner classes)
	//
	// Utility variables for displaying feedback at selected intervals
	protected int percentLevel;
	protected boolean displayFeedbackByTime;
	protected final double feedbackTimeDelta; // contains time in milliseconds
	protected double nextFeedbackTime;	// Write out intermediate results to file 
										//(e.g., the best network so far)
	protected final boolean trackIntermediateResults;
    
    protected final double screenReportTimeDelta; // contains time in milliseconds
    protected double nextScreenReportTime;
    protected final double screenReportInterval;
    protected final double fileReportTimeDelta; // contains time in milliseconds
    protected double nextFileReportTime;
    protected final double fileReportInterval;
    
	protected String displayMemoryInfo;
	
	// Utility variables for tracking the elapsed time
	protected volatile long startTime;
	protected volatile long elapsedTime;
	protected volatile long intermediateTime;
	protected DateFormat timeFormat;

	// private variables, declared here so we don't have to allocate
	// their memory in a frequent loop
	protected long estimatedTime;
	protected int numberOfDecimals = BANJO.FEEDBACK_NUMBEROFDECIMALSINTIMEDISPLAY;
	protected int percentPaddingLength = BANJO.FEEDBACK_PERCENTPADDINGLENGTH;
	protected int timePaddingLength = BANJO.FEEDBACK_TIMEPADDINGLENGTH;
	protected int loopPaddingLength;
	protected int restartPaddingLength;

	protected String newLinePlusPrefix = BANJO.FEEDBACK_NEWLINE + 
			BANJO.FEEDBACK_DASH + BANJO.FEEDBACK_SPACE;
	protected String linePostfix = BANJO.FEEDBACK_SPACE + BANJO.FEEDBACK_DASH;
	protected String prefix = BANJO.FEEDBACK_DASH;
	protected int lineLength = BANJO.FEEDBACK_LINELENGTH;
    
    protected String feedbackRestarts = new String( BANJO.FEEDBACKSTRING_RESTARTS );
    
	protected final boolean restartWithRandomNetwork;
    
    protected StringBuffer optionalThreadLabel =  new StringBuffer();
	
	/**
     * Base class of inner classes for executing a search.
     */
	protected abstract class SearchExecuter {
		
		protected abstract void executeSearch() throws Exception;
		protected abstract int sizeOfListOfChanges();
	}

    /**
     * Base class of inner classes for terminating a search.
     */
    protected abstract class SearchTerminator {
        
        protected double displayPercent;
        
        protected abstract boolean checkTerminationCondition() throws Exception;
    
        protected StringBuffer generateFeedback() throws Exception {
            
            return feedbackBasedOnTime();
        }
    }

    /**
     * Subclass class of inner classes for terminating a search using possibly multiple criteria.
     */
    protected abstract class SearchMultipleTerminator extends SearchTerminator {
                
        protected abstract boolean checkTerminationCondition() throws Exception;

        // Default feedback: first the summary info, appended by the intermediate results
        protected StringBuffer generateFeedback() throws Exception {

            return super.generateFeedback().append( 
                    trackIntermediateResults() );
        }
    }

	/**
     * Base class of inner classes for updating the score info for a search.
     */
	protected abstract class HighScoreSetUpdater {
		
	    protected abstract void updateHighScoreStructureData( double bayesNetScore ) 
			throws Exception;
	}

	/**
	 * Constructor for instantiating the objects shared between searcher
	 * implementations.
	 * 
	 * @param _processData The special container for the initial, validated, and
	 * dynamically updated settings.
	 */
	public Searcher( Settings _processData ) throws Exception {
		
		String strSettingChoice;
		searcherStats = new StringBuffer( BANJO.BUFFERLENGTH_STAT_INTERNAL );

		// We want to link our internal processData container to the one that is
	    // being passed in to have convenient access to the dynamic process data
	    // from within various methods (mainly for the Searcher subclasses).
	    this.processData = _processData;
        
        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println( new StringBuffer( StringUtil.compileMemoryInfo( 
                    BANJO.FEEDBACK_NEWLINE + BANJO.DATA_TRACEMEMORYSEARCHERSTART )) );
        }
        
		// Start the timer for the prep time
		startTime = System.currentTimeMillis();
		
		boolean isDataValid = validateRequiredData();
		
		// We check if there were any problems. If we found any, we cannot continue
		// setting up the searcher.
		if ( !isDataValid ) {
		    
		    throw new BanjoException( BANJO.ERROR_CHECKPOINTTRIGGER, 
		            processData.compileErrorMessages().toString() );
		}

		// 
		elapsedTime = System.currentTimeMillis() - startTime;
		processData.setDynamicProcessParameter ( 
		        BANJO.DATA_TOTALTIMEFORPREP, Long.toString( elapsedTime ) );

        maxRestarts = Long.parseLong( 
                processData.getValidatedProcessParameter(
                BANJO.SETTING_MAXRESTARTS ) );
        maxSearchTime = Long.parseLong( 
                processData.getValidatedProcessParameter( 
                BANJO.SETTING_MAXSEARCHTIME ));
        maxSearchLoops = Long.parseLong( 
                processData.getValidatedProcessParameter(
                        BANJO.SETTING_MAXPROPOSEDNETWORKS ) );
		maxNetworksVisitedInInnerLoop = Long.parseLong( 
		        processData.getValidatedProcessParameter( 
		                BANJO.SETTING_MINNETWORKSBEFORECHECKING ) );
        
        if ( maxSearchLoops > 0 && maxNetworksVisitedInInnerLoop > maxSearchLoops ) {
            
            maxNetworksVisitedInInnerLoop = maxSearchLoops;
        }

		// Inner search loops can't be larger than max search loops; In addition, the
		// following rules apply: When restarts are specified >=0, then we cannot set the
		// inner loop limit to 0 (else we won't ever move to another network); Note that
		// when maxRestarts=0, we want to go through "1 complete search" (until we reach the
		// restart point), whereas when maxSearchTime=0, we want to stop immediately.
		// Also if maxSearchTime=0 or maxSearchLoops=0 (i.e., they are specified), then
		// we don't want to do any searching.
        
        if ( ( maxRestarts < 0 && maxSearchTime <= 0 && maxSearchLoops <= 0 ) 
             || maxSearchTime == 0
             || maxSearchLoops == 0
             || maxNetworksVisitedInInnerLoop < 0 ) {
            
			maxNetworksVisitedInInnerLoop = 0;
		}
		
	    restartWithRandomNetwork = _processData.getValidatedProcessParameter( 
		        BANJO.SETTING_RESTARTWITHRANDOMNETWORK ).
		        equalsIgnoreCase( BANJO.UI_RESTARTWITHRANDOMNETWORK_YES );
	    
		//timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
		timeFormat = DateFormat.getDateTimeInstance( DateFormat.SHORT, 
		        DateFormat.MEDIUM );
		
		//
		strSettingChoice = _processData.getValidatedProcessParameter( 
		        BANJO.SETTING_VARCOUNT );
		varCount = Integer.parseInt(strSettingChoice);
		// 
		strSettingChoice = _processData.getValidatedProcessParameter( 
		        BANJO.SETTING_MINMARKOVLAG );
		minMarkovLag = Integer.parseInt(strSettingChoice);
		//
		strSettingChoice = _processData.getValidatedProcessParameter( 
		        BANJO.SETTING_MAXMARKOVLAG );
		maxMarkovLag = Integer.parseInt(strSettingChoice);

		//
		strSettingChoice = _processData.getValidatedProcessParameter( 
		        BANJO.SETTING_MAXPROPOSEDNETWORKS );
		maxSearchLoops = Long.parseLong(strSettingChoice);
		//
		strSettingChoice = _processData.getValidatedProcessParameter( 
		        BANJO.SETTING_MAXRESTARTS );
		maxRestarts = Long.parseLong(strSettingChoice);
		
		
        strSettingChoice =  processData.getValidatedProcessParameter(
		        BANJO.SETTING_NBEST );
		searcherStats.append( StringUtil.formatRightLeftJustified( 
		        newLinePlusPrefix, BANJO.SETTING_NBEST_DISP, 
		        strSettingChoice, null, lineLength ) );
        nBestMax = Long.parseLong( strSettingChoice );
		nBestCount = 1;
        
        // Only display the 'best networks are' property for n-best > 1
        if ( nBestMax > 1 ) {
        
            strSettingChoice = processData.getValidatedProcessParameter(
                    BANJO.SETTING_BESTNETWORKSARE );
            searcherStats.append( StringUtil.formatRightLeftJustified( 
                    newLinePlusPrefix, BANJO.SETTING_BESTNETWORKSARE_DISP, 
                    strSettingChoice, null, lineLength ) );
        }
        
        processData.setDynamicProcessParameter( 
                BANJO.DATA_SEARCHERINFO_FEEDBACK_1, searcherStats.toString() );

        searcherStats = new StringBuffer( BANJO.BUFFERLENGTH_STAT_INTERNAL );
        
        // Set the interval for saving intermediate results to file
        double tmpScreenReportInterval = Double.parseDouble( 
                processData.getValidatedProcessParameter( 
                        BANJO.SETTING_SCREENREPORTINGINTERVAL ) );
        if ( tmpScreenReportInterval <= 0 ) { 
            
            screenReportInterval = 0; 
            // Default to a reasonable number:
            screenReportTimeDelta = BANJO.DEFAULT_SCREENREPORTINGINTERVAL;
            nextScreenReportTime = screenReportTimeDelta;
            
            strSettingChoice = BANJO.BANJO_NOVALUE_INDISPLAY;
        }
        else {
            
            // screenReportInterval is supplied in minutes, but internally
            // we use seconds
            screenReportInterval = tmpScreenReportInterval;
            screenReportTimeDelta = screenReportInterval;
            nextScreenReportTime = screenReportTimeDelta;
            
            // Format the display string
            strSettingChoice = StringUtil.formatElapsedTime( 
                    screenReportInterval, 
                    BANJO.FEEDBACK_NUMBEROFDECIMALSINTIMEDISPLAY,
                    BANJO.FEEDBACK_OPTION_TIMEFORMAT_MIXED ).toString();
        }
        

        nextFeedbackTime = nextScreenReportTime;
        feedbackTimeDelta = screenReportTimeDelta;
        
        searcherStats.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_SCREENREPORTINGINTERVAL_DISP,
                strSettingChoice, null, lineLength ) );
        
        // Set the interval for saving intermediate results to file
        double tmpWriteToFileInterval = Double.parseDouble( 
                processData.getValidatedProcessParameter( 
                        BANJO.SETTING_FILEREPORTINGINTERVAL ) );
        if ( tmpWriteToFileInterval <= 0 ) {

            fileReportInterval = 0; 
            trackIntermediateResults = false;
            // Default to a reasonable number:
            fileReportTimeDelta = BANJO.DEFAULT_FILEREPORTINGINTERVAL;
            nextFileReportTime = fileReportTimeDelta;
            
            strSettingChoice = BANJO.BANJO_NOVALUE_INDISPLAY;
        }
        else {
            
            // fileReportInterval is supplied in minutes, but internally
            // we use seconds
            fileReportInterval = tmpWriteToFileInterval;
            trackIntermediateResults = true;
            fileReportTimeDelta = fileReportInterval;
            nextFileReportTime = fileReportTimeDelta;
            
            // Format the display string
            strSettingChoice = StringUtil.formatElapsedTime( 
                    fileReportInterval, 
                    BANJO.FEEDBACK_NUMBEROFDECIMALSINTIMEDISPLAY,
                    BANJO.FEEDBACK_OPTION_TIMEFORMAT_MIXED ).toString();
        }
		
		searcherStats.append( StringUtil.formatRightLeftJustified( 
		        newLinePlusPrefix, BANJO.SETTING_FILEREPORTINGINTERVAL_DISP,
		        strSettingChoice, null, lineLength ) );
		
		processData.setDynamicProcessParameter( 
				BANJO.DATA_SEARCHERINFO_FEEDBACK_2, searcherStats.toString() );
	
		
		// Internally used variables: track the number of networks visited, and 
        // the number of restarts
		networksVisitedGlobalCounter = 1;
		restartCount = 0;
		restartsAtCounts = new StringBuffer( BANJO.BUFFERLENGTH_STAT );
		restartsAtCounts.append( 0 );
				
		// Set the user-selected statistics collector
		String strStatisticsChoice;
		strStatisticsChoice = _processData.getValidatedProcessParameter( 
		        BANJO.SETTING_STATISTICSCHOICE );	
		strSettingChoice = "";
		if ( strStatisticsChoice == BANJO.UI_RECORDER_STANDARD ) { 
			
			this.searcherStatistics = new RecorderStandard( _processData );
		}
		else {

			// Default case:
			this.searcherStatistics = new RecorderStandard( _processData );
			strSettingChoice = BANJO.UI_DEFAULTEDTO_DISP;
		}
		strSettingChoice = strSettingChoice + 
				StringUtil.getClassName( searcherStatistics );
		
		processData.setDynamicProcessParameter( 
				BANJO.DATA_SEARCHERINFO_STATISTICS, 
				StringUtil.formatRightLeftJustified(
				        newLinePlusPrefix, BANJO.SETTING_STATISTICSCHOICE_DISP,
				        strSettingChoice, null, lineLength ).toString() );

		loopPaddingLength = (( new Long( maxSearchLoops ) ).toString() ).length();
		restartPaddingLength = (( new Long( maxRestarts ) ).toString() ).length();

        if ( processData.getValidatedProcessParameter( BANJO.SETTING_BESTNETWORKSARE )
                .equals( BANJO.UI_BESTNETWORKSARE_NONEQUIVALENT ) ) {

            equivalenceChecker = new EquivalenceCheckerBasic( processData );
        }
        else {

            equivalenceChecker = new EquivalenceCheckerSkip( processData );
        }
        
        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println( new StringBuffer( StringUtil.compileMemoryInfo( 
                    BANJO.FEEDBACK_NEWLINE + BANJO.DATA_TRACEMEMORYSEARCHEREND )) );
        }

        // Cache the value
	    displayMemoryInfo = new String( processData.getValidatedProcessParameter(
	            BANJO.SETTING_DISPLAYMEMORYINFO ));
        
        optionalThreadLabel = processData.getOptionalThreadInfo();
	}
	
	/**
	 * Validates the settings values required for the searcher base class (a multitude of
	 * settings including the variable count, min and max Markov orders, max parent counts,
	 * and more).
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
		String strCondition;
		final int maxItemsUsed = 4;
		double[] dblValue = new double[maxItemsUsed];
		SettingItem[] settingItem = new SettingItem[maxItemsUsed];
		int variableCount = -1;

	  	// Validate the 'Variable Count'
	    settingNameCanonical = BANJO.SETTING_VARCOUNT;
	    settingNameDescriptive = BANJO.SETTING_VARCOUNT_DESCR;
	    settingNameForDisplay = BANJO.SETTING_VARCOUNT_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_INTEGER;
	    validationType = BANJO.VALIDATIONTYPE_MANDATORY;
	    settingItem[0] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            null,
	            Integer.toString( BANJO.BANJO_NOVALUESUPPLIED_NUMBER ) );
	    
	    if ( settingItem[0].isValidSetting() ) {

			try {

			    strCondition = new String( "greater than 1" );
			    dblValue[0] = Double.parseDouble( 
			            processData.getValidatedProcessParameter(
			                    settingNameCanonical ));
		        if ( dblValue[0] <= 1 ) {
		            
		            processData.addToErrors( new BanjoError( 
			                StringUtil.composeErrorMessage( 
			                        settingItem[0], 
			                        strCondition ),
				            BANJO.ERRORTYPE_INVALIDRANGE,
				            settingNameCanonical,
		            		StringUtil.getClassName( this ) ) );
		            
				    isDataValid = false;
		            
		        }
		    }
		    catch ( Exception e ) {

				throw new BanjoException( 
                        BANJO.ERROR_BANJO_DEV, settingItem[0], this );
			}
	    }
	    else {
	        
            isDataValid = false;
	    }
	    
	    // Validate the 'Default Max. Parent Count'
	    settingNameCanonical = BANJO.SETTING_DEFAULTMAXPARENTCOUNT;
	    settingNameDescriptive = BANJO.SETTING_DEFAULTMAXPARENTCOUNT_DESCR;
	    settingNameForDisplay = BANJO.SETTING_DEFAULTMAXPARENTCOUNT_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_INTEGER;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem[0] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            null, 
	            Integer.toString( BANJO.LIMIT_DEFAULTMAXPARENTCOUNT ) );
	    
	    int defaultMaxParentCount = BANJO.LIMIT_DEFAULTMAXPARENTCOUNT;
	    if ( settingItem[0].isValidSetting() ) {
	        
		    // Make sure it's positive
		    try {
	
			    strCondition = new String( "greater than 0" );
			    dblValue[0] = Double.parseDouble( 
			            processData.getValidatedProcessParameter(
			                    settingNameCanonical ));
		        if ( dblValue[0] <= 0 ) {
		        	
		            processData.addToErrors( new BanjoError( 
			                StringUtil.composeErrorMessage( 
			                        settingItem[0], 
			                        strCondition ),
				            BANJO.ERRORTYPE_INVALIDRANGE,
				            settingNameCanonical,
		            		StringUtil.getClassName( this ) ) );
		            
				    isDataValid = false;
		        }
		        else {
		            
		            defaultMaxParentCount = Integer.parseInt( 
		                    processData.getValidatedProcessParameter(
		                            settingNameCanonical ));
                    if ( defaultMaxParentCount > BANJO.LIMIT_DEFAULTMAXPARENTCOUNT ) {

                        // Set an alert: 
                        processData.addToWarnings( new BanjoError(
                            "The specified value for the setting '" +
                            BANJO.SETTING_DEFAULTMAXPARENTCOUNT +
                            "' (=" + defaultMaxParentCount +
                            ") has exceeded its acceptable value (=" +
                            BANJO.LIMIT_DEFAULTMAXPARENTCOUNT +
                            "), and is reduced to this limiting value.",
                            BANJO.ERRORTYPE_ALERT_CORRECTEDCHOICE,
                            settingNameCanonical,
                            null ) );
                        
                        defaultMaxParentCount = BANJO.LIMIT_DEFAULTMAXPARENTCOUNT;
                        processData.setValidatedProcessParameter(
                                settingNameCanonical, Integer.toString( defaultMaxParentCount ));
                    }
		        }
		    }
		    catch ( Exception e ) {
	
				throw new BanjoException( 
                        BANJO.ERROR_BANJO_DEV, settingItem[0], this );
		    }
	    }
	    else {
	        
            isDataValid = false;
	    }

	    // Validate the 'Max. Parent Count'
	    settingNameCanonical = BANJO.SETTING_MAXPARENTCOUNT;
	    settingNameDescriptive = BANJO.SETTING_MAXPARENTCOUNT_DESCR;
	    settingNameForDisplay = BANJO.SETTING_MAXPARENTCOUNT_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_INTEGER;
	    validationType = BANJO.VALIDATIONTYPE_MANDATORY;
	    settingItem[1] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            null, 
	            Integer.toString( BANJO.DEFAULT_MAXPARENTCOUNT ) );
	    
	    int maxParentCount = defaultMaxParentCount;
	    
	    // Make sure it's positive
	    if ( settingItem[1].isValidSetting() ) {
	        
		    try {

			    strCondition = new String( "greater than 0" );
			    dblValue[1] = Double.parseDouble( 
			            processData.getValidatedProcessParameter(
			                    settingNameCanonical ));
		        if ( dblValue[1] <= 0 ) {
		        	
		            processData.addToErrors( new BanjoError( 
			                StringUtil.composeErrorMessage( 
			                        settingItem[1], 
			                        strCondition ),
				            BANJO.ERRORTYPE_INVALIDRANGE,
				            settingNameCanonical,
		            		StringUtil.getClassName( this ) ) );
		            
				    isDataValid = false;
		        }
		        else {
		            
		            maxParentCount = Integer.parseInt( 
		                    processData.getValidatedProcessParameter(
		                            settingNameCanonical ));
                    if ( maxParentCount > defaultMaxParentCount ) {

                        // Set an alert: 
                        processData.addToWarnings( new BanjoError(
                                "The specified value for the setting '" +
                                BANJO.SETTING_MAXPARENTCOUNT +
                                "' (=" + defaultMaxParentCount +
                                ") has exceeded its acceptable value ('" +
                                BANJO.SETTING_DEFAULTMAXPARENTCOUNT_DESCR +
                                "'=" +
                                defaultMaxParentCount +
                                "), and is reduced to the limiting value.",
                                BANJO.ERRORTYPE_ALERT_CORRECTEDCHOICE,
                                settingNameCanonical,
                                null ) );
                        
                        maxParentCount = defaultMaxParentCount;
                        processData.setValidatedProcessParameter(
                                settingNameCanonical, Integer.toString( maxParentCount ));
                    }
		        }
		    }
		    catch ( Exception e ) {

				throw new BanjoException( 
                        BANJO.ERROR_BANJO_DEV, settingItem[1], this );
		    }
	    }
	    else {
	        
            isDataValid = false;
	    }

	    // Validate against varCount, if that is valid itself
	    if ( !processData.getSettingItemValueAsValidated( BANJO.SETTING_VARCOUNT ).equals( 
	    		BANJO.DATA_SETTINGNOTFOUND ) && 
	    		!processData.getSettingItemValueAsValidated( BANJO.SETTING_VARCOUNT ).equals( 
	    	    		BANJO.DATA_SETTINGINVALIDVALUE ) ) {

            variableCount = Integer.parseInt( processData.getValidatedProcessParameter(
                    BANJO.SETTING_VARCOUNT ));
            
		    strCondition = new String( "less than the variable count (='" +
                    processData.getSettingItemValueAsLoaded( BANJO.SETTING_VARCOUNT ) + "')" );

		    if ( dblValue[1] >= variableCount ) {
		        
		        processData.addToErrors( new BanjoError( 
		                StringUtil.composeErrorMessage( 
		                        settingItem[1], 
		                        strCondition ),
			            BANJO.ERRORTYPE_INVALIDRANGE,
			            settingNameCanonical,
	            		StringUtil.getClassName( this ) ) );

	            isDataValid = false;
		    }
	    }
	    
//	    if ( settingItem[0].isValidSetting() && settingItem[1].isValidSetting() ) {
//	 	   	    
//	        // Validate against the default max. parent count
//		    try {
//
//			    dblValue[0] = Double.parseDouble( 
//			            processData.getValidatedProcessParameter(
//			                    settingItem[0].getItemNameCanonical() ));
//			    dblValue[1] = Double.parseDouble( 
//			            processData.getValidatedProcessParameter(
//			                    settingItem[1].getItemNameCanonical() ));
//			    strCondition = new String( "less than or equal to " +
//			            "the default max. parent count (=" + 
//			            settingItem[0].getItemValueValidated() + ")" );
//			    
//		        if ( dblValue[0] > 0 && dblValue[0] < dblValue[1] ) {
//		        	
//		            processData.addToErrors( new BanjoError( 
//			                StringUtil.composeErrorMessage( 
//			                        settingItem[1], 
//			                        strCondition ),
//				            BANJO.ERRORTYPE_INVALIDRANGE,
//				            settingNameCanonical,
//		            		StringUtil.getClassName( this ) ) );
//
//		            isDataValid = false;
//		        }
//		    }
//		    catch ( Exception e ) {
//
//				throw new BanjoException( 
//                        BANJO.ERROR_BANJO_DEV, settingItem[1], this );
//		    }
//	    }
//	    else {
//	        
//            isDataValid = false;
//	    }
	    	        
	  	// Validate the 'min. Markov lag'
	    settingNameCanonical = BANJO.SETTING_MINMARKOVLAG;
	    settingNameDescriptive = BANJO.SETTING_MINMARKOVLAG_DESCR;
	    settingNameForDisplay = BANJO.SETTING_MINMARKOVLAG_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_INTEGER;
	    validationType = BANJO.VALIDATIONTYPE_MANDATORY;
	    settingItem[1] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            null,
                BANJO.BANJO_NOVALUESUPPLIED_STRING );
	    
	    if ( settingItem[1].isValidSetting() ) {

		    try {
	
			    strCondition = new String( "greater than or equal to 0" );
			    dblValue[1] = Double.parseDouble( 
			            processData.getValidatedProcessParameter(
			                    settingItem[1].getItemNameCanonical() ));
		        if ( dblValue[1] < 0 ) {
	
		            processData.addToErrors( new BanjoError( 
			                StringUtil.composeErrorMessage( 
			                        settingItem[1], 
			                        strCondition ),
				            BANJO.ERRORTYPE_INVALIDRANGE,
				            settingNameCanonical,
		            		StringUtil.getClassName( this ) ) );

		            isDataValid = false;
		        }
		    }
		    catch ( Exception e ) {

				throw new BanjoException( 
                        BANJO.ERROR_BANJO_DEV, settingItem[1], this );
			}
	    }
	    else {

            isDataValid = false;
	    }
	    
	  	// Validate the 'max. Markov lag'
	    settingNameCanonical = BANJO.SETTING_MAXMARKOVLAG;
	    settingNameDescriptive = BANJO.SETTING_MAXMARKOVLAG_DESCR;
	    settingNameForDisplay = BANJO.SETTING_MAXMARKOVLAG_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_INTEGER;
	    validationType = BANJO.VALIDATIONTYPE_MANDATORY;
	    settingItem[2] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            null,
                BANJO.BANJO_NOVALUESUPPLIED_STRING );
	    
	    if ( settingItem[2].isValidSetting() ) {

		    try {
		        
			    strCondition = new String( "greater than or equal to 0" );
			    dblValue[2] = Double.parseDouble( 
			            processData.getValidatedProcessParameter(
			                    settingItem[2].getItemNameCanonical() ));
			    
		        if ( dblValue[2] < 0 ) {

		            processData.addToErrors( new BanjoError( 
			                StringUtil.composeErrorMessage( 
			                        settingItem[2], 
			                        strCondition ),
				            BANJO.ERRORTYPE_INVALIDRANGE,
				            settingNameCanonical,
		            		StringUtil.getClassName( this ) ) );

		            isDataValid = false;
		        }
		    }
		    catch ( Exception e ) {

				throw new BanjoException( 
                        BANJO.ERROR_BANJO_DEV, settingItem[2], this );
		    }
	    }
	    else {

            isDataValid = false;
	    }
	    
	    // Only compare the min and the max Markov orders if both values are valid
	    if ( settingItem[1].isValidSetting() && settingItem[2].isValidSetting() ) {
		        
			try {
	
			    strCondition = new String( "less than or equal to" );
			    dblValue[1] = Double.parseDouble( 
			            processData.getValidatedProcessParameter(
			                    settingItem[1].getItemNameCanonical() ));
			    dblValue[2] = Double.parseDouble( 
			            processData.getValidatedProcessParameter(
			                    settingItem[2].getItemNameCanonical() ));
			    
				if ( dblValue[1] > dblValue[2] ) {
		        	
		            processData.addToErrors( new BanjoError( 
			                StringUtil.composeErrorMessage(
			                        settingItem[1], 
			                        settingItem[2], 
			                        strCondition ),
				            BANJO.ERRORTYPE_RULEVIOLATION,
				            settingNameCanonical,
		            		StringUtil.getClassName( this ) ) );

		            isDataValid = false;
				}
		    }
		    catch ( Exception e ) {

				throw new BanjoException( 
                        BANJO.ERROR_BANJO_DEV, settingItem[2], this );
		    }
	    }
	    else {

            isDataValid = false;
	    }

	    // -----------------------------
	    // Validate the 'mandatory lags'
	    
	    // Check first that it makes sense to have dbnMandatoryLags
		int tmpMinMarkovLag = -1;
		int tmpMaxMarkovLag = -1;
		
		if ( !processData.getSettingItemValueAsValidated( BANJO.SETTING_MAXMARKOVLAG )
		        .equals( BANJO.DATA_SETTINGNOTFOUND ) &&
		        !processData.getSettingItemValueAsValidated( BANJO.SETTING_MAXMARKOVLAG )
		        .equals( BANJO.DATA_SETTINGINVALIDVALUE ) &&
		        !processData.getSettingItemValueAsValidated( BANJO.SETTING_MINMARKOVLAG )
		        .equals( BANJO.DATA_SETTINGNOTFOUND ) &&
		        !processData.getSettingItemValueAsValidated( BANJO.SETTING_MINMARKOVLAG )
		        .equals( BANJO.DATA_SETTINGINVALIDVALUE ) ) {

			tmpMaxMarkovLag = Integer.parseInt( 
			        processData.getValidatedProcessParameter(
			                BANJO.SETTING_MAXMARKOVLAG ));
			tmpMinMarkovLag = Integer.parseInt( 
			        processData.getValidatedProcessParameter(
			                BANJO.SETTING_MINMARKOVLAG ));
		}
		
		if ( tmpMinMarkovLag >= 0 
		        && tmpMaxMarkovLag >= tmpMinMarkovLag 
		        && tmpMaxMarkovLag > 0 ) {
	
		    settingNameCanonical = BANJO.SETTING_DBNMANDATORYIDENTITYLAGS;
		    settingNameDescriptive = BANJO.SETTING_DBNMANDATORYIDENTITYLAGS_DESCR;
		    settingNameForDisplay = BANJO.SETTING_DBNMANDATORYIDENTITYLAGS_DISP;
		    settingDataType = BANJO.VALIDATION_DATATYPE_INTEGERLIST;
		    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
		    settingItem[3] = processData.processSetting( settingNameCanonical, 
		            settingNameDescriptive,
		            settingNameForDisplay,
		            settingDataType,
		            validationType,
		            "", 
                    BANJO.BANJO_NOVALUESUPPLIED_STRING );
		    
		    if ( settingItem[3].isValidSetting() 
                  && !settingItem[3].getItemValueValidated().equals( 
                            BANJO.BANJO_NOVALUESUPPLIED_STRING ) ) {

		        // Can't combine the 2 clauses; Also: no need to do anything
		        // when no mandatory lags are specified
		        if ( settingItem[2].isValidSetting() && 
			            !settingItem[3].getItemValueValidated().equals("") ) {
	
				    int intMaxParentCount = Integer.parseInt( 
				            processData.getValidatedProcessParameter(
			                BANJO.SETTING_MAXPARENTCOUNT ));
					// Need to make sure that the max parent count is at least 1 larger
					// than the dbn identiy lag count
					String strDbnMandatoryIdentityLags = 
					    processData.getValidatedProcessParameter(
			                BANJO.SETTING_DBNMANDATORYIDENTITYLAGS );
					StringTokenizer dbnMandatoryLags = new StringTokenizer( 
					        strDbnMandatoryIdentityLags, BANJO.DELIMITER_DEFAULT_LIST );
					
				    // Check first that it makes sense to have dbnMandatoryLags
				    if ( dblValue[2] < 1 && dbnMandatoryLags.countTokens() > 0 ) {
				        
				      	// Can't have dbnMandatoryLags for static bayesnet
					    strCondition = new String( "omitted for static bayesnets " +
					    		"(i.e., max. Markov Order is 0)" );
				        processData.addToErrors( new BanjoError( 
				                StringUtil.composeErrorMessage( 
				                        settingItem[3], 
				                        strCondition ),
					            BANJO.ERRORTYPE_INVALIDRANGE,
					            settingNameCanonical,
			            		StringUtil.getClassName( this ) ) );
				        
				        isDataValid = false;
				    }
				    
				    // Now check that dbnMandatoryLags makes sense (detailed validation
				    // is done in the BayesNetManager class when the data is being consumed)
					if ( intMaxParentCount <= dbnMandatoryLags.countTokens() ) {
					    
					    // MaxParentCount not large enough
					    strCondition = new String( BANJO.SETTING_MAXPARENTCOUNT + 
					            " needs to be larger than the number of" +
					            " mandatory parents specified in DbnMandatoryIdentityLags ('" +
					            dbnMandatoryLags.countTokens() + "')." );
					    
					    processData.addToErrors( new BanjoError( 
				                StringUtil.composeErrorMessage( 
				                        settingItem[3], 
				                        strCondition ),
					            BANJO.ERRORTYPE_INVALIDRANGE,
					            settingNameCanonical,
			            		StringUtil.getClassName( this ) ) );

			            isDataValid = false;
					}
					 				    
				    // Validate each lag to be between minMarkovOrder and maxMarkovOrder
					Pattern integerListPattern = Pattern.compile( 
			                BANJO.PATTERN_INTEGERLIST );
			        String[] itemValues = integerListPattern.split( 
			                settingItem[3].getItemValueValidated() );
			        int dbnMandatoryLag;
		        
			        for ( int i=0; i<itemValues.length; i++ ) {
				        						        
				        dbnMandatoryLag = Integer.parseInt( itemValues[i].trim() );
				        
				        if ( ! ( dbnMandatoryLag > 0 
                                && dbnMandatoryLag >= tmpMinMarkovLag 
				                && dbnMandatoryLag <= tmpMaxMarkovLag ) ) {
				            			
                            String lowerBound;
                            if ( tmpMinMarkovLag == 0 ) {
                                
                                lowerBound = "(";
                            }
                            else {
                                
                                lowerBound = "[";
                            }
				            strCondition = new String( "- for each value - " +
				            		"within the required bounds " + lowerBound + 
				                    tmpMinMarkovLag +
						            "," + tmpMaxMarkovLag +
						            "] given by the min. and max. Markov lags" );
				            
						    processData.addToErrors( new BanjoError( 
					                StringUtil.composeErrorMessage( 
					                        settingItem[3], 
					                        strCondition ),
						            BANJO.ERRORTYPE_INVALIDRANGE,
						            settingNameCanonical,
				            		StringUtil.getClassName( this ) ) );
						    
					        isDataValid = false;
				        }
			        }
		    	}
		    }
            else if ( settingItem[3].getItemValueValidated().equals( 
                    BANJO.BANJO_NOVALUESUPPLIED_STRING ) ) {
                
                isDataValid = true;
            }
		    else {

		        isDataValid = false;
		    }
		}
	    
	  	// Validate the 'min. networks before checking'
	    settingNameCanonical = BANJO.SETTING_MINNETWORKSBEFORECHECKING;
	    settingNameDescriptive = BANJO.SETTING_MINNETWORKSBEFORECHECKING_DESCR;
	    settingNameForDisplay = BANJO.SETTING_MINNETWORKSBEFORECHECKING_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_LONG;
	    validationType = BANJO.VALIDATIONTYPE_MANDATORY;
	    settingItem[1] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            null,
	            Long.toString( BANJO.DEFAULT_MINPROPOSEDNETWORKSHIGHSCORE ) );
	    
	    if ( settingItem[1].isValidSetting() ) {
	        
		    try {
	
			    strCondition = new String( "greater than or equal to 0" );
			    dblValue[1] = Double.parseDouble( 
			            processData.getValidatedProcessParameter(
			                    settingItem[1].getItemNameCanonical() ));
		        if ( dblValue[1] < 0 ) {
		            
		            processData.addToErrors( new BanjoError( 
			                StringUtil.composeErrorMessage( 
			                        settingItem[1], 
			                        strCondition ),
				            BANJO.ERRORTYPE_INVALIDRANGE,
				            settingNameCanonical,
		            		StringUtil.getClassName( this ) ) );

		            isDataValid = false;
		        }
		    }
		    catch ( Exception e ) {
	
				throw new BanjoException( 
                        BANJO.ERROR_BANJO_DEV, settingItem[1], this );
			}
	    }
	    else {

            isDataValid = false;
	    }

	  	// Validate the 'max. restarts'
	    settingNameCanonical = BANJO.SETTING_MAXRESTARTS;
	    settingNameDescriptive = BANJO.SETTING_MAXRESTARTS_DESCR;
	    settingNameForDisplay = BANJO.SETTING_MAXRESTARTS_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_LONG;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem[1] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            null, 
	            Integer.toString( BANJO.BANJO_NOVALUESUPPLIED_NUMBER ) );
	    	    
	  	// Validate the 'max. proposed networks'
	    settingNameCanonical = BANJO.SETTING_MAXPROPOSEDNETWORKS;
	    settingNameDescriptive = BANJO.SETTING_MAXPROPOSEDNETWORKS_DESCR;
	    settingNameForDisplay = BANJO.SETTING_MAXPROPOSEDNETWORKS_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_LONG;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem[2] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            null, 
	            Integer.toString( BANJO.BANJO_NOVALUESUPPLIED_NUMBER ) );
	    
	  	// Validate the 'max. search time'
	    settingNameCanonical = BANJO.SETTING_MAXSEARCHTIME;
	    settingNameDescriptive = BANJO.SETTING_MAXSEARCHTIME_DESCR;
	    settingNameForDisplay = BANJO.SETTING_MAXSEARCHTIME_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_TIME;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem[3] = processData.processSetting( settingNameCanonical,
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            null,
	            BANJO.BANJO_NOVALUESUPPLIED_STRING );
	    
		try {

            // At least one of the 3 iteration-stop conditions needs to be set
            // so we can complete our set up for getting feedback and being able
            // to terminate the search loop
	        if ( ( !settingItem[1].isValidSetting() 
	                && !settingItem[2].isValidSetting() 
	                && !settingItem[3].isValidSetting() ) ||
	             ( settingItem[1].getItemValueValidated().equals( 
	                   Integer.toString( BANJO.BANJO_NOVALUESUPPLIED_NUMBER )) 
	                && settingItem[2].getItemValueValidated().equals(
	 	                   Integer.toString( BANJO.BANJO_NOVALUESUPPLIED_NUMBER ))
	 	            && settingItem[3].getItemValueValidated().equals(
	 	                   Integer.toString( BANJO.BANJO_NOVALUESUPPLIED_NUMBER )) )) {
	        	
	            processData.addToErrors( new BanjoError( 
		                "A valid value needs to be supplied for at least one " +
		                "of the settings '" +
		                settingItem[1].getItemNameCanonical() + 
		                "', '" + settingItem[2].getItemNameCanonical() + 
		                "', or '" + settingItem[3].getItemNameCanonical() + 
		                "'.",
			            BANJO.ERRORTYPE_MISSINGVALUE,
			            settingNameCanonical,
	            		StringUtil.getClassName( this ) ) );

	            isDataValid = false;
	        }
	    }
	    catch ( Exception e ) {
	        
			throw new BanjoException( 
                    BANJO.ERROR_BANJO_DEV, settingItem, this );
		}
	    
	    // Validate the recorder
	    settingNameCanonical = BANJO.SETTING_STATISTICSCHOICE;
	    settingNameDescriptive = BANJO.SETTING_STATISTICSCHOICE_DESCR;
	    settingNameForDisplay = BANJO.SETTING_STATISTICSCHOICE_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem[0] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            this.validChoices(), 
	            BANJO.SETTING_STATISTICDEFAULT );
	    
        // Validate
        settingNameCanonical = BANJO.SETTING_SCREENREPORTINGINTERVAL;
        settingNameDescriptive = BANJO.SETTING_SCREENREPORTINGINTERVAL_DESCR;
        settingNameForDisplay = BANJO.SETTING_SCREENREPORTINGINTERVAL_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_TIME;
        validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        settingItem[0] = processData.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                null,
                Integer.toString( BANJO.DEFAULT_SCREENREPORTINGINTERVAL ) );

        // Validate
        settingNameCanonical = BANJO.SETTING_FILEREPORTINGINTERVAL;
        settingNameDescriptive = BANJO.SETTING_FILEREPORTINGINTERVAL_DESCR;
        settingNameForDisplay = BANJO.SETTING_FILEREPORTINGINTERVAL_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_TIME;
        validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        settingItem[0] = processData.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                null,
                Integer.toString( BANJO.DEFAULT_FILEREPORTINGINTERVAL ) );

	  	// Validate:
	    settingNameCanonical = BANJO.SETTING_NBEST;
	    settingNameDescriptive = BANJO.SETTING_NBEST_DESCR;
	    settingNameForDisplay = BANJO.SETTING_NBEST_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_INTEGER;
	    validationType = BANJO.VALIDATIONTYPE_MANDATORY;
	    settingItem[0] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            null, 
	            Integer.toString( BANJO.DEFAULT_NBEST ) );

	    if ( settingItem[0].isValidSetting() ) {
	        
			try {
			    
			    strCondition = new String( "greater than 0" );
			    dblValue[0] = Double.parseDouble( 
			            processData.getValidatedProcessParameter(
			                    settingNameCanonical ));
		        if ( dblValue[0] <= 0 ) {
		            
		            processData.addToErrors( new BanjoError( 
			                StringUtil.composeErrorMessage( 
			                        settingItem[0], 
			                        strCondition ),
				            BANJO.ERRORTYPE_INVALIDRANGE,
				            settingNameCanonical,
		            		StringUtil.getClassName( this ) ) );

		            isDataValid = false;
		        }
                else {
                    
                    // Only need to check on this when we have a valid number for n-best
                    if ( dblValue[0] > 0 ) {
                    
                      // Validate:
                        settingNameCanonical = BANJO.SETTING_BESTNETWORKSARE;
                        settingNameDescriptive = BANJO.SETTING_BESTNETWORKSARE_DESCR;
                        settingNameForDisplay = BANJO.SETTING_BESTNETWORKSARE_DISP;
                        settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
                        validValues.clear();
                        validValues.add( BANJO.UI_BESTNETWORKSARE_NONIDENTICAL );
                        validValues.add( BANJO.UI_BESTNETWORKSARE_NONEQUIVALENT );
                        validValues.add( BANJO.UI_BESTNETWORKSARE_NONIDENTICALTHENPRUNED );
                        validationType = BANJO.VALIDATIONTYPE_MANDATORY;
                        settingItem[1] = processData.processSetting( settingNameCanonical, 
                                settingNameDescriptive,
                                settingNameForDisplay,
                                settingDataType,
                                validationType,
                                validValues, 
                                BANJO.DEFAULT_BESTNETWORKSARE );
                        
                        if ( !settingItem[1].isValidSetting() ) {
                            
                            // We won't be able to make a choice for the equivalence checker
                            // if this setting is not properly specified
                            isDataValid = false;
                        }
                        else {
                            
                            // Additional validation:
                            if ( Integer.parseInt( processData.getValidatedProcessParameter( 
                                    BANJO.SETTING_MINMARKOVLAG ))>0 && !processData.getValidatedProcessParameter(
                                    settingNameCanonical).equalsIgnoreCase( 
                                            BANJO.UI_BESTNETWORKSARE_NONIDENTICAL ) ) {
                             
                                // Need to set the value to non-identical, and let the user know
                                processData.setValidatedProcessParameter(
                                        settingNameCanonical, BANJO.UI_BESTNETWORKSARE_NONIDENTICAL );
                                
                                processData.addToWarnings( new BanjoError( 
                                        "The value of setting '" +
                                        settingItem[1].getItemNameCanonical() + 
                                        "' has been corrected to '" +
                                        BANJO.UI_BESTNETWORKSARE_NONIDENTICAL + 
                                        "' (because minMarkovlag>0).",
                                        BANJO.ERRORTYPE_ALERT_OTHER,
                                        settingNameCanonical,
                                        StringUtil.getClassName( this ) ) );
                            }
                        }
                    }
                }
		    }
		    catch ( Exception e ) {

				throw new BanjoException( 
                        BANJO.ERROR_BANJO_DEV, settingItem[0], this );
			}
	    }
	    else {
	        
	        isDataValid = false;
	    }
		
		// Note: Every searcher that uses the nBestThresholdScore needs
		// to set the nBestThresholdScore (generally to the initial network score)
		nBestThresholdScore = BANJO.BANJO_INITIALTHRESHOLDSCORE_BDE;
	    	    
	    // Validate the 'ask to verify settings'
	    settingNameCanonical = BANJO.SETTING_ASKTOVERIFYSETTINGS;
	    settingNameDescriptive = BANJO.SETTING_ASKTOVERIFYSETTINGS_DESCR;
	    settingNameForDisplay = BANJO.SETTING_ASKTOVERIFYSETTINGS_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        validValues.clear();
	    validValues.add( BANJO.UI_ASKTOVERIFYSETTINGS_YES );
	    validValues.add( BANJO.UI_ASKTOVERIFYSETTINGS_NO );
	    settingItem[1] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            validValues, 
	            BANJO.DEFAULT_ASKTOVERIFYSETTINGS );

        // Validate the 'restart with random network'
        settingNameCanonical = BANJO.SETTING_RESTARTWITHRANDOMNETWORK;
        settingNameDescriptive = BANJO.SETTING_RESTARTWITHRANDOMNETWORK_DESCR;
        settingNameForDisplay = BANJO.SETTING_RESTARTWITHRANDOMNETWORK_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
        validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        validValues.clear();
        validValues.add( BANJO.UI_RESTARTWITHRANDOMNETWORK_YES );
        validValues.add( BANJO.UI_RESTARTWITHRANDOMNETWORK_NO );
        settingItem[1] = processData.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                validValues, 
                BANJO.DEFAULT_RESTARTWITHRANDOMNETWORK );

        if ( settingItem[1].isValidSetting() 
                && settingItem[1].getItemValueValidated().equals( 
                        BANJO.UI_RESTARTWITHRANDOMNETWORK_YES ) 
                && processData.isSettingValueValid( BANJO.SETTING_MAXPARENTCOUNT )) {

            maxParentCount = Integer.parseInt( processData.getSettingItemValueAsValidated( 
                    BANJO.SETTING_MAXPARENTCOUNT ) );
                
            // The max. parent count for restarts is mandatory, if the "restart with
            // random network" flag is set
            settingNameCanonical = BANJO.SETTING_MAXPARENTCOUNTFORRESTART;
            settingNameDescriptive = BANJO.SETTING_MAXPARENTCOUNTFORRESTART_DESCR;
            settingNameForDisplay = BANJO.SETTING_MAXPARENTCOUNTFORRESTART_DISP;
            settingDataType = BANJO.VALIDATION_DATATYPE_INTEGER;
            validationType = BANJO.VALIDATIONTYPE_MANDATORY;
            settingItem[0] = processData.processSetting( settingNameCanonical, 
                    settingNameDescriptive,
                    settingNameForDisplay,
                    settingDataType,
                    validationType,
                    null, 
                    Integer.toString( maxParentCount ) );
            
            if ( settingItem[0].isValidSetting() ) {
            
                if ( processData.isSettingValueValid( BANJO.SETTING_MAXPARENTCOUNT ) ) {
                    
                    try {
                        
                        dblValue[0] = Double.parseDouble( 
                                processData.getValidatedProcessParameter(
                                        settingNameCanonical ));
            
                        int intMaxParentCount = Integer.parseInt( 
                                processData.getValidatedProcessParameter(
                                        BANJO.SETTING_MAXPARENTCOUNT ));
            
                        strCondition = new String( "greater than 0." );
                        
                        if ( dblValue[0] <= 0 ) {
                            
                            processData.addToErrors( new BanjoError( 
                                    StringUtil.composeErrorMessage( 
                                            settingItem[0], 
                                            strCondition ),
                                    BANJO.ERRORTYPE_INVALIDRANGE,
                                    settingNameCanonical,
                                    StringUtil.getClassName( this ) ) );
                        }
                        else {

                            // Only display the alert for a relevant searcher
                            if ( processData.getValidatedProcessParameter(
                                            BANJO.SETTING_SEARCHERCHOICE ).equalsIgnoreCase( 
                                                    BANJO.UI_SEARCHER_GREEDY )) {
                                
                                if ( dblValue[0] > intMaxParentCount ) {
    
                                    int intMaxParentCountForRestarts = Integer.parseInt( 
                                            processData.getValidatedProcessParameter(
                                                    settingNameCanonical ));
                                    // Set an alert: 
                                    processData.addToWarnings( new BanjoError(
                                        "The specified value for the setting '" +
                                        settingNameCanonical +
                                        "' (=" + intMaxParentCountForRestarts +
                                        ") has exceeded its acceptable value (=" +
                                        intMaxParentCount +
                                        "), and is reduced to this limiting value.",
                                        BANJO.ERRORTYPE_ALERT_CORRECTEDCHOICE,
                                        settingNameCanonical,
                                        null ) );
                                    
                                        processData.setValidatedProcessParameter(
                                                settingNameCanonical, Integer.toString( intMaxParentCount ));
                                }
                            }
                        }
                    }
                    catch ( Exception e ) {
                        
                        throw new BanjoException( 
                                BANJO.ERROR_BANJO_DEV, settingItem[0], this );
                    }
                }
            }
            else {
                
                isDataValid = false;
            }
        }
	    
	    return isDataValid;
	}

	/**
	 * Gathers the final set of data produced by the search, collected
	 * from the core components.
	 */
	protected synchronized void finalCleanup() throws Exception {

        if ( processData.getValidatedProcessParameter( BANJO.SETTING_BESTNETWORKSARE )
                .equals( BANJO.UI_BESTNETWORKSARE_NONIDENTICALTHENPRUNED )) {

            pruneEquivalentNetworks();
        }
        
	    processData.setDynamicProcessParameter( 
	            BANJO.DATA_TOTALTIMEFORSEARCH,
	            Long.toString( elapsedTime ) );
	    
	    processData.setDynamicProcessParameter( 
	            BANJO.DATA_PROPOSEDNETWORKS,
	            Long.toString( networksVisitedGlobalCounter ) );
	    
	    // Store the networks with the highest scores in processData
	    // for convenient post-processing
	    processData.setHighScoreStructureSet( highScoreStructureSet );

        synchronized ( this ) {
            
    	    // Record the final data
    		searcherStatistics.recordFinalData( this );
    		
    		// collect any statistics from core internal objects
    		//searcherStatistics.recordSpecifiedData( new StringBuffer( "\n" ) );
    		searcherStatistics.recordSpecifiedData(
    				this.provideCollectedStatistics() );
    		
    		if ( BANJO.CONFIG_DISPLAYSTATISTICS ) {
    			    
    			searcherStatistics.recordSpecifiedData(
    					proposer.provideCollectedStatistics() );
    			searcherStatistics.recordSpecifiedData(
    			        cycleChecker.provideCollectedStatistics() );
    			searcherStatistics.recordSpecifiedData(
    					evaluator.provideCollectedStatistics() );
    			searcherStatistics.recordSpecifiedData(
    					decider.provideCollectedStatistics() );
    			
    			// Only display the statistics for the equivalence checking when
    			// we have a set of n-best scores
    			if ( nBestMax > 1 ) {
    			
    			    searcherStatistics.recordSpecifiedData(
    			            equivalenceChecker.provideCollectedStatistics() );
    			}
    		}
        }
        
        if ( displayMemoryInfo.equals( BANJO.UI_DISPLAYMEMORYINFO_YES ) ) {

            searcherStatistics.recordSpecifiedData( 
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            BANJO.FEEDBACK_NEWLINE + BANJO.DATA_MEMORYINFOATFINISH )) );
        }
		
		// Display the elapsed time (again) - for (dev) convenience
		if ( BANJO.CONFIG_FINALTIMEDISPLAY && elapsedTime > 0 ) {
		    
		    // tack on the known data about the "job"
			searcherStatistics.recordSpecifiedData(
			        StringUtil.getJobSignature( 
			                processData ) );

			int decimalFactor = 100;
			StringBuffer displayElapsedTime = 
			    new StringBuffer( BANJO.BUFFERLENGTH_SMALL );
			displayElapsedTime.append( "\n  Elapsed time:  ");
			displayElapsedTime.append( 
			        StringUtil.formatElapsedTime( 
			                elapsedTime, 1, BANJO.FEEDBACK_OPTION_TIMEFORMAT_MIXED ) );
			double mnpm = Math.round( this.networksVisitedGlobalCounter * decimalFactor * 60 / 
				( elapsedTime * 1000 ) );
			mnpm = mnpm / decimalFactor;
			displayElapsedTime.append( "\n  Search Rate:   ");
			displayElapsedTime.append( mnpm );
			displayElapsedTime.append( " mnpm");
			        
			searcherStatistics.recordSpecifiedData( displayElapsedTime );
		}
	}

    /**
     * Prunes the set of "current" high-scoring networks into the set of non-equivalent ones
     */
    protected void pruneEquivalentNetworks() throws Exception {
                
        EquivalenceCheckerI localEquivalenceChecker = new EquivalenceCheckerBasic( processData );
        
        // nothing to do if we only have a trivial set
        if ( getHighScoreStructureSet().size() <= 1 ) return;

        if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCEPRUNING_DISPLAYNETWORKS ) {
            
            // temp. only:
            System.out.println( "-------------------------------------------------------" );
            System.out.println( "Best " + getHighScoreStructureSet().size() + 
                                           " structures before pruning:" );
            System.out.print( "-------------------------------------------------------" );
            System.out.println( listScores() );
        }
        
        Iterator highScoreSetIterator =  getHighScoreStructureSet().iterator();
        BayesNetStructureI nextNetwork;
        
        int prunedNetworkCount = 0;
        while ( highScoreSetIterator.hasNext() ) {
            
            nextNetwork = (BayesNetStructure) highScoreSetIterator.next();
            
            if ( !localEquivalenceChecker.isEquivalent( 
                    nonEquivalentHighScoreStructureSet, nextNetwork ) ) {

                nonEquivalentHighScoreStructureSet.add( nextNetwork );   
            }
            else {
                
                prunedNetworkCount++;
            }
        }
        
        // Note: the above loop allows for more than nBestMax networks to end up
        // in the set of non-equivalent networks, so we need to adjust if necessary
        while ( nonEquivalentHighScoreStructureSet.size() > nBestMax ) {
            
            nonEquivalentHighScoreStructureSet.remove( 
                    nonEquivalentHighScoreStructureSet.last() );
        }


        if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCEPRUNING ) {
            
            System.out.println( 
                    "High-score set: " + highScoreStructureSet.size() +
                    ", pruned down to: " + nonEquivalentHighScoreStructureSet.size() );
        }
        
        // Finally, we need to set the highScoreStructure set to the pruned set
        highScoreStructureSet.clear();
        highScoreStructureSet.addAll( nonEquivalentHighScoreStructureSet );

        // update the count of tracked highscores
        nBestCount = highScoreStructureSet.size();
    }
	
	/**
	 * @return Returns the networksVisitedGlobalCounter.
	 */
	public long getNetworksVisitedGlobalCounter() {
		
		return networksVisitedGlobalCounter;
	}	
	
	/**
	 * @return Returns the searcherStatistics.
	 */
	public RecorderI getSearcherStatistics() {
		return searcherStatistics;
	}
	
	/**
	 * @return Returns the bayesNetManager.
	 */
	public Object getBayesNetManager() {
		return bayesNetManager;
	}
	
	/**
	 * @return Returns the currentBestScoreSinceRestart.
	 */
	public double getHighScore() {

		Iterator highScoreSetIterator = getHighScoreStructureSet().iterator();
		BayesNetStructureI highScoreNetwork = null;
		if ( highScoreSetIterator.hasNext() ) {
		    highScoreNetwork = ( BayesNetStructureI ) highScoreSetIterator.next();
		}
		
		return highScoreNetwork.getNetworkScore();
	}
	
	/**
	 * @return Returns the highScoreStructureSinceRestart.
	 */
	public Object getHighScoreStructure() {
		
		Iterator highScoreSetIterator = getHighScoreStructureSet().iterator();
		BayesNetStructureI highScoreNetwork = null;
		if ( highScoreSetIterator.hasNext() ) {
		    highScoreNetwork = ( BayesNetStructureI ) highScoreSetIterator.next();
		}

		return highScoreNetwork;
}
	/**
	 * @return Returns the highScoreStructureSet.
	 */
	public Collection getHighScoreStructureSet() {
		return highScoreStructureSet;
	}
	
	/**
	 * @return Returns the elapsedTime that the search has been running.
	 */
	protected synchronized long getElapsedTime() {

		long localElapsedTime = System.currentTimeMillis() - startTime;
		return localElapsedTime;
	}

	/**
	 * @return Returns the top scoring network(s) that the search found so far.
	 */
	protected StringBuffer listScores() {
	    
	    // Provides a list of the N-best scoring networks. Note that even though the
	    // collection of networks (the highScoreStructureSet) is a set of objects with
	    // a BayesNetStructureI contract, we channel access to the set via our special
	    // wrapper HighScoreContainer class, which gives us access to the special
	    // formatting of the string representation of the network as defined within
	    // the individual BayesNetStructure implementations. 
	    
	    StringBuffer statisticsBuffer = new StringBuffer(
	    		BANJO.BUFFERLENGTH_STAT );
	    
		Iterator highScoreSetIterator = getHighScoreStructureSet().iterator();
		int size = getHighScoreStructureSet().size();
		BayesNetStructureI nextNetwork;
		int i = 0;
		while ( highScoreSetIterator.hasNext() ) {
			
			i++;
			nextNetwork = (BayesNetStructureI) highScoreSetIterator.next();
			
			// Record the score and the loop
			if ( size > 1 ) {
			    
				statisticsBuffer.append( "\n\nNetwork #" );
				statisticsBuffer.append( i );
				statisticsBuffer.append( ", score: " );
			}
			else {

				statisticsBuffer.append( "\n\nNetwork score: " );
			}
			statisticsBuffer.append( StringUtil.formatDecimalDisplay(
			        nextNetwork.getNetworkScore(), 
			        BANJO.FEEDBACK_DISPLAYFORMATFORNETWORKSCORE ) );
			statisticsBuffer.append( ", first found at iteration " );
			statisticsBuffer.append( nextNetwork.getSearchLoopIndex() );
			
			// Add the network structure as a specially formatted string
			statisticsBuffer.append( "\n" );
			statisticsBuffer.append( nextNetwork.toString() );
		}
		
		return statisticsBuffer;
	}

	/**
     * (Optional) Lets user verify the selected search parameters before running
     * a search.
     * 
     * @return Returns the boolean value of the user's choice.
     */
	protected boolean askToVerifySettings() throws Exception {
	
	    // When the 'askToVerifySettings' value in the settings file is set to 'yes'
	    // we post a quick check to the user with the main option that are selected.
	    // The user can then decide whether to run the search or to stop and change
	    // some parameters.
	    
		String askToVerifySettings;
		
		// hjs 7/16/04
		// Add optional feedback to user before running the main loop
		askToVerifySettings = processData.getValidatedProcessParameter(
		        BANJO.SETTING_ASKTOVERIFYSETTINGS );

		if ( askToVerifySettings.equalsIgnoreCase( 
		        BANJO.UI_ASKTOVERIFYSETTINGS_YES ) ) {
            
			int intInputChar;
			
			// Note: the settings will have printed at this time (unless the config option
			// has been changed)
			System.out.print( "\n*** " );
			System.out.print(
			        "\n*** You have provided the above parameters for your search. " );
			System.out.print( "\n*** " );
			
			System.out.print(searcherStats);
			System.out.print("\n*** \n*** To confirm and start executing the search, " +
					"press <Enter>. " +
					"Any other key will stop the search. " +
					"\n*** Note: set the askToVerifySettings parameter to 'no' " +
					"if you don't want this confirmation.\n*** \n");
			
			// then process the user input to decide whether to execute
            intInputChar = System.in.read();
            if ( intInputChar != BANJO.BANJO_SYSTEM_ENTER 
                    && intInputChar != BANJO.BANJO_SYSTEM_NL ) {
                          
				System.out.print( "\nExecution stopped per your request.\n" );
				return false;
			}
			
			System.out.print("\n\nStarting the search execution.\n\n");
		}
		
	    return true;
	}
	
	/**
     * @return Returns the statistics about the particular searcher implementation.
     */
	// Override this method if necessary
	public synchronized StringBuffer provideCollectedStatistics() throws Exception {
			    
	    int displayLineLength = BANJO.FEEDBACK_LINELENGTH;
        StringBuffer finalReportLabel = new StringBuffer();
        StringBuffer searchStatsLabel = new StringBuffer();
		
		StringBuffer collectedStats = 
			new StringBuffer( BANJO.BUFFERLENGTH_STAT );
        
        // "header" for the final report's network section
        collectedStats.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
                    substring( 0, displayLineLength ) );
        
        String nBest =  processData.getValidatedProcessParameter(
                BANJO.SETTING_NBEST );

        if ( optionalThreadLabel.length() > 0 ) {

            finalReportLabel.append( optionalThreadLabel + " " );
        }
        finalReportLabel.append( BANJO.DATA_FINALREPORT );

        if ( optionalThreadLabel.length() > 0 ) {

            searchStatsLabel.append( optionalThreadLabel + " " );
        }
        searchStatsLabel.append( BANJO.DATA_SEARCHSTATISTICS );
        
        if ( Integer.parseInt( nBest ) > 1 ) {
        
             collectedStats.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, finalReportLabel.toString(), 
                "Best networks overall", null, lineLength ) );
        }
        else {
            
            collectedStats.append( StringUtil.formatRightLeftJustified( 
               newLinePlusPrefix, finalReportLabel.toString(), 
               "Best network overall", null, lineLength ) );             
        }
        
        collectedStats.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
                substring( 0, displayLineLength ) ); 

        // 
		StringBuffer nBestStr = new StringBuffer( BANJO.BUFFERLENGTH_SMALL );

        if ( Integer.parseInt( nBest ) > 1 ) {
            
            if ( processData.getValidatedProcessParameter( 
                    BANJO.SETTING_BESTNETWORKSARE ).equals( 
                            BANJO.UI_BESTNETWORKSARE_NONIDENTICAL ) ) {

                nBestStr.append( "These are the " + getHighScoreStructureSet().size() + 
                        " top-scoring non-identical networks found during the search: " );
            }
            else if ( processData.getValidatedProcessParameter( 
                    BANJO.SETTING_BESTNETWORKSARE ).equals( 
                            BANJO.UI_BESTNETWORKSARE_NONEQUIVALENT ) ) {

                nBestStr.append( "These are the " + getHighScoreStructureSet().size() + 
                        " top-scoring non-equivalent networks found during the search: " );    
            }
            else if ( processData.getValidatedProcessParameter( 
                    BANJO.SETTING_BESTNETWORKSARE ).equals( 
                            BANJO.UI_BESTNETWORKSARE_NONIDENTICALTHENPRUNED ) ) {

                nBestStr.append( "During the search, the " + nBest +
                        " top-scoring non-identical networks were tracked. " );

                nBestStr.append( BANJO.FEEDBACK_NEWLINE );

                int intNumberOfHighScores = getHighScoreStructureSet().size();
                if ( intNumberOfHighScores == 1 ) {
                
                    nBestStr.append( "This is the only non-equivalent network left after pruning:" );
                }
                else {
                
                    nBestStr.append( "These are the " + intNumberOfHighScores +
                        " non-equivalent networks left after pruning:" );
                }
            }
		}
        

        collectedStats.append( BANJO.FEEDBACK_NEWLINE );
        collectedStats.append( nBestStr.toString() );
		
		// Now add all the n-best networks
		collectedStats.append( listScores() );
		
		// Add the statistics
		if ( BANJO.CONFIG_DISPLAYSTATISTICS ) {

			collectedStats.append( BANJO.FEEDBACK_NEWLINE );
			collectedStats.append( BANJO.FEEDBACK_NEWLINE );
			collectedStats.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
			        substring( 0, displayLineLength ) ); 
			collectedStats.append( StringUtil.formatRightLeftJustified( 
			        newLinePlusPrefix, searchStatsLabel.toString(), 
			        "", null, displayLineLength ) );
			collectedStats.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
			        substring( 0, displayLineLength ) );
			collectedStats.append( BANJO.FEEDBACK_NEWLINE );

			collectedStats.append( BANJO.FEEDBACK_NEWLINE );
			collectedStats.append( "Statistics collected in searcher '" + 
			        StringUtil.getClassName(this) + "':" );

//            if ( !(BANJO.DEBUG && BANJO.DATA_SEARCHER_OMITTIMEDISPLAY ) ) {
            if ( !BANJO.DATA_SEARCHER_OMITTIMEDISPLAY ) {
    			
                collectedStats.append( BANJO.FEEDBACK_NEWLINE );
    			collectedStats.append( "  Search completed at ");
    			collectedStats.append( timeFormat.format(new Date()) );
            }

			collectedStats.append( BANJO.FEEDBACK_NEWLINE );
			collectedStats.append( "  Number of networks examined: ");
			collectedStats.append( getNetworksVisitedGlobalCounter());

			collectedStats.append( BANJO.FEEDBACK_NEWLINE );
			collectedStats.append( "  Total time used: ");
			collectedStats.append( StringUtil.formatElapsedTime(
			        Long.parseLong( processData.getDynamicProcessParameter(
			        BANJO.DATA_TOTALTIMEFORSEARCH )), 2, 
			        BANJO.FEEDBACK_OPTION_TIMEFORMAT_MIXED ));

			collectedStats.append( BANJO.FEEDBACK_NEWLINE );
			collectedStats.append( "  High score: ");
			BayesNetStructureI highScoreNetwork = 
			    (BayesNetStructureI) this.getHighScoreStructure();
			collectedStats.append( StringUtil.formatDecimalDisplay(
			        highScoreNetwork.getNetworkScore(), 
			        BANJO.FEEDBACK_DISPLAYFORMATFORNETWORKSCORE ) );
			collectedStats.append( ", first found at iteration ");
			collectedStats.append( highScoreNetwork.getSearchLoopIndex() );
					
			// Note: [For Greedy Search] the sum of the computed and the cached scores will
			// be equal to the sum of additions + deletions + 2*reversal - 
			// (number of [re]starts)*(number of variables) 
			
            if ( processData.getValidatedProcessParameter(
                    BANJO.SETTING_SEARCHERCHOICE ).equalsIgnoreCase( 
                    BANJO.UI_SEARCHER_SIMANNEAL )) { 
            
                collectedStats.append( "\n  Number of re-anneals: " );
            }
            else {
			
                collectedStats.append( "\n  Number of restarts: " );
            }
			collectedStats.append( restartCount );
			if ( BANJO.CONFIG_DISPLAYITERATIONRESTARTS ) {
				
				collectedStats.append( "\n  at iterations " );
				collectedStats.append( restartsAtCounts );
			}
		}
		
		return collectedStats;
	}

	/**
     * @return Prepares a feedback string about the search progress, based on
     * the screen report interval. Used by the terminator inner classes.
     */
    protected synchronized StringBuffer feedbackBasedOnTime() throws Exception {

        StringBuffer messageToDisplay = new StringBuffer(BANJO.BUFFERLENGTH_SMALL );
        String prefixSpacer = "";
        String postfixSpacer = "";
        
        elapsedTime = getElapsedTime();

        if (  elapsedTime >= nextFeedbackTime ) {
            
            // The next 2 lines simply format the output to show some extra decimals
            double displayPercent;
            
            if ( maxSearchTime > 0 ) {
                
                displayPercent = Math.round( 1000 * elapsedTime / maxSearchTime );
                displayPercent /= 10;
                if ( displayPercent > 100 ) displayPercent = 100;
            }
            
            if ( optionalThreadLabel.length() > 0 ) {

                messageToDisplay.append( optionalThreadLabel + BANJO.FEEDBACK_NEWLINE );
            }
            messageToDisplay.append( BANJO.FEEDBACKSTRING_STATUS );
            prefixSpacer = BANJO.FEEDBACK_SPACESFORTRIMMING.substring( 
                    0, BANJO.FEEDBACKSTRING_STATUS.length() );
            int postfixLength = BANJO.FEEDBACKSTRING_NETWORKS.length();
            if ( BANJO.FEEDBACKSTRING_TIME.length() > postfixLength ) {
                postfixLength = BANJO.FEEDBACKSTRING_TIME.length();     
            }
            if ( feedbackRestarts.length() > postfixLength ) {
                postfixLength = feedbackRestarts.length();     
            }
            postfixLength++;

            messageToDisplay.append( BANJO.FEEDBACKSTRING_NETWORKS );
            postfixSpacer = BANJO.FEEDBACK_SPACESFORTRIMMING.substring( 
                    0, postfixLength-BANJO.FEEDBACKSTRING_NETWORKS.length() );
            messageToDisplay.append( postfixSpacer );
            messageToDisplay.append(  StringUtil.fillSpacesLeft( 
                    ( new Long( networksVisitedGlobalCounter ) ).toString(), 
                    loopPaddingLength ) );
            
            if ( maxSearchLoops > 0 ) {
                
                displayPercent = Math.round( 1000 * 
                        networksVisitedGlobalCounter / maxSearchLoops );
                displayPercent /= 10;
                if ( displayPercent > 100 ) displayPercent = 100;
                
                messageToDisplay.append( " (" );
                messageToDisplay.append( displayPercent );
                messageToDisplay.append( "% of max." );
                messageToDisplay.append( " " );
                messageToDisplay.append( maxSearchLoops );
                messageToDisplay.append( ")" );
//                messageToDisplay.append( 
//                      StringUtil.fillSpacesLeft( 
//                              ( new Double( displayPercent ).toString() ), 
//                              percentPaddingLength ) +
//                      "% of max.)" );
            }
            
            messageToDisplay.append( BANJO.FEEDBACK_NEWLINE );
            messageToDisplay.append( prefixSpacer );
            messageToDisplay.append( BANJO.FEEDBACKSTRING_TIME );
            postfixSpacer = BANJO.FEEDBACK_SPACESFORTRIMMING.substring( 
                    0, postfixLength-BANJO.FEEDBACKSTRING_TIME.length() );
            messageToDisplay.append( postfixSpacer );
            messageToDisplay.append( StringUtil.fillSpacesLeft(
                    ( StringUtil.formatElapsedTime( 
                    elapsedTime, numberOfDecimals,
                    BANJO.FEEDBACK_OPTION_TIMEFORMAT_MIXED )).toString(), 
                    loopPaddingLength ) );
            
            if ( maxSearchTime > 0 ) {
                
                displayPercent = Math.round( 1000 * elapsedTime / maxSearchTime );
                displayPercent /= 10;
                if ( displayPercent > 100 ) displayPercent = 100;
                
                messageToDisplay.append( " (" );
                messageToDisplay.append( displayPercent );
                messageToDisplay.append( "% of max." );
                messageToDisplay.append( " " );
                messageToDisplay.append( ( StringUtil.formatElapsedTime( 
                              maxSearchTime, numberOfDecimals,
                      BANJO.FEEDBACK_OPTION_TIMEFORMAT_MIXED )).toString() );
                messageToDisplay.append( ")" );
//                messageToDisplay.append( StringUtil.fillSpacesLeft( 
//                              ( new Double( displayPercent).toString() ), 
//                              percentPaddingLength ) + "% of max." );
//                messageToDisplay.append( StringUtil.fillSpacesLeft(
//                        ( StringUtil.formatElapsedTime( 
//                                maxSearchTime, numberOfDecimals,
//                        BANJO.FEEDBACK_OPTION_TIMEFORMAT_MIXED )).toString(), 
//                        timePaddingLength-1 ) );
//                messageToDisplay.append( ")" );
            }

            messageToDisplay.append( BANJO.FEEDBACK_NEWLINE );
            messageToDisplay.append( prefixSpacer );
            messageToDisplay.append( feedbackRestarts );
            postfixSpacer = BANJO.FEEDBACK_SPACESFORTRIMMING.substring( 
                    0, postfixLength - feedbackRestarts.length() );
            messageToDisplay.append( postfixSpacer );
            messageToDisplay.append( StringUtil.fillSpacesLeft( 
                    ( new Long( restartCount ) ).toString(), 
                    loopPaddingLength ) );
            
            if ( maxRestarts > 0 ) {
                
                displayPercent = Math.round( 1000 * restartCount / maxRestarts );
                displayPercent /= 10;
                if ( displayPercent > 100 ) displayPercent = 100;
                
                messageToDisplay.append( " (" );
                messageToDisplay.append( displayPercent );
                messageToDisplay.append( "% of max." );
                messageToDisplay.append( " " );
                messageToDisplay.append( maxRestarts );
                messageToDisplay.append( ")" );
//                messageToDisplay.append( 
//                      StringUtil.fillSpacesLeft( 
//                              ( new Double( displayPercent).toString() ), 
//                              percentPaddingLength ) + "% of max.)" );
            }
            
		    if ( displayMemoryInfo.equals( BANJO.UI_DISPLAYMEMORYINFO_YES ) ) {

                messageToDisplay.append( BANJO.FEEDBACK_NEWLINE );
                messageToDisplay.append( prefixSpacer );
		        messageToDisplay.append(
		            StringUtil.compileMemoryInfo( "" ));
                
                messageToDisplay.append( BANJO.FEEDBACK_NEWLINE );
		    }
		    
		    nextFeedbackTime += feedbackTimeDelta;
		}  
		
		return messageToDisplay;
	}

    /**
     * Method used by terminator inner classes, for providing a display of the
     * intermediate results (networks and their scores).
     */
	protected synchronized StringBuffer trackIntermediateResults() throws Exception {

        StringBuffer bestScoresSoFar = new StringBuffer(
                BANJO.BUFFERLENGTH_STAT_INTERNAL );
        
	    elapsedTime = getElapsedTime();
  	    
	    if ( trackIntermediateResults ) {
	        
		    if ( elapsedTime > nextFileReportTime ) {
		        
		      	// Write intermediate result to file:
		        bestScoresSoFar.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
				        substring( 0, lineLength ) ); 


                
                String nBest =  processData.getValidatedProcessParameter(
                        BANJO.SETTING_NBEST );
                
                if ( Integer.parseInt( nBest ) > 1 ) {
                    
                    bestScoresSoFar.append( StringUtil.formatRightLeftJustified( 
                            newLinePlusPrefix, "Intermediate report", 
                            "Best network(s) so far", null, lineLength ) );
                }
                else {
                    
                    bestScoresSoFar.append( StringUtil.formatRightLeftJustified( 
                            newLinePlusPrefix, "Intermediate report", 
                            "Best network so far", null, lineLength ) );      
                }
			    
		        bestScoresSoFar.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
				        substring( 0, lineLength ) );
		        bestScoresSoFar.append( listScores() );
		        bestScoresSoFar.append( "\n" );
                
                bestScoresSoFar.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
                        substring( 0, lineLength ) );
                bestScoresSoFar.append( "\n" );
                bestScoresSoFar.append( "\n" );
			    
			    nextFileReportTime += fileReportTimeDelta;
		    }
	    }
	    
	    return bestScoresSoFar;
	}
    
    protected void handleOutOfMemory() throws Exception {
        
        // Free some memory that we know we won't need anymore
        if ( evaluator instanceof Evaluator ) {
        
            // The evaluator likely had the largest memory requirements
            ((Evaluator) evaluator).cleanupOnException();
        }
        
        // Clear any (dynamic) settings that could bind enough memory to keep
        // us from displaying the final output
        processData.setDynamicProcessParameter( 
                BANJO.DATA_DISCRETIZATIONREPORT, "" );
        
        // .. and call the garbage collector, so we can do our final cleanup
        Runtime.getRuntime().gc();

        // Now try to write out the results obtained so far
        long maxMem = Runtime.getRuntime().maxMemory();
        searcherStatistics.recordSpecifiedData( new StringBuffer( 
                BANJO.ERRORMSG_BANJO_OUTOFMEMORY_1 + 
                BANJO.FEEDBACK_NEWLINE +
                BANJO.ERRORMSG_BANJO_OUTOFMEMORY_2 + 
                maxMem/(1024*1024) + " mb." +
                BANJO.FEEDBACK_NEWLINE +
                BANJO.ERRORMSG_BANJO_OUTOFMEMORY_3 ) );
        
        // We don't want to display any more memory info, because it may just confuse
        // the user
        displayMemoryInfo = BANJO.UI_DISPLAYMEMORYINFO_NO;
        
        finalCleanup();
    }

    /**
     * Provides the valid choices for this class, here: for the recorders.
     */
    public Object validChoices() {
	    
		Set validValues = new HashSet();

	    validValues.add( BANJO.UI_DEFAULT );
	   
	    // Recorders
	    validValues.add( BANJO.UI_RECORDER_STANDARD );
	    
	    return validValues;
	}
	
	// --------------------------------------------------------------------
	// Abstract functionality that any derived Searcher needs to provide
	// --------------------------------------------------------------------
	
    /**
     * Executes the search based on the particular search algorithm.
     */
	public abstract void executeSearch() throws Exception;
}
