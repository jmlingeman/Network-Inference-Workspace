package org.systemsbiology.gaggle.core.datatypes;

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
 * An n = 1 tuple with an optional name identifier.
 * The basic unit of which the Tuple class is composed.
 * Values must be of type Integer, Double, String, or Tuple, or an exception will be thrown.
 * This is done in order to keep the Tuple from being used as a complicated structured
 * object that may include non-serializable elements. In any case, Integer, Double, and String
 * (and nested Tuples) are sufficient for practically every systems biology application.
 * (2008-05-05 - Added Float, Double, Boolean, and GaggleData).
 *
 *
 * @see Tuple
 */
public class Single implements Serializable {
    private String name;

    private Serializable value;


    public Single() {
    }

    public Single(Serializable value) {
        this(null, value);
    }

    public Single(String name, Serializable value) {
        this.name = name;
        setValue(value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Serializable getValue() {
        return value;
    }

    public void setValue(Serializable value) {
        if (!(  value instanceof String ||
                value instanceof Integer ||
                value instanceof Long ||
                value instanceof Float ||
                value instanceof Double ||
                value instanceof Boolean ||
                value instanceof GaggleData ||
                value instanceof Tuple)) {
            throw new IllegalArgumentException("Value must be a String, Integer, Long, Float, Double, Boolean, or Tuple.");
        }
        this.value = value;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        if (name != null) {
            sb.append(name);
            sb.append(" = ");
        }
        sb.append(String.valueOf(getValue()));
        sb.append(")");
        return sb.toString();
    }

}
