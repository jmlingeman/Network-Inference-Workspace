// BossConfig.java
// a class to handle run-time configuration
//------------------------------------------------------------------------------------------
// $Revision$   
// $Date$ 
// $Author$
//-----------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.boss;
//------------------------------------------------------------------------------------------

import java.io.*;
import java.util.*;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import org.systemsbiology.gaggle.util.*;

//------------------------------------------------------------------------------------------
public class BossConfig {

    String argSpecificationString = "p:n:";

    String[] commandLineArguments;
    String[] commandLineArgumentsCopy;
    String propsFilename;
    String nameHelperUri = null;
    File projectFileDirectoryAbsolute;

    Properties props;
    StringBuffer errorMessages = new StringBuffer();
    boolean helpRequested = false;
    protected boolean startInvisibly = false;
    protected boolean startMinimized = false;

    //------------------------------------------------------------------------------------------
    public BossConfig() {
        this(new String[0]);
    }

    //------------------------------------------------------------------------------------------
    public BossConfig(String[] args) {

        commandLineArguments = new String[args.length];
        System.arraycopy(args, 0, commandLineArguments, 0, args.length);
        parseArgs();
        props = readProperties();

    } // ctor

    //------------------------------------------------------------------------------------------
    public Properties getProperties() {
        return props;
    }

    //------------------------------------------------------------------------------------------
    protected void parseArgs() {
        helpRequested = false;
        boolean argsError = false;
        String tmp;

        if (commandLineArguments == null || commandLineArguments.length == 0)
            return;

        LongOpt[] longopts = new LongOpt[2];
        longopts[0] = new LongOpt("startInvisibly", LongOpt.NO_ARGUMENT, null, '~');
        longopts[1] = new LongOpt("startMinimized", LongOpt.NO_ARGUMENT, null, '`');
        Getopt g = new Getopt("GaggleBoss", commandLineArguments, argSpecificationString, longopts);
        g.setOpterr(false); // We'll do our own error handling

        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case'p':
                    propsFilename = (g.getOptarg());
                    break;
                case'n':
                    nameHelperUri = (g.getOptarg());
                    break;
                case'?': // Optopt==0 indicates an unrecognized long option, which is reserved for plugins
                    int theOption = g.getOptopt();
                    if (theOption != 0)
                        errorMessages.append("The option '" + (char) theOption + "' is not valid\n");
                    break;
                case'~':
                    startInvisibly = true;
                    break;
                case'`':
                    startMinimized = true;
                    break;
                default:
                    argsError = true;
                    break;
            } // switch on c
        } // while


    } // parseArgs

    //---------------------------------------------------------------------------------
    public String[] getPluginNames() {
        String[] keys = (String[]) props.keySet().toArray(new String[0]);
        ArrayList list = new ArrayList();
        for (int i = 0; i < keys.length; i++) {
            String propertyName = keys[i];
            if (propertyName.toLowerCase().startsWith("plugin"))
                list.add(props.get(propertyName));
        } // for i

        return (String[]) list.toArray(new String[0]);

    } // getPluginNames

    //---------------------------------------------------------------------------------
    public String getNameHelperUri() {
        return nameHelperUri;
    }

    //---------------------------------------------------------------------------------
    public String getPropsFilename() {
        return propsFilename;
    }

    public boolean startInvisibly() {
        return startInvisibly;
    }

    public boolean startMinimized() {
        return startMinimized;
    }

    //---------------------------------------------------------------------------------
    protected Properties readProperties() {
        if (propsFilename == null)
            return new Properties();

        System.out.println("BossConfig about to read from " + propsFilename);
        Properties projectProps = readPropertyFileAsText(propsFilename);
        System.out.println("props: " + projectProps);

        return projectProps;

    } // readProperties

    //------------------------------------------------------------------------------------------
    public Properties readPropertyFileAsText(String filename) {
        String rawText = "";

        try {
            if (filename.trim().startsWith("jar://")) {
                TextJarReader reader = new TextJarReader(filename);
                reader.read();
                rawText = reader.getText();
            } else {
                File projectPropsFile = new File(absolutizeFilename(projectFileDirectoryAbsolute, filename));
                TextFileReader reader = new TextFileReader(projectPropsFile.getPath());
                reader.read();
                rawText = reader.getText();
            }
        }
        catch (Exception e0) {
            System.err.println("-- Exception while reading properties file " + filename);
            e0.printStackTrace();
        }

        // the Properties class contains its own parser, so it makes the most sense
        // to massage our text into a form suitable for that loader
        byte[] byteText = rawText.getBytes();
        InputStream is = new ByteArrayInputStream(byteText);
        Properties newProps = new Properties();
        try {
            newProps.load(is);
        }
        catch (IOException ioe) { //seems unlikely
            ioe.printStackTrace();
        }

        return newProps;

    } // readPropertyFileAsText

    //------------------------------------------------------------------------------------------
    protected String absolutizeFilename(File parentDirectory, String filename) {
        if (filename.trim().startsWith("/"))
            return filename;
        else
            return (new File(parentDirectory, filename)).getPath();

    }
//---------------------------------------------------------------------------------
/***************************************************
 public String [] getArgs ()
 {
 String [] returnVal = new String [commandLineArgumentsCopy.length];
 System.arraycopy (commandLineArgumentsCopy, 0, returnVal, 0, commandLineArgumentsCopy.length);
 return returnVal;
 }
 //------------------------------------------------------------------------------------------
 protected boolean legalArguments ()
 {
 boolean legal = true;

 } // legalArguments
 //---------------------------------------------------------------------------------
 public void setExpressionFilename (String newValue)
 {
 expressionFilename = newValue;
 }
 //---------------------------------------------------------------------------------
 public void setProjectFilename (String newValue)
 {
 projectFilename = newValue;
 }
 //---------------------------------------------------------------------------------
 *****************************************/
} // class BossConfig


