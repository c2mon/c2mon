package cern.c2mon.cache.api;

import cern.c2mon.cache.api.listener.ListenerDelegator;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.cache.api.spi.CacheQuery;
import cern.c2mon.cache.api.transactions.TransactionalCallable;
import cern.c2mon.shared.common.Cacheable;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @param <V> cache element type
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 * @author Brice Copy
 */
public interface C2monCache<V extends Cacheable> extends CacheDelegator<V>, Serializable, ListenerDelegator<V> {

  CacheLoader getCacheLoader();

  void setCacheLoader(CacheLoader<V> cacheLoader);

  /**
   * Default implementation for a simple getKeys function. Caches may choose to implement a version of this with
   * better performance
   *
   * WILL limit the result sets to {@link CacheQuery#DEFAULT_MAX_RESULTS} results. If you would like to go over
   * that limit, build a custom implementation like so:
   * <pre>
   * query(new CacheQuery<V>(i -> true).maxResults(MY_LIMIT)).stream().map(Cacheable::getId).collect(Collectors.toSet());
   * </pre>
   *
   * @return a {@code Set} of the keys contained in the Cache
   */
  default Set<Long> getKeys() {
    return query(i -> true).stream().map(Cacheable::getId).collect(Collectors.toSet());
  }

  void init();

  <T> Optional<T> executeTransaction(TransactionalCallable<T> callable);

  /**
   * Alternative to {@link C2monCache#executeTransaction(TransactionalCallable)} when you don't need the result
   *
   * @param runnable the {@code Runnable} you want to run
   */
  default void executeTransaction(Runnable runnable) {
    executeTransaction(() -> {
      runnable.run();
      return 1;
    });
  }

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
   * <p>
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
}