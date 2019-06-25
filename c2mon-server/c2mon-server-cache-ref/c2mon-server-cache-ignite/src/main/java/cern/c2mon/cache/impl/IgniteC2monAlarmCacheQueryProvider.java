package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.spi.C2monAlarmCacheQueryProvider;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import org.apache.ignite.IgniteCache;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class IgniteC2monAlarmCacheQueryProvider implements C2monAlarmCacheQueryProvider {

  @Autowired
  private final IgniteCache<String, Long> lastAccessCache;

  @Autowired
  private final C2monCache<Long, Alarm> alarmCacheRef;

  @Override
  public List<AlarmCacheObject> getOscillatingAlarms() {
    return alarmCacheRef;
  }

  @Override
  public long getLastOscillationCheck() {
    return lastAccessCache.get(this.getClass().getName());
  }

  @Override
  public void setLastOscillationCheck(long timestampMillis) {
    lastAccessCache.put(this.getClass().getName(), timestampMillis);
  }
}
