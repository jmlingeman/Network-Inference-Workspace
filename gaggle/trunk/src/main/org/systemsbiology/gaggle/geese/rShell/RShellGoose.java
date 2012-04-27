// RShellGoose.java
//-------------------------------------------------------------------------------------
// $Revision: 2680 $   
// $Date: 2007-12-20 13:41:25 -0800 (Thu, 20 Dec 2007) $
//-------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese.rShell;

import java.rmi.*;

import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.datatypes.*;
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;
import org.systemsbiology.gaggle.geese.common.GooseShutdownHook;
import org.systemsbiology.gaggle.geese.common.GaggleConnectionListener;

import java.util.*;
import java.io.Serializable;

//-------------------------------------------------------------------------------------
public class RShellGoose implements Goose, GaggleConnectionListener {

    String myGaggleName = "R";
    Boss gaggleBoss;
    RmiGaggleConnector connector = new RmiGaggleConnector(this);
    String gaggleBossHostName = "localhost";
    static RShellGoose self;

    boolean verbose = false;

    String[] activeGeeseNames = new String[0];

    // the gaggle data types.  once received from a broadcast, these class
    // instance variables are assigned the incoming value, and keep them
    // until new values are received

    String[] nameList = new String[0];
    DataMatrix matrix = null;
    Network network = null;
    GaggleTuple gaggleTuple = null;

    String clusterName = null;
    String[] clusterRowNames = null;
    String[] clusterColumnNames = null;

    String defaultSpecies = "Homo sapiens";
    String targetGoose = "all";

    Map<String,String> networkMetadata = new HashMap<String, String>();


    //-------------------------------------------------------------------------------------
    public RShellGoose() {
        this("localhost", false);

    } // ctor

    //-------------------------------------------------------------------------------------
    public RShellGoose(boolean verbose) {
        this("localhost", verbose);

    } // ctor

    //-------------------------------------------------------------------------------------
    public RShellGoose(String gaggleBossHostName) {
        this(gaggleBossHostName, false);

    } // ctor

    //-------------------------------------------------------------------------------------
    public RShellGoose(String gaggleBossHostName, boolean verbose) {
        new GooseShutdownHook(connector); // todo find out why this isn't working

        self = this;
        this.gaggleBossHostName = gaggleBossHostName;
        this.verbose = verbose;
        try {
            connectToGaggle();
        }
        catch (Exception ex0) {
            System.err.println("RShellGoose failed to export remote object: " + ex0.getMessage());
        }

    } // ctor

    //-------------------------------------------------------------------------------------
    public String getVersion() {
        String id = "$Revision: 2680 $";
        String signature = "Revision: ";
        int start = id.indexOf(signature);
        start += signature.length();
        int end = id.indexOf(" $", start);
        return id.substring(start, end);

    }

    //-------------------------------------------------------------------------------------
    public void connectToGaggle() {
        try {
            connector.connectToGaggle(gaggleBossHostName);
            System.out.println("Connected to gaggle with goose name " + getName());
            System.out.println("Use the disconnectFromGaggle() method to disconnect.");
        }
        catch (Exception ex0) {
            System.err.println("\n\n\tFailed to connect to Gaggle Boss:\n\t\t" +
                    ex0.getMessage());
            System.err.println("\n\tPossible causes: ");
            System.err.println("\t\t1) There is no Gaggle Boss running.");
            System.err.println("\t\t2) You are using an out-of-date Gaggle Boss.");
            System.err.println("\t\t3) Hang on a sec, a boss is probably autostarting now;");
            System.err.println("\t\t\t(Wait a few moments and give the showGoose('boss') command.)");
            if (verbose && !connector.getAutoStartBoss())
                ex0.printStackTrace();
        }
        gaggleBoss = connector.getBoss();
    }

    //-------------------------------------------------------------------------------------
    public static RShellGoose getCurrent() {
        return self;
    }

    //-------------------------------------------------------------------------------------
    public String[] getNameList() {
        if (nameList.length == 0) {
            System.out.println("The R goose has not received a namelist broadcast.");
        }
        return nameList;
    }

    //-------------------------------------------------------------------------------------
    public void handleNameList(String source, Namelist nameList) {
        System.out.println("nameList ready, length " + nameList.getNames().length);
        defaultSpecies = nameList.getSpecies();
        this.nameList = nameList.getNames();

    }

    //----------------------------------------------------------------------------------------
    public void handleMatrix(String source, DataMatrix matrix) {
        this.matrix = matrix;
        System.out.println("matrix ready, dimension " +
            this.matrix.getRowCount() + " x " + this.matrix.getColumnCount());
        defaultSpecies = matrix.getSpecies();
    }

    //----------------------------------------------------------------------------------------
    public int getMatrixRowCount() {
        if (matrix == null)
            return 0;
        else
            return matrix.getRowCount();
    }

