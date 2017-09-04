package cern.c2mon.server.cache.alarm;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.alarm.components.AlarmHandler;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.tag.Tag;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Service
public class AlarmService implements AlarmHandler {

  /**
   * Default max length for fault family
   */
  public static final int MAX_FAULT_FAMILY_LENGTH = 64;

  /**
   * Default max length for fault member
   */
  public static final int MAX_FAULT_MEMBER_LENGTH = 64;

  /**
   * Used to validate the alarm object at runtime configuration.
   */
  @Getter
  @Setter
  private int maxFaultFamily = MAX_FAULT_FAMILY_LENGTH;

  /**
   * Used to validate the alarm object at runtime configuration.
   */
  @Getter
  @Setter
  private int maxFaultMemberLength = MAX_FAULT_MEMBER_LENGTH;

  private final C2monCache<Long, Alarm> alarmCacheRef;

  private AlarmHandler alarmHandler;

  @Autowired
  public AlarmService(final C2monCache<Long, Alarm> alarmCacheRef, final AlarmHandler alarmHandler) {
    this.alarmCacheRef = alarmCacheRef;
    this.alarmHandler = alarmHandler;
  }

  @Override
  public void evaluateAlarm(Long alarmId) {
    this.alarmHandler.evaluateAlarm(alarmId);
  }

  @Override
  public Alarm update(Long alarmId, Tag tag) {
    return this.alarmHandler.update(alarmId, tag);
  }

  @Override
  public String getTopicForAlarm(Alarm alarm) {
    return alarmHandler.getTopicForAlarm(alarm);
  }
}
