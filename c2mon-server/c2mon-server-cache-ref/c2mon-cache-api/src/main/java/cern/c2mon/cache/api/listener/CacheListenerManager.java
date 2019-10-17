package cern.c2mon.cache.api.listener;

import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.Cacheable;

public interface CacheListenerManager<V extends Cacheable> {

  void notifyListenersOf(CacheEvent event, V source);

  void registerListener(CacheListener<V> listener, CacheEvent... events);

  void deregisterListener(CacheListener<V> listener);
}