    //----------------------------------------------------------------------------------------
    public int getMatrixColumnCount() {
        if (matrix == null)
            return 0;
        else
            return matrix.getColumnCount();
    }

    //----------------------------------------------------------------------------------------
    public String[] getMatrixRowNames() {
        if (matrix == null)
            return new String[0];
        else
            return matrix.getRowTitles();
    }

    //----------------------------------------------------------------------------------------
    public String[] getMatrixColumnNames() {
        if (matrix == null)
            return new String[0];
        else
            return matrix.getColumnTitles();
    }

    //----------------------------------------------------------------------------------------
    public double[] getMatrixRow(int rowNumber) {
        if (matrix == null)
            return new double[0];
        else
            return matrix.get(rowNumber);
    }

    //----------------------------------------------------------------------------------------
    public double[] getAllMatrixData() {
        if (matrix == null) {
            System.out.println("The R goose has not received a matrix broadcast.");
            return new double[0];
        }

        int rowCount = matrix.getRowCount();
        int columnCount = matrix.getColumnCount();
        int total = rowCount * columnCount;
        double result[] = new double[total];

        for (int r = 0; r < rowCount; r++) {
            double[] rowValues = matrix.get(r);
            int toPosition = r * columnCount;
            System.arraycopy(rowValues, 0, result, toPosition, columnCount);
        } // for r

        return result;

    } // getAllMatrixData

    //----------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------
    public void test(String name) {
        System.out.println("RShellGoose test (String name): " + name);
    }

    //----------------------------------------------------------------------------------------
    public void test(String[] names) {
        System.out.println("RShellGoose test (String [] name): " + names.length);
    }

    //----------------------------------------------------------------------------------------
    public void test(int i) {
        System.out.println("RShellGoose test (int i): " + i);
    }

    //----------------------------------------------------------------------------------------
    public void test(int[] i) {
        System.out.println("RShellGoose test (int i []): " + i.length);
    }

    //----------------------------------------------------------------------------------------
    public void test(Integer i) {
        System.out.println("RShellGoose test (Integer i): " + i);
    }

    //----------------------------------------------------------------------------------------
    public void test(Integer[] i) {
        System.out.println("RShellGoose test (Integer [] i): " + i.length);
    }

    //----------------------------------------------------------------------------------------
    public void test(double d) {
        System.out.println("RShellGoose test (double d): " + d);
    }

    //----------------------------------------------------------------------------------------
    public void test(Double d) {
        System.out.println("RShellGoose test (Double d: " + d);
    }

    //----------------------------------------------------------------------------------------
    public void test(double[] numbers) {
        System.out.println("RShellGoose test (double [] numbers): " + numbers.length);
    }

    //----------------------------------------------------------------------------------------
    public void test(Double[] numbers) {
        System.out.println("RShellGoose test (Double [] numbers): " + numbers.length);
    }

    public void testObject(Object obj) {
        System.out.println("RShellGoose test: " + obj.getClass().getName());
    }

    public HashMap convertGaggleTupleToHashMap(GaggleTuple gaggleTuple) {
        return null;
    }

    public GaggleTuple convertHashMapToGaggleTuple(HashMap map) {
        return null;
    }





    public void createAndBroadcastDoubleAttributes(String attributeName, String[] names, double[] values) {
        Double[] doubles = new Double[values.length];
        for (int i = 0; i <= values.length; i++) {
            doubles[i] = values[i];
        }
        GaggleTuple gt = createGaggleTupleForBroadcast(attributeName, names, doubles);
        broadcastAssociativeArray(gt);
    }

    //public void createAndBroadcastGaggleTuple(String title,
    //                                          String[] allNames, String allValues) {
     //   System.out.println("in wrong method");
    //}


    public void createAndBroadcastGaggleTuple(String title, String attributeName,
                                              String allNames, String[] allValues) {
        String[] allNamesArray = new String[1];
        allNamesArray[0] = allNames;
        createAndBroadcastGaggleTuple(title, attributeName, allNamesArray, allValues);
    }

    public void createAndBroadcastGaggleTuple(String title, String attributeName,
                                              String[] allNames, String allValues) {
        String[] allValuesArray = new String[1];
        allValuesArray[0] = allValues;
        createAndBroadcastGaggleTuple(title, attributeName, allNames, allValuesArray);
    }


/*
    public void createAndBroadcastGaggleTuple(String title, String attributeName,
                                              String[] allNames, double allValues) {
        String[] allValuesArray = new String[1];
        allValuesArray[0] = new String("hoser");
        createAndBroadcastGaggleTuple(title, attributeName, allNames, allValuesArray);
    }
*/


