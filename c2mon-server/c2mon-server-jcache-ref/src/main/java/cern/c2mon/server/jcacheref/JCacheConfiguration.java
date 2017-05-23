package cern.c2mon.server.jcacheref;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;

import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author Szymon Halastra
 */

@EnableAspectJAutoProxy
public class JCacheConfiguration {


  class C2monCache<K, V> {
    Cache<K, V> cache;
    Configuration<K, V> configuration;
  }

  interface InnerCache<K, V> {
    Long getIdByName(String name);
    String getCacheName();
  }

  public void configureCache(/*it should be a c2mon cache object wrapper*/Object obj) {
    /*define a URI for CachingProvider */
    CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();

    /*deifne configurations for generated caches */
    MutableConfiguration configuration = null;

    cacheManager.createCache("Here it should be a cache name caught during generation", configuration);
  }
}
