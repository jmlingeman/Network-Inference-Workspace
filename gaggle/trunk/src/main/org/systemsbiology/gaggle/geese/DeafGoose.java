// DeafGoose.java
//-------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.geese;
//---------------------------------------------------------------------------------

import java.rmi.*;
import java.util.HashMap;
//-------------------------------------------------------------------------------------

// todo - remove this interface?
public interface DeafGoose extends Remote {
    // todo - at least remove (or fix implementations of) connectToGaggle method
    void connectToGaggle() throws Exception;

    void exit() throws RemoteException, java.rmi.UnmarshalException;
//-------------------------------------------------------------------------------------
}

