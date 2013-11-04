/*
 * Created on Nov 29, 2004
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

/**
 * Checks for a cycle in the network by depth first traversal.
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Nov 23, 2004
 * <p>
 * 2/8/2005 (v2.0) hjs   Rename to better reflect the core function of this class.
 * 
 * <p>
 * hjs (v2.1)           Changes to update the use of FileUtil
 *  
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class CycleCheckerCheckThenApply extends CycleChecker {
    
	public CycleCheckerCheckThenApply( BayesNetManagerI initialBayesNet, 
	        Settings processData ) throws Exception {
	    
	    super( initialBayesNet, processData );
	}

	public boolean isChangeValid( 
			BayesNetManagerI _bayesNetManager,
			BayesNetChangeI _bayesNetChange ) throws Exception {
	
		boolean validBayesNetChange = false;

        bayesNetManager = _bayesNetManager;
        bayesNetChange = _bayesNetChange;
        
        validBayesNetChange = false;
        int randomChangeType = _bayesNetChange.getChangeType();
        int parentNodeLag = _bayesNetChange.getParentNodeLag();
        boolean isCyclic;
        
        consideredChangeTypeTracker[ randomChangeType ]++;

        ////////////////////
        // Check for cycle
        ////////////////////
        // Only need to check for non-Deletions, and lag = 0
        if ( randomChangeType == BANJO.CHANGETYPE_DELETION
                || parentNodeLag > 0 
                || !isCyclic() ) {
            

            if ( BANJO.DEBUG && BANJO.CONFIG_DISPLAYPROGRESS ) {
            
                processData.writeTraceToFile( "    --- NOT Cyclic: Before Applying change, BN=\n" + 
                        _bayesNetManager.getCurrentParents().toString(), 
                        true, BANJO.CONFIG_TRACEFILE_1 );
            }

            ////////////////////
            // Apply the change
            ////////////////////
            // For Shmueli's algorithm we don't apply the change until after 
            // the cycle checking, and of course we only apply it when there's no cycle
            _bayesNetManager.applyChange( _bayesNetChange );
                                
            validBayesNetChange = true;
            acyclicChangeTypeTracker[ randomChangeType ]++;
            
            if ( BANJO.DEBUG && BANJO.CONFIG_DISPLAYPROGRESS ) {
            
                processData.writeTraceToFile( "    --- NOT Cyclic: After applying change, BN=\n" + 
                        _bayesNetManager.getCurrentParents().toString(), 
                        true, BANJO.CONFIG_TRACEFILE_1 );
            }
        }
        else {

            // No 'else' ncessary, since there is no need to 'undo' (we only apply when 
            // the change would not complete a cycle)
        }
        
        return validBayesNetChange; 
	}
	
	// Criterion (or multiple criteria) that define a valid change in the graph
	// (In our case: a simple check whether the change results in a cycle)
	protected boolean isCyclic() throws Exception {
		
		// check if bayesnet graph will be cyclic after the proposed bayesNetChange		
		boolean isCyclic;

        if ( BANJO.DEBUG && BANJO.CONFIG_DISPLAYPROGRESS ) {

            processData.writeTraceToFile( "    Calling getCurrentParents.isCyclicDFS", 
                    true, BANJO.CONFIG_TRACEFILE_1 );
            processData.writeTraceToFile( "    currentParents:", 
                    true, BANJO.CONFIG_TRACEFILE_1 );
            processData.writeTraceToFile( bayesNetManager.getCurrentParents().toString(), 
                    true, BANJO.CONFIG_TRACEFILE_1 );
        }
        
        // Let the current node set determine if it contains a cycle
        isCyclic = this.bayesNetManager.getCurrentParents().
                isCyclic( bayesNetChange );
		
		return isCyclic;
	}
}
