package cern.c2mon.server.jcacheref.prototype.equipment;

import java.io.Serializable;

import javax.cache.Cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.common.equipment.Equipment;

/**
 * @author Szymon Halastra
 */

@Service
public class EquipmentCacheService {

  private Cache<Long, Equipment> equipmentTagCache;

  @Autowired
  public EquipmentCacheService(Cache<Long, Equipment> equipmentTagCache) {
    this.equipmentTagCache = equipmentTagCache;
  }

  public void addCommandToEquipment(Long equipmentId, Long commandId) {
    equipmentTagCache.invoke(equipmentId, (entry, arguments) -> {
      if (entry.exists()) {
        Equipment equipment = entry.getValue();
        equipment.getCommandTagIds().add(commandId);
      }

      return null;
    });
  }
}
