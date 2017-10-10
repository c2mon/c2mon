package cern.c2mon.server.cache.alarm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.api.loader.CacheLoader;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.cache.loader.AlarmLoaderDAO;
import cern.c2mon.server.cache.loader.common.BatchCacheLoader;
import cern.c2mon.server.cache.loader.config.CacheLoaderProperties;
import cern.c2mon.server.common.alarm.Alarm;

/**
 * @author Szymon Halastra
 */
@Configuration
public class AlarmCacheConfig {

  @Autowired
  private ThreadPoolTaskExecutor cacheLoaderTaskExecutor;

  @Autowired
  private CacheLoaderProperties properties;

  @Bean(name = CacheName.Names.ALARM)
  public Cache createCache(AbstractCacheFactory cachingFactory, AlarmLoaderDAO alarmLoaderDAORef) {
    Cache cache = cachingFactory.createCache(CacheName.ALARM.getLabel(), Long.class, Alarm.class);

    CacheLoader cacheLoader = new BatchCacheLoader<Long, Alarm>(cacheLoaderTaskExecutor, cache, alarmLoaderDAORef,
            properties.getBatchSize(), "AlarmCacheLoader-");

    cache.setCacheLoader(cacheLoader);

    return cache;
  }
}
