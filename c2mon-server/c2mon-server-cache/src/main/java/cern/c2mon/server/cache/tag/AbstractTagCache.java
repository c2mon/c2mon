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
package cern.c2mon.server.cache.tag;

import cern.c2mon.server.cache.C2monCacheWithSupervision;
import cern.c2mon.server.cache.CacheSupervisionListener;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.config.CacheProperties;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import cern.c2mon.server.cache.tag.query.TagQuery;
import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.loader.CacheLoader;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Common methods used by all tag caches (data, control and rule tags).
 * Objects in these caches are {@link AbstractTagCacheObject}s and implement
 * the {@link Tag} interface.
 *
 * TODO still need to add listener lifecycle callback if add listeners on new threads (not available so far)
 *
 * @param <T> cache object type
 *
 * @author Mark Brightwell
 *
 */
@Slf4j
public abstract class AbstractTagCache<T extends Tag> extends AbstractCache<Long, T> implements C2monCacheWithSupervision<Long, T> {

  /** The max result size should avoid to run into an OutOfMemory Exception when doing a wildcard search */
  private static final int MAX_RESULT_SIZE = 100000;

  /**
   * Synchronized list.
   */
  private final List<CacheSupervisionListener< ? super T>> listenersWithSupervision;
  private final ReentrantReadWriteLock listenerLock;

  private final TagQuery<T> tagQuery;

  /**
   * Constructor.
   */
  public AbstractTagCache(final ClusterCache clusterCache,
                          final Ehcache ehcache,
                          final CacheLoader cacheLoader,
                          final C2monCacheLoader c2monCacheLoader,
                          final SimpleCacheLoaderDAO<T> cacheLoaderDAO,
                          final CacheProperties properties,
                          final TagQuery<T> tagQuery) {
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO, properties);
    listenersWithSupervision = new ArrayList<>();
    listenerLock = new ReentrantReadWriteLock();
    this.tagQuery = tagQuery;
  }


  @Override
  public void notifyListenersOfSupervisionChange(final T tag) {
    //only notify if the current value in the cache is not more recent (a new update could have overraken the supervision notification)
    if (!this.getCopy(tag.getId()).getCacheTimestamp().after(tag.getCacheTimestamp())) {
      notifyListenersWithSupervision(tag);
    } else {
      log.info("Filtering out Tag supervison notification as newer value in cache - tag id is " + tag.getId());
    }
  }

  /**
   * Private method for notifying all listeners registered for supervision changes
   * to Tags (i.e. they get a Tag update call on a supervision change also)
   *
   * @param tag Tag copy (so not in cache), should be invalidated with supervision changes if necessary
   */
  private void notifyListenersWithSupervision(final T tag) {
    listenerLock.readLock().lock();
    try {
      for (CacheSupervisionListener< ? super T> cacheListener : listenersWithSupervision) {
        cacheListener.onSupervisionChange(tag);
      }
    } finally {
      listenerLock.readLock().unlock();
    }
  }

  @Override
  public void registerListenerWithSupervision(CacheSupervisionListener< ? super T> timCacheListener) {
    listenerLock.writeLock().lock();
    try {
      listenersWithSupervision.add(timCacheListener);
    } finally {
      listenerLock.writeLock().unlock();
    }
  }

  @Override
  public boolean hasTagWithName(String name) {
    if (name == null || name.equalsIgnoreCase("")) {
      throw new IllegalArgumentException("Attempting to retrieve a Tag from the cache with a NULL or empty name parameter.");
    }

    // This will prevent wildcard searches
    if (name.contains("*")) {
      name = name.replace("*", "\\*");
    }
    if (name.contains("?")) {
      name = name.replace("?", "\\?");
    }

    List<T> tagsFound = tagQuery.findTagsByWildcard(name, 1);

    return !tagsFound.isEmpty();
  }

  @Override
  public T get(String name) {
    if (name == null || name.equalsIgnoreCase("")) {
      throw new IllegalArgumentException("Attempting to retrieve a Tag from the cache with a NULL or empty name parameter.");
    }

    // This will prevent wildcard searches
    if (name.contains("*")) {
      name = name.replace("*", "\\*");
    }
    if (name.contains("?")) {
      name = name.replace("?", "\\?");
    }

    Collection<T> results = findByNameWildcard(name, 1);
    for (T tag : results) {
      return tag;
    }

    return null;
  }

  @Override
  public Collection<T> findByNameWildcard(String regex) {
    return findByNameWildcard(regex, MAX_RESULT_SIZE);
  }

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
   * @param maxResults the maximum amount of results that shall be returned
   * @return All tags where the tag name is matching the regular expression.
   * Please note, that the result is limited by {@code maxResults}
   * @see
   * @see #get(String)
   */
  private Collection<T> findByNameWildcard(String regex, int maxResults) {
    Collection<T> resultList = new ArrayList<>();

    if (regex == null || regex.equalsIgnoreCase("")) {
      throw new IllegalArgumentException("Attempting to retrieve a Tag from the cache with a NULL or empty name parameter.");
    }

    if (regex.equals("*")) {
      int counter = 0;
      for (Long  key : getKeys()) {
        resultList.add(get(key));

        counter++;
        if (counter >= maxResults) {
          log.warn(String.format("findByNameWildcard() - Reached maximum result size %d when retrieving all (*) entries of cache %s", maxResults, getCacheName()));
          break;
        }
      }
    }
    else {
      resultList = tagQuery.findTagsByWildcard(regex, 1);
    }

    log.debug(String.format("findByNameWildcard() - Found %d (maxResultSize = %d) tags in %s cache where tag names are matching wildcard \"%s\"", resultList.size(), maxResults, getCacheName(), regex));

    return resultList;
  }
}
