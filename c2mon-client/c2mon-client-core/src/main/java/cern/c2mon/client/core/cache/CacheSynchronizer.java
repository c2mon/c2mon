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
package cern.c2mon.client.core.cache;

import java.util.Set;
import cern.c2mon.client.common.listener.BaseTagListener;
/**
 * The <code>CacheSynchronizer</code> interface is used by the {@link ClientDataTagCache}
 * implementation for creating, removing or refreshing the <code>ClientDataTag</code>
 * references in the cache.
 *
 * @author Matthias Braeger
 */
interface CacheSynchronizer {

  /**
   * Checks if all tags in the list are present in the cache. Otherwise it will fetch
   * it from the server.
   * <p>
   *
   * @param tagIds The ids of the <code>ClientDataTag</code> objects that shall be
   *                      added to the cache.
   * @see ClientDataTagCache#subscribe(Set, BaseTagListener)
   * @return id list of all tags, which were newly created.
   * @throws CacheSynchronizationException In case of communication problems with
   *         the C2MON server during the tag creation.
   */
  Set<Long> initTags(final Set<Long> tagIds) throws CacheSynchronizationException;

  /**
   * Checks if all tags known by the server and matching one or more of the given regular
   * expressions are present in the local cache. Otherwise it will fetch
   * it from the server.
   * 
   * @param regexList List of wildcard expression to fetch all tags where the 
   *                  tag name is matching into one of the expressions.
   * @param allMatchingTags A list of all tag ids in the cache, where the name is matching to
   *         one or more of the given wildcard expressions.
   * @return id list of all tags, which were newly created.
   * @throws CacheSynchronizationException In case of communication problems with
   *         the C2MON server during the tag creation.
   */
  Set<Long> initTags(final Set<String> regexList, Set<Long> allMatchingTags) throws CacheSynchronizationException;
  
  /**
   * This method handles the subscription of the <code>ClientDataTag</code> to
   * the <code>JmsProxy</code> and <code>SupervisionManager</code>.
   *
   * @param tagIds The ids of the <code>ClientDataTag</code> objects that shall
   *          be subscribed to.
   */
  void subscribeTags(final Set<Long> tagIds);

  /**
   * Removes all <code>ClientDataTag</code> references
   * with the given id from the cache. At the same time it unsubscribes the
   * live tags from the <code>JmsProxy</code> where they were formerly
   * registered as <code>ServerUpdateListener</code> by the <code>TagServiceImpl</code>.
   *
   * @param tagIds list of <code>ClientDataTag</code> id's
   * @throws NullPointerException When the parameter is <code>null</code>
   */
  void removeTags(Set<Long> tagIds);

  /**
   * Synchronizes the live cache with the C2MON server. At the same time the
   * supervision status cache is also refreshed.
   * @param tagIds Set of tag id's that shall be refreshed. If the parameter
   *        is <code>null</code>, the entire cache is updated.
   * @throws CacheSynchronizationException In case of communication problems with
   *         the C2MON server during the cache refresh.
   */
  void refresh(Set<Long> tagIds) throws CacheSynchronizationException;
}
