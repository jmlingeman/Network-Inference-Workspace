/*
 * Created on May 17, 2004
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
import java.io.FilenameFilter;
import java.util.*;
import java.util.regex.Pattern;

import edu.duke.cs.banjo.data.observations.*;
import edu.duke.cs.banjo.utility.BANJO;
import edu.duke.cs.banjo.utility.BanjoException;
import edu.duke.cs.banjo.utility.BanjoError;
import edu.duke.cs.banjo.utility.FileUtil;
import edu.duke.cs.banjo.utility.JZOOWildCardFilter;
import edu.duke.cs.banjo.utility.StringUtil;
import edu.duke.cs.banjo.utility.BanjoRandomNumber;

/**
 * Manages the initial, validated, and dynamically updated parameters that are
 * used by the various search algorithms.
 *
 * <p><strong>Details:</strong> <br>
 * Contains the data shared by different objects involved in the search process.
 * This includes the initial parameters (i.e., loaded via settings file, in 
 * raw, unvalidated form), their validated counterparts, and the dynamic parameters
 * (the ones that are changing over the life of a search).
 * <p>
 * By keeping this data outside of the core Searcher/Proposer/Evaluator/Decider 
 * objects, we simplify communication between our core objects, without
 * sacrificing performance.
 *   
 * <p><strong>Change History:</strong> <br>
 * Created on May 17, 2004
 * <p>
 * 9/14/2005 (v1.0.4) hjs	Changes to properly handle discretization of multiple 
 * 							observation files
 * <p>
 * 10/19/2005 (v2.0) hjs	Large-scale refactoring of the settings processing
 * 
 * 							New features:
 * 							- support for default settings
 * 							- use of SettingsItem class (hierarchy)
 * <p>
 * (v2.1) hjs               Pulled observation processing and file I/O inside for
 *                          support of multi-threading. Extensive refactoring.
 * <p>
 * 3/24/2008 (v2.2) hjs     Add wrapper functions for accessing random sequences. 
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class Settings {

    // Used to make sure that multiple threads do not share the same seed
    protected static final long baseRandomSeed = System.currentTimeMillis();
    
    // Initially supplied command line parameters
    protected String[] commandLineParameters;
	
    // Parameters that cannot be changed
    protected final Properties initialProcessParametersAsLoaded;
    protected final Properties initialProcessParametersLowercase;

	protected Properties passedInParameters;
	protected Properties loadedSettings;
	
	// Dafault parameters for Banjo
	protected Properties defaultProcessParameters;
	
	// Parameters that change during the search
	protected Properties dynamicProcessParameters;
	
	// Results that were produced by the search
	protected TreeSet highScoreStructureSet;
	
	// General purpose storage container to be used by any object that has
	// access to processData
	protected Set generalProcessDataStorage;

    protected Set registeredSettings = new HashSet();
    protected Set processedSettings = new HashSet();
	protected Set collectedErrors = new HashSet();
	protected Set collectedWarnings = new HashSet();
	protected Properties validatedSettings = new Properties();

    // We make the underlying assumption that every searcher within a multi-threaded Banjo
    // execution will use the exact same copy of the (processed) observations (i.e., the
    // same discretization)
    protected static ObservationsI observations;
    
    // Use the settings as a wrapper around our I/O class
    protected FileUtil fileUtil;
    
    // Access to random sequence, application-wide
    protected BanjoRandomNumber banjoRandomNumber = new BanjoRandomNumber();

    /**
     * Basic constructor that simply sets up the internal data for the settings object.
     * 
     */ 
    public Settings() throws Exception { 
                
        // Create the objects for storing the initial, validated and dynamic parameters.
        // Note: the initial parameter set is simply the loaded "raw" set of data, and 
        // should only be used with caution. The validated data set contains the same
        // info, but has basic checks applied (a numeric property should not be an
        // alphanumneric string after all).
        initialProcessParametersAsLoaded = new Properties();
        initialProcessParametersLowercase = new Properties();
        
        defaultProcessParameters = new Properties();
        passedInParameters = new Properties();
        loadedSettings = new Properties();
        dynamicProcessParameters = new Properties();

        generalProcessDataStorage = new HashSet();
        registeredSettings = new HashSet();
        processedSettings = new HashSet();
        collectedErrors = new HashSet();
        collectedWarnings = new HashSet();

        validatedSettings = new Properties();
        observations = null;
    }

    /**
     * Constructor based on an existing settings. Note that this constructor does a "deep" copy.
     *
     * @param _settings The settings that we want to use as basis for the new object.
     * 
     */ 
    public Settings( Settings _settings ) throws Exception { 
        
        synchronized ( this ) {
            
            // (Need to ) Handle the "final" data (first)
            initialProcessParametersAsLoaded = 
                assignProperties( _settings.initialProcessParametersAsLoaded );
            initialProcessParametersLowercase = 
                assignProperties( _settings.initialProcessParametersLowercase );
            
            loadSettings( _settings );
            
            validateRequiredData();
            
            // Set up the file I/O
            prepareFileOutput();
        }
    }

    /**
     * Constructor based on an existing settings. Note that this constructor does a "deep" copy.
     *
     * @param _settings The settings that we want to use as basis for the new object.
     * 
     */ 
    public Settings( Settings _settings, int _threadIndex ) throws Exception { 
        
        synchronized ( this ) {
            
            // (Need to ) Handle the "final" data (first)
            initialProcessParametersAsLoaded = 
                assignProperties( _settings.initialProcessParametersAsLoaded );
            initialProcessParametersLowercase = 
                assignProperties( _settings.initialProcessParametersLowercase );
            
            loadSettings( _settings );
            
            this.setDynamicProcessParameter( 
                    BANJO.DATA_THREADINDEX, Integer.toString( _threadIndex ) );
            
            validateRequiredData();
            
            // Set up the file I/O
            prepareFileOutput();
        }
    }

    /**
     * Method to superseed the constructor with command line arguments.
     * 
     * Note: Due to the expanded role of the settings class ("processData"),
     * it is really important that we have a valid settings object immediately
     * from the very start. This is not guaranteed when we use the original
     * constructor that included the processing of the command line arguments.
     * 
     * @param _applicationParameters The list of parameters provided.
     */
  public void processCommandLine( final String[] _applicationParameters ) 
          throws Exception { 
      
      // store in case we need to report on them later
      commandLineParameters = _applicationParameters;
      
      // applicationParameters are the runtime parameters supplied by the user
      // to the "application class" that is the wrapper for executing the search (e.g.,
      // the commandline application, or a gui)
            
      loadSettings( _applicationParameters );

      // Validate any parameter that is part of this class
      validateRequiredData();

      // When loading settings based on application parameters we need to 
      // validate our report file name (we only want to do this once, so there
      // is no need to do it again when we base other settings instances on this
      // 'original' one
      validateReportFiles();
      
      // Set up the file I/O
      prepareFileOutput();

      // Set up access to this class for the static StringUtil class
      StringUtil.setProcessData( this );
  }

    /** Loads the settings based on the parameters from an existing settings object.
     * 
     * @param _settings    The existing settings object to be used as basis for loading.
     */
    public synchronized void loadSettings( final Settings _settings ) 
            throws Exception {
      
      defaultProcessParameters = assignProperties( _settings.defaultProcessParameters );
      passedInParameters = assignProperties( _settings.passedInParameters );
      loadedSettings = assignProperties( _settings.loadedSettings );
      dynamicProcessParameters = assignProperties( _settings.dynamicProcessParameters );

      collectedWarnings = assignSet( _settings.collectedWarnings );
      generalProcessDataStorage = assignSet( _settings.generalProcessDataStorage );
      registeredSettings = assignSet( _settings.registeredSettings );
      processedSettings = assignSet( _settings.processedSettings );
      collectedErrors = assignSet( _settings.collectedErrors );

      validatedSettings = assignProperties( _settings.validatedSettings );
    }

    /** Loads the settings based on the (typically: commandline) parameters provided by the user.
     * 
     * @param _applicationParameters The list of parameters provided by the user to the
     * Banjo application
     */
    protected synchronized void loadSettings( final String[] _applicationParameters ) 
			throws Exception {
		
	  	// If any optional parameters were specified, load them here
	    if ( _applicationParameters != null ) {
		
	        passedInParameters = loadPassedInParameters( _applicationParameters );
	    }
	    
	    // Load the parameters from the settings file 
		loadedSettings = loadFileBasedParameters();
		
		// Load the default parameters, allowing us to keep machine dependent
		// or user-designated "shared" parameters separate from core search parameters
		// (not tested: no support planed at this time, per conversation with Alex)
//		defaultProcessParameters = loadDefaultValuesForParameters();

		// Merge the different sets of loaded parameters
		combineParameters();
	}

    /** Helper function to make a copy of a Properties object.
     * 
     * @param _propertiesToAssign The properties to use as the basis of the assignment.
     */
    protected Properties assignProperties( Properties _propertiesToAssign ) {
        
        Properties clonedProperties = new Properties();
        Set parameterSet;
        Iterator parameterIterator;
        String strNextProperty;
        String strPropertyValue;
        String strParameterName;
        String strParameterValue;

        parameterSet = _propertiesToAssign.keySet();
        parameterIterator = parameterSet.iterator();
        while ( parameterIterator.hasNext() ) {
            
            strParameterName = (String) parameterIterator.next();

            strParameterValue = 
                _propertiesToAssign.getProperty( strParameterName );
            
            clonedProperties.setProperty( 
                    strParameterName, strParameterValue );
        }
        
        return clonedProperties;
    }

    /** Helper function to make a copy of a set of objects (of types SettingItem, String,
     * or BanjoError)
     * 
     * @param _setToAssign The set to assign to.
     */
    protected Set assignSet( Set _setToAssign ) {
        
        Set clonedSet = new HashSet();
        Object objToClone;
        Object clonedObj = null;

        Iterator itemIterator = _setToAssign.iterator();
        
        while ( itemIterator.hasNext() ) {
            
            objToClone = itemIterator.next();
            
            // Check what type the object is, and clone accordingly
            if ( objToClone instanceof MandatorySettingItem ) {
                
                clonedObj = 
                    new MandatorySettingItem( (MandatorySettingItem) objToClone );
            }
            else if ( objToClone instanceof OptionalSettingItem ) {
                
                clonedObj = 
                    new OptionalSettingItem( (OptionalSettingItem) objToClone );
            }
            else if ( objToClone instanceof String ) {
                
                clonedObj = 
                    new String( (String) objToClone );
            }
            else if ( objToClone instanceof BanjoError ) {
                
                clonedObj = 
                    new BanjoError( (BanjoError) objToClone );
            }
            else {
                
                // Add a warning if we end up here
                addToWarnings( new BanjoError( 
                        "(Settings.assignSet) " +
                        "Unknown object type encountered; cannot clone the object of class '" +
                        clonedObj.getClass().getName() + "'",
                        BANJO.ERRORTYPE_ALERT_DEV,
                        "",
                        null ) );
                
                // default, when we don't know or care what type the object is
                clonedObj = null;
            }
            
            // Add any cloned object to our set
            if ( clonedObj != null ) clonedSet.add( clonedObj );
        }
        
        return clonedSet;
    }
	
	protected void combineParameters() throws Exception {
	    
	    String settingsFileName = new String();
		String settingsFileDirectory = new String();
	    Set parameterSet;
		Iterator parameterIterator;
		String strNextProperty;
		String strPropertyValue;
		String strParameterName;
		String strParameterValue;

	    // This "overrides" the "regular" loaded settings with the passedInParameters
	    parameterSet = passedInParameters.keySet();
		parameterIterator = parameterSet.iterator();
		while ( parameterIterator.hasNext() ) {
		    
		    strParameterName = (String) parameterIterator.next();
		    strParameterName = strParameterName.trim();

		    strParameterValue = 
		        passedInParameters.getProperty( strParameterName ).trim();
		    strParameterValue = StringUtil.removeTrailingComment( strParameterValue );
			
		    loadedSettings.setProperty( 
		            strParameterName, strParameterValue );
		}
	    
		dynamicProcessParameters = new Properties();
		generalProcessDataStorage = new HashSet();
			    
	    // Store name and path of settings file
		dynamicProcessParameters.setProperty(
		        BANJO.DATA_SPECIFIEDSETTINGSFILE,
		        settingsFileName );
		dynamicProcessParameters.setProperty(
		        BANJO.DATA_SPECIFIEDSETTINGSFILEDIRECTORY,
		        settingsFileDirectory );
		
        registeredSettings = compileRegisteredSettings();
        
		// Load the "raw" initialSettings: avoid a simple reference to loadedSettings
		// to make initialProcessParametersAsLoaded truly final
	    parameterSet = loadedSettings.keySet();
		parameterIterator = parameterSet.iterator();
		while (parameterIterator.hasNext()) {
		    
			strNextProperty = (String) parameterIterator.next();	
			strPropertyValue = loadedSettings.getProperty( strNextProperty ).trim();
			
			// Special cleanup: remove any substring from the end of the property value
			// after a comment symbol is encountered. Then remove white space around
			// the property value.
			strPropertyValue = StringUtil.removeTrailingComment( strPropertyValue )
					.trim();
            
            // This will add a warning message to the output if the user has deprecated
            // settings as part of the input
            checkForDeprecatedSettings( strNextProperty );
            
            // This will add a warning message if the user supplies a setting that isn't
            // "registered" to Banjo (we provide this as a convenience to users in case
            // of misspelling, etc)
            checkForUnregisteredSettings( strNextProperty );
            

			initialProcessParametersAsLoaded.setProperty( 
			        strNextProperty, strPropertyValue );
			
			initialProcessParametersLowercase.setProperty( 
			        strNextProperty.toLowerCase(), strPropertyValue );
		}

		// Now handle embedded tokens - note that we can't do this in
		// the above loop, because of interdependencies
	    parameterSet = initialProcessParametersAsLoaded.keySet();
		parameterIterator = parameterSet.iterator();
		String updatedPropertyValue;
		
		while ( parameterIterator.hasNext() ) {
		    
			strNextProperty = (String) parameterIterator.next();	
			strPropertyValue = initialProcessParametersAsLoaded.getProperty( 
			        strNextProperty ).trim();

			if ( strPropertyValue.indexOf( BANJO.DEFAULT_TOKENINDICATOR ) >= 0 ) {

			    updatedPropertyValue = new String( parseForTokens( strPropertyValue ) );
			        
				initialProcessParametersAsLoaded.setProperty( 
				        strNextProperty, updatedPropertyValue );
				
			    initialProcessParametersLowercase.setProperty( 
			        strNextProperty.toLowerCase(), updatedPropertyValue );
			}
		}
	}


    /** Utility function that compiles a list of the settings that are known to be valid
     * within the current version of Banjo (as provided by the BANJO class.
     */
    // Check if the supplied setting is not in use anymore, and let the user know by
    // displaying an alert
    public Set compileRegisteredSettings() {

        Set knownSettings = new HashSet();
        
        knownSettings.add( BANJO.SETTING_ASKTOVERIFYSETTINGS );
        knownSettings.add( BANJO.SETTING_BESTNETWORKSARE );
        knownSettings.add( BANJO.SETTING_CMDARG_SETTINGSDIRECTORY );
        knownSettings.add( BANJO.SETTING_CMDARG_SETTINGSFILENAME );
        knownSettings.add( BANJO.SETTING_COMPUTECONSENSUSGRAPH );
        knownSettings.add( BANJO.SETTING_COMPUTEINFLUENCESCORES );
        knownSettings.add( BANJO.SETTING_COOLINGFACTOR );
        knownSettings.add( BANJO.SETTING_CREATEDOTOUTPUT );
        knownSettings.add( BANJO.SETTING_CYCLECHECKERCHOICE );
        knownSettings.add( BANJO.SETTING_DATASET );
        knownSettings.add( BANJO.SETTING_DBNMANDATORYIDENTITYLAGS );
        knownSettings.add( BANJO.SETTING_DECIDERCHOICE );
        knownSettings.add( BANJO.SETTING_DEFAULTMAXPARENTCOUNT );
        knownSettings.add( BANJO.SETTING_DEPREC_NUMBEROFINTERMEDIATEPROGRESSREPORTS );
        knownSettings.add( BANJO.SETTING_DEPREC_WRITETOFILEINTERVAL );
        knownSettings.add( BANJO.SETTING_DISCRETIZATIONEXCEPTIONS );
        knownSettings.add( BANJO.SETTING_DISCRETIZATIONPOLICY );
        knownSettings.add( BANJO.SETTING_DISCRETIZATIONREPORT );
        knownSettings.add( BANJO.SETTING_DISPLAYCONSENSUSGRAPHASHTML );
        knownSettings.add( BANJO.SETTING_DISPLAYDEBUGINFO );
        knownSettings.add( BANJO.SETTING_DISPLAYMEMORYINFO );
        knownSettings.add( BANJO.SETTING_DISPLAYSTATISTICS );
        knownSettings.add( BANJO.SETTING_DISPLAYSTRUCTURES );
        knownSettings.add( BANJO.SETTING_DOTFILEEXTENSION );
        knownSettings.add( BANJO.SETTING_DOTGRAPHICSFORMAT );
        knownSettings.add( BANJO.SETTING_EQUIVALENTSAMPLESIZE );
        knownSettings.add( BANJO.SETTING_ERRORDIRECTORY );
        knownSettings.add( BANJO.SETTING_ERRORFILE );
        knownSettings.add( BANJO.SETTING_EVALUATORCHOICE );
        knownSettings.add( BANJO.SETTING_FILENAMEFORCONSENSUSGRAPH );
        knownSettings.add( BANJO.SETTING_FILENAMEFORTOPGRAPH );
        knownSettings.add( BANJO.SETTING_FILENAMEFORTOPGRAPHSASHTML );
        knownSettings.add( BANJO.SETTING_FILEREPORTINGINTERVAL );
        knownSettings.add( BANJO.SETTING_FULLPATHTODOTEXECUTABLE );
        knownSettings.add( BANJO.SETTING_HTMLFILEEXTENSION );
        knownSettings.add( BANJO.SETTING_INITIALSTRUCTUREFILE );
        knownSettings.add( BANJO.SETTING_INITIALTEMPERATURE );
        knownSettings.add( BANJO.SETTING_INPUTDIRECTORY );
        knownSettings.add( BANJO.SETTING_MAXACCEPTEDNETWORKSBEFORECOOLING );
        knownSettings.add( BANJO.SETTING_MAXMARKOVLAG );
        knownSettings.add( BANJO.SETTING_MAXPARENTCOUNT );
        knownSettings.add( BANJO.SETTING_MAXPARENTCOUNTFORRESTART );
        knownSettings.add( BANJO.SETTING_MAXPROPOSEDNETWORKS );
        knownSettings.add( BANJO.SETTING_MAXPROPOSEDNETWORKSBEFORECOOLING );
        knownSettings.add( BANJO.SETTING_MAXPROPOSEDNETWORKSBEFORERESTART );
        knownSettings.add( BANJO.SETTING_MAXRESTARTS );
        knownSettings.add( BANJO.SETTING_MAXSEARCHTIME );
        knownSettings.add( BANJO.SETTING_MINACCEPTEDNETWORKSBEFOREREANNEALING );
        knownSettings.add( BANJO.SETTING_MINMARKOVLAG );
        knownSettings.add( BANJO.SETTING_MINNETWORKSBEFORECHECKING );
        knownSettings.add( BANJO.SETTING_MINPROPOSEDNETWORKSAFTERHIGHSCORE );
        knownSettings.add( BANJO.SETTING_MINPROPOSEDNETWORKSBEFORERESTART );
        knownSettings.add( BANJO.SETTING_MUSTBEPRESENTEDGESFILE );
        knownSettings.add( BANJO.SETTING_MUSTNOTBEPRESENTEDGESFILE );
        knownSettings.add( BANJO.SETTING_NBEST );
        knownSettings.add( BANJO.SETTING_NOTES );
        knownSettings.add( BANJO.SETTING_OBSERVATIONCOUNT );
        knownSettings.add( BANJO.SETTING_OBSERVATIONSFILE );
        knownSettings.add( BANJO.SETTING_OUTPUTDIRECTORY );
        knownSettings.add( BANJO.SETTING_PRECOMPUTE );
        knownSettings.add( BANJO.SETTING_PROJECT );
        knownSettings.add( BANJO.SETTING_PROPOSERCHOICE );
        knownSettings.add( BANJO.SETTING_REANNEALINGTEMPERATURE );
        knownSettings.add( BANJO.SETTING_REPORTFILE );
        knownSettings.add( BANJO.SETTING_RESTARTWITHRANDOMNETWORK );
        knownSettings.add( BANJO.SETTING_SCREENREPORTINGINTERVAL );
        knownSettings.add( BANJO.SETTING_SEARCHERCHOICE );
        knownSettings.add( BANJO.SETTING_STATISTICSCHOICE );
        knownSettings.add( BANJO.SETTING_SUMMARYFILE );
        knownSettings.add( BANJO.SETTING_TIMESTAMPSTRINGFORFILES );
        knownSettings.add( BANJO.SETTING_TRACKINGFILE );
        knownSettings.add( BANJO.SETTING_USECACHE );
        knownSettings.add( BANJO.SETTING_USER );
        knownSettings.add( BANJO.SETTING_VARCOUNT );
        knownSettings.add( BANJO.SETTING_VARIABLENAMES );
        knownSettings.add( BANJO.SETTING_VARIABLESAREINROWS );
        knownSettings.add( BANJO.SETTING_THREADS );
        knownSettings.add( BANJO.SETTING_FILENAMEPREFIXFORTHREADS );
        knownSettings.add( BANJO.SETTING_XMLINPUTFILES );
        knownSettings.add( BANJO.SETTING_XMLINPUTDIRECTORY );
        knownSettings.add( BANJO.SETTING_XMLOUTPUTDIRECTORY );
        knownSettings.add( BANJO.SETTING_XMLREPORTFILE );
        knownSettings.add( BANJO.SETTING_XMLSETTINGSTOEXPORT );
        knownSettings.add( BANJO.SETTING_BANJOSEED );
        
        return knownSettings;
    }
    
    // Check if the supplied setting is not in use anymore, and let the user know by
    // displaying an alert
    protected void checkForUnregisteredSettings( final String _settingName ) {

        String itemToFind;
        Iterator settingItemIterator = registeredSettings.iterator();
        while ( settingItemIterator.hasNext() ) {
            
            itemToFind = (String) settingItemIterator.next();
            if ( itemToFind.equalsIgnoreCase( _settingName ) ) {
                                
                return;
            }
        }
        
        addToWarnings( new BanjoError( 
            "The setting '" + _settingName +
                "' is not known as a valid setting in " +
                BANJO.APPLICATION_NAME + " " + BANJO.APPLICATION_VERSIONNUMBER + ".",
            BANJO.ERRORTYPE_ALERT_UNKNOWNSETTING,
            _settingName,
            null ) );
    }
    
    // Check if the supplied setting is not in use anymore, and let the user know by
    // displaying an alert
    protected void checkForDeprecatedSettings( final String _settingName ) {
        
        if ( _settingName.trim().equalsIgnoreCase( 
                BANJO.SETTING_DEPREC_NUMBEROFINTERMEDIATEPROGRESSREPORTS ) ) {
            
            addToWarnings( new BanjoError( 
                "The setting '" +
                BANJO.SETTING_DEPREC_NUMBEROFINTERMEDIATEPROGRESSREPORTS +
                "' has been deprecated in Banjo 2.0.",
                BANJO.ERRORTYPE_ALERT_DEPRECATEDSETTING,
                _settingName,
                null ) );
        }
        else if ( _settingName.trim().equalsIgnoreCase( 
                BANJO.SETTING_DEPREC_WRITETOFILEINTERVAL ) ) {
            
            addToWarnings( new BanjoError( 
                "The setting '" +
                BANJO.SETTING_DEPREC_WRITETOFILEINTERVAL +
                "' has been deprecated in Banjo 2.0.",
                BANJO.ERRORTYPE_ALERT_DEPRECATEDSETTING,
                _settingName,
                null ) );
        }
    }
	
	// Parses the supplied string for embedded tokens (e.g., a time stamp token. Note:
	// Currently we only have the need for parsing time stamps)
	protected String parseForTokens( final String _stringToParse ) {
	    
	    String processedString = new String( _stringToParse );
	    String tokenToProcess;
        String defaultToken;
        String valueToInsert;

	    // Parse for embedded time stamps
	    // ------------------------------
	    	    
	    Set tokenSet = new HashSet();
		
	    // Allow multiple, equivalent tokens for the time stamp
	    tokenSet.add( BANJO.DATA_TIMESTAMP_TOKEN );
	    tokenSet.add( BANJO.DATA_TIMESTAMP_TOKEN_ALT0 );
	    tokenSet.add( BANJO.DATA_TIMESTAMP_TOKEN_ALT1 );
        tokenSet.add( BANJO.DATA_TIMESTAMP_TOKEN_ALT2 );
        tokenSet.add( BANJO.DATA_TIMESTAMP_TOKEN_ALT3 );
	    
	    Iterator tokenIterator = tokenSet.iterator();
	    while ( tokenIterator.hasNext() ) {
	        
	        tokenToProcess = (String) tokenIterator.next();
	        
		    if ( _stringToParse.indexOf( tokenToProcess ) >= 0 ) {
				
                // The timeStampFormat won't be validated at this point, so get it
                // directly from the input
			    String tsf = this.getInitialProcessParameterLowercase( 
			            BANJO.SETTING_TIMESTAMPSTRINGFORFILES.toLowerCase() );
                
                if ( tsf == null || tsf == "" ) tsf = BANJO.DEFAULT_TIMESTAMPSTRINGFORFILES;
			    
		        processedString = processedString.replaceAll( tokenToProcess,
		            StringUtil.timeStamp( this, 
	                        BANJO.SETTING_TIMESTAMPSTRINGFORFILES, tsf, 
	                        BANJO.DEFAULT_TIMESTAMPSTRINGFORFILES ) );
		    }
	    }
        
        // generic processing of other tokens
        tokenSet = new HashSet();
        defaultToken = BANJO.DATA_THREADID_TOKEN;
        tokenSet.add( defaultToken );
        tokenSet.add( BANJO.DATA_THREADID_TOKEN_ALT0 );
        
        tokenIterator = tokenSet.iterator();
        while ( tokenIterator.hasNext() ) {
            
            tokenToProcess = (String) tokenIterator.next();
            
            int indexOfSubstring = _stringToParse.toLowerCase().indexOf( tokenToProcess.toLowerCase() ); 
            if ( indexOfSubstring >= 0 ) {
                                
                // we can't use the standard replaceAll function, because it can't ignore the case,
                // and we don't want to modify the letters' case in the source string 
                String part1 = processedString.substring( 0 , indexOfSubstring );
                String part2 = processedString.substring( indexOfSubstring + tokenToProcess.length() );
                // Note that for this token we don't insert the value, because we have a special
                // function that applies a rule
                processedString = part1 + defaultToken + part2;
            }
        }
        
	    return processedString;
	}

    // Parses the supplied string for embedded tokens (e.g., a time stamp token. Note:
    // Currently we mainly have the need for parsing time stamps)
    public String parseForTokensAfterValidation( final String _stringToParse ) {
        
        String processedString = new String( _stringToParse );
        String tokenToProcess;
        String defaultToken;
        String valueToInsert;
                
        Set tokenSet;
        Iterator tokenIterator;
        
        // generic processing of various "general" tokens
        tokenSet = new HashSet();
        tokenSet.add( BANJO.DATA_VARCOUNT_TOKEN );
        tokenSet.add( BANJO.DATA_VARCOUNT_TOKEN_ALT0 );
        valueToInsert = this.getValidatedProcessParameter( BANJO.SETTING_VARCOUNT );
        
        tokenIterator = tokenSet.iterator();
        while ( tokenIterator.hasNext() ) {
            
            tokenToProcess = (String) tokenIterator.next();
            
            int indexOfSubstring = processedString.toLowerCase().indexOf( tokenToProcess.toLowerCase() ); 
            if ( indexOfSubstring >= 0 ) {
                                
                // we can't use the standard replaceAll function, because it can't ignore the case,
                // and we don't want to modify the letters' case in the source string 
                String part1 = processedString.substring( 0 , indexOfSubstring );
                String part2 = processedString.substring( indexOfSubstring + tokenToProcess.length() );
                processedString = part1 + valueToInsert + part2;
            }
        }

        tokenSet = new HashSet();
        tokenSet.add( BANJO.DATA_MINMARKOVLAG_TOKEN );
        tokenSet.add( BANJO.DATA_MINMARKOVLAG_TOKEN_ALT0 );
        valueToInsert = this.getValidatedProcessParameter( BANJO.SETTING_MINMARKOVLAG );
        
        tokenIterator = tokenSet.iterator();
        while ( tokenIterator.hasNext() ) {
            
            tokenToProcess = (String) tokenIterator.next();
            
            int indexOfSubstring = processedString.toLowerCase().indexOf( tokenToProcess.toLowerCase() ); 
            if ( indexOfSubstring >= 0 ) {
                                
                // we can't use the standard replaceAll function, because it can't ignore the case,
                // and we don't want to modify the letters' case in the source string 
                String part1 = processedString.substring( 0 , indexOfSubstring );
                String part2 = processedString.substring( indexOfSubstring + tokenToProcess.length() );
                processedString = part1 + valueToInsert + part2;
            }
        }

        tokenSet = new HashSet();
        tokenSet.add( BANJO.DATA_MAXMARKOVLAG_TOKEN );
        tokenSet.add( BANJO.DATA_MAXMARKOVLAG_TOKEN_ALT0 );
        valueToInsert = this.getValidatedProcessParameter( BANJO.SETTING_MAXMARKOVLAG );
        
        tokenIterator = tokenSet.iterator();
        while ( tokenIterator.hasNext() ) {
            
            tokenToProcess = (String) tokenIterator.next();
            
            int indexOfSubstring = processedString.toLowerCase().indexOf( tokenToProcess.toLowerCase() ); 
            if ( indexOfSubstring >= 0 ) {
                                
                // we can't use the standard replaceAll function, because it can't ignore the case,
                // and we don't want to modify the letters' case in the source string 
                String part1 = processedString.substring( 0 , indexOfSubstring );
                String part2 = processedString.substring( indexOfSubstring + tokenToProcess.length() );
                processedString = part1 + valueToInsert + part2;
            }
        }
        
        return processedString;
    }
    
    // Note: this is not (yet) used in release 2.x
