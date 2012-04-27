// Translator.java
// given a NewNameHelper, and and any gaggle message (names, 
// network, hash, matrix) translate from old names to the specified new names
//---------------------------------------------------------------------------------
// $Revision: 332 $   
// $Date: 2005/04/03 19:15:04 $
//---------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.names;
//---------------------------------------------------------------------------------

import java.util.*;

import org.systemsbiology.gaggle.util.NewNameHelper;
import org.systemsbiology.gaggle.core.datatypes.*;

//---------------------------------------------------------------------------------
public class Translator {

    NewNameHelper nameHelper;

    //---------------------------------------------------------------------------------
    public Translator(NewNameHelper nameHelper) {
        this.nameHelper = nameHelper;
    }

    //---------------------------------------------------------------------------------
    public String get(String name, String targetNameSpace) {
        //System.out.println ("Translator.get (" + name + ", " + targetNameSpace + ")");
        HashMap allAliases = nameHelper.getInfo(name);
        if (allAliases == null) {
            return name;
        }

        String alias = (String) allAliases.get(targetNameSpace);
        if (alias == null) {
            return name;
        }

        return alias;

    } // get (String)

    //---------------------------------------------------------------------------------
    public String[] get(String[] names, String targetNameSpace) {
        String[] result = new String[names.length];
        for (int i = 0; i < names.length; i++)
            result[i] = get(names[i], targetNameSpace);

        return result;

    } // get (String [] names)

    //---------------------------------------------------------------------------------
    public Interaction get(Interaction interaction, String targetNameSpace) {
        String source = get(interaction.getSource(), targetNameSpace);
        String target = get(interaction.getTarget(), targetNameSpace);
        String type = interaction.getType();
        boolean directed = interaction.isDirected();

        Interaction result = new Interaction(source, target, type, directed);

        return result;

    } // get (Interaction)

    //---------------------------------------------------------------------------------
    public Network get(Network network, String targetNameSpace) {
        Network result = new Network();
        Interaction[] interactions = network.getInteractions();
        for (int i = 0; i < interactions.length; i++) {
            result.add(get(interactions[i], targetNameSpace));
        } // for i

        String[] orphanNodes = network.getOrphanNodes();
        for (int i = 0; i < orphanNodes.length; i++) {
            result.add(get(orphanNodes[i], targetNameSpace));
        }

        // attributes are stored in a nested hash
        //    attributeName: {nodeName: value}
        // as in 'moleculeType': {'VNG0212':'DNA'}
        // to translate, go through all the top level keys, get node:value hashes,
        // and change the node names

        String[] origAttributeNames = network.getNodeAttributeNames();

        for (int i = 0; i < origAttributeNames.length; i++) {
            HashMap map = network.getNodeAttributes(origAttributeNames[i]);
            String[] nodeNames = (String[]) map.keySet().toArray(new String[0]);
            for (int n = 0; n < nodeNames.length; n++) {
                String oldName = nodeNames[n];
                String newName = get(oldName, targetNameSpace);
                Object value = map.get(oldName);
                result.addNodeAttribute(newName, origAttributeNames[i], value);
            } // for n
        } // for i

        origAttributeNames = network.getEdgeAttributeNames();

        for (int i = 0; i < origAttributeNames.length; i++) {
            HashMap map = network.getEdgeAttributes(origAttributeNames[i]);
            String[] edgeNames = (String[]) map.keySet().toArray(new String[0]);
            for (int n = 0; n < edgeNames.length; n++) {
                String rawName = edgeNames[n];
                String[] parsedParts = Translator.parseEdgeName(rawName);
                String a = parsedParts[0];
                String b = parsedParts[2];
                String edgeType = parsedParts[1];
                //System.out.println ("a '" + a + "'");
                //System.out.println ("b '" + b + "'");
                //System.out.println ("type '" + edgeType + "'");
                String newA = get(a, targetNameSpace);
                String newB = get(b, targetNameSpace);
                String newEdgeName = newA + " " + edgeType + " " + newB;
                //System.out.println ("new edge name: " + newEdgeName);
                Object value = map.get(rawName);
                result.addEdgeAttribute(newEdgeName, origAttributeNames[i], value);
            } // for n
        } // for i

        return result;

    } // get (Network)

    //---------------------------------------------------------------------------------
    public org.systemsbiology.gaggle.core.datatypes.DataMatrix get(org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix, String targetNameSpace) {
        org.systemsbiology.gaggle.core.datatypes.DataMatrix result = new org.systemsbiology.gaggle.core.datatypes.DataMatrix();
        result.setSize(matrix.getRowCount(), matrix.getColumnCount());
        result.setColumnTitles(matrix.getColumnTitles());
        result.setRowTitlesTitle(matrix.getRowTitlesTitle());
        String[] oldRowNames = matrix.getRowTitles();
        String[] newRowNames = get(oldRowNames, targetNameSpace);
        result.setRowTitles(newRowNames);
        result.set(matrix.get());
        return result;

    } // get (DataMatrix)

    //---------------------------------------------------------------------------------

    /*
    public AttributeMap get(AttributeMap map, String targetNameSpace) {
        AttributeMap result = new AttributeMap();
        result.setName(map.getName());
        result.setSpecies(map.getSpecies());

        String[] attributeNames = AttributeMapUtil.getAttributeNames(map);
        for (String attributeName : attributeNames) {
            String[] names = AttributeMapUtil.getSortedNames(map, attributeName);
            Object[] values = AttributeMapUtil.getValuesInSortedNameOrder(map, attributeName);
            String[] translatedNames = get(names, targetNameSpace);
            AttributeMapUtil.addAttribute(result, attributeName, translatedNames, values);
        }
        return result;

    }
    */


    //---------------------------------------------------------------------------------
    static public String[] parseEdgeName(String rawName) {
        int firstParen = rawName.indexOf("(");
        int secondParen = rawName.indexOf(")") + 1;
        if (firstParen < 0 || secondParen < 0 || secondParen < firstParen)
            throw new IllegalArgumentException("ill-formed edge name: " + rawName);
        String[] leftParenTokens = rawName.split("\\(");
        String[] rightParenTokens = rawName.split("\\)");
        if (leftParenTokens.length != 2)
            throw new IllegalArgumentException("ill-formed edge name: " + rawName);
        if (rightParenTokens.length != 2)
            throw new IllegalArgumentException("ill-formed edge name: " + rawName);


        String a = rawName.substring(0, firstParen).trim();
        String b = rawName.substring(secondParen).trim();
        String edgeType = rawName.substring(firstParen, secondParen).trim();

        return new String[]{a, edgeType, b};

    } // parseEdgeName
//---------------------------------------------------------------------------------
} // class Translator
