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
 * Interface that must be implemented by all
 * cache loader DAOs; this is used for lazy
 * loading or when cache eviction is used.
 * 
 * @author Mark Brightwell
 * 
 * @param <T> the object type kept in the cache
 *
 */
public interface SimpleCacheLoaderDAO<T extends Cacheable> {
  
  /**
   * Fetch the cache element in the DB.
   * 
   * @param id the id of the cache object
   * @return the cache object
   */
  T getItem(Object id);
  
  /**
   * Checks if the current item can be found in the DB.
   * @param id key of the cache element
   * @return true if can be located
   */
  boolean isInDb(Long id);
  
}
