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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import cern.c2mon.server.cache.config.CacheProperties;
import cern.c2mon.server.cache.loading.common.C2monCacheLoader;
import lombok.extern.slf4j.Slf4j;
import cern.c2mon.server.cache.ComparableCacheListener;
import cern.c2mon.server.cache.listener.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.loader.CacheLoader;
import org.springframework.jmx.export.annotation.ManagedOperation;

import cern.c2mon.server.cache.C2monBufferedCacheListener;
import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.SimpleCacheLoaderDAO;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.shared.common.Cacheable;

/**
 * Abstract cache that all the other C2MON caches extend.
 *
 * Provides the common access methods to the underlying Ehcache. Attempting to
 * retrieve a cache element with a null key will throw an {@link IllegalArgumentException}
 * that must be caught by the caller if necessary.
 *
 * <p>In general, cache beans should be initialised from the DB only. If more complex
 * logic needs performing, this should be done in the corresponding Facade init()
 * method, or if necessary in a separate bean that accesses the facade.
 *
 * @param <K> the cache key type
 * @param <T> the cache object type
 *
 * @author Mark Brightwell
 *
 */
@Slf4j
public abstract class AbstractCache<K, T extends Cacheable> extends BasicCache<K, T> {

  private final CacheProperties properties;

  /**
   * Contains properties distributed across the server cluster.
   */
  private final ClusterCache clusterCache;

  /**
   * Reference to the Ehcache loader.
   */
  private final CacheLoader cacheLoader;

  /**
   * Reference to cache loader.
   */
  private final SimpleCacheLoaderDAO<T> cacheLoaderDAO;

  /**
   * Reference to C2MON loader mechanism.
   */
  private final C2monCacheLoader c2monCacheLoader;

  /**
   * Reference to the Ehcache event listeners
   */
  private LinkedBlockingDeque<C2monCacheListener< ? super T>> cacheListeners = new LinkedBlockingDeque<>();

  private LinkedBlockingDeque<ComparableCacheListener< ? super T>> compareCacheListeners = new LinkedBlockingDeque<>();

  /**
   * the RegisteredEventListeners instance for this cache which is used
   * to register and unregister listeners.
   */
  private RegisteredEventListeners registeredEventListeners; //only for monitoring

  public AbstractCache(final ClusterCache clusterCache,
                       final Ehcache cache,
                       final CacheLoader cacheLoader,
                       final C2monCacheLoader c2monCacheLoader,
                       final SimpleCacheLoaderDAO<T> cacheLoaderDAO,
                       final CacheProperties properties) {
    super();
    this.clusterCache = clusterCache;
    this.cache = cache;
    this.cacheLoader = cacheLoader;
    this.c2monCacheLoader = c2monCacheLoader;
    this.cacheLoaderDAO = cacheLoaderDAO;
    this.properties = properties;
  }

  /**
   * In the case where the application had to access the DB to retrieve this cache
   * object (during a get(..) call for instance), this method performs any required
   * changes to the cache object after loading from the DB, before it is returned to
   * the caller. Should be used for logic that needs to access multiple cache objects
   * in various caches on which this cache depends (more simple logic can be put in
   * the cache object itself).
   *
   * <p>Is called before the object is put in the cache (no locking needed).
   *
   * @param cacheObject the object that should be modified
   */
  protected abstract void doPostDbLoading(T cacheObject);

  /**
   * Returns the C2MON cache name of this cache.
   * @return the name of the cache
   */
  protected abstract C2monCacheName getCacheName();

  /**
   * Key used in cluster cache for indicating if cache is initialized.
   * @return
   */
  protected abstract String getCacheInitializedKey();

  /**
   * Common cache initialization procedure.
   */
  protected void commonInit() {

    //register the cache loader with the Ehcache
    cache.registerCacheLoader(cacheLoader);
    registeredEventListeners = cache.getCacheEventNotificationService();

    //if in single cache mode, clear the disk cache before reloading
    //(skipCacheLoading can be set to override this and use the disk store instead of DB loading)
    if (!properties.isSkipPreloading() && properties.getMode().equalsIgnoreCase("single")) {
        cache.removeAll();
    }

    //preload the cache
    //flag recording if THIS server should load THIS cache
    boolean loadCache = false;

    //lock cacheStatus while setting that this server will perform the preload
    getClusterCache().acquireWriteLockOnKey(getCacheInitializedKey());
    try {
      if (!properties.isSkipPreloading()
          && (!getClusterCache().hasKey(getCacheInitializedKey()) || getClusterCache().getCopy(getCacheInitializedKey()).equals(Boolean.FALSE))) {
        //record that the preload will be done by this server and set loading flag to TRUE
        loadCache = true;
        getClusterCache().put(getCacheInitializedKey(), Boolean.TRUE);
      }
    } finally {
      getClusterCache().releaseWriteLockOnKey(getCacheInitializedKey());
    }

    if (loadCache) {
      log.info("Preloading cache from DB: " + getCacheName());
      getC2monCacheLoader().preload();
    } else {
      log.info("No preloading necessary: " + getCacheName());
    }
  }

