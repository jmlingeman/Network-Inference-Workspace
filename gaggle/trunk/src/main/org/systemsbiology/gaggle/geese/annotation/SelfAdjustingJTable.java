package org.systemsbiology.gaggle.geese.annotation;

import java.awt.Component;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;


/**
 * A JTable that adds the ability to adjust column widths
 * to fit contents.
 * 
 * Snagged from:
 * http://binkley.blogspot.com/2006/01/getting-jtable-columns-widths-to-fit.html
 */
public class SelfAdjustingJTable extends JTable {

  {
    final TableCellRenderer renderer = getTableHeader().getDefaultRenderer();

    for (int i = 0; i < getColumnCount(); ++i)
      getColumnModel().getColumn(i).setPreferredWidth(
          renderer.getTableCellRendererComponent(this,
              getModel().getColumnName(i), false, false, 0, i)
              .getPreferredSize().width);
  }

  
  public SelfAdjustingJTable() {}

  public SelfAdjustingJTable(TableModel dm) {
    super(dm);
  }

  public SelfAdjustingJTable(TableModel dm, TableColumnModel cm) {
    super(dm, cm);
  }

  public SelfAdjustingJTable(int numRows, int numColumns) {
    super(numRows, numColumns);
  }

  public SelfAdjustingJTable(Vector rowData, Vector columnNames) {
    super(rowData, columnNames);
  }

  public SelfAdjustingJTable(Object[][] rowData, Object[] columnNames) {
    super(rowData, columnNames);
  }

  public SelfAdjustingJTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
    super(dm, cm, sm);
  }


  public Component prepareRenderer(final TableCellRenderer renderer, final int row, final int column) {
    final Component prepareRenderer = super.prepareRenderer(renderer, row, column);
    final TableColumn tableColumn = getColumnModel().getColumn(column);

    tableColumn.setPreferredWidth(Math.max(prepareRenderer.getPreferredSize().width,
        tableColumn.getPreferredWidth()));

    return prepareRenderer;
  }

}
