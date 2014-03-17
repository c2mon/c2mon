/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2014 CERN.
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
package cern.c2mon.server.cache.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.loader.CacheLoader;

import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedOperation;

import cern.c2mon.server.cache.BufferedTimCacheListener;
import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.listener.BufferedCacheListener;
import cern.c2mon.server.cache.listener.BufferedKeyCacheListener;
import cern.c2mon.server.cache.listener.CacheListener;
import cern.c2mon.server.cache.listener.MultiThreadedCacheListener;
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
 * @param <T> the cache object type
 * @param <K> the cache key type
 * 
 * @author Mark Brightwell
 *
 */
public abstract class AbstractCache<K, T extends Cacheable> extends BasicCache<K, T> {
  
  
  /**
   * Private class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(AbstractCache.class);
  
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
  private LinkedBlockingDeque<C2monCacheListener< ? super T>> cacheListeners = new LinkedBlockingDeque<C2monCacheListener< ? super T>>();
  
  /**
   * the RegisteredEventListeners instance for this cache which is used
   * to register and unregister listeners.
   */
  private RegisteredEventListeners registeredEventListeners; //only for monitoring
  
  public AbstractCache(final ClusterCache clusterCache, 
                       final Ehcache cache,
                       final CacheLoader cacheLoader,
                       final C2monCacheLoader c2monCacheLoader, 
                       final SimpleCacheLoaderDAO<T> cacheLoaderDAO) {
    super();
    this.clusterCache = clusterCache;
    this.cache = cache;
    this.cacheLoader = cacheLoader;
    this.c2monCacheLoader = c2monCacheLoader;
    this.cacheLoaderDAO = cacheLoaderDAO;
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
    if (!skipCachePreloading && cacheMode.equalsIgnoreCase("single")) {
        cache.removeAll();
    }
    
    //preload the cache
    //flag recording if THIS server should load THIS cache
    boolean loadCache = false;
    
    //lock cacheStatus while setting that this server will perform the preload
    getClusterCache().acquireWriteLockOnKey(getCacheInitializedKey());    
    try {      
      if (!skipCachePreloading && !getClusterCache().getCopy(getCacheInitializedKey()).equals(Boolean.TRUE)) {
        //record that the preload will be done by this server and set loading flag to TRUE
        loadCache = true;
        getClusterCache().put(getCacheInitializedKey(), Boolean.TRUE);        
      }      
    } finally {
      getClusterCache().releaseWriteLockOnKey(getCacheInitializedKey());      
    }
    
    if (loadCache) {      
      LOGGER.info("Preloading cache from DB: " + getCacheName());
      getC2monCacheLoader().preload();            
    } else {
      LOGGER.info("No preloading necessary: " + getCacheName());
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
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace(id + " Trying to get READ lock...");
      }
      cache.acquireReadLockOnKey(id);
      try {
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace(id + " Got READ lock");
        }
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
        LOGGER.error("Unable to get a serialized copy of the cache element as serialization is not supported for this object.", ex);
        throw new UnsupportedOperationException("The getCopy() method is not supported for this cache element since the cache object is not entirely serializable. Please revisit your object.", ex);
      } finally {
        cache.releaseReadLockOnKey(id);
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace(id + " Released READ lock");
        }
      }
    }
    else {
      LOGGER.error("getCopy() - Trying to access cache with a NULL key - throwing an exception!");
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
   * @param key
   * @param value
   */
  @Override
  public void put(K key, T value) {
    super.put(key, value);
    notifyListenersOfUpdate(value);          
  }

  public void lockAndNotifyListeners(final K id) {
    acquireReadLockOnKey(id);
    try {
      notifyListenersOfUpdate(this.get(id));
    } finally {
      releaseReadLockOnKey(id);      
    }
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
      LOGGER.error("CloneNotSupportedException caught while cloning a cache element - this should never happen!", e);
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
      LOGGER.error("CloneNotSupportedException caught while cloning a cache element - this should never happen!", e);
      throw new RuntimeException("CloneNotSupportedException caught while cloning a cache element - this should never happen!", e);
    }
  }
    
  public void registerSynchronousListener(C2monCacheListener<? super T> timCacheListener) {    
    cacheListeners.add(timCacheListener);  
  }
  
  public Lifecycle registerListener(C2monCacheListener<? super T> timCacheListener) {
    CacheListener wrappedCacheListener = new CacheListener(timCacheListener);
    cacheListeners.add(wrappedCacheListener);
    return wrappedCacheListener;
  }
  
  public Lifecycle registerThreadedListener(C2monCacheListener<? super T> timCacheListener, int queueCapacity, int threadPoolSize) {
    MultiThreadedCacheListener threadedCacheListener = new MultiThreadedCacheListener(timCacheListener, queueCapacity, threadPoolSize);
    cacheListeners.add(threadedCacheListener);
    return threadedCacheListener;
  }
  
  public Lifecycle registerBufferedListener(final BufferedTimCacheListener bufferedTimCacheListener) {
    BufferedCacheListener bufferedCacheListener = new BufferedCacheListener(bufferedTimCacheListener);    
    cacheListeners.add(bufferedCacheListener); 
    return bufferedCacheListener;
  }
  
  public Lifecycle registerKeyBufferedListener(final BufferedTimCacheListener<Long> bufferedTimCacheListener) {
    BufferedKeyCacheListener<T> bufferedKeyCacheListener = new BufferedKeyCacheListener<T>(bufferedTimCacheListener);
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
  
  /**
   * Tries to load the cache element with the given key from the database and puts it in the cache.
   * In case the cache contains already an entry for that key, the method simply returns
   * a reference to the cache object (see also {@link #get(Object)}
   * @param id The key element
   * @return A reference to the loaded cache object
   */
  public T loadFromDb(final K id) {
    T result;
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace(cache.getName() + " Acquiring WRITE lock for id=" + String.valueOf(id));
    }
    cache.acquireWriteLockOnKey(id);
    try {
      if (!cache.isKeyInCache(id)) {
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace(cache.getName() + " Got WRITE lock for id=" + String.valueOf(id));
        }
          
        //try to load from DB; is put in cache if successful; returns null o.w.
        try {
          result = getFromDb(id);
        } catch (Exception e) {
          LOGGER.error("Exception caught while loading cache element from DB", e);
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
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace(cache.getName() + " Released WRITE lock for id=" + String.valueOf(id));
      }
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
    if (LOGGER.isDebugEnabled()) {
      LOGGER.trace("Fetching cache object with Id " + key + " from database.");
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
