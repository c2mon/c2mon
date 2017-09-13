package cern.c2mon.cache.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import javax.annotation.PostConstruct;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteSpringBean;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.transactions.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.lock.TransactionalCallable;

/**
 * @author Szymon Halastra
 */
public class IgniteC2monCache<K, V> extends C2monCache<K, V> {

  @Autowired
  private IgniteSpringBean C2monIgnite;

  private IgniteCache<K, V> cache;
  private CacheConfiguration<K, V> cacheCfg;

  public IgniteC2monCache(CacheConfiguration cacheCfg) {
    this.cacheCfg = cacheCfg;
  }

  @PostConstruct
  public void init() {
    cache = C2monIgnite.getOrCreateCache(cacheCfg);
    C2monIgnite.addCacheConfiguration(cacheCfg);
  }

  @Override
  public V get(K key) {
    return cache.get(key);
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
  public String getName() {
    return ""; //TODO: rethink if this method should be here
  }

  @Override
  public List<K> getKeys() {
    List<K> keys = new ArrayList<>();
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
  public void loadFromDb(K id) {

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
  public void executeTransaction(TransactionalCallable callable) {
    try (Transaction tx = C2monIgnite.transactions().txStart()) {

      callable.call();

      tx.commit();
    }
  }
}
