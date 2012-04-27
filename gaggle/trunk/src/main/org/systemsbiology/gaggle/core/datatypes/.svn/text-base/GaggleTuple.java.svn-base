package org.systemsbiology.gaggle.core.datatypes;

/*
* Copyright (C) 2007 by Institute for Systems Biology,
* Seattle, Washington, USA.  All rights reserved.
*
* This source code is distributed under the GNU Lesser
* General Public License, the text of which is available at:
*   http://www.gnu.org/copyleft/lesser.html
*/

/**
 * The top-level GaggleDataType for transmitting relatively-free form
 * data structures. This replaces the old gaggle API which transmitted
 * HashMap objects; the Tuple can do anything that a HashMap can do
 * and more. Note that this object is actually composed of two Tuple
 * objects, as the GaggleData interface requires one for metadata.
 */
public class GaggleTuple implements GaggleData {
    String name;
    String species;
    Tuple data = new Tuple();
    Tuple metadata = new Tuple();

    public GaggleTuple() {}


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }


    public Tuple getData() {
        return data;
    }

    public void setData(Tuple data) {
        this.data = data;
    }

    public Tuple getMetadata() {
        return metadata;
    }

    public void setMetadata(Tuple metadata) {
        this.metadata = metadata;
    }
}
