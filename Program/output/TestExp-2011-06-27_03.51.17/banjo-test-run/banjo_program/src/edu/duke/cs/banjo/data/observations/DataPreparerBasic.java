/*
 * Created on Dec 1, 2005
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

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import edu.duke.cs.banjo.data.settings.*;
import edu.duke.cs.banjo.utility.BANJO;
import edu.duke.cs.banjo.utility.BanjoException;
import edu.duke.cs.banjo.utility.StringUtil;
/**
 * Implements a basic dataPreparer.
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Dec 1, 2005
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */

public class DataPreparerBasic extends DataPreparer {

    // temp only
    private int observationRowCount;
    
    // Data needed for setting up the data preparation
	private String[] discretizationType;
	private int[] discretizationPoints;
	private int[] intervals;
	private double[] minObsValue;
	private double[] maxObsValue;
	
	private SortedMap[] discreteValuesMap;
	private SortedMap[] originalValuesMap;
    
    
    public DataPreparerBasic( Settings _processData, 
            PreprocessedObservations _observationsData ) throws Exception {
        
        super( _processData, _observationsData );

        // Use arrays for storing the data for each problem domain variable
    	discretizationType = new String[ varCount ];
    	discretizationPoints = new int[ varCount ];
    	intervals = new int[ varCount ];
	    minObsValue = new double[ varCount ];
	    maxObsValue = new double[ varCount];
	    
		discreteValuesMap = new SortedMap[ varCount ];
		originalValuesMap = new SortedMap[ varCount ];
    	
		observationRowCount = observationsToDiscretize.getObservationCount();
    }
    
