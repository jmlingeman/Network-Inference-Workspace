package org.systemsbiology.gaggle.boss.unitTests;

import junit.framework.TestCase;
import org.systemsbiology.gaggle.boss.NameUniquifier;

/*
* Copyright (C) 2009 by Institute for Systems Biology,
* Seattle, Washington, USA.  All rights reserved.
*
* This source code is distributed under the GNU Lesser
* General Public License, the text of which is available at:
*   http://www.gnu.org/copyleft/lesser.html
*/
public class NameUniquifierTest extends TestCase {

    public void testNewNameUniquifier() {
        String result = NameUniquifier.makeUnique("blah", new String[0]);
        assertEquals("blah", result);
        result = NameUniquifier.makeUnique("blah", new String[] {"blah"});
        assertEquals("blah-01", result);
        result = NameUniquifier.makeUnique("blah", new String[] {"blah", "blah-01", "blah-02"});
        assertEquals("blah-03", result);
        result = NameUniquifier.makeUnique("blah", new String[] {"blah-01", "blah-02"});
        assertEquals("blah-03", result); 
        result = NameUniquifier.makeUnique("blah", new String[] {"blah", "blah-bogus"});
        assertEquals("blah-01", result);
        result = NameUniquifier.makeUnique("blah", new String[] {"blah", "blah-bogus", "blah-01"});
        assertEquals("blah-02", result);
        result = NameUniquifier.makeUnique("blah", new String[] {"blah", "blah-08"});
        assertEquals("blah-09", result);
        result = NameUniquifier.makeUnique("blah", new String[] {"blah", "blah-03"});
        assertEquals("blah-04", result);
        result = NameUniquifier.makeUnique("blah", new String[] {"blah", "blah-22"});
        assertEquals("blah-23", result);
        result = NameUniquifier.makeUnique("blah", new String[] {"blah", "something", "blah-22"});
        assertEquals("blah-23", result);
        result = NameUniquifier.makeUnique("my-goose", new String[] {"my-goose", "something", "my-goose-22"});
        assertEquals("my-goose-23", result);
        result = NameUniquifier.makeUnique("my-funny-goose", new String[] {"my-funny-goose", "something", "my-funny-goose-22"});
        assertEquals("my-funny-goose-23", result);
    }

}
