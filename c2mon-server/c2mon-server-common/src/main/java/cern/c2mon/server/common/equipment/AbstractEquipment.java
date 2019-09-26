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
package cern.c2mon.server.common.equipment;

import cern.c2mon.server.common.supervision.Supervised;

/**
 * Interface implemented by {@link AbstractEquipmentCacheObject}
 * implementing the common methods for Equipment and SubEquipment.
 *
 * @author Mark Brightwell
 *
 */
public interface AbstractEquipment extends Supervised {

  /**
   * Returns the (Sub)Equipment name.
   * @return the name
   */
  String getName();

  /**
   * Returns the id of the associated Communication Fault Tag.
   * @return the id of the CommFaultTag
   */
  Long getCommFaultTagId();

}
