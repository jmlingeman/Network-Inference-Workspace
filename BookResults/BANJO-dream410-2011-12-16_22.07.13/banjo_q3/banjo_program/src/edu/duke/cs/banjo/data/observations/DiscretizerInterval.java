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

import java.util.*;

/**
 * Implements a discretizer based on placing the data by intervals.
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Nov 30, 2005
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class DiscretizerInterval extends Discretizer {
    
    public DiscretizerInterval( final SortedMap _originalValuesMap, 
            final int _discretizationPoints ) throws Exception {
        
        super( _originalValuesMap, _discretizationPoints );
    }
    
    public SortedMap computeValueMap() {
        
        Double DblCurrentObsValue;
        double dblCurrentObsValue;
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
		        
			    dblCurrentObsValue = DblCurrentObsValue.doubleValue();
			    
			    // 11/28/2005 (v1.0.5) hjs
			    // This fixes a bug in the computation of the discretized values, and
			    // the application of the interval discretization algorithm itself
			    if ( maxObsValue == minObsValue ) {
			        
			        discreteValue = 0;
			    }
			    else {
			        
			        discreteValue = -1;
			        double intervalLength = ( maxObsValue - minObsValue ) /
			        	( intervals+1 );
			        double intervalBound = maxObsValue - intervalLength;
			        for ( int n=intervals; n>-1; n-- ) {
			        
				        if ( dblCurrentObsValue >= intervalBound ) {
			                
			                discreteValue = n;
			                n=-1;
			            }
				        intervalBound -= intervalLength;
			        }
			        if ( discreteValue == -1 ) {
			            discreteValue = 0;
			        }
			    }
				
				 discreteValuesMap.put( DblCurrentObsValue, 
			                new Counter( discreteValue ));
				 
			     originalValuesMap.remove( DblCurrentObsValue );
		    }
	    }
        
        return discreteValuesMap;
    }
}
