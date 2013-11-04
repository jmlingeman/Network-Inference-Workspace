/*
 * Created on Mar 3, 2004
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
import edu.duke.cs.banjo.data.settings.*;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Contains the data structures built around an adjacency matrix-approach for storing 
 * the network structure.
 *
 * <p><strong>Details:</strong> <br>
 * 
 * - Contains a number of "counts" and other auxilliary variables associated 
 * with the adjacency matrix, for efficient access to matrix data required by the 
 * core objects.  <br>
 * - Example:  (3 variables, called 0,1, and 2, of max. Markov lag 1) <br>
 * 0 1 0 &nbsp&nbsp&nbsp	0 0 1	&nbsp&nbsp&nbsp
 * 		- variable 0 has variable 1 as parent of lag 0, 
 * 			and variable 2 as parent of lag 1 <br>
 * 1 0 1 &nbsp&nbsp&nbsp 0 0 0	&nbsp&nbsp&nbsp
 * 		- variable 1 has variables 0 and 2 as parents of lag 0, 
 * 			and no parents of lag 1 <br>
 * 0 0 0 &nbsp&nbsp&nbsp 0 1 0	&nbsp&nbsp&nbsp
 * 		- variable 2 has no parents of lag 0, 
 * 			and variable 1 as parent of lag 1 <br>
 * - Can (optionally) be loaded from structure file.
 * 
 * <p><strong>Change History:</strong> <br>
 * Created on Mar 3, 2004
 * 
 * <p>
 * 2/22/2006 (v1.0.6/v2.0) hjs		Correct the computation of the parent counts 
 * 									in the subtractMatrix method.
 * <>
 * 3/7/2006 (v2.0)	hjs		Add regular expression parsing to loading of network structures:
 * 							Add additional constructor for extra feedback on a variety of
 * 							input errors. New code also supports comments and blank lines
 * 							in files.
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class EdgesAsMatrixWithCachedStatistics extends EdgesAsMatrix 
		implements Cloneable, EdgesWithCachedStatisticsI {
		
	// Remember that maxMarkovLag 0 means dimension of array/matrix is 1
	
	// Store the parentCounts for the variables (maintained transparently
	// when parentMatrix is updated); this is simply the number of 1's in
	// the row corresponding to a variable
	private int[] parentCount;
	// Store the number of all parent nodes (again: performance-wise it's
	// better to maintain this value that to recompute it for every loop)
	private int combinedParentCount;
	
	// Store the parentIDs for a node. We need this array as a temporary 
	// storage so we can efficiently compute cycles (and being able to apply
	// a bayesNetChange before it takes effect in the actual network)
	private int[] parentIDs;
	// Additional storage containers for computing cycles
	private int[] ancestorSet;
	private int[] nodesVisited;
	private int[] nodesToVisit;
	
	// Info where cycle was encountered;
	private int cycleAtNode = -1; 
	
    /**
     * Basic constructor for creating the initial matrix and its supporting structures.
     */
	public EdgesAsMatrixWithCachedStatistics(
	        final int _varCount, 
	        final int _minMarkovLag, 
	        final int _maxMarkovLag ) throws Exception {
		        
	    // hand off the low level stuff
		super( _varCount, _minMarkovLag, _maxMarkovLag );
        
        // Create the parentCount vector
        parentCount = new int[_varCount];

        // Allocate the containers used for computing cycles 
        parentIDs = new int[_varCount];
        ancestorSet = new int[_varCount];
        nodesVisited = new int[_varCount];
        nodesToVisit = new int[_varCount];
        
        // Initialize the matrix and the parentCount vector
        initMatrix();
	
        // Note: Only the newer "asArray" implementation can handle the Shmueli-based
        // DFS code, and we will not retrofit this class, hence the exception.
        // (Since this class only supports a apply-then-check approach, that is all we validate)
            
        // LAST-MINUTE INFO:
        // ONLY the "dfsOrig" value for the cycle-checker method is supported in this class!!
        throw new BanjoException(
               BANJO.ERROR_BANJO_DEV, 
               " (" + StringUtil.getClassName(this) + 
               " constructor) " + 
               "Developer Alert: " +
               "When using this class, the cycleChecker needs to be set to applyThenCheck! " +
               "\nSimply comment out this exception if you are using this class. " +
               "\nAlternatively, you can modify the code to have access to the settings class, " +
               "to get access to the dynamicProcessParameters. " +
               "(See the edgesAsArrayWithCachedStatistics as an example)" +
               "\nWe didn't implement this because we will deprecate this class in an " +
               "upcoming maintenance release." );
	}
	
    /**
     * Constructor used for loading a set of nodes (such as the 
     * must-be-absent-parents set) into the matrix structure, using a 
     * minimal amount of validation only. 
     */
	public EdgesAsMatrixWithCachedStatistics( 
	        final int _varCount, 
	        final int _minMarkovLag, 
	        final int _maxMarkovLag, 
			final String _directory, 
			final String _fileName ) throws Exception {
		
		super( _varCount, _minMarkovLag, _maxMarkovLag );

	    loadStructureAsNodes( _directory, _fileName );
	}

    /**
     * Constructor used for loading a network structure (such as the 
     * must-be-present-parents or the initial structure), using an extensive
     * amount of validation. 
     */
	public EdgesAsMatrixWithCachedStatistics( 
	        final int _varCount, 
	        final int _minMarkovLag, 
	        final int _maxMarkovLag, 
			final String _directory, 
			final String _fileName, 
			final Settings _processData ) throws Exception {
		
		super( _varCount, _minMarkovLag, _maxMarkovLag );

	    loadStructureFile( _directory, _fileName, _processData );
	}
	
	public EdgesAsMatrixWithCachedStatistics( 
	        final EdgesAsMatrix _edgesAsMatrix ) throws Exception {
		
	    // 1/18/2006 (v2.0) hjs We calling this super constructor (and not the
	    // basic constructor from this class) because it is more efficient,
	    // at the price of some code duplication.
		super( _edgesAsMatrix.varCount, 
		        _edgesAsMatrix.minMarkovLag, 
		        _edgesAsMatrix.maxMarkovLag );

		// Create the parentCount vector
		parentCount = new int[varCount];

		// Allocate the containers used for computing cycles 
		parentIDs = new int[varCount];
		ancestorSet = new int[varCount];
		nodesVisited = new int[varCount];
		nodesToVisit = new int[varCount];
		
		for (int i=0; i<varCount; i++) {

		    // Set all parentCounts to 0
			parentCount[i] = 0;
			
			// Set all matrix entries to 0
			for (int j=0; j<varCount; j++) {
				for (int k=0; k<maxMarkovLag+1; k++) {
				    
					matrix[i][j][k] = _edgesAsMatrix.matrix[i][j][k];
					parentCount[i] += _edgesAsMatrix.matrix[i][j][k];
				}
			}
		}
		
		// and set the grand total
		combinedParentCount = 0;
		for (int i=0; i<varCount; i++) {
		    
		    combinedParentCount += parentCount[i];
		}
	}
		
	// Initialize the parent matrix to all 0's, indicating "no parent" relationships.
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#initMatrix()
     */
	public void initMatrix() {
		
		for (int i=0; i<varCount; i++) {

		    // Set all parentCounts to 0
			parentCount[i] = 0;
			
			// Set all matrix entries to 0
			for (int j=0; j<varCount; j++) {
				for (int k=0; k<maxMarkovLag+1; k++) {
				    
					matrix[i][j][k] = 0; 
				}
			}
		}
		// and set the grand total
		combinedParentCount = 0;
	}
	
	// Initialize the parent matrix to a specified value (typically, to all '1's)
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#initMatrix(int)
     */
	public void initMatrix( final int valueToInitTo ) {

		combinedParentCount=0;

		// More verbose, but better performance by checking on the value first
		if ( valueToInitTo == 0 ) {
		    
			for (int i=0; i<varCount; i++) {
	
				parentCount[i] = 0;
				
				// Set the matrix entries and adjust the parent counts
				for (int j=0; j<varCount; j++) {
					for (int k=0; k<maxMarkovLag+1; k++) {
					    
						matrix[i][j][k] = valueToInitTo;
					}
				}
			}
		}
		else {
		    
		    for (int i=0; i<varCount; i++) {
			
				parentCount[i] = 0;
				
				// Set the matrix entries and adjust the parent counts
				for (int j=0; j<varCount; j++) {
					for (int k=0; k<maxMarkovLag+1; k++) {
					    
						matrix[i][j][k] = valueToInitTo;
						    
							parentCount[i]++;
							combinedParentCount++;
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#assignMatrix(edu.duke.cs.banjo.bayesnet.EdgesAsMatrixWithCachedStatistics)
     */
	public void assignMatrix( 
	        final EdgesWithCachedStatisticsI _matrixToAssign ) 
			throws Exception {
			            
        EdgesAsMatrixWithCachedStatistics matrixToAssign;
        if ( _matrixToAssign instanceof EdgesAsMatrixWithCachedStatistics ) {
            
            matrixToAssign = ( EdgesAsMatrixWithCachedStatistics ) _matrixToAssign;
                
    		if (this.varCount == matrixToAssign.varCount && 
    				this.maxMarkovLag == matrixToAssign.maxMarkovLag) {
    			
    		    for (int i=0; i<varCount; i++) {
    
    			    // Assign the parentCounts
    				parentCount[i] = matrixToAssign.parentCount[i];
    				
    				// Assign the matrix entries
    				for (int j=0; j<varCount; j++) {
    					for (int k=0; k<maxMarkovLag+1; k++) {
    					    
    						matrix[i][j][k] = matrixToAssign.matrix[i][j][k]; 
    					}
    				}
    			}
    		    
    			// Assign the grand total
    			combinedParentCount = matrixToAssign.combinedParentCount;
    		}
    		else {
    		    
    			// Flag as internal error - matrix dimensions don't match
    		    // (this should never happen..)
    			throw new BanjoException(
    			        BANJO.ERROR_BANJO_DEV, 
    			        "(EdgesAsMatrixWithCachedStatistics.assignMatrix) " +
    			        	"Development issue: Dimensions of matrices don't match!");		
    		}
        }
        else {
            
            // dev error: we should never end up here
            throw new BanjoException( 
                    BANJO.ERROR_BANJO_DEV, 
                    "(" + StringUtil.getClassName( this ) + ".assignMatrix) " +
                    "Development issue: Arguments are of unexpected type (" +
                    "need: 'EdgesAsMatrixWithCachedStatistics')" );
        }
	}	

	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#subtractMatrix(edu.duke.cs.banjo.bayesnet.EdgesAsMatrixWithCachedStatistics)
     */
	public void subtractMatrix( 
	        final EdgesWithCachedStatisticsI _matrixToSubtract ) 
			throws Exception {
        
        EdgesAsMatrixWithCachedStatistics matrixToSubtract;
        if ( _matrixToSubtract instanceof EdgesAsMatrixWithCachedStatistics ) {
            
            matrixToSubtract = ( EdgesAsMatrixWithCachedStatistics ) _matrixToSubtract;
            
    	    int[][][] tmpMatrix = matrixToSubtract.getMatrix();
    	    			
    	    for (int i=0; i<varCount; i++) {
    
    	        // 2/22/2006 (v1.0.6/v2.0) hjs Correct the computation of the parent counts:
    	        // This line should not be here at all, because we need to start out
    	        // with the parent counts of the current matrix!!
    	        ////parentCount[i] = matrixToSubtract.getParentCount(i);
    			
    			// Set the matrix entries
    			for (int j=0; j<varCount; j++) {
    				for (int k=0; k<maxMarkovLag+1; k++) {
    				    
    				    if ( tmpMatrix[i][j][k] == 1 ) {
    				        
    				        if ( matrix[i][j][k] == 1 ) {
    				            
    				            parentCount[i]--;
    				            combinedParentCount--;
    				            matrix[i][j][k] = 0;
    				        }
    					    // "else case": the original matrix keeps its value, and
    					    // there is no need to adjust any parent count
    				    }
    				    // "else case": subtract 0, so no change
    				}
    			}
    		}
        }
        else {
            
            // dev error: we should never end up here
            throw new BanjoException( 
                    BANJO.ERROR_BANJO_DEV, 
                    "(" + StringUtil.getClassName( this ) + ".subtractMatrix) " +
                    "Development issue: Arguments are of unexpected type (" +
                    "need: 'EdgesAsMatrixWithCachedStatistics')" );
        }
	}
	
	// Load a set of nodes (i.e., not an adjacency matrix) from a file
	private void loadStructureAsNodes(
	        final String _directory, 
	        final String _fileName ) 
					throws Exception {
		
		try {
			
			File dataFile = new File( _directory, _fileName );
			BufferedReader bufferedReader = null;
			
			if (!dataFile.exists()) {
			    				
				throw new BanjoException( 
				        BANJO.ERROR_BANJO_USERINPUT, 
				        "\n *** (EdgesAsMatrixWithCachedStatistics.loadFromFile) " +
				        "Cannot find the structure file: '" + _fileName + "' " +
				        "in directory '" + _directory + "'." );
			}

			bufferedReader = new BufferedReader(
			        new FileReader( _directory + File.separator + _fileName ));
								
			// -------------------------
			// Set up the matrix itself	
			// -------------------------
			
			// First check the loaded data for consistency: read in the first
			// line, which contains the variable count
			String firstTextline = "";
			String varCountReadFromFile = "";
			boolean varCountFound = false;
			
			// Set up boolean flags so we can track which variable has already been loaded
			boolean[] varAlreadyLoaded = new boolean[ varCount ];
			for ( int varIndex=0; varIndex< varCount; varIndex++ ) 
			    varAlreadyLoaded[ varIndex ] = false;
			
			Pattern varCountPattern = Pattern.compile( 
	                BANJO.PATTERN_VARCOUNT, Pattern.COMMENTS );
						
			// [For node sets, we don't need to check against the max. parent count]

			while ( !varCountFound && firstTextline != null ) {
			    
				firstTextline = bufferedReader.readLine();
				
		        String[] strFirstTextline = varCountPattern.split( firstTextline );
					
		        // The first item on this line needs to be the varCount, possibly
		        // followed by a comment (which will start with the # token)
				varCountReadFromFile = strFirstTextline[0];
		    
				// Ignore any line until the first entry is an integer, which needs
				// to match the variable count from the settings file
				try {
					if ( Integer.parseInt( varCountReadFromFile ) == varCount ) {
					    
					    varCountFound = true;
					}
					else {
					    
					    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
				            "The first entry (=" + varCountReadFromFile +
				            ") on the first (non-comment) line in the input file '" 
				            + _directory + File.separator + _fileName +
				            "' does not match the variable count " +
				            "given in the settings file (=" +
				            varCount + ").");
					}
				}
				catch ( BanjoException e ) {

				    throw new BanjoException( e );
				}
				catch ( Exception e ) {

				    // Ok to ignore: we end up here because there was no varCount
				}
			}
			
			// If we can't find the variable count, then don't bother parsing the remainder
			// of the structure file
			if ( !varCountFound ) {
			    
			    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
			            "The first entry on the first (non-comment) line in the input file '" 
			            + _directory + File.separator + _fileName +
			            ", which must be equal to the variable count, " +
			            "does not match the varCount " +
			            "given in the settings file (= " +
			            varCount + ").");
			}
			
			// Need to create the parent array:
			this.parentCount = new int[varCount];
			
			ancestorSet = new int[varCount];
			parentIDs = new int[varCount];
			nodesToVisit = new int[varCount];
			nodesVisited = new int[varCount];
			
			// Now it's on to populating the values:
			initMatrix();
			
			
			if ( maxMarkovLag > 0 ) {
			    
				// Example of a dbn structure to load:
			    //
				//20
				// 0   0:   0                 1:   2 0 7          
				// 1   0:   0                 1:   1 1            
				// 2   0:   0                 1:   3 0 1 2        
				// 3   0:   0                 1:   2 2 3   
				// ...
				// 19  0:	0                 1:   1 18
							
				String strParentCount = "";
				int intParentCount;
				// Used for the currently processed lag:
				int intMarkovLag = -1;
				int intNodeID = -1;
								
				String nextInputLine = "";
				String nextTextlineTrimmed = "";
				Pattern dbnPattern = Pattern.compile( 
		                BANJO.PATTERN_DYNAMICBAYESNET );
				Pattern dbnItemPattern = Pattern.compile( 
		                BANJO.PATTERN_DYNAMICBAYESNETITEM );
				
		        String[] dbnLagList;
				String[] dbnItemList;
				
				while ( nextInputLine != null ) {
					
				    nextTextlineTrimmed = nextInputLine.trim();	
				    
					// First split off any comments (Pattern.COMMENTS doesn't
					// do it for plain whitespace separators)
					if ( nextTextlineTrimmed.indexOf( BANJO.DEFAULT_COMMENTINDICATOR ) >= 0 ) {
					    
					    nextTextlineTrimmed = nextInputLine.substring( 0 , 
					            nextInputLine.indexOf( BANJO.DEFAULT_COMMENTINDICATOR )).trim();
					}

					// Ignore any blank line
					if ( !nextTextlineTrimmed.equals( "" ) ) {
						
						// Now split the remaining text into pieces
				        dbnLagList = dbnPattern.split( nextTextlineTrimmed );
				        
				        for ( int lagListindex=0; lagListindex<dbnLagList.length ; 
				        		lagListindex++ ) {
						            
				            dbnItemList = dbnItemPattern.split( 
					                dbnLagList[ lagListindex ] );
				            
				            // We need to process the first item from the dbnLagList differently
				            // (it only contains the variable and the first lag index)
				            if ( lagListindex==0 ) {
		
				                if ( dbnItemList.length < 2 ) {
					                
					                throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
						                    "The input line '" + nextInputLine  + 
						                    "' in '" + _fileName + "' needs to start by " +
				                    		"specifying the variable and the (first) lag." );
				                    
				                }
				                
				                // process the variable index
					            try {
					                
									intNodeID = Integer.parseInt( dbnItemList[ 0 ] );
					            }
					            catch ( Exception e ) {
					                
					                throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
						                    "The input line '" + nextInputLine  + 
						                    "' in '" + _fileName + "' does not " +
				                    		"specify the variable as integer." );
					            }

					            // Make sure that the same variable isn't specified twice
					            if ( varAlreadyLoaded[ intNodeID ] ) {
					                
					                throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
						                    "The input line '" + nextInputLine  + 
						                    "' in '" + _fileName + "' is a repeat entry " +
				                    		"for variable '" + intNodeID + 
				                    		"'." );
					            }
					            else {
					                
					                varAlreadyLoaded[ intNodeID ] = true;
					            }
					            
					            // process the number indicating the lag
					            try {
		
						            intMarkovLag = Integer.parseInt( dbnItemList[ 1 ] );
					            }
					            catch ( Exception e ) {
					                
					                throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
						                    "The input line '" + nextInputLine  + 
						                    "' in '" + _fileName + "' does not " +
				                    		"specify the first lag index as integer." );
					            }
					            
					            // Note: These 2 checks would potentially invalidate some older
					            // structure files, so we decided against using them (instead,
					            // we'll ignore any lag info the lies outside the acceptable range)
					            // Validate the lag:
//				                if ( intMarkovLag < minMarkovLag ) {
//		
//								    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
//						                    "The input line '" + nextInputLine  + 
//						                    "' in '" + fileName + 
//						                    "' specifies a lag (=" +
//						                    intMarkovLag +
//						                    ") for variable '" + intNodeID +
//						                    "' that is smaller than the min. Markov Lag (=" 
//						                    + minMarkovLag + ")." );
//				                }
//				                if ( intMarkovLag > maxMarkovLag ) {
//		
//								    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
//						                    "The input line '" + nextInputLine  + 
//						                    "' in '" + fileName + 
//						                    "' specifies a lag (=" +
//						                    intMarkovLag +
//						                    ") for variable '" + intNodeID +
//						                    "' that is larger than the max. Markov Lag (=" 
//						                    + maxMarkovLag + ")." );
//				                }
				            }
				            else {
		
								int intFoundParentCount;
										
						        if ( dbnItemList.length > 0 ) {
		
						            try {
						                
						                // Following the lag is the parent count
										strParentCount = dbnItemList[ 0 ];
										intParentCount = Integer.parseInt( strParentCount );
										
										// treat the last item differently
										if ( lagListindex == dbnLagList.length-1 ) {
										
										    intFoundParentCount = dbnItemList.length - 1;
										}
										else {
										    
										    intFoundParentCount = dbnItemList.length - 2;
										}
										
										if ( intParentCount > intFoundParentCount ) {
										    
										    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
								                    "The input line '" + nextInputLine  + 
								                    "' in '" + _fileName + 
								                    "' specifies a parent count of " +
								                    intParentCount +
								                    " for variable '" + intNodeID +
								                    "', but provides only " + 
								                    intFoundParentCount +
								                    " parent IDs." );
										}
										
										if ( intParentCount < intFoundParentCount ) {
										    
										    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
								                    "The input line '" + nextInputLine  + 
								                    "' in '" + _fileName + 
								                    "' specifies a parent count of " +
								                    intParentCount +
								                    " for variable '" + intNodeID +
								                    "', but provides " + 
								                    intFoundParentCount +
								                    " parent IDs." );
										}
										
//										if ( intParentCount > maxParentCount ) {
//										    
//										    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
//								                    "The input line '" + nextInputLine  + 
//								                    "' in '" + fileName + 
//								                    "' specifies a parent count (=" +
//								                    intParentCount + ") " +
//								                    "for variable (=" + intNodeID +
//								                    ") that exceeds the max. parent count (=" +
//								                    maxParentCount + ")." );
//										}
						            }
								    catch ( BanjoException e ) {
						                
						                throw new BanjoException( e );
								    }
								    catch ( Exception e ) {
						                
						                throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
							                    "The input line '" + nextInputLine  + 
							                    "' in '" + _fileName + "' does not " +
					                    		"specify a parent count (=" + strParentCount +
					                    		") as integer." );
								    }
		
								    // Now process the individual parents:
						            try {
						                
										for ( int intParentID=1; 
												intParentID <= intParentCount; intParentID++ ) {
										    
										    int nextParentID;
										    
										    try {
										    
										        nextParentID = Integer.parseInt( 
										                dbnItemList[intParentID] );
										    }
										    catch ( Exception e ) {
								                
								                throw new BanjoException( 
								                        BANJO.ERROR_BANJO_USERINPUT,
									                    "The input line '" + nextInputLine  +
									                    "' in '" + _fileName + "' does not " +
							                    		"specify a parent ID (=" + 
							                    		dbnItemList[intParentID] +
							                    		") as integer." );
										        
										    }
										    
										    if ( nextParentID >= varCount ) {
										        
										        throw new BanjoException( 
										                BANJO.ERROR_BANJO_USERINPUT,
								                    "The input line '" + nextInputLine  + 
								                    "' in '" + _fileName +
						                    		"' specifies a parent (index=" +
						                    		nextParentID + ", i.e. parent " +
						                    		( nextParentID + 1 ) + ") " +
						                    		"that exceeds the variable count (=" +
						                    		varCount + ")." );
										    }
										    try {
										    
										        // This is where we enforce the range of the lag index
										        if ( intMarkovLag >= minMarkovLag && 
										                intMarkovLag <= maxMarkovLag  ) {
										        
										            this.setEntry( 
										                intNodeID, nextParentID, intMarkovLag );
										        }
										    }
										    catch ( BanjoException e ) {
		
											    // If we encounter a BanjoException here, then 
										        // the setEntry call tried to set a value that
										        // had already been set.
											    throw new BanjoException( 
										            BANJO.ERROR_BANJO_USERINPUT,
										            "The input line '" + nextInputLine  + 
								                    "' in '" + _fileName + "' contains " +
										            "parent information for variable '" + 
										            intNodeID + "' that has already been " +
										            "recorded previously. " +
										            "Please check the structure file '" 
										            + _fileName + "' for " +
										            "duplicate parent info for variable '" +
										            intNodeID + "'." );
										    }
										}
						        	}
									catch ( BanjoException e ) {
										
									    throw new BanjoException( e );
									}
									catch ( Exception e ) {
			
									    // If any of the expected values turns out not to be an
									    // integer, the user will have to correct it.
									    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
						                    "The input line '" + nextInputLine  + 
						                    "' in '" + _fileName + "' does not " +
				                    		"specify a variable and its parent info " +
				                    		"in the expected format: all entries " +
		                    				"are expected to be (non-negative) integers." );
									}
						            
									if ( lagListindex != dbnLagList.length-1 ) {
									
									    // Get the new lag: process the number indicating the lag
							            try {
		
								            intMarkovLag = Integer.parseInt( 
								                    dbnItemList[ dbnItemList.length - 1 ] );
							            }
							            catch ( Exception e ) {
							                
							                throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
							                    "The input line '" + nextInputLine  + 
							                    "' in '" + _fileName + "' does not " +
					                    		"specify the first lag index as integer." );
							            }
							            
							            // Again, we don't enforce the range of the lag index here
//							            // Validate the lag:
//						                if ( intMarkovLag < minMarkovLag ) {
//		
//										    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
//							                    "The input line '" + nextInputLine  + 
//							                    "' in '" + fileName + 
//							                    "' specifies a lag (=" +
//							                    intMarkovLag +
//							                    ") for variable '" + intNodeID +
//							                    "' that is smaller than the min. Markov Lag (=" 
//							                    + minMarkovLag + ")." );
//						                }
//						                if ( intMarkovLag > maxMarkovLag ) {
//		
//										    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
//								                    "The input line '" + nextInputLine  + 
//								                    "' in '" + fileName + 
//								                    "' specifies a lag (=" +
//								                    intMarkovLag +
//								                    ") for variable '" + intNodeID +
//								                    "' that is larger than the max. Markov Lag (=" 
//								                    + maxMarkovLag + ")." );
//						                }
									}
						        }
						        else {
						            
						            // We are miising data: there should be at least the variable
						            // index and the parent count
						            throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
						                    "The text line '" + nextInputLine  + 
						                    "' in '" + _fileName + "' does not " +
				                    		"contain enough information to specify " +
				                    		"a variable and its parent info." );
						        }
							}
						}
					}

					nextInputLine = bufferedReader.readLine();
				}
			}
			else {			    
				
				// Example of a static network structure to load:
			    //
				//20
				// 0   2 0 7          
				// 1   1 1            
				// 2   3 0 1 2        
				// 3   2 2 3 
				// ...
				// 19  1 18         
				//

				int intNodeID = -1;
				int intParentCount;
				final int lag = 0;
								
				String nextInputLine = "";
				String nextTextlineTrimmed = "";
				Pattern staticBayesnetPattern = Pattern.compile( 
		                BANJO.PATTERN_STATICBAYESNET );
				
				while ( nextInputLine != null ) {

				    nextTextlineTrimmed = nextInputLine.trim();
				    
					// First split off any comments (Pattern.COMMENTS doesn't
					// do it for plain whitespace separators)
					if ( nextTextlineTrimmed.indexOf( BANJO.DEFAULT_COMMENTINDICATOR ) >= 0 ) {
					    
					    nextTextlineTrimmed = nextInputLine.substring( 0 , 
					            nextInputLine.indexOf( BANJO.DEFAULT_COMMENTINDICATOR )).trim();
					}

					// Ignore any blank line
					if ( !nextTextlineTrimmed.equals( "" ) ) {
						
						// Now split the remaining text into pieces
				        String[] parentIDlist = staticBayesnetPattern.split( 
				                nextTextlineTrimmed );
				        
				        if ( parentIDlist.length > 1 ) {
				            
				            try {
				                
								intNodeID = Integer.parseInt( parentIDlist[ 0 ] );

					            // Make sure that the same variable isn't specified twice
					            if ( varAlreadyLoaded[ intNodeID ] ) {
					                
					                throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
						                    "The input line '" + nextInputLine  + 
						                    "' in '" + _fileName + "' is a repeat entry " +
				                    		"for variable '" + intNodeID + 
				                    		"'." );
					            }
					            else {
					                
					                varAlreadyLoaded[ intNodeID ] = true;
					            }
					            
								intParentCount = Integer.parseInt( parentIDlist[ 1 ] );
								
								if ( intParentCount != parentIDlist.length-2 ) {
								    
								    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
						                    "The input line '" + nextInputLine  + 
						                    "' in '" + _fileName + 
						                    "' specifies a parent count of " +
						                    intParentCount +
						                    " for variable '" + intNodeID +
						                    "', but provides " + (parentIDlist.length-2) +
						                    " parent IDs." );
								}
								
//								if ( intParentCount > maxParentCount ) {
//								    
//								    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
//						                    "The input line '" + nextInputLine  + 
//						                    "' in '" + fileName + 
//						                    "' specifies a parent count (=" +
//						                    intParentCount + ") " +
//						                    "for variable (=" + intNodeID +
//						                    ") that exceeds the max. parent count (=" +
//						                    maxParentCount + ")." );
//								}
								
								for ( int intParentID=parentIDlist.length; 
										intParentID>2; intParentID-- ) {
								    
								    int nextParentID;
								    nextParentID = Integer.parseInt( 
								            parentIDlist[intParentID-1] );
								    
								    if ( nextParentID >= varCount ) {
								        
								        throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
						                    "The input line '" + nextInputLine  + 
						                    "' in '" + _fileName +
				                    		"' specifies a parent (index=" +
				                    		nextParentID + ", i.e. parent " +
				                    		( nextParentID + 1 ) + ") " +
				                    		"that exceeds the variable count (=" +
				                    		varCount + ")." );
								    }
								    try {
								    
								        this.setEntry( intNodeID, nextParentID, lag );
								    }
								    catch ( BanjoException e ) {

									    // If we encounter a BanjoException here, then 
								        // the setEntry call tried to set a value that 
								        // had already been set.
									    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
									            "The input line '" + nextInputLine  + 
							                    "' in '" + _fileName + "' contains " +
									            "parent information for variable '" 
							                    + intNodeID +
									            "' that has already been recorded previously. " +
									            "Please check the structure file '" 
									            + _fileName +
									            "' for duplicate parent info for variable '" +
									            intNodeID + "'." );
								    }
								}
				            }
							catch ( BanjoException e ) {
								
							    throw new BanjoException( e );
							}
							catch ( Exception e ) {
	
							    // If any of the expected values turns out not to be an
							    // integer, the user will have to correct it.
							    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
				                    "The text line '" + nextInputLine  + 
				                    "' in '" + _fileName + "' does not " +
		                    		"specify a variable and its parent info " +
		                    		"in the expected format: all entries " +
	                    			"are expected to be (non-negative) integers." );
							}
				            
				        }
				        else {
				            
				            // We are miising data: there should be at least the variable
				            // index and the parent count
				            throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
				                    "The text line '" + nextInputLine  + 
				                    "' in '" + _fileName + "' does not " +
		                    		"contain enough information to specify " +
		                    		"a variable and its parent info." );
				        }
					}					

					nextInputLine = bufferedReader.readLine();
				}
			}
		}
		catch (IOException e) {
			   
				throw new BanjoException( e, 
				        BANJO.ERROR_BANJO_DEV, 
				        "\n  (EdgesAsMatrixWithCachedStatistics.loadFromFile) - " +
				        "Encountered an I/O error while loading the structure file '" +
				        _fileName + "' from directory '" + _directory + "'." );
		}		
		catch ( BanjoException e ) {

		    	// "forward" any BanjoException
		        throw new BanjoException( e );
		}		
		catch ( Exception e ) {

			throw new BanjoException( e, 
			        BANJO.ERROR_BANJO_DEV,
			        "\n(EdgesAsMatrixWithCachedStatistics.loadFromFile) - " +
			        "Encountered an error while loading the structure file '" +
			        _fileName + "' from directory '" + _directory + "'.");
		}	
	}		
	
	// Load an "adjacency matrix" from a file
	// --------------------------------------
	// Example of a dbn structure to load:
    // Note that the "separation characters" need to be blank spaces!
    //
	//20
	// 0   0:   0                 1:   2 0 7          
	// 1   0:   0                 1:   1 1            
	// 2   0:   0                 1:   3 0 1 2        
	// 3   0:   0                 1:   2 2 3   
	// ...
	// 19  0:	0                 1:   1 18

	private void loadStructureFile(
	        final String _directory, 
	        final String _fileName,
	        final Settings _processData ) 
					throws Exception {
		
		try {
			
			File dataFile = new File( _directory, _fileName );
			BufferedReader bufferedReader = null;
			
			if (!dataFile.exists()) {
			    				
				throw new BanjoException( 
				        BANJO.ERROR_BANJO_USERINPUT, 
				        "\n *** (EdgesAsMatrixWithCachedStatistics.loadFromFile) " +
				        "Cannot find the structure file: '" + _fileName + "' " +
				        "in directory '" + _directory + "'." );
			}

			bufferedReader = new BufferedReader(
			        new FileReader( _directory + File.separator + _fileName ));
								
			// -------------------------
			// Set up the matrix itself	
			// -------------------------
			
			// First check the loaded data for consistency: read in the first
			// line, which contains the variable count
			String firstTextline = "";
			String varCountReadFromFile = "";
			boolean varCountFound = false;
			
			// Set up boolean flags so we can track which variable has already been loaded
			boolean[] varAlreadyLoaded = new boolean[ varCount ];
			for ( int varIndex=0; varIndex< varCount; varIndex++ ) 
			    varAlreadyLoaded[ varIndex ] = false;
			
			Pattern varCountPattern = Pattern.compile( 
	                BANJO.PATTERN_VARCOUNT, Pattern.COMMENTS );
			
			int maxParentCount;
			
			if ( _processData.isSettingValueValid( BANJO.SETTING_MAXPARENTCOUNT ) ) {
		    
			    maxParentCount = Integer.parseInt( 
			            _processData.getValidatedProcessParameter( 
			                BANJO.SETTING_MAXPARENTCOUNT ) );
			}
			else {
			    
			    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT, 
			            "Cannot load the structure file '" + _fileName +
			            "', because the max. parent count is not set " +
			            "(but needed for validation)." );
			}
			
			while ( !varCountFound && firstTextline != null ) {
			    
				firstTextline = bufferedReader.readLine();
				
		        String[] strFirstTextline = varCountPattern.split( firstTextline );
					
		        // The first item on this line needs to be the varCount, possibly
		        // followed by a comment (which will start with the # token)
				varCountReadFromFile = strFirstTextline[0];
		    
				// Ignore any line until the first entry is an integer, which needs
				// to match the variable count from the settings file
				try {
					if ( Integer.parseInt( varCountReadFromFile ) == varCount ) {
					    
					    varCountFound = true;
					}
					else {
					    
					    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
				            "The first entry (=" + varCountReadFromFile +
				            ") on the first (non-comment) line in the input file '" 
				            + _directory + File.separator + _fileName +
				            "' does not match the variable count " +
				            "given in the settings file (=" +
				            varCount + ").");
					}
				}
				catch ( BanjoException e ) {

				    throw new BanjoException( e );
				}
				catch ( Exception e ) {

				    // Ok to ignore: we end up here because there was no varCount
				}
			}
			
			// If we can't find the variable count, then don't bother parsing the remainder
			// of the structure file
			if ( !varCountFound ) {
			    
			    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
			            "The first entry on the first (non-comment) line in the input file '" 
			            + _directory + File.separator + _fileName +
			            ", which must be equal to the variable count, " +
			            "does not match the varCount " +
			            "given in the settings file (= " +
			            varCount + ").");
			}
			
			// Need to create the parent array:
			this.parentCount = new int[varCount];
			
			ancestorSet = new int[varCount];
			parentIDs = new int[varCount];
			nodesToVisit = new int[varCount];
			nodesVisited = new int[varCount];
			
			// Now it's on to populating the values:
			initMatrix();
			
			
			if ( maxMarkovLag > 0 ) {
			    
				// Example of a dbn structure to load:
			    //
				//20
				// 0   0:   0                 1:   2 0 7          
				// 1   0:   0                 1:   1 1            
				// 2   0:   0                 1:   3 0 1 2        
				// 3   0:   0                 1:   2 2 3   
				// ...
				// 19  0:	0                 1:   1 18
							
				String strParentCount = "";
				int intParentCount;
				// Used for the currently processed lag:
				int intMarkovLag = -1;
				int intNodeID = -1;
								
				String nextInputLine = "";
				String nextTextlineTrimmed = "";
				Pattern dbnPattern = Pattern.compile( 
		                BANJO.PATTERN_DYNAMICBAYESNET );
				Pattern dbnItemPattern = Pattern.compile( 
		                BANJO.PATTERN_DYNAMICBAYESNETITEM );
				
		        String[] dbnLagList;
				String[] dbnItemList;
				
				while ( nextInputLine != null ) {
					
				    nextTextlineTrimmed = nextInputLine.trim();	
				    
					// First split off any comments (Pattern.COMMENTS doesn't
					// do it for plain whitespace separators)
					if ( nextTextlineTrimmed.indexOf( BANJO.DEFAULT_COMMENTINDICATOR ) >= 0 ) {
					    
					    nextTextlineTrimmed = nextInputLine.substring( 0 , 
					            nextInputLine.indexOf( BANJO.DEFAULT_COMMENTINDICATOR )).trim();
					}

					// Ignore any blank line
					if ( !nextTextlineTrimmed.equals( "" ) ) {
						
						// Now split the remaining text into pieces
				        dbnLagList = dbnPattern.split( nextTextlineTrimmed );
				        
				        for ( int lagListindex=0; lagListindex<dbnLagList.length ; 
				        		lagListindex++ ) {
						            
						            dbnItemList = dbnItemPattern.split( 
							                dbnLagList[ lagListindex ] );
						            
				            if ( lagListindex==0 ) {
		
				                if ( dbnItemList.length < 2 ) {
					                
					                throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
						                    "The input line '" + nextInputLine  + 
						                    "' in '" + _fileName + "' needs to start by " +
				                    		"specifying the variable and the (first) lag." );
				                    
				                }
				                
				                // process the variable index
					            try {
					                
									intNodeID = Integer.parseInt( dbnItemList[ 0 ] );
					            }
					            catch ( Exception e ) {
					                
					                throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
						                    "The input line '" + nextInputLine  + 
						                    "' in '" + _fileName + "' does not " +
				                    		"specify the variable as integer." );
					            }
					            
					            // Make sure that the same variable isn't specified twice
					            if ( varAlreadyLoaded[ intNodeID ] ) {
					                
					                throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
						                    "The input line '" + nextInputLine  + 
						                    "' in '" + _fileName + "' is a repeat entry " +
				                    		"for variable '" + intNodeID + 
				                    		"'." );
					            }
					            else {
					                
					                varAlreadyLoaded[ intNodeID ] = true;
					            }
					            
					            // process the number indicating the lag
					            try {
		
						            intMarkovLag = Integer.parseInt( dbnItemList[ 1 ] );
					            }
					            catch ( Exception e ) {
					                
					                throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
						                    "The input line '" + nextInputLine  + 
						                    "' in '" + _fileName + "' does not " +
				                    		"specify the first lag index as integer." );
					            }
					            
					            // Note: These 2 checks would potentially invalidate some older
					            // structure files, so we decided against using them (instead,
					            // we'll ignore any lag info the lies outside the acceptable range)
					            // Validate the lag:
//				                if ( intMarkovLag < minMarkovLag ) {
//		
//								    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
//						                    "The input line '" + nextInputLine  + 
//						                    "' in '" + fileName + 
//						                    "' specifies a lag (=" +
//						                    intMarkovLag +
//						                    ") for variable '" + intNodeID +
//						                    "' that is smaller than the min. Markov Lag (=" 
//						                    + minMarkovLag + ")." );
//				                }
//				                if ( intMarkovLag > maxMarkovLag ) {
//		
//								    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
//						                    "The input line '" + nextInputLine  + 
//						                    "' in '" + fileName + 
//						                    "' specifies a lag (=" +
//						                    intMarkovLag +
//						                    ") for variable '" + intNodeID +
//						                    "' that is larger than the max. Markov Lag (=" 
//						                    + maxMarkovLag + ")." );
//				                }
				            }
				            else {
		
								int intFoundParentCount;
										
						        if ( dbnItemList.length > 0 ) {
		
						            try {
						                
						                // Following the lag is the parent count
										strParentCount = dbnItemList[ 0 ];
										intParentCount = Integer.parseInt( strParentCount );
										
										// treat the last item differently
										if ( lagListindex == dbnLagList.length-1 ) {
										
										    intFoundParentCount = dbnItemList.length - 1;
										}
										else {
										    
										    intFoundParentCount = dbnItemList.length - 2;
										}
										
										if ( intParentCount > intFoundParentCount ) {
										    
										    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
								                    "The input line '" + nextInputLine  + 
								                    "' in '" + _fileName + 
								                    "' specifies a parent count of " +
								                    intParentCount +
								                    " for variable '" + intNodeID +
								                    "', but provides only " + 
								                    intFoundParentCount +
								                    " parent IDs." );
										}
										
										if ( intParentCount < intFoundParentCount ) {
										    
										    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
								                    "The input line '" + nextInputLine  + 
								                    "' in '" + _fileName + 
								                    "' specifies a parent count of " +
								                    intParentCount +
								                    " for variable '" + intNodeID +
								                    "', but provides " + 
								                    intFoundParentCount +
								                    " parent IDs." );
										}
										
										if ( intParentCount > maxParentCount ) {
										    
										    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
								                    "The input line '" + nextInputLine  + 
								                    "' in '" + _fileName + 
								                    "' specifies a parent count (=" +
								                    intParentCount + ") " +
								                    "for variable (=" + intNodeID +
								                    ") that exceeds the max. parent count (=" +
								                    maxParentCount + ")." );
										}
						            }
								    catch ( BanjoException e ) {
						                
						                throw new BanjoException( e );
								    }
								    catch ( Exception e ) {
						                
						                throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
							                    "The input line '" + nextInputLine  + 
							                    "' in '" + _fileName + "' does not " +
					                    		"specify a parent count (=" + strParentCount +
					                    		") as integer." );
								    }
		
								    // Now process the individual parents:
						            try {
						                
										for ( int intParentID=1; 
												intParentID <= intParentCount; intParentID++ ) {
										    
										    int nextParentID;
										    
										    try {
										    
										        nextParentID = Integer.parseInt( 
										                dbnItemList[intParentID] );
										    }
										    catch ( Exception e ) {
								                
								                throw new BanjoException( 
								                        BANJO.ERROR_BANJO_USERINPUT,
									                    "The input line '" + nextInputLine  +
									                    "' in '" + _fileName + "' does not " +
							                    		"specify a parent ID (=" + 
							                    		dbnItemList[intParentID] +
							                    		") as integer." );
										        
										    }
										    
										    if ( nextParentID >= varCount ) {
										        
										        throw new BanjoException( 
										                BANJO.ERROR_BANJO_USERINPUT,
								                    "The input line '" + nextInputLine  + 
								                    "' in '" + _fileName +
						                    		"' specifies a parent (index=" +
						                    		nextParentID + ", i.e. parent " +
						                    		( nextParentID + 1 ) + ") " +
						                    		"that exceeds the variable count (=" +
						                    		varCount + ")." );
										    }
										    try {
											    
										        // This is where we enforce the range of the lag index
										        if ( intMarkovLag >= minMarkovLag && 
										                intMarkovLag <= maxMarkovLag  ) {
										        
										            this.setEntry( 
										                intNodeID, nextParentID, intMarkovLag );
										        }
										    }
										    catch ( BanjoException e ) {
		
											    // If we encounter a BanjoException here, then 
										        // the setEntry call tried to set a value that
										        // had already been set.
											    throw new BanjoException( 
										            BANJO.ERROR_BANJO_USERINPUT,
										            "The input line '" + nextInputLine  + 
								                    "' in '" + _fileName + "' contains " +
										            "parent information for variable '" + 
										            intNodeID + "' that has already been " +
										            "recorded previously. " +
										            "Please check the structure file '" 
										            + _fileName + "' for " +
										            "duplicate parent info for variable '" +
										            intNodeID + "'." );
										    }
										}
						        	}
									catch ( BanjoException e ) {
										
									    throw new BanjoException( e );
									}
									catch ( Exception e ) {
			
									    // If any of the expected values turns out not to be an
									    // integer, the user will have to correct it.
									    throw new BanjoException( 
									        BANJO.ERROR_BANJO_USERINPUT,
						                    "The input line '" + nextInputLine  + 
						                    "' in '" + _fileName + "' does not " +
				                    		"specify a variable and its parent info " +
				                    		"in the expected format: all entries " +
				                    		"are expected to be (non-negative) integers." );
									}
						            
									if ( lagListindex != dbnLagList.length-1 ) {
									
									    // Get the new lag: process the number indicating the lag
							            try {
		
								            intMarkovLag = Integer.parseInt( 
								                    dbnItemList[ dbnItemList.length - 1 ] );
							            }
							            catch ( Exception e ) {
							                
							                throw new BanjoException( 
							                    BANJO.ERROR_BANJO_USERINPUT,
							                    "The input line '" + nextInputLine  + 
							                    "' in '" + _fileName + "' does not " +
					                    		"specify the first lag index as integer." );
							            }

							            // Again, we don't enforce the range of the lag index here
							            // Validate the lag:
//						                if ( intMarkovLag < minMarkovLag ) {
//		
//										    throw new BanjoException( 
//										        BANJO.ERROR_BANJO_USERINPUT,
//							                    "The input line '" + nextInputLine  + 
//							                    "' in '" + fileName + 
//							                    "' specifies a lag (=" +
//							                    intMarkovLag +
//							                    ") for variable '" + intNodeID +
//							                    "' that is smaller than the min. Markov Lag (=" 
//							                    + minMarkovLag + ")." );
//						                }
//						                if ( intMarkovLag > maxMarkovLag ) {
//		
//										    throw new BanjoException( 
//									            BANJO.ERROR_BANJO_USERINPUT,
//							                    "The input line '" + nextInputLine  + 
//							                    "' in '" + fileName + 
//							                    "' specifies a lag (=" +
//							                    intMarkovLag +
//							                    ") for variable '" + intNodeID +
//							                    "' that is larger than the max. Markov Lag (=" 
//							                    + maxMarkovLag + ")." );
//						                }
									}
						        }
						        else {
						            
						            // We are miising data: there should be at least the
						            // variable index and the parent count
						            throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
						                    "The text line '" + nextInputLine  + 
						                    "' in '" + _fileName + "' does not " +
				                    		"contain enough information to specify " +
				                    		"a variable and its parent info." );
						        }
							}
						}
					}

					nextInputLine = bufferedReader.readLine();
				}
			}
			else {			    
				
				// Example of a static network structure to load:
			    //
				//20
				// 0   2 0 7          
				// 1   1 1            
				// 2   3 0 1 2        
				// 3   2 2 3 
				// ...
				// 19  1 18         
				//

				int intNodeID = -1;
				int intParentCount;
				final int lag = 0;
								
				String nextInputLine = "";
				String nextTextlineTrimmed = "";
				Pattern staticBayesnetPattern = Pattern.compile( 
		                BANJO.PATTERN_STATICBAYESNET );
				
				while ( nextInputLine != null ) {

				    nextTextlineTrimmed = nextInputLine.trim();
				    
					// First split off any comments (Pattern.COMMENTS doesn't
					// do it for plain whitespace separators)
					if ( nextTextlineTrimmed.indexOf( BANJO.DEFAULT_COMMENTINDICATOR ) >= 0 ) {
					    
					    nextTextlineTrimmed = nextInputLine.substring( 0 , 
					            nextInputLine.indexOf( BANJO.DEFAULT_COMMENTINDICATOR )).trim();
					}

					// Ignore any blank line
					if ( !nextTextlineTrimmed.equals( "" ) ) {
						
						// Now split the remaining text into pieces
				        String[] parentIDlist = staticBayesnetPattern.split( 
				                nextTextlineTrimmed );
				        
				        if ( parentIDlist.length > 1 ) {
				            
				            try {
				                
								intNodeID = Integer.parseInt( parentIDlist[ 0 ] );

					            
					            // Make sure that the same variable isn't specified twice
					            if ( varAlreadyLoaded[ intNodeID ] ) {
					                
					                throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
						                    "The input line '" + nextInputLine  + 
						                    "' in '" + _fileName + "' is a repeat entry " +
				                    		"for variable '" + intNodeID + 
				                    		"'." );
					            }
					            else {
					                
					                varAlreadyLoaded[ intNodeID ] = true;
					            }
					            
								intParentCount = Integer.parseInt( parentIDlist[ 1 ] );
								
								if ( intParentCount != parentIDlist.length-2 ) {
								    
								    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
						                    "The input line '" + nextInputLine  + 
						                    "' in '" + _fileName + 
						                    "' specifies a parent count of " +
						                    intParentCount +
						                    " for variable '" + intNodeID +
						                    "', but provides " + (parentIDlist.length-2) +
						                    " parent IDs." );
								}
								
								if ( intParentCount > maxParentCount ) {
								    
								    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
						                    "The input line '" + nextInputLine  + 
						                    "' in '" + _fileName + 
						                    "' specifies a parent count (=" +
						                    intParentCount + ") " +
						                    "for variable (=" + intNodeID +
						                    ") that exceeds the max. parent count (=" +
						                    maxParentCount + ")." );
								}
								
								for ( int intParentID=parentIDlist.length; 
										intParentID>2; intParentID-- ) {
								    
								    int nextParentID;
								    nextParentID = Integer.parseInt( 
								            parentIDlist[intParentID-1] );
								    
								    if ( nextParentID >= varCount ) {
								        
								        throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
						                    "The input line '" + nextInputLine  + 
						                    "' in '" + _fileName +
				                    		"' specifies a parent (index=" +
				                    		nextParentID + ", i.e. parent " +
				                    		( nextParentID + 1 ) + ") " +
				                    		"that exceeds the variable count (=" +
				                    		varCount + ")." );
								    }
								    try {
								    
								        this.setEntry( intNodeID, nextParentID, lag );
								    }
								    catch ( BanjoException e ) {

									    // If we encounter a BanjoException here, then 
								        // the setEntry call tried to set a value that 
								        // had already been set.
									    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
									            "The input line '" + nextInputLine  + 
							                    "' in '" + _fileName + "' contains " +
									            "parent information for variable '" 
							                    + intNodeID +
									            "' that has already been recorded previously. " +
									            "Please check the structure file '" 
									            + _fileName +
									            "' for duplicate parent info for variable '" +
									            intNodeID + "'." );
								    }
								}
				            }
							catch ( BanjoException e ) {
								
							    throw new BanjoException( e );
							}
							catch ( Exception e ) {
	
							    // If any of the expected values turns out not to be an
							    // integer, the user will have to correct it.
							    throw new BanjoException( 
						            BANJO.ERROR_BANJO_USERINPUT,
				                    "The text line '" + nextInputLine  + 
				                    "' in '" + _fileName + "' does not " +
		                    		"specify a variable and its parent info " +
		                    		"in the expected format: all entries " +
		                    		"are expected to be (non-negative) integers." );
							}
				            
				        }
				        else {
				            
				            // We are miising data: there should be at least the
				            // variable index and the parent count
				            throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
			                    "The text line '" + nextInputLine  + 
			                    "' in '" + _fileName + "' does not " +
	                    		"contain enough information to specify " +
	                    		"a variable and its parent info." );
				        }
					}					

					nextInputLine = bufferedReader.readLine();
				}
			}
		}
		catch (IOException e) {
			   
				throw new BanjoException( e, 
				        BANJO.ERROR_BANJO_DEV, 
				        "\n  (EdgesAsMatrixWithCachedStatistics.loadFromFile) - " +
				        "Encountered an I/O error while loading the structure file '" +
				        _fileName + "' from directory '" + _directory + "'." );
		}		
		catch ( BanjoException e ) {

		    	// "forward" any BanjoException
		        throw new BanjoException( e );
		}		
		catch ( Exception e ) {

			throw new BanjoException( e, 
			        BANJO.ERROR_BANJO_DEV,
			        "\n(EdgesAsMatrixWithCachedStatistics.loadFromFile) - " +
			        "Encountered an error while loading the structure file '" +
			        _fileName + "' from directory '" + _directory + "'.");
		}	
	}
	
	// This method can be used in the automatic validation of a series of results 
	// such as the collection of (n-best) networks.
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#reconstructMatrix(java.lang.String, int, int, int)
     */
	public void reconstructMatrix( 
	        final String _bayesNetStructureString,
	        final int _varCount, 
	        final int _minMarkovLag, 
	        final int _maxMarkovLag ) 
					throws BanjoException {
    
		String checkStructureString = "";
	    try {
	
	        // The lag=0 part is identical to the reconstructMatrixStatic method
		    if ( _maxMarkovLag == 0 ) {
		        
		        reconstructMatrixStatic( _bayesNetStructureString, 
		                _varCount, _minMarkovLag, _maxMarkovLag );
		    }
		    else {
		        
			    int readVarCount;
			    int currentNodeID;
				int parentNodeID;
			    int markovLag;
			    String strMarkovLag = "";
				int intParentCount;
		
				StringTokenizer strTok = 
			        new StringTokenizer( _bayesNetStructureString );
				
				// The first line of data contains the variable count
				readVarCount = Integer.parseInt( strTok.nextToken() );
				
				// Double-check against the parameter for the variable count
				if ( _varCount != readVarCount ) {
				    
				    throw new BanjoException( BANJO.ERROR_BANJO_DEV,
				            "(EdgesAsMatrixWithCachedStatistics.reconstructMatrix) " +
				            "Development issue: " +
				            "The read-in var count doesn't match the internal parameter.");
				}
	
				// Set up the matrix and the parentCounts (need to have values assigned)
				for (int i=0; i<readVarCount; i++ ) {
					for (int j=0; j<readVarCount; j++ ) {
						for (int k=_minMarkovLag; k<_maxMarkovLag+1; k++ ) {
				
						    matrix[i][j][k] = 0;
							parentCount[i] = 0;
						}
					}
				}
				// and set the grand total
				combinedParentCount = 0;
				
				checkStructureString = "\nChecking Structure: \n" + readVarCount;
				
				// Recall the structure form (example):
				//20
				// 0   0:   0                 1:   2 0 7          
				// 1   0:   0                 1:   1 1            
				// 2   0:   0                 1:   3 0 1 2        
				// 3   0:   0                 1:   2 2 3   
				// ...
				// 19  0:	0                 1:   1 18
				for (int i=0; i<readVarCount; i++ ) {
				    
				    currentNodeID = Integer.parseInt( strTok.nextToken() );
				    checkStructureString += "\n" + currentNodeID + " ";
			    
				    for (int k=0; k<_maxMarkovLag+1; k++) {
					    
					    strMarkovLag = strTok.nextToken();
					    // Split off the ":" that is part of the lag (=MarkovLag)
					    strMarkovLag = strMarkovLag.substring( 
					            0, strMarkovLag.length()-1 ); 
					    markovLag = Integer.parseInt( strMarkovLag );

                        // 8/1/06 hjs
                        // Adjust for the more flexible representations that may omit
                        // the (irrelevant) lag info for all lags < minMarkovLag
                        if ( markovLag > k ) {
                            
                            while ( k < markovLag ) {
                                
                                k++;
                            }
                        }
                        
					    checkStructureString += "  " + markovLag + ": ";
					    
					    // Get the parent count for this lag
					    intParentCount = Integer.parseInt( strTok.nextToken() );
					    checkStructureString += intParentCount + " ";
				        
					    for (int j=0; j<intParentCount; j++) {
				        
					        parentNodeID = Integer.parseInt( strTok.nextToken() );
						    checkStructureString += parentNodeID + " ";
						    
						    matrix[currentNodeID][parentNodeID][markovLag] = 1;
						    parentCount[currentNodeID] ++;
							combinedParentCount++;
					    }
				    }   
				}
		    }
		}
		catch (BanjoException e) {
		    
		    // "forward" any BanjoException
		    throw new BanjoException( e );
		}
		catch (Exception e) {
	
		    System.out.println( "Reconstructed BayesNetStructure:\n" + 
		            checkStructureString );
		    
		    e.printStackTrace();
		    
			throw new BanjoException( e, 
			        BANJO.ERROR_BANJO_DEV, 
			        "(EdgesAsMatrixWithCachedStatistics.reconstructMatrix) -- \n  " +
			        "Development issue: " + 
			        "could not create parent matrix from bayesNetManager structure\n" + 
			        _bayesNetStructureString );
		}		
	}

	// For static bayesNets only:
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#reconstructMatrixStatic(java.lang.String, int, int, int)
     */
	public void reconstructMatrixStatic( 
	        final String _bayesNetStructureString, 
	        final int _varCount, 
	        final int _minMarkovLag, 
	        final int _maxMarkovLag ) 
					throws BanjoException {
	    
		String checkStructureString = "";
	    try {

	        //// Static BN only (lag=0)
		    if ( _maxMarkovLag == 0 ) {
			    
			    int readVarCount;
			    int currentNodeID;
				int parentNodeID;
				int intParentCount;
		
		        
				StringTokenizer strTok = 
			        new StringTokenizer( _bayesNetStructureString );
				
				readVarCount = Integer.parseInt( strTok.nextToken() );
				
				// Double-check the read-in variable count
				if ( _varCount != readVarCount ) {
				    
				    throw new BanjoException( BANJO.ERROR_BANJO_DEV,
				            "(EdgesAsMatrixWithCachedStatistics.reconstructMatrixStatic) " +
				            "Development issue: " + 
				            "The read-in var count doesn't match the internal parameter.");
				}
				
				for (int i=0; i<readVarCount; i++ ) {
					for (int j=0; j<readVarCount; j++ ) {
				
					    matrix[i][j][0] = 0;
						parentCount[i] = 0;
					}
				}
				// and set the grand total
				combinedParentCount = 0;
				
				checkStructureString = "\nChecking Structure: \n" + readVarCount;
				
				for (int i=0; i<readVarCount; i++ ) {
				    
				    currentNodeID = Integer.parseInt( strTok.nextToken() );
				    checkStructureString += "\n" + currentNodeID + " ";
				    
				    intParentCount = Integer.parseInt( strTok.nextToken() );
				    checkStructureString += intParentCount + " ";
				    
				    for (int j=0; j<intParentCount; j++) {
				        
				        parentNodeID = Integer.parseInt( strTok.nextToken() );
					    checkStructureString += parentNodeID + " ";
					    
					    matrix[currentNodeID][parentNodeID][0] = 1;
					    parentCount[currentNodeID] ++;
						combinedParentCount++;
				    }   
				}
		    }
		    else {

		        // Developer message only:
		        throw new BanjoException( BANJO.ERROR_BANJO_DEV, 
		                "(EdgesAsMatrixWithCachedStatistics.reconstructMatrixStatic) " +
		                "Development issue: " +
		                "this method cannot be called for max. Markov lag > 0.");
		    }
		}
		catch (BanjoException e) {
		    
		    throw new BanjoException( e );
		}
		catch (Exception e) {
	
		    System.out.println( "Reconstructed BayesNetStructure:\n" + 
		            checkStructureString );
		    e.printStackTrace();
			throw new BanjoException( e, 
			        BANJO.ERROR_BANJO_DEV, 
			        "(EdgesAsMatrixWithCachedStatistics.reconstructMatrixStatic) " +
	                "Development issue: " +
			        "could not create parent matrix from bayesNetManager structure " +
			        "as provided:\n '" +
			        _bayesNetStructureString + "'" );
		}		
	}
	
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#setToCombinedMatrices(edu.duke.cs.banjo.bayesnet.EdgesAsMatrixWithCachedStatistics, edu.duke.cs.banjo.bayesnet.EdgesAsMatrixWithCachedStatistics)
     */
	public void setToCombinedMatrices( 
	        EdgesWithCachedStatisticsI _matrix1, 
	        EdgesWithCachedStatisticsI _matrix2 )
			throws Exception {
        
        EdgesAsMatrixWithCachedStatistics matrix1;
        EdgesAsMatrixWithCachedStatistics matrix2;
        if ( _matrix1 instanceof EdgesAsMatrixWithCachedStatistics && 
                _matrix2 instanceof EdgesAsMatrixWithCachedStatistics ) {

            matrix1 = ( EdgesAsMatrixWithCachedStatistics ) _matrix1;
            matrix2 = ( EdgesAsMatrixWithCachedStatistics ) _matrix2;
            
    		int combinedEntry;
    		
    	    combinedParentCount = 0;
    	    int[][][] tmpMatrix1 = matrix1.getMatrix();
    	    int[][][] tmpMatrix2 = matrix2.getMatrix();
    	    
    	    for (int i=0; i<varCount; i++) {
    
    			parentCount[i] = 0;
    			
    			// Set the matrix entries
    			for (int j=0; j<varCount; j++) {
    				for (int k=minMarkovLag; k<maxMarkovLag+1; k++) {
    					
    					combinedEntry = tmpMatrix1[i][j][k] + 
    							tmpMatrix2[i][j][k];
    					
    					if ( combinedEntry >= 1 ) {
    
    						matrix[i][j][k] = 1;						    
    					    parentCount[i]++;
    					    combinedParentCount++;
    					}
    					else {
    					    
    					    matrix[i][j][k] = 0;
    					}
    				}
    			}
    		}
        }
        else {
            
            // dev error: we should never end up here
            throw new BanjoException( 
                    BANJO.ERROR_BANJO_DEV, 
                    "(" + StringUtil.getClassName( this ) + ".setToCombinedMatrices) " +
                    "Development issue: Arguments are of unexpected type (" +
                    "need: 'EdgesAsMatrixWithCachedStatistics')" );
        }
	}
	
	// Check if a matrix overlaps any entry with another matrix (typically used
	// to verify that the "mustBePresent" and "mustBeAbsent" matrices are
	// properly specified by the user)
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#hasOverlap(edu.duke.cs.banjo.bayesnet.EdgesAsMatrixWithCachedStatistics)
     */
	public boolean hasOverlap( final EdgesWithCachedStatisticsI _matrix ) throws Exception {

        EdgesAsMatrixWithCachedStatistics matrixWithStats;
        
        if ( _matrix instanceof EdgesAsMatrixWithCachedStatistics ) {

            matrixWithStats = ( EdgesAsMatrixWithCachedStatistics ) _matrix;
            
    	    boolean hasOverlap = false;
    
    		int[][][] tmpMatrix = matrixWithStats.getMatrix();
    		
    		for (int i=0; i<varCount; i++) {
    			for (int j=0; j<varCount; j++) {
    				for (int k=minMarkovLag; k<maxMarkovLag+1; k++) {
    
    				    if ( this.matrix[i][j][k] + tmpMatrix[i][j][k] == 2 ) { 
    				        
    				        hasOverlap = true;
    				        break;
    				    }				    
    				}
    			}
    		}
            
    	    return hasOverlap;
        }
        else {
            
            // dev error: we should never end up here
            throw new BanjoException( 
                    BANJO.ERROR_BANJO_DEV, 
                    "(" + StringUtil.getClassName( this ) + ".hasOverlap) " +
                    "Development issue: Arguments are of unexpected type (" +
                    "need: 'EdgesAsMatrixWithCachedStatistics')" );
        }
	}
	
	// This is a special method that computes a "complementary" adjacency
	// matrix (relative to both supplied arguments)
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#computeComplementaryMatrix(edu.duke.cs.banjo.bayesnet.EdgesAsMatrixWithCachedStatistics, edu.duke.cs.banjo.bayesnet.EdgesAsMatrixWithCachedStatistics)
     */
	public void computeComplementaryMatrix(
	        final EdgesWithCachedStatisticsI _matrix1,
			final EdgesWithCachedStatisticsI _matrix2 ) 
			throws Exception {

        EdgesAsMatrixWithCachedStatistics matrix1;
        EdgesAsMatrixWithCachedStatistics matrix2;
        
        if ( _matrix1 instanceof EdgesAsMatrixWithCachedStatistics && 
                _matrix2 instanceof EdgesAsMatrixWithCachedStatistics ) {

            matrix1 = ( EdgesAsMatrixWithCachedStatistics ) _matrix1;
            matrix2 = ( EdgesAsMatrixWithCachedStatistics ) _matrix2;
        
    		this.combinedParentCount = 0;
    		for (int i=0; i<varCount; i++) {
    		    
    		    // Set all parentCounts to 0
    			this.parentCount[i] = 0;
    
    			int[][][] tmpMatrix1 = matrix1.getMatrix();
    			int[][][] tmpMatrix2 = matrix2.getMatrix();
    		    
    			for (int j=0; j<varCount; j++) {
    				for (int k=minMarkovLag; k<maxMarkovLag+1; k++) {
    					
    				    // Check the value of:
    				    // 1 - matrix1.matrix[i][j][k] - matrix2.matrix[i][j][k]
    				    if ( tmpMatrix1[i][j][k] == 1 || 
    				            tmpMatrix2[i][j][k] == 1 ) {
    					    
    					    this.matrix[i][j][k] = 0;
    					}
    					else {
    
    						this.matrix[i][j][k] = 1;
    					    this.parentCount[i]++;
    					    this.combinedParentCount++;
    					}
    				}
    			}
    		}
        }
        else {
            
            // dev error: we should never end up here
            throw new BanjoException( 
                    BANJO.ERROR_BANJO_DEV, 
                    "(" + StringUtil.getClassName( this ) + ".setToCombinedMatrices) " +
                    "Development issue: Arguments are of unexpected type (" +
                    "need: 'EdgesAsMatrixWithCachedStatistics')" );
        }
	}
		
	// Computes the list of parent ID's for the current node "nodeID"
	// "Overload" for DBN's: Note that the second argument is actually redundant,
	// but comes in handy as a visual reminder that we are in the DBN case
	// (the parent lists are only computed for "current" nodes (i.e. the lag is
	// always 0)
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#getCurrentParentIDlist(int, int)
     */
	public int[][] getCurrentParentIDlist( final int _nodeID, final int _lag ) {
		
		int[][] parentIDlist;
		int intParentCount = parentCount[_nodeID];
		parentIDlist = new int[intParentCount][2];
		
		int parentIndex = 0;
		
		// Go through the parent nodes for the node with id=nodeID
		for (int i=0; i<varCount && parentIndex < intParentCount; i++) {
			for (int k=minMarkovLag; k<maxMarkovLag+1; k++) {
				
				if ( matrix[_nodeID][i][k] == 1 ) {
				    
				    // Per convention, [index][0] holds the ID of a node
					parentIDlist[parentIndex][0] = i;
					
					// and [index][1] holds the lag
					parentIDlist[parentIndex][1] = k;
					parentIndex++;
				}
			}
		}
		
		return parentIDlist;
	}
		
	// Get the number of parents for the variable with given index (var ID)
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#parentCount(int)
     */
	public int parentCount(final int _varIndex) {
		return parentCount[_varIndex];
	}
	
	// Query if a variable has another variable as a parent
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#isParent(int, int, int)
     */
	public boolean isParent(
	        final int _varIndex, 
	        final int _parentVarIndex, 
	        final int _lag ) {
	    
		return (matrix[_varIndex][_parentVarIndex][_lag] == 1);
	}

	// Set any individual entry in the parent matrix to 1
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#setEntry(int, int, int)
     */
	public void setEntry(
	        final int _varIndex, 
			final int _parentVarIndex, 
			final int _lag ) throws Exception {
				
		// need to check if value is already 1 (this should not be
		// the case, but better to be safe)
		if ( matrix[_varIndex][_parentVarIndex][_lag] == 0 ){
		    
			parentCount[_varIndex] ++;
			combinedParentCount++;
		}
		else { 
						
		    // This case should never occur in the flow of the appplication,
		    // so throw an error if we ever end up here:
		    throw new BanjoException(BANJO.ERROR_BANJO_DEV, 
		            "(EdgesAsMatrixWithCachedStatistics.setEntry) " +
		            "Development issue: Expected a parent value of 0, but found m[" +
		            _varIndex + "][" + _parentVarIndex + "][" + _lag + "] = " +
		            matrix[_varIndex][_parentVarIndex][_lag] + "." );
		}
		
		// Now make the parent assignment
		matrix[_varIndex][_parentVarIndex][_lag] = 1;
	}

	// Set an individual entry in the parent matrix
	// This is a convenient method when merging sets of parent nodes. 
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#setEntry(int, int, int, int)
     */
	public void setEntry(
	        final int _varIndex, 
			final int _parentVarIndex, 
			final int _lag, 
			final int _newValue ) throws Exception {
		
		// It is the calling routine's task to assure that we only get 0 or 1
		// as values! Otherwise we complain by throwing an Exception
		
		// Update the adjaceny matrix
		int oldValue = matrix[_varIndex][_parentVarIndex][_lag];
		matrix[_varIndex][_parentVarIndex][_lag] = _newValue;
		
		// Update the parentCount only when the value changes
		if ( _newValue == 1 && oldValue == 0 ) {
		    
			parentCount[_varIndex] ++;
			combinedParentCount++;
		}
		else if ( _newValue == 0 && oldValue == 1 ) {
			
			parentCount[_varIndex] --;
			combinedParentCount--;
		}
		// Note: this case can happen, e.g., in the initializing of a matrix (assigning
		// a 0 value to a value that is already 0), but we assert here that the calling
		// routine takes care of this!!
		else {

		    throw new BanjoException(BANJO.ERROR_BANJO_DEV, 
		            "(EdgesAsMatrixWithCachedStatistics.setEntry) " +
		            "Development issue: The old and the new value of m[" +
		            _varIndex + "][" + _parentVarIndex + "][" + _lag + "] = " +
		            matrix[_varIndex][_parentVarIndex][_lag] + 
		            "; this should never happen, " +
		            "and needs to be corrected by the developer!" );
		}			
	}
	
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#addParent(int, int, int)
     */
	public void addParent(
	        final int _varIndex, 
			final int _parentVarIndex,
			final int _lag ) throws Exception {
		 
	    // Note that we throw an exception if the current value of the matrix indicates
	    // that there is already a parent
		
		if ( matrix[_varIndex][_parentVarIndex][_lag] == 0 ) {
		    
			matrix[_varIndex][_parentVarIndex][_lag] = 1;
			parentCount[_varIndex] ++;
			combinedParentCount++;
		}
		else {
			
		    // this should never occur:
		    throw new BanjoException(BANJO.ERROR_BANJO_DEV, 
                    "(" + StringUtil.getClassName( this ) + ".AddParent) " +
					"Development issue: Error in adding parent: \n" +
					"Parent id=" + _parentVarIndex + ", lag=" + _lag +
					" is already a parent of " +
					"id=" + _varIndex );
		}
	}
	
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#deleteParent(int, int, int)
     */
	public void deleteParent(
	        final int _varIndex, 
			final int _parentVarIndex, 
			final int _lag ) throws Exception {
					    
		if ( matrix[_varIndex][_parentVarIndex][_lag] == 1 ) {
		    
			matrix[_varIndex][_parentVarIndex][_lag] = 0;
			parentCount[_varIndex] --;
			combinedParentCount--;
		}
		else {
			
		    // this should never occur:
		    throw new BanjoException(BANJO.ERROR_BANJO_DEV, 
		            "(EdgesAsMatrixWithCachedStatistics.deleteParent) " +
					"Development issue: Error in deleting parent: \n" +
					"Node id=" + _varIndex + " does not have parent " +
					"id=" + _parentVarIndex + ", lag=" + _lag );
		}
	}
		
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#reverseRelation(int, int, int, int)
     */
	public void reverseRelation(
            final int _varIndex, 
            final int _lagVar, 
			final int _parentVarIndex, 
            final int _lagParent ) throws Exception {

		// Check that the lags are the same:
		// (they have to be in the same time slice for this to make sense)
		
		if ( _lagVar != _lagParent && ( _lagVar != 0 || _lagParent != 0 ) ) {
		    
		    throw new BanjoException(
		            BANJO.ERROR_BANJO_DEV,
		            "(EdgesAsMatrixWithCachedStatistics.reverseRelation) " +
		            "Development issue: " +
		            "Can only reverse an edge between nodes of lag 0.");
		}
		
		// Remove the parent for the specified node
		if (( matrix[_varIndex][_parentVarIndex][_lagParent] == 0 ) 
			|| ( matrix[_parentVarIndex][_varIndex][_lagVar] == 1 )) {

		    // throw an exception: the data is not consistent with 
			// the requested operation
		    throw new BanjoException(
	            BANJO.ERROR_BANJO_DEV,
	            "(EdgesAsMatrixWithCachedStatistics.reverseRelation) " +
	            "Development issue: " +
	            "Encountered inconsistent data when trying to reverse an edge.");
		}
		    
	    // First remove the <edge from the node to the parent>
		matrix[_varIndex][_parentVarIndex][_lagParent] = 0;
		parentCount[_varIndex] --;
		
		// then add the <edge from the parent to the node>
		matrix[_parentVarIndex][_varIndex][_lagVar] = 1;
		parentCount[_parentVarIndex] ++;
		
		// Note: of course we don't have to update the grand total in this case
	}
    
    // Setting all round and num values (related to Shmueli's cycle checking method) to 0
    public void resetRoundNum() throws Exception {

        // throw exception since we don't have support for Shmueli's method in this class
        throw new BanjoException( BANJO.ERROR_BANJO_DEV,
               "(EdgesAsMatrixWithCachedStatistics.resetRoundNum) " +
               "Development issue: this method is not supported." );
    }
    
    public void resetEntireGraph() throws Exception {

        // throw exception since we don't have support for Shmueli's method in this class
        throw new BanjoException( BANJO.ERROR_BANJO_DEV,
               "(EdgesAsMatrixWithCachedStatistics.resetDFS) " +
               "Development issue: this method is not supported." );
    }
    
    public void adjustForLocalChange( final int _nodeToCheck ) throws Exception {

        // throw exception since we don't have support for Shmueli's method in this class
        throw new BanjoException( BANJO.ERROR_BANJO_DEV,
               "(EdgesAsMatrixWithCachedStatistics.adjustDFS) " +
               "Development issue: this method is not supported." );
    }
	
	// Check if a node set represents a cyclic graph via a depth-first search:
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#isCyclicDFS(edu.duke.cs.banjo.bayesnet.BayesNetChangeI)
     */
	public boolean isCyclic(final BayesNetChangeI bayesNetChange) 
		throws Exception {
		
		int nodeToCheck = -1;
		int parentNodeToCheck = -1;
		int currentNodeLevel;
		int[] previousNodeLevel = new int[ varCount ];
		int[] lastNodeProcessed = new int[ varCount ];
		boolean[] _ancestorSet =  new boolean[ varCount ];
		
		try {
			
			// We always check for a cycle (only) for the node that has an edge added
			// (as described in the bayesNetChange)
			if ( bayesNetChange.getChangeType() == 
			    	BANJO.CHANGETYPE_ADDITION ) {
			    
				nodeToCheck = bayesNetChange.getCurrentNodeID();
				parentNodeToCheck = bayesNetChange.getParentNodeID();
			}
			else if ( bayesNetChange.getChangeType() == 
			    BANJO.CHANGETYPE_REVERSAL ) {
			    
				nodeToCheck = bayesNetChange.getParentNodeID();
				parentNodeToCheck = bayesNetChange.getCurrentNodeID();
			}
			else {
			    
			    // this should never occur:
			    throw new BanjoException(BANJO.ERROR_BANJO_DEV,
			            "(EdgesAsMatrixWithCachedStatistics.isCyclicDFS) " +
			            "Development issue: " +
			            "encountered non-addition/non-reversal BayesNetChange " +
			            "in cycle checking.");
			}
			
			int currentEntry;
			
			int nextParentToProcess;
			
			// Indicate that we haven't processed any nodes yet
			for ( int i=0; i<varCount; i++ ) lastNodeProcessed[i] = -1;
			// This sets the starting point to the node that had a parent added
			currentNodeLevel = nodeToCheck;
			_ancestorSet[ nodeToCheck ] =  false;
			// Trivial start modification
			lastNodeProcessed[ nodeToCheck ] = varCount; //parentNodeToCheck-1;
			currentNodeLevel = parentNodeToCheck;
			previousNodeLevel[ currentNodeLevel ] = nodeToCheck;
			
			// Check until either a cycle is found or all parents of the nodeToCheckID
			// (and its parents, etc) have been checked
			while ( !_ancestorSet[ nodeToCheck ] && 
			        !( currentNodeLevel == nodeToCheck ) ) {

			    // Go to the next potential parent
			    nextParentToProcess = lastNodeProcessed[ currentNodeLevel ];
			    nextParentToProcess++;
		        
			    while ( nextParentToProcess == varCount && 
			            currentNodeLevel != nodeToCheck ) {
			        
			        // We are done with all the parents at the current level, so
			        // go back a level			        
			        currentNodeLevel = previousNodeLevel[ currentNodeLevel ];
				    nextParentToProcess = lastNodeProcessed[ currentNodeLevel ];
				    nextParentToProcess++;
			    }
			    
			    if ( currentNodeLevel != nodeToCheck ) {
			        
				    currentEntry = matrix[ currentNodeLevel  ][ nextParentToProcess ][0];
				    lastNodeProcessed[ currentNodeLevel ] = nextParentToProcess;
				    
				    // If we find a parent at parentNodeToProcess, we
				    // process it right away
				    if ( currentEntry == 1 ) {
				        
				        // Add the found parent to the ancestor set
				        _ancestorSet[ nextParentToProcess ] = true;
				        
				        // Point the current stack level to the previous one (the node
				        // that had the parent that we are currently processing)
				        previousNodeLevel[ nextParentToProcess ] = currentNodeLevel;
				        currentNodeLevel = nextParentToProcess;
					    nextParentToProcess = lastNodeProcessed[ currentNodeLevel ];
					    nextParentToProcess++;
					    while ( nextParentToProcess == varCount ) {
					        
					        // Need to go back a level, since we are done with all the
					        // parents at the current level				        
					        currentNodeLevel = previousNodeLevel[ currentNodeLevel ];
						    nextParentToProcess = lastNodeProcessed[ currentNodeLevel ];
						    nextParentToProcess++;
					    }
				    }
			    }
			}
		}
		catch ( OutOfMemoryError e ) {
			
			System.out.print( "(EdgesAsMatrixWithCachedStatistics.isCyclicDFS) " +
					"Out of memory.");
			
			// Also: would need to check in the calling code for this return value
			e.printStackTrace();
            
            throw new BanjoException( BANJO.ERROR_BANJO_OUTOFMEMORY,
                    "Out of memory in (" +
                    StringUtil.getClassName(this) +
                    ".isCyclicDFS)" );
		}		
		catch (BanjoException e) {
		    
		    // "forward" any BanjoException
		    throw new BanjoException( e );
		}		
		catch (Exception e) {

			throw new BanjoException( e, 
			        BANJO.ERROR_BANJO_DEV,
			        "\n(EdgesAsMatrixWithCachedStatistics.isCyclicDFS) - " +
			        "Development issue: Encountered an error " +
			        "while checking for cycles based on bayesNetChange='" +
			        bayesNetChange.toString() + "'.");
		}

		return _ancestorSet[ nodeToCheck ];
	} // end isCyclicDFS
	
	// Check if a node set represents a cyclic graph via a breadth-first search:
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#isCyclicBFS(edu.duke.cs.banjo.bayesnet.BayesNetChangeI)
     */
    // Note: isCyclicBFS is not used by any calling code, because DFS is generally faster. Expect the 
    // method to get deprecated in an upcoming maintenance release.
	public boolean isCyclicBFS(
	        final BayesNetChangeI bayesNetChange) 
			throws Exception {
		
		boolean cycleFound = false;
		final int nodeToCheckID;
		int nextParentNodeToCheckID;
		
		try {
			
			// We always check for a cycle (only) for the node that has an edge added
			// (as described in the bayesNetChange)
			if ( bayesNetChange.getChangeType() 
					== BANJO.CHANGETYPE_ADDITION ) {
			    
				nodeToCheckID = bayesNetChange.getCurrentNodeID();
				nextParentNodeToCheckID = bayesNetChange.getParentNodeID();
			}
			else if ( bayesNetChange.getChangeType() == 
			    BANJO.CHANGETYPE_REVERSAL ) {
			    
				nodeToCheckID = bayesNetChange.getParentNodeID();
				nextParentNodeToCheckID = bayesNetChange.getCurrentNodeID();
			}
			else {
			    
			    // this should never occur:
			    throw new BanjoException(BANJO.ERROR_BANJO_DEV, 
			            "(EdgesAsMatrixWithCachedStatistics.isCyclicBFS) " +
			            "Development issue: " +
			            "encountered non-addition/reversal BayesNetChange " +
			            "in cycle checking.");
			}
			
			int currentEntry;
			
			
			// Initialize the ancestor set to the parent set
			for (int j=0; j<varCount; j++) {
				
				// Ancestors only care about entries with lag 0
				currentEntry = matrix[nodeToCheckID][j][0];
				ancestorSet[j] = currentEntry;
				nodesToVisit[j] = currentEntry;
				nodesVisited[j] = 0;
			}
							
			nextParentNodeToCheckID = firstParent( nodesToVisit );
					
			// Now visit each parent, and their parents, ...
			while ( !cycleFound && nextParentNodeToCheckID != -1 ) {
				
	
				nodesToVisit[nextParentNodeToCheckID] = 0;
				
				// Process this node if it hasn't been visited yet
				if (nodesVisited[nextParentNodeToCheckID] == 0) {
					
					// Add all parents of this node to the ancestor set
					for (int j=0; j<varCount; j++) {
						
						currentEntry = matrix[nextParentNodeToCheckID][j][0];
						if (nodesVisited[j] == 0 && currentEntry == 1){
							
							// We found a new node that we still need to visit
							nodesToVisit[j] = currentEntry;
							ancestorSet[j] = 1; 
						}
					}
					
					// Mark the node as visited
					nodesVisited[nextParentNodeToCheckID] = 1;
					
					// check if a cycle has been completed
					if (ancestorSet[nodeToCheckID] == 1) {
					    
					    cycleFound = true;
					}
				}
						
				// Move to the next parent
				nextParentNodeToCheckID = firstParent( nodesToVisit );
			}			
		}
		catch (OutOfMemoryError e ) {
		    
			System.out.print( "(EdgesAsMatrixWithCachedStatistics.isCyclicBFS) " +
					"Out of memory.");
			
			// Also: would need to check in the calling code for this return value
			e.printStackTrace();
		}
		
		return cycleFound;
	} // end isCyclicBFS
		
	// Check an entire parentMatrix (e.g., the mandatory parents set)
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#isCyclicBFS()
     */
	public boolean isCyclic() {
		
		// Note that we assume that the dimension of the nodeSetToCheck
		// is based on the same varCount that defines the underlying bayesnet
		//int varCount = this.getVarCount();
		boolean cycleFound = false;
		
		//resetCycleAtNode();  // Replaced for performance
		cycleAtNode = -1;
		
		// Check for each node that it is not contained in a cycle
		int i = 0;
		while (i<varCount && !cycleFound) {
			
			for (int j=0; j<varCount; j++) {
				
				parentIDs[j] = matrix[i][j][0];
			}
			cycleFound = isCyclic(i, parentIDs);
			i++;
		}
		
		return cycleFound;
	}

	// Check if a node set represents a cyclic graph, starting from the node with
	// ID=nodeID, and with a set of parents "parentNodeIDs":
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#isCyclic(int, int[])
     */
	public boolean isCyclic(
	        final int nodeID, 
	        final int[] parentNodeIDs) {
			
		boolean cycleFound = false;
		
		int parentNodeID;
		int currentEntry;
				
		
		// Initialize the ancestor set to the parent set
		for (int j=0; j<varCount; j++) {
			
			// Ancestors only care about entries with lag 0
			currentEntry = parentNodeIDs[j];
			ancestorSet[j] = currentEntry;
			nodesToVisit[j] = currentEntry;
			nodesVisited[j] = 0;
		}
				
		parentNodeID = firstParent( nodesToVisit );
	
		// Now visit each parent, and their parents, ...
		while ( !cycleFound && parentNodeID != -1 ) {
			
			nodesToVisit[parentNodeID] = 0;
			
			// Process this node if it hasn't been visited yet
			if (nodesVisited[parentNodeID] == 0) {
				
				// Add all parents of this node to the ancestor set
				for (int j=0; j<varCount; j++) {
					
					//currentEntry = this.getEntry(parentNodeID, j, 0);
					currentEntry = matrix[parentNodeID][j][0];
					if (nodesVisited[j] == 0 && currentEntry == 1){
												
						nodesToVisit[j] = currentEntry;
						ancestorSet[j] = 1; 
					}
				}
				
				// Mark the node as visited
				nodesVisited[parentNodeID] = 1;
				
				// check if a cycle has been completed
				if (ancestorSet[nodeID] == 1) {
					
					cycleFound = true;
					
					//this.setCycleAtNode(nodeID);  // Replaced for performance
					cycleAtNode = nodeID;
				}
			}
					
			// Move to the next parent
			parentNodeID = firstParent( nodesToVisit );
		}			
			
		return cycleFound;
	}
	
	private int firstParent( final int[] _nodesToVisit ){
		// Return the first parent in the list of nodes
		
		int firstParentID=-1;
		int i = 0;
		while (firstParentID == -1 && i < _nodesToVisit.length ) {
		    
			if (_nodesToVisit[i] == 1) firstParentID = i;
			i++;
		}

		return firstParentID;
	}

    public boolean hasIdenticalEntries( final EdgesI _otherMatrix ){

        if ( _otherMatrix instanceof EdgesAsMatrix ) {
            
            // Define a matrix as performance shortcut:
            int[][][] tmpMatrix = (( EdgesAsMatrix ) _otherMatrix ).getMatrix();
            
            if ( ( (EdgesAsMatrixWithCachedStatistics) _otherMatrix).getCombinedParentCount() !=
                this.getCombinedParentCount() ) {
                
                return false;
            }
            
            for (int i=0; i<varCount; i++) {
                
                if ( ( (EdgesAsMatrixWithCachedStatistics) _otherMatrix).parentCount[i] !=
                    this.parentCount[i] ) {
                    
                    return false;
                }
            }
            
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
	
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#omitNodesAsOwnParents()
     */
	public void omitNodesAsOwnParents() {
		
		// Using the fact that a node cannot be a parent to itself,
		// this method sets the (i,i) entries of a matrix to 1 (i.e.,
		// this is used primarily for the mustBeAbsentParents set)
		for (int i=0; i< varCount; i++) {

			matrix[i][i][0] = 1;
		}
	}
	
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#omitExcludedParents()
     */
	public void omitExcludedParents() {
		
		// Based on the selected Markov lag paramters, nodes with lag less
	    // than the minMarkovLag cannot be selected as parents (so we add them
	    // to the mustBeAbsentParents set)
		for (int i=0; i< varCount; i++) {
		    // Note that we only exclude nodes up to lag (minMarkovLag-1)
			for (int j=0; j<varCount; j++) {
				for (int k=0; k<minMarkovLag; k++) {

					matrix[i][j][k] = 1;
				}
			}
		}
	}

	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#getParentCount()
     */
	public int[] getParentCount() {
		return parentCount;
	}
	
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#getParentCount(int)
     */
	public int getParentCount(int nodeID) {
		return parentCount[nodeID];
	}
	
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#getCycleAtNode()
     */
	public int getCycleAtNode() {
		return cycleAtNode;
	}

	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#setCycleAtNode(int)
     */
	public void setCycleAtNode( final int cycleAtNode ) {
		this.cycleAtNode = cycleAtNode;
	}
	
	// This method is not used anymore for performance reasons
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#resetCycleAtNode()
     */
	public void resetCycleAtNode() {
		// Use the convention that only node IDs from 0 to varCount
		// are valid values.
		this.cycleAtNode = -1;
	}

	// For testing/tracing only:
	// Print the entire matrix as a string, formatted with various delimiters,
	// and the parentCount at the end.
	// (Can't move this method to superclass, because parentIDs are native to 
	// EdgesAsMatrixWithCachedStatistics)
	/* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#toStringWithIDandParentCount()
     */
	public StringBuffer toStringWithIDandParentCount() {
		
		StringBuffer localStringBuffer = new StringBuffer( 
		        BANJO.BUFFERLENGTH_STRUCTURE );
		localStringBuffer.append( "    " );
		// Add header: (need to make formatting more generic, but ok for quick test 
		for (int i=0; i<varCount; i++) {
			
			localStringBuffer.append( i + "  " ); 
			if (i<10) localStringBuffer.append( ' ' ); 
		}
		localStringBuffer.append( '\n' );
		for (int i=0; i<varCount; i++) {
			
			localStringBuffer.append( i );
			localStringBuffer.append( ": " );
			if (i<10) localStringBuffer.append( ' ' );
			
			for (int j=0; j<varCount; j++) {
				for (int k=0; k<maxMarkovLag+1; k++) {
					
					localStringBuffer.append( matrix[i][j][k] );
					// Add delimiter between (parent) entries
					localStringBuffer.append( "   " );					
				}
				
				// Add delimiter between lags
				localStringBuffer.append( '\t' );
			}
			
			// Add delimiter between variables
			localStringBuffer.append( "  , pc=" );
			localStringBuffer.append( parentCount[i] );
			localStringBuffer.append( '\n' );
		}
		return localStringBuffer;
	}
	
    /* (non-Javadoc)
     * @see edu.duke.cs.banjo.bayesnet.EdgesWithCachedStatisticsI#getCombinedParentCount()
     */
    public int getCombinedParentCount() {
        return combinedParentCount;
    }
}
