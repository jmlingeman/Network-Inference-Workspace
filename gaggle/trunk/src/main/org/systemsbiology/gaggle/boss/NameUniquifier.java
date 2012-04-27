package org.systemsbiology.gaggle.boss;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/*
* Copyright (C) 2009 by Institute for Systems Biology,
* Seattle, Washington, USA.  All rights reserved.
*
* This source code is distributed under the GNU Lesser
* General Public License, the text of which is available at:
*   http://www.gnu.org/copyleft/lesser.html
*/
public class NameUniquifier {


    public static String makeUnique(String candidate, String[] existingGooseNames) {
        if (existingGooseNames.length == 0) {
            return candidate;
        }


        List<String> gooseNamesList = Arrays.asList(existingGooseNames);


        String basename = "";

        if (!candidate.contains("-")) {
            basename = candidate;
        } else {
            String[] segs = candidate.split("-");

            for (int i = 0; i < (segs.length -1); i++) {
                basename += segs[i];
                if (i < segs.length -2) {
                    basename += "-";
                }
            }
        }


        List<String> temp = new ArrayList<String>();
        for (String name : gooseNamesList) {
            if (name.startsWith(basename)) {
                temp.add(name);
            }
        }
        if (temp.size() == 0) {
            return candidate;
        }




        int max = 0;
        int suffix;
        for (String name : temp) {
            String[] segs = name.split("-");
            String lastSeg = segs[segs.length-1];
            try {
                suffix = Integer.parseInt(lastSeg);
            } catch (NumberFormatException nfe) {
                continue;
            }
            if (suffix > max) {
                max = suffix;
            }
        }
        String stringToReturn = candidate;
        max++;

        stringToReturn += "-";
        if (max < 10) {
            stringToReturn += "0";
        }
        return stringToReturn += max;

     
    }

}
