/*
 * Created on Jun 12, 2006
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
 * Contains the basic adjacency matrix implementation, using a 1-dimensional array.
 *
 * <p><strong>Details:</strong> <br>
 * 
 *  - <strong>Important:</strong>
 * For performance reasons, several classes (e.g., BayesNetStructure, Hash classes)
 * use internal knowledge about this class; i.e., we are intentionally breaking the 
 * encapsulation of the internal data. We need to do this because it is extremely 
 * inefficient to iterate through loops with "getEntry" or "setEntry" calls. 
 * There are numerous instances of such loops throughout the core objects of 
 * the applications.
 * 
 * <p><strong>Change History:</strong> <br>
 * Created on Jun 12, 2006 
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 * 
 */
public class EdgesAsArray implements Cloneable, EdgesI {

    // Stores the parent configuration for the variables in a matrix such that
    // a single index references the entries in the matrix, with a simple "translation'
    // of the variable's index ("dim 1"), the parent variable's index ("dim 2"), and
    // the lag (determined by the maxMarkovLag of the Markov chain) of the parent 
    // ("dim 3").
    // Note that strict encapsulation of this class is broken due to performance
    // issues with the number of getEntry/setEntry calls that Banjo needs to make.
    public int[] matrix;
        
    protected final int varCount;
    protected final int maxMarkovLag;
    protected final int minMarkovLag;
    
    protected final int dimVariables;
    protected final int dimParents;
    protected final int dimLags;
    protected final int offsetVariables;
    protected final int offsetParents;
    protected final int offsetLags;
    
    protected int currentVarOffset;
    protected int currentLagOffset;
    
    public EdgesAsArray( final int _varCount, 
            final int _minMarkovLag, 
            final int _maxMarkovLag ) {
        
        // Store the variable count and the maxMarkovLag for easy access
        this.varCount = _varCount;
        this.maxMarkovLag = _maxMarkovLag;
        this.minMarkovLag = _minMarkovLag;
        
        // Create the matrix. Note that we may waste storage by sizing the
        // array dimension for the Markov lag to maxMarkovLag, and not to the 
        // difference between maxMarkovLag and minMarkovLag. However, we gain
        // a vast reduction in complexity (all loops would have to be adjusted by
        // the difference between the min and max order).

        dimVariables = _varCount;
        dimParents = _varCount;
        dimLags = _maxMarkovLag - _minMarkovLag + 1;
        
        // Parents are listed consecutively within each lag:
        offsetParents = 1;
        // The index "offset" between consecutive lags:
        offsetLags = dimParents;
        // The index "offset" between consecutive variables:
        offsetVariables = dimParents * dimLags;
            
        // Size of single-dimensional array is given by the cross product of
        // the individual dimensions
        matrix = new int[ dimVariables * dimParents * dimLags ];
    }
    
    /**
     * @return Returns the matrix (reference).
     */
    public int[] getMatrix() {
        return matrix;
    }
    /**
     * @param _matrix The matrix to set (reference assignment only).
     */
    public void setMatrix( final int[] _matrix ) {
        this.matrix = _matrix;
    }
    
    // Assign the values to the values of another matrix 
    // Expensive, so avoid if possible. Also: dangerous because
    // the dimensions need to match. Better to use the polymorphic
    // method that follows this one
    /**
     * @param _matrix The matrix to set (assignment of matrix values;
     * no reference assignment).
     */
    public void assignMatrix( final int[] _matrix ) {

        for (int i=0; i < _matrix.length; i++) {

            this.matrix[ i ] = _matrix[ i ];
        }
    }

    
    public void assignMatrix( final EdgesI _edgeMatrix ) throws Exception {

        if ( _edgeMatrix instanceof EdgesAsArray ) {
            
            // Create a reference to the edgeMatrix, so we don't
            // have to loop through getEntry calls
            int[] tmpEdgeMatrix = (( EdgesAsArray ) _edgeMatrix).getMatrix();
            assignMatrix( tmpEdgeMatrix );
        }
        else {
            
            // Flag as internal error - matrix types don't match
            // (this should never happen..)
            throw new BanjoException(
                    BANJO.ERROR_BANJO_DEV, 
                    "(EdgesAsArray.assignMatrix) " +
                    "Development issue: Unable to use supplied 'edgeMatrix' as EdgesAsArray!" );
        }
    }
    
    public void assignMatrix( final EdgesAsArray _edgesAsArray ) throws Exception {
            
        // Create a reference to the edgeMatrix, so we don't
        // have to loop through getEntry calls (performance shortcut)
        int[] tmpEdgeMatrix = _edgesAsArray.getMatrix();
        assignMatrix( tmpEdgeMatrix );
    }
    
