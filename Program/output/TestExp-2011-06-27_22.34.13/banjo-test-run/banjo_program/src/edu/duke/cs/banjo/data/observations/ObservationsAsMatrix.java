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
package edu.duke.cs.banjo.data.observations;

import java.io.*;
import java.util.*;

import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.utility.BANJO;
import edu.duke.cs.banjo.utility.BanjoException;
import edu.duke.cs.banjo.utility.StringUtil;
import edu.duke.cs.banjo.utility.JZOOWildCardFilter;

//import edu.duke.cs.banjo.utility.*;

/**
 * Loads the observation data necessary for the search.
 *
 * <p><strong>Details:</strong> <br>
 *  - <strong>Important:</strong>
 * The Evaluator class uses the internal representation of the ObservationsAsMatrix 
 * for performance, thus breaking encapsulation. <br>
 * 
 * - Implementation of a data set, using a simple matrix of <br>
 * 	&nbsp&nbsp&nbsp		n rows of observationDataPoints <br>
 * 	&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp	by <br>
 * 	&nbsp&nbsp&nbsp		m columns of variables (nodes) <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Based on Banjo 1.0 Observations class, created on Mar 4, 2004 <br>
 * <p>
 * 4/1/2004	hjs		Change data type for external access to int <br>
 * 7/7/2004	hjs		Change the internal representation from Char to Integer <br>
 * 
 * 9/14/2005 (v1.0.4) hjs	Changes to properly handle discretization of multiple 
 * 							observation files <br>
 * 
 * 11/28/2005 (v1.0.5) hjs	Correction to interval discretization <br>
 * 
 * 10/25/2005 (v2.0) hjs	Eliminate the references to the (never supported)
 * 							"related observations" code. (This was old code in
 * 							desperate need of cleanup.) <br>
 * 
 * 10/28/2005 (v2.0) hjs	Add additional field for access to the overall maximum
 *							value that the variables can assume. <br>
 * 
 * 3/4/2008 (v2.2) hjs      Pull out JZOOWildCardFilter into separate class. <br>  
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 * 
 */
public class ObservationsAsMatrix extends Observations {
	
    // Allow public access to this field, for performance reasons
    public int[][][] observationDataPoints; 
    
    /**
     * ObservationsAsMatrix constructor.
     * 
     * @param _processData The settings that define the search process.
     */
	public ObservationsAsMatrix( 
	        final Settings _processData ) throws Exception {

        super( _processData );
        
        boolean isDataValid = validateRequiredData();
        
        // We check if there were any problems. If we found any, we cannot continue
        // setting up the searcher.
        if ( !isDataValid ) {
            
            throw new BanjoException( BANJO.ERROR_CHECKPOINTTRIGGER, 
                    processData.compileErrorMessages().toString() );
        }
	}

    /**
     * Validates the settings values required for loading the observations.
     * 
     * @return Returns the boolean flag that indicates whether a crucial setting
     * could not be validated.
     */
    private boolean validateRequiredData() throws Exception {

        boolean isDataValid = true;
        
        // no specific code for this class
        
        return isDataValid;
    }