    public void createAndBroadcastGaggleTuple(String title, String attributeName,
                                              String allNames, String allValues) {
        String[] allNamesArray = new String[1];
        allNamesArray[0] = allNames;
        String[] allValuesArray = new String[1];
        allValuesArray[0] = allValues;
        //System.out.println("allValuesArray.length = " + allValuesArray.length);
        createAndBroadcastGaggleTuple(title, attributeName, allNamesArray, allValuesArray);
    }




    protected Class getValueType(String[] allValues) {
        /*
        if there is at least one double (and no strings) return double
        if there is a single string return string
        if everything can be made into an int return int
         */
        boolean foundDouble = false;

        for (String value : allValues) {
            try {
                Integer.valueOf(value);
            } catch (NumberFormatException e) {
                try {
                    Double.valueOf(value);
                    foundDouble = true;
                } catch (NumberFormatException e1) {
                    return String.class;
                }
            }
        }
        if (foundDouble) {
            return Double.class;
        }
        return Integer.class;
    }

    /**
     * Broadcasts a Cytoscape movie style tuple which assigns the value of a
     * single attribute to the nodes listed in allNames. [is this correct? -jcb]
     * @param title
     * @param attributeName
     * @param allNames
     * @param allValues
     */
    public void createAndBroadcastGaggleTuple(String title, String attributeName,
                                              String[] allNames, String[] allValues) {

        Class valueClass = getValueType(allValues);
        GaggleTuple gaggleTuple = new GaggleTuple();
        gaggleTuple.setName(title);
        Tuple metadata = new Tuple();
        metadata.addSingle(new Single(title));
        gaggleTuple.setMetadata(metadata);
        Tuple dataTuple = new Tuple();
        dataTuple.setName(title);
        gaggleTuple.setSpecies(defaultSpecies);
        for (int i = 0; i < allNames.length; i++) {
            Serializable value;

            if (valueClass.equals(Integer.class)) {
                value = Integer.valueOf(allValues[i]);
            } else if (valueClass.equals(Double.class)) {
                value = Double.valueOf(allValues[i]);
            } else { // string
                String s = allValues[i];
                if (s.startsWith("|") && s.endsWith("|")) {
                    value = s.substring(1, s.length()-1);
                } else {
                    value = allValues[i];
                }

            }

            Tuple row = new Tuple();
            row.addSingle(new Single(allNames[i]));
            row.addSingle(new Single(attributeName));
            row.addSingle(new Single(value));
            dataTuple.addSingle(new Single(row));
        }
        gaggleTuple.setData(dataTuple);
        broadcastGaggleTuple(gaggleTuple);
    }

    public void broadcastGaggleTuple(GaggleTuple gaggleTuple) {
        gaggleTuple.setSpecies(defaultSpecies);
        try {
            gaggleBoss.broadcastTuple(myGaggleName, targetGoose, gaggleTuple);
        } catch (RemoteException e) {
            System.err.println("error broadcasting network from RShellGoose " + myGaggleName);
            e.printStackTrace();
        }
    }

	/**
	 * Wrap a tuple in a GaggleTuple and broadcast it
	 */
    public void broadcastTuple(Tuple tuple) {
        GaggleTuple gaggleTuple = new GaggleTuple();
        gaggleTuple.setName(tuple.getName());
        Tuple metadata = new Tuple();
        gaggleTuple.setMetadata(metadata);
        gaggleTuple.setData(tuple);
        broadcastGaggleTuple(gaggleTuple);
    }

    protected GaggleTuple createGaggleTupleForBroadcast(String tupleName, String[] names, Serializable[] values) {
        Tuple tuple = new Tuple();
        for (int i = 0; i < names.length; i++) {
            tuple.addSingle(new Single(names[i], values[i]));
        }
        GaggleTuple gaggleTuple = new GaggleTuple();
        gaggleTuple.setName(tupleName);
        gaggleTuple.setData(tuple);
        gaggleTuple.setSpecies(defaultSpecies);
        return gaggleTuple;
    }

    public GaggleTuple getGaggleTuple() {
        if (gaggleTuple == null) {
            System.out.println("The R Goose has not received a Tuple broadcast.");
            return null;
        }
        return gaggleTuple;
    }

    public Tuple getTuple() {
        if (gaggleTuple == null) {
            System.out.println("The R Goose has not received a Tuple broadcast.");
            return null;
        }
        return gaggleTuple.getData();
    }

    public String getTupleAttributeName() {
        if (gaggleTuple == null) {
            System.out.println("The R Goose has not received a Tuple broadcast.");
            return "";
        }
        return (String)gaggleTuple.getMetadata().getSingleAt(0).getValue();
    }


    public String getTupleTitle() {
        if (gaggleTuple == null) {
            System.out.println("The R Goose has not received a Tuple broadcast.");
            return "";
        }
        return gaggleTuple.getName();
    }

