/*
 * Created on Feb 27, 2006
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
package edu.duke.cs.banjo.learner;

import java.util.TreeSet;

import edu.duke.cs.banjo.bayesnet.BayesNetManager;
import edu.duke.cs.banjo.bayesnet.BayesNetStructure;
import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.learner.components.EvaluatorBDe;
import edu.duke.cs.banjo.utility.BANJO;

/**
 * This searcher is only setting up as much of the bayesnet structures as is
 * necessary to prepare for the post-processing, without actually having to go
 * through a search. Consequently, we suppress the display of the various
 * search parameters in the output.
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Feb 27, 2006
 * 
 * 8/6/2008 hjs Add evaluator to have network score computed.
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class SearcherSkip extends Searcher {

	public SearcherSkip( Settings _processData ) throws Exception {
		
		super( _processData );
		
		// Validate the required settings
		boolean isDataValid = validateRequiredData();
		
		// If crucial settings could ne be validated, we can't execute the remaining
		// code in this constructor
		if ( !isDataValid ) return;

		// Set up the required (subordinate) objects
		setupSearch();
	}
	
	// Compiles various pieces of info about the search, to be used by the recorder
	protected void setupSearch() throws Exception {
				
		// Set up the bayesnet infrastructure (in particular, the user likely has an
		// initial structure to be examined by the post-processor)
		this.bayesNetManager = new BayesNetManager( processData );

		// Set up the standard container for our network structure:
		highScoreStructureSinceRestart = new BayesNetStructure( 
		        bayesNetManager.getCurrentParents(), 
				currentBestScoreSinceRestart, 0 );
				
		highScoreStructureSet = new TreeSet();
		
	    highScoreStructureSet.add( new BayesNetStructure( 
	            highScoreStructureSinceRestart,
	            currentBestScoreSinceRestart, 
                networksVisitedGlobalCounter ) );

        // (v2.2) hjs
        // Add an evaluator, so we actually compute the network score of the initial network
        if ( processData.getValidatedProcessParameter( 
                BANJO.SETTING_EVALUATORCHOICE )
                .equalsIgnoreCase( BANJO.UI_EVAL_BDE )) { 

            this.evaluator = new EvaluatorBDe(bayesNetManager, processData);
        }
        else {
            
            this.evaluator = new EvaluatorBDe(bayesNetManager, processData);
        }
        
        // Compute the initial score for the network
        currentBestScoreSinceRestart = evaluator.computeInitialNetworkScore(
                bayesNetManager );

        // Set the initial network as the current best score, and add to the high score networks
//        highScoreStructureSinceRestart = new BayesNetStructure( 
//                bayesNetManager.getCurrentParents(), 
//                currentBestScoreSinceRestart, 0 );
                
        highScoreStructureSet = new TreeSet();
        
        highScoreStructureSet.add( new BayesNetStructure( 
                bayesNetManager.getCurrentParents(), 
                currentBestScoreSinceRestart, 0 ) );
//        highScoreStructureSinceRestart,
//                currentBestScoreSinceRestart, 
//                networksVisitedGlobalCounter ) );
        
        
	    // Finally, store the network and score given by the initial structure
	    // for the post-processing
	    processData.setHighScoreStructureSet( highScoreStructureSet );
	    
		// Set the string buffer
		searcherStats = new StringBuffer( BANJO.BUFFERLENGTH_STAT_INTERNAL );
	    
		// Is there something we want to pass along at this point?
		
		// Well, here we pass "it" along (nothing for now)
		processData.setDynamicProcessParameter( BANJO.DATA_SEARCHERINFO_SPECIFICSEARCHER, 
		        searcherStats.toString() );
		processData.setDynamicProcessParameter( BANJO.DATA_SEARCHERINFO_COREOBJECTS, 
		        searcherStats.toString() );
	}

    /* (non-Javadoc)
     * @see edu.duke.cs.banjo.learner.SearcherI#executeSearch()
     */
    public void executeSearch() throws Exception {

        // There's not much to do in this searcher:	
        
        // Record the initial data
		searcherStatistics.recordInitialData(this);
    }

    /* (non-Javadoc)
     * @see edu.duke.cs.banjo.learner.SearcherI#updateProcessData(
     * 		edu.duke.cs.banjo.data.settings.Settings )
     */
    public void updateProcessData( Settings _processData ) throws Exception {
        // nothing to do
    }
    
	private boolean validateRequiredData() throws Exception {

	    // There is nothing to validate:
	    
	    boolean isDataValid = true;

	    return isDataValid;
	}
}
