package cern.c2mon.cache.actions.state;

import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import lombok.NonNull;

import java.sql.Timestamp;

public final class SupervisionStateTagController {

  private SupervisionStateTagController() {

  }

  public static SupervisionEvent createSupervisionEvent(@NonNull SupervisionStateTag stateTag) {
    Timestamp supervisionTime;
    String supervisionMessage;

    supervisionTime = stateTag.getStatusTime() != null
      ? stateTag.getStatusTime()
      : new Timestamp(System.currentTimeMillis());

    supervisionMessage = stateTag.getStatusDescription() != null
      ? stateTag.getStatusDescription()
      : stateTag.getSupervisedEntity() + " " + stateTag.getName() + " is " + stateTag.getSupervisionStatus();

    return new SupervisionEventImpl(stateTag.getSupervisedEntity(), stateTag.getSupervisedId(), stateTag.getName(),
      stateTag.getSupervisionStatus(), supervisionTime, supervisionMessage);
  }
}
