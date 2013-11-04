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

import edu.duke.cs.banjo.utility.BANJO;
import java.util.*;
/**
 * Contains the code common to Discretizer implementations.
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Nov 30, 2005
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public abstract class Discretizer implements DiscretizerI {
	
	protected SortedMap discreteValuesMap;
	protected int intervals;
	protected int mapSize;
	protected int discretizationPoints;
	protected double minObsValue;
	protected double maxObsValue;
	protected SortedMap tmpOriginalValuesMap;
	protected SortedMap originalValuesMap;
	
    public Discretizer( final SortedMap _originalValuesMap, 
            final int _discretizationPoints ) throws Exception {

        discreteValuesMap = new TreeMap();
        originalValuesMap = new TreeMap();
        tmpOriginalValuesMap = new TreeMap();

        originalValuesMap.putAll( _originalValuesMap );
        discreteValuesMap.putAll( _originalValuesMap );
        tmpOriginalValuesMap.putAll( _originalValuesMap );
        
        mapSize = originalValuesMap.size();
        intervals = _discretizationPoints - 1;
        discretizationPoints = _discretizationPoints;

        Double DblCurrentObsValue;
        double dblCurrentObsValue;
        // Find the min and max values of the data set
        
        minObsValue = BANJO.BANJO_LARGEINITIALVALUEFORMINIMUM;
        maxObsValue = BANJO.BANJO_SMALLINITIALVALUEFORMAXIMUM;
        
        for ( int m=0; m<mapSize; m++ ) {

            DblCurrentObsValue = 
	            (Double) tmpOriginalValuesMap.firstKey();
            dblCurrentObsValue = DblCurrentObsValue.doubleValue();
            
		    if ( dblCurrentObsValue < minObsValue ) 
		        minObsValue = dblCurrentObsValue;
		    if ( dblCurrentObsValue > maxObsValue ) 
		        maxObsValue = dblCurrentObsValue;
		    
		    tmpOriginalValuesMap.remove( DblCurrentObsValue );
        }
    }
    
    public abstract SortedMap computeValueMap();        
}
