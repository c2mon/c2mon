package cern.c2mon.cache.api.listener.impl;

import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.shared.common.Cacheable;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;

/**
 * Performance: Eventually scales much better than single thread listener, but (at least during my tests), that takes
 * 100K updates or more
 *
 * @param <V> the cache object type we are getting updates on
 * @author Alexandros Papageorgiou Koufidis
 * @see AbstractCacheListener
 */
@Slf4j
public class MultiThreadListener<V extends Cacheable> extends AbstractCacheListener<V> {
  protected CacheListener<V> eventHandler;

  public MultiThreadListener(int concurrency, CacheListener<V> eventHandler) {
    super(Executors.newFixedThreadPool(concurrency));
    this.eventHandler = eventHandler;
  }

  @Override
  public void apply(V cacheable) {
    executorService.submit(() -> eventHandler.apply(cacheable));
  }
}
