package cern.c2mon.server.jcacheref;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Szymon Halastra
 */

@Slf4j
public class C2monCache<K, V> {

  private Cache<K, V> cache;
  private Configuration<K, V> cacheConfiguration;

  public C2monCache(String cacheName) {
    cacheConfiguration = new MutableConfiguration<>(createConfiguration());
    cache = Caching.getCachingProvider().getCacheManager().createCache(cacheName, cacheConfiguration);
  }

  private Configuration<K, V> createConfiguration() {
    MutableConfiguration<K, V> configuration = new MutableConfiguration<>();

    configuration.setManagementEnabled(true);

    return configuration;
  }

  @ReadCacheGuard
  public V get(K key) {
    return cache.get(key);
  }

  @WriteCacheGuard
  public void put(K key, V value) {
    cache.put(key, value);
  }

  /* Locking should be done on an object in a cluster, so a call would not be able to modify the object,
   * maybe I don't need my implementation, I should use a lock provided by cluster implementation and just create an abstract layer,
   * as it is done in Spring Boot and CachingProvider classes
   */
}
