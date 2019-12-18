package cern.c2mon.cache.api;

import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.flow.CacheUpdateFlow;
import cern.c2mon.cache.api.listener.CacheListenerManager;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.cache.api.spi.CacheQuery;
import cern.c2mon.shared.common.Cacheable;
import lombok.NonNull;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * @param <CACHEABLE> cache element type
 * @author Szymon Halastra, Alexandros Papageorgiou Koufidis, Brice Copy
 */
public interface C2monCache<CACHEABLE extends Cacheable> extends CacheDelegator<CACHEABLE> {

  // === Section: Cache properties ===

  CacheUpdateFlow<CACHEABLE> getCacheUpdateFlow();

  /**
   * Injected by the service, when needed
   */
  void setCacheUpdateFlow(CacheUpdateFlow<CACHEABLE> cacheFlow);

  CacheLoader getCacheLoader();

  void setCacheLoader(CacheLoader<CACHEABLE> cacheLoader);

  CacheListenerManager<CACHEABLE> getCacheListenerManager();

  void setCacheListenerManager(CacheListenerManager<CACHEABLE> CacheListenerManager);

  // === Section: Custom cache methods ===

  void init();

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
  Set<Long> getKeys();

  /**
   * Executes the {@code callable} provided, in the current thread's transaction, if one exists,
   * otherwise start a new one
   *
   * @implNote Make sure that you check for the existence of a transaction before starting a new one!
   */
  <T> T executeTransaction(Supplier<T> callable);

  /**
   * Alternative api to {@link C2monCache#executeTransaction(Supplier)} when you don't need the result
   *
   * @param runnable the {@code Runnable} you want to run
   */
  void executeTransaction(Runnable runnable);

  /**
   * Atomically gets an element from the cache with the given key, applies a transformation on it,
   * then puts it back in the cache. Will emit events!
   *
   * @throws CacheElementNotFoundException if the element with the given key doesn't exist
   */
  CACHEABLE compute(long key, Consumer<CACHEABLE> transformer);

  /**
   * Atomically gets an element from the cache with the given key, applies a transformation on it,
   * then puts it back in the cache. Will NOT emit events!
   *
   * @throws CacheElementNotFoundException if the element with the given key doesn't exist
   */
  CACHEABLE computeQuiet(long key, Consumer<CACHEABLE> transformer);

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
   * @throws NullPointerException when {@code filter} is null
   */
  Collection<CACHEABLE> query(@NonNull Function<CACHEABLE, Boolean> filter) throws NullPointerException;

  /**
   * Overload of {@link C2monCache#query(Function)} allowing the user to provide additional search parameters
   *
   * @param providedQuery must not be null, a CacheQuery to execute on the cache
   * @return a {@code Collection} of results, may be empty, never null
   * @throws NullPointerException when {@code providedQuery} is null
   * @see C2monCache#query(Function)
   */
  Collection<CACHEABLE> query(@NonNull CacheQuery<CACHEABLE> providedQuery) throws NullPointerException;

  /**
   * Put an element to the cache - inserts or updates if the element existed already
   * <p>
   * Does NOT notify listeners of the event!
   *
   * @throws NullPointerException when {@code value} is null
   */
  void putQuiet(long key, @NonNull CACHEABLE value) throws NullPointerException;
}