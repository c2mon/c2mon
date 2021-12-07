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
package cern.c2mon.server.cache.common;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.ehcache.CacheException;
import cern.c2mon.server.ehcache.Ehcache;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import java.io.Serializable;
import java.util.List;

import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.jmx.export.annotation.ManagedOperation;

/**
 * Provides all core functionalities that are required to manage a cache. This
 * class uses internally Ehcache.
 *
 * @author Mark Brightwell
 * @author Justin Lewis Salmon
 *
 * @param <K> The key class type
 * @param <T> The value class type
 */
@Slf4j
public abstract class BasicCache<K, T extends Serializable> extends ApplicationObjectSupport {

  /**
   * Reference to the wrapped Ehcache.
   */
  protected Ehcache<K, T> cache;

  /**
   * Length of time (in milliseconds) to wait for a lock to be acquired.
   */
  private int lockTimeout = 500;

  /**
   * Number of times to attempt to lock a key before reporting that a deadlock
   * has possibly occurred.
   */
  private int lockAttemptThreshold = 60;

  /**
   * An inexpensive check to see if the key exists in the cache.
   *
   * <p>
   * Note: this method will block if the key to be checked is locked by another
   * thread.
   * </p>
   *
   * @param id The key to check for
   * @return <code>true</code> if an Element matching the key is found in the
   *         cache. No assertions are made about the state of the Element.
   * @see Ehcache#isKeyInCache(Object)
   * @throws NullPointerException In case a null pointer is passed as key
   */
  public final boolean hasKey(final K id) {
    if (id == null) {
      throw new NullPointerException("Querying cache with a null key.");
    }

    return cache.isKeyInCache(id);
  }

  /**
   * Get a Reference to the object of type T in the cache.
   *
   * <p>
   * Throws the following unchecked exceptions:
   * <li> {@link IllegalArgumentException} if called with a null key
   * <li> {@link CacheElementNotFoundException} if the object was not found in
   * the cache
   * <li> {@link RuntimeException} if an error occurs when accessing the cache
   * object in Ehcache.
   * <p>
   * If not sure whether an element is in the cache, first use the hasKey(Long)
   * method.
   *
   * <p>
   * Notice that since this method returns a reference, the object may need
   * locking to stay consistent (if several field are read for instance). For
   * this, the provided read (or write) lock should be used. In the distributed
   * cache mode, the class in which the locking is performed must be
   * instrumented in the Terracotta configuration file for the locking to be
   * effective across server nodes. For this reason, it is generally preferable
   * to use the provided getCopy method, which returns a clone of the cache
   * object.
   *
   * <p>
   * Notice this method does not go to the DB to find a cache element. To
   * explicitly load an element from the DB use the loadFromDb(Long id) method
   * below.
   *
   * @param id the id (key) of the cache element
   * @return a reference to the object stored in the cache
   */
  @SuppressWarnings("unchecked")
  public final T get(final K id) {
    T result = null;
    if (id != null) {

      acquireReadLockOnKey(id);
      try {
        result = cache.get(id);
        if (result == null) {
          throw new CacheElementNotFoundException("Failed to locate cache element with id " + id + " (Cache is " + this.getClass() + ")");
        }
      } catch (CacheException cacheException) {
        log.error("getReference() - Caught cache exception thrown by Ehcache while accessing object with id " + id, cacheException);
        throw new RuntimeException("An error occured when accessing the cache object with id " + id, cacheException);
      } finally {
        releaseReadLockOnKey(id);
      }
    } else {
      log.error("getReference() - Trying to access cache with a NULL key - throwing an exception!");
      // TODO throw runtime exception here or not?
      throw new IllegalArgumentException("Accessing cache with null key!");
    }

    return result;
  }

  /**
   * Returns the list of all keys in the cache. Only Longs can be inserted as
   * keys in C2monCache.
   *
   * @return list of keys
   */
  @SuppressWarnings("unchecked")
  public List<K> getKeys() {
    return cache.getKeys();
  }

  public void put(K key, T value) {
    cache.put(key, value);
  }

