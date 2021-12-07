package cern.c2mon.server.cache.alarm.query;

import cern.c2mon.shared.client.alarm.AlarmQueryFilter;

import java.util.List;

public interface AlarmQuery {

    List<Long> findAlarm(AlarmQueryFilter query);
}