    /**
     * Loads the data from the user-specified observations files.
     * 
     * @param _processData The settings that define the search process.
     */
    public void loadData( final Settings _processData ) throws Exception {
        
		// ------------------------
		// Process the supplied observations reference
		// ------------------------

		String strInputDirectory = processData.getValidatedProcessParameter( 
		        BANJO.SETTING_INPUTDIRECTORY );
		File inputDirectory = new File( strInputDirectory );
		if ( !inputDirectory.isDirectory() ) {
		    
		    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
		            "'" + strInputDirectory + "' is not a valid (input) directory." );
		}
		//
		String strOutputDirectory = processData.getValidatedProcessParameter( 
		        BANJO.SETTING_OUTPUTDIRECTORY );
		File outputDirectory = new File( strOutputDirectory );
		if ( !outputDirectory.isDirectory() ) {
		    
		    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
		            "'" + strOutputDirectory + "' is not a valid (output) directory." );
		}
		
        
        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            BANJO.FEEDBACK_NEWLINE + 
                            BANJO.DATA_TRACEMEMORYLOADINGOBSERVATIONS )) );
        }
		
		//
		final String strObservationsFile = processData.getValidatedProcessParameter( 
		        BANJO.SETTING_OBSERVATIONSFILE );
		
		// Store the names in an ordered way (even though this is only used
		// - and useful) for the case where the observations files are "related"
		String[] strArrayObservationFiles = null;
		int obsFileCount = 0;
		int obsFileIndex = 0;
		
	    // Process potentially multiple files
		StringTokenizer obsFileTokenizer = 
	        new StringTokenizer( strObservationsFile, BANJO.DELIMITER_DEFAULT_LIST );

		String strNextFile;
		String[] strTempWildcardFiles;
		FilenameFilter filter;

		// TODO: PULL out into separate method 
		
		// Are wildcards used to describe the files?
		if ( strObservationsFile.indexOf( BANJO.DEFAULT_WILDCARDINDICATOR ) < 0 ) {
		    
		    // Without wildcards we have as many files as there are tokens
		    obsFileCount = obsFileTokenizer.countTokens();
            
            if ( obsFileCount == 0 ) {
                
                // Can't continue without an observations file
                throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT, 
                        "The input for the observations file setting (='" +
                        strObservationsFile + "') did not yield any usuable file." );
            }
            
		    strArrayObservationFiles = new String[ obsFileCount  ];
		    
		    while ( obsFileTokenizer.hasMoreTokens() ) {
			    
			    // We simply add each file name to the array
			    strNextFile = obsFileTokenizer.nextToken().trim();
			    strArrayObservationFiles[ obsFileIndex ] = 
			        strNextFile.trim();
			    obsFileIndex++;
		    }
		}
		else {
		    
		    // Wildcard case: allow positive wildcards (files to add)
		    // and negative wildcards (files to omit)
			Set wildcardObservationFiles = new HashSet();
			Set omittedWildcardObservationFiles = new HashSet();
	    
			while ( obsFileTokenizer.hasMoreTokens() ) {
			    
			    // Note: we will validate the individual files (i.e., whether
			    // they exist) when we try to load the data
			    
			    strNextFile = obsFileTokenizer.nextToken().trim();
			    if ( strNextFile.charAt(0) == '-' ) {
					
			        filter = new JZOOWildCardFilter( 
			                strNextFile.substring(1).trim() );
			    }
			    else {
				
			        filter = new JZOOWildCardFilter( strNextFile );
			    }
		        
		        strTempWildcardFiles = inputDirectory.list( filter );
		        
		        if ( strTempWildcardFiles.length < 1 ) {
		            
		            throw new BanjoException( 
		                    BANJO.ERROR_BANJO_USERINPUT, 
		                    "\nThe wildcard specification '" + strNextFile +
		                    "' for the observation file(s) did not result in any " +
		                    "valid file." );
		        }
		        
			    // Add each file name to the respective set
			    for ( int i=0; i < strTempWildcardFiles.length; i++ ) {
			        
			        if ( strNextFile.charAt(0) == '-') {
			            
			            omittedWildcardObservationFiles.add( strTempWildcardFiles[i] );
			        }
			        else {
			            
			            wildcardObservationFiles.add( strTempWildcardFiles[i] );
			        }
			    }
			}
		    		    
			if ( !wildcardObservationFiles.isEmpty() ) {
			    
			  	// Take away any files
				if ( !omittedWildcardObservationFiles.isEmpty() ) {
				
				    wildcardObservationFiles.removeAll( 
				            omittedWildcardObservationFiles );

				    String omittedWildcardFiles = new String();
					Iterator wildcardIterator = 
					    omittedWildcardObservationFiles.iterator();
					
					omittedWildcardFiles = (String) wildcardIterator.next();
					String strNextWildcardFile = null;
					while ( wildcardIterator.hasNext() ) {
					    
					    strNextWildcardFile = (String) wildcardIterator.next();
					    omittedWildcardFiles += BANJO.DELIMITER_DEFAULT_LIST + strNextWildcardFile;
					}
					
					processData.setDynamicProcessParameter( 
					        BANJO.DATA_OMITTEDWILDCARDOBSERVATIONSFILES, 
					        omittedWildcardFiles );
				}
			}
			
			// Finally, create the array with the observation files 
			// (need to check if still non-empty)
			if ( !wildcardObservationFiles.isEmpty() ) {

				obsFileCount = wildcardObservationFiles.size();
				strArrayObservationFiles = new String[ 
				         wildcardObservationFiles.size() ];
			    String wildcardFiles = new String();
				Iterator wildcardIterator = wildcardObservationFiles.iterator();
				
				wildcardFiles = (String) wildcardIterator.next();
				strArrayObservationFiles[0] = wildcardFiles;
				int fileIndex = 1;
				String strNextWildcardFile = null;
				while ( wildcardIterator.hasNext() ) {
				    
				    strNextWildcardFile = (String) wildcardIterator.next();
				    wildcardFiles += BANJO.DELIMITER_DEFAULT_LIST + strNextWildcardFile;
				    strArrayObservationFiles[fileIndex] = strNextWildcardFile;
				    fileIndex++;
				}
			    processData.setDynamicProcessParameter( 
				        BANJO.DATA_WILDCARDOBSERVATIONSFILES, wildcardFiles );
			}
			else {
			    
			    // No valid files found: can't continue
			    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT, 
			            "The input for the observations file setting (='" +
			            strObservationsFile + "') did not yield any usuable file." );
			}
		}

		// -------------------------
		// Load the observation data
		// -------------------------
		
		// let the preprocessedObservations handle the loading

		preprocessedObservationsWithFileInfo = 
		    new PreprocessedObservationsWithFileInfo[ obsFileCount ];
		
		
		String strFileToLoad = null;
		PreprocessedObservationsWithFileInfo basicObservationsWithFileInfo;
		
		for ( int i = 0; i < obsFileCount; i++ ) {
		    		    
		    strFileToLoad = strArrayObservationFiles[i];
		    
            // Prep and load the observations one-by-one
            basicObservationsWithFileInfo = new PreprocessedObservationsWithFileInfo( processData );
		    
            basicObservationsWithFileInfo.loadObservations( strInputDirectory, strFileToLoad );
		    
		
            // "Store" the just loaded observations
			preprocessedObservationsWithFileInfo[i] = basicObservationsWithFileInfo;
            
            if ( variableNames == null ) {
            
                // and load the variable names if any
                String[] tmpVariableNames = basicObservationsWithFileInfo.getVariableNames();
                if ( tmpVariableNames.length == varCount ) {
                    
                    variableNames = tmpVariableNames;
                }
            }
		}		
		
		combinePreprocessedObservations( preprocessedObservationsWithFileInfo );

        // Prepare the data based on the rules the user specifies
        prepareData();

        String strObservationCountSpecifiedByUser = "";
        
        if ( processData.isSettingValueValid( BANJO.SETTING_OBSERVATIONCOUNT )) {
        
            strObservationCountSpecifiedByUser = processData.getValidatedProcessParameter(
                BANJO.SETTING_OBSERVATIONCOUNT );
        }
		
		// If the user has supplied us with the number of observations, then we
		// compare, otherwise we get it from the observations object
		if ( strObservationCountSpecifiedByUser.equals( Integer.toString( 
		        BANJO.BANJO_OBSERVATIONCOUNT_NONUMBERSUPPLIED )) ||
		        strObservationCountSpecifiedByUser.equals( "" ) ) {

            specifiedObservationCount = observationRowCount;
		}
		else {

            specifiedObservationCount = 
                Integer.parseInt( strObservationCountSpecifiedByUser );

            if ( numberOfObservationsFiles > 1 ) {
                
                // If there's more than one observation file, how would we get a
                // ordered processing of the files? Hence:
                throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
                        "\nThe observation count cannot be supplied in the settings file " + 
                        "when more than one observations file is specified." );
            }
            
			// Now check the user-supplied observation count against the combined
			// total of rows of data supplied in the observation file(s)
			if ( specifiedObservationCount > observationRowCount ) {
                
                throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT, 
                        "\nThe observation count supplied in the settings file (=" + 
                        specifiedObservationCount + ") cannot be larger than " +
                        "the number of observations supplied in the observation file (=" +
                        observationRowCount + ")." );
			}
		}
        
        
        // Compute the "effective" observationCount (i.e., the number of observations
        // for which we have a complete time series)
        if ( maxMarkovLag == 0 ) {
        
            observationCount = specifiedObservationCount;
        }
        else {
            
            // Need to adjust for the lag (to be consistent with the definition
            // of specifiedObservationCount as a data "row" based number)
            observationCount = specifiedObservationCount;// - maxMarkovLag;
            if ( observationCount > observedObservationCount ) {
                observationCount = observedObservationCount;
            }
        }
        
        // Finally, compute the max. value counts
        computeMaxValueCounts();
        
        // Store the "to be used" observation count for use by the search components
        processData.setDynamicProcessParameter( 
                BANJO.DATA_OBSERVATIONCOUNT, 
                ( new Integer( observationCount )).toString() );
        
        // Store the user specified observation count for display to the user
        processData.setDynamicProcessParameter( 
                BANJO.DATA_SPECIFIEDOBSERVATIONCOUNT, 
                ( new Integer( specifiedObservationCount )).toString() );
	    
		// Store the observed observation and row count for display to the user
	    processData.setDynamicProcessParameter( 
	            BANJO.DATA_OBSERVEDOBSERVATIONCOUNT, 
	            ( new Integer( observedObservationCount )).toString() );
	     
	    processData.setDynamicProcessParameter( 
	            BANJO.DATA_OBSERVEDOBSERVATIONROWCOUNT, 
	            ( new Integer( observationRowCount )).toString() );
        
        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            BANJO.FEEDBACK_NEWLINE + 
                            BANJO.DATA_TRACEMEMORYLOADINGOBSERVATIONSCOMPLETED )) );
        }
    }

    /**
     * Combines the loaded observations into a single large array.
     * 
     * @param _preprocessedObservations An array of PreprocessedObservations, each obtained, e.g., from a file.
     */
	protected void combinePreprocessedObservations( 
	        PreprocessedObservations[] _preprocessedObservations ) throws Exception {
	    
	    int sumOfObservationCounts = 0;
        
        numberOfObservationsFiles = _preprocessedObservations.length;
		
		
		// Add up the row counts from all the individual data sets (files) 
		for ( int i=0; i< _preprocessedObservations.length; i++ ) {
		    
		    sumOfObservationCounts += _preprocessedObservations[i].getObservationCount();
		}

		// The rowCounts are a "raw" count
		this.observationRowCount = sumOfObservationCounts;
        
		// The observation count is (effectively only for DBNs) an adjusted row count
        observedObservationCount = sumOfObservationCounts 
                - numberOfObservationsFiles * maxMarkovLag;
        /////
        
        // Now we know how many data points we'll need
        observationDataPoints = new int[observedObservationCount]
			                             [varCount][maxMarkovLag+1];
		
        // .. and how many "raw" data points were loaded
        combinedObservationDataPointsAsLoaded = 
            new String[ observationRowCount ][ varCount ];
        
        // Combine all raw (string) data to be able to discretize the data set
        int offset = 0;
        for ( int i=0; i < _preprocessedObservations.length; i++ ) {
            
            for ( int j=0; j < ( _preprocessedObservations[i])
                        .getObservationCount(); j++ ) {
                
                for ( int k=0; k < varCount; k++ ) {
                    
                    combinedObservationDataPointsAsLoaded[ offset + j ][ k ] = 
                        ( _preprocessedObservations[ i ] )
                                .getStrRawObservationDataPoints()[ j ][ k ];
                }
            }
                
            offset += ( _preprocessedObservations[ i ]).getObservationCount();
        }
        
        combinedPreprocessedObservationsAsLoaded = new PreprocessedObservations( processData );
        
        combinedPreprocessedObservationsAsLoaded.setStrRawObservationDataPoints( 
                combinedObservationDataPointsAsLoaded );
	}

    /**
     * Prepares the observations by applying an (optional) discretization.
     */
    public void prepareData() throws Exception {

		PreprocessedObservations singleObservation;
		
		// -----------------------
		// Discretize the data set
		// -----------------------
		
		DataPreparerI dataPreparer = new DataPreparerBasic( 
		        processData, combinedPreprocessedObservationsAsLoaded );

        // Discretize the (combined, loaded) data in the dataPreparer
        dataPreparer.discretizeData();
		
		
		// Get the dataPreparer to create the data report
		dataPreparer.prepareReport();

        
        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            BANJO.FEEDBACK_NEWLINE + 
                            BANJO.DATA_TRACEMEMORYLOADINGOBSERVATIONSINPROGRESS )) );
        }

        // -----------------------------
        // Finally, map the discrete values back to the original data
        // -----------------------------
        
        int obsRowCount;
        int discreteValue;
        // Simple cache to reduce number of calls
        String[][] originalDataPoints;
        
        // hjs  Note: by defining the dimensions of discretizedDataPoints in the order of
        // [numberOfFiles][numberOfObservations][numberOfVariables] we should have decent
        // memory allocation from the JVM (we want the largest number to be the last)
        int[][][] discretizedDataPoints = new int[preprocessedObservationsWithFileInfo.length][][];
        
        for ( int obsFileIndex=0; obsFileIndex < preprocessedObservationsWithFileInfo.length; obsFileIndex++ ) {
            
            // Another quick cache 
            originalDataPoints = 
                preprocessedObservationsWithFileInfo[obsFileIndex].getStrRawObservationDataPoints();
            obsRowCount = preprocessedObservationsWithFileInfo[obsFileIndex].getObservationCount();
            discretizedDataPoints[obsFileIndex] = new int[obsRowCount][];
            
            for ( int obsRowIndex=0; obsRowIndex < obsRowCount; obsRowIndex++ ) {

                discretizedDataPoints[obsFileIndex][obsRowIndex] = new int[varCount];
                for ( int varIndex=0; varIndex < varCount; varIndex++ ) {
                    
                    discreteValue = dataPreparer.getDiscretizedValue( 
                                varIndex, originalDataPoints[obsRowIndex][varIndex] );
                    discretizedDataPoints[obsFileIndex][obsRowIndex][varIndex] = discreteValue;
                }
            }
        }
        
			    
	    // This loop assigns the discretized data points to the final data set,
		// with its extra dimension for the lag values, which allows us to call up
		// data points without having to perform index computations in the main 
		// search code.
        int firstDataRow = maxMarkovLag;
        int lastDataRow;
	    int rowIndex = 0;
	    for ( int obsFileIndex=0; obsFileIndex < discretizedDataPoints.length; obsFileIndex++ ) {
	    	
			// Assign the starting and end points for the rows that need
			// to be assigned to the "lag" dimension of our main data set
            // [Reminder: when the lag is maxMarkovLag, there are maxMarkovLag rows
            // ahead of the firstDataRow (because we are starting with lag 0)]
            
            // The (locally) last data row will be the last row in the currently
            // processed file
            lastDataRow = discretizedDataPoints[obsFileIndex].length;

            // We cannot start at "lag 0"
			for ( int rowIndexForNthObservationFile=firstDataRow; 
    				rowIndexForNthObservationFile < lastDataRow; 
    				rowIndexForNthObservationFile++ ) {

                // Process one row at a time, filling in the "data slices" from 0 to 
                // maxMarkovLag, over all the variables
				for ( int varIndex=0; varIndex<varCount; varIndex++ ) {
				
                    // Set one data slice for each lag (from 0 to maxMarkovLag)
					for ( int lagIndex=0; lagIndex<maxMarkovLag+1; lagIndex++ ) {
					    
						observationDataPoints[rowIndex][varIndex][lagIndex] =
                            discretizedDataPoints[obsFileIndex]
                                                  [rowIndexForNthObservationFile - lagIndex]
                                                   [varIndex];
					}
				}
			
				rowIndex++;
			}
	    }
	}

    /**
     * Get the maximum value that a specified variable can assume.
     * 
     * @return The maximum value assumed by the specified variable.
     */
	public int getMaxValueCount( final int _nodeID ) {
		return maxValueCount[_nodeID];
	}

    /**
     * Get the maximum value that any variable can assume.
     * 
     * @return The maximum value assumed by any variable.
     */
	public int getMaxValueCount() {
	    return maxValue;
	}

    /**
     * Computes the maximum values for all the variables in the data set given by
     * the current observations.
     * 
     */
	public void computeMaxValueCounts() throws Exception {
		
		// init the maxValueCount array (i.e., how many values a variable attains)
		int currentObsValue;
		
		for (int i=0; i<observationCount; i++) {
		    
			// Go through the observationDataPoints and find the number of values
			// for each variable (Note: since the values are normalized between
			// 0 and max, we only need to keep track of max)
		    // We only need to examine the data points [.][.][0], because all
		    // other points are obtained via shifts
			for (int j=0; j<varCount; j++) {

			    // observation values are from 0.. (max-1), so need to shift by 1:
				currentObsValue = observationDataPoints[i][j][0]+1;
				
				if ( currentObsValue > this.maxValueCount[j] ) {
				    
					this.maxValueCount[j] = currentObsValue;
				}
				if ( currentObsValue > maxValue ) {
				    
				    maxValue = currentObsValue;
				}
			}
		}
		
		// Sanity check. Note that the value of CONFIG_MAXVALUECOUNT can be changed 
		// (currently in code only, though)
		if ( maxValue > BANJO.CONFIG_MAXVALUECOUNT ) {
		    
		    throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT, 
		            "The maximum number of states that a variable can assume is " +
		            "limited to " + BANJO.CONFIG_MAXVALUECOUNT + "." );
		}
	}
	
	public String toString() {
		
		StringBuffer tmpStr = new StringBuffer( 
		        BANJO.BUFFERLENGTH_STRUCTURE_LARGE );
		
		for (int i=0; i<observationRowCount; i++) {
			for (int j=0; j<varCount; j++) {

				tmpStr.append( observationDataPoints[i][j][0] );
				tmpStr.append( "  " );
			}
			tmpStr.append( "\n" );
		}
		return tmpStr.toString();
	}

    // Alternative format for toString()
