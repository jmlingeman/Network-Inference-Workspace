// StringToObjectConverter: try to convert a String into a URL, Double, Integer.
//-------------------------------------------------------------------------------------
// $Revision: 745 $   
// $Date: 2006-03-23 13:59:33 -0800 (Thu, 23 Mar 2006) $
//-------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.rShell;
//---------------------------------------------------------------------------------
import java.lang.reflect.*;
//-------------------------------------------------------------------------------------
public class StringToObjectConverter {

//-------------------------------------------------------------------------------------
static public Object convert (String rawString)
{
  String [] classNames = {"java.net.URL",
                          "java.lang.Integer",
                          "java.lang.Double",
                          "java.lang.String"};

    for (int i=0; i < classNames.length; i++) {
      try {
        // System.out.println ("stoc.convert, trying " + classNames[i]);
        Class requestedClass = Class.forName (classNames [i]);
        Class [] ctorArgsClasses = new Class [1];
        ctorArgsClasses [0] =  Class.forName ("java.lang.String");
        Object [] ctorArgs = new Object [1];
        ctorArgs [0] = new String (rawString);
        Constructor ctor = requestedClass.getConstructor (ctorArgsClasses);
        return ctor.newInstance (ctorArgs);
        } // try
      catch (Exception ex0) {
        ;
        }
      } // for i

  return rawString;

} // convert
//-------------------------------------------------------------------------------------
} // class StringToObjectConverter
