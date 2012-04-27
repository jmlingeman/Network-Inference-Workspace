// MatrixSpreadsheet.java
//-----------------------------------------------------------------------------------------------
// $Revision: 4368 $   $Date: 2005/04/13 02:03:00 $
//-----------------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui;
//-----------------------------------------------------------------------------------------------

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.io.*;
import java.awt.datatransfer.*;

import org.systemsbiology.gaggle.experiment.datamatrix.*;
import org.systemsbiology.gaggle.experiment.gui.actions.*;
import org.systemsbiology.gaggle.experiment.metadata.*;

import org.systemsbiology.gaggle.util.*;

//---------------------------------------------------------------------------------------
public class MatrixSpreadsheet extends JPanel implements DataMatrixView, Serializable {

    protected org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix;
    protected DataMatrixTableModel tableModel;
    protected String name;
    protected LensedDataMatrix lens;
    protected NameHelper nameHelper;
    protected MatrixViewCoordinator matrixViewCoordinator;

    protected JTable table;
    protected JTextField selectionCountTextField;
    protected JButton nameTypeButton;
    protected boolean rowSelectionListeningDisabled = false;

    private boolean sortOrderAscending = false;
    private boolean calculateByDataWidth = true;

    protected MetaDataNavigator navigator;

    HashMap columnCheckBoxHeaderMap;
    protected JPanel buttonPanel;
    protected JPanel controlPanel;
    protected JPanel movieController;

    // 'volcano' plots are x-y plots of a single column from two congruent matrices  -- having 
    // the same shape, the same row & column titles.  information about the existence
    // and identity of a possible congruent matrix can only be provided by the
    // MatrixViewCoordinator class.

    protected JButton volcanoPlotButton;
    protected int companionMatrixID = -1;

    protected MutableList rowHeader;
    protected int preferredTableWidth = 600;
    protected int preferredTableHeight = 100;
    static final int DEFAULT_COLUMN_WIDTH = 75;
//  private javax.swing.Timer dragTimer;
//  private int dragFrom = -1;
//  private int dragTo = -1;

    protected HashMap widgets = new HashMap();

    //-----------------------------------------------------------------------------------
    public MatrixSpreadsheet(org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix, MatrixViewCoordinator mvc, MetaDataNavigator navigator) throws Exception {
        this.matrix = matrix;
        this.navigator = navigator;
        MetaDataNavigatorSingleton.getInstance().setNavigator(navigator);
        name = matrix.getShortName();
        matrixViewCoordinator = mvc;
        nameHelper = NameHelperFactory.getNameHelper(matrix.getSpecies());
        if (canSortAllExperimentsByOrder()) {
            sortOrderableExperiments();
        }
        lens = new LensedDataMatrix(matrix);
        createPanel(matrix);

    } // ctor


