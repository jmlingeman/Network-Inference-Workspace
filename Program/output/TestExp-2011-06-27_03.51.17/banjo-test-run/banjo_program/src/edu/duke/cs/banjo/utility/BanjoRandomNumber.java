/*
 * Created on Mar 24, 2008
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

import java.util.Random;

/**
 * Basic utility class for providing random sequences, based on either a fixed seed
 * or a system time-based seed. 
 * 
 * <p><strong>Details:</strong> <br>
 * By using a regular (instead of a static) class we can use separate random sequences 
 * for multiple threads, and still get repeatable results for testing.
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Mar 24, 2008
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class BanjoRandomNumber {

    // random seed used for generating the random sequence
    // note: we default to a random start point, which will be used unless it is
    // explicitly overridden by a call to setRandomSeed
    protected long banjoSeed = System.currentTimeMillis();
    
    // sequence of random numbers
    protected Random randomSequence;
    
    // We use this constructor in our client code, before we know what seed the
    // user may have specified
    public BanjoRandomNumber() {
        
        // Use the default seed
        setBanjoSeed( banjoSeed );
    }
    
    // This is supplied for convenience
    public BanjoRandomNumber( long _seed ) {

        // Use the supplied seed
        setBanjoSeed( _seed );
    }
    
    // This shields the "customer" class from making any decision about what random sequence to use
    // so we can use a basic switch between regular and debug/test mode
    public Random getRandomSequence() {

        return randomSequence;
    }
    
    public long getBanjoSeed() {
        
        return banjoSeed;
    }
    
    public void setBanjoSeed( long _randomSeed ) {
        
        banjoSeed = _randomSeed;
        
        // Reset the random sequence based on the supplied seed
        randomSequence = new Random( banjoSeed );
    }
}
