// DataMatrix.java
//--------------------------------------------------------------------
// $Revision$
// $Date$
// $Author$
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

import java.util.*;
import java.io.*;


public class DataMatrix implements GaggleData {
    protected String name;
    protected Tuple metadata;
    protected String rowTitlesTitle = "DataMatrix";
    protected String[] columnTitles = new String[0];
    protected String[] rowTitles = new String[0];
    protected double[][] data = null;
    protected String uri = "";
    protected String fullName = "";
    protected String shortName = "";
    protected String fileExtension;
    protected String dataTypeBriefName;
    protected String species = "unknown";

    public DataMatrix() {
        this("");
    }

    public DataMatrix(String uri) {
        this.uri = uri.trim();
        fullName = uri;
        shortName = calculateShortName();
        fileExtension = calculateFileExtension();
        dataTypeBriefName = fileExtension;
    }


    public void setShortName(String newValue) {
        shortName = newValue;
    }

    public String getShortName() {
        return shortName;
    }

    public void setSpecies(String newValue) {
        species = newValue;
    }

    public String getSpecies() {
        return species;
    }

    protected String calculateShortName() {
        String[] tokens = fullName.split("/");
        int lastToken = tokens.length - 1;
        if (lastToken < 0)
            return fullName;
        else
            return tokens[lastToken];
    }

    protected String calculateFileExtension() {
        String shortName = getShortName();
        int lastDot = shortName.lastIndexOf(".");
        if (lastDot < 0)
            return "";
        else
            return shortName.substring(lastDot + 1);
    }

    public String getFileExtension() {
        return fileExtension;
    }

/**
 * the 'data type brief name' is often the file extension of the
 * uri from which the data has been read, eg, 'ratio' or 'lambda'.
 * indeed, that file extension is the value of this variable by default.
 * but it may be reset here.
 *
 * @return brief name of data type
 */
    public String getDataTypeBriefName() {
        return dataTypeBriefName;
    }

    public void setDataTypeBriefName(String newValue) {
        dataTypeBriefName = newValue;
    }

    public void setFullName(String newValue) {
        fullName = newValue;
        shortName = calculateShortName();
        fileExtension = calculateFileExtension();
    }

    public String getFullName() {
        return fullName;
    }

    public void setSize(int rows, int columns) {
        data = new double[rows][columns];

    }

    public void setDefault(double value) {
        if (data == null)
            return;

        for (int r = 0; r < data.length; r++)
            for (int c = 0; c < data[0].length; c++)
                data[r][c] = value;
    }

    public void set(int row, int column, double value) {
        data[row][column] = value;
    }

    public void set(int row, double[] values) {
        data[row] = values;
    }

    public void addRow(String rowName, double[] values) throws IllegalArgumentException {
        if (values.length != getColumnCount())
            throw new IllegalArgumentException("new row must have only " +
                    getColumnCount() + " values; you supplied " +
                    values.length);

        int newCount = getRowCount() + 1;
        double[][] newData = new double[newCount][getColumnCount()];

        System.arraycopy(data, 0, newData, 0, getRowCount());

        newData[newCount - 1] = values;
        data = newData;

        ArrayList<String> tmp = new ArrayList<String>(Arrays.asList(rowTitles));
        tmp.add(rowName);

        rowTitles = tmp.toArray(new String[0]);

    }

    public void setColumnTitles(String[] newValues) {
        columnTitles = newValues;
    }

    public void setRowTitles(String[] newValues) {
        rowTitles = newValues;
    }

    public void setRowTitlesTitle(String newValue) {
        rowTitlesTitle = newValue;
    }

    public int getRowCount() {
        if (data != null)
            return data.length;
        else
            return 0;
    }

    public int getColumnCount() {
        if (columnTitles != null)
            return columnTitles.length;
        else
            return 0;

    }

    public double get(int row, int column) {
        return data[row][column];
    }

    public double[] get(int row) {
        if (data != null)
            return data[row];
        else
            return new double[]{};
    }