    /**
     * Sorts matrix by distinct experiment and then by "order" attribute within each
     * (arbitrarily?  by experiment name? by date?) and then by order
     */
    private void sortOrderableExperiments() {
        LensedDataMatrix lens;
        try {
            lens = new LensedDataMatrix(matrix);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String[] reorderedColumnTitles = lens.getColumnTitles().clone();
        Arrays.sort(reorderedColumnTitles, new Comparator<String>() {
            public int compare(String s0, String s1) {
                MetaData md0 = navigator.getMetaDataForCondition(s0);
                MetaData md1 = navigator.getMetaDataForCondition(s1);
                Condition c0 = md0.getCondition(s0);
                Condition c1 = md1.getCondition(s1);

                if (md0.getTitle().equals(md1.getTitle()))  {
                    return c0.getOrder().compareTo(c1.getOrder());
                } else {
                    return md0.getTitle().compareTo(md1.getTitle());
                }

            }
        });

        lens.changeColumnPositions(reorderedColumnTitles);
        lens.setRowState(true); // this makes all rows always enabled which may not be what you want,
        // depending on where you call this method from. on the other hand, it fixes a really obscure bug.

        matrix = lens.getVisibleMatrix();

    }


    /**
     * Checks to see if all experiments in matrix have an "order" attribute which will enable correct sorting.
     * @return
     */
    private boolean canSortAllExperimentsByOrder() {
        for (String columnTitle : matrix.getColumnTitles()){
            MetaData md = null;
            try {
                md = navigator.getMetaDataForCondition(columnTitle);
                Condition cond = md.getCondition(columnTitle);
                if (cond == null) return false;
                if (cond.getOrder() == null) return false;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }


    /**
     * Changes column order to match <code>order</code> attribute in emi-ml, if present.
     * Changes the <code>matrix</code> class member.
     * @deprecated in favor of sortOrderableExperiments()
     */
    private void adjustColumnOrder() {
        /*
        this works fine if you just fire up DMV with one experiment. if you start combining conditions from
        different experiments, it stops working. however, we can detect that conditions come from different
        experiments. so the question then is how to sort, given that info.
        let's say we have 2 experiments. 
         */
        Map<String,MetaData> conditionToExperimentMap = new HashMap<String, MetaData>();
        Map<MetaData,String> distinctExperimentsMap = new HashMap<MetaData, String>();
        for (String columnTitle : matrix.getColumnTitles()){
            MetaData md = null;
            try {
                md = navigator.getMetaDataForCondition(columnTitle);
            } catch (Exception e) {

                return;
            }
            conditionToExperimentMap.put(columnTitle, md);
            distinctExperimentsMap.put(md, "nothing");
        }
        if (distinctExperimentsMap.size() > 1) { // just punt for now
            return; //todo - deal with multiple experiments
        }


        Map<Integer,Integer> oldToNewPositionMap = new HashMap<Integer, Integer>();
        final Map<String,Integer> conditionToOrderMap = new HashMap<String, Integer>();

        Map<String,Integer> oldColumnPositions = new HashMap<String, Integer>();
        Map<String,Integer> newColumnPositions = new HashMap<String, Integer>();


        String[] reorderedColumnTitles = matrix.getColumnTitles().clone();//new String[matrix.getColumnCount()];
        for (int i = 0; i < matrix.getColumnCount(); i++){
            String columnTitle = matrix.getColumnTitles()[i];
            oldColumnPositions.put(columnTitle, i);
            MetaData md = null;
            Condition cond = null;

            try {
                md = navigator.getMetaDataForCondition(columnTitle);
                cond = md.getCondition(columnTitle);
            } catch (NullPointerException e) {
                return;
            }
            if (cond.getOrder() == null) {
                return; // we can't re-order
            }
            conditionToOrderMap.put(columnTitle, cond.getOrder());
            oldToNewPositionMap.put(i, cond.getOrder()-1);
        }


        Arrays.sort(reorderedColumnTitles, new Comparator<String>() {
            public int compare(String s1, String s2) {
                return conditionToOrderMap.get(s1).compareTo(conditionToOrderMap.get(s2));
            }
        });

        for (int i = 0; i < reorderedColumnTitles.length; i++) {
            newColumnPositions.put(reorderedColumnTitles[i],i);
        }

        System.out.println("new column positions size = " + newColumnPositions.size());


        matrix.setColumnTitles(reorderedColumnTitles);


        // todo figure out what is happening below-- why is the data order change commented out?
        // it looks like a SERIOUS bug waiting to happen....if condition order is changed
        // so it doesn't match alphanumeric order, the column names will match the wrong data
        // and that is extremely serious.

        ///if(true)return;//todo remove


        //now change data order
        for (int i = 0; i < matrix.getRowCount(); i++) {
            double[] row = matrix.get(i);
            double[] newRow = new double[row.length];
            for (int j = 0; j < row.length; j++) {
                System.out.println("i = " + i + ", j = " + i);
                int newColumnPosition = newColumnPositions.get(j);
                newRow[newColumnPosition] = row[j];
            }
            matrix.set(i, newRow);
        }

    }

    private void reverseColumns() {
        String[] reversedColumnTitles = new String[matrix.getColumnCount()];

        //double[][] reversedData = new double[matrix.getRowCount()][matrix.getColumnCount()];

        System.out.println("matrix column count = " + matrix.getColumnCount());
        System.out.println("matrix row count = " + matrix.getRowCount());
        System.out.println("data dimension one length: " + matrix.get().length);
        System.out.println("data dimension two length: " + matrix.get()[0].length);

        //System.out.println("reversed data dimension one length: " + reversedData.length);
        //System.out.println("reversed data dimension two length: " + reversedData[0].length);
        


        int position = 0;

        for (int i = matrix.getColumnCount()-1; i >= 0; i--) {
            reversedColumnTitles[i] = matrix.getColumnTitles()[position];
            position++;
        }

        matrix.setColumnTitles(reversedColumnTitles);


        for (int index = 0; index < matrix.getRowCount(); index++) {
            double[] row = matrix.get(index);
            for(int i=0;i<row.length/2;i++){
              double t = row[i];
              row[i] = row[row.length-(1+i)];
              row[row.length-(1+i)] = t;
            }
            matrix.set(index, row);
        }




    }

    //----------------------------------------------------------------------------------------------------
    public String getClassName() {
        return "MatrixSpreadsheet";
    }

    public String getName() {
        return name;
    }

    public HashMap getWidgets() {
        return widgets;
    }

    //----------------------------------------------------------------------------------------------------
    public void clearSelection() {
        table.clearSelection();
        selectionCountTextField.setText("0");
        enableAllColumns();
        lens.clear();
    }

    //----------------------------------------------------------------------------------------------------
    public int getCompanionMatrixID() {
        return companionMatrixID;

    }

    //----------------------------------------------------------------------------------------------------
    public void setCompanionMatrixID(int newValue) {
        companionMatrixID = newValue;
        volcanoPlotButton.setEnabled(newValue >= 0);

    }

    //----------------------------------------------------------------------------------------------------
    public void selectAllRows() {
        rowSelectionListeningDisabled = true;
        table.selectAll();
        updateFromCurrentTableSelection();
        rowSelectionListeningDisabled = false;

        //int selectionCount = table.getSelectedRowCount ();
        //String s = (new Integer (selectionCount)).toString ();
        //selectionCountTextField.setText (s);
        // enableAllColumns ();

    }

    //----------------------------------------------------------------------------------------------------
    public void invertSelection() {
        rowSelectionListeningDisabled = true;
        //int [] selectedRows = table.getSelectedRows ();
        // clearSelection ();
        ListSelectionModel selectionModel = table.getSelectionModel();

        int selectCount = 0;
        int deselectCount = 0;

        for (int i = 0; i < table.getRowCount(); i++) {
            boolean selected = selectionModel.isSelectedIndex(i);
            if (selected) {
                // System.out.println ("    " + i + ": " + selected);
                selectionModel.removeSelectionInterval(i, i);
                deselectCount++;
            } else {
                selectionModel.addSelectionInterval(i, i);
                selectCount++;
            }
        } // for i

        rowSelectionListeningDisabled = false;
        updateFromCurrentTableSelection();

    } // invertSelection

    //----------------------------------------------------------------------------------------------------
    public String getSpecies() {
        return matrix.getSpecies();
    }

    //----------------------------------------------------------------------------------------------------
    public void select(String[] names) {
        select(getSpecies(), names);
    }

    //----------------------------------------------------------------------------------------------------
    public void select(String species, String[] names) {
        java.util.List newSelections = Arrays.asList(names);
        int max = rowHeader.getModel().getSize();
        String[] currentRowHeaderNames = new String[max];
        for (int i = 0; i < max; i++) {
            String name = (String) rowHeader.getModel().getElementAt(i);
            currentRowHeaderNames[i] = name;
        } // for i

        // initialize 'revisedSelection' with the currently selected row titles
        ArrayList revisedSelection = new ArrayList(Arrays.asList(lens.getRowTitles()));
        SpreadsheetLineFinder finder = new SpreadsheetLineFinder(currentRowHeaderNames, nameHelper);

        // addSelectionInterval where name is in
        // (canonical( currentRowHeaderNames ) intersection newSelections intersection ^revisedSelection)
        for (int r = 0; r < max; r++) {
            String currentName = currentRowHeaderNames[r];
            String canonicalName = nameHelper.getName("canonical", currentName);
            // JCB - made the following change because CorrelationFinder was
            // returning non-canonical row names and failing to select them
            // This might cause a bug if the current name of one gene is the canonical name of another
//            boolean nameFoundBothInTableAndList = newSelections.contains(canonicalName);
            boolean nameFoundBothInTableAndList = newSelections.contains(canonicalName) || newSelections.contains(currentName);
            if (nameFoundBothInTableAndList && !revisedSelection.contains(canonicalName)) {
                revisedSelection.add(canonicalName);
                int rowForThisName = finder.getRowNumber(currentName);
                table.getSelectionModel().addSelectionInterval(rowForThisName, rowForThisName);
            } // if
        } // for r

        String countString = (new Integer(revisedSelection.size()).toString());
        selectionCountTextField.setText(countString);

    } // select


    public MetaDataNavigator getMetaDataNavigator() {
        return navigator;
    }

    //----------------------------------------------------------------------------------------------------
    public String[] getSelection() {
        //System.out.println ("MatrixSpreadsheet.getSelection, lens row count: " + lens.getRowTitles().length);
        return lens.getRowTitles();
    }

    //----------------------------------------------------------------------------------------------------
    public LensedDataMatrix getLens() {
        return lens;
    }

    public org.systemsbiology.gaggle.core.datatypes.DataMatrix getMatrix() {
        return matrix;
    }

    public org.systemsbiology.gaggle.core.datatypes.DataMatrix getSelectedMatrix() {
        return lens;
    }

    //----------------------------------------------------------------------------------------------------
    public void createPanel(org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix) {
        ToolTipManager.sharedInstance().setInitialDelay(0);
        tableModel = new DataMatrixTableModel(matrix);
        columnCheckBoxHeaderMap = new HashMap();
        table = new JTable(tableModel);
        table.setShowGrid(true);

        rowHeader = new MutableList(matrix.getRowTitles());
        rowHeader.setFixedCellWidth(100);
        rowHeader.setFixedCellHeight(table.getRowHeight());
        rowHeader.setCellRenderer(new RowHeaderRenderer(table));

//  table.getColumnModel().addColumnModelListener (new TableColumnMovedListener ());
        calculateByDataWidth = true;
        recalculateColumnWidth();
        setupCheckBoxColumnHeaders();

        ListSelectionModel selectionModel = table.getSelectionModel();
        selectionModel.addListSelectionListener(new TableSelectionListener(table, matrix));
        table.setSelectionModel(selectionModel);

        table.setPreferredScrollableViewportSize(new Dimension(preferredTableWidth, preferredTableHeight));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setRowHeaderView(rowHeader);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        String matrixName = matrix.getShortName();
        String correspondingNodeAttributeName = "";
        int pos = matrixName.lastIndexOf(".");
        if (pos > 0 && (pos < matrixName.length() - 1))
            correspondingNodeAttributeName = matrixName.substring(pos + 1);
        if (matrixName.indexOf('.') < 0)
            correspondingNodeAttributeName = matrixName;

        controlPanel = createControlPanel(correspondingNodeAttributeName);

        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

    } // createPanel

    //-----------------------------------------------------------------------------------------------------
    private int[] calculateColumnWidths(JTable table, org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix, boolean calculateByDataWidth)
// column titles can be significantly longer than the column contents; this method
// calculates a width based upon the title string's length, inflates it a bit
// and then (if the value is larger than the default) returns the calculated value.
// 
// this method leaves a lot to be desired.  the flaws include:  (pshannon, 2004/01/22)
//    1) i don't understand why the FontMetrics.stringWidth (s) result needs inflation
//    2) the font on the table should be a run-time user option, and these column
//       widths will need to be recalculated
// (pshannon, 2005/02/20) this method is serious brain dead.  the precision of the
//    formatter used (by default?) in the JTable cell renderer provides only 3 decimal
//    points.  But (see below) the toString method of the Double class provides a lot
//    more precision.
//  todo: this all needs work.  far better would be to allow the user to specify the
//    display format, and to automatically shrink column widths to fit this, or (as now)
//    toggle to display the full width needed for column names.
// 
    {


        String[] columnTitles = matrix.getColumnTitles();
        int columnCount = columnTitles.length;
        int defaultWidth = DEFAULT_COLUMN_WIDTH;
        int[] result = new int[columnCount];
        //int sampleRowNumber = matrix.getRowCount() / 2;

        FontMetrics fontMetrics = table.getFontMetrics(table.getFont());

        String sampleCellContents;
        for (int column = 0; column < columnCount; column++) {
            if (calculateByDataWidth)
                sampleCellContents = "32.1234";
                //sampleCellContents = (new Double (matrix.get (sampleRowNumber, column))).toString ();
            else
                sampleCellContents = columnTitles[column];
            int calculatedWidth = fontMetrics.stringWidth(sampleCellContents);
            int repairedWidth = (int) (calculatedWidth * 1.4);
            int finalWidth = (repairedWidth > defaultWidth) ? repairedWidth : defaultWidth;
            //System.out.print ("  column " + column);
            //System.out.print ("  contents: " + sampleCellContents);
            //System.out.print ("   calc: " + calculatedWidth);
            //System.out.print ("   repa: " + repairedWidth);
            //System.out.println ("   finl: " + finalWidth);
            result[column] = finalWidth;
        } // for column

        return result;

    } // calculateColumnWidth

    //-----------------------------------------------------------------------------------------------------
    // this class is needed by interface ClipboardOwner
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }

    //-------------------------------------------------------------------------------
    class RowHeaderRenderer extends JLabel implements ListCellRenderer {

        RowHeaderRenderer(JTable table) {
            JTableHeader header = table.getTableHeader();
            setOpaque(true);
            // commenting out the following line because it causes the row header labels not
            // to appear in Leopard
            //setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setHorizontalAlignment(CENTER);
            setForeground(header.getForeground());
            setBackground(header.getBackground());
            JLabel lbl = new JLabel();
            setBackground(lbl.getBackground());
            setFont(header.getFont());
        }

        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected,
                                                      boolean cellHasFocus) {

            //DataMatrixTableModel model = (DataMatrixTableModel)table.getModel();
            int correctIndex = tableModel.getCorrectRowIndex(index);
            String originalName = lens.getUnderlyingMatrix().getRowTitles()[correctIndex];
            String commonName = nameHelper.getName(nameHelper.getCategoryList()[1], originalName);

            setText((value == null) ? "" : value.toString());

            if (null != value) {
                StringBuffer sb = new StringBuffer();
                sb.append(originalName);

                if (!commonName.equals(originalName)) {
                    sb.append(" (" + commonName + ")");
                }

                setToolTipText(sb.toString());
            }
            return this;
        }

    } // inner class RowHeaderRenderer
//-------------------------------------------------------------------------------

    /*
    class TableColumnMovedListener implements TableColumnModelListener {

        public void columnMoved (TableColumnModelEvent e) {
            if (e.getFromIndex() == e.getToIndex())return;

            dragTo = e.getToIndex();

          // TODO dragTime is always null
          if (!dragTimer.isRunning()) {
                    dragFrom = e.getFromIndex ();
            dragTimer.start();
            return;
          }
        } // columnMoved

        public void columnAdded (TableColumnModelEvent e) {;}
        public void columnRemoved (TableColumnModelEvent e) {;}
        public void columnMarginChanged (ChangeEvent e) {;}
        public void columnSelectionChanged (ListSelectionEvent e) {;}

      }  // inner class TableColumnMovedListener
    */
//------------------------------------------------------------------------------
    class TableHeaderCheckboxListener implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
            Object source = e.getSource();
            if (source instanceof CheckBoxHeader) {
                boolean state = e.getStateChange() == ItemEvent.SELECTED;
                int column = ((CheckBoxHeader) (e.getItem())).getColumn();
                lens.setColumnState(column, state);
            }
        } // itemStateChanged
    } // inner class TableHeaderCheckboxListener

    //------------------------------------------------------------------------------
    class TableSelectionListener implements ListSelectionListener {
        JTable table;
        org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix;

        TableSelectionListener(JTable table, org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix) {
            this.table = table;
            this.matrix = matrix;
        }

        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting())
                return;
            if (rowSelectionListeningDisabled)
                return;
            updateFromCurrentTableSelection();
        } // valueChanged

    } // TableSelectionListener

    //------------------------------------------------------------------------------
    protected void updateFromCurrentTableSelection() {
        lens.disableAllRows();
        int[] selectedRows = table.getSelectedRows();

        selectionCountTextField.setText("" + selectedRows.length);

        int[] rowsToSelect = new int[selectedRows.length];
        String rowNames[] = new String[rowsToSelect.length];
        //System.out.println ("MSS.updateFromCurrentTableSelection:  4");
        for (int i = 0; i < selectedRows.length; i++) {
            String rowName = (String) rowHeader.getContents().get(selectedRows[i]);
            String origName = rowHeader.getOriginalName(rowName);
            rowNames[i] = origName;
            rowsToSelect[i] = lens.getRowIndex(origName);
        } // for i

        //System.out.println ("MSS.updateFromCurrentTableSelection:  5");
        lens.enableRows(rowsToSelect);
        //System.out.println ("MSS.updateFromCurrentTableSelection:  6");

    } // updateFromCurrentTableSelection

    //------------------------------------------------------------------------------
    private void doPlot(LensedDataMatrix lens) {
        if (matrixViewCoordinator != null)
            matrixViewCoordinator.doPlot(lens);
    }

    //------------------------------------------------------------------------------
    private void doVolcanoPlot(LensedDataMatrix lens) {
        if (matrixViewCoordinator != null) {
            String[] columnNames = lens.getColumnTitles();
            VolcanoPlotColumnSelector vpcs =
                    new VolcanoPlotColumnSelector(columnNames);
            vpcs.pack();
            vpcs.setLocation(calculateCenter(vpcs));
            vpcs.setVisible(true);
            widgets.put("volcanoPlotColumnSelector", vpcs);
        }

    }// doVolcanoPlot

    //------------------------------------------------------------------------------
    class VolcanoPlotColumnSelector extends JDialog {

        String[] columnNames;
        String currentSelection;
        JList listbox;

        //----------------------------------------------------------------------------
        public VolcanoPlotColumnSelector(String[] columnNames) {
            super((Frame) null, "Choose column", false);
            this.columnNames = columnNames;
            JPanel listboxPanel = new JPanel();
            listboxPanel.setLayout(new BorderLayout());
            listbox = new JList(columnNames);
            listbox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane listboxscrollPane = new JScrollPane(listbox);
            listboxPanel.add(listboxscrollPane, BorderLayout.CENTER);
            listboxPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JPanel outerPanel = new JPanel();
            outerPanel.setLayout(new BorderLayout());
            JPanel buttonPanel = new JPanel();

            JButton plotButton = new JButton("Plot");
            plotButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String name = (String) listbox.getSelectedValue();
                    if (name != null)
                        matrixViewCoordinator.doVolcanoPlot(matrix, name, companionMatrixID);
                }
            });

            JButton dismissButton = new JButton("Dismiss");
            dismissButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    VolcanoPlotColumnSelector.this.dispose();
                    widgets.remove("volcanoPlotColumnSelector");
                    widgets.remove("vpcsListbox");
                    widgets.remove("vpcsPlotButton");
                    widgets.remove("vpcsDismissButton");
                }
            });

            widgets.put("vpcsListbox", listbox);
            widgets.put("vpcsPlotButton", plotButton);
            widgets.put("vpcsDismissButton", dismissButton);

            buttonPanel.add(plotButton);
            buttonPanel.add(dismissButton);
            outerPanel.add(buttonPanel, BorderLayout.SOUTH);
            outerPanel.add(listboxPanel, BorderLayout.CENTER);
            getContentPane().add(outerPanel);
            setVisible(true);
        } // ctor

    } // inner class VolcanoPlotColumnSelector

    //-------------------------------------------------------------------------------------------
    protected JPanel createControlPanel(String correspondingNodeAttributeName) {
        controlPanel = new JPanel();
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        controlPanel.add(toolbar);

        String nameTypeToolTip = "<html>Change the type of name displayed<br>" +
                "in the row headers</HTML>";
        nameTypeButton = new JButton(IconFactory.getToggleIcon());
        nameTypeButton.setBackground(Color.WHITE);
        nameTypeButton.setToolTipText(nameTypeToolTip);
        nameTypeButton.setActionCommand("1");
        nameTypeButton.addActionListener(new NameTypeChangeListener());
        toolbar.add(nameTypeButton);

        JButton plotSelectedButton = new JButton(IconFactory.getPlotIcon());
        toolbar.add(plotSelectedButton);
        plotSelectedButton.setToolTipText("Plot selected rows");
        plotSelectedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doPlot(lens);
            }
        });

        volcanoPlotButton = new JButton(IconFactory.getVolcanoPlotterIcon());
        toolbar.add(volcanoPlotButton);
        volcanoPlotButton.setToolTipText("Volcano plot of first two congruent matrices");

        volcanoPlotButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doVolcanoPlot(lens);
            }
        });
        volcanoPlotButton.setEnabled(false);

        JButton findCorrelationsButton = new JButton(IconFactory.getFindCorrelationsIcon());
        findCorrelationsButton.setToolTipText("Find rows correlated to selected row/s");
        findCorrelationsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JDialog dialog = new CorrelationFinderDialog(MatrixSpreadsheet.this);
                dialog.pack();
                dialog.setVisible(true);
            }
        });
        toolbar.add(findCorrelationsButton);

        JButton selectAboveThresholdButton = new JButton("T");
        selectAboveThresholdButton.setToolTipText("Select rows above threshold");
        selectAboveThresholdButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JDialog dialog = new ThresholdSelectorDialog(MatrixSpreadsheet.this);
                dialog.pack();
                dialog.setVisible(true);
            }
        });
        toolbar.add(selectAboveThresholdButton);

        JButton exportButton = new JButton(IconFactory.getExportIcon());
        exportButton.setToolTipText("Export selection to tab-delimited file");
        exportButton.addActionListener(new MatrixExporter(this));
        toolbar.add(exportButton);

        JButton createNewMatrixFromSelectionButton =
                new JButton(IconFactory.getCreateFromSelectionIcon());
        createNewMatrixFromSelectionButton.setToolTipText("Create new matrix from selection");
        createNewMatrixFromSelectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createNewMatrixFromSelection();
            }
        });

        toolbar.add(createNewMatrixFromSelectionButton);

        JButton sortButton = new JButton(IconFactory.getSortIcon());
        sortButton.setBackground(Color.WHITE);
        sortButton.setToolTipText("<html>Sort data in alphanumeric order (in all matrices)<br>" +
                "sorting by the currently selected name type.<BR>" +
                "Toggles between descending and ascending sort order.</html>");
        sortButton.addActionListener(new SortButtonListener());
        toolbar.add(sortButton);

        JButton fitColumnsButton = new JButton(IconFactory.getFitColumnWidthIcon());
        fitColumnsButton.setBackground(Color.WHITE);
        fitColumnsButton.setToolTipText("Toggle column width");
        fitColumnsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calculateByDataWidth = !calculateByDataWidth;
                recalculateColumnWidth();
            }
        });

        toolbar.add(fitColumnsButton);

        JButton andButton = new JButton("AND");
        andButton.setToolTipText("Create new row from the logical AND of 2 selected rows.");
        andButton.addActionListener(new MathematicalOperations(this, "AND"));
        toolbar.add(andButton);

        JButton orButton = new JButton("OR");
        orButton.setToolTipText("Create new row from the logical OR of 2 selected rows.");
        orButton.addActionListener(new MathematicalOperations(this, "OR"));
        toolbar.add(orButton);

        JButton avgButton = new JButton("AVG");
        avgButton.setToolTipText("Create new row from the average of 2 or more selected rows.");
        avgButton.addActionListener(new MathematicalOperations(this, "AVG"));
        toolbar.add(avgButton);

        selectionCountTextField = new JTextField("0", 5);
        selectionCountTextField.setToolTipText("Number of rows currently selected");
        selectionCountTextField.setEditable(false);
        // toolbar.add (new JLabel ("Selected "));
        toolbar.add(selectionCountTextField);

        JButton clearSelectionButton = new JButton("Clear");
        clearSelectionButton.setToolTipText("Clear selections: enable all columns, deselect all rows");
        clearSelectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearSelection();
            }
        });
        toolbar.add(clearSelectionButton);

        JButton selectAllButton = new JButton("All");
        selectAllButton.setToolTipText("Select all rows");
        selectAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectAllRows();
            }
        });
        toolbar.add(selectAllButton);

        JButton invertSelectionButton = new JButton("Inv");
        invertSelectionButton.setToolTipText("Invert selection");
        invertSelectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                invertSelection();
            }
        });
        toolbar.add(invertSelectionButton);

        widgets.put("nameTypeButton", nameTypeButton);
        widgets.put("plotSelectedButton", plotSelectedButton);
        widgets.put("volcanoPlotButton", volcanoPlotButton);
        widgets.put("findCorrelationsButton", findCorrelationsButton);
        widgets.put("selectAboveThresholdButton", selectAboveThresholdButton);
        widgets.put("exportButton", exportButton);
        widgets.put("createNewMatrixFromSelectionButton", createNewMatrixFromSelectionButton);
        widgets.put("sortButton", sortButton);
        widgets.put("fitColumnsButton", fitColumnsButton);
        widgets.put("andButton", andButton);
        widgets.put("orButton", orButton);
        widgets.put("avgButton", avgButton);
        widgets.put("selectionCountTextField", selectionCountTextField);
        widgets.put("clearSelectionButton", clearSelectionButton);
        widgets.put("selectAllButton", selectAllButton);
        widgets.put("invertSelectionButton", invertSelectionButton);


        return controlPanel;

    } // createControlPanel

    //------------------------------------------------------------------------------------
    class NameTypeChangeListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String[] originalNames = matrix.getRowTitles();
            Vector newNamesInOriginalOrder = new Vector();
            for (int i = 0; i < rowHeader.getOriginalValues().size(); i++) {
                String value = (String) rowHeader.getOriginalValues().get(i);
                String commonName = null;
                String canonicalName = originalNames[i];
                //assert(canonicalName.equals((String)rowHeader.getOriginalName(value)));
                commonName = nameHelper.getName(nameHelper.getCategoryList()[1], value);
                if (nameTypeButton.getActionCommand().equals("0"))
                    newNamesInOriginalOrder.add(canonicalName);
                else if (null != commonName)
                    newNamesInOriginalOrder.add(commonName);
                else
                    newNamesInOriginalOrder.add((value == null) ? "" : value);
            } // for i
            rowHeader.setOriginalValues(newNamesInOriginalOrder);
            String[] names = new String[rowHeader.getModel().getSize()];
            if (nameTypeButton.getActionCommand().equals("0")) {
                for (int i = 0; i < rowHeader.getModel().getSize(); i++) {
                    String value = (String) rowHeader.getModel().getElementAt(i);
                    names[i] = rowHeader.getOriginalName(value);
                } // for i
            } // if
            else {
                for (int i = 0; i < rowHeader.getModel().getSize(); i++) {
                    String value = (String) rowHeader.getModel().getElementAt(i);
                    String commonName = null;
                    commonName = nameHelper.getName(nameHelper.getCategoryList()[1], value);
                    if (null != commonName)
                        names[i] = commonName;
                    else
                        names[i] = (value == null) ? "" : value;
                } // for i
            } // else
            rowHeader.changeList(names);

            if (nameTypeButton.getActionCommand().equals("0"))
                nameTypeButton.setActionCommand("1");
            else
                nameTypeButton.setActionCommand("0");
        } // actionPerformed

    } // inner class NameTypeChangeListener

    //------------------------------------------------------------------------------
    public void createNewMatrixFromSelection() {
        if (lens.getRowCount() == 0) {
            JOptionPane.showMessageDialog(MatrixSpreadsheet.this, "No rows are selected!",
                    "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newMatrixName = JOptionPane.showInputDialog(MatrixSpreadsheet.this,
                "Please enter a name for this matrix:");
        if (newMatrixName == null)
            return;

        createNewMatrixFromSelection(newMatrixName);

    } // createNewMatrixFromSelection

    //------------------------------------------------------------------------------
    public void createNewMatrixFromSelection(String newMatrixName) {
        try {
            org.systemsbiology.gaggle.core.datatypes.DataMatrix subMatrix = lens.getVisibleMatrix();
            subMatrix.setShortName(newMatrixName);
            if (matrixViewCoordinator != null)
                matrixViewCoordinator.addMatrixSpreadsheetView(subMatrix, navigator);
        }
        catch (Exception ex0) {
            ex0.printStackTrace();
        }

    }  // createNewMatrixFromSelection

    //------------------------------------------------------------------------------
    public void recalculateColumnWidth() {
        int[] suggestedColumnWidths = calculateColumnWidths(table, matrix, calculateByDataWidth);

        Enumeration enumeration = table.getColumnModel().getColumns();
        int columnCount = 0;
        while (enumeration.hasMoreElements()) {
            TableColumn aColumn = (TableColumn) enumeration.nextElement();
            aColumn.setPreferredWidth(suggestedColumnWidths[columnCount]);
            columnCount++;
        } // while

    } // recalculateColumnWidth

    //------------------------------------------------------------------------------
    class SortButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            doSort(sortOrderAscending);
            sortOrderAscending = !sortOrderAscending;
        }

    } // inner class SortButtonListener

    //------------------------------------------------------------------------------
    public void doSort(boolean ascending) {
        Hashtable hash = new Hashtable();
        Hashtable selectedRowNames = new Hashtable();
        for (int i = 0; i < table.getSelectedRowCount(); i++) {
            String rowName = (String) rowHeader.getContents().getElementAt(table.getSelectedRows()[i]);
            selectedRowNames.put(rowName, "nothing");
        }

        Object[] sorted = new Object[rowHeader.getContents().size()];
        Object[] original = new Object[rowHeader.getOriginalValues().size()];
        rowHeader.getContents().copyInto(sorted);
        rowHeader.getOriginalValues().copyInto(original);

        if (ascending)
            Arrays.sort(sorted);
        else
            Arrays.sort(sorted, new Comparator() {
                public int compare(Object a, Object b) {
                    Comparable ca = (Comparable) a;
                    Comparable cb = (Comparable) b;
                    return cb.compareTo(ca);
                }
            });

        rowHeader.changeList(sorted);
        for (int i = 0; i < original.length; i++)
            hash.put(original[i], new Integer(i));

        int[] newRowIndices = new int[rowHeader.getContents().size()];
        for (int i = 0; i < sorted.length; i++) {
            int newValue = ((Integer) hash.get(sorted[i])).intValue();
            newRowIndices[i] = newValue;
        }

        //DataMatrixTableModel model = (DataMatrixTableModel)table.getModel();
        tableModel.setNewRowIndices(newRowIndices);
        ListSelectionModel lsm = table.getSelectionModel();
        if (!lsm.isSelectionEmpty()) {
            table.clearSelection();
            for (int i = 0; i < table.getRowCount(); i++) {
                String rowName = (String) rowHeader.getContents().getElementAt(i);
                if (null != selectedRowNames.get(rowName))
                    table.addRowSelectionInterval(i, i);
            }
        } // if isSelectionEmpty

        table.repaint();

    } // doSort

    //------------------------------------------------------------------------------
