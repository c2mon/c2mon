package cern.c2mon.cache.api.set;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.listener.BufferedCacheListener;
import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.Cacheable;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Collects caches to allow applying operations across all of them
 *
 * @apiNote Avoid passing
 * @param <T> The highest common parent type between the types stored in the caches
 */
public abstract class CacheCollection<T extends Cacheable> {

  protected final C2monCache<? extends T>[] caches;

  protected CacheCollection(C2monCache<? extends T>... caches) {
    this.caches = caches;
  }

  /**
   * Gets the object from any of the caches in this collection
   *
   * @param id the key to look for
   * @apiNote The iterator goes over the caches in the order they were passed to the Ctor
   * This means that if a key exists in multiple caches, only the first result will be returned.
   * Avoid using this "feature" as the underlying implementation may change (e.g to a Set)
   * @return the object found
   * @throws CacheElementNotFoundException If no cache contains the provided key
   */
  public T get(long id) {
    return doAcrossCaches(id, cache -> cache.get(id));
  }

  /**
   * Create and return a grouping of all keys in all caches, eliminating duplicates
   *
   * @implNote This method does not lock the caches in any way, so it will not represent the cache keys
   * during an absolutely single point in time, but rather the snapshot of every cache's keys during the
   * runtime of this operation
   * @return a Set of keys containing all keys from all caches
   */
  public Set<Long> getKeys() {
    return Stream.of(caches).parallel().flatMap(cache -> cache.getKeys().stream()).collect(Collectors.toSet());
  }

  /**
   * Register a listener across all caches, on the specified events
   *
   * @param listener the listener to register
   * @param baseEvent the event to listen to
   * @param additionalEvents additional events to listen to
   * @apiNote there is one baseEvent as a parameter to force you to provide at least one event
   * This avoids accidentally registering a listener to no events
   */
  public void registerListener(CacheListener<T> listener, CacheEvent baseEvent, CacheEvent... additionalEvents) {
    forEachCache(cache -> cache.getCacheListenerManager().registerListener(listener::apply, baseEvent, additionalEvents));
  }

  /**
   * Register a buffered listener across all caches, on the specified events
   *
   * @param listener the buffered listener to register
   * @param events the events to listen to
   */
  public void registerBufferedListener(BufferedCacheListener<T> listener, CacheEvent... events) {
    forEachCache(cache -> cache.getCacheListenerManager().registerBufferedListener(tags -> listener.apply((List<T>) tags), events));
  }

  /**
   * Closes all caches
   */
  public void close() {
    forEachCache(cache -> cache.getCacheListenerManager().close());
  }

  /**
   * Perform the given action across all the caches. Early termination!
   *
   * @param id the key to look for
   * @param cacheAction the action to apply
   * @param <R> the action return type
   * @apiNote Terminates early as soon as it finds a cache that contains the given key
   * @return the result of the action provided, applied on the matching key
   * @throws CacheElementNotFoundException If no cache contains the provided key
   */
  protected <R> R doAcrossCaches(long id, Function<C2monCache<? extends T>, R> cacheAction) {
    for (C2monCache<? extends T> cache : caches) {
      if (cache.containsKey(id)) {
        return cacheAction.apply(cache);
      }
    }

    throw new CacheElementNotFoundException();
  }

  /**
   * Easy wrapper for applying a void function on every cache
   *
   * @param action what you wish to do with the cache
   */
  protected void forEachCache(Consumer<C2monCache<? extends T>> action) {
    for (C2monCache<? extends T> c2monCache : caches) {
      action.accept(c2monCache);
    }
  }
}
