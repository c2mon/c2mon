package cern.c2mon.cache.api;

import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.shared.common.Cacheable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class C2monCacheLoader<K, V extends Cacheable> {

  private AbstractCache<K, V> cache;

  public C2monCacheLoader(AbstractCache<K, V> cache) {
    this.cache = cache;
  }

  public V loadFromDb(K key) {
    V result;

    if (!cache.containsKey(key)) {

      //try to load from DB; is put in cache if successful; returns null o.w.
      try {
        result = getFromDb(key);
      } catch (Exception e) {
        log.error("Exception caught while loading cache element from DB", e);
        result = null;
      }
      //if unable to find in DB
      if (result == null) {
        throw new CacheElementNotFoundException("Failed to locate cache element with id " + key + " (C2monCache is " + cache.getName() + ")");
      } else {
        doPostDbLoading(result);
        cache.put(key, result);
        return result;
      }
    } else { //try and retrieve; note this could still fail if the element is removed in the meantime! (error logged below in this case)
      return cache.get(key);
    }
  }

  private V getFromDb(final K key) {
    throw new UnsupportedOperationException("Not yet implemented"); //TODO: used for lazy loading, not required by C2mon, can be added in a future
  }

  // Should this be abstracted or interfaced?
  private void doPostDbLoading(V value) {
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
