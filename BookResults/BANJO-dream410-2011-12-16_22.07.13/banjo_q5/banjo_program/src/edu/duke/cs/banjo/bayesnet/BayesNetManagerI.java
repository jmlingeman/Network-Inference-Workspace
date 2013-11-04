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
package edu.duke.cs.banjo.bayesnet;

/**
 * 
 * Documents the interface for the BayesNetManager.
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Apr 14, 2004
 * 
 * <br>
 * 4/2/2008 (v2.2)  hjs     Eliminate methods relating to (newer used) nodes scores
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public interface BayesNetManagerI {

	public abstract void initializeBayesNet() throws Exception;
	public abstract void initializeBayesNet( 
            EdgesWithCachedStatisticsI initialMatrix ) 
			throws Exception;
	
	public abstract void applyChange(
	        BayesNetChangeI bayesNetChange ) throws Exception;
	public abstract void undoChange(
	        BayesNetChangeI bayesNetChange ) throws Exception;
        
    public abstract EdgesWithCachedStatisticsI getCurrentParents();
    public abstract void setCurrentParents(
            EdgesWithCachedStatisticsI currentParents ) 
            throws Exception;
	       
	/**
	 * @return Returns the addableNodes.
	 */
    public abstract EdgesWithCachedStatisticsI getAddableParents();
	/**
	 * @return Returns the deleteableNodes.
	 */
    public abstract EdgesWithCachedStatisticsI getDeleteableParents();
	/**
	 * @return Returns the mustBeAbsentParents.
	 */
    public abstract EdgesWithCachedStatisticsI getMustBeAbsentParents();
	/**
	 * @return Returns the mustBePresentParents.
	 */
    public abstract EdgesWithCachedStatisticsI getMustBePresentParents();
}