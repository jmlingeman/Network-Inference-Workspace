// VectorDataProvider.java
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
 * Interface to sources of modifiable vector data.
 */
public interface VectorDataProvider extends ReadOnlyVectorDataProvider {
    
    int size();
    double getQuick(int index);
    /**
     * Sets the element at the specified index to the specified value without
     * range-checking.
     */
    void setQuick(int index, double value);
}

