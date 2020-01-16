package cern.c2mon.cache.actions.state;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import static cern.c2mon.cache.actions.commfault.CommFaultTagEvaluator.inferSupervisionStatus;
import static cern.c2mon.cache.actions.state.SupervisionStateTagEvaluator.controlTagCanUpdateState;
import static cern.c2mon.cache.actions.state.SupervisionStateTagEvaluator.hasIdDiscrepancy;
import static cern.c2mon.shared.common.supervision.SupervisionStatus.DOWN;
import static cern.c2mon.shared.common.supervision.SupervisionStatus.RUNNING;

@Slf4j
@Service
public class SupervisionStateTagService extends AbstractCacheServiceImpl<SupervisionStateTag> {

  @Inject
  public SupervisionStateTagService(C2monCache<SupervisionStateTag> cache) {
    super(cache, new SupervisionStateTagCacheFlow());
  }

  /**
   * Updates the value of a SupervisionStateTag based on a {@link ControlTag}
   * <p>
   * If stateTagId is null, or {@link ControlTag#getValue()} is null, no action is taken
   *
   * @param controlTag the Control tag to update state based on - must be NonNull!
   * @throws CacheElementNotFoundException if the CommFaultTag.stateTagId does not exist in cache
   */
  public void updateBasedOnControl(Long stateTagId, @NonNull ControlTag controlTag) {
    if (!controlTagCanUpdateState(stateTagId, controlTag))
      return;

    cache.compute(stateTagId, stateTag -> {
      if (hasIdDiscrepancy(controlTag, stateTag)) {
        // TODO (Alex) Should this throw? Or just fix?
      }

      if (controlTag.getTimestamp().after(stateTag.getStatusTime())) {
        stateTag.setSupervision(
          inferSupervisionStatus(controlTag),
          controlTag.getValueDescription(),
          controlTag.getTimestamp()
        );
      }
    });
  }

  /**
   * Notifies all registered listeners of the current supervision status
   * of the cache element with given id. The timestamp of all events is refreshed
   * to the current time. Is used for example on server startup
   * to "refresh" all listeners in cache of server failure.
   *
   * @param id id of the cache element
   */
  public void refresh(long id) {
    cache.executeTransaction(() -> {
      SupervisionStateTag supervisionStateTag = cache.get(id);
      // TODO (Alex) EventTimestamp?
      supervisionStateTag.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
      cache.put(supervisionStateTag.getId(), supervisionStateTag);
    });
  }

  /**
   * Convenience accessor for {@link SupervisionStateTagEvaluator#isRunning(SupervisionStateTag)}
   * with providing only the StateTag id
   *
   * @param id the id of an existing StateTag
   * @return true if the StateTag with the given id exists and is in a running state
   * @throws CacheElementNotFoundException if the state tag was not found
   */
  public boolean isRunning(long id) {
    return SupervisionStateTagEvaluator.isRunning(cache.get(id));
  }

  /**
   * Returns the last supervision event that occured
   * for this state tag cache object (or a generated
   * one if none have yet occured for this cache object).
   *
   * @param stateTagId id of the StateTag object
   * @return the last supervision event
   * @throws CacheElementNotFoundException if the StateTag with the given id does not exist
   */
  public SupervisionEvent getSupervisionEvent(long stateTagId){
    return SupervisionStateTagController.createSupervisionEvent(cache.get(stateTagId));
  }

  /**
   * Returns the last supervision event that occured
   * for this supervised cache object (or a generated
   * one if none have yet occured for this cache object).
   *
   * @param supervisedId id of the supervised object
   * @param supervisionEntity type of the supervised object
   * @return the last supervision event
   * @throws CacheElementNotFoundException if the supervised or the state tag doesn't exist
   */
  public SupervisionEvent getSupervisionEventBySupervisedId(long supervisedId, SupervisionEntity supervisionEntity){
    // TODO (Alex)
    return null;
  }

  public List<SupervisionEvent> getAllSupervisionEvents() {
    return cache.getKeys().parallelStream()
      .map(key -> SupervisionStateTagController.createSupervisionEvent(cache.get(key)))
      .collect(Collectors.toList());
  }

  /**
   * Find the {@code SupervisionStateTag} object with {@code stateTagId} in the cache
   * and if it is stopped (not active), then do
   *
   * <ul>
   *   <li>{@code SupervisionStateTag#setValue(true)}
   *   <li>{@code SupervisionStateTag#setLastUpdate(now)}
   *   <li>Reinsert into cache
   * </ul>
   * <p>
   * The timestamp will not be updated, unless there is a change.
   * The cache object will not be reinserted, unless there is a change.
   *
   * @param stateTagId the stateTag id for the object to be force started
   */
  public void start(long stateTagId, long timestamp) throws NullPointerException {
    setStateTagAsActive(stateTagId, true, timestamp);
  }

  /**
   * Find the {@code SupervisionStateTag} object with {@code stateTagId} in the cache
   * and if it is started (active), then do
   *
   * <ul>
   *   <li>{@code SupervisionStateTag#setValue(false)}
   *   <li>{@code SupervisionStateTag#setLastUpdate(now)}
   *   <li>Reinsert into cache
   * </ul>
   * <p>
   * The timestamp will not be updated, unless there is a change.
   * The cache object will not be reinserted, unless there is a change.
   *
   * @param stateTagId the stateTag id for the object to be force started
   */
  public void stop(long stateTagId, long timestamp) throws NullPointerException {
    setStateTagAsActive(stateTagId, false, timestamp);
  }

  public void resume(long stateTagId, long timestamp) throws NullPointerException {
    start(stateTagId, timestamp);
  }

  public void suspend(long stateTagId, long timestamp) throws NullPointerException {
    stop(stateTagId, timestamp);
  }

  private void setStateTagAsActive(long stateTagId, boolean active, long timestamp) {
    log.debug("Attempting to set State tag " + stateTagId + " and dependent alive timers to " + active);

    if (!cache.containsKey(stateTagId)) {
      log.error("Cannot locate the StateTag in the cache (Id is " + stateTagId + ") - unable to stop it.");
      return;
    }

    try {
      cache.compute(stateTagId, stateTag -> {
        if (stateTag.getValue() != active) {
          stateTag.setValue(active);
          stateTag.setSupervision(active ? RUNNING : DOWN, "", new Timestamp(timestamp));
          stateTag.setSourceTimestamp(new Timestamp(timestamp)); // TODO (Alex) EventTimeStamp? StatusTime?
        }
      });
    } catch (Exception e) {
      log.error("Unable to stop the alive timer " + stateTagId, e);
    }
  }
}
