package cern.c2mon.cache.alarm;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.AlarmMapper;
import cern.c2mon.server.common.alarm.Alarm;

import static org.junit.Assert.*;

/**
 * @author Szymon Halastra
 */
public class AlarmCacheLoaderTest extends AbstractCacheLoaderTest {

  @Autowired
  private C2monCache<Long, Alarm> alarmCacheRef;

  @Autowired
  private AlarmMapper alarmMapper;

  @Before
  public void prepare() {
    //TODO: calling init twice, should be fixed, first time is called as a PostConstruct and second time here directly(as waiting for populating DB)
    alarmCacheRef.init();
  }

  @Test
  @Ignore
  public void preloadCache() {
    assertNotNull("Alarm Cache should not be null", alarmCacheRef);

    List<Alarm> alarmList = alarmMapper.getAll();

    Set<Long> keySet = alarmList.stream().map(Alarm::getId).collect(Collectors.toSet());
    assertTrue("List of alarms should not be empty", alarmList.size() > 0);

    assertEquals("Size of cache and DB mapping should be equal", alarmList.size(), alarmCacheRef.getKeys().size());
    //compare all the objects from the cache and buffer
    for (Alarm alarm : alarmList) {
      //compare ids of associated datatags
      assertEquals("Cached Alarm should have the same name as Alarm in DB", alarm.getTagId(), alarmCacheRef.get(alarm.getId()).getTagId());
    }
  }
}
