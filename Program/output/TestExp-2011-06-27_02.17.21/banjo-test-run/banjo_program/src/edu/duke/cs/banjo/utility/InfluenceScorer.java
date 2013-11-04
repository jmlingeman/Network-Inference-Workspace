/*
 * Created on May 10, 2005
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

import edu.duke.cs.banjo.bayesnet.*;
import edu.duke.cs.banjo.data.observations.ObservationsAsMatrix;
import edu.duke.cs.banjo.data.settings.SettingItem;
import edu.duke.cs.banjo.data.settings.Settings;

/**
 * Computes the influence score for all the edges in a network, based on the work of
 * Jing Yu and Alexander Hartemink, as published in Yu, et al., Bioinformatics (2004).
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on May 10, 2005
 * <p>
 * 9/2/2005 (v1.0.2) hjs	Change mapping of configuration indexes, between "counting"
 *							and "voting" code (defect correction)
 *
 * 11/10/2005 (v2.0) hjs	Use computed maxValueCount instead of max. allowed value.
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class InfluenceScorer {

	// Cache frequently used values 
	protected final int varCount;
	protected final int minMarkovLag;
	protected final int maxMarkovLag;
	protected final int observationCount;
	final int maxParentCount; 
	final int maxParentConfigs; 
	
	// Only one observations data set should be tied to this EvaluatorBDeStatic object:
	protected ObservationsAsMatrix observations;
	
	// alpha denotes the "equivalent sample size" (ess)
	protected final double alpha;
	
	protected final int maxValueCount;
	
	protected int[] N_ij;
	protected int[][] N_ijk;
	
	protected double[] Theta_ij;
	protected double[][] Theta_ijk;
	
	protected double[] alpha_ij;
	protected double[] alpha_ijk;
	
	protected double[] cum_ij;
	protected double[][] cum_ijk;
	
	// Access to the global data (static and dynamic)
	Settings processData;

	// For initial testing:
	protected final double scoreDiff = BANJO.TEST_SCORETOLERANCE;
	protected final double unreachableBDeScore = BANJO.BANJO_UNREACHABLESCORE_BDE;
	// Used for keeping track of the best score as a cross check to the regular
	// statistics:
	protected double checkBestNetworkScore = 0.0;
	
	public InfluenceScorer( 
	        final BayesNetStructureI _bayesNetStructure, 
	        final Settings _processData ) throws Exception {
	    
	    try {
	        
		    this.processData = _processData;

			// "stand-alone validation" (so we don't relay on other classes)
		    boolean isDataValid = validateRequiredData();
		    
			// We check if there were any problems. If we found any, we cannot continue
			// with the set up
			if ( !isDataValid ) {
			    
			    throw new BanjoException( BANJO.ERROR_CHECKPOINTTRIGGER, 
			    processData.compileErrorMessages().toString() );
			}

		    
			varCount = Integer.parseInt( 
			        _processData.getValidatedProcessParameter(
			        BANJO.SETTING_VARCOUNT ));
			minMarkovLag = Integer.parseInt( 
			        _processData.getValidatedProcessParameter(
			        BANJO.SETTING_MINMARKOVLAG ));
			maxMarkovLag = Integer.parseInt( 
			        _processData.getValidatedProcessParameter(
			        BANJO.SETTING_MAXMARKOVLAG ));
			
			// Get access to the observations associated with the bayesnet.
			observations = new ObservationsAsMatrix( processData );
			observations.loadData( _processData );
			
			observationCount = Integer.parseInt( 
			        _processData.getDynamicProcessParameter(
			        BANJO.DATA_OBSERVATIONCOUNT ));
			maxParentCount = Integer.parseInt( 
			        processData.getValidatedProcessParameter(
			                BANJO.SETTING_MAXPARENTCOUNT ));
			alpha = Double.parseDouble( 
			        processData.getValidatedProcessParameter(
			                BANJO.SETTING_EQUIVALENTSAMPLESIZE ) );
			
			maxValueCount = observations.getMaxValueCount();
			
			int tmpMaxParentConfigs = 1;
			for ( int i=0; i< maxParentCount; i++) tmpMaxParentConfigs *= maxValueCount;
			maxParentConfigs = tmpMaxParentConfigs;
			
			N_ij = new int[maxParentConfigs];
			N_ijk = new int[maxParentConfigs][maxValueCount];
			
			alpha_ij = new double[maxParentConfigs];
			alpha_ijk = new double[maxParentConfigs];
			
			Theta_ij = new double[maxParentConfigs];
			Theta_ijk = new double[maxParentConfigs][maxValueCount];
			
			cum_ij = new double[maxParentConfigs];
			cum_ijk = new double[maxParentConfigs][maxValueCount];
	    }
		catch (Exception e) {
			
			throw new BanjoException( 
			        BANJO.ERROR_BANJO_DEV,
			        "(InfluenceScorer constructor) Error in executing " +
			        "the InfluenceScorer constructor.");
		}
	}

	
	public StringBuffer computeInfluenceScores(
	        final BayesNetStructureI _bayesNetStructure ) throws Exception {

	    try {
	        StringBuffer collectedInfluenceScores = new StringBuffer(
	                BANJO.BUFFERLENGTH_STAT );
	        
			final int currentNodeLag = 0;
			int[][] parentIDlist;
			int parentCount;

            EdgesAsMatrix edgesAsMatrix;
            EdgesAsArray edgesAsArray;
            
			EdgesI edges = _bayesNetStructure.getNetworkStructure();

            
            // 8/1/2006 hjs     The new compact parent set implementations require
            // a slight adjustment, so that we can handle any bayesNetStructure that
            // stores its edge info in the newer format
            if ( edges instanceof EdgesAsMatrix ) {

                edgesAsMatrix = ( EdgesAsMatrix ) edges;
                
                for ( int nodeID = 0; nodeID < varCount; nodeID++ ) {

                    parentIDlist = edgesAsMatrix.getCurrentParentIDlist( 
                            nodeID , currentNodeLag );
                    parentCount = parentIDlist.length;
                    
                    if (parentCount > 0) {
                        
                        collectedInfluenceScores.append( 
                                computeInfluenceScoreForNode( edgesAsMatrix, nodeID ) );
                    }
                }
            }
            else if ( edges instanceof EdgesAsArray ) {

                edgesAsArray = ( EdgesAsArray ) edges;

                
                for ( int nodeID = 0; nodeID < varCount; nodeID++ ) {

                    parentIDlist = edgesAsArray.getCurrentParentIDlist( 
                            nodeID , currentNodeLag );
                    parentCount = parentIDlist.length;
                    
                    if (parentCount > 0) {
                        
                        collectedInfluenceScores.append( 
                                computeInfluenceScoreForNode( edgesAsArray, nodeID ) );
                    }
                }
            }
            else {
                
                throw new BanjoException( BANJO.ERROR_BANJO_DEV, 
                        "(" + StringUtil.getClassName( this ) 
                        + ".computeInfluenceScores) Developer issue: "
                        + "Encountered invalid data type for the structure "
                        + "(_bayesNetStructure)"
                        + " for which to compute the influence scores." );
            }
		    
		    return collectedInfluenceScores;
	    }
		catch ( BanjoException e) {
			
			throw new BanjoException( e );
		}
		catch (Exception e) {

			throw new BanjoException( 
			        BANJO.ERROR_BANJO_DEV,
			        "(InfluenceScorer.computeInfluenceScores) " +
			        "Error in computing the " +
			        "influence scores for the supplied bayesnet." );
		}
	}
	
	public StringBuffer computeInfluenceScoreForNode(
            final EdgesI _edges,
	        final int _currentNodeID ) throws Exception {
		
	    try {

            EdgesAsMatrix edgesAsMatrix;
            EdgesAsArray edgesAsArray;

	        StringBuffer sbInfluenceScore = new StringBuffer(
	                BANJO.BUFFERLENGTH_STAT );
	        
	        // Values set as convention
		    final int currentNodeLag = 0;
			final int POSITIVE = 0;
			final int NEGATIVE = 1;
			final int NEUTRAL = 2;
			// Variables that can't change after initial assignment
		    final int parentCount;
            int tmp_r_i;
            final int r_i;
		    final int maxParentBaseCount;
		    
			int parentNodeID;					
			int parentConfigIndex;
			int obsValue;
			
			int[] voteTally = new int[3];
			int shiftPOSITIVE;
			int shiftNEGATIVE;
			int shiftNEUTRAL;
						
			// The parentIDlist is a 2-dimensional array, where parentIDlist[0][]
			// contains the id's of the parents, and parentIDlist[1][] contains the
			// corresponding lags of the parent nodes
			int[][] parentIDlist =  null;
			

            if ( _edges instanceof EdgesAsMatrix ) {

                edgesAsMatrix = ( EdgesAsMatrix ) _edges;
                parentIDlist = edgesAsMatrix.getCurrentParentIDlist( 
                        _currentNodeID , currentNodeLag );
            }
            else if ( _edges instanceof EdgesAsArray ) {
                
                edgesAsArray =  ( EdgesAsArray ) _edges;
                parentIDlist = edgesAsArray.getCurrentParentIDlist( 
                        _currentNodeID , currentNodeLag );
            }
            
		    parentCount = parentIDlist.length;
	
		    if (parentCount == 0) {
		        
		        return new StringBuffer(" ");
		    }
		    
		    for (int j=0; j<maxParentConfigs; j++) {
		        
			    N_ij[j] = 0;
			    Theta_ij[j] = 0;
			    cum_ij[j] = 0;
		    }
	
		    for (int j=0; j<maxParentConfigs; j++) {
			    for (int k=0; k<maxValueCount; k++) {
		        
				    N_ijk[j][k] = 0;
				    Theta_ijk[j][k] = 0;
				    cum_ijk[j][k] = 0;
			    }
		    }
		    
		    int tmpMaxParentBaseCount = 1;
		    for ( int i=0; i<parentCount; i++ ) 
		        tmpMaxParentBaseCount *= maxValueCount;
		    maxParentBaseCount = tmpMaxParentBaseCount;
		    
			String[] strConfigs = new String[ maxParentBaseCount ];
			
			boolean[] validParentConfig =  new boolean[ maxParentBaseCount ];
		    
			// -------------------
			// Main counting loop:
			// -------------------
			// Outer loop iterates over all the (rows in the) observations
			// (DBN data is adjusted already via the data preparation in 
			// ObservationsAsMatrix class)
		    
            tmp_r_i = observations.getMaxValueCount( _currentNodeID );
            
            // hjs 7/19/2007
            if ( tmp_r_i < 2 ) {
                
                r_i = 2; 
            }
            else {
                
                r_i = tmp_r_i;
            }
			
			for ( int i=0; i<maxParentBaseCount; i++ ) {
			    
		        validParentConfig[i] =  false;
			}
			
			for (int observationRow=0; 
					observationRow < observationCount; observationRow++) {
				
				parentConfigIndex = 0;
	
			    int q_i = 1;
				if (parentCount > 0) {
						
					// Inner loop iterates over the number of parents in the current
					// configuration of this node (with nodeID), to compute the index
					// that we need to use in the N_ijk array
				    String strParentList = "";
					for ( int parentIndex=parentCount-1; 
							parentIndex >= 0; parentIndex-- ) {
	
					    /*
					    // (Keep for readability):
					    parentNodeID = parentIDlist[parentIndex][0];
					    parentNodeLag = parentIDlist[parentIndex][1];
						 */
			
						obsValue = observations.observationDataPoints
				            			[ observationRow ]
				            			  [ parentIDlist[parentIndex][0] ]
				            			    [ parentIDlist[parentIndex][1] ];
					
						strParentList += "  " + parentIDlist[parentIndex][0] + 
											":" + obsValue;
						// index = index * maxValueCount + obsValue;
						parentConfigIndex = parentConfigIndex * maxValueCount 
								+ obsValue;
						
						// For readability:
					    parentNodeID = parentIDlist[parentIndex][0];
						// Multiply by the value count for each parent
						q_i *= observations.getMaxValueCount( parentNodeID );
					}
					// Update the N_ij value for this parent index:
					N_ij[ parentConfigIndex ]++;
					strConfigs[ parentConfigIndex ] = parentConfigIndex + strParentList;
					validParentConfig[ parentConfigIndex ] = true;
				}
				else {
					// There's no parent, so there's only one N_ij (i.e., 
				    // parentConfigIndex=0)
					N_ij[ 0 ]++;
				}
				alpha_ij[ parentConfigIndex ] = alpha / q_i;
			    alpha_ijk[ parentConfigIndex ] = 
			        alpha_ij[ parentConfigIndex ] / r_i;
			
				// Finally, increment the count of the appropriate N_ijk 
				// for the current observation
				obsValue = observations.observationDataPoints
					[ observationRow ][ _currentNodeID ][ 0 ];
				N_ijk[ parentConfigIndex ][ obsValue ] ++;
			}
						
			// Note that not all parentConfig values in this loop may have valid
			// values, because maxParentBaseCount is based on powers of the "global"
			// maxValueCount
		    for (int parentConfig=0; parentConfig < maxParentBaseCount; 
		    		parentConfig++) {
		        
			    for (int nodeValue=0; nodeValue < r_i; nodeValue++) {
		        
			        if ( N_ij[parentConfig] == 0 ) {
			            
			            Theta_ijk[parentConfig][nodeValue] = 0;
			        }
			        else {
			        
					    Theta_ijk[parentConfig][nodeValue] = 
						        ( N_ijk[parentConfig][nodeValue] + 
						                alpha_ijk[parentConfig] ) /
						        ( N_ij[parentConfig] + 
						                alpha_ij[parentConfig] );
			        }
				    
			        // Note: if nodeValue==0, cum_ijk=0 by def.
				    if ( nodeValue > 0 ) {
				        
					    cum_ijk[parentConfig][nodeValue] = 
						        cum_ijk[parentConfig][nodeValue-1] + 
						        Theta_ijk[parentConfig][nodeValue];
					    
					    // For "pretty display", we want to get a 1.0 when 
					    // it's numerically a 1.0
					    if ( Math.abs( cum_ijk[parentConfig][nodeValue] - 1.0 ) <
					            BANJO.NUMERICSCORETOLERANCE ) {
				        
					        cum_ijk[parentConfig][nodeValue] = 1.0;
					    }
				    }
				    else {
				        
				        cum_ijk[parentConfig][nodeValue] =  
					        Theta_ijk[parentConfig][nodeValue];
				    }
			    }
		    }
		    
		    // Note that the index "j" of the parent configurations (the "j" in the
		    // literature) is - in our code - always based on a number representation
		    // of base <maxValueCount> so we just enumerate as follows:
		    // The last parent in the parent list gives us the last (right most) 
		    // "digit", ..., and the first parent gives us the first (left-most) digit
		    
		    final int[] baseFactorsForParent = new int[parentCount];
		    final int[] actualValueCountForParent = new int[parentCount];
		    int[] parentConfigsModuloSelectedParent;
		    int[][] fixedParentConfigs;
		    int fixedParentConfigsCount;
		    
		    // Put the "base factors" for computing our parent configuration indexes 
		    // in an array for easy access
		    // index 0 ----> parent 1 ----> factor 1
		    // index 1 ----> parent 2 ----> factor maxValueCount
		    // index parentCount-1 ----> parent parentCount ----> 
		    //			factor maxValueCount^(parentCount-1)
			for ( int parentIndex=0; parentIndex < parentCount; parentIndex++ ) {
			    
			    if ( parentIndex == 0 ) {
			        baseFactorsForParent[ parentIndex ] = 1;
			    }
			    else if ( parentIndex == 1 ) {
			        baseFactorsForParent[ parentIndex ] = maxValueCount;
			    }
			    else {
			        baseFactorsForParent[ parentIndex ] = 
			            baseFactorsForParent[ parentIndex-1 ] * maxValueCount;
			    }
	
			    // Cache how many values the parentID at parentIndex assumes 
			    actualValueCountForParent[ parentIndex ] = 
			        observations.getMaxValueCount( parentIDlist[ parentIndex ][0] );
			}
	
			// Inner loop iterates over the number of parents in the current
			// configuration of this node (with node ID = currentNodeID), to compute
			// the index that we need to use in the N_ijk array
		    int selectedParentValueCount;
		    int configIndex;
			for ( int selectedParentIndex=parentCount-1; selectedParentIndex >= 0; 
					selectedParentIndex-- ) {
			    
			    // first reset the tally array
				for ( int k=0; k<=NEUTRAL; k++ ) {
			    
				    voteTally[k] = 0;
				}
				shiftPOSITIVE = 0;
				shiftNEGATIVE = 0;
				shiftNEUTRAL = 0;
				
				selectedParentValueCount = actualValueCountForParent[ selectedParentIndex ];
				configIndex = 0;
				
				// --------------------------------------
				// Now compute the actual influence score for this parent
				// --------------------------------------
				
				// If there are no other parents, the configCount is 1:
				fixedParentConfigsCount = 1;
				
				// If there are other parents, update the config count
				for ( int parentIndex = parentCount-1; 
					parentIndex >= 0; parentIndex-- ) {
				    
				    // Don't include the selected parent
				    if ( parentIndex != selectedParentIndex ) {
				        
				        fixedParentConfigsCount *= 
				            actualValueCountForParent[ parentIndex ];			        
				    }
				}
				
				// Now we know how many (valid and possibly used) configurations there
				// are for the selected parent
				parentConfigsModuloSelectedParent = new int[fixedParentConfigsCount];
				for ( int i=0; i<fixedParentConfigsCount; i++ ) {
				    
				    parentConfigsModuloSelectedParent[i] = 0;
				}

				fixedParentConfigs = new int[ fixedParentConfigsCount ]
				        [actualValueCountForParent[ selectedParentIndex ]];

				// ------------------------------------
				// Map the fixedParentConfigs to the index values that were used in the
				// main counting loop
				// ------------------------------------
				int startVal = 0;
			    int maxVal;
			    int parentVal;

		        int[][] auxValues = new int [parentCount-1][fixedParentConfigsCount];
		        int auxIndex = 0;
		        int multipleValueCount;
		        int thresholdValue =  fixedParentConfigsCount / 
		        			actualValueCountForParent[ parentCount-1 ];
		        
		        // Process the parents in the same order (i.e., associated weights)
		        // as does the "counting" loop
		        for ( int parentIndex=parentCount-1; parentIndex >= 0; 
        				parentIndex-- ) {
		            
				    // Ignore the "selected" parent in setting the fixedParentConfigs
				    if ( parentIndex != selectedParentIndex ) {
				        
				        parentVal = startVal;
					    maxVal = actualValueCountForParent[ parentIndex ];
					    multipleValueCount = startVal;

				      	// Note: the size of fixedParentConfigsCount is a multiple
					    // of actualValueCountForParent[ parentIndex ]
					    for ( int confIndex=0; confIndex < fixedParentConfigsCount; 
					    		confIndex++ ) {
					    
					        auxValues[ auxIndex ][confIndex] = 
					            parentVal * baseFactorsForParent[ parentIndex ];
					        
					      	// Move the parentVal to the next value
					        multipleValueCount++;
					        if ( multipleValueCount == thresholdValue ) {
					            
						        parentVal++;
						        if ( parentVal >= maxVal ) parentVal = startVal;
						        
						        multipleValueCount = startVal;
					        }
					    }

					    auxIndex++;
				        
				        // Set up the threshold for the next parentIndex
					    if ( parentIndex != selectedParentIndex && parentIndex > 0) 
					        thresholdValue /= actualValueCountForParent[ parentIndex-1 ];
				    }
		        }
		        
		        // Now add up all the components to complete the mapping
		        for ( int k=0; k < actualValueCountForParent[
		                   selectedParentIndex ]; k++ ) {
		        
		            for ( int confIndex=0; confIndex < fixedParentConfigsCount; 
		    				confIndex++ ) {
		                
		                fixedParentConfigs[ confIndex ][ k ] = 
			                k * baseFactorsForParent[ selectedParentIndex ];
		                
			            for ( int i=0; i<parentCount-1; i++ ) {
			            
			                fixedParentConfigs[ confIndex ][ k ] += auxValues[ i ][confIndex];
			            }
				    }
		        }
						    			    
			    // -------------------------------------
			    // Now process the final configs: for each value of 
				// parentConfigsModuloSelectedParent[], we add each of the values that  
				// the selected parent assumes, to get the "final" config indexes. The
				// "vote tallying" also happens within each "block" of these configs.
			    // -------------------------------------
				double score = 0;
				double scorePos = 0;
				double scoreNeg = 0;
				for ( int j=0; j < fixedParentConfigsCount; j++ ) {
				    
				    scorePos = 0;
				    scoreNeg = 0;
				    voteTally[ POSITIVE ] = 0;
				    voteTally[ NEGATIVE ] = 0;
				    voteTally[ NEUTRAL ] = 0;
				    
					for ( int k=1; k < actualValueCountForParent[ 
					          selectedParentIndex ]; k++ ) {

						shiftPOSITIVE = 0;
						shiftNEGATIVE = 0;
						shiftNEUTRAL = 0;
					    // For last value (r_i-1), c_ijk ==1
					    for (int nodeValue=0; nodeValue < r_i-1; nodeValue++) {
						    
						    if ( cum_ijk[fixedParentConfigs[j][k]][nodeValue] < 
						        	cum_ijk[fixedParentConfigs[j][k-1]][nodeValue] ) {
						        
								shiftPOSITIVE++;
								if ( k == actualValueCountForParent[ 
								            selectedParentIndex ]-1 ) {
	
							        scorePos += - cum_ijk[fixedParentConfigs[j][k]]
							                              [nodeValue] +
							        	cum_ijk[fixedParentConfigs[j][0]]
							        	        [nodeValue];
							    }
						    }
						    else if ( cum_ijk[fixedParentConfigs[j][k]][nodeValue] >
						            cum_ijk[fixedParentConfigs[j][k-1]][nodeValue] ) {
						        
								shiftNEGATIVE++;
								if ( k == actualValueCountForParent
								        [ selectedParentIndex ]-1 ) {
	
							        scoreNeg += - cum_ijk[fixedParentConfigs[j][k]]
							                              [nodeValue] +
							        	cum_ijk[fixedParentConfigs[j][0]][nodeValue];
							    }
						    }
						    else {
						        
								shiftNEUTRAL++;
						    }
						} // end of loop over nodeValues
					    
						// Vote NEUTRAL if there are positive and negative shifts
						if ( shiftPOSITIVE > 0 && shiftNEGATIVE > 0 ){
						    
						    voteTally[ NEUTRAL ] ++;
						}
						// Vote POSITIVE if there are no negative shifts
						else if ( shiftPOSITIVE > 0 && shiftNEGATIVE == 0 ){
						    
						    voteTally[ POSITIVE ] ++;
						}
						// Vote NEGATIVE if there are no positive shifts
						else if ( shiftPOSITIVE == 0 && shiftNEGATIVE > 0 ) {
		
					        voteTally[ NEGATIVE ] ++;
						}
						// Vote NEUTRAL if there are neither positive nor
						// negative shifts
						else {
		
					        voteTally[ NEUTRAL ] ++;
						}
					} // end of loop over values for selectedParentIndex

					if ( voteTally[ NEUTRAL ] > 0 ) {

						// System.out.println( "score unchanged" );
					}
					else if ( ( voteTally[ POSITIVE ] > 0 && voteTally[ NEGATIVE ] == 0 ) ) {

					    score += scorePos;
					}
					else if ( voteTally[ POSITIVE ] == 0 && voteTally[ NEGATIVE ] > 0 ) {

					    score += scoreNeg;
					}				
				} // end of loop over fixedParentConfigsCount
				
				// Scale the score to the interval [-1,1]
				score = score / ( fixedParentConfigsCount * (r_i - 1) );
			            
				// Record the result				
				sbInfluenceScore.append( "\nInfluence score for " );
				sbInfluenceScore.append( StringUtil.formatRightLeftJustified(
				        "", "",
				        "(" + parentIDlist[selectedParentIndex][0] +
				        "," + parentIDlist[selectedParentIndex][1] + ")" , null, 7 ) );
				sbInfluenceScore.append( " -> " );
				sbInfluenceScore.append( StringUtil.formatRightLeftJustified(
				        "", "", 
				        "(" + _currentNodeID + ",0)" , null, 7 ) );
				sbInfluenceScore.append( "   " + StringUtil.formatDecimalDisplay( 
				                score, BANJO.FEEDBACK_DISPLAYFORMATFORINFLUENCESCORE ) );
			}
			
			// may want to pass more info back to the caller at some later point.
			// for now, simple output as above will do
			return sbInfluenceScore;
	    }
		catch (Exception e) {
			
		    e.printStackTrace();
		    
			throw new BanjoException( 
			        BANJO.ERROR_BANJO_DEV,
			        "(InfluenceScorer.computeInfluenceScoreForNode) " +
			        "Error in computing the " +
			        "influence score for node id=" + _currentNodeID + "." );
		}
	}
	/**
	 * Validates the settings values required for the influence scorer.
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
		String strCondition;
		double dblValue;
		int validationType;
		SettingItem settingItem;
	
		//  Validate
		settingNameCanonical = BANJO.SETTING_EQUIVALENTSAMPLESIZE;
		settingNameDescriptive = BANJO.SETTING_EQUIVALENTSAMPLESIZE_DESCR;
		settingNameForDisplay = BANJO.SETTING_EQUIVALENTSAMPLESIZE_DISP;
		settingDataType = BANJO.VALIDATION_DATATYPE_DOUBLE;
		validationType = BANJO.VALIDATIONTYPE_MANDATORY;		    
		settingItem = processData.processSetting( settingNameCanonical,
		        settingNameDescriptive,
	            settingNameForDisplay,
		        settingDataType,
		        validationType,
		        null, Double.toString( BANJO.DEFAULT_EQUIVALENTSAMPLESIZE ));
	    
		if ( settingItem.isValidSetting() ) {
		
		    try {
		
			    strCondition = new String( "greater than 0" );
			    dblValue = Double.parseDouble( 
			            processData.getValidatedProcessParameter(
			                    settingNameCanonical ));
		        if ( dblValue <= 0 ) {
		        	
		            processData.addToErrors( new BanjoError( 
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

	    
	    return isDataValid;
	}
}
