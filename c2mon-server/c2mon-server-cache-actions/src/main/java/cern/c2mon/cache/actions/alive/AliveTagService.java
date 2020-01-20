package cern.c2mon.cache.actions.alive;

import cern.c2mon.cache.actions.AbstractBooleanControlTagService;
import cern.c2mon.cache.actions.supervision.SupervisedCacheService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.server.common.thread.Event;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Manages operations on {@link AliveTag}s
 *
 * @author Szymon Halastra, Alexandros Papageorgiou Koufidis
 * @see AliveTag
 */
@Slf4j
@Service
public class AliveTagService extends AbstractBooleanControlTagService<AliveTag> implements SupervisedCacheService<AliveTag> {

  @Inject
  public AliveTagService(C2monCache<AliveTag> aliveTimerCacheRef) {
    super(aliveTimerCacheRef, new AliveTagCacheFlow());
  }

  public boolean isRegisteredAliveTimer(final Long id) {
    return cache.containsKey(id);
  }

  /**
   * Same as {@link AliveTagService#start(long, long)}, but will start the object
   * regardless of previous state (active or not)
   */
  public void startOrUpdateTimestamp(long aliveTimerId, long timestamp) throws NullPointerException {
    setTagAsActive(aliveTimerId, true, timestamp);
  }

  /**
   * Check whether this alive timer has expired. Adds an additional time buffer
   * as some clocks may be slightly out of sync.
   *
   * @return true if the alive timer is active and it has not been updated since
   * at least "aliveInterval" milliseconds.
   */
  public boolean hasExpired(final Long aliveTimerId) {
    AliveTag aliveTimer = cache.get(aliveTimerId);
    return (System.currentTimeMillis() - aliveTimer.getLastUpdate() > aliveTimer.getAliveInterval() + aliveTimer.getAliveInterval() / 3);
  }

  /**
   * Will set all previously inactive {@link AliveTag}s as active
   * <p>
   * Timestamps will not be affected on previously active {@code AliveTimer}s
   */
  public void startAllInactiveTimers() {
    log.debug("Starting all alive timers in the cache.");
    filterAndSetActive(true);
  }

  /**
   * Will set all previously inactive {@link AliveTag}s as inactive (stopped)
   * <p>
   * Timestamps will not be affected on previously inactive {@code AliveTimer}s
   */
  public void stopAllActiveTimers() {
    log.debug("Stopping all alive timers in the cache.");
    filterAndSetActive(false);
  }

  /**
   * Stops and removes this alive by alive id. Should only be
   * used when it is no longer referenced by a supervised object
   * (for instance on reconfiguration error recovery).
   *
   * @param aliveId id of the alive
   */
  public void removeAliveTimer(long aliveId) {
    stop(aliveId, System.currentTimeMillis());
    cache.remove(aliveId);
  }

  public void createAliveTimerFor(Supervised supervised) {
    AliveTag aliveTimer = new AliveTag(supervised.getAliveTagId(), supervised.getId(), supervised.getName(),
      supervised.getSupervisionEntity(), null, supervised.getStateTagId(), supervised.getAliveInterval());
    cache.put(aliveTimer.getId(), aliveTimer);
  }

  private void filterAndSetActive(boolean active) {
    try {
      for (AliveTag aliveTimer : cache.query(aliveTimer -> aliveTimer.getValue() != active)) {
        log.debug("Attempting to set alive timer " + aliveTimer.getId() + " and dependent alive timers to " + active);
        aliveTimer.setValue(active);
        aliveTimer.setLastUpdate(System.currentTimeMillis());
        cache.put(aliveTimer.getId(), aliveTimer);
      }
    } catch (Exception e) {
      log.error("Unable to retrieve list of alive timers from cache when attempting to set new active status to " + active, e);
    }
  }

  public final String generateSourceXML(final AliveTag aliveTag) {
    StringBuilder str = new StringBuilder("    <DataTag id=\"");
    str.append(aliveTag.getId());
    str.append("\" name=\"");
    str.append(aliveTag.getSupervisedName());
    str.append("\" control=\"true\">\n");

    if (aliveTag.getAddress() != null) {
      str.append(aliveTag.getAddress().toConfigXML());
    }

    str.append("    </DataTag>\n");
    return str.toString();
  }

  /**
   * Updates the tag object if the value is not filtered out. Contains the logic on when a
   * AliveTagCacheObject should be updated with new values and when not (in particular
   * timestamp restrictions).
   *
   * <p>Also notifies the listeners if an update was performed.
   *
   * <p>Notice the tag is not put back in the cache here.
   *
   * @param sourceDataTagValue the source value received from the DAQ
   * @return true if an update was performed (i.e. the value was not filtered out)
   */
  public Event<Boolean> updateFromSource(final SourceDataTagValue sourceDataTagValue) {
    return cache.executeTransaction(() -> {
      final AliveTag aliveTag = cache.get(sourceDataTagValue.getId());

      if (sourceDataTagValue == null) {
        log.error("Attempting to update a dataTag with a null source value - ignoring update.");
        return new Event<>(aliveTag.getCacheTimestamp().getTime(), false);
      }

      // TODO (Alex) This does not properly account for potential filterout as part of cache.put. Should it?
//      Event<Boolean> returnValue = updateFromSource(aliveTag, sourceDataTagValue);

      if (sourceDataTagValue.isValid()) {
        cache.putQuiet(sourceDataTagValue.getId(), aliveTag);
      } else {
        cache.put(sourceDataTagValue.getId(), aliveTag);
      }

      // TODO (Alex) Implement this based on the contents of sourceDataTagValue used
      return new Event<>(System.currentTimeMillis(), false);
    });
  }

  /**
   * Updates the AliveTag based on new supervised properties, e.g after a reconfiguration
   *
   * @param supervised
   */
  public void updateBasedOnSupervised(Supervised supervised) {
    // TODO (Alex)
  }

  @Override
  protected void compareAndSetNewValues(AliveTag controlTag, boolean active, long timestamp) {
    if (controlTag.getValue() != active || timestamp >= controlTag.getTimestamp().getTime()) {
      controlTag.setValue(active);
      controlTag.setLastUpdate(timestamp);
    }
  }
}
