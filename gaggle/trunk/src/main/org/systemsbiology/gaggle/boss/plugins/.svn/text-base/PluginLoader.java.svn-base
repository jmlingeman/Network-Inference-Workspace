// PluginLoader.java
//------------------------------------------------------------------------------
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

package org.systemsbiology.gaggle.boss.plugins;
//-----------------------------------------------------------------------------------
import java.util.*;
import java.lang.reflect.*;
import org.systemsbiology.gaggle.boss.GuiBoss;
//-----------------------------------------------------------------------------------
public class PluginLoader {

  String [] classNames;
  protected Properties props;

//-----------------------------------------------------------------------------------
public PluginLoader (GuiBoss gaggleBoss, String [] classNames)
{
  for (int i=0; i < classNames.length; i++)
   loadPlugin (classNames [i], gaggleBoss);

} // ctor
//-----------------------------------------------------------------------------------
protected void loadPlugin (String className, GuiBoss gaggleBoss)
{
 try {
    Class pluginClass = Class.forName (className);
    Class [] argClasses = new Class [1];
    argClasses [0] =  gaggleBoss.getClass ();
    Object [] args = new Object [1];
    args [0] = gaggleBoss;
    Constructor [] ctors = pluginClass.getConstructors ();
    Constructor ctor = pluginClass.getConstructor (argClasses);
    Object plugin = ctor.newInstance (args);
    }
  catch (Exception e) {
    e.printStackTrace ();
    System.err.println (e.getMessage ());
    }

} // loadPlugin
//------------------------------------------------------------------------------
} // class PluginLoader


