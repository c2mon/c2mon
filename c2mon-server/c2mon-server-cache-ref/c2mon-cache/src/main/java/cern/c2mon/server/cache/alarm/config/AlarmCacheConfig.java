package cern.c2mon.server.cache.alarm.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.cache.alarm.AbstractCacheConfig;
import cern.c2mon.server.cache.loader.AlarmLoaderDAO;
import cern.c2mon.server.cache.loader.config.CacheLoaderProperties;
import cern.c2mon.server.common.alarm.Alarm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
public class AlarmCacheConfig extends AbstractCacheConfig<Alarm> {

  public AlarmCacheConfig(ThreadPoolTaskExecutor cacheLoaderTaskExecutor, CacheLoaderProperties properties, AbstractCacheFactory cachingFactory) {
    super(cacheLoaderTaskExecutor, properties, cachingFactory);
  }

  @Bean(name = CacheName.Names.ALARM)
  @Autowired
  public C2monCache<Alarm> createCache(AlarmLoaderDAO alarmLoaderDAORef) {
    return super.createCache(alarmLoaderDAORef, CacheName.ALARM.getLabel(), Alarm.class, "AlarmCacheLoader-");
  }
}
