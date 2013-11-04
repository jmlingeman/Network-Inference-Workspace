/*
 * Created on Mar 29, 2005
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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

import edu.duke.cs.banjo.bayesnet.*;
import edu.duke.cs.banjo.data.settings.SettingItem;
import edu.duke.cs.banjo.data.settings.Settings;

/**
 * Contains the (optional) post-processing actions.
 * <p>
 * Computation of a dot-compliant file for generating a graphical representation. <br>
 * Computation of the consensus graph from a set of high-scoring networks.
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Mar 29, 2005
 * 
 * <p>
 * 10/10/2005 (v2.0) hjs		
 * 		Add method for computing a consensus graph from a set of high-scoring networks.
 * 
 * <p>
 * 2008 (v2.1) hjs          Changes to update the use of FileUtil
 * <p>
 * 4/14/2009 hjs (v2.2.1)   Modify the handling of return values from externally executed apps.
 * 
 * @author hjs <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class PostProcessor {

	protected Settings settings;
	
	// Data that will be part of the underlying problem domain
	protected final int varCount;
	protected final int minMarkovLag;
	protected final int maxMarkovLag;

    // Stores the variable labels, if supplied by the user; otherwise defaults to variable
    // indexes
    protected String[] variableNames;
	
	public PostProcessor( Settings _processData ) throws Exception {

	    this.settings = _processData;

	    // Cache the data given by the problem
	    varCount = Integer.parseInt( 
		        settings.getValidatedProcessParameter( 
		                BANJO.SETTING_VARCOUNT ) );
		minMarkovLag = Integer.parseInt( 
		        settings.getValidatedProcessParameter( 
		                BANJO.SETTING_MINMARKOVLAG ) );
		maxMarkovLag = Integer.parseInt( 
		        settings.getValidatedProcessParameter( 
		                BANJO.SETTING_MAXMARKOVLAG ) );
		
		validateRequiredData();
	}

	/**
	 * Validates the settings values required for the post-processing.
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


	    // Validate the displayStatistics flag
	    // (This is not yet used: may want to use in Recorder classes?)
	    settingNameCanonical = BANJO.SETTING_DISPLAYSTATISTICS;
	    settingNameDescriptive = BANJO.SETTING_DISPLAYSTATISTICS_DESCR;
	    settingNameForDisplay = BANJO.SETTING_DISPLAYSTATISTICS_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        validValues.clear();
	    validValues.add( BANJO.UI_DISPLAYSTATISTICS_YES );
	    validValues.add( BANJO.UI_DISPLAYSTATISTICS_NO );
	    settingItem = settings.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            validValues, 
	            BANJO.DEFAULT_DISPLAYSTATISTICS );

	    // Validate the dot output flag
	    settingNameCanonical = BANJO.SETTING_CREATEDOTOUTPUT;
	    settingNameDescriptive = BANJO.SETTING_CREATEDOTOUTPUT_DESCR;
	    settingNameForDisplay = BANJO.SETTING_CREATEDOTOUTPUT_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        validValues.clear();
	    validValues.add( BANJO.UI_CREATEDOTOUTPUT_YES );
	    validValues.add( BANJO.UI_CREATEDOTOUTPUT_NO );
	    settingItem = settings.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            validValues, 
	            BANJO.DEFAULT_CREATEDOTOUTPUT );
        
	    // Validate the compute influence scores flag
	    settingNameCanonical = BANJO.SETTING_COMPUTEINFLUENCESCORES;
	    settingNameDescriptive = BANJO.SETTING_COMPUTEINFLUENCESCORES_DESCR;
	    settingNameForDisplay = BANJO.SETTING_COMPUTEINFLUENCESCORES_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        validValues.clear();
	    validValues.add( BANJO.UI_COMPUTECONSENSUSGRAPH_YES );
	    validValues.add( BANJO.UI_COMPUTECONSENSUSGRAPH_NO );
	    settingItem = settings.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            validValues, 
	            BANJO.DEFAULT_COMPUTEINFLUENCESCORES );

	    // Validate
	    settingNameCanonical = BANJO.SETTING_COMPUTECONSENSUSGRAPH;
	    settingNameDescriptive = BANJO.SETTING_COMPUTECONSENSUSGRAPH_DESCR;
	    settingNameForDisplay = BANJO.SETTING_COMPUTECONSENSUSGRAPH_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        validValues.clear();
	    validValues.add( BANJO.UI_COMPUTEINFLUENCESCORES_YES );
	    validValues.add( BANJO.UI_COMPUTEINFLUENCESCORES_NO );
	    settingItem = settings.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            validValues, 
	            BANJO.DEFAULT_COMPUTECONSENSUSGRAPH );
	    
	    // Validate
	    settingNameCanonical = BANJO.SETTING_DISPLAYCONSENSUSGRAPHASHTML;
	    settingNameDescriptive = BANJO.SETTING_DISPLAYCONSENSUSGRAPHASHTML_DESCR;
	    settingNameForDisplay = BANJO.SETTING_DISPLAYCONSENSUSGRAPHASHTML_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        validValues.clear();
	    validValues.add( BANJO.UI_DISPLAYCONSENSUSGRAPHASHTML_YES );
	    validValues.add( BANJO.UI_DISPLAYCONSENSUSGRAPHASHTML_NO );
	    settingItem = settings.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            validValues, 
	            BANJO.DEFAULT_DISPLAYCONSENSUSGRAPHASHTML );

	    // Validate the 'display memory info' flag
	    settingNameCanonical = BANJO.SETTING_DISPLAYMEMORYINFO;
	    settingNameDescriptive = BANJO.SETTING_DISPLAYMEMORYINFO_DESCR;
	    settingNameForDisplay = BANJO.SETTING_DISPLAYMEMORYINFO_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
        validValues.clear();
	    validValues.add( BANJO.UI_DISPLAYMEMORYINFO_YES );
	    validValues.add( BANJO.UI_DISPLAYMEMORYINFO_NO );
	    settingItem = settings.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            validValues, 
	            BANJO.DEFAULT_DISPLAYMEMORYINFO );
	    
	    // Validate:
	    settingNameCanonical = BANJO.SETTING_TIMESTAMPSTRINGFORFILES;
	    settingNameDescriptive = BANJO.SETTING_TIMESTAMPSTRINGFORFILES_DESCR;
	    settingNameForDisplay = BANJO.SETTING_TIMESTAMPSTRINGFORFILES_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_TIMESTAMP;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem = settings.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            BANJO.BANJO_FREEFORMINPUT, 
	            BANJO.DEFAULT_TIMESTAMPSTRINGFORFILES );
	    
        if ( settings.getValidatedProcessParameter( 
                BANJO.SETTING_CREATEDOTOUTPUT ).equals( BANJO.UI_CREATEDOTOUTPUT_YES ) ) {
	    
            // Validate:
    	    settingNameCanonical = BANJO.SETTING_FULLPATHTODOTEXECUTABLE;
    	    settingNameDescriptive = BANJO.SETTING_FULLPATHTODOTEXECUTABLE_DESCR;
    	    settingNameForDisplay = BANJO.SETTING_FULLPATHTODOTEXECUTABLE_DISP;
    	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
    	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
    	    settingItem = settings.processSetting( settingNameCanonical, 
    	            settingNameDescriptive,
    	            settingNameForDisplay,
    	            settingDataType,
    	            validationType,
    	            BANJO.BANJO_FREEFORMINPUT, 
    	            BANJO.DEFAULT_NODOTEXECUTABLESUPPPLIED );
            
            // TODO: validate the given path
    
            // Give the user a warning if the path to dot is not spcified, but there's
            // a request to create output using dot
            if ( settings.getValidatedProcessParameter( 
                        BANJO.SETTING_FULLPATHTODOTEXECUTABLE ).equals( 
                            BANJO.DEFAULT_NODOTEXECUTABLESUPPPLIED )
                || settings.getValidatedProcessParameter( 
                        BANJO.SETTING_FULLPATHTODOTEXECUTABLE ).equals( "" ) ) {
                
                settings.addToWarnings( new BanjoError( 
                        BANJO.ERRORDESCRIPTION_ALERT_MISSINGDOTLOCATION_INFO,
                        BANJO.ERRORTYPE_ALERT_MISSINGDOTLOCATION,
                        settingNameCanonical,
                        null ) );
            }
        }
        
	    // Validate:
	    settingNameCanonical = BANJO.SETTING_FILENAMEFORTOPGRAPH;
	    settingNameDescriptive = BANJO.SETTING_FILENAMEFORTOPGRAPH_DESCR;
	    settingNameForDisplay = BANJO.SETTING_FILENAMEFORTOPGRAPH_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem = settings.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            BANJO.BANJO_FREEFORMINPUT, 
	            BANJO.DEFAULT_FILENAMEFORTOPGRAPH );

	    // Validate:
	    settingNameCanonical = BANJO.SETTING_FILENAMEFORCONSENSUSGRAPH;
	    settingNameDescriptive = BANJO.SETTING_FILENAMEFORCONSENSUSGRAPH_DESCR;
	    settingNameForDisplay = BANJO.SETTING_FILENAMEFORCONSENSUSGRAPH_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem = settings.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            BANJO.BANJO_FREEFORMINPUT, 
	            BANJO.DEFAULT_FILENAMEFORCONSENSUSGRAPH );
        
	    // Validate:
	    settingNameCanonical = BANJO.SETTING_DOTFILEEXTENSION;
	    settingNameDescriptive = BANJO.SETTING_DOTFILEEXTENSION_DESCR;
	    settingNameForDisplay = BANJO.SETTING_DOTFILEEXTENSION_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem = settings.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            BANJO.BANJO_FREEFORMINPUT,
	            BANJO.DEFAULT_DOTFILEEXTENSION );

	    // Validate:
	    settingNameCanonical = BANJO.SETTING_HTMLFILEEXTENSION;
	    settingNameDescriptive = BANJO.SETTING_HTMLFILEEXTENSION_DESCR;
	    settingNameForDisplay = BANJO.SETTING_HTMLFILEEXTENSION_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem = settings.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            BANJO.BANJO_FREEFORMINPUT,
	            BANJO.DEFAULT_HTMLFILEEXTENSION );
	    
	    // Validate:
	    settingNameCanonical = BANJO.SETTING_DOTGRAPHICSFORMAT;
	    settingNameDescriptive = BANJO.SETTING_DOTGRAPHICSFORMAT_DESCR;
	    settingNameForDisplay = BANJO.SETTING_DOTGRAPHICSFORMAT_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;
	    settingItem = settings.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
                this.validChoices(), 
	            BANJO.DEFAULT_DOTGRAPHICSFORMAT );
        
        updateVariableNames();
        
        // For Banjo 2.0, there shouldn't be a need to return false here (i.e., we don't need
        // to trigger an alert to the calling routine that a crucial setting is invalid)
	    return isDataValid;
	}
    
    public void updateVariableNames() {
        
        String strVariableNames = settings.getDynamicProcessParameter( 
                BANJO.DATA_VARIABLENAMES );
        
        // In case we skipped the search (i.e., didn't load the observations yet), we
        // need to be careful with loading the variable names
        if ( strVariableNames != null ) {
                      
            // Note that if strVariableNames contains a list of previously read-in
            // variable names, it will be in comma-delimited form
            Pattern p = Pattern.compile( 
                    BANJO.DELIMITER_SPECIAL );
            
            variableNames = p.split( strVariableNames );
        }
        
        if ( variableNames == null || variableNames.length != varCount ) {
            
            // Default to the variable index numbers
            variableNames = new String[ varCount ];
            for ( int i=0; i<varCount; i++ ) {
                
                variableNames[i] = Integer.toString( i );
            }
        }
    }
	
	/**
	 * @param _bayesNetStructure The bayesNet for which to compute the dot graph
	 * @return	The string representing the dot graph.
	 * @throws Exception
	 */
    public StringBuffer composeDotGraph( 
                final EdgesI _bayesNetStructure, 
                final String _networkLabel,
                final double _networkScore ) throws Exception {

		StringBuffer dotStructure = new StringBuffer( 
		        BANJO.BUFFERLENGTH_STRUCTURE );

		dotStructure.append( BANJO.FEEDBACK_NEWLINE );
		dotStructure.append( "digraph abstract { " );
		dotStructure.append( BANJO.FEEDBACK_NEWLINE );
	    
		dotStructure.append( createLabel( _bayesNetStructure, _networkLabel, _networkScore ));

		dotStructure.append( BANJO.FEEDBACK_SPACE );
		dotStructure.append( BANJO.FEEDBACK_NEWLINE );
        
        String dotIndent = "    ";
		
		// Static BN case
		if ( maxMarkovLag == 0 ) {
            
            for ( int i=0; i< varCount; i++ ) {
                
                if ( !variableNames[i].equalsIgnoreCase( Integer.toString( i ) ) ) {
                    
                    dotStructure.append( BANJO.FEEDBACK_NEWLINE );
                    dotStructure.append( dotIndent + 
                            i + " [label=\"" + variableNames[i] + "\"];" );
                }
            }
            dotStructure.append( BANJO.FEEDBACK_NEWLINE );
            
			for ( int i=0; i< varCount; i++ ) {
		        for ( int j=0; j< varCount; j++ ) {
		            
		            // lag is always 0 for static bn's:
		            if ( _bayesNetStructure.getEntry(i, j, 0) == 1 ) {

	                    dotStructure.append( BANJO.FEEDBACK_NEWLINE );
                        dotStructure.append( dotIndent + j + "->" + i + ";" );
		            }
		        }
			}

            dotStructure.append( BANJO.FEEDBACK_NEWLINE );
			dotStructure.append( "}" );
            dotStructure.append( BANJO.FEEDBACK_NEWLINE );
		}
		// When minMarkovLag == maxMarkovLag > 0, we "collapse" the nodes, since
		// it's obvious which are the parents and the children
		else {
            
		    // Special formatting for DBNs; this is based on Alex's code, but
		    // distinguishes between nodes of different lag
		    
		    if ( minMarkovLag == maxMarkovLag ) {

                // Set the labels
                for ( int i=0; i< varCount; i++ ) {
                    
                    if ( !variableNames[i].equalsIgnoreCase( Integer.toString( i ) ) ) {
                        
                        dotStructure.append( BANJO.FEEDBACK_NEWLINE );
                        dotStructure.append( dotIndent + 
                                i + " [label=\"" + variableNames[i] + "\"];" );
                    }
                }
                dotStructure.append( BANJO.FEEDBACK_NEWLINE );
                
		        // (Alex's code for minMarkovLag == maxMarkovLag)
				for ( int i=0; i< varCount; i++ ) {
                                        
					for ( int j=0; j< varCount; j++ ) {
						if (i==j) continue;
						boolean parentFoundFlag = false;
						for ( int k=minMarkovLag; k<= maxMarkovLag; k++ ) {
				            if ( _bayesNetStructure.getEntry(i, j, k) == 1 ) {
				            	parentFoundFlag = true;
				            	break;
				            }
						}
	
						if ( parentFoundFlag ) {
						    
		                    dotStructure.append( BANJO.FEEDBACK_NEWLINE );
                            dotStructure.append( dotIndent + j + "->" + i + ";" );
						}
					}
				}
		    }
		    else {

                // Set the labels
                for ( int k=0; k<= maxMarkovLag; k++ ) {
                    for ( int i=0; i< varCount; i++ ) {
                        
                        if ( !variableNames[i].equalsIgnoreCase( Integer.toString( i ) ) ) {
                            
                            dotStructure.append( BANJO.FEEDBACK_NEWLINE );
                            dotStructure.append( dotIndent + 
                                    "\"(lag " + k + ") " + i + "\" [label=\"" + 
                                    "(lag " + k + ") " + variableNames[i] + "\"];" );
                        }
                    }
                }
                dotStructure.append( BANJO.FEEDBACK_NEWLINE );
                                
                // General DBN case: indicate the lag explicitly. It would be
                // nice to have special formatting that separates all nodes of
                // the same lag (maybe some columnar or banded display?)... TODO
                for ( int i=0; i< varCount; i++ ) {
                    
                    for ( int j=0; j< varCount; j++ ) {
                        if (i==j) continue;
                        boolean parentFoundFlag = false;
                        int k;
                        for ( k=minMarkovLag; k<= maxMarkovLag; k++ ) {
                            if ( _bayesNetStructure.getEntry(i, j, k) == 1 ) {
                                parentFoundFlag = true;
                                break;
                            }
                        }
    
                        if ( parentFoundFlag ) {

                            dotStructure.append( BANJO.FEEDBACK_NEWLINE );
                            dotStructure.append( dotIndent + 
                                    "\"(lag " + k + ") " + j + 
                                    "\"->\"(lag 0) " + i + "\";");
                        }
                    }
                }
		    }		        

            dotStructure.append( BANJO.FEEDBACK_NEWLINE );
			dotStructure.append( "}" );
            dotStructure.append( BANJO.FEEDBACK_NEWLINE );
		}
	
	    return dotStructure;
	}
	
	/**
	 * 
	 * @param nBestStructures	The set of bayesnets (usually a set of n-best networks)
	 * 							for which to compute the consensus graph.
	 *  
	 * @return	The adjacency matrix (as EdgesI) representing the consensus graph.
	 * @throws Exception
	 */
	public EdgesI computeConsensusGraph( final TreeSet nBestStructures ) 
				throws Exception {

	    if ( nBestStructures.size() < 2 ) {
	        
            // This records the problem as a post-processing issue
	        throw new BanjoException( BANJO.ERROR_BANJO_DEV,
	                "To compute a consensus graph, the set of top scoring networks " +
	                "needs to contain at least 2 members." );
	    }
        
		EdgesI consensusGraph = new EdgesAsMatrix(
		        varCount, minMarkovLag, maxMarkovLag);
		
		BayesNetStructureI bayesNetStructure;
        
		Iterator highScoreSetIterator = nBestStructures.iterator();		
		        
        // Note: for most searches we will likely "fill up" all nBest networks as
        // specified in the user options. However, since this is not guaranteed, we
        // need to use the number of items in our nBestStructures set.
        int nBest = nBestStructures.size();
		
		double[][][] edgeProbability = 
		    new double[varCount][varCount][maxMarkovLag+1];
		double thresholdProbability = 0.5;
		
		BayesNetStructureI[] bayesNetStructures = new BayesNetStructureI[nBest];
		EdgesI[] edgeMatrices = new EdgesI[nBest];
		double[] networkScores = new double[nBest];
		double[] scoresForNetworksWithSelectedEdge = new double[nBest];
		double[] expNetworkScores = new double[nBest];
		double[] expScoresForNetworksWithSelectedEdge = new double[nBest];
		double sumOfAllScores;
		double expSumOfAllScores;
		double sumOfAllScoresWithSelectedEdge;
		double expSumOfAllScoresWithSelectedEdge;
		double currentScore;
		double overallHighScore = 0;
		double highScoreAmongGraphsWithSelectedEdge = 0;
		double sumExp = 0;
		
		int structureIndex;
		
		// Remember that for nBest > 1 we store the network in a BayesNetStructure object
		if ( nBest > 1 ) {
		    
		    // Prep the exp( score )-matrix
		    structureIndex = 0;
		    sumOfAllScores = 0;
		    expSumOfAllScores = 0;
		    while ( highScoreSetIterator.hasNext() ) {
		        
				bayesNetStructure = (BayesNetStructureI) highScoreSetIterator.next();
				bayesNetStructures[structureIndex] = bayesNetStructure;
				edgeMatrices[structureIndex] = new EdgesAsMatrix( 
				        varCount, minMarkovLag, maxMarkovLag );
				edgeMatrices[structureIndex].assignMatrix( 
				        bayesNetStructure.getNetworkStructure() );
				
				// Need to track the highest score to be able to compute
				// exponential scores numerically
				currentScore = bayesNetStructure.getNetworkScore();
				if ( overallHighScore == 0 ) {
				    
				    overallHighScore = currentScore;
				}
				else if ( overallHighScore > currentScore ) {
				    
				    overallHighScore = currentScore;
				}

		        structureIndex++;
		    }
		    
		    structureIndex = 0;
		    for ( int m=0; m<nBest; m++) {

		        structureIndex++;
		        networkScores[ m ] = bayesNetStructures[ m ].getNetworkScore();
		        expNetworkScores[ m ] = Math.exp( 
		                bayesNetStructures[ m ].getNetworkScore() - overallHighScore );
		                        
		        sumExp += expNetworkScores[ m ];
		        
		        if ( BANJO.DEBUG && BANJO.TRACE_CONSENSUSGRAPH ) {
		            
			        System.out.println( "Adjusted Score of network " + structureIndex + 
			                ":  " + networkScores[m] + ", " + expNetworkScores[ m ] );
		        }
		        sumOfAllScores += networkScores[ m ];
		        expSumOfAllScores += expNetworkScores[ m ];
		    }
		    		
		    highScoreSetIterator = nBestStructures.iterator();
		    
				
			for ( int i=0; i< varCount; i++ ) {
			    for ( int j=0; j< varCount; j++ ) {
			        for ( int k=0; k< maxMarkovLag+1; k++ ) {

			            sumOfAllScoresWithSelectedEdge = 0;
			            
			            highScoreAmongGraphsWithSelectedEdge = 0;
			            for ( int m=0; m < nBest; m++ ) {

			                // Select a score value that will not appear 
			                // (even after the adjustment)
			                scoresForNetworksWithSelectedEdge[m] = 1;
				            if ( edgeMatrices[m].getEntry( i, j, k) == 1 ) {
				                
				                scoresForNetworksWithSelectedEdge[m] = 
				                    networkScores[m];
				                
				                if ( highScoreAmongGraphsWithSelectedEdge == 0 ) {
				                    
				                    highScoreAmongGraphsWithSelectedEdge = 
				                        networkScores[m];
				                }
				                else if ( highScoreAmongGraphsWithSelectedEdge <
				                        networkScores[m] ) {
				                    
				                    highScoreAmongGraphsWithSelectedEdge = 
				                        networkScores[m];
				                }
				            }
			            }
			            
			            edgeProbability[i][j][k] = 0;
			            expSumOfAllScoresWithSelectedEdge = 0;
			            for ( int m=0; m < nBest; m++ ) {

				            expScoresForNetworksWithSelectedEdge[m] = 1;
				            if ( scoresForNetworksWithSelectedEdge[m] != 1 ) {
				                				                
				                expScoresForNetworksWithSelectedEdge[m] = 
				                    Math.exp( scoresForNetworksWithSelectedEdge[m] -
				                            highScoreAmongGraphsWithSelectedEdge );
				                
				                expSumOfAllScoresWithSelectedEdge += 
				                    expScoresForNetworksWithSelectedEdge[m];
				            }
			            }
			            
			            edgeProbability[i][j][k] = Math.exp( 
			                    highScoreAmongGraphsWithSelectedEdge - overallHighScore ) 
			                    	* expSumOfAllScoresWithSelectedEdge / expSumOfAllScores;
			            
			            if ( edgeProbability[i][j][k] >= thresholdProbability ) {

				            consensusGraph.setEntry(i, j, k, 1 );
			            }
			            else {
			                
			                consensusGraph.setEntry(i, j, k, 0 );
			            }
			        }
			    }
			}
		
			// For now, we also create special html display output (only for static bn)
			// that will be available to the calling code via a dynamic setting
            StringBuffer outputBufferForHTML = new StringBuffer( 
                    BANJO.BUFFERLENGTH_STRUCTURE_LARGE );
            
			if ( maxMarkovLag == 0 ) {

				boolean indicateAnyMatch;
			    			    
				// Compose special output format in HTML:
	
				outputBufferForHTML.append("\n<html>\n");
				outputBufferForHTML.append("<title> Comparing the Consensus Graph" +
						" with the n-Best Networks </title>\n");
				outputBufferForHTML.append("<body>\n");
				outputBufferForHTML.append("<FONT SIZE=\"1\" " +
						"FACE=\"Verdana, Arial, Helvetica, sans-serif\">\n");
				
				outputBufferForHTML.append("<table " +
						"BORDER=1 CELLPADDING=1 CELLSPACING=0>\n");
				
			    // insert new row for header
				outputBufferForHTML.append("<tr BGCOLOR=\"#CCCCCC\">\n");
				outputBufferForHTML.append("<td> Var </td>\n");
				outputBufferForHTML.append("<td> Consensus </td>\n");
				for (int i=0; i<nBest; i++ )
				    outputBufferForHTML.append("<td> #" + (i+1) + " </td>\n");
				outputBufferForHTML.append("</tr>\n");

				for (int varIndex=0; varIndex<varCount; varIndex++ ) {
				    
				    // insert new row
					outputBufferForHTML.append("<tr>\n");
					
					// insert the column valuefor the variable
					outputBufferForHTML.append("<td>");
					outputBufferForHTML.append( varIndex );
					outputBufferForHTML.append("</td>\n");
					
					// insert the column value for the consensus graph
					outputBufferForHTML.append("<td  BGCOLOR=\"#FFCCCC\"> <strong>");

					int parentCount = 0;
					StringBuffer tmpBuffer = new StringBuffer( 
				            BANJO.BUFFERLENGTH_SMALL );
					for (int j=0; j< varCount; j++) {
				        
					    int k = 0;
						if ( consensusGraph.getEntry( varIndex, j, k ) == 1 ) {

					        parentCount++;
					        tmpBuffer.append( "  " );
					        tmpBuffer.append( j );
					    }
					}
					// Add the list of parents (omit the parent count)
				    if ( parentCount > 0 )
				        outputBufferForHTML.append( tmpBuffer );
					outputBufferForHTML.append("</strong></td>\n\n");
					
					// Insert a new column value for each of the n-best networks
					for (int i=0; i<nBest; i++ ) {

					    indicateAnyMatch = false;
						parentCount = 0;
						tmpBuffer = new StringBuffer( 
					            BANJO.BUFFERLENGTH_SMALL );
						for (int j=0; j< varCount; j++) {
					        
						    int k = 0;
						    if ( edgeMatrices[i].getEntry(varIndex,j,k) == 1 ) {

						        parentCount++;
						        tmpBuffer.append( "  " );
						        if ( consensusGraph.getEntry( varIndex, j, k ) == 1 ) {
						            
						            // Make entry stand out
						            tmpBuffer.append( "<strong>" );
						            tmpBuffer.append( j );
						            tmpBuffer.append( "</strong>" );
						            indicateAnyMatch = true;
						        }
						        else {
						            tmpBuffer.append( j );						            
						        }
						    }
						}
						// Add the list of parents (omit the parent count)
					    if ( indicateAnyMatch ) 
							outputBufferForHTML.append("<td BGCOLOR=\"#AADDCC\">");
					    else 
							outputBufferForHTML.append("<td>");
					    if ( parentCount > 0 )
					        outputBufferForHTML.append( tmpBuffer );
						outputBufferForHTML.append("</td>\n\n");
					}
					
					outputBufferForHTML.append("</tr>\n\n");
				}
				    
				outputBufferForHTML.append("</table>\n");
				
				outputBufferForHTML.append("</body>\n");
				outputBufferForHTML.append("</html>\n\n");
			}
            else {
                
                outputBufferForHTML.append( 
                        "For max. Markov lag > 0, this feature is not available. \n\n");
            }

            String consensusGraphAsHTML = 
                new String( BANJO.DATA_CONSENSUSGRAPHASHTML );
            settings.setDynamicProcessParameter( 
                    consensusGraphAsHTML, outputBufferForHTML.toString() );
		}
		
	    return consensusGraph;
	}

    void createDotGraphic( 
            final String _fullPathToDotExecutable,
            final String _dotFile, 
            final String _graphicsFile, 
            final String _graphicsFormat ) throws Exception {
        
		// Compose the command that will be executed externally
		String commandToExecute = _fullPathToDotExecutable + " -T" + _graphicsFormat + "  " +
				_dotFile + " -o " + _graphicsFile;
	    
	    if ( _fullPathToDotExecutable != null && !_fullPathToDotExecutable.equals("") &&
	            !_fullPathToDotExecutable.equals( BANJO.UI_FULLPATHTODOTEXECUTABLE_NOTSUPPLIED ) ) {
		    
	        try {
            
	            StringBuffer appOutput = executeExternalProgram( commandToExecute );
	        }
	        catch ( InterruptedException e ) {

			   	settings.addToErrors( new BanjoError( 
		                "The execution of 'dot' to create the graphics file '" +
		                _graphicsFile + "' was interrupted. " +
		                "No output has been produced.",
			            BANJO.ERRORTYPE_DOTINTERRUPTION,
			            "(Executing 'dot')",
	            		StringUtil.getClassName( this ) ), e );
	        }
	        catch ( BanjoException e) {

			   	settings.addToErrors( new BanjoError( 
		                "The attempted execution of 'dot' to create the graphics file '" +
		                _graphicsFile + "' did not succeed. " +
		                "No output has been produced. [" + e.getMessage() + "]",
			            BANJO.ERRORTYPE_DOTEXECUTION,
			            "(Executing 'dot')",
	            		StringUtil.getClassName( this ) ) );
	        }
	        catch ( Exception e ) {

			   	settings.addToErrors( new BanjoError(  
		                "The attempted execution of 'dot' to create the graphics file '" +
		                _graphicsFile + "' did not succeed. " +
		                "No output has been produced.",
			            BANJO.ERRORTYPE_DOTEXECUTION,
			            "(Executing 'dot')",
	            		StringUtil.getClassName( this ) ), e );
	        }
	    }
        else {

            settings.addToErrors( new BanjoError( 
                    "'dot' could not be used to create the graphics output file '" +
                    _graphicsFile + "', because the location for dot as specified ('" +
                    _fullPathToDotExecutable +
                    "') is not valid.",
                    BANJO.ERRORTYPE_DOTEXECUTION,
                    "(Executing 'dot')",
                    StringUtil.getClassName( this ) ), null );
        }
    }

    private StringBuffer executeExternalProgram( final String cmdline ) throws Exception {

		StringBuffer appOutput = new StringBuffer();
		
		String outputLine;
			 
		Process process = Runtime.getRuntime().exec(cmdline);
		BufferedReader inputReader = 
		    new BufferedReader( new InputStreamReader( process.getInputStream() ) );

        // 4/14/2009 hjs    We don't want to do this generically, as is "pointed out" by 
        //                  the change in GraphViz v2.22 (where the binary form of the 
        //                  graphic file is returned, and we echo it as a result...
//		// Process the text being returned by the application that we execute externally
//		while ( ( outputLine = inputReader.readLine()) != null ) {
//		    
//		    appOutput.append( outputLine );
//		
//		    System.out.println(outputLine);
//		}

		// Wait for process to finish
	    process.waitFor(); 
		
		inputReader.close();
		
		return appOutput;
    }
    
	public String createLabel( 
            final EdgesI _bayesNetStructure,
	        final String _networkLabel,
            final double _networkScore ) {
	    
	    StringBuffer label = new StringBuffer();
        String spacer = BANJO.FEEDBACK_SPACE;
	    
		label.append( "\nlabel = \"" + BANJO.BANJOVERSION + spacer + 
		        BANJO.APPLICATION_VERSIONNUMBER );

        label.append( "\\n" + _networkLabel );
        
        // Only append the score if we actually have one (for consensus graphs, this
        // may not be true - in fact, in the calling code we assume just that)
        if ( _networkScore != BANJO.BANJO_UNREACHABLESCORE_BDE ) {
		
		    label.append( ", score: " + StringUtil.formatDecimalDisplay(
                    _networkScore, BANJO.FEEDBACK_DISPLAYFORMATFORNETWORKSCORE ) );
		}
        
		label.append( "\\n" + BANJO.SETTING_PROJECT_DISP + spacer +
		        settings.getValidatedProcessParameter( 
		                BANJO.SETTING_PROJECT ) );
        label.append( "\\n" + BANJO.SETTING_USER_DISP + spacer +
		        settings.getValidatedProcessParameter( 
		                BANJO.SETTING_USER ) );
        label.append( "\\n" + BANJO.SETTING_DATASET_DISP + spacer +
		        settings.getValidatedProcessParameter( 
		                BANJO.SETTING_DATASET ) );
		
		// (v2.0) Only add the number of proposed networks when we have actually
		// performed a search
		String strProposedNetworks = settings.getDynamicProcessParameter( 
                BANJO.DATA_PROPOSEDNETWORKS );
		if ( strProposedNetworks != null ) {
		    
		    long proposedNetworks = Long.parseLong( strProposedNetworks );
		    
		    if ( proposedNetworks > 0 ) 
		        label.append( "\\nNetworks searched: " + strProposedNetworks );
		}
		
		label.append( "\"; \nlabeljust=\"l\";" );
		
		return label.toString();
	}
    	
    /**
     * Provides the valid choices for this class, here: the available searchers.
     */
    public Object validChoices() {
	    
		Set validValues = new HashSet();
			    
	    // Supported graphics formats
        validValues.add( BANJO.UI_DOTFORMAT_CANON );
        validValues.add( BANJO.UI_DOTFORMAT_DOT );
        validValues.add( BANJO.UI_DOTFORMAT_FIG );
        validValues.add( BANJO.UI_DOTFORMAT_GD );
        validValues.add( BANJO.UI_DOTFORMAT_GIF );
        validValues.add( BANJO.UI_DOTFORMAT_HPGL );
        validValues.add( BANJO.UI_DOTFORMAT_IMAP );
        validValues.add( BANJO.UI_DOTFORMAT_CMAP );
        validValues.add( BANJO.UI_DOTFORMAT_JPG );
        validValues.add( BANJO.UI_DOTFORMAT_MIF );
        validValues.add( BANJO.UI_DOTFORMAT_MP );
        validValues.add( BANJO.UI_DOTFORMAT_PCL );
        validValues.add( BANJO.UI_DOTFORMAT_PIC );
        validValues.add( BANJO.UI_DOTFORMAT_PLAIN );
        validValues.add( BANJO.UI_DOTFORMAT_PNG );
        validValues.add( BANJO.UI_DOTFORMAT_PS );
        validValues.add( BANJO.UI_DOTFORMAT_PS2 );
        validValues.add( BANJO.UI_DOTFORMAT_SVG );
        validValues.add( BANJO.UI_DOTFORMAT_VRML );
        validValues.add( BANJO.UI_DOTFORMAT_VTX );
        validValues.add( BANJO.UI_DOTFORMAT_VBMP );

	    return validValues;
	}
	
	public boolean execute() throws Exception {
	    
	    boolean completedSuccessfully = true;
	    

		StringBuffer postProcessingData = new StringBuffer(
			BANJO.BUFFERLENGTH_STAT );
		
		BayesNetStructureI bayesNetStructure;
		
		if ( settings.getHighScoreStructureSet() == null ) {
		     
		    // if this set is empty then the search was not executed
		    // so it makes no sense to do any post-processing
		    completedSuccessfully = false;
		    return completedSuccessfully;
		}
		
		// Post-processing choices:
		String createDotOutput = settings.getValidatedProcessParameter(
	            BANJO.SETTING_CREATEDOTOUTPUT );
		String computeInfluenceScores = settings.getValidatedProcessParameter(
	            BANJO.SETTING_COMPUTEINFLUENCESCORES );
		String computeConsensusGraph = settings.getValidatedProcessParameter(
	            BANJO.SETTING_COMPUTECONSENSUSGRAPH );
		String consensusGraphAsHtml = settings.getValidatedProcessParameter(
	            BANJO.SETTING_DISPLAYCONSENSUSGRAPHASHTML );
		
		Iterator highScoreSetIterator = 
		    settings.getHighScoreStructureSet().iterator();

		int lineLength = BANJO.FEEDBACK_LINELENGTH;
		String newLinePlusPrefix = BANJO.FEEDBACK_NEWLINE +
				BANJO.FEEDBACK_DASH + BANJO.FEEDBACK_SPACE;


		if ( highScoreSetIterator.hasNext() ) {
			
			bayesNetStructure = (BayesNetStructureI) 
					highScoreSetIterator.next();
            
            // Note: we want to compute the influence scores first, in case the search was
            // skipped, and we need to load the variable names (which happens in the
            // influence scorer when it loads the observations)
            if ( computeInfluenceScores.equals( BANJO.UI_COMPUTEINFLUENCESCORES_YES ) ) {
                
                postProcessingData.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
                        substring( 0, lineLength )); 
                postProcessingData.append( StringUtil.formatRightLeftJustified( 
                        newLinePlusPrefix, "Post-processing", 
                        "Influence scores", null, lineLength ) );
                postProcessingData.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
                        substring( 0, lineLength ) );
                postProcessingData.append( BANJO.FEEDBACK_NEWLINE );
                                    
                try {
                    
                    InfluenceScorer influenceScorer = 
                            new InfluenceScorer( bayesNetStructure, settings );

                    // This is only necessary when we used SearcherSkip (which means that the 
                    // observations were not loaded, and thus the variable names were not
                    // available until we call updateVariableNames()
                    updateVariableNames();
                    
                    StringBuffer influenceScores = 
                            influenceScorer.computeInfluenceScores( bayesNetStructure );
                    
                    postProcessingData.append( influenceScores );
                    
                    postProcessingData.append( BANJO.FEEDBACK_NEWLINE );
                }
                catch (final BanjoException e) {

                    settings.addToErrors( new BanjoError( 
                            "The influence scores could not be computed." +
                            "\nDetailed info: '" + e.getMessage() + "'.",
                            BANJO.ERRORTYPE_POSTPROCESSING,
                            "(Computing influence scores) " +
                            e.getMessage() ,
                            StringUtil.getClassName( this ) ), null );
                    
                    completedSuccessfully = false;
                }
                catch (Exception e) {

                    settings.addToErrors( new BanjoError( 
                            "The influence scores could not be computed." +
                            "\nDetailed info: '" + e.getMessage() + "'.",
                            BANJO.ERRORTYPE_POSTPROCESSING,
                            "(Computing influence scores) ",
                            StringUtil.getClassName( this ) ), null );
                    
                    completedSuccessfully = false;
                }
            }
			
			if ( createDotOutput.equals( BANJO.UI_CREATEDOTOUTPUT_YES ) ) {
				
			    postProcessingData.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
				        substring( 0, lineLength ));
			    postProcessingData.append( StringUtil.formatRightLeftJustified( 
			            newLinePlusPrefix, "Post-processing", 
				        "DOT graphics format output", null, lineLength ) );
			    postProcessingData.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
				        substring( 0, lineLength ) );
		        postProcessingData.append( BANJO.FEEDBACK_NEWLINE );
				
			    try {

				    String networkLabel = "High scoring network";
				    
				    String dotCommandsForTopGraph = new String( 
					        composeDotGraph( bayesNetStructure.getNetworkStructure(), 
                                    networkLabel, 
                                    bayesNetStructure.getNetworkScore() ));
				    
				    String outputDirectory = new String( 
				            settings.getValidatedProcessParameter( 
				                    BANJO.SETTING_OUTPUTDIRECTORY ) );

                    String fileNameOnly = 
                        settings.getValidatedProcessParameter( 
                                BANJO.SETTING_FILENAMEFORTOPGRAPH );
                    
                    String fileNameForTopGraph = outputDirectory + File.separator + fileNameOnly;
                    
                    // Now validate the path to this file as a valid path

                    File dataFile = new File( fileNameForTopGraph );
                    File canonicalFile = dataFile.getCanonicalFile();
                    if ( dataFile.exists() ) {
                        
                        // TODO Add warning that existing file will be overwritten
                    }
                    else {
                        
                        if ( !canonicalFile.getParentFile().isDirectory() ) {
                            
                            throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT , 
                                    "(PostProcessor.execute) When the file name '" + fileNameOnly + 
                                    "' for the top graph is combined with the " + 
                                    BANJO.SETTING_OUTPUTDIRECTORY_DESCR + " '" +
                                    outputDirectory +
                                    "', the result does not form a valid path to a file." +
                                    "\nPlease make sure that all specified directories exist!" );
                        }
                    }
				    
					String dotExtension = settings.getValidatedProcessParameter( 
		                    BANJO.SETTING_DOTFILEEXTENSION );
					String graphicsExtension = settings.getValidatedProcessParameter( 
		                    BANJO.SETTING_DOTGRAPHICSFORMAT );
					String graphicsFormat = settings.getValidatedProcessParameter( 
		                    BANJO.SETTING_DOTGRAPHICSFORMAT );
				    											    
                    settings.writeStringToFile( 
					        fileNameForTopGraph + "." + dotExtension, 
					            dotCommandsForTopGraph, BANJO.TRACE_POSTPROCESSOR_FILEOUTPUT );

			        String fullPathToDotExecutable = 
			            settings.getValidatedProcessParameter( 
			                BANJO.SETTING_FULLPATHTODOTEXECUTABLE );
			        
			        if ( !fullPathToDotExecutable.equals( 
			                BANJO.SETTING_FULLPATHTODOTEXECUTABLE ) ) {
					
			            createDotGraphic( fullPathToDotExecutable, 
						        fileNameForTopGraph + "." + dotExtension, 
						        fileNameForTopGraph + "." + graphicsExtension, 
						        graphicsFormat );
			        }
					
				    postProcessingData.append( 
				            dotCommandsForTopGraph );
				}
			    catch ( final BanjoException e ) {

                    settings.addToErrors( new BanjoError( 
                            "The 'dot' output could not be created." +
                            " Detail info: '" + e.getMessage() + "'.",
                            BANJO.ERRORTYPE_POSTPROCESSING,
                            "(Creating the 'dot' output) ",
                            StringUtil.getClassName( this ) ), null );
                    
                    completedSuccessfully = false;
				}
				catch (Exception e) {

                    settings.addToErrors( new BanjoError( 
                            "The 'dot' output could not be created." +
                            " Detail info: '" + e.getMessage() + "'.",
                            BANJO.ERRORTYPE_POSTPROCESSING,
                            "(Creating the 'dot' output) ",
                            StringUtil.getClassName( this ) ), null );
                    
                    completedSuccessfully = false;
				} 
			}
			
			// It only makes sense to look at a consensus graph when there are
			// multiple graphs in the "high score set"; otherwise, skip this step.
			if ( computeConsensusGraph.equals( BANJO.UI_COMPUTECONSENSUSGRAPH_YES ) && 
			        settings.getHighScoreStructureSet().size() > 1 ) {

                String fileNameForConsensusGraph = "consensus.graph";
                
                String dotExtension = settings.getValidatedProcessParameter( 
                        BANJO.SETTING_DOTFILEEXTENSION );
                String graphicsExtension = settings.getValidatedProcessParameter( 
                        BANJO.SETTING_DOTGRAPHICSFORMAT );
                String graphicsFormat = settings.getValidatedProcessParameter( 
                        BANJO.SETTING_DOTGRAPHICSFORMAT );
                String htmlExtension = settings.getValidatedProcessParameter( 
                        BANJO.SETTING_HTMLFILEEXTENSION );
                
                try {

//                    EdgesI consensusGraph = new EdgesAsMatrixWithCachedStatistics(
//                            varCount, minMarkovLag, maxMarkovLag);
                    EdgesI consensusGraph = new EdgesAsArrayWithCachedStatistics(
                            varCount, minMarkovLag, maxMarkovLag, settings );
    				
    				consensusGraph = computeConsensusGraph( settings.getHighScoreStructureSet() );
    
    		        postProcessingData.append( BANJO.FEEDBACK_NEWLINE );
    			    postProcessingData.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
    				        substring( 0, lineLength )); 
    			    postProcessingData.append( StringUtil.formatRightLeftJustified( 
    			            newLinePlusPrefix, "Post-processing", 
    				        "Consensus graph", null, lineLength ) );
    			    postProcessingData.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
    				        substring( 0, lineLength ) );
    		        postProcessingData.append( BANJO.FEEDBACK_NEWLINE );
    		        postProcessingData.append( BANJO.FEEDBACK_NEWLINE );
    
                    postProcessingData.append( consensusGraph.toString() );    
    
    		        postProcessingData.append( BANJO.FEEDBACK_NEWLINE );
    			    String networkLabel = "Consensus graph (based on top " + 
    			    settings.getHighScoreStructureSet().size() + " networks)";
    			    
    			    String dotCommandsForConsensusGraph = new String( 
                          composeDotGraph( consensusGraph, networkLabel, 
                                  BANJO.BANJO_UNREACHABLESCORE_BDE ));
                    
                    String outputDirectory = new String( 
                            settings.getValidatedProcessParameter( 
                                    BANJO.SETTING_OUTPUTDIRECTORY ) );
    
                    String fileNameOnly = 
                        settings.getValidatedProcessParameter( 
                                BANJO.SETTING_FILENAMEFORCONSENSUSGRAPH );
                    
                    fileNameForConsensusGraph = outputDirectory + File.separator + fileNameOnly;
                    
                    // Now validate the path to this file as a valid path
    
                    File dataFile = new File( fileNameForConsensusGraph );
                    File canonicalFile = dataFile.getCanonicalFile();
                    if ( dataFile.exists() ) {
                        
                        // Add warning that existing file will be overwritten
                    }
                    else {
                        
                        if ( !canonicalFile.getParentFile().isDirectory() ) {
                            
                            throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT , 
                                    "(PostProcessor.execute) When the file name '" + fileNameOnly + 
                                    "' for the consensus graph is combined with the " + 
                                    BANJO.SETTING_OUTPUTDIRECTORY_DESCR + " '" +
                                    outputDirectory +
                                    "', the result does not form a valid path to a file." +
                                    "\nPlease make sure that all specified directories exist!" );
                        }
                    }
    			    
                    settings.writeStringToFile( 
    			            fileNameForConsensusGraph + "." + dotExtension,
    			            dotCommandsForConsensusGraph, BANJO.TRACE_POSTPROCESSOR_FILEOUTPUT );
    

                    // Only try to do this when the overall flag is set
                    if ( createDotOutput.equals( BANJO.UI_CREATEDOTOUTPUT_YES ) ) {
                    
        		        String fullPathToDotExecutable = 
        		            settings.getValidatedProcessParameter( 
        		                BANJO.SETTING_FULLPATHTODOTEXECUTABLE );
        		        
        		        if ( !fullPathToDotExecutable.equals( 
        		                BANJO.SETTING_FULLPATHTODOTEXECUTABLE ) ) {
        
        					createDotGraphic( fullPathToDotExecutable, 
        					        fileNameForConsensusGraph + "." + dotExtension, 
        					        fileNameForConsensusGraph + "." + graphicsExtension, 
        					        graphicsFormat );
        					
        					settings.setDynamicProcessParameter( 
        					        BANJO.DATA_FILE_BESTNETWORK_STRUCTURE, 
        					        fileNameForConsensusGraph + "." + dotExtension );
        					settings.setDynamicProcessParameter( 
        					        BANJO.DATA_FILE_BESTNETWORK_GRAPHICS, 
        					        fileNameForConsensusGraph + "." + graphicsExtension );
        		        }
        			    
        			    postProcessingData.append( dotCommandsForConsensusGraph );
                    }
                }
                catch (final BanjoException e) {

                    settings.addToErrors( new BanjoError( 
                            "The consensus graph could not be computed." +
                            " Detail info: '" + e.getMessage() + "'.",
                            BANJO.ERRORTYPE_POSTPROCESSING,
                            "(Post-processing) ",
                            StringUtil.getClassName( this ) ), null );
                    
                    completedSuccessfully = false;
                }
                catch (Exception e) {

                    settings.addToErrors( new BanjoError( 
                            "The consensus graph could not be computed." +
                            " Detail info: '" + e.getMessage() + "'.",
                            BANJO.ERRORTYPE_POSTPROCESSING,
                            "(Post-processing) ",
                            StringUtil.getClassName( this ) ), null );
                    
                    completedSuccessfully = false;
                }
                
                try {
                    
    			    if ( consensusGraphAsHtml.equals( 
    			            BANJO.UI_DISPLAYCONSENSUSGRAPHASHTML_YES ) ) {
    
    			        postProcessingData.append( BANJO.FEEDBACK_NEWLINE );
    				    postProcessingData.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
    					        substring( 0, lineLength )); 
    				    postProcessingData.append( StringUtil.formatRightLeftJustified( 
    				            newLinePlusPrefix, "Post-processing", 
    					        "Consensus graph and n-best graphs as HTML", 
    					        null, lineLength ) );
    				    postProcessingData.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
    					        substring( 0, lineLength ) );
    			        postProcessingData.append( BANJO.FEEDBACK_NEWLINE );
    				    postProcessingData.append( 
    				            settings.getDynamicProcessParameter( 
    				                    BANJO.DATA_CONSENSUSGRAPHASHTML ));
    				    
    				    // Convenience output of html table to separate file:
                        settings.writeStringToFile( 
    				            fileNameForConsensusGraph + "." + htmlExtension,
    				            settings.getDynamicProcessParameter( 
    				                    BANJO.DATA_CONSENSUSGRAPHASHTML ), 
    				            BANJO.TRACE_POSTPROCESSOR_FILEOUTPUT );
    			    }
                }
                catch (final BanjoException e) {

                    settings.addToErrors( new BanjoError( 
                            "The html table for the consensus graph could not be created." +
                            " Detail info: '" + e.getMessage() + "'.",
                            BANJO.ERRORTYPE_POSTPROCESSING,
                            "(Post-processing) ",
                            StringUtil.getClassName( this ) ), null );
                    
                    completedSuccessfully = false;
                }
                catch (Exception e) {

                    settings.addToErrors( new BanjoError( 
                            "The html table for the consensus graph could not be created." +
                            " Detail info: '" + e.getMessage() + "'.",
                            BANJO.ERRORTYPE_POSTPROCESSING,
                            "(Post-processing) ",
                            StringUtil.getClassName( this ) ), null );
                    
                    completedSuccessfully = false;
                }
			}

			Collection outputFileFlags = new HashSet();
			outputFileFlags.add( new Integer(BANJO.FILE_RESULTS) );
            settings.writeToFile( outputFileFlags , postProcessingData );
		}
		else {

            settings.addToErrors( new BanjoError( 
                    "Postprocessing cannot proceed because we can't process " +
                    "the high score network(s).",
                    BANJO.ERRORTYPE_POSTPROCESSING,
                    "(Post-processing) ",
                    StringUtil.getClassName( this ) ), null );
            
            completedSuccessfully = false;
		}
	    
	    return completedSuccessfully;
	}
}