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
package cern.c2mon.server.cache;

import java.util.List;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;

/**
 * The common interface for all caches used by the TIM server. Provides common methods for querying the
 * keys. Keys of all cache are of type Long. Also provides general methods for registering listeners to
 * this cache.
 * 
 * All accesses to the caches with a NULL key throw uncaught exceptions. These must be caught by the
 * developer if expected to happen.
 * 
 * @author Mark Brightwell
 * @param <K> cache key type
 * @param <T> cache element type
 *
 */
public interface C2monCache<K, T> {
  
  /**
   * Puts an object in the cache.
   * @param key the key of the cache object
   * @param value the value of the cache object
   */
  void put(K key, T value);
  
  /**
   * Returns the list of keys of all elements in the cache.
   * @return a list of cache id's
   */
  List<K> getKeys();
  
  /**
   * Checks if the given cache has an element with the passed key.
   * @param id the id to look for
   * @return true if the element can be located in the cache
   * @throws NullPointerException if passed a null key
   */
  boolean hasKey(K id);
  
  /**
   * Removes the elt from the cache, if one is found.
   * @param id the cache object key
   * @return true if an elt was removed, false if not
   */
  boolean remove(K id);

  /**
   * Get a Reference to the object of type T in the cache. 
   * 
   * <p>Throws the following unchecked exceptions:
   * <li> {@link IllegalArgumentException} if called with a null key
   * <li> {@link CacheElementNotFoundException} if the object was not found in the 
   *      cache
   * <li> {@link RuntimeException} if an error occurs when accessing the cache object
   *      in Ehcache.
   * 
   * <p>If not sure whether an element is in the cache, first use the hasKey(Long) 
   * method.
   * 
   * <p>Notice that since this method returns a reference, the object may need locking
   * to stay consistent (if several field are read for instance). For this, the provided
   * read (or write) lock should be used. In the distributed cache mode, the class in
   * which the locking is performed must be instrumented in the Terracotta configuration
   * file for the locking to be effective across server nodes. For this reason, it is
   * generally preferable to use the provided getCopy method, which returns a clone
   * of the cache object.
   * 
   * <p>Notice this method does not go to the DB to find a cache element. To explicitly
   * load an element from the DB use the loadFromDb(Long id) method below.
   * 
   * @param id the id (key) of the cache element
   * @return a reference to the object stored in the cache
   */
  T get(K id);
  
  /**
   * Returns a clone of the object of type T in the cache.
   * 
   * <p>Throws the following unchecked exceptions:
   * <li> {@link IllegalArgumentException} if called with a null key
   * <li> {@link CacheElementNotFoundException} if the object was not found in the 
   *      cache
   * <li> {@link RuntimeException} if an error occurs when accessing the cache object
   *      in Ehcache.
   * <li> {@link UnsupportedOperationException} if this operation is not available for
   *      the given cache (see the cache bean javadoc for this.)
   * 
   * <p>If not sure whether an element is in the cache, first use the hasKey(Long) 
   * method.
   * 
   * @param id the id of the object in the cache
   * @return a copy of the cache object
   */
  T getCopy(K id);

 

  /**
   * Puts the passed value into the cache quietly.
   * @param value the value to put in the cache
   */
  void putQuiet(T value);
 
  
  /**
   * Acquires a shared read lock on the cache object.
   * Will not throw an exception if called with null.
   * @param id key of the cache object
   */
  void acquireReadLockOnKey(K id);
  
  /**
   * Acquires an exclusive write lock on the cache object.
   * Will not throw an exception if called with null.
   * Can also be called on a non-existent key, before
   * inserting the element for instance.
   * 
   * @param id key of the cache object
   */
  void acquireWriteLockOnKey(K id);
  
  /**
   * Releases the held read lock.
   * @param id key of cache element
   */
  void releaseReadLockOnKey(K id);
 
  /**
   * Releases the held write lock.
   * @param id key of cache element
   */
  void releaseWriteLockOnKey(K id);

  /**
   * Determines whether or not the running thread is holding a read lock on a given
   * cache element.
   * @param id key of cache element
   * @return <code>true</code>, if the thread that is calling this method is
   * owns currently a read lock to this cache element. 
   */
  boolean isReadLockedByCurrentThread(K id);

  /**
   * Determines whether or not the running thread is holding a write lock on a given
   * cache element.
   * @param id key of cache element
   * @return <code>true</code>, if the thread that is calling this method is
   * owns currently a write lock to this cache element. 
   */
  boolean isWriteLockedByCurrentThread(K id);
  
}
