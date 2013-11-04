/*
 * Created on Apr 13, 2004
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.duke.cs.banjo.bayesnet.*;
import edu.duke.cs.banjo.data.settings.*;
import edu.duke.cs.banjo.utility.*;


/**
 * Combines common code shared by the different evaluator implementations. 
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Apr 13, 2004
 * <p>
 * 9/6/2005 (v1.0.3) hjs  	Defect in constructor: Make use of caching 
 * 							arrays conditional, so they are only created
 * 							when needed
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public abstract class Evaluator implements EvaluatorI {
    	
	// Cache frequently used values 
	protected final int varCount;
	protected final int minMarkovLag;
	protected final int maxMarkovLag;
	
	// Container for caching individual node scores
	protected NodeScoreCacheItemI currentNodeScoreHashItem;
	protected NodeScoreCacheItemI retrievedNodeScoreHashItem;
    protected HashMap nodeScoreHashMap = new HashMap( BANJO.INITIALHASHSIZE );
     
	// Container for feedback strings
	protected StringBuffer feedbackBuffer;   
    
	// Score of the LAST network structures visited
	protected double cachedNetworkScore = 0;
	
	// Array for caching the scores for the individual nodes, during a single iteration.
	protected double cachedNodeScores[];
	// When a change gets rejected in the searcher, we need to be able to
	// properly roll back
	protected double cachedCurrentNodePreviousScore;
	protected double cachedParentNodePreviousScore;
	protected double cachedNetworkPreviousScore;
	
	// Basic statistics: how many changes of each type do we consider
	protected long computedScoreTracker = 0;
	// Track the scores that are being fetched from one of the cache mechanisms
	// (indexed by the number of parents that the node had)
	protected long[] fetchedCachedScoreTracker;
    protected long[] placedInCacheScoreTracker;
    protected long[] collisionsInCacheTracker;
	protected long crossCheckCachedScoreTracker = 0;
	protected int highestParentCountEncountered = 0;
	
	// Access to the global settings (static and dynamic)
	protected Settings processData;
	
	protected boolean useCache;
	protected boolean useBasicCache;
	protected int fastCacheLevel;
	
	/**
	 * Used in the log-gamma computation. Declared here for
	 * optimized performance.
	 */
	static double b[] = new double[19];

    protected abstract class ObservationsSelector {
        
        /**
         * @return Returns the computed score for the node with id=nodeID.
         */
        protected abstract double computeNodeScore( 
                final int nodeID, final int[][] parentIDlist ) throws Exception;
        
        protected abstract int getObservationCount() throws Exception;
        
        protected abstract int getMaxValueCount() throws Exception;
        
        protected abstract int getMaxValueCount( int nodeID ) throws Exception;
    }
    
    /**
     * Constructor
     */
	public Evaluator( final BayesNetManagerI _initialBayesNet, final Settings _processData )
		throws Exception {


        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            BANJO.FEEDBACK_NEWLINE + "    Evaluator: Before hashMap allocation" )) );
        }
        
        synchronized( getClass() ) {
            
            if ( nodeScoreHashMap == null ) {
            
                nodeScoreHashMap = new HashMap( BANJO.INITIALHASHSIZE );
            }
        }
                
        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            "    Evaluator: After hashMap allocation" )) );
        }
                
	    this.processData = _processData;

		varCount = Integer.parseInt( _processData.getValidatedProcessParameter(
		        BANJO.SETTING_VARCOUNT ));
		minMarkovLag = Integer.parseInt( _processData.getValidatedProcessParameter(
		        BANJO.SETTING_MINMARKOVLAG ));
		maxMarkovLag = Integer.parseInt( _processData.getValidatedProcessParameter(
		        BANJO.SETTING_MAXMARKOVLAG ));
        
        validateRequiredData();
		
		// Create the array for holding the score for each node.
		cachedNodeScores = new double[varCount];
		
		// Create an object used for caching the individual node scores
		// and the network scores
		currentNodeScoreHashItem = new NodeScoreCacheItem();
				
		// Set up the tracking for the caching mechanism
		int maxTrackingSize = Integer.parseInt( 
	            _processData.getValidatedProcessParameter( 
	                    BANJO.SETTING_MAXPARENTCOUNT ) ) + 1;
		//-- 8/29/2005 (v1.0.1) hjs
		if ( maxTrackingSize < fastCacheLevel + 1 ) {
		    
		    maxTrackingSize = fastCacheLevel + 1;
		}
		//--
	    fetchedCachedScoreTracker = new long[ maxTrackingSize ];
	    placedInCacheScoreTracker = new long[ maxTrackingSize ];
        collisionsInCacheTracker = new long[ maxTrackingSize ];
	    for ( int i = 0; i < maxTrackingSize; i++ ) {
	        
	        fetchedCachedScoreTracker[i] = 0;
	        placedInCacheScoreTracker[i] = 0;
            collisionsInCacheTracker[i] = 0;
	    }
	    
	    // simple optimization: assign values once:
		b[1]  = -0.0761141616704358;  b[2]  = 0.0084323249659328;
		b[3]  = -0.0010794937263286;  b[4]  = 0.0001490074800369;
		b[5]  = -0.0000215123998886;  b[6]  = 0.0000031979329861;
		b[7]  = -0.0000004851693012;  b[8]  = 0.0000000747148782;
		b[9]  = -0.0000000116382967;  b[10] = 0.0000000018294004;
		b[11] = -0.0000000002896918;  b[12] = 0.0000000000461570;
		b[13] = -0.0000000000073928;  b[14] = 0.0000000000011894;
		b[15] = -0.0000000000001921;  b[16] = 0.0000000000000311;
		b[17] = -0.0000000000000051;  b[18] = 0.0000000000000008;
	}

	/**
	 * Validates the settings values required for loading the evaluator base class 
	 * (i.e., the cache settings).
	 * 
	 * @return Returns the boolean flag that indicates whether a crucial setting
	 * could not be validated.
	 */
	private boolean validateRequiredData() throws Exception {

	    boolean isDataValid = true;
	    
	    // utility variables for validating
	    String settingNameCanonical;
	    String settingNameDescriptive;
	    String settingNameForDisplay;
        String settingDataType;
		int validationType;
		SettingItem settingItem;
		Set validValues = new HashSet();
	    
		// 2/2006 Combine the (new) cache settings into a single one that is
		// exposed via the settings file
	  	// Validate the 'use cache'
	    settingNameCanonical = BANJO.SETTING_USECACHE;
	    settingNameDescriptive = BANJO.SETTING_USECACHE_DESCR;
	    settingNameForDisplay = BANJO.SETTING_USECACHE_DISP;
	    settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
	    validationType = BANJO.VALIDATIONTYPE_OPTIONAL;	
	    validValues.clear();
	    validValues.add( BANJO.UI_USECACHE_NONE );
	    validValues.add( BANJO.UI_USECACHE_BASIC );
	    validValues.add( BANJO.UI_USECACHE_FASTLEVEL_0 );
	    validValues.add( BANJO.UI_USECACHE_FASTLEVEL_1 );
	    validValues.add( BANJO.UI_USECACHE_FASTLEVEL_2 );
	    settingItem = processData.processSetting( settingNameCanonical,
	            settingNameDescriptive,
	            settingNameForDisplay,
	            settingDataType,
	            validationType,
	            validValues,
	            BANJO.DEFAULT_USECACHE );
	    
	    if ( settingItem.isValidSetting() ) {
		    
		    String validatedUseCache = processData.getValidatedProcessParameter( 
		            BANJO.SETTING_USECACHE );
		    
		    if ( validatedUseCache.equalsIgnoreCase( BANJO.UI_USECACHE_NONE )) {

		        // Turn off the basic cache
		        processData.setDynamicProcessParameter( 
		                BANJO.DATA_USEBASICCACHE , BANJO.UI_USEBASICCACHE_NO );
		        
		        // Turn off the fast cache
		        processData.setDynamicProcessParameter( 
		                BANJO.DATA_USEFASTCACHELEVEL , 
		                ( new Integer( BANJO.DATA_NOFASTCACHE )).toString() );
		    }
		    else {
		        
		        // Set the basic cache to the selected level
		        processData.setDynamicProcessParameter( 
		                BANJO.DATA_USEBASICCACHE , BANJO.UI_USEBASICCACHE_YES );
		        
		        // Set the fast cache to the selected level
		        if ( validatedUseCache.equalsIgnoreCase( BANJO.UI_USECACHE_FASTLEVEL_0 )) {
		            
		            processData.setDynamicProcessParameter( 
			                BANJO.DATA_USEFASTCACHELEVEL , 
			                ( new Integer( BANJO.DATA_FASTCACHELEVEL0 )).toString() );
		        }
		        else if ( validatedUseCache.equalsIgnoreCase( BANJO.UI_USECACHE_FASTLEVEL_1 )) {
		            
		            processData.setDynamicProcessParameter( 
			                BANJO.DATA_USEFASTCACHELEVEL , 
			                ( new Integer( BANJO.DATA_FASTCACHELEVEL1 )).toString() );
		        }
		        else if ( validatedUseCache.equalsIgnoreCase( BANJO.UI_USECACHE_FASTLEVEL_2 )) {
		            
		            processData.setDynamicProcessParameter( 
			                BANJO.DATA_USEFASTCACHELEVEL , 
			                ( new Integer( BANJO.DATA_FASTCACHELEVEL2 )).toString() );
		        }
		        else {
		            
		            // Default fast cache level: see BANJO constants for currently set value
		            processData.setDynamicProcessParameter( 
			                BANJO.DATA_USEFASTCACHELEVEL , 
			                ( new Integer( BANJO.DATA_DEFAULT_FASTCACHELEVEL )).toString() );
		        }
		    }
		    

			// New approach gives user control over the memory settings
			String tmpValue = processData.getValidatedProcessParameter(
			            BANJO.SETTING_USECACHE );
			if ( tmpValue.equalsIgnoreCase( BANJO.UI_USECACHE_NONE )) {
			
			    useCache = false;
			}
			else {
			    
			    useCache = true;
			}

			tmpValue = processData.getDynamicProcessParameter(
			            BANJO.DATA_USEBASICCACHE );
			if ( tmpValue.equalsIgnoreCase( BANJO.UI_USEBASICCACHE_YES )) {
			
			    useBasicCache = true;
			}
			else {
			    
			    useBasicCache = false;
			}

			int tmpIntValue = ( new Integer( processData.getDynamicProcessParameter(
			            BANJO.DATA_USEFASTCACHELEVEL ) )).intValue();
			if ( tmpIntValue > -1  ) {
			
			    fastCacheLevel = tmpIntValue;
			}
			else {
			    
			    fastCacheLevel = -1;
			}
	    }
	    else {
	        
	        processData.addToErrors( settingItem.getCollectedErrors() );
	        
		    useCache = false;
		    useBasicCache = false;
		    fastCacheLevel = -1;
	    }
	    
	    return isDataValid;
	}
	
	
	// Wrapper around logGamma from a numerical library,
	// or for using a lookup table
	protected double logGamma(final double arg) {
		
		return loggamma(arg);
	}
	
	/* 
	 * The loggamma function implementation as used below is from
	 * Hang T. Lau's "A Numerical Library in Java for Scientists and Engineers"
	 * Chapman & Hall/CRC, 2004, which is a Java translation of the NUMAL 
	 * procedures originally developed at the Mathematical Centre, Amsterdam.
	 */
	protected synchronized static double loggamma(double x)
	// Basic optimization applied (considering that we call this function over and over)
	{
		int i;
		double r,x2,y,f,u0,u1,u,z;

		if (x > 13.0) {
			r=1.0;
			while (x <= 22.0) {
				r /= x;
				x += 1.0;
			}
			x2 = -1.0/(x*x);
			r=Math.log(r);
			return Math.log(x)*(x-0.5)-x+r+0.918938533204672+
	           (((0.595238095238095e-3*x2+0.793650793650794e-3)*x2+
	           0.277777777777778e-2)*x2+0.833333333333333e-1)/x;
		}

		f=1.0;
		u0=u1=0.0;
		if (x < 1.0) {
			f=1.0/x;
			x += 1.0;
		} else
			while (x > 2.0) {
				x -= 1.0;
				f *= x;
			}
		f=Math.log(f);
		y=x+x-3.0;
		z=y+y;
		for (i=18; i>=1; i--) {
			u=u0;
			u0=z*u0+b[i]-u1;
			u1=u;
		}
		return (u0*y+0.491415393029387-u1)*(x-1.0)*(x-2.0)+f;
	}

	/* (non-Javadoc)
	 * @see edu.duke.cs.banjo.learner.EvaluatorI#computeInitialNetworkScore(
	 * 		edu.duke.cs.banjo.bayesnet.BayesNet)
	 */
	abstract public double computeInitialNetworkScore(
	        BayesNetManagerI currentBayesNetManager) throws Exception;

	/* (non-Javadoc)
	 * @see edu.duke.cs.banjo.learner.EvaluatorI#updateNetworkScore(
	 * 		edu.duke.cs.banjo.bayesnet.BayesNet, edu.duke.cs.banjo.bayesnet.BayesNetChange)
	 */
	abstract public double updateNetworkScore(
		BayesNetManagerI currentBayesNetManager,
		BayesNetChangeI currentBayesNetChange) throws Exception;

	/* (non-Javadoc)
	 * @see edu.duke.cs.banjo.learner.EvaluatorI#adjustNodeScoresForUndo(
	 * 		edu.duke.cs.banjo.bayesnet.BayesNetChange)
	 */
    abstract public void adjustNodeScoresForUndo( 
            BayesNetChangeI suggestedBayesNetChange ) throws Exception;
	
    // Placeholder for future use
	public void updateProcessData( Settings _processData ) throws Exception {
        // Placeholder for future use};
    }

	// Override this method to provide whatever statistics needs
	// to be collected in a particular evaluator
	public StringBuffer provideCollectedStatistics() throws Exception { 
		
        int columnWidth = 15;
		StringBuffer stats = 
			new StringBuffer( BANJO.BUFFERLENGTH_STAT );

		stats.append( BANJO.FEEDBACK_NEWLINE );
		stats.append( "Statistics collected in evaluator '");
		stats.append( StringUtil.getClassName(this) );
		stats.append( "':" );
		
		// Note: the sum of the computed and the cached scores will be equal
		// to the sum of additions + deletions + 2*reversal - 
		// (number of [re]starts)*(number of variables) 
		
		stats.append( BANJO.FEEDBACK_NEWLINE );
		stats.append( "  Scores computed:          " );
		stats.append( computedScoreTracker );
		
		int maxParentCount = Integer.parseInt( 
	            processData.getValidatedProcessParameter( 
	                    BANJO.SETTING_MAXPARENTCOUNT ) );
			
		if ( !useCache ) {
		    
		    stats.append( "     (Cache disabled)" );
		}
		else {
		    
		    stats.append( BANJO.FEEDBACK_NEWLINE );
			stats.append( "  Scores (cache)            " );
			stats.append( StringUtil.fillSpaces( "placed" , columnWidth ) );
            stats.append( StringUtil.fillSpaces( "fetched" , columnWidth ) );
            
            if ( BANJO.CONFIG_DISPLAYCOLLISIONS ) {
            
                stats.append( StringUtil.fillSpaces( "collisions" , columnWidth ) );
            }
            
		    for ( int i=0; i<= maxParentCount; i++ ) {

		        stats.append( BANJO.FEEDBACK_NEWLINE );
				stats.append( "      with " );
			    stats.append( i );
			    stats.append( " parents:       " );
			    stats.append( StringUtil.fillSpaces( 
				           Long.toString( placedInCacheScoreTracker[i] ), columnWidth ) );
                stats.append( StringUtil.fillSpaces( 
                           Long.toString( fetchedCachedScoreTracker[i] ), columnWidth ) );
                
                if ( BANJO.CONFIG_DISPLAYCOLLISIONS ) {
                
                    stats.append( StringUtil.fillSpaces( 
                               Long.toString( collisionsInCacheTracker[i] ), columnWidth ) );
                }
			}
		}

		return stats;
	};
    
    public void cleanupOnException() throws Exception {
        
        // Release the caches
        nodeScoreHashMap = null;
    }
}
