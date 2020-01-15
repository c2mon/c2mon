package cern.c2mon.cache.actions.commfault;

import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static cern.c2mon.shared.common.supervision.SupervisionStatus.DOWN;
import static cern.c2mon.shared.common.supervision.SupervisionStatus.RUNNING;

@Slf4j
public class CommFaultTagEvaluator {

  private CommFaultTagEvaluator() {

  }

  static boolean aliveTagCanUpdateCommFault(@NonNull AliveTag aliveTag) {
    if (aliveTag.getCommFaultTagId() == null) {
      log.warn("Tag " + aliveTag.getName() + " has no CommFaultTag ID - no CommFaultTag will be updated.");
      return false;
    }
    if (aliveTag.getValue() == null) {
      log.warn("Tag " + aliveTag.getName() + " has no value - no CommFaultTag will be updated.");
      return false;
    }
    return true;
  }

  public static SupervisionStatus inferSupervisionStatus(@NonNull ControlTag controlTag) {
    if (controlTag.getValue() == null)
      throw new IllegalArgumentException("Cannot evaluate null value of CommFaultTag " + controlTag.getName());

    return controlTag.getValue().equals(controlTag.getFaultValue()) ? DOWN : RUNNING;
  }
}
