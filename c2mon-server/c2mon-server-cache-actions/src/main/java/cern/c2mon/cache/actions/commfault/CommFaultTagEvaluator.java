package cern.c2mon.cache.actions.commfault;

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

  public static SupervisionStatus inferSupervisionStatus(@NonNull ControlTag controlTag) {
    if (controlTag.getValue() == null)
      throw new IllegalArgumentException("Cannot evaluate null value of CommFaultTag " + controlTag.getName());

    return controlTag.getValue().equals(controlTag.getFaultValue()) ? DOWN : RUNNING;
  }
}
