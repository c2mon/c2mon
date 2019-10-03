package cern.c2mon.cache.alarm;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.AlarmMapper;
import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou
 */
public class AlarmCacheLoaderTest extends AbstractCacheLoaderTest<Alarm> {

  @Autowired
  protected AlarmMapper alarmMapper;
  @Autowired
  private C2monCache<Alarm> alarmCacheRef;

  @Override
  protected void customCompare(List<Alarm> mapperList, Map<Long, Alarm> cacheList) {
    for (Alarm alarm : mapperList) {
      //compare ids of associated datatags
      assertEquals("Cached Alarm should have the same name as Alarm in DB", alarm.getDataTagId(), cache.get(alarm.getId()).getDataTagId());
    }
  }

  @Override
  protected LoaderMapper<Alarm> getMapper() {
    return alarmMapper;
  }

  @Override
  protected Long getExistingKey() {
    return 350000L;
  }

  @Override
  protected C2monCache<Alarm> initCache() {
    return alarmCacheRef;
  }

  @Override
  protected Alarm getSample() {
    return new AlarmCacheObject();
  }
}
