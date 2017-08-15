package cern.c2mon.cache.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import javax.annotation.PostConstruct;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteSpringBean;
import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.api.C2monCache;

/**
 * @author Szymon Halastra
 */
public class IgniteC2monCache<K, V> extends C2monCache<K, V> {

  private Lock lock; //TODO: move to external class

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
  public List getKeys() {
    return null; //TODO: rethink if this method is required
  }

  @Override
  public <T> T invoke(K var1, EntryProcessor<K, V, T> var2, Object... var3) throws EntryProcessorException {
    return cache.invoke(var1, var2, var3);
  }

  @Override
  public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> var1, EntryProcessor<K, V, T> var2, Object... var3) {
    return cache.invokeAll(var1, var2, var3);
  }

  public void acquireLockOnKey(K key) {
    Lock lock = cache.lock(key);
    lock.lock();
  }

  public void releaseLockOnKey(K key) {
    lock.unlock();
  }

  @Override
  public void preload() {
    cache.loadCacheAsync(null);
  }
}
