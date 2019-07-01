package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.C2monCacheBase;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.cache.api.transactions.TransactionalCallable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteSpringBean;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionDeadlockException;
import org.apache.ignite.transactions.TransactionTimeoutException;
import org.springframework.beans.factory.annotation.Autowired;

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
public class IgniteC2monCacheBase<K, V> implements C2monCacheBase<K, V> {

  protected final String cacheName;

  @Getter
  @Setter
  protected CacheLoader<K, V> cacheLoader;

  protected CacheConfiguration<K, V> cacheCfg;
  protected IgniteCache<K, V> cache;

  @Autowired
  private IgniteSpringBean C2monIgnite;


  public IgniteC2monCacheBase(String cacheName, CacheConfiguration<K, V> cacheCfg) {
    this.cacheName = cacheName;
    this.cacheCfg = cacheCfg;
  }

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
  public V get(K key) throws IllegalArgumentException {
    if (key instanceof Number) {
      return cache.get(key);
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public boolean containsKey(K key) {
    return cache.containsKey(key);
  }

  @Override
  public void put(K key, V value) {
    cache.put(key, value);
  }

  @Override
  public boolean remove(K key) {
    return cache.remove(key);
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> map) {
    cache.putAll(map);
  }

  @Override
  public Map<K, V> getAll(Set<? extends K> keys) {
    return cache.getAll(keys);
  }

  @Override
  public <T> T invoke(K var1, EntryProcessor<K, V, T> var2, Object... var3) throws EntryProcessorException {
    return cache.invoke(var1, var2, var3);
  }

  @Override
  public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> var1, EntryProcessor<K, V, T> var2, Object... var3) {
    return cache.invokeAll(var1, var2, var3);
  }

  @Override
  public Set<K> getKeys() {
    Set<K> keys = new TreeSet<>();
    cache.query(new ScanQuery<>(null)).forEach(objectObjectEntry -> keys.add((K) objectObjectEntry.getKey()));

    return keys;
  }

  @Override
  public void loadAll(Set<? extends K> set, boolean b, CompletionListener completionListener) {

  }

  @Override
  public V getAndPut(K k, V v) {
    return null;
  }

  @Override
  public boolean putIfAbsent(K k, V v) {
    return false;
  }

  @Override
  public boolean remove(K k, V v) {
    return false;
  }

  @Override
  public V getAndRemove(K k) {
    return null;
  }

  @Override
  public boolean replace(K k, V v, V v1) {
    return false;
  }

  @Override
  public boolean replace(K k, V v) {
    return false;
  }

  @Override
  public V getAndReplace(K k, V v) {
    return null;
  }

  @Override
  public void removeAll(Set<? extends K> set) {

  }

  @Override
  public void removeAll() {

  }

  @Override
  public void clear() {

  }

  @Override
  public <C extends Configuration<K, V>> C getConfiguration(Class<C> aClass) {
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
  public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {

  }

  @Override
  public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {

  }

  @Override
  public Iterator<Entry<K, V>> iterator() {
    return null;
  }
}