    public String[] getTupleKeys() {
        if (gaggleTuple == null) {
            System.out.println("The R Goose has not received a Tuple broadcast.");
            return new String[0];
        }
        List<String> keys = new ArrayList<String>();

        for (int i = 0; i < gaggleTuple.getData().getSingleList().size(); i++) {
            Tuple tuple = (Tuple)gaggleTuple.getData().getSingleAt(i).getValue();
            keys.add((String)tuple.getSingleAt(0).getValue());
        }
        return keys.toArray(new String[0]);
    }

    public String[] getTupleValues() {
        if (gaggleTuple == null) {
            System.out.println("The R Goose has not received a Tuple broadcast.");
            return new String[0];            
        }
        List<String> values = new ArrayList<String>();
        for (int i = 0; i < gaggleTuple.getData().getSingleList().size(); i++) {
            Tuple tuple = (Tuple)gaggleTuple.getData().getSingleAt(i).getValue();
            String str = (String)tuple.getSingleAt(2).getValue();
            values.add(str);
        }
        return values.toArray(new String[0]);
    }

    /**
     * Returns the type of the given object. Used to inquire about the type of
     * Tuple elements, which can be:
     *	   String, Integer, Long, Float, Double, Boolean, GaggleData, or Tuple
     */
    public String typeOf(Object object) {
    	if (object==null) {
    		return "null";
    	}
    	if (object instanceof Tuple) {
    		return "Tuple";
    	}
    	if (object instanceof GaggleData) {
    		return "GaggleData";
    	}
    	return object.getClass().getSimpleName();
    }


    //----------------------------------------------------------------------------------------

    public void createAndBroadcastIntegerAttributes(String attributeName, String[] names, int[] values) {
        Integer[] integers = new Integer[values.length];
        for (int i = 0; i <= values.length; i++) {
            integers[i] = values[i];
        }
        broadcastAssociativeArray(createGaggleTupleForBroadcast(attributeName, names, integers));
    }

    //----------------------------------------------------------------------------------------
    public void createAndBroadcastStringAtrributes(String attributeName, String[] names, String[] values) {
        broadcastAssociativeArray(createGaggleTupleForBroadcast(attributeName, names, values));
    }

    //----------------------------------------------------------------------------------------
    public void createAndBroadcastHashMap(String title, String attributeName, String name, String value) {
        // todo tuple fix
        String[] names = new String[1];
        names[0] = name;

        String[] values = new String[1];
        values[0] = value;

        createAndBroadcastHashMap(title, attributeName, names, values);
    }

    //----------------------------------------------------------------------------------------
    public void createAndBroadcastHashMap(String title, String attributeName, String name, double value) {
        // todo tuple fix
        String[] names = new String[1];
        names[0] = name;

        double[] values = new double[1];
        values[0] = value;

        createAndBroadcastHashMap(title, attributeName, names, values);
    }

    //----------------------------------------------------------------------------------------
    public void createAndBroadcastHashMap(String title, String attributeName, String[] names,
                                          String[] values) {
        try {
            gaggleBoss.broadcastTuple(myGaggleName, targetGoose, createGaggleTupleForBroadcast(title, names, values));
        }
        catch (RemoteException rex) {
            System.err.println("error broadcasting hashmap from RShellGoose " + myGaggleName);
            rex.printStackTrace();
        }

    } // createAndBroadcastHashMap (String [] values)

    //----------------------------------------------------------------------------------------
    // todo update method names (make sure calls in R side match)
    public void createAndBroadcastHashMap(String title, String attributeName, String[] names,
                                          double[] values) {

        GaggleTuple gaggleTuple = new GaggleTuple();
        gaggleTuple.setName(title);
        Tuple tuple = new Tuple(title);
        for (int i = 0; i <= names.length; i++) {
            tuple.addSingle(new Single(names[i], values[i]));
        }
        gaggleTuple.setData(tuple);
        gaggleTuple.setSpecies(defaultSpecies);

        try {
            gaggleBoss.broadcastTuple(myGaggleName, targetGoose, gaggleTuple);
        }
        catch (RemoteException rex) {
            System.err.println("error broadcasting hashmap from RShellGoose " + myGaggleName);
            rex.printStackTrace();
        }

    } // createAndBroadcastHashMap (double [] values)



    // special overrides if colnames or rownames has only one member, in which case R treats it as a string

    public void createAndBroadcastMatrix(String[] rowNames, String columnNames, double[] data, String matrixName) {
        String[] columnNamesArray = new String[1];
        columnNamesArray[0] = columnNames;
        createAndBroadcastMatrix(rowNames, columnNamesArray, data, matrixName);
    }


    public void createAndBroadcastMatrix(String rowNames, String columnNames[], double[] data, String matrixName) {
        String[] rowNamesArray = new String[1];
        rowNamesArray[0] = rowNames;
        createAndBroadcastMatrix(rowNamesArray, columnNames, data, matrixName);
    }


