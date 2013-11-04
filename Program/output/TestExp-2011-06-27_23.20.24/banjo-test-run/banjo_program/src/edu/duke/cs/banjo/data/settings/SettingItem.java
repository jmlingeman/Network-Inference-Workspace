/*
 * Created on Nov 4, 2005
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
package edu.duke.cs.banjo.data.settings;

import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import edu.duke.cs.banjo.utility.BANJO;
import edu.duke.cs.banjo.utility.BanjoException;
import edu.duke.cs.banjo.utility.BanjoError;

/**
 * Holds an individual setting that is loaded into Banjo.
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Nov 4, 2005 <br>
 * Part of a large-scale refactoring of the settings processing.
 * 
 * <p>
 * hjs (v2.1)   Add constructor based on existing settingItem
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public abstract class SettingItem {

    protected final String itemNameCanonical;
    protected final String itemNameForComparison;
    protected final String itemNameDescriptive;
    protected final String itemNameForDisplay;
    
    protected String itemNameFormattedForDisplay = new String();
    protected String itemValueAsLoaded = new String();
    protected String itemValueValidated = new String();
    protected Object objItemValue;
    protected Object objItemInfo;
    protected boolean isValidSetting;
    protected boolean isDefaultValueUsed = false;
    
    // Store any error information internally, so we can process any number
    // of validation errors "together" within the calling code; also, attach
    // any number of rule violations of a given setting item, if necessary.
	protected Set collectedErrors = new HashSet();
	
	protected abstract class Validator
	{
	    protected abstract boolean validate( 
                Object _itemValue, 
	            Object _additionalInfo) throws Exception;
	}
    
    protected abstract class DataTypeValidator extends Validator {
        
        String dataTypeToValidateAgainst;
        
        DataTypeValidator() {
            // nothing to do   
        }
        
        protected abstract boolean validate(
                Object _itemValue, 
                Object _additionalInfo) throws Exception;
    }
    
	protected class IntegerValidator extends DataTypeValidator {
	    
	    IntegerValidator() {
	    
	        dataTypeToValidateAgainst = BANJO.VALIDATION_DATATYPE_INTEGER;
	    }
	    
	    protected boolean validate( final Object _itemValue, 
                final Object _additionalInfo ) throws Exception {
	        
	        boolean isValidDataType = true;
	        String strItemValueToValidate = new String();
	        
	        if ( _itemValue instanceof String ) {
	            
	            strItemValueToValidate = (String) _itemValue;
	        }
	        else {

		        // This is purely a developer issue: our validation expects a string
	            throw new BanjoException( BANJO.ERROR_BANJO_DEV,
	                    "(IntegerValidator.validate) " +
	                    "Developer issue: The supplied value for the setting '" +
	                    itemNameCanonical + "' needs to be a string!" );
	        }
	        
	        
	        try {
		        
		        // Try to assign the provided value. If the data types don't match
		        // we trap the error and tell the user to fix it.
			    setObjItemValue( new Integer( strItemValueToValidate ) );
			    
			    setItemValueValidated( strItemValueToValidate );
		    }
		    catch ( Exception e ) { 

		        BanjoError errorItem = new BanjoError( 
		                errorMessageWrongDataType( 
				                getItemNameDescriptive(), 
				                getItemNameCanonical(), 
				                getItemValueAsLoaded(), 
				                dataTypeToValidateAgainst ),
			            BANJO.ERRORTYPE_MISMATCHEDDATATYPE,
			            getItemNameCanonical(),
	            		"(IntegerValidator.validate) " +
	            		"Data type is different from '" + 
	            		dataTypeToValidateAgainst + "'." );

		        collectedErrors.add( errorItem );
		        
		        isValidDataType = false;
		    }
	        
		    return isValidDataType;
	    }
	
	}
	
	protected class IntegerListValidator extends DataTypeValidator {
	    
	    IntegerListValidator() {
	    
	        dataTypeToValidateAgainst = BANJO.VALIDATION_DATATYPE_INTEGERLIST;
	    }
	    
	    protected boolean validate( final Object _itemValue, 
                final Object _additionalInfo) throws Exception {
	        
	        boolean isValidDataType = true;
	        String strItemValueAsLoaded = new String();
	        
	        if ( _itemValue instanceof String ) {
	            
	            strItemValueAsLoaded = (String) _itemValue;
	        }
	        else {

		        // This is purely a developer issue: our validation expects a string
	            throw new BanjoException( BANJO.ERROR_BANJO_DEV,
	                    "(IntegerListValidator.validate) " +
	                    "Developer issue: The supplied value for the setting '" +
	                    itemNameCanonical + "' needs to be a string!" );
	        }
	        
            
            if ( !strItemValueAsLoaded.equals( BANJO.BANJO_NOVALUESUPPLIED_STRING ) ) {
                
    		    if ( _additionalInfo instanceof Pattern ) {
    		        
    		        String[] itemValues = ( (Pattern) _additionalInfo).split( 
    		                strItemValueAsLoaded );
    	        
    		        for ( int i=0; i<itemValues.length && isValidDataType==true; i++ ) {
    			        
    			        try {
    				        
    				        // Try to assign the provided value. If the data types don't match
    				        // we trap the error and tell the user to fix it.
    					    setObjItemValue( new Integer( itemValues[i].trim() ) );
    				    }
    				    catch ( Exception e ) { 
    		
    				        BanjoError errorItem = new BanjoError( 
    				                errorMessageWrongDataType( 
    						                getItemNameDescriptive(), 
    						                getItemNameCanonical(), 
    						                strItemValueAsLoaded, 
    						                dataTypeToValidateAgainst ),
    					            BANJO.ERRORTYPE_MISMATCHEDDATATYPE,
    					            getItemNameCanonical(),
    			            		"(IntegerListValidator.validate) " +
    			            		"Data type is different from '" + 
    			            		dataTypeToValidateAgainst + "'." );
    		
    				        collectedErrors.add( errorItem );
    				        
    				        isValidDataType = false;
    				    }
    		        }
    		        if ( isValidDataType ) {
    
    		            StringBuffer tmpItemValueValidated = new StringBuffer();
    		            StringBuffer tmpItemNameFormattedForDisplay = new StringBuffer();
    		            
    		            for ( int i=0; i<itemValues.length-1; i++ ) {
    
    		                tmpItemValueValidated.append( itemValues[i] );
    		                tmpItemValueValidated.append( BANJO.DELIMITER_DEFAULT_LIST );
    
    		                tmpItemNameFormattedForDisplay.append( itemValues[i] );
    		                tmpItemNameFormattedForDisplay.append( 
                                    BANJO.DELIMITER_DEFAULT_LIST + BANJO.DELIMITER_SPACE );
    		            }
    	                tmpItemValueValidated.append( itemValues[itemValues.length-1] );
    	                tmpItemNameFormattedForDisplay.append( itemValues[itemValues.length-1] );
    		            
    				    setItemValueValidated( tmpItemValueValidated.toString() );
    				    setItemNameFormattedForDisplay( tmpItemNameFormattedForDisplay.toString() );
    		        }
    	        }
    		    else {

                    // This is purely a developer issue: our validation expects a string
                    throw new BanjoException( BANJO.ERROR_BANJO_DEV,
                            "(IntegerListValidator.validate) " +
                            "Developer issue: The supplied value for the argument " +
                            "'_additionalInfo' needs to be a pattern!" );
    		    }
            }
            else {

                setItemValueValidated( BANJO.BANJO_NOVALUESUPPLIED_STRING );               
            }
	        
		    return isValidDataType;
	    }
	}
	
	protected class LongValidator extends DataTypeValidator {
	    
	    LongValidator() {
		    
	        dataTypeToValidateAgainst = BANJO.VALIDATION_DATATYPE_LONG;
	    }
	    
	    protected boolean validate( final Object _itemValue, 
                final Object _additionalInfo) throws Exception {
	        
	        boolean isValidDataType = true;
	        String strItemValueAsLoaded = new String();
	        
	        if ( _itemValue instanceof String ) {
	            
	            strItemValueAsLoaded = (String) _itemValue;
	        }
	        else {

		        // This is purely a developer issue: our validation expects a string
	            throw new BanjoException( BANJO.ERROR_BANJO_DEV,
	                    "(LongValidator.validate) " +
	                    "Developer issue: The supplied value for the setting '" +
	                    itemNameCanonical + "' needs to be a string!" );
	        }     
	        
	        try {
		        
		        // Try to assign the provided value. If the data types don't match
		        // we trap the error and tell the user to fix it.
			    setObjItemValue( new Long( strItemValueAsLoaded ) );
			    
			    setItemValueValidated( strItemValueAsLoaded );
		    }
		    catch ( Exception e ) { 

		        BanjoError errorItem = new BanjoError( 
		                errorMessageWrongDataType( 
				                getItemNameDescriptive(), 
				                getItemNameCanonical(), 
				                getItemValueAsLoaded(), 
		                		"Integer" ),
			            BANJO.ERRORTYPE_MISMATCHEDDATATYPE,
			            getItemNameCanonical(),
	            		"(LongValidator.validate) " +
	            		"Data type is different from 'long'." );

		        collectedErrors.add( errorItem );
		        
		        isValidDataType = false;
		    }
	        
		    return isValidDataType;
	    }
	}
		
	protected class DoubleValidator extends DataTypeValidator {
	    
	    DoubleValidator() {
		    
	        dataTypeToValidateAgainst = BANJO.VALIDATION_DATATYPE_DOUBLE;
	    }
	    
	    protected boolean validate( final Object _itemValue, 
                final Object _additionalInfo) throws Exception {
	        
	        boolean isValidDataType = true;
	        String strItemValueAsLoaded = new String();
	        
	        if ( _itemValue instanceof String ) {
	            
	            strItemValueAsLoaded = (String) _itemValue;
	        }
	        else {

		        // This is purely a developer issue: our validation expects a string
	            throw new BanjoException( BANJO.ERROR_BANJO_DEV,
	                    "(DoubleValidator.validate) " +
	                    "Developer issue: The supplied value for the setting '" +
	                    itemNameCanonical + "' needs to be a string!" );
	        }     
	        
	        try {
		        
		        // Try to assign the provided value. If the data types don't match
		        // we trap the error and tell the user to fix it.
			    setObjItemValue( new Double( strItemValueAsLoaded ) );
			    
			    setItemValueValidated( strItemValueAsLoaded );
		    }
		    catch ( Exception e ) { 

		        BanjoError errorItem = new BanjoError( 
		                errorMessageWrongDataType( 
				                getItemNameDescriptive(), 
				                getItemNameCanonical(), 
				                getItemValueAsLoaded(), 
				                dataTypeToValidateAgainst ),
			            BANJO.ERRORTYPE_MISMATCHEDDATATYPE,
			            getItemNameCanonical(),
	            		"(DoubleValidator.validate) " +
	            		"Data type is different from '" + dataTypeToValidateAgainst +
	            		"'." );

		        collectedErrors.add( errorItem );
		        
		        isValidDataType = false;
		    }
	        
		    return isValidDataType;
	    }
	}
		
	protected class TimeStampValidator extends DataTypeValidator {
	    
	    TimeStampValidator() {
		    
	        dataTypeToValidateAgainst = BANJO.VALIDATION_DATATYPE_TIMESTAMP;
	    }
	    
	    protected boolean validate( final Object _itemValue, 
                final Object _additionalInfo) throws Exception {
	        
	        boolean isValidDataType = true;
	        String strItemValueAsLoaded = new String();
	        
	        if ( _itemValue instanceof String ) {
	            
	            strItemValueAsLoaded = (String) _itemValue;
	        }
	        else {

		        // This is purely a developer issue: our validation expects a string
	            throw new BanjoException( BANJO.ERROR_BANJO_DEV,
	                    "(TimeStampValidator.validate) " +
	                    "Developer issue: The supplied value for the setting '" +
	                    itemNameCanonical + "' needs to be a string!" );
	        }   
	        
	        try {

	            String timeStamp = new String();        
	            Format formatter;
	                
	            // Get today's date
	            Date date = new Date();

	            // Try to format the time based on the supplied format
		        formatter = new SimpleDateFormat( strItemValueAsLoaded );
		        timeStamp = formatter.format( date );
	            			    
			    setItemValueValidated( strItemValueAsLoaded );
		    }
		    catch ( Exception e ) { 

		        BanjoError errorItem = new BanjoError( 
		                errorMessageWrongDataType( 
				                getItemNameDescriptive(), 
				                getItemNameCanonical(), 
				                getItemValueAsLoaded(), 
				                dataTypeToValidateAgainst ),
			            BANJO.ERRORTYPE_MISMATCHEDDATATYPE,
			            getItemNameCanonical(),
			            "(TimeStampValidator.validate) " +
        				"The supplied string is not a valid '" + dataTypeToValidateAgainst +
			            "' format." );
		        
		        collectedErrors.add( errorItem );
		        
		        isValidDataType = false;
		    }
	        
		    return isValidDataType;
	    }
	}
	
	protected class StringValidator extends DataTypeValidator {

	    StringValidator() {
		    
	        dataTypeToValidateAgainst = BANJO.VALIDATION_DATATYPE_STRING;
	    }
	    
	    protected boolean validate( final Object _itemValue, 
                final Object _additionalInfo ) throws Exception {

	        boolean isValidDataType = true;
	        String strItemValueAsLoaded = new String();
	        
	        if ( _itemValue instanceof String ) {
	            
	            strItemValueAsLoaded = (String) _itemValue;
	        }
	        else {

		        // This is purely a developer issue: our validation expects a string
	            throw new BanjoException( BANJO.ERROR_BANJO_DEV,
	                    "(StringValidator.validate) " +
	                    "Developer issue: The supplied value for the setting '" +
	                    itemNameCanonical + "' needs to be a string!" );
	        }
		        
	        // Check that value is in supplied set of valid values
	        // First check if developer has supplied a set, as expected
	        if ( _additionalInfo == null ) {
	            
	            // This ensures that all string input is validated against the set
	            // of possible values (any mandatory setting won't make sense if
	            // it can't supply such a set of values)
	            throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
	                    "\n(StringValidator.validate) " +
	            		"Developer issue: " +
	                    "There are no valid values supplied for the setting '" +
	                    itemNameCanonical + "'!" );
	        }
	        
	        try {
		        	
		        if ( _additionalInfo instanceof Set ) {
		        
		            Set validValues = new HashSet();
		            validValues = (Set) _additionalInfo;
		            
		            // Quick pass through the acceptable setting values in the 
		            // supplied set, and case-independent comparison
		            boolean validValueInSet = false;
		            Iterator valueIterator = validValues.iterator();
		            String nextValue;
		            Object nextValueAsObj;
		            while ( valueIterator.hasNext() ){
		                
		                nextValueAsObj = valueIterator.next();
		                if ( nextValueAsObj instanceof String ) {
		                
		                    nextValue = (String) nextValueAsObj;
		                    if ( nextValue.equalsIgnoreCase( strItemValueAsLoaded )) {
		                        
		                        validValueInSet = true;
		                        break;
		                    }
		                }
		                else {
		                    // flag as error
		                }
		            }
		            
		            if ( validValueInSet ) {
		                
					    setItemValueValidated( strItemValueAsLoaded );
			        }
			        else {
			            
			            BanjoError errorItem = new BanjoError( 
			                    "The supplied value '" + strItemValueAsLoaded + 
			                    "' is not a valid option for the setting '" + 
			                    itemNameCanonical + "'.",
					            BANJO.ERRORTYPE_INVALIDCHOICE,
					            getItemNameCanonical(),
			            		"(StringValidator.validate) " +
			            		"Invalid choice supplied." );
				        
				        collectedErrors.add( errorItem );
		
				        isValidDataType = false;
			        }
		        }
		        else if ( _additionalInfo instanceof String ) {
	                
		            String additionalInfo = (String) _additionalInfo;
		            
	                if ( additionalInfo.equals( BANJO.BANJO_FREEFORMINPUT ) ) {
	                    
	                    setItemValueValidated( strItemValueAsLoaded );
	                }
	            }
		        else {
                    
                    throw new BanjoException( 
                            BANJO.ERROR_BANJO_DEV,
                            "\n(StringValidator.validate) " +
                            "Developer issue: " +
                            "The method argument 'additionalInfo' " +
                            "was not supplied in a usable format." );
		        }
		        
		        
	        }
			catch ( Exception e ) {
								    
				throw new BanjoException( 
				        BANJO.ERROR_BANJO_DEV,
				        "\n(StringValidator.validate) " +
	            		"Developer issue: " +
				        "Could not validate the setting '" + 
				        itemNameCanonical + "' - maybe the valid setting values were " +
				        "not supplied by the developer?" );
			}

		    return isValidDataType;
	    }
	}
		
	protected class TimeValidator extends DataTypeValidator {
	    
	    TimeValidator() {
		    
	        dataTypeToValidateAgainst = BANJO.VALIDATION_DATATYPE_TIME;
	    }
	    
	    protected boolean validate( final Object _itemValue, final Object _additionalInfo) 
	    		throws Exception {
	     
	        boolean isValidDataType = true;
            
            double timeToValidate;
	        String timeValue;
	        	    
	        if ( _itemValue instanceof String ) {
	            
	            timeValue = (String) _itemValue;
	        }
	        else {

		        // This is purely a developer issue: our validation expects a string
	            throw new BanjoException( BANJO.ERROR_BANJO_DEV,
	                    "(TimeValidator.validate) " +
	                    "Developer issue: The supplied value for the setting '" +
	                    itemNameCanonical + "' needs to be a string!" );
	        }

	        if ( !timeValue.equals( BANJO.BANJO_NOVALUESUPPLIED_STRING )) {
		            
			    try {
				    
			        // Don't allow negative times, but allow doubles via rounding (could
			        // use doubles throughout if we wanted to change the client code)
			        timeToValidate = Double.parseDouble( timeValue );
				    
				    if ( timeToValidate < 0 ) {
                        
				        BanjoError errorItem = new BanjoError( 
				                errorMessageWrongDataType( 
						                getItemNameDescriptive(), 
						                getItemNameCanonical(), 
						                getItemValueAsLoaded(), 
						                dataTypeToValidateAgainst ),
					            BANJO.ERRORTYPE_INVALIDRANGE,
					            getItemNameCanonical(),
			            		"(TimeValidator.validate) " +
			            		"The value for the setting '" +
			            		getItemNameCanonical() + "' (='" + timeToValidate +
			            		"') cannot be negative." );
				        
				        collectedErrors.add( errorItem );
				        
				        isValidDataType = false;
			        }
			        else {
			            
				        // Adjust for milliseconds (i.e., the input is meant in seconds):
				        timeToValidate *=1000;
				        
				        // Update the validated settings
					    setItemValueValidated( Long.toString( Math.round( timeToValidate ) ) );
			        }
			    }
			    catch ( Exception e ) {
	
			        // In this case, the time may be supplied in the format D:H:M:S,
			        // so we need to parse the string (we start from the left, 
			        // and let the user omit any m and s values)
			        
			        Set validTimeQualifiers = new HashSet();
			        validTimeQualifiers.add( BANJO.BANJO_TIMEQUALIFIER_DAYS );
			        validTimeQualifiers.add( BANJO.BANJO_TIMEQUALIFIER_HOURS );
			        validTimeQualifiers.add( BANJO.BANJO_TIMEQUALIFIER_MINUTES );
			        validTimeQualifiers.add( BANJO.BANJO_TIMEQUALIFIER_SECONDS );
			        validTimeQualifiers.add( BANJO.BANJO_TIMEQUALIFIER_MILLISECS );
			        HashMap timeQualifiers = new HashMap();
			        timeQualifiers.put( BANJO.BANJO_TIMEQUALIFIER_DAYS, new Long( 24*60*60*1000) );
			        timeQualifiers.put( BANJO.BANJO_TIMEQUALIFIER_HOURS, new Long(   60*60*1000) );
			        timeQualifiers.put( BANJO.BANJO_TIMEQUALIFIER_MINUTES, new Long(    60*1000) );
			        timeQualifiers.put( BANJO.BANJO_TIMEQUALIFIER_SECONDS, new Long(       1000) );
			        timeQualifiers.put( BANJO.BANJO_TIMEQUALIFIER_MILLISECS, new Long(        1) );
			        
			        timeToValidate = 0;
			        double[] timeValues = new double[4];
			        for (int i = 0; i< timeValues.length; i++) timeValues[i] = 0;
			        // Convention: i=0 holds days, i=1 holds hours
			        // i=2 holds minutes, and i=3 holds seconds
			        
					StringTokenizer tokenizer;
					int tokenCount;
					tokenizer = 
				        new StringTokenizer( timeValue, 
				                BANJO.DELIMITER_DEFAULT_TIME );
					tokenCount = tokenizer.countTokens();
	
					if ( tokenCount == 1 ) {
					    
					    // Check if user supplied time in the format "number specifier"
					    // (allow space between number and specifier)
					    
					    tokenizer = 
					        new StringTokenizer( getItemValueAsLoaded(), 
					                BANJO.DELIMITER_SPACE );
					    
					    if ( tokenizer.countTokens() > 2 ) {
					        
					        BanjoError errorItem = new BanjoError( 
					                errorMessageWrongTimeFormat( 
							                getItemNameDescriptive(), 
							                getItemNameCanonical(), 
							                getItemValueAsLoaded(), 
							                dataTypeToValidateAgainst ),
						            BANJO.ERRORTYPE_MISMATCHEDDATATYPE,
						            getItemNameCanonical(),
				            		"(TimeValidator.validate) " +
				            		"Cannot interpret '" +
				            		getItemNameCanonical() + "' as '" +
				            		dataTypeToValidateAgainst + "'." );
					        
					        collectedErrors.add( errorItem );
	
					        isValidDataType = false;
					    }
					    else if ( tokenizer.countTokens() == 1 ) {
					        
					        // 
					        String timeQualifier = timeValue.substring( 
					                timeValue.length()-1, timeValue.length() );
					        
					        try {
					        
					            timeToValidate = Math.round( Double.parseDouble( timeValue.substring( 
					                0, timeValue.length()-1 ) ));
					        }
					        catch ( Exception ne ) {
					            
							    BanjoError errorItem = new BanjoError( 
						                errorMessageWrongTimeFormat( 
								                getItemNameDescriptive(), 
								                getItemNameCanonical(), 
								                getItemValueAsLoaded(), 
								                dataTypeToValidateAgainst ),
							            BANJO.ERRORTYPE_MISMATCHEDDATATYPE,
							            getItemNameCanonical(),
					            		"(TimeValidator.validate) " +
					            		"Cannot interpret '" +
					            		getItemNameCanonical() + "' as '" +
					            		dataTypeToValidateAgainst + "'." );
						        
						        collectedErrors.add( errorItem );
			
						        isValidDataType = false;
					        }
					        
					        if ( validTimeQualifiers.contains( timeQualifier ) ) {
					            
					            timeToValidate *= Long.parseLong( (String) 
					                    timeQualifiers.get( timeQualifier ).toString() );
					        }
					        
					    }
					    else if ( tokenizer.countTokens() == 2 ) {
					        
					        timeToValidate = Double.parseDouble( tokenizer.nextToken() );
					        
					        String timeQualifier = tokenizer.nextToken();
					        
					        if ( validTimeQualifiers.contains( timeQualifier ) ) {
					            
					            timeToValidate *= Long.parseLong( (String) 
					                    timeQualifiers.get( timeQualifier ).toString() );
					        }
                            else {
                                
                                BanjoError errorItem = new BanjoError( 
                                        errorMessageWrongTimeFormat( 
                                                getItemNameDescriptive(), 
                                                getItemNameCanonical(), 
                                                getItemValueAsLoaded(), 
                                                dataTypeToValidateAgainst ),
                                        BANJO.ERRORTYPE_MISMATCHEDDATATYPE,
                                        getItemNameCanonical(),
                                        "(TimeValidator.validate) " +
                                        "Cannot interpret '" +
                                        getItemNameCanonical() + "' as '" +
                                        dataTypeToValidateAgainst + "'." );
                                
                                collectedErrors.add( errorItem );
            
                                isValidDataType = false;
                            }					        
					    }
					    
					    setItemValueValidated( Long.toString( Math.round( timeToValidate )));
					}
					else if ( tokenCount > 4 ) {
					    
					    BanjoError errorItem = new BanjoError( 
				                errorMessageWrongTimeFormat( 
						                getItemNameDescriptive(), 
						                getItemNameCanonical(), 
						                getItemValueAsLoaded(), 
						                dataTypeToValidateAgainst ),
					            BANJO.ERRORTYPE_MISMATCHEDDATATYPE,
					            getItemNameCanonical(),
			            		"(TimeValidator.validate) " +
			            		"Cannot interpret '" +
			            		getItemNameCanonical() + "' as '" +
			            		dataTypeToValidateAgainst + "'." );
				        
				        collectedErrors.add( errorItem );
	
				        isValidDataType = false;
					}
					
					else {
					    
						for (int i=timeValues.length-tokenCount; i<timeValues.length; i++) {
						    
							try {
							    
							    // Again, "fix" any negative number entered by the user
							    timeValues[i] = Math.abs( Double.parseDouble( 
							            tokenizer.nextToken() ) );
							}		
							catch (Exception ex) {
								
							    // If the loaded value is not usable, tell the user
							    BanjoError errorItem = new BanjoError( 
						                errorMessageWrongTimeFormat( 
								                getItemNameDescriptive(), 
								                getItemNameCanonical(), 
								                getItemValueAsLoaded(), 
								                dataTypeToValidateAgainst ),
							            BANJO.ERRORTYPE_MISMATCHEDDATATYPE,
							            getItemNameCanonical(),
					            		"(TimeValidator.validate) " +
					            		"Cannot interpret '" +
					            		getItemNameCanonical() + "' as '" +
					            		dataTypeToValidateAgainst + "'." );
						        
						        collectedErrors.add( errorItem );
		
						        isValidDataType = false;
							}
						}
						
						timeToValidate = (((( timeValues[0] ) * 24 + 
						        timeValues[1] ) * 60 +
						        timeValues[2] ) * 60 +
						        timeValues[3] ) * 1000;		  
					}
			    }

			    setItemValueValidated( Long.toString( Math.round( timeToValidate )));
	        }
	        else {

			    setItemValueValidated( Long.toString( BANJO.BANJO_NOVALUESUPPLIED_NUMBER ) );	            
	        }
		    		    
	        return isValidDataType;
	    }		        
	}
	
	protected class DiscretizationPolicyValidator extends DataTypeValidator {

	    DiscretizationPolicyValidator() {
		    
	        dataTypeToValidateAgainst = BANJO.VALIDATION_DATATYPE_DISCRETIZATIONPOLICY;
	    }
	    
	    protected boolean validate( final Object _itemValue, 
                final Object _additionalInfo) throws Exception {

	        boolean isValidDataType = true;
	        String strItemValueAsLoaded = new String();
	        
	        if ( _itemValue instanceof String ) {
	            
	            strItemValueAsLoaded = (String) _itemValue;
	        }
	        else {

		        // This is purely a developer issue: our validation expects a string
	            throw new BanjoException( BANJO.ERROR_BANJO_DEV,
	                    "(DiscretizationPolicyValidator.validate) " +
	                    "Developer issue: The supplied value for the setting '" +
	                    itemNameCanonical + "' needs to be a string!" );
	        }
		    
		    if ( strItemValueAsLoaded == null || strItemValueAsLoaded.length() < 1 ) {
		        
		        strItemValueAsLoaded = BANJO.UI_DISCRETIZATIONNONE;
		    }
	        
	        try {
		        
	            Settings processData = null;
	            if ( _additionalInfo instanceof Settings ) {
	                
	                processData = ( Settings ) _additionalInfo;
	            }
	            else {

		            // Code fails because we didn't get a reference to the global settings container
	                // (this is a developer issue)
		            throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
		                    "\n(Developer issue: DiscretizationPolicyValidator.validate) " +
		                    "This validator needs to receive a reference to the global Settings object!" );
	            }
		        
		        if ( strItemValueAsLoaded.equalsIgnoreCase( BANJO.UI_DISCRETIZATIONNONE ) ) {

				    setItemValueValidated( strItemValueAsLoaded );
				    
		            processData.setValidatedProcessParameter( 
				            BANJO.SETTING_DISCRETIZATIONPOLICY, 
				            BANJO.UI_DISCRETIZATIONNONE );

		            processData.setDynamicProcessParameter( 
				            BANJO.DATA_DEFAULTDISCRETIZATIONCHOICE, 
				            BANJO.UI_DISCRETIZATIONNONE );

		            processData.setDynamicProcessParameter( 
				            BANJO.DATA_DEFAULTDISCRETIZATIONPOINTS, 
				            Integer.toString( BANJO.DEFAULT_DISCRETIZATIONPOINTS ) );
		            
		            return isValidDataType;
		        }
		        else {
		            
		            Set validValues = new HashSet();
		    	    validValues.add( BANJO.UI_DISCRETIZATIONBYQUANTILE );
		    	    validValues.add( BANJO.UI_DISCRETIZATIONBYINTERVAL );
		        	
		            // all other choices
		            if ( ( strItemValueAsLoaded.length() != 
		                		BANJO.UI_DISCRETIZATIONBYQUANTILE.length()+1 || 
		                   strItemValueAsLoaded.length() != 
		                       BANJO.UI_DISCRETIZATIONBYINTERVAL.length()+1 ) || 
			               !validValues.contains( strItemValueAsLoaded.substring( 
			                       0, strItemValueAsLoaded.length()-1 ) ) ) {
		        
		                throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
			                    "\nThe supplied value ('" + strItemValueAsLoaded +
			                    "') for the setting '" +
			                    getItemNameFormattedForDisplay() +"' is not valid: " +
			                    "\nIt needs to be of the form 't#', " +
			                    "where the discretization type t is either '" +
			                    BANJO.UI_DISCRETIZATIONBYQUANTILE + "' (quantile discr.) " +
			                    		"or '" +
			                    BANJO.UI_DISCRETIZATIONBYINTERVAL + "' (value discr.)," +
			                    "\nand # is a single digit representing " +
			                    "the number of discretization points " +
			                    "for the selected discretization." );
		            }
		            
				    setItemValueValidated( strItemValueAsLoaded );
				    
		            processData.setValidatedProcessParameter( 
				            BANJO.SETTING_DISCRETIZATIONPOLICY, strItemValueAsLoaded );
		            
		            String defaultDiscretizationType = strItemValueAsLoaded.substring( 
		                    0, strItemValueAsLoaded.length()-1 );
	
		            String strDefaultDiscretizationPoints = strItemValueAsLoaded.substring( 
		                    strItemValueAsLoaded.length()-1,
		                    strItemValueAsLoaded.length());
		            try {
		                
			            int defaultDiscretizationPoints = Integer.parseInt( 
			                    strDefaultDiscretizationPoints );
		            }
		            catch ( Exception e ) {
					    
						throw new BanjoException( 
						        BANJO.ERROR_BANJO_USERINPUT,
						        "\nCould not validate the setting '" + 
						        getItemNameFormattedForDisplay() + 
						        "': discretization points (" +
						        strDefaultDiscretizationPoints +
						        ") need to be an integer.");
					}

		            processData.setDynamicProcessParameter(  
				            BANJO.DATA_DEFAULTDISCRETIZATIONCHOICE, 
				            defaultDiscretizationType );
		            processData.setDynamicProcessParameter( 
				            BANJO.DATA_DEFAULTDISCRETIZATIONPOINTS, 
				            strDefaultDiscretizationPoints );
		        }
	        }
            catch ( BanjoException e ) {

                throw new BanjoException( e );
            }
            catch ( Exception e ) {
                        			    
				throw new BanjoException( 
				        BANJO.ERROR_BANJO_DEV,
				        "\n(DiscretizationPolicyValidator.validate) " +
	            		"Developer issue: " +
				        "Could not validate the setting '" + 
				        itemNameCanonical + "' - maybe the valid setting values were " +
				        "not supplied by the developer?");
			}

		    return isValidDataType;
	    }
	}
	
	protected class DiscretizationExceptionsValidator extends DataTypeValidator {

        // This is a special purpose validation that only applies to input
        // of the form <Letter><Numeral>, describing the discretization policy

	    DiscretizationExceptionsValidator() {
		    
	        dataTypeToValidateAgainst = BANJO.VALIDATION_DATATYPE_DISCRETIZATIONEXCEPTIONS;
	    }
	    
	    protected boolean validate( final Object _itemValue, 
                final Object _additionalInfo) throws Exception {

	        boolean isValidDataType = true;
	        String strItemValueAsLoaded = new String();
	        int varCount;
	        
	        
	        if ( _itemValue instanceof String ) {
	            
	            strItemValueAsLoaded = (String) _itemValue;
	        }
	        else {

		        // This is purely a developer issue: our validation expects a string
	            throw new BanjoException( BANJO.ERROR_BANJO_DEV,
	                    "(DiscretizationExceptionsValidator.validate) " +
	                    "Developer issue: The supplied value for the setting '" +
	                    itemNameCanonical + "' needs to be a string!" );
	        }
		    
		    if ( strItemValueAsLoaded == null || strItemValueAsLoaded.length() < 1 ) {
		        
		        strItemValueAsLoaded = BANJO.BANJO_NOVALUESUPPLIED_STRING;
		    }

	        try {
	            
	            Settings processData = null;
	            if ( _additionalInfo instanceof Settings ) {
	                
	                processData = ( Settings ) _additionalInfo;
	                varCount = Integer.parseInt( 
	                        processData.getValidatedProcessParameter( BANJO.SETTING_VARCOUNT ));
	            }
	            else {
	
		            // Code fails because we didn't get a reference to the global settings container
	                // (this is a developer issue)
		            throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
		                    "\nDiscretizationPolicyValidator.validate) " +
		            		"Developer issue: " +
		                    "The current implementation of this validator needs to " +
		                    "receive a reference to the global Settings object!" );
	            }
	                
		        if ( strItemValueAsLoaded.equals( BANJO.BANJO_NOVALUESUPPLIED_STRING ) ) {
	
					    setItemValueValidated( strItemValueAsLoaded );
					    
			            processData.setValidatedProcessParameter( 
					            BANJO.SETTING_DISCRETIZATIONEXCEPTIONS, strItemValueAsLoaded );
			            
			            return isValidDataType;
			    }
		        
		        // This is a special purpose validation that only applies to a list of inputs
		        // of the form <variableIndex>:<Letter><Numeral>, describing the exceptions
		        // to the discretization policy
	
		        // Now do some basic validation: we split the list into individual items,
		        // then make sure that each item has the required format
		        
			    StringTokenizer exceptionsTokenizer = new StringTokenizer( 
			            strItemValueAsLoaded, BANJO.DELIMITER_DEFAULT_DISCRETIZATIONEXCEPTIONS );
			    int exceptionsCount = exceptionsTokenizer.countTokens();
			    String[] exceptionType = new String[ varCount ];
			    String[] strExceptionPoints = new String[ varCount ];
			    String[] discretizationExceptions;
			    String[] exceptionItem;
			    String strExceptionTypeIndex;
			    int exceptionPoint;
		        
		        for ( int i=0; i<varCount; i++ ) {
		            
		            exceptionType[ i ] = BANJO.UI_DISCRETIZATIONNONE;
		        }
		        
		        Pattern exceptionPattern = Pattern.compile( 
		                BANJO.PATTERN_DISCRETIZATIONEXCEPTIONS, Pattern.COMMENTS );
		        
		        discretizationExceptions = exceptionPattern.split( strItemValueAsLoaded );
		        exceptionsCount = discretizationExceptions.length;
		        
		        if ( exceptionsCount > varCount ) {
		            
		            // ADD error to collection
		            
		            BanjoError errorItem = new BanjoError( 
		                    errorMessageWrongDataType( 
					                getItemNameDescriptive(), 
					                getItemNameCanonical(), 
					                getItemValueAsLoaded(), 
					                dataTypeToValidateAgainst ),
				            BANJO.ERRORTYPE_MISMATCHEDDATATYPE,
				            getItemNameCanonical(),
		            		"(DiscretizationExceptionValidator.validate) " +
		            		"The number of exceptions listed (=" + 
		            		exceptionsCount + ") for '" +
		            		getItemNameCanonical() + "' exeeds the variable count (" +
		            		varCount + ")." );
		            
		            collectedErrors.add( errorItem );
		            
		            return false;
		        }
		        
		        Pattern exceptionItemPattern = Pattern.compile( 
		                BANJO.PATTERN_DISCRETIZATIONEXCEPTION_ITEM );
		        
		        for ( int i=0; i< exceptionsCount; i++ ) {
		            
		            exceptionItem = exceptionItemPattern.split( discretizationExceptions[i] );
		            
		            if ( exceptionItem.length != 2 ) {
		                		                
		                // Record the fact that this particular item has the wrong format
		                BanjoError errorItem = new BanjoError( 
	                            "(" + dataTypeToValidateAgainst + ") " +
			            		"The discretization exception '" + 
			            		discretizationExceptions[i] + "' for the setting '" +
			            		getItemNameCanonical() + "' does not follow the required format.",
					            BANJO.ERROR_BANJO_USERINPUT,
					            getItemNameCanonical(),
			            		"(DiscretizationExceptionValidator.validate) " +
			            		"The discretization exception '" + 
			            		discretizationExceptions[i] + "' for the setting '" +
			            		getItemNameCanonical() + "' does not follow the required format." );
		                
		                collectedErrors.add( errorItem );
		                
		                isValidDataType = false;
		            }
		            else {

		                // The first item needs to specify the variable index
		                strExceptionTypeIndex = exceptionItem[0];
		                
		                // The second item specifies the actual discretization, and well be
		                // parsed below in the next step
		                String strExceptionItem = exceptionItem[1];
		                
		                int exceptionTypeIndex = Integer.parseInt( strExceptionTypeIndex ) - 
                				BANJO.CONFIG_DISCRETIZATIONSTARTINDEX;
		                
		                
		                if ( exceptionTypeIndex < varCount ) {
			                    
			                String exceptionItemType = ( exceptionItem[1] ).substring(
				                    0, strExceptionItem.length()-1 );
			                
			                String strExceptionPoint = exceptionItem[1].substring( 
			                        strExceptionItem.length()-1, strExceptionItem.length());
			                exceptionPoint = Integer.parseInt( strExceptionPoint );
			                
		                    exceptionType[ exceptionTypeIndex ] = exceptionItemType;
		                    strExceptionPoints[ exceptionTypeIndex ] = strExceptionPoint;
			            }
		                else {
		                    
		                    BanjoError errorItem = new BanjoError(
		                            "(" + dataTypeToValidateAgainst + ") " +
				            		"The variable index (='" + (exceptionTypeIndex+1) +
				            		"') of the discretization exception (='" + 
				            		discretizationExceptions[i] + "') exceeds the " +
				            				"number of variables (='" + varCount + "').",
						            BANJO.ERROR_BANJO_USERINPUT,
						            getItemNameCanonical(),
				            		"(DiscretizationExceptionValidator.validate) " +
				            		"A discretization exception (='" + 
				            		discretizationExceptions[i] + "') exceeds the " +
				            				"number of variables (='" + varCount + "')." );
		                    
		                    collectedErrors.add( errorItem );
		                    
			                isValidDataType = false;
		                }
		            }
		        }
		            
		        if ( isValidDataType ) {

			        String strExceptionTypeList = new String();
			        String strExceptionPointsList = new String();
			        if ( exceptionType.length >= 1 ) {

		                // The first item needs to be the type of discretization
			            strExceptionTypeList = exceptionType[ 0 ];

		                // The second item needs to be an integer, specifying the number
		                // of discretization points
			            strExceptionPointsList = strExceptionPoints[ 0 ];
			            
				        for ( int i=1; i<exceptionType.length; i++ ) {
	
				            strExceptionTypeList += BANJO.DELIMITER_DEFAULT_DISCRETIZATIONEXCEPTIONS +  
				            		exceptionType[ i ];
				            strExceptionPointsList += BANJO.DELIMITER_DEFAULT_DISCRETIZATIONEXCEPTIONS +  
				            		strExceptionPoints[ i ];
				        }
			        }
			        else {
			            
			            strExceptionTypeList = "";
			        }
			        
		            processData.setDynamicProcessParameter( 
				            BANJO.DATA_DISCRETIZATIONEXCEPTION_TYPELIST, 
				            strExceptionTypeList );
	
		            processData.setDynamicProcessParameter( 
				            BANJO.DATA_DISCRETIZATIONEXCEPTION_POINTSLIST, 
				            strExceptionPointsList );
	
				    setItemValueValidated( strItemValueAsLoaded );
				    
		            processData.setValidatedProcessParameter( 
				            BANJO.SETTING_DISCRETIZATIONEXCEPTIONS, strItemValueAsLoaded );
		        }
	        }
			catch ( Exception e ) {
								    
				throw new BanjoException( 
				        BANJO.ERROR_BANJO_DEV,
				        "\n(DiscretizationExceptionsValidator.validate) " +
	            		"Developer issue: " +
				        "Could not validate the setting '" + 
				        itemNameCanonical + "' - maybe the valid setting values were " +
				        "not supplied by the developer?");
			}

		    return isValidDataType;
	    }
	}
    
    protected class FileValidator extends DataTypeValidator {
        
        FileValidator() {
            
            dataTypeToValidateAgainst = BANJO.VALIDATION_DATATYPE_FILE;
        }
        
        protected boolean validate( final Object _itemValue, 
                final Object _additionalInfo) throws Exception {


            if ( true ) {
                
                // This method is not ready for use
                throw new BanjoException( BANJO.ERROR_BANJO_DEV,
                        "(FileValidator.validate) " +
                        "This method is not ready for use!" );
            }
            
            
            boolean isValidDataType = true;
            String strItemValueAsLoaded = new String();
            
            if ( _itemValue instanceof String ) {
                
                strItemValueAsLoaded = (String) _itemValue;
            }
            else {

                // This is purely a developer issue: our validation expects a string
                throw new BanjoException( BANJO.ERROR_BANJO_DEV,
                        "(FileValidator.validate) " +
                        "Developer issue: The supplied value for the setting '" +
                        itemNameCanonical + "' needs to be a string!" );
            }
            
            try {
                
                // 
                String _fileName = strItemValueAsLoaded;
                File dataFile = new File( _fileName );
                if ( !dataFile.exists() ) {
                    
                    // TODO: will need the directory path (maybe better to
                    // pass the entire string along as one?)
                    
                }
                
                setItemValueValidated( strItemValueAsLoaded );
            }
            catch ( Exception e ) { 

                BanjoError errorItem = new BanjoError( 
                        errorMessageWrongDataType( 
                                getItemNameDescriptive(), 
                                getItemNameCanonical(), 
                                getItemValueAsLoaded(), 
                                dataTypeToValidateAgainst ),
                        BANJO.ERRORTYPE_MISMATCHEDDATATYPE,
                        getItemNameCanonical(),
                        "..." );

                collectedErrors.add( errorItem );
                
                isValidDataType = false;
            }
            
            return isValidDataType;
        }
    }
    
    // Validator for path when file is specified (not yet ready for use)    
    protected class PathValidator extends DataTypeValidator {
        
        PathValidator() {
            
            dataTypeToValidateAgainst = BANJO.VALIDATION_DATATYPE_FILEPATH;
        }
        
        protected boolean validate( final Object _itemValue, 
                final Object _additionalInfo) throws Exception {

            
            if ( true ) {
                
                // This method is not ready for use
                throw new BanjoException( BANJO.ERROR_BANJO_DEV,
                        "(PathValidator.validate) " +
                        "This method is not ready for use!" );
            }
            
            
            boolean isValidDataType = true;
            String strItemValueAsLoaded = new String();
            
            if ( _itemValue instanceof String ) {
                
                strItemValueAsLoaded = (String) _itemValue;
            }
            else {

                // This is purely a developer issue: our validation expects a string
                throw new BanjoException( BANJO.ERROR_BANJO_DEV,
                        "(PathValidator.validate) " +
                        "Developer issue: The supplied value for the setting '" +
                        itemNameCanonical + "' needs to be a string!" );
            }
            
            try {
                
                // 
                String _fileName = strItemValueAsLoaded;
                File dataFile = new File( _fileName );
                File canonicalFile = dataFile.getCanonicalFile();
                if ( !dataFile.exists() ) {
                    
                    if ( !canonicalFile.getParentFile().isDirectory() ) {
                    
                        // 
                        BanjoError errorItem = new BanjoError( 
                                errorMessageInvalidFilePath( 
                                        getItemNameDescriptive(), 
                                        getItemNameCanonical(), 
                                        getItemValueAsLoaded(), 
                                        dataTypeToValidateAgainst ),
                                BANJO.ERRORTYPE_INVALIDPATH,
                                getItemNameCanonical(),
                                "PathValidator could not validate the supplied path '" +
                                strItemValueAsLoaded + 
                                "'. FILE DOESN'T EXIST." );
    
                        collectedErrors.add( errorItem );
                        
                        isValidDataType = false;
                    }
                }
                
                setItemValueValidated( strItemValueAsLoaded );
            }
            catch ( Exception e ) { 

                BanjoError errorItem = new BanjoError( 
                        errorMessageInvalidFilePath( 
                                getItemNameDescriptive(), 
                                getItemNameCanonical(), 
                                getItemValueAsLoaded(), 
                                dataTypeToValidateAgainst ),
                        BANJO.ERRORTYPE_INVALIDPATH,
                        getItemNameCanonical(),
                        "PathValidator could not validate the supplied path '" +
                        strItemValueAsLoaded + 
                        "'." );

                collectedErrors.add( errorItem );
                
                isValidDataType = false;
            }
            
            return isValidDataType;
        }
    }
	
    public SettingItem( 
            final String _itemNameCanonical,
            final String _itemNameDescriptive, 
            final String _itemNameForDisplay ) throws Exception {

        itemNameCanonical = new String( _itemNameCanonical );
        // In our implementation we use the lowerCase version of the canonical
        // name as comparison criterion
        itemNameForComparison = new String( _itemNameCanonical.toLowerCase() );
	    
        if ( BANJO.DEBUG ) { 
            
	        if ( _itemNameCanonical == null || _itemNameCanonical.equalsIgnoreCase("") ) {
	            
	            // throw new BanjoException: this is a developer issue, so the end user
	            // should never encounter this message            
	            throw new BanjoException( BANJO.ERROR_BANJO_DEV,
	                    "(SettingItem.constructor) " +
	            		"Developer issue: " +
	            		"The value of the property 'itemNameCanonical' for item '" +
	                    getItemNameForComparison() + "' needs to be supplied." );
	        }
	        if ( _itemNameDescriptive == null || _itemNameDescriptive.equalsIgnoreCase("") ) {
	            
	            // throw new BanjoException: this is a developer issue, so the end user
	            // should never encounter this message            
	            throw new BanjoException( BANJO.ERROR_BANJO_DEV,
	                    "(SettingItem.constructor) " +
	            		"Developer issue: " +
	            		"The value of the property 'itemNameDescriptive' for item '" +
	                    getItemNameForComparison() + "' needs to be supplied." );
	        }
        }
        
        itemNameDescriptive = new String( _itemNameDescriptive );
        itemNameForDisplay = new String( _itemNameForDisplay );
        
        // For now this is all we need:
        setItemNameFormattedForDisplay( itemNameDescriptive );
    }


    /**
     * Constructor based on an existing settingItem.
     *
     * @param _settingItem The settingItem that we want to use as basis for the new one.
     * 
     */  
    public SettingItem( SettingItem _settingItem ) {
        
        // Copy the core data elements so the new settingsItem can stand on its own
        itemNameCanonical = new String( _settingItem.itemNameCanonical );
        itemNameForComparison = new String( _settingItem.itemNameForComparison );
        itemNameDescriptive = new String( _settingItem.itemNameDescriptive );
        itemNameForDisplay = new String( _settingItem.itemNameForDisplay );
        itemNameFormattedForDisplay = new String( _settingItem.itemNameFormattedForDisplay );

        itemValueAsLoaded = new String( _settingItem.itemValueAsLoaded );
        itemValueValidated = new String( _settingItem.itemValueValidated );
        isValidSetting = _settingItem.isValidSetting;
        isDefaultValueUsed = _settingItem.isDefaultValueUsed;
        
        // may want to clone even deeper by processing these further:
        objItemValue = _settingItem.objItemValue;
        objItemInfo = _settingItem.objItemInfo;
        collectedErrors.addAll( _settingItem.collectedErrors );
    }
    
    // Note: Depending on whether this method returns 'true' or 'false', we will
    // add the settingItem to a collection. This lets us present all input related
    // errors to the user at one time.
    protected boolean validate( 
            final String _itemValueToValidate,
            final String dataTypeToValidateAgainst,
            final int validationType,
            final Object _additionalInfo, 
            Object _defaultValue ) throws Exception {
        

        String strItemValueToValidate = _itemValueToValidate;
        boolean isValidDataType = false;
        DataTypeValidator dataTypeValidator;
        	    
	    if ( strItemValueToValidate != null ) 
            strItemValueToValidate = strItemValueToValidate.trim();
	    
	    if ( ( strItemValueToValidate == null || strItemValueToValidate.equals("") ) ) {
	        
            // First choice: use default value, if it is supplied
            if ( _defaultValue != null && _defaultValue instanceof String &&
                       !_defaultValue.equals("") ) {
                
                // Use the default value to continue the validation with
                strItemValueToValidate = (String) _defaultValue;
                setDefaultValueUsed( true );
            }
	        // Special case when we use free-form input:
	        // In this case, we "correct" a null default value to an empty string only
            else if ( _additionalInfo instanceof String && 
		            ((String) _additionalInfo).equals( BANJO.BANJO_FREEFORMINPUT ) ) {

		        if ( _defaultValue == null ) {
		        
		            _defaultValue = new String("");
		        }
		    }
		    else {
			    
		        // In case the "value as loaded" is unusable, we expect the default 
                // value to be usable (i.e., a string that is not empty)
		        if ( _defaultValue != null && _defaultValue instanceof String &&
		               !_defaultValue.equals("") ) {
		        
		            // Use the default value to continue the validation with
                    strItemValueToValidate = (String) _defaultValue;
			        setDefaultValueUsed( true );
		        }
		        else {

			        // This is purely a developer issue: our code expects a string
		            throw new BanjoException( BANJO.ERROR_BANJO_DEV,
		                    "(SettingItem.validate) " +
		                    "Developer issue: " +
		                    "The supplied default value for the setting '" +
		                    itemNameCanonical + "' needs to be a non-empty string!" );
		        }
		    }
	    }

	    if ( dataTypeToValidateAgainst.equalsIgnoreCase( 
	            BANJO.VALIDATION_DATATYPE_DISCRETIZATIONPOLICY )) {
	        
	        dataTypeValidator = new DiscretizationPolicyValidator();
	        isValidDataType = dataTypeValidator.validate( 
                    strItemValueToValidate, _additionalInfo );
	    }
	    else if ( dataTypeToValidateAgainst.equalsIgnoreCase( 
	            BANJO.VALIDATION_DATATYPE_DISCRETIZATIONEXCEPTIONS )) {
	        
	        dataTypeValidator = new DiscretizationExceptionsValidator();
	        isValidDataType = dataTypeValidator.validate( 
                    strItemValueToValidate, _additionalInfo );
	    }
        else if ( dataTypeToValidateAgainst.equalsIgnoreCase( 
                BANJO.VALIDATION_DATATYPE_TIMESTAMP )) {
            
            dataTypeValidator = new TimeStampValidator();
            isValidDataType = dataTypeValidator.validate( 
                    strItemValueToValidate, _additionalInfo );
        }
        else if ( dataTypeToValidateAgainst.equalsIgnoreCase( 
                BANJO.VALIDATION_DATATYPE_FILEPATH )) {
            
            dataTypeValidator = new PathValidator();
            isValidDataType = dataTypeValidator.validate( 
                    strItemValueToValidate, _additionalInfo );
        }
	    else if ( dataTypeToValidateAgainst.equalsIgnoreCase( 
	            BANJO.VALIDATION_DATATYPE_STRING )) {
	        
	        dataTypeValidator = new StringValidator();
	        isValidDataType = dataTypeValidator.validate( 
                    strItemValueToValidate, _additionalInfo );
	    }
	    else if ( dataTypeToValidateAgainst.equalsIgnoreCase( 
	            BANJO.VALIDATION_DATATYPE_INTEGERLIST )) {

	        Pattern pattern = Pattern.compile( 
	                BANJO.PATTERN_INTEGERLIST, Pattern.COMMENTS );
	        
	        dataTypeValidator = new IntegerListValidator();
	        
	        // in case the optional setting is an empty list
	        // we take care of it here
	        if ( !strItemValueToValidate.equals( "" ) ) {
	        
	            isValidDataType = dataTypeValidator.validate( 
                        strItemValueToValidate, pattern );
	        }
	        else {
	            
	            isValidDataType = true;
	        }
	    }
	    else if ( dataTypeToValidateAgainst.equalsIgnoreCase( 
	            BANJO.VALIDATION_DATATYPE_INTEGER )) {
	        
	        dataTypeValidator = new IntegerValidator();
	        isValidDataType = dataTypeValidator.validate( 
                    strItemValueToValidate, null );
	    }
	    else if ( dataTypeToValidateAgainst.equalsIgnoreCase( 
	            BANJO.VALIDATION_DATATYPE_LONG )) {
	        
	        dataTypeValidator = new LongValidator();
	        isValidDataType = dataTypeValidator.validate( 
                    strItemValueToValidate, null );
	    }
	    else if ( dataTypeToValidateAgainst.equalsIgnoreCase( 
	            BANJO.VALIDATION_DATATYPE_DOUBLE )) {

	        dataTypeValidator = new DoubleValidator();
	        isValidDataType = dataTypeValidator.validate( 
                    strItemValueToValidate, null );
	    }
	    else if ( dataTypeToValidateAgainst.equalsIgnoreCase( 
	            BANJO.VALIDATION_DATATYPE_TIME )) {
	        
	        dataTypeValidator = new TimeValidator();
	        isValidDataType = dataTypeValidator.validate( 
                    strItemValueToValidate, null );
	    }
	    else {
	        
	        throw new BanjoException( BANJO.ERROR_BANJO_DEV, 
	                "(SettingItem.validate) " +
	                "Developer issue: Trying to validate data type '" +
	                dataTypeToValidateAgainst + 
	                "', for which there is no code segment to handle it!" );
	    }
	    
	    return isValidDataType;
    }
    
    public boolean equals( final Object otherObj ) {
		
        // Note that as long as we don't have specific data as part of neither the mandatory 
        // nor the optional setting item, we can have this method as part of the SettingItem
        // base class.
        // In addition, by comparing items within the base class, we don't allow mandatory
        // and optional items associated with the same name, which would likely cause unexpected
        // behaviour.
		
		// Quick check for identity
		if (this == otherObj) return true;
		
		// Need to be an actual object
		if (otherObj == null) return false;
		
		// and need to be of the same class
		if (otherObj.getClass() != this.getClass() ) return false;
		
		SettingItem otherSettingItem = (SettingItem) otherObj;
		
		// Match if the canonical names are the same	
		if ( otherSettingItem.itemNameCanonical.equalsIgnoreCase( 
		        this.getItemNameCanonical() ) ) {
		    
			return true;
		}
		    
	    return false;
    }
    
    public String getErrorMessages() {
        
        String errorMessages = new String("");
        BanjoError localErrorItem;
        
		Iterator settingsIterator = collectedErrors.iterator();
		while ( settingsIterator.hasNext() ) {
		    
		    localErrorItem = ( BanjoError ) settingsIterator.next();
		        
		    errorMessages += formatForDisplay( localErrorItem.getErrorMessageText() );
		}
		return errorMessages;
    }

    protected String formatForDisplay( final String textToFormat ) {
        
        String prefix = BANJO.ERRORMESSAGEDISPLAYPREFIX;
        
        return prefix + textToFormat;
    }

    protected String errorMessageWrongDataType( 
            final String _itemNameDescriptive,
            final String _itemNameCanonical,
            final String _itemValue,
            final String _dataTypeToValidateAgainst ) {
        
        return "The value for the setting '" +
            _itemNameDescriptive + "' (" + _itemNameCanonical +
            " = '" + _itemValue + 
            "') is not of the correct data type (expected: " +
            _dataTypeToValidateAgainst + ").";
    }

    protected String errorMessageInvalidFilePath( 
            final String _itemNameDescriptive,
            final String _itemNameCanonical,
            final String _itemValue,
            final String _dataTypeToValidateAgainst ) {
        
        return "The value for the setting '" +
            _itemNameDescriptive + "' (" + _itemNameCanonical +
            " = '" + _itemValue + 
            "') does not describe a valid path. " +
            "Please make sure that all specified directories have been created.";
    }

    protected String errorMessageMissingValue( 
            final String _itemNameDescriptive,
            final String _itemNameCanonical,
            final String _itemValue,
            final String _dataTypeToValidateAgainst ) {
        
        return "The value for the setting '" +
            _itemNameDescriptive + "' (" + _itemNameCanonical +
            " = '" + _itemValue + 
            "') needs to be supplied.";
    }

	protected String errorMessageWrongTimeFormat( 
            final String _itemNameDescriptive,
            final String _itemNameCanonical,
            final String _itemValue,
            final String _dataTypeToValidateAgainst ) {
	    
	    return "The value for the setting '" +
	    	_itemNameDescriptive + "' (" + _itemNameCanonical +
		    " = '" + _itemValue + 
		    "') is not in a valid time format. Expected: " +
		    "number (in seconds), number with valid qualifier " +
		    "(" + BANJO.BANJO_TIMEQUALIFIER_DAYS + "=days, " +
		    BANJO.BANJO_TIMEQUALIFIER_HOURS + "=hours, " +
		    BANJO.BANJO_TIMEQUALIFIER_MINUTES + "=minutes, " +
		    BANJO.BANJO_TIMEQUALIFIER_SECONDS + "=seconds, " +
		    BANJO.BANJO_TIMEQUALIFIER_MILLISECS + "=milliseconds), " +
		    		"or a string in the form 'd:h:m:s'.";
	}
	
	
    /**
     * @return Returns the itemNameForComparison.
     */
	public String getItemNameForComparison() {
        return itemNameForComparison;
    }

    /**
     * @return Returns the itemNameCanonical.
     */
    public String getItemNameCanonical() {
        return itemNameCanonical;
    }

    /**
     * @return Returns the itemNameDescriptive.
     */
    public String getItemNameDescriptive() {
        return itemNameDescriptive;
    }

    /**
     * @param itemNameFormattedForDisplay The itemNameFormattedForDisplay to set.
     */
    protected void setItemNameFormattedForDisplay(
            final String itemNameFormattedForDisplay ) {
        
        this.itemNameFormattedForDisplay = itemNameFormattedForDisplay;
    }

    /**
     * @return Returns the itemNameFormattedForDisplay.
     */
    public String getItemNameFormattedForDisplay() {
        
        // Not all items may need a special display string (use the regular validated
        // string if there's no "display" string)
        if ( itemNameFormattedForDisplay == null || itemNameFormattedForDisplay.equals("")) {
            
            return itemValueValidated;
        }
        else {
        
            return itemNameFormattedForDisplay;
        }
    }

    /**
     * @param _itemValueAsLoaded The itemValueAsLoaded to set.
     */
    protected void setItemValueAsLoaded( final String _itemValueAsLoaded ) {
        
        if ( _itemValueAsLoaded == null ) { 

            this.itemValueAsLoaded = new String( "" );
        }
        else {

            this.itemValueAsLoaded = new String( _itemValueAsLoaded );
        }
    }

    /**
     * @return Returns the itemValueAsLoaded.
     */
    public String getItemValueAsLoaded() {
        return itemValueAsLoaded;
    }

    /**
     * @param itemValueValidated The itemValueValidated to set.
     */
    public void setItemValueValidated( final String itemValueValidated ) {
        this.itemValueValidated = itemValueValidated;
    }

    /**
     * @return Returns the itemValueValidated.
     */
    public String getItemValueValidated() {
        return itemValueValidated;
    }

    /**
     * @param objItemValue The objItemValue to set.
     */
    protected void setObjItemValue( Object objItemValue ) {
        this.objItemValue = objItemValue;
    }

    /**
     * @return Returns the objItemValue.
     */
    public Object getObjItemValue() {
        return objItemValue;
    }

    /**
     * @param isValidSetting The isValidSetting to set.
     */
    protected void setValidSetting( boolean isValidSetting ) {
        this.isValidSetting = isValidSetting;
    }

    /**
     * @return Returns the isValidSetting.
     */
    public boolean isValidSetting() {
        return isValidSetting;
    }
    /**
     * @return Returns the objItemInfo.
     */
    public Object getObjItemInfo() {
        return objItemInfo;
    }
    /**
     * @param _itemInfo The objItemInfo to set.
     */
    public void setObjItemInfo(Object _itemInfo) {
        this.objItemInfo = _itemInfo;
    }
    /**
     * @return Returns the collectedErrors.
     */
    public Set getCollectedErrors() {
        return collectedErrors;
    }
    /**
     * @return Returns the isDefaultValueUsed.
     */
    public boolean isDefaultValueUsed() {
        return isDefaultValueUsed;
    }
    /**
     * @param isDefaultValueUsed The isDefaultValueUsed to set.
     */
    public void setDefaultValueUsed( boolean isDefaultValueUsed ) {
        this.isDefaultValueUsed = isDefaultValueUsed;
    }
    
    public String toString() {
        
        return "canonicalName='" + itemNameCanonical + "'";
    }
}