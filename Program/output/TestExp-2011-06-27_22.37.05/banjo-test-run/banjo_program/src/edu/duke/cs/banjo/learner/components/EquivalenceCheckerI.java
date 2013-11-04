/*
 * Created on Jan 23, 2006
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

import java.util.Set;

import edu.duke.cs.banjo.bayesnet.BayesNetStructureI;

/**
 * Documents the interface for creating an equivalence checker implementation. 
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Jan 23, 2006
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public interface EquivalenceCheckerI {
    
    // Compare a given structure to an entire set of structures (e.g., in the set
    public abstract boolean isEquivalent( Set setOfBayesNetStructures,
            BayesNetStructureI bayesNetStructureToCheck ) throws Exception;

    public abstract StringBuffer provideCollectedStatistics() throws Exception;
}