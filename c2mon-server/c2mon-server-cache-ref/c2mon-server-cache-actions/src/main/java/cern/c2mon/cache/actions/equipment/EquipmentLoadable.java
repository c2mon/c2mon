package cern.c2mon.cache.actions.equipment;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.DbLoadable;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.status.SupervisionStateTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Szymon Halastra
 */
@Slf4j
@Component
public class EquipmentLoadable implements DbLoadable<Equipment> {

  private final C2monCache<Equipment> equipmentCacheRef;
  private final C2monCache<AliveTag> aliveTagCache;
  private final C2monCache<CommFaultTag> commFaultTagCache;
  private final C2monCache<SupervisionStateTag> stateTagCache;

  @Inject
  public EquipmentLoadable(C2monCache<Equipment> equipmentCacheRef,
                           C2monCache<AliveTag> aliveTagCache,
                           C2monCache<CommFaultTag> commFaultTagCache,
                           C2monCache<SupervisionStateTag> stateTagCache) {
    this.equipmentCacheRef = equipmentCacheRef;
    this.aliveTagCache = aliveTagCache;
    this.commFaultTagCache = commFaultTagCache;
    this.stateTagCache = stateTagCache;
  }

//  @PostConstruct
//  public void init() {
//    for (Long key : equipmentCacheRef.getKeys()) {
//      doPostDbLoading(equipmentCacheRef.get(key));
//    }
//  }

  @Override
  public void doPostDbLoading(Equipment equipment) {
//    Long processId = equipment.getProcessId();
//    Long equipmentId = equipment.getId();
//
//    Long aliveTagId = equipment.getAliveTagId();
//    if (aliveTagId != null) {
//      AliveTag aliveTagCopy = aliveTagCache.get(aliveTagId);
//      if (aliveTagCopy != null) {
//        setEquipmentId((aliveTagCopy) aliveTagCopy, equipmentId, processId);
//      }
//      else {
//        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
//                String.format("No Alive tag (%s) found for Equipment %s (#%d).", aliveTagId, equipment.getName(), equipment.getId()));
//      }
//    } // alive tag is not mandatory for an Equipment
//
//    Long commFaultTagId = equipment.getCommFaultTagId();
//    if (commFaultTagId != null) {
//      ControlTag commFaultTagCopy = aliveTagCache.get(commFaultTagId);
//      if (commFaultTagCopy != null) {
//        setEquipmentId((ControlTagCacheObject) commFaultTagCopy, equipmentId, processId);
//      }
//      else {
//        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
//                String.format("No CommFault tag (%s) found for Equipment %s (#%d).", commFaultTagId, equipment.getName(), equipment.getId()));
//      }
//    }
//    else {
//      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
//              String.format("No CommFault tag for Equipment %s (#%d) defined.", equipment.getName(), equipment.getId()));
//    }
//
//    Long statusTag = equipment.getStateTagId();
//    if (statusTag != null) {
//      ControlTag statusTagCopy = aliveTagCache.get(statusTag);
//      if (statusTagCopy != null) {
//        setEquipmentId((ControlTagCacheObject) statusTagCopy, equipmentId, processId);
//      }
//      else {
//        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
//                String.format("No Status tag (%s) found for Equipment %s (#%d).", statusTag, equipment.getName(), equipment.getId()));
//      }
//    }
//    else {
//      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
//              String.format("No Status tag for Equipment %s (#%d) defined.", equipment.getName(), equipment.getId()));
//    }
  }

//  private void setEquipmentId(ControlTagCacheObject copy, Long equipmentId, Long processId) {
//    String logMsg = String.format("Adding equipment id #%s to control tag #%s", equipmentId, copy.getId());
//    log.trace(logMsg);
//    copy.setEquipmentId(equipmentId);
//    copy.setProcessId(processId);
//    aliveTagCache.put(copy.getId(), copy);
//  }
}
