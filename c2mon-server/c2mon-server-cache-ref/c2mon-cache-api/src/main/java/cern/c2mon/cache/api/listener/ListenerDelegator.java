package cern.c2mon.cache.api.listener;

import cern.c2mon.shared.common.Cacheable;

/**
 * @author Alexandros Papageorgiou Koufidis
 */
public interface ListenerDelegator<V extends Cacheable> extends CacheListenerManager<V> {

  CacheListenerManager<V> getCacheListenerManager();

  @Override
  default void notifyListenersOf(CacheEvent event, V source) {
    try {
      getCacheListenerManager().notifyListenersOf(event, (V) source.clone());
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
  }

  @Override
  default void registerListener(CacheListener<V> listener, CacheEvent... events) {
    getCacheListenerManager().registerListener(listener, events);
  }

  @Override
  default void deregisterListener(CacheListener<V> listener) {
    getCacheListenerManager().deregisterListener(listener);
  }
}
