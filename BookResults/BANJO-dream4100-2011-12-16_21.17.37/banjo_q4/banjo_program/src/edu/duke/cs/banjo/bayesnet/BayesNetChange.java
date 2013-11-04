/*
 * Created on Mar 10, 2004
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
 * Defines a change to the BayesNet.
 *
 * <p><strong>Details:</strong> <br>
 * - Stores the ID of the current node, the ID and the lag of the parent node, 
 * and type and status of a change that can be applied to a BayesNet (as represented
 * by a BayesNetManager). <br>
 * 
 * The status value is used for tracking the proper use of a bayesNetChange within 
 * the search process. Acceptable values are: <br>
 * &nbsp&nbsp CHANGESTATUS_READY	- used when the change is assigned and 
 * 		ready to be applied <br>
 * &nbsp&nbsp CHANGESTATUS_APPLIED	- after the change has been applied to 
 * 		the bayes net <br>
 * &nbsp&nbsp CHANGESTATUS_UNDONE	- after the change has been undone (i.e., the 
 * 		change did not lead to a higher score) <br>
 * 
 * <p><strong>Change History:</strong> <br>
 * Created on Mar 10, 2004 <br>
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class BayesNetChange implements BayesNetChangeI {

	private int currentNodeID;
	private int parentNodeID;
	private int parentNodeLag;
	
	/*
	 The type of a BayesNetChange can be:
	 CHANGETYPE_ADDITION for adding the parentNode to the currentNode
	 CHANGETYPE_DELETION for removing the parentNode from the set
	 			of parents for the currentNode
	 CHANGETYPE_REVERSAL for reversing the child-parent relationship
	 			between the 2 nodes
	 */
	private int changeType;
	
	/*
	 The status of a BayesNetChange can be:
	 CHANGESTATUS_NONE	Status before all fields have been set by a proposer
	 CHANGESTATUS_READY	Status after all fields have been set by a proposer, but
				status has not been applied yet.
	 CHANGESTATUS_APPLIED	Status after the change has been applied.
	 */
	private int changeStatus;
			
	public BayesNetChange() {

		// Default constructor allows us to move to ID's instead of using
		// nodes (the rest of the program doesn't require nodes, so why not
		// optimize)
		
		this.currentNodeID = -1;
		this.parentNodeID = -1;
		this.parentNodeLag = -1;
		this.changeType = BANJO.CHANGETYPE_NONE;
		this.changeStatus = BANJO.CHANGESTATUS_NONE;
	}
	
	public BayesNetChange( int currentNodeID, 
				int _parentNodeID, int _parentNodeLag, int _changeType ) {

		this.currentNodeID = currentNodeID;
		this.parentNodeID = _parentNodeID;
		this.parentNodeLag = _parentNodeLag;
		this.changeType = _changeType;
		this.changeStatus = BANJO.CHANGESTATUS_READY;
	}

	public BayesNetChange( BayesNetChangeI _bayesNetChange ) {

		// Constructor for adding bayesNetChanges to a list, i.e.,
		// we need to create a new bayesNetChange on the fly from
		// an existing one (or a reference to one)
		
		this.currentNodeID = _bayesNetChange.getCurrentNodeID();
		this.parentNodeID = _bayesNetChange.getParentNodeID();
		this.parentNodeLag = _bayesNetChange.getParentNodeLag();
		this.changeType = _bayesNetChange.getChangeType();
		this.changeStatus = _bayesNetChange.getChangeStatus();
	}
	
	
	public void updateChange( BayesNetChangeI _bayesNetChange ) {

		this.currentNodeID = _bayesNetChange.getCurrentNodeID();
		this.parentNodeID = _bayesNetChange.getParentNodeID();
		this.parentNodeLag = _bayesNetChange.getParentNodeLag();
		this.changeType = _bayesNetChange.getChangeType();
		this.changeStatus = _bayesNetChange.getChangeStatus();
	}
	
	
	public void updateChange( int _currentNodeID, 
			int _parentNodeID, int _parentNodeLag, int _changeType ) {

		this.currentNodeID = _currentNodeID;
		this.parentNodeID = _parentNodeID;
		this.parentNodeLag = _parentNodeLag;
		this.changeType = _changeType;
		this.changeStatus = BANJO.CHANGESTATUS_READY;		
	}
	
	// Special case of update, but want to keep this as a separate method, to be able
	// to change the internal "meaning" of a reset within the search process.
	public void resetChange() {

		this.currentNodeID = -1;
		this.parentNodeID = -1;
		this.parentNodeLag = -1;
		this.changeType = BANJO.CHANGETYPE_NONE;
		this.changeStatus = BANJO.CHANGESTATUS_NONE;		
	}
		
	/**
	 * @return Returns the changeType.
	 */
	public int getChangeType() {
		return this.changeType;
	}
		
	/**
	 * @return Returns the changeStatus.
	 */
	public int getChangeStatus() {
		return this.changeStatus;
	}

	/**
	 * @param _changeStatus The changeStatus to set.
	 */
	public void setChangeStatus( int _changeStatus ) {
		this.changeStatus = _changeStatus;
	}

	/**
	 * @return Returns the currentNodeID.
	 */
	public int getCurrentNodeID() {
		return this.currentNodeID;
	}

	/**
	 * @return Returns the parentNodeID.
	 */
	public int getParentNodeID() {
		return this.parentNodeID;
	}

	/**
	 * @return Returns the parentNodeLag.
	 */
	public int getParentNodeLag() {
		return this.parentNodeLag;
	}

	// Used for debugging:
	public String toString() {
        
        String changeAsString;
        
        if ( BANJO.DEBUG ) {
            
            changeAsString = "BayesNetChange:   ";
            
            switch ( changeType ) {
                
            
            case BANJO.CHANGETYPE_ADDITION:
            
                changeAsString += "ADDITION: " + currentNodeID + "<--" + parentNodeID;
                break;
                
            
            case BANJO.CHANGETYPE_DELETION:
                
                changeAsString += "DELETION: " + currentNodeID + "<--" + parentNodeID;
                break;
                
            
            case BANJO.CHANGETYPE_REVERSAL:

                changeAsString += "REVERSAL: " + currentNodeID + "<--" + parentNodeID +
                    " to " + parentNodeID + "<--" + currentNodeID;
                break;
                                  
            default: 
            
                changeAsString += "ERROR: unknown changeType";
            }
            
            changeAsString += "; ";
            changeAsString += " nodeID = " + currentNodeID + ",  ";
            changeAsString += " parentID = " + parentNodeID + ",  "; 
            changeAsString += " parentLag = " + parentNodeLag + ",  "; 
            changeAsString += " type = " + changeType + ",  "; 
            changeAsString += " status = " + changeStatus; 
        }
        else {
            
            changeAsString = "BayesNetChange: \n  ";
            changeAsString += " nodeID = " + currentNodeID + ",\n  ";
            changeAsString += " parentID = " + parentNodeID + ",\n  "; 
            changeAsString += " parentLag = " + parentNodeLag + ",\n  "; 
            changeAsString += " type = " + changeType + ",\n  "; 
            changeAsString += " status = " + changeStatus; 
        }

        return changeAsString;
	}
}
