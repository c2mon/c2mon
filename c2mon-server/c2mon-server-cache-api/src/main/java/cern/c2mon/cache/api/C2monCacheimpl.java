package cern.c2mon.cache.api;

import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.flow.CacheUpdateFlow;
import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.cache.api.listener.CacheListenerManager;
import cern.c2mon.cache.api.listener.CacheListenerManagerImpl;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.Cacheable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static cern.c2mon.shared.common.CacheEvent.*;
import static java.util.Objects.requireNonNull;

@Slf4j
public abstract class C2monCacheimpl<CACHEABLE extends Cacheable> implements C2monCache<CACHEABLE> {

  protected final String cacheName;

  @Getter
  @Setter
  protected CacheUpdateFlow<CACHEABLE> cacheUpdateFlow = new DefaultCacheFlow<>();

  @Getter
  @Setter
  protected CacheLoader<CACHEABLE> cacheLoader;

  @Getter
  @Setter
  protected CacheListenerManager<CACHEABLE> cacheListenerManager = new CacheListenerManagerImpl<>();

  public C2monCacheimpl(String cacheName) {
    this.cacheName = cacheName;
  }

  @Override
  public Set<Long> getKeys() {
    return query(i -> true).stream().map(Cacheable::getId).collect(Collectors.toSet());
  }

  @Override
  public void executeTransaction(Runnable runnable) {
    executeTransaction(() -> {
      runnable.run();
      return 1;
    });
  }

  protected CACHEABLE compute(long key, Consumer<CACHEABLE> transformer, boolean notifyListeners) {
    return executeTransaction(() -> {
      CACHEABLE cachedObj = get(key);
      transformer.accept(cachedObj);
      if (notifyListeners)
        put(key, cachedObj); // TODO Write tests that verify this doesn't force cause a deadlock, as put will also get the obj
      else
        putQuiet(key, cachedObj);
      return cachedObj;
    });
  }

  @Override
  public CACHEABLE compute(long key, Consumer<CACHEABLE> transformer) {
    return compute(key, transformer, true);
  }

  @Override
  public CACHEABLE computeQuiet(long key, Consumer<CACHEABLE> transformer) {
    return compute(key, transformer, false);
  }

  @Override
  public void putQuiet(long key, CACHEABLE value) throws NullPointerException {
    put(key, value, false);
  }

  /**
   * Put an element to the cache - inserts or updates if the element existed already
   * <p>
   * Will generate {@link CacheEvent#UPDATE_ACCEPTED} events for all registered listeners
   *
   * @see C2monCache#putQuiet(long, Cacheable) to put without notifying
   */
  @Override
  public void put(@NonNull Long key, CACHEABLE value) throws NullPointerException {
    put(key, value, true);
  }

  protected void put(long key, CACHEABLE value, boolean notifyListeners) {
    value.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    final AtomicBoolean inserted = new AtomicBoolean(false);
    final AtomicReference<CACHEABLE> previous = new AtomicReference<>();
    executeTransaction(() -> {
      // Using this getter to avoid having to catch CacheElementNotFoundException
      // thrown by our own Cache.get
      previous.set(getCache().get(key));
      if (getCacheUpdateFlow().preInsertValidate(previous.get(), value)) {
        // We're in a transaction, so this can't have been modified
        getCache().put(key, value);
        inserted.set(true);
      }
    });
    if (notifyListeners)
      emitEvents(inserted.get(), previous.get(), value);
  }

  private void emitEvents(boolean success, CACHEABLE previousValue, CACHEABLE newValue) {
    if (success) {
      getCacheListenerManager().notifyListenersOf(UPDATE_ACCEPTED, newValue);
      getCacheUpdateFlow().postInsertEvents(previousValue, newValue)
        .forEach(event -> getCacheListenerManager().notifyListenersOf(event, newValue));
    } else
      getCacheListenerManager().notifyListenersOf(UPDATE_REJECTED, newValue);
  }

  @Override
  public boolean remove(@NonNull Long key) {
    CACHEABLE element = getCache().getAndRemove(key);

    if (element != null)
      getCacheListenerManager().notifyListenersOf(REMOVED, element);

    return element != null;
  }

  /**
   * @throws CacheElementNotFoundException when no value was found mapped to this key
   * @throws NullPointerException          when {@code key} is null
   */
  @Override
  public CACHEABLE get(@NonNull Long key) throws NullPointerException, CacheElementNotFoundException {
    requireNonNull(key);
    CACHEABLE result = getCache().get(key);
    if (result == null)
      throw new CacheElementNotFoundException("Did not find value for key: " + key);
    return result;
  }

  @Override
  public void close() {
    // It's quite possible that the cache is already closed, so check first
    synchronized (this) {
      if (!getCache().isClosed())
        getCache().close();
      // TODO (Alex) Close listener manager?
    }
  }
}
