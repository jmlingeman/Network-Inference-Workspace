package org.systemsbiology.gaggle;

import org.systemsbiology.gaggle.core.datatypes.DataMatrix;
import org.systemsbiology.gaggle.experiment.datamatrix.LensedDataMatrix;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import junit.framework.TestCase;

/*
 * Copyright (C) 2007 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */
public class MatrixTransposeTest extends TestCase {
    
    public void testMatrixTransposition() throws Exception {
        DataMatrix matrix = getDataMatrix();
        LensedDataMatrix.transposeMatrix(matrix);
        assertEquals(2.0, matrix.get(1, 0));
    }

    public static DataMatrix getDataMatrix() {
        DataMatrix matrix = new DataMatrix();

        matrix.setFullName("Demo Yeast created on the fly, meaningless data");
        matrix.setShortName("Demo Yeast");

        String[] columnTitles = {"T1", "T2", "T3"};
        String[] rowTitles = {"g1", "g2", "g3", "g4"};
        int dataRows = rowTitles.length;
        int dataColumns = columnTitles.length;
        matrix.setSize(dataRows, dataColumns);

        matrix.setSpecies("Saccharomyces cerevisiae");
        matrix.setRowTitlesTitle("GENE");
        matrix.setColumnTitles(columnTitles);
        matrix.setRowTitles(rowTitles);

        int i = 1;
        for (int r = 0; r < dataRows; r++)
            for (int c = 0; c < dataColumns; c++) {
                matrix.set(r,c, i++);
            }

        matrix.setName("a sample matrix");
        return matrix;
    }


    public void testTransposeLens() throws Exception {
        LensedDataMatrix lens = new LensedDataMatrix(getDataMatrix());
        lens.enableAllRows();
        lens.enableAllColumns();
        lens.disableColumn(1);
        lens.disableRow(2);
        DataMatrix matrix = LensedDataMatrix.toDataMatrix(lens);
        System.out.println("before:\n" + lens.toString());
        LensedDataMatrix.transposeMatrix(matrix);
        System.out.println("after:");
        System.out.println(matrix.toString());


    }



}
