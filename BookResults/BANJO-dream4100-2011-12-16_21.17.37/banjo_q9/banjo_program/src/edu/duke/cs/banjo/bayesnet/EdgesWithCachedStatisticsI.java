/*
 * Created on Jun 13, 2006
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

import edu.duke.cs.banjo.utility.BanjoException;

/**
 * Documents the interface for creating an EdgesAsMatrixWithCachedStatistics
 * implementation. 
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Jun 13, 2006
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public interface EdgesWithCachedStatisticsI extends EdgesI {

    // Initialize the parent matrix to all 0's, indicating "no parent" relationships.
    public abstract void initMatrix() throws Exception;

    // Initialize the parent matrix to a specified value (typically, to all '1's)
    public abstract void initMatrix( final int valueToInitTo ) throws Exception;

    public abstract void assignMatrix(
            final EdgesWithCachedStatisticsI matrixToAssign )
            throws Exception;

    public abstract void subtractMatrix(
            final EdgesWithCachedStatisticsI matrixToSubtract )
            throws Exception;

    // This method can be used in the automatic validation of a series of results 
    // such as the collection of (n-best) networks.
    public abstract void reconstructMatrix(
            final String bayesNetStructureString, final int _varCount,
            final int _minMarkovLag, final int _maxMarkovLag )
            throws BanjoException;

    // For static bayesNets only:
    public abstract void reconstructMatrixStatic(
            final String bayesNetStructureString, final int _varCount,
            final int _minMarkovLag, final int _maxMarkovLag )
            throws BanjoException;

    public abstract void setToCombinedMatrices(
            EdgesWithCachedStatisticsI matrix1,
            EdgesWithCachedStatisticsI matrix2 ) throws Exception;

    // Check if a matrix overlaps any entry with another matrix (typically used
    // to verify that the "mustBePresent" and "mustBeAbsent" matrices are
    // properly specified by the user)
    public abstract boolean hasOverlap(
            final EdgesWithCachedStatisticsI _matrix ) throws Exception;

    // This is a special method that computes a "complementary" adjacency
    // matrix (relative to both supplied arguments)
    public abstract void computeComplementaryMatrix(
            final EdgesWithCachedStatisticsI matrix1,
            final EdgesWithCachedStatisticsI matrix2 ) throws Exception;

    // Computes the list of parent ID's for the current node "nodeID"
    // "Overload" for DBN's: Note that the second argument is actually redundant,
    // but comes in handy as a visual reminder that we are in the DBN case
    // (the parent lists are only computed for "current" nodes (i.e. the lag is
    // always 0)
    public abstract int[][] getCurrentParentIDlist( 
            final int nodeID, final int lag ) throws Exception;

    // Get the number of parents for the variable with given index (var ID)
    public abstract int parentCount( final int varIndex ) throws Exception;

    // Query if a variable has another variable as a parent
    public abstract boolean isParent( 
            final int varIndex, final int parentVarIndex, final int lag ) throws Exception;

    // Set any individual entry in the parent matrix to 1
    public abstract void setEntry( 
            final int varIndex, final int parentVarIndex, final int lag ) throws Exception;

    // Set an individual entry in the parent matrix
    // This is a convenient method when merging sets of parent nodes. 
    public abstract void setEntry(
            final int varIndex, final int parentVarIndex,
            final int lag, final int newValue ) throws Exception;

    public abstract void addParent(
            final int varIndex, final int parentVarIndex, final int lag ) throws Exception;

    public abstract void deleteParent(
            final int varIndex, final int parentVarIndex, final int lag ) throws Exception;

    public abstract void reverseRelation(
            final int varIndex, final int lagVar,
            final int parentVarIndex, final int lagParent ) throws Exception;

    // Check if a node set represents a cyclic graph via a depth-first search:
    public abstract boolean isCyclic( final BayesNetChangeI bayesNetChange )
            throws Exception;

    // Check an entire parentMatrix (e.g., the mandatory parents set)
    public abstract boolean isCyclic() throws Exception;
    
    // Adjustment method for reversals (needed, e.g., for Shmueli cycle checking method)
    public void adjustForLocalChange( final int _nodeToCheck ) throws Exception;
    
    // Reset method for "global" searches using the Shmueli cycle checking
    public void resetEntireGraph() throws Exception;

    public abstract void omitNodesAsOwnParents() throws Exception;

    public abstract void omitExcludedParents() throws Exception;

    /**
     * @return Returns the parentCount.
     */
    public abstract int[] getParentCount();

    public abstract int getParentCount( int nodeID );

    /**
     * @return Returns the cycleAtNode.
     */
    public abstract int getCycleAtNode();

    // For testing/tracing only:
    // Print the entire matrix as a string, formatted with various delimiters,
    // and the parentCount at the end.
    // (Can't move this method to superclass, because parentIDs are native to 
    // EdgesAsMatrixWithCachedStatistics)
    public abstract StringBuffer toStringWithIDandParentCount();

    /**
     * @return Returns the combinedParentCount.
     */
    public abstract int getCombinedParentCount();
}