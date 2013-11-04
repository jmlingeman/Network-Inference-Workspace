/*
 * Created on Jul 11, 2006
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

import java.util.HashSet;
import java.util.Set;

import edu.duke.cs.banjo.bayesnet.*;
import edu.duke.cs.banjo.data.observations.*;
import edu.duke.cs.banjo.data.settings.SettingItem;
import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.utility.*;

/**
 * Computes the BDe score of a network.
 * 
 * This score is simply the sum of the scores of the individual nodes in the
 * network, which lends itself to performance-enhancements using several schemes
 * of caching the node scores.
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Jul 11, 2006
 * <p>
 * 9/6/2005 (v1.0.3) hjs    Correct defect in constructor, by making use of caching 
 *                          arrays conditional.
 * <p> 
 * 10/28/2005 (v2.0) hjs    Replace maxValueCount dependency on constant
 *                          with observed value from data.
 * <p>
 * hjs (v1.0.6) 12/7/2005   Add condition for checking varCount versus 
 *                          the maxParentCount (will be handled via validation
 *                          from v2.0.x forward).
 * <p>
 * hjs (v2.0)               Add inner classes for selecting between new compact observations
 *                          and original ("legacy") observations (which used multi-dim. arrays). <br>
 *                          The fast-cache implementations have been improved to be more sensitive
 *                          to memory use (applies for minMarkovLag>0). <br>
 *                          The use of the legacy observations is only provided for illustrative
 *                          purpose. The corresponding code will be deprecated in future versions
 *                          of Banjo. 
 * <p>                          
 * hjs (v2.1)               Modifications for execution as a multi-threaded application: <br>
 *                          - Observations are now loaded upfront before entering the searcher <br>
 *                          - fastCache is shared between threads (regular cache is not) <br>
 *                          - logGamma-preCompute cache is shared between threads
 *                           
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class EvaluatorBDe extends Evaluator {
        
    // alpha denotes the "equivalent sample size" (ess)
    protected final double alpha;

    protected final int observationCount;

    protected final int maxValueCount;// = observations.getMaxValueCount();
    
    protected int[] N_ij;
    protected int[] N_ijk;

    protected final double scoreDiff = BANJO.TEST_SCORETOLERANCE;
    
    protected final double unreachableBDeScore = BANJO.BANJO_UNREACHABLESCORE_BDE;
        
    // Used for keeping track of the best score as a cross check to the regular
    // statistics:
    protected double checkBestNetworkScore = 0.0;

    protected LogGammaProvider logGammaSelector;
    protected ObservationsSelector observationsSelector;
    
    protected final int dimObservations;
    protected final int dimVariables;
    protected final int dimLags;
    protected final int offsetObservations;
    protected final int offsetVariables;
    protected final int offsetLags;
        
    // Special "fast-access" cache for nodes with low parent count
    // In Banjo 2.0, the fastCache implementation uses a slight variation of the
    // storage implementation, to save array allocations to lags that are excluded
    // via the problem definition (i.e., lags less than minMarkovLag do not take up
    // storage anymore)
    // hjs  (v2.1) Declare arrays as static for shared access
    protected static double[] fastNodeScoreCacheFor0parents;
    protected static double[][] fastNodeScoreCacheFor1parent;
    protected static double[][][] fastNodeScoreCacheFor2parents;

    // Array for storing the precomputed log-gamma values,
    // for later retrieval via logGamma( alpha_ij, N_ij )
    // hjs  (v2.1) Declare array as static class variable for shared access
    protected static double[][] logGammaValue;
    
    // Set of inner classes to let us switch easily between different ways of 
    // computing the score values based on the log-Gamma function
    private abstract class LogGammaProvider {

        protected abstract double logGamma( int _q_i, int _N_ij ) throws Exception;
    }

    // When we use this implementation of LogGammaProvider, all calls to log-Gamma
    // are computed "as we go".
    private class LogGammaComputer extends LogGammaProvider {
        
        LogGammaComputer() {
            // nothing to do
        }
        
        protected double logGamma( int _q_i, int _N_ij ) throws Exception {
            
            return loggamma( alpha / _q_i + _N_ij );
        }
    }
    
    // In this implementation, we pre-compute log-Gamma() for all possible values 
    // based on the underlying data. This can be a very large table, based on the
    // values of the r_i. Note that for performance reasons we use an array even though
    // that can be rather sparse. For data with extremely large requirements, we will
    // likely have to do (either) a) restrict the observationCount-dimension of the
    // array (we want at least N=0), thus pre-computing only a subset of values,
    // and/or b) use a different data structure that stores values more efficiently.
    // (See the LogGammaTable class below)
    private class LogGammaArray extends LogGammaProvider {
    
        // The first dimension of the log-gamma value table
        int logGammaArgDimension;
        // Note: the second dimension is given by the observationCount
        
        // Array for tracking the cardinality of the different r_i values
        // e.g., c[2] would indicate how many r_i's have the value 2
        int[] c_i;
        
        // Utility variables
        int factorsUsed;
        int varCountUsed;
        
        final int maxVarCount; // max value count per variable
        final int maxParentCount; // max parent count (already available)
        
        // Using the precomputed log-gamma values provided by this class gives
        // the best performance, at a small cost of an extra preparation effort.
        LogGammaArray() throws Exception {
            
            int tmpMaxVarCount = 0;
            for ( int i=0; i<varCount; i++ ) {

                if ( tmpMaxVarCount < observationsSelector.getMaxValueCount(i)) {
                    
                    tmpMaxVarCount = observationsSelector.getMaxValueCount(i);
                }
            }
            maxVarCount = tmpMaxVarCount;
            
            maxParentCount = Integer.parseInt( 
                    processData.getValidatedProcessParameter(
                    BANJO.SETTING_MAXPARENTCOUNT ) );
            
            if ( BANJO.DEBUG && BANJO.TRACE_BDEPRECOMPUTE ) {
                
                System.out.println("(retrieved) maxVarCount = " + maxVarCount );
                System.out.println("(loaded) maxParentCount = " + maxParentCount );
            }
            /////////////////////////
            
            initLogGammaTable();
            fillLogGammaTable();
        }
        
        synchronized void initLogGammaTable() throws Exception {
                                    
            // Find the cardinalities for the r_i's in the possible q_i's
            c_i = new int[maxVarCount+1];
            for ( int i=0; i<=maxVarCount; i++ ) {
                c_i[i]=0;
            }
            for ( int i=0; i<varCount; i++ ) {
                c_i[ observationsSelector.getMaxValueCount(i) ]++;
            }
            // Account for the "extra" r_i factor (in q_i*r_i)
            for ( int i=0; i<=maxVarCount; i++ ) {
                
                if ( c_i[i] > 0 ) c_i[ i ]++;
                
                if ( BANJO.DEBUG && BANJO.TRACE_BDEPRECOMPUTE ) {
                    
                    System.out.println( "number of r_i's with " + i +
                            " values (at most 1 more than varCount): " + c_i[i] );
                }
            }
            
            // Cap the values at maxParentCount+1 (one for each factor of q_i, plus
            // the extra factor r_i)
            for ( int i=0; i<=maxVarCount; i++ ) {
                
                if ( c_i[i] > maxParentCount ) c_i[i] = maxParentCount+1;
                
                if ( BANJO.DEBUG && BANJO.TRACE_BDEPRECOMPUTE ) {
                    
                    System.out.println( "number of r_i's with " + i +
                            " values, adjusted: " + c_i[i] );
                }
            }

            factorsUsed = 0;
            logGammaArgDimension = 1;
            varCountUsed = maxVarCount;

            // hjs (v1.0.6) 12/7/2005
            // Add varCountUsed > 0 condition in case varCount is smaller than
            // the specified maxParentCount setting value.
            // Note that this condition will be enforced via validation of the
            // varCount versus the maxParentCount, starting in v2.0.x
            while ( factorsUsed <= maxParentCount && varCountUsed > 0 ) {
                
                // hjs (v2.2) 1/7/2008
                // Fix condition to properly account for all used factors
                // (the previous condition could get much larger than it needed too,
                // thus wasting substantial memory)
                for ( int i=0; i < c_i[ varCountUsed ] 
                            && factorsUsed <= maxParentCount; i++ ) {
                    
                    logGammaArgDimension *= varCountUsed;
                    factorsUsed++;
                }
                
                varCountUsed--;
            }
            
            if ( BANJO.DEBUG && BANJO.TRACE_BDEPRECOMPUTE ) {
            
                System.out.println( "max. product: " + logGammaArgDimension );
            }
        }
        
        synchronized void fillLogGammaTable() throws Exception {
                    
            // We only want to execute the filling of the table once when we
            // run in a MT environment
            synchronized( getClass() ) {
                
                // Only create the array (shared between multiple threads) once
                if ( logGammaValue == null ) {
                    
                    logGammaValue = new double[logGammaArgDimension+1][observationCount+1];
                    
                    for ( int i=1; i<logGammaArgDimension+1; i++ ) {
                        for ( int j=0; j<observationCount+1; j++ ) {
                            
                            // compute the value and store away in array
                            logGammaValue[i][j] = loggamma( alpha/i + j );
                        }                   
                    }
                }
            }
        }
        
        protected double logGamma( int _q_i, int _N_ij ) throws Exception {
            
            return logGammaValue[ _q_i ][ _N_ij ];
            // equivalent to: loggamma( alpha/q_i + N_ij );
        }
    }

    protected class LegacyObservationsSelector extends ObservationsSelector {

        protected ObservationsAsMatrix observations;

        protected LegacyObservationsSelector( ObservationsI _observations ) throws Exception {

            if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

                System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                        BANJO.FEEDBACK_NEWLINE + "    Evaluator: Before loading observations" )) );
            }
            
            observations = ( ObservationsAsMatrix ) _observations;
            

            if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

                System.out.println(
                        new StringBuffer( StringUtil.compileMemoryInfo( 
                                "    Evaluator: After loading observations" )) );
            }
        }

        protected int getObservationCount() throws Exception {
            
            return observations.getObservationCount();
        }
        
        protected int getMaxValueCount() throws Exception {
            
            return observations.getMaxValueCount();
        }
        
        protected int getMaxValueCount( int nodeID ) throws Exception {
            
            return observations.getMaxValueCount( nodeID );
        }
        
        protected double computeNodeScore( 
                final int nodeID, final int[][] parentIDlist ) 
                    throws Exception {
                  
          // --------------------------------------------
          // This is the core score computation algorithm
          // --------------------------------------------
                  
          // Data needed to frame the problem
          final int parentCount = parentIDlist.length;
          int parentNodeID;
    
          if (BANJO.TRACE_EVAL && BANJO.DEBUG) {
              for ( int parentIndex=0; parentIndex < parentCount; parentIndex++ ) {
                  
                  System.out.println(parentIndex + "    " + 
                          parentIDlist[parentIndex][0] + "    " + 
                          observations.getMaxValueCount(parentIDlist[parentIndex][0]));
              }
          }
          
          int q_i = 0;
          int r_i = observations.getMaxValueCount(nodeID);
          // Need the dimension for the N_ij and Nijk arrays:
          // Start with the current node
          int N_ij_arrayDimension = 1;
          int N_ijk_arrayDimension;
          // Then account for the values for all its parent nodes
          for ( int i=0; i < parentCount; i++ ) N_ij_arrayDimension *= maxValueCount;
          N_ijk_arrayDimension = N_ij_arrayDimension * maxValueCount;
          
          if (BANJO.TRACE_EVALPARENTCOUNT && BANJO.DEBUG) {
              System.out.println("Parent count = " + parentCount);
              System.out.println("Nij dimension = " + N_ij_arrayDimension);
              System.out.println("Nijk dimension = " + N_ijk_arrayDimension + "\n");
          }
          
          // Variables used in the computations
          int index = 0;
          int index2 = 0;
          double nodeScore = 0;
          int obsValue;
          int nonZeroN_ij_Count = 0;
          int nonZeroN_ijk_Count = 0;
    
          // Initialize the array:
          // Generally, this sort of shotgun preassignment would be wasteful, 
          // but here it lets us avoid computing the "filled" array INDEX values 
          // (i.e., the i's that are being used)
          for ( int i=0; i<N_ij_arrayDimension; i++ ) 
              N_ij[i] = 0;
          for ( int i=0; i<N_ijk_arrayDimension; i++ ) 
              N_ijk[i] = 0;
          
          // Compute the alpha(i,j,k): they are 1 / [ the products of the valueCounts ]
          // of the parents in the current parent configuration.
          // Start by computing the denominator of alpha(i,j,k)
          q_i = 1;
          for ( int parentIndex=0; parentIndex < parentCount; parentIndex++ ) {
              
              // For readability:
              parentNodeID = parentIDlist[parentIndex][0];
              // Multiply by the value count for each parent
              q_i *= observations.getMaxValueCount( parentNodeID );
          }
          
          // -------------------
          // Main counting loop:
          // -------------------
          // Outer loop iterates over all the (rows in the) observations
          // (DBN data is adjusted already via the data preparation in ObservationsAsMatrix class)
    
          // Variation that should be slightly faster than the above code
          if (parentCount == 0) {
              
              N_ij[0] = observationCount;
    
              for ( int observationRow=0; 
                      observationRow < observationCount; observationRow++ ) {
          
                  // Get the VALUE of the "current node" (given by nodeID) for this observation
                  index = observations.observationDataPoints[observationRow][nodeID][0];
                  
                  // Inner loop iterates over the number of parents in the current
                  // configuration of this node (with nodeID), to compute the index that we
                  // need to use in the N_ijk array
                  for ( int parentIndex=0; parentIndex < parentCount; parentIndex++ ) {
                                          
                      /*
                      // (Keep for readability):
                      parentNodeID = parentIDlist[parentIndex][0];
                      parentNodeLag = parentIDlist[parentIndex][1];
                      
                      // Compute the index for N_ijk, subject to the base "maxValueCount":
                      obsValue = observations.observationDataPoints
                              [ observationRow - parentNodeLag ][ parentNodeID ];
                       */
    
                      obsValue = observations.observationDataPoints
                                      [ observationRow ]
                                        [ parentIDlist[parentIndex][0] ]
                                          [ parentIDlist[parentIndex][1] ];
                  
                      index = index * maxValueCount + obsValue;
                  }
                  // Finally, increment the count of the appropriate N_ijk 
                  // for the current observation
                  N_ijk[index]++;
              }
          }
          else {
              
              for ( int observationRow=0; 
                      observationRow < observationCount; observationRow++ ) {
          
                  // Get the VALUE of the "current node" (given by nodeID) for this observation
                  index = observations.observationDataPoints[observationRow][nodeID][0];
                  index2 = 0;
                  
                  // Inner loop iterates over the number of parents in the current
                  // configuration of this node (with nodeID), to compute the index that we
                  // need to use in the N_ijk array
                  for ( int parentIndex=0; parentIndex < parentCount; parentIndex++ ) {
                                          
                      /*
                      // (Keep for readability):
                      parentNodeID = parentIDlist[parentIndex][0];
                      parentNodeLag = parentIDlist[parentIndex][1];
                      
                      // Compute the index for N_ijk, subject to the base "maxValueCount":
                      obsValue = observations.observationDataPoints
                              [ observationRow - parentNodeLag ][ parentNodeID ];
                       */
    
                      obsValue = observations.observationDataPoints
                                      [ observationRow ]
                                        [ parentIDlist[parentIndex][0] ]
                                          [ parentIDlist[parentIndex][1] ];
                  
                      index = index * maxValueCount + obsValue;
                      index2 = index2 * maxValueCount + obsValue;
                  }
                  // Update the N_ij value for this parent index:
                  N_ij[index2]++;
    
                  // Finally, increment the count of the appropriate N_ijk 
                  // for the current observation
                  N_ijk[index]++;
              }
          }
          
          // ---------------------------------------------
          // Now compute the logGamma score for this node:
          // ---------------------------------------------
          // Add the N_ij terms (Note: we omit N_ij(k)=0 terms, 
          // because they lead to log(1) = 0 contributions)
          nonZeroN_ij_Count = 0;
          for ( int i=0; i<N_ij_arrayDimension; i++ ) {
              if (N_ij[i] != 0) {
                  
                  nodeScore -= logGammaSelector.logGamma( q_i , N_ij[i] );
                  nonZeroN_ij_Count++;
              }
          }
          
          // Add the N_ijk terms
          nonZeroN_ijk_Count = 0;
          for ( int i=0; i<N_ijk_arrayDimension; i++ ) {
              if (N_ijk[i] != 0) {
    
                  nodeScore += logGammaSelector.logGamma( q_i*r_i, N_ijk[i] );
                  nonZeroN_ijk_Count++;
              }
          }
          
          // Add the remaining terms (the ones that are independent of the
          // N_ij's and N_ijk's)
          nodeScore +=  nonZeroN_ij_Count * logGammaSelector.logGamma( q_i, 0 ) - 
              nonZeroN_ijk_Count * logGammaSelector.logGamma( q_i*r_i, 0 );
          
          // ---------------------------------------------
    
          if (BANJO.TRACE_EVAL && BANJO.DEBUG) {
              //
              System.out.println("N_ijk_arrayDimension: " + N_ijk_arrayDimension );
              System.out.println("q_i * r_i: " + q_i*r_i );
              System.out.println("Non zero N_ij terms: " + nonZeroN_ij_Count );
              System.out.println("N_ij IndexAdjustment: " + ( q_i - nonZeroN_ij_Count ) );
              System.out.println("Non zero N_ijk terms: " + nonZeroN_ijk_Count );
              System.out.println("N_ijk IndexAdjustment: " + 
                      ( q_i*r_i - nonZeroN_ijk_Count ) );
              //
              System.out.println("Node i=" + nodeID + " contributes " + nodeScore);
              System.out.println();
          }
          
          return nodeScore;
      }
    }
    
    protected class CompactObservationsSelector extends ObservationsSelector {

        protected ObservationsAsArray observations;
        
        protected CompactObservationsSelector( ObservationsI _observations ) throws Exception {

          if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {
  
              System.out.println(
                      new StringBuffer( StringUtil.compileMemoryInfo( 
                              BANJO.FEEDBACK_NEWLINE + "    Evaluator: Before loading observations" )) );
          }
          
          synchronized( this ) {
              
              // Get access to the observations associated with the bayes net.
              observations = ( ObservationsAsArray ) _observations;
          }
  
          if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {
  
              System.out.println(
                      new StringBuffer( StringUtil.compileMemoryInfo( 
                              "    Evaluator: After loading observations" )) );
          }
        }

        protected int getObservationCount() throws Exception {
            
            return observations.getObservationCount();
        }
        
        protected int getMaxValueCount() throws Exception {
            
            return observations.getMaxValueCount();
        }
        
        protected int getMaxValueCount( int nodeID ) throws Exception {
            
            return observations.getMaxValueCount( nodeID );
        }
        
        protected double computeNodeScore( 
                final int nodeID, final int[][] parentIDlist ) 
                    throws Exception {
                    
            // --------------------------------------------
            // This is the core score computation algorithm
            // --------------------------------------------
                    
            // Data needed to frame the problem
            final int parentCount = parentIDlist.length;
            int parentNodeID;

            if (BANJO.TRACE_EVAL && BANJO.DEBUG) {
                for ( int parentIndex=0; parentIndex < parentCount; parentIndex++ ) {
                    
                    System.out.println( parentIndex + "    " + 
                            parentIDlist[parentIndex][0] + "    " + 
                            observations.getMaxValueCount(parentIDlist[parentIndex][0]) );
                }
            }
            
            int q_i = 0;
            int r_i = observations.getMaxValueCount(nodeID);
            // Need the dimension for the N_ij and Nijk arrays:
            // Start with the current node
            int N_ij_arrayDimension = 1;
            int N_ijk_arrayDimension;
            // Then account for the values for all its parent nodes
            for ( int i=0; i < parentCount; i++ ) N_ij_arrayDimension *= maxValueCount;
            N_ijk_arrayDimension = N_ij_arrayDimension * maxValueCount;
            
            if (BANJO.TRACE_EVALPARENTCOUNT && BANJO.DEBUG) {
                System.out.println("Parent count = " + parentCount);
                System.out.println("Nij dimension = " + N_ij_arrayDimension);
                System.out.println("Nijk dimension = " + N_ijk_arrayDimension + "\n");
            }
            
            // Variables used in the computations
            int index = 0;
            int index2 = 0;
            double nodeScore = 0;
            int obsValue;
            int nonZeroN_ij_Count = 0;
            int nonZeroN_ijk_Count = 0;

            // Initialize the array:
            // Generally, this sort of shotgun preassignment would be wasteful, 
            // but here it lets us avoid computing the "filled" array INDEX values 
            // (i.e., the i's that are being used)
            for ( int i=0; i<N_ij_arrayDimension; i++ ) 
                N_ij[i] = 0;
            for ( int i=0; i<N_ijk_arrayDimension; i++ ) 
                N_ijk[i] = 0;
            
            // Compute the alpha(i,j,k): they are 1 / [ the products of the valueCounts ]
            // of the parents in the current parent configuration.
            // Start by computing the denominator of alpha(i,j,k)
            q_i = 1;
            for ( int parentIndex=0; parentIndex < parentCount; parentIndex++ ) {
                
                // For readability:
                parentNodeID = parentIDlist[parentIndex][0];
                // Multiply by the value count for each parent
                q_i *= observations.getMaxValueCount( parentNodeID );
            }
            
            // -------------------
            // Main counting loop:
            // -------------------
            // Outer loop iterates over all the (rows in the) observations
            // (DBN data is adjusted already via the data preparation in ObservationsAsMatrix class)

            // Variation that should be slightly faster than the above code
            if (parentCount == 0) {
                
                N_ij[0] = observationCount;

                for ( int observationRow=0; 
                        observationRow < observationCount; observationRow++ ) {
            
                    // Get the VALUE of the "current node" (given by nodeID) for this observation
                    index = observations.observationDataPointsCompact[
                              nodeID + 
                              0*offsetLags + 
                              observationRow*offsetObservations ];
                    
                    // redundant (found by Josh -- 2/12/2008)
                    
                    // Inner loop iterates over the number of parents in the current
                    // configuration of this node (with nodeID), to compute the index that we
                    // need to use in the N_ijk array
//                    for ( int parentIndex=0; parentIndex < parentCount; parentIndex++ ) {
//                                            
//                        /*
//                        // (Keep for readability):
//                        parentNodeID = parentIDlist[parentIndex][0];
//                        parentNodeLag = parentIDlist[parentIndex][1];
//                        
//                        // Compute the index for N_ijk, subject to the base "maxValueCount":
//                        obsValue = observations.observationDataPoints
//                                [ observationRow - parentNodeLag ][ parentNodeID ];
//                         */
//
//                        obsValue = observations.observationDataPointsCompact[
//                               parentIDlist[parentIndex][0] + 
//                               parentIDlist[parentIndex][1]*offsetLags + 
//                               observationRow*offsetObservations ];
//                        
//                        index = index * maxValueCount + obsValue;
//                    }
                    // Finally, increment the count of the appropriate N_ijk 
                    // for the current observation
                    N_ijk[index]++;
                }
            }
            else {
                
                for ( int observationRow=0; 
                        observationRow < observationCount; observationRow++ ) {
            
                    // Get the VALUE of the "current node" (given by nodeID) for this observation
                    index = observations.observationDataPointsCompact[
                              nodeID + 
                              0*offsetLags + 
                              observationRow*offsetObservations ];
                    index2 = 0;
                    
                    // Inner loop iterates over the number of parents in the current
                    // configuration of this node (with nodeID), to compute the index that we
                    // need to use in the N_ijk array
                    for ( int parentIndex=0; parentIndex < parentCount; parentIndex++ ) {
                                            
                        /*
                        // (Keep for readability):
                        parentNodeID = parentIDlist[parentIndex][0];
                        parentNodeLag = parentIDlist[parentIndex][1];
                        
                        // Compute the index for N_ijk, subject to the base "maxValueCount":
                        obsValue = observations.observationDataPoints
                                [ observationRow - parentNodeLag ][ parentNodeID ];
                         */

                        obsValue = observations.observationDataPointsCompact[
                               parentIDlist[parentIndex][0] + 
                               parentIDlist[parentIndex][1]*offsetLags + 
                               observationRow*offsetObservations ];
                    
                        index = index * maxValueCount + obsValue;
                        index2 = index2 * maxValueCount + obsValue;
                    }
                    // Update the N_ij value for this parent index:
                    N_ij[index2]++;

                    // Finally, increment the count of the appropriate N_ijk 
                    // for the current observation
                    N_ijk[index]++;
                }
            }
            
            // ---------------------------------------------
            // Now compute the logGamma score for this node:
            // ---------------------------------------------
            // Add the N_ij terms (Note: we omit N_ij(k)=0 terms, 
            // because they lead to log(1) = 0 contributions)
            nonZeroN_ij_Count = 0;
            for ( int i=0; i<N_ij_arrayDimension; i++ ) {
                if (N_ij[i] != 0) {
                    
                    nodeScore -= logGammaSelector.logGamma( q_i , N_ij[i] );
                    nonZeroN_ij_Count++;
                }
            }
            
            // Add the N_ijk terms
            nonZeroN_ijk_Count = 0;
            for ( int i=0; i<N_ijk_arrayDimension; i++ ) {
                if (N_ijk[i] != 0) {

                    nodeScore += logGammaSelector.logGamma( q_i*r_i, N_ijk[i] );
                    nonZeroN_ijk_Count++;
                }
            }
            
            // Add the remaining terms (the ones that are independent of the
            // N_ij's and N_ijk's)
            nodeScore +=  nonZeroN_ij_Count * logGammaSelector.logGamma( q_i, 0 ) - 
                nonZeroN_ijk_Count * logGammaSelector.logGamma( q_i*r_i, 0 );
            
            // ---------------------------------------------

            if (BANJO.TRACE_EVAL && BANJO.DEBUG) {
                //
                System.out.println("N_ijk_arrayDimension: " + N_ijk_arrayDimension );
                System.out.println("q_i * r_i: " + q_i*r_i );
                System.out.println("Non zero N_ij terms: " + nonZeroN_ij_Count );
                System.out.println("N_ij IndexAdjustment: " + ( q_i - nonZeroN_ij_Count ) );
                System.out.println("Non zero N_ijk terms: " + nonZeroN_ijk_Count );
                System.out.println("N_ijk IndexAdjustment: " + 
                        ( q_i*r_i - nonZeroN_ijk_Count ) );
                //
                System.out.println("Node i=" + nodeID + " contributes " + nodeScore);
                System.out.println();
            }
            
            return nodeScore;
        }
    }
       
    public EvaluatorBDe( BayesNetManagerI _initialBayesNet, Settings _processData ) 
            throws Exception {

        
        super( _initialBayesNet, _processData );
        
        boolean isDataValid = validateRequiredData();

        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                    BANJO.FEEDBACK_NEWLINE + "    Evaluator: Before fastCache allocation" )) );
        }

        synchronized( getClass() ) {
            
            // Check if the user specified the fastCache use (potentially large memory use)
            if ( useCache ) {
    
                // Only create any of the arrays if they haven't been created yet
                if ( fastNodeScoreCacheFor0parents == null ) {
                    
                    // Create the arrays for the fast nodeScore cache (Note that the last value of the 
                    // second index will be used for "0 parents" case)
                    // Note also that we could save space by using (maxMarkovLag-minMarkovLag+1)
                    // instead of (maxMarkovLag+1) as a factor for several dimensions below
                    
                    // 9/6/2005 (v1.0.3) hjs    Set up the caching arrays only when necessary
                    // 11/1/2005 (v2.0) hjs     Streamline code, allow disabling of fast cache
                    if ( fastCacheLevel > -1 ) {
                        
                        fastNodeScoreCacheFor0parents = new double[varCount];
        
                        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {
        
                            System.out.println(
                                new StringBuffer( StringUtil.compileMemoryInfo( 
                                    BANJO.FEEDBACK_NEWLINE + 
                                    "    Evaluator: After level 0 allocation" )) );
                        }
                    }
                    if ( fastCacheLevel > 0 ) {
                        
                        fastNodeScoreCacheFor1parent = 
                            new double[varCount][varCount * ( maxMarkovLag+1-minMarkovLag )];
        
                        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {
        
                            System.out.println(
                                new StringBuffer( StringUtil.compileMemoryInfo( 
                                    BANJO.FEEDBACK_NEWLINE + 
                                    "    Evaluator: After level 1 allocation" )) );
                        }
                    }
                    if ( fastCacheLevel > 1 ) {
                        
                        fastNodeScoreCacheFor2parents = 
                            new double[varCount]
                                       [varCount * ( maxMarkovLag+1-minMarkovLag )]
                                        [varCount * ( maxMarkovLag+1-minMarkovLag )];
        
                        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {
        
                            System.out.println(
                                new StringBuffer( StringUtil.compileMemoryInfo( 
                                    BANJO.FEEDBACK_NEWLINE + 
                                    "    Evaluator: After level 2 allocation" )) );
                        }
                    }
                    if ( fastCacheLevel > BANJO.DEFAULT_MAXLEVELFORFASTCACHE ) {
                        
                        throw new BanjoException( BANJO.ERROR_BANJO_DEV,
                            "(Evaluator constructor) " +
                            "Development issue: " +
                            "Currently the fast cache can only handle up to 2 parents." );
                    }
        
                    // initialize the arrays to "unreachable" score values
                    if ( fastCacheLevel > -1 ) {
                        
                        for ( int i=0; i<varCount; i++ )
                            fastNodeScoreCacheFor0parents[i] = unreachableBDeScore;
                    }
                    if ( fastCacheLevel > 0 ) {
                        
                        for ( int i=0; i<varCount; i++ )
                            for ( int j=0; j<(varCount * (maxMarkovLag+1-minMarkovLag)); j++ ) 
                                fastNodeScoreCacheFor1parent[i][j] = unreachableBDeScore;
                    }
                    if ( fastCacheLevel > 1 ) {
                        
                        for ( int i=0; i<varCount; i++ )
                            for ( int j=0; j<(varCount * (maxMarkovLag+1-minMarkovLag)); j++ )
                                for ( int k=0; k<(varCount * (maxMarkovLag+1-minMarkovLag)); k++ ) 
                                    fastNodeScoreCacheFor2parents[i][j][k] = unreachableBDeScore;
                    }
                    if ( fastCacheLevel > BANJO.DEFAULT_MAXLEVELFORFASTCACHE ) {
                        
                        throw new BanjoException( BANJO.ERROR_BANJO_DEV,
                            "(EvaluatorBDe constructor) " +
                            "Development issue: " +
                            "Currently the fast cache can only handle up to 2 parents." );
                    }
                }
            }
        }

        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            "    Evaluator: After fastCache allocation" )) );
        }
        
            if ( ( _processData.getObservations() ) instanceof ObservationsAsArray ) {
                
                observationsSelector = new CompactObservationsSelector(
                        _processData.getObservations() );
            }
            else if ( ( _processData.getObservations() ) instanceof ObservationsAsMatrix ) {
    
                observationsSelector = new LegacyObservationsSelector(
                        _processData.getObservations() );
            }
            else {
                
                throw new BanjoException( 
                        BANJO.ERROR_BANJO_DEV,
                        "(EvaluatorBDe constructor) " +
                        "Development issue: There is no code for handling the supplied type " +
                        "of observations." );
            }
        
        maxValueCount = observationsSelector.getMaxValueCount();
        observationCount = observationsSelector.getObservationCount();
                
        // Cache the max. parent count
        final int maxParentCount = Integer.parseInt( 
                _processData.getValidatedProcessParameter(
                        BANJO.SETTING_MAXPARENTCOUNT ));

        if ( isDataValid ) {
            
            alpha = Double.parseDouble( 
                    _processData.getValidatedProcessParameter(
                            BANJO.SETTING_EQUIVALENTSAMPLESIZE ) );
        }
        else {
            
            alpha = BANJO.BANJO_EQUIVALENTSAMPLESIZE_INVALIDVALUE;
        }
        
        // Set up the Nij and Nijk arrays
        int N_ij_maxArrayDimension = 1;
        int N_ijk_maxArrayDimension;
        
        // Compute the max. dimensions that we need for storing the Nij's and Nijk's
        for ( int i=0; i < maxParentCount; i++ ) 
                N_ij_maxArrayDimension *= maxValueCount;
        N_ijk_maxArrayDimension = N_ij_maxArrayDimension * maxValueCount;
                
        // Now create the arrays
        N_ij = new int[N_ij_maxArrayDimension];
        N_ijk = new int[N_ijk_maxArrayDimension];
        
        long maxSearchLoops = Long.parseLong( _processData.getValidatedProcessParameter( 
                BANJO.SETTING_MAXPROPOSEDNETWORKS) );
        long maxTime = Long.parseLong( _processData.getValidatedProcessParameter( 
                BANJO.SETTING_MAXSEARCHTIME) );
        long maxRestarts = Long.parseLong( _processData.getValidatedProcessParameter( 
                BANJO.SETTING_MAXRESTARTS) );

        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            BANJO.FEEDBACK_NEWLINE + "    Evaluator: Before logGamma setup (precompute)" )) );
        }
        

        // Check if we should precompute the log-gamma values
        if ( _processData.getValidatedProcessParameter( 
                BANJO.SETTING_PRECOMPUTE ).equalsIgnoreCase( 
                        BANJO.UI_PRECOMPUTE_NO ) 
             || maxSearchLoops == 0 || maxTime == 0 || maxRestarts == 0 ) {
            // We don't want to precompute for short data runs

            logGammaSelector = new LogGammaComputer();
        }
        else {

            logGammaSelector = new LogGammaArray();
        }

        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            "    Evaluator: After logGamma setup (precompute)" )) );
        }
        
        // Note: The original ("legacy") observations don't require any offsets,
        // so this section is only used for the new compact implementation
        dimObservations = observationCount;
        dimVariables = varCount;
        dimLags = maxMarkovLag+1;
        
        // Variables are listed consecutively within each lag:
        offsetVariables = 1;

        // The index "offset" between consecutive observation rows:
        offsetObservations = dimVariables * dimLags;
        
        // The index "offset" between consecutive lags:
        offsetLags = dimVariables;
    }

    /**
     * Validates the settings values required for loading the BDe evaluator.
     * 
     * @return Returns the boolean flag that indicates whether a crucial setting
     * could not be validated.
     */
    private boolean validateRequiredData() throws Exception {
    
        boolean isDataValid = true;
        
        // utility variables for validating
        String settingNameCanonical;
        String settingNameDescriptive;
        String settingNameForDisplay;
        String settingDataType;
        int validationType;
        SettingItem settingItem;
        String strCondition;
        double dblValue;
        Set validValues = new HashSet();
        
        //  Validate the 'Equivalent Sample Size'
        settingNameCanonical = BANJO.SETTING_EQUIVALENTSAMPLESIZE;
        settingNameDescriptive = BANJO.SETTING_EQUIVALENTSAMPLESIZE_DESCR;
        settingNameForDisplay = BANJO.SETTING_EQUIVALENTSAMPLESIZE_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_DOUBLE;
        validationType = BANJO.VALIDATIONTYPE_MANDATORY;            
        settingItem = processData.processSetting( settingNameCanonical,
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                null, 
                " " );
        
        if ( settingItem.isValidSetting() ) {
            
            try {
    
                strCondition = new String( "greater than 0" );
                dblValue = Double.parseDouble( 
                        processData.getValidatedProcessParameter(
                                settingNameCanonical ));
                if ( dblValue <= 0 ) {
                    
                    processData.addToErrors( new BanjoError( 
                            StringUtil.composeErrorMessage( 
                                    settingItem, 
                                    strCondition ),
                            BANJO.ERRORTYPE_INVALIDRANGE,
                            settingNameCanonical,
                            StringUtil.getClassName( this ) ) );
                }
            }
            catch ( Exception e ) {
    
                throw new BanjoException( 
                        BANJO.ERROR_BANJO_DEV, "Code section for validating '" +
                        settingNameCanonical + "'." );
            }
        }
        else {
            
            isDataValid = false;
        }

        // Validate the 'pre-compute LogGamma'
        settingNameCanonical = BANJO.SETTING_PRECOMPUTE;
        settingNameDescriptive = BANJO.SETTING_PRECOMPUTE_DESCR;
        settingNameForDisplay = BANJO.SETTING_PRECOMPUTE_DISP;
        settingDataType = BANJO.VALIDATION_DATATYPE_STRING;
        validationType = BANJO.VALIDATIONTYPE_OPTIONAL; 
        validValues.clear();
        validValues.add( BANJO.UI_PRECOMPUTE_YES );
        validValues.add( BANJO.UI_PRECOMPUTE_NO );      
        settingItem = processData.processSetting( settingNameCanonical,
                settingNameDescriptive,
                settingNameForDisplay,
                settingDataType,
                validationType,
                validValues,
                BANJO.UI_PRECOMPUTE_DEFAULT );
        
        return isDataValid;
    }
    
    public double computeInitialNetworkScore( 
            BayesNetManagerI currentBayesNetManager )
            throws Exception {

        // On the first call to compute the network score, the currentScore is 0,
        // and we compute the complete score from all the individual scores.
        // On subsequent calls, we use the prior knowledge and subtract, then add
        // the score for whatever node changes for the current step.
        double networkScore;
        int currentNodeLag = 0;
        int parentNodeID;
        int parentNodeLag;
        // Utility variable to handle 2-parent case
        // dim 1 is for the IDs, dim 2 is for the lags of the parent nodes
        int[][] parentNodes = new int[2][2];        
        // The parentIDlist is a 2-dimensional array, where parentIDlist[0][] contains
        // the id's of the parents, and parentIDlist[1][] contains the corresponding
        // lags of the parent nodes
        int[][] parentIDlist;
        double nodeScore;
        int hashCode;
        
        networkScore = 0;
        
        // Note: It may seem odd that the code below checks for cached values.
        // However, even in cases where we restart witho random network configurations
        // we can save computing efforts by looking at the caches first.
        // Only on the very first call to this method do we need to compute the 
        // score of all nodes:
        for ( int currentNodeID=0; currentNodeID<varCount; currentNodeID++ ) {
            
            // Compute the score for each node
            parentIDlist = currentBayesNetManager.
                    getCurrentParents().getCurrentParentIDlist( 
                            currentNodeID , currentNodeLag );

            // Check if nodeScore is already in cache
            // First assign a "non-reachable" score (computed scores are <=0)
            nodeScore = unreachableBDeScore;
            
            if ( useCache ) {
        
                if ( parentIDlist.length <= fastCacheLevel ) {
                    
                    if ( parentIDlist.length == 0 ) {
                        
                        nodeScore = 
                            fastNodeScoreCacheFor0parents[currentNodeID];
                        
                        fetchedCachedScoreTracker[ 0 ]++;
                    }
                    else if ( parentIDlist.length == 1 ) {
                                            
                        // Find the ID and lag of the parent node:
                        // Since there's only 1 parent, the first array index is 0
                        parentNodeID = parentIDlist[0][0];
                        parentNodeLag = parentIDlist[0][1];
                        
                        nodeScore = 
                            fastNodeScoreCacheFor1parent[currentNodeID]
                                [parentNodeID + varCount * ( parentNodeLag-minMarkovLag )];
                        
                        fetchedCachedScoreTracker[ 1 ]++;
                    }
                    else if ( parentIDlist.length == 2 ) {

                        // Find the IDs and lags of the 2 parent nodes:
                        // Order them first by ID, and then by lag (if IDs are the same)
                        parentNodes = order2Parents( parentIDlist );
                            
                        nodeScore = 
                            fastNodeScoreCacheFor2parents[currentNodeID]
                              [parentNodes[0][0] + varCount*( parentNodes[0][1]-minMarkovLag )]
                              [parentNodes[1][0] + varCount*( parentNodes[1][1]-minMarkovLag )];
                        
                        fetchedCachedScoreTracker[ 2 ]++;
                    }
                }
                else if ( useBasicCache ) {
                    
                    synchronized ( this ) {
                                            
                        hashCode = currentNodeScoreHashItem.getHashCode( 
                                currentNodeID, parentIDlist );
                        
                        // First check if the key already exists
                        if ( nodeScoreHashMap.containsKey( new Integer(hashCode) ) ) {
                            
                            // Now check if the stored object matches our nodeID 
                            // and parentIDList:
                            retrievedNodeScoreHashItem = 
                                (NodeScoreCacheItemI) nodeScoreHashMap.get(
                                        new Integer(hashCode));
                            if ( currentNodeID == retrievedNodeScoreHashItem.getNodeID() &&
                                    retrievedNodeScoreHashItem.hasIdenticalParentList( 
                                            parentIDlist ) ) {
                                
                                nodeScore = 
                                    retrievedNodeScoreHashItem.getNodeScore();
                                
                                fetchedCachedScoreTracker[ parentIDlist.length ]++;
                            }
                            else {
                                
                                collisionsInCacheTracker[ parentIDlist.length ]++;
                            }
                        }
                    }
                }
            }
            
            // If the score is still positive (1), it wasn't in the cache and we 
            // need to compute it                   
            if ( nodeScore == unreachableBDeScore ) {
            
                nodeScore = computeNodeScore( currentNodeID, parentIDlist );
                
                // Update the counter for computed (node) scores
                computedScoreTracker++;
            
                // Cache the score 
                if ( useCache ) {
                    
                    // Fast cache (currently up to parent count = 2)
                    if ( parentIDlist.length <= fastCacheLevel ) {
                                            
                        if (parentIDlist.length == 0) {
                            
                            fastNodeScoreCacheFor0parents[currentNodeID] = nodeScore;
                            
                            placedInCacheScoreTracker[ 0 ]++;
                        }
                        else if (parentIDlist.length == 1) {

                            // Find the ID and lag of the parent node:
                            // Since there's only 1 parent, the first array index is 0
                            parentNodeID = parentIDlist[0][0];
                            parentNodeLag = parentIDlist[0][1];
                            fastNodeScoreCacheFor1parent[currentNodeID]
                                 [parentNodeID + varCount*( parentNodeLag-minMarkovLag )] = nodeScore;
                            
                            placedInCacheScoreTracker[ 1 ]++;
                        }
                        else if (parentIDlist.length == 2) {

                            // Find the IDs and lags of the 2 parent nodes:
                            // Order them first by ID, and then by lag 
                            // (if IDs are the same)
                            parentNodes = order2Parents( parentIDlist );
                                
                            fastNodeScoreCacheFor2parents[currentNodeID]
                                 [parentNodes[0][0] + varCount*( parentNodes[0][1] - minMarkovLag )]
                                 [parentNodes[1][0] + varCount*( parentNodes[1][1] - minMarkovLag )]
                                        = nodeScore;
                            
                            placedInCacheScoreTracker[ 2 ]++;
                        }
                    }
                    else if ( useBasicCache ) {

                        synchronized ( this ) {
                            
                            // General case: store the node score based on its 
                            // "parent signature"
                            nodeScoreHashMap.put(
                                    new Integer( currentNodeScoreHashItem.getHashCode( 
                                            currentNodeID, parentIDlist ) ), 
                                    new NodeScoreCacheItem( 
                                            currentNodeID, parentIDlist, nodeScore ) );
                            
                            placedInCacheScoreTracker[ parentIDlist.length ]++;
                        }
                    }
                }
            }
            
            networkScore += nodeScore;
            
            // Cache the node score, because each upcoming bayesNetChange is just
            // a local change
            cachedNodeScores[ currentNodeID ] = nodeScore;
            
            // Finally, store the computed network score for use in the next step
            cachedNetworkScore = networkScore;
            
            // Note: Can only update checkBestNetworkScore conditionally, because this
            // method is called on "restart" (and we don't want to lose our highest
            // score value for the cross-checking)
            if ( checkBestNetworkScore == 0 )
                checkBestNetworkScore = networkScore;
        }
                
        return networkScore;
    }
    
    // Update the score for the given change to the network
    public double updateNetworkScore( 
            final BayesNetManagerI currentBayesNetManager, 
            final BayesNetChangeI currentBayesNetChange ) throws Exception {
        
        // On the first call to compute the network score, the currentScore is 0,
        // and we compute the complete score from all the individual scores.
        // On subsequent calls, we use the prior knowledge and subtract, then add
        // the score for whatever node changes for the current step.

        double currentNetworkScore;
        double updatedCurrentNodeScore;
        double updatedParentNodeScore;
        final int currentNodeID;
        // The lag of the current node is always 0 (can't be in prior time slice)
        final int currentNodeLag = 0;
        int parentNodeIDinNetwork;
        int parentNodeLagInNetwork;
        final int[][] parentIDlistForNode;
        final int[][] parentIDlistForParent;
        int[][] parentNodes = new int[2][2];
        int hashCode;
        EdgesWithCachedStatisticsI currentNetwork = 
            currentBayesNetManager.getCurrentParents();
                
        // When a BayesNetChange is specified, we only need to compute the changes to
        // the total score (related to change of an edge between the node and its parent)
        // First get the ID of the node that changes:
        currentNodeID = currentBayesNetChange.getCurrentNodeID();
        parentIDlistForNode = currentNetwork.getCurrentParentIDlist( 
                currentNodeID, currentNodeLag );
        
        // Check if nodeScore is already in cache
        updatedCurrentNodeScore = unreachableBDeScore;
        
        if ( useCache ) {
    
            if ( parentIDlistForNode.length <= fastCacheLevel ) {
                
                if ( parentIDlistForNode.length == 0 ) {
                    
                    updatedCurrentNodeScore = 
                        fastNodeScoreCacheFor0parents[currentNodeID];
                    
                    fetchedCachedScoreTracker[ 0 ]++;
                }
                else if ( parentIDlistForNode.length == 1 ) {
                                        
                    // Find the ID and lag of the parent node:
                    // Since there's only 1 parent, the first array index is 0
                    parentNodeIDinNetwork = parentIDlistForNode[0][0];
                    parentNodeLagInNetwork = parentIDlistForNode[0][1];
                    
                    updatedCurrentNodeScore = 
                        fastNodeScoreCacheFor1parent[currentNodeID]
                            [parentNodeIDinNetwork
                             + varCount * ( parentNodeLagInNetwork - minMarkovLag ) ];
                    
                    fetchedCachedScoreTracker[ 1 ]++;
                }
                else if (parentIDlistForNode.length == 2) {

                    // Find the IDs and lags of the 2 parent nodes:
                    // Order them first by ID, and then by lag (if IDs are the same)
                    parentNodes = order2Parents( parentIDlistForNode );
                    
                    // Note that the parentLag for each of the parentNodes will be shifted based
                    // on our parent set representation, namely by minMarkovLag
                        
                    updatedCurrentNodeScore = 
                        fastNodeScoreCacheFor2parents[currentNodeID]
                             [parentNodes[0][0] + varCount*( parentNodes[0][1] - minMarkovLag )]
                             [parentNodes[1][0] + varCount*( parentNodes[1][1] - minMarkovLag )];
                                        
                    fetchedCachedScoreTracker[ 2 ]++;
                }
            }
            else if ( useBasicCache ) {
                
                synchronized ( this ) {
                    
                    hashCode = currentNodeScoreHashItem.getHashCode( 
                            currentNodeID, parentIDlistForNode );
                
                    // First check if the key already exists
                    if ( nodeScoreHashMap.containsKey( new Integer(hashCode) ) ) {
                        
                        // Now check if the stored object matches our nodeID + parentIDList:
                        retrievedNodeScoreHashItem = 
                            (NodeScoreCacheItemI) nodeScoreHashMap.get( 
                                    new Integer(hashCode) );
                        if ( currentNodeID == retrievedNodeScoreHashItem.getNodeID() &&
                                retrievedNodeScoreHashItem.hasIdenticalParentList( 
                                        parentIDlistForNode ) ) {
                            
                            updatedCurrentNodeScore = 
                                retrievedNodeScoreHashItem.getNodeScore();
                            
                            fetchedCachedScoreTracker[ parentIDlistForNode.length ]++;
                        }
                        else {

                            collisionsInCacheTracker[ parentIDlistForNode.length ]++;
                        }
                    }
                }
            }
        }
        
        // No matching entry found in the cache; compute the node score 
        if ( updatedCurrentNodeScore == unreachableBDeScore ) {
                
            try {
                    
                // Compute the score for the change
                updatedCurrentNodeScore = computeNodeScore( 
                        currentNodeID, parentIDlistForNode );
                computedScoreTracker++;
                if ( parentIDlistForNode.length > highestParentCountEncountered )
                    highestParentCountEncountered++;

                // and place it in the cache
                if ( useCache ) {
                    
                    if ( parentIDlistForNode.length <= fastCacheLevel ) {

                        if (parentIDlistForNode.length == 0) {
                            
                            fastNodeScoreCacheFor0parents[currentNodeID] = 
                                          updatedCurrentNodeScore;
                            
                            placedInCacheScoreTracker[ 0 ]++;
                        }
                        else if (parentIDlistForNode.length == 1) {
                            
                            // Find the ID and lag of the parent node:
                            // Since there's only 1 parent, the first array index is 0
                            parentNodeIDinNetwork = parentIDlistForNode[0][0];
                            parentNodeLagInNetwork = parentIDlistForNode[0][1];
                            fastNodeScoreCacheFor1parent[currentNodeID]
                                [parentNodeIDinNetwork 
                                 + varCount*( parentNodeLagInNetwork - minMarkovLag )]
                                         = updatedCurrentNodeScore;
                            
                            placedInCacheScoreTracker[ 1 ]++;
                        }
                        else if (parentIDlistForNode.length == 2) {

                            // Find the IDs and lags of the 2 parent nodes
                            parentNodes = order2Parents( parentIDlistForNode );
                                
                            fastNodeScoreCacheFor2parents[currentNodeID]
                                 [parentNodes[0][0] + varCount*( parentNodes[0][1] - minMarkovLag )]
                                 [parentNodes[1][0] + varCount*( parentNodes[1][1] - minMarkovLag )]
                                   = updatedCurrentNodeScore;
                            
                            placedInCacheScoreTracker[ 2 ]++;
                        }
                    }

                    else if ( useBasicCache ) {
                    
                        synchronized ( this ) {
                            
                            nodeScoreHashMap.put(
                                    new Integer( currentNodeScoreHashItem.getHashCode(
                                            currentNodeID, parentIDlistForNode ) ), 
                                    new NodeScoreCacheItem(
                                            currentNodeID, 
                                            parentIDlistForNode, 
                                            updatedCurrentNodeScore ));
                                                    
                            placedInCacheScoreTracker[ parentIDlistForNode.length ]++;
                        }
                    }
                }
            }
            catch (OutOfMemoryError e ) {
                
                // Try to make the best of things..
                System.out.println( "(EvaluatorBDE:updateNetworkScore)" +
                        " Out of memory. NodeID " + currentNodeID + 
                        " has parent count= " + 
                        parentIDlistForNode.length);
                
                throw new BanjoException( BANJO.ERROR_BANJO_OUTOFMEMORY,
                        "Out of memory in (" +
                        StringUtil.getClassName(this) +
                        ".updateNetworkScore)" );
            }
        }           

        cachedNetworkPreviousScore = cachedNetworkScore;
        
        cachedCurrentNodePreviousScore = cachedNodeScores[ currentNodeID ];
        cachedNodeScores[ currentNodeID ] = updatedCurrentNodeScore;
                            
        // -------------------
        // If there was a reversal of an arc then we also need to update
        // the score for the parent node (in the same way as above):
        
        if (currentBayesNetChange.getChangeType() 
                == BANJO.CHANGETYPE_REVERSAL) {
                        
            updatedParentNodeScore = unreachableBDeScore;
            
            // Get the parent info for the reversal case (the parent plays the 
            // role of the currentNode above)
            final int parentNodeID = 
                currentBayesNetChange.getParentNodeID();
            final int parentNodeLag = 
                currentBayesNetChange.getParentNodeLag();

            parentIDlistForParent = currentBayesNetManager.getCurrentParents().
                    getCurrentParentIDlist( parentNodeID, parentNodeLag );
            
            if ( useCache ) {
                
                if ( parentIDlistForParent.length <= fastCacheLevel ) {
                    
                    if ( parentIDlistForParent.length == 0 ) {
                        
                        updatedParentNodeScore = 
                            fastNodeScoreCacheFor0parents[parentNodeID];
                        
                        if ( updatedCurrentNodeScore != 0 ) 
                            fetchedCachedScoreTracker[ 0 ]++;
                    }
                    else if ( parentIDlistForParent.length == 1 ) {
                                    
                        // Nothing to compute in this case, since we have a 
                        // reversal of edges
                        updatedParentNodeScore = 
                            fastNodeScoreCacheFor1parent[ parentNodeID ]
                                 [ currentNodeID - minMarkovLag ];
                        
                        if ( updatedCurrentNodeScore != 0 ) 
                            fetchedCachedScoreTracker[ 1 ]++;   
                    }
                    else if (parentIDlistForParent.length == 2) {

                        // Find the IDs and lags of the 2 parent nodes:
                        // Order them first by ID, and then by lag (if IDs are the same)
                        parentNodes = order2Parents( parentIDlistForParent );
                            
                        updatedParentNodeScore = 
                            fastNodeScoreCacheFor2parents[parentNodeID]
                              [parentNodes[0][0] + varCount*( parentNodes[0][1] - minMarkovLag )]
                              [parentNodes[1][0] + varCount*( parentNodes[1][1] - minMarkovLag )];
                        
                        if ( updatedCurrentNodeScore != 0 ) 
                            fetchedCachedScoreTracker[ 2 ]++;
                    }
                }
                else if ( useBasicCache ) {

                    synchronized ( this ) {
                        
                        hashCode = currentNodeScoreHashItem.getHashCode( 
                                parentNodeID, parentIDlistForParent );
                        
                        // First check if the key already exists
                        if ( nodeScoreHashMap.containsKey( new Integer(hashCode) ) ) {
                            
                            // Now check if the stored object matches our nodeID 
                            // and parentIDList:
                            retrievedNodeScoreHashItem = (NodeScoreCacheItemI) 
                                nodeScoreHashMap.get( new Integer(hashCode) );
                            if ( parentNodeID == retrievedNodeScoreHashItem.getNodeID() &&
                                    retrievedNodeScoreHashItem.hasIdenticalParentList( 
                                            parentIDlistForParent ) ) {
                                
                                updatedParentNodeScore = 
                                    retrievedNodeScoreHashItem.getNodeScore();
                                
                                if ( updatedCurrentNodeScore != 0 ) 
                                    fetchedCachedScoreTracker[ 
                                        parentIDlistForParent.length ]++;
                            }
                            else {

                                collisionsInCacheTracker[ parentIDlistForParent.length ]++;
                            }
                        }
                    }
                }
            }
            
            // No matching entry found in the cache; compute the node score 
            if ( updatedParentNodeScore == unreachableBDeScore ) {
                
                try {

                    updatedParentNodeScore = computeNodeScore( 
                            parentNodeID, parentIDlistForParent );
                    computedScoreTracker++;

                    // and place the computed score in the cache
                    if ( useCache ) {
                     
                        if ( parentIDlistForParent.length <= fastCacheLevel ) {

                            if (parentIDlistForParent.length == 0) {
                                
                                fastNodeScoreCacheFor0parents[ parentNodeID ] = 
                                              updatedParentNodeScore;
                                placedInCacheScoreTracker[ 0 ]++;
                            }
                            else if (parentIDlistForParent.length == 1) {
                                                                
                                fastNodeScoreCacheFor1parent[ parentNodeID ]
                                     [ currentNodeID - minMarkovLag ] = updatedParentNodeScore;
                                placedInCacheScoreTracker[ 1 ]++;
                            }
                            else if (parentIDlistForParent.length == 2) {

                                // Find the IDs and lags of the 2 parent nodes:
                                // Order them first by ID, and then by lag 
                                //(if IDs are the same)
                                parentNodes = order2Parents( parentIDlistForParent );
                                    
                                fastNodeScoreCacheFor2parents[ parentNodeID ]
                                      [parentNodes[0][0] + varCount*( parentNodes[0][1] - minMarkovLag )]
                                      [parentNodes[1][0] + varCount*( parentNodes[1][1] - minMarkovLag )]
                                       = updatedParentNodeScore;
                                placedInCacheScoreTracker[ 2 ]++;
                            }
                        }
                        else if ( useBasicCache ) {
                            
                            synchronized ( this ) {

                                nodeScoreHashMap.put(
                                        new Integer( currentNodeScoreHashItem.getHashCode(
                                                parentNodeID, parentIDlistForParent ) ), 
                                        new NodeScoreCacheItem(
                                                parentNodeID, 
                                                parentIDlistForParent, 
                                                updatedParentNodeScore ));
                                placedInCacheScoreTracker[ parentIDlistForParent.length ]++;
                            }
                        }
                    }
                }
                catch (OutOfMemoryError e ) {
                    
                    // Let's hope that we can still print this, if we end up here:
                    System.out.print( "OUCH: Out of memory. Node 'ParentID'=" +
                            parentNodeID + " parent count= ");
                    System.out.println( parentIDlistForParent.length );
                    
                    throw new BanjoException( BANJO.ERROR_BANJO_DEV,
                            "Out of memory in " +
                            StringUtil.getClassName(this) +
                            ".updateNetworkScore" );
                }
            }

            cachedParentNodePreviousScore = cachedNodeScores[ parentNodeID ];
            cachedNodeScores[ parentNodeID ] = updatedParentNodeScore;
        }       
        
        currentNetworkScore = 0;
        for ( int i=0; i < varCount; i++ ) {
            
            currentNetworkScore += cachedNodeScores[ i ];
        }
        
        // Finally, cache the computed network score
        cachedNetworkScore = currentNetworkScore;
                
        if ( checkBestNetworkScore < currentNetworkScore ) {
            checkBestNetworkScore = currentNetworkScore;
        }
        
        return currentNetworkScore;
    }
    
    private double computeNodeScore( 
            final int nodeID, final int[][] parentIDlist ) 
                throws Exception {
                
        return observationsSelector.computeNodeScore( nodeID, parentIDlist );
    }

    public void adjustNodeScoresForUndo( 
            final BayesNetChangeI suggestedBayesNetChange ) throws Exception {
    
        // When a bayesNetChange gets rejected within the searcher, the bayesNetManager  
        // will reverse the change to its structure. Since the scores for any affected
        // nodes and the entire network are cached between search steps, we need to
        // adjust these scores to reflect the previous network state.
        
        // Adjust the score for the current node
        cachedNodeScores[ suggestedBayesNetChange.getCurrentNodeID() ] = 
            cachedCurrentNodePreviousScore;
    
        // Adjust the score of the parent node: needed only for the reversal case
        if ( suggestedBayesNetChange.getChangeType() == 
            BANJO.CHANGETYPE_REVERSAL ) {

            cachedNodeScores[ suggestedBayesNetChange.getParentNodeID() ] = 
                cachedParentNodePreviousScore;
        }
    
        // Adjust the score for the network
        cachedNetworkScore = cachedNetworkPreviousScore;
    }
    
    private int[][] order2Parents( int[][] parentListWith2Nodes ) {
        // Note: first dim denotes the node, and second dim determines ID and
        // lag (0=ID, 1=lag)
        int tmpValue;
        
        // Swap the parent nodes if the IDs are not in ascending order
        if ( parentListWith2Nodes[0][0] > parentListWith2Nodes[1][0] ) {
                        
            // swap IDs
            tmpValue = parentListWith2Nodes[0][0];
            parentListWith2Nodes[0][0] = parentListWith2Nodes[1][0];
            parentListWith2Nodes[1][0] = tmpValue;
            // swap Lags
            tmpValue = parentListWith2Nodes[0][1];
            parentListWith2Nodes[0][1] = parentListWith2Nodes[1][1];
            parentListWith2Nodes[1][1] = tmpValue;
        }
        // Also swap, if the ID's are the same, but the lags are not in asc. order
        else if ( parentListWith2Nodes[0][0] == parentListWith2Nodes[1][0] && 
                parentListWith2Nodes[0][1] > parentListWith2Nodes[1][1] ) {

            // Need to swap lags only
            tmpValue = parentListWith2Nodes[0][1];
            parentListWith2Nodes[0][1] = parentListWith2Nodes[1][1];
            parentListWith2Nodes[1][1] = tmpValue;
        }                           
        
        return parentListWith2Nodes;
    }
  
    
    // Keep: used for testing
    public void testNodeScores( 
            int currentNodeID, int[][] parentIDlist, double nodeScore ) 
                throws Exception {
        
        // testing code
        double testNodeScore = computeNodeScore( 
                currentNodeID, parentIDlist );
        
        if ( Math.abs( testNodeScore - nodeScore ) > 
                scoreDiff ) {
            
            // dummy statement for setting breakpoint
            int breakPoint = 0;
        }
    }
    
    public void cleanupOnException() throws Exception {
                
        observationsSelector = null;
        
        fastNodeScoreCacheFor0parents = null;
        fastNodeScoreCacheFor1parent = null;
        fastNodeScoreCacheFor2parents = null;
        
        super.cleanupOnException();
    }
}
