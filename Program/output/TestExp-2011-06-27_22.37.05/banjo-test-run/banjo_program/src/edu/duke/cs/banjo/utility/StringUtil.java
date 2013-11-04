/*
 * Created on Apr 2, 2004
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
package edu.duke.cs.banjo.utility;

import java.util.*; 
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.SimpleDateFormat;

import edu.duke.cs.banjo.data.settings.SettingItem;
import edu.duke.cs.banjo.data.settings.Settings;

/**
 * Contains a collection of general purpose string-related code.
 * 
 * <p><strong>Details:</strong> <br>
 * 
 * 
 * <p><strong>Change History:</strong> <br>
 * Created on Apr 2, 2004
 * <p>
 * 10/13/2005 (v2.0) hjs 	
 * 		Added formatDecimalDisplay method for more robust display of decimal 
 * 		numbers (formatting)
 * 
 * <p>
 * hjs (v2.1)               Add methods for generating "subordinate" file names
 *                          when dealing with output from multiple threads.
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 *
 */
public class StringUtil {
	
    // Trivial conversion constants
    private static final int MILLISECSPERSECOND = 1000;
    private static final int MILLISECSPERMINUTE = 60*1000;
    private static final int MILLISECSPERHOUR = 60*60*1000;
    private static final int MILLISECSPERDAY = 24*60*60*1000;

 	// This gives us access to the main Settings class, so we can validate the file
 	// info in the same way the rest of the application does it.
 	private static Settings processData = null;
    
    private static String cachedTimeStamp = new String("");

    /**
     * (Empty) constructor for static StringUtil class.
     */
	StringUtil(){
        // nothing to do   
    };

    /**
     * Provides access to the (main) settings.
     * @param  _processData The settings.
     */
	public static void setProcessData( final Settings _processData ) throws Exception {
	
	    processData = _processData;
	    
	    validateRequiredData();	    
	};
	
	/**
	 * Validates the settings values required for the string util.
	 * 
	 * @return Returns a boolean flag that indicates whether any crucial setting
	 * could not be validated.
	 */
	private static boolean validateRequiredData() throws Exception {
	    
	    boolean isDataValid = true;
	    
	    // utility variables for validating
	    String settingNameCanonical;
	    String settingNameDescriptive;
	    String settingNameForDisplay;
        String settingDataType;
		SettingItem settingItem;
		int validationType;

	    // Validate:
	    settingNameCanonical = BANJO.SETTING_PROJECT;
	    settingNameDescriptive = BANJO.SETTING_PROJECT_DESCR;
	    settingNameForDisplay = BANJO.SETTING_PROJECT_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            BANJO.BANJO_FREEFORMINPUT,
	            BANJO.DATA_SETTINGDEFAULTVALUE_EMPTY );
	    
