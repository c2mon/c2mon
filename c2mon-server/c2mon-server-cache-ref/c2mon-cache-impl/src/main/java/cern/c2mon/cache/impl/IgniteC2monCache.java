package cern.c2mon.cache.impl;

import java.util.List;
import java.util.concurrent.locks.Lock;

import javax.annotation.PostConstruct;

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
  protected V get(K key) {
    return cache.get(key);
  }

  @Override
  protected boolean containsKey(K key) {
    return cache.containsKey(key);
  }

  @Override
  protected void put(K key, V value) {
    cache.put(key, value);
  }

  @Override
  protected boolean remove(K key) {
    return cache.remove(key);
  }

  @Override
  protected String getName() {
    return ""; //TODO: rethink if this method should be here
  }

  @Override
  protected List getKeys() {
    return null; //TODO: rethink if this method is required
  }

  public void acquireLockOnKey(K key) {
    Lock lock = cache.lock(key);
    lock.lock();
  }

  public void releaseLockOnKey(K key) {
    lock.unlock();
  }
}