/**
 * if any portion of the subtable specified by the arugments is present in
 * the target table (currently on top in the browser), then clear any
 * existing selection and select the intersecting subtable.  if there
 * is no intersection between the requested subtable and the current table,
 * simply return.
 */
    public void selectSubTable(String[] rowNames, String[] columnNames) {
        clearSelection();
        disableAllColumns();

        if (!columnNamesIntersect(columnNames))
            return;

        if (!rowNamesIntersect(rowNames))
            return;

        select(matrix.getSpecies(), rowNames);
        disableAllColumns();
        enableColumnsByName(columnNames);

    } // selectSubTable

    //------------------------------------------------------------------------------
    public void disableAllColumns() {
        String[] allTitles = (String[]) columnCheckBoxHeaderMap.keySet().toArray(new String[0]);

        for (int i = 0; i < allTitles.length; i++) {
            CheckBoxHeader cbh = (CheckBoxHeader) columnCheckBoxHeaderMap.get(allTitles[i]);
            cbh.setSelected(false);
        }

        table.getTableHeader().resizeAndRepaint();

    } // disableAllColumns

    //------------------------------------------------------------------------------
    public void enableAllColumns() {
        String[] allTitles = (String[]) columnCheckBoxHeaderMap.keySet().toArray(new String[0]);

        for (int i = 0; i < allTitles.length; i++) {
            CheckBoxHeader cbh = (CheckBoxHeader) columnCheckBoxHeaderMap.get(allTitles[i]);
            cbh.setSelected(true);
        }

        table.getTableHeader().resizeAndRepaint();

    } // enableAllColumns

    //------------------------------------------------------------------------------
    protected void enableColumnsByName(String[] enabledColumnNames) {
        for (int i = 0; i < enabledColumnNames.length; i++) {
            String name = enabledColumnNames[i];
            boolean recognized = columnCheckBoxHeaderMap.containsKey(name);
            if (recognized) {
                CheckBoxHeader cbh = (CheckBoxHeader) columnCheckBoxHeaderMap.get(name);
                cbh.setSelected(true);
            } // if recognized
        } // for i

        table.getTableHeader().resizeAndRepaint();

    } // enableColumnsByName

    //------------------------------------------------------------------------------
    protected boolean columnNamesIntersect(String[] candidateColumnNames) {
        //String [] allTitles = (String []) columnCheckBoxHeaderMap.keySet().toArray (new String [0]);

        for (int i = 0; i < candidateColumnNames.length; i++) {
            String name = candidateColumnNames[i];
            if (columnCheckBoxHeaderMap.containsKey(name))
                return true;
        } // for i

        return false;

    } // columnNamesIntersect

    //------------------------------------------------------------------------------
    protected boolean rowNamesIntersect(String[] candidateRowNames) {
        java.util.List listOfNamesToSelect = Arrays.asList(candidateRowNames);

        String[] namesInTable = matrix.getRowTitles();
        for (int i = 0; i < namesInTable.length; i++)
            if (listOfNamesToSelect.contains(namesInTable[i]))
                return true;

        return false;

    } // rowNamesIntersect

    //------------------------------------------------------------------------------
    private void debugCheckBoxHeaders(String msg) {
        Enumeration enumeration = table.getColumnModel().getColumns();
        //System.out.println ("---- debugging CheckBoxHeaders: " + msg);

        while (enumeration.hasMoreElements()) {
            TableColumn aColumn = (TableColumn) enumeration.nextElement();
            //System.out.println (aColumn.getHeaderRenderer ());
        }

    } // debugCheckBoxHeaders

    //------------------------------------------------------------------------------
    public void addRow(String rowLabel, double[] values) {
        lens.addRow(rowLabel, values);
        tableModel.addRow(rowLabel, values);
        rowHeader.add(rowLabel);
        setupCheckBoxColumnHeaders();
        System.out.println("about to recalc column width, use data? " + calculateByDataWidth);
        recalculateColumnWidth();

    } // addRow

    //------------------------------------------------------------------------------
    protected void setupCheckBoxColumnHeaders() {
        Enumeration enumeration = table.getColumnModel().getColumns();
        JTableHeader header = table.getTableHeader();
        int columnCount = 0;

        while (enumeration.hasMoreElements()) {
            TableColumn aColumn = (TableColumn) enumeration.nextElement();
            String columnTitle = matrix.getColumnTitles()[columnCount];
            if (columnTitle == null) columnTitle = "";

            // create and set up renderer for column header
            CheckBoxHeader checkBoxHeader = new CheckBoxHeader(new TableHeaderCheckboxListener());
            checkBoxHeader.setToolTipText(constructToolTip(columnTitle));
            checkBoxHeader.setColumn(columnCount);
            checkBoxHeader.setText(columnTitle);
            checkBoxHeader.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            if (header != null) {
                checkBoxHeader.setForeground(header.getForeground());
                checkBoxHeader.setBackground(header.getBackground());
                checkBoxHeader.setFont(header.getFont());
            }

            columnCheckBoxHeaderMap.put(columnTitle, checkBoxHeader);
            aColumn.setHeaderRenderer(checkBoxHeader);
            columnCount++;
        }

        // add single listener for all check box headers
        header.addMouseListener(new TableHeaderMouseListener());
    } // setupCheckBoxColumnHeaders

    //------------------------------------------------------------------------------
