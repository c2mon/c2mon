package cern.c2mon.cache.api;

import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.flow.C2monCacheUpdateFlow;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.cache.api.spi.CacheQuery;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.Cacheable;
import lombok.NonNull;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static cern.c2mon.shared.common.CacheEvent.UPDATE_ACCEPTED;
import static cern.c2mon.shared.common.CacheEvent.UPDATE_REJECTED;
import static java.util.Objects.requireNonNull;


/**
 * @param <V> cache element type
 * @author Szymon Halastra, Alexandros Papageorgiou Koufidis, Brice Copy
 */
public interface C2monCache<V extends Cacheable> extends CacheDelegator<V>, Serializable, ListenerDelegator<V> {

  C2monCacheUpdateFlow<V> getCacheUpdateFlow();

  /**
   * Injected by the service, when needed
   */
  void setCacheUpdateFlow(C2monCacheUpdateFlow<V> cacheFlow);

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
  default void executeTransaction(Runnable runnable) {
    executeTransaction(() -> {
      runnable.run();
      return 1;
    });
  }

  /**
   * Atomically gets an element from the cache with the given key, applies a transformation on it,
   * then puts it back in the cache. Will emit events!
   *
   * @throws CacheElementNotFoundException if the element with the given key doesn't exist
   */
  default V compute(long key, Consumer<V> transformer) {
    return executeTransaction(() -> {
      V cachedObj = getCache().get(key);
      transformer.accept(cachedObj);
      put(key, cachedObj); // TODO Write tests that verify this doesn't force cause a deadlock, as put will also get the obj
      return cachedObj;
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
   * @throws NullPointerException when {@code filter} is null
   */
  Collection<V> query(@NonNull Function<V, Boolean> filter) throws NullPointerException;

  /**
   * Overload of {@link C2monCache#query(Function)} allowing the user to provide additional search parameters
   *
   * @param providedQuery must not be null, a CacheQuery to execute on the cache
   * @return a {@code Collection} of results, may be empty, never null
   * @throws NullPointerException when {@code providedQuery} is null
   * @see C2monCache#query(Function)
   */
  Collection<V> query(@NonNull CacheQuery<V> providedQuery) throws NullPointerException;

  /**
   * Put an element to the cache - inserts or updates if the element existed already
   * <p>
   * Does NOT notify listeners of the event!
   *
   * @throws NullPointerException when {@code value} is null
   */
  default void putQuiet(long key, @NonNull V value) throws NullPointerException {
    put(key, value, false);
  }

  default void put(long key, @NonNull V value, boolean notifyListeners) {
    value.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    executeTransaction(() -> {
      // Using this getter to avoid having to catch CacheElementNotFoundException
      // thrown by our own Cache.get
      V previous = getCache().get(key);
      if (getCacheUpdateFlow().preInsertValidate(previous, value)) {
        // We're in a transaction, so this can't have been modified
        getCache().put(key, value);
        if (notifyListeners) {
          notifyListenersOf(UPDATE_ACCEPTED, value);
          getCacheUpdateFlow().postInsertEvents(previous, value).forEach(event -> notifyListenersOf(event, value));
        }
      } else if (notifyListeners) {
        notifyListenersOf(UPDATE_REJECTED, value);
      }
    });
  }

  // === Section: C2MON overrides of javax.cache.Cache methods ===

  /**
   * @throws CacheElementNotFoundException when no value was found mapped to this key
   * @throws NullPointerException          when {@code key} is null
   */
  @Override
  default V get(@NonNull Long key) throws NullPointerException, CacheElementNotFoundException {
    requireNonNull(key);
    V result = getCache().get(key);
    if (result == null)
      throw new CacheElementNotFoundException("Did not find value for key: " + key);
    return result;
  }

  /**
   * Put an element to the cache - inserts or updates if the element existed already
   * <p>
   * Will generate {@link CacheEvent#UPDATE_ACCEPTED} events for all registered listeners
   *
   * @see C2monCache#putQuiet(long, Cacheable) to put without notifying
   */
  @Override
  default void put(@NonNull Long key, @NonNull V value) throws NullPointerException {
    put(key, value, true);
  }

  @Override
  default void close() {
    // It's quite possible that the cache is already closed, so check first
    synchronized (this) {
      if (!getCache().isClosed())
        getCache().close();
    }
  }
}