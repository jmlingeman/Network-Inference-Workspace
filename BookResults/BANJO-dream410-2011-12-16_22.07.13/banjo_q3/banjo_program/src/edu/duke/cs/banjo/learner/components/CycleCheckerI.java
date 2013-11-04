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


/**
 * Documents the interface for creating a cycle checker implementation. 
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Nov 23, 2004
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public interface CycleCheckerI {
	
	/**
	 * Checks whether the proposed bayesNetChange results in a cycle
	 * in the network described by the bayesNetManager.
	 * 
	 * @return Returns the boolean that indicates whether the change can be
	 * applied.
	 * 
	 * @param bayesNetManager The current underlying network. 
	 * @param bayesNetChange The proposed change to the network.
	 */
    public abstract boolean isChangeValid( 
			BayesNetManagerI bayesNetManager,
			BayesNetChangeI bayesNetChange ) throws Exception;
		
    /**
     * For future use.
     * 
	 * Updates whatever setting within processData that is being
	 * changed in the particular cycleChecker implementation.
	 * 
	 * @param processData The data to be exchanged.
	 */
	public abstract void updateProcessData( Settings processData ) throws Exception;
	
	/**
     * @return Returns the statistics about the particular cyclechecker implementation.
     */
	public abstract StringBuffer provideCollectedStatistics() throws Exception;
}
