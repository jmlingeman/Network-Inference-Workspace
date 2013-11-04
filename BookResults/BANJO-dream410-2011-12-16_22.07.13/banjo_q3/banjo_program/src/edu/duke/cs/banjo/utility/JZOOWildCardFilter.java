/*
 * Created on Mar 4, 2008
 * 
 * This file is used by Banjo (Bayesian Network Inference with Java Objects)
 * edu.duke.cs.banjo, based on the license below.
 * 
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

import java.io.File;
import java.io.FilenameFilter;
import java.util.StringTokenizer;
import java.util.Vector;

public class JZOOWildCardFilter implements FilenameFilter
/*
 * Copyright (c) 1998 Kevan Stannard. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL or COMMERCIAL purposes and
 * without fee is hereby granted. 
 * 
 * Please note that this software comes with
 * NO WARRANTY 
 *
 * BECAUSE THE PROGRAM IS LICENSED FREE OF CHARGE, THERE IS NO WARRANTY FOR THE PROGRAM, TO THE EXTENT PERMITTED
 * BY APPLICABLE LAW. EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER PARTIES
 * PROVIDE THE PROGRAM "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE ENTIRE RISK AS
 * TO THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU. SHOULD THE PROGRAM PROVE DEFECTIVE, YOU ASSUME
 * THE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION. 
 * 
 * IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING WILL ANY COPYRIGHT HOLDER, OR ANY OTHER
 * PARTY WHO MAY MODIFY AND/OR REDISTRIBUTE THE PROGRAM AS PERMITTED ABOVE, BE LIABLE TO YOU FOR DAMAGES,
 * INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OR INABILITY TO USE
 * THE PROGRAM (INCLUDING BUT NOT LIMITED TO LOSS OF DATA OR DATA BEING RENDERED INACCURATE OR LOSSES SUSTAINED
 * BY YOU OR THIRD PARTIES OR A FAILURE OF THE PROGRAM TO OPERATE WITH ANY OTHER PROGRAMS), EVEN IF SUCH HOLDER
 * OR OTHER PARTY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */
{
    String wildPattern = null;
    Vector pattern = new Vector();

    final String FIND     = "find";
    final String EXPECT   = "expect";
    final String ANYTHING = "anything";
    final String NOTHING  = "nothing";

    public JZOOWildCardFilter(String wildString)
    {
        wildPattern = wildString;

        // ensure wildString is lowercase for all testing

        wildString = wildString.toLowerCase();

        // remove duplicate asterisks

        int i = wildString.indexOf("**");
        while ( i >= 0 )
        {
            wildString = wildString.substring(0, i+1)
                       + wildString.substring(i+2);

            i = wildString.indexOf("**");
        }

        // parse the input string

        StringTokenizer tokens = new StringTokenizer(wildString, "*", true);
        String token = null;
        while (tokens.hasMoreTokens())
        {
            token = tokens.nextToken();

            if (token.equals("*"))
            {
                pattern.addElement(FIND);
                if (tokens.hasMoreTokens())
                {
                    token = tokens.nextToken();
                    pattern.addElement(token);
                }
                else
                {
                    pattern.addElement(ANYTHING);
                }
            }
            else
            {
                pattern.addElement(EXPECT);
                pattern.addElement(token);
            }
        }

        if ( !token.equals("*") )
        {
            pattern.addElement(EXPECT);
            pattern.addElement(NOTHING);
        }
    }


    public boolean accept(File dir, String name)
    {
        // allow directories to match all patterns
        // not sure if this is the best idea, but
        // suits my needs for now

        String path = dir.getPath();
        if ( !path.endsWith("/") && !path.endsWith("\\") )
        {
            path += File.separator;
        }
        File tempFile = new File(path, name);

        if ( tempFile.isDirectory() )
        {
            // BANJO: don't need any directory returned
            return false;
        }

        // ensure name is lowercase for all testing

        name = name.toLowerCase();

        // start processing the pattern vector

        boolean acceptName = true;

        String command = null;
        String param = null;

        int currPos = 0;
        int cmdPos = 0;

        while ( cmdPos < pattern.size() )
        {
            command = (String) pattern.elementAt(cmdPos);
            param = (String) pattern.elementAt(cmdPos + 1);

            if ( command.equals(FIND) )
            {
                // if we are to find 'anything'
                // then we are done
                   
                if ( param.equals(ANYTHING) )
                {
                    break;
                }

                // otherwise search for the param
                // from the curr pos

                int nextPos = name.indexOf(param, currPos);
                if (nextPos >= 0)
                {
                    // found it

                    currPos = nextPos + param.length();
                }
                else
                {
                    acceptName = false;
                    break;
                }
            }
            else
            {
                if ( command.equals(EXPECT) )
                {
                    // if we are to expect 'nothing'
                    // then we MUST be at the end of the string

                    if ( param.equals(NOTHING) )
                    {
                        if ( currPos != name.length() )
                        {
                            acceptName = false;
                        }

                        // since we expect nothing else,
                        // we must finish here

                        break;
                    }
                    
                    // otherwise, check if the expected string
                    // is at our current position

                    int nextPos = name.indexOf(param, currPos);
                    if ( nextPos != currPos )
                    {
                        acceptName = false;
                        break;
                    }

                    // if we've made it this far, then we've
                    // found what we're looking for

                    currPos += param.length();
                }
            }

            cmdPos += 2;
        }

        return acceptName;
    }


    public String toString()
    {
        return wildPattern;
    }


    public String toPattern()
    {
        StringBuffer out = new StringBuffer();

        int i=0;
        while (i<pattern.size())
        {
             out.append( "(" );
             out.append( (String) pattern.elementAt(i) );
             out.append( " " );
             out.append( (String) pattern.elementAt(i+1) );
             out.append( ") " );

             i += 2;
        }

        return out.toString();
    }
}
