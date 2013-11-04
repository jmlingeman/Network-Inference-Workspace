/*
 * Created on Nov 23, 2004
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
 * Combines common code shared by the different cycle checker implementations. 
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Nov 23, 2004
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public abstract class CycleChecker implements CycleCheckerI {

	protected BayesNetManagerI bayesNetManager;
	protected Settings processData;

	protected long consideredChangeTypeTracker[];
	protected long acyclicChangeTypeTracker[];
	
	protected BayesNetChangeI bayesNetChange = new BayesNetChange();

	public CycleChecker( BayesNetManagerI initialBayesNet, 
	        Settings processData ) throws Exception {
	    
	    this.bayesNetManager = initialBayesNet;
	    this.processData = processData;

	    // Create an array for tracking how many of the proposed bayesNetChange
	    // types are cyclic (Note that for now we won't use the [0] index of the array)
	    consideredChangeTypeTracker = new long[ BANJO.CHANGETYPE_COUNT+1 ];
		acyclicChangeTypeTracker = new long[ BANJO.CHANGETYPE_COUNT+1 ];
		for (int i=0; i<BANJO.CHANGETYPE_COUNT+1; i++) {

		    consideredChangeTypeTracker[i]=0;
		    acyclicChangeTypeTracker[i]=0;
		}
	}
	
	public abstract boolean isChangeValid( 
			BayesNetManagerI _bayesNetManager,
			BayesNetChangeI _bayesNetChange ) throws Exception ;		

	
	public void updateProcessData( Settings _processData ) throws Exception {
	    //    Placeholder for future use   
    };

	// Override this method to provide whatever statistics needs
	// to be collected in a particular cycleChecker
	public StringBuffer provideCollectedStatistics() throws Exception {
		
		StringBuffer stats;

		stats = new StringBuffer( BANJO.BUFFERLENGTH_STAT );
		stats.append( BANJO.FEEDBACK_NEWLINE );
		stats.append( "Statistics collected in cycle checker '" );
		stats.append( StringUtil.getClassName(this) );
        stats.append( "':" );
		
		int minMarkovLag = Integer.parseInt( 
		        processData.getValidatedProcessParameter(
		                BANJO.SETTING_MINMARKOVLAG ) );
		
		if ( minMarkovLag == 0 ) {

		    stats.append( BANJO.FEEDBACK_NEWLINE );
			stats.append( "  Additions -- considered:  " );
			stats.append( consideredChangeTypeTracker[
			              	BANJO.CHANGETYPE_ADDITION] );
			stats.append( ",  acyclic:  " );
			stats.append( acyclicChangeTypeTracker[
			                BANJO.CHANGETYPE_ADDITION] );
            
            stats.append( BANJO.FEEDBACK_NEWLINE );
            stats.append( "  Deletions -- no cyclicity test necessary" );

			stats.append( BANJO.FEEDBACK_NEWLINE );
			stats.append( "  Reversals -- considered:  " );
			stats.append( consideredChangeTypeTracker[
			                BANJO.CHANGETYPE_REVERSAL] );
			stats.append( ",  acyclic:  " );
			stats.append( acyclicChangeTypeTracker[
			                BANJO.CHANGETYPE_REVERSAL] );
		}
		else {
		    
		    stats.append( BANJO.FEEDBACK_NEWLINE );
			stats.append( "  Additions -- no cyclicity test necessary" );
			
			stats.append( BANJO.FEEDBACK_NEWLINE );
			stats.append( "  Deletions -- no cyclicity test necessary" );

			stats.append( BANJO.FEEDBACK_NEWLINE );
			stats.append( "  Reversals -- none proposed" );		    
		}
		
		return stats;
	}
}
