/*
 * Created on Sep 17, 2004
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
package edu.duke.cs.banjo.learner.components;

import java.util.ArrayList;
import java.util.List;

import edu.duke.cs.banjo.bayesnet.BayesNetChange;
import edu.duke.cs.banjo.bayesnet.BayesNetChangeI;
import edu.duke.cs.banjo.bayesnet.BayesNetManagerI;
import edu.duke.cs.banjo.bayesnet.*;
import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.utility.BANJO;
import edu.duke.cs.banjo.utility.BanjoException;
import edu.duke.cs.banjo.utility.StringUtil;

/**
 * Proposes a single potential BayesNetChange based on the current network 
 * configuration.
 * 
 * <p><strong>Details:</strong> <br>
 * Computes a single bayesNetChange by randomly selecting <br>
 * - first the changeType (add/delete/reverse) <br>
 * - second a node <br>
 * Note that the change is subject to the restrictions imposed by
 * mandatory, disallowed edges, etc.
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Sep 17, 2004 <br>
 * <p>
 * 12/20/2004 hjs 	Added a wrapper for supplying a list of changes, so this class
 * can be used within a global search (but conceivably also for future "hybrid" 
 * proposers)
 * <p>
 * 8/26/2005 (v1.0.1) hjs	Add conditions to check proposed changes against maxParentCount
 * <br>
 * 4/15/2008 (v2.2) hjs     Add additional condition for selecting reversals.
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class ProposerRandomLocalMove extends Proposer {
	
	protected final int bayesNetChangeSelectLimit;
    
    // Use "placeholder' for 0 (Because reversals are only possible for lag 0):
    final int markovLagZero = 0; 

    protected class EdgesAsMatrixSelector extends StructureSelector {

        EdgesAsMatrixWithCachedStatistics potentialParentMatrix;
        EdgesAsMatrixWithCachedStatistics mustBeAbsentParentMatrix;
        EdgesAsMatrixWithCachedStatistics addableParentMatrix;        
        
        /**
         * @return Returns the list of bayesNetChanges.
         */
        public List suggestBayesNetChanges(
                final BayesNetManagerI _bayesNetManager ) throws Exception {
            

            // Note: this method enables us to use this proposer within
            // a searcher that uses the changeList instead of a single change
            
            // Clear the list of BayesNetChanges
            changeList.clear();
            
            // This picks a single change based on a random choice method
            changeList.add( new BayesNetChange( 
                    suggestBayesNetChange(_bayesNetManager)) );
            
            return changeList;
        }

        /**
         * @return Returns the bayesNetChange.
         */
        public BayesNetChangeI suggestBayesNetChange(
            final BayesNetManagerI _bayesNetManager ) throws Exception {
        
            int randomChangeType = -1;
            int potentialParentsCount;
            int proposedParentsCount = -1;
            // 
            int combinedParentCount;
            int[] parentCountArray;
            int proposedParentIndex;
            
            int currentNodeID = -1;
            int parentNodeID = -1;
            int parentNodeLag = -1;
            
            int parentNodeIndex;
            boolean validBayesNetChange;
            int changeSelectCounter;
            int nodeSelectCounter;
            int runningIndex;
                          
            // Clear the previous bayesNetChange
            bayesNetChange.resetChange();
            
            // Loop for computing a (potential) bayesNetChange
            validBayesNetChange = false;
            changeSelectCounter = 0;
            while (!validBayesNetChange && changeSelectCounter < bayesNetChangeSelectLimit ) {
                        
                do {
                
                    // Pick a changeType at random
                    randomChangeType = rnd.nextInt( changeTypeCount ) + 1;
      
                    // 8/29/2005 (v1.0.1) hjs   Error in proposing reversals traced to 
                    // missing resets:
                    currentNodeID = -1;
                    parentNodeID = -1;
                    
                    // reset the number of proposed parent nodes
                    potentialParentsCount = 0;
                    proposedParentsCount = 0;
      
                    // reset the counter for the number of nodes we tried to select
                    nodeSelectCounter = 0;
                    
                    if ( randomChangeType == BANJO.CHANGETYPE_REVERSAL ) {
                          
                        // Get the count of the reversable edges (via the deleteable parents)
                        // for all nodes combined (Note: only lag 0 edges can be used)
                        potentialParentMatrix = 
                            ( EdgesAsMatrixWithCachedStatistics ) _bayesNetManager.getDeleteableParents();
                        mustBeAbsentParentMatrix = 
                            ( EdgesAsMatrixWithCachedStatistics ) _bayesNetManager.getMustBeAbsentParents();
                        addableParentMatrix = 
                            ( EdgesAsMatrixWithCachedStatistics ) _bayesNetManager.getAddableParents();
                        parentCountArray = potentialParentMatrix.getParentCount();
                        combinedParentCount = potentialParentMatrix.getCombinedParentCount();
                        
                        // Note that combinedParentCount is relatively small (<< varCount)
                        // so it's not easy to get a good bound for parents with lag 0
                        if ( combinedParentCount != 0 ) {
                            
                            // We now have all parents for which an edge can be reversed.
                            // We select one at random by picking its index in the list
                            // (Note: the parent list contains parents of all possible lags,
                            // but we can only use the ones with lag 0)
                            parentNodeIndex = rnd.nextInt(combinedParentCount);
                            
                            // Then find the parentNodeID that corresponds to the chosen index
                            // Remember that the lag for the node and the prospective parent 
                            // both have to be 0.
                            // Initialize to "invalid" index value
                            runningIndex = -1;
                            // Because reversals are only possible for lag 0
//                            final int markovLagZero = 0; 
                            // Go through the parents to find the (i=parentNodeIndex)-th one
                            for (int i=0; i<varCount; i++) {
                                for (int j=0; j<varCount; j++) {
      
                                    if ( potentialParentMatrix.matrix
                                            [i][j][markovLagZero] == 1 ) {
                                        
                                        runningIndex++;
                                    }
                                    
                                    if ( runningIndex == parentNodeIndex ) { 
                                        
                                        // Need to validate the potential reversal:
                                        // It's possible that the reversed edge is 
                                        // excluded via the mustNotBePresentEdges
                                        //-- 8/29/2005 (v1.0.1) hjs
                                        //-- Add condition on maxParentCount 
                                        // hjs (v2.2) Add condition against addableParentMatrix
                                        if ( mustBeAbsentParentMatrix.
                                                matrix[j][i][markovLagZero] == 0 &&
                                             addableParentMatrix.
                                                matrix[j][i][markovLagZero] == 1 && 
                                             _bayesNetManager.getCurrentParents().getParentCount( j ) 
                                                  < maxParentCount ) {
                                                                                    
                                            // Set the node and parent data
                                            currentNodeID = i;
                                            parentNodeID = j;
                                            parentNodeLag = markovLagZero;
                                        }
                                        
                                        // Indicate "done" (with or without success)
                                        j = varCount;
                                        i = varCount;
                                    }
                                }
                            }
                            
                            if ( parentNodeID != -1 ) {
                            
                                // Update the proposed parent count for the PARENT
                                proposedParentsCount = _bayesNetManager.
                                    getCurrentParents().getParentCount( parentNodeID ) + 1;
                            }
                        }
                    } // end of REVERSAL case
                    else {
                        // addition and deletion are partially combined
                        
                        if ( randomChangeType == BANJO.CHANGETYPE_ADDITION ) {
                            
                            // Get the count of the addable parents for all nodes combined
                            potentialParentMatrix = ( EdgesAsMatrixWithCachedStatistics ) 
                                     _bayesNetManager.getAddableParents();
                            combinedParentCount = potentialParentMatrix.
                                    getCombinedParentCount();
                            parentCountArray = potentialParentMatrix.getParentCount();
                            
                        } else if ( randomChangeType == BANJO.CHANGETYPE_DELETION ){
                            
                            // Get the count of the deleteable parents for all nodes combined
                            potentialParentMatrix = ( EdgesAsMatrixWithCachedStatistics ) 
                                     _bayesNetManager.getDeleteableParents();
                            combinedParentCount = potentialParentMatrix.
                                    getCombinedParentCount();
                            parentCountArray = potentialParentMatrix.getParentCount();
                            
                        } else {
                            
                            // Internal error - we should never end up in this case!
                            throw new BanjoException(
                                   BANJO.ERROR_BANJO_DEV, 
                                   " (" + StringUtil.getClassName(this) + 
                                   ".suggestBayesNetchange) " + 
                                    "Development issue: " +
                                   "Invalid bayesNetChange type (='" +
                                   randomChangeType + "'. " );
                        }
                        
                        // The 'add' and 'delete' cases can be handled together from here on
                        if ( combinedParentCount != 0 ) {
                            
                            // First select an index for the new parent
                            proposedParentIndex = rnd.nextInt( combinedParentCount ) + 1;
                            
                            try {
                            // and find the associated nodeID
                            currentNodeID = 0;
                            while ( proposedParentIndex > parentCountArray[currentNodeID] ) {
                                
                                proposedParentIndex -= parentCountArray[currentNodeID];
                                currentNodeID++;
                            }
                            }
                            catch ( Exception e ) {
                                
                                throw new Exception( e );
                            }
                            
                            nodeSelectCounter++;
            
                            // Now get the parent count for the selected node
                            potentialParentsCount = parentCountArray[currentNodeID];                    
                            
                            // Pick a parent from the available parents (pick the position of
                            // the parent, based on the number of parents)
                            parentNodeIndex = proposedParentIndex; 
                                //rnd.nextInt(potentialParentsCount) + 1;
                            
                            // Then find the parentNodeID that corresponds to the chosen 
                            // index (Since we already subtracted the "offsets" due to 
                            // other nodeIDs we only have to go through the values   
                            // associated with the currentNodeID) 
                            runningIndex = 0;
                            for (int j=0; j<varCount; j++) {
                                for (int k=minMarkovLag; k<maxMarkovLag+1; k++) {
                                    
                                    if ( potentialParentMatrix.matrix[currentNodeID][j][k]
                                               == 1 ) {
                                        
                                        runningIndex++;
                                    }
                                    
                                    if ( runningIndex == parentNodeIndex ) {
                                        
                                        // Set the parent data
                                        parentNodeID = j;
                                        parentNodeLag = k;
                                        
                                        // Indicate "done"
                                        j = varCount;
                                        k = maxMarkovLag;
                                    }
                                }
                            }
                            
                            proposedParentsCount = _bayesNetManager.
                                getCurrentParents().getParentCount(currentNodeID);
                            if ( randomChangeType == BANJO.CHANGETYPE_ADDITION )
                                proposedParentsCount++;
                        }
                    } // end - add+delete case              
                }
                while ( proposedParentsCount == 0 || 
                            proposedParentsCount > maxParentCount );
                
                if ( proposedParentsCount > 0 ) {
      
                    // Track the (proposed) change:
                    proposedChangeTypeTracker[randomChangeType]++;
      
                    bayesNetChange.updateChange(
                            currentNodeID, parentNodeID, parentNodeLag, randomChangeType);
                    bayesNetChange.setChangeStatus(
                            BANJO.CHANGESTATUS_READY);
                
                    validBayesNetChange = true;
                }
                else {
                                
                    bayesNetChange.setChangeStatus(
                            BANJO.CHANGESTATUS_NONE );
                }
        
                changeSelectCounter++;
            } // end of main "while" loop
            
            return bayesNetChange;
        }
    }    

    protected class EdgesAsArraySelector extends StructureSelector {

      protected EdgesAsArrayWithCachedStatistics potentialParentMatrix;
      protected EdgesAsArrayWithCachedStatistics addableParentMatrix;
      protected EdgesAsArrayWithCachedStatistics mustBeAbsentParentMatrix;
              
      protected final int dimVariables;
      protected final int dimParents;
      protected final int dimLags;
      protected final int offsetVariables;
      protected final int offsetParents;
      protected final int offsetLags;

      protected EdgesAsArraySelector() {
          
          dimVariables = varCount;
          dimParents = varCount;
          dimLags = maxMarkovLag - minMarkovLag + 1;
          
          // Parents are listed consecutively within each lag:
          offsetParents = 1;
          // The index "offset" between consecutive lags:
          offsetLags = dimParents;
          // The index "offset" between consecutive variables:
          offsetVariables = dimParents * dimLags;
      }
      
        /**
         * @return Returns the list of bayesNetChanges.
         */
        public List suggestBayesNetChanges(
                final BayesNetManagerI _bayesNetManager ) throws Exception {
            

            // Note: this method enables us to use this proposer within
            // a searcher that uses the changeList instead of a single change
            
            // Clear the list of BayesNetChanges
            changeList.clear();
            
            // This picks a single change based on a random choice method
            changeList.add( new BayesNetChange( 
                    suggestBayesNetChange(_bayesNetManager)) );
            
            return changeList;
        }

        /**
         * @return Returns the bayesNetChange.
         */
        public BayesNetChangeI suggestBayesNetChange(
              final BayesNetManagerI _bayesNetManager ) throws Exception {
            
            int potentialParentsCount;
            int proposedParentsCount = -1;
            int randomChangeType = -1;
            // 
            int combinedParentCount;
            int[] parentCountArray;
            int proposedParentIndex;
            
            int currentNodeID = -1;
            int parentNodeID = -1;
            int parentNodeLag = -1;
            
            int parentNodeIndex;
            boolean validBayesNetChange;
            int changeSelectCounter;
            int nodeSelectCounter;
            int runningIndex;
                    
            // Clear the previous bayesNetChange
            bayesNetChange.resetChange();
            
            // Loop for computing a (potential) bayesNetChange
            validBayesNetChange = false;
            changeSelectCounter = 0;
            while ( !validBayesNetChange && changeSelectCounter < bayesNetChangeSelectLimit ) {
                        
                do {
                
                    // Pick a changeType at random
                    randomChangeType = rnd.nextInt( changeTypeCount ) + 1;

                    // 8/29/2005 (v1.0.1) hjs   Error in proposing reversals traced to 
                    // missing resets:
                    currentNodeID = -1;
                    parentNodeID = -1;
                    
                    // reset the number of proposed parent nodes
                    potentialParentsCount = 0;
                    proposedParentsCount = 0;

                    // reset the counter for the number of nodes we tried to select
                    nodeSelectCounter = 0;
                    
                    
                    if ( randomChangeType == BANJO.CHANGETYPE_REVERSAL ) {
                                                
                        // Get the count of the reversable edges (via the deleteable parents)
                        // for all nodes combined (Note: only lag 0 edges can be used)
                        potentialParentMatrix = 
                            ( EdgesAsArrayWithCachedStatistics ) 
                               _bayesNetManager.getDeleteableParents();
                        mustBeAbsentParentMatrix = 
                            ( EdgesAsArrayWithCachedStatistics ) 
                               _bayesNetManager.getMustBeAbsentParents();
                        parentCountArray = potentialParentMatrix.getParentCount();
                        combinedParentCount = potentialParentMatrix.getCombinedParentCount();
                        // hjs (v2.2)
                        addableParentMatrix = 
                            ( EdgesAsArrayWithCachedStatistics ) 
                               _bayesNetManager.getAddableParents();
                        
                        // Note that combinedParentCount is relatively small (<< varCount)
                        // so it's not easy to get a good bound for parents with lag 0
                        if ( combinedParentCount > 0 ) {
                            
                            // We now have all parents for which an edge can be reversed.
                            // We select one at random by picking its index in the list
                            // (Note: the parent list contains parents of all possible lags,
                            // but we can only use the ones with lag 0)
                            parentNodeIndex = rnd.nextInt( combinedParentCount );
                            
                            // Then find the parentNodeID that corresponds to the chosen index
                            // Remember that the lag for the node and the prospective parent 
                            // both have to be 0.
                            // Initialize to "invalid" index value
                            runningIndex = -1;
                            // Go through the parents to find the (i=parentNodeIndex)-th one
                            for (int i=0; i<varCount; i++) {
                                for (int j=0; j<varCount; j++) {
                                    
                                    if ( potentialParentMatrix.matrix
                                         [ i*offsetVariables + markovLagZero*offsetLags + j ] 
                                              == 1 ) {
                                        
                                        runningIndex++;
                                    }
                                    
                                    if ( runningIndex == parentNodeIndex ) { 
                                        
                                        // Need to validate the potential reversal:
                                        // It's possible that the reversed edge is 
                                        // excluded via the mustNotBePresentEdges
                                        //-- 8/29/2005 (v1.0.1) hjs
                                        //-- Add condition on maxParentCount 
                                        // hjs (v2.2) Add condition against addableParentMatrix
                                        if ( mustBeAbsentParentMatrix.matrix
                                             [ j*offsetVariables + markovLagZero*offsetLags + i ]
                                                  == 0 && 
                                             addableParentMatrix.matrix
                                             [ j*offsetVariables + markovLagZero*offsetLags + i ]
                                                  == 1 &&
                                             _bayesNetManager.
                                             getCurrentParents().getParentCount( j ) < 
                                                    maxParentCount ) {
                                        
                                            // Set the node and parent data
                                            currentNodeID = i;
                                            parentNodeID = j;
                                            parentNodeLag = markovLagZero;
                                        }
                                        
                                        // Indicate "done" (with or without success)
                                        j = varCount;
                                        i = varCount;
                                    }
                                }
                            }
                            
                            if ( parentNodeID != -1 ) {
                            
                                // Update the proposed parent count for the PARENT
                                proposedParentsCount = _bayesNetManager.
                                    getCurrentParents().getParentCount(parentNodeID) + 1;
                            }
                        }
                    } // end of REVERSAL case
                    else {
                        // addition and deletion are partially combined
                        
                        if ( randomChangeType == BANJO.CHANGETYPE_ADDITION ) {
                            
                            // Get the count of the addable parents for all nodes combined
                            potentialParentMatrix = 
                                ( EdgesAsArrayWithCachedStatistics ) 
                                   _bayesNetManager.getAddableParents();
                            combinedParentCount = potentialParentMatrix.
                                    getCombinedParentCount();
                            parentCountArray = potentialParentMatrix.getParentCount();
                            
                        } else if ( randomChangeType == BANJO.CHANGETYPE_DELETION ){
                            
                            // Get the count of the deleteable parents for all nodes combined
                            potentialParentMatrix = 
                                ( EdgesAsArrayWithCachedStatistics )
                                   _bayesNetManager.getDeleteableParents();
                            combinedParentCount = potentialParentMatrix.
                                    getCombinedParentCount();
                            parentCountArray = potentialParentMatrix.getParentCount();
                            
                        } else {
                            
                            // Internal error - we should never end up in this case!
                            throw new BanjoException(
                                   BANJO.ERROR_BANJO_DEV, 
                                   " (" + StringUtil.getClassName( this ) + 
                                   ".suggestBayesNetchange) " + 
                                    "Development issue: " +
                                   "Invalid bayesNetChange type (='" +
                                   randomChangeType + "'. " );
                        }
                        
                        // The 'add' and 'delete' cases can be handled together from here on
                        if ( combinedParentCount > 0 ) {
                             
                            // First select an index for the new parent
                            proposedParentIndex = rnd.nextInt( combinedParentCount ) + 1;
//                            proposedParentIndex = rnd.nextInt( combinedParentCount );
                            
                            try {
                                // and find the associated nodeID
                                currentNodeID = 0;
                                while ( proposedParentIndex > parentCountArray[currentNodeID] ) {
                                    
                                    proposedParentIndex -= parentCountArray[currentNodeID];
                                    currentNodeID++;
                                }
                            }
                            catch ( Exception e ) {
                                
                                throw new Exception( e );
                            }
                            
                            nodeSelectCounter++;
            
                            // Now get the parent count for the selected node
                            potentialParentsCount = parentCountArray[currentNodeID];                    
                            
                            // Pick a parent from the available parents (pick the position of
                            // the parent, based on the number of parents)
                            parentNodeIndex = proposedParentIndex;
                            
                            // Then find the parentNodeID that corresponds to the chosen 
                            // index (Since we already subtracted the "offsets" due to 
                            // other nodeIDs we only have to go through the values   
                            // associated with the currentNodeID) 
                            // hjs (v2.2) Fix the starting point for the "running index"
                            runningIndex = 0;
//                            runningIndex = -1;
                            for ( int j=0; j<varCount; j++ ) {
                                for ( int k=0; k<maxMarkovLag+1-minMarkovLag; k++ ) {
                                     
                                    if ( potentialParentMatrix.matrix[ 
                                             currentNodeID*offsetVariables + k*offsetLags + j ]
                                               == 1 ) {
                                        
                                        runningIndex++;
                                    }
                                    
                                    if ( runningIndex == parentNodeIndex ) {
                                        
                                        // Set the parent data
                                        parentNodeID = j;
                                        parentNodeLag = k + minMarkovLag;
                                        
                                        // Indicate "done"
                                        j = varCount;
                                        k = maxMarkovLag;
                                    }
                                }
                            }
                            
                            proposedParentsCount = _bayesNetManager.
                                getCurrentParents().getParentCount( currentNodeID );
                            if ( randomChangeType == BANJO.CHANGETYPE_ADDITION )
                                proposedParentsCount++;
                        }
                    } // end - add+delete case              
                }
                while ( proposedParentsCount == 0 || 
                            proposedParentsCount > maxParentCount );
                
                if ( proposedParentsCount > 0 ) {

                    // Track the (proposed) change:
                    proposedChangeTypeTracker[ randomChangeType ]++;

                    bayesNetChange.updateChange(
                            currentNodeID, parentNodeID, parentNodeLag, randomChangeType );
                    bayesNetChange.setChangeStatus(
                            BANJO.CHANGESTATUS_READY );
                
                    validBayesNetChange = true;
                }
                else {
                                
                    bayesNetChange.setChangeStatus(
                            BANJO.CHANGESTATUS_NONE );
                }
        
                changeSelectCounter++;
            } // end of main "while" loop
                        
            return bayesNetChange;
        }
    }
    
	// Constructor, using the base class constructor
	public ProposerRandomLocalMove( BayesNetManagerI _initialBayesNet, 
			Settings _processData ) throws Exception {
		
		super( _initialBayesNet, _processData );
        
        if ( BANJO.CONFIG_PARENTSETS.equalsIgnoreCase( 
                BANJO.UI_PARENTSETSASARRAYS ) ) {
        
            structureSelector = new EdgesAsArraySelector();
        }
        else if ( BANJO.CONFIG_PARENTSETS.equalsIgnoreCase( 
                BANJO.UI_PARENTSETSASMATRICES ) ) {
        
            structureSelector = new EdgesAsMatrixSelector();
        }
        else {
            
            throw new BanjoException( 
                    BANJO.ERROR_BANJO_DEV,
                    "(ProposerRandomLocalMove constructor) " +
                    "Development issue: There is no code for handling the supplied type " +
                    "of parent sets." );
        }
		
		// set the limit for the number of attempts to select a bayesNetChange
		bayesNetChangeSelectLimit = BANJO.LIMITFORTRIES;
		
		// Create the list for holding the single change
		changeList = new ArrayList( 1 );
	}
	
	// hjs 12/20/04 Wrapper function for searchers that request a list of changes
	// instead of a single bayesNetChange
	public List suggestBayesNetChanges(
	        final BayesNetManagerI _bayesNetManager ) throws Exception {

        return structureSelector.suggestBayesNetChanges( _bayesNetManager );
	}
    
    public BayesNetChangeI suggestBayesNetChange(
            final BayesNetManagerI _bayesNetManager ) throws Exception {
        
        return structureSelector.suggestBayesNetChange( _bayesNetManager );
    }
}
