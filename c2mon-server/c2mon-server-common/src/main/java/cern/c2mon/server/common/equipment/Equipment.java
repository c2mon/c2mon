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

import java.util.Collection;
import java.util.List;

/**
 * Interface to Equipment objects residing in cache. The methods exposed
 * here are thread safe and can be used on objects residing in the cache.
 * Non-exposed methods in the implementation should not be accessed.
 * Modifications to objects in the cache should be made using the
 * EquipmentFacade bean.
 *
 * @author Mark Brightwell
 *
 */
public interface Equipment extends AbstractEquipment {

  /**
   * Returns the list of SubEquipment ids attached to this Equipment
   * @return list of ids
   */
  List<Long> getSubEquipmentIds();


  /**
   * Returns the parent process id.
   * @return the Process id
   */
  Long getProcessId();

  /**
   * Returns the list of CommandTag ids attached to this Equipment
   * @return list of CommandTag ids
   */
  Collection<Long> getCommandTagIds();
}
