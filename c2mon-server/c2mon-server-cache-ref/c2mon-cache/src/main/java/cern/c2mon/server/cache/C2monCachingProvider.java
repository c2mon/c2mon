package cern.c2mon.server.cache;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import cern.c2mon.cache.api.C2monCache;

/**
 * @author Szymon Halastra
 */
public class C2monCachingProvider {

  @Autowired
  private
  ApplicationContext applicationContext;

  private List<String> caches;

  public C2monCachingProvider() {
    caches = Arrays.asList(applicationContext.getBeanDefinitionNames());
  }

  public C2monCache getCache(C2monCacheName cacheName) {
    return null;
  }
}
