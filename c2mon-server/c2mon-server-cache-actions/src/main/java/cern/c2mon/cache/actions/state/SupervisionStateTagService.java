package cern.c2mon.cache.actions.state;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

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
}
