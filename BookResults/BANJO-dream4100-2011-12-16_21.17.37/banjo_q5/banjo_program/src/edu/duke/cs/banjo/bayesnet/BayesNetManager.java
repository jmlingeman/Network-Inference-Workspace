/*
 * Created on Mar 10, 2004
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
package edu.duke.cs.banjo.bayesnet;

import edu.duke.cs.banjo.data.settings.SettingItem;
import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.utility.*;

import java.util.*;

/**
 * 
 * Contains the implementation of the network. 
 *
 * <p><strong>Details:</strong> <br>
 * 
 * - Consists of a collection of objects that describe the state of the network,
 * including "parentMatrices" for various subsets of network nodes, such as the
 * addable and deleteable, mustBePresent and mustBeAbsent, initial and current
 * nodes (parents). <br>
 * - Geared towards performance: frequently trades additional storage and code
 * for execution speed. <br>
 *
 * <p><strong>Change History:</strong> <br>
 * Created on Mar 10, 2004
 * 
 * <br>
 * (v2.0) Use of the more efficient parent set implementations. Note that the original
 * parent sets from version 1.0.x will be deprecated in an upcoming maintenance release.
 * 
 * <br>
 * 3/24/2008 (v2.2) hjs     Modify access to random numbers.
 * 
 * <br>
 * 4/2/2008 (v2.2)  hjs     Eliminate nodeScores and changedNodeScores arrays, as well
 *                          as related methods (<--> changes to interface)
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class BayesNetManager implements BayesNetManagerI {
	
	// Note: The 4 node sets are choosen in a way that they are mutually exclusive
	// which simplifies significantly many of the operations that we want to apply.
	// e.g., currentParents = mustBePresentParents + deleteableParents
	// changeableParents = deleteableParents + addableParents, etc
	
	// Initial, mandatory and disallowed node sets cannot be changed after
	// their initial assignment:
	private final EdgesWithCachedStatisticsI initialParents;
	private final EdgesWithCachedStatisticsI mustBePresentParents;
	private final EdgesWithCachedStatisticsI mustBeAbsentParents;
    
    // Only deleteable, addable, and current node sets can be manipulated
    // after the initial assignment:
    private EdgesWithCachedStatisticsI deleteableParents;
    private EdgesWithCachedStatisticsI addableParents;
    // Derived set of nodes that represents the entire current network structure
    // (Note: for performance reasons, we apply changes to currentParents
    // as we go)
    private EdgesWithCachedStatisticsI currentParents;
	
	// Internal data (actually retrievable from the parentMatrices,
	// but cached for performance)
	private final int varCount;
	private final int minMarkovLag;
	private final int maxMarkovLag;
    
	private int[] dbnMandatoryIdentityLags;
	private String strDbnMandatoryIdentityLags;

	private final int maxParentCountForRestart;
	protected final boolean restartWithRandomNetwork;
    protected boolean checkThenApply;

	protected Settings processData;
    private final String configCycleCheckerMethod;
    private final boolean cycleCheckingNeedsAdjustment;
	
	// Used for random restarts
    protected final Random rnd;
	
	// Constructor(s)
	public BayesNetManager( final Settings _processData ) throws Exception {

        BANJO.APPLICATIONSTATUS = BANJO.APPLICATIONSTATUS_LOADINGDATA; 
            
		boolean isCyclic;
		
		// Need a temporary variable to validate several core "final" variables
		int tmpValidationVar;
		StringTokenizer tokenizer;
		int tokenCount = 0;
		
        // Set the reference to the global settings
		processData = _processData;
        
        rnd = processData.getRandomSequence();
        
        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            BANJO.FEEDBACK_NEWLINE + BANJO.DATA_TRACEMEMORYBNMSTART )) );
        }
		
	    restartWithRandomNetwork = processData.getValidatedProcessParameter( 
		        BANJO.SETTING_RESTARTWITHRANDOMNETWORK ).
		        equalsIgnoreCase( BANJO.UI_RESTARTWITHRANDOMNETWORK_YES );
        
		// Note: All 'settings' have been validated, and can be assumed to be of
		// the correct data type
		varCount = Integer.parseInt( processData.getValidatedProcessParameter(
		        BANJO.SETTING_VARCOUNT ) );
				
		minMarkovLag = Integer.parseInt( processData.getValidatedProcessParameter(
		        BANJO.SETTING_MINMARKOVLAG ) );
		
		maxMarkovLag = Integer.parseInt( processData.getValidatedProcessParameter(
		        BANJO.SETTING_MAXMARKOVLAG ) );

        validateRequiredData();
        
        configCycleCheckerMethod = processData.getDynamicProcessParameter( 
                BANJO.CONFIG_CYCLECHECKER_METHOD );
        
        if ( configCycleCheckerMethod.equalsIgnoreCase( BANJO.DATA_CYCLECHECKING_DFS ) || 
                configCycleCheckerMethod.equalsIgnoreCase( BANJO.DATA_CYCLECHECKING_SHMUELI ) ) {
            
            processData.setDynamicProcessParameter( BANJO.CONFIG_CYCLECHECKER_ACTIONORDER,
                    BANJO.DATA_CYCLECHECKER_CHECKTHENAPPLY );
            checkThenApply = true;
        }
        else if ( configCycleCheckerMethod.equalsIgnoreCase( BANJO.DATA_CYCLECHECKING_DFSORIG ) ) {
            
            processData.setDynamicProcessParameter( BANJO.CONFIG_CYCLECHECKER_ACTIONORDER,
                    BANJO.DATA_CYCLECHECKER_APPLYTHENCHECK );
            checkThenApply = false;
        }
        
        if ( configCycleCheckerMethod.equalsIgnoreCase( BANJO.DATA_CYCLECHECKING_SHMUELI ) ) {
            
            cycleCheckingNeedsAdjustment = true;
        }
        else {
            
            cycleCheckingNeedsAdjustment = false;
        }
        
		if ( restartWithRandomNetwork ) {
			
		    maxParentCountForRestart = Integer.parseInt( 
			        processData.getValidatedProcessParameter(
			        BANJO.SETTING_MAXPARENTCOUNTFORRESTART ) );
		}
		else {
		    
		    maxParentCountForRestart = 0;
		}

        // Since the next setting is entirely optional, we handle it a little differently
        strDbnMandatoryIdentityLags = processData.getValidatedProcessParameter(
                BANJO.SETTING_DBNMANDATORYIDENTITYLAGS );
        
        // Note: the validation of the mandatory lags is now being handled at the original 
        // validation spot in the searcher
        if ( !strDbnMandatoryIdentityLags.equals( BANJO.BANJO_NOVALUESUPPLIED_STRING ) ) {
	
		    // Load the optional list of mandatory parents for a dbn, going to a node 
		    // from the same node in a previous time slice
		    if ( strDbnMandatoryIdentityLags != null && 
		            maxMarkovLag > 0 && strDbnMandatoryIdentityLags.length() > 0 ) {
		        
				tokenizer = 
			        new StringTokenizer( strDbnMandatoryIdentityLags, 
			                BANJO.DELIMITER_DEFAULT_LIST );
				
				tokenCount = tokenizer.countTokens();
			    dbnMandatoryIdentityLags = new int[tokenCount];
			    dbnMandatoryIdentityLags[0] = -1;

				for (int i=0; i<tokenCount; i++) {
				    
					try {
					
						tmpValidationVar = Integer.parseInt( tokenizer.nextToken().trim() );
					}		
					catch (Exception e) {
						
					    // If the loaded value is not an integer, tell the user
						throw new BanjoException( e, 
						        BANJO.ERROR_BANJO_USERINPUT,
						        "A 'dbnMandatoryIdentityLags' value is not an integer" +
						        "(" + strDbnMandatoryIdentityLags + ").");
					}
					if ( tmpValidationVar > 0 && 
					        tmpValidationVar >= minMarkovLag && 
					        tmpValidationVar <= maxMarkovLag ) {
					    dbnMandatoryIdentityLags[i] = tmpValidationVar;
					}
					else {
						
						BanjoError errorItem = new BanjoError( 

						        "\n   A 'dbnMandatoryIdentityLags' (= '" + 
						        strDbnMandatoryIdentityLags +
						        "') value is either a non-positive integer," +
						        "\n   or is outside the interval of " +
						        "[minMarkovLag,maxMarkovLag]=[" + 
						        minMarkovLag + "," + maxMarkovLag + "].",
					            BANJO.ERRORTYPE_MISMATCHEDDATATYPE, 
					            "dbnMandatoryIdentityLags",
						        "\n   A 'dbnMandatoryIdentityLags' (= '" + 
						        strDbnMandatoryIdentityLags +
						        "') value is either a non-positive integer," +
						        "\n   or is outside the interval of " +
						        "[minMarkovLag,maxMarkovLag]=[" + 
						        minMarkovLag + "," + maxMarkovLag + "]." );
		
						processData.addToErrors( errorItem );
					}
				}
		    }
	    }
	    else {
	        
		    dbnMandatoryIdentityLags = new int[1];
		    dbnMandatoryIdentityLags[0] = -1;
		    tokenCount = 0;
	    }
		
		// Get the file names for the various parent matrices from the 
	    // initialSettings. If none are specified, we will use default 
	    // matrices (initialized with 0) below.
		String loadDirectory = processData.getValidatedProcessParameter( 
		        BANJO.SETTING_INPUTDIRECTORY );
		String mandatoryNodesFile = processData.getValidatedProcessParameter( 
		        BANJO.SETTING_MUSTBEPRESENTEDGESFILE );
		String disallowedNodesFile = processData.getValidatedProcessParameter( 
		        BANJO.SETTING_MUSTNOTBEPRESENTEDGESFILE );
		String initialNodesFile = processData.getValidatedProcessParameter( 
		        BANJO.SETTING_INITIALSTRUCTUREFILE );
		
        if ( BANJO.CONFIG_PARENTSETS.equalsIgnoreCase( 
                BANJO.UI_PARENTSETSASARRAYS ) ) {
            
            // Load and check the set of mandatory parents
            if ( mandatoryNodesFile.length() < 1 ) {
                
                mustBePresentParents = new EdgesAsArrayWithCachedStatistics(
                        varCount, minMarkovLag, maxMarkovLag, processData );
            }
            else {
                
                mustBePresentParents = new EdgesAsArrayWithCachedStatistics( 
                        varCount, minMarkovLag, maxMarkovLag,
                        loadDirectory, mandatoryNodesFile, processData,
                        BANJO.DATA_CYCLECHECKING_CHECKFORCYCLES );
            
                processData.setDynamicProcessParameter( BANJO.DATA_MUSTBEPRESENTPARENTS,
                        mustBePresentParents.toString() );
            }

            // Now that we have the mandatory parents loaded, apply the (optional)
            // assignment of mandatory parents via the dbnMandatoryIdentityLags
            if ( maxMarkovLag > 0 && tokenCount > 0 ) { 
                
                for (int i=0; i<tokenCount; i++) { 
                
                    if ( dbnMandatoryIdentityLags[i] >= minMarkovLag && 
                            dbnMandatoryIdentityLags[i] <= maxMarkovLag ) {
                        
                        for (int j=0; j<varCount; j++) {
                            
                            // Indicate that a node j is dependent on node j at a previous
                            // time slice, namely dbnMandatoryIdentityLags[i]
                            mustBePresentParents.setEntry(j,j,
                                    dbnMandatoryIdentityLags[i]);
                        }
                    }
                }
                    
                if (BANJO.DEBUG && BANJO.TRACE_BAYESNETMANAGER ) {
                    
                    System.out.println( "Mandatory matrix with identity for lag " + 
                            dbnMandatoryIdentityLags[0] + ":" );
                    System.out.println( 
                            mustBePresentParents.toStringWithIDandParentCount() );
                }
            }
            
            if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

                System.out.println(
                        new StringBuffer( StringUtil.compileMemoryInfo( 
                                BANJO.FEEDBACK_NEWLINE + "  After adding mustBePresentParents" )) );
            }
                    
            if ( minMarkovLag == 0 ) {
                
                isCyclic = mustBePresentParents.isCyclic();
                if ( isCyclic ) { 
                    
                    int cycleAtNode = mustBePresentParents.getCycleAtNode();
                    
                    throw new BanjoException(
                            BANJO.ERROR_BANJO_USERINPUT, 
                            "\nThe must-be-present edges file '" 
                            + mandatoryNodesFile +
                            "' contains a cycle at node " + cycleAtNode +
                            ".");
                }
            }
            
            // Load the set of disallowed parents
            if (disallowedNodesFile.length() < 1 ) {
                
                mustBeAbsentParents = new EdgesAsArrayWithCachedStatistics(
                        varCount, minMarkovLag, maxMarkovLag, processData );
            }
            else {
                
                // note the different constructor, because we don't want to apply
                // much checking for the "node sets" of must-be-absent-parents
                mustBeAbsentParents = new EdgesAsArrayWithCachedStatistics( 
                        varCount, minMarkovLag, maxMarkovLag,
                        loadDirectory, disallowedNodesFile, processData,
                        BANJO.DATA_CYCLECHECKING_TREATASPLAINNODES );
                
                processData.setDynamicProcessParameter( BANJO.DATA_MUSTBEABSENTPARENTS,
                        mustBeAbsentParents.toString() );
            }
            
            if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

                System.out.println(
                        new StringBuffer( StringUtil.compileMemoryInfo( 
                                BANJO.FEEDBACK_NEWLINE + "  After adding mustBeAbsentParents" )) );
            }
            
            // This method makes sure that no node is available to be a parent to itself:
            if ( minMarkovLag == 0 ) {
            
                mustBeAbsentParents.omitNodesAsOwnParents();
            }
            // This method is only needed in our original (wasteful) parent set implementation
            mustBeAbsentParents.omitExcludedParents();
    
            // Sanity-check: the mustBeAbsentParents and mustBePresentParents cannot overlap
            if ( mustBePresentParents.hasOverlap( mustBeAbsentParents ) ) {
                
                throw new BanjoException(
                        BANJO.ERROR_BANJO_USERINPUT, 
                        "\nInconsistent input: The 'must be present' and 'must be absent' edges " + 
                        "cannot overlap. Banjo cannot continue execution.");
            }
    
            
            if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {
    
                System.out.println(
                        new StringBuffer( StringUtil.compileMemoryInfo(
                                  "  Before adding initialParents " )) );
            }
            
            // Now load the initial parent configuration:
            if ( initialNodesFile.length() < 1 ) {
                
                // If no file is specified, we set the initial parents to the empty network
                initialParents = new EdgesAsArrayWithCachedStatistics(
                        varCount, minMarkovLag, maxMarkovLag, processData );
            }
            else {
                
                initialParents = new EdgesAsArrayWithCachedStatistics( 
                        varCount, minMarkovLag, maxMarkovLag,
                        loadDirectory, initialNodesFile, processData,
                        BANJO.DATA_CYCLECHECKING_CHECKFORCYCLES );
                            
                processData.setDynamicProcessParameter( BANJO.DATA_INITIALSTRUCTURE,
                        initialParents.toString() );
                
                // Make sure that the user supplied file doesn't contain a cycle
                if ( minMarkovLag == 0 ) {
                    
                    isCyclic = initialParents.isCyclic();
                    
                    if ( isCyclic ) {
                        
                        int cycleAtNode = initialParents.getCycleAtNode();
                        
                        throw new BanjoException(
                                BANJO.ERROR_BANJO_USERINPUT, 
                                "\nInitial parents file '" 
                                + initialNodesFile +
                                "' contains a cycle at node " + cycleAtNode +
                                ".");
                    }
                }
            }
            
            if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

                System.out.println(
                        new StringBuffer( StringUtil.compileMemoryInfo( 
                                BANJO.FEEDBACK_NEWLINE + "  After adding initialParents" )) );
            }
    
    
            // The initial parents need to include the mandatory parents:
            initialParents.setToCombinedMatrices( 
                    initialParents, mustBePresentParents );
                    
            // Create the various parent sets
            deleteableParents = new EdgesAsArrayWithCachedStatistics(
                    varCount, minMarkovLag, maxMarkovLag, processData );
            addableParents = new EdgesAsArrayWithCachedStatistics(
                    varCount, minMarkovLag, maxMarkovLag, processData );
            currentParents = new EdgesAsArrayWithCachedStatistics(
                    varCount, minMarkovLag, maxMarkovLag, processData );
            
            if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

                System.out.println(
                        new StringBuffer( StringUtil.compileMemoryInfo( 
                                BANJO.FEEDBACK_NEWLINE + "  After adding 3 more parents set" )) );
            }
        }        
        // Original (matrix-based) parent sets implementation
        // Note: this code branch will disappear when the edgesAsMatrix code
        // gets deprecated in an upcoming maintenance release
        else {
                   
            // Load and check the set of mandatory parents
            if ( mandatoryNodesFile.length() < 1 ) {
                
                mustBePresentParents = new EdgesAsMatrixWithCachedStatistics(
                        varCount, minMarkovLag, maxMarkovLag );
            }
            else {
                
                mustBePresentParents = new EdgesAsMatrixWithCachedStatistics( 
                        varCount, minMarkovLag, maxMarkovLag,
                        loadDirectory, mandatoryNodesFile, processData );
            
                processData.setDynamicProcessParameter( 
                        BANJO.DATA_MUSTBEPRESENTPARENTS, mustBePresentParents.toString() );
            }
    		
    		// Now that we have the mandatory parents loaded, apply the (optional)
    		// assignment of mandatory parents via the dbnMandatoryIdentityLags
    		if ( maxMarkovLag > 0 && tokenCount > 0 ) { 
    		    
    		    for (int i=0; i<tokenCount; i++) { 
    		    
    		        if ( dbnMandatoryIdentityLags[i] >= minMarkovLag && 
    		                dbnMandatoryIdentityLags[i] <= maxMarkovLag ) {
    		        	
    				    for (int j=0; j<varCount; j++) {
    				        
    				        // Indicate that a node j is dependent on node j at a previous
    				        // time slice, namely dbnMandatoryIdentityLags[i]
    					    mustBePresentParents.setEntry(j,j,
    					            dbnMandatoryIdentityLags[i]);
    				    }
    		        }
    		    }
    			    
    		    if (BANJO.DEBUG && BANJO.TRACE_BAYESNETMANAGER ) {
    		        
    		        System.out.println( "Mandatory matrix with identity for lag " + 
    		                dbnMandatoryIdentityLags[0] + ":" );
    		        System.out.println( 
    		                mustBePresentParents.toStringWithIDandParentCount() );
    		    }
    		}

            if ( minMarkovLag == 0 ) {
            
        		isCyclic = mustBePresentParents.isCyclic();
        		if ( isCyclic ) { 
        		    
        			throw new BanjoException(
        			        BANJO.ERROR_BANJO_USERINPUT, 
        			        "\nThe must-be-present edges file '" 
        			        + mandatoryNodesFile +
        			        "' contains a cycle.");
        		}
            }
    		
    		// Load the set of disallowed parents
    		if (disallowedNodesFile.length() < 1 ) {
    			
    			mustBeAbsentParents = new EdgesAsMatrixWithCachedStatistics(
    			        varCount, minMarkovLag, maxMarkovLag );
    		}
    		else {
    			
    		    // note the different constructor, because we don't want to apply
    		    // much checking for the "node sets" of must-be-absent-parents
    			mustBeAbsentParents = new EdgesAsMatrixWithCachedStatistics( 
    			        varCount, minMarkovLag, maxMarkovLag,
    					loadDirectory, disallowedNodesFile );
    			
    			processData.setDynamicProcessParameter( BANJO.DATA_MUSTBEABSENTPARENTS,
    			        mustBeAbsentParents.toString() );
    		}
    		
    		// This method makes sure that no node is available to be a parent to itself:
    		mustBeAbsentParents.omitNodesAsOwnParents();
    		mustBeAbsentParents.omitExcludedParents();
    
    		// Sanity-check: the mustBeAbsentParents and mustBePresentParents cannot overlap
    		if ( mustBePresentParents.hasOverlap( mustBeAbsentParents ) ) {
    		    
    			throw new BanjoException(
    			        BANJO.ERROR_BANJO_USERINPUT, 
    			        "\nInconsistent input: The 'must be present' and 'must be absent' edges " + 
    			        "cannot overlap. Banjo cannot continue execution.");
    		}
    
            
            if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {
    
                System.out.println(
                        new StringBuffer( StringUtil.compileMemoryInfo( 
                                BANJO.FEEDBACK_NEWLINE + BANJO.DATA_TRACEMEMORYBNMBEFOREINITIALSTRUCT )) );
            }
            
    		// Now load the initial parent configuration:
    		if (initialNodesFile.length() < 1 ) {
    			
    		    // If no file is specified, we set the initial parents to the empty network
    			initialParents = new EdgesAsMatrixWithCachedStatistics(
    			        varCount, minMarkovLag, maxMarkovLag );
    		}
    		else {
    			
    			initialParents = new EdgesAsMatrixWithCachedStatistics( 
    			        varCount, minMarkovLag, maxMarkovLag,
    					loadDirectory, initialNodesFile, processData );
    						
    			processData.setDynamicProcessParameter( BANJO.DATA_INITIALSTRUCTURE,
    			        initialParents.toString() );
    			
    			// Make sure that the user supplied file doesn't contain a cycle
                if ( minMarkovLag == 0 ) {
                        
        			isCyclic = initialParents.isCyclic();
        			if ( isCyclic ) { 
        			    
        				throw new BanjoException(
        				        BANJO.ERROR_BANJO_USERINPUT, 
        				        "\nInitial parents file '" 
        				        + initialNodesFile +
        				        "' contains a cycle.");
        			}
                }
    		}
    
    
    	    // The initial parents need to include the mandatory parents:
    		initialParents.setToCombinedMatrices( 
    	            initialParents, mustBePresentParents );
    				
    		// Create the various parent sets
    		deleteableParents = new EdgesAsMatrixWithCachedStatistics(
    		        varCount, minMarkovLag, maxMarkovLag );
    		addableParents = new EdgesAsMatrixWithCachedStatistics(
    		        varCount, minMarkovLag, maxMarkovLag );
    		currentParents = new EdgesAsMatrixWithCachedStatistics(
    		        varCount, minMarkovLag, maxMarkovLag );

        }
        
		// Now compute the initial parent sets of the bayesnet
		// Note that a "cleaned" version of the initialParents will be cached
		// in the cachedInitialParents container after this call
		initializeBayesNet( initialParents );
        
        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            BANJO.FEEDBACK_NEWLINE + 
                            BANJO.DATA_TRACEMEMORYBNMBEFORENODECACHE )) );
        }
        
        BANJO.APPLICATIONSTATUS = BANJO.APPLICATIONSTATUS_STARTUPCOMPLETED;
	}

	/**
	 * Validates the settings values required for the BayesNetManager (i.e., and 
	 * special requirements on edges, and the optional initial structure).
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
        Set validValues = new HashSet();
		
	    // Validate the 'must be present edges file'
	    settingNameCanonical = BANJO.SETTING_MUSTBEPRESENTEDGESFILE;
	    settingNameDescriptive = BANJO.SETTING_MUSTBEPRESENTEDGESFILE_DESCR;
	    settingNameForDisplay = BANJO.SETTING_MUSTBEPRESENTEDGESFILE_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            BANJO.BANJO_FREEFORMINPUT, 
	            "" );	    
	    
	    // Validate the 'must be absent edges file'
	    settingNameCanonical = BANJO.SETTING_MUSTNOTBEPRESENTEDGESFILE;
	    settingNameDescriptive = BANJO.SETTING_MUSTNOTBEPRESENTEDGESFILE_DESCR;
	    settingNameForDisplay = BANJO.SETTING_MUSTNOTBEPRESENTEDGESFILE_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive, 
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            BANJO.BANJO_FREEFORMINPUT, 
	            "" );	    
	    
	    // Validate the 'initial structure file'
	    settingNameCanonical = BANJO.SETTING_INITIALSTRUCTUREFILE;
	    settingNameDescriptive = BANJO.SETTING_INITIALSTRUCTUREFILE_DESCR;
	    settingNameForDisplay = BANJO.SETTING_INITIALSTRUCTUREFILE_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive, 
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            BANJO.BANJO_FREEFORMINPUT, 
	            "" );
        
        // Validate the 'display memory info' flag
        settingNameCanonical = BANJO.SETTING_DISPLAYSTRUCTURES;
        settingNameDescriptive = BANJO.SETTING_DISPLAYSTRUCTURES_DESCR;
        settingNameForDisplay = BANJO.SETTING_DISPLAYSTRUCTURES_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
        validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        validValues.clear();
        validValues.add( BANJO.UI_DISPLAYSTRUCTURES_YES );
        validValues.add( BANJO.UI_DISPLAYSTRUCTURES_NO );
        settingItem = processData.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                validValues, 
                BANJO.DEFAULT_DISPLAYSTRUCTURES );

        // Validate the cycle checker (need to do it here, so we will have access
        // to the info when we create our parent set objects)
        settingNameCanonical = BANJO.SETTING_CYCLECHECKERCHOICE;
        settingNameDescriptive = BANJO.SETTING_CYCLECHECKERCHOICE_DESCR;
        settingNameForDisplay = BANJO.SETTING_CYCLECHECKERCHOICE_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
        validationType = BANJO.VALIDATIONTYPE_MANDATORY;
        settingItem = processData.processSetting( settingNameCanonical, 
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                this.validChoices(),
                BANJO.UI_DEFAULT );

        String tmpConfigCycleCheckerMethod;
        if ( processData.getValidatedProcessParameter( 
                BANJO.SETTING_CYCLECHECKERCHOICE )
                .equalsIgnoreCase( BANJO.UI_CYCLECHECKER_DFS )) { 

            tmpConfigCycleCheckerMethod = BANJO.DATA_CYCLECHECKING_DFS;
        }
        else if ( processData.getValidatedProcessParameter( 
                BANJO.SETTING_CYCLECHECKERCHOICE )
                .equalsIgnoreCase( BANJO.UI_CYCLECHECKER_DFSWITHSHMUELI )) {

            tmpConfigCycleCheckerMethod = BANJO.DATA_CYCLECHECKING_SHMUELI;
        }
        else if ( processData.getValidatedProcessParameter( 
                BANJO.SETTING_CYCLECHECKERCHOICE )
                .equalsIgnoreCase( BANJO.UI_CYCLECHECKER_DFSORIG )) { 

            tmpConfigCycleCheckerMethod = BANJO.DATA_CYCLECHECKING_DFSORIG;
        }
        else if ( processData.getValidatedProcessParameter( 
                BANJO.SETTING_CYCLECHECKERCHOICE )
                .equalsIgnoreCase( BANJO.UI_CYCLECHECKER_BFS )) { 

            tmpConfigCycleCheckerMethod = BANJO.DATA_CYCLECHECKING_BFS;
        }
        else {

            // default choice
            tmpConfigCycleCheckerMethod = BANJO.DATA_CYCLECHECKING_SHMUELI;
        }
        
        processData.setDynamicProcessParameter( 
                BANJO.CONFIG_CYCLECHECKER_METHOD, tmpConfigCycleCheckerMethod );
        
	    return isDataValid;
	}
    
    /**
     * Provides the valid choices for this class, here: the available cycle checking methods.
     */
    public Object validChoices() {
        
        Set validValues = new HashSet();
        
        validValues.add( BANJO.UI_DEFAULT );
        
        // Cycle-checker options
        validValues.add( BANJO.UI_CYCLECHECKER_DFS );
        validValues.add( BANJO.UI_CYCLECHECKER_DFSWITHSHMUELI );
        validValues.add( BANJO.UI_CYCLECHECKER_DFSORIG );
        validValues.add( BANJO.UI_CYCLECHECKER_BFS );
        
        return validValues;
    }

	public void initializeBayesNet() throws Exception {
	            
		// Start with the standard procedure for creating an initial network,
	    // using the last cached parents as the starting set of parents	
        initializeBayesNet( initialParents );
        
        // .prepareForInitializing
        currentParents.resetEntireGraph();
		
		// Get ready to apply a random "perturbation" of the edge configuration
		if ( restartWithRandomNetwork ) {
		    
	        BayesNetChangeI bayesNetChange = new BayesNetChange();
			
			int randomParentID;
			int randomParentLag;
			int randomParentIndex;
			boolean isCyclic;

			int randomParentCount;
			int addableParentCount;
			int currentParentCount;
			int[][] parentList;
					
			// Assign a random number (limited by maxParentCountForRestart) of parents
			// to each node
			for (int nodeID=0; nodeID<varCount; nodeID++) {
					    
			    // See how many parents we can potentially add for nodeID
			    addableParentCount = addableParents.getParentCount( nodeID );
			    // Check how many parents the node already has (from dbnMandatoryLags
			    // or mustBePresent edges)
			    currentParentCount = currentParents.getParentCount( nodeID );
			    // Pick a random number for the number of parents to add, taking
			    // the currentParents and the maxParent limit into account
			    if ( maxParentCountForRestart - currentParentCount > 0 ) {
			    
			        randomParentCount = rnd.nextInt( 
			            maxParentCountForRestart - currentParentCount ) + 1;
			    }
			    else {
			        
			        randomParentCount = 0;
			    }			        
			    
			    if ( randomParentCount > addableParentCount ) {
			        
			        randomParentCount = addableParentCount;
			    }
			    
			    if ( randomParentCount > 0 ) {
			     
				    // Get the list of parents for nodeID (lag defaults to 0)
				    parentList = addableParents.getCurrentParentIDlist( nodeID , 0 );
				    
				    for ( int j=0; j<randomParentCount; j++ ){
				        
				        // Pick a parent node at random from the list
				        // Note that won't repeat the process if we end up with a 
				        // cyclic graph. We try to find a valid parent with some effort, 
				        // though, by tracking which parents have already been used.
				        int i = 1;
				        do {
				            // Pick an index between  0 and addableParentCount-1:
				            randomParentIndex = rnd.nextInt( addableParentCount );
				            i++;
				        } while ( parentList[randomParentIndex][0] == -1 
				                && i < addableParentCount );
				        
				        if ( parentList[randomParentIndex][0] != -1 ) {
				            
				            randomParentID = parentList[randomParentIndex][0];
				            randomParentLag = parentList[randomParentIndex][1];
	
				            // now flag the already selected parent so it won't get
				            // used in the future
				            parentList[randomParentIndex][0] = -1;
			            	                
				            // Now change the network
						    bayesNetChange.updateChange( 
						            nodeID, randomParentID, randomParentLag, 
						            BANJO.CHANGETYPE_ADDITION );
						    
                            if ( checkThenApply ) {
                                
                                // Note: this change will only work with the any cycle finder
                                // where we apply the change AFTER we check (and thus will NOT
                                // work at all with the older EdgesAsMatrix code)
                                
                                if ( minMarkovLag == 0 && randomParentLag == 0 ) {
                                    
        							isCyclic = currentParents.isCyclic( bayesNetChange );
        							if ( !isCyclic ) this.applyChange( bayesNetChange );
                                }
                            }
                            else {
                                
                                // "Apply-check-undo" as implemented in the version 1.0 cycle checking
                                this.applyChange( bayesNetChange );
                                
                                if ( minMarkovLag == 0 ) {

                                    isCyclic = currentParents.isCyclic( 
                                            bayesNetChange );
                                    if ( isCyclic ) this.undoChange( bayesNetChange );
                                }
                            }
				        }
				    }
			    }
			}
	    }		
	}
	
	// This method is designed to enable restarts with different initial parent data,
	// e.g., when an already found "good" network is to be used for the restart
	public void initializeBayesNet( 
	        final EdgesWithCachedStatisticsI parentsToAssign ) 
			throws Exception {
		        
        // Save memory by removing all cached parent sets

        initialParents.setToCombinedMatrices( parentsToAssign, mustBePresentParents );
        // .. but cannot include any mustBeAbsentParents
        initialParents.subtractMatrix( mustBeAbsentParents );
          
        // The initial parents (which we'll cache in cachedInitialParents) need to
        // include the mustBePresentParents
        addableParents.computeComplementaryMatrix( 
                initialParents, mustBePresentParents );
        // .. but cannot include any mustBeAbsentParents
        addableParents.subtractMatrix( mustBeAbsentParents );
        
        // By now, the cachedInitialParents is the "cleaned" set of parents
                
        // The deleteableParents are based on the cachedInitialParents, but cannot
        // contain any mustBePresentParents
        deleteableParents.assignMatrix( initialParents );
        deleteableParents.subtractMatrix( mustBePresentParents );
        
        // The current set of nodes is derived from all deleteable plus the 
        // mandatory edges
        currentParents.setToCombinedMatrices( 
                deleteableParents, mustBePresentParents );
        
	}
		
	// ApplyChange works in concerto with UndoChange:
	// Whenever a BayesNetChange is suggested by the Proposer, ApplyChange provides
	// an updated BayesNet for a) checking for cycles, and b) computing the score
	// in the Evaluator. If the BayesNetChange is not kept, the UndoChange method
	// below will revert the applied change, and restore the network.
	public void applyChange( final BayesNetChangeI bayesNetChange ) 
			throws Exception {
		
	    int currentNodeID = bayesNetChange.getCurrentNodeID();
	    int parentNodeID = bayesNetChange.getParentNodeID();
	    int parentNodeLag = bayesNetChange.getParentNodeLag();
	    			
		try {
			if ( bayesNetChange.getChangeStatus() 
			        != BANJO.CHANGESTATUS_READY ) {
				
				// Since this case should never happen, we throw an exception
			    // if it does
			    throw new BanjoException( 
			            BANJO.ERROR_BANJO_DEV,
			            "(BayesNetManager.applyChange) " +
			            "Development issue: " +
			            "Can only apply a BayesNetChange with READY status, " +
			            "\nbut encountered status value = '" +
						bayesNetChange.getChangeStatus() + "'.");
			}
				
			// 
			switch ( bayesNetChange.getChangeType() ){
			// NOTE: At this point, the validity of the change will have been checked
			// so we can apply it without further verification
			case BANJO.CHANGETYPE_ADDITION:
				
				addableParents.deleteParent( 
				        currentNodeID, parentNodeID, parentNodeLag );
				deleteableParents.addParent( 
				        currentNodeID, parentNodeID, parentNodeLag );
				
				// hjs 6/29/04 Update current parents set also 
				// (solves performance issue!)
				currentParents.addParent( 
				        currentNodeID, parentNodeID, parentNodeLag );
				break;
				
			case BANJO.CHANGETYPE_DELETION:
				
				addableParents.addParent( 
				        currentNodeID, parentNodeID, parentNodeLag );
				deleteableParents.deleteParent( 
				        currentNodeID, parentNodeID, parentNodeLag );
				
				// hjs 6/29/04 Update current parents set also 
				// (solves performance issue!)
				currentParents.deleteParent( 
				        currentNodeID, parentNodeID, parentNodeLag );
				break;
				
			case BANJO.CHANGETYPE_REVERSAL:
				
				// The lag of the parent node must be 0 (i.e., the same as for the
				// current node)
			
				// First do the deletion ..
				addableParents.addParent( currentNodeID, parentNodeID, 0 );
				deleteableParents.deleteParent( currentNodeID, parentNodeID, 0 );
				// hjs 6/29/04
				currentParents.deleteParent( currentNodeID, parentNodeID, 0 );
				
				// .. then the addition
				addableParents.deleteParent( parentNodeID, currentNodeID, 0 );
				deleteableParents.addParent( parentNodeID, currentNodeID, 0 );
				// hjs 6/29/04
				currentParents.addParent( parentNodeID, currentNodeID, 0 );
								
				break;
				
			default:
				
			    // Since this case should never happen, we throw an exception 
			    // if it does
				throw new BanjoException(
				        BANJO.ERROR_BANJO_DEV, 
				        "(BayesNetManager.applyChange) " +
			            "Development issue: " +
				        "Can not continue due to invalid BayesNetChange " +
				        "type.\n(Encountered type value = " + 
						bayesNetChange.getChangeType() + ")." );
			}
			
			// Update the change status
			bayesNetChange.setChangeStatus( BANJO.CHANGESTATUS_APPLIED );
		}
		catch (BanjoException e) {
		    
		    throw new BanjoException( e );
		}
		catch (Exception e) {

		    throw new BanjoException( e, 
		            BANJO.ERROR_BANJO_DEV, 
		            "(BayesNetManager.applyChange) Development issue: " + e.getMessage() );
		}
	}
	
	// UndoChange works in concerto with the ApplyChange method. Whenever a
	// suggested BayesNetChange is rejected by the Decider (or if it results
	// in a cycle), UndoChange reverts the bayesNetManager back to the previous 
	// state.
	public void undoChange( final BayesNetChangeI _bayesNetChange ) throws Exception {
        
	    try {
            
            // for testing only, to cause an exception:
//            _bayesNetChange.setChangeStatus( BANJO.CHANGESTATUS_APPLIED - 1 );
            
			if ( _bayesNetChange.getChangeStatus() != 
			    		BANJO.CHANGESTATUS_APPLIED ) {
				
			    throw new BanjoException( 
			            BANJO.ERROR_BANJO_DEV,
		        	"(BayesNetManager.undoChange) " +
		            "Development issue: " +
		        	"Search stopped due to invalid BayesNetChange " +
		        	"status. (Encountered status = " + 
					_bayesNetChange.getChangeStatus() + ").");
			}
			
		    final int currentNodeID = _bayesNetChange.getCurrentNodeID();
		    final int parentNodeID = _bayesNetChange.getParentNodeID();
		    final int parentNodeLag = _bayesNetChange.getParentNodeLag();
            final int changeType = _bayesNetChange.getChangeType();
	
		    // Set bayesNetChange status back to READY:
		    _bayesNetChange.setChangeStatus( BANJO.CHANGESTATUS_READY );
		    
		    switch ( changeType ){
			case BANJO.CHANGETYPE_ADDITION:
				
			    addableParents.addParent( 
			            currentNodeID, parentNodeID, parentNodeLag );
				deleteableParents.deleteParent(
				        currentNodeID, parentNodeID, parentNodeLag );
				currentParents.deleteParent( 
				        currentNodeID, parentNodeID, parentNodeLag );
				break;
				
			case BANJO.CHANGETYPE_DELETION:
				
			    addableParents.deleteParent(
			            currentNodeID, parentNodeID, parentNodeLag );
				deleteableParents.addParent(
			            currentNodeID, parentNodeID, parentNodeLag );
				currentParents.addParent( 
				        currentNodeID, parentNodeID, parentNodeLag );
				break;
				
			case BANJO.CHANGETYPE_REVERSAL:
				
			    // first UNDO the deletion
			    addableParents.deleteParent(
			            currentNodeID, parentNodeID, 0 );
				deleteableParents.addParent(
				        currentNodeID, parentNodeID, 0 );
				currentParents.addParent( 
				        currentNodeID, parentNodeID, 0 );
				// then UNDO the addition
			    addableParents.addParent(
			            parentNodeID, currentNodeID, 0 );
				deleteableParents.deleteParent(
				        parentNodeID, currentNodeID, 0 );
				currentParents.deleteParent( 
				        parentNodeID, currentNodeID, 0 );
                    
                if ( BANJO.DEBUG && BANJO.CONFIG_DISPLAYPROGRESS ) {
                
                    processData.writeTraceToFile( "    Decider rejected, special adjust for Reversal " +
                            _bayesNetChange.toString(),
                            true, BANJO.CONFIG_TRACEFILE_1 );
                }
                
                if ( !BANJO.CONFIG_OMITREVERSALS && cycleCheckingNeedsAdjustment ) {
                    
                    // Adjustment for Shmueli approach (handled by the parent set implementations)
                    // Note: for "non-Shmueli", this will be just an empty stub
                    currentParents.adjustForLocalChange( parentNodeID );
                }
				break;
				
			default:
				
				// Since this case should never happen, we pull the emergency brake
				throw new BanjoException(
				        BANJO.ERROR_BANJO_DEV, 
				        "(BayesNetManager.undoChange) " +
			            "Development issue: " +
				        "Search stopeed due to invalid BayesNetChange " +
				        "type.\n(Encountered type = " + 
						_bayesNetChange.getChangeType() + ").");
			}
			
			// Update the change status
		    // hjs 12/22/04 Instead of setting the status to 'undone', reset it to
		    // 'ready' (needed for global search, where the apply/undo changes)
			_bayesNetChange.setChangeStatus( BANJO.CHANGESTATUS_READY );
		}
		catch (BanjoException e) {
		    
		    throw new BanjoException( e );
		}
		catch (Exception e) {
		    
		    throw new BanjoException( 
		            e, BANJO.ERROR_BANJO_DEV,
		            "(BayesNetManager.undoChange) Development issue: " + e.getMessage() );
		}
	}
	
	/**
	 * @return Returns the currentParents.
	 */
	public EdgesWithCachedStatisticsI getCurrentParents() {
		return this.currentParents;
	}
	
	/**
	 * @return Returns the addableParents.
	 */
	public EdgesWithCachedStatisticsI getAddableParents() {
		return this.addableParents;
	}

	/**
	 * @return Returns the deleteableParents.
	 */
	public EdgesWithCachedStatisticsI getDeleteableParents() {
		return this.deleteableParents;
	}

	/**
	 * @return Returns the mustBeAbsentParents.
	 */
	public EdgesWithCachedStatisticsI getMustBeAbsentParents() {
		return this.mustBeAbsentParents;
	}

	/**
	 * @return Returns the mustBePresentParents.
	 */
	public EdgesWithCachedStatisticsI getMustBePresentParents() {
		return this.mustBePresentParents;
	}
	
    /**
     * @return Returns the min Markov lag.
     */
    public int getMinMarkovLag() {
        return this.minMarkovLag;
    }
	/**
	 * @return Returns the max Markov lag.
	 */
	public final int getMaxMarkovLag() {
		return this.maxMarkovLag;
	}

	/**
	 * @return Returns the variable count.
	 */
	public final int getVarCount() {
		return this.varCount;
	}

	/**
	 * @return Returns the initialParents.
	 */
	public EdgesWithCachedStatisticsI getInitialParents() {
		return this.initialParents;
	}

    /**
     * @param currentParents The currentParents to set.
     */
    public void setCurrentParents( EdgesWithCachedStatisticsI currentParents ) 
    		throws Exception {

        this.currentParents.assignMatrix( currentParents );
    }
}
