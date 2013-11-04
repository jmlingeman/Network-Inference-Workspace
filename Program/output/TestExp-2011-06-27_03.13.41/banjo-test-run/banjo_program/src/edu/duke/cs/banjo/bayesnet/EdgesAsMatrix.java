/*
 * Created on Jun 1, 2004
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
 * Contains the basic adjacency matrix implementation.
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
 * Created on Jun 1, 2004 
 * 
 * <br>
 * (v2.0) Use of the more efficient parent set implementations. Note that the original
 * parent sets from version 1.0.x will be deprecated in an upcoming maintenance release.
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 * 
 */
public class EdgesAsMatrix implements Cloneable, EdgesI {

	// Stores the parent configuration for the variables in a matrix such that
	// - dim 1 refers to the variable's index
	// - dim 2 refers to the parent variable's index
	// - dim 3 refers to the lag (determined by the maxMarkovLag of the 
    //			Markov chain) of the parent
    //
	// Remember that maxMarkovLag 0 means dimension #3 of array/matrix is 1
    
	// hjs 11/29/04 After profiling, decide to break encapsulation and allow public 
    // access: this eliminates a large number of getEntry calls in the proposer class
    public int[][][] matrix;
    	
	protected final int varCount;
	protected final int maxMarkovLag;
	protected final int minMarkovLag;

	public EdgesAsMatrix( final int _varCount, 
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
        matrix = new int[_varCount][_varCount][_maxMarkovLag+1];
	}
    
    public int[][][] getMatrix() {
        return matrix;
    }
    
