/*
 * Created on Jul 11, 2006
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
package edu.duke.cs.banjo.data.observations;

/**
 * Interface to the observations.
 *
 * <p><strong>Details:</strong> <br>
 *  - <strong>Important:</strong>
 * This class is included to complete the observations hierarchy, and should be used
 * in situations that are less performance critical.
 * [The Evaluator class uses the internal representation of specific observations 
 * implementations for performance, thus bypassing this interface] <br>
 * 
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Jul 11, 2006 <br>
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 * 
 */
public interface ObservationsI {

    public abstract void setObservationValue(
            final int observationIndex, 
            final int variableIndex, 
            final int MarkovLagIndex, 
            final int observationValue);
    
    public abstract int getObservationValue(
            final int observationIndex, final int variableIndex, final int MarkovLag );
    
    public abstract String[] getVariableNames();
    
    public abstract void setVariableNames( String[] variableNames );
}