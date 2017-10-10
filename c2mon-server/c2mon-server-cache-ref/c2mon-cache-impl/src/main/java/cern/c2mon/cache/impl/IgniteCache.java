package cern.c2mon.cache.impl;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;

import javax.annotation.PostConstruct;
import javax.cache.CacheException;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.IgniteSpringBean;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionDeadlockException;
import org.apache.ignite.transactions.TransactionTimeoutException;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.transactions.TransactionalCallable;
import cern.c2mon.shared.common.Cacheable;

/**
 * @author Szymon Halastra
 */
@Slf4j
public class IgniteCache<K, V extends Cacheable> extends Cache<K, V> {

  @Autowired
  private IgniteSpringBean C2monIgnite;

  private org.apache.ignite.IgniteCache<K, V> cache;
  private CacheConfiguration<K, V> cacheCfg;

  public IgniteCache(String cacheName, CacheConfiguration cacheCfg) {
    super(cacheName);
    this.cacheCfg = cacheCfg;
  }

  @PostConstruct
  public void init() {
    cache = C2monIgnite.getOrCreateCache(cacheCfg);
    C2monIgnite.addCacheConfiguration(cacheCfg);

    if (getCacheLoader() != null) {
      this.getCacheLoader().preload();
    }
  }

  @Override
  public V get(K key) throws IllegalArgumentException {
    if (key instanceof Number) {
      return cache.get(key);
    }
    else {
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
  public Set<K> getKeys() {
    Set<K> keys = new TreeSet<>();
    cache.query(new ScanQuery<>(null)).forEach(objectObjectEntry -> keys.add((K) objectObjectEntry.getKey()));

    return keys;
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
  public void lockOnKey(K key) {
    Lock lock = cache.lock(key);

    lock.lock();
  }

  @Override
  public void unlockOnKey(K key) {
    Lock lock = cache.lock(key);

    lock.unlock();
  }

  @Override
  public <S> Optional<S> executeTransaction(TransactionalCallable<S> callable) {
    try (Transaction tx = C2monIgnite.transactions().txStart()) {

      S returnValue = callable.call();

      tx.commit();

      return Optional.of(returnValue);
    }
    catch (CacheException e) {
      if (e.getCause() instanceof TransactionTimeoutException &&
              e.getCause().getCause() instanceof TransactionDeadlockException) {
        log.error("DeadLock occurred", e.getCause().getCause().getMessage());
      }
    }

    return Optional.empty();
  }
}
