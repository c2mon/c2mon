package cern.c2mon.server.jcacheref.prototype.equipment.integration;

import javax.cache.configuration.Factory;

/**
 * @author Szymon Halastra
 */
public class EquipmentCacheWriterFactory implements Factory<EquipmentCacheWriter> {

  @Override
  public EquipmentCacheWriter create() {
    return new EquipmentCacheWriter();
  }
}
