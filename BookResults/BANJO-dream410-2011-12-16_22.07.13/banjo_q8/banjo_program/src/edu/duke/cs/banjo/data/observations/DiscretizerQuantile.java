/*
 * Created on Nov 30, 2005
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
 * Implements a discretizer based on placing the data by quantiles.
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Nov 30, 2005
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class DiscretizerQuantile extends Discretizer {

	protected int observationsCount;

    public DiscretizerQuantile( final SortedMap _originalValuesMap,
            final int _discretizationPoints,
            final int _observationsCount ) throws Exception {
        
        super( _originalValuesMap, _discretizationPoints );
        
        observationsCount = _observationsCount;
    }

    public SortedMap computeValueMap() {
        
        Double DblCurrentObsValue;
        int discreteValue;
        long cumulativeCount;
        
        if ( originalValuesMap.size() <= discretizationPoints ) {
            
            for ( int m=0; m<mapSize; m++ ) {
                
                DblCurrentObsValue = 
		            (Double) originalValuesMap.firstKey();
		        						    
			    // Trivial assignment of the new discrete value; 
                // keep for clarity
				discreteValue = m;
				
				discreteValuesMap.put( DblCurrentObsValue, 
			                new Counter( discreteValue ));
				
		        originalValuesMap.remove( DblCurrentObsValue );
            }
        }
        else {

            DblCurrentObsValue = 
	            (Double) originalValuesMap.firstKey();
	        discreteValue = 0;

	        cumulativeCount = 
			      ( (Counter) originalValuesMap.get( 
			              DblCurrentObsValue )).i;
	        
	        discreteValuesMap.put( DblCurrentObsValue, 
			          new Counter( discreteValue ));
	        
	        originalValuesMap.remove( DblCurrentObsValue );
        
		    for ( int m=1; m<mapSize; m++ ) {
		        				         
	        
		        DblCurrentObsValue = 
		            (Double) originalValuesMap.firstKey();

				cumulativeCount += 
				      ( (Counter) originalValuesMap.get( 
				              DblCurrentObsValue )).i;
				
				while ( cumulativeCount*discretizationPoints > 
				        observationsCount*( discreteValue+1 ) && 
				          discreteValue < intervals ) {
				      
				      discreteValue++;
				}
				  
				discreteValuesMap.put( DblCurrentObsValue, 
				          new Counter( discreteValue ));
		        
		        originalValuesMap.remove( DblCurrentObsValue );
		    }
	    }
        
        return discreteValuesMap;
    }
}