    public double[] get(String rowName) {
        for (int i = 0; i < rowTitles.length; i++)
            if (rowTitles[i].equals(rowName))
                return data[i];

        throw new IllegalArgumentException("no data for '" + rowName + "'");
    }

    public String[] getRowTitles() {
        return rowTitles;
    }

    public void set(double[][] d) {
        data = d;
    }

    public double[][] get() {
        return data;
    }

    public int getColumnNumber(String columnName) {
        for (int c = 0; c < getColumnCount(); c++) {
            if (columnTitles[c].equals(columnName))
                return c;
        }
        throw new IllegalArgumentException("no column named " + columnName);

    }

    public int getRowNumber(String rowName) {
        for (int r = 0; r < getRowCount(); r++) {
            if (rowTitles[r].equals(rowName))
                return r;
        }

        throw new IllegalArgumentException("no row named " + rowName);

    }

    public double[] getColumn(String columnName) {
        int columnNumber = getColumnNumber(columnName);
        double[] result = new double[getRowCount()];
        for (int r = 0; r < getRowCount(); r++)
            result[r] = data[r][columnNumber];

        return result;
    }

    public double[] getColumn(int columnNumber) {
        double[] result = new double[getRowCount()];
        for (int r = 0; r < getRowCount(); r++)
            result[r] = data[r][columnNumber];

        return result;

    }

    public String getRowTitlesTitle() {
        return rowTitlesTitle;
    }

    public String[] getColumnTitles() {
        return columnTitles;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        int colMax = columnTitles.length;
        String[] columnTitles = getColumnTitles();

        sb.append(rowTitlesTitle);
        for (int c = 0; c < colMax; c++) {
            sb.append("\t");
            sb.append(columnTitles[c]);
        }
        sb.append("\n");

        int rowMax = rowTitles.length;
        for (int r = 0; r < rowMax; r++) {
            double[] adjustedRow = get(r);
            sb.append(rowTitles[r]);
            for (int c = 0; c < colMax; c++) {
                sb.append("\t");
                sb.append(adjustedRow[c]);
            }
            sb.append("\n");
        }

        return sb.toString();

    }

    public void sortByRowName() {
        double[][] sortedData = new double[getRowCount()][getColumnCount()];
        Map<String, Integer> oldMap = new HashMap<String, Integer>();
        Map<String, Integer> newMap = new HashMap<String, Integer>();

        Map<Integer, Integer> indexMap = new HashMap<Integer, Integer>();

        boolean foundDuplicates = false;

        for (int i = 0; i < rowTitles.length; i++) {
             if (oldMap.put(rowTitles[i], i) != null) {
                 System.out.println("Duplicate value: " + rowTitles[i]);
                 foundDuplicates = true;
             }
        }

        if (foundDuplicates) {
            throw new IllegalArgumentException("Encountered duplicate row names in matrix!");
        }

        Arrays.sort(rowTitles);

        for (int i = 0; i < rowTitles.length; i++) {
            newMap.put(rowTitles[i], i);
        }

        for (String rowName : rowTitles) {
            indexMap.put(oldMap.get(rowName), newMap.get(rowName));
        }

        for (int i = 0; i < data.length; i++) {
            sortedData[indexMap.get(i)] = data[i];
        }

        data = sortedData;
    }

    public boolean equals(DataMatrix other) {
        return (toString().equals(other.toString()));
    }

    public void writeObject(String uri) throws IOException {
        FileOutputStream fstream = new FileOutputStream(uri);
        ObjectOutputStream ostream = new ObjectOutputStream(fstream);
        ostream.writeObject(this);
        ostream.close();
    }

    static public DataMatrix readObject(String uri) throws
            IOException, ClassNotFoundException {
        FileInputStream fstream = new FileInputStream(uri);
        ObjectInputStream ostream = new ObjectInputStream(fstream);
        return (DataMatrix) ostream.readObject();

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

}
