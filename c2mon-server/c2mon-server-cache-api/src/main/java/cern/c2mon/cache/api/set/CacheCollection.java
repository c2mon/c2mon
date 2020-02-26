package cern.c2mon.cache.api.set;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.listener.BatchConsumer;
import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.server.common.util.KotlinAPIs;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.Cacheable;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Collects caches to allow applying operations across all of them
 *
 * @param <T> The highest common parent type between the types stored in the caches
 */
public class CacheCollection<T extends Cacheable> {
  private static final Logger log = LoggerFactory.getLogger(CacheCollection.class);

  @Getter
  protected final List<C2monCache<? extends T>> caches;

  public CacheCollection(C2monCache<? extends T>... caches) {
    this.caches = Arrays.asList(caches);
  }

  public CacheCollection(CacheCollection<? extends T>... cacheCollections) {
    caches = Arrays
      .stream(cacheCollections)
      .flatMap(cacheCollection -> cacheCollection.getCaches().stream())
      .collect(Collectors.toList());
  }

  /**
   * Checks if the given key is contained in any of the caches
   *
   * @param id the key to look for
   * @return true if the object was found, false otherwise
   */
  public boolean containsKey(long id) {
    try {
      return doAcrossCaches(id, cache -> cache.containsKey(id));
    } catch (CacheElementNotFoundException e) {
      log.debug("Cache element #{} was not found in any of the caches of {}", id, getClass().toString());
      return false;
    }
  }

  /**
   * Gets the object from any of the caches in this collection
   *
   * @param id the key to look for
   * @return the object found
   * @throws CacheElementNotFoundException If no cache contains the provided key
   * @apiNote The iterator goes over the caches in the order they were passed to the Ctor
   * This means that if a key exists in multiple caches, only the first result will be returned.
   * Avoid using this "feature" as the underlying implementation may change (e.g to a Set)
   */
  public T get(long id) {
    return doAcrossCaches(id, cache -> cache.get(id));
  }

  /**
   * Gets all the objects from all the caches whose id is contained
   * in the input set of ids.
   *
   * @param ids the keys to look for
   * @return a set of all the objects found across the caches, potentially empty
   */
  public Map<Long, ? extends T> getAll(Set<Long> ids) {
    return caches
      .parallelStream()
      .map(cache -> cache.getAll(ids))
      .reduce(new HashMap<>(),
        (map, map2) -> KotlinAPIs.apply(map, __ -> map.putAll(map2))
      );
  }

  /**
   * Create and return a grouping of all keys in all caches, eliminating duplicates
   *
   * @return a Set of keys containing all keys from all caches
   * @implNote This method does not lock the caches in any way, so it will not represent the cache keys
   * during an absolutely single point in time, but rather the snapshot of every cache's keys during the
   * runtime of this operation
   */
  public Set<Long> getKeys() {
    return caches.parallelStream().flatMap(cache -> cache.getKeys().stream()).collect(Collectors.toSet());
  }

  /**
   * Register a listener across all caches, on the specified events
   *
   * @param listener         the listener to register
   * @param baseEvent        the event to listen to
   * @param additionalEvents additional events to listen to
   * @apiNote there is one baseEvent as a parameter to force you to provide at least one event
   * This avoids accidentally registering a listener to no events
   */
  public void registerListener(CacheListener<T> listener, CacheEvent baseEvent, CacheEvent... additionalEvents) {
    caches.forEach(cache ->
      cache.getCacheListenerManager()
        .registerListener(listener::apply, baseEvent, additionalEvents));
  }

  /**
   * Register a buffered listener across all caches, on the specified events
   *
   * @param listener the buffered listener to register
   * @param events   the events to listen to
   */
  public void registerBufferedListener(BatchConsumer<T> listener, CacheEvent baseEvent, CacheEvent... events) {
    caches.forEach(cache ->
      cache.getCacheListenerManager()
        .registerBufferedListener(tags -> listener.apply((Set<T>) tags), baseEvent, events)
    );
  }

  /**
   * Closes all caches
   */
  public void close() {
    caches.forEach(cache -> cache.getCacheListenerManager().close());
  }

  /**
   * Perform the given action across all the caches. Early termination!
   *
   * @param id          the key to look for
   * @param cacheAction the action to apply
   * @param <R>         the action return type
   * @return the result of the action provided, applied on the matching key
   * @throws CacheElementNotFoundException If no cache contains the provided key
   * @apiNote Terminates early as soon as it finds a cache that contains the given key
   */
  protected <R> R doAcrossCaches(long id, Function<C2monCache<? extends T>, R> cacheAction) {
    for (C2monCache<? extends T> cache : caches) {
      if (cache.containsKey(id)) {
        return cacheAction.apply(cache);
      }
    }

    throw new CacheElementNotFoundException(id + " not found in caches");
  }

}
