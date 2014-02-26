package cern.c2mon.server.cache.common;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PreDestroy;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.jmx.export.annotation.ManagedOperation;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.shared.common.Cacheable;

/**
 * Provides all core functionalities that are required to manage a cache. This class uses
 * internally Ehcache.
 * 
 * @author Mark Brightwell
 *
 * @param <K> The key class type
 * @param <T> The value class type
 */
public abstract class BasicCache<K, T extends Serializable> extends ApplicationObjectSupport {

  /**
   * Private class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(BasicCache.class);
  
  /**
   * Reference to the wrapped Ehcache.
   */
  protected Ehcache cache;
  
  /**
   * if c2mon.cache.skippreloading is set to true, the cache will not be preloaded from the database, instead
   * ehcache will initialize it from a local cache storage (if available)  
   */
  @Value("${c2mon.server.cache.skippreloading}")
  protected boolean skipCachePreloading = false;
  
  /**
   * The cache mode is either set to "multi" or "single", depending on whether the server is running with
   * a single or distributed cache.
   */
  @Value("${cern.c2mon.cache.mode}")
  protected String cacheMode;
  
  /**
   * An inexpensive check to see if the key exists in the cache.
   * @param id The key to check for
   * @return <code>true</code> if an Element matching the key is found in the cache. 
   *         No assertions are made about the state of the Element.
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
   * <p>Throws the following unchecked exceptions:
   * <li> {@link IllegalArgumentException} if called with a null key
   * <li> {@link CacheElementNotFoundException} if the object was not found in the 
   *      cache
   * <li> {@link RuntimeException} if an error occurs when accessing the cache object
   *      in Ehcache.
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
  @SuppressWarnings("unchecked")
  public final T get(final K id) {
    T result = null;
    if (id != null) {
      cache.acquireReadLockOnKey(id);
      try {
        Element element = cache.get((Object) id);
        
        if (element != null) {
          result = (T) element.getObjectValue();
        } else {
          throw new CacheElementNotFoundException("Failed to locate cache element with id " + id + " (Cache is " + this.getClass() + ")");
        }                        
      } catch (CacheException cacheException) {
        LOGGER.error("getReference() - Caught cache exception thrown by Ehcache while accessing object with id " + id, cacheException);
        throw new RuntimeException("An error occured when accessing the cache object with id " + id, cacheException);
      } finally {
        cache.releaseReadLockOnKey(id);
      }
    } else {
      LOGGER.error("getReference() - Trying to access cache with a NULL key - throwing an exception!");
      //TODO throw runtime exception here or not?
      throw new IllegalArgumentException("Accessing cache with null key!");
    }
    
    return result;
  }
  
  /**
   * Returns the list of all keys in the cache.
   * Only Longs can be inserted as keys in C2monCache.
   * @return list of keys
   */
  @SuppressWarnings("unchecked")
  public List<K> getKeys() {
    return (List<K>) cache.getKeys();
  }
  
  public void put(K key, T value) {
    cache.put(new Element(key, value));       
  }
  
  /**
   * Remove an object from the cache.
   * @param id the key of the cache element
   * @return true if successful
   */
  @ManagedOperation(description="Manually remove a given object from the cache (will need re-loading manually from DB)")
  public boolean remove(K id) {
    return cache.removeWithWriter(id);
  }
  
  /**
   * @return the cache
   */
  public Ehcache getCache() {
    return cache;
  }
  
  @PreDestroy
  public void shutdown() {
    LOGGER.debug("Closing cache (" + this.getClass() + ")");
  }
  
  public void acquireReadLockOnKey(K id) {
    if (id != null) {
      cache.acquireReadLockOnKey(id);
    } else {
      LOGGER.error("Trying to acquire read lock with a NULL key - throwing an exception!");
      throw new IllegalArgumentException("Acquiring read lock with null key!");
    }
  }
  
  public void releaseReadLockOnKey(K id) {
    if (id != null) {
      cache.releaseReadLockOnKey(id);
    } else {
      LOGGER.error("Trying to release read lock with a NULL key - throwing an exception!");
      throw new IllegalArgumentException("Trying to release read lock with null key!");
    }
  }
  
  public void acquireWriteLockOnKey(K id) {
    if (id != null) {
      cache.acquireWriteLockOnKey(id);
    } else {
      LOGGER.error("Trying to acquire write lock with a NULL key - throwing an exception!");
      throw new IllegalArgumentException("Acquiring write lock with null key!");
    }
  }
  
  public void releaseWriteLockOnKey(K id) {
    if (id != null) {
      cache.releaseWriteLockOnKey(id);
    } else {
      LOGGER.error("Trying to release write lock with a NULL key - throwing an exception!");
      throw new IllegalArgumentException("Trying to release write lock with null key!");
    }
  }
  
  public boolean isWriteLockedByCurrentThread(K id) {
    return cache.isWriteLockedByCurrentThread(id);
  }
  
  public boolean isReadLockedByCurrentThread(K id) {
    return cache.isReadLockedByCurrentThread(id);
  }
    
}