//      public String toString() {
//      
//      StringBuffer collectedSettings = 
//          new StringBuffer( BANJO.BUFFERLENGTH_STAT_INTERNAL );
//      
//      collectedSettings.append( "Observation data points:\n");
//      
//      for (int i=0; i<observationDataPoints.length ; i++) {
//
//          collectedSettings.append( "\n" );
//          
//          for (int j=0; j<observationDataPoints[i].length ; j++) {
//              
//              collectedSettings.append( "        " );
//              
//              for (int k=0; k<observationDataPoints[i][j].length ; k++) {
//
//                  collectedSettings.append( " " );
//                  collectedSettings.append( observationDataPoints[i][j][k] );
//              }
//          }
//      }
//              
//      return collectedSettings.toString();
//  }

	public void setObservationValue(
	        final int _observationIndex, 
	        final int _variableIndex, 
	        final int _MarkovLagIndex, 
			final int _observationValue) {
		
		observationDataPoints[_observationIndex][_variableIndex][_MarkovLagIndex] =
		    _observationValue;
	}
	
	public int getObservationValue(
	        final int _observationRow, final int _variableID, final int _MarkovLag ) {
		
		return observationDataPoints[_observationRow][_variableID][_MarkovLag];
	}
	
	/**
	 * @return Returns the observationRowCount.
	 */
	public final int getObservationRowCount() {
		return observationRowCount;
	}

	/**
	 * @param _observations The observationDataPoints to set.
	 */
	public void setObservationDataPoints( final int[][][] _observations ) {
		this.observationDataPoints = _observations;
	}

	/**
	 * @return Returns the varCount.
	 */
	public final int getVarCount() {
		return varCount;
	}
		
	public String getValueCountList() {
		
		StringBuffer valueList = new StringBuffer(
		        BANJO.BUFFERLENGTH_STRUCTURE );
		valueList.append( "Value counts:\n" );
		
		for (int i=0; i< getVarCount(); i++) {
			valueList.append( i );
			valueList.append( ": " );
			valueList.append( this.maxValueCount[i] );
			valueList.append( "\n" );
		}
		
		return valueList.toString();
	}
    /**
     * @return Returns the observationCount.
     */
    public int getObservationCount() {
        return observationCount;
    }
    
    /**
     * @return Returns the preprocessedObservationsWithFileInfo.
     */
    public PreprocessedObservations[] getObservationsArrayWithFileInfo() {
        return preprocessedObservationsWithFileInfo;
    }
    

    /**
     * Releases the data structures used for storing the original "raw" data 
     * (as loaded from file in String format).
     */
    public void releaseStagingData() {
        
        // Only call this method when you are absolutely sure that you won't
        // need the various internal observations data anymore
        
        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            BANJO.FEEDBACK_NEWLINE + 
                            BANJO.DATA_TRACEMEMORYOBSERVATIONSTAGING )) );
        }
        
        if ( preprocessedObservationsWithFileInfo != null ) {
            
            for ( int i=0; i<preprocessedObservationsWithFileInfo.length; i++ )
                preprocessedObservationsWithFileInfo[i] = null;
            
            preprocessedObservationsWithFileInfo = null;
        }
        
        combinedObservationDataPointsAsLoaded = null;
        combinedPreprocessedObservationsAsLoaded = null;
        observationDataPoints = null;
        
        if ( BANJO.DEBUG && BANJO.TRACE_MEMORYUSE ) {

            System.out.println(
                    new StringBuffer( StringUtil.compileMemoryInfo( 
                            BANJO.FEEDBACK_NEWLINE + 
                            BANJO.DATA_TRACEMEMORYOBSERVATIONSTAGINGRELEASED )) );
        }
    }

    /**
     * @return Returns the variableNames.
     */
    public String[] getVariableNames() {
        return variableNames;
    }

    /**
     * @param _variableNames The variableNames to set.
     */
    public void setVariableNames( String[] _variableNames ) {
        this.variableNames = _variableNames;
    }

}

