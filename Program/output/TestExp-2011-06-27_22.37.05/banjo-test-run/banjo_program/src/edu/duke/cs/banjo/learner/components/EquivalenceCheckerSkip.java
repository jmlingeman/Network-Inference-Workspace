/*
 * Created on Mar 31, 2006
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

import java.util.*;

import edu.duke.cs.banjo.bayesnet.BayesNetStructureI;
import edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI;
import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.utility.BANJO;
import edu.duke.cs.banjo.utility.StringUtil;

/**
 * Implements a "dummy" equivalence checker that lets us skip 
 * any equivalence comparisons during a search.
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Mar 31, 2006
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class EquivalenceCheckerSkip extends EquivalenceChecker {

    // Details on the checking method
    protected long mismatchInParentCount = 0; 
    protected long mismatchInSkeleton = 0; 
    protected long mismatchInVStructures = 0; 
    protected long matchDueToIdentity = 0;
    protected long matchAfterCompleteCheck = 0;
    
    public EquivalenceCheckerSkip( final Settings _processData ) throws Exception {
	
        super( _processData );
    }
    
    // Compare a given structure to an entire set of structures (e.g., in the set
    // of tracking the n-best structures)
    // Note: we can apply this method to a given set of structures, but it won't make
    // much sense to apply it within a search (instead, we only need to compare 2 
    // structures for equivalence when their scores match)
    // -- skip only
    public boolean isEquivalent( Set setOfBayesNetStructures, 
            BayesNetStructureI bayesNetStructureToCheck ) throws Exception {
        
        return false;
    }
    
    // Compare two structures - skip only
    protected boolean isEquivalent( BayesNetStructureI referenceBayesNetStructure,
            BayesNetStructureI bayesNetStructureToCheck ) throws Exception {

        return false;
    }
    
    // Compare two adjacency matrices - skip only
    protected boolean isEquivalent( 
            EdgesWithCachedStatisticsI referenceStructure,
            EdgesWithCachedStatisticsI structureToCheck ) throws Exception {

        return false;
    }

	public StringBuffer provideCollectedStatistics() throws Exception {
	
		StringBuffer collectedStats = 
			new StringBuffer( BANJO.BUFFERLENGTH_STAT );

		// Add the statistics
		if ( BANJO.CONFIG_DISPLAYSTATISTICS_EQUIVCHECK ) {
			
			collectedStats.append( BANJO.FEEDBACK_NEWLINE );
			collectedStats.append( "No Statistics collected in equivalence checker '" + 
			        StringUtil.getClassName(this) + "'." );
		}
		
		return collectedStats;
	}
}

