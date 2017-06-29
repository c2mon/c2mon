package cern.c2mon.server.jcacheref;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;

import static org.junit.Assert.assertNotNull;

/**
 * @author Szymon Halastra
 */
@Slf4j
public class JCacheTest {

  CachingProvider provider;

  @Before
  public void init() {
    Config config = new ClasspathXmlConfig("hazelcast-test.xml");
    HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);

    provider = Caching.getCachingProvider();
  }

  @After
  public void clean() {
    Hazelcast.shutdownAll();
  }

  @Test
  public void checkProvider() {
    assertNotNull(provider);
  }

  @Test
  public void checkCacheManager() {
    CacheManager cacheManager = provider.getCacheManager();

    assertNotNull(cacheManager);
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
