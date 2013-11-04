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

package edu.duke.cs.banjo.application;

import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;

import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.learner.components.EquivalenceCheckerBasic;
import edu.duke.cs.banjo.learner.components.EquivalenceCheckerI;
import edu.duke.cs.banjo.learner.components.EquivalenceCheckerSkip;
import edu.duke.cs.banjo.utility.BANJO;
import edu.duke.cs.banjo.utility.BanjoErrorHandler;
import edu.duke.cs.banjo.utility.BanjoException;
import edu.duke.cs.banjo.utility.XMLProcessor;
import edu.duke.cs.banjo.utility.PostProcessor;
import edu.duke.cs.banjo.utility.StringUtil;
import edu.duke.cs.banjo.utility.NBestProcessor;

/**
 * Provides a simple interface to running the Banjo application in a multi-threaded scenario.
 * (Similar to the "old" Banjo class)
 *
 * <p><strong>Details:</strong> <br>
 * Provides an application entry point for running the application
 * from the commandline, using multiple threads. <br>
 * (Optional) arguments: <br>
 * 1. The name of the settings file. <br>
 * 2. The number of threads. <br>
 * 3. The name of the directory where the settings file is found (defaults
 * to the application's (current) directory).
 * 
 * <p><strong>Change History:</strong> <br>
 * Created on Mar 3, 2004
 * 
 * <p>
 * 9/10/2007 (v2.1) hjs <br>
 *      Code handles shared memory for the fastCache and the log-Gamma table.
 *      
 * <p>
 * (2.2) hjs        Add code to check if we found problems during initial validation 
 *                  of various settings.
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */

public class Banjo {

    protected int maxThreads = 1;
    protected int threadCounter = 0;
    protected Object BanjoInstance[];
    protected String[] sharedArgs;
    protected Settings settings;
    // Once we start the search threads, they validate the main set of parameters,
    // which we need to reference if we want to use them in this class
    protected Settings settingsUsedInThread;
    
    public Banjo() throws Exception {

        // nothing to do here for now
    }

    private class SearchInstance implements Runnable {

        protected int threadIndex;
        
        public SearchInstance () throws Exception {
            
            threadIndex = ++threadCounter;
        }
        
        // obligatory "run " method for each thread
        public void run() {

            (( BanjoThreadHandler ) BanjoInstance[ threadIndex-1 ]).runSearch();
        }
    }
    
    public void execute( String _args[] ) throws Exception {

        BanjoErrorHandler errorHandler = new BanjoErrorHandler();
        XMLProcessor xmlProcessor;
        
        try {
            
            String[] fileList;
            
            // Set up the settings (processData) instance
            settings = new Settings();
            // Update the error handler to know about settings
            errorHandler = new BanjoErrorHandler( settings );
            // Load and validate the parameters for running the application 
            settings.processCommandLine( _args );

            // Get the (optional) list of XML files
            fileList = settings.validateXMLresultFiles();
            
            // If XML files are specified, we go into "result harvest" mode
            if ( fileList.length > 0 && !fileList[0].equalsIgnoreCase("") ) {
                
                // Process and combine the set of files with XML-formatted data                
                xmlProcessor = new XMLProcessor( settings );
                xmlProcessor.processXMLResultFiles( fileList );
            }
            else {
                //----------------------
                //    "search" mode
                //----------------------
        
                // load the observations (stored in the settings for shared use)
                settings.loadObservations();

                // hjs (v2.2) While validation of thread-related settings was preformed
                // in version 2.1, we still need to make sure that we didn't run into 
                // any problems
                if ( settings.wereThereProblems() ) {
                    
                    throw new BanjoException( BANJO.ERROR_CHECKPOINTTRIGGER,
                            "(Checkpoint) Banjo performed a set of validation checks, " +
                            "and discovered the following issues " +
                            "which prevented further program execution:" + 
                            BANJO.FEEDBACK_NEWLINE +
                            settings.compileErrorMessages().toString() );
                }
                
                // Find out how many threads to use
                maxThreads = Integer.parseInt( 
                        settings.getDynamicProcessParameter( BANJO.DATA_MAXTHREADS ));
            
                if ( maxThreads > 0 ) {
                    
                    runMultipleInstances( _args );
                }
            }
        }
        
        // ---------------------------------------
        // Handle any exception that might have occurred
        // ---------------------------------------
        catch ( final BanjoException e ) {
            
            errorHandler.handleApplicationException( e, settings );
        }
        catch ( OutOfMemoryError e ) {
                                    
            errorHandler.handleOutOfMemoryError( e, settings );
        }
        catch ( Exception e ) {
            
            errorHandler.handleGeneralException( e, settings );
        }
    }
    
