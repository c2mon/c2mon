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
package cern.c2mon.shared.daq.config;

/**
 * Common interface implemented by all
 * DataTag and CommandTag change events.
 * 
 * <p>Note that in the server CommandTag
 * does implement the Tag interface, but
 * on the DAQ layer TagChange makes sense
 * in this case. 
 * 
 * @author Mark Brightwell
 *
 */
public interface ITagChange extends IChange {

  /**
   * Returns the id of the equipment this DataTag or
   * CommandTag is associated with.
   * @return the equipment id
   */
  long getEquipmentId();
  
  /**
   * Set the id of the equipment this tag is associated to.
   * @param equipmentId the equipment id
   */
  void setEquipmentId(final long equipmentId);
  
}
