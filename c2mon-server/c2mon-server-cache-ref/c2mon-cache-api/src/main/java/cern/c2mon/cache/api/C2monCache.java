package cern.c2mon.cache.api;

import cern.c2mon.cache.api.listener.BufferedCacheListener;
import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.cache.api.listener.CacheSupervisionListener;
import cern.c2mon.cache.api.listener.Listener;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.cache.api.spi.CacheQuery;
import cern.c2mon.cache.api.transactions.TransactionalCallable;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.shared.common.Cacheable;
import lombok.NonNull;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;


/**
 * @param <V> cache element type
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 * @author Brice Copy
 */
public interface C2monCache<V extends Cacheable> extends Cache<Long, V>, Serializable, Listener<V> {

  CacheLoader getCacheLoader();

  void setCacheLoader(CacheLoader<V> cacheLoader);

  Set<Long> getKeys();

  // TOOO Remove this useless annotation and test system
  @PostConstruct
  void init();

  <T> Optional<T> executeTransaction(TransactionalCallable<T> callable);

  void executeTransaction(Runnable callable);

  default void putQuiet(Long key, V value) {
    put(key, value);
  }

  // TODO Document this to show it should be default behavior
  default void putAndNotify(Long key, V value) {
    put(key, value);
    notifyListenersOfUpdate(value);
  }

  /**
   * Search the cache for objects that satisfy the given filter. More formally, the returned collection
   * will contain all cache objects for which:
   * <pre>
   * filter(cacheObject) == true
   * </pre>
   *
   * Important bits:
   * <ul>
   *   <li>To get all the cache objects, you can simply do {@code query(i -> true)}
   *   <li>Results are capped to {@link CacheQuery#DEFAULT_MAX_RESULTS}. Want more?
   *       Use the overloaded {@link C2monCache#query(CacheQuery)}
   *   <li>This method makes no guarantees of thread safety. That is up to the cache implementation!
   *   <li>C2mon uses a lot of wildcard matching. Make sure to use {@code String#matches},
   *       or {@code String#startsWith} not {@code String#equals}!
   * </ul>
   *
   * @param filter must not be null, the function to filter elements by
   * @return a {@code Collection} of results, may be empty, never null
   */
  Collection<V> query(@NonNull Function<V, Boolean> filter);

  /**
   * Overload of {@link C2monCache#query(Function)} allowing the user to provide additional search parameters
   *
   * @param providedQuery
   * @return a {@code Collection} of results, may be empty, never null
   * @see C2monCache#query(Function)
   */
  Collection<V> query(@NonNull CacheQuery<V> providedQuery);

//  === Listeners ===

  Listener<V> getListenerService();

  @Override
  default void notifyListenersOfUpdate(V cacheable) {
    getListenerService().notifyListenersOfUpdate(cacheable);
  }

  @Override
  default void notifyListenersOfSupervisionChange(V tag) {
    getListenerService().notifyListenersOfSupervisionChange(tag);
  }

  @Override
  default void notifyListenerStatusConfirmation(V cacheable, long timestamp) {
    getListenerService().notifyListenerStatusConfirmation(cacheable, timestamp);
  }

  @Override
  default void registerSynchronousListener(CacheListener<? super V> cacheListener) {
    getListenerService().registerSynchronousListener(cacheListener);
  }

  @Override
  default Lifecycle registerListener(CacheListener<? super V> cacheListener) {
    return getListenerService().registerListener(cacheListener);
  }

  @Override
  default void registerListenerWithSupervision(CacheSupervisionListener<? super V> cacheSupervisionListener) {
    getListenerService().registerListenerWithSupervision(cacheSupervisionListener);
  }

  @Override
  default Lifecycle registerThreadedListener(CacheListener<? super V> cacheListener, int queueCapacity, int threadPoolSize) {
    return getListenerService().registerThreadedListener(cacheListener, queueCapacity, threadPoolSize);
  }

  @Override
  default Lifecycle registerBufferedListener(BufferedCacheListener<Cacheable> bufferedCacheListener, int frequency) {
    return getListenerService().registerBufferedListener(bufferedCacheListener, frequency);
  }

  @Override
  default Lifecycle registerKeyBufferedListener(BufferedCacheListener<Long> bufferedCacheListener, int frequency) {
    return getListenerService().registerKeyBufferedListener(bufferedCacheListener, frequency);
  }
}