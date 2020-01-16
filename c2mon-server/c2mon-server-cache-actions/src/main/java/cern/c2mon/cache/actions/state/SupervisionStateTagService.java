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
}
