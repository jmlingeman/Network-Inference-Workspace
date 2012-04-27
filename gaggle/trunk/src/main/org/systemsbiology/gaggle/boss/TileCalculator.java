// TileCalculator.java
// calculate the position and size of n windows tiled within the 
// the specified frame -- which is prototypically the computer screen
//-------------------------------------------------------------------------------------
// $Revision: 727 $  
// $Date: 2006-03-16 16:13:08 -0500 (Thu, 16 Mar 2006) $
// $Author: pshannon $
//-------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.boss;
//-------------------------------------------------------------------------------------
import java.lang.Math;
//-------------------------------------------------------------------------------------
public class TileCalculator {

  int windowWidth;
  int windowHeight;
  int numberOfWindows;
  int cellWidth;
  int cellHeight;
  int rowCount;
  int columnCount;
  int TITLE_BAR_HEIGHT_ALLOWANCE = 20;
  int CELL_SIDE_GAP = 10;
  int CELL_TOP_GAP = 10;
  int X_EDGE_GAP = 5;   // at left & right of the window
  int Y_EDGE_GAP = 5;   // at top & bottom of the window
  int YGap = 10;
  int XGap = 10;
//-------------------------------------------------------
public TileCalculator (int width, int height, int numberOfWindows)
{
  calculateGrid (numberOfWindows);
  this.windowWidth = width - ((1 + columnCount) * XGap);
  this.windowHeight = height - ((1 + rowCount) * YGap);
  calculateWindowSize ();
}
//-------------------------------------------------------
public int [] getGeometry (int windowNumber)
{
  int [] rowAndColumn = getRowAndColumn (windowNumber);
  int row = rowAndColumn [0];
  int column = rowAndColumn [1];
  int x = XGap + (column * (cellWidth + XGap));
  int y = YGap + (row * (cellHeight + YGap));
  int width = cellWidth - XGap;
  int height = cellHeight; //  - YGap;
  int [] result = new int [] {x, y, width, height};
  return result;
}
//-------------------------------------------------------
public int [] getRowAndColumn (int windowNumber)
{
    // windowNumber is zero-based

  int row = (new Integer (windowNumber/columnCount)).intValue ();
  int column = windowNumber - (row * columnCount);
  return new int [] {row, column};
}
//-------------------------------------------------------
public int getTitleBarHeight ()
{
  return TITLE_BAR_HEIGHT_ALLOWANCE;
}
//-------------------------------------------------------
public int getRowCount ()
{
  return rowCount;
}
//-------------------------------------------------------
public int getColumnCount ()
{
  return columnCount;
}
//-------------------------------------------------------
public int getCellWidth ()
{
  return cellWidth;
}
//-------------------------------------------------------
public int getCellHeight ()
{
  return cellHeight;
}
//-------------------------------------------------------
protected void calculateGrid (int numberOfWindows)
{
  int squareRoot = (int) (Math.sqrt (numberOfWindows));
  
  int rows = squareRoot;
  int cols = squareRoot;

  if ((rows * cols) < numberOfWindows)
    cols +=1;

  if ((rows * cols) < numberOfWindows)
   rows += 1;

  rowCount = rows;
  columnCount = cols;
 
}    
//-------------------------------------------------------
protected void calculateWindowSize ()
{
  cellWidth = (int) (windowWidth / columnCount);
  cellHeight = (int) (windowHeight / rowCount);
}
//-------------------------------------------------------------------------------------
} // class TileCalculator
