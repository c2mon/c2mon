/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
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
package cern.c2mon.server.cache.tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import cern.c2mon.server.cache.C2monCacheWithSupervision;
import cern.c2mon.server.cache.CacheSupervisionListener;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.common.AbstractCache;
import cern.c2mon.server.cache.common.C2monCacheLoader;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.loader.CacheLoader;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Result;
import net.sf.ehcache.search.Results;

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
public abstract class AbstractTagCache<T extends Tag> extends AbstractCache<Long, T> implements C2monCacheWithSupervision<Long, T> {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(AbstractTagCache.class);
  
  /** The max result size should avoid to run into an OutOfMemory Exception when doing a wildcard search */
  private static final int MAX_RESULT_SIZE = 100000;
  
  /**
   * Synchronized list.
   */
  private final List<CacheSupervisionListener< ? super T>> listenersWithSupervision;
  private final ReentrantReadWriteLock listenerLock;
  
  /**
   * Constructor.
   */
  public AbstractTagCache(final ClusterCache clusterCache, 
                          final Ehcache ehcache,
                          final CacheLoader cacheLoader, 
                          final C2monCacheLoader c2monCacheLoader,
                          final SimpleCacheLoaderDAO<T> cacheLoaderDAO) {
    super(clusterCache, ehcache, cacheLoader, c2monCacheLoader, cacheLoaderDAO);    
    listenersWithSupervision = new ArrayList<CacheSupervisionListener< ? super T>>();
    listenerLock = new ReentrantReadWriteLock();
  }
  
  
  @Override
  public void notifyListenersOfSupervisionChange(final T tag) {
    //only notify if the current value in the cache is not more recent (a new update could have overraken the supervision notification)
    if (!this.getCopy(tag.getId()).getCacheTimestamp().after(tag.getCacheTimestamp())) {
      notifyListenersWithSupervision(tag);
    } else {
      LOGGER.info("Filtering out Tag supervison notification as newer value in cache - tag id is " + tag.getId());
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
    
    Results results = null;

    try {
      Ehcache ehcache = getCache();
      Attribute<String> tagName = ehcache.getSearchAttribute("tagName");

      Query query = ehcache.createQuery();
      results = query.includeKeys().addCriteria(tagName.ilike(name)).maxResults(1).execute();

      return results.hasKeys();
    }
    finally {
      if (results != null) {
        // Discard the results when done to free up cache resources.
        results.discard();
      }
    }
    
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
   * @see net.sf.ehcache.search.expression.ILike
   * @see #get(String)
   */
  private Collection<T> findByNameWildcard(String regex, int maxResults) {
    Results results = null;
    Collection<T> resultList = new ArrayList<>();

    if (regex == null || regex.equalsIgnoreCase("")) {
      throw new IllegalArgumentException("Attempting to retrieve a Tag from the cache with a NULL or empty name parameter.");
    }
    
    if (regex.equals("*")) {
      Map<Object, Element> allElements = getCache().getAll(getCache().getKeys());
      int counter = 0;
      for (Element element : allElements.values()) {
        resultList.add((T) element.getObjectValue());
        
        counter++;
        if (counter >= maxResults) {
          LOGGER.warn(String.format("findByNameWildcard() - Reached maximum result size %d when retrieving all (*) entries of cache %s", maxResults, getCacheName()));
          break;
        }
      }
    }
    else {
      try {
        Ehcache ehcache = getCache();
        Attribute<String> tagName = ehcache.getSearchAttribute("tagName");
  
        Query query = ehcache.createQuery();
        results = query.includeValues().addCriteria(tagName.ilike(regex)).maxResults(maxResults).execute();
  
        LOGGER.debug(String.format("findByNameWildcard() - Got %d results for regex \"%s\"", results.size(), regex));
        
        T value;
        for (Result result : results.all()) {
          value = (T) result.getValue();
          if (value != null) {
            resultList.add(value);
          }
          else {
            LOGGER.warn(String.format("findByNameWildcard() - Regex \"%s\" returned a null value for cache %s", regex, getCacheName()));
          }
        }
      }
      finally {
        if (results != null) {
          // Discard the results when done to free up cache resources.
          results.discard();
        }
      }
    }
    
    LOGGER.debug(String.format("findByNameWildcard() - Found %d (maxResultSize = %d) tags in %s cache where tag names are matching wildcard \"%s\"", resultList.size(), maxResults, getCacheName(), regex));
    
    return resultList;
  }
}
