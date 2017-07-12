package cern.c2mon.server.jcacheref.prototype.equipment.integration;

import javax.cache.configuration.Factory;

/**
 * @author Szymon Halastra
 */
public class EquipmentCacheLoaderFactory implements Factory<EquipmentCacheLoader> {

  @Override
  public EquipmentCacheLoader create() {
    return new EquipmentCacheLoader();
  }
}