    public void createAndBroadcastMatrix(String rowNames, String columnNames, double[] data, String matrixName) {
        String[] rowNamesArray = new String[1];
        rowNamesArray[0] = rowNames;
        String[] columnNamesArray = new String[1];
        columnNamesArray[0] = columnNames;
        createAndBroadcastMatrix(rowNamesArray, columnNames, data, matrixName);
    }


    public void createAndBroadcastMatrix(String rowNames, String columnNames, double data, String matrixName) {
        String[] rowNamesArray = new String[1];
        rowNamesArray[0] = rowNames;
        String[] columnNamesArray = new String[1];
        columnNamesArray[0] = columnNames;
        double[] dataArray = new double[1];
        dataArray[0] = data;
        createAndBroadcastMatrix(rowNamesArray, columnNames, dataArray, matrixName);
    }



    //----------------------------------------------------------------------------------------
    public void createAndBroadcastMatrix(String[] rowNames, String[] columnNames, double[] data,
                                         String matrixName) {
        //System.out.println (" ------- RShellGoose.cabm");
        //System.out.println ("   row name count: " + rowNames.length);
        //System.out.println ("   col name count: " + columnNames.length);
        //System.out.println ("      data length: " + data.length);
        //System.out.println ("             name: " + matrixName);
        DataMatrix matrix = new DataMatrix();

        int rowCount = rowNames.length;
        int columnCount = columnNames.length;
        matrix.setSize(rowCount, columnCount);
        if (matrixName != null)  {
            matrix.setShortName(matrixName); //
            matrix.setName(matrixName);
        }

        matrix.setRowTitles(rowNames);
        matrix.setColumnTitles(columnNames);

        for (int r = 0; r < rowCount; r++) {
            double[] rowValues = new double[columnCount];
            int fromPosition = r * columnCount;
            System.arraycopy(data, fromPosition, rowValues, 0, columnCount);
            matrix.set(r, rowValues);
        } // for r

        broadcastMatrix(matrix);

    } // createAndBroadcastMatrix

    //----------------------------------------------------------------------------------------
/**
 * create a 1-d String array from a network, for easy transmission to R.
 * each element in the array is a string, with format
 * sourceNode::targetNode::edgeType
 * @param network the network to convert to a string
 * @return an array of strings representing a network
 */
    protected String[] networkToStringArray(Network network) {
        Interaction[] interactions = network.getInteractions();
        ArrayList<String> list = new ArrayList<String>();

        //System.out.println ("networkToStringArray, interaction ount: " + interactions.length);
        for (Interaction interaction : interactions) {
            String source = interaction.getSource();
            String target = interaction.getTarget();
            String type = interaction.getType();
            String combined = source + "::" + target + "::" + type;
            //System.out.println ("   interaction: " + combined);
            list.add(combined);
        } // for i

        String[] orphanNodes = network.getOrphanNodes();
        //System.out.println ("networkToStringArray, orphanCount: " + orphanNodes.length);
        for (String orphanNode : orphanNodes) {
            //System.out.println ("    orphan: " + orphanNodes [i]);
            list.add(orphanNode);
        }

        return list.toArray(new String[0]);

    } // networkToStringArray

    //----------------------------------------------------------------------------------------
/**
 * create a 1-d String array from a network, for easy transmission to R.
 * triples (source, edgeType, target) are filled in to an array of length 3 * # of interactions
 * @param network a gaggle network
 * @return the edge attributes of the network as a string array
 */
    protected String[] networkEdgeAttributesToStringArray(Network network) {
        Interaction[] interactions = network.getInteractions();
        String source, target, type;
        String[] attributeNames = network.getEdgeAttributeNames();
        ArrayList<String> list = new ArrayList<String>();

        for (Interaction interaction : interactions) {
            source = interaction.getSource();
            type = interaction.getType();
            target = interaction.getTarget();
            String edgeName = source + " (" + type + ") " + target;
            String terseEdgeName = source + "::" + target + "::" + type;
            for (String name : attributeNames) {
                HashMap hash = network.getEdgeAttributes(name);
                if (hash.containsKey(edgeName)) {
                    Object value = hash.get(edgeName);
                    StringBuffer sb = new StringBuffer();
                    sb.append(terseEdgeName);
                    sb.append("::");
                    sb.append(name);
                    sb.append("::");
                    sb.append(value.toString());
                    list.add(sb.toString());
                } else {
                    System.out.println("no " + name + " attribute for " + edgeName);
                }
            } // for a
        } // for r

        return list.toArray(new String[0]);

    } // networkEdgeAttributesToStringArray

