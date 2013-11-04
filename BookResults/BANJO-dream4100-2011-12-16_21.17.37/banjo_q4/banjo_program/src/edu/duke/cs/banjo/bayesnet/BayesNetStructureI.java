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
 * Documents the interface for creating a BayesNetStructure implementation. 
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Feb 21, 2005
 * 
 * @author hjs <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public interface BayesNetStructureI {

    /**
     * @param networkStructure The networkStructure (as set of edges) to assign.
     * @param networkScore The score of the network.
     * @param searchLoopIndex The search loop where the network was discovered.
     */
    public abstract void assignBayesNetStructure( EdgesI networkStructure,
            double networkScore, long searchLoopIndex ) throws Exception;
    
    
    public abstract void assignBayesNetStructure( 
            EdgesWithCachedStatisticsI networkStructure,
            double networkScore, long searchLoopIndex ) throws Exception;

    /**
     * @return Returns the networkStructure.
     */
    public abstract EdgesI getNetworkStructure();

    /**
     * @param structureToCompareTo The network structure to compare to.    
     * @return Returns the result of the comparison (where 1=greater score,
     * 0=equal score, -1=lower score).
     */
    public abstract int compareTo( Object structureToCompareTo );

    /**
     * @return The network structure in string representation.
     */
    public abstract String toString();

    /**
     * @param networkStructure The networkStructure to set.
     */
    public abstract void setNetworkStructure( EdgesI networkStructure )
            throws Exception;

    /**
     * @return Returns the networkScore.
     */
    public abstract double getNetworkScore();

    /**
     * @param networkScore The network score to set to.
     */
    public abstract void setNetworkScore( double networkScore );

    /**
     * @return Returns the search iteration where the score was found.
     */
    public abstract long getSearchLoopIndex();
}