    public void discretizeData() throws Exception {
        
        String[][] strRawObservationDataPoints = 
            observationsToDiscretize.getStrRawObservationDataPoints();
        
        PreprocessedObservations discretizedObservations =
                                new PreprocessedObservations( processData );
        
		// -------------------------
		// Set up the discretization
		// -------------------------
	
		final int obsRowCount = observationsToDiscretize.getObservationCount();
	    
	    for ( int z=0; z<varCount; z++ ) 
	        minObsValue[z] = BANJO.BANJO_LARGEINITIALVALUEFORMINIMUM;
	    
	    for ( int z=0; z<varCount; z++ ) 
	        maxObsValue[z] = BANJO.BANJO_SMALLINITIALVALUEFORMAXIMUM;
	    
	    double dblCurrentObsValue;
	    Double tmpDbl;
		for ( int z=0; z<varCount; z++ )
		    originalValuesMap[z] = new TreeMap();	
		
		String strDiscretizationChoice = 
		    processData.getDynamicProcessParameter( 
		        BANJO.DATA_DEFAULTDISCRETIZATIONCHOICE );
		
		int defaultDiscretizationPoints = Integer.parseInt(  
				    processData.getDynamicProcessParameter( 
					BANJO.DATA_DEFAULTDISCRETIZATIONPOINTS ) );
		
		if ( defaultDiscretizationPoints > BANJO.CONFIG_MAXVALUECOUNT ) {
            
            throw new BanjoException( BANJO.ERROR_BANJO_USERINPUT,
                    "The number of points ('" + defaultDiscretizationPoints +
                    "') specified in the discretization exception '" + 
                    strDiscretizationChoice + defaultDiscretizationPoints +
                    "'\ncannot exceed Banjo's restriction " +
                    "(via configuration) on " +
                    "the max. number of values \n" +
                    "that a variable can have ('" +
                    BANJO.CONFIG_MAXVALUECOUNT +
                    "')." );
        }
		
		String strDiscretizationExceptions = 
		    processData.getValidatedProcessParameter( 
		        BANJO.SETTING_DISCRETIZATIONEXCEPTIONS );
		
		String areExceptionsValid = processData.getSettingItemValueAsValidated(
		        	BANJO.SETTING_DISCRETIZATIONEXCEPTIONS );
		
	    boolean discretizationFlag = false;	    
	    
		// ----------------------------------------------
		// Set up the optional discretization of the data
	    // (Parse the user's choices)
		// ----------------------------------------------
		
		// Part 1: Apply the discretization policy
		for ( int item=0; item<varCount; item++ ) {
	    	
		    discretizationType[ item ] = strDiscretizationChoice;
		    discretizationPoints[ item ] = defaultDiscretizationPoints;
			intervals[ item ] = defaultDiscretizationPoints - 1;	
		}
		
		if ( !strDiscretizationChoice.equals( BANJO.UI_DISCRETIZATIONNONE )) {
		    
	        discretizationFlag = true;
		
			// Part 1: Apply the (optional) exceptions to the discretization policy
			if ( !strDiscretizationExceptions.equals( BANJO.BANJO_NOVALUESUPPLIED_STRING ) &&
			        !areExceptionsValid.equals( BANJO.DATA_SETTINGNOTFOUND) && 
			        !areExceptionsValid.equals("") ) {
	
		        String strExceptionTypes = processData.getDynamicProcessParameter( 
			            BANJO.DATA_DISCRETIZATIONEXCEPTION_TYPELIST );
		        
		        String strExceptionPoints = processData.getDynamicProcessParameter( 
			            BANJO.DATA_DISCRETIZATIONEXCEPTION_POINTSLIST );		        
	
		        Pattern exceptionPattern = Pattern.compile( 
		                BANJO.PATTERN_DISCRETIZATIONEXCEPTIONS, Pattern.COMMENTS );
		        String[] exceptionsTypesList = exceptionPattern.split( strExceptionTypes );
		        String[] exceptionsPointsList = exceptionPattern.split( strExceptionPoints );
		        
		        for ( int exceptionIndex=0; exceptionIndex<varCount; exceptionIndex++ ) {
	
		            String strExceptionItemType = exceptionsTypesList[ exceptionIndex ];
		            
		            if ( !strExceptionItemType.equals( BANJO.UI_DISCRETIZATIONNONE ) ) {
		                
			            String strExceptionItemPoints = exceptionsPointsList[ exceptionIndex ];
			            
			            discretizationType[ exceptionIndex ] = strExceptionItemType;
					    discretizationPoints[ exceptionIndex ] = 
                            Integer.parseInt( strExceptionItemPoints );
						intervals[ exceptionIndex ] = 
                            Integer.parseInt( strExceptionItemPoints ) - 1;
		            }
		        }
		    }
		}
		
		
	    // Do the pre-processing for the discretization and the optional
	    // data (discretization) report
        
        // We need to use all data points (hence observationRowCount instead 
        // of observationCount)
        String[][] originalDataPoints = strRawObservationDataPoints;
            
        for ( int j=0; j < this.observationRowCount; j++ ) {

            for ( int k=0; k < varCount; k++ ) {
            
                dblCurrentObsValue = Double.parseDouble(
                        originalDataPoints[ j ][ k ] );

                // First check if the current value is a new max or min 
                // (used in the report)
                if ( dblCurrentObsValue < minObsValue[ k ] ) 
                    minObsValue[ k ] = dblCurrentObsValue;
                if ( dblCurrentObsValue > maxObsValue[ k ] ) 
                    maxObsValue[ k ] = dblCurrentObsValue;
                
                // update or place in the valueMap for discretization
                if ( originalValuesMap[ k ].containsKey( 
                        new Double( dblCurrentObsValue )) ) { 
                    
                    ( (Counter) originalValuesMap[ k ].get( 
                            new Double( dblCurrentObsValue ) )).i++;
                }
                else {
                    
                    originalValuesMap[ k ].put( new Double( dblCurrentObsValue ) , 
                            new Counter() );
                }
            }
        }

        // May not want this data for the report output, but it's useful when
		// examining the discretization more closely:
	    if ( BANJO.DEBUG && BANJO.TRACE_DISCRETIZATION ) {
	        
			System.out.println();
			for ( int z=0; z<varCount; z++ ) {
				System.out.print( "Var= " + z );
				System.out.print( ", min= " + minObsValue[z] );
				System.out.print( " max= " + maxObsValue[z] );
				System.out.print( "  " );
				System.out.println( originalValuesMap[z] );
			}
	    }
	    
	    // --------------------------------
		// Discretize the combined data set
	    // --------------------------------
	    
		for ( int z=0; z<varCount; z++ ) 
		    discreteValuesMap[z] = new TreeMap();
	    
	    DiscretizerI discretizer;
	    for ( int z=0; z<varCount; z++ ) {
	        
		    if ( discretizationType[z].equalsIgnoreCase( 
	                BANJO.UI_DISCRETIZATIONBYINTERVAL ) ) {
		        
		        // set up the required discretizer
		        discretizer = new DiscretizerInterval(
		                originalValuesMap[z],
		                discretizationPoints[z] );
		        
		        // Let the discretizer compute the discretized values
		        discreteValuesMap[z] = discretizer.computeValueMap();
		    }
		    else if ( discretizationType[z].equalsIgnoreCase( 
	                BANJO.UI_DISCRETIZATIONBYQUANTILE ) ) {

		        // set up the required discretizer
		        discretizer = new DiscretizerQuantile(
		                originalValuesMap[z],
		                discretizationPoints[z],
		                obsRowCount );

		        // Let the discretizer compute the discretized values
		        discreteValuesMap[z] = discretizer.computeValueMap();
		    }
		    else if ( discretizationType[z].equalsIgnoreCase( 
	                BANJO.UI_DISCRETIZATIONNONE ) ) {
		        
		        // set up the trivial discretizer
		        discretizer = new DiscretizerTrivial(
		                originalValuesMap[z],
		                discretizationPoints[z],
		                obsRowCount );

		        // Let the discretizer compute the discretized values
		        discreteValuesMap[z] = discretizer.computeValueMap();
		    }
		    else {
		        
                throw new BanjoException( 
                        BANJO.ERROR_BANJO_DEV, 
                        "\n *** (DataPreparerBasic.discretizeData) " +
                        "Invalid discretization type '" + discretizationType[z] +
                        "'." );
		    }
	    }
    }
    
