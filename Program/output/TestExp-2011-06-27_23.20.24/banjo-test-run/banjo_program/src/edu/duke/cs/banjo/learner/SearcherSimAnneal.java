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
 * Implements a simulated annealing-based search.
 *
 * <p><strong>Details:</strong> <br>
 * - Compatible with "random local move" and "all local moves" proposers. 
 * <br>
 * - Requires that the user specifies several parameters that are annealer-specific
 *   and not used by other searchers (in addition to the standard set of parameters).
 * 
 * <p><strong>Change History:</strong> <br>
 * Created on May 17, 2004
 * <p>
 * 8/25/2005 (v1.0.1) hjs	Improve the fixedNumberOfRestarts handling (enable search
 * 							with 0 restarts).
 * <p>
 * 9/6/2005 (v1.0.3) hjs	Defect in GlobalSearchExecuter: acceptedPerTemperature and
 * 							iterationsPerTemperature were not incremented properly.
 * <p>
 * 10/18/2005 (v2.0) hjs	Code cleanup as part of initial setting up of version 1.5.
 * 
 * <p>
 * hjs (v2.1)               Make 2 methods "synchronized".
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class SearcherSimAnneal extends Searcher {
	
	// Data that is unique to this Searcher implementation 
	protected double currentTemperature;
	protected double coolingFactor;
	protected int innerLoopStart;
	
	protected long iterationsPerTemperature = 0;
	protected long acceptedPerTemperature = 0;

	protected long maxAcceptedNetworksBeforeCooling = 2500;
	protected long maxSearchIterationsBeforeCooling = 10000;
	protected long minAcceptedNetworksBeforeReannealing = 500;
	protected double reannealingTemperature = 800;
	
    /**
     * Inner class for terminating a search based on a limit on the search time.
     */
	protected class TimeTerminator extends SearchTerminator {
		
	    private boolean timeRemaining = true;
		
		protected boolean checkTerminationCondition() throws Exception {
		    								
			elapsedTime = System.currentTimeMillis() - startTime;

			// Check if we reached the time or max search loop limit
			if ( maxSearchTime >= 0 && elapsedTime >= maxSearchTime ) {
			    
			    timeRemaining = false;
			}

			return timeRemaining;
		}
	}

    /**
     * Inner class for terminating a search based on a limit on the number of iterations.
     */
	protected class IterationsTerminator extends SearchTerminator {
		
		private boolean searchLoopsRemaining = true;
		
		protected boolean checkTerminationCondition() throws Exception {
		    
			if ( ( maxSearchLoops >= 0 && maxSearchLoops <= networksVisitedGlobalCounter )
				|| maxNetworksVisitedInInnerLoop == 0 ) {
			    
			    searchLoopsRemaining = false;
			}

			return searchLoopsRemaining;
		}
	}

    /**
     * Inner class for terminating a search based on a limit on the number of restarts.
     */
	protected class RestartsTerminator extends SearchTerminator {
	    
		protected boolean checkTerminationCondition() throws Exception {

		    // hjs 8/23/05 Change the logic slightly to enable test runs with 0 restarts
	        if ( acceptedPerTemperature > maxAcceptedNetworksBeforeCooling
					|| iterationsPerTemperature > maxSearchIterationsBeforeCooling
					|| maxRestarts == 0 ) {
				
			    currentTemperature *= coolingFactor;
			    
			    if ( acceptedPerTemperature < minAcceptedNetworksBeforeReannealing ) {
			        
			        // Reanneal by increasing the temperature
			        currentTemperature = reannealingTemperature;
			        
			        if ( restartCount >= maxRestarts ) {

						elapsedTime = System.currentTimeMillis() - startTime;
				        return false;
				    }
		            
				    restartCount++;
					restartsAtCounts.append( ", " + networksVisitedGlobalCounter );		        
			    }
			    acceptedPerTemperature = 0;
			    iterationsPerTemperature = 0;
			    
			    // Update the processData, and push the change,
				// so that the new currentTemperature can be used by the decider
				processData.setDynamicProcessParameter( 
				        BANJO.DATA_CURRENTTEMPERATURE, 
				        new Double( currentTemperature ).toString());
				
				// Push the change to the decider
				decider.updateProcessData( processData );
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
	        
	        checkForReanneal();
	        
			return terminator[0].checkTerminationCondition();
	    }
	}

    /**
     * Inner class for terminating a search with 2 termination criteria.
     */
	protected class DoubleTerminator extends SearchMultipleTerminator {
	    
	    protected boolean checkTerminationCondition() throws Exception {

	        checkForReanneal();
	        
	        return ( terminator[0].checkTerminationCondition() && 
	                 terminator[1].checkTerminationCondition() );
	    }
	}

    /**
     * Inner class for terminating a search with 3 termination criteria.
     */
	protected class TripleTerminator extends SearchMultipleTerminator {
	    
	    protected boolean checkTerminationCondition() throws Exception {	        

	        checkForReanneal();
	        
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
			boolean isValidChange;
			int validChangesFound = 0;
			innerLoopStart = 0;
			
			// Execute the outer loop until the stop criteria is met
			do {
			    
			    networksVisitedInnerLoopCounter = innerLoopStart;
			    
				// Execute the core search loop a specified (namely:
				// maxNetworksVisitedInInnerLoop number) of times
				while ( networksVisitedInnerLoopCounter < maxNetworksVisitedInInnerLoop ) {
													
					// hjs 11/24/04 Separated Cycle-checking from proposer class
					isValidChange = false;
					bayesNetScore = unreachableScore;
					bestScoreInInnerLoop = unreachableScore;
					
					// Get the list of changes from the proposer
					suggestedChangeList = proposer.suggestBayesNetChanges( 
					        bayesNetManager );
					
					// Process each BayesNetChange in the list
					Iterator changeListIterator = suggestedChangeList.iterator();
                    
					validChangesFound = 0;
					while ( changeListIterator.hasNext() ) {
						
						networksVisitedGlobalCounter++;
					    networksVisitedInnerLoopCounter++;
					    iterationsPerTemperature++;
												
						suggestedBayesNetChange = 
							(BayesNetChangeI) changeListIterator.next();
						
						// Note that the change will be "applied" to the underlying 
						// BayesNet structure in the cycleChecker. If we encounter a 
						// cycle, the cycleChecker will undo the change immediately.
						isValidChange = cycleChecker.isChangeValid( bayesNetManager, 
								suggestedBayesNetChange );
						    
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
						    evaluator.adjustNodeScoresForUndo( 
						            suggestedBayesNetChange );
						    
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
					            "Please check if your problem settings are feasible."
					            );
					}
					else if ( validChangesFound == 0 ) {
					    
					    // No valid changes were found. We need to restart the search
					    networksVisitedInnerLoopCounter = maxNetworksVisitedInInnerLoop;
					    System.out.println( "Resetting the inner loop counter, at iteration " + 
					            networksVisitedGlobalCounter );
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
							acceptedPerTemperature++;
						}					
						else {
						    
						    // The current network is not kept: Prepare for the next 
						    // step by reverting the structure and the cached node 
						    // scores back to the previous network
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
		    
		    // bound on number of local moves
		    return BANJO.CHANGETYPE_COUNT * varCount * varCount;
		}
	}
	
	protected class LocalSearchExecuter extends SearchExecuter {
		
		protected void executeSearch() throws Exception {
		    
			double bayesNetScore;
			boolean isValidChange;
			int attemptedTries;
			StringBuffer feedbackMessage = new StringBuffer( BANJO.BUFFERLENGTH_SMALL );

            // This lets us to properly account for the initial network
			innerLoopStart = 1;
			
			// Execute an outer loop until the user-specified stop criteria is met,
			// i.e., either time limit or max. number of search loops reached
			do {
				
			    networksVisitedInnerLoopCounter = innerLoopStart;
			    innerLoopStart = 0;
			    
				// Execute the core search loop a specified (namely:
				// maxNetworksVisitedInInnerLoop number) of times
				while ( networksVisitedInnerLoopCounter < 
				        maxNetworksVisitedInInnerLoop ) {
													
					// hjs 11/24/04 Separated Cycle-checking from proposer class
					isValidChange = false;
					attemptedTries = 0;
					bayesNetScore = 1;
                    
					while ( !isValidChange && networksVisitedInnerLoopCounter < 
					        maxNetworksVisitedInInnerLoop ) {
                        
                        if ( BANJO.DEBUG && BANJO.CONFIG_DISPLAYPROGRESS ) {
                        
                            processData.writeTraceToFile( "Proposer: inner loop=" 
                                    + networksVisitedInnerLoopCounter, 
                                    true, BANJO.CONFIG_TRACEFILE_1 );
                        }


                        
                        if ( BANJO.DEBUG && BANJO.TRACE_DFSNEW ) {
                            
                            System.out.println(
                                "\n----------------------------------------------\n" +
                                "Structure before entering Proposer:\n" +
                                bayesNetManager.getCurrentParents().toString() );
                        }
                        
						// Get the change from the proposer
						suggestedBayesNetChange = proposer.suggestBayesNetChange( 
						        bayesNetManager );
                        
                        if ( BANJO.DEBUG && BANJO.TRACE_DFSNEW ) {
                            
                            System.out.println( "\n\n  *** Proposed " + 
                                    suggestedBayesNetChange.toString() + "\n" );
                        }
							
						networksVisitedGlobalCounter++;
					    attemptedTries++;
					    networksVisitedInnerLoopCounter++;
					    iterationsPerTemperature++;
                        
						if ( suggestedBayesNetChange.getChangeStatus() == 
						    BANJO.CHANGESTATUS_READY ) {

                            if ( BANJO.DEBUG && BANJO.CONFIG_DISPLAYPROGRESS ) {
                                
                                processData.writeTraceToFile( "  **Cycle-checker", 
                                        true, BANJO.CONFIG_TRACEFILE_1 );
                            }
						    
							// Note that the change will be "applied" to the underlying 
							// BayesNet structure in the cycleChecker. If we encounter a
							// cycle, the cycleChecker will undo the change immediately.
							isValidChange = cycleChecker.isChangeValid( bayesNetManager, 
									suggestedBayesNetChange );
						}
                        
                        if ( BANJO.DEBUG && BANJO.CONFIG_DISPLAYPROGRESS ) {
                        
                            processData.writeTraceToFile( "    is change valid?  " 
                                    + isValidChange, true, BANJO.CONFIG_TRACEFILE_1 );
                        }
					}


                    if ( BANJO.DEBUG && BANJO.CONFIG_DISPLAYPROGRESS ) {

                        processData.writeTraceToFile( "Proposer: DONE", 
                                true, BANJO.CONFIG_TRACEFILE_1 );
                    }
					
					if ( isValidChange ) {

                        
                        if ( BANJO.DEBUG && BANJO.CONFIG_DISPLAYPROGRESS ) {
                        
                            processData.writeTraceToFile( "Evaluator: ready", 
                                    true, BANJO.CONFIG_TRACEFILE_1 );
                        }
                        
					  	// Since the (valid) change is already applied (to the 
						// bayesNetManager), we can compute the score
						bayesNetScore = evaluator.updateNetworkScore(
						        bayesNetManager, suggestedBayesNetChange);

                        if ( BANJO.DEBUG && BANJO.CONFIG_DISPLAYPROGRESS ) {
                        
                            processData.writeTraceToFile( "    score = " + bayesNetScore, 
                                    true, BANJO.CONFIG_TRACEFILE_1 );
                        }

                        if ( BANJO.DEBUG && BANJO.CONFIG_DISPLAYPROGRESS ) {
                        
                            processData.writeTraceToFile( "Decider: ready",
                                    true, BANJO.CONFIG_TRACEFILE_1 );
                        }
                        
						// Call the decider whether to keep the change (new highscore),
						// or to ignore it
						if ( decider.isChangeAccepted(
						        bayesNetScore, suggestedBayesNetChange )) {

                            if ( BANJO.DEBUG && BANJO.CONFIG_DISPLAYPROGRESS ) {
                            
                                processData.writeTraceToFile( "    Decider accepted " +
                                        suggestedBayesNetChange.toString(),
                                        true, BANJO.CONFIG_TRACEFILE_1 );
                            }
                            
						    highScoreSetUpdater.updateHighScoreStructureData( 
						            bayesNetScore );
							acceptedPerTemperature++;
                            
                            if ( BANJO.DEBUG && BANJO.CONFIG_DISPLAYPROGRESS ) {
                            
                                processData.writeTraceToFile( "    ---------  (Accepted) Change is applied:\n",
                                        true, BANJO.CONFIG_TRACEFILE_1 );
                            }
						}					
						else {


                            if ( BANJO.DEBUG && BANJO.CONFIG_DISPLAYPROGRESS ) {
                            
                                processData.writeTraceToFile( "    Decider rejected " +
                                        suggestedBayesNetChange.toString(),
                                        true, BANJO.CONFIG_TRACEFILE_1 );
                            }
                            
                            if ( BANJO.DEBUG && BANJO.TRACE_DFSNEW ) {
                            
                                System.out.println(
                                    "\nStructure BEFORE un-doing in SimAnneal:\n" +
                                    bayesNetManager.getCurrentParents().toString() );
                            }
                            
						    // The current network is not kept: Prepare for the next step by
						    // reverting the structure and the cached node scores back to
						    // the previous network
						    bayesNetManager.undoChange( suggestedBayesNetChange );
						    evaluator.adjustNodeScoresForUndo( suggestedBayesNetChange );
                            
                            if ( BANJO.DEBUG && BANJO.TRACE_DFSNEW ) {
                            
                                System.out.println(
                                    "\nStructure AFTER un-doing in SimAnneal:\n" +
                                    bayesNetManager.getCurrentParents().toString() );
                            }
                            
                            if ( BANJO.DEBUG && BANJO.CONFIG_DISPLAYPROGRESS ) {
                            
                                processData.writeTraceToFile( "    ---------  (Rejected) Change is undone:\n",
                                        true, BANJO.CONFIG_TRACEFILE_1 );
                            }
						}


                        if ( BANJO.DEBUG && BANJO.CONFIG_DISPLAYPROGRESS ) {
                        
                            processData.writeTraceToFile( "Decider: DONE" + BANJO.FEEDBACK_NEWLINE,
                                    true, BANJO.CONFIG_TRACEFILE_1 );
                        }
					}
				}
				
				feedbackMessage = searchTerminator.generateFeedback();
				if ( feedbackMessage.length() > 0 ) {

					searcherStatistics.recordSpecifiedData( feedbackMessage );
					feedbackMessage = new StringBuffer( BANJO.BUFFERLENGTH_SMALL );
				}


                if ( BANJO.DEBUG && BANJO.CONFIG_DISPLAYPROGRESS ) {
                
                    processData.writeTraceToFile( "SimAnneal: networksVisitedGlobalCounter = " 
                            + networksVisitedGlobalCounter + ", before checking stop-condition" 
                            + BANJO.FEEDBACK_NEWLINE,
                            true, BANJO.CONFIG_TRACEFILE_1 );
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
	    
	    protected synchronized void updateHighScoreStructureData( double bayesNetScore ) 
				throws Exception {		
			
			// Even though the naming may seem off, we need to update the 
			// "highScoreStructureSinceRestart" (this is really the current state of
			// the network as we proceed with our search; in particular, in search
			// strategies such as simulated annealing, this structure may not be a
			// high[est] scoring structure at all)
		    
			currentBestScoreSinceRestart = bayesNetScore;
			highScoreStructureSinceRestart.assignBayesNetStructure(
					bayesNetManager.getCurrentParents(), 
					currentBestScoreSinceRestart,
					networksVisitedGlobalCounter );	

		    if ( bayesNetScore > nBestThresholdScore ) {
				
				// Remove the previous high-score, then add the new network
		        // to the nBest set
				highScoreStructureSet.remove( highScoreStructureSet.last());
				highScoreStructureSet.add( new BayesNetStructure( 
						highScoreStructureSinceRestart,
						bayesNetScore, 
						networksVisitedGlobalCounter ));
				
				nBestThresholdScore = bayesNetScore;
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
		
			// Even though the naming may seem off, we need to update the 
			// "highScoreStructureSinceRestart" (this is really the current state of
			// the network as we proceed with our search; in particular, in search
			// strategies such as simulated annealing, this structure may not be a
			// high[est] scoring structure at all)
		    
			currentBestScoreSinceRestart = bayesNetScore;
			highScoreStructureSinceRestart.assignBayesNetStructure(
					bayesNetManager.getCurrentParents(), 
					currentBestScoreSinceRestart,
					networksVisitedGlobalCounter );					
			
			if ( nBestCount < nBestMax || bayesNetScore > nBestThresholdScore ) {
					
			    isEquivalent = equivalenceChecker.isEquivalent( highScoreStructureSet, 
			            highScoreStructureSinceRestart );
				
//				if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
//				    
//				    if ( isEquivalent ) 
//				        System.out.println( "- (equiv. networks)" );
//				    else
//				        System.out.println( "+ (network added)" );
//				}
		
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
     * Constructor for the simulated annealing searcher implementation.
     */
	public SearcherSimAnneal( Settings _processData ) throws Exception {
				
		super( _processData );
        
        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            BANJO.FEEDBACK_NEWLINE + BANJO.DATA_TRACEMEMORYANNEALERSTART )) );
        }

		// Validate the required settings
		boolean isDataValid = validateRequiredData();
		
		// If crucial settings could ne be validated, we can't execute the remaining
		// code in this constructor
		if ( !isDataValid ) return;

		// Set up the required (subordinate) objects
		setupSearch();
        
        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            BANJO.FEEDBACK_NEWLINE + BANJO.DATA_TRACEMEMORYANNEALEREND )) );
        }
        
        // override the feedback string
        feedbackRestarts = new String( BANJO.FEEDBACKSTRING_REANNEALS );
	}
	
	/**
	 * Validates the settings values required for the simulated annealing searcher.
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

	  	// Validate the 'max. accepted networks before cooling'
	    settingNameCanonical = BANJO.SETTING_MAXACCEPTEDNETWORKSBEFORECOOLING;
	    settingNameDescriptive = BANJO.SETTING_MAXACCEPTEDNETWORKSBEFORECOOLING_DESCR;
	    settingNameForDisplay = BANJO.SETTING_MAXACCEPTEDNETWORKSBEFORECOOLING_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_LONG;
	    validationType = BANJO.VALIDATIONTYPE_MANDATORY;
	    settingItem[0] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            null,
	            Long.toString( BANJO.DEFAULT_MAXACCEPTEDNETWORKSBEFORECOOLING ) );
	    
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
	    
	  	// Validate the 'max. search iterations before cooling'
	    settingNameCanonical = BANJO.SETTING_MAXPROPOSEDNETWORKSBEFORECOOLING;
	    settingNameDescriptive = BANJO.SETTING_MAXPROPOSEDNETWORKSBEFORECOOLING_DESCR;
	    settingNameForDisplay = BANJO.SETTING_MAXPROPOSEDNETWORKSBEFORECOOLING_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_LONG;
	    validationType = BANJO.VALIDATIONTYPE_MANDATORY;
	    settingItem[0] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            null,
	            Long.toString( BANJO.DEFAULT_MAXPROPOSEDNETWORKSBEFORECOOLING ) );
	    
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

	  	// Validate the 'min. accepted networks before reannealing'
	    settingNameCanonical = BANJO.SETTING_MINACCEPTEDNETWORKSBEFOREREANNEALING;
	    settingNameDescriptive = BANJO.SETTING_MINACCEPTEDNETWORKSBEFOREREANNEALING_DESCR;
	    settingNameForDisplay = BANJO.SETTING_MINACCEPTEDNETWORKSBEFOREREANNEALING_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_LONG;
	    validationType = BANJO.VALIDATIONTYPE_MANDATORY;
	    settingItem[0] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            null,
	            Long.toString( BANJO.DEFAULT_MINACCEPTEDNETWORKSBEFOREREANNEALING ) );
	    
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
	    
	  	// Validate the 'initial temperature'
	    settingNameCanonical = BANJO.SETTING_INITIALTEMPERATURE;
	    settingNameDescriptive = BANJO.SETTING_INITIALTEMPERATURE_DESCR;
	    settingNameForDisplay = BANJO.SETTING_INITIALTEMPERATURE_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_DOUBLE;
	    validationType = BANJO.VALIDATIONTYPE_MANDATORY;
	    settingItem[0] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            null,
	            Long.toString( BANJO.DEFAULT_INITIALTEMPERATURE ) );
	    
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

	  	// Validate the 'reannealing temperature'
	    settingNameCanonical = BANJO.SETTING_REANNEALINGTEMPERATURE;
	    settingNameDescriptive = BANJO.SETTING_REANNEALINGTEMPERATURE_DESCR;
	    settingNameForDisplay = BANJO.SETTING_REANNEALINGTEMPERATURE_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_DOUBLE;
	    validationType = BANJO.VALIDATIONTYPE_MANDATORY;
	    settingItem[0] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            null,
	            Long.toString( BANJO.DEFAULT_REANNEALINGTEMPERATURE ) );
	    
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

	  	// Validate the 'cooling factor'
	    settingNameCanonical = BANJO.SETTING_COOLINGFACTOR;
	    settingNameDescriptive = BANJO.SETTING_COOLINGFACTOR_DESCR;
	    settingNameForDisplay = BANJO.SETTING_COOLINGFACTOR_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_DOUBLE;
	    validationType = BANJO.VALIDATIONTYPE_MANDATORY;
	    settingItem[0] = processData.processSetting( settingNameCanonical, 
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            null,
	            Double.toString( BANJO.DEFAULT_COOLINGFACTOR ) );
	    
	    if ( settingItem[0].isValidSetting() ) {
	    
			try {
	
			    strCondition = new String( "greater than 0 and less than 1" );
			    dblValue[0] = Double.parseDouble( 
			            processData.getValidatedProcessParameter(
			                    settingNameCanonical ));
		        if ( dblValue[0] <= 0 || dblValue[0] >= 1 ) {
		        	
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

        String strInitialTemperature;
        
        //------------------------------------------------
        // Manage the searcher-specific process parameters
        strInitialTemperature = processData.getValidatedProcessParameter( 
                BANJO.SETTING_INITIALTEMPERATURE );
        
        currentTemperature = Double.parseDouble(strInitialTemperature);
        
        processData.setDynamicProcessParameter( 
                BANJO.DATA_CURRENTTEMPERATURE, strInitialTemperature);
        
        coolingFactor = Double.parseDouble( 
                processData.getValidatedProcessParameter( 
                        BANJO.SETTING_COOLINGFACTOR ));
        
        //----------------------------------------------
        // Create the core objects needed for the search
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
        
        // Reset the string buffer
        searcherStats = new StringBuffer( BANJO.BUFFERLENGTH_STAT_INTERNAL );
                
        // This will display the name that the user enter as settings parameter 
        strSettingChoice = StringUtil.getClassName(this);
        searcherStats.append( StringUtil.formatRightLeftJustified( 
                    newLinePlusPrefix, 
                    BANJO.SETTING_SEARCHERCHOICE_DISP, 
                    StringUtil.getClassName(this), null, lineLength ) );
        
        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            BANJO.FEEDBACK_NEWLINE + "  Annealer: before setting up core objects" )) );
        }
        
        // Set up which Proposer-Evaluator-Decider combo we want to use based on user 
        // input (i.e., via initialSettings file). We apply rules to ensure that only 
        // acceptable combinations are selected by the user, and apply appropriate 
        // defaults where necessary.
        strSettingChoice = "";
        if ( processData.getValidatedProcessParameter( 
                BANJO.SETTING_PROPOSERCHOICE )
                .equalsIgnoreCase( BANJO.UI_PROP_RANDOMLOCALMOVE )) { 
            
            this.proposer = new ProposerRandomLocalMove( bayesNetManager, processData );
            searchExecuter = new LocalSearchExecuter();
        }
        else if ( processData.getValidatedProcessParameter( 
                BANJO.SETTING_PROPOSERCHOICE )
                .equalsIgnoreCase( BANJO.UI_PROP_ALLLOCALMOVES )) { 
            
            this.proposer = new ProposerAllLocalMoves( bayesNetManager, processData );
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
        
        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            BANJO.FEEDBACK_NEWLINE + "  Annealer: after setting up proposer" )) );
        }
        
        // Evaluator
        strSettingChoice = "";
        if ( processData.getValidatedProcessParameter( 
                BANJO.SETTING_EVALUATORCHOICE )
                .equalsIgnoreCase( BANJO.UI_EVAL_BDE )) { 

            this.evaluator = new EvaluatorBDe(bayesNetManager, processData);
        }
        else {
            
            this.evaluator = new EvaluatorBDe(bayesNetManager, processData);
            
            strSettingChoice = BANJO.UI_DEFAULTEDTO_DISP;
        }
        strSettingChoice = strSettingChoice + StringUtil.getClassName( evaluator );
        searcherStats.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_EVALUATORCHOICE_DISP,
                strSettingChoice, null, lineLength ) );
        
        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            BANJO.FEEDBACK_NEWLINE + "  Annealer: after setting up evaluator" )) );
        }
        
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
        
        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            BANJO.FEEDBACK_NEWLINE + "  Annealer: after setting up cycle checker" )) );
        }
        
        // Compute the initial score for our Bayesnet
        currentBestScoreSinceRestart = evaluator.computeInitialNetworkScore(
                bayesNetManager );
        nBestThresholdScore = currentBestScoreSinceRestart;
        
        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            BANJO.FEEDBACK_NEWLINE + "  Annealer: after computing initial score" )) );
        }
        
        // Select the Decider
        strSettingChoice = "";
        if ( processData.getValidatedProcessParameter( 
                BANJO.SETTING_DECIDERCHOICE )
                .equalsIgnoreCase( BANJO.UI_DEC_METROPOLIS )) { 
            
            this.decider = new DeciderMetropolis( bayesNetManager,
                    processData, currentBestScoreSinceRestart );
        }
        else {
            
            // Default case:
            this.decider = new DeciderMetropolis( bayesNetManager,
                    processData, currentBestScoreSinceRestart );
            
            strSettingChoice = BANJO.UI_DEFAULTEDTO_DISP;
        }
        strSettingChoice = strSettingChoice + StringUtil.getClassName( decider );
        searcherStats.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_DECIDERCHOICE_DISP,
                strSettingChoice, null, lineLength ) );
        
        processData.setDynamicProcessParameter( BANJO.DATA_SEARCHERINFO_COREOBJECTS,
                searcherStats.toString() );
        
        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            BANJO.FEEDBACK_NEWLINE + "  Annealer: after setting up decider" )) );
        }
        
        // Reset the string buffer
        searcherStats = new StringBuffer( BANJO.BUFFERLENGTH_STAT_INTERNAL );
        
        
        // Load the settings specific to this searcher
        maxAcceptedNetworksBeforeCooling = Long.parseLong( 
                processData.getValidatedProcessParameter( 
                BANJO.SETTING_MAXACCEPTEDNETWORKSBEFORECOOLING ) );
        maxSearchIterationsBeforeCooling = Long.parseLong( 
                processData.getValidatedProcessParameter( 
                BANJO.SETTING_MAXPROPOSEDNETWORKSBEFORECOOLING ) );
        minAcceptedNetworksBeforeReannealing = Long.parseLong( 
                processData.getValidatedProcessParameter( 
                BANJO.SETTING_MINACCEPTEDNETWORKSBEFOREREANNEALING ) );
        reannealingTemperature = Double.parseDouble( 
                processData.getValidatedProcessParameter( 
                BANJO.SETTING_REANNEALINGTEMPERATURE ) );
                
        searcherStats.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_INITIALTEMPERATURE_DISP, 
                strInitialTemperature, null, lineLength ) );
        searcherStats.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_COOLINGFACTOR_DISP, 
                Double.toString( coolingFactor ), null, lineLength ) );

        strSettingChoice = processData.getValidatedProcessParameter( 
                BANJO.SETTING_REANNEALINGTEMPERATURE );
        searcherStats.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_REANNEALINGTEMPERATURE_DISP, 
                strSettingChoice, null, lineLength ) );
        
        strSettingChoice = Long.toString( maxAcceptedNetworksBeforeCooling );
        searcherStats.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_MAXACCEPTEDNETWORKSBEFORECOOLING_DISP,
                strSettingChoice, null, lineLength ) );
        strSettingChoice = Long.toString( maxSearchIterationsBeforeCooling );
        searcherStats.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_MAXPROPOSEDNETWORKSBEFORECOOLING_DISP,
                strSettingChoice, null, lineLength ) );
        strSettingChoice = Long.toString( minAcceptedNetworksBeforeReannealing );
        searcherStats.append( StringUtil.formatRightLeftJustified( 
                newLinePlusPrefix, BANJO.SETTING_MINACCEPTEDNETWORKSBEFOREREANNEALING_DISP, 
                strSettingChoice, null, lineLength ) );     
        
        // Set the initial network as the current best score:
        highScoreStructureSinceRestart = new BayesNetStructure( 
                bayesNetManager.getCurrentParents(), 
                currentBestScoreSinceRestart, 0 );
                
        highScoreStructureSet = new TreeSet();
        
        highScoreStructureSet.add( new BayesNetStructure( 
                highScoreStructureSinceRestart,
                currentBestScoreSinceRestart, 
                networksVisitedGlobalCounter ) );
        
        // Set up the set for storing the non-equivalent networks
        nonEquivalentHighScoreStructureSet = new TreeSet();        
        
        // Set up the container for the list of changes
        suggestedChangeList = new ArrayList( searchExecuter.sizeOfListOfChanges() );

        // Store the feedback info
        processData.setDynamicProcessParameter( BANJO.DATA_SEARCHERINFO_SPECIFICSEARCHER,
                searcherStats.toString() );
        
        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            BANJO.FEEDBACK_NEWLINE + "  Annealer: after setting up annealer objects" )) );
        }
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
     * Method used by terminator inner classes, for checking when to reanneal
     * the (simulated annealing) search.
     */
	protected void checkForReanneal() throws Exception {
	    
		// Check if we need to reanneal
		if ( acceptedPerTemperature > maxAcceptedNetworksBeforeCooling ||
				iterationsPerTemperature > maxSearchIterationsBeforeCooling ) {
		
		    currentTemperature *= coolingFactor;
		    
		    if ( acceptedPerTemperature < minAcceptedNetworksBeforeReannealing ) {
		        
		        currentTemperature = reannealingTemperature;
			    
			    restartCount++;
				restartsAtCounts.append( ", " + networksVisitedGlobalCounter );
		    }
		    acceptedPerTemperature = 0;
		    iterationsPerTemperature = 0;
		    
		    // Update the processData, and push the change,
			// so that the new currentTemperature can be used by the decider
			processData.setDynamicProcessParameter( 
			        BANJO.DATA_CURRENTTEMPERATURE, 
			        new Double(currentTemperature).toString());
			
			// Push the change to the decider
			decider.updateProcessData(processData);
		}
	}

    /**
     * Provides the valid choices for this class, here: for all compatible components.
     */
	public Object validChoices() {
	    
		Set validValues = new HashSet();
		
		// Allow a default value to be specified when there is only a single option
		// for a component
		validValues.add( BANJO.UI_DEFAULT );
		
		// Proposers
		validValues.add( BANJO.UI_PROP_RANDOMLOCALMOVE );
		validValues.add( BANJO.UI_PROP_ALLLOCALMOVES );
		
		// Evaluators
        validValues.add( BANJO.UI_EVAL_BDE );
		
		// Deciders
		validValues.add( BANJO.UI_DEC_METROPOLIS );
		
		// Recorders
		validValues.add( BANJO.UI_RECORDER_STANDARD );
		
		return validValues;
	}
}


