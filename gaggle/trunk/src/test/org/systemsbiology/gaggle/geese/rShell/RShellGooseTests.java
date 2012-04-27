package org.systemsbiology.gaggle.geese.rShell;

import junit.framework.TestCase;/*
* Copyright (C) 2007 by Institute for Systems Biology,
* Seattle, Washington, USA.  All rights reserved.
*
* This source code is distributed under the GNU Lesser
* General Public License, the text of which is available at:
*   http://www.gnu.org/copyleft/lesser.html
*/

/**
 * Tests some java-only aspects of the goose. For further testing please see the R
 * unit tests.
 */



public class RShellGooseTests extends TestCase {

    RShellGoose goose;

    public void setUp() {
        goose = new RShellGoose();
    }




    public void testGetValueType() throws Exception {
        String[] args = new String[1];
        args[0] = "'25'";
        Class klass = goose.getValueType(args);
        assertEquals(String.class, klass);
    }

    public void testCreateAndBroadcastGaggleTuple() throws Exception {
        String[] names = new String[1];
        names[0] = "name";
        String[] values = new String[1];
        values[0] = "|25|";
        goose.createAndBroadcastGaggleTuple("title", "attributename", names, values);


    }

    public void testFoo() {
        Class c = Double.class;
        System.out.println("double class looks like this: " +c);
    }


}
