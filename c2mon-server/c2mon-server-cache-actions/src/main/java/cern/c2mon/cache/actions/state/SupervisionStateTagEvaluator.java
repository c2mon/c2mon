package cern.c2mon.cache.actions.state;

import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SupervisionStateTagEvaluator {

  private SupervisionStateTagEvaluator() {

  }

  public static boolean controlTagCanUpdateState(Long stateTagId, @NonNull ControlTag controlTag) {
    if (stateTagId == null) {
      log.warn("Null state tag Id provided - no state tag will be updated.");
      return false;
    }
    if (controlTag.getValue() == null) {
      log.warn("Tag " + controlTag.getName() +" has no value - no state tag will be updated.");
      return false;
    }
    return true;
  }

  public static boolean hasIdDiscrepancy(@NonNull ControlTag controlTag, @NonNull SupervisionStateTag stateTag) {
    if (stateTag.getCommFaultTagId() != controlTag.getId()) {
      log.warn(
        "CommFaultTag cache object" + controlTag.getName() +"(" + controlTag.getId() +")" +
          " has StateTag "+ stateTag.getName() + "(" +stateTag.getId() +") listed," +
          " but StateTag cache object has a different CommFaultTag id listed (" + stateTag.getCommFaultTagId()
      );
      return true;
    }
    return false;
  }
}
