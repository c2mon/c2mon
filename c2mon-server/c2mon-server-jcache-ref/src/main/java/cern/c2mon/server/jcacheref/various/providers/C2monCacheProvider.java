package cern.c2mon.server.jcacheref.various.providers;

import javax.annotation.PostConstruct;
import javax.cache.Caching;

/**
 * @author Szymon Halastra
 */
public class C2monCacheProvider {

  Iterable<String> cacheNames;

  @PostConstruct
  public void init() {
    cacheNames = Caching.getCachingProvider().getCacheManager().getCacheNames();
  }

  /*
  * Provides getters to all defined caches in CacheManager, question is how to make it clean and efficient?
  */
}
