package cern.c2mon.cache.api;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.ApplicationObjectSupport;

import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.listener.C2monBufferedCacheListener;
import cern.c2mon.cache.api.listener.C2monCacheListener;
import cern.c2mon.cache.api.listener.C2monListener;
import cern.c2mon.cache.api.listener.C2monListenerService;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.cache.api.lock.C2monLock;
import cern.c2mon.cache.api.transactions.TransactionalCallable;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.shared.common.Cacheable;


/**
 * @param <K> cache key type
 * @param <V> cache element type
 *
 * @author Szymon Halastra
 */
@Slf4j
public abstract class Cache<K, V extends Cacheable> extends ApplicationObjectSupport implements C2monListener<V>, Serializable, C2monLock<K> {

  private final String cacheName;

  private C2monCacheLoader cacheLoader;

  private final C2monListener<V> listenerService;

  /**
   * Reference to cache loader.
   */

  public Cache(String cacheName) {
    this.cacheName = cacheName;
    this.listenerService = new C2monListenerService<>();
  }

  public abstract void init();

  public abstract V get(K key);

  public abstract boolean containsKey(K key);

  public abstract void put(K key, V value);

  public abstract boolean remove(K key);

  public abstract Set<K> getKeys();

  public abstract void putAll(Map<? extends K, ? extends V> map);

  public abstract Map<K, V> getAll(Set<? extends K> keys);

  public abstract <T> T invoke(K var1, EntryProcessor<K, V, T> var2, Object... var3) throws EntryProcessorException;

  public abstract <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> var1, EntryProcessor<K, V, T> var2, Object... var3);

  public abstract <S> Optional<S> executeTransaction(TransactionalCallable<S> callable);

  public String getName() {
    return cacheName;
  }

  public void setCacheLoader(C2monCacheLoader cacheLoader) {
    this.cacheLoader = cacheLoader;
  }

  public C2monCacheLoader getCacheLoader() {
    return cacheLoader;
  }

  public V loadFromDb(K key) {
    V result;

    if (!containsKey(key)) {

      //try to load from DB; is put in cache if successful; returns null o.w.
      try {
        result = getFromDb(key);
      }
      catch (Exception e) {
        log.error("Exception caught while loading cache element from DB", e);
        result = null;
      }
      //if unable to find in DB
      if (result == null) {
        throw new CacheElementNotFoundException("Failed to locate cache element with id " + key + " (Cache is " + this.cacheName + ")");
      }
      else {
        doPostDbLoading(result);
        put(key, result);
        return result;
      }
    }
    else { //try and retrieve; note this could still fail if the element is removed in the meantime! (error logged below in this case)
      return get(key);
    }
  }

  private V getFromDb(final K key) {
    throw new UnsupportedOperationException("Not yet implemented"); //TODO: used for lazy loading, not required by C2mon, can be added in a future
  }

  private void doPostDbLoading(V value) {

  }

  @Override
  public void notifyListenersOfUpdate(V cacheable) {
    this.listenerService.notifyListenersOfUpdate(cacheable);
  }

  @Override
  public void notifyListenerStatusConfirmation(V cacheable, long timestamp) {
    this.listenerService.notifyListenerStatusConfirmation(cacheable, timestamp);
  }

  @Override
  public void registerSynchronousListener(C2monCacheListener cacheListener) {
    this.listenerService.registerSynchronousListener(cacheListener);
  }

  @Override
  public Lifecycle registerListener(C2monCacheListener cacheListener) {
    return this.listenerService.registerListener(cacheListener);
  }

  @Override
  public Lifecycle registerThreadedListener(C2monCacheListener cacheListener, int queueCapacity, int threadPoolSize) {
    return this.listenerService.registerThreadedListener(cacheListener, queueCapacity, threadPoolSize);
  }

  @Override
  public Lifecycle registerBufferedListener(C2monBufferedCacheListener c2monBufferedCacheListener, int frequency) {
    return this.listenerService.registerBufferedListener(c2monBufferedCacheListener, frequency);
  }

  @Override
  public Lifecycle registerKeyBufferedListener(C2monBufferedCacheListener bufferedCacheListener, int frequency) {
    return this.listenerService.registerKeyBufferedListener(bufferedCacheListener, frequency);
  }
}