  /**
   * Find an object in the cache given the object id and create a deep copy.
   * The copy is relealized through serialization and NOT by cloning.
   *
   * @param id the unique id of the cache object (should not be NULL)
   * @return Copy to the {@link Cacheable} object
   * @throws CacheElementNotFoundException if the element if not found in the cache
   * @throws IllegalArgumentException if the cache is accessed with a null key
   * @throws UnsupportedOperationException If something goes wrong whilst creating a deep clone
   *         through serialization
   */
  @SuppressWarnings("unchecked")
  public final T getCopy(final K id) {
    if (id != null) {
      cache.acquireReadLockOnKey(id);

      try {
        T reference = get(id);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(reference);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (T) ois.readObject();
      } catch (CacheElementNotFoundException cenfe) {
        throw cenfe;
      }
      catch (Exception ex) {
        log.error("Unable to get a serialized copy of the cache element as serialization is not supported for this object.", ex);
        throw new UnsupportedOperationException("The getCopy() method is not supported for this cache element since the cache object is not entirely serializable. Please revisit your object.", ex);
      } finally {
        cache.releaseReadLockOnKey(id);
      }
    }
    else {
      log.error("getCopy() - Trying to access cache with a NULL key - throwing an exception!");
      //TODO throw runtime exception here or not?
      throw new IllegalArgumentException("Accessing cache with null key!");
    }
  }

  /**
   * Puts an object in the cache, without notifying the cache listeners.
   * Wraps the call to the underlying Ehcache.
   */
  public void putQuiet(T value) {
    cache.putQuiet(new Element(value.getId(), value));
  }

  /**
   * Put a new object in the cache, notifying the listeners.
   * The value object itself is put into the cache (not a copy).
   *
   * <p> Call this method only if the the value was already insert with the
   * {@link #putQuiet(Cacheable)} method.
   * @param key
   * @param value
   */
  @Override
  public void put(K key, T value) {
    T originalValue = getCopy(key);
    super.put(key, value);
    notifyCompareListenersOfUpdate(originalValue, value);
    notifyListenersOfUpdate(value);
  }

  public void notifyListenersOfUpdate(final K id) {
    notifyListenersOfUpdate(this.getCopy(id));
  }

  /**
   * Creates a new cache element and calls the Ehcache notifyElementUpdated
   * method.
   *
   * Notifies the listeners that an update occurred for this DataTag. Should
   * be called *within a lock on the cache object* so the object is not modified
   * before being passed to the listeners (using a clone).
   *
   * @param cacheable the cache object that has been updated
   */
  public void notifyListenersOfUpdate(final T cacheable) {
    registeredEventListeners.notifyElementUpdated(new Element(cacheable.getId(), null), false); //only for monitoring via Ehcache: not using Ehcache listeners o.w.
    try {
      @SuppressWarnings("unchecked")
      T cloned = (T) cacheable.clone();
      for (C2monCacheListener< ? super T> listener : cacheListeners) {
        listener.notifyElementUpdated(cloned);
      }
    } catch (CloneNotSupportedException e) {
      log.error("CloneNotSupportedException caught while cloning a cache element - this should never happen!", e);
      throw new RuntimeException("CloneNotSupportedException caught while cloning a cache element - this should never happen!", e);
    }
  }

  /**
   * Gets called after a new cache element was put into the cache.
   *
   * Notifies the listeners that an update occurred for a cache object.
   * The listener gets the information of the former cache object and the new
   * cache object.
   *
   * @param original the cache object which was in the cache before the new one
   * @param updated the cache object which was put into the cache
   */
  public void notifyCompareListenersOfUpdate(final T original, final T updated) {
    try {
      T clonedUpdated = (T) updated.clone();
      if (original != null) {
        for (ComparableCacheListener<? super T> listener : compareCacheListeners) {
          listener.notifyElementUpdated(original, clonedUpdated);
        }
      }
    } catch (CloneNotSupportedException e) {
      log.error("CloneNotSupportedException caught while cloning a cache element - this should never happen!", e);
      throw new RuntimeException("CloneNotSupportedException caught while cloning a cache element - this should never happen!", e);
    }
  }

  public void notifyListenerStatusConfirmation(final T cacheable, final long timestamp) {
    try {
      @SuppressWarnings("unchecked")
      T cloned = (T) cacheable.clone();
      for (C2monCacheListener< ? super T> listener : cacheListeners) {
        listener.confirmStatus(cloned);
      }
    } catch (CloneNotSupportedException e) {
      log.error("CloneNotSupportedException caught while cloning a cache element - this should never happen!", e);
      throw new RuntimeException("CloneNotSupportedException caught while cloning a cache element - this should never happen!", e);
    }
  }

  public void registerSynchronousListener(C2monCacheListener<? super T> cacheListener) {
    cacheListeners.add(cacheListener);
  }

  public Lifecycle registerListener(C2monCacheListener<? super T> cacheListener) {
    CacheListener<? super T> wrappedCacheListener = new CacheListener<>(cacheListener);
    cacheListeners.add(wrappedCacheListener);
    return wrappedCacheListener;
  }

