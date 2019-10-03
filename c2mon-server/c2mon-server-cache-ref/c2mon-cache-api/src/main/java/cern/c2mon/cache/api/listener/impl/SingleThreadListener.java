package cern.c2mon.cache.api.listener.impl;

import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.shared.common.Cacheable;

/**
 * Performance: Surprisingly light and efficient for a single thread implementation. Use this unless you're doing
 * some huge operation, or have significant incoming traffic
 *
 * @param <V> the cache object type we are getting updates on
 * @author Alexandros Papageorgiou Koufidis
 * @see MultiThreadListener
 * @see AbstractCacheListener
 */
public class SingleThreadListener<V extends Cacheable> extends MultiThreadListener<V> {

  public SingleThreadListener(CacheListener<V> eventHandler) {
    super(1, eventHandler);
  }
}
