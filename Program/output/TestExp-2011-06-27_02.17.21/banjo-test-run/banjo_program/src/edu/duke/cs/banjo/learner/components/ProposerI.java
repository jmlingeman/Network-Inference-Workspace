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

import java.util.*;

/**
 * Documents the interface for creating a proposer implementation. 
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Apr 5, 2004
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public interface ProposerI {

    /**
     * @return Returns a BayesNetChange that a proposer computes based on its algorithm.
     */
	public abstract BayesNetChangeI suggestBayesNetChange(
	        BayesNetManagerI bayesNetManager) throws Exception;
	
    /**
     * @return Returns a list of BayesNetChanges based on the proposer algorithm.
     */
	public abstract List suggestBayesNetChanges(
	        BayesNetManagerI bayesNetManager) throws Exception;
	
    /**
     * For future use.
     * 
	 * Updates whatever setting within processData that is being
	 * changed in the particular proposer implementation.
	 * 
	 * @param processData The data to be exchanged.
	 */
	public abstract void updateProcessData( Settings processData ) throws Exception;
	
	/**
     * @return Returns the statistics about the particular proposer implementation.
     */
	public abstract StringBuffer provideCollectedStatistics() throws Exception;
}