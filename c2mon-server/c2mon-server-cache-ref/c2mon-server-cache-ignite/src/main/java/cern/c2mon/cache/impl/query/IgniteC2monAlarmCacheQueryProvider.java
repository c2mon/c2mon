package cern.c2mon.cache.impl.query;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.spi.C2monAlarmCacheQueryProvider;
import cern.c2mon.cache.impl.IgniteC2monCache;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmServiceTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Alexandros Papageorgiou
 * @author Brice Copy
 */
@Component
public class IgniteC2monAlarmCacheQueryProvider implements C2monAlarmCacheQueryProvider {

  private final C2monCache<AlarmServiceTimestamp> lastAccessCache;
  private static final long LAST_ACCESS_KEY = 0;

  private final IgniteC2monCache<Alarm> alarmCacheRef;

  @Autowired
  public IgniteC2monAlarmCacheQueryProvider(final C2monCache<AlarmServiceTimestamp> lastAccessCache, final C2monCache<Alarm> alarmCacheRef) {
    this.lastAccessCache = lastAccessCache;
    this.alarmCacheRef = (IgniteC2monCache<Alarm>) alarmCacheRef;
  }

  @Override
  public List<AlarmCacheObject> getOscillatingAlarms() {
    return filterAndCast(Alarm::isOscillating);
  }

  @Override
  public List<AlarmCacheObject> getActiveAlarms() {
    return filterAndCast(Alarm::isActive);
  }

  @Override
  public long getLastOscillationCheck() {
    return lastAccessCache.get(LAST_ACCESS_KEY).getTimeInMillis();
  }

  @Override
  public void setLastOscillationCheck(long timestampMillis) {
    lastAccessCache.put(LAST_ACCESS_KEY, new AlarmServiceTimestamp(LAST_ACCESS_KEY, timestampMillis));
  }

  protected List<AlarmCacheObject> filterAndCast(Function<Alarm,Boolean> query) {
    return alarmCacheRef.query(query)
      .stream().map(alarm -> (AlarmCacheObject) alarm).collect(Collectors.toList());
  }
}
