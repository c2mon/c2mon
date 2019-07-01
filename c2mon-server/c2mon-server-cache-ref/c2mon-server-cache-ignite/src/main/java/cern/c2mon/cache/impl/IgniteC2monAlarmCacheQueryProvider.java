package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.spi.C2monAlarmCacheQueryProvider;
import cern.c2mon.server.cache.C2monCacheTyped;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Alexandros Papageorgiou
 * @author Brice Copy
 */
@Component
public class IgniteC2monAlarmCacheQueryProvider implements C2monAlarmCacheQueryProvider {

  // TODO enable this
  private final IgniteCache<String, Long> lastAccessCache = null;

  private final C2monCacheTyped<Alarm> alarmCacheRef;

  @Autowired
  public IgniteC2monAlarmCacheQueryProvider(/*final IgniteCache<String, Long> lastAccessCache,*/ final C2monCacheTyped<Alarm> alarmCacheRef) {
//    this.lastAccessCache = lastAccessCache;
    this.alarmCacheRef = alarmCacheRef;
  }

  @Override
  public List<AlarmCacheObject> getOscillatingAlarms() {
    return getAlarmsApplyingQuery((key, alarm) -> alarm.isOscillating());
  }

  @Override
  public List<AlarmCacheObject> getActiveAlarms() {
    return getAlarmsApplyingQuery((key, alarm) -> alarm.isActive());
  }

  @Override
  public long getLastOscillationCheck() {
    return lastAccessCache.get(this.getClass().getName());
  }

  @Override
  public void setLastOscillationCheck(long timestampMillis) {
    lastAccessCache.put(this.getClass().getName(), timestampMillis);
  }

  private List<AlarmCacheObject> getAlarmsApplyingQuery(IgniteBiPredicate<Long, Alarm> filter) {
    return ((IgniteC2monCache<Alarm>) alarmCacheRef).query(new ScanQuery<>(filter),
      // TODO Verify we want this cast - are we using AlarmCacheObjects downstream?
      longAlarmEntry -> (AlarmCacheObject) longAlarmEntry.getValue()
    ).getAll();
  }
}
