package cern.c2mon.server.cache.alarm.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.cache.alarm.C2monCacheConfig;
import cern.c2mon.server.cache.loader.AlarmLoaderDAO;
import cern.c2mon.server.common.alarm.Alarm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
public class AlarmCacheConfig extends C2monCacheConfig<Alarm> {

  @Bean(name = CacheName.Names.ALARM)
  @Autowired
  public C2monCache<Alarm> createCache(AlarmLoaderDAO alarmLoaderDAORef) {
    return super.createCache(alarmLoaderDAORef, CacheName.ALARM.getLabel(), Alarm.class, "AlarmCacheLoader-");
  }
}
