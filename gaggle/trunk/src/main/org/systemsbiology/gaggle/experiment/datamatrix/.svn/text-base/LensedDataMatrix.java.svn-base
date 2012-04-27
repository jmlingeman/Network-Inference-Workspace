// LensedDataMatrix
//-----------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.datamatrix;
//-----------------------------------------------------------------------------------------

import java.util.*;
import java.io.Serializable;

import org.systemsbiology.gaggle.core.datatypes.DataMatrix;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;

//-----------------------------------------------------------------------------------------
public class LensedDataMatrix extends DataMatrix implements Serializable {

    int[] columnOrder;         // tracks the user's preferred column order
    boolean[] columnState;     // tracks which columns in each table are enabled

    boolean[] rowState;        // if no rows are explicitly selected, then (by convention)
    // -all- rows are considered to be selected -- that is,
    // enabled, and returned in response to any query

    int[] rowMap;              // derived from rowState, this maps requested (enabled) row number --
    // to an absolute row number

    boolean noExplicitRowSelection = true;
    DataMatrix underlyingMatrix;

    Hashtable nameMap = new Hashtable();


    //-----------------------------------------------------------------------------------------
    public LensedDataMatrix(DataMatrix matrix) throws Exception {
        underlyingMatrix = matrix;
        rowTitlesTitle = matrix.getRowTitlesTitle();
        columnTitles = matrix.getColumnTitles();
        rowTitles = matrix.getRowTitles();
        data = matrix.get();
        uri = matrix.getFullName();
        fullName = matrix.getFullName();
        shortName = matrix.getShortName();
        fileExtension = matrix.getFileExtension();
        dataTypeBriefName = matrix.getDataTypeBriefName();
        species = matrix.getSpecies();

        for (int i = 0; i < rowTitles.length; i++)
            nameMap.put(rowTitles[i], new Integer(i));

        init();

    } // ctor

    //--------------------------------------------------------------------
    public org.systemsbiology.gaggle.core.datatypes.DataMatrix getUnderlyingMatrix() {
        return underlyingMatrix;
    }

    //--------------------------------------------------------------------
    private void init() {
        clear();

    }

    //--------------------------------------------------------------------
/**
 * restore the lens to its initial state, in which no columns are
 * swapped, and all columns are enabled; this view is equivalent
 * to the underlying matrix
 */
    public void clear() {
        int columnCount = columnTitles.length;
        columnState = new boolean[columnCount];
        columnOrder = new int[columnCount];

        for (int c = 0; c < columnCount; c++) {
            columnState[c] = true;
            columnOrder[c] = c;
        }

        rowState = new boolean[rowTitles.length];
        setRowState(false);

        noExplicitRowSelection = true;

    } // init

    //----------------------------------------------------------------------------------------
    public void setColumnState(boolean newState) {
        for (int c = 0; c < columnState.length; c++)
            columnState[c] = newState;
    }

    //----------------------------------------------------------------------------------------
    public void setColumnState(int[] columns, boolean newState) {
        for (int c = 0; c < columns.length; c++)
            columnState[columns[c]] = newState;
    }

    //----------------------------------------------------------------------------------------
    public void setColumnState(int column, boolean newState) {
        columnState[column] = newState;
    }

    //----------------------------------------------------------------------------------------
    public boolean[] getColumnState() {
        return columnState;

    }

    //----------------------------------------------------------------------------------------
    public boolean getColumnState(int column) {
        return columnState[column];
    }

    //----------------------------------------------------------------------------------------
    public void setRowState(boolean[] newState) {
        rowState = newState;
        updateRowMap();
    }

    //----------------------------------------------------------------------------------------
    public void setRowState(boolean newState) {
        for (int r = 0; r < underlyingMatrix.getRowCount(); r++)
            setRowStateImpl(r, newState);

        updateRowMap();
    }

    //----------------------------------------------------------------------------------------
    public void setRowState(int[] rows, boolean newState) {
        for (int r = 0; r < rows.length; r++)
            setRowStateImpl(rows[r], newState);

        updateRowMap();

    }

    //----------------------------------------------------------------------------------------
    public void setRowState(int row, boolean newState) {
        setRowStateImpl(row, newState);
        updateRowMap();
    }

    //----------------------------------------------------------------------------------------
/**
 * everyone calling this will be responsible for also calling updateRowMap.
 * this 'impl' (actual implementation) method for assigning an individual row's state
 * should be the only place single-element rowState assignments are actually made.
 * <p/>
 * this strategy allows setRowState (int [] rows, boolean newState) to avoid the multiple
 * calls to 'updateRowMap' which the public method
 * setRowState (int row, boolean newState)
 * must, perforce, itself call.
 */
    private void setRowStateImpl(int row, boolean newState) {
        rowState[row] = newState;
    }