	    // Validate:
	    settingNameCanonical = BANJO.SETTING_USER;
	    settingNameDescriptive = BANJO.SETTING_USER_DESCR;
	    settingNameForDisplay = BANJO.SETTING_USER_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            BANJO.BANJO_FREEFORMINPUT,
	            BANJO.DATA_SETTINGDEFAULTVALUE_EMPTY );
	    
	    // Validate:
	    settingNameCanonical = BANJO.SETTING_DATASET;
	    settingNameDescriptive = BANJO.SETTING_DATASET_DESCR;
	    settingNameForDisplay = BANJO.SETTING_DATASET_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            BANJO.BANJO_FREEFORMINPUT,
	            BANJO.DATA_SETTINGDEFAULTVALUE_EMPTY );
	    
	    // Validate:
	    settingNameCanonical = BANJO.SETTING_NOTES;
	    settingNameDescriptive = BANJO.SETTING_NOTES_DESCR;
	    settingNameForDisplay = BANJO.SETTING_NOTES_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            BANJO.BANJO_FREEFORMINPUT,
	            BANJO.DATA_SETTINGDEFAULTVALUE_EMPTY );

	    return isDataValid;
	}
	
	
	// ----------------------------------------------------------------------------
	// TODO: Change all functions to use StringBuffer for performance
	// (low priority, though - used mainly internally for trace, not within regular search)
    // ----------------------------------------------------------------------------
	
        
    /**
     * Utility method: Fills in blank spaces for creating formatted output.
     * 
     * @param _startString The string that is to be filled up with spaces, up to the
     * specified length.
     * @param _fillLength The length to which the string is to be filled.
     * 
     * @return Returns the string with the appended spaces.
     */
	// Fills in blank spaces for creating formatted output:
	public static String fillSpaces( 
            final String _startString, 
	        final int _fillLength) {
        
		String fillString;
		String blanks = BANJO.FEEDBACK_SPACESFORTRIMMING;
		
		if ( _startString.length() <= _fillLength)  {
		    
		    fillString = new String( 
		            blanks.substring(0, _fillLength - _startString.length()));
		    return new String( _startString + fillString );
		}
		
		return new String( _startString );
	}
    
    /**
     * Utility method: Fills in blank spaces for creating formatted output, 
     * to the left of the supplied string.
     * 
     * @param _startString The string that is to be filled up with spaces, up to the
     * specified length.
     * @param _fillLength The length to which the string is to be filled.
     * 
     * @return Returns the string with the prepended spaces.
     */
	public static String fillSpacesLeft( 
            final String _startString, 
	        final int _fillLength) {
        
		String fillString;
		String blanks = "                                      " +
				"                              ";
		
		if ( _startString.length() <= _fillLength)  {
		    
		    fillString = new String( 
		            blanks.substring(0, _fillLength - _startString.length()));
		    return new String( fillString + _startString );
		}
		
		return new String( _startString );
	}
    
    /**
     * Utility method: Removes comments at the end of a line of text.
     * 
     * @param _stringToProcess The string that may contain a comment at the end (comments
     * are indicated by a # symbol)
     * 
     * @return Returns the string without the comment.
     */
	public static String removeTrailingComment( final String _stringToProcess ) {
	    
	    String processedString = new String( _stringToProcess );
	    int charPosition = processedString.indexOf( BANJO.DEFAULT_COMMENTINDICATOR );
	    
	    if ( charPosition >= 0 ) 
	        processedString = processedString.substring( 0, charPosition );
	    
	    return processedString;
	}
    
    /**
     * Utility method: Creates a line of text that is both left and right justified.
     * 
     * @param _prefix An optional prefix to the display Item.
     * @param _displayItem The display Item to be left-hand justified.
     * @param _displayItemValue The display Item value that is to be left-hand justified.
     * @param _separator A optional separator string for splitting the text.
     * @param _lineLength The length of the text line to be returned. 
     * 
     * @return Returns the text line.
     */
	public static StringBuffer formatRightLeftJustified( 
	        String _prefix, 
            String _displayItem, 
            String _displayItemValue, 
            final String _separator, 
            final int _lineLength ) {
	    
	    StringBuffer formattedString = new StringBuffer();
		String blankSpaces = BANJO.FEEDBACK_SPACESFORTRIMMING;

        if ( _prefix == null ) _prefix = "";
		if ( _displayItem == null ) _displayItem = "";
		if ( _displayItemValue == null ) _displayItemValue = "";
		
		formattedString.append( _prefix );
		formattedString.append( _displayItem );
		int subLength = _lineLength - _displayItem.length() 
        		- _displayItemValue.length() - _prefix.length();
		if ( subLength > 0 && subLength < blankSpaces.length() ) {
		    
			formattedString.append( blankSpaces.substring( 0, subLength ));
			formattedString.append( _displayItemValue );
		}
		else {
		    
		    // In this case the length of displayItemValue is too long
		    // for the string to fit on a single line. If the separator
		    // is specified, we try to split the string
		    
		    if ( _separator == null || _separator.length() < 1 ) {
		        
			    formattedString.append( "  " );
				formattedString.append( _displayItemValue );
		    }
		    else {
		        
		        formattedString.append( "  " );
				formattedString.append( _displayItemValue );
		    }
		}
		    
		        
	    return formattedString;
	}
    
    /**
     * Utility method (debug/test): Compiles a list of the supplied properties.
     * 
     * @param _settingsToList Properties to be listed.
     * 
     * @return Returns the listing of the properties.
     */
	public static StringBuffer listSettings( final Properties _settingsToList ) {
		
		//loop through the set of properties:
		Set params = _settingsToList.keySet();
		Iterator iter = params.iterator();
		String strNextProperty;
		StringBuffer settingsString = 
		    new StringBuffer( BANJO.BUFFERLENGTH_STAT );
		int settingsCount = 0;

	    settingsString.append( "Summary of initialSettings as loaded: " );
	    settingsString.append( BANJO.FEEDBACK_NEWLINE );
	    settingsString.append( "===================================== " );
	    settingsString.append( BANJO.FEEDBACK_NEWLINE );
		while (iter.hasNext()) {
			strNextProperty = (String) iter.next();
			
			settingsString.append( strNextProperty );
			settingsString.append( " = " );
			settingsString.append( StringUtil.fillSpaces(strNextProperty, 28) );
			settingsString.append( "'" );
			settingsString.append( _settingsToList.getProperty(strNextProperty) );
			settingsString.append( "'" );
			settingsString.append( "\n" );
			settingsCount++;
		}
		
		settingsString.append( settingsCount );
		settingsString.append( " Loading of initialSettings complete." );
	    settingsString.append( BANJO.FEEDBACK_NEWLINE );

	    settingsString.append( BANJO.FEEDBACK_NEWLINE );
	    settingsString.append( BANJO.FEEDBACK_NEWLINE );
	
		return settingsString;
	}
    
    /**
     * Utility method: Returns the name of the class where this method is called from.
     * 
     * @param _someObj Object for which we want its class name.
     * 
     * @return Returns the class name.
     */
	// Utility function for quick concatenation of the name of the class in which
	// we are (works properly for subclasses)
	public static String getClassName( final Object _someObj ) {

		Object classObj = _someObj.getClass();
	    String strClassName = new String( classObj.toString() );

		strClassName = strClassName.substring(strClassName.lastIndexOf(".")+1);

	    return strClassName;
	}
    
    /**
     * Utility method (debug/test): Trivial helper function for quick trace output.
     * 
     * @param _intArray An array of integers.
     * 
     * @return Returns a string compiled from the array elements.
     */
	public static String arrayAsString( final int[] _intArray ) {
	    
	    String strArray = "";
	    
	    for (int i=0; i< _intArray.length; i++ )
	        strArray += _intArray[i] + " ";
	    
	    return strArray;
	}
    
    /**
     * Compiles the setting values that we group as the "Banjo signature".
     *
     * @return StringBuffer The collect "job signature".
     */
    public static StringBuffer getBanjoSignature() {
        
        StringBuffer jobSignature = new StringBuffer( BANJO.BUFFERLENGTH_STAT );	    

	    // Adding a little extra formatting
		String newLinePlusPrefix = BANJO.FEEDBACK_NEWLINE + BANJO.FEEDBACK_DASH + 
				BANJO.FEEDBACK_SPACE;
		String linePostfix = BANJO.FEEDBACK_SPACE + BANJO.FEEDBACK_DASH;
		int lineLength = BANJO.FEEDBACK_LINELENGTH;
		String HeaderInfo;
		
		jobSignature.append( StringUtil.formatRightLeftJustified(
		        newLinePlusPrefix, BANJO.APPLICATION_NAME, 
		        BANJO.APPLICATION_NAME_LONG + linePostfix, 
		        null, lineLength ) );

		HeaderInfo = BANJO.RELEASE + BANJO.FEEDBACK_SPACE + 
			BANJO.APPLICATION_VERSIONNUMBER;
		jobSignature.append( StringUtil.formatRightLeftJustified( 
		        newLinePlusPrefix, HeaderInfo, 
		        BANJO.APPLICATION_VERSIONDATE + linePostfix, 
		        null, lineLength ) );

		HeaderInfo = BANJO.RELEASE_LICENCED;
		jobSignature.append( StringUtil.formatRightLeftJustified( 
		        newLinePlusPrefix, HeaderInfo, 
		        linePostfix, 
		        null, lineLength ) );

		HeaderInfo = BANJO.RELEASE_COPYRIGHT;
		jobSignature.append( StringUtil.formatRightLeftJustified( 
		        newLinePlusPrefix, HeaderInfo, 
		        linePostfix, 
		        null, lineLength ) );

		HeaderInfo = BANJO.RELEASE_ALLRIGHTS;
		jobSignature.append( StringUtil.formatRightLeftJustified( 
		        newLinePlusPrefix, HeaderInfo, 
		        linePostfix, 
		        null, lineLength ) );        
        
        return jobSignature;
    }
    
    /**
     * Compiles the setting values that we group as the "job signature".
     * 
     * @param _processData The (main) settings, giving access to the settings and their
     * values that make up the "job signature' (project, user, dataset, notes).
     * 
     * @return StringBuffer The collect "job signature".
     */
    public static StringBuffer getJobSignature( final Settings _processData ) {
        
        StringBuffer searcherFeedback = new StringBuffer( BANJO.BUFFERLENGTH_STAT );

	    // Adding a little extra formatting
		String newLinePlusPrefix = BANJO.FEEDBACK_NEWLINE + BANJO.FEEDBACK_DASH + 
				BANJO.FEEDBACK_SPACE;
		int lineLength = BANJO.FEEDBACK_LINELENGTH;
		
        String strSettingChoice;

		strSettingChoice = _processData.getValidatedProcessParameter( 
		        BANJO.SETTING_PROJECT );
		searcherFeedback.append( StringUtil.formatRightLeftJustified( 
		        newLinePlusPrefix, 
		        BANJO.SETTING_PROJECT_DESCR + BANJO.FEEDBACK_COLON,
		        strSettingChoice, null, lineLength ) );
		
		strSettingChoice = _processData.getValidatedProcessParameter( 
		        BANJO.SETTING_USER );
		searcherFeedback.append( StringUtil.formatRightLeftJustified( 
		        newLinePlusPrefix, 
		        BANJO.SETTING_USER_DESCR + BANJO.FEEDBACK_COLON,
		        strSettingChoice, null, lineLength ) );
		
		strSettingChoice = _processData.getValidatedProcessParameter( 
		        BANJO.SETTING_DATASET );
		searcherFeedback.append( StringUtil.formatRightLeftJustified( 
		        newLinePlusPrefix, 
		        BANJO.SETTING_DATASET_DESCR + BANJO.FEEDBACK_COLON,
		        strSettingChoice, null, lineLength ) );
		
		strSettingChoice = _processData.getValidatedProcessParameter( 
		        BANJO.SETTING_NOTES );
		searcherFeedback.append( StringUtil.formatRightLeftJustified( 
		        newLinePlusPrefix, 
		        BANJO.SETTING_NOTES_DESCR + BANJO.FEEDBACK_COLON,
		        strSettingChoice, null, lineLength ) );
		 
        return searcherFeedback;
    }

    
    /**
     * Generates the report file name for a given thread in the multi-threaded scenario.
     * 
     * @param _reportFileName The base part of the report file name (i.e., the one
     *                          used by the main thread
     * 
     * @param _prefix           The prefix to add to file name. In case this string contains
     *                          a token for the thread index (ID), we replace the token with 
     *                          the supplied ID.
     * 
     * @param _threadIndex      The index of the thread that makes the request
     * 
     * @return String The name of the report file for the specified thread.
     */
    public static String getReportFileNameMT( String _reportFileName, 
            String _prefix, int _threadIndex ) {
        
        String reportFileName;
        String strToken;
        String defaultPrefix;
            
        // If the user specified a valid prefix, we will use that;
        // otherwise we'll use our default string (we want the outputs of the 
        // individual threads to go to separate files; a single file is very hard
        // to read because of the asynchronous nature of the threads' output)
        strToken = BANJO.DATA_THREADID_TOKEN.toLowerCase();
        defaultPrefix = BANJO.SETTING_FILENAMEPREFIXFORTHREADS_DEFAULT;
        reportFileName = _prefix + _reportFileName;
        
        if ( _prefix.toLowerCase().indexOf( strToken ) < 0 ) {
                                    
            reportFileName = defaultPrefix + _reportFileName;
        }

        int indexOfSubstring = reportFileName.toLowerCase().indexOf( strToken.toLowerCase() ); 
       
        // need to do a simple splitting of the string, because we want to preserve the
        // case-sensitivity of the supplied file name
        String part1 = reportFileName.substring( 0 , indexOfSubstring );
        String part2 = reportFileName.substring( indexOfSubstring + strToken.length() );
        reportFileName = part1 + ( new Integer( _threadIndex+1 )).toString() + part2;
        
        return reportFileName;
    }

    /**
     * Applies formatting to the elapsed time, based on supplied parameters.
     * 
     * @param _elapsedTime The time to format (expected to be in milliseconds).
     * @param _decimalsToDisplay The number of decimals to format the time with.
     * @param _formatFlag The format to apply to the supplied time (plain number with
     * qualifier, or in special d:h:m:s format).
     * 
     * @return StringBuffer The time in the specified format.
     */
    // Formatting of the elapsed time, based on some parameters:
    // - decimalsToDisplay controls how many decimals are left after we appply some
    //   rounding
    // - formatFlag determines whether we express the result in [hrs only], [min only],
    //   ..., and a mixed format that folds the elapsed time into the display format.
    // - elapsedTime is provided by the caller in milliseconds
    public static StringBuffer formatElapsedTime(
            double _elapsedTime, int _decimalsToDisplay, final int _formatFlag ) {
	    
		StringBuffer formattedElapsedTime = new StringBuffer(40);
		double dblElapsedTime;
		int decimalFactor = 1;
		
		if ( _elapsedTime < 0 ) _elapsedTime = 0;
		
		if ( _decimalsToDisplay < 0 ) {
		    _decimalsToDisplay = BANJO.FEEDBACK_NUMBEROFDECIMALSINTIMEDISPLAY;
		}
		
		if ( _decimalsToDisplay > 0 ) {
		    for (int i=0; i<_decimalsToDisplay; i++) decimalFactor *=10;
		}
		
		if ( _formatFlag == BANJO.FEEDBACK_OPTION_TIMEFORMAT_MIXED  ) {
			
		    // Note: the cutoffs when using different formats are somewhat arbitrary
            if ( _elapsedTime > BANJO.FEEDBACK_DEFAULT_CUTOFF_TO_DAYS * MILLISECSPERDAY ) {
                
                // Display time in hours
                dblElapsedTime = Math.round( decimalFactor * 
                        _elapsedTime/MILLISECSPERDAY );
                dblElapsedTime = dblElapsedTime / decimalFactor;
                if ( _decimalsToDisplay == 0 ) 
                    dblElapsedTime = Math.round( dblElapsedTime );
                formattedElapsedTime.append( dblElapsedTime );
                formattedElapsedTime.append( " " );
                formattedElapsedTime.append( BANJO.FEEDBACK_DEFAULT_TIME_DAYS );
            }
            else if ( _elapsedTime > BANJO.FEEDBACK_DEFAULT_CUTOFF_TO_HOURS * MILLISECSPERHOUR ) {
			    
				// Display time in hours
			    dblElapsedTime = Math.round( decimalFactor * 
			            _elapsedTime/MILLISECSPERHOUR );
			    dblElapsedTime = dblElapsedTime / decimalFactor;
			    if ( _decimalsToDisplay == 0 ) 
			        dblElapsedTime = Math.round( dblElapsedTime );
				formattedElapsedTime.append( dblElapsedTime );
				formattedElapsedTime.append( " " );
				formattedElapsedTime.append( BANJO.FEEDBACK_DEFAULT_TIME_HOURS );
			}
			else if ( _elapsedTime > BANJO.FEEDBACK_DEFAULT_CUTOFF_TO_MINUTES * 
			        MILLISECSPERMINUTE ) {
			    
				// Display time in minutes
			    dblElapsedTime = Math.round( decimalFactor * 
			            _elapsedTime/MILLISECSPERMINUTE );
			    dblElapsedTime = dblElapsedTime / decimalFactor;
			    if ( _decimalsToDisplay == 0 ) 
			        dblElapsedTime = Math.round( dblElapsedTime );
				formattedElapsedTime.append( dblElapsedTime );
				formattedElapsedTime.append( " " );
				formattedElapsedTime.append( BANJO.FEEDBACK_DEFAULT_TIME_MINUTES );
			}
			else if ( _elapsedTime > BANJO.FEEDBACK_DEFAULT_CUTOFF_TO_SECONDS * 
			        MILLISECSPERSECOND ) {
			    
				// Display time in seconds
			    dblElapsedTime = Math.round( decimalFactor * 
			            _elapsedTime/MILLISECSPERSECOND );
			    dblElapsedTime = dblElapsedTime / decimalFactor;
			    if ( _decimalsToDisplay == 0 ) 
			        dblElapsedTime = Math.round( dblElapsedTime );
				formattedElapsedTime.append( dblElapsedTime );
				formattedElapsedTime.append( " " );
				formattedElapsedTime.append( BANJO.FEEDBACK_DEFAULT_TIME_SECONDS );
			}
			else {
			    
				// Display time in ms for times <= N seconds (see previous if-condition)
				formattedElapsedTime.append( _elapsedTime );
				formattedElapsedTime.append( " " );
				formattedElapsedTime.append( BANJO.FEEDBACK_DEFAULT_TIME_MILLISECS );
			}
		}
        else if ( _formatFlag == BANJO.FEEDBACK_OPTION_TIMEFORMAT_IN_D ) {           

            dblElapsedTime = Math.round( decimalFactor * 
                    _elapsedTime/MILLISECSPERDAY );
            dblElapsedTime = dblElapsedTime / decimalFactor;
            formattedElapsedTime.append( dblElapsedTime );
            formattedElapsedTime.append( " " );
            formattedElapsedTime.append( BANJO.FEEDBACK_DEFAULT_TIME_DAYS );
        }
        else if ( _formatFlag == BANJO.FEEDBACK_OPTION_TIMEFORMAT_IN_H ) {           

            dblElapsedTime = Math.round( decimalFactor * 
                    _elapsedTime/MILLISECSPERHOUR );
            dblElapsedTime = dblElapsedTime / decimalFactor;
            formattedElapsedTime.append( dblElapsedTime );
            formattedElapsedTime.append( " " );
            formattedElapsedTime.append( BANJO.FEEDBACK_DEFAULT_TIME_HOURS );
        }
		else if ( _formatFlag == BANJO.FEEDBACK_OPTION_TIMEFORMAT_IN_M ) {		    
		    
		    dblElapsedTime = Math.round( decimalFactor * 
		            _elapsedTime/MILLISECSPERMINUTE );
		    dblElapsedTime = dblElapsedTime / decimalFactor;
			formattedElapsedTime.append( dblElapsedTime );
			formattedElapsedTime.append( " " );
			formattedElapsedTime.append( BANJO.FEEDBACK_DEFAULT_TIME_MINUTES );
		}
		else if ( _formatFlag == BANJO.FEEDBACK_OPTION_TIMEFORMAT_IN_S ) {
			
		    dblElapsedTime = Math.round( decimalFactor * 
		            _elapsedTime/MILLISECSPERSECOND );
		    dblElapsedTime = dblElapsedTime / decimalFactor;
			formattedElapsedTime.append( dblElapsedTime );
			formattedElapsedTime.append( " " );
			formattedElapsedTime.append( BANJO.FEEDBACK_DEFAULT_TIME_SECONDS );
		}
		else if ( _formatFlag == BANJO.FEEDBACK_OPTION_TIMEFORMAT_IN_MS ) {

			formattedElapsedTime.append( _elapsedTime );
			formattedElapsedTime.append( " " );
			formattedElapsedTime.append( BANJO.FEEDBACK_DEFAULT_TIME_MILLISECS );
		}
		else {

		    // default format in ms?
			formattedElapsedTime.append( _elapsedTime );
			formattedElapsedTime.append( " " );
			formattedElapsedTime.append( BANJO.FEEDBACK_DEFAULT_TIME_MILLISECS );
		}
		
		return formattedElapsedTime;
	}

    /**
     * Utility function for applying formatting to numbers.
     * @param _numberToFormat The number to format with the _formatToApply.
     * @param _formatToApply The format to apply to the supplied number
     * 
     * @return The formatted number as a string.
     */
    // 10/13/2005 hjs
    // Better (more robust) method for applying formatting to numbers
    public static String formatDecimalDisplay( 
            final double _numberToFormat,
            final String _formatToApply ) {
        
        String formattedNumber;
        
        // hjs 7/18/2007 When no number is passed in, or the format is invalid,
        // simply return the passed-in value (note: value can be NaN, which we can't format)
        // hjs 9/27/2007 Fix a localization issue (thanks to F. Menolascina and M. Clerx)
        try {
            
            DecimalFormat decimalFormatForDisplay = new DecimalFormat( _formatToApply,
                    new DecimalFormatSymbols( Locale.US ) );
            
            formattedNumber = decimalFormatForDisplay.format( _numberToFormat );
        }
        catch ( Exception e ) {
            
            // Simply use whatever number was supplied (including NaN, etc)
            formattedNumber = new Double( _numberToFormat ).toString();
        }
        
        return formattedNumber;
    }

    /**
     * Utility function for listing a node list (parent list), in the form
     * array[variableIndex][lagIndex]. Useful for testing/debugging.
     * @param _nodeList The list of nodes to be listed.
     * 
     * @return The string describing the list of nodes.
     */
	public static String listNodes( final int[][] _nodeList ) {
	    
	    String strNodeList;
	    
	    if ( _nodeList.length > 0 ) {
	        
		    strNodeList = new String( Integer.toString( _nodeList[0][0] ));
		    
		    for (int i = 1; i<_nodeList.length; i++ ) {
		        strNodeList = strNodeList + ",  " + _nodeList[i][0];
		    }
	    }
	    else {
	        
	        strNodeList = "none";
	    }
	    
	    return strNodeList;	    
	}

    /**
     * Composes an error message that resulted from a comparison between 2 setting items.
     * @param  _settingItem The setting item.
     * @param  _strCondition The condition string that the setting item needs to satisfy.
     * 
     * @return The string consisting of the error message.
     */
    public static String composeErrorMessage( 
            final SettingItem _settingItem, 
            final String _strCondition ) {
        
        return "The value of the setting '" + _settingItem.getItemNameCanonical() + 
				"' (='" + _settingItem.getItemValueAsLoaded() +
				"') needs to be " + _strCondition + ".";
    }

    /**
     * Composes an error message that resulted from a comparison between 2 setting items.
     * @param  _settingItem1 The first setting item.
     * @param  _settingItem2 The second setting item.
     * @param  _strCondition The condition string that the second setting item needs
     * to satisfy in relation to the first setting item.
     * 
     * @return The string consisting of the error message.
     */
    public static String composeErrorMessage( 
            final SettingItem _settingItem1, 
            final SettingItem _settingItem2, 
            final String _strCondition ) {

        return "The value of the setting '" + _settingItem1.getItemNameCanonical() + 
			    "' (='" + _settingItem1.getItemValueAsLoaded() +
				"') needs to be " + _strCondition + " that of setting '" + 
				_settingItem2.getItemNameCanonical() +
				"' (='" + _settingItem2.getItemValueAsLoaded() +
				"').";
    }

	/**
	 * Composes a basic string that is a time stamp based on the supplied format.
	 * If the suplied format is not usable, the code uses a default format, so it always
	 * returns a valid time stamp. Note that the first computed time stamp is cached for
     * later re-use, i.e., the time stamp computation is only done once.
     * 
     * @param _processData The underlying (main) settings object, for placing any errorItems.
     * @param _settingName The name of the setting.
     * @param _timeStampFormat The time stamp format to be applied in creating a time stamp.
     * @param _defaultTimeStampFormat The default time stamp format to be used if the 
     * supplie _timeStampFormat is not a valid one.
	 * 
	 * @return The string consisting of the created time stamp.
	 */
    public static String timeStamp( 
            final Settings _processData,
            final String _settingName, 
            final String _timeStampFormat, 
            String _defaultTimeStampFormat ) {
        
        // Note: we don't pass a settingItem instead of the settingName to this method,
        // because this function gets called so early in the processing of the settings 
        // that we may not have access to a settingItem.
           
        
        if ( !cachedTimeStamp.equals("") ) {
            
            return cachedTimeStamp;
        }
        
        Format formatter;
            
        // Get today's date
        Date date = new Date();
        
        try {
            
	        // Formatting the time for our desired format
	        formatter = new SimpleDateFormat( _timeStampFormat );
	        cachedTimeStamp = formatter.format( date );
        }
        catch ( Exception e ) {
            
            if ( _defaultTimeStampFormat == null || 
                    _defaultTimeStampFormat.trim().equals("") ) {
                
                _defaultTimeStampFormat = BANJO.DEFAULT_TIMESTAMP;
            }
            // Generally, the only error we should encounter here is an invalid
            // format. In case this happens, we use the default value, and record
            // a warning message
            _processData.addToWarnings( new BanjoError( 
                "The format for the supplied time stamp ('" +
                _timeStampFormat + "') for setting '" +
                _settingName + "' is not valid. " +
                BANJO.APPLICATION_NAME + " will use the supplied default value (='" +
                _defaultTimeStampFormat + "') instead.",
	            BANJO.ERRORTYPE_ALERT_CORRECTEDCHOICE,
                _settingName,
        		null ) );
            
            try {
                
	            formatter = new SimpleDateFormat( _defaultTimeStampFormat );
		        cachedTimeStamp = formatter.format(date);
            }
            catch ( Exception ex ) {
                
                // In case the supplied default value is not usable:
                
                _processData.addToWarnings( new BanjoError( 
	                "The format for the supplied default time stamp ('" +
	                _timeStampFormat + "') for setting '" +
                    _settingName + "' is not valid. " +
	                BANJO.APPLICATION_NAME + " will use the system default value (='" +
	                BANJO.DEFAULT_TIMESTAMP + "') instead.",
		            BANJO.ERRORTYPE_ALERT_CORRECTEDCHOICE,
                    _settingName,
            		null ) );
                
                formatter = new SimpleDateFormat( BANJO.DEFAULT_TIMESTAMP );
		        cachedTimeStamp = formatter.format(date);
            }
        }
        
        return cachedTimeStamp;
    }

	/**
	 * Composes a basic string that details the memory use of the application
	 * at the current moment.
	 * 
	 * @return The string containing the memory use information.
	 */
    public static String compileMemoryInfo( final String _info ) {
        
        StringBuffer memoryInfo = new StringBuffer();
        long maxMem;
        long totalMem;
        long freeMem;

        Runtime.getRuntime().gc();

        totalMem = Runtime.getRuntime().totalMemory();
        freeMem = Runtime.getRuntime().freeMemory();
        maxMem = Runtime.getRuntime().maxMemory();

        if ( _info != null && !_info.equals("") ) {

            memoryInfo.append( _info );
            memoryInfo.append( BANJO.FEEDBACK_SPACE );
        }
        
        memoryInfo.append( "Banjo is using " + (totalMem - freeMem)/(1024*1024) + 
                " mb of memory" );
        
        // Omit: not that useful
//        memoryInfo.append( BANJO.FEEDBACK_NEWLINE );
//        memoryInfo.append( "     " + totalMem/(1024*1024) + " mb " +
//                "is the total amount of memory actually in use by Banjo." );
//        
//        memoryInfo.append( BANJO.FEEDBACK_NEWLINE );
//        memoryInfo.append( "     " + freeMem/(1024*1024) + " mb " +
//        		"is the size of the memory currently made available to Banjo " +
//        		"by the JVM." );
//
//        memoryInfo.append( BANJO.FEEDBACK_NEWLINE );
//        memoryInfo.append( "     " + maxMem/(1024*1024) + " mb " +
//        		"is the max. amount of memory assigned initially to the JVM " +
//        		"(controllable via the -Xmx option)." );
        
        return memoryInfo.toString();
    }
}
