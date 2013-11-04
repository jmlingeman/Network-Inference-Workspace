/*
 * Created on May 10, 2004
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

import edu.duke.cs.banjo.learner.SearcherI;

/**
 * Documents the interface for creating a statistics implementation for 
 * describing the progress and results of the search algorithms.
 *
 * <p><strong>Details:</strong> <br>
 * 
 * <p><strong>Change History:</strong> <br>
 * Created May 11, 2004
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 *
 */
public interface RecorderI {
	
	public abstract void recordInitialData( 
	        SearcherI searcher ) throws Exception;
	
	public abstract void recordRecurringData( 
	        SearcherI searcher ) throws Exception;
	
	public abstract void recordFinalData( 
	        SearcherI searcher ) throws Exception;
	
	public abstract void recordSpecifiedData(
	        StringBuffer dataToRecord ) throws Exception;
}