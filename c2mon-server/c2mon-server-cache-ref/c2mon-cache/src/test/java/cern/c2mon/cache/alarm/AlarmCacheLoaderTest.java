package cern.c2mon.cache.alarm;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.junit.CachePopulationRule;
import cern.c2mon.server.cache.CacheModuleRef;
import cern.c2mon.server.cache.dbaccess.AlarmMapper;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.config.CommonModule;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Szymon Halastra
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        CommonModule.class,
//        CacheLoaderModuleRef.class,
        CacheModuleRef.class,
        CacheDbAccessModule.class,
        CachePopulationRule.class
})
public class AlarmCacheLoaderTest {

  @Rule
  @Autowired
  public CachePopulationRule cachePopulationRule;

  @Autowired
  private C2monCache<Long, Alarm> alarmCacheRef;

  @Autowired
  private AlarmMapper alarmMapper;

  @Test
  public void preloadCache() {
    assertNotNull("Alarm Cache should not be null", alarmCacheRef);

    List<Alarm> alarmList = alarmMapper.getAll();

    Set<Long> keySet = alarmList.stream().map(Alarm::getId).collect(Collectors.toSet());
    assertTrue("List of alarms should not be empty", alarmList.size() > 0);

    Map<Long, Alarm> alarms = alarmCacheRef.getAll(keySet);
    assertTrue("Alarm cache should have 4 elements", alarms.size() == 4);
  }
}
