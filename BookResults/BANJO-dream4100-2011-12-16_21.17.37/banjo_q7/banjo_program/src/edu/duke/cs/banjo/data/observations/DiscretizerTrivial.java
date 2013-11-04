/*
 * Created on Feb 15, 2006
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

import java.util.SortedMap;

/**
 * Implements a "dummy" discretizer when we don't need to discretize the data.
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Feb 15, 2006
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class DiscretizerTrivial extends Discretizer {

	protected int observationsCount;

    public DiscretizerTrivial( final SortedMap _originalValuesMap,
            final int _discretizationPoints,
            final int _observationsCount ) throws Exception {
        
        super( _originalValuesMap, _discretizationPoints );
        
        observationsCount = _observationsCount;
    }

    public SortedMap computeValueMap() {
        
        Double DblCurrentObsValue;
        
        // This iterates through the original values and places them into the
        // container for the discrete values 
        for ( int m=0; m<mapSize; m++ ) {
            
            DblCurrentObsValue = 
	            (Double) originalValuesMap.firstKey();
			
			discreteValuesMap.put( DblCurrentObsValue, 
		                new Counter( m ));
			
	        originalValuesMap.remove( DblCurrentObsValue );
        }
    
        return discreteValuesMap;
    }
}
