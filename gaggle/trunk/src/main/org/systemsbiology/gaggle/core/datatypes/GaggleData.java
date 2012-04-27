package org.systemsbiology.gaggle.core.datatypes;

import java.io.Serializable;

/**
 * Every gaggle data type implements this.
 *
 */
public interface GaggleData extends Serializable {

    /**
     * Returns the name of this GaggleData object
     * @return the name of this GaggleData object
     */
    public String getName();

    /**
     * Returns the species of this GaggleData object
     * @return the species of this GaggleData object
     */
    public String getSpecies();

    /**
     * Every GaggleData object contains a Tuple which can contain optional metadata.
     * The Tuple is a general-purpose object which can hold data in many ways;
     * it is up to the sending and receiving goose to put together and parse out the
     * metadata from the Tuple.
     * @return a Tuple containing optional metadata
     */
    public Tuple getMetadata();

}
