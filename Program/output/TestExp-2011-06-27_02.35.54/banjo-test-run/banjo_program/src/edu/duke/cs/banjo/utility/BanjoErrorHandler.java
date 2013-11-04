/*
 * Created on Oct 11, 2004
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

import edu.duke.cs.banjo.data.settings.SettingItem;
import edu.duke.cs.banjo.data.settings.Settings;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Combines the exception handling for various front-end classes.
 *
 * <p><strong>Details:</strong> <br>
 * 
 * - BayesNetExceptions are handled separately, since their special
 * treatment within our code enables us to provide more detailed
 * feedback to the user about the potential source of the problem. <br>
 * 
 * - All other exceptions are treated fairly generically. <br>
 * 
 * - Output includes writing the error to the designated error file, as well
 * as to the command line. <br>
 * 
 * - Note: for exceptions where the associated message provides exhaustive
 * information, a separate case is listed. Generally, such errors can likely
 * be "fixed" by the user by correcting some input value, etc.
 * All other errors are combined in the default case, since they will likely
 * require developer intervention. <br>

 * <p><strong>Change History:</strong> <br>
 * Created on Oct 11, 2004
 * <p>
 * 2005/10/20 (v2.0) hjs	Add more detail to the error messages.
 * 
 * <p>
 * hjs (v2.1)               Add condition around validateData() call, to avoid errors
 *                          due to bootstrap timing.
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class BanjoErrorHandler {
        
    protected Settings processData;
    protected StringBuffer additionalInfo = new StringBuffer( BANJO.BUFFERLENGTH_STAT );

    public BanjoErrorHandler() {
        // nothing to do
    };
    
    public BanjoErrorHandler( final Settings _processData ) {
        
        processData = _processData;
        
        try {
        
            if ( processData != null ) {
            
                validateRequiredData();
            }
        }
        catch ( Exception ex ) {
            
            System.out.println( "[BanjoErrorHandler] Could not validate " +
                "the required data for setting up the error handler." +
                "\nThe Banjo developers apologize for this problem." +
                "\nPlease record all information pertinent to this error " +
                "and contact the Banjo developers. " +
                "\nThank you for your cooperation.\n\n" +
                "Original error message:\n" +
                ex.getMessage() + "\n\n" 
                );
            
            if ( BANJO.DEBUG || 
                    processData.getSettingItemValueAsValidated( 
                            BANJO.SETTING_DISPLAYDEBUGINFO ).equals( 
                                    BANJO.UI_DEBUGINFO_STACKTRACE ) ) 
                ex.printStackTrace();
        }
    }
    
	/**
	 * Validates the settings values for the BanjoErrorHandler.
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
		
	    // Validate
	    settingNameCanonical = BANJO.SETTING_DISPLAYDEBUGINFO;
	    settingNameDescriptive = BANJO.SETTING_DISPLAYDEBUGINFO_DESCR;
	    settingNameForDisplay = BANJO.SETTING_DISPLAYDEBUGINFO_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            validChoices(), 
	            BANJO.SETTING_DEFAULT_DEBUGINFO );

	    return isDataValid;
	}

    /**
     * Provides the valid choices for validation relevant to this class
     */
    public Object validChoices() {
	    
		Set validValues = new HashSet();
		
	    validValues.add( BANJO.UI_DEBUGINFO_STACKTRACE );
	    validValues.add( BANJO.UI_DEBUGINFO_NONE );
	    
	    return validValues;
	}
    
    /**
     * Process any encountered Banjo exception  <br>
     *  
     * @param e The BanjoException to process.
     */
    public void handleApplicationException( final BanjoException e ) {
        
        StringBuffer errorMessage = new StringBuffer( BANJO.BUFFERLENGTH_STAT );
        
//        try {
//            errorMessage.append( processData.getOptionalThreadInfo() );
//        }
//        catch ( Exception ex ) {
//            
//            // Swallow any exception
//        } 
        
        try {
            
	        String strMessage;
			//DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
			DateFormat timeFormat = DateFormat.getDateTimeInstance( DateFormat.SHORT, 
			        DateFormat.MEDIUM );
			String timeStamp = timeFormat.format(new Date());
	

	        int lineLength = BANJO.FEEDBACK_LINELENGTH;
            
            errorMessage.append( composeErrorSignature () );
		    
		    int exceptionType = e.getExceptionType();
		    
		    // Note: for exceptions where the associated message provides exhaustive
		    // information, a separate case is listed. Generally, such errors can likely
		    // be "fixed" by the user by correcting some input value, etc.
		    // All other errors are combined in the default case, since they will likely
		    // require developer intervention.
		    
		    switch ( exceptionType )
		    {	    	    
	    	    		    
	    	case BANJO.ERROR_BANJO_DEV:
	    	    
	    	    errorMessage.append( 
	    	            "[Development-related error: This message may be " +
	    	            "generated to remind the Banjo developers to complete " +
	    	            "or restructure a section of code] " +
                        "\nError details -- " );
                
                strMessage = e.getMessage();
                if ( strMessage == null || strMessage.length() == 0 ) {
        
                    errorMessage.append(
                        "Please notify the developer that Banjo provided the following info: \n" +
                        "  'exceptionType=' " + exceptionType );
                }
                else {
                    errorMessage.append(
                        "Please notify the developer that Banjo provided the following info: \n" );
                    errorMessage.append( e.getMessage() );
                }
		    
	    	    break;
	    	    		    	  
	    	default: 
	    	    
		    	strMessage = e.getMessage();
				if ( strMessage == null || strMessage.length() == 0 ) {
		
					errorMessage.append(
                        "The following info [possibly relevant only to a developer]" +
                        " is provided to assist in trouble-shooting: \n" +
                        "  'exceptionType=' " + exceptionType );
				}
				else {
				    errorMessage.append(
                        "The following info is provided to assist in trouble-shooting: \n");
				    errorMessage.append( e.getMessage() );
				}
    	        	
		       	break;
		    }

            errorMessage.append( BANJO.FEEDBACK_NEWLINE );
            errorMessage.append( processData.compileErrorMessages() );
            errorMessage.append( BANJO.FEEDBACK_NEWLINE );
            errorMessage.append( processData.compileWarningMessages() );  

            errorMessage.append( BANJO.FEEDBACK_NEWLINE );
            errorMessage.append( "\nStack trace info:" );
            errorMessage.append( BANJO.FEEDBACK_NEWLINE );
            errorMessage.append( displayBasicStackTrace( e ) );
            
            errorMessage.append( BANJO.FEEDBACK_NEWLINE );
            errorMessage.append( additionalInfo );
            errorMessage.append( composeErrorClosing() );      
            
		    // Record the error message to the error log file
		    try {
                
		        processData.recordError( errorMessage.toString() );
		    }
	        catch ( Exception ex ) {
	            
	            System.out.println( "[BanjoErrorHandler][1] Oops! Error while trying to " +
	            		"record an error that was encountered while running Banjo." +
	            		"\nThe Banjo developers apologize for this problem." +
	            		"\nPlease record all information pertinent to this error " +
	            		"and contact the Banjo developers. " +
	            		"\nThank you for your cooperation.\n\n" +
                        "Original error message:\n" +
                        errorMessage.toString() +
                        "Error message while trying to record the error:\n" +
                        ex.getMessage() + "\n\n" );
	        }
		    
			
//	        if ( processData != null && ( BANJO.DEBUG || 
//			        processData.getSettingItemValueAsValidated( 
//			                BANJO.SETTING_DISPLAYDEBUGINFO ).equals( 
//			                        BANJO.UI_DEBUGINFO_STACKTRACE ) ) ) {
//	            
//			    e.printStackTrace();
//	        }
	    }
        catch ( Exception ex ) {
            
            System.out.println( "[BanjoErrorHandler][2] Oops! Error trying to process " +
        		"an error that was encountered while running Banjo." +
        		"\nThe Banjo developers apologize for this problem." +
        		"\nPlease record all information pertinent to this error " +
        		"and contact the Banjo developers. " +
        		"\nThank you for your cooperation.\n\n" +
                "Original error message:\n" +
                errorMessage.toString() +
                "Error message while trying to record the error:\n" +
                ex.getMessage() + "\n\n" );
            
			if ( BANJO.DEBUG || 
			        processData.getSettingItemValueAsValidated( 
			                BANJO.SETTING_DISPLAYDEBUGINFO ).equals( 
			                        BANJO.UI_DEBUGINFO_STACKTRACE ) ) 
			    e.printStackTrace();
        }
    }
    
    /**
     * Process any encountered Banjo exception  <br>
     *  
     * @param e The BanjoException to process.
     * @param _additionalInfo Additional info, e.g., the settings for the search.
     */
    public void handleApplicationException( 
            final BanjoException e, final Object _additionalInfo ) {
        
        if ( _additionalInfo instanceof Settings ){
            
            processData = ( Settings ) _additionalInfo;
            
            if ( BANJO.DEBUG ){
                
                additionalInfo.append( "\n\n" );
                additionalInfo.append( _additionalInfo.toString() );
            }
        }

        handleApplicationException( e );
    }

    /**
     * Process any unexpected exception  <br>
     *  
     * @param _exception The BanjoException to process.
     */
    public void handleGeneralException( final Exception _exception ) {

        StringBuffer errorMessage = new StringBuffer( BANJO.BUFFERLENGTH_STAT );

//        try {
//            errorMessage.append( processData.getOptionalThreadInfo() );
//        }
//        catch ( Exception e ) {
//            
//            // Swallow any exception
//        }
        
        errorMessage.append( composeErrorSignature () );
        
        errorMessage.append( 
                "Execution has stopped due to the following exception: \n'" +
                _exception.toString() + "'\n" );
        
        errorMessage.append( additionalInfo );

        errorMessage.append( "\nStack trace info:\n" );
        errorMessage.append( displayBasicStackTrace( _exception ) );

        errorMessage.append( composeErrorClosing() );

        try {
            
            processData.recordError( errorMessage.toString() );
        }
        catch ( Exception ex ) {
            
            System.out.println( "[BanjoErrorHandler.handleGeneralException] " +
                    "Oops! Error while trying to record " +
                    "an error that was encountered while running Banjo." +
                    "\nThe Banjo developers apologize for this problem." +
                    "\nPlease record all information pertinent to this error " +
                    "and contact the Banjo developers. " +
                    "\nThank you for your cooperation.\n\n" +
                    "Original error message:\n" +
                    errorMessage.toString() +
                    "Error message while trying to record the error:\n" +
                    ex.getMessage() + "\n\n" );
        }
        
//        if ( BANJO.DEBUG || 
//                processData.getSettingItemValueAsValidated( 
//                        BANJO.SETTING_DISPLAYDEBUGINFO ).equals( 
//                                BANJO.UI_DEBUGINFO_STACKTRACE ) ) 
//            
//            _exception.printStackTrace();
    }

    /**
     * Process any unexpected exception  <br>
     *  
     * @param _exception The BanjoException to process.
     * @param _additionalInfo Additional info, e.g., the settings for the search.
     */
    public void handleGeneralException( final Exception _exception, 
            final Object _additionalInfo ) {

 		if ( BANJO.DEBUG && _additionalInfo instanceof Settings ){
            
            additionalInfo.append( _additionalInfo.toString() );
		}

        handleGeneralException( _exception );
   }

    /**
     * Process any out-of-memory errors  <br>
     *  
     * @param _exception The BanjoException to process.
     * @param _additionalInfo Additional info, e.g., the settings for the search.
     */

    public void handleOutOfMemoryError( final OutOfMemoryError _exception, 
            final Object _additionalInfo ) {

        StringBuffer errorMessage = new StringBuffer( BANJO.BUFFERLENGTH_STAT );
        errorMessage.append( composeErrorSignature () );
        
        errorMessage.append(
                "Execution has stopped because Banjo has run out of available memory. \n'" +
                _exception.toString() + "'\n" );
        
        if ( BANJO.DEBUG && _additionalInfo instanceof Settings ){
            
            errorMessage.append( _additionalInfo.toString() );
        }
        
        errorMessage.append( composeErrorClosing() );

        try {
            
            processData.recordError( errorMessage.toString() );
        }
        catch ( Exception ex ) {
            
            System.out.println( "[BanjoErrorHandler.handleOutOfMemoryError] " +
                    "Oops! Error trying to record an error " +
                    "that was encountered while trying to run Banjo." +
                    "\nThe Banjo developers apologize for this problem." +
                    "\nPlease record all information pertinent to this error " +
                    "and contact the Banjo developers. " +
                    "\nThank you for your cooperation.\n\n" +
                    "Original error message:\n" +
                    errorMessage.toString() +
                    "Error message while trying to record the error:\n" +
                    ex.getMessage() + "\n\n" );
        }

        if ( BANJO.DEBUG || 
                processData.getSettingItemValueAsValidated( 
                        BANJO.SETTING_DISPLAYDEBUGINFO ).equals( 
                                BANJO.UI_DEBUGINFO_STACKTRACE ) ) 
            _exception.printStackTrace();
    }
    
    public StringBuffer composeErrorSignature() {

        //DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
        DateFormat timeFormat = DateFormat.getDateTimeInstance( DateFormat.SHORT, 
                DateFormat.MEDIUM );
        String timeStamp = timeFormat.format(new Date());

        StringBuffer errorMessage = new StringBuffer( BANJO.BUFFERLENGTH_STAT );
        StringBuffer optionalThreadInfo = new StringBuffer();

        try {
            optionalThreadInfo.append( processData.getOptionalThreadInfo() );
        }
        catch ( Exception ex ) {
            
            // Swallow any exception
        } 
        
        int lineLength = BANJO.FEEDBACK_LINELENGTH;
        
        errorMessage.append( 
                BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.substring( 0, lineLength ));
        errorMessage.append( BANJO.FEEDBACK_NEWLINE );
        // When there's no thread info, we need to adjust the blanks before the
        // text that follows
        if ( optionalThreadInfo.length() > 0 ) {
        
            errorMessage.append( optionalThreadInfo );
        }
        else {
            
            errorMessage.append( "          " );
        }
        errorMessage.append( "                       ERROR DETAILS" );
        errorMessage.append( 
                BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.substring( 0, lineLength ));

        // Start with the Banjo header
        errorMessage.append( StringUtil.getBanjoSignature() );
        errorMessage.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.substring( 0, lineLength ));
        
        // Add the "job" info
        if ( processData != null ) {
        
            errorMessage.append( StringUtil.getJobSignature( processData ) );
            errorMessage.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.substring( 0, lineLength ));
        }
        
        // Add the error info
        errorMessage.append( "\n\n" );
        errorMessage.append( "[" );
        errorMessage.append( "ERROR: " );
        errorMessage.append( BANJO.APPLICATION_NAME );
        errorMessage.append( " " );
        errorMessage.append( BANJO.APPLICATION_VERSIONNUMBER );
        errorMessage.append( ", " );
        errorMessage.append( timeStamp );
        errorMessage.append( "]" );
        errorMessage.append( "\n" );
        
        return errorMessage;
    }
    
    public StringBuffer composeErrorClosing() {

        StringBuffer errorMessage = new StringBuffer( BANJO.BUFFERLENGTH_STAT );
        int lineLength = BANJO.FEEDBACK_LINELENGTH;
        StringBuffer optionalThreadInfo = new StringBuffer();

        try {
            optionalThreadInfo.append( processData.getOptionalThreadInfo() );
        }
        catch ( Exception ex ) {
            
            // Swallow any exception
        } 
        
        errorMessage.append( 
                BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.substring( 0, lineLength ));
        errorMessage.append( BANJO.FEEDBACK_NEWLINE );
        // When there's no thread info, we need to adjust the blanks before the
        // text that follows
        if ( optionalThreadInfo.length() > 0 ) {
        
            errorMessage.append( optionalThreadInfo );
        }
        else {
            
            errorMessage.append( "          " );
        }
        errorMessage.append( "                 End of error notification" );
        errorMessage.append( 
                BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.substring( 0, lineLength ));
        errorMessage.append( "\n" );
        
        return errorMessage;
    }
    
    public StringBuffer displayBasicStackTrace( Throwable _exception ) {

        StringBuffer basicStackTrace = new StringBuffer( BANJO.BUFFERLENGTH_STAT );

        StackTraceElement[] stackElements = _exception.getStackTrace();
        
        for (int i = 0; i < stackElements.length; i++) {
            
            basicStackTrace.append( BANJO.FEEDBACK_NEWLINE );
            basicStackTrace.append( stackElements[i].toString() );
        
//            String className = stackElements[i].getClassName();
//            
//            basicStackTrace.append( "File = " +
//                    stackElements[i].getFileName() );
//            
////            basicStackTrace.append( BANJO.FEEDBACK_NEWLINE );
//            basicStackTrace.append( ", Line = " +
//                    stackElements[i].getLineNumber() );
//            
////            basicStackTrace.append( BANJO.FEEDBACK_NEWLINE );
//            basicStackTrace.append( ", Class = " + className );
//            
////            basicStackTrace.append( BANJO.FEEDBACK_NEWLINE );
//            basicStackTrace.append( ", Method = " +
//                    stackElements[i].getMethodName() );
//            
////            basicStackTrace.append( BANJO.FEEDBACK_NEWLINE );
////            basicStackTrace.append( "Native method?: " +
////                    stackElements[i].isNativeMethod() );
//            
////            basicStackTrace.append( BANJO.FEEDBACK_NEWLINE );
//            basicStackTrace.append( ", toString() = " +
//                    stackElements[i].toString() );
        
//        System.out.println ("File name: " +
//                stackElements[lcv].getFileName());
//        System.out.println ("Line number: " +
//                stackElements[lcv].getLineNumber());
//
//        String className = stackElements[lcv].getClassName();
////        String packageName = extractPackageName (className);
////        String simpleClassName = extractSimpleClassName (className);
//
////        System.out.println ("Package name: " +
////                ("".equals (packageName)?
////                "[default package]" : packageName));
//        System.out.println ("Full class name: " + className);
////        System.out.println ("Simple class name: " + simpleClassName);
////        System.out.println ("Unmunged class name: " +
////                unmungeSimpleClassName (simpleClassName));
////        System.out.println ("Direct class name: " +
////                extractDirectClassName (simpleClassName));
//
//        System.out.println ("Method name: " +
//                stackElements[lcv].getMethodName());
//        System.out.println ("Native method?: " +
//                stackElements[lcv].isNativeMethod());
//
//        System.out.println ("toString(): " +
//                stackElements[lcv].toString());
//        System.out.println ("");

//        // Only continue if the caller really wanted all of the
//        // elements displayed.
//        if (!displayAll)
//        return true;
//        }
        
//        basicStackTrace.append(  );
        
        }
        return basicStackTrace;
    }
}
