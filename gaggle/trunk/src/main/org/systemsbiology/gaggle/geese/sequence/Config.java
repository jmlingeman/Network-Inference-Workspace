// Config.java
// a class to handle run-time configuration
//------------------------------------------------------------------------------------------
// $Revision: 231 $   
// $Date: 2005-09-03 16:15:18 -0700 (Sat, 03 Sep 2005) $ 
// $Author: pshannon $
//-----------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.sequence;
//------------------------------------------------------------------------------------------
import java.io.*;
import java.util.*;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import org.systemsbiology.gaggle.util.*;
//------------------------------------------------------------------------------------------
public class Config {

  String argSpecificationString = "s:d:";

  String [] commandLineArguments;
  String [] commandLineArgumentsCopy;
  String species = "unknown";
  String dataUri = null;
  String rawText = null;

  StringBuffer errorMessages = new StringBuffer ();
  boolean helpRequested = false;

//------------------------------------------------------------------------------------------
public Config (String [] args) 
{
  commandLineArguments = new String [args.length];
  System.arraycopy (args, 0, commandLineArguments, 0, args.length);
  parseArgs ();

} // ctor
//------------------------------------------------------------------------------------------
protected void parseArgs ()
{
  helpRequested = false;
  boolean argsError = false;
  String tmp;

  if (commandLineArguments == null || commandLineArguments.length == 0)
    return;

  LongOpt[] longopts = new LongOpt[0];
  Getopt g = new Getopt ("GaggleBoss", commandLineArguments, argSpecificationString, longopts);
  g.setOpterr (false); // We'll do our own error handling

  int c;
  while ((c = g.getopt ()) != -1) {
   switch (c) {
     case 's':
       species =  (g.getOptarg ());
       break;
     case 'd':
       dataUri =  (g.getOptarg ());
       break;
   case '?': // Optopt==0 indicates an unrecognized long option, which is reserved for plugins 
      int theOption = g.getOptopt();
      if (theOption != 0 )
        errorMessages.append ("The option '" + (char) theOption + "' is not valid\n");
       break;
     default:
       argsError = true;
       break;
     } // switch on c
   } // while

} // parseArgs
//---------------------------------------------------------------------------------
public String getSpecies ()
{
  return species;
}
//---------------------------------------------------------------------------------
public String getDataUri ()
{
  return dataUri;
}
//---------------------------------------------------------------------------------
protected String absolutizeFilename (File parentDirectory, String filename)
{
  if (filename.trim().startsWith ("/"))
    return filename;
  else 
    return (new File (parentDirectory, filename)).getPath ();

}
//---------------------------------------------------------------------------------
} // class Config
