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
package cern.c2mon.server.cache;

import java.util.Collection;
import java.util.List;

import cern.c2mon.server.common.tag.Tag;

/**
 * Implemented by caches with listeners/modules relying on invalidation notifications due
 * to supervision changes. Currently this corresponds to Tag caches (datatags,
 * rules and controltags).
 *
 * @author Mark Brightwell
 * @param <T> the type of the objects contained in this cache
 * @param <K> the type of the cache keys
 *
 */
public interface C2monCacheWithSupervision<K, T extends Tag> extends C2monCacheWithListeners<K, T> {


  /**
   * Register a listener to be notified of supervision invalidations/validation callbacks
   * due to Supervision changes for each affected Tag (including rules).
   *
   * <p>Notice the cache get(Long) method does not append this information
   * to the Tag (no change is made in the cache).
   *
   * <p>Listeners get notified of tag supervision status changes, but the Tag timestamps remain UNCHANGED.
   * Consequently, tags arriving with identical timetamps should be accepted by the listener, at least if
   * they have a Supervision invalidation set. The listener can publish the event with a new timestamp
   * if necessary (although it will then need to decide how to deal with an older incoming valid value).
   *
   * <p>However, these listeners SHOULD FILTER OUT UPDATES WITH OLDER SERVER TIMESTAMPS (by checking
   * the current value in the cache for instance), never accepting older updates. This must be done as
   * there is no guarantee that a "revalidation" supervision event will not be overtaken by a newer valid
   * update, which will have a more recent server timestamp (since the revalidation notification is
   * using the timestamp of the previous value).
   *
   * <p>Notice that if the supervision status changes twice in close succession, there is no.
   *
   * @param cacheSupervisionListener the listener to register
   */
  void registerListenerWithSupervision(CacheSupervisionListener< ? super T> cacheSupervisionListener);

  /**
   * Calls all listeners notified for supervision invalidation messages. These are
   * passed the tag as supplied to this method (supervision status needs to have
   * been applied beforehand.
   *
   * @param tag the tag affected by the supervision change, *with* the supervision
   *        status applied
   */
  void notifyListenersOfSupervisionChange(T tag);

  /**
   * Check whether the cache contains a tag with
   * the given tag name. The call is always case insensitive.
   * @param name name of the tag
   * @return <code>true</code>, if a tag with the given name exists.
   */
  boolean hasTagWithName(String name);

  /**
   * A {@link Tag} can also be retrieved with its unique name
   * that has to correspond to {@link Tag#getName()}. Please
   * note that the query is case insensitive.
   * @param name The unique name of a tag
   * @return The corresponding cache object or <code>null</code>, if
   *         the cache does not contain any tag with this name
   * @see #get(Object)
   * @see #findByNameWildcard(String)
   * @see Tag#getName()
   */
  T get(String name);

  /**
   * Searches for all {@link Tag} instances in the given cache, where
   * the {@link Tag#getName()} attribute matches the given regular
   * Expression.
   * <p>
   * A regular expression matcher. '?' and '*' may be used.
   * The search is always case insensitive.
   * <p>
   * WARN: Expressions starting with a leading wildcard character are
   * potentially very expensive (ie. full scan) for indexed caches
   *
   * @param regex The regular expression including '?' and '*'
   * @return All tags where the tag name is matching the regular expression.
   * Please note, that the result is limited to 100'000 in order to avoid a
   * OutOfMemory exception!
   * @see net.sf.ehcache.search.expression.ILike
   * @see #get(String)
   */
  Collection<T> findByNameWildcard(String regex);

  /**
   * @return list of tags with expressions that are currently {@literal true}
   * and are marked as alarms
   */
  List<T> getActiveAlarms();
}
