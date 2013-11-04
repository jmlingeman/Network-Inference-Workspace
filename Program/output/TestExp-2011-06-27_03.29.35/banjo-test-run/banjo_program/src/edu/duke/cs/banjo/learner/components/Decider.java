/*
 * Created on Apr 13, 2004
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

import java.util.Random;

import edu.duke.cs.banjo.bayesnet.BayesNetChangeI;
import edu.duke.cs.banjo.bayesnet.BayesNetManagerI;
import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.utility.BANJO;

/**
 * Combines common code shared by the different decider implementations. 
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Apr 13, 2004
 * 
 * <br>
 * 3/24/2008 (v2.2) hjs     Modify access to random numbers.
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public abstract class Decider implements DeciderI {

	// Keep track of the score of the current network
	protected double currentScore;
    protected final Random rnd;
	protected final int minMarkovLag;
	
	// Tracking basic statistics
	protected long comparisonsSinceLastHighScore;

	// Basic statistics: how many changes of each type do we consider
	protected long consideredChangeTypeTracker[];
	protected long otherAcceptedChangeTypeTracker[];
	protected long betterScoreChangeTypeTracker[];

    // Access to the global settings (static and dynamic)
    protected Settings processData;

	public Decider( final BayesNetManagerI _initialBayesNet, 
            final Settings _processData ) throws Exception {

        // Reference the associated settings
        processData = _processData;
        
        // Get access to the designated random sequence
        rnd = processData.getRandomSequence();
        
        // Initialize the comparisons counter
		comparisonsSinceLastHighScore = 0;
        
		minMarkovLag = Integer.parseInt( processData.getValidatedProcessParameter(
		        BANJO.SETTING_MINMARKOVLAG ));
		
		// Prepare the arrays for tracking the internal statistics
		// (Note: with 3 types, set via global constants to "1,2,3", we don't
		// use the 0 index (wastes a little space, but avoids index shift computation 
		// each time we update our tracking numbers)
		consideredChangeTypeTracker = new long[ BANJO.CHANGETYPE_COUNT+1 ];
		for (int i=0; i<BANJO.CHANGETYPE_COUNT+1; i++)	
		    consideredChangeTypeTracker[i]=0;
		otherAcceptedChangeTypeTracker = new long[ BANJO.CHANGETYPE_COUNT+1 ];
		for (int i=0; i<BANJO.CHANGETYPE_COUNT+1; i++)	
		    otherAcceptedChangeTypeTracker[i]=0;
		betterScoreChangeTypeTracker = new long[ BANJO.CHANGETYPE_COUNT+1 ];
		for (int i=0; i<BANJO.CHANGETYPE_COUNT+1; i++)	
		    betterScoreChangeTypeTracker[i]=0;
	}
	
	/**
	 * @return Returns whether to keep the newScore based on the bayesNetChange.
	 */
	abstract public boolean isChangeAccepted( final double _newScore,
	        BayesNetChangeI _bayesNetChange ) throws Exception;

	/**
	 * @return Returns the currentScore.
	 */
	public final double getCurrentScore() {
		return currentScore;
	}

	/**
	 * @return Returns the comparisonsSinceLastHighScore.
	 */
	public double getComparisonsSinceLastHighScore() {
		return comparisonsSinceLastHighScore;
	}
	
	/**
	 * @param _currentScore The currentScore to set.
	 */
	// Need access to the score from the outside, to be able to
	// reset the high score when we restart with a different
	// network in the bayesNetManager
	public void setCurrentScore(final double _currentScore) {
		this.currentScore = _currentScore;
	}
	
	// Not all decider subclasses will need to use this method, so declare
	// an empty one here. Used for example in the DeciderMetropolis class.
	public void updateProcessData( Settings _processData ) throws Exception {
        // Placeholder for future use};
    }
	
	/**
	 * Collects a basic set of decider-related statistics that any subclass
	 * can use as default.
	 */
	// Override this method to provide whatever statistics needs
	// to be collected in a particular decider
	public StringBuffer provideCollectedStatistics() throws Exception {
		
		StringBuffer stats;
		double num;
		double denom;
		double permissivity;

		stats = new StringBuffer( BANJO.BUFFERLENGTH_STAT );
		
		stats.append( "\n  Average permissivity:     " );
				
		num = 
	          otherAcceptedChangeTypeTracker[BANJO.CHANGETYPE_ADDITION] 
	        + otherAcceptedChangeTypeTracker[BANJO.CHANGETYPE_DELETION]
	        + otherAcceptedChangeTypeTracker[BANJO.CHANGETYPE_REVERSAL];
	    denom = 
	          consideredChangeTypeTracker[BANJO.CHANGETYPE_ADDITION]
	        + consideredChangeTypeTracker[BANJO.CHANGETYPE_DELETION]
	        + consideredChangeTypeTracker[BANJO.CHANGETYPE_REVERSAL]
	        - betterScoreChangeTypeTracker[BANJO.CHANGETYPE_ADDITION] 
	        - betterScoreChangeTypeTracker[BANJO.CHANGETYPE_DELETION]
	        - betterScoreChangeTypeTracker[BANJO.CHANGETYPE_REVERSAL];
	    
	    // Note: for strict "greedy" deciders, the count for "otherAccepted" 
	    // will be 0, because only better scores are accepted. 
	    if ( denom != 0 && num > 0 ) {
	        
	        permissivity  = Math.round( 1000 * num / denom ) ;
		    stats.append( permissivity / 1000 );
	    }
	    else {
	        
		    stats.append( "n/a" );
	    }
		
		return stats;
	}
}
