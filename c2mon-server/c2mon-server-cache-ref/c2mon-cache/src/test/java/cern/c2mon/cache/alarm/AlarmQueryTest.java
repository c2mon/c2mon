package cern.c2mon.cache.alarm;

import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.api.C2monCacheBase;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.shared.client.alarm.AlarmQuery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Szymon Halastra
 */

//TODO: remove @Ignore annotation when Query API will be created
public class AlarmQueryTest extends AlarmCacheLoaderTest {

  @Autowired
  private C2monCacheBase<Alarm> alarmCacheRef;

  @Test
  @Ignore
  public void testFindAlarms() {
    AlarmQuery query = AlarmQuery.builder().faultFamily("TEST_*").build();

    Collection<Long> result = null; /*alarmCacheRef.findAlarm(query);*/
    assertNotNull(result);
    assertEquals("Search result != 4", 4, result.size());
  }

  @Test
  @Ignore
  public void testGetActiveAlarms() {
    AlarmQuery query = AlarmQuery.builder().active(true).build();
    AlarmCacheObject toChange = (AlarmCacheObject) alarmCacheRef.get(350000L);
//    toChange.setState("ACTIVE");

    alarmCacheRef.put(toChange.getId(), toChange);
    Collection<Long> result = null; /*alarmCacheRef.findAlarm(query);*/
    assertNotNull(result);
    assertEquals("Search result != 1", 1, result.size());
  }

  @Test
  @Ignore
  public void testGetAlarmsByCodeAndFamily() {
    AlarmQuery query = AlarmQuery.builder().faultFamily("TEST_*").faultCode(20).build();
    Collection<Long> result = null; /*alarmCacheRef.findAlarm(query);*/
    assertNotNull(result);
    assertEquals("Search result != 4", 4, result.size());
  }
}
