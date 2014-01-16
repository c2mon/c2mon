/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2010 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.common.datatag;

import java.sql.Timestamp;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.datatag.DataTagAddress;

/**
 * Interface giving access to the most important attributes of a DataTag.
 * 
 * <p>It only provides read methods as in general this object should only be modified
 * by the cache modules (with the object residing in the cache).
 */

public interface DataTag extends Tag, Cacheable {

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

  DataTagAddress getAddress();

  Comparable getMinValue();

  Comparable getMaxValue();

  /**
   * Returns the unique Process id to which a DataTag is attached.
   * @return
   */
  Long getProcessId();

  
}
