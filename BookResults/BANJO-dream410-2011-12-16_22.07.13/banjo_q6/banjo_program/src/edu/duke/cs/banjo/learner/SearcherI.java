/*
 * Created on Apr 14, 2004
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
package edu.duke.cs.banjo.learner;

import edu.duke.cs.banjo.data.settings.Settings;

/**
 * Documents the interface for creating a searcher implementation. 
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Apr 14, 2004
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public interface SearcherI {

    /**
     * Performes a search (this is the core method for any searcher
     * implementation).
     */
    public abstract void executeSearch() throws Exception;
    
	/**
	 * Updates whatever setting within processData that is being
	 * changed in the particular searcher implementation
	 * 
	 * @param processData The data to be exchanged.
	 */
    // Note: this method is not yet "usable", because all of the current searchers
    // cannot be monitored or interrupted (e.g., using event notifications). However,
    // we anticipate this as a future enhancement.
	public abstract void updateProcessData( Settings processData ) throws Exception;
    
	/**
	 * @return Returns the statistics about the particular evaluator implementation.
	 */
	public abstract StringBuffer provideCollectedStatistics() throws Exception;
}