/*
 * Created on May 14, 2004
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
import edu.duke.cs.banjo.utility.StringUtil;

/**
 * Decides whether to keep a proposed change, based on a
 * Metropolis-Hastings decision algorithm. 
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on May 14, 2004
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class DeciderMetropolis extends Decider {

	// Keep track of current "temperature" of the Metropolis-Hastings algorithm
	private double currentTemperature;
		
	public DeciderMetropolis( BayesNetManagerI _initialBayesNet, 
			final Settings _processData, final double _initialScore ) throws Exception {
		
	    super( _initialBayesNet, _processData );
	    
		this.currentScore = _initialScore;
		// Safe to parse; any potential problem has already been fixed
		this.currentTemperature = Double.parseDouble(
		        _processData.getValidatedProcessParameter( 
				        BANJO.SETTING_INITIALTEMPERATURE ) );
	}
	
	public boolean isChangeAccepted( final double _newScore,
	        BayesNetChangeI _bayesNetChange ) throws Exception {
		
	    int changeType = _bayesNetChange.getChangeType();
	    consideredChangeTypeTracker[changeType]++;
		// Method for comparing scores
		// Here: "greater or equal is better", but this could be different 
		// (also, when equal, keep the current Score)
		if ( _newScore >= currentScore ) {
			
			currentScore = _newScore;
		    betterScoreChangeTypeTracker[changeType]++;
		    
			return true;
		}
						
		// Accept a lower scoring network with a certain probability:
		if ( Math.exp(( _newScore - currentScore )/ currentTemperature) 
		        > rnd.nextDouble() ) {
			
		    currentScore = _newScore;
		    otherAcceptedChangeTypeTracker[changeType]++;
		    
			return true;
		}

		return false;
	}
	
	// The "outside" process (i.e., likely the specific searcher that uses this
	// decider) will know when the process data has changed and will let us know.
	// I.e., we depend on a push to get the data, which in this case is much more
	// efficient than if we check (pull) every time we need to use the temperature.
	public void updateProcessData( final Settings _processData ) throws Exception {
		
		this.currentTemperature = Double.parseDouble(
				_processData.getDynamicProcessParameter(
				        BANJO.DATA_CURRENTTEMPERATURE ));	
	}
	
	public StringBuffer provideCollectedStatistics() throws Exception {
		
		StringBuffer collectedStats;

		collectedStats = new StringBuffer( BANJO.BUFFERLENGTH_STAT );
		
		collectedStats.append( BANJO.FEEDBACK_NEWLINE );
		collectedStats.append( "Statistics collected in decider '" );
		collectedStats.append( StringUtil.getClassName(this) );
		collectedStats.append( "':" );
		collectedStats.append( BANJO.FEEDBACK_NEWLINE );
		collectedStats.append( "  Additions -- considered:  " );
		collectedStats.append( consideredChangeTypeTracker[BANJO.CHANGETYPE_ADDITION] );
		collectedStats.append( ",  better score:  " );
		collectedStats.append( betterScoreChangeTypeTracker[BANJO.CHANGETYPE_ADDITION] );
		collectedStats.append( ",  other accepted:  " );
		collectedStats.append( otherAcceptedChangeTypeTracker[BANJO.CHANGETYPE_ADDITION] );

		collectedStats.append( BANJO.FEEDBACK_NEWLINE );
		collectedStats.append( "  Deletions -- considered:  " );
		collectedStats.append( consideredChangeTypeTracker[BANJO.CHANGETYPE_DELETION] );
		collectedStats.append( ",  better score:  " );
		collectedStats.append( betterScoreChangeTypeTracker[BANJO.CHANGETYPE_DELETION] );
		collectedStats.append( ",  other accepted:  " );
		collectedStats.append( otherAcceptedChangeTypeTracker[BANJO.CHANGETYPE_DELETION] );
		
		if ( minMarkovLag == 0 ) {

			collectedStats.append( BANJO.FEEDBACK_NEWLINE );
			collectedStats.append( "  Reversals -- considered:  " );
			collectedStats.append( consideredChangeTypeTracker[BANJO.CHANGETYPE_REVERSAL] );
			collectedStats.append( ",  better score:  " );
			collectedStats.append( betterScoreChangeTypeTracker[BANJO.CHANGETYPE_REVERSAL] );
			collectedStats.append( ",  other accepted:  " );
			collectedStats.append( otherAcceptedChangeTypeTracker[BANJO.CHANGETYPE_REVERSAL] );
		}
		else {

			collectedStats.append( BANJO.FEEDBACK_NEWLINE );
			collectedStats.append( "  Reversals -- considered:  0 (min. Markov lag = " +
			        minMarkovLag + ")" );
		}
		
		// Append the average permissivity
		collectedStats.append( super.provideCollectedStatistics() );		
		return collectedStats;
	}
}
