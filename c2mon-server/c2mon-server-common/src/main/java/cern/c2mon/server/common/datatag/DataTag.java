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
package cern.c2mon.server.common.datatag;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.DataTagAddress;

import java.sql.Timestamp;

/**
 * Interface giving access to the most important attributes of a DataTag.
 *
 * <p>It only provides read methods as in general this object should only be modified
 * by the cache modules (with the object residing in the cache).
 */

public interface DataTag extends Tag {

  /**
   * Returns the timestamp of the value set at source.
   * @return the Timestamp set at the equipment level
   */
  Timestamp getSourceTimestamp();

  /**
   * The DAQ timestamp indicates when the value change message has been sent from the DAQ.
   * @return The DAQ timestamp
   */
  Timestamp getDaqTimestamp();

  Long getEquipmentId();

  Long getSubEquipmentId();

  DataTagAddress getAddress();

  Comparable getMinValue();

  Comparable getMaxValue();

  /**
   * Returns the unique Process id to which a DataTag is attached.
   * @return
   */
  Long getProcessId();


}