    //----------------------------------------------------------------------------------------
/**
 * create a 1-d String array from a network's node attributes, for easy transmission to R.
 * use this format for each attribute of each node:
 * nodeName::attributeName::value
 * @param network a gaggle network
 * @return the network's node attributes as a string array
 */
    protected String[] networkNodeAttributesToStringArray(Network network) {
        String[] attributeNames = network.getNodeAttributeNames();
        //System.out.println (" nnatsa, attribute name count: " + attributeNames.length);
        ArrayList<String> list = new ArrayList<String>();

        for (String attributeName : attributeNames) {
            //System.out.println (" nnatsa, attribute name: " + attributeName);
            HashMap attributeHash = network.getNodeAttributes(attributeName);
            // can't remove this warning until Network is genericized in next api release:
            String[] nodeNames = (String[]) attributeHash.keySet().toArray(new String[0]);
            for (String nodeName : nodeNames) {
                //System.out.println (" nnatsa, node name: " + nodeName);
                Object value = attributeHash.get(nodeName);
                String terseForm = nodeName + "::" + attributeName + "::" + value.toString();
                list.add(terseForm);
            } // for n
        } // for i

        return list.toArray(new String[0]);

    } // networkEdgeAttributesToStringArray

    //----------------------------------------------------------------------------------------
    public String[] getNetworkAsStringArray() {
        String[] result = new String[0];
        if (network != null) {
            result = networkToStringArray(network);
        } else {
            System.out.println("The R goose has not received a network broadcast.");
        }

        return result;

    } // getNetworkAsStringArray

    //----------------------------------------------------------------------------------------
    public String[] getNetworkEdgeAttributesAsStringArray() {
        String[] result = new String[0];

        if (network != null)
            result = networkEdgeAttributesToStringArray(network);

        return result;

    } // getNetworkEdgeAttributesAsStringArray

    //----------------------------------------------------------------------------------------
    public String[] getNetworkNodeAttributesAsStringArray() {
        String[] result = new String[0];

        if (network != null) {
            //System.out.println (" ----- calling nnatsa ------ ");
            result = networkNodeAttributesToStringArray(network);
        }

        return result;

    } // getNetworkNodeAttributesAsStringArray

    //----------------------------------------------------------------------------------------
/**
 * a network is a collection of (possibly degenerate) interactions.
 * the R client should pass 3 arrays of strings, having the following structure
 * <p/>
 * edges consist of strings like  "VNG0723G::VNG1233G::PhylogeneticProfile"
 * node attributes: "VNG1233G::commonName::pepq2"
 * edge attributes: "VNG0723G::VNG1233G::PhylogeneticProfile::confidence::0.533"
 */
    public void createAndBroadcastNetwork(String[] interactionStrings,
                                          String[] nodeAttributeStrings,
                                          String[] edgeAttributeStrings,
                                          String name)

    {
        Network network = new Network();
        addMetaDataToNetwork(network);
        network.setName(name);
        network.setSpecies(defaultSpecies);

        for (String interactionString : interactionStrings) {
            String[] tokens = interactionString.split("::");
            int tokenCount = tokens.length;
            //for (int t=0; t < tokens.length; t++)
            //  System.out.println ("  edge token " + t + ": " + tokens [t]);
            if (tokenCount == 1 && tokens[0].trim().length() > 0) {
                //System.out.println ("adding one orphan node to network: " + tokens [0]);
                network.add(tokens[0]);
            } else if (tokenCount == 3) {
                String sourceNode = tokens[0];
                String targetNode = tokens[1];
                String interactionType = tokens[2];
                network.add(new Interaction(sourceNode, targetNode, interactionType));
            } // else:  good interaction
        } // for i

        //System.out.println ("nodeAttributeStrings count: " + nodeAttributeStrings.length);
        for (String nodeAttributeString : nodeAttributeStrings) {
            //System.out.println ("   " + nodeAttributeStrings [i]);
            String[] tokens = nodeAttributeString.split("::");
            if (tokens.length == 3) {
                String nodeName = tokens[0].trim();
                String attributeName = tokens[1].trim();
                String rawValue = tokens[2].trim();
                Object value = StringToObjectConverter.convert(rawValue);
                network.addNodeAttribute(nodeName, attributeName, value);
            } // if 3 tokens
        } // for i

        //System.out.println ("edgeAttributeStrings count: " + edgeAttributeStrings.length);
        for (String edgeAttributeString : edgeAttributeStrings) {
            //System.out.println ("   " + edgeAttributeStrings [i]);
            String[] tokens = edgeAttributeString.split("::");
            if (tokens.length == 5) {
                String sourceNode = tokens[0].trim();
                String targetNode = tokens[1].trim();
                String edgeType = tokens[2].trim();
                String attributeName = tokens[3].trim();
                String rawValue = tokens[4].trim();
                Object value = StringToObjectConverter.convert(rawValue);
                String edgeName = sourceNode + " (" + edgeType + ") " + targetNode;
                network.addEdgeAttribute(edgeName, attributeName, value);
            } // if 5 tokens
        } // for i

        //System.out.println ("RShellGoose, about to broadcast network");
        //System.out.println ("     node count: " + network.getNodes().length);
        //System.out.println ("      connected: " + network.getConnectedNodes ().size ());
        //System.out.println ("         orphan: " + network.getOrphanNodes().length);

        try {
            gaggleBoss.broadcastNetwork(myGaggleName, targetGoose, network);
        }
        catch (RemoteException rex) {
            System.err.println("error broadcasting network from RShellGoose " + myGaggleName);
            rex.printStackTrace();
        }

    } // createAndBroadcastNetwork

