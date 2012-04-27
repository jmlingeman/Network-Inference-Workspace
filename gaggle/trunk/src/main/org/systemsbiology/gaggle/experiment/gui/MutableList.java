// MutableList.java
//---------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui;

import java.util.Hashtable;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;

public class MutableList extends JList {
	
    Vector originalValues = new Vector(); // order stays the same, names change
    Vector originalNames = new Vector(); // order and names stay the same
    
    Hashtable nameMap = new Hashtable();
    
    Hashtable indexMap = new Hashtable();

    public MutableList() {
    	super(new DefaultListModel());
   }

	public MutableList(Object[] values) {
		super(new DefaultListModel());
		for (int i = 0; i < values.length; i++) {
			getContents().addElement(values[i]);
			originalValues.addElement(values[i]);
			originalNames.addElement(values[i]);
			nameMap.put(values[i], values[i]);
			indexMap.put(values[i], new Integer(i));
		}
	}
	
	public Vector getOriginalValues() {
	    return originalValues;
	}
	
	public void setOriginalValues(Vector originalValues) {
	    this.originalValues = originalValues;
	    for (int i = 0; i < originalValues.size(); i++) {
            String key = (String)originalValues.get(i);
            nameMap.put(key, (String)originalNames.get(i));
        }
	}
	
	public String getOriginalName(String name) {
	    if (null == nameMap.get(name)) {
	        return name;
	    }
	    return (String)nameMap.get(name);
	}

	public void add (Object value) {
          getContents().addElement (value);
          int position = getContents().size ();
          indexMap.put (value, new Integer (position));
          }

	public void changeList(Object[] newValues) {
		getContents().clear();
		for (int i = 0; i < newValues.length; i++) {
			getContents().addElement(newValues[i]);
			indexMap.put(getOriginalName((String)newValues[i]),new Integer(i));
		}
	}
	
	
	public int getIndex(String originalName) {
	    return ((Integer)indexMap.get(originalName)).intValue();
	}
	
    public DefaultListModel getContents() {
	return (DefaultListModel)getModel();
    }
}   



