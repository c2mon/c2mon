package cern.c2mon.cache.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.cache.integration.CacheLoader;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;

import org.springframework.context.support.ApplicationObjectSupport;

import cern.c2mon.cache.api.listener.C2monBufferedCacheListener;
import cern.c2mon.cache.api.listener.C2monCacheListener;
import cern.c2mon.cache.api.listener.C2monListener;
import cern.c2mon.cache.api.listener.C2monListenerService;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.cache.api.lock.C2monLock;
import cern.c2mon.cache.api.lock.TransactionalCallable;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.shared.common.Cacheable;

/**
 * @param <K> cache key type
 * @param <V> cache element type
 *
 * @author Szymon Halastra
 */
public abstract class C2monCache<K, V> extends ApplicationObjectSupport implements C2monListener, C2monLock<K>, Serializable {

  protected String cacheName;

  protected C2monCacheLoader cacheLoader;

  C2monListener<Cacheable> listenerService;

  public C2monCache(String cacheName) {
    this.cacheName = cacheName;
    this.listenerService = new C2monListenerService();
  }

  public abstract void init();

  public abstract V get(K key);

  public abstract boolean containsKey(K key);

  public abstract void put(K key, V value);

  public abstract boolean remove(K key);

  public abstract List<K> getKeys();

  public abstract void putAll(Map<? extends K, ? extends V> map);

  public abstract Map<K, V> getAll(Set<? extends K> keys);

  public abstract <T> T invoke(K var1, EntryProcessor<K, V, T> var2, Object... var3) throws EntryProcessorException;

  public abstract <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> var1, EntryProcessor<K, V, T> var2, Object... var3);

  public String getName() {
    return cacheName;
  }

  public void setCacheLoader(C2monCacheLoader cacheLoader) {
    this.cacheLoader = cacheLoader;
  }

  public C2monCacheLoader getCacheLoader() {
    return cacheLoader;
  }

  @Override
  public void notifyListenersOfUpdate(Cacheable cacheable) {
    this.listenerService.notifyListenersOfUpdate(cacheable);
  }

  @Override
  public void notifyListenerStatusConfirmation(Cacheable cacheable, long timestamp) {
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

  public abstract void executeTransaction(TransactionalCallable callable);
}