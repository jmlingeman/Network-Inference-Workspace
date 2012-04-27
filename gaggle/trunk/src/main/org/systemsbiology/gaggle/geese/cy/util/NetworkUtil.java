// NetworkUtil.java
//-------------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.cy.util;
//-------------------------------------------------------------------------------------------

import java.util.*;
import java.lang.reflect.*;
import java.net.URL;

import y.base.*;
import y.view.*;
import cytoscape.*;
import cytoscape.undo.UndoableGraphHider;
import org.systemsbiology.gaggle.core.datatypes.Network;

public class NetworkUtil {
    //-------------------------------------------------------------------------------------------
/**
 * add everything from network to the yFiles graph (from 'cw', the CytoscapeWindow) and
 * its accompanying node and edge attributes.  cw is changed.
 */
    public static void extend(CytoscapeWindow cw, Network network) {
        org.systemsbiology.gaggle.core.datatypes.Interaction[] interactions = network.getInteractions();
        Graph2D cyGraph = cw.getGraph();
        GraphObjAttributes nodeAttributes = cw.getNodeAttributes();
        GraphObjAttributes edgeAttributes = cw.getEdgeAttributes();

        HashMap cwNodes = getCurrentNodes(cw);

        // go through the new interactions, get (or create) the equivalent Cytoscape
        // source and target nodes; create new edges were necessary

        for (int i = 0; i < interactions.length; i++) {
            String sourceName = interactions[i].getSource();
            String targetName = interactions[i].getTarget();
            String interactionType = interactions[i].getType();
            //System.out.println ("adding interaction #" + i + " " +
            //                    sourceName + " (" + interactionType + ") " + targetName);
            Node sourceNode = (Node) cwNodes.get(sourceName);
            if (sourceNode == null) {
                sourceNode = cyGraph.createNode(200.0, 200.0, 70.0, 30.0, sourceName);
                cwNodes.put(sourceName, sourceNode);
                nodeAttributes.addNameMapping(sourceName, sourceNode);
            }

            Node targetNode = (Node) cwNodes.get(targetName);
            if (targetNode == null) {
                targetNode = cyGraph.createNode(300.0, 300.0, 70.0, 30.0, targetName);
                cwNodes.put(targetName, targetNode);
                nodeAttributes.addNameMapping(targetName, targetNode);
            }

            //int edgeAttributesWithThisName = edgeAttributes.countIdentical (edgeName);
            //System.out.println (" edgeAttributesWithThisName: " + edgeAttributesWithThisName);
            //if (edgeAttributesWithThisName == 0
            //GraphObjAttributes edgeAttributes = cw.getEdgeAttributes ();

            String edgeName = sourceName + " (" + interactionType + ") " + targetName;
            int matchingEdges = findMatchingEdges(cw, edgeName);
            if (findMatchingEdges(cw, edgeName) == 0) {
                //System.out.println (" no matching edges...");
                Edge edge = cyGraph.createEdge(sourceNode, targetNode);
                edgeAttributes.append("interaction", edgeName, interactionType);//changed from "add" dt
                edgeAttributes.addNameMapping(edgeName, edge);
            } // if new edge
            copyAllEdgeAttributes(cw, network, sourceName, targetName, interactionType);
            copyAllNodeAttributes(cw, network, sourceName);
            copyAllNodeAttributes(cw, network, targetName);
        } // for i

        String[] orphanNodes = network.getOrphanNodes();
        //System.out.println ("--- now checking for orphan nodes: " + orphanNodes.length);
        for (int i = 0; i < orphanNodes.length; i++) {
            String orphanName = orphanNodes[i];
            //System.out.println ("adding orphan named " + orphanName);
            Node cwNode = (Node) cwNodes.get(orphanName);
            if (cwNode == null) {
                cwNode = cyGraph.createNode(200.0, 200.0, 70.0, 30.0, orphanName);
                cwNodes.put(orphanName, cwNode);
                nodeAttributes.addNameMapping(orphanName, cwNode);
            } // if new node needed
            copyAllNodeAttributes(cw, network, orphanName);
        } // for i

        cw.displayCommonNodeNames();

    } // extend

    //-------------------------------------------------------------------------------------------
    protected static int findMatchingEdges(CytoscapeWindow cw, String edgeName) {
        int matchCount = 0;
        Graph2D graph = cw.getGraph();
        //System.out.println ("looking for edges matching >" + edgeName + "<");
        for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
            Edge edge = ec.edge();
            String canonicalName = cw.getEdgeAttributes().getCanonicalName(edge);
            //System.out.println ("canonical edge name: " + canonicalName);
            if (canonicalName.equals(edgeName))
                matchCount++;
        } // for ec

