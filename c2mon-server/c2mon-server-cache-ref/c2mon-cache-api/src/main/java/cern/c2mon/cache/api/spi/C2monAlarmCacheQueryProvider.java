package cern.c2mon.cache.api.spi;

import cern.c2mon.server.common.alarm.AlarmCacheObject;

import java.util.List;

/**
 *
 * @author Alexandros Papageorgiou
 * @author Brice Copy
 */
public interface C2monAlarmCacheQueryProvider {

  List<AlarmCacheObject> getOscillatingAlarms();

  long getLastOscillationCheck();

  void setLastOscillationCheck(long timestampMillis);

  List<AlarmCacheObject> getActiveAlarms();
}