    // Access any individual entry in the parent matrix
    public int getEntry( 
            final int _varIndex, 
            final int _parentVarIndex, 
            final int _parentLag ) {
        
        return matrix[ _varIndex*offsetVariables 
                 + ( _parentLag-minMarkovLag )*offsetLags + _parentVarIndex ];
    }
    
    // Unrestricted assignment of values in matrix (see EdgesAsMatrixWithCachedStatistics subclass)
    public void setEntry( 
            final int _varIndex, 
            final int _parentVarIndex, 
            final int _parentLag,
            final int _newValue) throws Exception {
        
        matrix[ _varIndex*offsetVariables
                + ( _parentLag-minMarkovLag )*offsetLags + _parentVarIndex ] = _newValue;
    }


    public boolean hasIdenticalEntries( final int[] _matrix ) {

        for (int i=0; i < _matrix.length; i++) {

            if(this.matrix[ i ] != _matrix[ i ])
                return false;
        }

        return true;
    } 
    
    public boolean hasIdenticalEntries( final EdgesI _otherMatrix ) {

        if ( _otherMatrix instanceof EdgesAsArray ) {
            
            // Define a matrix as performance shortcut:
            int[] tmpMatrix = (( EdgesAsArray ) _otherMatrix ).getMatrix();

            return hasIdenticalEntries( tmpMatrix );
        }
        else {
            
            // Can't throw exception here, so do the next best thing:
            
        }
        return true;
    }

    public boolean hasIdenticalEntries( final EdgesAsArray _otherMatrix ){

        return hasIdenticalEntries( _otherMatrix.matrix );
    }
    
    ////////////////////////
    // The next 2 methods are added temporarily: only used in influence score
    // computation (as of 5/10/05)
    ////////////////////////
    // Computes the list of parent ID's for the current node "nodeID"
    // "Overload" for DBN's: Note that the second argument is actually redundant,
    // but comes in handy as a visual reminder that we are in the DBN case
    // (the parent lists are only computed for "current" nodes (i.e. the lag is
    // always 0)
    public int[][] getCurrentParentIDlist( final int nodeID, final int lag ) throws Exception {
        
        int[][] parentIDlist;
        int intParentCount = getParentCount( nodeID );
        parentIDlist = new int[intParentCount][2];
        
        int parentIndex = 0;
        
        currentVarOffset = nodeID*offsetVariables;
        // Go through the parent nodes for the node with id=nodeID
        for ( int k=0; k<maxMarkovLag+1-minMarkovLag; k++ ) {
            
            currentLagOffset = k*offsetLags;
            for ( int j=0; j<varCount && parentIndex < intParentCount; j++ ) {
                
                if ( matrix[ currentVarOffset + currentLagOffset + j ] == 1 ) {
                    
                    // Per convention, [index][0] holds the nodeID
                    parentIDlist[parentIndex][0] = j;
                    
                    // and [index][1] holds the lag
                    parentIDlist[parentIndex][1] = k+minMarkovLag;
                    parentIndex++;
                }
            }
        }
        
        return parentIDlist;
    }
    
    protected int getParentCount( final int _nodeID ) {
        
        int parentCount = 0;

        currentVarOffset = _nodeID*offsetVariables;
        // Count the parent nodes for the node with id=nodeID
        for ( int k=0; k<maxMarkovLag+1-minMarkovLag; k++ ) {
            
            currentLagOffset = k*offsetLags;
            for ( int j=0; j<varCount; j++ ) {
                
                if ( matrix[ currentVarOffset + currentLagOffset + j ] == 1 ) {
                    
                    parentCount++;
                }
            }
        }
        
        return parentCount;
    }
    ////////////////////////
    
    public Object clone() {
        
        // Note: This only does shallow cloning
        try {
            
            EdgesI cloned = (EdgesI) super.clone();
                        
            return cloned;
        }
        catch ( Exception e ) {
            
            System.out.println(
                    "(EdgesAsMatrix.clone) cloning failed. Returning null object.");
            
            return null;
        }
    }

    /**
     * @return A custom string representation for a set of edges.
     */
    public String toStringAsAdjacencyMatrix() {
        
        StringBuffer tmpStr = new StringBuffer( varCount * varCount );
        
        for (int i=0; i<varCount; i++) {
            for (int j=0; j<varCount; j++) {
                for (int k=0; k<maxMarkovLag+1-minMarkovLag; k++) {
                    
                    if ( i == j && k == 0 ) {
                        
                        // indicator that for lag=0, nodes can't be parents of themselves
                        tmpStr.append( "-" );
                    }
                    else {

                        tmpStr.append( matrix[ i*offsetVariables + k*offsetLags + j ] );
                    }
                    
                    // Add delimiter between (parent) entries
                    tmpStr.append( " " );                   
                }
                // Add delimiter between blocks of "markovLags"
                tmpStr.append( "\t" );
            }
            // Add delimiter between variables
            tmpStr.append( "\n" );
        }
        return tmpStr.toString();
    }

