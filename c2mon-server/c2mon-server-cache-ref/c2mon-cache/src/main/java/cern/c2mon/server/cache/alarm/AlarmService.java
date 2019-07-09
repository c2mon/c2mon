package cern.c2mon.server.cache.alarm;

import cern.c2mon.cache.api.listener.CacheSupervisionListener;
import cern.c2mon.server.common.alarm.AlarmCacheUpdater;
import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.alarm.TagWithAlarmsImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.tag.Tag;

import java.util.*;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou
 * @author Brice Copy
 */
//TODO: change name for more proper

@Slf4j
@Service
public class AlarmService implements AlarmAggregator, CacheSupervisionListener<Tag> {

  private C2monCache<Alarm> alarmCacheRef;

  private C2monCache<Tag> tagCacheRef;

  private Observable alarmUpdateObservable = new Observable();

  private AlarmCacheUpdater alarmCacheUpdater;

  @Autowired
  public AlarmService(final C2monCache<Alarm> alarmCacheRef, /*final C2monCache<Long, Tag> tagCacheRef,*/ final AlarmCacheUpdater alarmCacheUpdater) {
    this.alarmCacheRef = alarmCacheRef;
//    this.tagCacheRef = tagCacheRef;
    this.alarmCacheUpdater = alarmCacheUpdater;
  }

  public Alarm update(final Long alarmId, final Tag tag) {
    return alarmCacheRef.executeTransaction( () -> {
      Alarm alarm = alarmCacheRef.get(alarmId);
      // Notice, in this case the update() method is putting the changes back into the cache
      return alarmCacheUpdater.update(alarm, tag);
    }).orElse(null);
  }

  public void evaluateAlarm(Long alarmId) {
    alarmCacheRef.executeTransaction( () -> {
      Alarm alarm = alarmCacheRef.get(alarmId);
      Tag tag = tagCacheRef.get(alarm.getTagId());
      return update(alarmId, tag);
    });
  }

  public List<Alarm> evaluateAlarms(final Tag tag) {
    List<Alarm> linkedAlarms = new ArrayList<>();
    alarmCacheRef.executeTransaction( () -> {
      for (Long alarmId : tag.getAlarmIds()) {
        try {
          linkedAlarms.add(update(alarmId, tag));
        }
        catch (Exception e) {
          log.error("Exception caught when attempting to evaluate alarm ID " + alarmId + "  for tag " + tag.getId() + " - publishing to the client with no attached alarms.", e);
        }
      }
      return null;
    });
    return linkedAlarms;
  }

  /**
   * Accesses and locks Tag in cache, fetches associated
   * alarms (since Alarm evaluation is on the same thread as
   * the Tag cache update, these correspond to the Tag value
   * and cannot be modified during this method).
   */
  public TagWithAlarms getTagWithAlarms(Long id) {
    return alarmCacheRef.executeTransaction( () -> {
      Tag tag = tagCacheRef.get(id);
      Collection<Alarm> alarms = new LinkedList<>();
      for (Long alarmId : tag.getAlarmIds()) {
        alarms.add(alarmCacheRef.get(alarmId));
      }
      return new TagWithAlarmsImpl(tag, alarms);
    }).get();
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


  @Override
  public void registerForTagUpdates(Observer aggregatorObserver) {
    alarmUpdateObservable.addObserver(aggregatorObserver);
  }

  @Override
  public void onSupervisionChange(Tag tag) {
    log.trace("Evaluating alarm for tag " + tag.getId() + " due to supervision status notification.");

    evaluateAlarms(tag);
  }

  //TODO: move and modify code from AbstractTagFacade connected with Alarms
}
