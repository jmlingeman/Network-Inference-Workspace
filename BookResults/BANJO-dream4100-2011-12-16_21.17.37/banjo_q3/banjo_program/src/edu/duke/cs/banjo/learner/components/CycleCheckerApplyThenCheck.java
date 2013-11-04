/*
 * Created on Nov 24, 2004
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

/**
 * Checks for a cycle in the network by breadth first traversal.
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Nov 23, 2004
 * <p>
 * 2/8/2005 (v2.0) hjs   Rename to better reflect the core function of this class.
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class CycleCheckerApplyThenCheck extends CycleChecker {

	public CycleCheckerApplyThenCheck( BayesNetManagerI initialBayesNet, 
	        Settings processData ) throws Exception {
	    
	    super( initialBayesNet, processData );
	}

	public boolean isChangeValid( 
			BayesNetManagerI _bayesNetManager,
			BayesNetChangeI _bayesNetChange ) throws Exception {
	
		// code for cycle checking
		this.bayesNetManager = _bayesNetManager;
		this.bayesNetChange = _bayesNetChange;
		
		final boolean validBayesNetChange;
		int randomChangeType = _bayesNetChange.getChangeType();
		int parentNodeLag = _bayesNetChange.getParentNodeLag();
	
		// hjs 8/30/04 Revert to original approach: Simply apply the 
		// change to the network, so we can easily check for cycles
		_bayesNetManager.applyChange( _bayesNetChange );
		
		consideredChangeTypeTracker[ randomChangeType ]++;
		// Only need to check for non-Deletions, and lag = 0
		if ( randomChangeType == BANJO.CHANGETYPE_DELETION 
		        || parentNodeLag > 0
		        || !isCyclic() ) {
	
			validBayesNetChange = true;
			acyclicChangeTypeTracker[ randomChangeType ]++;
		}
		else {
		    
		    // hjs 8/30/04 Part of reverting to original approach:
		    // Undo the change from the network when we find a cycle;
		    // We will then try another change in the main search loop
		    validBayesNetChange = false;
		    _bayesNetManager.undoChange( _bayesNetChange );
		}
		
		return validBayesNetChange;
	}
	
	// Criterion (or multiple criteria) that define a valid change in the graph
	// (In our case: a simple check whether the change results in a cycle)
	protected boolean isCyclic() throws Exception {
		
		// check if bayesnet graph will be cyclic after the proposed bayesNetChange		
		boolean isCyclic;
			
		// Let the current node set determine if it contains a cycle
		isCyclic = this.bayesNetManager.getCurrentParents().
                isCyclic( bayesNetChange );
		
		return isCyclic;
	}
}
