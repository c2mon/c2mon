package cern.c2mon.cache.api.listener;

import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.Cacheable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CacheListenerManagerImpl<V extends Cacheable> implements CacheListenerManager<V> {

  // Is and should remain concurrent both in the map level and the set level!
  private final Map<CacheEvent, Set<CacheListener<V>>> eventListeners;

  public CacheListenerManagerImpl() {
    eventListeners = new ConcurrentHashMap<>();
    // Initialize each list using a "ConcurrentSet"
    for (CacheEvent event : CacheEvent.values()) {
      eventListeners.put(event, Collections.newSetFromMap(new ConcurrentHashMap<>()));
    }
  }

  //  TODO Should switch to try-catch for safety and also clone the object?

  @Override
  public void notifyListenersOf(CacheEvent event, V source) {
    eventListeners.get(event).forEach(listener -> listener.apply(source));
  }

  @Override
  public void registerListener(CacheListener<V> listener, CacheEvent... events) {
    for (CacheEvent cacheEvent : events) {
      eventListeners.get(cacheEvent).add(listener);
    }
  }

  @Override
  public void deregisterListener(CacheListener<V> listener) {
    for (CacheEvent cacheEvent : CacheEvent.values()) {
      eventListeners.get(cacheEvent).remove(listener);
    }
  }
}