        return matchCount;

    } // findMatchingEdges

    //-------------------------------------------------------------------------------------------
    protected static void copyAllEdgeAttributes(CytoscapeWindow cw, Network network,
                                                String sourceName, String targetName,
                                                String interactionType)

// the new network -- rec'd by broadcast -- has some number of attributes on the edges.
// the new network stores all the attributes of all of its edges in a hashmap of hashmaps.
// the higher level hashmap is keyed by attribute name (say, 'confidence' or 'weight)
// for each of these, there is a lower level hashmap, keyed by edge name
// strategy:
//   - loop through all edge attribute names for the newly received network
//   - get the hashmap associated with that edge attribute name
//   - see if that (lower-level) hashmap has a value for the current edge
//   - if it does, add that value to the old network
    {
        //System.out.println ("\n\n              NetworkUtil.copyAllEdgeAttributes\n\n");

        String edgeName = sourceName + " (" + interactionType + ") " + targetName;
        GraphObjAttributes oldAttributes = cw.getEdgeAttributes();
        String[] newNames = network.getEdgeAttributeNames();

        try {
            for (int n = 0; n < newNames.length; n++) {
                String newName = newNames[n];
                HashMap newMap = network.getEdgeAttributes(newName);
                if (newMap.containsKey(edgeName)) {
                    Object value = newMap.get(edgeName);
                    Class attributeClass = value.getClass();
                    if (attributeClass.getName().equals("java.lang.String")) try {
                        URL url = new URL((String) value);
                        attributeClass = Class.forName("java.net.URL");
                    }
                    catch (Exception ex1) {
                        attributeClass = Class.forName("java.lang.String");
                    }
                    if (!oldAttributes.hasAttribute(newName))  // a new attribute, unknow to existing cy network
                        oldAttributes.setClass(newName, attributeClass);
                    else {
                        attributeClass = oldAttributes.getClass(newName);
                        //System.out.println (">>> class type for attribute named '" + newName + "': " + attributeClass);
                    }
                    //System.out.println (" about to copy " + newName + " for edge " + edgeName + ": " + value +
                    //                    " of class: " + attributeClass.toString ());
                    Object obj = createInstanceFromString(attributeClass, value.toString());
                    oldAttributes.append(newName, edgeName, obj);//changed from 'add' - dt
                } // if the new network has an attribute for the current edge
            } // for n over newNames
        } // try
        catch (Exception ex0) {
            ex0.printStackTrace();
            System.out.println(ex0.getMessage());
        }

    } // copyAllEdgeAttributes

    //-------------------------------------------------------------------------------------------
/**
 * determine (heuristically) the most-specialized class instance which can be
 * constructed from the supplied string.
 */
    protected static Class deduceClass(String string) {
        String[] classNames = {"java.net.URL",
                "java.lang.Integer",    // using this breaks the vizmapper, see below
                "java.lang.Double",
                "java.lang.String"};

        for (int i = 0; i < classNames.length; i++) {
            try {
                if (string.indexOf("http://") == 0)
                    return Class.forName("java.net.URL");
                Object obj = createInstanceFromString(Class.forName(classNames[i]), string);
                return obj.getClass();
            }
            catch (Exception e) {
                ; // try the next class
            }
        } // for i

        return null;

    } // deduceClass

    //--------------------------------------------------------------------------------
