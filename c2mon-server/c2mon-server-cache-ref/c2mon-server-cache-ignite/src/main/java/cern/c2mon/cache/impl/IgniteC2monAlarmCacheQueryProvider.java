package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.spi.C2monAlarmCacheQueryProvider;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.ScanQuery;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class IgniteC2monAlarmCacheQueryProvider implements C2monAlarmCacheQueryProvider {

  @Autowired
  private final IgniteCache<String, Long> lastAccessCache;

  @Autowired
  private final IgniteCache<Long, Alarm> alarmCacheRef;

  @Override
  public List<AlarmCacheObject> getOscillatingAlarms() {
    return alarmCacheRef.query(new ScanQuery<Long, Alarm>(
      (key, alarm) -> alarm.isOscillating()),
      // TODO Verify we want this cast - are we using AlarmCacheObjects downstream?
      longAlarmEntry -> (AlarmCacheObject) longAlarmEntry.getValue()
    ).getAll();
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