    //----------------------------------------------------------------------------------------
    // is this called from anywhere? doesn't look like it
    public void createAndBroadcastNetwork(String sourceNode, String[] targetNodes,
                                          double[] weights, String[] targetTypes) {
        //System.out.println ("createAndBroadcastNetwork, source: " + sourceNode);
        Network network = new Network();
        addMetaDataToNetwork(network);
        network.setSpecies(defaultSpecies);

        for (int i = 0; i < targetNodes.length; i++) {
            Interaction interaction = new Interaction(sourceNode, targetNodes[i], targetTypes[i]);
            network.add(interaction);
            String edgeName = sourceNode + " (" + targetTypes[i] + ") " + targetNodes[i];
            //System.out.println ("adding weight " + weights [i] + " for edge " + edgeName);
            network.addEdgeAttribute(edgeName, "weight", weights[i]);
        }

        try {
            gaggleBoss.broadcastNetwork(myGaggleName, targetGoose, network);
        }
        catch (RemoteException rex) {
            System.err.println("error broadcasting network from RShellGoose " + myGaggleName);
            rex.printStackTrace();
        }

    } // createAndBroadcastNetwork


    protected void addMetaDataToNetwork(Network network) {
        if (networkMetadata.size() == 0) {
            return;
        }
        Tuple metadata = new Tuple();
        Set<String> keys = networkMetadata.keySet();
        for (Iterator<String> it = keys.iterator(); it.hasNext();) {
            String key = it.next();
            metadata.addSingle(new Single(key, networkMetadata.get(key)));
        }
        network.setMetadata(metadata);
    }

    //----------------------------------------------------------------------------------------
    public void handleTuple(String source, GaggleTuple gaggleTuple) {
        System.out.println("tuple ready, " + gaggleTuple.getName());
        this.gaggleTuple = gaggleTuple;
        defaultSpecies = gaggleTuple.getSpecies();
    }

    //----------------------------------------------------------------------------------------
    public void handleCluster(String source, Cluster cluster) {
        System.out.println("cluster ready, rowNames " + cluster.getRowNames().length +
                " columnNames " + cluster.getColumnNames().length);
        this.clusterName = cluster.getName();
        this.clusterRowNames = cluster.getRowNames();
        this.clusterColumnNames = cluster.getColumnNames();
        defaultSpecies = cluster.getSpecies();

    }

    //----------------------------------------------------------------------------------------
    public String getClusterName() {
        if (clusterName != null) {
            return clusterName;
        } else {
            System.out.println("The R goose has not received a cluster broadcast.");
            return "";
        }

    }

    //----------------------------------------------------------------------------------------
    public String[] getClusterRowNames() {
        if (clusterRowNames != null)
            return clusterRowNames;
        else
            return new String[0];

    }

    //----------------------------------------------------------------------------------------
    public String[] getClusterColumnNames() {
        if (clusterColumnNames != null)
            return clusterColumnNames;
        else
            return new String[0];

    }

    //----------------------------------------------------------------------------------------
    public void handleNetwork(String source, Network newNetwork) {
        System.out.println("network ready, node count " + newNetwork.getNodes().length +
                ", edges: " + newNetwork.edgeCount());

        network = newNetwork;
        defaultSpecies = newNetwork.getSpecies();
    }

    //----------------------------------------------------------------------------------------
    public void clearSelections() {
    }

    //----------------------------------------------------------------------------------------
    public int getSelectionCount() {
        return 0;
    }

    //----------------------------------------------------------------------------------------

    public String[] getGeeseNames() {
        List<String> results = new ArrayList<String>();
            for (String name : activeGeeseNames) {
                if (!this.myGaggleName.equals(name)) {
                    results.add(name);
                }
            }
        return results.toArray(new String[0]);
    }


    //----------------------------------------------------------------------------------------
    public String getName() {
        return myGaggleName;
    }

    //----------------------------------------------------------------------------------------
    public void setName(String newName) {
        myGaggleName = newName;
    }

    //----------------------------------------------------------------------------------------
    public void setGeometry(int x, int y, int width, int height) {
    }

    //----------------------------------------------------------------------------------------
    public void doBroadcastList() {
    }

    //----------------------------------------------------------------------------------------
    protected void broadcastToGaggle(String species, String[] names) {

    } // broadcastToGaggle

