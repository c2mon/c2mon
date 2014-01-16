package cern.c2mon.server.cache.dbaccess;

import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;

public interface EquipmentMapper extends LoaderMapper<Equipment>, BatchLoaderMapper<Equipment>, PersistenceMapper<Equipment> {
  void insertEquipment(EquipmentCacheObject equipmentCacheObject);
  void deleteEquipment(Long id);
  void updateEquipmentConfig(EquipmentCacheObject equipmentCacheObject);
}
