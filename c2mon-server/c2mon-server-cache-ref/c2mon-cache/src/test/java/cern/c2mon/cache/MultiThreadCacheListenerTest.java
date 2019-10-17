package cern.c2mon.cache;

import cern.c2mon.cache.api.listener.impl.AbstractCacheListener;
import cern.c2mon.cache.api.listener.impl.MultiThreadListener;
import cern.c2mon.shared.common.Cacheable;

public abstract class MultiThreadCacheListenerTest<V extends Cacheable> extends AbstractCacheListenerTest<V> {
  @Override
  protected AbstractCacheListener<V> generateListener() {
    return new MultiThreadListener<>(8, listenerAction);
  }

  @Override
  protected AbstractCacheListener<V> generateMutatingListener() {
    return new MultiThreadListener<>(8, mutatingListenerAction);
  }
}