    //
    public int getDiscretizedValue( int variableIndex, String originalValue ) throws Exception {

        int discreteValue = -1;
        
        try {
            
            Double tmpDbl = new Double( originalValue );
            
            discreteValue = (( Counter ) discreteValuesMap[ variableIndex ].get( tmpDbl )).i;
        }
        catch (Exception e) {
            
            throw new BanjoException ( BANJO.ERROR_BANJO_DEV, 
                    "(DataPreparerBasic.getDiscretizedValue) " +
                    "Development error: cannot retrieve discretized value " +
                    "for variable i=" + variableIndex +
                    " and value=" + originalValue );
        }
        
        return discreteValue;
    }

    public Object prepareReport() throws Exception {
        
        StringBuffer dataReport = new StringBuffer( 
                BANJO.BUFFERLENGTH_STAT );
        boolean discretizationFlag = false;
        Double DblCurrentObsValue;

        SortedMap[] tmpOriginalValuesMap;
        tmpOriginalValuesMap = new SortedMap[ varCount ];

        // For temp. output only:
        for ( int z=0; z<varCount; z++ )
            tmpOriginalValuesMap[z] = new TreeMap();
        
        for ( int z=0; z<varCount; z++ ) {
            
            tmpOriginalValuesMap[z].putAll( originalValuesMap[z] );
        }
        
	    // Create the optional data report
	    if ( !processData.getValidatedProcessParameter( 
		        BANJO.SETTING_DISCRETIZATIONREPORT ).equalsIgnoreCase( 
			                BANJO.UI_DATAREPORT_NO )) {
	
	        final int usedTabStops = 7;
	        int[] tabStop = new int[ usedTabStops];
	        int[] rightDiff = new int[ usedTabStops];
		    String prefix = "\n";
			String newLinePlusPrefix = "\n" + BANJO.FEEDBACK_DASH;
			String slash = BANJO.FEEDBACK_SEPARATOR_VERTICAL;
			int lineLength = BANJO.FEEDBACK_LINELENGTH;
			tabStop[0] = "Variable".length();
			rightDiff[0] = 4;
			tabStop[1] = "Discr.".length();
			rightDiff[1] = 3;
			tabStop[2] = "Min. Val.".length();
			rightDiff[2] = 3;
			tabStop[3] = "Max. Val.".length();
			rightDiff[3] = 3;
			tabStop[4] = " Orig. ".length()-1;
			rightDiff[4] = 3;
			tabStop[5] = " Used  ".length()-1;
			rightDiff[5] = 3;
			tabStop[6] = "Original value counts".length();
			rightDiff[6] = 3;
								
			// Add the header "indicator band"
			dataReport.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
			        substring( 0, lineLength )); 
		    dataReport.append( StringUtil.formatRightLeftJustified( 
		            newLinePlusPrefix, " Pre-processing", 
			        "Discretization report", null, lineLength ) );
		    dataReport.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
			        substring( 0, lineLength )); 
	
