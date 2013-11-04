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
import edu.duke.cs.banjo.bayesnet.BayesNetManagerI;
import edu.duke.cs.banjo.data.settings.Settings;

/**
 * Documents the interface for creating an evaluator implementation. 
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Apr 13, 2004
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public interface EvaluatorI {
	
    /**
     * @return The score for the network described by currentBayesNetManager.
     * 
     * @param currentBayesNetManager The underlying network.
     */
	public abstract double computeInitialNetworkScore( 
			BayesNetManagerI currentBayesNetManager) throws Exception;
	
	/**
	 * Computes the score of the network described by the bayesNetManager,
	 * based on the proposed bayesNetChange.
	 * 
	 * @return Returns the score of the updated network.
	 * 
	 * @param currentBayesNetManager The current underlying network. 
	 * @param currentBayesNetChange The proposed change to the network.
	 */
	public abstract double updateNetworkScore( 
			BayesNetManagerI currentBayesNetManager, 
			BayesNetChangeI currentBayesNetChange) throws Exception;
	
	/**
     * Adjusts the various internal score containers when a bayesNetChange
     * is not permanently kept.
     * 
	 * @param suggestedBayesNetChange The change to the network whose effect needs 
	 * to be undone.
     */
	public abstract void adjustNodeScoresForUndo( 
	        BayesNetChangeI suggestedBayesNetChange ) throws Exception;
	
    /**
     * For future use.
     * 
	 * Updates whatever setting within processData that is being
	 * changed in the particular evaluator implementation.
	 * 
	 * @param processData The data to be exchanged.
	 */
	public abstract void updateProcessData( Settings processData ) throws Exception;
	
	/**
     * @return Returns the statistics about the particular evaluator implementation.
     */
	public abstract StringBuffer provideCollectedStatistics() throws Exception;
}