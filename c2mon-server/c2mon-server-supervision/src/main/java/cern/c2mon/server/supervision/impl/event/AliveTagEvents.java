package cern.c2mon.server.supervision.impl.event;

import cern.c2mon.cache.actions.alive.AliveTagService;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Slf4j
@Named
@Singleton
public class AliveTagEvents {

  private final AliveTagService aliveTimerService;

  @Inject
  public AliveTagEvents(AliveTagService aliveTimerService) {
    this.aliveTimerService = aliveTimerService;
  }

  /**
   * Calls the ProcessDown, EquipmentDown or SubEquipmentDown methods depending
   * on the type of alive that has expired.
   */
  public void onAliveTimerExpiration(final Long aliveTimerId) {
    if (aliveTimerId == null) {
      log.warn("onAliveTimerExpiration(null) called - ignoring the call.");
      return;
    }

    if (!aliveTimerService.isRegisteredAliveTimer(aliveTimerId)) {
      log.error("AliveTimer received does not exist in cache - unable to take any action on alive reception.");
      return;
    }

    AliveTag aliveTimer = aliveTimerService.getCache().get(aliveTimerId);

    log.debug("Alive of " + aliveTimer.getSupervisedEntity().toString() + " " + aliveTimer.getSupervisedName()
      + " (alive tag: " + aliveTimer.getId() + ") has expired.");

    SupervisionEntity supervisedEntity = aliveTimer.getSupervisedEntity();

    // TODO (Alex) Consider try-catching this?
    SupervisionEventHandler.getEventHandlers().get(supervisedEntity).onAliveTimerDown(aliveTimer.getSupervisedId());

//    try {
//      final Long processId = processFacade.getProcessIdFromAlive(aliveTimer.getId());
//      if (aliveTimer.isProcessAliveType()) {
//        final Long processId = processService.getProcessIdFromAlive(aliveTimerId);
//        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//        processEvents.onDown(processId, timestamp, msg);
//      } else if (aliveTimer.isEquipmentAliveType()) {
//        Long equipmentId = aliveTimer.getRelatedId();
//        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//        equipmentEvents.onDown(equipmentId, timestamp, msg);
//
//         Manually set the CommFaultTag (TIMS-972)
//        ControlTag commFaultTag = controlTagCache.getCopy(equipmentCache.getCopy(equipmentId).getCommFaultTagId());
//        setCommFaultTag(commFaultTag.getId(), false, commFaultTag.getValueDescription(), timestamp);
//
    // Bring down all SubEquipments
//        for (Long subEquipmentId : equipmentCache.get(equipmentId).getSubEquipmentIds()) {
//          String message = "Alive timer for parent Equipment expired: " + msg;
//          subEquipmentEvents.onDown(subEquipmentId, timestamp, message);
//
//          commFaultTag = controlTagCache.getCopy(subEquipmentCache.getCopy(subEquipmentId).getCommFaultTagId());
//          setCommFaultTag(commFaultTag.getId(), false, commFaultTag.getValueDescription(), timestamp);
//        }
//      } else {
//        Long subEquipmentId = aliveTimer.getRelatedId();
//        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//        subEquipmentEvents.onDown(subEquipmentId, timestamp, msg);
//
    // Manually set the CommFaultTag (TIMS-972)
//        ControlTag commFaultTag = controlTagCache.getCopy(subEquipmentCache.getCopy(subEquipmentId).getCommFaultTagId());
//        setCommFaultTag(commFaultTag.getId(), false, commFaultTag.getValueDescription(), timestamp);
//      }
//    } catch (CacheElementNotFoundException cacheEx) {
//      log.error("Unable to locate a required element within the cache on Alive Timer expiration.", cacheEx);
//    } catch (NullPointerException nullEx) {
//      log.error("NullPointer exception caught on Alive Timer expiration.", nullEx);
//    } catch (IllegalArgumentException argEx) {
//      log.error("IllegalArgument exception caught on Aliver Timer expiration", argEx);
//    }

  }
}