    /**
     * @return A special string representation for a set of edges that includes the
     * IDs for the variables.
     */
    public String toStringWithIDs() {
        
        StringBuffer tmpStr = new StringBuffer( varCount * varCount );
        // Add header: (need to make formatting more generic, but ok for quick test
        tmpStr.append( "    " ); 
        for (int i=0; i<varCount; i++) {
            tmpStr.append( i );
            tmpStr.append( "  " ); 
            if (i<10) tmpStr.append( " " ); }
        tmpStr.append( "\n" );
        for (int i=0; i<varCount; i++) {
            tmpStr.append( i );
            tmpStr.append( ": " );
            if (i<10) tmpStr.append( " " );
            for (int j=0; j<varCount; j++) {
                for (int k=0; k<maxMarkovLag+1-minMarkovLag; k++) {

                    tmpStr.append( matrix[ i*offsetVariables + k*offsetLags + j ] );
                    // Add delimiter between (parent) entries
                    tmpStr.append( "   " );                 
                }
                // Add delimiter between "maxMarkovLag lines"
                tmpStr.append( "\t" );
            }
            // Add delimiter between variables
            tmpStr.append( "\n" );
        }
        return tmpStr.toString();
    }

    /**
     * @return Another special string representation for a set of edges: This
     * one prints all entries of the matrix.
     */
    public String toStringAsTest() {
        
        // Output in string form, using the Proprietary format
        StringBuffer strStructure = new StringBuffer( varCount * varCount );
        StringBuffer strParentList = new StringBuffer( 2*varCount );
        StringBuffer strFormatted = new StringBuffer( 2*varCount );
        String strTmp = new String();
        int parentCount;

        for (int i=0; i<varCount; i++) {
            
            parentCount = 0;
            strParentList = new StringBuffer( 2*varCount );
            strFormatted = new StringBuffer( 2*varCount );
            
            for (int j=0; j<varCount; j++) {
                for (int k=0; k<maxMarkovLag+1; k++) {
                
                    if ( matrix[ i*offsetVariables + j + 0 ] == 1 ) {
                        strParentList.append( " " );
                        strParentList.append( j );
                        parentCount++;
                    }
                }
                strStructure.append( "  \t  " );
            }
            
            //strFormatted.append( "   " );
            strFormatted.append( i );
            strFormatted.append( ":" );
            strFormatted.append( parentCount );
            strFormatted.append( strParentList );
            strTmp = strFormatted.toString();
            strTmp = StringUtil.fillSpaces( strTmp, 9 );

            strStructure.append( strTmp );
            strStructure.append( " " );
        }
         
        return strStructure.toString();
    }
    
    /**
     * @return A special string representation for a set of edges in a 
     * simplified (proprietary) format (the same one that we use for
     * input and output of networks).
     */
    public String toString() {
        
        // Output in string form, using the Banjo (proprietary) format
        StringBuffer strStructure = new StringBuffer( varCount * varCount );
        StringBuffer strParentList = new StringBuffer( 2*varCount );
        int parentCount;

        strStructure.append( varCount );
        
        for (int i=0; i<varCount; i++) {
            
            strStructure.append( "\n" );
            
            // Basic formatting for columnar display
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
            if ( varCount > 9999 && varCount < 100000 ) {

                // Adjust the variable index to fit columnar format
                if ( i < 10000 ) strStructure.append( " " );
                if ( i < 1000 ) strStructure.append( " " );
                if ( i < 100 ) strStructure.append( " " );
                if ( i < 10 ) strStructure.append( " " );
            }
            
            strStructure.append( i );
            strStructure.append( " " );
            
            for (int k=0; k<maxMarkovLag+1-minMarkovLag; k++) {

                parentCount = 0;
                if ( maxMarkovLag > 0 ) {
                    
                    strStructure.append( " " );
                    strStructure.append( k + minMarkovLag );
                    strStructure.append( ":" );
                }
                
                strParentList = new StringBuffer("");
                
                for (int j=0; j<varCount; j++) {

                    if ( matrix[ i*offsetVariables + k*offsetLags + j ] == 1 ) {
                        
                        strParentList.append( " " );
                        strParentList.append( j );
                        parentCount++;
                    }
                }
                
                strStructure.append( parentCount );
                strStructure.append( strParentList );
                strStructure.append( "  \t  " );
            }
        }
         
        return strStructure.toString();
    }
}

