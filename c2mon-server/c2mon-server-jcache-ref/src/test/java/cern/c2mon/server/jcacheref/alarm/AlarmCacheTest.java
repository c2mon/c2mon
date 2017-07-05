package cern.c2mon.server.jcacheref.alarm;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.jcacheref.HazelcastBaseTestingSetup;

import static org.junit.Assert.assertNotNull;

/**
 * @author Szymon Halastra
 */
public class AlarmCacheTest extends HazelcastBaseTestingSetup {

  CachingProvider provider;

  @Before
  public void setup() {
    provider = Caching.getCachingProvider();
  }

  @Test
  public void checkAlarmCacheExistence() {
    CacheManager cacheManager = provider.getCacheManager();

    Cache<Long, Alarm> alarmCache = cacheManager.getCache("alarmTagCache", Long.class, Alarm.class);

    assertNotNull(alarmCache);
  }

  @Test
  public void putAndGetAlarmFromCache() {
    CacheManager cacheManager = provider.getCacheManager();

    Cache<Long, Alarm> alarmCache = cacheManager.getCache("alarmTagCache", Long.class, Alarm.class);

    AlarmCacheObject alarm = new AlarmCacheObject(10L);

    assertNotNull(alarmCache);

    alarmCache.put(alarm.getId(), alarm);

    assertNotNull(alarmCache.get(10L));
  }
}
