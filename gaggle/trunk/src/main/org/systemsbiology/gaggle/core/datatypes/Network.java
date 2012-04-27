// Network.java
//--------------------------------------------------------------------
// $Revision: 2605 $
// $Date: 2007-11-30 17:43:33 -0500 (Fri, 30 Nov 2007) $
// $Author: dtenenba $
//--------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.core.datatypes;
//--------------------------------------------------------------------

/**
 * Encapsulates the notion of a network graph.
 */
import java.util.*;

//--------------------------------------------------------------------
public class Network implements GaggleData {


    List<Interaction> interactionList = new ArrayList<Interaction>();
    Map<String, HashMap<String, Object>> nodeAttributes =
            new HashMap<String, HashMap<String, Object>>();
    Map<String, HashMap<String, Object>> edgeAttributes =
            new HashMap<String, HashMap<String, Object>>();
    Set<String> nodes = new HashSet<String>();
    String species = "unknown";
    String name;
    Tuple metadata;

    //--------------------------------------------------------------------
    public Network() {
    }

    //--------------------------------------------------------------------
    public void add(Interaction interaction) {
        interactionList.add(interaction);
        add(interaction.getSource());
        add(interaction.getTarget());

    }

    //--------------------------------------------------------------------
    public void add(String nodeName) {
        nodes.add(nodeName);

    }

    //--------------------------------------------------------------------
    public void add(Interaction[] interactions) {
        for (Interaction interaction : interactions) add(interaction);
    }

    //--------------------------------------------------------------------
    public Interaction[] getInteractions() {
        return interactionList.toArray(new Interaction[0]);
    }

    //--------------------------------------------------------------------
    public int nodeCount() {
        return nodes.size();
    }

    //--------------------------------------------------------------------
    public String[] getNodes() {
        return nodes.toArray(new String[0]);
    }

    //--------------------------------------------------------------------
    public HashSet<String> getConnectedNodes() {
        HashSet<String> result = new HashSet<String>();
        Interaction[] interactions = getInteractions();
        for (Interaction interaction : interactions) {
            result.add(interaction.getSource());
            result.add(interaction.getTarget());
        } // for i

        return result;

    } // getConnectedNodes

    //--------------------------------------------------------------------
    public String[] getOrphanNodes() {
        HashSet<String> result = new HashSet<String>();
        HashSet connectedNodes = getConnectedNodes();
        String[] allNodes = getNodes();
        for (String node : allNodes) {
            if (!connectedNodes.contains(node))
                result.add(node);
        }
        return result.toArray(new String[0]);

    } // getOrphanNodes

    //--------------------------------------------------------------------
    public int getOrphanNodeCount() {
        return getOrphanNodes().length;
    }

    //--------------------------------------------------------------------
    public int edgeCount() {
        return interactionList.size();
    }

    protected void validateObjectType(Object value) {
        if (!(value instanceof Double ||
                value instanceof String ||
                value instanceof Integer ||
                value instanceof Vector
        )) {
            throw new IllegalArgumentException("Value must be a String, Double, or Integer.");
        }
    }
    //--------------------------------------------------------------------
/*
 * an edgeName looks like: node1 (interaction) node2.
 */
    public void addEdgeAttribute(String edgeName, String attributeName, Object value) {
        validateObjectType(value);
        if (!edgeAttributes.containsKey(attributeName))
            edgeAttributes.put(attributeName, new HashMap<String, Object>());

        HashMap<String, Object> attributeHash = edgeAttributes.get(attributeName);
        attributeHash.put(edgeName, value);

    } // addEdgeAttribute

    //--------------------------------------------------------------------
    public void addNodeAttribute(String nodeName, String attributeName, Object value) {
        validateObjectType(value);
        if (!nodeAttributes.containsKey(attributeName))
            nodeAttributes.put(attributeName, new HashMap<String, Object>());

        HashMap<String, Object> attributeHash = nodeAttributes.get(attributeName);
        attributeHash.put(nodeName, value);

    } // addNodeAttribute

    //--------------------------------------------------------------------
    public String[] getNodeAttributeNames() {
        return nodeAttributes.keySet().toArray(new String[0]);
    }

    //--------------------------------------------------------------------
    public String[] getEdgeAttributeNames() {
        return edgeAttributes.keySet().toArray(new String[0]);
    }

    //--------------------------------------------------------------------
    // todo genericize (in next api release)  <String,Object>
    public HashMap getEdgeAttributes(String attributeName) {
        return edgeAttributes.get(attributeName);
    }

    //--------------------------------------------------------------------
    // todo genericize (in next api release)  <String,Object>
    public HashMap getNodeAttributes(String attributeName) {
        return nodeAttributes.get(attributeName);
    }

    //--------------------------------------------------------------------
    public void setSpecies(String newValue) {
        species = newValue;
    }

    //--------------------------------------------------------------------
    public String getSpecies() {
        return species;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Tuple getMetadata() {
        return metadata;
    }

    public void setMetadata(Tuple metadata) {
        this.metadata = metadata;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        Interaction[] interactions = getInteractions();
        for (Interaction interaction : interactions) {
            sb.append(interaction.toString());
            sb.append("\n");
        }

        return sb.toString();

    } // toString
//--------------------------------------------------------------------
} // class Network
