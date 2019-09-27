package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.listener.Listener;
import cern.c2mon.cache.api.listener.ListenerService;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.cache.api.spi.CacheQuery;
import cern.c2mon.cache.api.transactions.TransactionalCallable;
import cern.c2mon.shared.common.Cacheable;
import javax.cache.Cache;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.Query;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.lang.IgniteClosure;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionDeadlockException;
import org.apache.ignite.transactions.TransactionTimeoutException;

import javax.annotation.PostConstruct;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import java.util.*;

@Slf4j
public class IgniteC2monCache<V extends Cacheable> implements C2monCache<V> {

  protected final String cacheName;
  private final Ignite igniteInstance;

  @Getter
  @Setter
//  TODO This should be activated and connected
  protected CacheLoader<V> cacheLoader;

  protected CacheConfiguration<Long, V> cacheCfg;
  protected IgniteCache<Long, V> cache;
  private Listener<V> listenerService;


  public IgniteC2monCache(String cacheName, CacheConfiguration<Long, V> cacheCfg, Ignite igniteInstance) {
    this.cacheName = cacheName;
    this.cacheCfg = cacheCfg;
    this.igniteInstance = igniteInstance;
    this.listenerService = new ListenerService<>();
  }

  @Override
  @PostConstruct
  public void init() {
    igniteInstance.addCacheConfiguration(cacheCfg);
    cache = igniteInstance.getOrCreateCache(cacheName);

    if (cacheLoader != null) {
      cacheLoader.preload();
    }
  }

  public <T, R> QueryCursor<R> query(Query<T> query, IgniteClosure<T, R> closure) {
    return cache.query(query, closure);
  }

  @Override
  public Collection<V> query(CacheQuery<V> providedQuery) {
    return cache.query(new ScanQuery<>(new IgniteC2monBiPredicate<>(providedQuery)),
        Entry::getValue).getAll();
  }

  @Override
  public Listener<V> getListenerService() {
    return listenerService;
  }

  @Override
  public <S> Optional<S> executeTransaction(TransactionalCallable<S> callable) {
    try (Transaction tx = igniteInstance.transactions().txStart()) {

      S returnValue = callable.call();

      tx.commit();

      return Optional.of(returnValue);
    } catch (CacheException e) {
      if (e.getCause() instanceof TransactionTimeoutException &&
        e.getCause().getCause() instanceof TransactionDeadlockException) {
        log.error("DeadLock occurred", e.getCause().getCause().getMessage());
      }
    }

    return Optional.empty();
  }

  @Override
  public void executeTransaction(Runnable runnable) {
    try (Transaction tx = igniteInstance.transactions().txStart()) {

      runnable.run();

      tx.commit();
    } catch (CacheException e) {
      if (e.getCause() instanceof TransactionTimeoutException &&
        e.getCause().getCause() instanceof TransactionDeadlockException) {
        log.error("DeadLock occurred", e.getCause().getCause().getMessage());
      }
    }
  }


  // Cache methods

  @Override
  public V get(Long key) throws IllegalArgumentException {
    if (key != null) {
      return cache.get(key);
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public boolean containsKey(Long key) {
    return cache.containsKey(key);
  }

  @Override
  public void put(Long key, V value) {
    if (value != null && key != null) {
      cache.put(key, value);
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public boolean remove(Long key) {
    return cache.remove(key);
  }

  @Override
  public void putAll(Map<? extends Long, ? extends V> map) {
    cache.putAll(map);
  }

  @Override
  public Map<Long, V> getAll(Set<? extends Long> keys) {
    return cache.getAll(keys);
  }

  @Override
  public <T> T invoke(Long var1, EntryProcessor<Long, V, T> var2, Object... var3) throws EntryProcessorException {
    return cache.invoke(var1, var2, var3);
  }

  @Override
  public <T> Map<Long, EntryProcessorResult<T>> invokeAll(Set<? extends Long> var1, EntryProcessor<Long, V, T> var2, Object... var3) {
    return cache.invokeAll(var1, var2, var3);
  }

  @Override
  public Set<Long> getKeys() {
    Set<Long> keys = new TreeSet<>();
    cache.query(new ScanQuery<>(null)).forEach(objectObjectEntry -> keys.add((Long) objectObjectEntry.getKey()));

    return keys;
  }

  @Override
  public void loadAll(Set<? extends Long> set, boolean b, CompletionListener completionListener) {
    cache.loadAll(set, b, completionListener);
  }

  @Override
  public V getAndPut(Long k, V v) {
    return cache.getAndPut(k, v);
  }

  @Override
  public boolean putIfAbsent(Long k, V v) {
    return cache.putIfAbsent(k, v);
  }

  @Override
  public boolean remove(Long k, V v) {
    return cache.remove(k, v);
  }

  @Override
  public V getAndRemove(Long k) {
    return cache.getAndRemove(k);
  }

  @Override
  public boolean replace(Long k, V v, V v1) {
    return cache.replace(k, v, v1);
  }

  @Override
  public boolean replace(Long k, V v) {
    return cache.replace(k, v);
  }

  @Override
  public V getAndReplace(Long k, V v) {
    return cache.getAndReplace(k, v);
  }

  @Override
  public void removeAll(Set<? extends Long> set) {
    cache.removeAll(set);
  }

  @Override
  public void removeAll() {
    cache.removeAll();
  }

  @Override
  public void clear() {
    cache.clear();
  }

  @Override
  public <C extends Configuration<Long, V>> C getConfiguration(Class<C> aClass) {
    return cache.getConfiguration(aClass);
  }

  @Override
  public String getName() {
    return cacheName;
  }

  @Override
  public CacheManager getCacheManager() {
    return cache.getCacheManager();
  }

  @Override
  public void close() {
    cache.close();
  }

  @Override
  public boolean isClosed() {
    return cache.isClosed();
  }

  @Override
  public <T> T unwrap(Class<T> aClass) {
    return cache.unwrap(aClass);
  }


  @Override
  public void registerCacheEntryListener(CacheEntryListenerConfiguration<Long, V> cacheEntryListenerConfiguration) {
    cache.registerCacheEntryListener(cacheEntryListenerConfiguration);
  }

  @Override
  public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<Long, V> cacheEntryListenerConfiguration) {
    cache.deregisterCacheEntryListener(cacheEntryListenerConfiguration);
  }

  @Override
  public Iterator<Entry<Long, V>> iterator() {
    return cache.iterator();
  }
}
