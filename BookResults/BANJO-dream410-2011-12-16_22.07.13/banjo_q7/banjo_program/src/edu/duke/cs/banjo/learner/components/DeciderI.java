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

import edu.duke.cs.banjo.bayesnet.BayesNetChangeI;
import edu.duke.cs.banjo.data.settings.Settings;

/**
 * Documents the interface for creating a decider implementation. 
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Apr 13, 2004
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public interface DeciderI {

	/**
	 * @return Determines whether to keep the proposed bayesNetChange, based
	 * on the newScore. Uses its internal knowledge of the current score
	 * together with its algorithm.
	 * 
	 * @param newScore The score of the network. 
	 * @param bayesNetChange The change to the network.
	 */
    public abstract boolean isChangeAccepted( 
            final double newScore, 
	        BayesNetChangeI bayesNetChange ) throws Exception;
    	
	/**
	 * Sets the score of the current network.
	 * 
	 * @param currentScore The score of the current network.
	 */
	// Note: this method is needed for properly resetting the decider when we
	// restart a search with a different network.
	public abstract void setCurrentScore( final double currentScore ) 
			throws Exception;
	
	/**
	 * Updates whatever setting within processData that is being
	 * changed in the particular decider implementation (usually called from 
	 * the overarching search method; See, e.g., the exchange of data between
	 * SearcherSimAnneal and DeciderMetropolis).
	 * 
	 * @param processData The data to be exchanged.
	 */
	public abstract void updateProcessData( Settings processData ) throws Exception;
	
	/**
     * @return The statistics about the particular decider implementation.
     */
	public abstract StringBuffer provideCollectedStatistics() throws Exception;
}