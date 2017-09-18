package cern.c2mon.server.cache.alarm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractC2monCacheFactory;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.server.cache.C2monCacheName;
import cern.c2mon.server.cache.loading.AlarmLoaderDAO;
import cern.c2mon.server.cache.loading.common.BatchCacheLoader;
import cern.c2mon.server.common.alarm.Alarm;

/**
 * @author Szymon Halastra
 */
@Configuration
public class AlarmCacheConfig {

  @Autowired
  private AlarmLoaderDAO alarmLoaderDAO;

  @Bean(name = C2monCacheName.Names.ALARM)
  public C2monCache createCache(AbstractC2monCacheFactory cachingFactory) {
    C2monCache cache = cachingFactory.createCache(C2monCacheName.ALARM.getLabel(), Long.class, Alarm.class);

    C2monCacheLoader cacheLoader = new BatchCacheLoader<>(cache, alarmLoaderDAO);

    cache.setCacheLoader(cacheLoader).preload();

    return cache;
  }
}
