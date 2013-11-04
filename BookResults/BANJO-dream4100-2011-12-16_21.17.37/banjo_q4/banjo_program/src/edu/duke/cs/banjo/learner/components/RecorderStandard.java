/*
 * Created on May 4, 2004
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

import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.learner.*;
import edu.duke.cs.banjo.utility.*;

/**
 * Provides a set of "standard" statistics describing the progress and results of 
 * the search algorithms.
 *
 * <p><strong>Details:</strong> <br>
 * 
 * <p><strong>Change History:</strong> <br>
 * Created May 4, 2004
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class RecorderStandard extends Recorder {
	
	public RecorderStandard( Settings _processData ) throws Exception {
		
		super( _processData );
	}
	
	// Keep the initial data recording separate from the constructor
	// for flexibility
	public void recordInitialData( SearcherI searcher ) throws Exception {
		
		super.recordInitialData( searcher );
	}
	
	public synchronized void recordRecurringData( SearcherI searcher ) throws Exception {
			
	    // Note: we cast "searcher" twice to ((Searcher)searcher ), to be able to
	    // complete our statistics gathering. The called methods will go away in the
	    // future and be replaced with the generic sharing mechanism via the Settings
	    // class.
	    
		statisticsBuffer.append( StringUtil.getClassName( this ) + 
		        "  --  Global loop i = " );
		statisticsBuffer.append( ((Searcher)searcher ).getNetworksVisitedGlobalCounter() );
		statisticsBuffer.append( ": Network score = " );
		// 
		statisticsBuffer.append( ((Searcher)searcher ).getHighScore());
		statisticsBuffer.append( BANJO.FEEDBACK_NEWLINE );
		
		// check on the searcher type: Don't commit the collected data for
		// those searcher subclasses that don't collect a huge amount of data
		//commitData();
	    
//        outputFileFlags.clear();
//        outputFileFlags.add( new Integer(BANJO.FILE_RESULTS) );
//        outputFileFlags.add( new Integer(BANJO.FILE_SUMMARY) );
//        outputFileFlags.add( new Integer(BANJO.FILE_TRACE) );
//
//		commitData( outputFileFlags , statisticsBuffer );
//		commitData( outputResultsOnly , statisticsBuffer );
//		statisticsBuffer = new StringBuffer( BANJO.BUFFERLENGTH_STAT );
	}
	
	public synchronized void recordFinalData( SearcherI searcher ) throws Exception {
		
		super.recordFinalData( searcher );
		
		// Record data that is specific to this stat implementation:
		
//        outputFileFlags.clear();
//        outputFileFlags.add( new Integer(BANJO.FILE_RESULTS) );
//        outputFileFlags.add( new Integer(BANJO.FILE_SUMMARY) );
//        outputFileFlags.add( new Integer(BANJO.FILE_TRACE) );
//        commitData( outputFileFlags , statisticsBuffer );
		
		commitData( outputResultsOnly , statisticsBuffer );
		
		// Now clear the text buffer that was written to file
		statisticsBuffer = new StringBuffer(BANJO.BUFFERLENGTH_STAT);
	}
	
	public synchronized void recordSpecifiedData( StringBuffer dataToRecord ) throws Exception{
		
		statisticsBuffer.append( dataToRecord );
		
//        outputFileFlags.clear();
//        outputFileFlags.add( new Integer(BANJO.FILE_RESULTS) );
//        outputFileFlags.add( new Integer(BANJO.FILE_SUMMARY) );
//        outputFileFlags.add( new Integer(BANJO.FILE_TRACE) );
//		commitData( outputFileFlags , statisticsBuffer );
		
		commitData( outputResultsOnly , statisticsBuffer );
		
		// Now clear the text buffer that was written to file
		statisticsBuffer = new StringBuffer(BANJO.BUFFERLENGTH_STAT);
	}
}
