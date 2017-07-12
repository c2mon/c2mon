package cern.c2mon.server.jcacheref.prototype.equipment.integration;

import java.util.Map;

import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;

import cern.c2mon.server.cache.loading.EquipmentDAO;
import cern.c2mon.server.common.equipment.Equipment;

/**
 * @author Szymon Halastra
 */
public class EquipmentCacheLoader implements CacheLoader<Long, Equipment> {

  EquipmentDAO equipmentDAO;

  @Override
  public Equipment load(Long key) throws CacheLoaderException {
    return equipmentDAO.getItem(key);
  }

  @Override
  public Map<Long, Equipment> loadAll(Iterable<? extends Long> keys) throws CacheLoaderException {
    return equipmentDAO.getAllAsMap();
  }
}
