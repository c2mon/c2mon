package cern.c2mon.server.jcacheref.various.general;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;

import cern.c2mon.server.common.tag.Tag;

/**
 * @author Szymon Halastra
 */
public class C2monCache<K, V> implements CommonCache<K, Tag> {

  private Cache<K, V> cache;
  private MutableConfiguration<K, V> configuration;

  public C2monCache() {
    CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();

    this.configuration = new MutableConfiguration<>();
    this.cache = cacheManager.createCache("Generated name", this.configuration);
  }

  public void configureCache(/*it should be a c2mon cache object wrapper*/Object obj) {
    /*define a URI for CachingProvider */
    CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();

    /*deifne configurations for generated caches */
    MutableConfiguration configuration = null;

    cacheManager.createCache("Here it should be a cache name caught during generation", configuration);
  }

  @Override
  public Long getIdByName(String name) {
    return null;
  }

  @Override
  public String getCacheName() {
    return cache.getName();
  }

  @Override
  public void setCacheInitializedKey() {

  }

  public C2monCache<K, V> setManagementEnabled(boolean isManagementEnabled) {
    this.configuration.setManagementEnabled(isManagementEnabled);
    return this;
  }

  public C2monCache<K, V> setStatisticsEnabled(boolean isStatisticsEnabled) {
    this.configuration.setStatisticsEnabled(isStatisticsEnabled);
    return this;
  }
}