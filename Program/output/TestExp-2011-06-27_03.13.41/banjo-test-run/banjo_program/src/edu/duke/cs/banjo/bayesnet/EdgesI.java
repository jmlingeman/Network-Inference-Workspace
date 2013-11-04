/*
 * Created on Feb 21, 2005
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
 * Documents the interface for creating an Edges implementation. 
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Feb 21, 2005
 * 
 * hjs (v2.0)      Remove the method int[][][] getMatrix(), which should never have
 *                      been part of the interface.
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public interface EdgesI {
    
    // Having the getMatrix() method in the interface was a mistake, which is why it 
    // is not available anymore. It's comment section is kept for reference use only,
    // but will be removed in the next release.
    /**
     * @return Returns the matrix.
     * 
     * Note: this breaks with clean object-oriented design, but it allows
     * us to squeeze a lot of performance from the code. Use this method
     * only where absolutely necessary, since it ties your code to a matrix-
     * based implementation.
     */
    
    // Removed in Banjo 2.0 (it was really bad to have this method as part of the
    // interface in the first place)
    //public abstract int[][][] getMatrix();

    // This is not such a good name, but we introduced it in version 1.0.
    public abstract void assignMatrix( EdgesI edges ) throws Exception;

    // Access an individual entry in the set of edges
    public abstract int getEntry( int varIndex, int parentVarIndex, int lag );

    // Set an individual entry in the set of edges
    public abstract void setEntry(
            final int varIndex, final int parentVarIndex,
            final int lag, final int newValue ) throws Exception;

    // Note: can't throw exception for this method
    public abstract boolean hasIdenticalEntries( EdgesI otherEdges );

    public abstract Object clone();
}