/**
 * given a string and a class, dynamically create an instance of that class from
 * the string
 */
    public static Object createInstanceFromString(Class requestedClass, String ctorArg)
            throws Exception

    {
        Class[] ctorArgsClasses = new Class[1];
        ctorArgsClasses[0] = Class.forName("java.lang.String");
        Object[] ctorArgs = new Object[1];
        ctorArgs[0] = new String(ctorArg);
        Constructor ctor = requestedClass.getConstructor(ctorArgsClasses);
        return ctor.newInstance(ctorArgs);

    } // createInstanceFromString

    //--------------------------------------------------------------------------------
    protected static void copyAllNodeAttributes(CytoscapeWindow cw, org.systemsbiology.gaggle.core.datatypes.Network network, String nodeName) {
        String[] newNames = network.getNodeAttributeNames();
        GraphObjAttributes oldAttributes = cw.getNodeAttributes();

        try {
            for (int n = 0; n < newNames.length; n++) {
                String newName = newNames[n];
                HashMap newMap = network.getNodeAttributes(newName);
                if (newMap.containsKey(nodeName)) {
                    //System.out.println ("\n\t\t\00000000000000000000000000000000000\n");
                    Object value = newMap.get(nodeName);
                    if (value == null) {
                        System.err.println("null value for node " + nodeName + " attribute " + newName);
                        continue;
                    }
                    Class attributeClass = value.getClass();
                    if (attributeClass.getName().equals("java.lang.String")) try {
                        URL url = new URL((String) value);
                        attributeClass = Class.forName("java.net.URL");
                    }
                    catch (Exception ex1) {
                        attributeClass = Class.forName("java.lang.String");
                    }
                    if (!oldAttributes.hasAttribute(newName))  // a new attribute, unknown to existing cy network
                        oldAttributes.setClass(newName, attributeClass);
                    else
                        attributeClass = oldAttributes.getClass(newName);
                    Object obj = createInstanceFromString(attributeClass, value.toString());
                    //System.out.println (" about to copy " + newName + " for node " + nodeName + ": " + value);
                    oldAttributes.append(newName, nodeName, obj);//changed from 'add' dt
                } // if  the new networks has an attribute for the current node
            } // for n over newNames
        } // try
        catch (Exception ex0) {
            ex0.printStackTrace();
            System.out.println(ex0.getMessage());
        }

    } // transferAllNodeAttributes

    //-------------------------------------------------------------------------------------------
    public static Network createNetworkFromSelection(CytoscapeWindow cw) {
        Graph2D graph = cw.getGraph();
        SelectedSubGraphFactory factory =
                new SelectedSubGraphFactory(graph, cw.getNodeAttributes(), cw.getEdgeAttributes());
        Graph2D subGraph = factory.getSubGraph();
        GraphObjAttributes nodeAttributes = factory.getNodeAttributes();
        GraphObjAttributes edgeAttributes = factory.getEdgeAttributes();
        Node[] nodes = subGraph.getNodeArray();
        Edge[] edges = subGraph.getEdgeArray();
        //System.out.println ("node count: " + nodes.length);
        //System.out.println ("edge count: " + edges.length);

        HashSet allNodes = new HashSet();
        Network network = new Network();

        for (int i = 0; i < nodes.length; i++) {
            String nodeName = nodeAttributes.getCanonicalName(nodes[i]);
            network.add(nodeName);
            HashMap attributes = nodeAttributes.getAttributes(nodeName);
            String[] attributeNames = (String[]) attributes.keySet().toArray(new String[0]);
            for (int j = 0; j < attributeNames.length; j++) {
                String attributeName = attributeNames[j];
                Object value = attributes.get(attributeName);
                if (value instanceof URL) {
                    network.addNodeAttribute(nodeName, attributeName, ((URL)value).toString());
                }
            } // for j
        } // for i

        for (int i = 0; i < edges.length; i++) {
            String edgeName = edgeAttributes.getCanonicalName(edges[i]);
            String sourceName = nodeAttributes.getCanonicalName(edges[i].source());
            String targetName = nodeAttributes.getCanonicalName(edges[i].target());
            String interactionType = edgeAttributes.getStringValue("interaction", edgeName);
            network.add(new org.systemsbiology.gaggle.core.datatypes.Interaction(sourceName, targetName, interactionType));
            HashMap attributes = edgeAttributes.getAttributes(edgeName);
            String[] attributeNames = (String[]) attributes.keySet().toArray(new String[0]);
            for (int j = 0; j < attributeNames.length; j++) {
                String attributeName = attributeNames[j];
                Object value = attributes.get(attributeName);
                if (value instanceof URL) {
                    network.addEdgeAttribute(edgeName, attributeName, ((URL)value).toString());
                }
            } // for j
        } // for i

        return network;

    } // createNetworkFromSelection

    //-------------------------------------------------------------------------------------------
    public static HashMap getCurrentNodes(CytoscapeWindow cw) {
        Graph2D graph = cw.getGraph();
        HashMap result = new HashMap();
        for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
            Node node = nc.node();
            String name = cw.getCanonicalNodeName(node);
            //String name = node.toString ();
            //System.out.println ("adding node named " + name + " to hash");
            result.put(name, node);
        } // for nc

        return result;

    } // getCurrentNodes

    //---------------------------------------------------------------------------------------
/**
 * 'orphan' is defined broadly here to include, not only all unconnected nodes, but also
 * all nodes not connected, directly or indirectly, to a bicluster.  a simple trick
 * makes it easy to figure out where the second rule applies:
 * <ol>
 * <li> find all the logicGate nodes
 * <li> for each, count the number of regulatory edges (type = 'activates' or 'represses')
 * <li> when that count is zero, remove ALL edges on the logicGate node
 * </ol>
 * <p/>
 * after these edges are removed, the final step is simply to hide all nodes with no edges
 */
    public static void hideOrphans(CytoscapeWindow cw, UndoableGraphHider graphHider) {
        Node[] nodes = cw.getGraph().getNodeArray();

        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].degree() == 0)
                graphHider.hide(nodes[i]);
        } // for i

        cw.redrawGraph();

    } // hideOrphans
//-----------------------------------------------------------------------------------

} // class NetworkUtil
