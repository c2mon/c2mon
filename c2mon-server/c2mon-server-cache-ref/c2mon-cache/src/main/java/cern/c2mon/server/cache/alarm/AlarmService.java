package cern.c2mon.server.cache.alarm;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alarm.*;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.DataTagConstants;

/**
 * @author Szymon Halastra
 */
//TODO: change name for more proper

@Slf4j
@Service
public class AlarmService {

  private C2monCache<Long, Alarm> alarmCacheRef;

  private C2monCache<Long, Tag> tagCacheRef;

  @Autowired
  public AlarmService(final C2monCache<Long, Alarm> alarmCacheRef, final C2monCache<Long, Tag> tagCacheRef) {
    this.alarmCacheRef = alarmCacheRef;
    this.tagCacheRef = tagCacheRef;
  }

  public Alarm update(final Long alarmId, final Tag tag) {
    alarmCacheRef.lockOnKey(alarmId);
    try {
      Alarm alarm = alarmCacheRef.get(alarmId);
      // Notice, in this case the update() method is putting the changes back into the cache
      return update(alarm, tag);
    } finally {
      alarmCacheRef.unlockOnKey(alarmId);
    }
  }

  public void evaluateAlarm(Long alarmId) {
    alarmCacheRef.lockOnKey(alarmId);
    try {
      Alarm alarm = alarmCacheRef.get(alarmId);
      Tag tag = tagCacheRef.get(alarm.getTagId());
      update(alarm, tag);
    } finally {
      alarmCacheRef.unlockOnKey(alarmId);
    }
  }

  public List<Alarm> evaluateAlarms(final Tag tag) {
    List<Alarm> linkedAlarms = new ArrayList<>();
    tagCacheRef.lockOnKey(tag.getId());
    try {
      for (Long alarmId : tag.getAlarmIds()) {
        linkedAlarms.add(update(alarmId, tag));
      }
    } finally {
      tagCacheRef.unlockOnKey(tag.getId());
    }
    return linkedAlarms;
  }

  /**
   * Accesses and locks Tag in cache, fetches associated
   * alarms (since Alarm evaluation is on the same thread as
   * the Tag cache update, these correspond to the Tag value
   * and cannot be modified during this method).
   */
  public TagWithAlarms getTagWithAlarms(Long id) {
    tagCacheRef.lockOnKey(id);
    try {
      Tag tag = tagCacheRef.get(id);
      Collection<Alarm> alarms = new LinkedList<>();
      for (Long alarmId : tag.getAlarmIds()) {
        alarms.add(alarmCacheRef.get(alarmId));
      }
      return new TagWithAlarmsImpl(tag, alarms);
    } finally {
      tagCacheRef.unlockOnKey(id);
    }
  }

  /**
   * Logic kept the same as in TIM1.
   * The locking of the objets is done in the public class.
   * Notice, in this case the update() method is putting the changes back into the cache.
   */
  private Alarm update(final Alarm alarm, final Tag tag) {
    AlarmCacheObject alarmCacheObject = (AlarmCacheObject) alarm;
    // Reset previous change state
    alarmCacheObject.setAlarmChangeState(AlarmCacheObject.AlarmChangeState.CHANGE_NONE);
    // this time is then used in LASER publication as user timestamp
    Timestamp alarmTime = tag.getCacheTimestamp();
    // not possible to evaluate alarms with associated null tag; occurs during normal operation
    // (may change in future is alarm state depends on quality f.eg.)
    if (tag.getValue() == null) {
      log.debug("Alarm update called with null Tag value - leaving Alarm status unchanged at " + alarm.getState());

      // change the alarm timestamp if the alarm has never been initialised
      if (alarmCacheObject.getTimestamp().equals(new Timestamp(0))) {
        alarmCacheObject.setTimestamp(alarmTime);
      }
      return alarmCacheObject;
    }

    if (!tag.getDataTagQuality().isInitialised()) {
      log.debug("Alarm update called with uninitialised Tag - leaving Alarm status unchanged.");
      return alarm;
    }

    // timestamp should never be null
    if (tag.getTimestamp() == null) {
      log.warn("update() : tag value or timestamp null -> no update");
      throw new IllegalArgumentException("update method called on Alarm facade with either null tag value or null tag timestamp.");
    }

    // Compute the alarm state corresponding to the new tag value
    String newState = alarmCacheObject.getCondition().evaluateState(tag.getValue());

    // Return immediately if the alarm new state is null
    if (newState == null) {
      log.error("update() : new state would be NULL -> no update.");
      throw new IllegalStateException("Alarm evaluated to null state!");
    }

    // Return if new state is TERMINATE and old state was also TERMINATE, return original alarm (no need to save in cache)
    if (newState.equals(AlarmCondition.TERMINATE) && alarmCacheObject.getState().equals(AlarmCondition.TERMINATE)) {
      return alarm;
    }

    // Build up a prefix according to the tag value's validity and mode
    String additionalInfo = buildPrefixForAlarm(tag);

    // Default case: change the alarm's state
    // (1) if the alarm has never been initialised
    // (2) if tag is VALID and the alarm changes from ACTIVE->TERMINATE or TERMIATE->ACTIVE
    if (alarmCacheObject.getTimestamp().equals(new Timestamp(0))
            || (tag.isValid() && !alarmCacheObject.getState().equals(newState))) {
      return changeAlarmState(alarmCacheObject, alarmTime, newState, additionalInfo);
    }

    // Even if the alarm state itself hasn't change, the additional
    // information
    // related to the alarm (e.g. whether the alarm is valid or invalid)
    // might
    // have changed
    if (alarmCacheObject.getInfo() == null) {
      alarmCacheObject.setInfo("");
    }
    if (!alarmCacheObject.getInfo().equals(additionalInfo)) {
      return updateInfoForAlarm(alarmCacheObject, alarmTime, additionalInfo);
    }

    // In all other cases, the value of the alarm related to the DataTag has
    // not changed. No need to publish an alarm change.
    if (log.isTraceEnabled()) {
      log.trace(new StringBuffer("update(): alarm ").append(alarmCacheObject.getId())
              .append(" has not changed.").toString());
    }
    //no change so no listener notification in this case

    //this.alarmChange = CHANGE_NONE;
    return alarmCacheObject;
  }

