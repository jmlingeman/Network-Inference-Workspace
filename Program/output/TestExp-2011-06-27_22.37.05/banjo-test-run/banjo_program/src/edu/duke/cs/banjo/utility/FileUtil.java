/*
 * Created on Apr 16, 2004
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

//import edu.duke.cs.banjo.data.*;
import java.util.*;
import java.io.*;

import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.data.observations.ObservationsAsArray;
import edu.duke.cs.banjo.data.observations.ObservationsAsMatrix;
import edu.duke.cs.banjo.data.observations.ObservationsI;


/**
 * Handles all file input and output for the application.
 *
 * <p><strong>Details:</strong> <br>
 * 
 * 
 * <p><strong>Change History:</strong> <br>
 * Created on Apr 16, 2004
 * <p>
 * 2/23/2006 (v2.0) hjs     Use standard Banjo validation for files and directories.
 * 
 * <p>
 * hjs (v2.1)               Updated for multi-threaded execution
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class FileUtil {
    
    private FileWriter[] fileWriter = new FileWriter[BANJO.MAXOUTPUTFILES];
    private OutputStreamWriter[] streamWriter = new OutputStreamWriter[BANJO.MAXOUTPUTFILES];
    private PrintWriter[] printWriter = new PrintWriter[BANJO.MAXOUTPUTFILES];
    private String[] fileName = new String[BANJO.MAXOUTPUTFILES];
    private boolean[] fileReady = new boolean[BANJO.MAXOUTPUTFILES];
    
    // Note: There is no buffer for errors; instead, errors are reported immediatedly
    
    // Use(d) as raw storage container for the initialSettings as read from the
    // settings file
    private Properties settings = new Properties();
    
    // This gives us access to the main Settings class, so we can validate the file
    // info in the same way the rest of the application does it.
    private Settings processData = null;
    
    private boolean traceToConsole;
    
    private String[] traceFileName;
    
    // Flag to avoid setting up the output files multiple times

    
    public FileUtil() throws Exception { 
        
        // nothing to do: 
        // use this constructor only for the initial loading of the settings file
    }
    
    public FileUtil( final Settings _processData ) throws Exception {        
                
        processData = _processData;

        // Check if we have access to the applications main settings object,
        // and bail out if we don't
        if ( processData == null ) {
            
            throw new BanjoException( BANJO.ERROR_BANJO_DEV, 
                    "(FileUtil.setupOutputFile) " +
                    "Development issue: With no access set up for the " +
                    "main settings, the output file and directory cannot be " +
                    "validated." );
        }
            
        // DON"T want to do this here:
        // Validate any parameters for setting up the output files
//        validateRequiredData();
    }
    
    synchronized public void prepareResultsFile() throws Exception {

        String tmpResultsFileName = new String("");
        String errorMessage;
        String resultDirectoryName = processData.getSettingItemValueAsValidated(
                BANJO.SETTING_OUTPUTDIRECTORY.toLowerCase() );
        File resultDirectory;
        
        if ( resultDirectoryName == null || resultDirectoryName.equals( "" ) ) {
            
            // Use default directory
            resultDirectoryName = BANJO.DEFAULT_OUTPUTDIRECTORY;
            resultDirectory = new File( resultDirectoryName );
            
            if ( !resultDirectory.exists() ) resultDirectory.mkdirs();
        }
        else {
            
            resultDirectory = new File( resultDirectoryName );
            
            if ( !resultDirectory.exists() ) resultDirectory.mkdirs();
        }
            
        resultDirectoryName = resultDirectoryName + File.separator;
        
        // TODO: load from settings file
        // For now use config option:
        traceToConsole = BANJO.CONFIG_ECHOOUTPUTTOCMDLINE;
        
        // For now, we route all output to a file (report/summary/trace)
        // Note that writing out an error is handled differently

        try {
            
            tmpResultsFileName = processData.getDynamicProcessParameter( 
                    BANJO.DATA_REPORTFILE );
            
            // Check if we need to use the default
            if ( tmpResultsFileName == null || tmpResultsFileName.length() < 1 ) {
                
                tmpResultsFileName = BANJO.DEFAULT_REPORTFILE;
            }

            tmpResultsFileName = processData.parseForTokensAfterValidation( tmpResultsFileName );
            processData.setDynamicProcessParameter( BANJO.DATA_REPORTFILE,
                    tmpResultsFileName );
            tmpResultsFileName = resultDirectoryName + tmpResultsFileName;
            
            fileName[ BANJO.FILE_RESULTS ] = tmpResultsFileName;
            
            streamWriter[ BANJO.FILE_RESULTS ] = new OutputStreamWriter(
                    new FileOutputStream( fileName[ BANJO.FILE_RESULTS ], true ));
            
            printWriter[ BANJO.FILE_RESULTS ] = new PrintWriter( streamWriter[ BANJO.FILE_RESULTS ] );
        }
        catch (IOException e) {
            
            errorMessage = "FileUtil.prepareResultsFile  --  Can't access results file '" + 
                tmpResultsFileName +  "'.";
            System.out.println( errorMessage );
            if ( BANJO.DEBUG && BANJO.TRACE_FILEUTIL ) 
                e.printStackTrace();
            recordError( errorMessage );
            throw new BanjoException( e,
                    BANJO.ERROR_BANJO_USERINPUT, errorMessage  );
        }
    }
    
    synchronized public void prepareXMLResultsFile() throws Exception {

        String errorMessage;
        String resultDirectoryNameXML = processData.getSettingItemValueAsValidated(
                BANJO.SETTING_XMLOUTPUTDIRECTORY.toLowerCase() );
        File resultDirectoryXML;
        String tmpResultsFileNameXML = new String("");
        
        if ( resultDirectoryNameXML == null || resultDirectoryNameXML.equals( "" ) ) {
            
            // Use default directory
            resultDirectoryNameXML = BANJO.DEFAULT_XMLOUTPUTDIRECTORY;
            resultDirectoryXML = new File( resultDirectoryNameXML );
            
            if ( !resultDirectoryXML.exists() ) resultDirectoryXML.mkdirs();
        }
        else {
            
            resultDirectoryXML = new File( resultDirectoryNameXML );
            
            if ( !resultDirectoryXML.exists() ) resultDirectoryXML.mkdirs();
        }
            
        resultDirectoryNameXML = resultDirectoryNameXML + File.separator;
        
        // TODO: load from settings file
        // For now use config option:
        traceToConsole = BANJO.CONFIG_ECHOOUTPUTTOCMDLINE;
        
        // For now, we route all output to a file (report/summary/trace)
        // Note that writing out an error is handled differently

        try {
            
            tmpResultsFileNameXML = processData.getDynamicProcessParameter( 
                    BANJO.DATA_XMLREPORTFILE );
            
            // Check if we need to use the default
            if ( tmpResultsFileNameXML == null || tmpResultsFileNameXML.length() < 1 ) {
                
                fileReady[ BANJO.FILE_RESULTSXML ] = false;
            }
            else {

                tmpResultsFileNameXML = processData.parseForTokensAfterValidation( 
                        tmpResultsFileNameXML );
                processData.setDynamicProcessParameter( BANJO.DATA_XMLREPORTFILE,
                        tmpResultsFileNameXML );
                tmpResultsFileNameXML = resultDirectoryNameXML + tmpResultsFileNameXML;
                
                fileName[ BANJO.FILE_RESULTSXML ] = tmpResultsFileNameXML;
                
                streamWriter[ BANJO.FILE_RESULTSXML ] = new OutputStreamWriter(
                        new FileOutputStream( fileName[ BANJO.FILE_RESULTSXML ], true ));
                
                printWriter[ BANJO.FILE_RESULTSXML ] = 
                    new PrintWriter( streamWriter[ BANJO.FILE_RESULTSXML ] );

                fileReady[ BANJO.FILE_RESULTSXML ] = true;
            }
        }
        catch (IOException e) {
            
            errorMessage = "FileUtil.prepareXMLResultsFile  --  Can't access XML results file '" + 
                tmpResultsFileNameXML +  "'.";
            System.out.println( errorMessage );
            if ( BANJO.DEBUG && BANJO.TRACE_FILEUTIL ) 
                e.printStackTrace();
            recordError( errorMessage );
            throw new BanjoException( e,
                    BANJO.ERROR_BANJO_USERINPUT, errorMessage  );
        }
    }
    
    /**
     * Validates the settings values required for the file access.
     * 
     * @return Returns the boolean flag that indicates whether a crucial setting
     * could not be validated.
     */
