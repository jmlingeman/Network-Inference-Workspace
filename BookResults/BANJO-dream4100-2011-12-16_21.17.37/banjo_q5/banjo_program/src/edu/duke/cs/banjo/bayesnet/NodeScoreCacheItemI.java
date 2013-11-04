/*
 * Created on Feb 17, 2005
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
 * Documents the interface for creating a NodeScoreCacheItem implementation. 
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Feb 17, 2005
 * 
 * @author hjs <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public interface NodeScoreCacheItemI {

    /**
     * Assigns a cached item using the underlying parameters that
     * describe it. Note that the associated hashcode is being computed
     * within this method
     *     
     * @param nodeID The node ID of the item.
     * @param parentIDlist The list of parent IDs of the item.
     * @param nodeScore The nodeScore of the item.
     */
    public abstract void assignNodeScoreHashItem(int nodeID,
            int[][] parentIDlist, double nodeScore);

    /**
     * @return Returns the hash code of the cached item.
     */
    public abstract int getHashCode();

    /**
     * @return Returns the hash code of the cached item after it is
     * computed based on the supplied parameters.
     * 
     * @param nodeID The node ID of the item.
     * @param parentIDlist The list of parent IDs of the item.
     * 
     */
    public abstract int getHashCode(int nodeID, int[][] parentIDlist);
    
    /**
     * @return Returns the result of comparing this item to another 
     * CacheItem.
     */
    public abstract boolean equals(Object otherObj);

    /**
     * @return Returns the nodeScore.
     */
    public abstract double getNodeScore();

    /**
     * @return Returns the nodeID.
     */
    public abstract int getNodeID();

    /**
     * @return Returns the result of comparing to a supplied parent list.
     * 
     * @param listToCheck The list of parent IDs to compare with.
     */
    public abstract boolean hasIdenticalParentList(int[][] listToCheck);
}