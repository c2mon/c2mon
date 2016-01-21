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
package cern.c2mon.web.restapi.cache;

import cern.c2mon.client.common.tag.Tag;

/**
 * The {@link TagCache} is used to store the latest values of data
 * tags in order for them to be pulled RESTfully.
 *
 * @author Justin Lewis Salmon
 */
public interface TagCache {

  /**
   * Add a tag to the cache.
   *
   * @param tag the tag to add
   */
  void add(Tag tag);

  /**
   * Retrieve a tag from the cache.
   *
   * @param id the ID of the tag to retrieve
   * @return the requested tag, or null if it wasn't in the cache
   */
  Tag get(Long id);

  /**
   * Remove a tag from the cache.
   *
   * @param id the ID of the tag to be removed.
   */
  void remove(Long id);

  /**
   * Check if the cache contains a tag.
   *
   * @param id the ID of the tag to be checked
   * @return true if the cache contains the tag, false otherwise
   */
  boolean contains(Long id);
}
