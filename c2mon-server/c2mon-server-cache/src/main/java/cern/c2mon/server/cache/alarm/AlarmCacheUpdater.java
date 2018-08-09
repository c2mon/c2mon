package cern.c2mon.server.cache.alarm;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AlarmFacade;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCondition;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
final class AlarmCacheUpdater {
  
  @Autowired
  AlarmCache alarmCache;
  
  /**
   * Logic kept the same as in TIM1 (see {@link AlarmFacade}). The locking of
   * the objets is done in the public class. Notice, in this case the update()
   * method is putting the changes back into the cache.
   */
  Alarm update(final Alarm alarm, final Tag tag) {
    AlarmCacheObject alarmCacheObject = (AlarmCacheObject) alarm;
    // this time is then used in LASER publication as user timestamp
    Timestamp alarmTime = tag.getCacheTimestamp();
    // not possible to evaluate alarms with associated null tag; occurs during
    // normal operation
    // (may change in future is alarm state depends on quality f.eg.)
    if (tag.getValue() == null) {
      log.debug("Alarm update called with null Tag value - leaving Alarm status unchanged at " + alarm.getState());

      // change the alarm timestamp ifl the alarm has never been initialised
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

    //Evaluate oscillation
    final boolean isCurrentlyActive = alarm.isActive();
    if (isCurrentlyActive != alarmCacheObject.isLastActiveState()) {
      increaseOscillCounter(alarmCacheObject);
    } else {
      resetOscillCounter(alarmCacheObject);
    }
    alarmCacheObject.setLastActiveState(isCurrentlyActive);
    
   
  
    // Return immediately if the alarm new state is null
    if (newState == null) {
      log.error("update() : new state would be NULL -> no update.");
      throw new IllegalStateException("Alarm evaluated to null state!");
    }

    // Return if new state is TERMINATE and old state was also TERMINATE, return
    // original alarm (no need to save in cache)
    if (newState.equals(AlarmCondition.TERMINATE) && alarmCacheObject.getState().equals(AlarmCondition.TERMINATE)) {
      return alarm;
    }

    // Build up a prefix according to the tag value's validity and mode
    String additionalInfo = null;

    switch (tag.getMode()) {
    case DataTagConstants.MODE_MAINTENANCE:
      if (tag.isValid()) {
        additionalInfo = "[M]";
      } else {
        additionalInfo = "[M][?]";
      }
      break;
    case DataTagConstants.MODE_TEST:
      if (tag.isValid()) {
        additionalInfo = "[T]";
      } else {
        additionalInfo = "[T][?]";
      }
      break;
    default:
      if (tag.isValid()) {
        additionalInfo = "";
      } else {
        additionalInfo = "[?]";
      }
    }

    // Add another flag to the info if the value is simulated
    if (tag.isSimulated()) {
      additionalInfo = additionalInfo + "[SIM]";
    }

    // Default case: change the alarm's state
    // (1) if the alarm has never been initialised
    // (2) if tag is VALID and the alarm changes from ACTIVE->TERMINATE or
    // TERMIATE->ACTIVE
    if (alarmCacheObject.getTimestamp().equals(new Timestamp(0)) || (tag.isValid() && !alarmCacheObject.getState().equals(newState))) {

      log.trace(new StringBuffer("update(): alarm ").append(alarmCacheObject.getId()).append(" changed STATE to ").append(newState).toString());

      alarmCacheObject.setState(newState);
      alarmCacheObject.setTimestamp(alarmTime);
      alarmCacheObject.setInfo(additionalInfo);
      alarmCacheObject.notYetPublished();
      alarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
      return alarmCacheObject;
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
      log.trace(new StringBuffer("update(): alarm ").append(alarmCacheObject.getId()).append(" changed INFO to ").append(additionalInfo).toString());

      alarmCacheObject.setInfo(additionalInfo);
      alarmCacheObject.setTimestamp(alarmTime);
      if (!alarmCacheObject.getState().equals(AlarmCondition.TERMINATE)) {
        // We only send a notification about a property change
        // to the subscribed alarm listeners, if the alarm is active
        alarmCacheObject.notYetPublished();
        alarmCache.put(alarmCacheObject.getId(), alarmCacheObject);
      }
      return alarmCacheObject;
    }

    // In all other cases, the value of the alarm related to the DataTag has
    // not changed. No need to publish an alarm change.
    log.trace(new StringBuffer("update(): alarm ").append(alarmCacheObject.getId()).append(" has not changed.").toString());

    // no change so no listener notification in this case

    // this.alarmChange = CHANGE_NONE;
    return alarmCacheObject;
  }

private void increaseOscillCounter(AlarmCacheObject alarm) {
  alarm.setCounterFault(alarm.getCounterFault()+1);
  if (alarm.getCounterFault() == 1) {
    alarm.setFirstOscTS(System.currentTimeMillis());
  }
  alarm.setOscillating(checkOscillConditions(alarm));
}

private void resetOscillCounter(AlarmCacheObject alarm) {
  alarm.setCounterFault(0);
  alarm.setOscillating(false);
}

private boolean checkOscillConditions(AlarmCacheObject alarm) {
  return (alarm.getCounterFault() >= alarm.getOscNumbers() && (System.currentTimeMillis()-alarm.getFirstOscTS()) <= alarm.getTimeRange() * 1000);
}

}