/**
 * responds to mouse clicks in the table header by artificially "clicking"
 * the header checkboxes.
 */
class TableHeaderMouseListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 1) {
                JTableHeader header = (JTableHeader) (e.getSource());
                JTable table = header.getTable();
                TableColumnModel columnModel = table.getColumnModel();
                int columnIndex = columnModel.getColumnIndexAtX(e.getX());
                if (columnIndex > -1) {
                    TableColumn column = columnModel.getColumn(columnIndex);
                    CheckBoxHeader cbh = (CheckBoxHeader) column.getHeaderRenderer();
                    cbh.doClick();
                    ((JTableHeader) e.getSource()).repaint(); //Header doesn't repaint itself properly
                }
            }
        }
    }

    //------------------------------------------------------------------------------
    protected String constructToolTip(String columnTitle) {
        StringBuffer sb = new StringBuffer();
        sb.append("<html><b>" + columnTitle + "</b>");

        /*
        MetaData md;

        // todo - fix it so this doesn't happen
        try {
            md = navigator.getMetaDataForCondition(columnTitle);
        } catch (NullPointerException e) {
            sb.append("<br>No Metadata Available");
            sb.append("</html>");
            return sb.toString();
        }

        if (md == null || md.getConditions() == null) {
            sb.append("<br>No Metadata Available");
            sb.append("</html>");
            return sb.toString();
        }

        for (Condition cond : md.getConditions()) {
            if (cond.getAlias().equals(columnTitle)) {
                String[] variableNames = cond.getVariableNames();
                //sb.append ("<ul>");
                for (int v = 0; v < variableNames.length; v++) {
                    String name = variableNames[v];
                    sb.append("<br>" + name);
                    Variable var = cond.getVariable(name);
                    sb.append(": ");
                    sb.append(var.getValue());
                    String units = var.getUnits();
                    if (units != null) {
                        sb.append(" ");
                        sb.append(units);
                    }
                }

            }
        }

        */
        sb.append("</html>");

        return sb.toString();

    } // constructToolTip


    //------------------------------------------------------------------------------
    private Point calculateCenter(Component component) {
        GraphicsConfiguration gc = getGraphicsConfiguration();
        int screenHeight = (int) gc.getBounds().getHeight();
        int screenWidth = (int) gc.getBounds().getWidth();
        int windowWidth = component.getWidth();
        int windowHeight = component.getHeight();
        return new Point((screenWidth - windowWidth) / 2, (screenHeight - windowHeight) / 2);

    } // placeInCenter

    //------------------------------------------------------------------------
    public void save(File directory) {
        if (matrix == null)
            return;

        try {
            String correctedName = name.replaceAll(" ", "");
            String filename = new File(directory, correctedName).getAbsolutePath();
            matrix.writeObject(filename);
        }
        catch (Exception ex0) {
            ex0.printStackTrace();
        }
    } // save
//------------------------------------------------------------------------
} // MatrixSpreadsheet

