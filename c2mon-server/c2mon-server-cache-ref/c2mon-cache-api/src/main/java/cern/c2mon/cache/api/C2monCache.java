package cern.c2mon.cache.api;

import java.util.List;

import org.springframework.context.support.ApplicationObjectSupport;

import cern.c2mon.cache.api.listener.C2monBufferedCacheListener;
import cern.c2mon.cache.api.listener.C2monCacheListener;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.shared.common.Cacheable;

/**
 * @param <K> cache key type
 * @param <V> cache element type
 *
 * @author Szymon Halastra
 */
public abstract class C2monCache<K, V> extends ApplicationObjectSupport implements C2monListener, C2monCacheLoader {

  C2monListener<Cacheable> listenerService;

  protected C2monCache() {
    this.listenerService = new C2monListenerService();
  }

  protected abstract V get(K key);

  protected abstract boolean containsKey(K key);

  protected abstract void put(K key, V value);

  protected abstract boolean remove(K key);

  protected abstract String getName();

  protected abstract List<K> getKeys();

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
}