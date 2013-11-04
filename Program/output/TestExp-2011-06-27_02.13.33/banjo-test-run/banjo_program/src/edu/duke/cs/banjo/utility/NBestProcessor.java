/*
 * Created on Mar 12, 2008
 * 
 * This file is part of Banjo (Bayesian Network Inference with Java Objects)
 * edu.duke.cs.banjo
 * Banjo is licensed from Duke University.
 * Copyright (c) 2005 by Alexander J. Hartemink.
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

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import edu.duke.cs.banjo.bayesnet.BayesNetStructure;
import edu.duke.cs.banjo.bayesnet.BayesNetStructureI;
import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.learner.components.EquivalenceCheckerI;

public class NBestProcessor {

    protected Settings settings;
    // Once we start the search threads, they validate the main set of parameters,
    // which we need to reference if we want to use them in this class
    
    protected long nBestMax = 0;
    
    public NBestProcessor ( Settings _settingsUsedInThread ) {
                   
        settings = _settingsUsedInThread;

        if ( getNBestMax() <= 0 ) {

            setNBestMax( Long.parseLong( settings.getValidatedProcessParameter(
                      BANJO.SETTING_NBEST )) );
        }
    }
        
    public TreeSet foldIntoHighScoreSet( 
            TreeSet _highScoreStructureSet,
            TreeSet _highScoreStructureSetToAdd,
            EquivalenceCheckerI _equivalenceChecker,
            final int threadID ) throws Exception {
        
        try {
        
            // First a sanity check:
            if ( _highScoreStructureSetToAdd == null ) return _highScoreStructureSet;
            
            double currentBestScoreSinceRestart;
            double nBestThresholdScore;
            double bayesNetScore;
            long sizeBefore;
            long sizeAfter;
            boolean isEquivalent;
            long nBestCount = _highScoreStructureSet.size();
            
            // iterate over all networks in the set "_highScoreStructureSetToAdd"
            Iterator highScoreSetIterator = _highScoreStructureSetToAdd.iterator();
            BayesNetStructureI nextNetwork;
            
            while ( highScoreSetIterator.hasNext() ) {
    
                nextNetwork = (BayesNetStructureI) highScoreSetIterator.next();
                bayesNetScore = nextNetwork.getNetworkScore();
                
                // Set the (current) threshold score
                if ( nBestCount > 0 ) {
                    
                    BayesNetStructureI bns = (BayesNetStructureI) 
                            _highScoreStructureSet.last();
                    nBestThresholdScore = bns.getNetworkScore();
                }
                else {
                    
                    // Special case: nBest set is still empty, so make sure
                    // that the threshold score lets the first new network join the set
                    nBestThresholdScore = bayesNetScore - 1;
                }
            
                if ( nBestCount < getNBestMax() || bayesNetScore > nBestThresholdScore ) {
                        
                    isEquivalent = _equivalenceChecker.isEquivalent( _highScoreStructureSet, 
                            nextNetwork );
            
                    // There is nothing to do when we encounter equivalent networks, so:
                    if ( !isEquivalent ) {
                        if ( nBestCount < getNBestMax() ) {
                            
                            // The nBest container is not yet filled up
                            
                            // Note that we now use the threadID instead of the iteration where
                            // the particular n-best network was found
                            _highScoreStructureSet.add( new BayesNetStructure( 
                                    nextNetwork,
                                    bayesNetScore,
                                    threadID ));
                            
                            // update the count of tracked highscores
                            nBestCount = _highScoreStructureSet.size();
                        }
                        else if ( bayesNetScore > nBestThresholdScore ) {
                            
                            // Once the N-best container is filled up with N entries, we only 
                            // proceed with updating the n-Best container when we encounter 
                            // a "better" score (defined here as ">")
                            sizeBefore = _highScoreStructureSet.size();
                            
                            // Add the new network to the nBest set
                            _highScoreStructureSet.add( new BayesNetStructure( 
                                    nextNetwork,
                                    bayesNetScore,
                                    threadID ));
                            
                            sizeAfter = _highScoreStructureSet.size();
                            
                            // If both sizes are the same, then the network to be added
                            // is already stored. Otherwise, we need to remove the now
                            // lowest scoring network
                            if ( sizeBefore < sizeAfter ) {
                                
                                // Remove the network with the lowest score from the n-best
                                // set of networks
                                _highScoreStructureSet.remove( _highScoreStructureSet.last());
                            }
                        }
                    }
                }
            }
        }
        catch ( BanjoException e ) { 

            throw new BanjoException( e );
        }
        catch ( Exception e ) { 

            throw new BanjoException( e,
                    BANJO.ERROR_BANJO_DEV, 
                    "Error while folding a subset into the n-best networks." );
        }
        
        return _highScoreStructureSet;
    }

    public StringBuffer listNBestNetworksXML( TreeSet _highScoreStructureSet ) throws Exception {
        
        // Provides a list of the N-best scoring networks in XML format. 
        // Note that even though the collection of networks (the highScoreStructureSet) is a set 
        // of objects with a BayesNetStructureI contract, we channel access to the set via our 
        // special wrapper HighScoreContainer class, which gives us access to the special
        // formatting of the string representation of the network as defined within
        // the individual BayesNetStructure implementations. 
        
        StringBuffer statisticsBuffer = new StringBuffer(
                BANJO.BUFFERLENGTH_STAT );
        
        try {
        
            Iterator highScoreSetIterator = _highScoreStructureSet.iterator();
            int size = _highScoreStructureSet.size();
            BayesNetStructureI nextNetwork;
            
            // First add the XML header
            statisticsBuffer.append( getXMLHeader() );
            
            // Now add all the structures
            int i = 0;
            while ( highScoreSetIterator.hasNext() ) {
                
                i++;
                nextNetwork = (BayesNetStructureI) highScoreSetIterator.next();
    
                statisticsBuffer.append( BANJO.DATA_XML_NEWLINE + BANJO.DATA_XML_NEWLINE +
                        "<" +
                        BANJO.DATA_BANJOXMLTAG_NETWORK +
                        ">" );
                
                statisticsBuffer.append( BANJO.DATA_XML_NEWLINE + BANJO.DATA_XML_NEWLINE +
                        "<" +
                        BANJO.DATA_BANJOXMLTAG_NETWORKSCORE +
                        ">" +
                        BANJO.DATA_XML_NEWLINE );
                statisticsBuffer.append( StringUtil.formatDecimalDisplay(
                      nextNetwork.getNetworkScore(), 
                      BANJO.FEEDBACK_DISPLAYFORMATFORNETWORKSCORE ) );
                statisticsBuffer.append( BANJO.DATA_XML_NEWLINE +
                        "</" +
                        BANJO.DATA_BANJOXMLTAG_NETWORKSCORE +
                        ">" );
                
                statisticsBuffer.append( BANJO.DATA_XML_NEWLINE +
                        "<" +
                        BANJO.DATA_BANJOXMLTAG_NETWORKSTRUCTURE +
                        ">" +
                        BANJO.DATA_XML_NEWLINE );
                statisticsBuffer.append( nextNetwork.toString() );
                statisticsBuffer.append( BANJO.DATA_XML_NEWLINE +
                        "</" +
                        BANJO.DATA_BANJOXMLTAG_NETWORKSTRUCTURE +
                        ">" );
                
                statisticsBuffer.append( BANJO.DATA_XML_NEWLINE +
                        "</" +
                        BANJO.DATA_BANJOXMLTAG_NETWORK +
                        ">" );
            }
            
            // finally add our XML footer
            statisticsBuffer.append( getXMLFooter() );
        }
        catch ( Exception e ) { 

            throw new BanjoException( e, 
                    BANJO.ERROR_BANJO_DEV, 
                    "Error while creating the listing of the n-best networks in XML format." );
        }
        
        return statisticsBuffer;
    }

    protected StringBuffer getXMLHeader() throws Exception {

        StringBuffer XMLHeader = new StringBuffer( BANJO.DATA_XML_NEWLINE +
                "<" + BANJO.DATA_BANJOXMLTAG_BANJODATA + ">" +
                BANJO.DATA_XML_NEWLINE + 
                "<" + BANJO.DATA_BANJOXMLTAG_BANJOXMLFORMATVERSION + ">" +
                BANJO.DATA_XML_NEWLINE + 
                BANJO.DATA_BANJOXMLFORMATVERSION + 
                BANJO.DATA_XML_NEWLINE + 
                "</" + BANJO.DATA_BANJOXMLTAG_BANJOXMLFORMATVERSION + ">" +
                BANJO.DATA_XML_NEWLINE + 
                "<" + BANJO.DATA_BANJOXMLTAG_BANJOSETTINGS + ">" );
        XMLHeader.append( BANJO.DATA_XML_NEWLINE );
        
        
        if ( !true) {
            
            XMLHeader.append( "</" + BANJO.DATA_BANJOXMLTAG_BANJOSETTINGS + ">" );
            XMLHeader.append( "<" + BANJO.DATA_BANJOXMLTAG_NBESTNETWORKS + ">" );
            return XMLHeader;
        }
        
        try {
        
            StringBuffer settingsToExport = new StringBuffer();
    
            Pattern stringListPattern = Pattern.compile( 
                    BANJO.PATTERN_STRINGLIST, Pattern.COMMENTS );
//            Set processedSettings = settings.getProcessedSettings();
            
            // Add the settings that the user wants to have exported
            String strItemValueAsLoaded = settings.getValidatedProcessParameter( 
                    BANJO.SETTING_XMLSETTINGSTOEXPORT );
            
            String[] itemValues = stringListPattern.split( strItemValueAsLoaded );
            String itemValue;
            String itemName;
            Set settingsToList = new HashSet();
            Set settingsToOmit = new HashSet();
            
            // Quick initial pass through the settings to see if any of them is "all",
            // in which case we just list all (registered) settings...
            for ( int i=0; i<itemValues.length; i++ ) {
                
                itemName = itemValues[i].trim();
                if ( !itemName.equals( "" )) {
                    
                    if ( itemName.equalsIgnoreCase( BANJO.UI_XMLSETTINGSTOEXPORT_ALL ) ) {
                        
                        settingsToList = settings.compileRegisteredSettings();
                        // Let's exit the loop
    //                    i = itemValues.length;
                    }
                    else {
                        
                        if ( itemName.charAt(0) == BANJO.DATA_OMITFILE_IDENTIFIER ) {
                            
                            // trim away the "omit" indicator, and trim blanks again afterwards
                            if ( itemName.length()>1 ) {
                                
                                itemName = itemName.substring( 1, itemName.length() ).trim();
                            
                                // Only add a file name if it is meaningful
                                if ( !itemName.equals( "" )) {
                                
                                    settingsToOmit.add( new String( itemName ));
                                }
                            }
                        }
                        else {
                            
                            // Only add a file name if it is meaningful
                            if ( !itemName.equals( "" )) {
                                
                                settingsToList.add( new String( itemName ));
                            }
                        }
                    }
                }
            }
            
            // Add the absolute required items (for later XML file processing)
            // However, we will allow user to override them (see below)
            settingsToList.add( BANJO.SETTING_VARCOUNT );
            settingsToList.add( BANJO.SETTING_MINMARKOVLAG );
            settingsToList.add( BANJO.SETTING_MAXMARKOVLAG );
            settingsToList.add( BANJO.SETTING_BANJOSEED );
            
            // Subtract the "to-be-omitted" settings
            settingsToList.removeAll( settingsToOmit );
            
            // Sort the settings
            Set sortedSettingsList = new TreeSet( settingsToList );
            Iterator itemsIterator = sortedSettingsList.iterator();
            
            while ( itemsIterator.hasNext() ) {
                
                // get the next setting and process it
                itemName = (String) itemsIterator.next();
                try {

                    // this makes sure that we don't insert garbage when the list is empty
                    if ( itemName != null && !itemName.equals("") ) {
    
                        // This would use the setting values as they were initially 
                        // supplied by the user:
                        // itemValue = settings.getSettingItemValueAsLoaded( itemName );
                        //
                        // These are the validated setting values:
                        itemValue = settings.getValidatedProcessParameter( itemName );
        
                        if ( itemValue.equalsIgnoreCase( BANJO.DATA_SETTINGNOTFOUND )) {
                         
                            // tell the user that the setting is not known to Banjo
                            settingsToExport.append( BANJO.DATA_XMLPREFIXSPACER );
                            settingsToExport.append( "<" + itemName + ">" );
                            settingsToExport.append( BANJO.DATA_SETTINGNOTFOUND );
                            settingsToExport.append( "</" + itemName + ">" );
                            settingsToExport.append( BANJO.DATA_XML_NEWLINE );
                        }
                        else {
    
                            settingsToExport.append( BANJO.DATA_XMLPREFIXSPACER );
                            settingsToExport.append( "<" + itemName + ">" );
                            settingsToExport.append( itemValue );
                            settingsToExport.append( "</" + itemName + ">" );
                            settingsToExport.append( BANJO.DATA_XML_NEWLINE );
                        }
                    }
                }
                catch ( Exception e ) { 
    
                    throw new BanjoException( e,
                            BANJO.ERROR_BANJO_XML, "Processing setting '" +
                            "' (value='" +
                            "') during export of data in XML form." );
                }
            }
            
            XMLHeader.append( settingsToExport );
            
            XMLHeader.append( BANJO.DATA_XML_NEWLINE +
                    "</" + BANJO.DATA_BANJOXMLTAG_BANJOSETTINGS + ">" +
                    BANJO.DATA_XML_NEWLINE + BANJO.DATA_XML_NEWLINE +
                    "<" + BANJO.DATA_BANJOXMLTAG_NBESTNETWORKS + ">" );
        }
        catch ( BanjoException e ) { 

            throw new BanjoException( e );
        }
        catch ( Exception e ) { 

            throw new BanjoException( e, 
                    BANJO.ERROR_BANJO_XML, " Error while creating XML header " +
                    "(collecting the user-specified settings)." );
        }
        
        return XMLHeader;
    }

    protected StringBuffer getXMLFooter() throws Exception {

        StringBuffer XMLFooter = new StringBuffer( BANJO.DATA_XML_NEWLINE +
                "</" + BANJO.DATA_BANJOXMLTAG_NBESTNETWORKS + ">" +
                BANJO.DATA_XML_NEWLINE + 
                "</" + BANJO.DATA_BANJOXMLTAG_BANJODATA + ">" );
        
        return XMLFooter;
    }

    public StringBuffer listNBestNetworks( TreeSet _highScoreStructureSet ) throws Exception {
        
        // Provides a list of the N-best scoring networks. Note that even though the
        // collection of networks (the highScoreStructureSet) is a set of objects with
        // a BayesNetStructureI contract, we channel access to the set via our special
        // wrapper HighScoreContainer class, which gives us access to the special
        // formatting of the string representation of the network as defined within
        // the individual BayesNetStructure implementations. 
        
        StringBuffer statisticsBuffer = new StringBuffer(
                BANJO.BUFFERLENGTH_STAT );
        
        try {
        
            Iterator highScoreSetIterator = _highScoreStructureSet.iterator();
            int size = _highScoreStructureSet.size();
            BayesNetStructureI nextNetwork;
            int i = 0;
            while ( highScoreSetIterator.hasNext() ) {
                
                i++;
                nextNetwork = (BayesNetStructureI) highScoreSetIterator.next();
                
                // Record the score and the loop
                if ( size > 1 ) {
                    
                    statisticsBuffer.append( BANJO.DATA_NBEST_NEWLINE + BANJO.DATA_NBEST_NEWLINE +
                            "Network #" );
                    statisticsBuffer.append( i );
                    statisticsBuffer.append( ", score: " );
                }
                else {
        
                    statisticsBuffer.append( BANJO.DATA_NBEST_NEWLINE + BANJO.DATA_NBEST_NEWLINE +
                            "Network score: " );
                }
                statisticsBuffer.append( StringUtil.formatDecimalDisplay(
                        nextNetwork.getNetworkScore(), 
                        BANJO.FEEDBACK_DISPLAYFORMATFORNETWORKSCORE ) );
                
                // May not want to append this info:
                statisticsBuffer.append( ", found by thread " );
                statisticsBuffer.append( nextNetwork.getSearchLoopIndex() );
                
                // Add the network structure as a specially formatted string
                statisticsBuffer.append( BANJO.DATA_NBEST_NEWLINE );
                statisticsBuffer.append( nextNetwork.toString() );
            }
        }
        catch ( Exception e ) { 

            throw new BanjoException( e,
                    BANJO.ERROR_BANJO_DEV, 
                    "Error while creating the listing of the n-best networks." );
        }
        
        return statisticsBuffer;
    }

    
    /**
     * @return Returns the nBestMax.
     */
    public long getNBestMax() {
        
        return nBestMax;
    }

    /**
     * @param _nBestMax The n-best max value to set.
     */
    public void setNBestMax( final long _nBestMax ) {
        
        nBestMax = _nBestMax;
    }
}
