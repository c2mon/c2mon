/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.common.equipment;

import java.util.Collection;

import cern.c2mon.shared.common.Cacheable;

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
public interface Equipment extends AbstractEquipment, Cacheable {

  /**
   * Returns the live list of SubEquipment ids attached to this
   * Equipment; locking on Equipment level required if accessing
   * this.
   * @return list of ids
   */
  Collection<Long> getSubEquipmentIds();
  
  /**
   * Returns the live list of DataTag ids attached to this
   * Equipment; locking on Equipment level required if accessing
   * this.
   * @return list of DataTag ids
   */
  Collection<Long> getDataTagIds();

  /**
   * Returns the parent process id.
   * @return the Process id
   */
  Long getProcessId();

  /**
   * Returns the live list of CommandTag ids attached to this
   * Equipment; locking on Equipment level required if accessing
   * this.
   * @return list of CommandTag ids
   */
  Collection<Long> getCommandTagIds();
  

}
