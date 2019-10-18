package cern.c2mon.cache;

import cern.c2mon.cache.api.listener.impl.AbstractCacheListener;
import cern.c2mon.cache.api.listener.impl.SingleThreadListener;
import cern.c2mon.shared.common.Cacheable;

public abstract class SingleThreadCacheListenerTest<V extends Cacheable> extends AbstractCacheListenerTest<V> {
  @Override
  protected AbstractCacheListener<V> generateListener() {
    return new SingleThreadListener<>(listenerAction);
  }

  @Override
  protected AbstractCacheListener<V> generateMutatingListener() {
    return new SingleThreadListener<>(mutatingListenerAction);
  }
}