    protected void runMultipleInstances( String _args[] ) throws Exception {

        NBestProcessor nBestProcessor;
        
        try {
        
            Thread arrThreads[];
            EquivalenceCheckerI equivalenceChecker;
            PostProcessor postProcessor;
            TreeSet highScoreStructureSet;
            String adjustedReportFileName;
            String loadedReportFileName;
            int lineLength = BANJO.FEEDBACK_LINELENGTH;
            String dashedLine = BANJO.FEEDBACK_DASHEDLINE.substring( 0, lineLength-1 ); 
            
            Runtime.getRuntime().gc();
            
            BanjoInstance = new Object[ maxThreads ];
            
            sharedArgs = _args;
       
            long startTime = System.currentTimeMillis();
            
            // Set up ..
            arrThreads = new Thread[ maxThreads ];
            for (int i=0; i < maxThreads; i++) {
        
                BanjoInstance[ i ] = 
                    new BanjoThreadHandler( sharedArgs, i, settings );
                Runtime.getRuntime().gc();
                
                arrThreads[ i ] = new Thread( new SearchInstance() );
                Runtime.getRuntime().gc();
            }

            // and run the threads (we may put some code inbetween the setup and the
            // search execution of the threads that checks on problems)
            for (int i=0; i < maxThreads; i++) {
        
                arrThreads[ i ].setName( BANJO.DATA_THREADNAME + (i+1) );
                arrThreads[ i ].start();
                Runtime.getRuntime().gc();
            }
            
            // Wait until all threads are done
            for ( int i=0; i < maxThreads; i++ ) {
                
                try {
                  arrThreads[i].join();
                }
                catch (InterruptedException e) {
    
                    throw new BanjoException( e,
                            BANJO.ERROR_BANJO_DEV, 
                            "Join (of threads after completion) interrupted" );
                }
            }
    
            // "Re-import" the settings that were used in the first thread (note: down the 
            // road it may not hold true that all the threads have very similar settings,
            // but it is the case for now)
            settingsUsedInThread = ((BanjoThreadHandler) BanjoInstance[ 0 ]).getSettings();
            
            nBestProcessor = new NBestProcessor( settingsUsedInThread );
            
            // set up the final results set
            highScoreStructureSet = new TreeSet();
            
            // set up an equivalence checker to be able to compare networks
            if ( settings.getValidatedProcessParameter( BANJO.SETTING_BESTNETWORKSARE )
                    .equalsIgnoreCase ( BANJO.UI_BESTNETWORKSARE_NONEQUIVALENT ) || 
                 settings.getValidatedProcessParameter( BANJO.SETTING_BESTNETWORKSARE )
                    .equalsIgnoreCase( BANJO.UI_BESTNETWORKSARE_NONIDENTICALTHENPRUNED ) ) {
    
                equivalenceChecker = new EquivalenceCheckerBasic( settings );
            }
            else {
    
                equivalenceChecker = new EquivalenceCheckerSkip( settings );
            }
    
            // set up access to cycle-checker data (note: the "global" settings object
            // does not know about many of the "internal" settings choices, so we need
            // to pass some info along from one of the threads [we pick the first one])
            settings.setDynamicProcessParameter( BANJO.CONFIG_CYCLECHECKER_METHOD,
                    (( BanjoThreadHandler ) BanjoInstance[ 0 ])
                    .getSettings().getDynamicProcessParameter( 
                            BANJO.CONFIG_CYCLECHECKER_METHOD ));
    
            settings.setDynamicProcessParameter( BANJO.CONFIG_CYCLECHECKER_ACTIONORDER,
                    (( BanjoThreadHandler ) BanjoInstance[ 0 ])
                    .getSettings().getDynamicProcessParameter( 
                            BANJO.CONFIG_CYCLECHECKER_ACTIONORDER ));


            // Set up a collection used for directing the results printing
            Collection outputResultsOnly = new HashSet(
                    BANJO.MAXOUTPUTFILES );
            outputResultsOnly.add( new Integer(BANJO.FILE_RESULTS) );
            
            // 
            Collection xmlResults = new HashSet( BANJO.MAXOUTPUTFILES );
            xmlResults.add( new Integer(BANJO.FILE_RESULTSXML) );
            StringBuffer nBestNetworks = new StringBuffer();
            
            // --------------------
            // Compute the combined set of n-best networks
            // --------------------
            if ( maxThreads < 2 ) {
    
                try {
                
                    // For a single thread, simply get the set of networks
                    int threadID = 1;
                    highScoreStructureSet = nBestProcessor.foldIntoHighScoreSet( 
                        highScoreStructureSet,
                        (( BanjoThreadHandler ) BanjoInstance[ 0 ]).
                            getSettings().getHighScoreStructureSet(),
                        equivalenceChecker, threadID );
                    
                    settings.setHighScoreStructureSet( highScoreStructureSet );
        
                    nBestNetworks.append( nBestProcessor.listNBestNetworks( highScoreStructureSet ));
                }
                catch ( BanjoException e ) { 

                    throw new BanjoException( e );
                }
                catch ( Exception e ) { 

                    throw new BanjoException( e,
                            BANJO.ERROR_BANJO_DEV, "Error while collecting the " +
                            "results from the search (produced by a single thread)." );
                }
            }
            else {
                
                // For multiple threads, will need to combine the individual result sets                
                try {
                
                    // This duplicates the generic search info (without the search-specific
                    // parameters) for the final user feedback (note that there may have been 
                    // a lot of feedback generated since the start of the search)
//                    nBestNetworks.append( settings.getOptionalThreadInfo() );
                    nBestNetworks.append( BANJO.FEEDBACK_NEWLINE );
                    nBestNetworks.append( dashedLine );
                    nBestNetworks.append( StringUtil.getBanjoSignature() );
                    nBestNetworks.append( BANJO.FEEDBACK_NEWLINE );
                    nBestNetworks.append( dashedLine );
                    nBestNetworks.append( StringUtil.getJobSignature( settings ) );
                    nBestNetworks.append( BANJO.FEEDBACK_NEWLINE );
                    nBestNetworks.append( dashedLine );
                    
                    for ( int i=0; i < maxThreads; i++ ) {
                        
                        if ( BANJO.DEBUG && BANJO.TRACE_COMBINENBEST ) {
                            
                            nBestNetworks.append( 
                                    "\nN-best networks from thread " + (i+1) + ":\n" );
                            nBestNetworks.append( nBestProcessor.listNBestNetworks( 
                                    (( BanjoThreadHandler ) BanjoInstance[ i ])
                                        .getSettings().getHighScoreStructureSet() ));
                        }
                        
                        highScoreStructureSet = nBestProcessor.foldIntoHighScoreSet(
                            highScoreStructureSet,
                            (( BanjoThreadHandler ) 
                                    BanjoInstance[ i ]).getSettings().getHighScoreStructureSet(),
                            equivalenceChecker, (i+1) );
        
                        if ( BANJO.DEBUG && BANJO.TRACE_COMBINENBEST ) {
                            
                            nBestNetworks.append( "\n\n*** " );
                            nBestNetworks.append( 
                                    "\n*** N-best networks after adding results from thread " 
                                    + (i+1) + ":" );
                            nBestNetworks.append( "\n*** \n" );
                            nBestNetworks.append( nBestProcessor.listNBestNetworks( 
                                    highScoreStructureSet ));
                            nBestNetworks.append( "\n" );
                            
                            settings.writeToFile( outputResultsOnly , nBestNetworks );
                            nBestNetworks = new StringBuffer();
                        }
                    }
                    
                    if ( BANJO.DEBUG && BANJO.TRACE_COMBINENBEST ) {
                        
                        nBestNetworks.append( "\n\n*** *** " );
                        nBestNetworks.append( 
                                "\n*** *** N-best networks obtained from all " 
                                + maxThreads + " threads:" );
                        nBestNetworks.append( "\n*** *** \n" );
                    }
                    
                    nBestNetworks.append( nBestProcessor.listNBestNetworks( 
                            highScoreStructureSet ));                
                    
                    settings.setHighScoreStructureSet( highScoreStructureSet );
                }
                catch ( BanjoException e ) { 

                    throw new BanjoException( e );
                }
                catch ( Exception e ) { 

                    throw new BanjoException( e,
                            BANJO.ERROR_BANJO_DEV, "Error while collecting the " +
                            "(combined) search results produced by the " + maxThreads +
                            " threads." );
                }
            }

            settings.writeToFile( outputResultsOnly , nBestNetworks );
            settings.prepareXMLOutput();
            settings.writeToFile( xmlResults , 
                    nBestProcessor.listNBestNetworksXML( highScoreStructureSet ) );
            
            // Set up a post-processor object, so we can display its options, if necessary
            postProcessor = new PostProcessor( settings );
            
            // We are now ready to do the postprocessing, but since we don't throw
            // exceptions directly anymore, we need to run a quick check:
            if ( settings.wereThereProblems() ) {
                
                throw new BanjoException( BANJO.ERROR_CHECKPOINTTRIGGER,
                        "(Checkpoint) " +
                        "Banjo could not prepare the post-processing successfully; " +
                        "the following issues prevented further program execution:" + 
                        BANJO.FEEDBACK_NEWLINE +
                        settings.compileErrorMessages().toString() );
            }
            
            // Execute the postprocessing functions
            postProcessor.execute();
            
            // Do a final check (there may have been warnings, or errors that have 
            // been corrected automatically by Banjo)   
            if ( settings.wereThereProblems() ) {
                
                StringBuffer finalCheckPointFeedback = new StringBuffer(
                    BANJO.BUFFERLENGTH_STAT );
                
                finalCheckPointFeedback.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
                        substring( 0, lineLength ));
                finalCheckPointFeedback.append( BANJO.FEEDBACK_NEWLINE );
                finalCheckPointFeedback.append( BANJO.FEEDBACKSTRING_POSTPROC );
                finalCheckPointFeedback.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
                        substring( 0, lineLength ));
                
                finalCheckPointFeedback.append( BANJO.FEEDBACK_NEWLINE );
                finalCheckPointFeedback.append( settings.compileErrorMessages().toString() );
    
                Collection outputFileFlags = new HashSet();
                outputFileFlags.add( new Integer(BANJO.FILE_RESULTS) );
                settings.writeToFile( outputFileFlags , finalCheckPointFeedback );
            }
        }
        catch ( BanjoException e ) { 

            throw new BanjoException( e );
        }
        catch ( Exception e ) { 

            throw new BanjoException( e,
                    BANJO.ERROR_BANJO_DEV, "Error while setting up and running " +
                    "multiple search threads." );
        }    
    }

    // Main entry point for Banjo
    public static void main( String _args[] ) throws Exception {

        Banjo banjoAppWithThreads =  new Banjo();
        
        banjoAppWithThreads.execute( _args );
    }
}