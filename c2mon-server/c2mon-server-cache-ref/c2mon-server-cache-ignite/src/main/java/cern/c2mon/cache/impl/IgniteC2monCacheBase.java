package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.listener.Listener;
import cern.c2mon.cache.api.listener.ListenerService;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.cache.api.transactions.TransactionalCallable;
import cern.c2mon.shared.common.Cacheable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteSpringBean;
import org.apache.ignite.cache.query.Query;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.lang.IgniteClosure;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionDeadlockException;
import org.apache.ignite.transactions.TransactionTimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

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
public class IgniteC2monCacheBase<V extends Cacheable> implements C2monCache<V> {

  protected final String cacheName;

  @Getter
  @Setter
  protected CacheLoader<V> cacheLoader;

  protected CacheConfiguration<Long, V> cacheCfg;
  protected IgniteCache<Long, V> cache;
  private Listener<V> listenerService;
  @Autowired
  private IgniteSpringBean C2monIgnite;

  public IgniteC2monCacheBase(String cacheName) {
    this(cacheName, new DefaultIgniteCacheConfiguration<>(cacheName));
  }

  public IgniteC2monCacheBase(String cacheName, CacheConfiguration<Long, V> cacheCfg) {
    this.cacheName = cacheName;
    this.cacheCfg = cacheCfg;
    this.listenerService = new ListenerService<>();
  }

  @EventListener
  public void init(ContextRefreshedEvent event) {
    init();
  }

  public <T, R> QueryCursor<R> query(Query<T> query, IgniteClosure<T, R> closure) {
    return cache.query(query, closure);
  }

  @Override
  public Listener<V> getListenerService() {
    return listenerService;
  }

  @Override
  public void init() {
    cache = C2monIgnite.getOrCreateCache(cacheName);
    C2monIgnite.addCacheConfiguration(cacheCfg);

    if (cacheLoader != null) {
      cacheLoader.preload();
    }
  }

  @Override
  public <S> Optional<S> executeTransaction(TransactionalCallable<S> callable) {
    try (Transaction tx = C2monIgnite.transactions().txStart()) {

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


  // Cache methods

  @Override
  public V get(Long key) throws IllegalArgumentException {
    if (key instanceof Number) {
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
    cache.put(key, value);
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

  // ===========  Not yet done  ===========

  @Override
  public void loadAll(Set<? extends Long> set, boolean b, CompletionListener completionListener) {

  }

  @Override
  public V getAndPut(Long k, V v) {
    return null;
  }

  @Override
  public boolean putIfAbsent(Long k, V v) {
    return false;
  }

  @Override
  public boolean remove(Long k, V v) {
    return false;
  }

  @Override
  public V getAndRemove(Long k) {
    return null;
  }

  @Override
  public boolean replace(Long k, V v, V v1) {
    return false;
  }

  @Override
  public boolean replace(Long k, V v) {
    return false;
  }

  @Override
  public V getAndReplace(Long k, V v) {
    return null;
  }

  @Override
  public void removeAll(Set<? extends Long> set) {

  }

  @Override
  public void removeAll() {

  }

  @Override
  public void clear() {

  }

  @Override
  public <C extends Configuration<Long, V>> C getConfiguration(Class<C> aClass) {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public CacheManager getCacheManager() {
    return null;
  }

  @Override
  public void close() {

  }

  @Override
  public boolean isClosed() {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> aClass) {
    return null;
  }


  @Override
  public void registerCacheEntryListener(CacheEntryListenerConfiguration<Long, V> cacheEntryListenerConfiguration) {

  }

  @Override
  public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<Long, V> cacheEntryListenerConfiguration) {

  }

  @Override
  public Iterator<Entry<Long, V>> iterator() {
    return null;
  }
}
