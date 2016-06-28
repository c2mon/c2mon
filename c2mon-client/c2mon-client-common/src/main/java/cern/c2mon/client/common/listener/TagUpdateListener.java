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
package cern.c2mon.client.common.listener;

import cern.c2mon.shared.client.tag.TagValueUpdate;


/**
 * Must be implemented by objects that wish to register
 * as the listener for incoming updates for a given
 * ClientDataTag.
 * 
 * <p>Registration to live updates from the C2MON server
 * is achieved by calling the registration method on the
 * <code>JmsProxy</code>.
 * 
 * @author Mark Brightwell
 *
 */
public interface TagUpdateListener {
  
  /**
   * Called on incoming updates.
   * 
   * @param tagValueUpdate contains the value information
   *          used to update the <code>ClientDataTag</code> 
   *          in the client
   * @return 
   */
  boolean onUpdate(TagValueUpdate tagValueUpdate);
  
}
