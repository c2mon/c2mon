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
package cern.c2mon.server.cache.loading;

import cern.c2mon.shared.common.Cacheable;

/**
 * Interface used during reconfiguration of the cache.
 * 
 * @param <T> the cache object type
 * 
 * @author Mark Brightwell
 *
 */
public interface ConfigurableDAO<T extends Cacheable> {
  
  /**
   * Delete this cache object from the cache DB.
   * @param id the cache object unique id
   */
  void deleteItem(Long id);
  
  /**
   * Saves the configuration fields of the cache object to the database.
   * Is used when a DataTag or other object has been reconfigured (UPDATE 
   * reconfiguration).
   * 
   * @param cacheable the tag object where the configuraton fields have
   * been updated
   */
  void updateConfig(T cacheable);
  
  /**
   * Inserts this cache object into the DB.
   * @param cacheable the object to insert
   */
  void insert(T cacheable);
  
}
