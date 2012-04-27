package org.systemsbiology.gaggle.experiment.datamatrix.unitTests;

import java.util.Arrays;

import org.systemsbiology.gaggle.core.datatypes.DataMatrix;
import org.systemsbiology.gaggle.experiment.datamatrix.LensedDataMatrix;
import org.systemsbiology.gaggle.experiment.readers.DataMatrixFileReader;
import org.systemsbiology.gaggle.experiment.readers.DataMatrixReader;

import junit.framework.TestCase;


/**
 * Test whether a LensedDataMatrix fulfills the contract of a DataMatrix.
 * @author cbare
 */
public class LensedDataMatrixDecoratorTest extends TestCase {
  String dataPath = "org/systemsbiology/gaggle/experiment/datamatrix/unitTests/sampleData/";
  DataMatrix matrix;

  protected void setUp() throws Exception {
    DataMatrixReader reader = new DataMatrixFileReader ("jar://" + dataPath + "sample.ratio");
    reader.read ();
    matrix = reader.get();
  }

  public void test() throws Exception {
    assertNotNull(matrix);
    
    LensedDataMatrix lensedMatrix = new LensedDataMatrix(matrix);
    
    // this shouldn't be necessary. They should be on by default.
    lensedMatrix.enableAllRows();
    
    assertEquals(matrix.getDataTypeBriefName(), lensedMatrix.getDataTypeBriefName());
    assertEquals(matrix.getFullName(), lensedMatrix.getFullName());
    assertEquals(matrix.getRowTitlesTitle(), lensedMatrix.getRowTitlesTitle());
    assertEquals(matrix.getShortName(), lensedMatrix.getShortName());
    assertEquals(matrix.getSpecies(), lensedMatrix.getSpecies());

    assertEquals(matrix.getColumnCount(), lensedMatrix.getColumnCount());
    
    System.out.println("Column Titles:");
    System.out.println(Arrays.toString(matrix.getColumnTitles()));
    System.out.println(Arrays.toString(lensedMatrix.getColumnTitles()));
    assertTrue(Arrays.equals(matrix.getColumnTitles(), lensedMatrix.getColumnTitles()));
    
    System.out.println("RowCount:");
    System.out.println("RowCount:" + matrix.getRowCount());
    System.out.println("RowCount:" + lensedMatrix.getRowCount());
    assertEquals(matrix.getRowCount(), lensedMatrix.getRowCount());

    System.out.println("Row Titles:");
    System.out.println(Arrays.toString(matrix.getRowTitles()));
    System.out.println(Arrays.toString(lensedMatrix.getRowTitles()));
    assertTrue(Arrays.equals(matrix.getRowTitles(), lensedMatrix.getRowTitles()));
    
    assertEquals(matrix.get(0, 0), lensedMatrix.get(0, 0));
    
    System.out.println("Get row:");
    System.out.println(Arrays.toString(matrix.get(1)));
    System.out.println(Arrays.toString(lensedMatrix.get(1)));
    assertTrue(Arrays.equals(matrix.get(1), lensedMatrix.get(1)));

    assertTrue(Arrays.equals(matrix.get("VNG6413H"), lensedMatrix.get("VNG6413H")));

    assertEquals(matrix.getRowNumber("VNG6413H"), lensedMatrix.getRowNumber("VNG6413H"));
    
  }
  
  public void testData() throws Exception {
    assertNotNull(matrix);
    
    LensedDataMatrix lensedMatrix = new LensedDataMatrix(matrix);
    lensedMatrix.enableAllRows();

    for (int i=0; i<5; i++)
      lensedMatrix.setRowState(i, false);
    
    assertEquals(matrix.getRowCount() - 5, lensedMatrix.getRowCount());
    
    double[][] data = lensedMatrix.get();
    
    System.out.println("rowCount = " + lensedMatrix.getRowCount());
    System.out.println("Actual data length = " + data.length);
  }

}