//    private boolean validateRequiredData() throws Exception {
//        
//        boolean isDataValid = true;
//        
//
//        return isDataValid;
//    }
        
    public synchronized void writeToFile( final Collection _outputFileFlags, 
            final StringBuffer _dataToWrite ) throws Exception {
        
        Integer fileFlag;
                
        Iterator fileFlagIterator = _outputFileFlags.iterator();
        while ( fileFlagIterator.hasNext() ) {
            
            fileFlag = (Integer) fileFlagIterator.next();
        
            switch (fileFlag.intValue())
            {
            case BANJO.FILE_RESULTS:
                
                setupFile( fileWriter[ BANJO.FILE_RESULTS ], 
                        streamWriter[ BANJO.FILE_RESULTS ], 
                        printWriter[ BANJO.FILE_RESULTS ], 
                        fileName[ BANJO.FILE_RESULTS ] );

                printWriter[ BANJO.FILE_RESULTS ].println( _dataToWrite );
                if ( traceToConsole ) System.out.println( _dataToWrite );
                
                printWriter[ BANJO.FILE_RESULTS ].flush();
                break;
                
            case BANJO.FILE_RESULTSXML:
                
                if ( fileReady[ BANJO.FILE_RESULTSXML ] ) {
                    
                    setupFile( fileWriter[ BANJO.FILE_RESULTSXML ], 
                            streamWriter[ BANJO.FILE_RESULTSXML ], 
                            printWriter[ BANJO.FILE_RESULTSXML ], 
                            fileName[ BANJO.FILE_RESULTSXML ] );
    
                    printWriter[ BANJO.FILE_RESULTSXML ].println( _dataToWrite );
                    
                    printWriter[ BANJO.FILE_RESULTSXML ].flush();
                }
                break;
                
            default: 
                
                // this should never happen:
                throw new BanjoException(
                        BANJO.ERROR_BANJO_DEV, 
                        "(FileUtil.writeToFile) Dev error: " +
                        "trying to write data to unspecified file." );
            }
        }
    }
    
    public void setupFile( 
            final FileWriter _fileWriter,
            OutputStreamWriter _outputStreamWriter,
            PrintWriter _printWriter,
            final String _fileName ) throws Exception {
                
        try {
                                                    
            _outputStreamWriter = new OutputStreamWriter(
                    new FileOutputStream( _fileName, true ) );
            _printWriter = new PrintWriter( _outputStreamWriter );
        }
        catch (IOException e) {
            
            String errorMessage = "(FileUtil.setupFile)  --  Can't access output file '" +
                    _fileName +  "'.";
            System.out.println( errorMessage );
            if ( BANJO.DEBUG && BANJO.TRACE_FILEUTIL ) 
                e.printStackTrace();
            recordError( errorMessage );
            
            throw new BanjoException(  e  , BANJO.ERROR_BANJO_DEV, errorMessage );
        }
        catch ( Exception e ) {
            
            throw new BanjoException( e , BANJO.ERROR_BANJO_DEV );
        }
    }
    
    // Completely separate method for writing out an error
    public void recordError( final String _strErrorMessage ) throws Exception {
        
        
        OutputStreamWriter errorStreamWriter = null;
        PrintWriter errorPrintWriter = null;

        // Decide to place error file in application directory instead
        String errorFileName = new String();
        
        try {
            
            errorFileName = settings.getProperty( 
                    BANJO.SETTING_ERRORFILE.toLowerCase() );
                    
            if ( errorFileName == null || errorFileName.length() < 1 ) 
                errorFileName = BANJO.DEFAULT_ERRORFILE;
                        
            errorStreamWriter = new OutputStreamWriter(
                    new FileOutputStream(errorFileName, true));
            errorPrintWriter = new PrintWriter(errorStreamWriter);      
        
            // For now, write all error output to the error file
            errorPrintWriter.println( BANJO.FEEDBACK_NEWLINE );
            errorPrintWriter.println( _strErrorMessage );

            if ( BANJO.CONFIG_ECHOERROROUTPUTTOCMDLINE ) {

                System.out.println( _strErrorMessage );
            }
            
            errorPrintWriter.close();
        }
        catch (Exception e) {
            
            // Record any IO error separately - could happen for various reasons
            // that the user could do something about?
            System.out.println( "(FileUtil.recordError)  Can't access error file  '" + 
                    errorFileName +  "'." );
            System.out.println( "(FileUtil.recordError) " +
                    "The following error could not be written to the error file '"
                    + _strErrorMessage + "'." );
            

            if ( BANJO.DEBUG && BANJO.TRACE_FILEUTIL ) 
                e.printStackTrace();
            
            throw new Exception( e );
        }       
        finally {
            try {
                errorPrintWriter.close();
                errorStreamWriter.close();
            }
            catch (Exception e) {
                
                // Is there a point to do anything besides telling the user?
                System.out.println( "(FileUtil.recordError) Error cleaning up after trying " +
                        "to write to error file." );
                
                throw new Exception( e );
            }
        }
    }
    
    public Properties loadSettings( 
            String _directoryName, 
            final String _inputFileName ) throws Exception {
                
        File inputFile;
        
        try {
            
            // Check if a file name has been supplied:
            if ( _inputFileName != null || _inputFileName.length() > 0 ) {
    
                // Check if a directory name has been supplied:
                if ( _directoryName != null && _directoryName.length() > 0 ) {
                    
                    inputFile = new File( _directoryName, _inputFileName );
                }
                else {
                
                    inputFile = new File( _inputFileName );
                }
                
                // Ccheck if the file actually exists
                if ( !inputFile.exists() ) {
                    
                    // Now try to find the settings file in the default input directory
                    _directoryName = BANJO.DEFAULT_INPUTDIRECTORY;
                    
                    processData.addToWarnings( new BanjoError( 
                            "Default value ('" + BANJO.DEFAULT_INPUTDIRECTORY +
                            "') applied to 'settingsDirectory' parameter.",
                            BANJO.ERRORTYPE_ALERT_DEFAULTAPPLIED,
                            BANJO.SETTING_CMDARG_SETTINGSFILENAME,
                            null ) );
                    
                    inputFile = new File( _directoryName, _inputFileName );
                    
                    if ( !inputFile.exists() ) {
                        
                        // We give up looking for the settings file and tell the
                        // user that the file could not be found
                        // Note: we don't try to recover by trying to find a file with the 
                        // default settings file name
                        throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
                            "The settings file '" + _inputFileName +
                            "' cannot be found at '" + _directoryName + "'." );
                    }
                }
            }
            else {
                
                // No name for the settings file has been provided, so check if the default
                // file exists
                inputFile = new File( BANJO.DEFAULT_INPUTDIRECTORY, 
                        BANJO.DEFAULT_SETTINGSFILENAME );
                
                // If there is no settings file in the specified LoadDirectory
                // then try the directory from where the application is running:
                if ( !inputFile.exists() ) {
                    
                    String firstInputFile = inputFile.getAbsolutePath();
                    inputFile = new File( BANJO.DEFAULT_SETTINGSFILENAME );
                    
                    if ( !inputFile.exists() ) {
                        
                        throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT, 
                                "\n *** Cannot find the settings file at either location: '" +
                                firstInputFile + "' and '" + 
                                inputFile.getAbsolutePath() + "'?!" );
                    }
                }
                else {
                    
                    if ( BANJO.DEBUG && BANJO.TRACE_FILEUTIL )
                        System.out.println( "Setting file '" + 
                                BANJO.DEFAULT_INPUTDIRECTORY + File.separator 
                                + BANJO.DEFAULT_SETTINGSFILENAME
                                + "' found." );
                }
            }
               
            // We should have a valid file now, which we try to load into the 
            // properties container
            FileInputStream cmdParameterFile = new FileInputStream( inputFile );
            
            // (Marker only) This is the place where we can add support for
            // other input file formats
            
            // Load the properties from the settings file to a temporary properties object,
            // then add them all in lower case to their final container
            Properties tmpSettings = new Properties();
            tmpSettings.load( cmdParameterFile );

            Set parameterSet;
            Iterator parameterIterator;
            String strNextProperty;
            String strPropertyValue;
            String strParameterName;
            String strParameterValue;
            // Need to convert all parameters to lowercase
            parameterSet = tmpSettings.keySet();
            parameterIterator = parameterSet.iterator();
            while (parameterIterator.hasNext()) {
                
                strParameterName = (String) parameterIterator.next();
                strParameterName = strParameterName.trim();

                strParameterValue = 
                    tmpSettings.getProperty( strParameterName ).trim();
                strParameterValue = StringUtil.removeTrailingComment( strParameterValue );
                
                settings.setProperty( 
                        strParameterName.toLowerCase(), strParameterValue );
            }
        }
        catch (BanjoException e){
            
            throw new BanjoException( e );
        }
        catch (Exception e){
            
            // May not want the internal error message displayed to the end user
            if ( BANJO.DEBUG && BANJO.TRACE_FILEUTIL )
                System.out.println( "\n(FileUtil.LoadSettings) " + e.toString() + "\n" );
            
            if ( BANJO.DEBUG && BANJO.TRACE_FILEUTIL ) 
                e.printStackTrace();
            
            // Pass the message on to the caller for final handling
            throw new BanjoException( e, BANJO.ERROR_BANJO_DEV,
                    "(Development issue?) Error(s) trying to load the settings file." );
        }   
        
        // Add the input file to the just loaded settings
        settings.setProperty( BANJO.SETTING_CMDARG_SETTINGSFILENAME.toLowerCase(),
                inputFile.toString() );
        
        return settings;
    }
    
    public ObservationsI loadObservations() throws Exception{

        ObservationsI observations;
        
        // (ver 2.1) hjs 10/12/2007 MT-related change
        // Load the observations here to use a single copy in all threads
        if ( BANJO.CONFIG_OBSERVATIONS.equalsIgnoreCase( BANJO.UI_COMPACTOBSERVATIONS ) ) {

            observations = new ObservationsAsArray( processData );

            (( ObservationsAsArray ) observations ).loadData( processData );
        }
        else if ( BANJO.CONFIG_OBSERVATIONS.equalsIgnoreCase( BANJO.UI_ORIGINALOBSERVATIONS ) ) {
            
            observations = new ObservationsAsMatrix( processData );

            (( ObservationsAsMatrix ) observations ).loadData( processData );
        }
        else {
            
            throw new BanjoException( 
                    BANJO.ERROR_BANJO_DEV,
                    "(Settings constructor) " +
                    "Development issue: There is no code for handling the supplied type " +
                    "of observations." );
        }
        
        return observations;
    }
    
    protected void setTraceFileName( 
            final int _traceFileID, 
            final String _traceFileName  ) throws Exception {
        
        traceFileName[ _traceFileID-1 ] = _traceFileName;
    }

    
    protected String getTraceFileName( final int _traceFileID ) throws Exception {
        
        String fileNameToReturn;
        
        if ( traceFileName != null && _traceFileID < traceFileName.length ) {
            
            fileNameToReturn = traceFileName[ _traceFileID-1 ];
        }
        else {
            
            fileNameToReturn = "traceFile_" + _traceFileID;
        }
        return fileNameToReturn + ".txx";
    }
    
    public void writeTraceToFile( 
            final String _dataToWrite, 
            final boolean _traceToConsole,
            final int _traceFileID ) throws Exception {
        
        String localFileName = "fileNameNotYetAssigned";
                    
        try {
            
            localFileName = getTraceFileName( _traceFileID );

            writeStringToFile( localFileName, _dataToWrite, _traceToConsole );
        }
        catch ( Exception e ) {

            throw new BanjoException(
                    BANJO.ERROR_BANJO_DEV, 
                    "(FileUtil.writeTraceToFile) Error " +
                    "trying to write data to the file '" +
                    fileName + "'." );
        }
    }
    
    /**
     * Wrapper for writing a string to a file.
     * 
     */
    public void writeStringToFile( 
            final String _fileName, 
            final String _dataToWrite, 
            final boolean _traceToConsole ) throws Exception {
        
        final OutputStreamWriter outputStreamWriter;
        final PrintWriter localPrintWriter;
                    
        try {
            
            if ( _traceToConsole ) System.out.println( _dataToWrite );
    
            outputStreamWriter = new OutputStreamWriter(
                    new FileOutputStream( _fileName, true) );
            localPrintWriter = new PrintWriter(outputStreamWriter);
    
            localPrintWriter.println( _dataToWrite );
            localPrintWriter.close();
        }
        catch ( Exception e ){

            throw new BanjoException(
                    BANJO.ERROR_BANJO_DEV, 
                    "(FileUtil.writeStringToFile) Error " +
                    "trying to write data to the file '" +
                    _fileName + "'." );
        }
    }
}
