/*
 * Created on Dec 5, 2005
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
 * Documents the interface for the data preparer classes.
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Dec 5, 2005
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public interface DataPreparerI {

    // Discretizes the data that is supplied to the data preparer
    public abstract void discretizeData() throws Exception;
    
    // Provides the discretized value for the supplied variable's original value
    public abstract int getDiscretizedValue( int variableIndex, String originalValue ) 
        throws Exception;

    // Creates a basic data report on how the original values are mapped to the
    // discretized values
    public abstract Object prepareReport() throws Exception;
}