    //----------------------------------------------------------------------------------------
    public void enableRow(int row) {
        setRowStateImpl(row, true);
        updateRowMap();
    }

    //----------------------------------------------------------------------------------------
    public void enableRows(int[] rows) {
        //System.out.println ("LDM.enableRows (" + rows.length + ")");
        setRowState(rows, true);
        //System.out.println ("LDM.enableRows 2");
        //System.out.println ("LDM.enableRows 3");
    }

    //----------------------------------------------------------------------------------------
    public void enableAllRows() {
        for (int i = 0; i < rowState.length; i++)
            setRowStateImpl(i, true);
        updateRowMap();
    }

    //----------------------------------------------------------------------------------------
    public void disableRow(int row) {
        setRowStateImpl(row, false);
        updateRowMap();
    }

    //----------------------------------------------------------------------------------------
    public void disableRows(int[] rows) {
        setRowState(rows, false);
    }

    //----------------------------------------------------------------------------------------
    public void disableAllRows() {
        //System.out.println ("LDM.disableAllRows");
        //for (int i=0; i < rowState.length; i++)
        //  setRowState (i,  false);
        Arrays.fill(rowState, false);
        updateRowMap();
    }

    //----------------------------------------------------------------------------------------
    private void updateRowMap() {
        rowMap = new int[rowState.length];
        Arrays.fill(rowMap, -1);

        int enabledRowCount = 0;
        for (int r = 0; r < rowState.length; r++)
            if (rowState[r])
                rowMap[enabledRowCount++] = r;


    } // oldUpdateRowMap

    //----------------------------------------------------------------------------------------
/**
 * ******
 * private void oldUpdateRowMap ()
 * {
 * ArrayList list = new ArrayList ();
 * <p/>
 * for (int r=0; r < rowState.length; r++) {
 * if (rowState [r])
 * list.add (new Integer (r));
 * } // for r
 * <p/>
 * rowMap = (Integer []) list.toArray (new Integer [0]);
 * <p/>
 * } // oldUpdateRowMap
 * ********
 */
//----------------------------------------------------------------------------------------
    public void enableColumn(int column) {
        setColumnState(column, true);
    }

    //----------------------------------------------------------------------------------------
    public void enableColumns(int[] columns) {
        setColumnState(columns, true);
    }

    //----------------------------------------------------------------------------------------
    public void enableAllColumns() {
        for (int i = 0; i < columnState.length; i++)
            setColumnState(i, true);
    }

    //----------------------------------------------------------------------------------------
    public void disableColumn(int column) {
        setColumnState(column, false);
    }

    //----------------------------------------------------------------------------------------
    public void disableColumns(int[] columns) {
        setColumnState(columns, false);
    }

    //----------------------------------------------------------------------------------------
    public void disableAllColumns() {
        for (int i = 0; i < columnState.length; i++)
            setColumnState(i, false);
    }

    //----------------------------------------------------------------------------------------
    public boolean[] getRowState() {
        return rowState;
    }

    //----------------------------------------------------------------------------------------
    public boolean getRowState(int row) {
        return rowState[row];
    }

    //----------------------------------------------------------------------------------------
    public int getRowCount() {
        int result = 0;
        for (int r = 0; r < rowState.length; r++)
            if (rowState[r])
                result++;

        return result;

    } // getRowCount

    //----------------------------------------------------------------------------------------
    public int getColumnCount() {
        int result = 0;

        for (int i = 0; i < columnState.length; i++)
            if (columnState[i])
                result++;

        return result;

    }
//----------------------------------------------------------------------------------------

    /**
     * Moves the column in position <code>from</code> to position <code>to</code>,
     * shifting some columns rightward if necessary.
     */
    public void changeColumnPosition(int from, int to) {
        Vector v = new Vector();
        for (int i = 0; i < columnOrder.length; i++) {
            v.add(new Integer(columnOrder[i]));
        }
        Integer move = (Integer) v.remove(from);
        v.add(to, move);

        for (int i = 0; i < columnOrder.length; i++) {
            columnOrder[i] = ((Integer) v.get(i)).intValue();
        }
    }


    public void changeColumnPositions(String[] newColumnNames) {
        for (int i = 0; i < columnTitles.length; i++) {
            changeColumnPosition(i, getOriginalColumnPosition(newColumnNames[i]));
        }
    }