    //------------------------------------------------------------------------------
    public void doHide() {
        System.out.println("doHide not implemented for the R goose");
    }

    //----------------------------------------------------------------------------------------
    public void doShow() {
        System.out.println("doShow not implemented for the R goose");
    }

    //----------------------------------------------------------------------------------------
    public void doExit() {
        connector.disconnectFromGaggle(false);
        System.out.println("No longer connected to the boss.");
        System.out.println("use the connectToGaggle() method to reconnect.");
    }

    //----------------------------------------------------------------------------------------
    void show(String name) {
        try {
            gaggleBoss.show(name);
        }
        catch (RemoteException rex) {
            System.err.println("error rmi at boss.show , from RShellGoose " + name);
        }

    } // show

    //----------------------------------------------------------------------------------------
    void hide(String name) {
        try {
            gaggleBoss.hide(name);
        }
        catch (RemoteException rex) {
            System.err.println("error rmi at boss.hide , from RShellGoose " + name);
        }

    } // hide

    //----------------------------------------------------------------------------------------
    void broadcastAssociativeArray(GaggleTuple gaggleTuple) {
    	broadcastGaggleTuple(gaggleTuple);
    }

    //----------------------------------------------------------------------------------------
    public void setSpecies(String newValue) {
        defaultSpecies = newValue;
    }

    //----------------------------------------------------------------------------------------
    public String getSpecies() {
        return defaultSpecies;
    }

    //----------------------------------------------------------------------------------------
    public void broadcastList(String name, String broadcastName) {
        String[] names = new String[1];
        names[0] = name;
        broadcastList(names, broadcastName);
    }

    public void broadcastList(String[] names, String broadcastName) {
        Namelist gaggleNameList = new Namelist();
        gaggleNameList.setName(broadcastName);
        gaggleNameList.setSpecies(defaultSpecies);
        gaggleNameList.setNames(names);
        try {
            gaggleBoss.broadcastNamelist(myGaggleName, targetGoose, gaggleNameList);
        }
        catch (RemoteException rex) {
            System.err.println("error rmi selecting at boss, from RShellGoose.broadcastList() " + myGaggleName);
        }

    } // broadcastList


    public void broadcastCluster(String clusterName, String rowNames, String[] columnNames) {
        String[] rowNamesArray = new String[1];
        rowNamesArray[0] = rowNames;
        broadcastCluster(clusterName, rowNamesArray, columnNames);
    }

    public void broadcastCluster(String clusterName, String[] rowNames, String columnNames) {
        String[] columnNamesArray = new String[1];
        columnNamesArray[0] = columnNames;
        broadcastCluster(clusterName, rowNames, columnNamesArray);
    }

    public void broadcastCluster(String clusterName, String rowNames, String columnNames) {
        String[] rowNamesArray = new String[1];
        rowNamesArray[0] = rowNames;
        String[] columnNamesArray = new String[1];
        columnNamesArray[0] = columnNames;
        broadcastCluster(clusterName, rowNamesArray, columnNamesArray);
    }





    //----------------------------------------------------------------------------------------
    public void broadcastCluster(String clusterName, String[] rowNames, String[] columnNames) {
        try {
            Cluster cluster = new Cluster(clusterName, getSpecies(), rowNames, columnNames);
            cluster.setSpecies(defaultSpecies);
            gaggleBoss.broadcastCluster(myGaggleName, targetGoose, cluster);
        }
        catch (RemoteException rex) {
            System.err.println("broadcast cluster error, in RShellGoose " + myGaggleName);
        }

    } // broadcastCluster

    //----------------------------------------------------------------------------------------
    void broadcastMatrix(DataMatrix matrix) {
        try {
            matrix.setSpecies(defaultSpecies);
            //matrix.setName();
            gaggleBoss.broadcastMatrix(myGaggleName, targetGoose, matrix);
        }
        catch (RemoteException rex) {
            System.err.println("error rmi selecting at boss, from RShellGoose.broadcastMatrix() " + myGaggleName);
        }

    } // broadcastMatrix

    //----------------------------------------------------------------------------------------
    public void setTargetGoose(String gooseName) {
        targetGoose = gooseName;
    }

    //----------------------------------------------------------------------------------------
    public String getTargetGoose() {
        return targetGoose;
    }

    public void update(String[] gooseNames) {
        activeGeeseNames = gooseNames;
    }


    public void setConnected(boolean connected, Boss boss) {
        this.gaggleBoss = boss;
        // todo do something with connection information
        // need to add R and java code to call connect/disconnect
    }

    public void addNetworkMetadata(String key, String value) {
        networkMetadata.put(key, value);
    }


    //----------------------------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        new RShellGoose();

    } // main
//-------------------------------------------------------------------------------------
} // RShellGoose
