// DoubleArrayDataProvider.java
//------------------------------------------------------------------------------
// $Revision: 1.1 $
// $Date: 2002/07/18 00:08:28 $
// $Author: amarkiel $
//------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.experiment.gui.vectormath;
//------------------------------------------------------------------------------
/**
 * Data provider for arrays of doubles.
 */
public class DoubleArrayDataProvider implements VectorDataProvider {
    
    private double[] data;
    
    public DoubleArrayDataProvider(double[] d) {data = d;}
    
    public int size() {return data.length;}
    public double getQuick(int index) {return data[index];}
    public void setQuick(int index, double value) {data[index] = value;}
}