  public void registerComparableListener(ComparableCacheListener<? super T> cacheListener) {
    AsynchronousComparableCacheListener<? super T> wrappedCacheListener = new AsynchronousComparableCacheListener<>(cacheListener);
    compareCacheListeners.add(wrappedCacheListener);
  }

  public Lifecycle registerThreadedListener(C2monCacheListener<? super T> cacheListener, int queueCapacity, int threadPoolSize) {
    MultiThreadedCacheListener<? super T> threadedCacheListener = new MultiThreadedCacheListener<>(cacheListener, queueCapacity, threadPoolSize);
    cacheListeners.add(threadedCacheListener);
    return threadedCacheListener;
  }

  public Lifecycle registerBufferedListener(final C2monBufferedCacheListener bufferedCacheListener, int frequency) {
    DefaultBufferedCacheListener defaultBufferedCacheListener = new DefaultBufferedCacheListener(bufferedCacheListener, frequency);
    cacheListeners.add(defaultBufferedCacheListener);
    return defaultBufferedCacheListener;
  }

  public Lifecycle registerKeyBufferedListener(final C2monBufferedCacheListener<Long> bufferedCacheListener, int frequency) {
    BufferedKeyCacheListener<T> bufferedKeyCacheListener = new BufferedKeyCacheListener<>(bufferedCacheListener, frequency);
    cacheListeners.add(bufferedKeyCacheListener);
    return bufferedKeyCacheListener;
  }


  //***************************
  // GETTERS AND SETTERS
  //***************************

  protected CacheLoader getCacheLoader() {
    return cacheLoader;
  }

  /**
   * @return The C2MON cache loader instance
   */
  public C2monCacheLoader getC2monCacheLoader() {
    return c2monCacheLoader;
  }

  public LinkedBlockingDeque<C2monCacheListener<? super T>> getCacheListeners() {
    return cacheListeners;
  }

  /**
   * Tries to load the cache element with the given key from the database and puts it in the cache.
   * In case the cache contains already an entry for that key, the method simply returns
   * a reference to the cache object (see also {@link #get(Object)}
   * @param id The key element
   * @return A reference to the loaded cache object
   */
  public T loadFromDb(final K id) {
    T result;

    cache.acquireWriteLockOnKey(id);
    try {
      if (!cache.isKeyInCache(id)) {

        //try to load from DB; is put in cache if successful; returns null o.w.
        try {
          result = getFromDb(id);
        } catch (Exception e) {
          log.error("Exception caught while loading cache element from DB", e);
          result = null;
        }
        //if unable to find in DB
        if (result == null) {
          throw new CacheElementNotFoundException("Failed to locate cache element with id " + id + " (Cache is " + this.getClass() + ")");
        } else {
          doPostDbLoading(result);
          putQuiet(result);
          return result;
        }
      } else { //try and retrieve; note this could still fail if the element is removed in the meantime! (error logged below in this case)
        return get(id);
      }
    } finally {
      cache.releaseWriteLockOnKey(id);
    }
  }

  /**
   * @return true if the cache has already been loaded from the DB
   */
  protected ClusterCache getClusterCache() {
    return clusterCache;
  }

  /**
   * As loadFromDb, but with no return value. For JMX management.
   *
   * @param id cache element key
   */
  @ManagedOperation(description="Load a cache object manually from the DB into the cache (if not already there)")
  public void loadFromDatabase(final K id) {
    loadFromDb(id);
  }

  /**
   * Looks for the cache object in the DB.
   *
   * @param key cache object key
   * @return the cache object retrieved from the database, or null
   *          if not found
   */
  private T getFromDb(final K key) {
    if (log.isDebugEnabled()) {
      log.trace("Fetching cache object with Id " + key + " from database.");
    }
    return cacheLoaderDAO.getItem(key);
  }

  /**
   * For management purposes.
   * @return the queue sizes of all the listeners to this cache.
   */
  @ManagedOperation(description="Get listener queue sizes.")
  public List<Integer> getListenerTaskQueueSizes() {
    ArrayList<Integer> queueSizes = new ArrayList<Integer>();
    for (C2monCacheListener listener : cacheListeners) {
      if (listener instanceof MultiThreadedCacheListener) {
        queueSizes.add(((MultiThreadedCacheListener) listener).getTaskQueueSize());
      } else if (listener instanceof CacheListener) {
        queueSizes.add(((CacheListener) listener).getTaskQueueSize());
      }
    }
    return queueSizes;
  }

  /**
   * For management purposes.
   * @return the number of active threads for multi-threaded listeners
   */
  @ManagedOperation(description="Get listener active thread number.")
  public List<Integer> getActiveThreadNumber() {
    ArrayList<Integer> threadPoolSizes = new ArrayList<Integer>();
    for (C2monCacheListener listener : cacheListeners) {
      if (listener instanceof MultiThreadedCacheListener) {
        threadPoolSizes.add(((MultiThreadedCacheListener) listener).getActiveThreadPoolNumber());
      }
    }
    return threadPoolSizes;
  }


}
