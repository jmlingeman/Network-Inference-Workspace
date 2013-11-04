/*
 * Created on Mar 1, 2004
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

/**
 * Defines all constants (numeric, string, etc) used by Banjo.
 *
 * <p><strong>Details:</strong> <br>
 * - By declaring all used constants in one place, any changing of values is greatly
 * simplified. <br>
 * - Contains sections for Developer options (e.g., debugging flags for subsections
 * of the code), Exception handling, User input (settings defined in the settings file),
 * and values used internally by the application (e.g., acceptable values for a 
 * BayesNetChange, default values for certain search parameters, etc).
 *  
 * 
 * <p><strong>Change History:</strong> <br>
 * Created on Mar 1, 2004
 * <p>
 * 10/10/2005 (v2.0) hjs		
 * 		Add constants for computing a consensus graph, and creating a html version of
 * 		the output.
 * <p>
 * 10/13/2005 (v2.0) hjs
 * 		Add constants for specifying the format in displaying decimal
 * 		numbers for network and influence scores
 * 
 * <p>
 * March 2006 (v2.0) hjs
 * 		Remove deprecated constants, in particular error-handling related
 * 		Clean-up, renaming, and re-grouping of constants in several sections
 * 		for better consistency
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public final class BANJO {
	
	// Remember to recompile all classes that reference this class when you
	// change a static final value (especially the DEBUG/TRACE flags when 
	// switching to production mode)
	
    //-------------------------
    // Banjo version identification:
    //-------------------------
	public static final String APPLICATION_NAME = "Banjo";
	public static final String APPLICATION_NAME_LONG = 
        "Bayesian Network Inference with Java Objects";
	public static final String APPLICATION_VERSIONNUMBER = "2.2.0";
	public static final String APPLICATION_VERSIONDATE = "15 Apr 2008";
	public static final String APPLICATION_VERSIONDESCRIPTION = "Public Release 2.2";
	//
    public static final String RELEASE = "Release";
    public static final String BANJOVERSION = "Banjo Version";
	public static final String RELEASE_LICENCED = "Licensed from Duke University";
	public static final String RELEASE_COPYRIGHT = 
	    "Copyright (c) 2005-08 by Alexander J. Hartemink";
	public static final String RELEASE_ALLRIGHTS = "All rights reserved";
    // Used XML version
    public static final String DATA_BANJOXMLTAG_XMLVERSION = 
        "<?xml version=" + "\"1.0\"" + " encoding= " + "\"UTF-8\"" + "?>";
    public static final String DATA_XMLVERSION = 
        "1.0";
    public static final String DATA_XMLVERSION_DISP = 
        "XML version:";
    // Banjo's XML output data versioning:
    public static final String DATA_BANJOXMLFORMATVERSION_1 = "1.0";
    public static final String DATA_BANJOXMLFORMATVERSION = DATA_BANJOXMLFORMATVERSION_1;
    public static final String DATA_BANJOXMLFORMATVERSION_DISP = "Banjo XML format version:";
	
	// ------------------------
	// Cache settings
	// ------------------------
	
	// It is sometimes useful to turn the score caching mechanism off
	// during development. For deployment, always set this flag to true,
	// and set the main DEBUG flag to false, then recompile the project.
	// Note that all the code for the score cache is in the Evaluator class
	// default value: true

	// Setting SETTING_USECACHE provides the user interface:
	public static final String SETTING_USECACHE = "useCache";
	public static final String SETTING_USECACHE_DESCR = "Use cache";
	public static final String SETTING_USECACHE_DISP = "Cache:";
    // Cache settings that the user can select:
	public static final String UI_USECACHE_NONE = "none";
	public static final String UI_USECACHE_BASIC = "basic";
	public static final String UI_USECACHE_FASTLEVEL_0 = "fastLevel0";
	public static final String UI_USECACHE_FASTLEVEL_1 = "fastLevel1";
	public static final String UI_USECACHE_FASTLEVEL_2 = "fastLevel2";
    // default value if user doesn't specify cache use
	public static final String DEFAULT_USECACHE = UI_USECACHE_FASTLEVEL_2;
	// DATA_USEFASTCACHELEVEL is derived from SETTING_USECACHE:
	public static final String DATA_USEFASTCACHELEVEL = "useFastCacheLevel";
	// Internal constant for setting fast cache access level (max. value CANNOT exceed 2)
	// To completely disable, set to -1; default value: 2
	// DO NOT INCREASE UNLESS YOU HAVE ADDED THE CODE TO SUPPORT IT
	public static final int DEFAULT_MAXLEVELFORFASTCACHE = 2;
	// Values for DATA_USEFASTCACHELEVEL
	public static final int DATA_NOFASTCACHE = -1;
	public static final int DATA_FASTCACHELEVEL0 = 0;
	public static final int DATA_FASTCACHELEVEL1 = 1;
    public static final int DATA_FASTCACHELEVEL2 = 2;
    public static final int DATA_DEFAULT_FASTCACHELEVEL = DATA_FASTCACHELEVEL0;

	// DATA_USEBASICCACHE is also derived from SETTING_USECACHE:
	// This constant can turn the "regular" cache (for parent counts > fastCache level)
	//  on and off, with default value DEFAULT_USEBASICCACHE (true)
	public static final String DATA_USEBASICCACHE = "useBasicCache";
	// Values for DATA_USEBASICCACHE:
	public static final String UI_USEBASICCACHE_YES = "yes";
	public static final String UI_USEBASICCACHE_NO = "no";
	public static final String DEFAULT_USEBASICCACHE = UI_USEBASICCACHE_YES;
    
    //-------------------------
    // DEVELOPMENT constants:
    //-------------------------
	
	//-------------------------
	// Part I: 		(Internal Development) Settings
	//-------------------------

    // Display the progress through a search (all dev. options are
    // tied to the global DEBUG flag unless otherwise indicated)
    public static final boolean CONFIG_DISPLAYPROGRESS = false;
    public static final int CONFIG_TRACEFILE_1 = 1;
    public static final int CONFIG_TRACEFILE_2 = 2;
    public static final int CONFIG_TRACEFILE_3 = 3;

	//-------------------------
	// Part II: 	Control application behaviour before folding into 
	// 				general release
	//-------------------------
	// For now, several special output sections are included in each search report.
	// To change the default behaviour, change the values of the constants:
	//
	public static final boolean CONFIG_DISPLAYSTATISTICS = true;
	public static final boolean CONFIG_DISPLAYSTATISTICS_EQUIVCHECK = true;
	//
    // To get the entire output that is written to file to also print
    // on the command line, set this to true:
    //
	// If you absolutely don't care about any command line output,
	// then set this flag to false:
    public static final boolean CONFIG_ECHOOUTPUTTOCMDLINE = true;
    // This should probably always be true (for the user's sake)
    public static final boolean CONFIG_ECHOERROROUTPUTTOCMDLINE = true;
	//
	// For displaying at what iterations the restarts took place:
	public static final boolean CONFIG_DISPLAYITERATIONRESTARTS = false;
 

    // (Optional) final display of elapsed time
    public static final boolean CONFIG_FINALTIMEDISPLAY = false;
    
    // (Optional) show collisions in cache
    public static final boolean CONFIG_DISPLAYCOLLISIONS = false;
    
    // Path separators
    public static final String DATA_SEPARATORFORWARDSLASH = "/";
    public static final String DATA_SEPARATORBACKWARDSLASH = "\\";
    public static final String DATA_SEPARATORCOLON = ":";

    // Choices for the parent set implementations
    // Multi-dimensional arrays (matrices) representation; turned out to waste memory due to 
    // Java's implementation
    public static final String UI_PARENTSETSASMATRICES = "parentSetsAsMatrices";
    // Compact parent set representation, by using the variable as the outermost
    // dimension in mapping from 3-dimensional to 1-dimensional arrays
    public static final String UI_PARENTSETSASARRAYS = "parentSetsAsArrays";
    // This the current choice of parent sets
    public static final String CONFIG_PARENTSETS = UI_PARENTSETSASARRAYS;
    // To experiment with the "original" (Banjo v1.0.x) parent sets, use this:
    // (but check that CONFIG_CYCLECHECKER_ACTIONORDER is set properly, since the associated
    // ~AsMatrix class has not been retrofit to handle the Shmueli-based cycle checking)
//    public static final String CONFIG_PARENTSETS = UI_PARENTSETSASMATRICES;

    // Choices for observations implementations
    public static final String UI_ORIGINALOBSERVATIONS = "originalObservations";
    public static final String UI_COMPACTOBSERVATIONS = "compactObservations";
    // This the current choice of observations
    public static final String CONFIG_OBSERVATIONS = UI_COMPACTOBSERVATIONS;
    // To experiment with the "original" (Banjo v1.0.x) observations, use this:
//    public static final String CONFIG_OBSERVATIONS = UI_ORIGINALOBSERVATIONS;
    
	//-------------------------
	// Part III: 	Debug and Development Feedback flags
	//-------------------------
	
	// Main "Global" debug flag: turn(ed) off for release
	// default in user mode (vs developer mode): false
    public static final boolean DEBUG = false;

	// Individual (secondary) debug flags (always used as a dual
	// condition with DEBUG, so that we only need to turn off DEBUG
	// when we are ready to deploy)
	public static final boolean TRACE_BAYESNETMANAGER = false;
	public static final boolean TRACE_CMDLINE = false;
	public static final boolean TRACE_BAYESNET = false;
	public static final boolean TRACE_EVAL = false;
	public static final boolean TRACE_BDEPRECOMPUTE = false;
	public static final boolean TRACE_EVALPARENTCOUNT = false;
	public static final boolean TRACE_BAYESNETSTRUCTURE = false;
	public static final boolean TRACE_FILEUTIL = false;
	public static final boolean TRACE_STATISTICS = false;
	public static final boolean TRACE_PROPOSER = false;
	public static final boolean TRACE_NODESCORES = false;
	public static final boolean TRACE_DISCRETIZATION = false;
    public static final boolean TRACE_CONSENSUSGRAPH = false;
    public static final boolean TRACE_VALIDATEDPARAMS = false;
    public static final boolean TRACE_DYNAMICPARAMS = false;
    public static final boolean TRACE_EQUIVALENCECHECKER = false;
    public static final boolean TRACE_EQUIVALENCEPRUNING = false;
    public static final boolean TRACE_EQUIVALENCEPRUNING_DISPLAYNETWORKS = false;
	public static final boolean TRACE_POSTPROCESSOR_FILEOUTPUT = false;
    public static final boolean TRACE_TRANSPOSEDDATA = false;
    public static final boolean TRACE_DFSNEW = false;
    public static final boolean TRACE_DFSNEW_RESET = false;
    public static final boolean TRACE_COMBINENBEST = false;
    public static final boolean TRACE_XMLOUTPUT = false;
	// This is more than a debug trace: it displays the loop indexes for all restarts
	// (useful for algorithm monitoring/tuning)
    public static final boolean TRACE_RESTART = false;
    // Monitoring detailed memory use at startup (for sim. annealer only at this time)
    public static final boolean TRACE_MEMORYUSE = false;
    
	//--------------------------
	// Internal constants
	//--------------------------
	
    // String value used when we (internally) ask for a value for a property or setting,
    // but no such value exists (yet)
//    public static final String DATA_STRINGDOESNOTEXIST = "does not exist";
    
	//--------------------------
	// Part I:	Tuning constants
	//--------------------------

	// Max. number of values that a node can assume
    // Note: Increasing this value past 7 will greatly affect the memory use
	public static final int CONFIG_MAXVALUECOUNT = 7;
	// Internal setting (make sure you understand what it does!!), use only values 0 or 1:
	public static final int CONFIG_DISCRETIZATIONSTARTINDEX = 0;
	
	//--------------------------
	// Part II: Values set as conventions
	//--------------------------
	
	// Type values of a BayesNetChange
	public static final int CHANGETYPE_NONE = 0;
	public static final int CHANGETYPE_ADDITION = 1;
	public static final int CHANGETYPE_DELETION = 2;
	public static final int CHANGETYPE_REVERSAL = 3;
	// Some proposers make use of the count value:
	// This count matches the highest value of the changeType values,
	// so we CANNOT skip a (changeType) value in the above list!
	public static final int CHANGETYPE_COUNT = CHANGETYPE_REVERSAL;
	
	// Status values of a BayesNetChange
	public static final int CHANGESTATUS_NONE = 0;
	public static final int CHANGESTATUS_READY = 1;
	public static final int CHANGESTATUS_APPLIED = 2;
	public static final int CHANGESTATUS_UNDONE = 3;
	
	// Values for setting the logging preferences
	public static final int LOGGING_REGULAR = 1;
	public static final int LOGGING_MINIMAL = 2;
	public static final int LOGGING_VERBOSE = 3;
	
	// Values to indicate the logging files
	public static final int FILE_RESULTS = 0;
	public static final int FILE_SUMMARY = 1;
    public static final int FILE_TRACE = 2;
    public static final int FILE_RESULTSXML = 3;

    public static final int BANJO_SYSTEM_ENTER = 13;
    public static final int BANJO_SYSTEM_NL = 10;
    
    public static final String BANJO_FREEFORMINPUT = "freeFormInput";
    //
    public static final String BANJO_NOVALUESUPPLIED_STRING = 
        "noValueSuppliedByUser";
    // Use this as general purpose indicator that user didn't specify
    // a value for a numeric input (of course, only applicable for
    // data that is supposed to be >=0, which most of ours is; for
    // data that can be negative, may need to use the string, and check
    // before converting setting back to number)
    public static final int BANJO_NOVALUESUPPLIED_NUMBER = -1;
    // This is the default value used when the user doesn't input a value
    // (could change to "none" or "not specified", etc.)
    public static final String BANJO_NOVALUE_INDISPLAY = "";
	
	// ------------------
	// Feedback-related formatting constants
	// ------------------
	
	public static final int FEEDBACK_LINELENGTH = 78;
	public static final String FEEDBACK_SEPARATOR_ITEMS = ":";
	public static final String FEEDBACK_SEPARATOR_VERTICAL = "|";
	public static final String FEEDBACK_NEWLINE = "\n";
	public static final String FEEDBACK_DASH = "-";
	public static final String FEEDBACK_SPACE = " ";
    public static final String FEEDBACK_COLON = ":";
    public static final String FEEDBACK_QUOTES = "\"";
    public static final String FEEDBACK_DASHEDLINE = 
        "----------------------------------------------------" + 
        "----------------------------------------------------";
    public static final String FEEDBACK_NEWLINEPLUSDASHEDLINE = 
        "\n----------------------------------------------------" + 
        "----------------------------------------------------";
    public static final String FEEDBACK_SPACESFORTRIMMING = 
        "                                                                            " + 
        "                                                                            ";
    
    public static final String FEEDBACKSTRING_STATUS = "Status:  ";
    public static final String FEEDBACKSTRING_NETWORKS = "Networks";
    public static final String FEEDBACKSTRING_TIME = "Time";
    public static final String FEEDBACKSTRING_RESTARTS = "Restarts";
    public static final String FEEDBACKSTRING_REANNEALS = "Re-anneals";
	public static final int FEEDBACK_PERCENTPADDINGLENGTH = 5;
	public static final int FEEDBACK_TIMEPADDINGLENGTH = 8;

    public static final String FEEDBACKSTRING_FINALCHECK = 
        "(Final Checkpoint, after search completion) A final check revealed the following issues " +
        "that were encountered during Banjo's execution:";
    
    public static final String FEEDBACKSTRING_POSTPROC = 
        "(Final Checkpoint, after post-processing) A final check revealed the following issues " +
        "that were encountered during Banjo's execution:";
		
	public static final int FEEDBACK_OPTION_TIMEFORMAT_DEFAULT = 0;
    public static final int FEEDBACK_OPTION_TIMEFORMAT_IN_D = 1;
    public static final int FEEDBACK_OPTION_TIMEFORMAT_IN_H = 2;
	public static final int FEEDBACK_OPTION_TIMEFORMAT_IN_M = 3;
	public static final int FEEDBACK_OPTION_TIMEFORMAT_IN_S = 4;
	public static final int FEEDBACK_OPTION_TIMEFORMAT_IN_MS = 5;
	public static final int FEEDBACK_OPTION_TIMEFORMAT_MIXED = 6;
	
	// Determines how many decimals are displayed for the elapsed time
	public static final int FEEDBACK_NUMBEROFDECIMALSINTIMEDISPLAY = 2;
	
	// String constants used for specifying time values
	public static final String BANJO_TIMEQUALIFIER_DAYS = "d";
	public static final String BANJO_TIMEQUALIFIER_HOURS = "h";
	public static final String BANJO_TIMEQUALIFIER_MINUTES = "m";
	public static final String BANJO_TIMEQUALIFIER_SECONDS = "s";
	public static final String BANJO_TIMEQUALIFIER_MILLISECS = "ms";

	// String constants used for time display
    public static final String FEEDBACK_DEFAULT_TIME_DAYS = "d";
    public static final String FEEDBACK_DEFAULT_TIME_HOURS = "h";
	public static final String FEEDBACK_DEFAULT_TIME_MINUTES = "m";
	public static final String FEEDBACK_DEFAULT_TIME_SECONDS = "s";
	public static final String FEEDBACK_DEFAULT_TIME_MILLISECS = "ms";

	// Limits where we change the display from milliseconds to seconds
	public static final double FEEDBACK_DEFAULT_CUTOFF_TO_SECONDS = 0.99;
	// .. from seconds to minutes
	public static final double FEEDBACK_DEFAULT_CUTOFF_TO_MINUTES = 0.99;
	// .. minutes to hours
    public static final double FEEDBACK_DEFAULT_CUTOFF_TO_HOURS = 0.99;
    // .. hours to days
    public static final double FEEDBACK_DEFAULT_CUTOFF_TO_DAYS = 0.99;
	
	
	// 10/13/2005 (v2.0) hjs	Use the following "DecimalFormat"-compliant
	// strings to specify the formatting of decimal numbers
    // 9/27/2007 hjs Note: the strings are locale-dependent, so when we use them
    // we need to specify the US locale (see, e.g., StringUtil.formatDecimalDisplay)
//    public static final String FEEDBACK_DISPLAYFORMATFORNETWORKSCORE = "####.00000##################";
    public static final String FEEDBACK_DISPLAYFORMATFORNETWORKSCORE = "####.0000";
    //
	public static final String FEEDBACK_DISPLAYFORMATFORINFLUENCESCORE = "####.0000";
		
	// Assign a value for a missing entry (regular values are from 
	// 0 to CONFIG_MAXVALUECOUNT-1; If user specifies the max. value
	// count, it has to be lower than the default value.
	public static final int MISSINGENTRYVALUE = CONFIG_MAXVALUECOUNT;
		
	// Output files used: summary, results, trace, xml
	public static final int MAXOUTPUTFILES = 4;
	// Constant used in generating hash codes
	public static final int HASHGENERATORFACTOR = 37;
	// Constant for setting initial size of secondary cache
	public static final int INITIALHASHSIZE = 1000000;
	// Tolerance for testing numerical equality of 2 scores
    public static final double NUMERICSCORETOLERANCE = 0.0000000000000005;
	
	// Score value used internally in evaluator (feasible BDe scores
	// are never positive)
	public static final double BANJO_UNREACHABLESCORE_BDE = 1.0;
	public static final double BANJO_INITIALTHRESHOLDSCORE_BDE = 0;
	
	// Initial starting numbers used for finding max and mins
	public static final double BANJO_LARGEINITIALVALUEFORMINIMUM = 100000000.0;
	public static final double BANJO_SMALLINITIALVALUEFORMAXIMUM = -100000000.0;
		
	// Limit on number of tries to get a valid bayesNetChange
	public static final int LIMITFORTRIES = 1000;
	public static final int LIMITFORGLOBALGREEDYSEARCHTRIES = 1;

	// Initial initialSettings for internal string buffers
	public static final int BUFFERLENGTH_STAT = 10000;
	public static final int BUFFERLENGTH_STAT_INTERNAL = 2000;
	public static final int BUFFERLENGTH_STRUCTURE = 1000;
	public static final int BUFFERLENGTH_STRUCTURE_LARGE = 30000;
	public static final int BUFFERLENGTH_SMALL = 100;
		
	// -------------------
	// Test-related
	// -------------------
	
	// Testing-related data
    public static final double TEST_SCORETOLERANCE = 0.0000005;
	
	
	// -----------------------
	// Data that is being updated during search (setup or execution),
	// or during postprocessing
	//-----------------------
	
	// References to data that changes during the search
	public static final String DATA_ELAPSEDTIME = "elapsedTime";
	// Data that is being collected during the search
	public static final String DATA_TOTALTIMEFORPREP = "totalTimeForPrep";
	public static final String DATA_TOTALTIMEFORSEARCH = "totalTimeForSearch";
	public static final String DATA_DISCRETIZATIONREPORT = "discretizationReport";
    
	// Data that is updated in the searcher via the updateProcessData method
	public static final String DATA_NETWORKSVISITEDGLOBALCOUNTER = 
	    "networksVisitedGlobalCounter";
	public static final String DATA_GLOBALHIGHSCORE = "globalHighScore";
	public static final String DATA_GLOBALHIGHSCORESTRUCTURE = 
	    "globalHighScoreStructure";

    
    // -------------
    // Customization
    // -------------
    
    // Default time stamp for general use
    public static final String DEFAULT_TIMESTAMP = "yyyy.MM.dd.HH.mm.ss";

    // String appended as time stamp for various output files
    public static final String SETTING_TIMESTAMPSTRINGFORFILES = "timeStampFormat";
    public static final String SETTING_TIMESTAMPSTRINGFORFILES_DESCR = "Time stamp format";
    public static final String SETTING_TIMESTAMPSTRINGFORFILES_DISP = "Time stamp format:";
    public static final String DEFAULT_TIMESTAMPSTRINGFORFILES = "yyyy.MM.dd.HH.mm.ss";
    // alternative time stamps (with examples):
    // ----------------------------------------
    // appends nothing:
//  public static final String DEFAULT_TIMESTAMPSTRINGFORFILES = "";
    //
    // appends, e.g., "___23Jan2006":
//  public static final String DEFAULT_TIMESTAMPSTRINGFORFILES = "___ddMMMyyyy";

    // tokens accepted for time stamp
    public static final String DATA_TIMESTAMP_TOKEN = "@timestamp@";
    public static final String DATA_TIMESTAMP_TOKEN_ALT0 = "@time stamp@";
    public static final String DATA_TIMESTAMP_TOKEN_ALT1 = "@time_stamp@";
    public static final String DATA_TIMESTAMP_TOKEN_ALT2 = "@ts@";
    public static final String DATA_TIMESTAMP_TOKEN_ALT3 = "@TS@";
    
    // (v2.1) other tokens
    // Note that the generic pre-processing if the tokens will be case-insensitive,
    // and will replace all "related" tokens by the default token
    public static final String DATA_THREADID_TOKEN = "@threadID@";
    public static final String DATA_THREADID_TOKEN_ALT0 = "@thID@";
    
    public static final String DATA_VARCOUNT_TOKEN = "@varCount@";
    public static final String DATA_VARCOUNT_TOKEN_ALT0 = "@varC@";
    
    public static final String DATA_MINMARKOVLAG_TOKEN = "@minMarkovLag@";
    public static final String DATA_MINMARKOVLAG_TOKEN_ALT0 = "@minML@";
    
    public static final String DATA_MAXMARKOVLAG_TOKEN = "@maxMarkovLag@";
    public static final String DATA_MAXMARKOVLAG_TOKEN_ALT0 = "@maxML@";
    
	
	 // -----------------------
	 // Settings (user input)
	 // -----------------------
	
    //
    public static final String SETTING_SCREENREPORTINGINTERVAL =
        "screenReportingInterval";
    public static final String SETTING_SCREENREPORTINGINTERVAL_DESCR =
        "Screen reporting interval";
    public static final String SETTING_SCREENREPORTINGINTERVAL_DISP =
        "Screen reporting interval:";
    // Enter a number in seconds (we chose a small number so new users would
    // not have to wait too long for feedback)
    public static final int DEFAULT_SCREENREPORTINGINTERVAL = 10;
    //
    public static final String SETTING_FILEREPORTINGINTERVAL =
        "fileReportingInterval";
    public static final String SETTING_FILEREPORTINGINTERVAL_DESCR =
        "File reporting interval";
    public static final String SETTING_FILEREPORTINGINTERVAL_DISP =
        "File reporting interval:";
    // Enter a number in seconds (we chose a small number so new users would
    // not have to wait too long for feedback)
    public static final int DEFAULT_FILEREPORTINGINTERVAL = 60;
	
	// ------------------------------------------------------
	// Settings that can only be entered on the command line
	// ------------------------------------------------------
	
	// The main settings file, and possibly a directory where it is located (of
	// course, one can also include the path in the file specification) can only
	// be entered as a commandline argument (how else could it be found?). If it 
    // isn't specified, then the default value (DEFAULT_SETTINGSFILENAME) will be used. 
	// The settings file contains the various initialSettings for running a search,
	// such as selecting between different methods, input and output file names, etc.
	public static final String SETTING_CMDARG_SETTINGSFILENAME = "settingsFile";
	public static final String SETTING_CMDARG_SETTINGSFILENAME_DESCR = "Settings file";
	public static final String SETTING_CMDARG_SETTINGSFILENAME_DISP = "Settings file:";
	public static final String DEFAULT_SETTINGSFILENAME = "banjo.txt";
	//
	public static final String SETTING_CMDARG_SETTINGSDIRECTORY = "settingsDirectory";
	public static final String SETTING_CMDARG_SETTINGSDIRECTORY_DESCR = "Settings directory";
	// Stores the name and the absolute path to the input file (after it has been
	// located and validated, hence the name)
//	public static final String DATA_VALIDATEDSETTINGSFILEWITHPATH = 
//	    "dataSettingsFileWithPath"; 
		
	// -- This can be specified in the settings file
	// Location of Banjo default settings (not yet implemented)
	public static final String DATA_SETTINGSDIRECTORYFORDEFAULTVALUES = "data";
	public static final String DATA_SETTINGSFILEFORDEFAULTVALUES = "banjo.cfg";
	
	// Default for interactive feedback within (selected) searchers
	public static final String SETTING_ASKTOVERIFYSETTINGS = 
	    "askToVerifySettings";
	public static final String SETTING_ASKTOVERIFYSETTINGS_DESCR = 
	    "Ask to verify settings";
	public static final String SETTING_ASKTOVERIFYSETTINGS_DISP = 
	    "Ask to verify settings:";
	public static final String UI_ASKTOVERIFYSETTINGS_YES = "yes";
	public static final String UI_ASKTOVERIFYSETTINGS_NO = "no";
	public static final String DEFAULT_ASKTOVERIFYSETTINGS = UI_ASKTOVERIFYSETTINGS_NO;
	
    // 
    public static final String DATA_SPECIFIEDSETTINGSFILE = 
        "specifiedSettingsFile";
    public static final String DATA_SPECIFIEDSETTINGSFILEDIRECTORY = 
        "specifiedSettingsFileDirectory";
    
    
	//--------------------------------
	// Strings - Names for user input parameters
	//
	// NOTE: these strings define the Banjo application's interface to the
	// outside world via the settings file. So any change in the values of
	// the strings will invalidate previous versions of the settings file, and
	// should thus be approached with great caution!!
	//--------------------------------
	
	// Searchers
	public static final String SETTING_SEARCHERCHOICE = "searcherChoice";
	public static final String SETTING_SEARCHERCHOICE_DESCR = "Searcher choice";
	public static final String SETTING_SEARCHERCHOICE_DISP = "Searcher:";
	public static final String UI_DEFAULT = "default"; // depends on implementation
	public static final String UI_SEARCHER_GREEDY = "Greedy";
	public static final String UI_SEARCHER_SIMANNEAL = "SimAnneal";
	// (v2.0) This value lets the user run Banjo without conducting a search,
	// e.g., to compute influence scores on given network:
	public static final String UI_SEARCHER_SKIP = "Skip";	
    //
    public static final boolean DATA_SEARCHER_OMITTIMEDISPLAY = true;

	// Proposers
	public static final String SETTING_PROPOSERCHOICE = "proposerChoice";
	public static final String SETTING_PROPOSERCHOICE_DESCR = "Proposer choice";
	public static final String SETTING_PROPOSERCHOICE_DISP = "Proposer:";
	public static final String UI_PROP_RANDOMLOCALMOVE = "RandomLocalMove";
	public static final String UI_PROP_ALLLOCALMOVES = "AllLocalMoves";
    
    // Internal option for experimenting
    public static final boolean CONFIG_OMITREVERSALS = false;

	// Evaluators
	public static final String SETTING_EVALUATORCHOICE = "evaluatorChoice";
	public static final String SETTING_EVALUATORCHOICE_DESCR = "Evaluator choice";
	public static final String SETTING_EVALUATORCHOICE_DISP = "Evaluator:";
    public static final String UI_EVAL_BDE = "BDe";
	
	// Deciders
	public static final String SETTING_DECIDERCHOICE = "deciderChoice";
	public static final String SETTING_DECIDERCHOICE_DESCR = "Decider choice";
	public static final String SETTING_DECIDERCHOICE_DISP = "Decider:";
	public static final String UI_DEC_GREEDY = "Greedy";
	public static final String UI_DEC_METROPOLIS = "Metropolis";

	// Cycle-checkers
    
    // This is somewhat a piece of legacy code at the 2.0 release: SETTING_CYCLECHECKERCHOICE
    // and the UI values are used to read and process the supplied choice, after which the
    // dynamic parameter CONFIG_CYCLECHECKER_METHOD is used to store the choice.
    // (Will stremaline and update in a maintenance release soon)
	public static final String SETTING_CYCLECHECKERCHOICE = "cycleCheckingMethod";
	public static final String SETTING_CYCLECHECKERCHOICE_DESCR = "Cycle checker choice";
	public static final String SETTING_CYCLECHECKERCHOICE_DISP = "Cycle checker:";
    
	public static final String UI_CYCLECHECKER_DFS = "DFS";
    public static final String UI_CYCLECHECKER_DFSWITHSHMUELI = "DFSwithShmueli";
    public static final String UI_CYCLECHECKER_DFSORIG = "DFSorig";
    public static final String UI_CYCLECHECKER_BFS = "BFS";

    public static final String CONFIG_CYCLECHECKER_METHOD = "cycleCheckingMethod";
    public static final String CONFIG_CYCLECHECKINGMETHOD_DESCR = "cycle checking method";
    public static final String CONFIG_CYCLECHECKINGMETHOD_DISP = "Cycle checking method:";

    public static final String DATA_CYCLECHECKING_DFSORIG = "DFSORIG";
    public static final String DATA_CYCLECHECKING_DFSORIG_DESCR = "depth-first search (from version 1.0)";
    public static final String DATA_CYCLECHECKING_DFSORIG_DISP = "Depth-first Search (from version 1.0)";
    public static final String DATA_CYCLECHECKING_DFS = "DFS";
    public static final String DATA_CYCLECHECKING_DFS_DESCR = "depth-first search";
    public static final String DATA_CYCLECHECKING_DFS_DISP = "Depth-first Search";
    public static final String DATA_CYCLECHECKING_SHMUELI = "DFSwithShmueli";
    public static final String DATA_CYCLECHECKING_SHMUELI_DESCR = 
        "depth-first search with Shmueli optimization";
    public static final String DATA_CYCLECHECKING_SHMUELI_DISP = 
        "Depth-first Search with Shmueli Optimization";
    public static final String DATA_CYCLECHECKING_BFS = "BFS";
    public static final String DATA_CYCLECHECKING_BFS_DESCR = "breadth-first search";
    public static final String DATA_CYCLECHECKING_BFS_DISP = "Breadth-first search";
    
    public static final String CONFIG_CYCLECHECKER_ACTIONORDER = "cycleCheckerOrderOfAction";
    public static final String DATA_CYCLECHECKER_CHECKTHENAPPLY = "checkThenApply";
    public static final String DATA_CYCLECHECKER_APPLYTHENCHECK = "applyThenCheck";
    
    // These values are used "concurrently", so there is no config constant
    public static final String DATA_CYCLECHECKING_CHECKFORCYCLES = "checkForCycles";
    public static final String DATA_CYCLECHECKING_TREATASPLAINNODES = "treatAsPlainNodes";
    public static final String DATA_CYCLECHECKING_NOCHECKING = "noCycleChecking";
    
    
	// Statistics recording
	public static final String SETTING_STATISTICSCHOICE = "statisticsChoice";
	public static final String SETTING_STATISTICSCHOICE_DESCR = "Statistics choice";
	public static final String SETTING_STATISTICSCHOICE_DISP = "Statistics:";
	public static final String UI_RECORDER_STANDARD = "Standard";
	// Planned (but not yet implemented) choices
	public static final String UI_RECORDER_COMPACT = "Compact";
	public static final String UI_RECORDER_VERBOSE = "Verbose";
	public static final String SETTING_STATISTICDEFAULT = UI_RECORDER_STANDARD;

	// Displayed string when a core object's default value is used
	public static final String UI_DEFAULTEDTO_DISP = " defaulted to ";
	

	public static final String SETTING_INPUTDIRECTORY = "inputDirectory";
	public static final String SETTING_INPUTDIRECTORY_DESCR = "Input directory";
	public static final String SETTING_INPUTDIRECTORY_DISP = "Input directory:";
	public static final String DEFAULT_INPUTDIRECTORY = "input";	

	public static final String SETTING_ERRORDIRECTORY = "errorDirectory";
	public static final String SETTING_ERRORDIRECTORY_DESCR = "Error directory";
	public static final String SETTING_ERRORFILE = "errorFile";
	public static final String DEFAULT_ERRORFILE = "error.txt";

    public static final String SETTING_OUTPUTDIRECTORY = "outputDirectory";
    public static final String SETTING_OUTPUTDIRECTORY_DESCR = "Output directory";
    public static final String SETTING_OUTPUTDIRECTORY_DISP = "Output directory:";
    public static final String DEFAULT_OUTPUTDIRECTORY = "output";

    public static final String SETTING_REPORTFILE = "reportFile";
    public static final String SETTING_REPORTFILE_DESCR = "Report file";
    public static final String SETTING_REPORTFILE_DISP = "Report file:";
    public static final String DEFAULT_REPORTFILE = "results.txt";
    public static final String DATA_REPORTFILE = "reportFile";

	public static final String SETTING_TRACKINGFILE = "trackingFile";
	public static final String SETTING_TRACKINGFILE_DESCR = "Tracking file";
	public static final String DEFAULT_TRACKINGFILE = "trace.txt";

	public static final String SETTING_SUMMARYFILE = "summaryFile";
	public static final String SETTING_SUMMARYFILE_DESCR = "Summary file";
	public static final String DEFAULT_SUMMARYFILE = "summary.txt";

    public static final String SETTING_XMLINPUTFILES = "xmlInputFiles";
    public static final String SETTING_XMLINPUTFILES_DESCR = "XML input files";
    public static final String SETTING_XMLINPUTFILES_DISP = "XML input files:";
    public static final String DATA_XMLINPUTFILES = "dataXmlInputFiles";

    public static final String SETTING_XMLINPUTDIRECTORY = "xmlInputDirectory";
    public static final String SETTING_XMLINPUTDIRECTORY_DESCR = "XML input directory";
    public static final String SETTING_XMLINPUTDIRECTORY_DISP = "XML input directory:";

    public static final String SETTING_XMLOUTPUTDIRECTORY = "xmlOutputDirectory";
    public static final String SETTING_XMLOUTPUTDIRECTORY_DESCR = "XML output directory";
    public static final String SETTING_XMLOUTPUTDIRECTORY_DISP = "XML output directory:";
    public static final String DEFAULT_XMLOUTPUTDIRECTORY = DEFAULT_OUTPUTDIRECTORY;

    public static final String SETTING_XMLREPORTFILE = "xmlReportFile";
    public static final String SETTING_XMLREPORTFILE_DESCR = "XML Report file";
    public static final String SETTING_XMLREPORTFILE_DISP = "XML Report file:";
    public static final String DEFAULT_XMLREPORTFILE = "results.xml";
    public static final String DATA_XMLREPORTFILE = "xmlReportFile";

    public static final String SETTING_XMLSETTINGSTOEXPORT = "xmlSettingsToExport";
    public static final String SETTING_XMLSETTINGSTOEXPORT_DESCR = "XML settings to export";
    public static final String SETTING_XMLSETTINGSTOEXPORT_DISP = "XML settings to export:";
    public static final String DATA_XMLSETTINGSTOEXPORT = "xmlSettingsToExport";
    public static final String UI_XMLSETTINGSTOEXPORT_ALL = "all";
    public static final String DATA_XMLPREFIXSPACER = "\t";
    

    public static final String DATA_WILDCARDXMLFILES = 
        "wildcardXMLFiles";
    public static final String DATA_WILDCARDXMLFILES_DESCR = 
        "Wildcard XML files";

    public static final String DATA_OMITTEDWILDCARDXMLFILES = 
        "omittedWildcardXMLFiles";
    public static final String DATA_OMITTEDWILDCARDXMLFILES_DESCR = 
        "Omitted wildcard XML files";
	

	public static final String SETTING_INITIALSTRUCTUREFILE = 
	    "initialStructureFile";
	public static final String SETTING_INITIALSTRUCTUREFILE_DESCR = 
	    "Initial structure file";
	public static final String SETTING_INITIALSTRUCTUREFILE_DISP = 
	    "Initial structure file:";
	public static final String DATA_INITIALSTRUCTURE = "initialStructure";
	public static final String DATA_INITIALSTRUCTURE_DISP = 
	    "Initial structure, as loaded:";
	
	public static final String SETTING_MUSTBEPRESENTEDGESFILE = 
	    "mustBePresentEdgesFile";
	public static final String SETTING_MUSTBEPRESENTEDGESFILE_DESCR = 
	    "'Must be present edges' file";
	public static final String SETTING_MUSTBEPRESENTEDGESFILE_DISP = 
	    "'Must be present' edges file:";
	public static final String DATA_MUSTBEPRESENTPARENTS = "mustBePresentParents";
	public static final String DATA_MUSTBEPRESENTPARENTS_DISP = 
	    "'Must be present' edges, as loaded:";
		
	public static final String SETTING_MUSTNOTBEPRESENTEDGESFILE = 
	    "mustNotBePresentEdgesFile";
	public static final String SETTING_MUSTNOTBEPRESENTEDGESFILE_DESCR = 
	    "'Must not be present edges' file";	
	public static final String SETTING_MUSTNOTBEPRESENTEDGESFILE_DISP = 
	    "'Must not be present' edges file:";	
	public static final String DATA_MUSTBEABSENTPARENTS = "mustBeAbsentParents";
	public static final String DATA_MUSTBEABSENTPARENTS_DISP = 
	    "'Must not be present' edges, as loaded:";

    public static final String SETTING_DISPLAYSTRUCTURES = "displayStructures";
    public static final String SETTING_DISPLAYSTRUCTURES_DESCR = "Display structures";
    public static final String SETTING_DISPLAYSTRUCTURES_DISP = "Display structures:";
    public static final String UI_DISPLAYSTRUCTURES_YES = "yes";
    public static final String UI_DISPLAYSTRUCTURES_NO = "no";
    public static final String DEFAULT_DISPLAYSTRUCTURES = UI_DISPLAYSTRUCTURES_NO;
    
    public static final String SETTING_DISPLAYMEMORYINFO = "displayMemoryInfo";
    public static final String SETTING_DISPLAYMEMORYINFO_DESCR = "Display memory info";
    public static final String SETTING_DISPLAYMEMORYINFO_DISP = "Display memory info:";
    public static final String UI_DISPLAYMEMORYINFO_YES = "yes";
    public static final String UI_DISPLAYMEMORYINFO_NO = "no";
    public static final String DEFAULT_DISPLAYMEMORYINFO = UI_DISPLAYMEMORYINFO_NO;
    public static final String DATA_MEMORYINFO = "Memory info:";
    public static final String DATA_MEMORYINFOBEFORESTART = "Memory info before starting the search:";
    public static final String DATA_MEMORYINFOATFINISH = "Memory info after completing the search:";
    public static final String DATA_FINALREPORT = "Final report";
    public static final String DATA_SEARCHSTATISTICS = "Search Statistics";
    public static final String DATA_SEARCHDATA = "Banjo search data:";

    // Tracking detailed memory use
    public static final String DATA_TRACEMEMORYSEARCHERSTART = "Memory info at Searcher start:";
    public static final String DATA_TRACEMEMORYSEARCHEREND = "Memory info at Searcher end:";
    public static final String DATA_TRACEMEMORYANNEALERSTART = "Memory info at Annealer start:";
    public static final String DATA_TRACEMEMORYANNEALEREND = "Memory info at Annealer end:";
    public static final String DATA_TRACEMEMORYBNMSTART = "Memory info at BNM start:";
    public static final String DATA_TRACEMEMORYBNMBEFORENODECACHE = "Memory info before node cache:";
    public static final String DATA_TRACEMEMORYBNMBEFOREINITIALSTRUCT = "Memory info before initial structure:";
    public static final String DATA_TRACEMEMORYBNMEND = "Memory info at BNM end:";
    
    public static final String DATA_TRACEMEMORYLOADINGOBSERVATIONS = 
        "      Memory info before loading observations:";
    public static final String DATA_TRACEMEMORYLOADINGOBSERVATIONSINPROGRESS = 
        "      Memory info during loading of observations:";
    public static final String DATA_TRACEMEMORYLOADINGOBSERVATIONSCOMPLETED = 
        "      Memory info after loading observations:";
    public static final String DATA_TRACEMEMORYOBSERVATIONSTAGING = 
        "      Memory info before releasing staging objects:";
    public static final String DATA_TRACEMEMORYOBSERVATIONSTAGINGRELEASED = 
        "      Memory info after releasing staging objects:";
    
	
	public static final String SETTING_DISPLAYDEBUGINFO = 
	    "displayDebugInfo";
	public static final String SETTING_DISPLAYDEBUGINFO_DESCR = 
	    "Display debug info";
	public static final String SETTING_DISPLAYDEBUGINFO_DISP = 
	    "Display debug info:";
	public static final String UI_DEBUGINFO_STACKTRACE = 
	    "stackTrace";
	public static final String UI_DEBUGINFO_NONE = 
	    "none";
	public static final String SETTING_DEFAULT_DEBUGINFO = 
	    UI_DEBUGINFO_NONE;
	
	

    public static final String SETTING_THREADS = "threads";
    public static final String SETTING_THREADS_DESCR = "Number of threads";
    public static final String SETTING_THREADS_DISP = "Number of threads:";
    public static final String DATA_THREADNAME = "Banjo thread #";
    //
    public static final String DATA_MAXTHREADS = "maxThreads";
    public static final String DATA_THREADINDEX = "threadIndex";
    public static final String DATA_THREADINDEX_DISP = "Index of current thread:";
    public static final String DATA_THREAD_FILEIDENTIFIER = "thread=";
    public static final int SETTING_DEFAULT_THREADS = 1;
    //
    public static final String SETTING_FILENAMEPREFIXFORTHREADS = "fileNamePrefixForThreads";
    public static final String SETTING_FILENAMEPREFIXFORTHREADS_DESCR = 
        "file name prefix for identifying threads";
    public static final String SETTING_FILENAMEPREFIXFORTHREADS_DISP = 
        "File name prefix for identifying threads:";
    public static final String SETTING_FILENAMEPREFIXFORTHREADS_DEFAULT = 
        "thread=@threadid@_";
    
	//----------------------------------------
	// Validation  and related error constants
	//----------------------------------------

	public static final String DATA_SETTINGNOTFOUND = "setting not found";
	public static final String DATA_SETTINGNOVALUEPROVIDED = "no value provided";
	public static final String DATA_SETTINGINVALIDVALUE = "invalid setting value";

	// This string is used in the error message when no searcher is specified by the user
	public static final String DATA_SETTINGNODEFAULTVALUE = "no value found";
	public static final String DATA_SETTINGDEFAULTVALUE_EMPTY = "";
	
	// Valid data types for settings validation
	public static final String VALIDATION_DATATYPE_STRING = "String";
	public static final String VALIDATION_DATATYPE_INTEGER = "Integer";
	public static final String VALIDATION_DATATYPE_INTEGERLIST = "IntegerList";
	public static final String VALIDATION_DATATYPE_LONG = "Long";
	public static final String VALIDATION_DATATYPE_DOUBLE = "Double";
	public static final String VALIDATION_DATATYPE_TIME = "Time";
	public static final String VALIDATION_DATATYPE_TIMESTAMP = "Timestamp";
	public static final String VALIDATION_DATATYPE_DISCRETIZATIONPOLICY = 
	    "discretizationPolicy";
	public static final String VALIDATION_DATATYPE_DISCRETIZATIONEXCEPTIONS = 
	    "discretizationExceptions";
    // -- Not yet supported data types:
    public static final String VALIDATION_DATATYPE_FILE = "File";
    public static final String VALIDATION_DATATYPE_DIRECTORY = "Directory";
    public static final String VALIDATION_DATATYPE_FILEPATH = "Path";
    // --
	
	// Basic types of validation
	public static final int VALIDATIONTYPE_MANDATORY = 1;
	public static final int VALIDATIONTYPE_OPTIONAL = 2;
	public static final int VALIDATIONTYPE_BASICRANGE = 3;
	public static final int VALIDATIONTYPE_RULE = 4;
	
	// Error types for validation, and their descriptive strings
	public static final int ERRORTYPE_MISSINGVALUE = 101;
	public static final String ERRORDESCRIPTION_MISSINGVALUE =
	    "Missing value of required setting";

	public static final int ERRORTYPE_MISSINGMANDATORYVALUE = 102;
	public static final String ERRORDESCRIPTION_MISSINGMANDATORYVALUE = 
	    "Missing mandatory value";

	public static final int ERRORTYPE_MISMATCHEDDATATYPE = 103;
	public static final String ERRORDESCRIPTION_MISMATCHEDDATATYPE = 
	    "Data is of unexpected type";
	
	public static final int ERRORTYPE_INVALIDRANGE = 104;
	public static final String ERRORDESCRIPTION_INVALIDRANGE = 
	    "Value out of accepted range";
	
	public static final int ERRORTYPE_RULEVIOLATION = 105;
	public static final String ERRORDESCRIPTION_RULEVIOLATION = 
	    "Rule violation";
    
    public static final int ERRORTYPE_INVALIDCHOICE = 106;
    public static final String ERRORDESCRIPTION_INVALIDCHOICE = 
        "Invalid setting choice";
    
    public static final int ERRORTYPE_INVALIDPATH = 107;
    public static final String ERRORDESCRIPTION_INVALIDPATH = 
        "Invalid file or directory path";
	
	public static final int ERRORTYPE_OTHER = 110;
	public static final String ERRORDESCRIPTION_OTHER = 
	    "Other error";
	
	public static final int ERRORTYPE_DOTINTERRUPTION = 120;
	public static final String ERRORDESCRIPTION_DOTINTERRUPTION = 
	    "Interruption of 'dot'";
    
    public static final int ERRORTYPE_DOTEXECUTION = 121;
    public static final String ERRORDESCRIPTION_DOTEXECUTION = 
        "'dot' execution";
    
    public static final int ERRORTYPE_POSTPROCESSING = 130;
    public static final String ERRORDESCRIPTION_POSTPROCESSING = 
        "Post-processing";
    
    public static final int ERRORTYPE_ALERT_OTHER = 200;
    public static final String ERRORDESCRIPTION_ALERT_OTHER =
        "Alert: (general)";
    
    public static final int ERRORTYPE_ALERT_CORRECTEDCHOICE = 201;
    public static final String ERRORDESCRIPTION_ALERT_CORRECTEDCHOICE =
        "Alert: invalid setting choice was corrected.";
    
    public static final int ERRORTYPE_ALERT_MISSINGDOTLOCATION = 202;
    public static final String ERRORDESCRIPTION_ALERT_MISSINGDOTLOCATION =
        "Alert: the path to dot is not specified";
    public static final String ERRORDESCRIPTION_ALERT_MISSINGDOTLOCATION_INFO =
        "To automatically create dot output, the path where dot is located " +
        "on your system needs to be specified in the setting '" +
        BANJO.SETTING_FULLPATHTODOTEXECUTABLE + "'.";
    
    public static final int ERRORTYPE_ALERT_DEPRECATEDSETTING = 203;
    public static final String ERRORDESCRIPTION_ALERT_DEPRECATEDSETTING =
        "Alert: deprecated setting.";
    
    public static final int ERRORTYPE_ALERT_UNKNOWNSETTING = 204;
    public static final String ERRORDESCRIPTION_ALERT_UNKNOWNSETTING =
        "Alert: unknown setting.";
    
    public static final int ERRORTYPE_ALERT_DEFAULTAPPLIED = 205;
    public static final String ERRORDESCRIPTION_ALERT_DEFAULTAPPLIED =
        "Alert: default applied.";
	
	public static final int ERRORTYPE_WARNING_INVALIDCHOICE = 306;
	public static final String ERRORDESCRIPTION_WARNING_INVALIDCHOICE =
	    "Warning: Invalid setting choice";
	
	public static final int ERRORTYPE_WARNING_OTHER = 307;
	public static final String ERRORDESCRIPTION_WARNING_OTHER = 
	    "Warning (dev)";
    
    public static final int ERRORTYPE_ALERT_DEV = 308;
    public static final String ERRORDESCRIPTION_ALERT_DEV =
        "Alert: (dev)";
	
	// ---------------
	// General input
	// ---------------
	
	// 
	public static final String SETTING_VARCOUNT = "variableCount";
	public static final String SETTING_VARCOUNT_DESCR = "Variable count";
	public static final String SETTING_VARCOUNT_DISP = "Number of variables:";
	// Mandatory. Validated as int > 0.    
    
    public static final String SETTING_VARIABLESAREINROWS = "variablesAreInRows";
    public static final String SETTING_VARIABLESAREINROWS_DESCR = "Variables are in rows";
    public static final String SETTING_VARIABLESAREINROWS_DISP = "Variables are in rows:";
    public static final String UI_VARIABLESAREINROWS_YES = "yes";
    public static final String UI_VARIABLESAREINROWS_NO = "no";
    // The default has each row corresponding to a single observation, i.e., the
    // variables are in columns
    public static final String DEFAULT_VARIABLESAREINROWS = UI_VARIABLESAREINROWS_NO;

    // Setting for attaching labels to the variables
    // 
    // Origianlly, this setting as supplied by the user may be an instruction, namely,
    // "inFile" or "none", or it may be the actual list of variable names, so we need to
    // treat it carefully
    public static final String SETTING_VARIABLENAMES = "variableNames";
    public static final String SETTING_VARIABLENAMES_DESCR = "Variable names";
    public static final String SETTING_VARIABLENAMES_DISP = "Variable names:";
    public static final String UI_VARIABLENAMES_INFILE = "inFile";
    public static final String UI_VARIABLENAMES_NONE = "none";
    public static final String DEFAULT_VARIABLENAMES = UI_VARIABLENAMES_NONE;
    // Once the setting is processed, we use two internal constants to hold the user's
    // choice and the actual variable names
    public static final String DATA_VARIABLENAMESCHOICE = "variableNamesChoice";
    public static final String UI_VARIABLENAMESCHOICE_NONE = "NONE";
    public static final String UI_VARIABLENAMESCHOICE_INFILE = "INFILE";
    public static final String UI_VARIABLENAMESCHOICE_COMMAS = "COMMAS";
    public static final String DEFAULT_VARIABLENAMESCHOICE = UI_VARIABLENAMESCHOICE_NONE;
    public static final String DATA_VARIABLENAMES = "variableNamesAsAssigned";    
    
	public static final String SETTING_MINMARKOVLAG = "minMarkovLag";
	public static final String SETTING_MINMARKOVLAG_DESCR = "Min. Markov lag";
	public static final String SETTING_MINMARKOVLAG_DISP = "Min. Markov lag:";
	// Mandatory. Validated as int >= 0, and <= maxMarkovLag.

	public static final String SETTING_MAXMARKOVLAG = "maxMarkovLag";
	public static final String SETTING_MAXMARKOVLAG_DESCR = "Max. Markov lag";
	public static final String SETTING_MAXMARKOVLAG_DISP = "Max. Markov lag:";
	// Mandatory. Validated as int >= 0, and >= minMarkovLag.
	
    // Special input for convenient DBN input
    public static final String SETTING_DBNMANDATORYIDENTITYLAGS = 
        "dbnMandatoryIdentityLags";
    public static final String SETTING_DBNMANDATORYIDENTITYLAGS_DESCR = 
        "Dbn mandatory identity lags";
    public static final String SETTING_DBNMANDATORYIDENTITYLAGS_DISP = 
        "DBN mandatory identity lag(s):";
    public static final String SETTING_DBNMANDATORYIDENTITYLAGS_NONE = 
        "none";

    
	// ------------------------	
	// BDe metric related input
	// ------------------------
	
	// Equivalent Sample Size (frequently "alpha" in papers)
	public static final String SETTING_EQUIVALENTSAMPLESIZE = 
	    "equivalentSampleSize";
	public static final String SETTING_EQUIVALENTSAMPLESIZE_DESCR = 
	    "Equivalent sample size";
	public static final String SETTING_EQUIVALENTSAMPLESIZE_DISP = 
	    "Equivalent sample size for Dirichlet parameter prior:";
	// Validated as double > 0
	public static final int BANJO_EQUIVALENTSAMPLESIZE_INVALIDVALUE = -1;
	public static final int DEFAULT_EQUIVALENTSAMPLESIZE = 
	    BANJO_EQUIVALENTSAMPLESIZE_INVALIDVALUE;

	// ObservationsAsMatrix
	public static final String SETTING_OBSERVATIONSFILE = "observationsFile";
	public static final String SETTING_OBSERVATIONSFILE_DESCR = "Observations file";
	public static final String SETTING_OBSERVATIONSFILE_DISP = "Observations file:";
	public static final String SETTING_OBSERVATIONSFILES_DISP = "Observations files:";
	public static final String SETTING_OBSERVATIONSFILE_WILDCARD_DISP = 
	    "Observations files (wildcard spec.):";
	public static final String SETTING_OBSERVATIONSFILES_USED_DISP = "  Files used:";
	public static final String SETTING_OBSERVATIONSFILES_OMIT_DISP = "  Files omitted:";

	public static final String DATA_WILDCARDOBSERVATIONSFILES = 
	    "wildcardObservationsFiles";
	public static final String DATA_WILDCARDOBSERVATIONSFILES_DESCR = 
	    "Wildcard observations files";

	public static final String DATA_OMITTEDWILDCARDOBSERVATIONSFILES = 
	    "omittedWildcardObservationsFiles";
	public static final String DATA_OMITTEDWILDCARDOBSERVATIONSFILES_DESCR = 
	    "Omitted wildcard observations files";

	// The observation count value, as supplied by the user:
	public static final String SETTING_OBSERVATIONCOUNT = "observationCount";
	public static final String SETTING_OBSERVATIONCOUNT_DESCR = "Observation count";
	public static final String SETTING_OBSERVATIONCOUNT_DISP = "Observation count:";
	public static final int BANJO_OBSERVATIONCOUNT_NONUMBERSUPPLIED = -1;
	// The observation count value, as accessible by the application (we keep
	// it separate, because we may apply different logic on whether to use
	// the count supplied via the actual rows in the data file, or the value
	// from  user input)
	public static final String DATA_OBSERVATIONCOUNT = "observationCount";
    public static final String DATA_OBSERVATIONCOUNT_DESCR = "Observation count";
    public static final String DATA_OBSERVATIONCOUNT_DISP = "Observation count:";
    public static final String DATA_OBSERVATIONCOUNT_EFFECTIVE_DISP = 
        "Number of observations used for learning the network:";
    public static final String DATA_OBSERVATIONCOUNT_EFFECTIVE_DISP_DBN = 
        "Number of observations used for learning DBN:";
    
    public static final String DATA_SPECIFIEDOBSERVATIONCOUNT = 
        "specifiedObservationCount";
    public static final String DATA_SPECIFIEDOBSERVATIONCOUNT_DESCR = 
        "Number of observation specified to be used";
    public static final String DATA_SPECIFIEDOBSERVATIONCOUNT_DISP = 
        "Number of observations specified to be used:";
    
    public static final String DATA_OBSERVEDOBSERVATIONCOUNT = 
        "observedObservationCount";
    public static final String DATA_OBSERVEDOBSERVATIONCOUNT_DESCR = 
        "Observed observation count";
    public static final String DATA_OBSERVEDOBSERVATIONCOUNT_DISP = 
        "Number of observations:";
    
	public static final String DATA_OBSERVEDOBSERVATIONROWCOUNT = 
	    "observedObservationRowCount";
    public static final String DATA_OBSERVEDOBSERVATIONROWCOUNT_DISP = 
        "Number of observations (in file):";
    public static final String DATA_OBSERVEDOBSERVATIONROWCOUNT_DISP_PL = 
        "Number of observations (in files):";

	public static final String SETTING_MAXTHREADS = "threadCount";

	public static final String DATA_PROPOSEDNETWORKS = "proposedNetworks";

    // -------------------------
    // General searcher settings
    // -------------------------
	
	// Settings for the stoppping criteria (time, networks, restarts)
	public static final String SETTING_MAXSEARCHTIME = "maxTime";
	public static final String SETTING_MAXSEARCHTIME_DESCR = "Max. time";
	public static final String SETTING_MAXSEARCHTIME_DISP = "Max. time:";

	public static final String SETTING_MAXPROPOSEDNETWORKS = "maxProposedNetworks";
	public static final String SETTING_MAXPROPOSEDNETWORKS_DESCR = "Max. proposed networks";
	public static final String SETTING_MAXPROPOSEDNETWORKS_DISP = "Max. proposed networks:";

	public static final String SETTING_MAXRESTARTS = "maxRestarts";
	public static final String SETTING_MAXRESTARTS_DESCR = "Max. restarts";
	public static final String SETTING_MAXRESTARTS_DISP = "Max. restarts:";

	public static final String SETTING_MINNETWORKSBEFORECHECKING = 
	    "minNetworksBeforeChecking";
	public static final String SETTING_MINNETWORKSBEFORECHECKING_DESCR = 
	    "Min. networks before checking";
	public static final String SETTING_MINNETWORKSBEFORECHECKING_DISP = 
	    "Min. networks before checking:";
	// Validated as long > 0, with default
	public static final long DEFAULT_MINPROPOSEDNETWORKSHIGHSCORE = 1000;
	
    public static final String SETTING_NBEST = "nBestNetworks";
    public static final String SETTING_NBEST_DESCR = "N-best networks";
    public static final String SETTING_NBEST_DISP = "Number of best networks tracked:";
    // Validated as int > 0. Defaults to 1 when not supplied.
    public static final int DEFAULT_NBEST = 1;

    public static final String SETTING_BESTNETWORKSARE = "bestNetworksAre";
    public static final String SETTING_BESTNETWORKSARE_DESCR = "Best networks are";
    public static final String SETTING_BESTNETWORKSARE_DISP = "Best networks are:";
    // Validated as int > 0. Defaults to 1 when not supplied.
    public static final String UI_BESTNETWORKSARE_NONIDENTICAL = 
        "nonidentical";
    public static final String UI_BESTNETWORKSARE_NONIDENTICAL_DISP = 
        " non-identical";
    public static final String UI_BESTNETWORKSARE_NONEQUIVALENT = 
        "nonequivalent";
    public static final String UI_BESTNETWORKSARE_NONEQUIVALENT_DISP = 
        " non-equivalent";
    public static final String UI_BESTNETWORKSARE_NONIDENTICALTHENPRUNED = 
        "nonidenticalThenPruned";
    public static final String UI_BESTNETWORKSARE_NONIDENTICALTHENPRUNED_DISP = 
        " non-identical then pruned";
    public static final String DEFAULT_BESTNETWORKSARE = UI_BESTNETWORKSARE_NONIDENTICAL;

    public static final String SETTING_MAXPARENTCOUNT = "maxParentCount";
    public static final String SETTING_MAXPARENTCOUNT_DESCR = "Max. parent count";
    public static final String SETTING_MAXPARENTCOUNT_DISP = "Max. parent count:";
    // Validated as int > 0, and <= default max. parent count.
    public static final String SETTING_DEFAULTMAXPARENTCOUNT = "defaultMaxParentCount";
    public static final String SETTING_DEFAULTMAXPARENTCOUNT_DESCR = 
        "Default max. parent count";
    public static final String SETTING_DEFAULTMAXPARENTCOUNT_DISP = 
        "Default max. parent count:";
    // Default max. parent count is > 0; if not specified, it defaults to 
    // the DEFAULT_MAXPARENTCOUNT value.
    // Note: On PC's with up to 1 Gb of memory,
    // the max. parent count without running out of memory is about 10. 
    // (with ca. 30 vars, 300 observations)
    // To lift this restriction, the use of arrays in the evaluator
    // implementations likely has to be rewritten (e.g. using sparse array
    // techniques)
    public static final int DEFAULT_MAXPARENTCOUNT = 5;
    // The limit value for the defaultMaxParentCount (note: this is a safeguard due
    // to the large memory requirements associated with this setting; change at
    // your own peril :)  )
    public static final int LIMIT_DEFAULTMAXPARENTCOUNT = 10;

    // ------------------------
	// Greedy searcher settings
    // ------------------------
    
	public static final String SETTING_MINPROPOSEDNETWORKSAFTERHIGHSCORE =
	    "minProposedNetworksAfterHighScore";
	public static final String SETTING_MINPROPOSEDNETWORKSAFTERHIGHSCORE_DESCR =
	    "Min. proposed networks after high score";
	public static final String SETTING_MINPROPOSEDNETWORKSAFTERHIGHSCORE_DISP =
	    "Min. proposed networks after high score:";
	public static final long DEFAULT_MINPROPOSEDNETWORKSAFTERHIGHSCORE = 1000;
	
	public static final String SETTING_MINPROPOSEDNETWORKSBEFORERESTART =
	    "minProposedNetworksBeforeRestart";
	public static final String SETTING_MINPROPOSEDNETWORKSBEFORERESTART_DESCR =
	    "Min. proposed networks before restart";
	public static final String SETTING_MINPROPOSEDNETWORKSBEFORERESTART_DISP =
	    "Min. proposed networks before restart:";
	public static final long DEFAULT_MINPROPOSEDNETWORKSBEFORERESTART = 5000;

	public static final String SETTING_MAXPROPOSEDNETWORKSBEFORERESTART = 
	    "maxProposedNetworksBeforeRestart";
	public static final String SETTING_MAXPROPOSEDNETWORKSBEFORERESTART_DESCR = 
	    "Max. proposed networks before restart";
	public static final String SETTING_MAXPROPOSEDNETWORKSBEFORERESTART_DISP = 
	    "Max. proposed networks before restart:";
	public static final long DEFAULT_MAXPROPOSEDNETWORKSBEFORERESTART = 10000;
	
	// Restart-related: restart based on random versus fixed structure
	public static final String SETTING_RESTARTWITHRANDOMNETWORK = 
		"restartWithRandomNetwork";
	public static final String SETTING_RESTARTWITHRANDOMNETWORK_DESCR = 
		"Restart with random network";
	public static final String SETTING_RESTARTWITHRANDOMNETWORK_DISP = 
		"Restart method:";
	public static final String DATA_RESTARTWITHRANDOMNETWORK_DISP = 
		"use random network";
	public static final String DATA_RESTARTWITHINITALNETWORK_DISP = 
		"use initial network";
	public static final String UI_RESTARTWITHRANDOMNETWORK_YES = "yes";
	public static final String UI_RESTARTWITHRANDOMNETWORK_NO = "no";
	public static final String DEFAULT_RESTARTWITHRANDOMNETWORK = 
	    UI_RESTARTWITHRANDOMNETWORK_YES;
	// User choice: actual limit on the parent count for structures that
	// are computed at "restart" of the search
	public static final String SETTING_MAXPARENTCOUNTFORRESTART = 
		"maxParentCountForRestart";
	public static final String SETTING_MAXPARENTCOUNTFORRESTART_DESCR = 
		"Max. parent count for restarts";
	public static final String SETTING_MAXPARENTCOUNTFORRESTART_DISP = 
	    "  with max. parent count:";
	
    // ----------------------
	// Annealer related input
	// ----------------------
    
	// Initial temperature for annealing process
	public static final String SETTING_INITIALTEMPERATURE = "initialTemperature";
	public static final String SETTING_INITIALTEMPERATURE_DESCR = "Initial temperature";
	public static final String SETTING_INITIALTEMPERATURE_DISP = "Initial temperature:";
	public static final String DATA_CURRENTTEMPERATURE = "currentTemperature";
	// Values for "initialTemperature":
	// Validated as double > 0, with default if not user-supplied:
	public static final long DEFAULT_INITIALTEMPERATURE = 1000;
	
	// Cooling factor appplied after SETTING_MINNETWORKSBEFORECHECKING loops
	public static final String SETTING_COOLINGFACTOR = "coolingFactor";
	public static final String SETTING_COOLINGFACTOR_DESCR = "Cooling factor";
	public static final String SETTING_COOLINGFACTOR_DISP = "Cooling factor:";
	// Values for "initialTemperature":
	// Validated as double > 0, with default if no user-supplied value:
	public static final double DEFAULT_COOLINGFACTOR = 0.7;
	//
	public static final String SETTING_MAXACCEPTEDNETWORKSBEFORECOOLING =
	    "maxAcceptedNetworksBeforeCooling";
	public static final String SETTING_MAXACCEPTEDNETWORKSBEFORECOOLING_DESCR =
	    "Max. accepted networks before cooling";
	public static final String SETTING_MAXACCEPTEDNETWORKSBEFORECOOLING_DISP =
	    "Max. accepted networks before cooling:";
	public static final long DEFAULT_MAXACCEPTEDNETWORKSBEFORECOOLING = 2500;
	//
	public static final String SETTING_MAXPROPOSEDNETWORKSBEFORECOOLING = 
	    "maxProposedNetworksBeforeCooling";
	public static final String SETTING_MAXPROPOSEDNETWORKSBEFORECOOLING_DESCR = 
	    "Max. proposed networks before cooling";
	public static final String SETTING_MAXPROPOSEDNETWORKSBEFORECOOLING_DISP = 
	    "Max. proposed networks before cooling:";
	public static final long DEFAULT_MAXPROPOSEDNETWORKSBEFORECOOLING = 10000;
	//
	public static final String SETTING_MINACCEPTEDNETWORKSBEFOREREANNEALING = 
	    "minAcceptedNetworksBeforeReannealing";
	public static final String SETTING_MINACCEPTEDNETWORKSBEFOREREANNEALING_DESCR = 
	    "Min. accepted networks before reannealing";
	public static final String SETTING_MINACCEPTEDNETWORKSBEFOREREANNEALING_DISP = 
	    "Min. accepted networks before reannealing:";
	public static final long DEFAULT_MINACCEPTEDNETWORKSBEFOREREANNEALING = 500;
	//
	public static final String SETTING_REANNEALINGTEMPERATURE = 
	    "reannealingTemperature";
	public static final String SETTING_REANNEALINGTEMPERATURE_DESCR = 
	    "Reannealing temperature";
	public static final String SETTING_REANNEALINGTEMPERATURE_DISP = 
	    "Reannealing temperature:";
	public static final long DEFAULT_REANNEALINGTEMPERATURE = 800;
	

    // ------------
	// Misc. inputs
	// ------------
    
	// Computation choice
	public static final String SETTING_PRECOMPUTE = 
	    "precomputeLogGamma";
	public static final String SETTING_PRECOMPUTE_DESCR = 
	    "Pre-compute logGamma";
	public static final String SETTING_PRECOMPUTE_DISP = 
	    "Pre-compute logGamma:";
	// Values for "precomputeLogGamma":
	public static final String UI_PRECOMPUTE_YES = "yes";
	public static final String UI_PRECOMPUTE_NO = "no";
	// If not supplied, defaults to "yes":
	public static final String UI_PRECOMPUTE_DEFAULT = "yes";
    
	// ------------------------------
	// Pre-processing related input
	// ------------------------------
    
	// 1. Discretization
	public static final String SETTING_DISCRETIZATIONPOLICY = 
	    "discretizationPolicy";
	public static final String SETTING_DISCRETIZATIONPOLICY_DESCR = 
	    "Discretization policy";
	public static final String SETTING_DISCRETIZATIONPOLICY_DISP = 
	    "Discretization policy:";
	public static final String SETTING_DISCRETIZATIONEXCEPTIONS = 
	    "discretizationExceptions";
	public static final String SETTING_DISCRETIZATIONEXCEPTIONS_DESCR = 
	    "Exceptions to the discretization policy";
	public static final String SETTING_DISCRETIZATIONEXCEPTIONS_DISP = 
	    "Exceptions to the discretization policy:";
	public static final String SETTING_DISCRETIZATIONEXCEPTIONS_NONE_DISP = 
	    "none";
	public static final String UI_DISCRETIZATIONBYQUANTILE = "q"; //"quantile";
	public static final String UI_DISCRETIZATIONBYINTERVAL = "i"; //"interval";
	public static final String UI_DISCRETIZATIONNONE = "none"; //"none";
	public static final String DEFAULT_DISCRETIZATION = UI_DISCRETIZATIONNONE;
	// 
	// Default settings for discretization (used internally only)
	public static final String DATA_DEFAULTDISCRETIZATIONCHOICE = 
	    "defaultDiscretizationChoice";
	public static final String DATA_DEFAULTDISCRETIZATIONPOINTS = 
	    "defaultDiscretizationPoints";
	public static final String DEFAULT_DISCRETIZATIONTYPE = 
	    UI_DISCRETIZATIONNONE;
	public static final int DEFAULT_DISCRETIZATIONPOINTS = 2;
	
	// Internal data for the exceptions to the discretization
	// ------------------------------------------------------
	// Format: a comma-delimited list, with one entry per variable, and
	// each value a standard discretization choice (see list above)
	public static final String DATA_DISCRETIZATIONEXCEPTION_TYPELIST = 
	    "discretizationExceptionTypeList";
	// Format: a comma-delimited list, with one entry per variable, and
	// each value an integer between 2 and the max number of values that
	// each variable can hold
	public static final String DATA_DISCRETIZATIONEXCEPTION_POINTSLIST = 
	    "discretizationExceptionPointsList";
        
    // ---------------------
    // Project related input
    // ---------------------
    
    // All input is treated as string and not validated
    public static final String SETTING_PROJECT = "project";
    public static final String SETTING_PROJECT_DESCR = "Project";
    public static final String SETTING_PROJECT_DISP = "Project:";

    public static final String SETTING_USER = "user";
    public static final String SETTING_USER_DESCR = "User";
    public static final String SETTING_USER_DISP = "User:";
    
    public static final String SETTING_DATASET = "dataset";
    public static final String SETTING_DATASET_DESCR = "Dataset";
    public static final String SETTING_DATASET_DISP = "Dataset:";
    
    public static final String SETTING_NOTES = "notes";
    public static final String SETTING_NOTES_DESCR = "Notes";
    public static final String SETTING_NOTES_DISP = "Notes:";

    // -----------------------
    // Output-related settings
    //------------------------

	// Discretization report
	public static final String SETTING_DISCRETIZATIONREPORT =
	    "createDiscretizationReport";
	public static final String SETTING_DATAREPORT_DESCR =
	    "Discretization (data) report";
	public static final String UI_DATAREPORT_STANDARD =
	    "standard";
	public static final String UI_DATAREPORT_WITHMAPPEDVALUES =
	    "withMappedValues";
	public static final String UI_DATAREPORT_WITHMAPPEDANDORIGINALVALUES =
	    "withMappedAndOriginalValues";
	public static final String UI_DATAREPORT_NO =
	    "no";
	public static final String DEFAULT_DATAREPORT =
	    UI_DATAREPORT_NO;

	// -----------------------
	// Post-processing options
	// -----------------------
    // displayStatistics is not implemented yet
	public static final String SETTING_DISPLAYSTATISTICS = "displayStatistics";
	public static final String SETTING_DISPLAYSTATISTICS_DESCR = "Display statistics";
	public static final String SETTING_DISPLAYSTATISTICS_DISP = "Display statistics:";
	public static final String UI_DISPLAYSTATISTICS_YES = "yes";
	public static final String UI_DISPLAYSTATISTICS_NO = "no";
	public static final String DEFAULT_DISPLAYSTATISTICS = UI_DISPLAYSTATISTICS_NO;

	public static final String SETTING_COMPUTEINFLUENCESCORES = "computeInfluenceScores";
	public static final String SETTING_COMPUTEINFLUENCESCORES_DESCR = 
	    "Compute influence scores"; 
	public static final String SETTING_COMPUTEINFLUENCESCORES_DISP = 
	    "Compute influence scores:"; 
	public static final String UI_COMPUTEINFLUENCESCORES_YES = "yes";
	public static final String UI_COMPUTEINFLUENCESCORES_NO = "no";
	public static final String DEFAULT_COMPUTEINFLUENCESCORES = 
	    UI_COMPUTEINFLUENCESCORES_NO;
	public static final String DATA_FILE_INFLUENCESCORES = "influenceScoresFile";
	
	public static final String SETTING_CREATEDOTOUTPUT = "createDotOutput";
	public static final String SETTING_CREATEDOTOUTPUT_DESCR = "Create 'dot' output";
	public static final String SETTING_CREATEDOTOUTPUT_DISP = "Create 'dot' output:";
//	public static final String SETTING_CREATEDOTOUTPUT_STRUCTURE = "'Dot' structure:";
//	public static final String SETTING_CREATEDOTOUTPUT_GRAPHICS = "'Dot' graphics:";
	public static final String UI_CREATEDOTOUTPUT_YES = "yes";
	public static final String UI_CREATEDOTOUTPUT_NO = "no";
	public static final String DEFAULT_CREATEDOTOUTPUT = UI_CREATEDOTOUTPUT_NO;
	public static final String DATA_FILE_BESTNETWORK_STRUCTURE = "fileBestNetworkStructure";
	public static final String DATA_FILE_BESTNETWORK_GRAPHICS = "fileBestNetworkGraphics";
	//
	public static final String SETTING_COMPUTECONSENSUSGRAPH = "computeConsensusGraph";
	public static final String SETTING_COMPUTECONSENSUSGRAPH_DESCR = 
	    "Compute consensus graph";
	public static final String SETTING_COMPUTECONSENSUSGRAPH_DISP = 
	    "Compute consensus graph:";
	public static final String UI_COMPUTECONSENSUSGRAPH_YES = "yes";
	public static final String UI_COMPUTECONSENSUSGRAPH_NO = "no";
	public static final String DEFAULT_COMPUTECONSENSUSGRAPH = UI_COMPUTECONSENSUSGRAPH_NO;
	public static final String DATA_FILE_CONSENSUSGRAPH_STRUCTURE = 
	    "fileConsensusGraphStructure";
	public static final String DATA_FILE_CONSENSUSGRAPH_GRAPHICS = 
	    "fileConsensusGraphGraphics";

	public static final String SETTING_DISPLAYCONSENSUSGRAPHASHTML = 
	    "createConsensusGraphAsHTML";
	public static final String SETTING_DISPLAYCONSENSUSGRAPHASHTML_DESCR = 
    	"Create consensus graph as HTML";
	public static final String SETTING_DISPLAYCONSENSUSGRAPHASHTML_DISP = 
    	"Create consensus graph as HTML:";
	public static final String UI_DISPLAYCONSENSUSGRAPHASHTML_YES = "yes";
	public static final String UI_DISPLAYCONSENSUSGRAPHASHTML_NO = "no";
	public static final String DEFAULT_DISPLAYCONSENSUSGRAPHASHTML = 
	    UI_DISPLAYCONSENSUSGRAPHASHTML_NO;
	// Store the optional consensus graph in a table-like format
	public static final String DATA_CONSENSUSGRAPHASHTML = "consensusGraphAsHTML";
	public static final String DATA_FILE_CONSENSUSGRAPHASHTML = "consensusGraphAsHtmlFile";

    // Settings for handling the creation of ggraphics files using dot
	public static final String SETTING_FULLPATHTODOTEXECUTABLE = "fullPathToDotExecutable";
	public static final String SETTING_FULLPATHTODOTEXECUTABLE_DESCR = 
	    "Full path to 'dot' executable";
	public static final String SETTING_FULLPATHTODOTEXECUTABLE_DISP = 
	    "Location of 'dot':";
	public static final String DEFAULT_NODOTEXECUTABLESUPPPLIED = 
	    DATA_SETTINGINVALIDVALUE;
	public static final String UI_FULLPATHTODOTEXECUTABLE_NOTSUPPLIED = 
	    "not supplied";
	
	//
	public static final String SETTING_FILENAMEFORTOPGRAPH = "fileNameForTopGraph";
	public static final String SETTING_FILENAMEFORTOPGRAPH_DESCR = 
	    "File name for top graph";
	public static final String SETTING_FILENAMEFORTOPGRAPH_DISP = 
	    "File name for top graph:";
	public static final String DEFAULT_FILENAMEFORTOPGRAPH = "graph.top";
	//
	public static final String SETTING_FILENAMEFORCONSENSUSGRAPH = 
	    "fileNameForConsensusGraph";
	public static final String SETTING_FILENAMEFORCONSENSUSGRAPH_DESCR = 
	    "File name for consensus graph";
	public static final String SETTING_FILENAMEFORCONSENSUSGRAPH_DISP = 
	    "File name for consensus graph:";
	public static final String DEFAULT_FILENAMEFORCONSENSUSGRAPH = "graph.consensus";
	//
	public static final String SETTING_FILENAMEFORTOPGRAPHSASHTML = 
	    "fileNameForGraphsAsHtml";
	public static final String SETTING_FILENAMEFORTOPGRAPHSASHTML_DESCR = 
	    "File name for graphs as html";
	public static final String SETTING_FILENAMEFORTOPGRAPHSASHTML_DISP = 
	    "File name for graphs in html format:";
	public static final String DEFAULT_FILENAMEFORTOPGRAPHSASHTML = "graphs.html";
	//
	//
	public static final String SETTING_DOTGRAPHICSFORMAT = "dotGraphicsFormat";
	public static final String SETTING_DOTGRAPHICSFORMAT_DESCR = "'dot' graphics format";
	public static final String SETTING_DOTGRAPHICSFORMAT_DISP = "'dot' graphics format:";
	// Supported graphics formats for dot
    public static final String UI_DOTFORMAT_CANON = "canon";
    public static final String UI_DOTFORMAT_DOT = "dot";
    public static final String UI_DOTFORMAT_FIG = "fig";
    public static final String UI_DOTFORMAT_GD = "gd";
    public static final String UI_DOTFORMAT_GIF = "gif";
    public static final String UI_DOTFORMAT_HPGL = "hpgl";
    public static final String UI_DOTFORMAT_IMAP = "IMAP";
    public static final String UI_DOTFORMAT_CMAP = "CMAP";
    public static final String UI_DOTFORMAT_JPG = "jpg";
    public static final String UI_DOTFORMAT_MIF = "mif";
    public static final String UI_DOTFORMAT_MP = "mp";
    public static final String UI_DOTFORMAT_PCL = "pcl";
    public static final String UI_DOTFORMAT_PIC = "pic";
    public static final String UI_DOTFORMAT_PLAIN = "plain";
    public static final String UI_DOTFORMAT_PNG = "png";
    public static final String UI_DOTFORMAT_PS = "ps";
    public static final String UI_DOTFORMAT_PS2 = "ps2";
    public static final String UI_DOTFORMAT_SVG = "svg";
    public static final String UI_DOTFORMAT_VRML = "vrml";
    public static final String UI_DOTFORMAT_VTX = "vtx";
    public static final String UI_DOTFORMAT_VBMP = "vbmp";
	public static final String DEFAULT_DOTGRAPHICSFORMAT = UI_DOTFORMAT_PNG;
	//
	public static final String SETTING_DOTFILEEXTENSION = "dotFileExtension";
	public static final String SETTING_DOTFILEEXTENSION_DESCR = "'dot' file extension";
	public static final String SETTING_DOTFILEEXTENSION_DISP = "'Dot' file extension:";
	public static final String DEFAULT_DOTFILEEXTENSION = "txt";
	//
	public static final String SETTING_HTMLFILEEXTENSION = "htmlFileExtension";
	public static final String SETTING_HTMLFILEEXTENSION_DESCR = "HTML file extension";
	public static final String SETTING_HTMLFILEEXTENSION_DISP = "HTML file extension:";
	public static final String DEFAULT_HTMLFILEEXTENSION = "html";

	// Containers that enable us to provide a certain order of the items in our feedback display
	public static final String DATA_SEARCHERINFO_SPECIFICSEARCHER = "InfoForSpecificSearcher";
	public static final String DATA_SEARCHERINFO_COREOBJECTS = "SearcherInfoCoreObjects";
	public static final String DATA_SEARCHERINFO_STATISTICS = "SearcherInfoStatistics";
    public static final String DATA_SEARCHERINFO_FEEDBACK_1 = "SearcherInfoFeedback";
    public static final String DATA_SEARCHERINFO_FEEDBACK_2 = "SearcherInfoFeedback2";
	
	
	//----------------------------------------------
	// Codes and strings used for EXCEPTION handling
	//----------------------------------------------

    // Internal errors, likely due to development issues
    public static final int ERROR_BANJO_DEV = 0;

    // Input-related errors
    public static final int ERROR_BANJO_USERINPUT = 100;
    // XML-processing-related errors
    public static final int ERROR_BANJO_XML = 110;

    // Finally, errors that can happen in the program, but likely only
    // for pathological network configurations (used in Greedy and SimAnneal searchers)
    public static final int ERROR_BANJO_UNEXPECTED = 200;
    
    // 1/18/2006 (v2.0) hjs
    // This is a special "error": it is used as part of our special error tracking
    // mechanism to trigger the processing of any encountered error to this point.
    public static final int ERROR_CHECKPOINTTRIGGER = 500;
    
    // This is another special error, for handling the out-of-memory scenario:
    public static final int ERROR_BANJO_OUTOFMEMORY = 1000;
    //
    public static final String ERRORMSG_BANJO_OUTOFMEMORY_1 = 
        "[Unrecoverable runtime error: out of memory]";
    public static final String ERRORMSG_BANJO_OUTOFMEMORY_2 = 
        "Banjo's memory requirements during the search execution exceeded " +
        "its maximum alloted memory of ";
    public static final String ERRORMSG_BANJO_OUTOFMEMORY_3a = 
        "Although the search cannot be continued, Banjo will try to " +
        "display as much information as possible about the obtained results.";
    public static final String ERRORMSG_BANJO_OUTOFMEMORY_3b = 
        "In addition, Banjo will attempt to complete as many post-processing " +
        "options as possible.";
    public static final String ERRORMSG_BANJO_OUTOFMEMORY_3 = 
        ERRORMSG_BANJO_OUTOFMEMORY_3a + 
        FEEDBACK_NEWLINE +
        ERRORMSG_BANJO_OUTOFMEMORY_3b;
    
	public static final String ERRORMESSAGEDISPLAYPREFIX = "\n- ";
	
	// Errors-related strings
	public static final String ERRORMSG_MISSING_SETTINGSFILE = 
		"The main settings file could not be loaded.";
	public static final String ERRORMSG_INVALIDSEARCHER = 
		"No valid searcher was specified in the settings file.";
	public static final String ERRORMSG_NULL_SEARCHER = 
		"The application was unable to create a searcher object.";
	public static final String ERRORMSG_MISSINGOBSERVATIONS = 
		"ObservationsAsMatrix file cannot be found.";
	public static final String ERRORMSG_MISSINGSTRUCTUREFILE = 
		"A user-specified structure file cannot be found.";
	public static final String ERRORMSG_MISSINGFILE = 
		"File cannot be found: ";
	public static final String ERRORMSG_COULDNOTFINDVALIDCHANGE = 
		"Tried unsuccessfully to find a valid bayesNetChange.";
	
	
	// -------------------------------------------
	// Regular expressions for pattern (e.g., whitespace) matching
	// -------------------------------------------

    // Common characters in data file
    public static final String DEFAULT_COMMENTINDICATOR = "#";
    public static final String DEFAULT_WILDCARDINDICATOR = "*";
    public static final String DEFAULT_TOKENINDICATOR = "@";
    public static final String DEFAULT_ITEMVALUESEPARATOR = "=";
    // Multiple white space characters between data entries:
    public static final String WHITESPACE_MULTIPLECHARACTERS = "\\s+";
    // Exactly one white space character between data entries:
    public static final String WHITESPACE_SINGLECHARACTER = "\\s";
    
    // Various delimiters
    public static final String DELIMITER_DEFAULT_LIST = ",";
    public static final String DELIMITER_DEFAULT_LIST_XML = ",";
    public static final String DELIMITER_DEFAULT_ITEM = ":";
    public static final String DELIMITER_DEFAULT_TIME = ":";
    public static final String DELIMITER_SPACE = " ";
    // Use this to store (e.g., in variable names processing) a comma-delimited list
    public static final String DELIMITER_SPECIAL = "@,@";
    // for observations
    public static final String DELIMITER_DEFAULT_OBSERVATIONS = "\t";
    // for exceptions (in discretization handling)
    public static final String DELIMITER_DEFAULT_DISCRETIZATIONEXCEPTIONS = ",";
    public static final String DELIMITER_DEFAULT_DISCRETIZATIONEXCEPTIONITEM = ":";

    // -----------------------------------------------------------------------
    // Currently there is no user access to the patterns via the settings file
    // Create your own pattern here, if necessary:
    // -----------------------------------------------------------------------	
    
    // Exactly one white space character between data entries:
    public static final String DELIMITERSWITHOUTWHITESPACE = "[,:;]";
    
    // Multiple white space characters between data entries separated by ":":
    // (used for checking a list of variable names)
    public static final String DELIMITERCOLONWITHWHITESPACE = "\\s?[:]\\s?";
    
    // Multiple white space characters between data entries separated by ",":
    // (used for checking a list of variable names)
    public static final String DELIMITERCOMMAWITHWHITESPACE = "\\s?[,]\\s?";
	
	// Multiple white space characters plus a delimiter between data entries:
    public static final String DELIMITERSPLUSWHITESPACE = "\\s?[,:;]\\s?";
    public static final String DELIMITERSPLUSWHITESPACE2 = "\\s+[,:;]\\s+";

	// These are the constants used in the Banjo code
	public static final String PATTERN_OBSERVATIONSPARSING_WHITESPACE = WHITESPACE_MULTIPLECHARACTERS;
    public static final String PATTERN_VARIABLENAMESPARSING_WHITESPACE = WHITESPACE_MULTIPLECHARACTERS;
    public static final String PATTERN_VARIABLENAMESPARSING_DELS = DELIMITERSPLUSWHITESPACE;
    public static final String PATTERN_VARIABLENAMESCHECKFORCOLON = DELIMITERCOLONWITHWHITESPACE;
    public static final String PATTERN_VARIABLENAMESCHECKFORCOMMAS = DELIMITERCOMMAWITHWHITESPACE;
    public static final String PATTERN_VARIABLENAMES_SPECIALSPECIFIER = "COMMAS";
    // Used so the variable names list can include commas within a variable name
    public static final String PATTERN_VARIABLENAMESSPECIALDELIMITER = DELIMITER_SPECIAL;
	
	// pattern for mandatory lag for DBNs parsing
    public static final String PATTERN_INTEGERLIST = "\\s?[,]\\s?";
    // pattern for parsing list of settings
    public static final String PATTERN_STRINGLIST = "\\s?[,]\\s?";
	
	// pattern for mandatory lag for parsing of discretization exceptions
	public static final String PATTERN_DISCRETIZATIONEXCEPTIONS = "\\s?,\\s?";
	public static final String PATTERN_DISCRETIZATIONEXCEPTION_ITEM = "\\s?[:]\\s?";
	
	// patterns for parsing a structure file
	public static final String PATTERN_STATICBAYESNET = WHITESPACE_MULTIPLECHARACTERS;
	public static final String PATTERN_DYNAMICBAYESNET = "\\s?[:]\\s?";
	public static final String PATTERN_DYNAMICBAYESNETITEM = WHITESPACE_MULTIPLECHARACTERS;
	public static final String PATTERN_VARCOUNT = WHITESPACE_MULTIPLECHARACTERS;
    
    
    // (to be) used for monitoring the status of a running Banjo app
    // (and as such may want it to be more grannular, or even processData-specific)
    public static int APPLICATIONSTATUS_STARTUP = 100;
    public static int APPLICATIONSTATUS_LOADINGDATA = 101;
    public static int APPLICATIONSTATUS_LOADINGOBSERVATIONS = 102;
    public static int APPLICATIONSTATUS_LOADINGSTRUCTUREFILE = 103;
    public static int APPLICATIONSTATUS_STARTUPCOMPLETED = 110;
    public static int APPLICATIONSTATUS_SEARCHING = 200;
    //
    public static int APPLICATIONSTATUS = APPLICATIONSTATUS_STARTUP;
    
    // Deprecated settings
    public static final String SETTING_DEPREC_NUMBEROFINTERMEDIATEPROGRESSREPORTS = 
        "numberOfIntermediateProgressReports";
    public static final String SETTING_DEPREC_WRITETOFILEINTERVAL = 
        "writeToFileInterval";
    
    // XML handling, added in version 2.2
    public static final String DATA_XMLPARSER_DISP = "XML parser:";
    public static final String DATA_XMLPARSERCHOICE = "org.apache.xerces.parsers.SAXParser";
    public static final int DATA_XMLTYPE_ELEMENT = 1;
    
    // Banjo specific XML tags
    public static final String DATA_BANJOXMLTAG_BANJOXMLFORMATVERSION = "BanjoXMLformatVersion";
    public static final String DATA_BANJOXMLTAG_BANJODATA = "BanjoData";
    public static final String DATA_BANJOXMLTAG_BANJOSETTINGS = "BanjoSettings";
    public static final String DATA_BANJOXMLTAG_NBESTNETWORKS = "nBestNetworks";
    public static final String DATA_BANJOXMLTAG_VARCOUNT = 
        SETTING_VARCOUNT; // i.e. "variableCount";
    public static final String DATA_BANJOXMLTAG_MINMARKOVLAG = 
        SETTING_MINMARKOVLAG; // i.e. "minMarkovLag";
    public static final String DATA_BANJOXMLTAG_MAXMARKOVLAG = 
        SETTING_MAXMARKOVLAG; // i.e. "maxMarkovLag";
    public static final String DATA_BANJOXMLTAG_NETWORK = "network";
    public static final String DATA_BANJOXMLTAG_NETWORKSCORE = "networkScore";
    public static final String DATA_BANJOXMLTAG_NETWORKSTRUCTURE = "networkStructure";
    //
    public static final String DATA_XML_NEWLINE = "\n";
    public static final String DATA_NBEST_NEWLINE = "\n";
    public static final char DATA_OMITFILE_IDENTIFIER = '-';
    

    // Controlling the seed for the random sequence
    public static final String SETTING_BANJOSEED = "seedForStartingSearch";
    public static final String SETTING_BANJOSEED_DESCR = "seed for starting search";
    public static final String SETTING_BANJOSEED_DISP = "Seed for starting search:";
    public static final String DATA_BANJOSEED = "seedForStartingSearch";
}