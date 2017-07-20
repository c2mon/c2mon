package cern.c2mon.server.jcacheref.prototype.common;

import javax.annotation.PostConstruct;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteSpringBean;
import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Szymon Halastra
 */
public abstract class AbstractCacheRef<K, V> implements C2monCacheConfiguration {

  @Autowired
  protected IgniteSpringBean C2monIgnite;

  protected IgniteCache<K, V> cache;

  public AbstractCacheRef() {
  }

  @PostConstruct
  @Override
  public void init() {
    CacheConfiguration<K, V> cacheCfg = configureCache();
    cache = C2monIgnite.getOrCreateCache(cacheCfg);
    C2monIgnite.addCacheConfiguration(cacheCfg);
  }

  protected abstract CacheConfiguration<K, V> configureCache();
}