    private int getOriginalColumnPosition(String columnName) {
        for (int i = 0; i < underlyingMatrix.getColumnTitles().length; i++) {
            if (underlyingMatrix.getColumnTitles()[i].equals(columnName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("no such column: '" + columnName + "'");

    }

    //----------------------------------------------------------------------------------------
    public int getColumnOrder(int column) {
        return columnOrder[column];
    }

    //----------------------------------------------------------------------------------------
    public String[] getColumnTitles() {
        return adjustTitlesForColumnOrderAndState(columnTitles); // super.getColumnTitles ());
    }

    //----------------------------------------------------------------------------------------
    public String[] getRowTitles() {
        String[] allRowTitles = underlyingMatrix.getRowTitles();
        ArrayList result = new ArrayList();
        for (int r = 0; r < rowState.length; r++)
            if (rowState[r])
                result.add(allRowTitles[r]);

        return (String[]) result.toArray(new String[0]);

    }

    //----------------------------------------------------------------------------------------
    public int adjustRowIndexForRowState(int requestedRow) {
        if (requestedRow >= rowMap.length)
            throw new IllegalArgumentException("requestedRow " + requestedRow +
                    " is out of range.  visible row count: " +
                    rowMap.length);

        return rowMap[requestedRow];


    } // adjustRowIndexForRowState

    //----------------------------------------------------------------------------------------
    public int oldAdjustRowIndexForRowState(int requestedRow) {
        //assert (requestedRow < getRowCount ());

        int visibleRowCount = -1;
        for (int r = 0; r < rowState.length; r++) {
            if (rowState[r]) {
                visibleRowCount++;
                if (requestedRow == visibleRowCount)
                    return r;
            } // if r
        } // for r

        throw new IllegalArgumentException("requestedRow " + requestedRow +
                " is out of range.  visible row count: " +
                getRowCount());

    } // adjustRowIndexForRowState

    //----------------------------------------------------------------------------------------
    public DataMatrix getVisibleMatrix() {
        // if no rows are selected, we return the whole underlying matrix:
        DataMatrix matrix = new DataMatrix(this.getFullName());

        matrix.setDataTypeBriefName(this.getDataTypeBriefName());
        //for (MetaData metaData : this.getMetaData()) {
        //  matrix.addMetaData(metaData);
        //}

        matrix.setColumnTitles(this.getColumnTitles());
        matrix.setRowTitles(this.getRowTitles());
        matrix.setRowTitlesTitle(this.getRowTitlesTitle());

        matrix.setSpecies(this.getSpecies());

        matrix.set(this.get());

        return matrix;
    }  // getVisibleMatrix

    public static DataMatrix toDataMatrix(LensedDataMatrix lens) {
        DataMatrix matrix = new DataMatrix();
        matrix.setRowTitlesTitle(lens.getRowTitlesTitle());
        matrix.setColumnTitles(lens.getColumnTitles());
        matrix.setRowTitles(lens.getRowTitles());
        matrix.set(lens.get());
        // can't set URI or file extension - todo determine if that is a problem
        matrix.setFullName(lens.getFullName());
        matrix.setShortName(lens.getShortName());
        matrix.setDataTypeBriefName(lens.getDataTypeBriefName());
        matrix.setSpecies(lens.getSpecies());
        matrix.setName(lens.getName());
        matrix.setMetadata(lens.getMetadata());
        return matrix;
    }

    //----------------------------------------------------------------------------------------
    public String toString() {
        return getVisibleMatrix().toString();

    }

    //--------------------------------------------------------------------
    public void printTransformation() {
        //assert (columnState.length == columnOrder.length);
        int columnCount = columnState.length;
        //assert (columnState.length == underlyingMatrix.getColumnCount ());

        System.out.print("column\t");
        for (int i = 0; i < columnCount; i++)
            System.out.print(i + "\t");
        System.out.println();
        System.out.println("---------------------------");

        System.out.print("order\t");
        for (int i = 0; i < columnCount; i++) {
            System.out.print(columnOrder[i] + "\t");
        }
        System.out.println();

        System.out.print("state\t");
        for (int i = 0; i < columnCount; i++) {
            String stateString = "F";
            if (columnState[i]) stateString = "T";
            System.out.print(stateString + "\t");
        }
        System.out.println();
        System.out.println();

        System.out.println("row\tstate\t");
        System.out.println("------------------");
        for (int i = 0; i < rowState.length; i++)
            System.out.println(i + "\t" + rowState[i]);

    }

    //----------------------------------------------------------------------------------------
    public double[] get(int row) {
        int correctedRow = adjustRowIndexForRowState(row);
        return adjustRowForColumnOrderAndState(underlyingMatrix.get(correctedRow));
    }

    //----------------------------------------------------------------------------------------
    public double get(int row, int column) {
        int correctedRow = adjustRowIndexForRowState(row);
        return (adjustRowForColumnOrderAndState(underlyingMatrix.get(correctedRow)))[column];
    }

    //----------------------------------------------------------------------------------------
    public double[] get(String rowName) {
        return adjustRowForColumnOrderAndState(underlyingMatrix.get(rowName));
    }

    //----------------------------------------------------------------------------------------
    public double[][] get() {
        double[][] result = new double[getRowCount()][getColumnCount()];
        for (int r = 0; r < getRowCount(); r++)
            result[r] = get(r);

        return result;

    }

    //----------------------------------------------------------------------------------------
/**
 * return a vector of doubles from the column currently found at the specified
 * column number.
 */
    public double[] getColumn(int columnNumber) {
        int correctedColumn = columnOrder[columnNumber];
        return underlyingMatrix.getColumn(correctedColumn);

    }

    //----------------------------------------------------------------------------------------
    public int getRowIndex(String rowName) {
        return ((Integer) nameMap.get(rowName)).intValue();
    }

    //----------------------------------------------------------------------------------------
    protected double[] adjustRowForColumnOrderAndState(double[] row) {
        int dataColumnCount = getColumnCount();

        ArrayList list = new ArrayList();
        for (int i = 0; i < columnOrder.length; i++) {
            if (columnState[i]) {
                double nextValue = row[columnOrder[i]];
                list.add(new Double(nextValue));
            } // if
        } // for i

        double[] result = new double[list.size()];
        Double[] tmp = (Double[]) list.toArray(new Double[0]);
        for (int i = 0; i < tmp.length; i++)
            result[i] = tmp[i].doubleValue();

        return result;

    } // adjustRowForColumnOrderAndState

    //----------------------------------------------------------------------------------------
    protected String[] adjustTitlesForColumnOrderAndState(String[] columnTitles)
// so start, below, by creating an array which is 1 element shorter, and
// therefore equal in length to the number of data columns in the matrix.
// add the column zero label to the list, then traverse the data column
// titles, placing them as the columnOrder & columnStatus info decree.
    {
        int dataColumnCount = columnTitles.length; // getColumnCount ();
        String[] movableTitles = new String[dataColumnCount];
        for (int i = 0; i < dataColumnCount; i++)
            movableTitles[i] = columnTitles[i];

        ArrayList list = new ArrayList();
        for (int i = 0; i < columnOrder.length; i++) {
            if (columnState[i]) {
                String nextValue = movableTitles[columnOrder[i]];
                list.add(nextValue);
            } // if
        } // for i

        String[] result = (String[]) list.toArray(new String[0]);

        return result;

    } // adjustTitlesForColumnOrderAndState

    //----------------------------------------------------------------------------------------
    public void addRow(String rowTitle, double[] values) {
        int oldMax = rowState.length;
        int newMax = oldMax + 1;
        int newRowNumber = oldMax;

        boolean[] newRowState = new boolean[newMax];
        for (int i = 0; i < oldMax; i++)
            newRowState[i] = rowState[i];

        nameMap.put(rowTitle, new Integer(newRowNumber));

        newRowState[newRowNumber] = true;
        rowState = newRowState;
        underlyingMatrix.addRow(rowTitle, values);
        updateRowMap();

    } // addRow

    /**
     * Transposes in place the x and y axes of a data matrix (data, and row and column titles).
     * Use this with DataMatrix objects, it won't work with LensedDataMatrix objects
     * (use toDataMatrix() to convert a lens beforehand)
     * @param matrix the matrix to transpose in place
     */
    public static void transposeMatrix(DataMatrix matrix) {
        transposeMatrix(matrix, null);
    }

    /**
     * Transposes in place the x and y axes of a data matrix (data, and row and column titles),
     * using the supplied value as the title of row titles.
     * Use this with DataMatrix objects, it won't work with LensedDataMatrix objects
     * (use toDataMatrix() to convert a lens beforehand)
     * @param matrix the matrix to transpose in place
     * @param rowTitlesTitle the new title of row titles
     */
    public static void transposeMatrix(DataMatrix matrix, String rowTitlesTitle) {
        if (rowTitlesTitle != null)
            matrix.setRowTitlesTitle(rowTitlesTitle);
        DoubleMatrix2D coltMatrix = new SparseDoubleMatrix2D(matrix.get());
        matrix.set(coltMatrix.viewDice().toArray());


        String[] tmp = matrix.getColumnTitles();

        matrix.setColumnTitles(matrix.getRowTitles());
        matrix.setRowTitles(tmp);
    }
    

//----------------------------------------------------------------------------------------
} // class LensedDataMatrix
