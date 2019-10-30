package cern.c2mon.cache.actions.equipment;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.DbLoadable;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.shared.common.ConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Szymon Halastra
 */
@Slf4j
@Component
public class EquipmentLoadable implements DbLoadable<Equipment> {

  private C2monCache<Equipment> equipmentCacheRef;

  private C2monCache<ControlTag> controlTagCacheRef;

//  @Inject
//  public EquipmentLoadable(C2monCache<Long, Equipment> equipmentCacheRef, C2monCache<Long, ControlTag> controlTagCacheRef) {
//    this.equipmentCacheRef = equipmentCacheRef;
//    this.controlTagCacheRef = controlTagCacheRef;
//  }

//  @PostConstruct
//  public void init() {
//    for (Long key : equipmentCacheRef.getKeys()) {
//      doPostDbLoading(equipmentCacheRef.get(key));
//    }
//  }

  @Override
  public void doPostDbLoading(Equipment equipment) {
    Long processId = equipment.getProcessId();
    Long equipmentId = equipment.getId();

    Long aliveTagId = equipment.getAliveTagId();
    if (aliveTagId != null) {
      ControlTag aliveTagCopy = controlTagCacheRef.get(aliveTagId);
      if (aliveTagCopy != null) {
        setEquipmentId((ControlTagCacheObject) aliveTagCopy, equipmentId, processId);
      }
      else {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
                String.format("No Alive tag (%s) found for Equipment %s (#%d).", aliveTagId, equipment.getName(), equipment.getId()));
      }
    } // alive tag is not mandatory for an Equipment

    Long commFaultTagId = equipment.getCommFaultTagId();
    if (commFaultTagId != null) {
      ControlTag commFaultTagCopy = controlTagCacheRef.get(commFaultTagId);
      if (commFaultTagCopy != null) {
        setEquipmentId((ControlTagCacheObject) commFaultTagCopy, equipmentId, processId);
      }
      else {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
                String.format("No CommFault tag (%s) found for Equipment %s (#%d).", commFaultTagId, equipment.getName(), equipment.getId()));
      }
    }
    else {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
              String.format("No CommFault tag for Equipment %s (#%d) defined.", equipment.getName(), equipment.getId()));
    }

    Long statusTag = equipment.getStateTagId();
    if (statusTag != null) {
      ControlTag statusTagCopy = controlTagCacheRef.get(statusTag);
      if (statusTagCopy != null) {
        setEquipmentId((ControlTagCacheObject) statusTagCopy, equipmentId, processId);
      }
      else {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
                String.format("No Status tag (%s) found for Equipment %s (#%d).", statusTag, equipment.getName(), equipment.getId()));
      }
    }
    else {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
              String.format("No Status tag for Equipment %s (#%d) defined.", equipment.getName(), equipment.getId()));
    }
  }

  private void setEquipmentId(ControlTagCacheObject copy, Long equipmentId, Long processId) {
    String logMsg = String.format("Adding equipment id #%s to control tag #%s", equipmentId, copy.getId());
    log.trace(logMsg);
    copy.setEquipmentId(equipmentId);
    copy.setProcessId(processId);
    controlTagCacheRef.put(copy.getId(), copy);
  }
}
