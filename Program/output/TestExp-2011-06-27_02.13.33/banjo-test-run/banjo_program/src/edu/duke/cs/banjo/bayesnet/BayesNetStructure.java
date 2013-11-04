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
package edu.duke.cs.banjo.bayesnet;

import edu.duke.cs.banjo.utility.*;

/**
 * Stores a basic BayesNet structure (where the use of a BayesNetManager is redundant).
 *
 * <p><strong>Details:</strong> <br>
 * - Used for storing the N-best network structures.  <br>
 * - Similar to the BayesNetManager, but without the overhead of tracking multiple
 * subsets of nodes (parents). <br>
 * - Uses internal knowledge about the EdgesAsMatrix class for performance reasons.
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Apr 13, 2004
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class BayesNetStructure implements Comparable, BayesNetStructureI {

    protected EdgesWithCachedStatisticsI networkStructure;
    protected double networkScore;
    protected long searchLoopIndex;
    protected BayesNetStructure otherBayesNetStructure;
    protected final int varCount;
    // Allocate once for frequest re-use
    protected int combinedParentCount;
    protected int combinedParentCountToCompareTo;
    protected int[] arrayOfParentCounts;
    protected int[] arrayOfParentCountsToCompareTo;
    
    public BayesNetStructure( 
            BayesNetStructureI _bayesNetStructure, 
            double _networkScore, 
            long _searchLoopIndex ) throws Exception {
                
        // "Clone" the bayesnet structure that is being passed in:      
        this.networkScore = _networkScore;
        this.searchLoopIndex = _searchLoopIndex;
        
        EdgesI tmpMatrix = _bayesNetStructure.getNetworkStructure();
        int tmpVarCount = 0;
        
        if ( tmpMatrix instanceof EdgesAsArrayWithCachedStatistics ) {

            EdgesAsArrayWithCachedStatistics edgeMatrix = ( EdgesAsArrayWithCachedStatistics ) tmpMatrix;
    
            // this does the actual cloning
            this.networkStructure = new EdgesAsArrayWithCachedStatistics( 
                    ( EdgesAsArrayWithCachedStatistics ) tmpMatrix );

            this.networkStructure.assignMatrix( tmpMatrix );
            tmpVarCount = edgeMatrix.getParentCount().length;
        }
        else if ( tmpMatrix instanceof EdgesAsMatrixWithCachedStatistics ) {

            EdgesAsMatrixWithCachedStatistics edgeMatrix = ( EdgesAsMatrixWithCachedStatistics ) tmpMatrix;
    
            // this does the actual cloning
            this.networkStructure = new EdgesAsMatrixWithCachedStatistics( 
                    ( EdgesAsMatrixWithCachedStatistics ) tmpMatrix );

            this.networkStructure.assignMatrix( tmpMatrix );
            tmpVarCount = edgeMatrix.getParentCount().length;
        }
        else {

            throw new BanjoException(
                    BANJO.ERROR_BANJO_DEV, 
                    "(BayesNetStructure constructor) " +
                    "Developer issue: 'bayesNetStructure' object is of unknown data type." );
        }
        
        varCount = tmpVarCount;
    }

    public BayesNetStructure( 
            EdgesWithCachedStatisticsI _networkStructure, 
            double _networkScore, 
            long _searchLoopIndex ) throws Exception {

        int tmpVarCount = 0;
        
        if ( _networkStructure instanceof EdgesAsArrayWithCachedStatistics ) {
            
            this.networkStructure = ( EdgesAsArrayWithCachedStatistics ) _networkStructure;

            this.networkStructure = ( EdgesAsArrayWithCachedStatistics ) _networkStructure.clone();
            
            if ( this.networkStructure == null ) {
                
                // Cloning failed:
                throw new BanjoException(
                        BANJO.ERROR_BANJO_DEV, 
                        "(BayesNetStructure constructor) Cloning failed." );
            }
    
            networkScore = _networkScore;
            searchLoopIndex = _searchLoopIndex;
            
            tmpVarCount = networkStructure.getParentCount().length;
        
            if (BANJO.DEBUG && BANJO.TRACE_BAYESNETSTRUCTURE ) {
                
                System.out.println( "BayesNetStructure (BayesNetStructure constructor)  " +
                        "-- i=" + _searchLoopIndex +
                        ", Initial highscore: " + this.networkScore );
            }
        }
        else if ( _networkStructure instanceof EdgesAsMatrixWithCachedStatistics ) {
            
            networkStructure = ( EdgesAsMatrixWithCachedStatistics ) _networkStructure;

            networkStructure = ( EdgesAsMatrixWithCachedStatistics ) _networkStructure.clone();
            
            if ( networkStructure == null ) {
                
                // Cloning failed:
                throw new BanjoException(
                        BANJO.ERROR_BANJO_DEV, 
                        "(BayesNetStructure constructor) Cloning failed." );
            }
    
            networkScore = _networkScore;
            searchLoopIndex = _searchLoopIndex;

            tmpVarCount = networkStructure.getParentCount().length;
        
            if (BANJO.DEBUG && BANJO.TRACE_BAYESNETSTRUCTURE ) {
                
                System.out.println( "BayesNetStructure (BayesNetStructure constructor)  " +
                        "-- i=" + _searchLoopIndex +
                        ", Initial highscore: " + this.networkScore );
            }
        }
        else {

            throw new BanjoException(
                    BANJO.ERROR_BANJO_DEV, 
                    "(BayesNetStructure.assignBayesNetStructure) " +
                    "Developer issue: 'networkStructure' object is of unknown data type." );
        }
        
        varCount = tmpVarCount;
    }
    
    public void assignBayesNetStructure( 
            EdgesI _networkStructure, 
            double _networkScore, 
            long _searchLoopIndex ) throws Exception {
        
        if ( _networkStructure instanceof EdgesAsArrayWithCachedStatistics ) {

            this.networkStructure = ( EdgesAsArrayWithCachedStatistics ) _networkStructure.clone();
            
            if ( _networkStructure == null ) {
                
                // Cloning failed:
                throw new BanjoException(
                        BANJO.ERROR_BANJO_DEV, 
                        "(BayesNetStructure.assignBayesNetStructure) " +
                        "Cloning failed (EdgesAsArray).");
            }
        }
        else if ( _networkStructure instanceof EdgesAsMatrixWithCachedStatistics ) {

            this.networkStructure = ( EdgesAsMatrixWithCachedStatistics ) _networkStructure.clone();
            
            if ( _networkStructure == null ) {
                
                // Cloning failed:
                throw new BanjoException(
                        BANJO.ERROR_BANJO_DEV, 
                        "(BayesNetStructure.assignBayesNetStructure) " +
                        "Cloning failed (EdgesAsMatrix).");
            }
        }
        else {

            throw new BanjoException(
                    BANJO.ERROR_BANJO_DEV, 
                    "(BayesNetStructure.assignBayesNetStructure) " +
                    "Developer issue: '_networkStructure' object is of unknown data type." );
        }

        networkScore = _networkScore;
        searchLoopIndex = _searchLoopIndex;
    }
    
    public void assignBayesNetStructure( 
            EdgesWithCachedStatisticsI _networkStructure, 
            double _networkScore, 
            long _searchLoopIndex ) throws Exception {
                
        if ( _networkStructure instanceof EdgesAsArrayWithCachedStatistics ) {

            networkStructure = ( EdgesAsArrayWithCachedStatistics ) _networkStructure;
            this.networkStructure = ( EdgesAsArrayWithCachedStatistics ) networkStructure.clone();
            
            if ( networkStructure == null ) {
                
                // Cloning failed:
                throw new BanjoException(
                        BANJO.ERROR_BANJO_DEV, 
                        "(BayesNetStructure.assignBayesNetStructure[2]) " +
                        "Cloning failed (EdgesAsArrayWithCachedStatistics).");
            }
    
            networkScore = _networkScore;
            searchLoopIndex = _searchLoopIndex;
        }
        else if ( _networkStructure instanceof EdgesAsMatrixWithCachedStatistics ) {
            
            networkStructure = ( EdgesAsMatrixWithCachedStatistics ) _networkStructure;
            this.networkStructure = ( EdgesAsMatrixWithCachedStatistics ) networkStructure.clone();
            
            if ( networkStructure == null ) {
                
                // Cloning failed:
                throw new BanjoException(
                        BANJO.ERROR_BANJO_DEV, 
                        "(BayesNetStructure.assignBayesNetStructure[2]) " +
                        "Cloning failed (EdgesAsMatrixWithCachedStatistics).");
            }
    
            networkScore = _networkScore;
            searchLoopIndex = _searchLoopIndex;
        }
        else {

            throw new BanjoException(
                    BANJO.ERROR_BANJO_DEV, 
                    "(BayesNetStructure.assignBayesNetStructure[2]) " +
                    "Developer issue: 'networkStructure' object is of unknown data type." );
        }
    }
    
    /**
     * @return Returns the networkStructure.
     */
    public EdgesI getNetworkStructure() {
        return networkStructure;
    }


    public int compareTo( Object otherStructure ) {
        
        // Note: bullet-proofing really needs to be done outside of this method,
        // e.g., before we attempt to insert an object into our n-best list...
        
        if ( otherStructure == null || otherStructure.getClass() != this.getClass() ) {
        
            System.out.println( "(BayesNetStructure.compareTo) " +
                    "ERROR trying to compare 2 structures." );
        }

        // At this point we now dare to make our blind jump:
        otherBayesNetStructure = ( BayesNetStructure ) otherStructure;
        
        // Comparison as implemented places top score in first slot in treeset used
        // for storing the ordered collection
        if ( this.networkScore > otherBayesNetStructure.networkScore ) {
            
            return -1;
        }
        else if ( this.networkScore < otherBayesNetStructure.networkScore ) {
            
            return 1;
        }
        else {
         
            // This code is added to properly order the bayesNetStructures in the nBest set (we need
            // this to avoid anomalies where the same network would be added multiple times, with some
            // other network listed between the identical instances) 
            combinedParentCount = this.networkStructure.getCombinedParentCount();
            combinedParentCountToCompareTo = 
                otherBayesNetStructure.networkStructure.getCombinedParentCount();
            
            if ( combinedParentCount > combinedParentCountToCompareTo ) {
                
                return 1;
            }
            else if ( combinedParentCount < combinedParentCountToCompareTo ) {
                
                return -1;
            }

            arrayOfParentCounts = this.networkStructure.getParentCount();
            arrayOfParentCountsToCompareTo = otherBayesNetStructure.networkStructure.getParentCount();
            for ( int i=0; i<varCount; i++ ) {
                
                if ( arrayOfParentCounts[i] > arrayOfParentCountsToCompareTo[i] ) {
            
                    return 1;
                }
                else if ( arrayOfParentCounts[i] < arrayOfParentCountsToCompareTo[i] ) {
            
                    return -1;
                }
            }

            if ( this.networkStructure.hasIdenticalEntries( 
                        otherBayesNetStructure.networkStructure )) {
                
                return 0;
            }
            
            // We could apply additional criteria (by going down to the individual parent values)
            // but that seems overkill compared to the performance penalty incurred in return
            
            // Whether we return +1 or -1 here doesn't really matter; it simply 
            // determines the order in which the 2 different networks with
            // the "same" score are stored (this is our final tie-break)
            return 1;
        }
    }

    // Output in string form, using the Proprietary format
    public String toString() {

        int minMarkovLag = -1;
        int maxMarkovLag = -1;
        
        if ( networkStructure instanceof EdgesAsMatrix ) {
            
            EdgesAsMatrix tmpMatrix = ( EdgesAsMatrix ) networkStructure;

            minMarkovLag = tmpMatrix.minMarkovLag;
            maxMarkovLag = tmpMatrix.maxMarkovLag;
        }
        else if ( networkStructure instanceof EdgesAsArray ){
            
            EdgesAsArray tmpMatrix = ( EdgesAsArray ) networkStructure;

            minMarkovLag = tmpMatrix.minMarkovLag;
            maxMarkovLag = tmpMatrix.maxMarkovLag;
        }
        else {
            
            // Dev error, but can't throw exception here
        }

        StringBuffer strStructure = new StringBuffer( varCount * varCount );
        
        if ( minMarkovLag >=0 && maxMarkovLag >=0 ) {
            
            StringBuffer strParentList;
            int parentCount;
            final String strBlanks = new String("              ");
                    
            strStructure.append( varCount );
    
            for (int i=0; i<varCount; i++) {
                
                strParentList = new StringBuffer( varCount );
                
                strStructure.append( "\n" );
                
                if ( varCount > 9 && varCount < 100 ) {
                    
                    // Adjust the variable index to fit columnar format
                    if ( i < 10 ) strStructure.append( " " );
                }
                if ( varCount > 99 && varCount < 1000 ) {
    
                    // Adjust the variable index to fit columnar format
                    if ( i < 100 ) strStructure.append( " " );
                    if ( i < 10 ) strStructure.append( " " );
                }
                if ( varCount > 999 && varCount < 10000 ) {
    
                    // Adjust the variable index to fit columnar format
                    if ( i < 1000 ) strStructure.append( " " );
                    if ( i < 100 ) strStructure.append( " " );
                    if ( i < 10 ) strStructure.append( " " );
                }
                strStructure.append( i );
    
                for (int k=minMarkovLag; k<maxMarkovLag+1; k++) {
    
                    parentCount = 0;
                    strParentList = new StringBuffer( varCount );
                    
                    for (int j=0; j<varCount; j++) {
                                    
                        if ( networkStructure.getEntry(i,j,k) == 1 ) {
        
                            strParentList.append( " " );
                            strParentList.append( j );
                            parentCount++;
                        }
                    }
    
                    if ( maxMarkovLag > 0 ) {
                        // Check if we can omit printing empty "sets"
                        // (Not ideal output for nodes that have no parents at all)
                        //if ( parentCount > 0 ) {
                            
                        strStructure.append( "   " );
                        //strStructure.append( "lag=" );
                        strStructure.append( k );
                        strStructure.append( ":   " );
                        strStructure.append( parentCount );
                        strStructure.append( strParentList );
                        
                        // Column-oriented formating (this will still be "off"
                        // for long parent lists, but for short lists it's useful)
                        // Of course, this issue will eventually go away anyway
                        // when we support other formats
                        if ( strParentList.length() < strBlanks.length() ) {
                            strStructure.append( 
                                    strBlanks.substring( strParentList.length() ));
                        }
                    }
                    else {
                        
                        // Output is simpler when we have no lag
                        strStructure.append( " " );
                        strStructure.append( parentCount );
                        strStructure.append( strParentList );           
                    }
                }
            }
        }
        
        return strStructure.toString();
    }
    
    /**
     * @param networkStructure The networkStructure to set.
     */
    public void setNetworkStructure( EdgesI networkStructure ) 
            throws Exception {
                
        // Need to copy entire object for storing

        if ( networkStructure instanceof EdgesAsArrayWithCachedStatistics ) {

            this.networkStructure = ( EdgesAsArrayWithCachedStatistics ) networkStructure.clone();
            
            if ( networkStructure == null ) {
        
                // Cloning failed:
                throw new BanjoException(
                        BANJO.ERROR_BANJO_DEV, 
                        "(BayesNetStructure.setNetworkStructure)" +
                        " Cloning failed (EdgesAsArray)." );
            }
        }
        else if ( networkStructure instanceof EdgesAsMatrixWithCachedStatistics ) {

            this.networkStructure = ( EdgesAsMatrixWithCachedStatistics ) networkStructure.clone();
            
            if ( networkStructure == null ) {
        
                // Cloning failed:
                throw new BanjoException(
                        BANJO.ERROR_BANJO_DEV, 
                        "(BayesNetStructure.setNetworkStructure)" +
                        " Cloning failed (EdgesAsMatrix)." );
            }
        }
        else {

            throw new BanjoException(
                    BANJO.ERROR_BANJO_DEV, 
                    "(BayesNetStructure.setNetworkStructure) " +
                    "Developer issue: 'networkStructure' object is of unknown data type." );
        }
    }

    /**
     * @return Returns the networkScore.
     */
    public double getNetworkScore() {
        return networkScore;
    }

    /**
     * @param networkScore  The score (of the stored network) to set.
     */
    public void setNetworkScore( double networkScore ) {
        this.networkScore = networkScore;
    }

    /**
     * @return Returns the searchLoopIndex.
     */
    public long getSearchLoopIndex() {
        return searchLoopIndex;
    }
}