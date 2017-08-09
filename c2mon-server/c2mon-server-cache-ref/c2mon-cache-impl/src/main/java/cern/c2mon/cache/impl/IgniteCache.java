package cern.c2mon.cache.impl;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.ignite.IgniteSpringBean;
import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.api.C2monCache;

/**
 * @author Szymon Halastra
 */
public class IgniteCache extends C2monCache {

  @Autowired
  protected IgniteSpringBean C2monIgnite;

  org.apache.ignite.IgniteCache cache;
  CacheConfiguration cacheCfg;

  public IgniteCache(CacheConfiguration cacheCfg) {
    this.cacheCfg = cacheCfg;
  }

  @PostConstruct
  public void init() {
    cache = C2monIgnite.getOrCreateCache(cacheCfg);
    C2monIgnite.addCacheConfiguration(cacheCfg);
  }

  @Override
  protected Object get(Object key) {
    return cache.get(key);
  }

  @Override
  protected boolean containsKey(Object key) {
    return cache.containsKey(key);
  }

  @Override
  protected void put(Object key, Object value) {
    cache.put(key, value);
  }

  @Override
  protected boolean remove(Object key) {
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
}
