/*
 * Created on Jan 23, 2006
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

import java.util.Set;

import edu.duke.cs.banjo.bayesnet.BayesNetStructureI;
import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.utility.BANJO;

/**
 * Combines common code shared by the different equivalence checker implementations. 
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Jan 23, 2006
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public abstract class EquivalenceChecker implements EquivalenceCheckerI {
    
    // Access to the search process data
    protected final Settings processData;
    
    // Cached data
    protected final int varCount;
    protected final int minMarkovLag;
    protected final int maxMarkovLag;

    // Internal data: tracking the equivalence checks
    protected long equivalenceCheckCount = 0; 
    
    public EquivalenceChecker( final Settings _processData ) throws Exception {
    	
	    processData  = _processData;

	    varCount = Integer.parseInt( processData.getValidatedProcessParameter( 
	            BANJO.SETTING_VARCOUNT ));
	    minMarkovLag = Integer.parseInt( processData.getValidatedProcessParameter( 
	            BANJO.SETTING_MINMARKOVLAG ));
	    maxMarkovLag = Integer.parseInt( processData.getValidatedProcessParameter( 
	            BANJO.SETTING_MAXMARKOVLAG ));
    }

    /* (non-Javadoc)
     * @see edu.duke.cs.banjo.learner.components.EquivalenceCheckerI#isEquivalent(
     * 			java.util.Set, edu.duke.cs.banjo.bayesnet.BayesNetStructureI )
     */
    public abstract boolean isEquivalent( Set setOfBayesNetStructures,
            BayesNetStructureI bayesNetStructureToCheck ) throws Exception;

    /* (non-Javadoc)
     * @see edu.duke.cs.banjo.learner.components.EquivalenceCheckerI#provideCollectedStatistics()
     */
    public abstract StringBuffer provideCollectedStatistics() throws Exception;

}
