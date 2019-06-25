package cern.c2mon.cache.api.spi;

import cern.c2mon.server.common.alarm.AlarmCacheObject;

import java.util.List;

public interface C2monAlarmCacheQueryProvider {

  List<AlarmCacheObject> getOscillatingAlarms();

  long getLastOscillationCheck();

  void setLastOscillationCheck(long timestampMillis);
}
