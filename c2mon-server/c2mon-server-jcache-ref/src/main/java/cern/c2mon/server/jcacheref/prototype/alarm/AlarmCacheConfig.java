package cern.c2mon.server.jcacheref.prototype.alarm;

import java.io.Serializable;

import javax.cache.Cache;
import javax.cache.CacheManager;

import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.config.C2monCacheName;
import cern.c2mon.server.jcacheref.prototype.BasicCache;

/**
 * @author Szymon Halastra
 */

@Configuration
public class AlarmCacheConfig implements BasicCache, Serializable {

  private static final String ALARM_TAG_CACHE = "alarmTagCache";

  @Bean(name = ALARM_TAG_CACHE)
  public Cache<Long, Alarm> getAlarmTagCache(JCacheCacheManager cacheManager) {
    CacheManager cm = cacheManager.getCacheManager();
    return cm.getCache(ALARM_TAG_CACHE, Long.class, Alarm.class);
  }

  @Override
  public C2monCacheName getName() {
    return C2monCacheName.ALARM;
  }
}
