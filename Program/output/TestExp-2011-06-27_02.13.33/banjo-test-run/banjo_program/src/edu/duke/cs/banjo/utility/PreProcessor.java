/*
 * Created on Mar 29, 2005
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
package edu.duke.cs.banjo.utility;

import edu.duke.cs.banjo.data.settings.Settings;


/**
 * Contains the pre-processing actions (not yet implemented).
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Mar 29, 2005
 * 
 * @author hjs <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class PreProcessor {

	protected Settings settings;
	
	// Data that will be part of the underlying problem domain
	protected final int varCount;
	protected final int minMarkovLag;
	protected final int maxMarkovLag;
	
	public PreProcessor( final Settings _processData ) {

	    this.settings = _processData;

	    varCount = Integer.parseInt( 
		        settings.getValidatedProcessParameter( 
		                BANJO.SETTING_VARCOUNT ) );
		minMarkovLag = Integer.parseInt( 
		        settings.getValidatedProcessParameter( 
		                BANJO.SETTING_MINMARKOVLAG ) );
		maxMarkovLag = Integer.parseInt( 
		        settings.getValidatedProcessParameter( 
		                BANJO.SETTING_MAXMARKOVLAG ) );
	}
    
    public void execute() throws Exception {
        
        // Place for any pre-processing code
    }
}