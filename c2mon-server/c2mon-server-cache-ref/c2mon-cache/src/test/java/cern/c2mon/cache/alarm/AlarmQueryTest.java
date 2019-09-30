package cern.c2mon.cache.alarm;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
public class AlarmQueryTest extends AlarmCacheLoaderTest {

  @Test
  public void testFindAlarms() {
    Collection<Alarm> result = cache.query(alarm -> alarm.getFaultFamily().matches("TEST_.*"));
    assertNotNull(result);
    assertEquals("Search result != 4", 4, result.size());
  }

  @Test
  public void testGetActiveAlarms() {
    AlarmCacheObject toChange = (AlarmCacheObject) cache.get(350000L);
    toChange.setActive(true);

    cache.put(toChange.getId(), toChange);
    Collection<Alarm> result = cache.query(Alarm::isActive);
    assertNotNull(result);
    assertEquals("Search result != 1", 1, result.size());
  }

  @Test
  public void testFindOscillatingAlarms() {
    Collection<Alarm> result = cache.query(Alarm::isOscillating);
    assertNotNull(result);
    assertEquals("Search result != 4", 4, result.size());
  }

  @Test
  public void testGetAlarmsByCodeAndFamily() {
    Collection<Alarm> result = cache.query(alarm ->
      alarm.getFaultFamily().startsWith("TEST_") && alarm.getFaultCode() == 20);
    assertNotNull(result);
    assertEquals("Search result != 4", 4, result.size());
  }
}