		    String headerLine1 = " Variable | Discr. | Min. Val. | Max. Val. |" +
					"  Orig. |  Used  |";
		    String headerLine2 = "          |        |           |           |" +
					" points | points |";
		    
		    if ( processData.getValidatedProcessParameter( 
			        BANJO.SETTING_DISCRETIZATIONREPORT ).equalsIgnoreCase( 
			                BANJO.UI_DATAREPORT_WITHMAPPEDVALUES ) || 
                processData.getValidatedProcessParameter( 
    			        BANJO.SETTING_DISCRETIZATIONREPORT ).equalsIgnoreCase( 
    			                BANJO.UI_DATAREPORT_WITHMAPPEDANDORIGINALVALUES )) {
		        
		        // Generally, if no variable is tagged to be discretized, then
		        // we don't want to do anything
			    if ( discretizationFlag ) {
			        
			        headerLine1 += "  Mapped values";
			        headerLine2 += " |";
			    }
		    }
		    dataReport.append( StringUtil.formatRightLeftJustified( 
			        prefix, headerLine1,
			        "", null, lineLength ) );
		    dataReport.append( StringUtil.formatRightLeftJustified( 
			        prefix, headerLine2,
			        "", null, lineLength ) );
		    dataReport.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
			        substring( 0, lineLength )); 
		    
		    for ( int z=0; z< varCount; z++ ) {
		    					        
			    dataReport.append( StringUtil.formatRightLeftJustified( 
				        prefix, "", Integer.toString( z ), null, tabStop[0] ) );
			    dataReport.append( StringUtil.formatRightLeftJustified( 
			            "", "", slash, null, rightDiff[0] ) );
			    
			    if ( discretizationType[z].equalsIgnoreCase( 
			            BANJO.UI_DISCRETIZATIONNONE )) {
			        
				    dataReport.append( StringUtil.formatRightLeftJustified( 
				            "", "", discretizationType[z], null, tabStop[1] ) );
			    }
			    else {
			        
			        dataReport.append( StringUtil.formatRightLeftJustified( 
				            "", "", discretizationType[z] + 
				            discretizationPoints[z], null, tabStop[1] ) );
			    }
			    dataReport.append( StringUtil.formatRightLeftJustified( 
			            "", "", slash, null, rightDiff[1] ) );
			    
			    dataReport.append( StringUtil.formatRightLeftJustified( 
			            "", "", Double.toString( minObsValue[z] ), 
				        null, tabStop[2] ) );
			    dataReport.append( StringUtil.formatRightLeftJustified( 
			            "", "", slash, null, rightDiff[2] ) );
			    
			    dataReport.append( StringUtil.formatRightLeftJustified( 
			            "", "", Double.toString( maxObsValue[z] ), 
				        null, tabStop[3] ) );
			    dataReport.append( StringUtil.formatRightLeftJustified( 
			            "", "", slash, null, rightDiff[3] ) );
			    
			    dataReport.append( StringUtil.formatRightLeftJustified( 
			            "", "", Integer.toString( 
				                tmpOriginalValuesMap[z].size() ), 
				        null, tabStop[4] ) );
			    dataReport.append( StringUtil.formatRightLeftJustified( 
			            "", "", slash, null, rightDiff[4] ) );
	
			    if ( discretizationType[z].equalsIgnoreCase( 
			            BANJO.UI_DISCRETIZATIONNONE )) {
			    
			        dataReport.append( StringUtil.formatRightLeftJustified( 
				            "", "", Integer.toString( 
					                tmpOriginalValuesMap[z].size() ), 
					        null, tabStop[5] ) );
			    }
			    else {
			        
			        int pointsUsed = 0;
			        boolean[] valuesTracked = new boolean[ discretizationPoints[z] ];
			        int usedValue;
					SortedMap tmpValuesMap = new TreeMap();
					tmpValuesMap.putAll( discreteValuesMap[z] );
			        						        
			        for ( int j=0; j<discretizationPoints[z]; j++ ) {
			            valuesTracked[j] = false;
			        }
			        for ( int j=0; j<discreteValuesMap[z].size(); j++ ) {
			            
			            DblCurrentObsValue = 
				            (Double) tmpValuesMap.firstKey();
			            usedValue = ( (Counter) tmpValuesMap.get( 
					              DblCurrentObsValue )).i;
			            tmpValuesMap.remove( DblCurrentObsValue );
			            
			            try {
			            if ( !valuesTracked[ usedValue ] ) pointsUsed++;
			            valuesTracked[ usedValue ] = true;
			            }
			            catch ( Exception e ) {
			                throw new Exception( e );
			            }
			        }
			        
			        dataReport.append( StringUtil.formatRightLeftJustified( 
				            "", "", Integer.toString( pointsUsed ), 
					        null, tabStop[5] ) );
			    }
			    dataReport.append( StringUtil.formatRightLeftJustified( 
			            "", "", slash, null, rightDiff[5] ) );
			    					    
			    // Useful for examining in detail how the discretization actually 
			    // "maps" the data values (can become too much data quickly, though)
			    if ( processData.getValidatedProcessParameter( 
				        BANJO.SETTING_DISCRETIZATIONREPORT ).equalsIgnoreCase( 
				                BANJO.UI_DATAREPORT_WITHMAPPEDVALUES ) || 
	                processData.getValidatedProcessParameter( 
	    			        BANJO.SETTING_DISCRETIZATIONREPORT ).equalsIgnoreCase( 
	    			                BANJO.UI_DATAREPORT_WITHMAPPEDANDORIGINALVALUES )) {
			        
				    if ( discretizationType[z].equalsIgnoreCase( 
				            BANJO.UI_DISCRETIZATIONNONE )) {
				        
				        dataReport.append( StringUtil.formatRightLeftJustified( 
					            "", "", "counts: " + 
					            tmpOriginalValuesMap[z].toString(), 
						        null, tabStop[6] ));
				    }
				    else {
				            
				        dataReport.append( StringUtil.formatRightLeftJustified( 
					            "", "", "map: " + 
					            discreteValuesMap[z].toString(),
						        null, tabStop[6] ));
				        
				        // This displays the original values (on a separate line)
				        if ( processData.getValidatedProcessParameter( 
		    			        BANJO.SETTING_DISCRETIZATIONREPORT ).equalsIgnoreCase( 
		    			            BANJO.UI_DATAREPORT_WITHMAPPEDANDORIGINALVALUES )) {
				            
				            int extraSpaces = 5;
					        dataReport.append( StringUtil.fillSpaces( 
						            "" , extraSpaces ));
					        dataReport.append( 
						            "orig. values with counts: " + 
						            originalValuesMap[z].toString() );
				        }
				    }
			    }
		    }
	
			dataReport.append( BANJO.FEEDBACK_NEWLINEPLUSDASHEDLINE.
			        substring( 0, lineLength )); 
			dataReport.append( "\n" );
		}

        // Store the report in a dynamic setting for easy retrieval
        processData.setDynamicProcessParameter( BANJO.DATA_DISCRETIZATIONREPORT,
                dataReport.toString() );

        // Release unnecessary data (maps)
        originalValuesMap = null;
        tmpOriginalValuesMap = null;
        
        return dataReport;
    }
}
