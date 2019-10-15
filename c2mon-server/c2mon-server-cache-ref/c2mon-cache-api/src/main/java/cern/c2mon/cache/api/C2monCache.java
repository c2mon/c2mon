package cern.c2mon.cache.api;

import cern.c2mon.cache.api.listener.CacheEvent;
import cern.c2mon.cache.api.listener.ListenerDelegator;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.cache.api.spi.CacheQuery;
import cern.c2mon.cache.api.transactions.TransactionalCallable;
import cern.c2mon.shared.common.Cacheable;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cern.c2mon.cache.api.listener.CacheEvent.UPDATE_ACCEPTED;
import static java.util.Objects.requireNonNull;


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
   * <p>
   * WILL limit the result sets to {@link CacheQuery#DEFAULT_MAX_RESULTS} results. If you would like to go over
   * that limit, build a custom implementation like so:
   * <pre>
   * query(new CacheQuery<V>(i -> true).maxResults(MY_LIMIT)).stream().map(Cacheable::getId).collect(Collectors.toSet());
   * </pre>
   * <p>
   * It's important to limit the max results as close to the cache layer as possible (i.e before getting them back)
   * as it could possibly overflow the memory!
   *
   * @return a {@code Set} of the keys contained in the Cache
   */
  default Set<Long> getKeys() {
    return query(i -> true).stream().map(Cacheable::getId).collect(Collectors.toSet());
  }

  void init();

  <T> T executeTransaction(TransactionalCallable<T> callable);

  /**
   * Alternative api to {@link C2monCache#executeTransaction(TransactionalCallable)} when you don't need the result
   *
   * @param runnable the {@code Runnable} you want to run
   */
  default void executeTransaction(Runnable runnable) {
    executeTransaction(() -> {
      runnable.run();
      return 1;
    });
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
   * @param providedQuery must not be null, a CacheQuery to execute on the cache
   * @return a {@code Collection} of results, may be empty, never null
   * @see C2monCache#query(Function)
   */
  Collection<V> query(@NonNull CacheQuery<V> providedQuery);

  /**
   * Put an element to the cache - inserts or updates if the element existed already
   * <p>
   * Does NOT notify listeners of the event!
   *
   * @param key
   * @param value
   */
  default void putQuiet(@NonNull Long key,@NonNull V value) {
    requireNonNull(key);
    requireNonNull(value);
    value.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    getCache().put(key, value);
  }

  // === Section: C2MON overrides of javax.cache.Cache methods ===

  @Override
  default V get(@NonNull Long key) throws IllegalArgumentException {
    requireNonNull(key);
    return getCache().get(key);
  }

  /**
   * Put an element to the cache - inserts or updates if the element existed already
   * <p>
   * Will generate {@link CacheEvent#UPDATE_ACCEPTED} events for all registered listeners
   *
   * @param key
   * @param value
   * @see C2monCache#putQuiet(Long, Cacheable) to put without notifying
   */
  @Override
  default void put(@NonNull Long key, @NonNull V value) {
    requireNonNull(key);
    requireNonNull(value);
    putQuiet(key, value);
    notifyListenersOf(UPDATE_ACCEPTED, value);
  }

//  TODO Support some form of compareAndPut(V expected, V new)?

  @Override
  default void close() {
    // When getting here, it's quite possible that the cache is already closed, so check first!
    if (!getCache().isClosed())
      getCache().close();
  }
}