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
package edu.duke.cs.banjo.learner;

import edu.duke.cs.banjo.bayesnet.*;
import edu.duke.cs.banjo.data.settings.*;
import edu.duke.cs.banjo.learner.components.*;
import edu.duke.cs.banjo.utility.*;

import java.util.*;

/**
 * Implements a greedy search.
 *
 * <p><strong>Details:</strong> <br>
 * - Compatible with "random local move" and "all local moves" proposers. 
 * <br>
 * - Requires that the user specifies several parameters that are greedy-specific and
 *   not used by other searchers (in addition to the standard set of parameters).
 * 
 * <p><strong>Change History:</strong> <br>
 * Created on May 17, 2004
 * <p>
 * 8/25/2005 (v1.0.1) hjs	Improve the fixedNumberOfRestarts handling (enable search
 * 							with 0 restarts).
 * <p>
 * 10/18/2005 (v2.0) hjs	Code cleanup as part of initial setting up of version 1.5.
 * 
 * <p>
 * hjs (v2.1)               Make 2 methods "synchronized".
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class SearcherGreedy extends Searcher {
	
    // Adjustment variable related to restarts. Keeps our feedback
    // numbers nice and "round"
    protected int innerLoopStart;
	
	// Variables related to the restart of the current bayesnet
	protected long networksVisitedSinceHighScore;
	protected long networksVisitedSinceRestart;
	protected long minNetworksVisitedSinceHighScore;
	protected long minNetworksVisitedBeforeRestart;
	protected long maxNetworksVisitedBeforeRestart;

    /**
     * Inner class for terminating a search based on a limit on the search time.
     */
	protected class TimeTerminator extends SearchTerminator {
		
	    private boolean timeRemaining = true;
		
		protected boolean checkTerminationCondition() throws Exception {
		    								
			elapsedTime = System.currentTimeMillis() - startTime;

			// Check if we reached the time or max search loop limit
			if ( maxSearchTime >= 0 && maxSearchTime <= elapsedTime ) {
			    
			    timeRemaining = false;
			}

			return ( timeRemaining );
		}
	}

    /**
     * Inner class for terminating a search based on a limit on the number of iterations.
     */
	protected class IterationsTerminator extends SearchTerminator {
		
		private boolean searchLoopsRemaining = true;
		
		protected boolean checkTerminationCondition() throws Exception {
		    
			if ( ( maxSearchLoops >= 0 && maxSearchLoops <= networksVisitedGlobalCounter)
				|| maxNetworksVisitedInInnerLoop == 0 ) {
			    
			    searchLoopsRemaining = false;
			}

			return ( searchLoopsRemaining );
		}
	}

    /**
     * Inner class for terminating a search based on a limit on the number of restarts.
     */
	protected class RestartsTerminator extends SearchTerminator {
	    
		protected boolean checkTerminationCondition() throws Exception {

		    // hjs 8/23/05 Change the logic slightly to enable test runs with 0 restarts
	        if ( ( networksVisitedSinceRestart >= minNetworksVisitedBeforeRestart &&
					networksVisitedSinceHighScore >= minNetworksVisitedSinceHighScore )
					|| networksVisitedSinceRestart >= maxNetworksVisitedBeforeRestart
					) {
    			
				if ( restartCount >= maxRestarts ) {

					elapsedTime = System.currentTimeMillis() - startTime;
			        return false;
			    }
				restartCount++;
			
			  	// First: restart the network
			    bayesNetManager.initializeBayesNet();
				restartsAtCounts.append( ", " + networksVisitedGlobalCounter );
				
				// Second: reset the score
				currentBestScoreSinceRestart = 
				    evaluator.computeInitialNetworkScore(bayesNetManager);
				
				// Third: reset the current high score (the working copy; 
				//		the global copy is tracked in the high-score-set)
				highScoreStructureSinceRestart = new BayesNetStructure( 
                        bayesNetManager.getCurrentParents(),
						currentBestScoreSinceRestart, 
						networksVisitedGlobalCounter );
				
				// Fourth: Reset the decider
				decider.setCurrentScore( currentBestScoreSinceRestart );
				
				// Finally: reset the counter
				networksVisitedSinceRestart = 0;
			}
	        else {
	            
	            if ( restartCount >= maxRestarts ) {

					elapsedTime = System.currentTimeMillis() - startTime;
			        return false;
			    }
	        }
	        
			return true;
		}
	}

    /**
     * Inner class for terminating a search with 1 termination criterion.
     */
	protected class SingleTerminator extends SearchMultipleTerminator {
	    
	    protected boolean checkTerminationCondition() throws Exception {

	        checkForRestart();
	        
			return terminator[0].checkTerminationCondition();
	    }
	}

    /**
     * Inner class for terminating a search with 2 termination criteria.
     */
	protected class DoubleTerminator extends SearchMultipleTerminator {
	    
	    protected boolean checkTerminationCondition() throws Exception {

	        checkForRestart();
	        
	        return ( terminator[0].checkTerminationCondition() && 
	                 terminator[1].checkTerminationCondition() );
	    }
	}

    /**
     * Inner class for terminating a search with 3 termination criteria.
     */
	protected class TripleTerminator extends SearchMultipleTerminator {
	    
	    protected boolean checkTerminationCondition() throws Exception {	        

	        checkForRestart();
	        
	        return ( terminator[0].checkTerminationCondition() && 
	                 terminator[1].checkTerminationCondition() && 
	                 terminator[2].checkTerminationCondition() );
	    }
	}
	
    /**
     * Inner class for executing a search based on a set of local changes.
     */
	protected class GlobalSearchExecuter extends SearchExecuter {

		protected void executeSearch() throws Exception {
		    
			BayesNetChange bestChangeInInnerLoop = new BayesNetChange();
			StringBuffer feedbackMessage = new StringBuffer( BANJO.BUFFERLENGTH_SMALL );
			double bestScoreInInnerLoop = unreachableScore;
			double bayesNetScore;
			int validChangesFound = 0;
			boolean isValidChange;
			
			// Execute the outer loop until the stop criteria is met								    
			do {
				
				// Execute the core search loop a specified (namely:
				// maxNetworksVisitedInInnerLoop number) of times
				for ( networksVisitedInnerLoopCounter=0; 
					networksVisitedInnerLoopCounter < maxNetworksVisitedInInnerLoop; ) {
													
					// hjs 11/24/04 Separated Cycle-checking from proposer class
					isValidChange = false;
					bayesNetScore = unreachableScore;
					bestScoreInInnerLoop = unreachableScore;
											
					// Get the list of changes from the proposer
					suggestedChangeList = 
					    proposer.suggestBayesNetChanges( bayesNetManager );
					
					// Process each BayesNetChange in the list
					Iterator changeListIterator = suggestedChangeList.iterator();
					
					validChangesFound = 0;
					
					while ( changeListIterator.hasNext() ) {
						
						networksVisitedGlobalCounter++;
						networksVisitedSinceHighScore++;
						networksVisitedSinceRestart++;
						networksVisitedInnerLoopCounter++;
												
						suggestedBayesNetChange = 
							(BayesNetChangeI) changeListIterator.next();
						
						if ( suggestedBayesNetChange.getChangeStatus() == 
						    BANJO.CHANGESTATUS_READY ) {
						    
							// Note that the change will be "applied" to the underlying 
							// BayesNet structure in the cycleChecker. If we encounter a
							// cycle, the cycleChecker will undo the change immediately.
							isValidChange = cycleChecker.isChangeValid( bayesNetManager, 
									suggestedBayesNetChange );
						}
						else {

						    // Proposer was not able to come up with a valid change
						    isValidChange = false;
						}
						
						if ( isValidChange ) {
							
							// Since the (valid) change is already applied (to the 
							// bayesNetManager), we can compute the score without 
						    // add'l prep
							bayesNetScore = evaluator.updateNetworkScore(
							        bayesNetManager, suggestedBayesNetChange);						    

							// Store away if we encounter a new "best score" 
							// within this round
							    
							if ( bayesNetScore > bestScoreInInnerLoop 
									|| bestScoreInInnerLoop == unreachableScore ) {
																    
								bestScoreInInnerLoop = bayesNetScore;
								bestChangeInInnerLoop.updateChange( 
										suggestedBayesNetChange );
							}

							// We need to undo the current change, so we can check 
							// the next change
						    bayesNetManager.undoChange( suggestedBayesNetChange );
						    evaluator.adjustNodeScoresForUndo( suggestedBayesNetChange );
						    
						    validChangesFound++;
                        }
					}

					// Check on the (success of the) proposed changes
					if ( validChangesFound > 0 && bayesNetScore == unreachableScore ) {
					    
					    // This error really should not happen if the problem is set up
					    // correctly. 
					    throw new BanjoException( 
					            BANJO.ERROR_BANJO_UNEXPECTED, 
					            "(" + StringUtil.getClassName( this ) + 
					            ".executeSearch) " + 
					            "The global search component could not find " +
					            "a single valid change.\n" +
					            "Please check if your problem settings are feasible." );
					}
					else if ( validChangesFound == 0 ) {
					    
					    // In this case the greedy search exhausted its possibilities.
					    // We indicate that a restart is in order.
					    networksVisitedInnerLoopCounter = maxNetworksVisitedInInnerLoop;
					    networksVisitedSinceRestart = maxNetworksVisitedBeforeRestart;					    
					}
					else {
					    
					    // When a valid change has been found, we proceed by applying
					    // the best change from the proposed set of changes

						// This is a crucial step when we process a lists of changes
						// (because the cached scores stored in the evaluator get out of
						// sync otherwise, see the above "adjustNodeScoresForUndo")
						bestChangeInInnerLoop.setChangeStatus( 
								BANJO.CHANGESTATUS_READY );
						bayesNetManager.applyChange( bestChangeInInnerLoop );
						bayesNetScore = evaluator.updateNetworkScore(
						        bayesNetManager, bestChangeInInnerLoop );			
						
						// Call the decider whether to keep the change (new highscore),
						// or to ignore it
						if ( decider.isChangeAccepted( bestScoreInInnerLoop, 
								bestChangeInInnerLoop )) {
									
						    highScoreSetUpdater.updateHighScoreStructureData( 
						            bestScoreInInnerLoop );
						}					
						else {
						    
						    // The current network is not kept: Prepare for the next step
						    // by reverting the structure and the cached node scores back
						    // to the previous network
						    bayesNetManager.undoChange( bestChangeInInnerLoop );
						    evaluator.adjustNodeScoresForUndo( bestChangeInInnerLoop );
						}
						
						// Clear the change
						bestChangeInInnerLoop.resetChange();
					}
				} // done with inner loop
				
				feedbackMessage = searchTerminator.generateFeedback();
				if ( feedbackMessage.length() > 0 ) {

					searcherStatistics.recordSpecifiedData( feedbackMessage );
					feedbackMessage = new StringBuffer( BANJO.BUFFERLENGTH_SMALL );
				}
			}	
			while ( searchTerminator.checkTerminationCondition() );
			
			finalCleanup();
		}

		protected int sizeOfListOfChanges() {
		    
		    // bound on number of ("single step") local moves
		    return BANJO.CHANGETYPE_COUNT * varCount * varCount;
		}
	}
	
    /**
     * Inner class for executing a search based on a single local change.
     */
	protected class LocalSearchExecuter extends SearchExecuter {
		
		protected void executeSearch() throws Exception {
		    
			double bayesNetScore;
			boolean isValidChange;
			StringBuffer feedbackMessage = new StringBuffer( BANJO.BUFFERLENGTH_SMALL );

			innerLoopStart = 1;
			
			// Execute an outer loop until the user-specified stop criteria is met	
			do {
				
			    networksVisitedInnerLoopCounter = innerLoopStart;
			    innerLoopStart = 0;
			    
				// Execute the core search loop a specified (namely:
				// maxNetworksVisitedInInnerLoop) number of times
				while ( networksVisitedInnerLoopCounter < 
				        maxNetworksVisitedInInnerLoop ) {
													
					// hjs 11/24/04 Separated Cycle-checking from proposer class
					isValidChange = false;
					bayesNetScore = unreachableScore;
					
					while ( !isValidChange && 
					        networksVisitedInnerLoopCounter 
					        < maxNetworksVisitedInInnerLoop ) {
						
						// Get the list of changes from the proposer
						suggestedBayesNetChange = proposer.suggestBayesNetChange( 
						        bayesNetManager );
						
						networksVisitedGlobalCounter++;
						networksVisitedSinceHighScore++;
						networksVisitedSinceRestart++;
						networksVisitedInnerLoopCounter++;
						
						if ( suggestedBayesNetChange.getChangeStatus() == 
						    BANJO.CHANGESTATUS_READY ) {
						    
							// Note that the change will be "applied" to the underlying 
							// BayesNet structure in the cycleChecker. If we encounter a
							// cycle, the cycleChecker will undo the change immediately.
							isValidChange = cycleChecker.isChangeValid( bayesNetManager, 
									suggestedBayesNetChange );
						}					
					}
					
					if ( isValidChange ) {
						
						// Since any (valid) change is already applied (to the 
						// bayesNetManager), we can compute the score without add'l prep
						bayesNetScore = evaluator.updateNetworkScore(
						        bayesNetManager, suggestedBayesNetChange);

					    // Call the decider whether to keep the change (new highscore),
						// or to ignore it
						if ( decider.isChangeAccepted( 
						        bayesNetScore, suggestedBayesNetChange )) {
									
						    highScoreSetUpdater.updateHighScoreStructureData( 
						            bayesNetScore );
						}					
						else {
						    
						    // The current network is not kept: Prepare for the next step by
						    // reverting the structure and the cached node scores back to
						    // the previous network
						    bayesNetManager.undoChange( suggestedBayesNetChange );
						    evaluator.adjustNodeScoresForUndo( suggestedBayesNetChange );
						}
					}
				} // done with inner loop
				
				feedbackMessage = searchTerminator.generateFeedback();
				if ( feedbackMessage.length() > 0 ) {

					searcherStatistics.recordSpecifiedData( feedbackMessage );
					feedbackMessage = new StringBuffer( BANJO.BUFFERLENGTH_SMALL );
				}
			}	
			while ( searchTerminator.checkTerminationCondition() );

			finalCleanup();
		}

		protected int sizeOfListOfChanges() {
		    
		    // 1 local move
		    return 1;
		}
	}
	
    /**
     * Inner class for tracking a single high-scoring network
     */
	protected class SingleHighScoreUpdater extends HighScoreSetUpdater {
	    
	    // Note that for the single best network case, we don't check for
	    // equivalence classes between networks
		protected synchronized void updateHighScoreStructureData( double bayesNetScore ) 
				throws Exception {
			
			// If we actually have a new high score, "record" the related counter
			if ( bayesNetScore > currentBestScoreSinceRestart ) {
			    
				networksVisitedSinceHighScore = 0;
				
				// Even though the naming may seem off, we need to update the 
				// "highScoreStructureSinceRestart"
				currentBestScoreSinceRestart = bayesNetScore;
				highScoreStructureSinceRestart.assignBayesNetStructure(
						bayesNetManager.getCurrentParents(), 
						currentBestScoreSinceRestart,
						networksVisitedGlobalCounter );		
			}			
			
			if ( bayesNetScore > nBestThresholdScore ) {
		     
				if ( bayesNetScore > nBestThresholdScore ) {
					
					// Remove the current high-scoring network then 
					// add the new network to the nBest set
					highScoreStructureSet.remove( highScoreStructureSet.last());
					highScoreStructureSet.add( new BayesNetStructure( 
							highScoreStructureSinceRestart,
							bayesNetScore, 
							networksVisitedGlobalCounter ));
					
					// Finally, we need to update the new threshold score:
					nBestThresholdScore = bayesNetScore;
				}
			}
		}
	}

    /**
     * Inner class for tracking a set of N-best (N>1) scoring networks.
     */
	protected class BasicHighScoreSetUpdater extends HighScoreSetUpdater {

	    int sizeBefore;
	    int sizeAfter;
	    
		protected synchronized void updateHighScoreStructureData( double bayesNetScore ) 
				throws Exception {
			
			// If we actually have a new high score, "record" the related counter
			if ( bayesNetScore > currentBestScoreSinceRestart ) {
			    
				networksVisitedSinceHighScore = 0;
				
				// Even though the naming may seem off, we need to update the 
				// "highScoreStructureSinceRestart"
				currentBestScoreSinceRestart = bayesNetScore;
				highScoreStructureSinceRestart.assignBayesNetStructure(
						bayesNetManager.getCurrentParents(), 
						currentBestScoreSinceRestart,
						networksVisitedGlobalCounter );
			}			
			
			// (v2.0) hjs Add equivalence checking: we only care when we update the set
			// of high scores. Note that this implementation moves between any networks
			// (i.e., moves don't distinguish between equivalent networks, but we only
			// record non-equivalent ones)
			if ( nBestCount < nBestMax || bayesNetScore > nBestThresholdScore ) {
			    
			    isEquivalent = equivalenceChecker.isEquivalent( highScoreStructureSet, 
			            highScoreStructureSinceRestart );
				
				if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
				    
				    if ( isEquivalent ) 
				        System.out.println( "- (equiv. networks)" );
				    else
				        System.out.println( "+ (network added)" );
				}
		
				// There is nothing to do when we encounter equivalent networks, so:
				if ( !isEquivalent ) {
				    
				    if ( nBestCount < nBestMax ) {
						             
						// The nBest container is not yet filled up
					    highScoreStructureSet.add( new BayesNetStructure( 
								highScoreStructureSinceRestart,
								currentBestScoreSinceRestart, 
								networksVisitedGlobalCounter ));
					    
						// update the count of tracked highscores
						nBestCount = highScoreStructureSet.size();
						
						// Update the threshold score
						BayesNetStructureI bns = (BayesNetStructureI) 
								highScoreStructureSet.last();
						nBestThresholdScore = bns.getNetworkScore();
					}
					else if ( bayesNetScore > nBestThresholdScore ) {
					    
						// Once the N-best container is filled up with N entries, we only 
						// proceed with updating the n-Best container when we encounter 
						// a "better" score (defined here as ">")
						
						sizeBefore = highScoreStructureSet.size();
						
						// Add the new network to the nBest set
						highScoreStructureSet.add( new BayesNetStructure( 
								highScoreStructureSinceRestart,
								bayesNetScore, 
								networksVisitedGlobalCounter ));
						
						sizeAfter = highScoreStructureSet.size();
						
						// If both sizes are the same, then the network to be added
						// is already stored. Otherwise, we need to remove the now
						// lowest scoring network
						if ( sizeBefore < sizeAfter ) {
						    
						    // Remove the network with the lowest score from the n-best
						    // set of networks
							highScoreStructureSet.remove( highScoreStructureSet.last());
						}
						
						// Finally, we need to update the new threshold score:
						BayesNetStructureI bns = (BayesNetStructureI) 
								highScoreStructureSet.last();
						nBestThresholdScore = bns.getNetworkScore();
					}
				}
			}
		}
	}

    /**
     * Constructor for the greedy searcher implementation.
     */
	public SearcherGreedy( Settings _processData ) throws Exception {
				
		super( _processData );
		
		// Validate the required settings
		boolean isDataValid = validateRequiredData();
		
		// If crucial settings could not be validated, we can't execute the remaining
		// code in this constructor
		if ( !isDataValid ) return;

		// Set up the required (subordinate) objects
		setupSearch();
	}	

	/**
	 * Validates the settings values required for the greedy searcher.
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
		final int maxItemsUsed = 3;
		SettingItem[] settingItem = new SettingItem[maxItemsUsed];
		double[] dblValue = new double[maxItemsUsed];
		String strCondition;
		int validationType;
        Set validValues = new HashSet();
   
	  	// Validate the 'min. proposed networks after high score'
	    settingNameCanonical = BANJO.SETTING_MINPROPOSEDNETWORKSAFTERHIGHSCORE;
	    settingNameDescriptive = BANJO.SETTING_MINPROPOSEDNETWORKSAFTERHIGHSCORE_DESCR;
	    settingNameForDisplay = BANJO.SETTING_MINPROPOSEDNETWORKSAFTERHIGHSCORE_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_LONG;
	    validationType = BANJO.VALIDATIONTYPE_MANDATORY;
	    settingItem[0] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            null, 
	            Long.toString( BANJO.DEFAULT_MINPROPOSEDNETWORKSAFTERHIGHSCORE ));
	    
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
			    
	  	// Validate the 'min. proposed networks before restart'
	    settingNameCanonical = BANJO.SETTING_MINPROPOSEDNETWORKSBEFORERESTART;
	    settingNameDescriptive = BANJO.SETTING_MINPROPOSEDNETWORKSBEFORERESTART_DESCR;
	    settingNameForDisplay = BANJO.SETTING_MINPROPOSEDNETWORKSBEFORERESTART_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_LONG;
	    validationType = BANJO.VALIDATIONTYPE_MANDATORY;
	    settingItem[0] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            null,
	            Long.toString( BANJO.DEFAULT_MINPROPOSEDNETWORKSBEFORERESTART ));
	    
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

	  	// Validate the 'max. proposed networks before restart'
	    settingNameCanonical = BANJO.SETTING_MAXPROPOSEDNETWORKSBEFORERESTART;
	    settingNameDescriptive = BANJO.SETTING_MAXPROPOSEDNETWORKSBEFORERESTART_DESCR;
	    settingNameForDisplay = BANJO.SETTING_MAXPROPOSEDNETWORKSBEFORERESTART_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_LONG;
	    validationType = BANJO.VALIDATIONTYPE_MANDATORY;
	    settingItem[0] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            null,
	            Long.toString( BANJO.DEFAULT_MAXPROPOSEDNETWORKSBEFORERESTART ));

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
	    
	    // Validate the proposer
	    settingNameCanonical = BANJO.SETTING_PROPOSERCHOICE;
	    settingNameDescriptive = BANJO.SETTING_PROPOSERCHOICE_DESCR;
	    settingNameForDisplay = BANJO.SETTING_PROPOSERCHOICE_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_MANDATORY;
	    settingItem[0] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            this.validChoices(),
	            BANJO.UI_DEFAULT );
	    
	    // Validate the evaluator
	    settingNameCanonical = BANJO.SETTING_EVALUATORCHOICE;
	    settingNameDescriptive = BANJO.SETTING_EVALUATORCHOICE_DESCR;
	    settingNameForDisplay = BANJO.SETTING_EVALUATORCHOICE_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_MANDATORY;
	    settingItem[0] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            this.validChoices(),
	            BANJO.UI_DEFAULT );

	    // Validate the decider
	    settingNameCanonical = BANJO.SETTING_DECIDERCHOICE;
	    settingNameDescriptive = BANJO.SETTING_DECIDERCHOICE_DESCR;
	    settingNameForDisplay = BANJO.SETTING_DECIDERCHOICE_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_MANDATORY;
	    settingItem[0] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            this.validChoices(),
	            BANJO.UI_DEFAULT );
	    
	    return isDataValid;
	}

    /**
     * Set up the search, and compile various pieces of info about the search,
     * to be used by the recorder.
     */
    protected void setupSearch() throws Exception {
        
        String strSettingChoice;

        // Reset the string buffer
        searcherStats = new StringBuffer( BANJO.BUFFERLENGTH_STAT_INTERNAL );
        
        searcherStats.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, 
                BANJO.SETTING_SEARCHERCHOICE_DISP,
                StringUtil.getClassName(this), null, lineLength ) );
        
        networksVisitedSinceHighScore = 0;
        networksVisitedSinceRestart = 0;
        
        //----------------------------------------------
        // Create the core objects needed for the search
        //----------------------------------------------
        this.bayesNetManager = new BayesNetManager( processData );

        // Determine the set of stopping criteria
        int numberOfTerminationCriteria = 0;
        if ( maxSearchTime >= 0 ) {
            
            terminator[ numberOfTerminationCriteria ] = new TimeTerminator();
            numberOfTerminationCriteria++;
        }
        if ( maxSearchLoops >= 0 ) {

            terminator[ numberOfTerminationCriteria ] = new IterationsTerminator();
            numberOfTerminationCriteria++;
        }
        if ( maxRestarts >= 0 ) {

            terminator[ numberOfTerminationCriteria ] = new RestartsTerminator();
            numberOfTerminationCriteria++;
        }

        if ( numberOfTerminationCriteria == 1 ) searchTerminator = new SingleTerminator();
        else if ( numberOfTerminationCriteria == 2 ) searchTerminator = new DoubleTerminator();
        else if ( numberOfTerminationCriteria == 3 ) searchTerminator = new TripleTerminator();

        // Determine the high-score tracking 
        if ( nBestMax > 1 ) {
            
            highScoreSetUpdater = new BasicHighScoreSetUpdater();
        }
        else {
            
            highScoreSetUpdater = new SingleHighScoreUpdater();
        }
        
        // Set up which Proposer-Evaluator-Decider combo we want to use based on user 
        // input (i.e., via initialSettings file). We apply rules to ensure that only 
        // acceptable combinations are selected by the user, and apply appropriate 
        // defaults where necessary.
        // Can't use string in switch, so use integer mapping to global constants:  
        strSettingChoice = "";
        if ( processData.getValidatedProcessParameter( 
                BANJO.SETTING_PROPOSERCHOICE )
                .equalsIgnoreCase( BANJO.UI_PROP_RANDOMLOCALMOVE )) { 
            
            this.proposer = new ProposerRandomLocalMove(
                    bayesNetManager, processData );
            searchExecuter = new LocalSearchExecuter();
        }
        else if ( processData.getValidatedProcessParameter( 
                BANJO.SETTING_PROPOSERCHOICE )
                .equalsIgnoreCase( BANJO.UI_PROP_ALLLOCALMOVES )) { 
            
            this.proposer = new ProposerAllLocalMoves(
                    bayesNetManager, processData );
            searchExecuter = new GlobalSearchExecuter();
        }
        else {
            
            // Default case: 
            this.proposer = new ProposerRandomLocalMove( 
                    bayesNetManager, processData );
            searchExecuter = new LocalSearchExecuter();
            
            strSettingChoice = BANJO.UI_DEFAULTEDTO_DISP;
        }
        strSettingChoice = strSettingChoice + StringUtil.getClassName(proposer);
        searcherStats.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_PROPOSERCHOICE_DISP,
                strSettingChoice, null, lineLength ) );
        
        // Evaluator  
        strSettingChoice = "";
        if ( processData.getValidatedProcessParameter( 
                BANJO.SETTING_EVALUATORCHOICE )
                .equalsIgnoreCase( BANJO.UI_EVAL_BDE )) { 
            
            this.evaluator = new EvaluatorBDe( bayesNetManager, processData );
        }
        else {
            
            this.evaluator = new EvaluatorBDe( bayesNetManager, processData );
            
            strSettingChoice = BANJO.UI_DEFAULTEDTO_DISP;
        }
        strSettingChoice = strSettingChoice + StringUtil.getClassName(evaluator);
        searcherStats.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_EVALUATORCHOICE_DISP,
                strSettingChoice, null, lineLength ) );
        
        // Cycle-checker
        if ( processData.getDynamicProcessParameter( BANJO.CONFIG_CYCLECHECKER_METHOD )
                .equalsIgnoreCase( BANJO.DATA_CYCLECHECKING_DFS ) ) {

            cycleChecker = new CycleCheckerCheckThenApply( bayesNetManager, processData );
        }
        else if ( processData.getDynamicProcessParameter( BANJO.CONFIG_CYCLECHECKER_METHOD )
                .equalsIgnoreCase( BANJO.DATA_CYCLECHECKING_SHMUELI ) ) {

            cycleChecker = new CycleCheckerCheckThenApply( bayesNetManager, processData );
        }
        else if ( processData.getDynamicProcessParameter( BANJO.CONFIG_CYCLECHECKER_METHOD )
                .equalsIgnoreCase( BANJO.DATA_CYCLECHECKING_DFSORIG ) ) {

            // Note the apply-then-check
            cycleChecker = new CycleCheckerApplyThenCheck( bayesNetManager, processData );
        }
        else if ( processData.getDynamicProcessParameter( BANJO.CONFIG_CYCLECHECKER_METHOD )
                .equalsIgnoreCase( BANJO.UI_CYCLECHECKER_BFS ) ) {

            // Note the apply-then-check; In addition, this method is only compatible
            // with the edgesAsMatrix class
            cycleChecker = new CycleCheckerApplyThenCheck( bayesNetManager, processData );
        }
        else {

            // default choice
            cycleChecker = new CycleCheckerCheckThenApply( bayesNetManager, processData );
        }
            
        // Compute the initial score for our Bayesnet
        currentBestScoreSinceRestart = 
            evaluator.computeInitialNetworkScore(bayesNetManager);
        nBestThresholdScore = currentBestScoreSinceRestart;
        
        strSettingChoice = "";
        if ( processData.getValidatedProcessParameter( 
                BANJO.SETTING_DECIDERCHOICE )
                .equalsIgnoreCase( BANJO.UI_DEC_GREEDY )) {
            
            this.decider = new DeciderGreedy( bayesNetManager,
                    processData, currentBestScoreSinceRestart );
        }
        else {
            // Default case:
                
            this.decider = new DeciderGreedy( bayesNetManager,
                    processData, currentBestScoreSinceRestart );
            
            strSettingChoice = BANJO.UI_DEFAULTEDTO_DISP;
        }
        strSettingChoice = strSettingChoice + StringUtil.getClassName(decider);
        searcherStats.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_DECIDERCHOICE_DISP,
                strSettingChoice, null, lineLength ) );
        
        processData.setDynamicProcessParameter( BANJO.DATA_SEARCHERINFO_COREOBJECTS,
                searcherStats.toString() );
        
        // Reset the string buffer
        searcherStats = new StringBuffer( BANJO.BUFFERLENGTH_STAT_INTERNAL );
        
        try {
            minNetworksVisitedSinceHighScore = Long.parseLong( 
                    processData.getValidatedProcessParameter( 
                    BANJO.SETTING_MINPROPOSEDNETWORKSAFTERHIGHSCORE ) );
        }
        catch (Exception e) {
            
            // There's only a NumberFormatException to catch; fix it:
            minNetworksVisitedSinceHighScore = 0;
        }
        // 
        if (minNetworksVisitedSinceHighScore < 0) {
            
            minNetworksVisitedSinceHighScore = 
                BANJO.DEFAULT_MINPROPOSEDNETWORKSHIGHSCORE;
        }
        strSettingChoice = Long.toString( minNetworksVisitedSinceHighScore );
        searcherStats.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_MINPROPOSEDNETWORKSAFTERHIGHSCORE_DISP, 
                strSettingChoice, null, lineLength ) );
        // ---
        try {
            minNetworksVisitedBeforeRestart = Long.parseLong( 
                    processData.getValidatedProcessParameter( 
                    BANJO.SETTING_MINPROPOSEDNETWORKSBEFORERESTART ) );
        }
        catch (Exception e) {
            
            // There's only a NumberFormatException to catch; fix it:
            // (Actually, validation will have flagged this issue before we get here) 
            minNetworksVisitedBeforeRestart = 0;
        }
        // Can't restart within inner loop anyway
        if ( minNetworksVisitedBeforeRestart < maxNetworksVisitedInInnerLoop ) {
            minNetworksVisitedBeforeRestart = maxNetworksVisitedInInnerLoop;
        }
        strSettingChoice = Long.toString( minNetworksVisitedBeforeRestart );
        searcherStats.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_MINPROPOSEDNETWORKSBEFORERESTART_DISP, 
                strSettingChoice, null, lineLength ) );
        // ---
        try {
            maxNetworksVisitedBeforeRestart = Long.parseLong( 
                    processData.getValidatedProcessParameter(
                    BANJO.SETTING_MAXPROPOSEDNETWORKSBEFORERESTART ) );
        }
        catch (Exception e) {
            
            // There's only a NumberFormatException to catch; fix it:
            maxNetworksVisitedBeforeRestart = 0;
        }
        // 
        if ( maxNetworksVisitedBeforeRestart <= minNetworksVisitedBeforeRestart ) {
            maxNetworksVisitedBeforeRestart = minNetworksVisitedBeforeRestart + 1;
        }
        strSettingChoice = Long.toString( maxNetworksVisitedBeforeRestart);
        searcherStats.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_MAXPROPOSEDNETWORKSBEFORERESTART_DISP, 
                strSettingChoice, null, lineLength ) );
        
        // Restart method
        if ( processData.getValidatedProcessParameter( 
                BANJO.SETTING_RESTARTWITHRANDOMNETWORK).equals( 
                    BANJO.UI_RESTARTWITHRANDOMNETWORK_YES )) {

            strSettingChoice = BANJO.DATA_RESTARTWITHRANDOMNETWORK_DISP;
            searcherStats.append( StringUtil.formatRightLeftJustified( 
                    newLinePlusPrefix, BANJO.SETTING_RESTARTWITHRANDOMNETWORK_DISP, 
                    strSettingChoice, null, lineLength ) );
            
            strSettingChoice = processData.getValidatedProcessParameter( 
                    BANJO.SETTING_MAXPARENTCOUNTFORRESTART );
            searcherStats.append( StringUtil.formatRightLeftJustified( 
                    newLinePlusPrefix, BANJO.SETTING_MAXPARENTCOUNTFORRESTART_DISP,
                    strSettingChoice, null, lineLength ) );
        }
        else {

            strSettingChoice = BANJO.DATA_RESTARTWITHINITALNETWORK_DISP;
            searcherStats.append( StringUtil.formatRightLeftJustified( 
                    newLinePlusPrefix, BANJO.SETTING_RESTARTWITHRANDOMNETWORK_DISP, 
                    strSettingChoice, null, lineLength ) );
        }       
        
        // Set the initial network as the current best score:
        highScoreStructureSinceRestart = new BayesNetStructure( 
                bayesNetManager.getCurrentParents(),
                currentBestScoreSinceRestart, 0 );
                
        highScoreStructureSet = new TreeSet();
        
        // Add the initial network (trivially) to the set of n-best networks
        highScoreStructureSet.add( new BayesNetStructure( 
                highScoreStructureSinceRestart,
                currentBestScoreSinceRestart, 
                networksVisitedGlobalCounter ));
        
        // Set up the set for storing the non-equivalent networks
        nonEquivalentHighScoreStructureSet = new TreeSet();

        // Set up the container for the list of changes
        suggestedChangeList = new ArrayList( searchExecuter.sizeOfListOfChanges() );

        // Store the feedback info
        processData.setDynamicProcessParameter( BANJO.DATA_SEARCHERINFO_SPECIFICSEARCHER, 
                searcherStats.toString() );
    }

    /**
     * Wrapper around the executeSearch method of the inner classes that govern
     * the search behaviour.
     */
	public void executeSearch() throws Exception {
		
        try {
            
    		// Feedback of selected settings to the user, with option to stop the search
    		if ( !askToVerifySettings() ) {
    		    
    		    return;
    		}
    		
    		// Compute the time it took for preparation of the search
    		elapsedTime = System.currentTimeMillis() - startTime;
    		processData.setDynamicProcessParameter ( 
    		        BANJO.DATA_TOTALTIMEFORPREP, Long.toString( elapsedTime ) );
    		
    		// Record the initial data
    		searcherStatistics.recordInitialData(this);
    
    		searcherStats = new StringBuffer( BANJO.BUFFERLENGTH_STAT_INTERNAL );
    
    		// (Re)start the timer for the actual search 
    		startTime = System.currentTimeMillis();
    
    		// Execute the actual search code (which varies between global and local
    		// search strategies)
    		searchExecuter.executeSearch();
        }
        catch ( BanjoException e ) {
        
            if ( e.getExceptionType() == BANJO.ERROR_BANJO_OUTOFMEMORY ) {
                
                handleOutOfMemory();
                throw new BanjoException( e );
            }
            else {

                throw new BanjoException( e );
            }
        }
        catch ( OutOfMemoryError e ) {
            
            handleOutOfMemory();
            
            throw new BanjoException( BANJO.ERROR_BANJO_OUTOFMEMORY,
                    "Out of memory in (" +
                    StringUtil.getClassName(this) +
                    ".executeSearch)" );
        }
	}
	
	public void updateProcessData( Settings _processData ) {
        // nothing to do
    }

    /**
     * Method used by terminator inner classes, for checking when to restart
     * the greedy search.
     */
	protected void checkForRestart() throws Exception {
	    		
		// Check if finished
		
		// ------------------
		// Restart the search:
		// ------------------
		// Logic for restarting: "Always visit at least minNetworksVisitedBeforeRestart
		// networks after a restart, AND at least minNetworksVisitedSinceHighScore after
		// the last highScore was found"  OR
		// "Visit at most maxNetworksVisitedBeforeRestart after a restart"
		if ( ( networksVisitedSinceRestart >= minNetworksVisitedBeforeRestart &&
				networksVisitedSinceHighScore >= minNetworksVisitedSinceHighScore ) ||
				networksVisitedSinceRestart >= maxNetworksVisitedBeforeRestart ) {
		
		  	// - First restart the network
		    bayesNetManager.initializeBayesNet();
		    			
			restartCount++;
			restartsAtCounts.append( ", " + networksVisitedGlobalCounter );
			
			//- Second the score
			currentBestScoreSinceRestart = 
			    evaluator.computeInitialNetworkScore(bayesNetManager);
			
			//- Third the current high score (the working copy; the global copy is
			//   		tracked in the high-score-set)
			highScoreStructureSinceRestart = new BayesNetStructure( 
			        bayesNetManager.getCurrentParents(), 
					currentBestScoreSinceRestart, 
					networksVisitedGlobalCounter );
			
			//- Fourth: Reset the decider
			decider.setCurrentScore( currentBestScoreSinceRestart );
			
			//- Finally, Reset the counter
			networksVisitedSinceRestart = 0;
		}
	}
	
    /**
     * Provides the valid choices for this class, here: for all compatible components.
     */
	public Object validChoices() {
	    
		Set validValues = new HashSet();
		
		validValues.add( BANJO.UI_DEFAULT );
		
		// Proposers
		validValues.add( BANJO.UI_PROP_RANDOMLOCALMOVE );
		validValues.add( BANJO.UI_PROP_ALLLOCALMOVES );
		
		// Evaluators
		validValues.add( BANJO.UI_EVAL_BDE );
		
		// Deciders
		validValues.add( BANJO.UI_DEC_GREEDY );
		
		// Recorders
		validValues.add( BANJO.UI_RECORDER_STANDARD );
		
		return validValues;
	}
}