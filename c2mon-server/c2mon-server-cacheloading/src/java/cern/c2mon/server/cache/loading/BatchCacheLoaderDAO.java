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

import java.util.Map;

import cern.c2mon.shared.common.Cacheable;

/**
 * DAO that must be provided for using the C2MON batch loading mechanism.
 * 
 * @author Mark Brightwell
 * @param <T> the type of cache object
 *
 */
public interface BatchCacheLoaderDAO<T extends Cacheable> extends SimpleCacheLoaderDAO<T> {

  /**
   * Returns the lowest Id of elements to be loaded. Returns 0 if no cache
   * objects are defined.
   * @return the lowest id
   */
  Long getMinId();
  
  /**
   * Returns the highest id of elements to be loaded. Returns 0 if no
   * cache objects are defined.
   * @return the highest id
   */
  Long getMaxId();
  
  /**
   * Return a map of elements that need loading. All elements with ids between
   * the provided numbers are returned.
   * 
   * <p>This method also performs post-DB-access logic to the object itself, using
   * the implemented doPostDbLoading method.
   * 
   * @param firstId fetches all elements with ids above or including this number
   * @param lastId fetches all elements with ids below or including this number
   * @return a map of objects ready to load into the cache
   */
  Map<Object, T> getBatchAsMap(Long firstId, Long lastId);
  
}
