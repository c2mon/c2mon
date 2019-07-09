package cern.c2mon.cache.alarm;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.AlarmMapper;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.test.CacheObjectComparison;

/**
 * @author Szymon Halastra
 */
public class AlarmCacheTest extends AlarmCacheLoaderTest {

  @Autowired
  private C2monCache<Alarm> alarmCacheRef;

  @Autowired
  private AlarmMapper alarmMapper;

  /**
   * If null is used as  a key, an exception should be thrown.
   */
  @Test(expected = IllegalArgumentException.class)
  @Ignore
  public void testGetWithNull() {
    //test robustness to null call
    alarmCacheRef.get(null);
  }

  /**
   * Tests the get method retrieves an existing Alarm correctly.
   */
  @Test
  @Ignore
  public void testGet() {
    AlarmCacheObject cacheObject = (AlarmCacheObject) alarmCacheRef.get(350000L);
    AlarmCacheObject objectInDb = (AlarmCacheObject) alarmMapper.getItem(350000L);
    CacheObjectComparison.equals(cacheObject, objectInDb);
  }
}
