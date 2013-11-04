/*
 * Created on Apr 5, 2004
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
import edu.duke.cs.banjo.utility.*;

import java.util.Random;
import java.util.*;
/**
 * Combines common code shared by the different proposer implementations. 
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Apr 5, 2004
 * <p>
 * 2/14/2005 (v2.0) hjs     Add option to omit reversals from proposed changes (for any lag)
 * 
 * <br>
 * 3/24/2008 (v2.2) hjs     Modify access to random numbers.
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public abstract class Proposer implements ProposerI {
	
	// Reference to the underlying bayesnet representation
	protected BayesNetManagerI bayesNetManager;
	
	// Create a BayesNetChangeI per Proposer instance, so we don't have to create
	// an object copy again and again
	protected BayesNetChangeI bayesNetChange = new BayesNetChange();

	// List object for processing lists of bayesNetChanges
	protected ArrayList changeList;
	
	// Cache values that don't change during the entire search process
	protected final int varCount;
	protected final int minMarkovLag;
	protected final int maxMarkovLag;
	protected final int changeTypeCount;
	
	protected final int maxParentCount;
	
	// Basic statistics: how many changes of each type do we propose
	protected long proposedChangeTypeTracker[];

    // Access to the global settings (static and dynamic)
    protected Settings processData;

    // Get a random sequence
    protected final Random rnd;
	
	protected EdgeSelector edgeSelector;
    protected StructureSelector structureSelector;

	// Set of inner classes to efficiently handle the switching between static
	// and dynamic bayesnets
    protected abstract class EdgeSelector {
	    int selectorChangeTypeCount;
	    
        /**
         * @return Returns the selectorChangeTypeCount.
         */
        public int getSelectorChangeTypeCount() {
            return selectorChangeTypeCount;
        }	    
	}
    protected class OmitEdgeReversalSelector extends EdgeSelector {
	    
	    OmitEdgeReversalSelector() {
	        
			// Special rule for dynamic Bayesnets with min Markov lag > 0:
			// Can't select reversals, so restrict the selectorChangeTypeCount range
	        selectorChangeTypeCount = BANJO.CHANGETYPE_COUNT - 1;
	    }
	}
    protected class AllowEdgeReversalSelector extends EdgeSelector {
	    
	    AllowEdgeReversalSelector() {
	        
	        // For static bayesnets, allow all changeType values
	        selectorChangeTypeCount = BANJO.CHANGETYPE_COUNT;
	    }
	}

    protected abstract class StructureSelector {
        
        /**
         * @return Returns the list of bayesNetChanges.
         */
        public abstract List suggestBayesNetChanges(
                final BayesNetManagerI _bayesNetManager ) throws Exception;

        /**
         * @return Returns the bayesNetChange.
         */
        public abstract BayesNetChangeI suggestBayesNetChange(
              final BayesNetManagerI _bayesNetManager ) throws Exception;
    }
	
	// Constructor
	public Proposer( final BayesNetManagerI _initialBayesNet, final Settings _processData )
		throws Exception {
		
		// Associate this proposer with an underlying BayesNet
		bayesNetManager = _initialBayesNet;

        // and with the associated settings
        processData = _processData;

        // Get access to the designated random sequence
        rnd = processData.getRandomSequence();
        
		varCount = Integer.parseInt( processData.getValidatedProcessParameter(
		        BANJO.SETTING_VARCOUNT ));
		minMarkovLag = Integer.parseInt( processData.getValidatedProcessParameter(
		        BANJO.SETTING_MINMARKOVLAG ));
		maxMarkovLag = Integer.parseInt( processData.getValidatedProcessParameter(
		        BANJO.SETTING_MAXMARKOVLAG ));
		
        // Note: To manage the allowable moves (e.g., to use only additions and deletions
        // even in the static case), this is the place where to modify the code:
        
		// If the min. Markov lag is 0, we need to allow for edge reversals,
		// hence choose the "AllowEdgeReversalSelector"
		if ( minMarkovLag == 0 ) {
            
            // version 2.0: enable searches using "atomic changes" (i.e., add and del) only
            if ( BANJO.CONFIG_OMITREVERSALS ) {

                edgeSelector = new OmitEdgeReversalSelector();
            }
            else {
		    
                edgeSelector = new AllowEdgeReversalSelector();
            }
		}
		else {
            
		    edgeSelector = new OmitEdgeReversalSelector();
		}
		changeTypeCount = edgeSelector.getSelectorChangeTypeCount();
		
		String strMaxParentCount = processData.getValidatedProcessParameter( 
		        BANJO.SETTING_MAXPARENTCOUNT );
		maxParentCount = Integer.parseInt( strMaxParentCount );
				
		// Prepare the arrays for tracking the internal statistics
		// (Note: with 3 types, set via global constants to "1,2,3", we don't
		// use the 0 index (wastes space, but avoids index shift computation each time
		// we update our tracking numbers)
		proposedChangeTypeTracker = new long[ BANJO.CHANGETYPE_COUNT+1 ];
		for (int i=0; i<BANJO.CHANGETYPE_COUNT+1; i++)	
		    proposedChangeTypeTracker[i]=0;
	}
			
	// ---------------------------------------------------------------------------------
	// Core method for the individual proposer subclasses:
	// Compute a change for the given BayesNet
	// ---------------------------------------------------------------------------------
	abstract public BayesNetChangeI suggestBayesNetChange(
	        BayesNetManagerI _bayesNetManager ) throws Exception;
		
	// Placeholder for future use
	public void updateProcessData( Settings _processData ) throws Exception {
        // nothing to do   
    };
	
	// Override this method to provide whatever statistics needs
	// to be collected in a particular proposer
	public StringBuffer provideCollectedStatistics() {
		
		StringBuffer stats;

		stats = new StringBuffer( BANJO.BUFFERLENGTH_STAT );
		stats.append( BANJO.FEEDBACK_NEWLINE );
		stats.append( "Statistics collected in proposer '" );
		stats.append( StringUtil.getClassName(this) );
		stats.append( "':" );

		stats.append( BANJO.FEEDBACK_NEWLINE );
		stats.append( "  Additions -- proposed:    " );
		stats.append(  proposedChangeTypeTracker[BANJO.CHANGETYPE_ADDITION] );
		
		stats.append( BANJO.FEEDBACK_NEWLINE );
		stats.append( "  Deletions -- proposed:    " );
		stats.append( proposedChangeTypeTracker[BANJO.CHANGETYPE_DELETION] );
		
		stats.append( BANJO.FEEDBACK_NEWLINE );
		stats.append( "  Reversals -- proposed:    " );
		stats.append( proposedChangeTypeTracker[BANJO.CHANGETYPE_REVERSAL] );

		if ( minMarkovLag > 0 ) {
		
			stats.append( " (min. Markov lag = " );
			stats.append(  minMarkovLag + ")" );
		}
		
		return stats;
	}
}
