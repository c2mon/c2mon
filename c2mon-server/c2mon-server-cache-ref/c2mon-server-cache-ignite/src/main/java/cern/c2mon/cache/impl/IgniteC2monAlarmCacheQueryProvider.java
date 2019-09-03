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
import java.util.stream.Collectors;

/**
 * @author Alexandros Papageorgiou
 * @author Brice Copy
 */
@Component
public class IgniteC2monAlarmCacheQueryProvider extends IgniteCacheQueryProvider implements C2monAlarmCacheQueryProvider {

  private final C2monCache<AlarmServiceTimestamp> lastAccessCache;

  private final IgniteC2monCacheBase<Alarm> alarmCacheRef;

  @Autowired
  public IgniteC2monAlarmCacheQueryProvider(final C2monCache<AlarmServiceTimestamp> lastAccessCache, final C2monCache<Alarm> alarmCacheRef) {
    this.lastAccessCache = lastAccessCache;
    this.alarmCacheRef = (IgniteC2monCacheBase<Alarm>) alarmCacheRef;
  }

  @Override
  public List<AlarmCacheObject> getOscillatingAlarms() {
    return filterAndCast(alarmCacheRef,(key, alarm) -> alarm.isOscillating());
  }

  @Override
  public List<AlarmCacheObject> getActiveAlarms() {
    return filterAndCast(alarmCacheRef, (key, alarm) -> alarm.isActive());
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

  protected List<AlarmCacheObject> filterAndCast(IgniteC2monCacheBase<Alarm> cache, IgniteBiPredicate<Long, Alarm> filter) {
    return super.filter(cache, filter).stream().map(alarm -> (AlarmCacheObject) alarm).collect(Collectors.toList());
  }
}
