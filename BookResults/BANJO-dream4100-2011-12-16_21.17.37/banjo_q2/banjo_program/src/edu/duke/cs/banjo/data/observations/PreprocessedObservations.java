/*
 * Created on Dec 14, 2005
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
package edu.duke.cs.banjo.data.observations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.regex.*;

import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.utility.BANJO;
import edu.duke.cs.banjo.utility.BanjoException;

/**
 * Contains a single, basic, set of observation data (usually supplied by a 
 * single file).
 * 
 * <p><strong>Details:</strong> <br>
 * The supplied data is assumed to be originally supplied in string format. 
 * A copy of the original data is stored in string format. <br>
 * After data transformation and (optional) discretization, the processed data
 * is stored in integer format. The customers of this class are served the data
 * in integer format.
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Dec 14, 2005
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class PreprocessedObservations {

    // Original data points in string format, as loaded from file
    protected String[][] strRawObservationDataPoints;
    
    // Cached variables
    protected final Settings processData;
    protected final int varCount;
    // Can't make the row count final, because we may not know it until we load the
    // data from file
    protected int observationCount;
    
    // (optional) names of variables
    protected String[] variableNames;

    // Our "standard" format is for observations to be in rows 
    // and variables in columns
    protected boolean variablesInColumns = true;

    // We don't expect the variable names to be listed in the file unless 
    // the user has specifically specified it
    protected boolean variableNamesInFile = false;
    
    // Store the counts for the number of columns and rows in the observations file
    // Note: We perform a strong parsing of all data to ensure the proper rectangular
    // shape of the data.
    protected int observedColumnCount = -1;
    protected int observedRowCount = -1;

    public PreprocessedObservations( final Settings _processData ) throws Exception {
    
        processData = _processData;
        varCount = Integer.parseInt( processData.getValidatedProcessParameter(
              BANJO.SETTING_VARCOUNT ) );
    }
	
    protected void loadObservationsAsRows( final String _directoryName,
            final String _fileName ) throws Exception {

        // Find (and check) the number of data rows and columns in the specified file
        findObservationsRowAndColumnCounts( _directoryName, _fileName );

        // Find the number of data rows (OBSERVATIONS) in the specified file
        int lineCountInObsFile = observedRowCount;
        
        int observedVarCount = observedColumnCount;
        
        // Compare the found number of columns with the user-specified varCount
        if ( observedVarCount != varCount && !BANJO.DEBUG ) {
            
            String strVariableAreInRowsChoice = processData.getValidatedProcessParameter( 
                BANJO.SETTING_VARIABLESAREINROWS );
            
            throw new BanjoException( 
                    BANJO.ERROR_BANJO_USERINPUT,
                    "(Loading observations) " +
                    "The number of variables as specified in the settings file (" 
                    + varCount + ") differs from the number of variables found " +
                    "in the observations file (" + observedVarCount + 
                    " columns, since '" + BANJO.SETTING_VARIABLESAREINROWS +
                    "= " + strVariableAreInRowsChoice +
                    "' is specified)." );
        }
       
        // Set the pattern for the data parsing
        // Note we allow comments at the end of a line (via Pattern.COMMENTS)
        Pattern pattern = Pattern.compile( 
                BANJO.PATTERN_OBSERVATIONSPARSING_WHITESPACE, Pattern.COMMENTS );
        
		final File dataFile = new File( _directoryName, _fileName);
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;

		try {
		
			if ( !dataFile.exists() ) {
			    
				throw new BanjoException( 
				        BANJO.ERROR_BANJO_USERINPUT, 
                        "(Loading observations) " +
				        "Cannot find the observations file: '" 
				        + _fileName + "' in directory '" + _directoryName + "'." );
			}
			
			int i;
			String strCurrentObsValue;
		    double dblCurrentObsValue;
		    String textline;
            int suppliedVarCount = varCount;
            
            // We also don't expect the variable names to be listed in the file unless 
            // the user has specifically specified it
            String strVariableNamesChoice = processData.getDynamicProcessParameter( 
                    BANJO.DATA_VARIABLENAMESCHOICE );
            String strVariableNames = "";
            
            if ( strVariableNamesChoice.equalsIgnoreCase( BANJO.UI_VARIABLENAMESCHOICE_INFILE )) {
                
                variableNamesInFile = true;
            }

            if ( variableNamesInFile ) {
            
                observationCount = lineCountInObsFile-1;
            }
            else {
                
                observationCount = lineCountInObsFile;
            }

            strRawObservationDataPoints = 
                new String[ observationCount ][ varCount ];
            
			// Set up the file loading:
			bufferedReader = new BufferedReader(
			        new FileReader(_directoryName + File.separator + _fileName));
					
            boolean variablesRead = false;
			// Now read the data
			i = 0;
			while ( ( textline = bufferedReader.readLine()) != null ) {
                				
				// ---------------------------------------
				// Process the entries on the current line
				// ---------------------------------------
				
                textline = textline.trim();
                String[] lineComponents = pattern.split( textline );
                int observedVarCountOnCurrentLine = lineComponents.length;
                
				// Ignore blank or commented out lines
				if ( observedVarCountOnCurrentLine > 0 && 
                        !( textline.startsWith( "#" ) ) && !( textline.equals( "" ) ) ) {

                    // First process the variable names if needed
                    if ( variableNamesInFile && variablesInColumns && i==0 && !variablesRead ) {
                            
                        // Process the first line as the variable labels (whitespace-separated)
                        Pattern p = Pattern.compile( BANJO.PATTERN_VARIABLENAMESPARSING_WHITESPACE );
                                                        
                        strVariableNames = textline;
                        variableNames = p.split( textline );

                        // Try other delimiters, if we didn't find the right number of names
                        if ( variableNames.length != varCount ) {
                            
                            p = Pattern.compile( BANJO.PATTERN_VARIABLENAMESPARSING_DELS );
                            
                            strVariableNames = textline;
                            variableNames = p.split( textline );
                        }
                        
                        // Since the user told us that the observations file contains the variable
                        // names, we check if we found them as expected
                        if ( variableNames.length == varCount ) {
                            
                            variablesRead = true;
                        }
                        else {
                            
                            // The line where we expected the variable labels did not
                            // have the correct format
                            throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT, 
                                "The following data line from the observations file \n'" +
                                textline + "'\n" +
                                "could not be interpreted as variable names.\n" +
                                "Is the setting '" + BANJO.SETTING_VARIABLENAMES + "=" +
                                BANJO.UI_VARIABLENAMESCHOICE_INFILE + "' really intended?" );
                        }
                    }
                    // process the observations
                    else {
                        
                        // We can never (even in debug mode) indicate more variables than
                        // there are in the file, so apply sanity check:
                        if ( observedVarCountOnCurrentLine < suppliedVarCount ) {
                            
                            throw new BanjoException( 
                                    BANJO.ERROR_BANJO_USERINPUT,
                                    "(Loading observations) " + "Observation #" + (i+1) + 
                                    " in observations file '" + _fileName +
                                    "' contains fewer (namely, " + observedVarCountOnCurrentLine + 
                                    ") data points than the expected " + varCount + "." );
                        }

    				    // Next "Sanity" cross-check on the data: The observations need to have
    				    // exactly as many data points as the separately user-supplied
    				    // variable count - HOWEVER, we allow exceptions in debug mode, so we
                        // can run multiple tests from a single data set
    					if ( observedVarCountOnCurrentLine > suppliedVarCount && !BANJO.DEBUG ) {
    					    
        					// Note: this allows for the observation file to contain more observation
        					// points than there are specified variables (the extra values are ignored),
        					// but we throw an exception when there are less observation points than
        					// variables
    					    throw new BanjoException( 
    					            BANJO.ERROR_BANJO_USERINPUT,
                                    "(Loading observations) " + "Observation #" + (i+1) + 
    					            " in observations file '" + _fileName +
    					            "' contains " + observedVarCountOnCurrentLine + 
    					            " data points instead of the expected " + varCount + "." );
    					}
                        
    					// Use varCount as loop conditions
    					for ( int j=0; j<suppliedVarCount; j++ ) {
    					    
    					    strCurrentObsValue = lineComponents[j];
    					    strRawObservationDataPoints[i][j] = strCurrentObsValue;
    					    
    					    if ( strCurrentObsValue.length() == 0 || 
    					            strCurrentObsValue.equalsIgnoreCase( "" ) ) {
    					        
    					        // flag invalid (missing) observation value
    					        throw new BanjoException( 
    					                BANJO.ERROR_BANJO_USERINPUT,
    					                "Line '" + (i+1) + "' of the observations file '" +
                                        _fileName + "' is " +
    					                "missing a value for variable '" + j + "'." );
    					    }
    					    else if ( strCurrentObsValue.length() > 1  ) {
    					        
    					        // Check if non-numeric. If so, tell the user that we
    					        // only support numeric entries at this time
    					        try {
    					            
    					            dblCurrentObsValue = 
    					                Double.parseDouble( strCurrentObsValue );
    					        }
    							catch (Exception e) {
    							    
    							    throw new BanjoException( e,
    							            BANJO.ERROR_BANJO_USERINPUT,
    						                "Line '" + (i+1) + "' of the observations file '" +
                                            _fileName + "' contains an invalid value ('" +
    						                strCurrentObsValue +
    						                "') for variable '" +
    						                j + "'." );
    							}
    					    }
    					    else {
    					        
    					        // Again, test against single-letter strings, since we
    					        // only support numeric values right at this point
    					        
    					        try {
    					            
    					            dblCurrentObsValue =
    					                Integer.parseInt( strCurrentObsValue );
    					        }
    							catch (Exception e) {
    							    
    							    throw new BanjoException( e,
    							            BANJO.ERROR_BANJO_USERINPUT,
    						                "Line '" + (i+1) + "' of the observations file '" +
                                            _fileName + "' contains an invalid value ('" +
    						                strCurrentObsValue +
    						                "') for variable '" +
    						                j + "'." );
    							}
    					    }
    					}
                        
                        i++;
                    }
				}
			}

            // Store the variable names away if they were supplied in the file
            if ( variableNamesInFile ) {
                
                String strVariableNamesToStore;
                
                // When the names were supplied, harvest them now
                if ( variableNames != null && variableNames.length == varCount ) {
                    
                    // Collect the names
                    strVariableNamesToStore = variableNames[0];
                    for ( int j=1; j<varCount; j++ ) {
                        
                        strVariableNamesToStore += BANJO.DELIMITER_SPECIAL + variableNames[j];
                    }
                }
                else { 
                    
                    // Otherwise set the names to their index values
                    strVariableNamesToStore = "0";
                    for ( int j=1; j<varCount; j++ ) {
                        
                        strVariableNamesToStore += BANJO.DELIMITER_SPECIAL + j;
                    }
                }
                
                // Place in processData for access by other components
                processData.setDynamicProcessParameter( 
                        BANJO.DATA_VARIABLENAMES, strVariableNamesToStore );
            }
		}
		catch (IOException e) {
		   
		    throw new Exception( e );
		}		
		catch (BanjoException e) {

			throw new BanjoException( e );
		}		
		finally {
			try { 
								
				if (fileReader != null ) fileReader.close();
				if (bufferedReader != null ) bufferedReader.close();
			} 
			catch( IOException e ) {
                // nothing else to do?
            };
		}
        
        // Dump transposed observations to standard output
//        if ( strRawObservationDataPoints != null && BANJO.DEBUG ) {
//            
//            String[][] transposedDataPoints = transposeDataPoints( strRawObservationDataPoints );
//            
//            StringBuffer transposedData = new StringBuffer( "\nTransposed data:\n" );
//            for ( int i=0; i<transposedDataPoints.length; i++ ) {
//                
//                transposedData.append( "iR" + i + "   "  );
//                for ( int j=0; j<transposedDataPoints[0].length; j++ ) {
//                    
//                    transposedData.append( transposedDataPoints[i][j] );
//                    transposedData.append( " " );
//                }
//                transposedData.append( "\n" );
//            }
//            System.out.print( transposedData.toString() );
//        }
    }
    
    // Load observations that are supplied in "transposed" format (i.e., each row contains
    // all the values for one variable)
    protected void loadObservationsAsColumns( final String _directoryName,
            final String _fileName ) throws Exception {

        // Find (and check) the number of data rows and columns in the specified file
        findObservationsRowAndColumnCounts( _directoryName, _fileName );
        
        int observedVarCount = observedRowCount;

        // Compare the found number of rows with the user-specified varCount
        if ( observedVarCount != varCount && !BANJO.DEBUG ) {
            
            String strVariableAreInRowsChoice = processData.getValidatedProcessParameter( 
                BANJO.SETTING_VARIABLESAREINROWS );
            
            throw new BanjoException( 
                    BANJO.ERROR_BANJO_USERINPUT,
                    "(Loading observations) " +
                    "The number of variables as specified in the settings file (" 
                    + varCount + ") differs from the number of variables found " +
                    "in the observations file (" + observedVarCount + 
                    " rows, since '" + BANJO.SETTING_VARIABLESAREINROWS +
                    "= " + strVariableAreInRowsChoice +
                    "' is specified)." );
        }
        
        // Note we allow comments at the end of a line (via Pattern.COMMENTS)
                        
        // Set the pattern for the data parsing
        Pattern pattern = Pattern.compile( 
                BANJO.PATTERN_OBSERVATIONSPARSING_WHITESPACE, Pattern.COMMENTS );        
        
        final File dataFile = new File( _directoryName, _fileName);
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;

        try {
        
            if ( !dataFile.exists() ) {
                
                throw new BanjoException( 
                        BANJO.ERROR_BANJO_USERINPUT,
                        "(Loading observations) " +
                        "Cannot find the observations file: '" 
                        + _fileName + "' in directory '" + _directoryName + "'." );
            }
            
            int i;
            int observedObservationCount;
            String strCurrentObsValue;
            double dblCurrentObsValue;
            String textline;
            
            int offsetForSuppliedVarName;
            String strVariableNamesChoice = processData.getDynamicProcessParameter( 
                    BANJO.DATA_VARIABLENAMESCHOICE );
            if ( strVariableNamesChoice.equalsIgnoreCase( BANJO.UI_VARIABLENAMESCHOICE_INFILE )) {
                
                variableNamesInFile = true;
            }

            int adjustedObservationCount;
            if ( variableNamesInFile ) {

                adjustedObservationCount = observedColumnCount - 1;
                offsetForSuppliedVarName = 1;
                
                if ( variableNames == null ) {
                    
                    variableNames = new String[ varCount ];
                }
            }
            else {

                adjustedObservationCount = observedColumnCount;
                offsetForSuppliedVarName = 0;
            }
            
            observedObservationCount = observedColumnCount;
            
            // Set the data field:
            observationCount = adjustedObservationCount;

            strRawObservationDataPoints = 
                new String[ observationCount ][ varCount ];
            
            // Set up the file loading:
            bufferedReader = new BufferedReader(
                    new FileReader(_directoryName + File.separator + _fileName));
                    
            boolean variablesRead = false;
            // Now read the data
            i = 0;
            while ( ( textline = bufferedReader.readLine()) != null ) {
                                
                // ---------------------------------------
                // Process the entries on the current line
                // ---------------------------------------
                
                textline = textline.trim();
                String[] lineComponents = pattern.split( textline );
                observedObservationCount = lineComponents.length;
                
                // this allows us to have a blank line or one that starts with #
                // without upsetting the observation loading
                if ( observedObservationCount > 1 && 
                        !( textline.startsWith( "#" ) ) && !( textline.equals( "" ) ) ) {
                    
                    // We can never (even in debug mode) indicate more observations than
                    // there are in the file
                    if ( observedObservationCount < adjustedObservationCount ) {
                        
                        throw new BanjoException( 
                                BANJO.ERROR_BANJO_USERINPUT,
                                "(Loading observations) " + "Observation #" + (i+1) + 
                                " in observations file '" + _fileName +
                                "' contains fewer (namely, " + observedObservationCount + 
                                ") data points than the expected " + varCount + "." );
                    }
                            
                            
                    // "Sanity" cross-check on the data: The observations need to have
                    // exactly as many data points as the separately user-supplied
                    // variable count - HOWEVER, we allow exceptions in debug mode, so we
                    // can run multiple tests from a single data set
                    if ( observedObservationCount > adjustedObservationCount+offsetForSuppliedVarName 
                            && !BANJO.DEBUG ) {
                        
                        // Note: this allows for the observation file to contain more observation
                        // points than there are specified variables (the extra values are ignored),
                        // but we throw an exception when there are less observation points than
                        // variables
                        throw new BanjoException( 
                                BANJO.ERROR_BANJO_USERINPUT,
                                "(Loading observations) " + "Observation #" + (i+1) + 
                                " in observations file '" + _fileName +
                                "' contains " + observedObservationCount + 
                                " data points instead of the expected " + 
                                adjustedObservationCount + "." );
                    }
                    
                    // Take care of the first entry if we need to parse out the variable
                    // names in a "transposed" observations file
                    if ( variableNamesInFile && !variablesInColumns ) {
                        
                        variableNames[i] = lineComponents[0];
                    }
                    
                    // Use varCount as loop conditions
                    for ( int j=0; j<adjustedObservationCount; j++ ) {
                        
                        strCurrentObsValue = lineComponents[j + offsetForSuppliedVarName ];
                        strRawObservationDataPoints[j][i] = strCurrentObsValue;
                        
                        if ( strCurrentObsValue.length() == 0 || 
                                strCurrentObsValue.equalsIgnoreCase( "" ) ) {

                            // flag invalid (missing) observation value
                            throw new BanjoException( 
                                    BANJO.ERROR_BANJO_USERINPUT,
                                    "Line '" + (i+1) + 
                                    "' of the observations file '" + _fileName +
                                    "' is missing a value for variable '" + j + "'." );
                        }
                        else if ( strCurrentObsValue.length() > 1  ) {
                            
                            // Check if non-numeric. If so, tell the user that we
                            // only support numeric entries at this time
                            try {
                                
                                dblCurrentObsValue = 
                                    Double.parseDouble( strCurrentObsValue );
                            }
                            catch (Exception e) {
                                
                                throw new BanjoException( e,
                                        BANJO.ERROR_BANJO_USERINPUT,
                                        "Line '" + (i+1) + "' of the observations file '" +
                                        _fileName + "' contains an invalid value ('" +
                                        strCurrentObsValue +
                                        "') for variable '" +
                                        j + "'." );
                            }
                        }
                        else {
                            
                            // again test against single-letter strings, since we
                            // only support numeric values right at this point
                            
                            try {
                                
                                dblCurrentObsValue = 
                                    Integer.parseInt( strCurrentObsValue );
                            }
                            catch (Exception e) {
                                
                                throw new BanjoException( e,
                                        BANJO.ERROR_BANJO_USERINPUT,
                                        "Line '" + (i+1) + "' of the observations file '" +
                                        _fileName + "' contains an invalid value ('" +
                                        strCurrentObsValue +
                                        "') for variable '" +
                                        j + "'." );
                            }
                        }
                    }
                    
                    i++;
                }
            }

            if ( variableNamesInFile ) {
                String strVariableNamesToStore;
                if ( variableNames != null && variableNames.length == varCount ) {
                    
                    // Collect the names
                    strVariableNamesToStore = variableNames[0];
                    for ( int j=1; j<varCount; j++ ) {
                        
                        strVariableNamesToStore += BANJO.DELIMITER_SPECIAL + variableNames[j];
                    }
                }
                else { 
                    
                    // Set the names to their index values
                    strVariableNamesToStore = "0";
                    for ( int j=1; j<varCount; j++ ) {
                        
                        strVariableNamesToStore += BANJO.DELIMITER_SPECIAL + j;
                    }
                }
                                
                // Note: this sets the variable names to whatever we just found in the currently
                // processed file (i.e., for multiple files, the last processed file would determine
                // the variable names - we don't guarantee any particular processing order)
                processData.setDynamicProcessParameter( 
                        BANJO.DATA_VARIABLENAMES, strVariableNamesToStore );
            }
        }
        catch (IOException e) {
           
            throw new Exception( e );
        }       
        catch (BanjoException e) {

            throw new BanjoException( e );
        }       
        finally {
            try { 
                                
                if (fileReader != null ) fileReader.close();
                if (bufferedReader != null ) bufferedReader.close();
            } 
            catch( IOException e ) {
                // nothing else to do?
                throw new BanjoException( BANJO.ERROR_BANJO_DEV, 
                        "Error trying to close the observations file '" +
                        _fileName + "'." );
            };
        }
        
        // This is just for checking the code for handling transposed observations
        if ( strRawObservationDataPoints != null && BANJO.DEBUG && BANJO.TRACE_TRANSPOSEDDATA ) {
            
            String[][] transposedDataPoints = transposeDataPoints( strRawObservationDataPoints );
            
            StringBuffer transposedData = new StringBuffer( "\nTransposed data:\n" );
            for ( int i=0; i<transposedDataPoints.length; i++ ) {
                
                transposedData.append( "iC" + i + "   "  );
                for ( int j=0; j<transposedDataPoints[0].length; j++ ) {
                    
                    transposedData.append( transposedDataPoints[i][j] );
                    transposedData.append( " " );
                }
                transposedData.append( "\n" );
            }
            System.out.print( transposedData.toString() );
        }
    }
    
    protected void findObservationsRowAndColumnCounts(
            final String _directory, final String _fileName ) throws Exception {
        
        // Note: the column count can be 
        // 1) if observations are in rows:
        //      the number of variables (in this case the first column can possibly
        //      be the variable labels)
        // 2) if variables are in rows:
        //      a) the number of valid observations
        //      b) the number of observations + 1 (if the variable labels are specified)

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        StringTokenizer tokenizer;
        
        try {

            int rowCountInObservationsFile = 0;
            int columnCountInObservationsFile = 0;
            File dataFile = new File(_directory, _fileName);
            String itemDelimiter = BANJO.DELIMITER_DEFAULT_OBSERVATIONS;
        
            if (!dataFile.exists()) {
                
                throw new BanjoException( 
                        BANJO.ERROR_BANJO_USERINPUT,
                        "(Loading observations) " +
                        "Cannot find the file: '" 
                        + _fileName + "' in directory '" + _directory + "'." );
            }
                        
            // Load each line 1 by 1:
            bufferedReader = new BufferedReader(
                    new FileReader(_directory + File.separator + _fileName));
                               
            boolean columnsCounted = false;
            String line;
            // Now start reading the data
            while ( ( line = bufferedReader.readLine()) != null ) {
                
                
                // ---------------------------------------
                // Process the entries on the current line
                // ---------------------------------------

                // Set the pattern for the data parsing
                Pattern pattern = Pattern.compile( 
                        BANJO.PATTERN_OBSERVATIONSPARSING_WHITESPACE, Pattern.COMMENTS );
                
                line = line.trim();
                String[] lineComponents = pattern.split( line );
                int columnCountForCurrentLine = lineComponents.length;
                
                
                // this allows us to have a blank line or one that starts with #
                // without upsetting the observation loading
                if ( !line.startsWith( "#" ) && columnCountForCurrentLine > 1 ) {
                    
                    if ( columnCountInObservationsFile > 0 && 
                            columnCountForCurrentLine != columnCountInObservationsFile ) {
                        
                        // This violates the stipulation that the observations data needs to be in
                        // rectangular form
                        throw new BanjoException( 
                                BANJO.ERROR_BANJO_USERINPUT, 
                                "(Loading observations) " +
                                "The observations file '" 
                                + _fileName + "' in directory '" + _directory + "'" +
                                " needs to have the same number of columns in all rows " +
                                "(First discrepancy found at row " + 
                                (rowCountInObservationsFile+1) + 
                                ")." );
                        
                    }
                    columnCountInObservationsFile = lineComponents.length;
                    
                    rowCountInObservationsFile++;
                }
            }

            // finally assign results to data members
            observedRowCount = rowCountInObservationsFile;
            observedColumnCount = columnCountInObservationsFile;
        }
        catch (IOException e) {
               
            throw new Exception( e );
        }       
        catch (BanjoException e) {

            throw new BanjoException( e );
        }       
        finally {
                                
            if (fileReader != null ) fileReader.close();
            if (bufferedReader != null ) bufferedReader.close();
        }
    }
    
    /**
     * @return Returns the strRawObservationDataPoints.
     */
    public String[][] getStrRawObservationDataPoints() {
        
        return strRawObservationDataPoints;
    }
    /**
     * @param _basicObservationDataPoints The strRawObservationDataPoints to set.
     */
    public void setStrRawObservationDataPoints(
            String[][] _basicObservationDataPoints) {
        
        strRawObservationDataPoints = _basicObservationDataPoints;
    }
    /**
     * @return Returns the observationCount.
     */
    public int getObservationCount() {
        return strRawObservationDataPoints.length; //observationCount;
    }
    
    public String toString() {
        
        StringBuffer dataPointsInColumnFormat = new StringBuffer();
        
        for (int i=0; i<strRawObservationDataPoints.length; i++ ) {
            
            for (int j=0; j<varCount; j++ ) {
                
                dataPointsInColumnFormat.append( strRawObservationDataPoints[i][j] );
                dataPointsInColumnFormat.append( "  " );
            }
            
            dataPointsInColumnFormat.append( "\n" );
        }
        
        return dataPointsInColumnFormat.toString();
    }

    /**
     * @return Returns the variableNames.
     */
    public String[] getVariableNames() {
                
        if ( variableNames == null || variableNames.length != varCount ) {
            
            variableNames = new String[ varCount ];
            for ( int i=0; i<varCount; i++ ) {
            
                variableNames[i] = Integer.toString( i );
            }
        }
        
        return variableNames;
    }
    
    private String[][] transposeDataPoints( String[][] datapointsToTranspose )
            throws Exception {
        
        if ( datapointsToTranspose == null ) {
            
            throw new BanjoException( BANJO.ERROR_BANJO_DEV, 
                    "(PreprocessedObservations.transposeDataPoints) " +
                    "Cannot transpose a null array.");
        }
        
        // All our data is rectangular:        
        String[][] transposedDataPoints = new String[ datapointsToTranspose[0].length ]
                                                      [ datapointsToTranspose.length ];

        for ( int i=0; i<datapointsToTranspose.length; i++ ) {
            for ( int j=0; j<datapointsToTranspose[0].length; j++ ) {
                
                transposedDataPoints[j][i] = datapointsToTranspose[i][j];
            }
        }
        
        return transposedDataPoints;
    }
}