  /**
   * Remove an object from the cache.
   *
   * @param id the key of the cache element
   * @return true if successful
   */
  @ManagedOperation(description = "Manually remove a given object from the cache (will need re-loading manually from DB)")
  public boolean remove(K id) {
    return cache.remove(id);
  }

  /**
   * @return the cache
   */
  public Ehcache getCache() {
    return cache;
  }

  @PreDestroy
  public void shutdown() {
    log.debug("Closing cache (" + this.getClass() + ")");
  }

  public void acquireReadLockOnKey(K id) {
    if (id == null) {
      log.error("Trying to acquire read lock with a NULL key - throwing an exception!");
      throw new IllegalArgumentException("Acquiring read lock with null key!");
    }

    if (log.isTraceEnabled()) {
      log.trace(cache.getName() + " Acquiring READ lock for id=" + String.valueOf(id));
    }

    cache.acquireReadLockOnKey(id);

    if (log.isTraceEnabled()) {
      log.trace(cache.getName() + " Got READ lock for id=" + String.valueOf(id));
    }
  }

  public void releaseReadLockOnKey(K id) {
    if (id != null) {
      cache.releaseReadLockOnKey(id);

      if (log.isTraceEnabled()) {
        log.trace(cache.getName() + " Released READ lock for id=" + String.valueOf(id));
      }

    } else {
      log.error("Trying to release read lock with a NULL key - throwing an exception!");
      throw new IllegalArgumentException("Trying to release read lock with null key!");
    }
  }

  /**
   * Acquires the proper write lock for a given cache key
   * @param id The key that retrieves a value that you want to protect via locking
   */
  public void acquireWriteLockOnKey(K id) {
    if (id == null) {
      log.error("Trying to acquire write lock with a NULL key - throwing an exception!");
      throw new IllegalArgumentException("Acquiring write lock with null key!");
    }

    if (log.isTraceEnabled()) {
      log.trace(cache.getName() + " Acquiring WRITE lock for id=" + String.valueOf(id));
    }

    cache.acquireWriteLockOnKey(id);

    if (log.isTraceEnabled()) {
      log.trace(cache.getName() + " Got WRITE lock for id=" + String.valueOf(id));
    }
  }

  /**
   * Release a held write lock for the passed in key
   * @param id The key that retrieves a value that you want to protect via locking
   */
  public void releaseWriteLockOnKey(K id) {
    if (id != null) {
      cache.releaseWriteLockOnKey(id);

      if (log.isTraceEnabled()) {
        log.trace(cache.getName() + " Released WRITE lock for id=" + String.valueOf(id));
      }

    } else {
      log.error("Trying to release write lock with a NULL key - throwing an exception!");
      throw new IllegalArgumentException("Trying to release write lock with null key!");
    }
  }

  public boolean isWriteLockedByCurrentThread(K id) {
    return cache.isWriteLockedByCurrentThread(id);
  }

  public boolean isReadLockedByCurrentThread(K id) {
    return cache.isReadLockedByCurrentThread(id);
  }

  /**
   * Try to get a read lock on a given key.
   * If can't get it in timeout millis then return a boolean telling that it didn't get the lock
   *
   * @param id The key that retrieves a value that you want to protect via locking
   * @param timeout millis until giveup on getting the lock
   * @return whether the lock was awarded
   */
  public boolean tryReadLockOnKey(K id, Long timeout) {
    try {
      return cache.tryReadLockOnKey(id, timeout);
    } catch (InterruptedException e) {
      log.debug("Thread interrupted for id=" + String.valueOf(id) + " (" + this.getClass() + ")");
      return false;
    }
  }

  /**
   * Try to get a write lock on a given key.
   * If can't get it in timeout millis then return a boolean telling that it didn't get the lock
   * @param id The key that retrieves a value that you want to protect via locking
   * @param timeout millis until giveup on getting the lock
   * @return whether the lock was awarded
   */
  public boolean tryWriteLockOnKey(K id, Long timeout) {
    try {
      return cache.tryWriteLockOnKey(id, timeout);
    } catch (InterruptedException e) {
      log.debug("Thread interrupted for id=" + String.valueOf(id) + " (" + this.getClass() + ")");
      return false;
    }
  }
}
