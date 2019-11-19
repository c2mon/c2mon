package cern.c2mon.server.supervision.impl.event;

import cern.c2mon.cache.actions.subequipment.SubEquipmentService;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.shared.common.supervision.SupervisionConstants;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.sql.Timestamp;

@Slf4j
public class SubEquipmentEvents extends SupervisionEventHandler<SubEquipment> {

  @Inject
  public SubEquipmentEvents(SubEquipmentService subEquipmentService) {
    super(subEquipmentService);
  }

  /**
   * This method is called when the subequipment's alivetag or the
   * subequipment's commfault tag (good value) is received. In both cases we
   * assume the equipment is running and we modify its state tag accordingly.
   *
   * @param pId         Identifer of the subequipment for which the alivetag/commfaulttag
   *                    was received
   * @param timestamp   Timestamp indicating when it was received
   * @param pStateTagId The id of the state tag that indicates the subequipment state
   * @param message     Message explaining which is the cause for the subequipment to be
   *                    considered as being up.
   */
  @Override
  public void onUp(SubEquipment supervised, Timestamp timestamp, String message) {
    logMethodEntry("subEquipmentUp", supervised.getId(), timestamp, message);

    try {
      // Try to obtain a copy of the state tag with its current value
      subEquipmentFacade.resume(pId, pTimestamp, pMessage);
      SubEquipment subEquipmentCopy = subEquipmentCache.getCopy(pId);
      Long stateTagId = subEquipmentCopy.getStateTagId();
      Long commFaultId = subEquipmentCopy.getCommFaultTagId();
      controlTagCache.acquireWriteLockOnKey(stateTagId);
      try {
        ControlTag stateTag = controlTagCache.get(stateTagId);
        if (stateTag.getValue() == null || !stateTag.getValue().equals(SupervisionConstants.SupervisionStatus.RUNNING.toString()) || !stateTag.isValid()) {
          controlTagFacade.updateAndValidate(stateTagId, SupervisionConstants.SupervisionStatus.RUNNING.toString(), pMessage, pTimestamp);
        }
      } catch (CacheElementNotFoundException controlCacheEx) {
        log.error("Unable to locate subequipment state tag in control tag cache (id is " + stateTagId + ")", controlCacheEx);
      } finally {
        controlTagCache.releaseWriteLockOnKey(stateTagId);
      }

      ControlTag commFaultTag = controlTagCache.getCopy(commFaultId);
      setCommFaultTag(commFaultId, true, commFaultTag.getValueDescription(), pTimestamp);
    } catch (CacheElementNotFoundException subEquipmentCacheEx) {
      log.error("Unable to locate subequipment in cache (id is " + pId + ")", subEquipmentCacheEx);
    }
  }
}
