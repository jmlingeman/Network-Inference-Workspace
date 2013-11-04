/*
 * Created on Jan 18, 2008
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

import edu.duke.cs.banjo.bayesnet.*;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Collection;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import edu.duke.cs.banjo.data.settings.SettingItem;
import edu.duke.cs.banjo.data.settings.Settings;
import edu.duke.cs.banjo.learner.components.EquivalenceCheckerBasic;
import edu.duke.cs.banjo.learner.components.EquivalenceCheckerI;
import edu.duke.cs.banjo.learner.components.EquivalenceCheckerSkip;

public class XMLProcessor {

    protected Settings settings;
    protected TreeSet highScoreStructureSet;
    protected TreeSet highScoreStructuresCombinedSet;
    protected String[] arrFileList;
    protected int fileID;
    String fileName = "";

    public XMLProcessor( Settings _settings ) throws Exception {

        settings = _settings;

        // default choices for cycle checking
        String tmpConfigCycleCheckerMethod = BANJO.DATA_CYCLECHECKING_SHMUELI;
        settings.setDynamicProcessParameter( 
            BANJO.CONFIG_CYCLECHECKER_METHOD, tmpConfigCycleCheckerMethod );
        settings.setDynamicProcessParameter( BANJO.CONFIG_CYCLECHECKER_ACTIONORDER,
                BANJO.DATA_CYCLECHECKER_CHECKTHENAPPLY );
    }
    
    protected static StringBuffer printSystemSettings() throws Exception {

        Properties processedSettings = System.getProperties();
        String strParameterName;
        String strParameterValue;
        StringBuffer collectedSettings = new StringBuffer();
        
        if ( processedSettings != null ) {

            Set parameterSet = new TreeSet();
            parameterSet = processedSettings.keySet();
            Iterator settingItemIterator = parameterSet.iterator();

              // Create a simple properties set of the validated parameters
              SettingItem nextItem; 
              
              while ( settingItemIterator.hasNext() ) {
    
                  strParameterName = (String) settingItemIterator.next();
                  strParameterName = strParameterName.trim();
    
                  strParameterValue = 
                      processedSettings.getProperty( strParameterName ).trim();
                  
                  collectedSettings.append( "   " );
                  collectedSettings.append( strParameterName );
                  collectedSettings.append( " = " );
                  collectedSettings.append( strParameterValue );
                  collectedSettings.append( BANJO.FEEDBACK_NEWLINE );
              }
        }
        
        return collectedSettings;
    }

    public void processXMLResultFiles( String[] fileList ) throws Exception {

        arrFileList = new String[ fileList.length ];
        String vendorParserClass = BANJO.DATA_XMLPARSERCHOICE;
        int lineLength = BANJO.FEEDBACK_LINELENGTH;
        String dashedLine = BANJO.FEEDBACK_NEWLINE + 
                BANJO.FEEDBACK_DASHEDLINE.substring( 0, lineLength-1 );

        NBestProcessor nBestProcessor;
        nBestProcessor = new NBestProcessor( settings );

        EquivalenceCheckerI equivalenceChecker;
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
          
        try {
        
            XMLReader parser = XMLReaderFactory.createXMLReader();
            double bayesNetScore;
            int xmlFileID;
            
            // set up the final results set
            highScoreStructureSet = new TreeSet();
            highScoreStructuresCombinedSet = new TreeSet();
            
            ContentHandler handler = new TextExtractor();
            parser.setContentHandler(handler);
          
            // Parse the data in the supplied files
            for ( int i=0; i<fileList.length; i++ )
            {
                fileName = fileList[i];
                fileID = i;
                arrFileList[ fileID ] = fileName;

                if ( BANJO.DEBUG && BANJO.TRACE_XMLOUTPUT ) {
                
                    System.out.println( "Processing file '" + fileName + "'" +
                        ", file ID = " + fileID );
                }
                
                // parse this file
                parser.parse( fileName );
                
                // Note: at this point we may have 2 sets that each contain <n-best> number
                // of networks. This clearly is not the most memory-efficient way to do this.
                // However, while it is possible to add each network right after it is read
                // from the file, it is unlikely that the original search could have run (in
                // whatever memory was/is available) if we run out of memory now. So, we opt for
                // simplicity of the code for now, by processing all networks that were just
                // read into highScoreStructureSet (from file i), and folding them wholesale
                // into the combined set highScoreStructuresCombinedSet:
                highScoreStructuresCombinedSet = nBestProcessor.foldIntoHighScoreSet( 
                        highScoreStructuresCombinedSet,
                        highScoreStructureSet,
                        equivalenceChecker, 0 );

                // Reset the highScoreStructureSet for loading the networks from the next file
                highScoreStructureSet = new TreeSet();
            }
            
            // Process the networks in the combined n-Best set
            Iterator highScoreSetIterator = highScoreStructuresCombinedSet.iterator();
            BayesNetStructureI nextNetwork;

            // Add the "header info"            
            Collection fileOutputFlags = new HashSet( BANJO.MAXOUTPUTFILES );
            fileOutputFlags.add( new Integer(BANJO.FILE_RESULTS) );
            StringBuffer networkInfo = new StringBuffer();
            networkInfo.append( dashedLine );
            networkInfo.append( StringUtil.getBanjoSignature() );
            networkInfo.append( dashedLine );
            networkInfo.append( StringUtil.getJobSignature( settings ) );
            networkInfo.append( dashedLine );
            networkInfo.append( BANJO.FEEDBACK_NEWLINE );
            networkInfo.append( BANJO.FEEDBACK_NEWLINE );

            networkInfo.append( "Combined results from the " + 
                    arrFileList.length + " supplied XML files:" );
            
            settings.writeToFile( fileOutputFlags , networkInfo );
            
            int i = 0;
            // Write the combined results set back out (to the results file)
            while ( highScoreSetIterator.hasNext() ) {

                i++;
                networkInfo = new StringBuffer();
                nextNetwork = (BayesNetStructureI) highScoreSetIterator.next();
                bayesNetScore = nextNetwork.getNetworkScore();
                xmlFileID = Integer.parseInt( 
                        Long.toString( nextNetwork.getSearchLoopIndex() ));

                networkInfo.append( BANJO.FEEDBACK_NEWLINE );
                networkInfo.append( "Original file: " + arrFileList[ xmlFileID ] );
                networkInfo.append( BANJO.FEEDBACK_NEWLINE );
                networkInfo.append( "Network #" + i + " of " + 
                        highScoreStructuresCombinedSet.size() );
                networkInfo.append( BANJO.FEEDBACK_NEWLINE );
                networkInfo.append( "Score: " + bayesNetScore );
                networkInfo.append( BANJO.FEEDBACK_NEWLINE );
                networkInfo.append( nextNetwork.toString() );
                
                settings.writeToFile( fileOutputFlags , networkInfo );
            }            
        } 
        catch ( SAXException e ) {
            
            throw new BanjoException( e,
                    BANJO.ERROR_BANJO_XML );
        }
        catch ( IOException e ) {
            
            throw new BanjoException( e,
                    BANJO.ERROR_BANJO_XML, 
                    "Due to an IOException, the parser could not check the file '" +
                    fileName + "'" );
        }
        catch ( Exception e ) {
            
            throw new BanjoException( e,
                    BANJO.ERROR_BANJO_XML, 
                    "General Exception while parsing the XML in the file '" +
                    fileName + "'" );        
        }
    }
    
    public static void main(String[] args) {

        String[] fileList = args;
        BanjoErrorHandler errorHandler = new BanjoErrorHandler();
        XMLProcessor banjoXML;
        
        if (args.length <= 0) {
            System.out.println("Usage: java BanjoXML URL");
            return;
        }
        
        try {
            
            banjoXML = new XMLProcessor( null );

            try {
                banjoXML.processXMLResultFiles( fileList ); 
                }
                catch (SAXException e) {
                    
                    throw new BanjoException( e,
                            BANJO.ERROR_BANJO_DEV, 
                            "(One of) the input file(s) is not well-formed." );
                }
                catch (IOException e) { 
                    
                    throw new BanjoException( e,
                            BANJO.ERROR_BANJO_DEV, 
                            "Due to an IOException, the parser could not check " +
                            "(one of) the input file(s)" );
                }
              catch ( Exception e ) {
                  
                  throw new BanjoException( e,
                          BANJO.ERROR_BANJO_DEV, 
                          "General Exception while parsing XML." );
                  }
            }
            catch ( final BanjoException e ) {
                
                errorHandler.handleApplicationException( e );
            }
            catch ( Exception e ) {
                
                errorHandler.handleGeneralException( e );
            }
    }

    /**
     * <b><code>JTreeErrorHandler</code></b> implements the SAX
     *   <code>ErrorHandler</code> interface and defines callback
     *   behavior for the SAX callbacks associated with an XML
     *   document's warnings and errors.
     */
    class JTreeErrorHandler implements ErrorHandler {

        /**
         * <p>
         * This will report a warning that has occurred; this indicates
         *   that while no XML rules were "broken", something appears
         *   to be incorrect or missing.
         * </p>
         *
         * @param exception <code>SAXParseException</code> that occurred.
         * @throws <code>SAXException</code> when things go wrong 
         */
        public void warning(SAXParseException exception)
            throws SAXException {
                      
            throw new SAXException( "** XML Parsing Warning**\n" +
                    "  Line:    " + 
                    exception.getLineNumber() + "\n" +
                 "  URI:     " + 
                    exception.getSystemId() + "\n" +
                 "  Message: " + 
                    exception.getMessage() );
        }

        /**
         * <p>
         * This will report an error that has occurred; this indicates
         *   that a rule was broken, typically in validation, but that
         *   parsing can reasonably continue.
         * </p>
         *
         * @param exception <code>SAXParseException</code> that occurred.
         * @throws <code>SAXException</code> when things go wrong 
         */
        public void error(SAXParseException exception)
            throws SAXException {
            
            throw new SAXException( "** XML Parsing Error**\n" +
                    "  Line:    " + 
                    exception.getLineNumber() + "\n" +
                 "  URI:     " + 
                    exception.getSystemId() + "\n" +
                 "  Message: " + 
                    exception.getMessage() );
        }

        /**
         * <p>
         * This will report a fatal error that has occurred; this indicates
         *   that a rule has been broken that makes continued parsing either
         *   impossible or an almost certain waste of time.
         * </p>
         *
         * @param exception <code>SAXParseException</code> that occurred.
         * @throws <code>SAXException</code> when things go wrong 
         */
        public void fatalError(SAXParseException exception)
            throws SAXException {
               
            throw new SAXException( "** XML Parsing Fatal Error**\n" +
                    "  Line:    " + 
                    exception.getLineNumber() + "\n" +
                 "  URI:     " + 
                    exception.getSystemId() + "\n" +
                 "  Message: " + 
                    exception.getMessage() );
        }
    } // end JTreeErrorHandler class
        
    public class TextExtractor implements ContentHandler {

        private StringBuffer collectedCharacters = new StringBuffer();
        private String strNetworkScore;
        private String strNetworkStructure;
        private String strBanjoSettings;
        private int varCount = -1;
        private int minMarkovLag = -1;
        private int maxMarkovLag = -1;
        
//        public TextExtractor() {
//            // nothing to do
//        }

        // ContentHandler methods (some "do-nothing")
        public void characters(char[] text, int start, int length)
         throws SAXException {
           
          try {
              
              collectedCharacters.append( text, start, length );
          }
          catch ( Exception e ) {

              if ( BANJO.DEBUG && BANJO.TRACE_XMLOUTPUT ) {
              
                  System.out.println( "Exception in 'characters' processing." );
              }
              throw new SAXException(e);
          }
          
        }  
        
        // -----------------------------------------------------------
        // Misc. methods for implementing the ContentHandler interface
        // -----------------------------------------------------------
        
        public void setDocumentLocator(Locator locator) {
            // not needed
        }
        public void startDocument() throws SAXException {
            
            try {
//
            }
            catch ( Exception e ) {

                if ( BANJO.DEBUG && BANJO.TRACE_XMLOUTPUT ) {
                
                    System.out.println( "Exception in 'startDocument' processing." );
                }
                throw new SAXException(e);
            }
        }
        public void endDocument() throws SAXException {
            try {
//
            }
            catch ( Exception e ) {

                if ( BANJO.DEBUG && BANJO.TRACE_XMLOUTPUT ) {
                
                    System.out.println( "Exception in 'endDocument' processing." );
                }
                throw new SAXException(e);
            }
        }
        public void startPrefixMapping(String prefix, String uri) {
            // not needed
        }
        public void endPrefixMapping(String prefix) {
            // not needed
        }
        
        public void startElement(String namespaceURI, String localName,
                String qualifiedName, Attributes atts) throws SAXException {

            try {
                collectedCharacters = new StringBuffer();
            }
            catch ( Exception e ) {

                if ( BANJO.DEBUG && BANJO.TRACE_XMLOUTPUT ) {
                
                    System.out.println( "Exception in 'startElement' processing." );
                }
                throw new SAXException(e);
            }
        }
        
        public void endElement( final String namespaceURI, final String localName,
                final String qualifiedName) throws SAXException {

            try {

                String xmlExtension = "txt";
                String fileNameForXML = "__XML.output";
                FileUtil fileUtil = new FileUtil();
                
                if ( localName.equalsIgnoreCase( BANJO.DATA_BANJOXMLTAG_NETWORKSCORE )) {
                    
                    strNetworkScore = new String( ( collectedCharacters.toString() ).trim() );
                }
                else if ( localName.equalsIgnoreCase( BANJO.DATA_BANJOXMLTAG_VARCOUNT )) {
                    
                    varCount = Integer.parseInt( 
                            new String( ( collectedCharacters.toString() ).trim() ));
                }
                else if ( localName.equalsIgnoreCase( BANJO.DATA_BANJOXMLTAG_MINMARKOVLAG )) {
                    
                    minMarkovLag = Integer.parseInt( 
                            new String( ( collectedCharacters.toString() ).trim() ));
                }
                else if ( localName.equalsIgnoreCase( BANJO.DATA_BANJOXMLTAG_MAXMARKOVLAG )) {
                    
                    maxMarkovLag = Integer.parseInt( 
                            new String( ( collectedCharacters.toString() ).trim() ));
                }
                else if ( localName.equalsIgnoreCase( BANJO.DATA_BANJOXMLTAG_NETWORKSTRUCTURE )) {
                    
                    strNetworkStructure = new String( ( collectedCharacters.toString() ).trim() );
                }
                else if ( localName.equalsIgnoreCase( BANJO.DATA_BANJOXMLTAG_BANJOSETTINGS )) {
                    
       //// TODO: need to do more?
                    
                    strBanjoSettings = new String( ( collectedCharacters.toString() ).trim() );
                    
                    if ( BANJO.DEBUG && BANJO.TRACE_XMLOUTPUT ) {
                        
                        System.out.println();
                        System.out.print( "settings=" + strBanjoSettings );
                    }
                }
                else if ( localName.equalsIgnoreCase( BANJO.DATA_BANJOXMLTAG_NETWORK )) {

                    if ( BANJO.DEBUG && BANJO.TRACE_XMLOUTPUT ) {
                        
                        System.out.println();
                        System.out.println();
                        System.out.print( "Score= " );
                        System.out.print( strNetworkScore );
                        System.out.println();
                        System.out.print( "Network= " );
                        System.out.print( strNetworkStructure );
                    }

                    fileUtil.writeStringToFile( 
                            fileNameForXML + "." + xmlExtension,
                            composeCompleteXMLElement( 
                                    BANJO.DATA_BANJOXMLTAG_NETWORKSCORE,
                                    strNetworkScore,
                                    BANJO.DATA_XMLTYPE_ELEMENT ),
                            BANJO.TRACE_XMLOUTPUT );

                    fileUtil.writeStringToFile( 
                            fileNameForXML + "." + xmlExtension,
                            composeCompleteXMLElement( 
                                    BANJO.DATA_BANJOXMLTAG_NETWORKSTRUCTURE,
                                    strNetworkStructure,
                                    BANJO.DATA_XMLTYPE_ELEMENT ),
                            BANJO.TRACE_XMLOUTPUT );
                    
                    
                    // Check that the necessary data describing the structure was supplied:
                    if ( varCount<0 || minMarkovLag<0 || maxMarkovLag<0 ) {
                        
                        //
                        throw new BanjoException( BANJO.ERROR_BANJO_XML,
                                "Cannot re-create a banjo network from the supplied data " +
                                "in the XML file '" + fileName + "': " +
                                BANJO.FEEDBACK_NEWLINE  + 
                                "Banjo encountered " +
                                BANJO.DATA_BANJOXMLTAG_VARCOUNT + "=" + varCount + ", " +
                                BANJO.DATA_BANJOXMLTAG_MINMARKOVLAG + "=" + minMarkovLag + ", " +
                                BANJO.DATA_BANJOXMLTAG_MAXMARKOVLAG + "=" + maxMarkovLag + ", " +
                                "although none of them can be less than 0." );
                    }
                    
                    // Create a new network structure from this data
                    
                    EdgesWithCachedStatisticsI networkAsEdges = 
                        new EdgesAsArrayWithCachedStatistics( 
                                varCount, minMarkovLag, maxMarkovLag, settings, strNetworkStructure );
                    
                    

                    highScoreStructureSet.add( new BayesNetStructure( 
                            networkAsEdges,
                            new Double( strNetworkScore ).doubleValue(),
                            (long) fileID ));
                    

                    if ( BANJO.DEBUG && BANJO.TRACE_XMLOUTPUT ) {
                        
                        System.out.print( "\n\n(ECHO) " );
                        System.out.println( "Added network #" + fileID + " retrieved from XML:\n" );
                        System.out.println( strNetworkScore );
                        System.out.println( new BayesNetStructure( 
                                networkAsEdges,
                                new Double( strNetworkScore ).doubleValue(),
                                (long) fileID ).toString() );
                    }
                    
                    
                }
                else if ( localName.equalsIgnoreCase( BANJO.DATA_BANJOXMLTAG_BANJODATA )) {

                    try {                        

                        fileUtil.writeStringToFile( 
                                fileNameForXML + "." + xmlExtension,
                                composeCompleteXMLElement( 
                                        BANJO.DATA_BANJOXMLTAG_BANJOSETTINGS,
                                        strBanjoSettings,
                                        BANJO.DATA_XMLTYPE_ELEMENT ), 
                                BANJO.TRACE_XMLOUTPUT );                        
                    }
                    catch ( Exception e ) {

                        if ( BANJO.DEBUG && BANJO.TRACE_XMLOUTPUT ) {
                        
                            System.out.println( "Exception in 'endElement[BanjoData]' processing." );
                        }
                        throw new SAXException( e );
                    }
                }
            }
            catch ( Exception e ) {
                
                if ( BANJO.DEBUG && BANJO.TRACE_XMLOUTPUT ) {
                
                    System.out.println( "Exception in 'endElement' processing." );
                }
                throw new SAXException( e );
            }
        }
        
        public void ignorableWhitespace(char[] text, int start, 
                int length) throws SAXException {
            // currently not needed
        }
        public void processingInstruction(String target, String data){
            // currently not needed
        }
        public void skippedEntity(String name) {
            // currently not needed
        }
        
        protected String composeCompleteXMLElement( 
                final String _XMLtag, final String _XMLvalue, final int _XMLtype ) {
            
            StringBuffer XMLstring = new StringBuffer();
            
            if ( _XMLtype == BANJO.DATA_XMLTYPE_ELEMENT ) {
                
                XMLstring.append( makeStartElement( _XMLtag ) );
                
                XMLstring.append( BANJO.DATA_XML_NEWLINE );
                XMLstring.append( _XMLvalue );
                XMLstring.append( BANJO.DATA_XML_NEWLINE );

                XMLstring.append( makeEndElement( _XMLtag ) );
                
                XMLstring.append( BANJO.DATA_XML_NEWLINE );
            }
            else {
            
                // throw an execption: unknown XML type
            }
            
            return XMLstring.toString();
        }

        protected String makeStartElement( final String _XMLtag ) {
            
            StringBuffer XMLstring = new StringBuffer();

            XMLstring.append( "<" );
            XMLstring.append( _XMLtag );
            XMLstring.append( ">" );

            return XMLstring.toString();
        }

        protected String makeEndElement( final String _XMLtag ) {
            
            StringBuffer XMLstring = new StringBuffer();

            XMLstring.append( "</" );
            XMLstring.append( _XMLtag );
            XMLstring.append( ">" );

            return XMLstring.toString();
        }
    } // end TextExtractor class
}
