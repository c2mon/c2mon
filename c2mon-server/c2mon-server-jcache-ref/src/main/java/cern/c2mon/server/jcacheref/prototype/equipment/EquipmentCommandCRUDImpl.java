package cern.c2mon.server.jcacheref.prototype.equipment;

import javax.cache.Cache;
import javax.cache.processor.EntryProcessorException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.jcacheref.prototype.common.SerializableEntryProcessor;

/**
 * @author Szymon Halastra
 */
@Slf4j
public class EquipmentCommandCRUDImpl implements EquipmentCommandCRUD {

  private Cache<Long, Equipment> equipmentTagCache;

  @Autowired
  public EquipmentCommandCRUDImpl(Cache<Long, Equipment> equipmentTagCache) {
    this.equipmentTagCache = equipmentTagCache;
  }

  @Override
  public void addCommandToEquipment(Long equipmentId, Long commandId) throws EntryProcessorException {
    log.trace("Adding Command to Equipment");
    equipmentTagCache.invoke(equipmentId, (SerializableEntryProcessor<Long, Equipment, Object[]>) (entry, arguments) -> {
      if (entry.exists()) {
        Equipment equipment = entry.getValue();
        equipment.getCommandTagIds().add(commandId);
        entry.setValue(equipment);
      }

      return null;
    });
  }

  @Override
  public void removeCommandFromEquipment(Long equipmentId, Long commandId) {
    log.trace("Removing Command from Equipment");
    equipmentTagCache.invoke(equipmentId, (SerializableEntryProcessor<Long, Equipment, Object[]>) (entry, arguments) -> {
      if (entry.exists()) {
        Equipment equipment = entry.getValue();
        equipment.getCommandTagIds().remove(commandId);
        entry.setValue(equipment);
      }

      return null;
    });
  }
}
