package cern.c2mon.cache.api.listener;

import java.util.concurrent.LinkedBlockingDeque;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.cache.api.listener.impl.BufferedKeyCacheListener;
import cern.c2mon.cache.api.listener.impl.CacheListener;
import cern.c2mon.cache.api.listener.impl.DefaultBufferedCacheListener;
import cern.c2mon.cache.api.listener.impl.MultiThreadedCacheListener;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.shared.common.Cacheable;

/**
 * @author Szymon Halastra
 */
@Slf4j
public class C2monListenerService<K, V extends Cacheable> implements C2monListener<V> {

  /**
   * Reference to the Cache event listeners
   */
  private LinkedBlockingDeque<C2monCacheListener<? super V>> cacheListeners = new LinkedBlockingDeque<>();

  public C2monListenerService() {
  }


  /**
   * In init method there should be a line which more or less looks like this:
   * <p>
   * registeredEventListeners = cache.getCacheEventNotificationService();
   * <p>
   * Above line is strongly connected with Ehcache, so in our case it should be
   * abstracted and explicit call to cache should be in IMPL module
   */

  @Override
  public void notifyListenersOfUpdate(final V cacheable) {
//    registeredEventListeners.notifyElementUpdated(new Element(cacheable.getId(), null), false); //only for monitoring via Ehcache: not using Ehcache listeners o.w.
    try {
      @SuppressWarnings("unchecked")
      V cloned = (V) cacheable.clone();
      for (C2monCacheListener<? super V> listener : cacheListeners) {
        listener.notifyElementUpdated(cloned);
      }
    }
    catch (CloneNotSupportedException e) {
      log.error("CloneNotSupportedException caught while cloning a cache element - this should never happen!", e);
      throw new RuntimeException("CloneNotSupportedException caught while cloning a cache element - this should never happen!", e);
    }
  }

  @Override
  public void notifyListenerStatusConfirmation(final V cacheable, final long timestamp) {
    try {
      @SuppressWarnings("unchecked")
      V cloned = (V) cacheable.clone();
      for (C2monCacheListener<? super V> listener : cacheListeners) {
        listener.confirmStatus(cloned);
      }
    }
    catch (CloneNotSupportedException e) {
      log.error("CloneNotSupportedException caught while cloning a cache element - this should never happen!", e);
      throw new RuntimeException("CloneNotSupportedException caught while cloning a cache element - this should never happen!", e);
    }
  }

  @Override
  public void registerSynchronousListener(C2monCacheListener<? super V> cacheListener) {
    cacheListeners.add(cacheListener);
  }

  @Override
  public Lifecycle registerListener(C2monCacheListener<? super V> cacheListener) {
    CacheListener<? super V> wrappedCacheListener = new CacheListener<>(cacheListener);
    cacheListeners.add(wrappedCacheListener);
    return wrappedCacheListener;
  }

  @Override
  public Lifecycle registerThreadedListener(C2monCacheListener<? super V> cacheListener, int queueCapacity, int threadPoolSize) {
    MultiThreadedCacheListener<? super V> threadedCacheListener = new MultiThreadedCacheListener<>(cacheListener, queueCapacity, threadPoolSize);
    cacheListeners.add(threadedCacheListener);
    return threadedCacheListener;
  }

  @Override
  public Lifecycle registerBufferedListener(final C2monBufferedCacheListener<Cacheable> c2monBufferedCacheListener, int frequency) {
    DefaultBufferedCacheListener<Cacheable> bufferedCacheListener = new DefaultBufferedCacheListener<>(c2monBufferedCacheListener, frequency);
    cacheListeners.add(bufferedCacheListener);
    return bufferedCacheListener;
  }

  @Override
  public Lifecycle registerKeyBufferedListener(final C2monBufferedCacheListener<Long> bufferedCacheListener, int frequency) {
    BufferedKeyCacheListener<V> bufferedKeyCacheListener = new BufferedKeyCacheListener<V>(bufferedCacheListener, frequency);
    cacheListeners.add(bufferedKeyCacheListener);
    return bufferedKeyCacheListener;
  }
}