//	protected Properties loadDefaultValuesForParameters() throws Exception {
//	    
//	    Properties loadedParameters = new Properties();
//	    
//		// Load the default values for the settings, if there is a file for them
//		String defaultSettingsDirectoryName = BANJO.DATA_SETTINGSDIRECTORYFORDEFAULTVALUES;
//		String defaultSettingsFileName = BANJO.DATA_SETTINGSFILEFORDEFAULTVALUES;
//		
//		File inputFile = 
//		    new File( defaultSettingsDirectoryName, defaultSettingsFileName );
//		
//		if ( inputFile.exists() ) {
//		    
//		    // TODO: May need to tune FileUtil.loadSettings (it was only designed for loading
//		    // the regular settings values)
//		    loadedParameters = FileUtil.loadSettings( 
//		            defaultSettingsDirectoryName, 
//		            defaultSettingsFileName );
//		}
//		
//		return loadedParameters;
//	}
		
	protected Properties loadFileBasedParameters() throws Exception {

	    Properties loadedParameters = new Properties();
	    
	    String settingsFileName = new String();
		String settingsFileDirectory = new String();
	
		settingsFileName = passedInParameters.getProperty( 
		        BANJO.SETTING_CMDARG_SETTINGSFILENAME.toLowerCase() );	
		
	    if ( settingsFileName == null || settingsFileName.length() < 1 ) {
	        
	        settingsFileName = BANJO.DEFAULT_SETTINGSFILENAME;
            
            // Create a meaningfull error message to the user
            StringBuffer errorMessage = new StringBuffer(
                    "No settings file ('settingsFile' parameter) " +
                    "supplied via command line argument?!" );

            errorMessage.append( BANJO.FEEDBACK_NEWLINE );
            errorMessage.append( BANJO.FEEDBACK_NEWLINE );
            errorMessage.append( "Supplied command line arguments:" );
            for ( int i=0; i < commandLineParameters.length; i++ ) {

                errorMessage.append( BANJO.FEEDBACK_NEWLINE );
                errorMessage.append( BANJO.FEEDBACK_QUOTES );
                errorMessage.append( commandLineParameters[i].toString() );
                errorMessage.append( BANJO.FEEDBACK_QUOTES );
            }
            
            this.addToWarnings( new BanjoError( 
                    errorMessage.toString(),
                    BANJO.ERRORTYPE_ALERT_UNKNOWNSETTING,
                    BANJO.SETTING_CMDARG_SETTINGSFILENAME,
                    null ) );
            
            this.addToWarnings( new BanjoError( 
                    "Default value ('" + BANJO.DEFAULT_SETTINGSFILENAME +
                    "') applied to 'settingsFile' parameter.",
                    BANJO.ERRORTYPE_ALERT_DEFAULTAPPLIED,
                    BANJO.SETTING_CMDARG_SETTINGSFILENAME,
                    null ) );
	    }
	    
	    settingsFileDirectory = passedInParameters.getProperty( 
		        BANJO.SETTING_CMDARG_SETTINGSDIRECTORY.toLowerCase() );
	    
		if ( settingsFileDirectory == null || settingsFileDirectory.length() < 1 ) {
		    
		    settingsFileDirectory = "";
	    }
        
	    // Now load the settings. The file needs to adhere to Java's convention of
	    // listing properties and their values.
        if ( fileUtil == null ) fileUtil = new FileUtil( this );
        loadedParameters = fileUtil.loadSettings( 
                settingsFileDirectory, settingsFileName );
	    
	    return loadedParameters;
	}
	
	protected Properties loadPassedInParameters ( final String[] _applicationParameters ) 
		throws Exception {
	    
	    Properties loadedParameters = new Properties();
		String applicationParameter;
		String strParameterName;
		String strParameterValue;
	    StringTokenizer tokenizer;
		int tokenCount;
		
		for ( int i=_applicationParameters.length-1; i >= 0; i-- ) {

		    applicationParameter = _applicationParameters[i].trim();
		    
		    if ( applicationParameter.indexOf( BANJO.DEFAULT_ITEMVALUESEPARATOR ) < 0 ) {
		        
		        // It seems to make no sense to let a parameter be specified without
		        // a value (even an empty one), so tell the user:
		        throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
			            "\n  (Settings) Cannot recognize the input parameter '" + 
			            applicationParameter + 
			            "'. Please use the 'item=value' format." );
		    }
	    
			tokenizer = 
		        new StringTokenizer( applicationParameter, "=" );
			tokenCount = tokenizer.countTokens();
			
			// 9/8/2005 hjs	1.0.3	Add a little extra flexibility to the command 
			//						line parameter parsing, by allowing the use 
			//						of empty arguments (e.g., to "cancel" an 
			//						already specified value)
			if ( tokenCount > 2 ) {
			    
			    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
			            "\n (Settings) Cannot recognize the input parameter '" +
			            applicationParameter +
			            "'. Please use the 'item=value' format." );
			}
			
			strParameterName = tokenizer.nextToken();

			if ( tokenCount == 2 ) {
			    
			    strParameterValue = tokenizer.nextToken();
			}
			else {
			
			    strParameterValue = "";
			}			      

			loadedParameters.setProperty( 
			        strParameterName.toLowerCase(), strParameterValue );
		}
		
		return loadedParameters;
	}
    
    protected boolean validateReportFiles()throws Exception {

        boolean isDataValid = true;

        String settingNameCanonical;
        String settingNameDescriptive;
        String settingNameForDisplay;
        String settingDataType;
        SettingItem settingItem;
        Set validValues = new HashSet();
        int validationType;
        String strCondition;
        final int maxItemsUsed = 4;
        double[] dblValue = new double[maxItemsUsed];
        SettingItem[] arrSettingItem = new SettingItem[maxItemsUsed];

        
        settingNameCanonical = BANJO.SETTING_REPORTFILE;
        settingNameDescriptive = BANJO.SETTING_REPORTFILE_DESCR;
        settingNameForDisplay = BANJO.SETTING_REPORTFILE_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
        validationType = BANJO.VALIDATIONTYPE_MANDATORY;
        settingItem = processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                BANJO.BANJO_FREEFORMINPUT, 
                BANJO.DEFAULT_REPORTFILE );
        
        if ( !settingItem.isValidSetting() ) {
            
            throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT, 
                    "(Settings.validateRequiredData) " +
                    "Need to supply a valid name for the report file." );
        }
        else {
            
            this.setDynamicProcessParameter( BANJO.DATA_REPORTFILE, 
                    this.getValidatedProcessParameter( BANJO.SETTING_REPORTFILE ));
        }
        
        settingNameCanonical = BANJO.SETTING_XMLREPORTFILE;
        settingNameDescriptive = BANJO.SETTING_XMLREPORTFILE_DESCR;
        settingNameForDisplay = BANJO.SETTING_XMLREPORTFILE_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
        validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        settingItem = processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                BANJO.BANJO_FREEFORMINPUT, 