	/**
	 * @param _matrix The matrix to set (reference assignment only).
	 */
	public void setMatrix(final int[][][] _matrix) {
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
	public void assignMatrix( final int[][][] _matrix ) {
	    
		for (int i=0; i<varCount; i++) {
			for (int j=0; j<varCount; j++) {
				for (int k=0; k<maxMarkovLag+1; k++) {
					this.matrix[i][j][k] = _matrix[i][j][k];
				}				
			}
		}
	}
	
	public void assignMatrix( final EdgesI _edges ) throws Exception {
		    
        if ( _edges instanceof EdgesAsMatrix ) {
            
            // Create a reference to the edgeMatrix, so we don't
            // have to loop through getEntry calls (performance shortcut)
            int[][][] tmpEdgeMatrix = (( EdgesAsMatrix ) _edges).getMatrix();
            for (int i=0; i<varCount; i++) {
                for (int j=0; j<varCount; j++) {
                    for (int k=minMarkovLag; k<maxMarkovLag+1; k++) {
                        this.matrix[i][j][k] = tmpEdgeMatrix[i][j][k];
                    }               
                }
            }
        }
        else if ( _edges instanceof EdgesAsArray ) {
            
            // This defines a mapping between our original EdgesAsMatrix and the new
            // EdgesAsMatrixCompactLagFirst representation
            
            // Create a reference to the edgeMatrix, so we don't
            // have to loop through getEntry calls (performance shortcut)
            int[] tmpEdgeMatrix = (( EdgesAsArray ) _edges).getMatrix();
            int mappedIndex;
            
            final int dimVariables;
            final int dimParents;
            final int dimLags;
            final int offsetVariables;
            final int offsetParents;
            final int offsetLags;
            
            dimVariables = varCount;
            dimParents = varCount;
            dimLags = maxMarkovLag - minMarkovLag + 1;
            
            // Parents are listed consecutively within each lag:
            offsetParents = 1;
            // The index "offset" between consecutive lags:
            offsetLags = dimParents;
            // The index "offset" between consecutive variables:
            offsetVariables = dimParents * dimLags;
                        
            for (int i=0; i<varCount; i++) {
                for (int j=0; j<varCount; j++) {
                    for (int k=0; k<maxMarkovLag+1-minMarkovLag; k++) {
                        
                        mappedIndex = i*offsetVariables + k*offsetLags + j;
                        this.matrix[i][j][k+minMarkovLag] = tmpEdgeMatrix[ mappedIndex ];
                    }               
                }
            }
        }
        else {
            
            throw new BanjoException( BANJO.ERROR_BANJO_DEV, 
                    "(" + StringUtil.getClassName( this ) 
                    + ".assignMatrix) Developer issue: "
                    + "Encountered invalid data type for the EdgesI _edges." );
        }
	}

	
	public void assignMatrix( final EdgesAsMatrix _edgeMatrix ) throws Exception {

        if ( _edgeMatrix instanceof EdgesAsMatrix ) {
            
    		// Create a reference to the edgeMatrix, so we don't
    		// have to loop through getEntry calls
    		int[][][] tmpEdgeMatrix = _edgeMatrix.getMatrix();
    		for (int i=0; i<varCount; i++) {
    			for (int j=0; j<varCount; j++) {
    				for (int k=minMarkovLag; k<maxMarkovLag+1; k++) {
    					this.matrix[i][j][k] = tmpEdgeMatrix[i][j][k];
    				}				
    			}
    		}
        }
        else {
            
            throw new BanjoException( BANJO.ERROR_BANJO_DEV, 
                    "(" + StringUtil.getClassName( this ) 
                    + ".assignMatrix) Developer issue: "
                    + "Encountered invalid data type for the EdgesAsMatrix _edgeMatrix." );
        }
	}
	
	// Access any individual entry in the parent matrix
	public int getEntry( final int _varIndex, final int _parentVarIndex, final int _lag ) {
		
	    return matrix[_varIndex][_parentVarIndex][_lag];
	}
	
	// Unrestricted assignment of values in matrix (see EdgesAsMatrixWithCachedStatistics subclass)
	public void setEntry( final int _varIndex, final int _parentVarIndex, final int _lag,
	        final int _newValue ) throws Exception {
		
	    matrix[_varIndex][_parentVarIndex][_lag] = _newValue;
	}

	public boolean hasIdenticalEntries( final EdgesI _otherMatrix ){

        if ( _otherMatrix instanceof EdgesAsMatrix ) {
            
    	    // Define a matrix as performance shortcut:
    	    int[][][] tmpMatrix = (( EdgesAsMatrix ) _otherMatrix ).getMatrix();
    		for (int i=0; i<varCount; i++) {
    			for (int j=0; j<varCount; j++) {
    				for (int k=minMarkovLag; k<maxMarkovLag+1; k++) {
    					if ( this.matrix[i][j][k] != tmpMatrix[i][j][k] ) {
    					    
    						return false;
    					}
    				}				
    			}
    		}
        }
        else {
            
            // can't throw DEV error here: may want to simply record as issue
        }
		return true;
	}

	public boolean hasIdenticalEntries( final EdgesAsMatrix _otherMatrix ){
		  
		for (int i=0; i<varCount; i++) {
			for (int j=0; j<varCount; j++) {
				for (int k=minMarkovLag; k<maxMarkovLag+1; k++) {
					if ( this.matrix[i][j][k] != _otherMatrix.matrix[i][j][k] ) {
						return false;
					}
				}				
			}
		}	
		return true;
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
	public int[][] getCurrentParentIDlist( final int _nodeID, final int _lag ) {
		
		int[][] parentIDlist;
		int intParentCount = getParentCount( _nodeID );
		parentIDlist = new int[intParentCount][2];
		
		int parentIndex = 0;
		
		// Go through the parent nodes for the node with id=nodeID
		for (int i=0; i<varCount && parentIndex < intParentCount; i++) {
			for (int k=minMarkovLag; k<maxMarkovLag+1; k++) {
				
				if ( matrix[_nodeID][i][k] == 1 ) {
				    
				    // Per convention, [index][0] holds the nodeID
					parentIDlist[parentIndex][0] = i;
					
					// and [index][1] holds the lag
					parentIDlist[parentIndex][1] = k;
					parentIndex++;
				}
			}
		}
		
		return parentIDlist;
	}
	
	protected int getParentCount( final int _nodeID ) {
	    
	    int parentCount = 0;
		
		// Count the parent nodes for the node with id=nodeID
		for (int i=0; i<varCount; i++) {
			for (int k=minMarkovLag; k<maxMarkovLag+1; k++) {
				
				if ( matrix[_nodeID][i][k] == 1 ) {
				    
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
				for (int k=0; k<maxMarkovLag+1; k++) {
					
				    if ( i == j && k == 0 ) {
				        
				        // indicator that for lag=0, nodes can't be parents of themselves
				        tmpStr.append( "-" );
				    }
				    else {
					
				        tmpStr.append( matrix[i][j][k] );
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
				for (int k=0; k<maxMarkovLag+1; k++) {
					
				    tmpStr.append( matrix[i][j][k] );
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
				
					if ( matrix[i][j][0] == 1 ) {
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
			
			strStructure.append( i );
			strStructure.append( " " );
			
			for (int k=minMarkovLag; k<maxMarkovLag+1; k++) {

				parentCount = 0;
				if ( maxMarkovLag > 0 ) {
					
				    strStructure.append( " " );
					strStructure.append( k );
					strStructure.append( ":" );
				}
				
				strParentList = new StringBuffer("");
			    
			    for (int j=0; j<varCount; j++) {
			    				    
					if ( matrix[i][j][k] == 1 ) {
					    
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
