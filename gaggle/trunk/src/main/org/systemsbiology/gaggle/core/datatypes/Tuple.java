package org.systemsbiology.gaggle.core.datatypes;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

/*
* Copyright (C) 2007 by Institute for Systems Biology,
* Seattle, Washington, USA.  All rights reserved.
*
* This source code is distributed under the GNU Lesser
* General Public License, the text of which is available at:
*   http://www.gnu.org/copyleft/lesser.html
*/

/**
 * If n = 1 we say "single", if n = 2 we say "double", and if
 * n is any arbitrary number, we call it a tuple. In other words, this is
 * just a list of Single objects.
 *
 *
 * todo: format this as HTML.
 *
 * A discussion of Tuples as used within Gaggle:
 *
 * A tuple is simply a list of items, in the case of Gaggle, a list of values with optional keys. It is more flexible than a HashMap and has therefore replaced the HashMap in the newest Gaggle API.
 *
 *
 * The name comes from the pattern single, double, triple, etc. Tuple means there can be an arbitrary number of items.
 *
 * http://en.wikipedia.org/wiki/Tuple
 *
 * This page shows the Javadoc for the class containing the core gaggle data types:
 * http://gaggle.systemsbiology.net/2007-04/javadoc/org/systemsbiology/gaggle/core/datatypes/package-summary.html
 *
 * Tuples are composed as follows:
 * 
 * GaggleTuple
 *   Tuple
 *      Single
 *
 * Every top-level gaggle datatype gaggle data type (that is, every object that gets sent from one goose to another) implements the interface GaggleData
 * (http://gaggle.systemsbiology.net/2007-04/javadoc/org/systemsbiology/gaggle/core/datatypes/GaggleData.html). You can see here that every broadcast includes a Tuple where you can stuff any sort of metadata along with your broadcast. A GaggleTuple is simply a top-level Gaggle datatype where the data and metadata are both Tuples.
 *
 * An important concept in the way that Gaggle uses tuples is this: You can fill a tuple with whatever you want, however you want. The receiving goose must know how to "unpack" the tuple. Therefore there is an informal contract between these two geese. Other pairs of geese might honor a different contract.
 *
 *
 * The Tuple class (duh) represents a Tuple, which is composed of a name and a list of Singles.
 * A Single is the basic unit of data within a tuple and consists of an optional name and a value. The value is of type Serializable and must be an Integer, Double, String, or another Tuple, or a runtime exception will be thrown. This restriction allows complex list-of-lists behavior while keeping the basic data types to those commonly needed in systems biology, and avoiding complex object graphs and possible serialization problems.
 *
 * The implementation of the classes described above is as simple as possible, and could probably be much more efficient. However, the simplicity exists for a reason. Because Gaggle uses Java RMI for inter-VM communication, the classes on either side of the communication must have the same set of method signatures. Otherwise an exception is thrown. This means these classes are essentially frozen and cannot be changed until a new version of Gaggle is released;  otherwise all goose developers would immediately have to get the new class, to avoid communication failures between geese.
 *
 * We are looking into augmenting and/or replacing RMI with a platform-neutral messaging technology which would ease these restrictions.
 *
 * So the Tuple classes are basically POJOs or DTOs if you like, with as little actual code in them as possible.
 *
 * We envision either static utility classes or wrapper classes which would do much of the heavy lifting as far as parsing and traversing tuples and retrieving specified components. These classes would not be top-level and could therefore be continually refined without breaking the Gaggle API.
 *
 *
 *
 *
 * @see Single
 */
public class Tuple implements Serializable {
    List<Single> singleList = null;
    String name;

    public Tuple () {
    	singleList = new ArrayList<Single>();
    }

    public Tuple(String name, List<Single> singleList) {
        this.name = name;
        this.singleList = singleList;
    }

    public Tuple(String name) {
        this(name, new ArrayList<Single>());
    }


    public List<Single> getSingleList() {
        return singleList;
    }

    public void setSingleList(List<Single> singleList) {
        this.singleList = singleList;
    }

    public void addSingle(Single single) {
        singleList.add(single);
    }


    public Single getSingleAt(int index) {
        return singleList.get(index);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {

        String nameToShow = (name == null ) ? "(no name)" : name;
        if (singleList.size() == 0) {
            return "empty tuple with name " + nameToShow;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(nameToShow);
        sb.append(": ");
        for (Single single : singleList) {
            sb.append(single.toString());
            sb.append(" ");
        }
        return sb.toString();
    }
}
