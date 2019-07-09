package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.C2monCacheBase;
import cern.c2mon.cache.impl.configuration.C2monIgniteConfiguration;
import cern.c2mon.server.cache.CacheModuleRef;
import cern.c2mon.server.cache.alarm.config.AlarmCacheConfig;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.config.CommonModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
  CommonModule.class,
  CacheModuleRef.class,
  C2monIgniteConfiguration.class,
  AlarmCacheConfig.class
})
public class AutoWireTest {

  @Autowired
  private C2monCacheBase<Alarm> alarmCacheRef;

  @Test
  public void simpleWire() {
    assertNotEquals(alarmCacheRef, null);
    Alarm alarm = new AlarmCacheObject();
    alarmCacheRef.init();
    alarmCacheRef.put(1L, alarm);
    assertEquals(alarmCacheRef.get(1L), alarm);
  }
}
