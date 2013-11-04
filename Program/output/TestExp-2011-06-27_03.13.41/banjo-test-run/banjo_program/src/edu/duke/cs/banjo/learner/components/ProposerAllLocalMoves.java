/*
 * Created on Dec 20, 2004
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

import edu.duke.cs.banjo.bayesnet.*;
import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.utility.BANJO;
import edu.duke.cs.banjo.utility.BanjoException;

import java.util.*;

/**
 * Proposes a list of all BayesNetChanges that can be applied in a single step (using
 * addition, deletion, or reversal of a single edge), based on the current network 
 * configuration.
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Dec 20, 2004
 * <p>
 * 8/25/2005 (v1.0.1) hjs	Add conditions to check proposed changes against maxParentCount.
 * <br>
 * 2/15/2006 (v2.0) hjs	    Properly enable reversals of edges between any 2 nodes of lag 0.
 * <br>
 * 4/15/2008 (v2.2) hjs     Add additional condition for selecting reversals.
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class ProposerAllLocalMoves extends Proposer {
	
	protected final int bayesNetChangeSelectLimit;

    private abstract class multipleMovesStructureSelector extends StructureSelector {
        
         public abstract List suggestBayesNetChanges(
                final BayesNetManagerI _bayesNetManager ) throws Exception;
         
         public BayesNetChangeI suggestBayesNetChange(
                 final BayesNetManagerI _bayesNetManager ) throws Exception {
             
             // This global proposer cannot be used to supply a single bayesNetChange only
             throw new BanjoException( 
                     BANJO.ERROR_BANJO_DEV,
                     "(ProposerAllLocalMoves.suggestBayesNetChange) " +
                     "This method is not available (by design). " +
                     "Use 'suggestBayesNetChanges' instead." );
         }
    }
    
    protected class EdgesAsMatrixSelector extends multipleMovesStructureSelector {

        /**
         * @return Returns the list of bayesNetChanges.
         */
        public List suggestBayesNetChanges(
                final BayesNetManagerI _bayesNetManager ) throws Exception {
            

          // Clear the list of BayesNetChanges
          changeList.clear();
          
          int[][] parentIDlist;
          
          // Collect all possible additions from the addableEdges
          EdgesWithCachedStatisticsI addableParents = 
              _bayesNetManager.getAddableParents();
          
          // Collect all possible deletions from the deleteableEdges
          EdgesWithCachedStatisticsI deleteableParents = 
              _bayesNetManager.getDeleteableParents();
          
          // Used for checking against the parent count limit 
          EdgesWithCachedStatisticsI currentParents = 
              _bayesNetManager.getCurrentParents();
          
          int typeCount;
                  
          // For each node, get its parentIDlist, then add a bayesNetChange 
          // for each addable parent
          for (int i=0; i< varCount; i++) {
              
              // hjs 8/23/05
              // Only add parents if we don't bump against the parentCount limit
              // for variable i
              if ( currentParents.getParentCount( i ) < maxParentCount ) {
                  
                  parentIDlist = addableParents.getCurrentParentIDlist(i, 0);
                             
                  for ( int j=0; j<parentIDlist.length; j++ ) {
                  
                      changeList.add( new BayesNetChange( i,  
                              parentIDlist[j][0], parentIDlist[j][1],
                              BANJO.CHANGETYPE_ADDITION ) );
                  }
              }
          }
          typeCount = changeList.size();
          proposedChangeTypeTracker[BANJO.CHANGETYPE_ADDITION] += typeCount;
  
          // For each node, get its parentIDlist, then add a bayesNetChange 
          // for each deleteable parent
          for (int i=0; i< varCount; i++) {
          
              parentIDlist = deleteableParents.getCurrentParentIDlist(i, 0);
              
              for ( int j=0; j<parentIDlist.length; j++ ) {
              
                  changeList.add( new BayesNetChange( i,  
                          parentIDlist[j][0], parentIDlist[j][1], 
                          BANJO.CHANGETYPE_DELETION ) );
              }
          }
          
          typeCount = changeList.size() - typeCount;      
          proposedChangeTypeTracker[BANJO.CHANGETYPE_DELETION] += typeCount;
                   
          // Collect all possible reversals from the deleteableEdges
          EdgesAsMatrixWithCachedStatistics reversableParents =
              ( EdgesAsMatrixWithCachedStatistics ) deleteableParents;
          EdgesAsMatrixWithCachedStatistics mustBeAbsentParentMatrix =
              ( EdgesAsMatrixWithCachedStatistics ) _bayesNetManager.getMustBeAbsentParents();
          EdgesAsMatrixWithCachedStatistics addableParentsMatrix =
              ( EdgesAsMatrixWithCachedStatistics ) addableParents;
          
          typeCount = changeList.size();
  
          // For each node, get its parentIDlist, then add a bayesNetChange 
          // for each reversal
          int parentIndex;
          int parentLag;
          for (int varIndex=0; varIndex< varCount; varIndex++) {
                  
              parentIDlist = reversableParents.getCurrentParentIDlist(varIndex, 0);
                  
              for ( int j=0; j<parentIDlist.length; j++ ) {
                  
                  // hjs 8/23/05
                  // Only add parents if we don't bump against the parentCount limit
                  // for the considered parent, i.e., parentIDlist[j][0]
                  parentIndex = parentIDlist[j][0];
                  parentLag = parentIDlist[j][1];
                  if ( currentParents.getParentCount( parentIndex ) < maxParentCount ) {
                      
                      // Make sure that a reversal doesn't conflict with mustBeAbsent edges
                      // (i.e., that i can actually be a parent of j!), and that the parent
                      // has lag 0:
                      // 2/15/2006 (v2.0.0) hjs Remove the (unintended) restriction to lag 0, 
                      // so that we can have reversals of edges between any nodes of lag 0 
                      // (i.e., parentIDlist[j][*1*] == 0)
                      // Note: (hjs) The 2 conditions, when combined, are sufficient!
                      // hjs (1.0.7) Use parentIndex to properly check on any parents that are 
                      // not allowed
                      // hjs (2.2) Add check against addableParents (avoid immediate cycle)
                      if ( parentLag == 0 && 
                           addableParentsMatrix.matrix[ parentIndex ][varIndex][0] == 1 &&
                           mustBeAbsentParentMatrix.matrix[ parentIndex ][varIndex][0] == 0 ) {
  
                          changeList.add( new BayesNetChange( 
                                  varIndex, parentIndex, parentLag,
                                  BANJO.CHANGETYPE_REVERSAL ) );
                      }
                  }
              }
          }
  
          // update the count
          typeCount = changeList.size() - typeCount;  
          proposedChangeTypeTracker[BANJO.CHANGETYPE_REVERSAL] += typeCount;
          
          return changeList;
      }
    
      public BayesNetChangeI suggestBayesNetChange(
              final BayesNetManagerI _bayesNetManager ) throws Exception {
          
          // This global proposer cannot be used to supply a single bayesNetChange only
          throw new BanjoException( 
                  BANJO.ERROR_BANJO_DEV,
                  "(ProposerAllLocalMoves.suggestBayesNetChange) " +
                  "This method is not available (by design). " +
                  "Use 'suggestBayesNetChanges' instead." );
      }
    }

    protected class EdgesAsArraySelector extends multipleMovesStructureSelector {

        /**
         * @return Returns the list of bayesNetChanges.
         */
        public List suggestBayesNetChanges(
                final BayesNetManagerI _bayesNetManager ) throws Exception {
          
          int[][] parentIDlist;
          
          int dimVariables;
          int dimParents;
          int dimLags;
          int offsetVariables;
          int offsetParents;
          int offsetLags;
          
          dimVariables = varCount;
          dimParents = varCount;
          dimLags = maxMarkovLag - minMarkovLag + 1;
          
          // Parents are listed consecutively within each lag:
          offsetParents = 1;
          // The index "offset" between consecutive lags:
          offsetLags = dimParents;
          // The index "offset" between consecutive variables:
          offsetVariables = dimParents * dimLags;
          
          // Clear the list of BayesNetChanges
          changeList.clear();
            
          // Collect all possible additions from the addableEdges
          EdgesAsArrayWithCachedStatistics addableParents = 
              ( EdgesAsArrayWithCachedStatistics ) _bayesNetManager.getAddableParents();
          
          // Collect all possible deletions from the deleteableEdges
          EdgesAsArrayWithCachedStatistics deleteableParents = 
              ( EdgesAsArrayWithCachedStatistics ) _bayesNetManager.getDeleteableParents();
          
          // Used for checking against the parent count limit 
          EdgesAsArrayWithCachedStatistics currentParents = 
              ( EdgesAsArrayWithCachedStatistics ) _bayesNetManager.getCurrentParents();
          
          int typeCount;
                  
          // For each node, get its parentIDlist, then add a bayesNetChange 
          // for each addable parent
          for (int i=0; i< varCount; i++) {
              
              // hjs 8/23/05
              // Only add parents if we don't bump against the parentCount limit
              // for variable i
              if ( currentParents.getParentCount( i ) < maxParentCount ) {
                  
                  parentIDlist = addableParents.getCurrentParentIDlist(i, 0);
                             
                  for ( int j=0; j<parentIDlist.length; j++ ) {
                  
                      changeList.add( new BayesNetChange( i,  
                              parentIDlist[j][0], parentIDlist[j][1],
                              BANJO.CHANGETYPE_ADDITION ) );
                  }
              }
          }
          typeCount = changeList.size();
          proposedChangeTypeTracker[BANJO.CHANGETYPE_ADDITION] += typeCount;
  
          // For each node, get its parentIDlist, then add a bayesNetChange 
          // for each deleteable parent
          for (int i=0; i< varCount; i++) {
          
              parentIDlist = deleteableParents.getCurrentParentIDlist(i, 0);
              
              for ( int j=0; j<parentIDlist.length; j++ ) {
              
                  changeList.add( new BayesNetChange( i,  
                          parentIDlist[j][0], parentIDlist[j][1], 
                          BANJO.CHANGETYPE_DELETION ) );
              }
          }
          
          typeCount = changeList.size() - typeCount;      
          proposedChangeTypeTracker[BANJO.CHANGETYPE_DELETION] += typeCount;
                    
          // Collect all possible reversals from the deleteableEdges
          EdgesAsArrayWithCachedStatistics reversableParents =
              ( EdgesAsArrayWithCachedStatistics ) deleteableParents;
          EdgesAsArrayWithCachedStatistics mustBeAbsentParentMatrix =
              ( EdgesAsArrayWithCachedStatistics ) _bayesNetManager.getMustBeAbsentParents();
          
          typeCount = changeList.size();
  
          // For each node, get its parentIDlist, then add a bayesNetChange 
          // for each reversal
          int parentIndex;
          int parentLag;
          for (int varIndex=0; varIndex< varCount; varIndex++) {
                  
              parentIDlist = reversableParents.getCurrentParentIDlist(varIndex, 0);
                  
              for ( int j=0; j<parentIDlist.length; j++ ) {
                  
                  // hjs 8/23/05
                  // Only add parents if we don't bump against the parentCount limit
                  // for the considered parent, i.e., parentIDlist[j][0]
                  parentIndex = parentIDlist[j][0];
                  parentLag = parentIDlist[j][1];
                  if ( currentParents.getParentCount( parentIndex ) < maxParentCount ) {
                      
                      // Make sure that a reversal doesn't conflict with mustBeAbsent edges
                      // (i.e., that i can actually be a parent of j!), and that the parent
                      // has lag 0:
                      // 2/15/2006 (v2.0.0) hjs Remove the (unintended) restriction to lag 0, 
                      // so that we can have reversals of edges between any nodes of lag 0 
                      // (i.e., parentIDlist[j][*1*] == 0)
                      // Note: (hjs) The 2 conditions, when combined, are sufficient!
                      // hjs (1.0.7) Use parentIndex to properly check on any parents that are 
                      // not allowed
                      // hjs (2.2) Fix check against mustBeAbsentParentMatrix ( variable
                      // references were reversed by mistake), and add check against 
                      // addableParents (to avoid immediate cycles)
                      if ( parentLag == 0 &&
                              addableParents.matrix[ 
                                     parentIndex*offsetVariables + varIndex ] == 1 &&
                              mustBeAbsentParentMatrix.matrix[ 
                                     parentIndex*offsetVariables + varIndex ] == 0 ) {
  
                          changeList.add( new BayesNetChange( 
                                  varIndex, parentIndex, parentLag,
                                  BANJO.CHANGETYPE_REVERSAL ) );
                      }
                  }
              }
          }
  
          // update the count
          typeCount = changeList.size() - typeCount;  
          proposedChangeTypeTracker[BANJO.CHANGETYPE_REVERSAL] += typeCount;
          
          return changeList;
        }
    }

    /**
     * @return Returns the bayesNetChange.
     */
    public BayesNetChangeI suggestBayesNetChange(
        final BayesNetManagerI _bayesNetManager ) throws Exception {
        
        return structureSelector.suggestBayesNetChange( _bayesNetManager );
    }
		
	// Constructor, using the base class constructor
	public ProposerAllLocalMoves( BayesNetManagerI initialBayesNet, 
			Settings processData ) throws Exception {
		
		super(initialBayesNet, processData);
        
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
                    "(ProposerAllLocalMoves constructor) " +
                    "Development issue: There is no code for handling the supplied type " +
                    "of parent sets." );
        }
		
		// set the limit for the number of attempts to select a bayesNetChange
		bayesNetChangeSelectLimit = BANJO.LIMITFORTRIES;
		
		// Create the list for holding all possible changes for the step
		changeList = new ArrayList();
	}
    
    public List suggestBayesNetChanges(
            final BayesNetManagerI _bayesNetManager ) throws Exception {
                
        return structureSelector.suggestBayesNetChanges( _bayesNetManager );        
    }
}
