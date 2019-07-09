package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.spi.C2monAlarmCacheQueryProvider;
import cern.c2mon.server.cache.alarm.AlarmServiceTimestamp;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
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

  private final C2monCache<AlarmServiceTimestamp> lastAccessCache;

  private final C2monCache<Alarm> alarmCacheRef;

  @Autowired
  public IgniteC2monAlarmCacheQueryProvider(final C2monCache<AlarmServiceTimestamp> lastAccessCache, final C2monCache<Alarm> alarmCacheRef) {
    this.lastAccessCache = lastAccessCache;
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
    return lastAccessCache.get((long) this.getClass().getName().hashCode()).getTimeInMillis();
  }

  @Override
  public void setLastOscillationCheck(long timestampMillis) {
    Long key = (long) this.getClass().getName().hashCode();
    lastAccessCache.put(key, new AlarmServiceTimestamp(key, timestampMillis));
  }

  private List<AlarmCacheObject> getAlarmsApplyingQuery(IgniteBiPredicate<Long, Alarm> filter) {
    return ((IgniteC2monCacheBase<Alarm>) alarmCacheRef).query(new ScanQuery<>(filter),
      // TODO Verify we want this cast - are we using AlarmCacheObjects downstream?
      longAlarmEntry -> (AlarmCacheObject) longAlarmEntry.getValue()
    ).getAll();
  }
}
