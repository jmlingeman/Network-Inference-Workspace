/*
 * Created on Mar 15, 2004
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

import edu.duke.cs.banjo.bayesnet.BayesNetChangeI;
import edu.duke.cs.banjo.bayesnet.BayesNetManagerI;
import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.utility.BANJO;
import edu.duke.cs.banjo.utility.StringUtil;

/**
 * Decides whether to keep a proposed change, based on a
 * greedy decision algorithm. 
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Mar 15, 2004
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class DeciderGreedy extends Decider {
		
	public DeciderGreedy( BayesNetManagerI _initialBayesNet, 
			final Settings _processData, final double _initialScore ) throws Exception {
		
	    super( _initialBayesNet, _processData );
	    
		currentScore = _initialScore;
	}
	
	public boolean isChangeAccepted(final double newScore,
	        BayesNetChangeI bayesNetChange) throws Exception {
				
	    int changeType = bayesNetChange.getChangeType();
	    consideredChangeTypeTracker[changeType]++;

	    // Method for comparing scores
		// Here: "greater or equal is better", but this could be different 
		// Equal being better is used to also allow movement through the space of
		// equivalent graphs
		if (newScore >= currentScore) {
			
			currentScore = newScore;
			
			// reset the counter
			comparisonsSinceLastHighScore = 0;
		    betterScoreChangeTypeTracker[changeType]++;

		    return true;
		}

		comparisonsSinceLastHighScore++;
		return false;
	}
	
	public StringBuffer provideCollectedStatistics() throws Exception {
		
		StringBuffer stats;

		stats = new StringBuffer( BANJO.BUFFERLENGTH_STAT );
		stats.append( "\nStatistics collected in decider '" );
		stats.append( StringUtil.getClassName(this) );
		stats.append( "':" );
		stats.append( "\n  Additions -- considered:  " );
		stats.append( consideredChangeTypeTracker[BANJO.CHANGETYPE_ADDITION] );
		stats.append( ",  better score:  " );
		stats.append( betterScoreChangeTypeTracker[BANJO.CHANGETYPE_ADDITION] );
		
		stats.append( "\n  Deletions -- considered:  " );
		stats.append( consideredChangeTypeTracker[BANJO.CHANGETYPE_DELETION] );
		stats.append( ",  better score:  " );
		stats.append( betterScoreChangeTypeTracker[BANJO.CHANGETYPE_DELETION] );
		
		if ( minMarkovLag == 0 ) {

			stats.append( "\n  Reversals -- considered:  " );
			stats.append( consideredChangeTypeTracker[BANJO.CHANGETYPE_REVERSAL] );
			stats.append( ",  better score:  " );
			stats.append( betterScoreChangeTypeTracker[BANJO.CHANGETYPE_REVERSAL] );
		}
		else {
		    
			stats.append( "\n  Reversals -- considered:  0 (min. Markov lag = " +
			        minMarkovLag + ")" );
		}

		// Append the average permissivity
		//stats.append( super.collectedStatistics() );
		return stats;
	}
}
