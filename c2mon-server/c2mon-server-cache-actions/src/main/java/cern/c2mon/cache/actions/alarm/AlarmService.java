package cern.c2mon.cache.actions.alarm;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.actions.oscillation.OscillationUpdater;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.config.collections.TagCacheCollection;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.CacheEvent;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static cern.c2mon.cache.actions.alarm.AlarmEvaluator.createAdditionalInfoString;

/**
 * Service containing the required methods to interact with Alarms in the cache
 * @author Szymon Halastra, Alexandros Papageorgiou, Brice Copy
 */
@Slf4j
@Named
@Singleton
public class AlarmService extends AbstractCacheServiceImpl<Alarm> implements AlarmAggregator{

    private List<AlarmAggregatorListener> alarmUpdateObservable = new ArrayList<>();

    private TagCacheCollection tagCacheCollection;

    private OscillationUpdater oscillationUpdater;


    /**
     *
     * @param cache the alarm cache
     * @param tagCacheCollection aggregates [Control,Data,Rule] tag caches
     * @param oscillationUpdater contains the logic for evaluating, if an alarm is oscillating
     */
    @Inject
    public AlarmService(final C2monCache<Alarm> cache, final TagCacheCollection tagCacheCollection,
                        final OscillationUpdater oscillationUpdater) {
        super(cache, new AlarmCacheFlow());
        // TODO (Alex) We probably want to increase the number of threads in the CacheListenerManager here
        this.tagCacheCollection = tagCacheCollection;
        this.oscillationUpdater = oscillationUpdater;
    }

    /**
     * Initialization method that registers the tag cache collection listeners
     */
    @PostConstruct
    public void init() {
        tagCacheCollection.registerListener(this::supervisionChangeListener, CacheEvent.SUPERVISION_CHANGE);
        tagCacheCollection.registerListener(this::updateAcceptedListener, CacheEvent.UPDATE_ACCEPTED);
    }

    /**
     * Atomically get an alarm, get the connected tag, evaluate them,
     * then put any changes back into the cache if needed
     *
     * @param alarmId the id of an alarm in cache
     * @return the alarm with the given ID, after the update has happened
     * @throws CacheElementNotFoundException if no alarm exists with the given id
     */
    public Alarm evaluateAlarm(Long alarmId) {
        return cache.executeTransaction(() -> {
            Alarm alarm = cache.get(alarmId);
            Tag tag = tagCacheCollection.get(alarm.getTagId());
            update((AlarmCacheObject) alarm, tag, true);
            return alarm;
        });
    }

    /**
     * Atomically evaluate all alarms connected to this tag,
     * then put any changes back into the cache if needed
     *
     * @param tag the tag to be evaluated
     * @return A list of successfully evaluated alarms
     */
    public List<Alarm> evaluateAlarms(final Tag tag) {
        return cache.executeTransaction(() -> {
            Set<Long> keys = new HashSet<>(tag.getAlarmIds());

            return cache.getAll(keys).values().stream().map(
                    alarm -> {
                        try {
                            update((AlarmCacheObject) alarm, tag, true);
                            return alarm;
                        } catch (Exception e) {
                            cache.getCacheListenerManager().notifyListenersOf(CacheEvent.UPDATE_FAILED, alarm);
                            log.error("Exception caught when attempting to evaluate alarm ID " + alarm.getId() + "  for tag " + tag.getId() + " - publishing to the client with no attached alarms.", e);
                            return null;
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toList());
        });
    }


    /**
     * If the alarm started (or restarted now), the timestamp will be set to now.
     * Otherwise, it will use the timestamp of the previous alarm
     * <p>
     * If the alarm was oscillating, listeners will NOT be notified
     * <p>
     * The alarm will only be put in the cache if changes would be made to the current one
     *
     * @param alarmCacheObject Alarm object held in the cache
     * @param tag the tag interface
     * @param updateOscillation oscillation status
     * @return alarm updated
     */
    public boolean update(final AlarmCacheObject alarmCacheObject, final Tag tag, boolean updateOscillation) {
        if (updateOscillation) {
            oscillationUpdater.updateOscillationStatus(alarmCacheObject, tag.getTimestamp().getTime());
        }

        boolean isOscillating = alarmCacheObject.isOscillating();

        boolean alarmShouldBeUpdated = AlarmEvaluator.alarmShouldBeUpdated(alarmCacheObject, tag);

        if (alarmShouldBeUpdated) {
            mutateAlarmUsingTag(alarmCacheObject, tag);

            if (isOscillating) {
                cache.putQuiet(alarmCacheObject.getId(), alarmCacheObject);
            } else {
                cache.put(alarmCacheObject.getId(), alarmCacheObject);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Retrieves the tag and all the corresponding alarms
     * @param tagId the Tag id
     * @return tag with associated alarms
     */
    public TagWithAlarms getTagWithAlarmsAtomically(Long tagId) {
        return cache.executeTransaction(() -> {
            Tag tag = tagCacheCollection.get(tagId);
            Set<Long> alarms = new HashSet<>(tag.getAlarmIds());
            return new TagWithAlarms<>(tag, cache.getAll(alarms).values());
        });
    }

  @Override
  public void registerForTagUpdates(AlarmAggregatorListener aggregatorObserver) {
    alarmUpdateObservable.add(aggregatorObserver);
  }

  /**
   * Resets the oscillation flag to false and computes the alarm state corresponding to the actual tag value.
   * It will also update the Alarm cache and if there was a change, notify the listeners.
   *
   * @param alarmCacheObject The current alarm object in the cache
   * @param tag              The tag update
   */
  public void stopOscillatingAndUpdate(final AlarmCacheObject alarmCacheObject, final Tag tag) {
    alarmCacheObject.setOscillating(false);
    update(alarmCacheObject, tag, false);
  }

    private void mutateAlarmUsingTag(final AlarmCacheObject alarmCacheObject, Tag tag) {
        alarmCacheObject.setInfo(createAdditionalInfoString(alarmCacheObject, tag));

        boolean newState = alarmCacheObject.getCondition().evaluateState(tag.getValue());
        if (newState && !alarmCacheObject.isActive()) {
            // TODO Potentially set this to previous, during preInsertValidate, using the previous Alarm in cache?
            alarmCacheObject.setTriggerTimestamp(new Timestamp(System.currentTimeMillis()));
        }

        alarmCacheObject.setSourceTimestamp(tag.getTimestamp());

        alarmCacheObject.setInternalActive(newState);

        if (alarmCacheObject.isOscillating()) {
            // When oscillating we force the alarm to *active*
            // (only the *internalActive* property reflects the true status)
            alarmCacheObject.setActive(true);
        } else {
            alarmCacheObject.setActive(newState);
        }
        log.trace("Alarm #{} changed STATE to {}", alarmCacheObject.getId(), alarmCacheObject.isActive());
    }

    /**
     * Notify the listeners of a tag update with associated alarms.
     */
    private void notifyListeners(final Tag tag, final List<Alarm> alarms) {
        for (AlarmAggregatorListener listener : alarmUpdateObservable) {
            listener.notifyOnUpdate(tag.clone(), alarms);
        }
    }

    private void supervisionChangeListener(Tag tag) {
        log.trace("Evaluating alarm for tag " + tag.getId() + " due to supervision status notification.");
        evaluateAlarms(tag);
    }

    private void updateAcceptedListener(Tag tag) {
        log.trace("Evaluating alarm for tag " + tag.getId() + " due to update notification.");
        List<Alarm> alarmList = evaluateAlarms(tag);
        notifyListeners(tag.clone(), alarmList);
    }
}
