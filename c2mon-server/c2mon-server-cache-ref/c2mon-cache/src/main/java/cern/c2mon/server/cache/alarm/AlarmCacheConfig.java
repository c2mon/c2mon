package cern.c2mon.server.cache.alarm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractC2monCacheFactory;
import cern.c2mon.cache.api.loader.C2monCacheLoader;
import cern.c2mon.server.cache.C2monCacheName;
import cern.c2mon.server.cache.loader.AlarmLoaderDAO;
import cern.c2mon.server.cache.loader.common.BatchCacheLoader;
import cern.c2mon.server.cache.loader.config.CacheLoaderProperties;
import cern.c2mon.server.common.alarm.Alarm;

/**
 * @author Szymon Halastra
 */
@Configuration
public class AlarmCacheConfig{

  @Autowired
  private ThreadPoolTaskExecutor cacheLoaderTaskExecutor;

  @Autowired
  private CacheLoaderProperties properties;

  @Bean(name = C2monCacheName.Names.ALARM)
  public C2monCache createCache(AbstractC2monCacheFactory cachingFactory, AlarmLoaderDAO alarmLoaderDAORef) {
    C2monCache cache = cachingFactory.createCache(C2monCacheName.ALARM.getLabel(), Long.class, Alarm.class);

    C2monCacheLoader cacheLoader = new BatchCacheLoader<Long, Alarm>(cacheLoaderTaskExecutor, cache, alarmLoaderDAORef,
            properties.getBatchSize(), "AlarmCacheLoader-");

    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
