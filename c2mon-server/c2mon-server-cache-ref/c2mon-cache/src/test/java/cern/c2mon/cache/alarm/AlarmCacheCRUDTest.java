package cern.c2mon.cache.alarm;

import cern.c2mon.cache.CacheCRUDTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import org.springframework.beans.factory.annotation.Autowired;

public class AlarmCacheCRUDTest extends CacheCRUDTest<Alarm> {

  @Autowired
  private C2monCache<Alarm> alarmCacheRef;

  @Override
  protected Long getExistingKey() {
    return 350000L;
  }

  @Override
  protected Alarm getSample() {
    return new AlarmCacheObject();
  }

  @Override
  protected C2monCache<Alarm> getCache() {
    return alarmCacheRef;
  }
}
