/*
 * Created on Jul 13, 2005
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
package edu.duke.cs.banjo.application;

import edu.duke.cs.banjo.data.observations.ObservationsI;
import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.learner.*;
import edu.duke.cs.banjo.utility.*;
/**
 * 
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * "Hello World" example on how to access the Banjo classes.
 * 
 * <p><strong>Change History:</strong> <br>
 * Created on Jul 13, 2005
 * 
 * hjs (v2.1)   Modifications to accomodate the various code changes to make 
 *              the application multi-threaded (this class doesn't use multiple 
 *              threads, though) 
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class RunBasicSearchExample {

	public static void main( String[] args ) {

		SearcherI searcher;
        PostProcessor postProcessor;
		Settings settings;
        ObservationsI observations;
		BanjoErrorHandler errorHandler = new BanjoErrorHandler();
        
        try {

            // Load and validate the parameters for running the application
            settings = new Settings();
            settings.processCommandLine( args );
            
            // Setup the error handler so it knows about the (loaded) settings
            errorHandler = new BanjoErrorHandler( settings );

            // Load the observations
            settings.loadObservations();
            
            // Set up the search(er) and run the search
            searcher = new SearcherGreedy( settings );            
		    searcher.executeSearch();
	    }
		catch ( final BanjoException e ) {
		    
		    errorHandler.handleApplicationException( e );
		}
		catch ( final Exception e ) {
		    
		    errorHandler.handleGeneralException( e );
		}
	}
}
