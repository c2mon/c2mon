package cern.c2mon.cache.actions.state;

import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static cern.c2mon.server.common.util.KotlinAPIs.apply;
import static cern.c2mon.shared.common.supervision.SupervisionStatus.*;

@Slf4j
public class SupervisionStateTagEvaluator {

  private SupervisionStateTagEvaluator() {

  }

  /**
   * Verifies that the given control tag, can update SupervisionState tag cache
   *
   * @param stateTagId possible null, the id of the SupervisionState to update
   * @param controlTag
   * @return
   */
  public static boolean controlTagCanUpdateState(Long stateTagId, @NonNull ControlTag controlTag) {
    if (stateTagId == null) {
      log.warn("Null state tag Id provided - no state tag will be updated.");
      return false;
    }
    if (controlTag.getValue() == null) {
      log.warn("Tag {} has no value - no state tag will be updated.", controlTag.getName());
      return false;
    }
    return true;
  }

  public static boolean matchesAnyTagId(@NonNull ControlTag controlTag, @NonNull SupervisionStateTag stateTag) {
    return apply(
      matchesAnyTagId(controlTag.getId(), stateTag.getCommFaultTagId(), stateTag.getAliveTagId()),
      anyMatched -> {
        if (!anyMatched)
          logIdMatchingError(controlTag, stateTag);
      }
    );
  }

  private static boolean matchesAnyTagId(long controlTagId, Long stateCommFaultId, Long stateAliveId) {
    return stateCommFaultId != null && stateCommFaultId == controlTagId
      || stateAliveId != null && stateAliveId == controlTagId;
  }

  private static void logIdMatchingError(ControlTag controlTag,SupervisionStateTag stateTag) {
    log.error(
      "ControlTag cache object {} #{} has StateTag {} #{} listed, but StateTag cache object does not know this id." +
        "In StateTag, AliveTag Id #{} - CommFaultTag id listed: #{}",
      controlTag.getName(), controlTag.getId(), stateTag.getName(), stateTag.getId(),
      stateTag.getAliveTagId(), stateTag.getCommFaultTagId());
  }

  /**
   * Returns true if the object is either running or in
   * the start up phase. And false if either DOWN or STOPPED, or
   * if the status is UNCERTAIN.
   *
   * @return true if it is running (or starting up)
   */
  public static boolean isRunning(@NonNull SupervisionStateTag supervisionStateTag) {
    // Assigning it here keeps us safe from concurrent modifications
    SupervisionStatus status = supervisionStateTag.getSupervisionStatus();
    return status.equals(STARTUP)
      || status.equals(RUNNING)
      || status.equals(RUNNING_LOCAL);
  }

  /**
   * Returns true only if the object is in UNCERTAIN status.
   *
   * @return true if the status is uncertain
   */
  public static boolean isUncertain(@NonNull SupervisionStateTag supervisionStateTag) {
    return supervisionStateTag.getSupervisionStatus().equals(UNCERTAIN);
  }

  public static boolean hasNoEvents(@NonNull SupervisionStateTag supervisionStateTag) {
    return supervisionStateTag.getSupervisionStatus() == DOWN && supervisionStateTag.getStatusTime() == null;
  }
}
