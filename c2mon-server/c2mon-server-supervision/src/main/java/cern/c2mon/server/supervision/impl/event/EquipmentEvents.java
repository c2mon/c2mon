package cern.c2mon.server.supervision.impl.event;

import cern.c2mon.cache.actions.equipment.EquipmentService;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.shared.common.supervision.SupervisionConstants;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.sql.Timestamp;

@Slf4j
public class EquipmentEvents extends SupervisionEventHandler<Equipment> {

  @Inject
  public EquipmentEvents(EquipmentService equipmentService) {
    super(Equipment.class, equipmentService);
  }

  /**
   * <UL>
   * <LI>either on reception of the equipment's alive tag
   * <LI>or on reception of the equipment's commfault tag (good value).
   * </UL>
   * The state tag of the equipment is updated (value RUNNING).
   *
   * Must be called within a block synchronized on the process object.
   *
   * @param pId id of the equipment concerned
   * @param pTimestamp time when the equipment was detected to be "up"
   * @param pMessage custom message with more information of why the equipment is believed to be up.
   */
  @Override
  public void onUp(Equipment supervised, Timestamp timestamp, String message) {
    // Try to obtain a copy of the state tag with its current value
    try {
      equipmentFacade.resume(pId, pTimestamp, pMessage);
      Equipment equipmentCopy = equipmentCache.getCopy(pId);
      //set state tag if necessary
      Long stateTagId = equipmentCopy.getStateTagId();
      Long commFaultId = equipmentCopy.getCommFaultTagId();
      controlTagCache.acquireWriteLockOnKey(stateTagId);
      try {
        ControlTag stateTag = controlTagCache.get(stateTagId);
        if (stateTag.getValue() == null || !stateTag.getValue().equals(SupervisionConstants.SupervisionStatus.RUNNING.toString()) || !stateTag.isValid()) {
          controlTagFacade.updateAndValidate(stateTagId, SupervisionConstants.SupervisionStatus.RUNNING.toString(), pMessage, pTimestamp);
        }
      } catch (CacheElementNotFoundException controlCacheEx) {
        log.error("Unable to locate equipment state tag in control tag cache (id is " + stateTagId + ")", controlCacheEx);
      } finally {
        controlTagCache.releaseWriteLockOnKey(stateTagId);
      }

      ControlTag commFaultTag = controlTagCache.getCopy(commFaultId);
      setCommFaultTag(commFaultId, true, commFaultTag.getValueDescription(), pTimestamp);
    } catch (CacheElementNotFoundException equipmentCacheEx) {
      log.error("Unable to locate equipment in cache (id is " + pId + ") - not taking any invalidation action.", equipmentCacheEx);
    }
  }
}
