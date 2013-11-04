/*
 * Created on Jan 9, 2006
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

import edu.duke.cs.banjo.bayesnet.*;
import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.utility.*;

import java.util.*;

/**
 * Provides basic equivalence checking between networks.
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Jan 9, 2006
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class EquivalenceCheckerBasic extends EquivalenceChecker {

    // Details on the checking method
    protected long mismatchInParentCount = 0; 
    protected long mismatchInSkeleton = 0; 
    protected long mismatchInVStructures = 0; 
    protected long matchDueToIdentity = 0;
    protected long matchAfterCompleteCheck = 0;

    protected final int dimVariables;
    protected final int dimParents;
    protected final int dimLags;
    protected final int offsetVariables;
    protected final int offsetParents;
    protected final int offsetLags;
    
    public EquivalenceCheckerBasic( final Settings _processData ) throws Exception {
	
        super( _processData );

        if ( BANJO.CONFIG_PARENTSETS.equalsIgnoreCase( BANJO.UI_PARENTSETSASARRAYS ) ) {
            
            // Set up support for the EdgesAsMatrixCompactVariablesFirst class
            dimVariables = varCount;
            dimParents = varCount;
            dimLags = maxMarkovLag - minMarkovLag + 1;
            
            // Parents are listed consecutively within each lag:
            offsetParents = 1;
            // The index "offset" between consecutive lags:
            offsetLags = dimParents;
            // The index "offset" between consecutive variables:
            offsetVariables = dimParents * dimLags;
        }
        else if ( BANJO.CONFIG_PARENTSETS.equalsIgnoreCase( BANJO.UI_PARENTSETSASMATRICES ) ) {

            // The dummy values will cause errors, should subsequent unsupported code
            // be executed
            dimVariables = 0;
            dimParents = 0;
            dimLags = 0;
            
            // Parents are listed consecutively within each lag:
            offsetParents = 0;
            // The index "offset" between consecutive lags:
            offsetLags = 0;
            // The index "offset" between consecutive variables:
            offsetVariables = 0;
        }
        else {
            
            throw new BanjoException( 
                    BANJO.ERROR_BANJO_DEV,
                    "(EquivalenceCheckerBasic constructor) " +
                    "Development issue: There is no code for handling the supplied type " +
                    "of parent sets." );
            
//            // The dummy values will cause errors, should subsequent unsupported code
//            // be executed
//            dimVariables = 0;
//            dimParents = 0;
//            dimLags = 0;
//            
//            // Parents are listed consecutively within each lag:
//            offsetParents = 0;
//            // The index "offset" between consecutive lags:
//            offsetLags = 0;
//            // The index "offset" between consecutive variables:
//            offsetVariables = 0;
        }
    }
    
    // Compare a given structure to an entire set of structures (e.g., in the set
    // of tracking the n-best structures)
    // Note: we can apply this method to a given set of structures, but it won't make
    // much sense to apply it within a search (instead, we only need to compare 2 
    // structures for equivalence when their scores match)
    public boolean isEquivalent( 
            final Set _setOfBayesNetStructures, 
            final BayesNetStructureI _bayesNetStructureToCheck ) throws Exception {

        boolean isEquiv = false;
        int i = 0;
        
        // If the reference set if empty, then there's nothing to do, and we make a quick exit
        if ( _setOfBayesNetStructures.size() < 1 ) return false;
        
        // Set up a reference structure for iterating through the reference set
        BayesNetStructureI referenceBayesNetStructure = null;
        
        // iterate through the members of the set of bayesNetStructures
        Iterator structureIterator = _setOfBayesNetStructures.iterator();
		        
        // We return "is equivalent" as soon as we find a single "equivalent" match,
        // otherwise we have to keep comparing
		while ( !isEquiv && structureIterator.hasNext() ) {
		    
		    referenceBayesNetStructure = ( BayesNetStructureI ) structureIterator.next();

	        // Check each structure against the bayesNetStructureToCheck
	        isEquiv = isEquivalent( referenceBayesNetStructure, _bayesNetStructureToCheck );
            i++;
		}
        
        if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
        
            System.out.println( "'" + isEquiv + "'" + 
                " - Set size: " + _setOfBayesNetStructures.size() +
                " - Equivalence checks: " + i );
        }
        
        // Return true if the structureToCheck is equivalent to any structure in the
        // reference set
        return isEquiv;
    }
    
    // Compare two structures
    protected boolean isEquivalent( 
            final BayesNetStructureI _referenceBayesNetStructure,
            final BayesNetStructureI _bayesNetStructureToCheck ) throws Exception {

        boolean isEquiv = false;
        
        try {
            
            if ( _referenceBayesNetStructure.getNetworkStructure() instanceof EdgesAsMatrix ) {

                EdgesAsMatrix referenceStructureAsMatrix = ( EdgesAsMatrix )
                        _referenceBayesNetStructure.getNetworkStructure();
                
                EdgesAsMatrix toCheckStructureAsMatrix = ( EdgesAsMatrix )
                        _bayesNetStructureToCheck.getNetworkStructure();
                
    	        EdgesWithCachedStatisticsI referenceMatrix =
    	            new EdgesAsMatrixWithCachedStatistics( referenceStructureAsMatrix );
                
                EdgesWithCachedStatisticsI matrixToCheck =
                    new EdgesAsMatrixWithCachedStatistics( toCheckStructureAsMatrix );
    	        
    	        isEquiv = isEquivalent( referenceMatrix, matrixToCheck );
            }
            else if ( _referenceBayesNetStructure.getNetworkStructure() 
                    instanceof EdgesAsArray ) {

                EdgesAsArray referenceStructureAsArray = ( EdgesAsArray )
                        _referenceBayesNetStructure.getNetworkStructure();
                
                EdgesAsArray toCheckStructureAsArray = ( EdgesAsArray )
                        _bayesNetStructureToCheck.getNetworkStructure();
                
                // hjs 2/26/2008 (v2.2) Add processData to parameter list (not really necessary
                // here, but heads off potential problems down the road)
                EdgesWithCachedStatisticsI referenceArray =
                    new EdgesAsArrayWithCachedStatistics( referenceStructureAsArray, processData );
                
                EdgesWithCachedStatisticsI arrayToCheck =
                    new EdgesAsArrayWithCachedStatistics( toCheckStructureAsArray, processData );
                
                isEquiv = isEquivalent( referenceArray, arrayToCheck );
            }
            else {
                
                // indicate a problem: Developer needs to be made aware
                throw new BanjoException( BANJO.ERROR_BANJO_DEV, 
                        "(EquivalenceCheckerBasic.isEquivalent) " +
                        "Developer issue: BayesNetStructureI arguments are of a type (" +
                        StringUtil.getClassName( 
                                _referenceBayesNetStructure.getNetworkStructure() ) +
                        ") " + "that is not implemented, " +
                        "so that the equivalence check could not be performed." );
            }
        }
        catch ( BanjoException e ) {
            
            // indicate a problem: Developer needs to be made aware
            throw new BanjoException( e );
        }
        catch ( Exception e ) {
            
            // indicate a problem: Developer needs to be made aware
            throw new BanjoException( BANJO.ERROR_BANJO_DEV, 
                    "(EquivalenceCheckerBasic.isEquivalent) " +
                    "Developer issue? Main 'isEquivalent' method " +
                    "with BayesNetStructureI arguments " +
                    "(class=" +
                    StringUtil.getClassName( 
                            _referenceBayesNetStructure.getNetworkStructure() ) +
                    ")" + " encountered a serious error " +
                    "that caused the search to be aborted." );
        }

        return isEquiv;
    }
    
    // Compare two adjacency matrices
    protected boolean isEquivalent( 
            final EdgesAsMatrixWithCachedStatistics _referenceStructure,
            final EdgesAsMatrixWithCachedStatistics _structureToCheck ) throws Exception {

        EdgesAsMatrixWithCachedStatistics referenceStructure;
        EdgesAsMatrixWithCachedStatistics structureToCheck;
        
        if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
            
            System.out.println( "\n-- 'Checking equivalence for 'EdgesAsMatrixWithCachedStatistics'." );
        }

        boolean isEquiv = false;

        referenceStructure = ( EdgesAsMatrixWithCachedStatistics ) _referenceStructure;
        structureToCheck = ( EdgesAsMatrixWithCachedStatistics ) _structureToCheck;

        equivalenceCheckCount++;

        // Only need EdgesAsMatrix
        EdgesAsMatrix skeletonForReferenceStructure =  new EdgesAsMatrix( 
                varCount, minMarkovLag, maxMarkovLag );
        EdgesAsMatrix skeletonForStructureToCheck =  new EdgesAsMatrix( 
                varCount, minMarkovLag, maxMarkovLag );


        // ---------------------------------------------------------
        // 1. Check the (total) parent count
        // ---------------------------------------------------------
                
        if ( referenceStructure.getCombinedParentCount() 
                != structureToCheck.getCombinedParentCount() ) {
            
            if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
          
                System.out.println( "\n-- 'Total parent counts' don't match." );
            }
            mismatchInParentCount++;
            return false;
        }

        if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
        
            System.out.println( "\n** 'Total parent counts' match." );
        }


        // ---------------------------------------------------------
        // 1b. Check the matrices for identity
        // ---------------------------------------------------------
    
        if ( referenceStructure.hasIdenticalEntries( structureToCheck ) ) {
            
            if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
          
                System.out.println( "\n-- " +
                        "'Both matrices are identical, hence equivalent." );
            }
            matchDueToIdentity++;
            return true;
        }
        else {
            
            if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {

                System.out.println( "\n-- 'Both matrices are NOT identical." );
            }
        }
        

        // For the remainder of this check, assume equivalence
        isEquiv = true;
        
        // ---------------------------------------------------------
        // 2. Check the skeletons
        // ---------------------------------------------------------
        
        // First create the skeletons
        
        // This computes the skeletons (for display)
        for ( int i=0; i< varCount; i++ ) {
            for ( int j=i+1; j<varCount; j++ ) {
                for ( int k=minMarkovLag; k<maxMarkovLag+1; k++ ) {
                
                    skeletonForReferenceStructure.matrix[i][j][k] = 
                        referenceStructure.matrix[i][j][k] + 
                        referenceStructure.matrix[j][i][k];
                }
            }
        }
        for ( int i=0; i< varCount; i++ ) {
            for ( int j=i+1; j<varCount; j++ ) {
                for ( int k=minMarkovLag; k<maxMarkovLag+1; k++ ) {
                
                    skeletonForStructureToCheck.matrix[i][j][k] = 
                        structureToCheck.matrix[i][j][k] + 
                        structureToCheck.matrix[j][i][k];
                }
            }
        }       
        
        // This stops as soon as a mismatch is found:
        for ( int i=0; i< varCount; i++ ) {
            for ( int j=i+1; j<varCount; j++ ) {
                for ( int k=minMarkovLag; k<maxMarkovLag+1; k++ ) {
                
                    // This effectively compares the entries for the two skeletons
                    if ( referenceStructure.matrix[i][j][k] 
                         + referenceStructure.matrix[j][i][k] 
                         - structureToCheck.matrix[i][j][k] 
                         - structureToCheck.matrix[j][i][k] != 0 ) {
                        
                        if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
                        
                            System.out.println( "Skeleton mismatch found at triple (" + 
                                    i + ", " + j + ", " + k + ")" );
                        }
                        
                        mismatchInSkeleton++;
                        isEquiv = false;
                        
                        // Comment out these lines to see all mismatches
                        // between the two skeletons
                        i = varCount;
                        j = varCount;
                        k = maxMarkovLag + 1;
                    }
                }
            }
        }

        
        if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {

            System.out.println( "\nReference matrix skeleton:" );
            System.out.println( skeletonForReferenceStructure.toString() );
            System.out.println( "\nCheck matrix skeleton:" );
            System.out.println( skeletonForStructureToCheck.toString() );
        }

        if ( !isEquiv ) {
            
            if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {

                System.out.println( "-- 'Skeletons' don't match." );
            }
            return false;
        }
        
        if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
        
            System.out.println( "** 'Skeletons' match." );
        }
        
        // ----------------------------------------------------------
        // 3. Check the v-structures (see, e.g., Chickering's papers)
        // ----------------------------------------------------------
        
        if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {

            System.out.println( "\nChecking 'v-structures':\n" );
        }
        
        // get the parent lists, node by node
        int[][] parentIDlistForReferenceStructure;
        int[][] parentIDlistForStructureToCheck;
        int parentCountRefStruc;
        int parentCountStrucToCheck;
        // 2 parents (that make up a v-structure), each with 2 entries (index and lag):
        int[][] parentsForPotentialVStructure = new int[2][2];
        
        for ( int i=0; i< varCount; i++ ) {

            parentIDlistForReferenceStructure =
                referenceStructure.getCurrentParentIDlist( i, 0 );
            parentIDlistForStructureToCheck =
                structureToCheck.getCurrentParentIDlist( i, 0 );
            
            parentCountRefStruc = parentIDlistForReferenceStructure.length;
            // Only nodes that have more than 1 parent can have a v-structure
            if ( parentCountRefStruc > 1 ) {
                
                if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {

                    System.out.println( "RefStructure: node i=" + i + " has " + 
                            parentCountRefStruc + " parents:" );
                }
                
                // Now process each pair of parents as a (potential) v-structure
                for ( int j=0; j<parentCountRefStruc; j++ ) {

                    parentsForPotentialVStructure[0][0]= 
                        parentIDlistForReferenceStructure[j][0];
                    parentsForPotentialVStructure[0][1]= 
                        parentIDlistForReferenceStructure[j][1];
                        
                    for ( int k=j+1; k<parentCountRefStruc; k++ ){
 
                        parentsForPotentialVStructure[1][0]= 
                            parentIDlistForReferenceStructure[k][0];
                        parentsForPotentialVStructure[1][1]= 
                            parentIDlistForReferenceStructure[k][1];
                        
                        // The v-structure is described by the triple (i,j,k)
                        
                        // if the triple can be completed (i.e., the skeleton for
                        // referenceStructure has an entry 1 for i,j,k), then the given
                        // triple is not a v-structure:
                        if ( referenceStructure.matrix[ parentsForPotentialVStructure[1][0] ]
                               [ parentsForPotentialVStructure[0][0] ]
                               [ parentsForPotentialVStructure[0][1] ] 
                             + referenceStructure.matrix[ parentsForPotentialVStructure[0][0] ]
                               [ parentsForPotentialVStructure[1][0] ]
                               [ parentsForPotentialVStructure[1][1] ] == 1 ) {
                            
                            if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
                            
                                // triple is complete
                                System.out.println( "  Triple (" + 
                                    parentsForPotentialVStructure[0][0] + ","  + i + "," + 
                                    parentsForPotentialVStructure[1][0] + ") can be completed " +
                                            "(i.e., is not a v-structure)." );
                            }
                        }
                        else {
                         
                            // Need to check that the structureToCheck has the exact same 
                            // v-structure
                            // (Note: this structure cannot be "complete", or else we would have
                            // different skeletons)!
                            if ( structureToCheck.matrix[i]
                                   [ parentsForPotentialVStructure[0][0] ]
                                   [ parentsForPotentialVStructure[0][1] ] == 1 && 
                                 structureToCheck.matrix[i]
                                   [ parentsForPotentialVStructure[1][0] ]
                                   [ parentsForPotentialVStructure[1][1] ] == 1 ) {
                                
                                if ( 
                                     structureToCheck.matrix[ parentsForPotentialVStructure[1][0] ]
                                       [ parentsForPotentialVStructure[0][0] ]
                                       [ parentsForPotentialVStructure[0][1] ] == 0 && 
                                     structureToCheck.matrix[ parentsForPotentialVStructure[0][0] ]
                                       [ parentsForPotentialVStructure[1][0] ]
                                       [ parentsForPotentialVStructure[1][1] ] == 0 ) {

                                    if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
                                    
                                        System.out.println( "+ Triple (" + 
                                                parentsForPotentialVStructure[0][0] + ","  + i + "," + 
                                                parentsForPotentialVStructure[1][0] + ") is shared " +
                                                        "between both structures." );
                                    }
                                }
                                else {
                                    
                                    if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
                                    
                                        System.out.println( "- Triple (" + 
                                            parentsForPotentialVStructure[0][0] + ","  + i + "," + 
                                            parentsForPotentialVStructure[1][0] + ") is NOT shared " +
                                                    "between both structures " +
                                                    "(can be completed in check structure)." );
                                    }
                                    

                                    isEquiv = false;
                                    mismatchInVStructures++;

                                    return false;
                                }
                            }
                            else {
                                
                                if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
                                
                                    // found a difference in v-structures:
                                    System.out.println( "- Found difference at triple (" 
                                            + parentsForPotentialVStructure[0][0]
                                            + "," + i + "," 
                                            + parentsForPotentialVStructure[1][0] + ')' );
                                }
                                
                                // for now, simply set the flag, so we can see 
                                // all intermediate results
                                isEquiv = false;
                                mismatchInVStructures++;

                                return false;
                            }
                        }
                    }
                }
            }
            
            // -----------------------------------------------------------
            // Symmetrical check for the v-structures for structureToCheck
            // -----------------------------------------------------------
            parentCountStrucToCheck = parentIDlistForStructureToCheck.length;
            if ( parentCountStrucToCheck > 1 ) {
                
                if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {

                    System.out.println( "\nStructureToCheck: node i=" + i + " has " + 
                            parentCountStrucToCheck + " parents:" );
                }
                
                // Now process each pair of parents as a (potential) v-structure
                for ( int j=0; j<parentCountStrucToCheck; j++ ) {

                    parentsForPotentialVStructure[0][0]= 
                        parentIDlistForStructureToCheck[j][0];
                    parentsForPotentialVStructure[0][1]= 
                        parentIDlistForStructureToCheck[j][1];
                        
                    for ( int k=j+1; k<parentCountStrucToCheck; k++ ){
 
                        parentsForPotentialVStructure[1][0]= 
                            parentIDlistForStructureToCheck[k][0];
                        parentsForPotentialVStructure[1][1]= 
                            parentIDlistForStructureToCheck[k][1];
                        
                        // The v-structure is described by the triple (i,j,k)
                        
                        // if the triple can be completed (i.e., the skeleton for
                        // referenceStructure has an entry 1 for i,j,k), then the given
                        // triple is not a v-structure:
                        if ( structureToCheck.matrix[ parentsForPotentialVStructure[1][0] ]
                               [ parentsForPotentialVStructure[0][0] ]
                               [ parentsForPotentialVStructure[0][1] ] 
                             + structureToCheck.matrix[ parentsForPotentialVStructure[0][0] ]
                               [ parentsForPotentialVStructure[1][0] ]
                               [ parentsForPotentialVStructure[1][1] ] == 1 ) {
                            
                            if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
                            
                                // triple is complete ("y and z are adjacent")
                                System.out.println( "  Triple (" +
                                    parentsForPotentialVStructure[0][0] + "," + i + "," + 
                                    parentsForPotentialVStructure[1][0] + ") can be completed " +
                                        "(i.e., is not a v-structure)." );
                            }
                        }
                        else {
                     
                           // 
                           if ( referenceStructure.matrix[i]
                                  [ parentsForPotentialVStructure[0][0] ]
                                  [ parentsForPotentialVStructure[0][1] ] == 1 && 
                                  referenceStructure.matrix[i]
                                  [ parentsForPotentialVStructure[1][0] ]
                                  [ parentsForPotentialVStructure[1][1] ] == 1 ) {
                                   
                               if ( referenceStructure.matrix[ 
                                        parentsForPotentialVStructure[1][0] ]
                                       [ parentsForPotentialVStructure[0][0] ]
                                       [ parentsForPotentialVStructure[0][1] ] == 0 && 
                                    referenceStructure.matrix[ 
                                        parentsForPotentialVStructure[0][0] ]
                                       [ parentsForPotentialVStructure[1][0] ]
                                       [ parentsForPotentialVStructure[1][1] ] == 0 ) {
    
                                    
                                    if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
                                    
                                        System.out.println( "+ Triple (" + 
                                                parentsForPotentialVStructure[0][0] + ","  + i + "," + 
                                                parentsForPotentialVStructure[1][0] + ") is shared " +
                                                        "between both structures." );
                                    }
                                }
                                else {
    
                                    if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
                                        
                                        System.out.println( "- Triple (" + 
                                            parentsForPotentialVStructure[0][0] + ","  + i + "," + 
                                            parentsForPotentialVStructure[1][0] + ") is NOT shared " +
                                                    "between both structures " +
                                                    "(can be completed in check structure)." );
                                    }
    
                                    isEquiv = false;
                                    mismatchInVStructures++;
                                    
                                    return false;
                                }
                           }
                           else {
                               
                               if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
                               
                                   // found a difference in v-structures:
                                   System.out.println( "- Found difference at triple (" 
                                           + parentsForPotentialVStructure[0][0]
                                           + "," + i + "," 
                                           + parentsForPotentialVStructure[1][0] + ')' );
                               }
                               
                               // for now, simply set the flag, so we can see 
                               // all intermediate results
                               isEquiv = false;
                               mismatchInVStructures++;

                               return false;
                            }
                        }
                    }
                }
            }
        }
        
        if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
            
            if ( !isEquiv ) {

                System.out.println( "\n-- 'V-structures' don't match." );
            }
            else {
            
                System.out.println( "\n** 'V-structures' match." );
            }
        }
    
        matchAfterCompleteCheck++;
        
        return isEquiv;
    }
        
    // Compare two adjacency matrices, when we use the compact parent sets
    protected boolean isEquivalent( 
            EdgesAsArrayWithCachedStatistics _referenceStructure,
            EdgesAsArrayWithCachedStatistics _structureToCheck ) throws Exception {

        EdgesAsArrayWithCachedStatistics referenceStructure;
        EdgesAsArrayWithCachedStatistics structureToCheck;
        int mappedIndex;                // i*offsetVariables + k*offsetLags + j;
        int mappedIndexTranspose;       // j*offsetVariables + k*offsetLags + i;
        int mappedIndex2;
                
        if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
            
            System.out.println( "\n-- 'Checking equivalence for 'EdgesAsArrayWithCachedStatistics'." );
        }
        boolean isEquiv = false;

        referenceStructure = ( EdgesAsArrayWithCachedStatistics ) _referenceStructure;
        structureToCheck = ( EdgesAsArrayWithCachedStatistics ) _structureToCheck;
        
        equivalenceCheckCount++;

        EdgesAsArray skeletonForReferenceStructure =  new EdgesAsArray( 
                varCount, minMarkovLag, maxMarkovLag  );
        EdgesAsArray skeletonForStructureToCheck =  new EdgesAsArray( 
                varCount, minMarkovLag, maxMarkovLag  );
        
        // ---------------------------------------------------------
        // 1. Check the (total) parent count
        // ---------------------------------------------------------
                
        if ( referenceStructure.getCombinedParentCount() 
                != structureToCheck.getCombinedParentCount() ) {
            
            if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
          
                System.out.println( "\n-- 'Total parent counts' don't match." );
            }
            mismatchInParentCount++;
            return false;
        }

        if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
        
            System.out.println( "\n** 'Total parent counts' match." );
        }

        // ---------------------------------------------------------
        // 1b. Check the matrices for identity
        // ---------------------------------------------------------
    
        if ( referenceStructure.hasIdenticalEntries( structureToCheck ) ) {
            
            if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
          
                System.out.println( "\n-- " +
                        "'Both matrices are identical, hence equivalent." );
            }
            matchDueToIdentity++;
            return true;
        }
        else {
            
            if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {

                System.out.println( "\n-- 'Both matrices are NOT identical." );
            }
        }
        
        // For the remainder of this check, assume equivalence
        isEquiv = true;
        
        // ---------------------------------------------------------
        // 2. Check the skeletons
        // ---------------------------------------------------------
        
        // First create the skeletons
        
        // This computes the skeletons (for display)
        for ( int i=0; i< varCount; i++ ) {
            for ( int j=i+1; j<varCount; j++ ) {
                for ( int k=0; k<maxMarkovLag+1-minMarkovLag; k++ ) {
                
                    mappedIndex = i*offsetVariables + k*offsetLags + j;
                    mappedIndexTranspose = j*offsetVariables + k*offsetLags + i;
                    
                    skeletonForReferenceStructure.matrix[ mappedIndex ] =
                        referenceStructure.matrix[ mappedIndex ] + 
                        referenceStructure.matrix[ mappedIndexTranspose ];
                }
            }
        }
        for ( int i=0; i< varCount; i++ ) {
            for ( int j=i+1; j<varCount; j++ ) {
                for ( int k=0; k<maxMarkovLag+1-minMarkovLag; k++ ) {
                
                    mappedIndex = i*offsetVariables + k*offsetLags + j;
                    mappedIndexTranspose = j*offsetVariables + k*offsetLags + i;
                    
                    skeletonForStructureToCheck.matrix[ mappedIndex ] = 
                        structureToCheck.matrix[ mappedIndex ] + 
                        structureToCheck.matrix[ mappedIndexTranspose ];
                }
            }
        }       
        
        // This stops as soon as a mismatch is found:
        for ( int i=0; i< varCount; i++ ) {
            for ( int j=i+1; j<varCount; j++ ) {
                for ( int k=0; k<maxMarkovLag+1-minMarkovLag; k++ ) {
                
                    // This effectively compares the entries for the two skeletons
                    mappedIndex = i*offsetVariables + k*offsetLags + j;
                    mappedIndexTranspose = j*offsetVariables + k*offsetLags + i;
                    
                    if ( referenceStructure.matrix[ mappedIndex ] 
                             + referenceStructure.matrix[ mappedIndexTranspose ] 
                             - structureToCheck.matrix[ mappedIndex ] 
                             - structureToCheck.matrix[ mappedIndexTranspose ] != 0 ) {
                        
                        if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
                        
                            System.out.println( "Skeleton mismatch found at triple (" + 
                                    i + ", " + j + ", " + k + ")" );
                        }
                        
                        mismatchInSkeleton++;
                        isEquiv = false;
                        
                        // Comment out these lines to see all mismatches
                        // between the two skeletons
                        i = varCount;
                        j = varCount;
                        k = maxMarkovLag + 1;
                    }
                }
            }
        }
        
        if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {

            System.out.println( "\nReference matrix skeleton:" );
            System.out.println( skeletonForReferenceStructure.toString() );
            System.out.println( "\nCheck matrix skeleton:" );
            System.out.println( skeletonForStructureToCheck.toString() );
        }

        if ( !isEquiv ) {
            
            if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {

                System.out.println( "-- 'Skeletons' don't match." );
            }
            return false;
        }
        
        if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
        
            System.out.println( "** 'Skeletons' match." );
        }
        
        // ----------------------------------------------------------
        // 3. Check the v-structures (see, e.g., Chickering's papers)
        // ----------------------------------------------------------
        
        if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {

            System.out.println( "\nChecking 'v-structures':\n" );
        }
            
        // get the parent lists, node by node
        int[][] parentIDlistForReferenceStructure;
        int[][] parentIDlistForStructureToCheck;
        int parentCountRefStruc;
        int parentCountStrucToCheck;
        // 2 parents (that make up a v-structure), each with 2 entries (index and lag):
        int[][] parentsForPotentialVStructure = new int[2][2];
        
        for ( int i=0; i< varCount; i++ ) {

            parentIDlistForReferenceStructure =
                referenceStructure.getCurrentParentIDlist( i, 0 );
            parentIDlistForStructureToCheck =
                structureToCheck.getCurrentParentIDlist( i, 0 );

            parentCountRefStruc = parentIDlistForReferenceStructure.length;
            // Only nodes that have more than 1 parent can have a v-structure
            if ( parentCountRefStruc > 1 ) {
                
                if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {

                    System.out.println( "RefStructure: node i=" + i + " has " + 
                            parentCountRefStruc + " parents:" );
                }
                
                // Now process each pair of parents as a (potential) v-structure
                for ( int j=0; j<parentCountRefStruc; j++ ) {

                    parentsForPotentialVStructure[0][0]= 
                        parentIDlistForReferenceStructure[j][0];
                    parentsForPotentialVStructure[0][1]= 
                        parentIDlistForReferenceStructure[j][1];
                        
                    for ( int k=j+1; k<parentCountRefStruc; k++ ){
 
                        parentsForPotentialVStructure[1][0]= 
                            parentIDlistForReferenceStructure[k][0];
                        parentsForPotentialVStructure[1][1]= 
                            parentIDlistForReferenceStructure[k][1];
                        
                        // The v-structure is described by the triple (i,j,k)
                        
                        // if the triple can be completed (i.e., the skeleton for
                        // referenceStructure has an entry 1 for i,j,k), then the given
                        // triple is not a v-structure:
                        mappedIndex = parentsForPotentialVStructure[1][0]*offsetVariables 
                            + ( parentsForPotentialVStructure[0][1] - minMarkovLag )*offsetLags 
                            + parentsForPotentialVStructure[0][0];
                        mappedIndex2 = parentsForPotentialVStructure[0][0]*offsetVariables 
                            + ( parentsForPotentialVStructure[1][1] - minMarkovLag )*offsetLags 
                            + parentsForPotentialVStructure[1][0];
                        
                        if ( referenceStructure.matrix[ mappedIndex ] 
                             + referenceStructure.matrix[ mappedIndex2 ] == 1 ) {
                            
                            if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
                            
                                // triple is complete
                                System.out.println( "  Triple (" + 
                                    parentsForPotentialVStructure[0][0] + ","  + i + "," + 
                                    parentsForPotentialVStructure[1][0] + ") can be completed " +
                                            "(i.e., is not a v-structure)." );
                            }
                        }
                        else {
                         
                            // Need to check that the structureToCheck has the exact same 
                            // v-structure
                            // (Note: this structure cannot be "complete", or else we would have
                            // different skeletons)!
                            mappedIndex = i*offsetVariables 
                                + ( parentsForPotentialVStructure[0][1] - minMarkovLag )*offsetLags 
                                + parentsForPotentialVStructure[0][0];
                            mappedIndex2 = i*offsetVariables 
                                + ( parentsForPotentialVStructure[1][1] - minMarkovLag )*offsetLags 
                                + parentsForPotentialVStructure[1][0];
                            
                            if ( structureToCheck.matrix[ mappedIndex ] == 1 && 
                                   structureToCheck.matrix[ mappedIndex2 ] == 1 ) {

                                mappedIndex = parentsForPotentialVStructure[1][0]*offsetVariables 
                                    + ( parentsForPotentialVStructure[0][1] - minMarkovLag )*offsetLags 
                                    + parentsForPotentialVStructure[0][0];
                                mappedIndex2 = parentsForPotentialVStructure[0][0]*offsetVariables 
                                    + ( parentsForPotentialVStructure[1][1] - minMarkovLag )*offsetLags 
                                    + parentsForPotentialVStructure[1][0];
                                
                                if ( structureToCheck.matrix[ mappedIndex ] == 0 && 
                                      structureToCheck.matrix[ mappedIndex2 ] == 0 ) {

                                    if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
                                    
                                        System.out.println( "+ Triple (" + 
                                                parentsForPotentialVStructure[0][0] + ","  + i + "," + 
                                                parentsForPotentialVStructure[1][0] + ") is shared " +
                                                        "between both structures." );
                                    }
                                }
                                else {
                                    
                                    if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
                                    
                                        System.out.println( "- Triple (" + 
                                            parentsForPotentialVStructure[0][0] + ","  + i + "," + 
                                            parentsForPotentialVStructure[1][0] + ") is NOT shared " +
                                                    "between both structures " +
                                                    "(can be completed in check structure)." );
                                    }

                                    isEquiv = false;
                                    mismatchInVStructures++;

                                    return false;
                                }
                            }
                            else {
                                
                                if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
                                
                                    // found a difference in v-structures:
                                    System.out.println( "- Found difference at triple (" 
                                            + parentsForPotentialVStructure[0][0]
                                            + "," + i + "," 
                                            + parentsForPotentialVStructure[1][0] + ')' );
                                }
                                
                                // for now, simply set the flag, so we can see 
                                // all intermediate results
                                isEquiv = false;
                                mismatchInVStructures++;
                                
                                return false;
                            }
                        }
                    }
                }
            }
            
            // -----------------------------------------------------------
            // Symmetrical check for the v-structures for structureToCheck
            // -----------------------------------------------------------
            parentCountStrucToCheck = parentIDlistForStructureToCheck.length;
            if ( parentCountStrucToCheck > 1 ) {
                
                if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {

                    System.out.println( "\nStructureToCheck: node i=" + i + " has " + 
                            parentCountStrucToCheck + " parents:" );
                }
                
                // Now process each pair of parents as a (potential) v-structure
                for ( int j=0; j<parentCountStrucToCheck; j++ ) {

                    parentsForPotentialVStructure[0][0]= 
                        parentIDlistForStructureToCheck[j][0];
                    parentsForPotentialVStructure[0][1]= 
                        parentIDlistForStructureToCheck[j][1];
                        
                    for ( int k=j+1; k<parentCountStrucToCheck; k++ ){
 
                        parentsForPotentialVStructure[1][0]= 
                            parentIDlistForStructureToCheck[k][0];
                        parentsForPotentialVStructure[1][1]= 
                            parentIDlistForStructureToCheck[k][1];
                        
                        // The v-structure is described by the triple (i,j,k)
                        
                        // if the triple can be completed (i.e., the skeleton for
                        // referenceStructure has an entry 1 for i,j,k), then the given
                        // triple is not a v-structure:
                        mappedIndex = parentsForPotentialVStructure[1][0]*offsetVariables 
                            + ( parentsForPotentialVStructure[0][1] - minMarkovLag )*offsetLags 
                            + parentsForPotentialVStructure[0][0];
                        mappedIndex2 = parentsForPotentialVStructure[0][0]*offsetVariables 
                            + ( parentsForPotentialVStructure[1][1] - minMarkovLag )*offsetLags 
                            + parentsForPotentialVStructure[1][0];
                        
                        if ( structureToCheck.matrix[ mappedIndex ] 
                           + structureToCheck.matrix[ mappedIndex2 ] == 1 ) {
                            
                            if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
                            
                                // triple is complete ("y and z are adjacent")
                                System.out.println( "  Triple (" +
                                    parentsForPotentialVStructure[0][0] + "," + i + "," + 
                                    parentsForPotentialVStructure[1][0] + ") can be completed " +
                                        "(i.e., is not a v-structure)." );
                            }
                        }
                        else {
                     
                            mappedIndex = i*offsetVariables 
                                + parentsForPotentialVStructure[0][1]*offsetLags 
                                + parentsForPotentialVStructure[0][0];
                            mappedIndex2 = i*offsetVariables 
                                + parentsForPotentialVStructure[1][1]*offsetLags 
                                + parentsForPotentialVStructure[1][0];
                           
                            if ( referenceStructure.matrix[ mappedIndex ] == 1 && 
                                  referenceStructure.matrix[ mappedIndex ] == 1 ) {

                                mappedIndex = parentsForPotentialVStructure[1][0]*offsetVariables 
                                   + ( parentsForPotentialVStructure[0][1] - minMarkovLag )*offsetLags 
                                   + parentsForPotentialVStructure[0][0];
                                mappedIndex2 = parentsForPotentialVStructure[0][0]*offsetVariables 
                                   + ( parentsForPotentialVStructure[1][1] - minMarkovLag )*offsetLags 
                                   + parentsForPotentialVStructure[1][0];
                                
                                if ( referenceStructure.matrix[ mappedIndex ] == 0 && 
                                        referenceStructure.matrix[ mappedIndex2 ] == 0 ) {    
                                    
                                    if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
                                    
                                        System.out.println( "+ Triple (" + 
                                                parentsForPotentialVStructure[0][0] + ","  + i + "," + 
                                                parentsForPotentialVStructure[1][0] + ") is shared " +
                                                        "between both structures." );
                                    }
                                }
                                else {
    
                                    if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
                                        
                                        System.out.println( "- Triple (" + 
                                            parentsForPotentialVStructure[0][0] + ","  + i + "," + 
                                            parentsForPotentialVStructure[1][0] + ") is NOT shared " +
                                                    "between both structures " +
                                                    "(can be completed in check structure)." );
                                    }
    
                                    isEquiv = false;
                                    mismatchInVStructures++;
                                    
                                    return false;
                                }
                           }
                           else {

                               if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
                               
                                   // found a difference in v-structures:
                                   System.out.println( "- Found difference at triple (" 
                                           + parentsForPotentialVStructure[0][0]
                                           + "," + i + "," 
                                           + parentsForPotentialVStructure[1][0] + ')' );
                               }
                               
                               // for now, simply set the flag, so we can see 
                               // all intermediate results
                               isEquiv = false;
                               mismatchInVStructures++;
                               
                               return false;
                            }
                        }
                    }
                }
            }
        }
        
        if ( BANJO.DEBUG && BANJO.TRACE_EQUIVALENCECHECKER ) {
            
            if ( !isEquiv ) {

                System.out.println( "\n-- 'V-structures' don't match." );
            }
            else {
            
                System.out.println( "\n** 'V-structures' match." );
            }
        }
    
        matchAfterCompleteCheck++;
    
        return isEquiv;
    }
    
    // Compare two adjacency matrices
    protected boolean isEquivalent( 
            EdgesWithCachedStatisticsI _referenceStructure,
            EdgesWithCachedStatisticsI _structureToCheck ) throws Exception {

        boolean isEquiv = false;
        
        if ( _referenceStructure instanceof EdgesAsArrayWithCachedStatistics && 
                _structureToCheck instanceof EdgesAsArrayWithCachedStatistics ) {

            EdgesAsArrayWithCachedStatistics referenceStructure;
            EdgesAsArrayWithCachedStatistics structureToCheck;

            referenceStructure = ( EdgesAsArrayWithCachedStatistics ) _referenceStructure;
            structureToCheck = ( EdgesAsArrayWithCachedStatistics ) _structureToCheck;
            
            isEquiv = isEquivalent( referenceStructure, structureToCheck );
        }
        else if ( _referenceStructure instanceof EdgesAsMatrixWithCachedStatistics && 
                _structureToCheck instanceof EdgesAsMatrixWithCachedStatistics ) {

            EdgesAsMatrixWithCachedStatistics referenceStructure;
            EdgesAsMatrixWithCachedStatistics structureToCheck;

            referenceStructure = ( EdgesAsMatrixWithCachedStatistics ) _referenceStructure;
            structureToCheck = ( EdgesAsMatrixWithCachedStatistics ) _structureToCheck;
            
            isEquiv = isEquivalent( referenceStructure, structureToCheck );
        }
        else {
            
            // dev error: reminder to a developer only
            throw new BanjoException(
                    BANJO.ERROR_BANJO_DEV, 
                    "(EquivalenceCheckerBasic.isEquivalent) " +
                    "Development issue: the type of at least one of the objects, provided as '" +
                    StringUtil.getClassName( 
                            _referenceStructure ) +
                    "' or '" +
                    StringUtil.getClassName( 
                                _structureToCheck ) +
                    "', is not supported by the current code!");
        }
        
        return isEquiv;
    }

	public StringBuffer provideCollectedStatistics() throws Exception {
			
		StringBuffer collectedStats = 
			new StringBuffer( BANJO.BUFFERLENGTH_STAT );

		// Add the statistics
		if ( BANJO.CONFIG_DISPLAYSTATISTICS_EQUIVCHECK ) {
			
			collectedStats.append( BANJO.FEEDBACK_NEWLINE );
			collectedStats.append( "Statistics collected in equivalence checker '" + 
			        StringUtil.getClassName(this) + "':" );

			collectedStats.append( BANJO.FEEDBACK_NEWLINE );
			collectedStats.append( "  Number of equivalence checks: " );
			collectedStats.append( equivalenceCheckCount );
			collectedStats.append( BANJO.FEEDBACK_NEWLINE );
			collectedStats.append( "  Mismatches in parent counts: " );
			collectedStats.append( mismatchInParentCount );
			collectedStats.append( BANJO.FEEDBACK_NEWLINE );
			collectedStats.append( "  Mismatches in skeletons: " );
			collectedStats.append( mismatchInSkeleton );
			collectedStats.append( BANJO.FEEDBACK_NEWLINE );
			collectedStats.append( "  Mismatches in v-structures: " );
			collectedStats.append( mismatchInVStructures );
			collectedStats.append( BANJO.FEEDBACK_NEWLINE );
			collectedStats.append( "  Matches due to identity: " );
			collectedStats.append( matchDueToIdentity );
			collectedStats.append( BANJO.FEEDBACK_NEWLINE );
			collectedStats.append( "  Matches after complete check: " );
			collectedStats.append( matchAfterCompleteCheck );
			
//			if ( BANJO.DEBUG ) {
//
//				collectedStats.append( BANJO.FEEDBACK_NEWLINE );
//				collectedStats.append( "  Cross-check: " );
//				collectedStats.append( 
//                        mismatchInParentCount
//                        + mismatchInSkeleton
//				          + mismatchInVStructures
//                        + matchDueToIdentity
//				          + matchAfterCompleteCheck
//                        - equivalenceCheckCount );
//			}
		}
		
		return collectedStats;
	}
}
