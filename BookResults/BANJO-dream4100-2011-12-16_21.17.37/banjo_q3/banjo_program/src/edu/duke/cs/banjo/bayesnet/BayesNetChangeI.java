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
 * Documents the interface for a BayesNetChange.
 *
 * <p><strong>Details:</strong> <br>
 * 
 * <p><strong>Change History:</strong> <br>
 * Created on Apr 14, 2004
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public interface BayesNetChangeI {
    
	
	/**
	 * Sets a BayesNetChange by assigning the currentNodeID, the parentNodeID,
	 * the parentNodeLag, and the changeType. The individual values for these
	 * fields are always associated with a single BayesNetChange, and there is 
	 * no use for setting them separately. 
	 * 
	 * @param currentNodeID The currentNodeID to set.
	 * @param parentNodeID The parentNodeID to set.
	 * @param parentNodeLag The parentNodeLag to set.
	 * @param changeType The changeType to set.
	 */
	public abstract void updateChange(
		int currentNodeID,
		int parentNodeID,
		int parentNodeLag,
		int changeType);
	
	/**
	 * Special case of update. From the point of process logic, we keep this
	 * method separate from the general assignment of a BayesNetChange.
	 */
	public abstract void resetChange();
	
	/**
	 * The status of a BayesNetChange may change over the search process. We use
	 * this field as an internal check mechanism within the process flow.
	 * 
	 * @param changeStatus The changeStatus to set.
	 */
	public abstract void setChangeStatus(int changeStatus);
	
	/**
	 * @return Returns the currentNodeID.
	 */
	public abstract int getCurrentNodeID();
	/**
	 * @return Returns the parentNodeID.
	 */
	public abstract int getParentNodeID();
	/**
	 * @return Returns the parentNodeLag.
	 */
	public abstract int getParentNodeLag();
	/**
	 * @return Returns the changeStatus.
	 */
	public abstract int getChangeStatus();
	/**
	 * @return Returns the changeType.
	 */
	public abstract int getChangeType();
}