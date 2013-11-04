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
import edu.duke.cs.banjo.data.settings.*;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Contains the data structures built around an adjacency matrix-approach for storing 
 * the network structure, using a 1-dimensional array.
 *
 * <p><strong>Details:</strong> <br>
 * 
 * <p><strong>Change History:</strong> <br>
 * Created on June 12, 2006
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class EdgesAsArrayWithCachedStatistics extends EdgesAsArray 
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
    protected int[] parentIDs;
    // Additional storage containers for computing cycles
    protected int[] ancestorSet;
    protected int[] nodesVisited;
    protected int[] nodesToVisit;
    
    // Info where cycle was encountered;
    protected int cycleAtNode = -1;
    
    // Instances of this class may be built from an associated file, which we store here
    String associatedDirectory;
    String associatedFileName;
    
    protected final Settings processData;
    
    // Internal object for "walking" through the represented structure (e.g., using a
    // dfs = depth-first search)
    protected CycleFinder cycleFinder;
    // only used for dev:
    protected BayesNetChangeI bayesNetChange;
    
    protected abstract class CycleFinder {
        
        protected boolean cycleFound;
                
        public abstract boolean isCyclic(
                int nodeToCheck, int parentNodeToCheck ) throws Exception;
        public abstract boolean isCyclic__TEST_ENTIRE_STRUC() throws Exception;
        public abstract void adjustForLocalChange( final int _nodeToCheck ) throws Exception;
        public abstract void resetEntireGraph() throws Exception;

        public boolean isCyclic(
                final int nodeID, 
                final int[] parentNodeIDs) {

            cycleFound = false;
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
                        
                        currentEntry = matrix[ parentNodeID*offsetVariables + j ];
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
    }
    
    protected class CycleFinderDFS extends CycleFinder {
        
        protected CycleFinderDFS() throws Exception  {

            // Note: The original dfs implementation is based on the apply-check-undo approach,
            // hence the exception in case that is not set
            if ( processData.getDynamicProcessParameter( BANJO.CONFIG_CYCLECHECKER_ACTIONORDER )
                    .equalsIgnoreCase( BANJO.DATA_CYCLECHECKER_APPLYTHENCHECK ) ) {
                
                throw new BanjoException(
                       BANJO.ERROR_BANJO_DEV, 
                       " (" + StringUtil.getClassName(this) + 
                       " constructor) " + 
                       "Development issue: " +
                       "The 'dfs' cycle checking requires the 'check-apply' approach." );
            }
        }

        protected int time = 0;
        protected int[] round = new int[ varCount ];
        
        public boolean isCyclic(
                int nodeToCheck, int parentNodeToCheck ) throws Exception {
            
            final int changeType = bayesNetChange.getChangeType();
            
            if ( changeType == BANJO.CHANGETYPE_REVERSAL ) {
            
                // Delete the original link (from "node to parent"!)
                matrix[ parentNodeToCheck*offsetVariables + nodeToCheck ] = 0;
    
                time++;
                round[ nodeToCheck ] = time;
                DFSplain( nodeToCheck );
                
                cycleFound = ( round[ parentNodeToCheck ] == time );
                
                // Undo the deletion of the original link
                matrix[ parentNodeToCheck*offsetVariables + nodeToCheck ] = 1;
            }
            else {
              
                time++;
                round[ nodeToCheck ] = time;
                DFSplain( nodeToCheck );
                cycleFound = ( round[ parentNodeToCheck ] == time );
            }
            
            cycleAtNode = nodeToCheck;
            return cycleFound;
        }
        
        public void adjustForLocalChange( final int _nodeToCheck ) throws Exception {
        
            // straight DFS, so there's nothing to adjust
        }
        
        public void resetEntireGraph() throws Exception {

            // straight DFS, so there's nothing to reset
        }
        
        // Check an entire parentMatrix (e.g., the mandatory parents set)
        public boolean isCyclic__TEST_ENTIRE_STRUC() {
            
            cycleFound = false;
            cycleAtNode = -1;
            
            // Check for each node that it is not contained in a cycle
            int i = 0;
            while (i<varCount && !cycleFound) {
                
                for (int j=0; j<varCount; j++) {

                    parentIDs[j] = matrix[ i*offsetVariables + j ];
                }
                cycleFound = isCyclic(i, parentIDs);
                i++;
            }
            
            cycleAtNode = i-1;
            return cycleFound;
        }

        // recursive def. for Shmueli's DFS (plain version)
        protected void DFSplain( final int s ) throws Exception {

            round[ s ] = time;        
            
            for ( int t=0; t<varCount; t++ ) {
                
                if ( matrix[ t*offsetVariables + s ] == 1 &&
                        round[ s ] != round[ t ] &&
                        t!=s ) {
                    
                    DFSplain( t );
                }
            }
        }
    }
    
    protected class CycleFinderDFSwithShmueli extends CycleFinder {

        protected int[] round = new int[ varCount ];
        protected int[] num = new int[ varCount ];
        protected int lastChange;
        protected int visit;
        protected int time = 0;
        // for debugging only
        int roundNumCounter = 0;
        int roundOnlyCounter = 0;
     
        protected CycleFinderDFSwithShmueli() throws Exception  {

            // Note: The original dfs implementation is based on the apply-check-undo approach,
            // hence the exception in case that is not set
            if ( processData.getDynamicProcessParameter( BANJO.CONFIG_CYCLECHECKER_ACTIONORDER )
                    .equalsIgnoreCase( BANJO.DATA_CYCLECHECKER_APPLYTHENCHECK ) ) {
                
                throw new BanjoException(
                       BANJO.ERROR_BANJO_DEV, 
                       " (" + StringUtil.getClassName(this) + 
                       " constructor) " + 
                       "Development issue: " +
                       "The 'dfs with Shmueli' cycle checking requires the 'check-apply' approach." );
            }
        }

        public boolean isCyclic(
                int nodeToCheck, int parentNodeToCheck ) throws Exception {

            if ( round[ nodeToCheck ] > round[ parentNodeToCheck ] ||
                    ( round[ nodeToCheck ] == round[ parentNodeToCheck ] && 
                      num[ nodeToCheck ] < num[ parentNodeToCheck ] ) ) {
                
                cycleFound = false;
            }
            else {
                
                // For reversals we need to subtract the original edge first, else we will 
                // always end up with a trivial cycle

                final int changeType = bayesNetChange.getChangeType();
                
                if ( changeType == BANJO.CHANGETYPE_REVERSAL ) {

                    // Undo the deletion of the existing link
                    matrix[ parentNodeToCheck*offsetVariables + nodeToCheck ] = 0;

                    time++;

                    round[ nodeToCheck ] = time;
                    visit = 0;
                    DFS( nodeToCheck );
                  
                    cycleFound = ( round[ parentNodeToCheck ] == time );  
                    
                    // Undo the deletion of the original link (the one we deleted so we could add the opposite one)
                    matrix[ parentNodeToCheck*offsetVariables + nodeToCheck ] = 1;
                }
                else {
                    // "anything but reversal" case (in effect: additions)
                    
                    time++;
                    round[ nodeToCheck ] = time;
                    visit = 0;
                    DFS( nodeToCheck );
                    
                    cycleFound = ( round[ parentNodeToCheck ] == time );  
                }
            }
        
            return cycleFound;
        }

        public void adjustForLocalChange( final int _nodeToCheck ) throws Exception {
            
            // This resets the round/num values to valid ones
            time++;
            round[ _nodeToCheck ] = time;
            visit = 0;
            DFS( _nodeToCheck );
        }

        public void resetEntireGraph() throws Exception {
                     
          // This resets the round/num values
          time++;

          for ( int t=0; t<varCount; t++ ) {
              round[ t ] = time;
              num[ t ] = 0;
          }
        }

        // Check an entire parentMatrix (e.g., the mandatory parents set)
        public boolean isCyclic__TEST_ENTIRE_STRUC() {
            
            cycleFound = false;
            cycleAtNode = -1;
            
            // Check for each node that it is not contained in a cycle
            int i = 0;
            while (i<varCount && !cycleFound) {
                
                for (int j=0; j<varCount; j++) {

                    parentIDs[j] = matrix[ i*offsetVariables + j ];
                }
                cycleFound = isCyclic(i, parentIDs);
                i++;
            }
            
            return cycleFound;
        }
        
        // (recursive def.) Shmueli's DFS
        protected void DFS( final int s ) throws Exception {

            round[ s ] = time;
            
            for ( int t=0; t<varCount; t++ ) {
                
                if ( matrix[ t*offsetVariables + s ] == 1 &&
                        round[ s ] != round[ t ] &&
                        t!=s ) {
                    
                    DFS( t );
                }
            }
            
            visit++;
            num[ s ] = visit;
        }
    }
    
    protected class CycleFinderDFSorig extends CycleFinder {
        
        protected CycleFinderDFSorig() throws Exception  {

            // Note: The original dfs implementation is based on the apply-check-undo approach,
            // hence the exception in case that is not set
            if ( processData.getDynamicProcessParameter( BANJO.CONFIG_CYCLECHECKER_ACTIONORDER )
                    .equalsIgnoreCase( BANJO.DATA_CYCLECHECKER_CHECKTHENAPPLY ) ) {
                
                throw new BanjoException(
                       BANJO.ERROR_BANJO_DEV, 
                       " (" + StringUtil.getClassName(this) + 
                       " constructor) " + 
                       "Development issue: " +
                       "The 'original' dfs cycle checking requires the 'apply-check-undo' approach." );
            }
        }
        
        public boolean isCyclic(
                int nodeToCheck, int parentNodeToCheck ) throws Exception {

            // This is the "truly" old code
            return cycleFound = DFSorig( nodeToCheck, parentNodeToCheck, bayesNetChange );
        }

        public void adjustForLocalChange( final int _nodeToCheck ) throws Exception {

            // straight DFS, so there's nothing to adjust
        }
        
        public void resetEntireGraph() throws Exception {

            // straight DFS, so there's nothing to reset
        }

        // Check an entire parentMatrix (e.g., the mandatory parents set)
        public boolean isCyclic__TEST_ENTIRE_STRUC() {
            
            // Note that we assume that the dimension of the nodeSetToCheck
            // is based on the same varCount that defines the underlying bayesnet
            cycleFound = false;
            
            cycleAtNode = -1;
            
            // Check for each node that it is not contained in a cycle
            int i = 0;
            while (i<varCount && !cycleFound) {
                
                for (int j=0; j<varCount; j++) {

                    parentIDs[j] = matrix[ i*offsetVariables + j ];
                }
                cycleFound = isCyclic(i, parentIDs);
                i++;
            }
            
            return cycleFound;
        }

        protected boolean DFSorig( 
                final int nodeToCheck, 
                final int parentNodeToCheck, 
                final BayesNetChangeI _bayesNetChange ) throws Exception {

            int[] previousNodeLevel = new int[ varCount ];
            int[] lastNodeProcessed = new int[ varCount ];
            boolean[] _ancestorSet =  new boolean[ varCount ];
            
            try {

                int currentNodeLevel;
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

                        currentEntry = matrix[ 
                                currentNodeLevel*offsetVariables + nextParentToProcess ];
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
                
                System.out.print( "(EdgesAsArrayWithCachedStatistics.isCyclicDFS) " +
                        "Out of memory.");
                
                // Also: would need to check in the calling code for this return value
                e.printStackTrace();
                
                throw new BanjoException( BANJO.ERROR_BANJO_OUTOFMEMORY,
                        "Out of memory in (" +
                        StringUtil.getClassName(this) +
                        ".isCyclicDFS)" );
            }
            catch (Exception e) {

                throw new BanjoException( e, 
                        BANJO.ERROR_BANJO_DEV,
                        "\n(EdgesAsArrayWithCachedStatistics.isCyclicDFS) - " +
                        "Development issue: Encountered an error " +
                        "while checking for cycles based on bayesNetChange='" +
                        _bayesNetChange.toString() + "'.");
            }

            return _ancestorSet[ nodeToCheck ];
        } // end isCyclicDFS
        
    }
    
    // Note that we only provide constructors that give us access to the settings
    // (== processData). The resaon: (unfortunately) we need to be able to select between
    // "regular" and Shmueli-based "graph walkers", based on the selected search algorithm)
    /**
     * Basic constructor for creating the initial matrix and its supporting structures.
     */
    public EdgesAsArrayWithCachedStatistics(
            final int _varCount, 
            final int _minMarkovLag, 
            final int _maxMarkovLag,
            final Settings _processData ) throws Exception {
        
        // hand off the low level stuff
        super( _varCount, _minMarkovLag, _maxMarkovLag );

        processData = _processData;
        
        // This sets up the internal cycle-finder classes
        String cycleCheckerMethod = processData.getDynamicProcessParameter( 
                BANJO.CONFIG_CYCLECHECKER_METHOD );
        if ( cycleCheckerMethod.equalsIgnoreCase( BANJO.DATA_CYCLECHECKING_DFS ) ) {

            // Use the standard DFS
            cycleFinder = new CycleFinderDFS();
        }
        else if ( cycleCheckerMethod.equalsIgnoreCase( BANJO.DATA_CYCLECHECKING_SHMUELI ) ) {

            // Use the Shmueli-based approach 
            cycleFinder = new CycleFinderDFSwithShmueli();
        }
        else if ( cycleCheckerMethod.equalsIgnoreCase( BANJO.DATA_CYCLECHECKING_DFSORIG ) ) {

            // Use the standard DFS
            cycleFinder = new CycleFinderDFSorig();
        }
//        else {
//
//            throw new BanjoException(  
//                    BANJO.ERROR_BANJO_DEV,
//                    "\n(EdgesAsArrayWithCachedStatistics constructor) - " +
//                    "Development issue: Invalid cycle finder class specified.");
//        }
        
        ////////////////////
        
        // Create the parentCount vector
        parentCount = new int[_varCount];

        // Allocate the containers used for computing cycles 
        parentIDs = new int[_varCount];
        ancestorSet = new int[_varCount];
        nodesVisited = new int[_varCount];
        nodesToVisit = new int[_varCount];
        
        // Initialize the matrix and the parentCount vector
        initMatrix();
    }
    
    public EdgesAsArrayWithCachedStatistics( 
            final int _varCount,
            final int _minMarkovLag,
            final int _maxMarkovLag,
            final String _directory, 
            final String _fileName,
            final Settings _processData,
            final String cycleCheckInstructions ) throws Exception {
        
        this( _varCount, _minMarkovLag, _maxMarkovLag, _processData );

        // The EdgesAsArrayWithCachedStatistics may store a "true" network (directed acyclic graph),
        // or simply a collection of nodes (as for the mustBeAbsentParents set), each of which may
        // be loaded from a file. Since the validation is different in each case, we use separate
        // methods for each case.
        if ( cycleCheckInstructions.equalsIgnoreCase( BANJO.DATA_CYCLECHECKING_CHECKFORCYCLES ) ) {
        
            loadStructureFile( _directory, _fileName, _processData );
        }
        else if ( cycleCheckInstructions.equalsIgnoreCase( BANJO.DATA_CYCLECHECKING_TREATASPLAINNODES ) ) {
            
            loadStructureAsNodes( _directory, _fileName );
        }
        else if ( cycleCheckInstructions.equalsIgnoreCase( BANJO.DATA_CYCLECHECKING_NOCHECKING ) ) {
            
            // Nothing to do (as the constant states), but we make the developer specify that this 
            // is indeed the case
        }
        else {
            
            // Instructions need to be supplied, else we throw an exception to let 
            // the developer know
            throw new BanjoException(
                    BANJO.ERROR_BANJO_DEV, 
                    "(" + StringUtil.getClassName( this ) + ".constructor) " +
                    "Development issue: When loading a set of nodes or a network, " +
                    "whichever it is must be specified, so that proper validation " +
                    "can take place.)" );
        }
    }
    
    // hjs 2/26/2008 (ver2.2) Add processData to constructor parameter list, to avoid problems
    // down the road
    public EdgesAsArrayWithCachedStatistics( 
            final EdgesAsArray _edgesAsArray, final Settings _processData ) throws Exception {
        
        // 1/18/2006 (v2.0) hjs We call this particular super constructor (and not the
        // basic constructor from this class) because it is more efficient,
        // at the price of some code duplication.

        this( _edgesAsArray.varCount, 
                _edgesAsArray.minMarkovLag, 
                _edgesAsArray.maxMarkovLag, _processData );
                
        for (int i=0; i<varCount; i++) {

            // Set all parentCounts to 0
            parentCount[i] = 0;
            
            // Set all matrix entries to 0
            for (int j=0; j<varCount; j++) {
                for (int k=0; k<maxMarkovLag+1-minMarkovLag; k++) {

                    matrix[ i*offsetVariables + k*offsetLags + j ] = 
                        _edgesAsArray.matrix[ i*offsetVariables + k*offsetLags + j ];
                    parentCount[i] += _edgesAsArray.matrix[ i*offsetVariables + k*offsetLags + j ];
                }
            }
        }
        
        // and set the grand total
        combinedParentCount = 0;
        for (int i=0; i<varCount; i++) {
            
            combinedParentCount += parentCount[i];
        }
    }
    
    public EdgesAsArrayWithCachedStatistics( 
            final EdgesAsArrayWithCachedStatistics _edgesAsArray ) 
            throws Exception {
        
        // 1/18/2006 (v2.0) hjs We call this particular super constructor (and not the
        // basic constructor from this class) because it is more efficient,
        // at the price of some code duplication.
        this( _edgesAsArray.varCount, 
                _edgesAsArray.minMarkovLag, 
                _edgesAsArray.maxMarkovLag,
                _edgesAsArray.processData );
        
        for (int i=0; i<varCount; i++) {

            // Set all parentCounts to 0
            parentCount[i] = _edgesAsArray.parentCount[i];
        }
        
        // and set the grand total
        combinedParentCount = _edgesAsArray.combinedParentCount;
    }
    
    public EdgesAsArrayWithCachedStatistics( 
            final int _varCount,
            final int _minMarkovLag,
            final int _maxMarkovLag,
            final Settings _processData,
            final String _structureAsString ) 
            throws Exception {
        

        this( _varCount, _minMarkovLag, _maxMarkovLag, _processData );
        
        // Create the parentCount vector
        parentCount = new int[_varCount];

        // Allocate the containers used for computing cycles 
        parentIDs = new int[_varCount];
        ancestorSet = new int[_varCount];
        nodesVisited = new int[_varCount];
        nodesToVisit = new int[_varCount];
        
        loadStructure( _structureAsString, _processData );        
    }
        
    // Initialize the parent matrix to all 0's, indicating "no parent" relationships.
    public void initMatrix() {
        
        for (int i=0; i<varCount; i++) {

            // Set all parentCounts to 0
            parentCount[i] = 0;
            
            // Set all matrix entries to 0
            for (int j=0; j<varCount; j++) {
                 for (int k=0; k<maxMarkovLag+1-minMarkovLag; k++) {

                    matrix[ i*offsetVariables + k*offsetLags + j ] = 0;
                }
            }
        }
        // and set the grand total
        combinedParentCount = 0;
    }
    
    // Initialize the parent matrix to a specified value (typically, to all '1's)
    public void initMatrix( final int valueToInitTo ) {

        combinedParentCount=0;

        // More verbose, but better performance by checking on the value first
        if ( valueToInitTo == 0 ) {
            
            for (int i=0; i<varCount; i++) {
    
                parentCount[i] = 0;
                
                // Set the matrix entries and adjust the parent counts
                for (int j=0; j<varCount; j++) {
                    for (int k=0; k<maxMarkovLag+1-minMarkovLag; k++) {
                        
                        matrix[ i*offsetVariables + k*offsetLags + j ] = valueToInitTo;
                    }
                }
            }
        }
        else {
            
            for (int i=0; i<varCount; i++) {
            
                parentCount[i] = 0;
                
                // Set the matrix entries and adjust the parent counts
                for (int j=0; j<varCount; j++) {
                    for (int k=0; k<maxMarkovLag+1-minMarkovLag; k++) {

                        matrix[ i*offsetVariables + k*offsetLags + j ] = valueToInitTo;
                            
                            parentCount[i]++;
                            combinedParentCount++;
                    }
                }
            }
        }
    }
    
    public void assignMatrix( 
            final EdgesWithCachedStatisticsI _matrixToAssign ) 
            throws Exception {
                        
        EdgesAsArrayWithCachedStatistics matrixToAssign;
        if ( _matrixToAssign instanceof EdgesAsArrayWithCachedStatistics ) {
            
            matrixToAssign = ( EdgesAsArrayWithCachedStatistics ) _matrixToAssign;
 
            if (this.varCount == matrixToAssign.varCount && 
                    this.maxMarkovLag == matrixToAssign.maxMarkovLag) {
                
                for (int i=0; i < matrixToAssign.matrix.length; i++) {

                    this.matrix[ i ] = matrixToAssign.matrix[ i ];
                }
                
                for (int i=0; i<varCount; i++) {
                    
                    // Assign the parentCounts
                    parentCount[i] = matrixToAssign.parentCount[i];
                }
                
                // Assign the grand total
                combinedParentCount = matrixToAssign.combinedParentCount;
            }
            else {
                
                // Flag as internal error - matrix dimensions don't match
                // (this should never happen..)
                throw new BanjoException(
                        BANJO.ERROR_BANJO_DEV, 
                        "(" + StringUtil.getClassName( this ) + ".assignMatrix) " +
                        "Development issue: Dimensions of matrices don't match!");
            }
        }
        else {
            
            // dev error: we should never end up here
            throw new BanjoException( 
                    BANJO.ERROR_BANJO_DEV, 
                    "(" + StringUtil.getClassName( this ) + ".assignMatrix) " +
                    "Development issue: Arguments are of unexpected type (" +
                    "need: 'EdgesAsArrayWithCachedStatistics')" );
        }
    }   

    public void subtractMatrix( 
            final EdgesWithCachedStatisticsI _matrixToSubtract ) 
            throws Exception {
                        
        EdgesAsArrayWithCachedStatistics matrixToSubtract;
        if ( _matrixToSubtract instanceof EdgesAsArrayWithCachedStatistics ) {
            
            matrixToSubtract = ( EdgesAsArrayWithCachedStatistics ) _matrixToSubtract;
 
            
            int[] tmpMatrix = matrixToSubtract.getMatrix();
                        
            for (int i=0; i<varCount; i++) {
    
                // 2/22/2006 (v1.0.6/v2.0) hjs Correct the computation of the parent counts:
                // This line should not be here at all, because we need to start out
                // with the parent counts of the current matrix!!
                ////parentCount[i] = matrixToSubtract.getParentCount(i);
                
                // Set the matrix entries
                for (int j=0; j<varCount; j++) {
                    for (int k=0; k<maxMarkovLag+1-minMarkovLag; k++) {
                        
                        if ( tmpMatrix[ i*offsetVariables + k*offsetLags + j ] == 1 ) {
                            
                            if ( matrix[ i*offsetVariables + k*offsetLags + j ] == 1 ) {
                                
                                parentCount[i]--;
                                combinedParentCount--;
                                matrix[ i*offsetVariables + k*offsetLags + j ] = 0;
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
            throw new BanjoException(BANJO.ERROR_BANJO_DEV, 
                    "(" + StringUtil.getClassName( this ) + ".subtractMatrix) " +
                    "Development issue: Arguments are of unexpected type (" +
                    "need: 'EdgesAsArrayWithCachedStatistics')" );
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
                        "(EdgesAsArrayWithCachedStatistics.loadFromFile) " +
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
                // 19  0:   0                 1:   1 18
                            
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
                                    dbnLagList[ lagListindex ].trim() );
                            
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
                                // we'll ignore any lag info that is outside the acceptable range)
                                // Validate the lag:
//                              if ( intMarkovLag < minMarkovLag ) {
//      
//                                  throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
//                                          "The input line '" + nextInputLine  + 
//                                          "' in '" + fileName + 
//                                          "' specifies a lag (=" +
//                                          intMarkovLag +
//                                          ") for variable '" + intNodeID +
//                                          "' that is smaller than the min. Markov Lag (=" 
//                                          + minMarkovLag + ")." );
//                              }
//                              if ( intMarkovLag > maxMarkovLag ) {
//      
//                                  throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
//                                          "The input line '" + nextInputLine  + 
//                                          "' in '" + fileName + 
//                                          "' specifies a lag (=" +
//                                          intMarkovLag +
//                                          ") for variable '" + intNodeID +
//                                          "' that is larger than the max. Markov Lag (=" 
//                                          + maxMarkovLag + ")." );
//                              }
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
                                        
//                                      if ( intParentCount > maxParentCount ) {
//                                          
//                                          throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
//                                                  "The input line '" + nextInputLine  + 
//                                                  "' in '" + fileName + 
//                                                  "' specifies a parent count (=" +
//                                                  intParentCount + ") " +
//                                                  "for variable (=" + intNodeID +
//                                                  ") that exceeds the max. parent count (=" +
//                                                  maxParentCount + ")." );
//                                      }
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
//                                      // Validate the lag:
//                                      if ( intMarkovLag < minMarkovLag ) {
//      
//                                          throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
//                                              "The input line '" + nextInputLine  + 
//                                              "' in '" + fileName + 
//                                              "' specifies a lag (=" +
//                                              intMarkovLag +
//                                              ") for variable '" + intNodeID +
//                                              "' that is smaller than the min. Markov Lag (=" 
//                                              + minMarkovLag + ")." );
//                                      }
//                                      if ( intMarkovLag > maxMarkovLag ) {
//      
//                                          throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
//                                                  "The input line '" + nextInputLine  + 
//                                                  "' in '" + fileName + 
//                                                  "' specifies a lag (=" +
//                                                  intMarkovLag +
//                                                  ") for variable '" + intNodeID +
//                                                  "' that is larger than the max. Markov Lag (=" 
//                                                  + maxMarkovLag + ")." );
//                                      }
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
                                
//                              if ( intParentCount > maxParentCount ) {
//                                  
//                                  throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
//                                          "The input line '" + nextInputLine  + 
//                                          "' in '" + fileName + 
//                                          "' specifies a parent count (=" +
//                                          intParentCount + ") " +
//                                          "for variable (=" + intNodeID +
//                                          ") that exceeds the max. parent count (=" +
//                                          maxParentCount + ")." );
//                              }
                                
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
                        "\n  (EdgesAsArrayWithCachedStatistics.loadFromFile) - " +
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
                    "\n(EdgesAsArrayWithCachedStatistics.loadFromFile) - " +
                    "Encountered an error while loading the structure file '" +
                    _fileName + "' from directory '" + _directory + "'.");
        }   
    }
    
    private void loadStructure( final String _structureAsString, final Settings _processData ) 
        throws Exception {

        String _fileName = "FILENAME";
        String _directory = "DIRECTORYNAME";
        
        try {

            Scanner stringScanner = new Scanner( _structureAsString );
            
//            associatedDirectory = new String( _directory );
//            associatedFileName = new String(_fileName );
//            
//            File dataFile = new File( _directory, _fileName );
//            BufferedReader bufferedReader = null;
//            
//            if (!dataFile.exists()) {
//                                
//                throw new BanjoException( 
//                        BANJO.ERROR_BANJO_USERINPUT, 
//                        "(EdgesAsArrayWithCachedStatistics.loadFromFile) " +
//                        "Cannot find the structure file: '" + _fileName + "' " +
//                        "in directory '" + _directory + "'." );
//            }

//            bufferedReader = new BufferedReader(
//                    new FileReader( _directory + File.separator + _fileName ));
                                
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
            
//            if ( _processData.isSettingValueValid( BANJO.SETTING_MAXPARENTCOUNT ) ) {
//            
//                maxParentCount = Integer.parseInt( 
//                        _processData.getValidatedProcessParameter( 
//                            BANJO.SETTING_MAXPARENTCOUNT ) );
//            }
//            else {
//                
//                throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT, 
//                        "Cannot load the structure file '" + _fileName +
//                        "', because the max. parent count is not set " +
//                        "(but needed for validation)." );
//            }
            maxParentCount = 7;
            
            while ( !varCountFound && firstTextline != null ) {
                
                firstTextline = stringScanner.nextLine();
                
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
                // 19  0:   0                 1:   1 18
                            
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
                                            dbnLagList[ lagListindex ].trim() );
                                    
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
//                              if ( intMarkovLag < minMarkovLag ) {
//      
//                                  throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
//                                          "The input line '" + nextInputLine  + 
//                                          "' in '" + fileName + 
//                                          "' specifies a lag (=" +
//                                          intMarkovLag +
//                                          ") for variable '" + intNodeID +
//                                          "' that is smaller than the min. Markov Lag (=" 
//                                          + minMarkovLag + ")." );
//                              }
//                              if ( intMarkovLag > maxMarkovLag ) {
//      
//                                  throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
//                                          "The input line '" + nextInputLine  + 
//                                          "' in '" + fileName + 
//                                          "' specifies a lag (=" +
//                                          intMarkovLag +
//                                          ") for variable '" + intNodeID +
//                                          "' that is larger than the max. Markov Lag (=" 
//                                          + maxMarkovLag + ")." );
//                              }
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
//                                      if ( intMarkovLag < minMarkovLag ) {
//      
//                                          throw new BanjoException( 
//                                              BANJO.ERROR_BANJO_USERINPUT,
//                                              "The input line '" + nextInputLine  + 
//                                              "' in '" + fileName + 
//                                              "' specifies a lag (=" +
//                                              intMarkovLag +
//                                              ") for variable '" + intNodeID +
//                                              "' that is smaller than the min. Markov Lag (=" 
//                                              + minMarkovLag + ")." );
//                                      }
//                                      if ( intMarkovLag > maxMarkovLag ) {
//      
//                                          throw new BanjoException( 
//                                              BANJO.ERROR_BANJO_USERINPUT,
//                                              "The input line '" + nextInputLine  + 
//                                              "' in '" + fileName + 
//                                              "' specifies a lag (=" +
//                                              intMarkovLag +
//                                              ") for variable '" + intNodeID +
//                                              "' that is larger than the max. Markov Lag (=" 
//                                              + maxMarkovLag + ")." );
//                                      }
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

                    if ( stringScanner.hasNext() ) {
                    
                        nextInputLine = stringScanner.nextLine();
                    }
                    else {
                        
                        nextInputLine = null;
                    }
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

                    if ( stringScanner.hasNext() ) {
                    
                        nextInputLine = stringScanner.nextLine();
                    }
                    else {
                        
                        nextInputLine = null;
                    }
                }
            }
        }
//        catch (IOException e) {
//               
//                throw new BanjoException( e, 
//                        BANJO.ERROR_BANJO_DEV, 
//                        "(EdgesAsArrayWithCachedStatistics.loadFromFile) - " +
//                        "Encountered an I/O error while loading the structure file '" +
//                        _fileName + "' from directory '" + _directory + "'." );
//        }       
        catch ( BanjoException e ) {

                // "forward" any BanjoException
                throw new BanjoException( e );
        }       
        catch ( Exception e ) {

            throw new BanjoException( e, 
                    BANJO.ERROR_BANJO_DEV,
                    "\n(EdgesAsArrayWithCachedStatistics.loadFromFile) - " +
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
    // 19  0:   0                 1:   1 18

    private void loadStructureFile(
            final String _directory, 
            final String _fileName,
            final Settings _processData ) 
                    throws Exception {
        
        try {

            associatedDirectory = new String( _directory );
            associatedFileName = new String(_fileName );
            
            File dataFile = new File( _directory, _fileName );
            BufferedReader bufferedReader = null;
            
            if (!dataFile.exists()) {
                                
                throw new BanjoException( 
                        BANJO.ERROR_BANJO_USERINPUT, 
                        "(EdgesAsArrayWithCachedStatistics.loadFromFile) " +
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
                // 19  0:   0                 1:   1 18
                            
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
                                            dbnLagList[ lagListindex ].trim() );
                                    
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
//                              if ( intMarkovLag < minMarkovLag ) {
//      
//                                  throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
//                                          "The input line '" + nextInputLine  + 
//                                          "' in '" + fileName + 
//                                          "' specifies a lag (=" +
//                                          intMarkovLag +
//                                          ") for variable '" + intNodeID +
//                                          "' that is smaller than the min. Markov Lag (=" 
//                                          + minMarkovLag + ")." );
//                              }
//                              if ( intMarkovLag > maxMarkovLag ) {
//      
//                                  throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
//                                          "The input line '" + nextInputLine  + 
//                                          "' in '" + fileName + 
//                                          "' specifies a lag (=" +
//                                          intMarkovLag +
//                                          ") for variable '" + intNodeID +
//                                          "' that is larger than the max. Markov Lag (=" 
//                                          + maxMarkovLag + ")." );
//                              }
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
//                                      if ( intMarkovLag < minMarkovLag ) {
//      
//                                          throw new BanjoException( 
//                                              BANJO.ERROR_BANJO_USERINPUT,
//                                              "The input line '" + nextInputLine  + 
//                                              "' in '" + fileName + 
//                                              "' specifies a lag (=" +
//                                              intMarkovLag +
//                                              ") for variable '" + intNodeID +
//                                              "' that is smaller than the min. Markov Lag (=" 
//                                              + minMarkovLag + ")." );
//                                      }
//                                      if ( intMarkovLag > maxMarkovLag ) {
//      
//                                          throw new BanjoException( 
//                                              BANJO.ERROR_BANJO_USERINPUT,
//                                              "The input line '" + nextInputLine  + 
//                                              "' in '" + fileName + 
//                                              "' specifies a lag (=" +
//                                              intMarkovLag +
//                                              ") for variable '" + intNodeID +
//                                              "' that is larger than the max. Markov Lag (=" 
//                                              + maxMarkovLag + ")." );
//                                      }
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
                        "(EdgesAsArrayWithCachedStatistics.loadFromFile) - " +
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
                    "\n(EdgesAsArrayWithCachedStatistics.loadFromFile) - " +
                    "Encountered an error while loading the structure file '" +
                    _fileName + "' from directory '" + _directory + "'.");
        }   
    }
    
    // This method can be used in the automatic validation of a series of results 
    // such as the collection of (n-best) networks.
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
                            "(EdgesAsArrayWithCachedStatistics.reconstructMatrix) " +
                            "Development issue: " +
                            "The read-in var count doesn't match the internal parameter.");
                }
    
                // Set up the matrix and the parentCounts (need to have values assigned)
                for (int i=0; i<readVarCount; i++ ) {
                    for (int j=0; j<readVarCount; j++ ) {
                        for (int k=_minMarkovLag; k<_maxMarkovLag+1; k++ ) {

                            matrix[ i*offsetVariables + k*offsetLags + j ] = 0;
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
                // 19  0:   0                 1:   1 18
                for (int i=0; i<readVarCount; i++ ) {
                    
                    currentNodeID = Integer.parseInt( strTok.nextToken() );
                    checkStructureString += "\n" + currentNodeID + " ";
                
                    for (int k=0; k<_maxMarkovLag+1; k++) {
                        
                        strMarkovLag = strTok.nextToken();
                        // Split off the ":" that is part of the lag (=MarkovLag)
                        strMarkovLag = strMarkovLag.substring( 
                                0, strMarkovLag.length()-1 ); 
                        markovLag = Integer.parseInt( strMarkovLag );
                        checkStructureString += "  " + markovLag + ": ";
                        
                        // Get the parent count for this lag
                        intParentCount = Integer.parseInt( strTok.nextToken() );
                        checkStructureString += intParentCount + " ";
                        
                        for (int j=0; j<intParentCount; j++) {
                        
                            parentNodeID = Integer.parseInt( strTok.nextToken() );
                            checkStructureString += parentNodeID + " ";

                            matrix[ currentNodeID*offsetVariables + 
                                    markovLag*offsetLags + parentNodeID ] = 1;
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
                    "(EdgesAsArrayWithCachedStatistics.reconstructMatrix) -- \n  " +
                    "Development issue: " + 
                    "could not create parent matrix from bayesNetManager structure\n" + 
                    _bayesNetStructureString );
        }       
    }

    // For static bayesNets only:
    public void reconstructMatrixStatic( 
            final String _bayesNetStructureString, 
            final int _varCount, 
            final int _minMarkovLag, 
            final int _maxMarkovLag ) throws BanjoException {
        
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
                            "(EdgesAsArrayWithCachedStatistics.reconstructMatrixStatic) " +
                            "Development issue: " + 
                            "The read-in var count doesn't match the internal parameter.");
                }
                
                for (int i=0; i<readVarCount; i++ ) {
                    for (int j=0; j<readVarCount; j++ ) {

                        matrix[ i*offsetVariables + j ] = 0;
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

                        matrix[ currentNodeID*offsetVariables + parentNodeID ] = 1;
                        parentCount[currentNodeID] ++;
                        combinedParentCount++;
                    }   
                }
            }
            else {

                // Developer message only:
                throw new BanjoException( BANJO.ERROR_BANJO_DEV, 
                        "(EdgesAsArrayWithCachedStatistics.reconstructMatrixStatic) " +
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
                    "(EdgesAsArrayWithCachedStatistics.reconstructMatrixStatic) " +
                    "Development issue: " +
                    "could not create parent matrix from bayesNetManager structure " +
                    "as provided:\n '" +
                    _bayesNetStructureString + "'" );
        }
    }
    
    public void setToCombinedMatrices( 
            EdgesWithCachedStatisticsI _edges1, 
            EdgesWithCachedStatisticsI _edges2 )
            throws Exception {

        EdgesAsArrayWithCachedStatistics edgesAsArray1;
        EdgesAsArrayWithCachedStatistics edgesAsArray2;
        
        if ( _edges1 instanceof EdgesAsArrayWithCachedStatistics && 
                _edges2 instanceof EdgesAsArrayWithCachedStatistics ) {

            edgesAsArray1 = ( EdgesAsArrayWithCachedStatistics ) _edges1;
            edgesAsArray2 = ( EdgesAsArrayWithCachedStatistics ) _edges2;
            
            int combinedEntry;
            
            combinedParentCount = 0;
            int[] tmpEdgesAsArray1 = edgesAsArray1.getMatrix();
            int[] tmpEdgesAsArray2 = edgesAsArray2.getMatrix();
            
            for (int i=0; i<varCount; i++) {
    
                parentCount[i] = 0;
                
                // Set the matrix entries
                for (int j=0; j<varCount; j++) {
                    for (int k=0; k<maxMarkovLag+1-minMarkovLag; k++) {
    
                        combinedEntry = tmpEdgesAsArray1[ i*offsetVariables + k*offsetLags + j ] + 
                            tmpEdgesAsArray2[ i*offsetVariables + k*offsetLags + j ];
                        
                        if ( combinedEntry >= 1 ) {
    
                            matrix[ i*offsetVariables + k*offsetLags + j ] = 1;
                            parentCount[i]++;
                            combinedParentCount++;
                        }
                        else {
    
                            matrix[ i*offsetVariables + k*offsetLags + j ] = 0;
                        }
                    }
                }
            }
        }
        else {
            
            // dev error: we should never end up here
            throw new BanjoException(BANJO.ERROR_BANJO_DEV, 
                    "(" + StringUtil.getClassName( this ) + ".setToCombinedMatrices) " +
                    "Development issue: Arguments are of unexpected type (" +
                    "need: 'EdgesAsArrayWithCachedStatistics')" );
        }
    }
    
    // Check if a matrix overlaps any entry with another matrix (typically used
    // to verify that the "mustBePresent" and "mustBeAbsent" matrices are
    // properly specified by the user)
    public boolean hasOverlap( final EdgesWithCachedStatisticsI _edges ) throws Exception {

        boolean hasOverlap = false;
        
        EdgesAsArrayWithCachedStatistics edgesAsArray;
        if ( _edges instanceof EdgesAsArrayWithCachedStatistics ) {
            
            edgesAsArray = ( EdgesAsArrayWithCachedStatistics ) _edges;
     
            int[] tmpEdgesAsArray = edgesAsArray.getMatrix();
            
            for (int i=0; i<varCount; i++) {
                for (int j=0; j<varCount; j++) {
                    for (int k=0; k<maxMarkovLag+1-minMarkovLag; k++) {
     
                        if ( this.matrix[ i*offsetVariables + k*offsetLags + j ] 
                             + tmpEdgesAsArray[ i*offsetVariables + k*offsetLags + j ] == 2 ) { 
                            
                            hasOverlap = true;
                            break;
                        }                   
                    }
                }
            }
        }
        else {
            
            // dev error: we should never end up here
            throw new BanjoException( BANJO.ERROR_BANJO_DEV, 
                    "(" + StringUtil.getClassName( this ) + ".hasOverlap) " +
                    "Development issue: Arguments are of unexpected type (" +
                    "need: 'EdgesAsArrayWithCachedStatistics')" );
        }
        
        return hasOverlap;
    }
    
    // This is a special method that computes a "complementary" adjacency
    // matrix (relative to both supplied arguments)
    public void computeComplementaryMatrix(
            final EdgesWithCachedStatisticsI _edges1,
            final EdgesWithCachedStatisticsI _edges2 ) 
            throws Exception {

        EdgesAsArrayWithCachedStatistics edgesAsArray1;
        EdgesAsArrayWithCachedStatistics edgesAsArray2;
        
        if ( _edges1 instanceof EdgesAsArrayWithCachedStatistics && 
                _edges2 instanceof EdgesAsArrayWithCachedStatistics ) {

            edgesAsArray1 = ( EdgesAsArrayWithCachedStatistics ) _edges1;
            edgesAsArray2 = ( EdgesAsArrayWithCachedStatistics ) _edges2;
                
            this.combinedParentCount = 0;
            for (int i=0; i<varCount; i++) {
                
                // Set all parentCounts to 0
                this.parentCount[i] = 0;
    
                int[] tmpEdgesAsArray1 = edgesAsArray1.getMatrix();
                int[] tmpEdgesAsArray2 = edgesAsArray2.getMatrix();
                
                for (int j=0; j<varCount; j++) {
                    for (int k=0; k<maxMarkovLag+1-minMarkovLag; k++) {
                        
                        // Check the value of:
                        // 1 - matrix1.matrix[ i*offsetVariables + k*offsetLags + j ] - 
                        //      matrix2.matrix[ i*offsetVariables + k*offsetLags + j ]
                        if ( tmpEdgesAsArray1[ i*offsetVariables + k*offsetLags + j ] == 1 || 
                                tmpEdgesAsArray2[ i*offsetVariables + k*offsetLags + j ] == 1 ) {
    
                            this.matrix[ i*offsetVariables + k*offsetLags + j ] = 0;
                        }
                        else {
    
                            this.matrix[ i*offsetVariables + k*offsetLags + j ] = 1;
                            this.parentCount[i]++;
                            this.combinedParentCount++;
                        }
                    }
                }
            }
        }
        else {
            
            // dev error: we should never end up here
            throw new BanjoException(BANJO.ERROR_BANJO_DEV, 
                    "(" + StringUtil.getClassName( this ) + ".computeComplementaryMatrix) " +
                    "Development issue: Arguments are of unexpected type (" +
                    "need: 'EdgesAsArrayWithCachedStatistics')" );
        }
    }
        
    // Computes the list of parent ID's for the current node "nodeID"
    // "Overload" for DBN's: Note that the second argument is actually redundant,
    // but comes in handy as a visual reminder that we are in the DBN case
    // (the parent lists are only computed for "current" nodes (i.e. the lag is
    // always 0)
    public int[][] getCurrentParentIDlist( final int _nodeID, final int _lag ) {
        
        int[][] parentIDlist;
        int intParentCount = parentCount[_nodeID];
        parentIDlist = new int[intParentCount][2];
        
        int parentIndex = 0;
        
        // Go through the parent nodes for the node with id=nodeID
        for (int i=0; i<varCount && parentIndex < intParentCount; i++) {
            for (int k=0; k<maxMarkovLag+1-minMarkovLag; k++) {

                if ( matrix[ _nodeID*offsetVariables + k*offsetLags + i ] == 1 ) {
                    
                    // Per convention, [index][0] holds the ID of a node
                    parentIDlist[parentIndex][0] = i;
                    
                    // and [index][1] holds the lag
                    parentIDlist[parentIndex][1] = k + minMarkovLag;
                    parentIndex++;
                }
            }
        }
        
        return parentIDlist;
    }
        
    // Get the number of parents for the variable with given index (var ID)
    public int parentCount(final int _varIndex) {
        return parentCount[ _varIndex ];
    }
    
    // Query if a variable has another variable as a parent
    public boolean isParent(
            final int _varIndex, 
            final int _parentVarIndex, 
            final int _parentLag ) {

        return ( matrix[ _varIndex*offsetVariables + 
                        ( _parentLag-minMarkovLag )*offsetLags + _parentVarIndex ] == 1 );
    }

    // Set any individual entry in the parent matrix to 1
    public void setEntry(
            final int _varIndex, 
            final int _parentVarIndex, 
            final int _lagIndex ) throws Exception {

        // Adjust for our compact memory representation:
        final int lagIndex = _lagIndex - minMarkovLag;
        final int mappedIndex = _varIndex*offsetVariables + lagIndex*offsetLags + _parentVarIndex;
        
        // need to check if value is already 1 (this should not be
        // the case, but better to be safe)
        if ( matrix[ mappedIndex ] == 0 ){
            
            parentCount[ _varIndex ] ++;
            combinedParentCount++;
        }
        else { 
                        
            if ( BANJO.APPLICATIONSTATUS == BANJO.APPLICATIONSTATUS_LOADINGDATA ) {
                
                // When we load data, the user may try to enter the same parent data twice
                throw new BanjoException(BANJO.ERROR_BANJO_USERINPUT, 
                        "(EdgesAsArrayWithCachedStatistics.setEntry) " +
                        "Data entry issue: the variable " +
                        _varIndex + " already has the parent " +
                        "(variable=" + _parentVarIndex + ", lag=" + _lagIndex + 
                        ") assigned. " +
                        "Are any dbnMandatoryIdentityLags used in addition to explicit parent lists?" );
            }
            else {
                
                // This case should never occur in the flow of the appplication,
                // so throw an error if we ever end up here:
                throw new BanjoException(BANJO.ERROR_BANJO_DEV, 
                        "(EdgesAsArrayWithCachedStatistics.setEntry) " +
                        "Development issue: Expected a parent value of 0, but found m(node=" +
                        _varIndex + ", parent=" + _parentVarIndex + ", lag=" + lagIndex + ") = " +
                        matrix[ mappedIndex ] + "." );
            }
        }
        
        // Now make the parent assignment
        matrix[ mappedIndex ] = 1;
    }

    // Set an individual entry in the parent matrix
    // This is a convenient method when merging sets of parent nodes. 
    public void setEntry(
            final int _varIndex, 
            final int _parentVarIndex, 
            final int _lagIndex, 
            final int _newValue ) throws Exception {
        
        // It is the calling routine's task to assure that we only get 0 or 1
        // as values! Otherwise we complain by throwing an Exception
        
        // Adjust for our compact memory representation:
        final int lagIndex = _lagIndex - minMarkovLag;
        final int mappedIndex = _varIndex*offsetVariables + lagIndex*offsetLags + _parentVarIndex;
        
        // Update the adjaceny matrix
        int oldValue = matrix[ mappedIndex ];
        matrix[ mappedIndex ] = _newValue;
        
        // Update the parentCount only when the value changes
        if ( _newValue == 1 && oldValue == 0 ) {
            
            parentCount[ _varIndex ] ++;
            combinedParentCount++;
        }
        else if ( _newValue == 0 && oldValue == 1 ) {
            
            parentCount[ _varIndex ] --;
            combinedParentCount--;
        }
        // Note: this case can happen, e.g., in the initializing of a matrix (assigning
        // a 0 value to a value that is already 0), but we assert here that the calling
        // routine takes care of this!!
        else {

            throw new BanjoException( BANJO.ERROR_BANJO_DEV,
                    "(" + StringUtil.getClassName( this ) + ".setEntry) " +
                    "Development issue: The old and the new value of the parent set for var=" +
                    _varIndex + ", parent=" + _parentVarIndex + ", lag=" + lagIndex + ") = " +
                    matrix[ mappedIndex ] + 
                    "; this should never happen, " +
                    "and needs to be corrected by the developer!" );
        }           
    }
    
    public void addParent(
            final int _varIndex, 
            final int _parentVarIndex,
            final int _lagIndex ) throws Exception {

        // Adjust for our compact memory representation:
        final int lagIndex = _lagIndex - minMarkovLag;
        final int mappedIndex = _varIndex*offsetVariables + lagIndex*offsetLags + _parentVarIndex;
         
        // Note that we throw an exception if the current value of the matrix indicates
        // that there is already a parent
        
        if ( matrix[ mappedIndex ] == 0 ) {

            matrix[ mappedIndex ] = 1;
            parentCount[ _varIndex ] ++;
            combinedParentCount++;
        }
        else {
            
            // this should never occur:
            throw new BanjoException(BANJO.ERROR_BANJO_DEV,
                    "(" + StringUtil.getClassName( this ) + ".AddParent) " +
                    "Development issue: Error in adding parent: \n" +
                    "Parent id=" + _parentVarIndex + ", lag=" + lagIndex +
                    " is already a parent of " +
                    "id=" + _varIndex + 
                    " in parent set \n" +
                    this.toString() );
        }
    }
    
    public void deleteParent(
            final int _varIndex, 
            final int _parentVarIndex, 
            final int _lagIndex ) throws Exception {

        // Adjust for our compact memory representation:
        int lagIndex = _lagIndex - minMarkovLag;
        int mappedIndex = _varIndex*offsetVariables + lagIndex*offsetLags + _parentVarIndex;

        if ( matrix[ mappedIndex ] == 1 ) {

            matrix[ mappedIndex ] = 0;
            parentCount[ _varIndex ]--;
            combinedParentCount--;
        }
        else {
            
            // this should never occur:
            throw new BanjoException(BANJO.ERROR_BANJO_DEV,
                    "(" + StringUtil.getClassName( this ) + ".DeleteParent) " +
                    "Development issue: Error in deleting parent: \n" +
                    "Node id=" + _varIndex + " does not have parent " +
                    "id=" + _parentVarIndex + ", lag=" + lagIndex +
                    " in parent set \n" +
                    this.toString() );
        }
    }
        
    public void reverseRelation(
            final int _nodeIndex,
            final int _nodeLagIndex,
            final int _parentIndex,
            final int _parentLagIndex ) throws Exception {

        // Check that the lags are the same:
        // (they have to be in the same time slice for this to make sense)
        
        if ( _nodeLagIndex != _parentLagIndex && ( _nodeLagIndex != 0 || _parentLagIndex != 0 ) ) {
            
            throw new BanjoException(
                    BANJO.ERROR_BANJO_DEV,
                    "(EdgesAsArrayWithCachedStatistics.reverseRelation) " +
                    "Development issue: " +
                    "Can only reverse an edge between nodes of lag 0.");
        }
        
        // Remove the parent for the specified node
        if (( matrix[ _nodeIndex*offsetVariables + _parentLagIndex*offsetLags + _parentIndex] == 0 ) 
                || ( matrix[ _parentIndex*offsetVariables + _nodeLagIndex*offsetLags + _nodeIndex ] == 1 )) {

            // throw an exception: the data is not consistent with 
            // the requested operation
            throw new BanjoException(
                BANJO.ERROR_BANJO_DEV,
                "(EdgesAsArrayWithCachedStatistics.reverseRelation) " +
                "Development issue: " +
                "Encountered inconsistent data when trying to reverse an edge.");
        }
            
        // First remove the <edge from the node to the parent>
        matrix[ _nodeIndex*offsetVariables + _parentLagIndex*offsetLags + _parentIndex ] = 0;
        parentCount[_nodeIndex] --;
        
        // then add the <edge from the parent to the node>
        matrix[ _parentIndex*offsetVariables + _nodeLagIndex*offsetLags + _nodeIndex ] = 1;
        parentCount[_parentIndex] ++;
        
        // Note: of course we don't have to update the grand total in this case
    }

    // This code is needed to be handle reversals when Shmueli's cycle checking "shortcuts"
    // are employed.
    public void resetEntireGraph() throws Exception {

        cycleFinder.resetEntireGraph();
    }

    // This code is needed to be handle reversals when Shmueli's cycle checking "shortcuts"
    // are employed.
    public void adjustForLocalChange( final int _nodeToCheck ) throws Exception {

        cycleFinder.adjustForLocalChange( _nodeToCheck );
    }
    
    // Core cycle checking method
    public boolean isCyclic( final BayesNetChangeI _bayesNetChange ) 
            throws Exception {

        int nodeToCheck = -1;
        int parentNodeToCheck = -1;
        int currentNodeLevel;
        boolean cycleFound = false;
        
        // We always check for a cycle (only) for the node that has an edge added
        // (as described in the bayesNetChange)
        if ( _bayesNetChange.getChangeType() == 
                BANJO.CHANGETYPE_ADDITION ) {
            
            nodeToCheck = _bayesNetChange.getCurrentNodeID();
            parentNodeToCheck = _bayesNetChange.getParentNodeID();
            
            if ( BANJO.DEBUG && BANJO.TRACE_DFSNEW ) {
            
                System.out.println( "\n\nAdding  " + nodeToCheck + "<--" + parentNodeToCheck );
            }
        }
        else if ( _bayesNetChange.getChangeType() == 
            BANJO.CHANGETYPE_REVERSAL ) {
            
            nodeToCheck = _bayesNetChange.getParentNodeID();
            parentNodeToCheck = _bayesNetChange.getCurrentNodeID();

            if ( BANJO.DEBUG && BANJO.TRACE_DFSNEW ) {
            
                System.out.println( "\nReversing  " + parentNodeToCheck + "<--" + nodeToCheck +
                    " to " + nodeToCheck + "<--" + parentNodeToCheck );
            }
        }
        else if ( _bayesNetChange.getChangeType() == 
            BANJO.CHANGETYPE_DELETION ) {
            
            nodeToCheck = _bayesNetChange.getCurrentNodeID();
            parentNodeToCheck = _bayesNetChange.getParentNodeID();
            
            if ( BANJO.DEBUG && BANJO.TRACE_DFSNEW ) {
            
                System.out.println( "\n\nDeleting  " + _bayesNetChange.getCurrentNodeID() +
                    "<--" + _bayesNetChange.getParentNodeID() +
                    "\nStructure:\n" +
                    this.toString() );
            }
            
            // this should never occur:
            throw new BanjoException( BANJO.ERROR_BANJO_DEV,
                    "(EdgesAsArrayWithCachedStatistics.isCyclicDFS) " +
                    "Development issue: " +
                    "encountered non-addition/non-reversal BayesNetChange " +
                    "in cycle checking." );
        }
        else {
            
            // this should never occur in version 2.0 either
            throw new BanjoException( BANJO.ERROR_BANJO_DEV,
                    "(EdgesAsArrayWithCachedStatistics.isCyclicDFS) " +
                    "Development issue: " +
                    "encountered BayesNetChange in cycle checking" +
                    "without specified 'type'. Cannot proceed further." );
        }


        // Only need this for Dev (unless we keep the DFSorig/DFSorigForCrossCheck code around)
        bayesNetChange = _bayesNetChange;
        
        return cycleFinder.isCyclic( nodeToCheck, parentNodeToCheck );
    }

    protected boolean DFSorigForCrossCheck( 
            final int nodeToCheck, 
            final int parentNodeToCheck, 
            final BayesNetChangeI _bayesNetChange ) throws Exception {

        int[] previousNodeLevel = new int[ varCount ];
        int[] lastNodeProcessed = new int[ varCount ];
        boolean[] _ancestorSet =  new boolean[ varCount ];
        
        try {

            int currentNodeLevel;
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

                    currentEntry = matrix[ 
                            currentNodeLevel*offsetVariables + nextParentToProcess ];
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
            
            System.out.print( "(EdgesAsArrayWithCachedStatistics.isCyclicDFS) " +
                    "Out of memory.");
            
            // Also: would need to check in the calling code for this return value
            e.printStackTrace();
            
            throw new BanjoException( BANJO.ERROR_BANJO_OUTOFMEMORY,
                    "Out of memory in (" +
                    StringUtil.getClassName(this) +
                    ".isCyclicDFS)" );
        }
        catch (Exception e) {

            throw new BanjoException( e, 
                    BANJO.ERROR_BANJO_DEV,
                    "\n(EdgesAsArrayWithCachedStatistics.isCyclicDFS) - " +
                    "Development issue: Encountered an error " +
                    "while checking for cycles based on bayesNetChange='" +
                    _bayesNetChange.toString() + "'.");
        }

        return _ancestorSet[ nodeToCheck ];
    } // end isCyclicDFS
    
    
    // Check if a node set represents a cyclic graph via a depth-first search:
    public boolean isCyclicDFSorig( final BayesNetChangeI _bayesNetChange ) throws Exception {
        
        int nodeToCheck = -1;
        int parentNodeToCheck = -1;
        int currentNodeLevel;
        int[] previousNodeLevel = new int[ varCount ];
        int[] lastNodeProcessed = new int[ varCount ];
        boolean[] _ancestorSet =  new boolean[ varCount ];
        
        try {
            
            // We always check for a cycle (only) for the node that has an edge added
            // (as described in the bayesNetChange)
            if ( _bayesNetChange.getChangeType() == 
                    BANJO.CHANGETYPE_ADDITION ) {
                
                nodeToCheck = _bayesNetChange.getCurrentNodeID();
                parentNodeToCheck = _bayesNetChange.getParentNodeID();
            }
            else if ( _bayesNetChange.getChangeType() == 
                BANJO.CHANGETYPE_REVERSAL ) {
                
                nodeToCheck = _bayesNetChange.getParentNodeID();
                parentNodeToCheck = _bayesNetChange.getCurrentNodeID();
            }
            else {
                
                // this should never occur:
                throw new BanjoException(BANJO.ERROR_BANJO_DEV,
                        "(EdgesAsArrayWithCachedStatistics.isCyclicDFS) " +
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

                    currentEntry = matrix[ 
                            currentNodeLevel*offsetVariables + nextParentToProcess ];
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
            
            System.out.print( "(EdgesAsArrayWithCachedStatistics.isCyclicDFS) " +
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
                    "\n(EdgesAsArrayWithCachedStatistics.isCyclicDFS) - " +
                    "Development issue: Encountered an error " +
                    "while checking for cycles based on bayesNetChange='" +
                    _bayesNetChange.toString() + "'.");
        }

        return _ancestorSet[ nodeToCheck ];
    } // end isCyclicDFS
            
    // Check an entire parentMatrix (e.g., the mandatory parents set)
    public boolean isCyclic() throws Exception {        
        
        // Note that we assume that the dimension of the nodeSetToCheck
        // is based on the same varCount that defines the underlying bayesnet
        boolean cycleFound = false;
        
        cycleAtNode = -1;
        
        // Check for each node that it is not contained in a cycle
        int i = 0;
        while (i<varCount && !cycleFound) {
            
            for (int j=0; j<varCount; j++) {

                parentIDs[j] = matrix[ i*offsetVariables + j ];
            }
            cycleFound = isCyclic(i, parentIDs);
            i++;
        }
        
        return cycleFound;
    }
    
    // Check if a node set represents a cyclic graph, starting from the node with
    // ID=nodeID, and with a set of parents "parentNodeIDs":
    public boolean isCyclic(
            final int nodeID, 
            final int[] parentNodeIDs ) {
            
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
                    
                    currentEntry = matrix[ parentNodeID*offsetVariables + j ];
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
    

    public boolean hasIdenticalEntries( final EdgesI _otherMatrix ) {

        if ( _otherMatrix instanceof EdgesAsArrayWithCachedStatistics ) {
            
            // Define a matrix as performance shortcut:
            int[] tmpMatrix = (( EdgesAsArray ) _otherMatrix ).getMatrix();
            
            if ( ( (EdgesAsArrayWithCachedStatistics) _otherMatrix).getCombinedParentCount() !=
                this.getCombinedParentCount() ) {
                
                return false;
            }
            
            for (int i=0; i<varCount; i++) {
                
                if ( ( (EdgesAsArrayWithCachedStatistics) _otherMatrix).parentCount[i] !=
                    this.parentCount[i] ) {
                    
                    return false;
                }
            }

            return hasIdenticalEntries( tmpMatrix ); 
        }
        else {
            
            // Can't throw exception here, so do the next best thing:
            
        }
        
        return true;
    }
    
    public void omitNodesAsOwnParents() {
        
        // Using the fact that a node cannot be a parent to itself,
        // this method sets the (i,i) entries of a matrix to 1 (i.e.,
        // this is used primarily for the mustBeAbsentParents set)
        for (int i=0; i< varCount; i++) {

            matrix[ i*offsetVariables + i ] = 1;
        }
    }
    
    public void omitExcludedParents() {
        
        // Note: our implementation only considers lags from minMarkovLag to
        // maxMarkovLag, so there is nothing to do here (anymore).
        
    }

    /**
     * @return Returns the parentCount.
     */
    public int[] getParentCount() {
        return parentCount;
    }
    
    public int getParentCount(int nodeID) {
        return parentCount[nodeID];
    }
    
    /**
     * @return Returns the cycleAtNode.
     */
    public int getCycleAtNode() {
        return cycleAtNode;
    }

    // For testing/tracing only:
    // Print the entire matrix as a string, formatted with various delimiters,
    // and the parentCount at the end.
    // (Can't move this method to superclass, because parentIDs are native to 
    // EdgesAsArrayWithCachedStatistics)
    public StringBuffer toStringWithIDandParentCount() {
        
        StringBuffer localStringBuffer = new StringBuffer( 
                BANJO.BUFFERLENGTH_STRUCTURE );
        localStringBuffer.append( "    " );
        // Add header: (need to make formatting more generic, but ok for quick test)
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
                for (int k=0; k<maxMarkovLag+1-minMarkovLag; k++) {

                    localStringBuffer.append( matrix[ i*offsetVariables + k*offsetLags + j ] );
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
    
    /**
     * @return Returns the combinedParentCount.
     */
    public int getCombinedParentCount() {
        return combinedParentCount;
    }
}

