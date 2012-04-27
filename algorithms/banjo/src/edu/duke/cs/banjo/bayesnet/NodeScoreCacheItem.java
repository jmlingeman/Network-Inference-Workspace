/*
 * Created on May 4, 2004
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

import edu.duke.cs.banjo.utility.*;

/**
 * Implements storing the (already computed) node scores in a hash.
 *
 * <p><strong>Details:</strong> <br>
 * - Computes the hash code needed for storing an item in a hash <br>
 * - Implements comparison ('equal') between items <br>
 * 
 * <p><strong>Change History:</strong> <br>
 * Created on May 4, 2004
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class NodeScoreCacheItem implements NodeScoreCacheItemI {
	
	private int nodeID;
	private int[][] parentIDlist;
	private double nodeScore;
	private int hashCode;

    /**
     * Basic constructor for creating a cached item. Any object created
     * with this constructor will need to get assigned a "valid" cache item.
     */	
	public NodeScoreCacheItem() {
	    
	    // This constructor is only used to create a "place holder"
	    // object, so we want to assign data that would raise a flag
	    nodeID = -1;
	    nodeScore = 1;
	    // leave parentIDlist and hashCode null
	}

    /**
     * constructor for creating a cached item from the set of underlying
     * parameters.
     * 
     * @param _nodeID The node ID of the item.
     * @param _parentIDlist The list of parent IDs of the item.
     * @param _nodeScore The nodeScore of the item.
     * 
     */
	public NodeScoreCacheItem(
	        final int _nodeID, 
	        final int[][] _parentIDlist, 
	        final double _nodeScore ) {
					    
		this.nodeID = _nodeID;
		this.nodeScore = _nodeScore;

		this.parentIDlist = _parentIDlist;
		
		this.hashCode = getHashCode( nodeID, parentIDlist );	
	}
	
	/**
     * Assigns a cached item using the underlying parameters that
     * describe it. Note that the associated hashcode is being computed
     * within this method
     *     
     * @param _nodeID The node ID of the item.
     * @param _parentIDlist The list of parent IDs of the item.
     * @param _nodeScore The nodeScore of the item.
     */
	public void assignNodeScoreHashItem(
	        final int _nodeID, 
	        final int[][] _parentIDlist, 
	        final double _nodeScore ) {
			    
		this.nodeScore = _nodeScore;
		this.nodeID = _nodeID;
		
		this.parentIDlist = _parentIDlist;
		
		this.hashCode = getHashCode( nodeID, parentIDlist );
	}
	
	/**
     * @return Returns the hash code of the cached item.
     */
    public int getHashCode() {
		
		return hashCode;
	}

    /**
     * @return Returns the hash code of the cached item after it is
     * computed based on the supplied parameters.
     * 
     * @param _nodeID The node ID of the item.
     * @param _parentIDlist The list of parent IDs of the item.
     * 
     */
	public int getHashCode( final int _nodeID, final int[][] _parentIDlist) {
		
		// Compute a suitable hash code from the data
		// describing the object:

        hashCode = 0;
        
        for (int i=0; i<_parentIDlist.length; i++) {
            hashCode += BANJO.HASHGENERATORFACTOR * _parentIDlist[i][0];
        }
        hashCode += BANJO.HASHGENERATORFACTOR * _nodeID;

        return hashCode;
	}

    /**
     * @return Returns the result of comparing this item to another 
     * cached item.
     */
	public boolean equals( final Object _objToCompareTo ) {
		
		// Quick check for identity
		if (this == _objToCompareTo) return true;
		
		// Need to be an actual object
		if (_objToCompareTo == null) return false;
		
		// and need to be of the same class
		if (_objToCompareTo.getClass() != this.getClass() ) return false;
		
		NodeScoreCacheItem scoreHashForObjToCompareTo = (NodeScoreCacheItem) _objToCompareTo;
		// All data members need to match		
		if ( scoreHashForObjToCompareTo.parentIDlist != null ) {
		    
			return (scoreHashForObjToCompareTo.nodeID == this.nodeID &&
					scoreHashForObjToCompareTo.hasIdenticalParentList( this.parentIDlist ) &&
					scoreHashForObjToCompareTo.nodeScore == this.nodeScore);
		}
		    
	    return false;
	}

    /**
     * @return A custom string representation for a cached item.
     * 
     * Note: Used internally for testing only. (Inefficient)
     */
	public String toString(){
		
		String description;
		
		description = "Node = " + nodeID + "; parents = ";
		for (int i = 0; i< parentIDlist.length-1; i++) {
			
			description += " id=" + parentIDlist[i][0] 
			                + " lag=" + parentIDlist[i][1] + ", "; 
		}
		description += " id=" + parentIDlist[parentIDlist.length-1][0]
		           + " lag=" + parentIDlist[parentIDlist.length-1][1] + ";";
		description += "  score = " + nodeScore;
		
		return description;
	}
	
	/**
	 * @return Returns the nodeScore.
	 */
	public double getNodeScore() {
		return nodeScore;
	}
	
    /**
     * @return Returns the nodeID.
     */
    public int getNodeID() {
        return nodeID;
    }

    /**
     * @return Returns the result of comparing to a supplied parent list.
     * 
     * @param listToCheck The list of parent IDs to compare with.
     */
    public boolean hasIdenticalParentList(final int[][] listToCheck){
		
        // Quick check if the length of both arrays match 
        if ( this.parentIDlist.length != listToCheck.length ) 
            return false;
        
        // If lengths match, check entry by entry
        // Note: parentIDlistStatic are in "sorted" order; currently they are automatically
        // in sorted order due to the way they are computed
		for (int i=0; i<this.parentIDlist.length; i++) {
			if ( this.parentIDlist[i][0] != listToCheck[i][0] || 
			        this.parentIDlist[i][1] != listToCheck[i][1] ) {
				return false;
			}
		}	
		return true;
	}
}