//                BANJO.DEFAULT_XMLREPORTFILE );
                null );
        
        if ( !settingItem.isValidSetting() ) {
            
            throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT, 
                    "(Settings.validateRequiredData) " +
                    "Need to supply a valid name for the XML report file." );
        }
        else {
            
            this.setDynamicProcessParameter( BANJO.DATA_XMLREPORTFILE, 
                    this.getValidatedProcessParameter( BANJO.SETTING_XMLREPORTFILE ));
        }

        return isDataValid;
    }
		
	public boolean validateRequiredData() throws Exception {

	    boolean isDataValid = true;

	    String settingNameCanonical;
	    String settingNameDescriptive;
	    String settingNameForDisplay;
        String settingDataType;
		SettingItem settingItem;
		Set validValues = new HashSet();
		int validationType;
        String strCondition;
        final int maxItemsUsed = 4;
        double[] dblValue = new double[maxItemsUsed];
        SettingItem[] arrSettingItem = new SettingItem[maxItemsUsed];

        // Note: we use the follwoing convention for seeding the individual threads:
        // - first we get our "base" seed, which can be either user suppplied or based on
        // a rule (currently we use a call system time)
        // - then thread 1 (i.e., internal index 0) is assigned this seed, and all other
        // threads are assigned seeds by adding their thread index to the base
        // - the controller (which is not a search thread per se) gets assigned the (base
        // seed -1) [although currently we don't make use of this value].
        synchronized( getClass() ) {
            
            String strThreadID = getDynamicProcessParameter( BANJO.DATA_THREADINDEX );
            int threadID = -1;
            long banjoSeed;
            
            // Note: the controller doesn't have a threadID, so make sure we get through here
            if ( strThreadID != null ) {
                
                try {

                    // Get the thread's ID
                    threadID = Integer.parseInt( strThreadID );
                }
                catch ( Exception e ) {
                    
                    // use -1 by convention for the "thread controller" (i.e., main Banjo app
                    // with its own settings)
                    threadID = -1;
                }
            }
                
            // Validate the 'seed'
            settingNameCanonical = BANJO.SETTING_BANJOSEED;
            settingNameDescriptive = BANJO.SETTING_BANJOSEED_DESCR;
            settingNameForDisplay = BANJO.SETTING_BANJOSEED_DISP;
            settingDataType = BANJO.VALIDATION_DATATYPE_LONG;
            validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
            settingItem = this.processSetting( settingNameCanonical, 
                    settingNameDescriptive,
                    settingNameForDisplay,
                    settingDataType,
                    validationType,
                    null,
                    Long.toString( baseRandomSeed ) );
    
            String strBanjoSeed;
            if ( settingItem.isValidSetting ) {
            
                strBanjoSeed = getValidatedProcessParameter( BANJO.SETTING_BANJOSEED );
                banjoSeed = Long.parseLong( strBanjoSeed );
            }
            else {
                
                banjoSeed = baseRandomSeed;
            }

            // Compute the seed for the current thread from the base seed and the threadID
            banjoSeed += threadID;
            this.setBanjoSeed( banjoSeed );
            this.setDynamicProcessParameter( BANJO.DATA_BANJOSEED, Long.toString( banjoSeed ) );
        }
        
	    // TODO: Need to validate the next 2 settings as valid directories.
        // Right now the error messages will refer only to files that cannot
        // be found in those directories
	    
	    // Validate the 'Input Directory'
	    settingNameCanonical = BANJO.SETTING_INPUTDIRECTORY;
	    settingNameDescriptive = BANJO.SETTING_INPUTDIRECTORY_DESCR;
	    settingNameForDisplay = BANJO.SETTING_INPUTDIRECTORY_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem = this.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            BANJO.BANJO_FREEFORMINPUT, null );
        
        // Validate the 'Output Directory'
        settingNameCanonical = BANJO.SETTING_OUTPUTDIRECTORY;
        settingNameDescriptive = BANJO.SETTING_OUTPUTDIRECTORY_DESCR;
        settingNameForDisplay = BANJO.SETTING_OUTPUTDIRECTORY_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
        validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        settingItem = this.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                BANJO.BANJO_FREEFORMINPUT, null );
        
        // Validate the 'XML input files'
        settingNameCanonical = BANJO.SETTING_XMLINPUTFILES;
        settingNameDescriptive = BANJO.SETTING_XMLINPUTFILES_DESCR;
        settingNameForDisplay = BANJO.SETTING_XMLINPUTFILES_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
        validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        settingItem = this.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                BANJO.BANJO_FREEFORMINPUT, null );
        
        // Validate the 'XML Input Directory'
        settingNameCanonical = BANJO.SETTING_XMLINPUTDIRECTORY;
        settingNameDescriptive = BANJO.SETTING_XMLINPUTDIRECTORY_DESCR;
        settingNameForDisplay = BANJO.SETTING_XMLINPUTDIRECTORY_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
        validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        settingItem = this.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                BANJO.BANJO_FREEFORMINPUT, null );
        
        // Validate the 'XML Output Directory' (default to the regular output
        // directory when not specified)
        settingNameCanonical = BANJO.SETTING_XMLOUTPUTDIRECTORY;
        settingNameDescriptive = BANJO.SETTING_XMLOUTPUTDIRECTORY_DESCR;
        settingNameForDisplay = BANJO.SETTING_XMLOUTPUTDIRECTORY_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
        validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        settingItem = this.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                BANJO.BANJO_FREEFORMINPUT, 
                getValidatedProcessParameter( BANJO.SETTING_OUTPUTDIRECTORY ) );
        
        // Validate the 'XML settings to export'
        settingNameCanonical = BANJO.SETTING_XMLSETTINGSTOEXPORT;
        settingNameDescriptive = BANJO.SETTING_XMLSETTINGSTOEXPORT_DESCR;
        settingNameForDisplay = BANJO.SETTING_XMLSETTINGSTOEXPORT_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
        validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        settingItem = this.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                BANJO.BANJO_FREEFORMINPUT, null );
        
        
        // Validate the 'prefix for file names when threads are used'
        settingNameCanonical = BANJO.SETTING_FILENAMEPREFIXFORTHREADS;
        settingNameDescriptive = BANJO.SETTING_FILENAMEPREFIXFORTHREADS_DESCR;
        settingNameForDisplay = BANJO.SETTING_FILENAMEPREFIXFORTHREADS_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
        validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        settingItem = this.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                BANJO.BANJO_FREEFORMINPUT, 
                BANJO.SETTING_FILENAMEPREFIXFORTHREADS_DEFAULT );
	    
	    // Validate the displayStatistics flag
	    // (This is not yet used: may want to use in Recorder classes?)
	    settingNameCanonical = BANJO.SETTING_DISPLAYSTATISTICS;
	    settingNameDescriptive = BANJO.SETTING_DISPLAYSTATISTICS_DESCR;
	    settingNameForDisplay = BANJO.SETTING_DISPLAYSTATISTICS_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    validValues.add( BANJO.UI_DISPLAYSTATISTICS_YES );
	    validValues.add( BANJO.UI_DISPLAYSTATISTICS_NO );
	    settingItem = processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            validValues, 
	            BANJO.DEFAULT_DISPLAYSTATISTICS );

	    // Validate the 'display memory info' flag
	    settingNameCanonical = BANJO.SETTING_DISPLAYMEMORYINFO;
	    settingNameDescriptive = BANJO.SETTING_DISPLAYMEMORYINFO_DESCR;
	    settingNameForDisplay = BANJO.SETTING_DISPLAYMEMORYINFO_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    validValues.add( BANJO.UI_DISPLAYMEMORYINFO_YES );
	    validValues.add( BANJO.UI_DISPLAYMEMORYINFO_NO );
	    settingItem = processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            validValues, 
	            BANJO.DEFAULT_DISPLAYMEMORYINFO );
        

        // Validate the 'Threads Count'
        settingNameCanonical = BANJO.SETTING_THREADS;
        settingNameDescriptive = BANJO.SETTING_THREADS_DESCR;
        settingNameForDisplay = BANJO.SETTING_THREADS_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_INTEGER;
        validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        settingItem = this.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                null,
                Integer.toString( BANJO.SETTING_DEFAULT_THREADS ) );
        
        setDynamicProcessParameter( BANJO.DATA_MAXTHREADS , 
                getValidatedProcessParameter( settingNameCanonical ) );
        
        if ( settingItem.isValidSetting() ) {

            try {

                strCondition = new String( "greater than 0" );
                dblValue[0] = Double.parseDouble( 
                        this.getValidatedProcessParameter(
                                settingNameCanonical ));
                if ( dblValue[0] <= 0 ) {
                    
                    this.addToErrors( new BanjoError( 
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
            
            isDataValid = false;
        }
        
        
        /////////////////
        
        int variableCount = -1;

        // Validate the 'Variable Count'
        settingNameCanonical = BANJO.SETTING_VARCOUNT;
        settingNameDescriptive = BANJO.SETTING_VARCOUNT_DESCR;
        settingNameForDisplay = BANJO.SETTING_VARCOUNT_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_INTEGER;
        validationType = BANJO.VALIDATIONTYPE_MANDATORY;
        arrSettingItem[0] = this.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                null,
                Integer.toString( BANJO.BANJO_NOVALUESUPPLIED_NUMBER ) );
        
        if ( arrSettingItem[0].isValidSetting() ) {

            try {

                strCondition = new String( "greater than 1" );
                dblValue[0] = Double.parseDouble( 
                        this.getValidatedProcessParameter(
                                settingNameCanonical ));
                if ( dblValue[0] <= 1 ) {
                    
                    this.addToErrors( new BanjoError( 
                            StringUtil.composeErrorMessage( 
                                    arrSettingItem[0], 
                                    strCondition ),
                            BANJO.ERRORTYPE_INVALIDRANGE,
                            settingNameCanonical,
                            StringUtil.getClassName( this ) ) );
                    
                    isDataValid = false;
                    
                }
            }
            catch ( Exception e ) {

                throw new BanjoException( 
                        BANJO.ERROR_BANJO_DEV, arrSettingItem[0], this );
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
        arrSettingItem[0] = this.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                null, 
                Integer.toString( BANJO.LIMIT_DEFAULTMAXPARENTCOUNT ) );
        
        int defaultMaxParentCount = BANJO.LIMIT_DEFAULTMAXPARENTCOUNT;
        if ( arrSettingItem[0].isValidSetting() ) {
            
            // Make sure it's positive
            try {
    
                strCondition = new String( "greater than 0" );
                dblValue[0] = Double.parseDouble( 
                        this.getValidatedProcessParameter(
                                settingNameCanonical ));
                if ( dblValue[0] <= 0 ) {
                    
                    this.addToErrors( new BanjoError( 
                            StringUtil.composeErrorMessage( 
                                    arrSettingItem[0], 
                                    strCondition ),
                            BANJO.ERRORTYPE_INVALIDRANGE,
                            settingNameCanonical,
                            StringUtil.getClassName( this ) ) );
                    
                    isDataValid = false;
                }
                else {
                    
                    defaultMaxParentCount = Integer.parseInt( 
                            this.getValidatedProcessParameter(
                                    settingNameCanonical ));
                    if ( defaultMaxParentCount > BANJO.LIMIT_DEFAULTMAXPARENTCOUNT ) {

                        // Set an alert: 
                        this.addToWarnings( new BanjoError(
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
                        this.setValidatedProcessParameter(
                                settingNameCanonical, Integer.toString( defaultMaxParentCount ));
                    }
                }
            }
            catch ( Exception e ) {
    
                throw new BanjoException( 
                        BANJO.ERROR_BANJO_DEV, arrSettingItem[0], this );
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
        arrSettingItem[1] = this.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                null, 
                Integer.toString( BANJO.DEFAULT_MAXPARENTCOUNT ) );
        
        int maxParentCount = defaultMaxParentCount;
        
        // Make sure it's positive
        if ( arrSettingItem[1].isValidSetting() ) {
            
            try {

                strCondition = new String( "greater than 0" );
                dblValue[1] = Double.parseDouble( 
                        this.getValidatedProcessParameter(
                                settingNameCanonical ));
                if ( dblValue[1] <= 0 ) {
                    
                    this.addToErrors( new BanjoError( 
                            StringUtil.composeErrorMessage( 
                                    arrSettingItem[1], 
                                    strCondition ),
                            BANJO.ERRORTYPE_INVALIDRANGE,
                            settingNameCanonical,
                            StringUtil.getClassName( this ) ) );
                    
                    isDataValid = false;
                }
                else {
                    
                    maxParentCount = Integer.parseInt( 
                            this.getValidatedProcessParameter(
                                    settingNameCanonical ));
                    if ( maxParentCount > defaultMaxParentCount ) {

                        // Set an alert: 
                        this.addToWarnings( new BanjoError(
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
                        this.setValidatedProcessParameter(
                                settingNameCanonical, Integer.toString( maxParentCount ));
                    }
                }
            }
            catch ( Exception e ) {

                throw new BanjoException( 
                        BANJO.ERROR_BANJO_DEV, arrSettingItem[1], this );
            }
        }
        else {
            
            isDataValid = false;
        }

        // Validate against varCount, if that is valid itself
        if ( !this.getSettingItemValueAsValidated( BANJO.SETTING_VARCOUNT ).equals( 
                BANJO.DATA_SETTINGNOTFOUND ) && 
                !this.getSettingItemValueAsValidated( BANJO.SETTING_VARCOUNT ).equals( 
                        BANJO.DATA_SETTINGINVALIDVALUE ) ) {

            variableCount = Integer.parseInt( this.getValidatedProcessParameter(
                    BANJO.SETTING_VARCOUNT ));
            
            strCondition = new String( "less than the variable count (='" +
                    this.getSettingItemValueAsLoaded( BANJO.SETTING_VARCOUNT ) + "')" );

            if ( dblValue[1] >= variableCount ) {
                
                this.addToErrors( new BanjoError( 
                        StringUtil.composeErrorMessage( 
                                arrSettingItem[1], 
                                strCondition ),
                        BANJO.ERRORTYPE_INVALIDRANGE,
                        settingNameCanonical,
                        StringUtil.getClassName( this ) ) );

                isDataValid = false;
            }
        }
                    
        // Validate the 'min. Markov lag'
        settingNameCanonical = BANJO.SETTING_MINMARKOVLAG;
        settingNameDescriptive = BANJO.SETTING_MINMARKOVLAG_DESCR;
        settingNameForDisplay = BANJO.SETTING_MINMARKOVLAG_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_INTEGER;
        validationType = BANJO.VALIDATIONTYPE_MANDATORY;
        arrSettingItem[1] = this.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                null,
                BANJO.BANJO_NOVALUESUPPLIED_STRING );
        
        if ( arrSettingItem[1].isValidSetting() ) {

            try {
    
                strCondition = new String( "greater than or equal to 0" );
                dblValue[1] = Double.parseDouble( 
                        this.getValidatedProcessParameter(
                                arrSettingItem[1].getItemNameCanonical() ));
                if ( dblValue[1] < 0 ) {
    
                    this.addToErrors( new BanjoError( 
                            StringUtil.composeErrorMessage( 
                                    arrSettingItem[1], 
                                    strCondition ),
                            BANJO.ERRORTYPE_INVALIDRANGE,
                            settingNameCanonical,
                            StringUtil.getClassName( this ) ) );

                    isDataValid = false;
                }
            }
            catch ( Exception e ) {

                throw new BanjoException( 
                        BANJO.ERROR_BANJO_DEV, arrSettingItem[1], this );
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
        arrSettingItem[2] = this.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                null,
                BANJO.BANJO_NOVALUESUPPLIED_STRING );
        
        if ( arrSettingItem[2].isValidSetting() ) {

            try {
                
                strCondition = new String( "greater than or equal to 0" );
                dblValue[2] = Double.parseDouble( 
                        this.getValidatedProcessParameter(
                                arrSettingItem[2].getItemNameCanonical() ));
                
                if ( dblValue[2] < 0 ) {

                    this.addToErrors( new BanjoError( 
                            StringUtil.composeErrorMessage( 
                                    arrSettingItem[2], 
                                    strCondition ),
                            BANJO.ERRORTYPE_INVALIDRANGE,
                            settingNameCanonical,
                            StringUtil.getClassName( this ) ) );

                    isDataValid = false;
                }
            }
            catch ( Exception e ) {

                throw new BanjoException( 
                        BANJO.ERROR_BANJO_DEV, arrSettingItem[2], this );
            }
        }
        else {

            isDataValid = false;
        }
        
        // Only compare the min and the max Markov orders if both values are valid
        if ( arrSettingItem[1].isValidSetting() && arrSettingItem[2].isValidSetting() ) {
                
            try {
    
                strCondition = new String( "less than or equal to" );
                dblValue[1] = Double.parseDouble( 
                        this.getValidatedProcessParameter(
                                arrSettingItem[1].getItemNameCanonical() ));
                dblValue[2] = Double.parseDouble( 
                        this.getValidatedProcessParameter(
                                arrSettingItem[2].getItemNameCanonical() ));
                
                if ( dblValue[1] > dblValue[2] ) {
                    
                    this.addToErrors( new BanjoError( 
                            StringUtil.composeErrorMessage(
                                    arrSettingItem[1], 
                                    arrSettingItem[2], 
                                    strCondition ),
                            BANJO.ERRORTYPE_RULEVIOLATION,
                            settingNameCanonical,
                            StringUtil.getClassName( this ) ) );

                    isDataValid = false;
                }
            }
            catch ( Exception e ) {

                throw new BanjoException( 
                        BANJO.ERROR_BANJO_DEV, arrSettingItem[2], this );
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
        
        if ( !this.getSettingItemValueAsValidated( BANJO.SETTING_MAXMARKOVLAG )
                .equals( BANJO.DATA_SETTINGNOTFOUND ) &&
                !this.getSettingItemValueAsValidated( BANJO.SETTING_MAXMARKOVLAG )
                .equals( BANJO.DATA_SETTINGINVALIDVALUE ) &&
                !this.getSettingItemValueAsValidated( BANJO.SETTING_MINMARKOVLAG )
                .equals( BANJO.DATA_SETTINGNOTFOUND ) &&
                !this.getSettingItemValueAsValidated( BANJO.SETTING_MINMARKOVLAG )
                .equals( BANJO.DATA_SETTINGINVALIDVALUE ) ) {

            tmpMaxMarkovLag = Integer.parseInt( 
                    this.getValidatedProcessParameter(
                            BANJO.SETTING_MAXMARKOVLAG ));
            tmpMinMarkovLag = Integer.parseInt( 
                    this.getValidatedProcessParameter(
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
            arrSettingItem[3] = this.processSetting( settingNameCanonical, 
                    settingNameDescriptive,
                    settingNameForDisplay,
                    settingDataType,
                    validationType,
                    "", 
                    BANJO.BANJO_NOVALUESUPPLIED_STRING );
            
            if ( arrSettingItem[3].isValidSetting() 
                  && !arrSettingItem[3].getItemValueValidated().equals( 
                            BANJO.BANJO_NOVALUESUPPLIED_STRING ) ) {

                // Can't combine the 2 clauses; Also: no need to do anything
                // when no mandatory lags are specified
                if ( arrSettingItem[2].isValidSetting() && 
                        !arrSettingItem[3].getItemValueValidated().equals("") ) {
    
                    int intMaxParentCount = Integer.parseInt( 
                            this.getValidatedProcessParameter(
                            BANJO.SETTING_MAXPARENTCOUNT ));
                    // Need to make sure that the max parent count is at least 1 larger
                    // than the dbn identiy lag count
                    String strDbnMandatoryIdentityLags = 
                        this.getValidatedProcessParameter(
                            BANJO.SETTING_DBNMANDATORYIDENTITYLAGS );
                    StringTokenizer dbnMandatoryLags = new StringTokenizer( 
                            strDbnMandatoryIdentityLags, BANJO.DELIMITER_DEFAULT_LIST );
                    
                    // Check first that it makes sense to have dbnMandatoryLags
                    if ( dblValue[2] < 1 && dbnMandatoryLags.countTokens() > 0 ) {
                        
                        // Can't have dbnMandatoryLags for static bayesnet
                        strCondition = new String( "omitted for static bayesnets " +
                                "(i.e., max. Markov Order is 0)" );
                        this.addToErrors( new BanjoError( 
                                StringUtil.composeErrorMessage( 
                                        arrSettingItem[3], 
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
                        
                        this.addToErrors( new BanjoError( 
                                StringUtil.composeErrorMessage( 
                                        arrSettingItem[3], 
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
                            arrSettingItem[3].getItemValueValidated() );
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
                            
                            this.addToErrors( new BanjoError( 
                                    StringUtil.composeErrorMessage( 
                                            arrSettingItem[3], 
                                            strCondition ),
                                    BANJO.ERRORTYPE_INVALIDRANGE,
                                    settingNameCanonical,
                                    StringUtil.getClassName( this ) ) );
                            
                            isDataValid = false;
                        }
                    }
                }
            }
            else if ( arrSettingItem[3].getItemValueValidated().equals( 
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
        arrSettingItem[1] = this.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                null,
                Long.toString( BANJO.DEFAULT_MINPROPOSEDNETWORKSHIGHSCORE ) );
        
        if ( arrSettingItem[1].isValidSetting() ) {
            
            try {
    
                strCondition = new String( "greater than or equal to 0" );
                dblValue[1] = Double.parseDouble( 
                        this.getValidatedProcessParameter(
                                arrSettingItem[1].getItemNameCanonical() ));
                if ( dblValue[1] < 0 ) {
                    
                    this.addToErrors( new BanjoError( 
                            StringUtil.composeErrorMessage( 
                                    arrSettingItem[1], 
                                    strCondition ),
                            BANJO.ERRORTYPE_INVALIDRANGE,
                            settingNameCanonical,
                            StringUtil.getClassName( this ) ) );

                    isDataValid = false;
                }
            }
            catch ( Exception e ) {
    
                throw new BanjoException( 
                        BANJO.ERROR_BANJO_DEV, arrSettingItem[1], this );
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
        arrSettingItem[1] = this.processSetting( settingNameCanonical, 
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
        arrSettingItem[2] = this.processSetting( settingNameCanonical, 
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
        arrSettingItem[3] = this.processSetting( settingNameCanonical,
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
            if ( ( !arrSettingItem[1].isValidSetting() 
                    && !arrSettingItem[2].isValidSetting() 
                    && !arrSettingItem[3].isValidSetting() ) ||
                 ( arrSettingItem[1].getItemValueValidated().equals( 
                       Integer.toString( BANJO.BANJO_NOVALUESUPPLIED_NUMBER )) 
                    && arrSettingItem[2].getItemValueValidated().equals(
                           Integer.toString( BANJO.BANJO_NOVALUESUPPLIED_NUMBER ))
                    && arrSettingItem[3].getItemValueValidated().equals(
                           Integer.toString( BANJO.BANJO_NOVALUESUPPLIED_NUMBER )) )) {
                
                this.addToErrors( new BanjoError( 
                        "A valid value needs to be supplied for at least one " +
                        "of the settings '" +
                        arrSettingItem[1].getItemNameCanonical() + 
                        "', '" + arrSettingItem[2].getItemNameCanonical() + 
                        "', or '" + arrSettingItem[3].getItemNameCanonical() + 
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
        
        // Validate
        settingNameCanonical = BANJO.SETTING_SCREENREPORTINGINTERVAL;
        settingNameDescriptive = BANJO.SETTING_SCREENREPORTINGINTERVAL_DESCR;
        settingNameForDisplay = BANJO.SETTING_SCREENREPORTINGINTERVAL_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_TIME;
        validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        arrSettingItem[0] = this.processSetting( settingNameCanonical, 
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
        arrSettingItem[0] = this.processSetting( settingNameCanonical, 
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
        arrSettingItem[0] = this.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                null, 
                Integer.toString( BANJO.DEFAULT_NBEST ) );

        if ( arrSettingItem[0].isValidSetting() ) {
            
            try {
                
                strCondition = new String( "greater than 0" );
                dblValue[0] = Double.parseDouble( 
                        this.getValidatedProcessParameter(
                                settingNameCanonical ));
                if ( dblValue[0] <= 0 ) {
                    
                    this.addToErrors( new BanjoError( 
                            StringUtil.composeErrorMessage( 
                                    arrSettingItem[0], 
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
                        arrSettingItem[1] = this.processSetting( settingNameCanonical, 
                                settingNameDescriptive,
                                settingNameForDisplay,
                                settingDataType,
                                validationType,
                                validValues, 
                                BANJO.DEFAULT_BESTNETWORKSARE );
                        
                        if ( !arrSettingItem[1].isValidSetting() ) {
                            
                            // We won't be able to make a choice for the equivalence checker
                            // if this setting is not properly specified
                            isDataValid = false;
                        }
                        else {
                            
                            // Additional validation:
                            if ( Integer.parseInt( this.getValidatedProcessParameter( 
                                    BANJO.SETTING_MINMARKOVLAG ))>0 && !this.getValidatedProcessParameter(
                                    settingNameCanonical).equalsIgnoreCase( 
                                            BANJO.UI_BESTNETWORKSARE_NONIDENTICAL ) ) {
                             
                                // Need to set the value to non-identical, and let the user know
                                this.setValidatedProcessParameter(
                                        settingNameCanonical, BANJO.UI_BESTNETWORKSARE_NONIDENTICAL );
                                
                                this.addToWarnings( new BanjoError( 
                                        "The value of setting '" +
                                        arrSettingItem[1].getItemNameCanonical() + 
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
                        BANJO.ERROR_BANJO_DEV, arrSettingItem[0], this );
            }
        }
        else {
            
            isDataValid = false;
        }
        
        // Note: Every searcher that uses the nBestThresholdScore needs
        // to set the nBestThresholdScore (generally to the initial network score)
        double nBestThresholdScore = BANJO.BANJO_INITIALTHRESHOLDSCORE_BDE;
                
        // Validate the 'ask to verify settings'
        settingNameCanonical = BANJO.SETTING_ASKTOVERIFYSETTINGS;
        settingNameDescriptive = BANJO.SETTING_ASKTOVERIFYSETTINGS_DESCR;
        settingNameForDisplay = BANJO.SETTING_ASKTOVERIFYSETTINGS_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
        validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        validValues.clear();
        validValues.add( BANJO.UI_ASKTOVERIFYSETTINGS_YES );
        validValues.add( BANJO.UI_ASKTOVERIFYSETTINGS_NO );
        arrSettingItem[1] = this.processSetting( settingNameCanonical, 
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
        arrSettingItem[1] = this.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                validValues, 
                BANJO.DEFAULT_RESTARTWITHRANDOMNETWORK );

        if ( arrSettingItem[1].isValidSetting() 
                && arrSettingItem[1].getItemValueValidated().equals( 
                        BANJO.UI_RESTARTWITHRANDOMNETWORK_YES ) 
                && this.isSettingValueValid( BANJO.SETTING_MAXPARENTCOUNT )) {

            maxParentCount = Integer.parseInt( this.getSettingItemValueAsValidated( 
                    BANJO.SETTING_MAXPARENTCOUNT ) );
                
            // The max. parent count for restarts is mandatory, if the "restart with
            // random network" flag is set
            settingNameCanonical = BANJO.SETTING_MAXPARENTCOUNTFORRESTART;
            settingNameDescriptive = BANJO.SETTING_MAXPARENTCOUNTFORRESTART_DESCR;
            settingNameForDisplay = BANJO.SETTING_MAXPARENTCOUNTFORRESTART_DISP;
            settingDataType = BANJO.VALIDATION_DATATYPE_INTEGER;
            validationType = BANJO.VALIDATIONTYPE_MANDATORY;
            arrSettingItem[0] = this.processSetting( settingNameCanonical, 
                    settingNameDescriptive,
                    settingNameForDisplay,
                    settingDataType,
                    validationType,
                    null, 
                    Integer.toString( maxParentCount ) );
            
            if ( arrSettingItem[0].isValidSetting() ) {
            
                if ( this.isSettingValueValid( BANJO.SETTING_MAXPARENTCOUNT ) ) {
                    
                    try {
                        
                        dblValue[0] = Double.parseDouble( 
                                this.getValidatedProcessParameter(
                                        settingNameCanonical ));
            
                        int intMaxParentCount = Integer.parseInt( 
                                this.getValidatedProcessParameter(
                                        BANJO.SETTING_MAXPARENTCOUNT ));
            
                        strCondition = new String( "greater than 0." );
                        
                        if ( dblValue[0] <= 0 ) {
                            
                            this.addToErrors( new BanjoError( 
                                    StringUtil.composeErrorMessage( 
                                            arrSettingItem[0], 
                                            strCondition ),
                                    BANJO.ERRORTYPE_INVALIDRANGE,
                                    settingNameCanonical,
                                    StringUtil.getClassName( this ) ) );
                        }
                        else {

                            // Only display the alert for a relevant searcher
                            if ( this.getValidatedProcessParameter(
                                            BANJO.SETTING_SEARCHERCHOICE ).equalsIgnoreCase( 
                                                    BANJO.UI_SEARCHER_GREEDY )) {
                                
                                if ( dblValue[0] > intMaxParentCount ) {
    
                                    int intMaxParentCountForRestarts = Integer.parseInt( 
                                            this.getValidatedProcessParameter(
                                                    settingNameCanonical ));
                                    // Set an alert: 
                                    this.addToWarnings( new BanjoError(
                                        "The specified value for the setting '" +
                                        settingNameCanonical +
                                        "' (=" + intMaxParentCountForRestarts +
                                        ") has exceeded its acceptable value (=" +
                                        intMaxParentCount +
                                        "), and is reduced to this limiting value.",
                                        BANJO.ERRORTYPE_ALERT_CORRECTEDCHOICE,
                                        settingNameCanonical,
                                        null ) );
                                    
                                        this.setValidatedProcessParameter(
                                                settingNameCanonical, Integer.toString( intMaxParentCount ));
                                }
                            }
                        }
                    }
                    catch ( Exception e ) {
                        
                        throw new BanjoException( 
                                BANJO.ERROR_BANJO_DEV, arrSettingItem[0], this );
                    }
                }
            }
            else {
                
                isDataValid = false;
            }
        }
        
	    return isDataValid;
	}
    
    public String[] validateXMLresultFiles() throws Exception {

        String[] xmlFileList;
        
        try {
        
            
//            if ( true ) {
//                
//                xmlFileList = new String[1];
//                xmlFileList[0] = "";
//                return xmlFileList;
//            }
            
            
            
            
            boolean isDataValid = true;
            String xmlInputFiles;
            String delimiter = ",";
            
            // First validate the directory info
    
            String strInputDirectory = getValidatedProcessParameter( 
                    BANJO.SETTING_XMLINPUTDIRECTORY );
            File inputDirectory = new File( strInputDirectory );
            if ( !inputDirectory.isDirectory() ) {
                
//                throw new BanjoException( BANJO.ERRORTYPE_ALERT_OTHER,
//                        "'" + strInputDirectory + "' is not a valid (xml input) directory." );
            }
            //
            String strOutputDirectory = getValidatedProcessParameter( 
                    BANJO.SETTING_XMLOUTPUTDIRECTORY );
            File outputDirectory = new File( strOutputDirectory );
            if ( !outputDirectory.isDirectory() ) {
                
                throw new BanjoException( BANJO.ERRORTYPE_ALERT_OTHER,
                        "'" + strOutputDirectory + "' is not a valid (xml output) directory." );
            }
    
            // Split the (comma-separated) list into its individual components
    
            xmlInputFiles = getValidatedProcessParameter( BANJO.SETTING_XMLINPUTFILES );
            
            if ( xmlInputFiles.equalsIgnoreCase( "" )) {
                
                xmlFileList = new String[1];
                xmlFileList[0] = "";
                return xmlFileList;
            }
            
            
            // Store the names in an ordered way (even though this is only used
            // - and useful) for the case where the observations files are "related"
            xmlFileList = null;
            int xmlFileCount = 0;
            int xmlFileIndex = 0;
            
            // Process potentially multiple files
            StringTokenizer xmlFileTokenizer = 
                new StringTokenizer( xmlInputFiles, BANJO.DELIMITER_DEFAULT_LIST_XML );
    
            String strNextFile;
            String[] strTempWildcardFiles;
            FilenameFilter filter;
            
            // Are wildcards used to describe the files?
            if ( xmlInputFiles.indexOf( BANJO.DEFAULT_WILDCARDINDICATOR ) < 0 ) {
                
                // Without wildcards we have as many files as there are tokens
                xmlFileCount = xmlFileTokenizer.countTokens();
                
                //// This won't happen for xml file(s)
                if ( xmlFileCount == 0 ) {
                    
                    // Can't continue without an observations file
                    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT, 
                            "The input for the xml file setting (='" +
                            xmlInputFiles + "') did not yield any usuable file." );
                }
                ////
                
                xmlFileList = new String[ xmlFileCount ];
                
                while ( xmlFileTokenizer.hasMoreTokens() ) {
                    
                    // We simply add each file name to the array
                    strNextFile = xmlFileTokenizer.nextToken().trim();
                    xmlFileList[ xmlFileIndex ] = 
                        strNextFile.trim();
                    xmlFileIndex++;
                }
            }
            else {
                
                // Wildcard case: allow positive wildcards (files to add)
                // and negative wildcards (files to omit)
                Set wildcardXMLFiles = new HashSet();
                Set omittedWildcardXMLFiles = new HashSet();
            
                while ( xmlFileTokenizer.hasMoreTokens() ) {
                    
                    // Note: we will validate the individual files (i.e., whether
                    // they exist) when we try to load the data
                    
                    strNextFile = xmlFileTokenizer.nextToken().trim();
                    if ( strNextFile.charAt(0) == '-' ) {
                        
                        filter = new JZOOWildCardFilter( 
                                strNextFile.substring(1).trim() );
                    }
                    else {
                    
                        filter = new JZOOWildCardFilter( strNextFile );
                    }
                    
                    strTempWildcardFiles = inputDirectory.list( filter );
                    
                    if ( strTempWildcardFiles.length < 1 ) {
                        
                        throw new BanjoException( 
                                BANJO.ERROR_BANJO_USERINPUT, 
                                "\nThe wildcard specification '" + strNextFile +
                                "' for the observation file(s) did not result in any " +
                                "valid file." );
                    }
                    
                    // Add each file name to the respective set
                    for ( int i=0; i < strTempWildcardFiles.length; i++ ) {
                        
                        if ( strNextFile.charAt(0) == '-') {
                            
                            omittedWildcardXMLFiles.add( strTempWildcardFiles[i] );
                        }
                        else {
                            
                            wildcardXMLFiles.add( strTempWildcardFiles[i] );
                        }
                    }
                }
                            
                if ( !wildcardXMLFiles.isEmpty() ) {
                    
                    // Take away any files
                    if ( !omittedWildcardXMLFiles.isEmpty() ) {
                    
                        wildcardXMLFiles.removeAll( 
                                omittedWildcardXMLFiles );
    
                        String omittedWildcardFiles = new String();
                        Iterator wildcardIterator = 
                            omittedWildcardXMLFiles.iterator();
                        
                        omittedWildcardFiles = (String) wildcardIterator.next();
                        String strNextWildcardFile = null;
                        while ( wildcardIterator.hasNext() ) {
                            
                            strNextWildcardFile = (String) wildcardIterator.next();
                            omittedWildcardFiles += BANJO.DELIMITER_DEFAULT_LIST_XML + strNextWildcardFile;
                        }
                        
                        setDynamicProcessParameter( 
                                BANJO.DATA_OMITTEDWILDCARDXMLFILES, 
                                omittedWildcardFiles );
                    }
                }
                
                // Finally, create the array with the observation files 
                // (need to check if still non-empty)
                if ( !wildcardXMLFiles.isEmpty() ) {
    
                    xmlFileCount = wildcardXMLFiles.size();
                    xmlFileList = new String[ 
                             wildcardXMLFiles.size() ];
                    String wildcardFiles = new String();
                    Iterator wildcardIterator = wildcardXMLFiles.iterator();
                    
                    wildcardFiles = (String) wildcardIterator.next();
                    xmlFileList[0] = wildcardFiles;
                    int fileIndex = 1;
                    String strNextWildcardFile = null;
                    while ( wildcardIterator.hasNext() ) {
                        
                        strNextWildcardFile = (String) wildcardIterator.next();
                        wildcardFiles += BANJO.DELIMITER_DEFAULT_LIST_XML + strNextWildcardFile;
                        xmlFileList[fileIndex] = strNextWildcardFile;
                        fileIndex++;
                    }
                    setDynamicProcessParameter( 
                            BANJO.DATA_WILDCARDXMLFILES, wildcardFiles );
                }
                else {
                    
                    // No valid files found: can't continue
                    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT, 
                            "The input for the XML file setting (='" +
                            xmlInputFiles + "') did not yield any usuable file." );
                }
            }
                    
            for ( int i=0; i<xmlFileList.length; i++ ) {
                
                xmlFileList[i] = strInputDirectory + File.separator + xmlFileList[i];
            }
        }
        catch ( BanjoException e ) { 

            throw new BanjoException( e );
        }
        catch ( Exception e ) { 

            throw new BanjoException( e, 
                    BANJO.ERROR_BANJO_XML, "Error while validating " +
                    "the user-supplied XML result file(s)." );
        }
        
        return xmlFileList;
    }

	public StringBuffer compileErrorMessages() {

	    StringBuffer dataTypeErrors =  
	        new StringBuffer( BANJO.BUFFERLENGTH_STAT );
	    StringBuffer missingValueErrors =  
	        new StringBuffer( BANJO.BUFFERLENGTH_STAT );
	    StringBuffer rulesViolationErrors =  
	        new StringBuffer( BANJO.BUFFERLENGTH_STAT );
	    StringBuffer invalidRangeErrors =  
	        new StringBuffer( BANJO.BUFFERLENGTH_STAT );
	    StringBuffer otherErrors =  
	        new StringBuffer( BANJO.BUFFERLENGTH_STAT );
	    StringBuffer errorMessages =  
	        new StringBuffer( BANJO.BUFFERLENGTH_STAT );
	    BanjoError errorItem;
	    
		Iterator errorsIterator = collectedErrors.iterator();
		while ( errorsIterator.hasNext() ) {
		    errorItem = ( BanjoError ) errorsIterator.next();
		    
		    switch ( errorItem.getErrorType() )
		    {
		    case BANJO.ERRORTYPE_MISSINGVALUE:
	    	    
		        missingValueErrors.append( "(" + 
		                BANJO.ERRORDESCRIPTION_MISSINGVALUE + ") " +
		                errorItem.getErrorMessageText() + BANJO.FEEDBACK_NEWLINE );
	    	    break;

		    case BANJO.ERRORTYPE_MISMATCHEDDATATYPE:
	    	    
		        dataTypeErrors.append( "(" +
		        		BANJO.ERRORDESCRIPTION_MISMATCHEDDATATYPE + ") " +
		                errorItem.getErrorMessageText() + BANJO.FEEDBACK_NEWLINE );
	    	    break;

		    case BANJO.ERRORTYPE_INVALIDRANGE:
	    	    
		        invalidRangeErrors.append( "(" +
		        		BANJO.ERRORDESCRIPTION_INVALIDRANGE + ") " +
		                errorItem.getErrorMessageText() + BANJO.FEEDBACK_NEWLINE );
	    	    break;

            case BANJO.ERRORTYPE_INVALIDCHOICE:
                
                invalidRangeErrors.append( "(" +
                        BANJO.ERRORDESCRIPTION_INVALIDCHOICE + ") " +
                        errorItem.getErrorMessageText() + BANJO.FEEDBACK_NEWLINE );
                break;

            case BANJO.ERRORTYPE_INVALIDPATH:
                
                invalidRangeErrors.append( "(" +
                        BANJO.ERRORDESCRIPTION_INVALIDPATH + ") " +
                        errorItem.getErrorMessageText() + BANJO.FEEDBACK_NEWLINE );
                break;

		    case BANJO.ERRORTYPE_RULEVIOLATION:
	    	    
		        rulesViolationErrors.append( "(" +
		        		BANJO.ERRORDESCRIPTION_RULEVIOLATION + ") " +
		                errorItem.getErrorMessageText() + BANJO.FEEDBACK_NEWLINE );
	    	    break;

		    case BANJO.ERRORTYPE_DOTINTERRUPTION:
	    	    
		        rulesViolationErrors.append( "(" +
		        		BANJO.ERRORDESCRIPTION_DOTINTERRUPTION + ") " +
		                errorItem.getErrorMessageText() + BANJO.FEEDBACK_NEWLINE );
	    	    break;

            case BANJO.ERRORTYPE_DOTEXECUTION:
                
                rulesViolationErrors.append( "(" +
                        BANJO.ERRORDESCRIPTION_DOTEXECUTION + ") " +
                        errorItem.getErrorMessageText() + BANJO.FEEDBACK_NEWLINE );
                break;

            case BANJO.ERRORTYPE_POSTPROCESSING:
                
                rulesViolationErrors.append( "(" +
                        BANJO.ERRORDESCRIPTION_POSTPROCESSING + ") " +
                        errorItem.getErrorMessageText() + BANJO.FEEDBACK_NEWLINE );
                break;
	    	    
		    default: 
	    	    
		        otherErrors.append( "(" +
		        		BANJO.ERRORDESCRIPTION_OTHER + ") " +
		                errorItem.getErrorMessageText() + BANJO.FEEDBACK_NEWLINE );
	
		       	break;
		    }
		}
		
		errorMessages.append( dataTypeErrors );
		errorMessages.append( missingValueErrors );
		errorMessages.append( invalidRangeErrors );
		errorMessages.append( rulesViolationErrors );
		errorMessages.append( otherErrors );
				    
		return errorMessages;
	}
	
	public StringBuffer compileWarningMessages() {

	    StringBuffer warnings =  
	        new StringBuffer( BANJO.BUFFERLENGTH_STAT );
	    
	    BanjoError warningItem;
	    
		Iterator errorsIterator = collectedWarnings.iterator();
		while ( errorsIterator.hasNext() ) {
		    warningItem = ( BanjoError ) errorsIterator.next();
		    
		    switch ( warningItem.getErrorType() )
		    {
            case BANJO.ERRORTYPE_ALERT_CORRECTEDCHOICE:
                
                warnings.append( "(" +
                        BANJO.ERRORDESCRIPTION_ALERT_CORRECTEDCHOICE + ") " +
                        warningItem.getErrorMessageText() );
                break;


            case BANJO.ERRORTYPE_ALERT_MISSINGDOTLOCATION:
                
                warnings.append( "(" +
                        BANJO.ERRORDESCRIPTION_ALERT_MISSINGDOTLOCATION + ") " +
                        warningItem.getErrorMessageText() );
                break;

		    case BANJO.ERRORTYPE_ALERT_OTHER:
	    	    
		        warnings.append( "(" +
		        		BANJO.ERRORDESCRIPTION_ALERT_OTHER + ") " +
		        		warningItem.getErrorMessageText() );
	    	    break;
                
            case BANJO.ERRORTYPE_WARNING_INVALIDCHOICE:
                
                warnings.append( "(" +
                        BANJO.ERRORDESCRIPTION_WARNING_INVALIDCHOICE + ") " +
                        warningItem.getErrorMessageText() );
                break;
                
            case BANJO.ERRORTYPE_ALERT_DEPRECATEDSETTING:
                
                warnings.append( "(" +
                        BANJO.ERRORDESCRIPTION_ALERT_DEPRECATEDSETTING + ") " +
                        warningItem.getErrorMessageText() );
                break;
                
            case BANJO.ERRORTYPE_ALERT_UNKNOWNSETTING:
                
                warnings.append( "(" +
                        BANJO.ERRORDESCRIPTION_ALERT_UNKNOWNSETTING + ") " +
                        warningItem.getErrorMessageText() );
                break;
                
            case BANJO.ERRORTYPE_ALERT_DEFAULTAPPLIED:
                
                warnings.append( "(" +
                        BANJO.ERRORDESCRIPTION_ALERT_DEFAULTAPPLIED + ") " +
                        warningItem.getErrorMessageText() );
                break;
                
            case BANJO.ERRORTYPE_ALERT_DEV:
                
                warnings.append( "(" +
                        BANJO.ERRORDESCRIPTION_ALERT_DEV + ") " +
                        warningItem.getErrorMessageText() );
                break;
	    	    
		    default: 
	    	    
		        warnings.append( "(" +
		        		BANJO.ERRORDESCRIPTION_WARNING_OTHER + ") " +
		        		warningItem.getErrorMessageText() );
	
		       	break;
		    }
		    
		    if ( errorsIterator.hasNext() ) {
		        
		        warnings.append( BANJO.FEEDBACK_NEWLINE );
		    }
		}
				    
		return warnings;
	}
		
	// Wrapper around the internal collection for the processed items
	protected void addToProcessedSettings( final SettingItem _settingItem ) {
        
	    processedSettings.add( _settingItem );
	}

	public Set getProcessedSettings() {
	    
	    return processedSettings;
	}

    public String getSettingItemValueAsValidated( final String _settingName ) {
        
        SettingItem itemToFind;
        
        // If we can't find a matching setting, we return this value:
        String itemValue =  new String( BANJO.DATA_SETTINGNOTFOUND );
        
        Iterator settingItemIterator = processedSettings.iterator();
        while ( settingItemIterator.hasNext() ) {
            
            itemToFind = ( SettingItem ) settingItemIterator.next();
            if ( itemToFind.getItemNameForComparison().equalsIgnoreCase( 
                    _settingName ) ) {
                
                itemValue = itemToFind.getItemValueValidated();
                
                // if the setting has an invalid value, we return it
                if ( !itemToFind.isValidSetting() )
                    itemValue = BANJO.DATA_SETTINGINVALIDVALUE;
                
                break;
            }
        }
        
        return itemValue;
    }

    public String getSettingItemValueAsLoaded( final String _settingName ) {
        
        SettingItem itemToFind;
        
        // If we can't find a matching setting, we return this value:
        String itemValue =  new String( BANJO.DATA_SETTINGNOTFOUND );
        
        Iterator settingItemIterator = processedSettings.iterator();
        while ( settingItemIterator.hasNext() ) {
            
            itemToFind = ( SettingItem ) settingItemIterator.next();
            String tmpName = itemToFind.getItemNameForComparison();

            if ( itemToFind.getItemNameForComparison().equalsIgnoreCase( 
                    _settingName ) ) {
                
                itemValue = itemToFind.getItemValueAsLoaded();
                
                // if the setting has an invalid value, we return it
                if ( !itemToFind.isValidSetting() )
                    itemValue = BANJO.DATA_SETTINGINVALIDVALUE;
                
                break;
            }
        }
        
        return itemValue;
    }
	
	public boolean isSettingValueValid( final String _settingName ) {
	    
	    boolean isSettingValueValid = true;
	    
	    if ( getSettingItemValueAsValidated( _settingName ).equals( 
	            	BANJO.DATA_SETTINGNOTFOUND ) || 
             getSettingItemValueAsValidated( _settingName ).equals( 
    	            BANJO.DATA_SETTINGINVALIDVALUE ) ) {
	        
	        isSettingValueValid = false;
	    }
	    
	    return isSettingValueValid;	    
	}
    
    /**
     * Adds a set of errors to the collectedErrors set.
     */
	// Add a set of errors to the collectedErrors set
	public void addToErrors( final Set _errorItems ) {
	    
	    collectedErrors.addAll( _errorItems );
	}
    
    /**
     * Add a single error to the collectedErrors set.
     */
	// Add a single error to the collectedErrors set
	public void addToErrors( final BanjoError _errorItem ) {
	    
	    collectedErrors.add( _errorItem );
	}
    
    /**
     * Add a single error to the collectedErrors set.
     */
	// In select exception instances (e.g., interrupted execution of dot, etc)
	// we want to pass along the exception, so we can provide the trace
	public void addToErrors( final BanjoError _errorItem, final Exception e ) {
	    
	    collectedErrors.add( _errorItem );

	    // Add the optional stack trace if the user has this option set
		if ( e != null && 
		        this.getValidatedProcessParameter( BANJO.SETTING_DISPLAYDEBUGINFO )
	   	        	.equals( BANJO.UI_DEBUGINFO_STACKTRACE ) ) {
	    
	   	    e.printStackTrace();
	   	}
	}
    
    /**
     * Get the set of collected errors.
     */
	public Set getCollectedErrors() {
	    
	    return collectedErrors;
	}
    
    /**
     * Adds a set of warnings to the collectedWarnings set.
     */
	// Add a set of warnings to the collectedWarnings set
	public void addToWarnings( final Set _warningItems ) {
	    
	    collectedWarnings.addAll( _warningItems );
	}
    
    /**
     * Add a single warning to the collectedWarnings set.
     */
	// Add a single warning to the collectedWarnings set
	public void addToWarnings( final BanjoError _warningItem ) {
	    
	    collectedWarnings.add( _warningItem );
	}
    
    /**
     * Get the set of collected warnings.
     */
	// Add a set of warnings to the collectedWarnings set
	public Set getCollectedWarnings() {
	    
	    return collectedWarnings;
	}

    /**
     * Checks if the process encountered any (fatal) problems.
     */
	// Check if the error set is (not) empty
    public boolean wereThereProblems() throws Exception {
        
        boolean errorsEncountered = false;
        
        if ( getCollectedErrors().size() > 0 ) {
            			
			errorsEncountered = true;
		}
        
        return errorsEncountered;
    }

    
    /**
     * Processes the supplied setting.
     */
	// This is a simple wrapper around the validation of a setting 
	// that is provided by the settingItem class
	// Note that it encapsulates access to the internal storage of the
	// setting items and their values (currently using a simple property
	// called initialProcessParametersLowercase)
	// Features: makes lookup independent of caps in spelling
    public SettingItem processSetting( 
            final String _settingNameCanonical,
            final String _settingNameDescriptive,
            final String _settingNameForDisplay,
            final String _settingDataType,
            final int _validationType,
            final Object _additionalInfo, 
            final Object _defaultValue ) throws Exception {
                
        // _additionalInfo is used for acceptable values, patterns, etc.,
        // based on the type of data to be validated
        
	    String settingValue = new String();
		SettingItem settingItem = null;

	    settingValue = this.initialProcessParametersLowercase.getProperty(
	            _settingNameCanonical.toLowerCase() );
        
		if ( _validationType == BANJO.VALIDATIONTYPE_OPTIONAL ) {
		    
		    // Set up validation of an optional item 
		    settingItem = new OptionalSettingItem(
			    	_settingNameCanonical,_settingNameDescriptive, _settingNameForDisplay );
		}
		else if ( _validationType == BANJO.VALIDATIONTYPE_MANDATORY ) {

		    // Set up validation of a mandatory item 
		    settingItem = new MandatorySettingItem(
		    	_settingNameCanonical,_settingNameDescriptive, _settingNameForDisplay );
		}
		else {
		    
		    // To use a new type of validation object besides OptionalSettingItem and
		    // MandatorySettingItem, that object needs to be properly defined, hence:   
		    throw new BanjoException( BANJO.ERROR_BANJO_DEV, 
		            "(Settings.processSetting) " +
		            "Development issue: " +
		            "Need to define a new class for the attempted validation! " +
		            "(Associated 'validation type' value = '" + _validationType +
		            "')." );
		}
		    
	    // Finally validate the setting item
	    settingItem.validate( 
	            settingValue,
	            _settingDataType,
	            _validationType,
	            _additionalInfo,
	            _defaultValue );
                
	    // Add the setting item to our "processed items" collection
	    addToProcessedSettings( settingItem );
	    
	    // If the validation encountered any errors, then collect those
	    if ( !settingItem.isValidSetting() ) {
	        
	        addToErrors( settingItem.getCollectedErrors() );
	    }
		
	    return settingItem;
    }

    /**
     * Applies formatting to the supplied string.
     */
    protected String formatForDisplay( final String _textToFormat ) {
        
        String prefix = BANJO.ERRORMESSAGEDISPLAYPREFIX;

        return prefix + _textToFormat;
    }
	
	// -----------------------------------------
	// Access functions to individual parameters
	// -----------------------------------------

    /**
     * @return Returns the initialProcessParametersLowercase.
     */
    public String getInitialProcessParameterLowercase( final String _settingName ) {
        
        return initialProcessParametersLowercase.getProperty( 
                _settingName );
    }

    /**
     * @return Returns the value of the specified validated setting.
     */
	// Validated data access
    // Wrapper for retrieving a value from a processed (validated) setting item
	public String getValidatedProcessParameter( final String _settingName ) {
        
	    SettingItem itemToFind; 
	    String itemValue =  new String( BANJO.DATA_SETTINGNOTFOUND );
	    
		Iterator settingItemIterator = processedSettings.iterator();
		while ( settingItemIterator.hasNext() ) {
		    
		    itemToFind = ( SettingItem ) settingItemIterator.next();
//            String tmp = itemToFind.toString();
//            System.out.println( tmp );
		    if ( itemToFind.getItemNameForComparison().equalsIgnoreCase( 
		            _settingName ) ) {
		        
		        itemValue = itemToFind.getItemValueValidated();
		        break;
		    }
		}

        if ( BANJO.DEBUG && BANJO.TRACE_VALIDATEDPARAMS ) {
        
            System.out.print( "\nGETTING validated param " + _settingName + 
                "= '" + itemValue +
                "'");
        }
        
	    return itemValue;
	}

    
    /**
     * Sets the validated process parameter (setting) to the supplied value.
     */
	public void setValidatedProcessParameter( final String _settingName, 
	        final String _settingValue ) throws Exception {

        if ( BANJO.DEBUG && BANJO.TRACE_VALIDATEDPARAMS ) {
        
            System.out.print( "\nSetting validated param " + _settingName + 
                "= '" + _settingValue +
                "'");
        }
        
	    SettingItem itemToFind =  null; 
	    
		Iterator settingItemIterator = processedSettings.iterator();
		while ( settingItemIterator.hasNext() ) {
		    
		    itemToFind = ( SettingItem ) settingItemIterator.next();
            
            if ( itemToFind.getItemNameForComparison().contains("xml") ) {
                int x=0;
            }
            
            
		    if ( itemToFind.getItemNameForComparison().equalsIgnoreCase( 
		            _settingName ) ) {
		        
		        itemToFind.setItemValueValidated( _settingValue );
		        break;
		    }
		}
		
		if ( itemToFind == null ) {
		    
		    // Currently we don't let outside code set the value of setting items
		    // that don't exist in our processedSettings collection
		    // (Note: it's conceivable that one could want different behaviour
		    // here, but it would be at the cost of increased complexity elsewhere
		    // as well as [likely] less predictability)
            throw new BanjoException( BANJO.ERROR_BANJO_DEV,
                    "(Settings.setValidatedProcessParameter) " +
            		"(Developer issue) " +
            		"There is no setting item '" + _settingName + "' " +
    				"for assigning the value ('" + _settingValue + "')."  );
		}
	}
    
    /**
     * Gets the value of the specified dynamic process parameter.
     */
	// Dynamic data: generally data used by core classes in the implementation
	// of a given search strategy
	public String getDynamicProcessParameter( final String _parameterName ) {

        String itemValue =  dynamicProcessParameters.getProperty( 
                _parameterName );
        
        // 
//        if ( itemValue == null ) { 
//            
//            itemValue = BANJO.DATA_STRINGDOESNOTEXIST;
//        }
        
        if ( BANJO.DEBUG && BANJO.TRACE_DYNAMICPARAMS ) {
        
            System.out.print( "\nGETTING dynamic param " + _parameterName + 
                "= '" + itemValue +
                "'");
        }
        
		return itemValue;
	}
    
    /**
     * Sets the dynamic process parameter to the supplied value.
     */
	public void setDynamicProcessParameter(
            final String _parameterName, 
	        final String _parameterValue ) {

        if ( BANJO.DEBUG && BANJO.TRACE_DYNAMICPARAMS ) {
        
            System.out.print( "\nSetting dynamic param " + _parameterName + 
                "= '" + _parameterValue +
                "'");
        }
		
		dynamicProcessParameters.setProperty(
		        _parameterName, _parameterValue);
	}
	
	/**
	 * @return Returns the dynamicProcessParameters.
	 */
	public Properties getDynamicProcessParameters() {
		return dynamicProcessParameters;
	}
    
    /**
     * @param objectToAdd The object to add to the generalProcessDataStorage set.
     */
    public void addToGeneralProcessDataStorage( Object objectToAdd ) {
        generalProcessDataStorage.add( objectToAdd );
    }
    /**
     * @return Returns the generalProcessDataStorage.
     */
    public Set getGeneralProcessDataStorage() {
        return generalProcessDataStorage;
    }
    /**
     * @return Returns the size of the generalProcessDataStorage.
     */
    public int getStorageSize() {
        return generalProcessDataStorage.size();
    }
	
    /**
     * @return Returns the highScoreStructureSet.
     */
    public TreeSet getHighScoreStructureSet() {
        return highScoreStructureSet;
    }
    /**
     * @param highScoreStructureSet The highScoreStructureSet to set.
     */
    public void setHighScoreStructureSet(TreeSet highScoreStructureSet) {
        this.highScoreStructureSet = highScoreStructureSet;
    }

	/**
	 * @return Convenient display of the current sets of parameters 
	 * (validated and passed-in). 
	 * Lists items within sets in alphabetic order.
	 */
	public synchronized String toString() {
	    
	    StringBuffer collectedSettings = 
	        new StringBuffer( BANJO.BUFFERLENGTH_STAT_INTERNAL );
	    	    
	    SortedSet parameterSet;
		Iterator parameterIterator;
		String strParameterName;
		String strParameterValue;
        Properties tmpValidatedSettings = new Properties();
		
		
		collectedSettings.append( "Validated Parameters (so far):\n" );

        if ( processedSettings != null ) {
            
    		// Create a simple properties set of the validated parameters
    	    SettingItem nextItem; 
    	    
    		Iterator settingItemIterator = processedSettings.iterator();
    		while ( settingItemIterator.hasNext() ) {
    		    
    		    nextItem = ( SettingItem ) settingItemIterator.next();
    		    strParameterName = nextItem.getItemNameCanonical();
    		    strParameterName = strParameterName.trim();
    		    
    		    if ( nextItem.isValidSetting() ) {
    		        
    			    strParameterValue = 
    			        nextItem.getItemValueValidated().trim();
    		    }
    		    else {
    		        
    		        strParameterValue = "(no valid value)";
    		    }
    		    
    		    tmpValidatedSettings.setProperty( strParameterName, strParameterValue );
    		}
        }

        if ( tmpValidatedSettings != null ) {
            
    		parameterSet = new TreeSet();
    	    parameterSet.addAll( tmpValidatedSettings.keySet() );
    	    
    	    parameterIterator = parameterSet.iterator();
    		while ( parameterIterator.hasNext() ) {
    
    		    strParameterName = (String) parameterIterator.next();
    		    strParameterName = strParameterName.trim();
    
    		    strParameterValue = 
    		        tmpValidatedSettings.getProperty( strParameterName ).trim();
    		    strParameterValue = StringUtil.removeTrailingComment( strParameterValue );
    
    		    collectedSettings.append( "   " );
    		    collectedSettings.append( strParameterName );
    		    collectedSettings.append( " = " );
    		    collectedSettings.append( strParameterValue );
    		    collectedSettings.append( "\n" );
    		}
        }

		collectedSettings.append( "\n\nPassed-in Parameters:\n" );

        if ( passedInParameters != null ) {
            
    		parameterSet = new TreeSet();
    	    parameterSet.addAll( passedInParameters.keySet() );
    	    
    		parameterIterator = parameterSet.iterator();
    		while (parameterIterator.hasNext()) {
    		    
    		    strParameterName = (String) parameterIterator.next();
    		    strParameterName = strParameterName.trim();
    
    		    strParameterValue = 
    		        passedInParameters.getProperty( strParameterName ).trim();
    		    strParameterValue = StringUtil.removeTrailingComment( strParameterValue );
    			
    		    collectedSettings.append( "   " );
    		    collectedSettings.append( strParameterName );
    		    collectedSettings.append( " = " );
    		    collectedSettings.append( strParameterValue );
    		    collectedSettings.append( "\n" );
    		}
        }
		            
		collectedSettings.append( "\n\n(Current set of) 'Dynamic' Parameters:\n" );

   
        if ( dynamicProcessParameters != null ) {
            
    		parameterSet = new TreeSet();
    	    parameterSet.addAll( dynamicProcessParameters.keySet() );
    	    
    		parameterIterator = parameterSet.iterator();
    		while (parameterIterator.hasNext()) {
    		    
    		    strParameterName = (String) parameterIterator.next();
    		    strParameterName = strParameterName.trim();
    
    		    strParameterValue = 
    		        dynamicProcessParameters.getProperty( strParameterName ).trim();
    		    strParameterValue = StringUtil.removeTrailingComment( strParameterValue );
    			
    		    collectedSettings.append( "   " );
    		    collectedSettings.append( strParameterName );
    		    collectedSettings.append( " = " );
    		    collectedSettings.append( strParameterValue );
    		    collectedSettings.append( "\n" );
    		}
        }

		
		collectedSettings.append( "\n\nInitial Parameters (as loaded):\n" );

        if ( initialProcessParametersAsLoaded != null ) {
            
    		parameterSet = new TreeSet();
    		parameterSet.addAll( initialProcessParametersAsLoaded.keySet() );
    		
    		parameterIterator = parameterSet.iterator();
    		while (parameterIterator.hasNext()) {
    		    
    		    strParameterName = (String) parameterIterator.next();
    		    strParameterName = strParameterName.trim();
    		
    		    strParameterValue = 
    		        initialProcessParametersAsLoaded.getProperty( strParameterName ).trim();
    		    strParameterValue = StringUtil.removeTrailingComment( strParameterValue );
    		
    		    collectedSettings.append( "   " );
    		    collectedSettings.append( strParameterName );
    		    collectedSettings.append( " = " );
    		    collectedSettings.append( strParameterValue );
    		    collectedSettings.append( "\n" );
    		}
        }
		
		return collectedSettings.toString();
	}
    
    public StringBuffer getOptionalThreadInfo() throws Exception {
        
        // Default the threadInfo string to a blank line that is approximately
        // the same length as the composed string below (currently: "[Thread i]"),
        // to keep some derived feedback strings properly composed (e.g., in errors).
        StringBuffer threadInfo = new StringBuffer( "" );
        int maxThreads = 1;
        int threadID = -1;
        
        try {
            maxThreads = Integer.parseInt( 
                    getDynamicProcessParameter( BANJO.DATA_MAXTHREADS ));
        }
        catch ( Exception e ) {
            // swallow any exception
        }
        
        try {
            threadID = Integer.parseInt( 
                    getDynamicProcessParameter( BANJO.DATA_THREADINDEX ));
        }
        catch ( Exception e ) {
            // swallow any exception
        }
        
        // Only supply thread info when there are >1 threads
        if ( maxThreads > 1 ) {
            
            // and: Only when we are within a thread (not the controller)
            if ( threadID > -1 ) {
                
                // Note that for the user's sake we start the thread numbering at 1 
                // (instead of 0)
                threadInfo = new StringBuffer( "[Thread " + ( threadID + 1 ) +
                        "]" );
            }
            else {
                
                threadInfo = new StringBuffer( "[Controller]" );
            }
        }
     
        return threadInfo;
    }

    /**
     * @return Returns the observations.
     */
    public ObservationsI getObservations() {
        
        return observations;
    }

    /**
     * @param _observations The observations to set.
     */
    public void setObservations( ObservationsI _observations ) {
        
        observations = _observations;
    }
    
    // -------------------------------------------
    // Wrapper functions around the fileUtil class
    // ------------------------------------------- 
    
    public void loadObservations() throws Exception {

        observations = fileUtil.loadObservations();
    }
    
    public void writeToFile( final Collection _outputFileFlags, 
            final StringBuffer _stringBufferToWrite ) throws Exception {
                
        fileUtil.writeToFile( _outputFileFlags, _stringBufferToWrite );
    }
    
    public void writeStringToFile( 
            final String _fileName, 
            final String _dataToWrite, 
            final boolean _traceToConsole ) throws Exception {
        
        fileUtil.writeStringToFile( _fileName, _dataToWrite, _traceToConsole );
    }
    
    public void writeTraceToFile( 
            final String _dataToWrite, 
            final boolean _traceToConsole,
            final int _traceFileID ) throws Exception {
    
        fileUtil.writeTraceToFile( _dataToWrite, _traceToConsole, _traceFileID );
    }
    
    public void recordError( final String _strErrorMessage ) throws Exception {
    
        if ( fileUtil == null ) fileUtil = new FileUtil();
        fileUtil.recordError( _strErrorMessage );
    }

    // Note: this is called at the start of the search process, so we can write out 
    // data as it accumulates
    public void prepareFileOutput() throws Exception {
        
        fileUtil = new FileUtil( this );
        fileUtil.prepareResultsFile();
    }

    // Note: this is called at the very end of the search process, to write out
    // the final data in xml format
    public void prepareXMLOutput() throws Exception {
        
        fileUtil.prepareXMLResultsFile();
    }
    
    // Main wrapper function for accessing the random sequence
    // THIS method should be used so that the tesMode setting can work its magic.
    // Note: by having a 1-to-1 correspondence between the settings and the random sequence,
    // we can now run repeatable tests even for multi-threaded scenarios.
    public Random getRandomSequence() {
        
        return banjoRandomNumber.getRandomSequence();
    }
    
    // Wrapper functions for accessing the seed for the random sequence
    public long getBanjoSeed() {
        
        return banjoRandomNumber.getBanjoSeed();
    }
    public void setBanjoSeed( long _randomSeed ) {
        
        banjoRandomNumber.setBanjoSeed( _randomSeed );
    }
}
