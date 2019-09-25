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

  /**
   * Tests the get method retrieves an existing Alarm correctly.
   */
  @Test
  public void testGet() {
    AlarmCacheObject cacheObject = (AlarmCacheObject) alarmCacheRef.get(350000L);
    AlarmCacheObject objectInDb = (AlarmCacheObject) alarmMapper.getItem(350000L);
    CacheObjectComparison.equals(cacheObject, objectInDb);
  }
}