  /**
   * Derives a valid JMS topic name for distributing the alarm's values to
   * clients (currently the same for all alarms, so returns a constant).
   *
   * @param alarm the alarm for which the topic should be provided
   *
   * @return a valid JMS topic name for the alarm
   */
  public String getTopicForAlarm(final Alarm alarm) {

    /*
     * StringBuffer str = new StringBuffer("tim.alarm.");
     * str.append(pFaultFamily); str.append("."); str.append(pFaultMember);
     * str.append("."); str.append(pFaultCode); String topic = str.toString();
     * topic = topic.replace('$', 'X'); topic = topic.replace('*', 'X'); topic =
     * topic.replace('#', 'X'); return topic;
     */

    // we decided to distribute all alarms on the same topic in order to reduce
    // the number of topics for SonicMQ, the client has to make the decision if
    // the received alarm is useful for it, otherwise it will discard the alarm
    return "tim.alarm";
  }

  @NotNull
  private Alarm updateInfoForAlarm(AlarmCacheObject alarmCacheObject, Timestamp alarmTime, String additionalInfo) {
    if (log.isTraceEnabled()) {
      log.trace(new StringBuffer("update(): alarm ").append(alarmCacheObject.getId())
              .append(" changed INFO to ").append(additionalInfo).toString());
    }
    alarmCacheObject.setInfo(additionalInfo);
    alarmCacheObject.setAlarmChangeState(AlarmCacheObject.AlarmChangeState.CHANGE_PROPERTIES);
    alarmCacheObject.setTimestamp(alarmTime);
    if (!alarmCacheObject.getState().equals(AlarmCondition.TERMINATE)) {
      // We only send a notification about a property change
      // to the subscribed alarm listeners, if the alarm is active
      alarmCacheObject.notYetPublished();
      alarmCacheRef.put(alarmCacheObject.getId(), alarmCacheObject);
    }
    return alarmCacheObject;
  }

  @NotNull
  private Alarm changeAlarmState(AlarmCacheObject alarmCacheObject, Timestamp alarmTime, String newState, String additionalInfo) {
    if (log.isTraceEnabled()) {
      log.trace(new StringBuffer("update(): alarm ").append(alarmCacheObject.getId())
              .append(" changed STATE to ").append(newState).toString());
    }
    alarmCacheObject.setState(newState);
    alarmCacheObject.setTimestamp(alarmTime);
    alarmCacheObject.setInfo(additionalInfo);
    alarmCacheObject.setAlarmChangeState(AlarmCacheObject.AlarmChangeState.CHANGE_STATE);
    alarmCacheObject.notYetPublished();
    alarmCacheRef.put(alarmCacheObject.getId(), alarmCacheObject);
    return alarmCacheObject;
  }

  @NotNull
  private String buildPrefixForAlarm(Tag tag) {
    String additionalInfo = null;

    switch (tag.getMode()) {
      case DataTagConstants.MODE_MAINTENANCE:
        if (tag.isValid()) {
          additionalInfo = "[M]";
        }
        else {
          additionalInfo = "[M][?]";
        }
        break;
      case DataTagConstants.MODE_TEST:
        if (tag.isValid()) {
          additionalInfo = "[T]";
        }
        else {
          additionalInfo = "[T][?]";
        }
        break;
      default:
        if (tag.isValid()) {
          additionalInfo = "";
        }
        else {
          additionalInfo = "[?]";
        }
    }

    // Add another flag to the info if the value is simulated
    if (tag.isSimulated()) {
      additionalInfo = additionalInfo + "[SIM]";
    }
    return additionalInfo;
  }

  //TODO: move code from AlarmUpdateHandler here, remove other class
  //TODO: move and modify code from AbstractTagFacade connected with Alarms
}
