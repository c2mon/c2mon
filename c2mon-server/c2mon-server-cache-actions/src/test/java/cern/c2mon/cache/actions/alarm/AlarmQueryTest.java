package cern.c2mon.cache.actions.alarm;

import cern.c2mon.cache.actions.AbstractCacheTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.test.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
public class AlarmQueryTest extends AbstractCacheTest<Alarm, AlarmCacheObject> {

  @Inject
  private C2monCache<Alarm> alarmCacheRef;

  @Test
  public void testFindAlarms() {
    Collection<Alarm> result = cache.query(alarm -> alarm.getFaultFamily().matches("TEST_.*"));
    assertNotNull(result);
    assertEquals("Search result != 4", 4, result.size());
  }

  @Test
  public void testGetActiveAlarms() {
    long alarmId = 350000L;
    int count = 1;
    
    while (alarmId <= 350003L) {
      AlarmCacheObject toChange = (AlarmCacheObject) cache.get(alarmId++);
      toChange.setActive(true);
      cache.put(toChange.getId(), toChange);
      
      Collection<Alarm> result = cache.query(Alarm::isActive);
      assertNotNull(result);
      assertEquals("Search result != " + count, count++, result.size());
    }
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

  @Override
  protected C2monCache<Alarm> initCache() {
    return alarmCacheRef;
  }

  @Override
  protected AbstractCacheObjectFactory<AlarmCacheObject> initFactory() {
    return null;
  }
}
