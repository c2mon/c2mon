/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.shared.util.buffer;

import java.util.Collection;
import java.util.EventObject;

/** A buffer pull event class. A PullEvent instance
 * is passed as parameter to the SynchroBufferListener pull method.
 * @author F.Calderini
 */
public class PullEvent extends EventObject {
    private Collection pulled;
    
    /** Creates a new instance of PullEvent.
     * @param source the event source
     * @param pulled the pulled objects
     */
    public PullEvent(Object source, Collection pulled) {
        super(source);
        this.pulled = pulled;
    }
    
    /** Accessor method. 
     * @return the pulled objects
     */
    public Collection getPulled() {
        return pulled;
    }

    public String toString() 
    {
      StringBuffer buffer = new StringBuffer();
      buffer.append("[source=");
      buffer.append(getSource());
      buffer.append(", pulled=");
      buffer.append(getPulled());
      buffer.append("]");

      return buffer.toString();
    }
    
}

